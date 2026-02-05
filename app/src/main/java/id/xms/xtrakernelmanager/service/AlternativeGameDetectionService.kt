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
    private var bankingModeEnabled: Boolean = false
    private var isMonitoring = false
    private var lastForegroundApp: String = ""
    
    private val handler = Handler(Looper.getMainLooper())
    private var monitoringRunnable: Runnable? = null

    // Banking apps whitelist (same as GameMonitorService)
    private val bankingPackages = setOf(
        // Indonesian Banks
        "com.bni.mobile", "com.bri.brimo", "com.bca", "com.bca.mybca",
        "com.mandiri.mandirionline", "com.bankmandiri.livin", "com.bankmandiri.livin.merchant",
        "id.co.bankbkemobile.digitalbank", // SeaBank
        "com.cimbniaga.mobile.cimbgo", "com.danamon.dbmobile", "com.btpn.wow",
        // E-Wallets
        "com.gojek.app", "com.grab.passenger", "ovo.id", "com.dana.id",
        "com.telkom.mwallet", "com.shopee.id", "com.tokopedia.tkpd"
    )

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
            loadBankingMode()
        }
        
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        startMonitoring()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification() = NotificationCompat.Builder(this, "game_monitor_channel")
        .setContentTitle("XKM Game Detection")
        .setContentText("Monitoring apps using Usage Stats")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .setSilent(true)
        .build()

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "game_monitor_channel",
                "Game Monitor Service",
                android.app.NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors game apps for automatic overlay activation using usage statistics"
                setShowBadge(false)
                setSound(null, null)
            }
            val notificationManager = getSystemService(android.app.NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        Log.d(TAG, "Starting app monitoring")
        
        monitoringRunnable = object : Runnable {
            override fun run() {
                if (isMonitoring) {
                    checkForegroundApp()
                    handler.postDelayed(this, POLLING_INTERVAL)
                }
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
            // Skip if banking mode is enabled
            if (bankingModeEnabled) {
                return
            }

            val currentTime = System.currentTimeMillis()
            val usageEvents = usageStatsManager.queryEvents(
                currentTime - 5000, // Last 5 seconds
                currentTime
            )

            var foregroundApp: String? = null
            val event = UsageEvents.Event()
            
            // Find the most recent foreground app
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

        // Priority 1: Banking apps
        if (bankingPackages.contains(packageName)) {
            Log.d(TAG, "Banking app detected: $packageName")
            stopGameOverlay()
            return
        }

        // Priority 2: Game apps
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

    private suspend fun loadBankingMode() {
        try {
            preferencesManager.getBankingModeEnabled().collect { enabled ->
                bankingModeEnabled = enabled
                Log.d(TAG, "Banking mode: ${if (enabled) "ENABLED" else "DISABLED"}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading banking mode", e)
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
                    is org.json.JSONObject -> {
                        val enabled = item.optBoolean("enabled", true)
                        if (enabled) {
                            packages.add(item.optString("packageName"))
                        }
                    }
                }
            }
            packages
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse game apps", e)
            emptySet()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }
}