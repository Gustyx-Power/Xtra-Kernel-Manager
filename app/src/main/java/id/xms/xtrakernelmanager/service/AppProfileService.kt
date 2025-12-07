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
import id.xms.xtrakernelmanager.domain.root.RootManager
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
        private const val POLL_INTERVAL = 2000L // Check every 2 seconds
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var preferencesManager: PreferencesManager
    private val cpuUseCase = CPUControlUseCase()
    private val thermalUseCase = ThermalControlUseCase()
    
    private var lastForegroundPackage: String? = null
    private var pollingJob: Job? = null
    private var appProfiles: List<AppProfile> = emptyList()

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(applicationContext)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        startPolling()
        Log.d(TAG, "AppProfileService started")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Per-App Profile Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors foreground apps and applies profiles"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Per-App Profile Active")
            .setContentText("Monitoring foreground apps")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun startPolling() {
        pollingJob = serviceScope.launch {
            while (isActive) {
                try {
                    // Load profiles
                    val profilesJson = preferencesManager.getAppProfiles().first()
                    appProfiles = parseProfiles(profilesJson)
                    
                    // Check foreground app
                    val foregroundPackage = getForegroundPackage()
                    
                    if (foregroundPackage != null && foregroundPackage != lastForegroundPackage) {
                        Log.d(TAG, "Foreground app changed: $foregroundPackage")
                        lastForegroundPackage = foregroundPackage
                        
                        // Check if there's a profile for this app
                        val profile = appProfiles.find { 
                            it.packageName == foregroundPackage && it.enabled 
                        }
                        
                        if (profile != null) {
                            applyProfile(profile)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in polling: ${e.message}")
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
            ?: return null

        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 5000

        val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)
        var lastPackage: String? = null
        
        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)
            
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastPackage = event.packageName
            }
        }
        
        return lastPackage
    }

    private fun applyProfile(profile: AppProfile) {
        serviceScope.launch {
            try {
                Log.d(TAG, "Applying profile for ${profile.appName}: Governor=${profile.governor}, Thermal=${profile.thermalPreset}")
                
                // Apply governor to all clusters
                val clusters = cpuUseCase.detectClusters()
                clusters.forEach { cluster ->
                    cpuUseCase.setClusterGovernor(cluster.clusterNumber, profile.governor)
                }
                
                // Apply thermal preset
                if (profile.thermalPreset != "Not Set") {
                    thermalUseCase.setThermalMode(profile.thermalPreset, false)
                }
                
                // Show toast
                withContext(Dispatchers.Main) {
                    showToast("${profile.appName}: ${profile.governor} / ${profile.thermalPreset}")
                }
                
                Log.d(TAG, "Profile applied successfully for ${profile.appName}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to apply profile: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pollingJob?.cancel()
        serviceScope.cancel()
        Log.d(TAG, "AppProfileService stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
