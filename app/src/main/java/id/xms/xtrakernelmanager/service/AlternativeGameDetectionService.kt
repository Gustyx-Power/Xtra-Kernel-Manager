package id.xms.xtrakernelmanager.service

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import kotlinx.coroutines.*
import org.json.JSONArray

/**
 * Alternative game detection service using UsageStats API instead of Accessibility Service
 * This approach is less likely to be detected by Play Protect
 */
class AlternativeGameDetectionService : Service() {

    companion object {
        private const val TAG = "AltGameDetection"
        private const val NOTIFICATION_ID = 2002
        private const val POLLING_INTERVAL = 2000L // 2 seconds
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var usageStatsManager: UsageStatsManager
    private var enabledGamePackages: Set<String> = emptySet()
    private var isMonitoring = false
    private var lastForegroundApp: String = ""
    
    private val handler = Handler(Looper.getMainLooper())
    private var monitoringRunnable: Runnable? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Alternative Game Detection Service Created")
        
        preferencesManager = PreferencesManager(applicationContext)
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        
        // Create notification channel
        createNotificationChannel()
        
        // Load initial configuration
        serviceScope.launch {
            loadGameList()
        }
        
        // For Android 14+ (API 34+), specify the foreground service type
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            try {
                startForeground(NOTIFICATION_ID, createNotification(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } catch (e: Exception) {
                Log.w(TAG, "Could not start with specific service type, using default", e)
                startForeground(NOTIFICATION_ID, createNotification())
            }
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        
        startMonitoring()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        Log.d(TAG, "Starting app monitoring")
        
        monitoringRunnable = Runnable {
            if (isMonitoring) {
                checkForegroundApp()
                handler.postDelayed(monitoringRunnable!!, POLLING_INTERVAL)
            }
        }
        
        handler.post(monitoringRunnable!!)
    }

    private fun stopMonitoring() {
        isMonitoring = false
        monitoringRunnable?.let { handler.removeCallbacks(it) }
        Log.d(TAG, "Stopped app monitoring")
    }

    private fun checkForegroundApp() {
        try {
            val currentTime = System.currentTimeMillis()
            val usageEvents = usageStatsManager.queryEvents(
                currentTime - 5000, 
                currentTime
            )

            var foregroundApp: String? = null
            val event = UsageEvents.Event()
            
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    foregroundApp = event.packageName
                }
            }

            foregroundApp?.let { packageName ->
                if (packageName != lastForegroundApp) {
                    lastForegroundApp = packageName
                    handleAppChange(packageName)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking foreground app", e)
        }
    }

    private fun handleAppChange(packageName: String) {
        Log.d(TAG, "App changed to: $packageName")

        // Check if it's a game app
        if (enabledGamePackages.contains(packageName)) {
            Log.d(TAG, "Game detected: $packageName")
            startGameOverlay()
        } else {
            // Other apps - stop overlay
            stopGameOverlay()
        }
    }

    private fun startGameOverlay() {
        try {
            val intent = Intent(applicationContext, GameOverlayService::class.java)
            startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start overlay service", e)
        }
    }

    private fun stopGameOverlay() {
        try {
            val intent = Intent(applicationContext, GameOverlayService::class.java)
            stopService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop overlay service", e)
        }
    }

    private suspend fun loadGameList() {
        try {
            preferencesManager.getGameApps().collect { json ->
                enabledGamePackages = parseEnabledGamePackages(json)
                Log.d(TAG, "Updated game list: ${enabledGamePackages.size} games")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading game list", e)
        }
    }

    private fun parseEnabledGamePackages(json: String): Set<String> {
        return try {
            val jsonArray = JSONArray(json)
            val packages = mutableSetOf<String>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.opt(i)
                when (item) {
                    is String -> packages.add(item)
                    else -> {
                        val obj = item as? org.json.JSONObject
                        val packageName = obj?.optString("packageName")
                        val enabled = obj?.optBoolean("enabled", false) ?: false
                        if (!packageName.isNullOrEmpty() && enabled) {
                            packages.add(packageName)
                        }
                    }
                }
            }
            packages
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing game packages", e)
            emptySet()
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, "game_monitor_channel")
        .setContentTitle("XKM Alternative Detection")
        .setContentText("Monitoring for game apps")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build()

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "game_monitor_channel",
                "Game Monitor Service",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(android.app.NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        stopMonitoring()
        serviceScope.cancel()
    }
}