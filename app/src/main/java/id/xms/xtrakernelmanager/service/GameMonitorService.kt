package id.xms.xtrakernelmanager.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.JSONArray

/**
 * Service that monitors foreground apps and shows game overlay
 * when a game from the user's game list is opened.
 */
class GameMonitorService : Service() {

    companion object {
        private const val TAG = "GameMonitorService"
        private const val CHANNEL_ID = "game_monitor_channel"
        private const val NOTIFICATION_ID = 3001
        private const val POLL_INTERVAL = 1500L // Check every 1.5 seconds
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var preferencesManager: PreferencesManager
    
    private var lastForegroundPackage: String? = null
    private var pollingJob: Job? = null
    private var enabledGamePackages: Set<String> = emptySet()
    private var isGameOverlayActive = false
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate called")
        preferencesManager = PreferencesManager(applicationContext)
        
        createNotificationChannel()
        // SDK 34+ requires foreground service type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        
        startPolling()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called")
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Game Monitor",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Monitors games for overlay"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Game Monitor")
            .setContentText("Monitoring for games...")
            .setSmallIcon(R.drawable.ic_tile_performance)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = serviceScope.launch {
            while (isActive) {
                try {
                    // Load enabled game packages
                    val gameAppsJson = preferencesManager.getGameApps().first()
                    enabledGamePackages = parseEnabledGamePackages(gameAppsJson)
                    
                    if (enabledGamePackages.isEmpty()) {
                        // No enabled games, stop overlay if active
                        if (isGameOverlayActive) {
                            stopGameOverlay()
                        }
                        delay(POLL_INTERVAL)
                        continue
                    }
                    
                    // Check foreground app
                    val foregroundPackage = getForegroundPackage()
                    
                    if (foregroundPackage != null && foregroundPackage != lastForegroundPackage) {
                        Log.d(TAG, "Foreground app changed to: $foregroundPackage")
                        lastForegroundPackage = foregroundPackage
                        
                        // Check if this is an enabled game
                        if (enabledGamePackages.contains(foregroundPackage)) {
                            if (!isGameOverlayActive) {
                                Log.d(TAG, "Enabled game detected: $foregroundPackage, starting overlay")
                                startGameOverlay()
                            }
                        } else {
                            if (isGameOverlayActive) {
                                Log.d(TAG, "Leaving game, stopping overlay")
                                stopGameOverlay()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in polling: ${e.message}", e)
                }
                
                delay(POLL_INTERVAL)
            }
        }
    }

    private fun parseEnabledGamePackages(json: String): Set<String> {
        return try {
            val jsonArray = JSONArray(json)
            val packages = mutableSetOf<String>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val enabled = obj.optBoolean("enabled", true)
                if (enabled) {
                    packages.add(obj.getString("packageName"))
                }
            }
            packages
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse game apps: ${e.message}")
            emptySet()
        }
    }

    private fun getForegroundPackage(): String? {
        return try {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val beginTime = endTime - 5000 // Last 5 seconds
            
            val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)
            var lastPackage: String? = null
            
            while (usageEvents.hasNextEvent()) {
                val event = UsageEvents.Event()
                usageEvents.getNextEvent(event)
                
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                    event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    lastPackage = event.packageName
                }
            }
            
            lastPackage
        } catch (e: Exception) {
            Log.e(TAG, "Error getting foreground package: ${e.message}")
            null
        }
    }
    
    private fun startGameOverlay() {
        if (!Settings.canDrawOverlays(applicationContext)) {
            Log.w(TAG, "Cannot start game overlay: no overlay permission")
            return
        }
        
        try {
            val intent = Intent(applicationContext, GameOverlayService::class.java)
            applicationContext.startService(intent)
            isGameOverlayActive = true
            Log.d(TAG, "Game overlay started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start game overlay: ${e.message}")
        }
    }
    
    private fun stopGameOverlay() {
        try {
            val intent = Intent(applicationContext, GameOverlayService::class.java)
            applicationContext.stopService(intent)
            isGameOverlayActive = false
            Log.d(TAG, "Game overlay stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop game overlay: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pollingJob?.cancel()
        serviceScope.cancel()
        
        if (isGameOverlayActive) {
            stopGameOverlay()
        }
        
        Log.d(TAG, "GameMonitorService stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
