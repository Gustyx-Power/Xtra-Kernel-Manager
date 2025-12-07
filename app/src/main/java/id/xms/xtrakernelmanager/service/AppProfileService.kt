package id.xms.xtrakernelmanager.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.AppProfile
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.domain.usecase.CPUControlUseCase
import id.xms.xtrakernelmanager.domain.usecase.ThermalControlUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.JSONArray

class AppProfileService : Service() {

    companion object {
        private const val TAG = "AppProfileService"
        private const val CHANNEL_ID = "app_profile_channel"
        private const val NOTIFICATION_ID = 2001
        private const val POLL_INTERVAL = 1500L // Check every 1.5 seconds
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var preferencesManager: PreferencesManager
    private val cpuUseCase = CPUControlUseCase()
    private val thermalUseCase = ThermalControlUseCase()
    
    private var lastForegroundPackage: String? = null
    private var pollingJob: Job? = null
    private var appProfiles: List<AppProfile> = emptyList()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate called")
        preferencesManager = PreferencesManager(applicationContext)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        startPolling()
        
        // Debug toast to confirm service started
        mainHandler.post {
            Toast.makeText(applicationContext, getString(R.string.per_app_profile_service_started), Toast.LENGTH_SHORT).show()
        }
        Log.d(TAG, "AppProfileService started successfully")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called")
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.per_app_profile),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.per_app_profile_notif_text)
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.per_app_profile_notif_title))
            .setContentText(getString(R.string.per_app_profile_notif_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun startPolling() {
        Log.d(TAG, "Starting polling loop")
        pollingJob = serviceScope.launch {
            while (isActive) {
                try {
                    // Load profiles
                    val profilesJson = preferencesManager.getAppProfiles().first()
                    appProfiles = parseProfiles(profilesJson)
                    Log.d(TAG, "Loaded ${appProfiles.size} profiles")
                    
                    // Check foreground app
                    val foregroundPackage = getForegroundPackage()
                    Log.d(TAG, "Current foreground: $foregroundPackage, last: $lastForegroundPackage")
                    
                    if (foregroundPackage != null && foregroundPackage != lastForegroundPackage) {
                        Log.d(TAG, "Foreground app changed to: $foregroundPackage")
                        lastForegroundPackage = foregroundPackage
                        
                        // Check if there's a profile for this app
                        val profile = appProfiles.find { 
                            it.packageName == foregroundPackage && it.enabled 
                        }
                        
                        if (profile != null) {
                            Log.d(TAG, "Found profile for ${profile.appName}, applying...")
                            applyProfile(profile)
                        } else {
                            Log.d(TAG, "No profile found for $foregroundPackage")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in polling: ${e.message}", e)
                }
                
                delay(POLL_INTERVAL)
            }
        }
    }

    private fun parseProfiles(json: String): List<AppProfile> {
        return try {
            val jsonArray = JSONArray(json)
            val profiles = mutableListOf<AppProfile>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                profiles.add(
                    AppProfile(
                        packageName = obj.getString("packageName"),
                        appName = obj.getString("appName"),
                        governor = obj.optString("governor", "schedutil"),
                        thermalPreset = obj.optString("thermalPreset", "Not Set"),
                        enabled = obj.optBoolean("enabled", true)
                    )
                )
            }
            profiles
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse profiles: ${e.message}")
            emptyList()
        }
    }

    private fun getForegroundPackage(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        if (usageStatsManager == null) {
            Log.e(TAG, "UsageStatsManager is null")
            return null
        }

        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 10000 // Look back 10 seconds

        val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)
        var lastPackage: String? = null
        var lastTime: Long = 0
        
        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)
            
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                if (event.timeStamp > lastTime) {
                    lastPackage = event.packageName
                    lastTime = event.timeStamp
                }
            }
        }
        
        return lastPackage
    }

    private fun applyProfile(profile: AppProfile) {
        serviceScope.launch {
            try {
                Log.d(TAG, "Applying profile for ${profile.appName}: Governor=${profile.governor}, Thermal=${profile.thermalPreset}")
                
                // Show toast first (before applying settings)
                mainHandler.post {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.per_app_profile_applied, profile.appName, profile.governor, profile.thermalPreset),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
                // Apply governor to all clusters
                val clusters = cpuUseCase.detectClusters()
                Log.d(TAG, "Applying governor ${profile.governor} to ${clusters.size} clusters")
                clusters.forEach { cluster ->
                    cpuUseCase.setClusterGovernor(cluster.clusterNumber, profile.governor)
                }
                
                // Apply thermal preset
                if (profile.thermalPreset != "Not Set") {
                    Log.d(TAG, "Applying thermal preset: ${profile.thermalPreset}")
                    thermalUseCase.setThermalMode(profile.thermalPreset, false)
                }
                
                Log.d(TAG, "Profile applied successfully for ${profile.appName}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to apply profile: ${e.message}", e)
                mainHandler.post {
                    Toast.makeText(
                        applicationContext,
                        "Failed to apply profile: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pollingJob?.cancel()
        serviceScope.cancel()
        mainHandler.post {
            Toast.makeText(applicationContext, getString(R.string.per_app_profile_service_stopped), Toast.LENGTH_SHORT).show()
        }
        Log.d(TAG, "AppProfileService stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
