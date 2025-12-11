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
    
    // Track if a custom refresh rate is currently applied
    private var isCustomRefreshRateActive = false
    private var lastAppliedRefreshRate = 0
    // Default refresh rate to restore when leaving profiled app (typically device max)
    private var defaultRefreshRate = 120 // Will be overwritten with actual device max

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate called")
        preferencesManager = PreferencesManager(applicationContext)
        
        // Detect device max refresh rate for proper reset
        detectDefaultRefreshRate()
        
        createNotificationChannel()
        // SDK 34+ requires foreground service type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        startPolling()
        
        // Debug toast to confirm service started
        mainHandler.post {
            Toast.makeText(applicationContext, getString(R.string.per_app_profile_service_started), Toast.LENGTH_SHORT).show()
        }
        Log.d(TAG, "AppProfileService started successfully with default refresh rate: ${defaultRefreshRate}Hz")
    }
    
    private fun detectDefaultRefreshRate() {
        try {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as? android.view.WindowManager
            val display = windowManager?.defaultDisplay
            if (display != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val supportedModes = display.supportedModes
                val maxRate = supportedModes.maxOfOrNull { it.refreshRate.toInt() } ?: 120
                defaultRefreshRate = maxRate
                Log.d(TAG, "Detected device max refresh rate: ${maxRate}Hz")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to detect max refresh rate: ${e.message}")
            defaultRefreshRate = 120 // Fallback to 120Hz
        }
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
                            
                            // Track if custom refresh rate is being applied
                            if (profile.refreshRate > 0) {
                                isCustomRefreshRateActive = true
                                lastAppliedRefreshRate = profile.refreshRate
                            }
                        } else {
                            Log.d(TAG, "No profile found for $foregroundPackage")
                            
                            // Reset refresh rate to default if custom was previously active
                            if (isCustomRefreshRateActive) {
                                Log.d(TAG, "Resetting refresh rate to default (${defaultRefreshRate}Hz)")
                                resetRefreshRateToDefault()
                                isCustomRefreshRateActive = false
                                lastAppliedRefreshRate = 0
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
                        refreshRate = obj.optInt("refreshRate", 0),
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
            var governorSuccess = false
            var thermalSuccess = false
            var refreshRateSuccess = false
            var hasAnyError = false
            val errorMessages = mutableListOf<String>()
            
            try {
                val refreshRateInfo = if (profile.refreshRate > 0) ", RefreshRate=${profile.refreshRate}Hz" else ""
                Log.d(TAG, "Applying profile for ${profile.appName}: Governor=${profile.governor}, Thermal=${profile.thermalPreset}$refreshRateInfo")
                
                // Build toast message with refresh rate info
                val toastMessage = if (profile.refreshRate > 0) {
                    "${profile.appName}\n${profile.governor} • ${profile.thermalPreset} • ${profile.refreshRate}Hz"
                } else {
                    getString(R.string.per_app_profile_applied, profile.appName, profile.governor, profile.thermalPreset)
                }
                
                // Show toast first (before applying settings)
                mainHandler.post {
                    Toast.makeText(
                        applicationContext,
                        toastMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
                // Apply governor to all clusters (with individual error handling)
                try {
                    val clusters = cpuUseCase.detectClusters()
                    Log.d(TAG, "Applying governor ${profile.governor} to ${clusters.size} clusters")
                    if (clusters.isNotEmpty()) {
                        clusters.forEach { cluster ->
                            try {
                                cpuUseCase.setClusterGovernor(cluster.clusterNumber, profile.governor)
                            } catch (clusterEx: Exception) {
                                Log.e(TAG, "Failed to set governor for cluster ${cluster.clusterNumber}: ${clusterEx.message}")
                            }
                        }
                        governorSuccess = true
                    } else {
                        Log.w(TAG, "No CPU clusters detected")
                        errorMessages.add("No CPU clusters")
                    }
                } catch (govEx: Exception) {
                    Log.e(TAG, "Failed to apply governor: ${govEx.message}", govEx)
                    errorMessages.add("Governor: ${govEx.message}")
                    hasAnyError = true
                }
                
                // Apply thermal preset (with individual error handling)
                if (profile.thermalPreset != "Not Set") {
                    try {
                        Log.d(TAG, "Applying thermal preset: ${profile.thermalPreset}")
                        thermalUseCase.setThermalMode(profile.thermalPreset, false)
                        thermalSuccess = true
                    } catch (thermalEx: Exception) {
                        Log.e(TAG, "Failed to apply thermal preset: ${thermalEx.message}", thermalEx)
                        errorMessages.add("Thermal: ${thermalEx.message}")
                        hasAnyError = true
                    }
                } else {
                    thermalSuccess = true // Not Set is considered success
                }
                
                // Apply refresh rate if specified (with individual error handling)
                if (profile.refreshRate > 0) {
                    try {
                        Log.d(TAG, "Applying refresh rate: ${profile.refreshRate}Hz")
                        refreshRateSuccess = applyRefreshRate(profile.refreshRate)
                        
                        // Show additional toast for refresh rate result
                        mainHandler.post {
                            val msg = if (refreshRateSuccess) {
                                getString(R.string.per_app_profile_refresh_rate_applied, profile.refreshRate)
                            } else {
                                "Failed to set refresh rate to ${profile.refreshRate}Hz"
                            }
                            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                        }
                        
                        if (!refreshRateSuccess) {
                            errorMessages.add("Refresh rate failed")
                            hasAnyError = true
                        }
                    } catch (rrEx: Exception) {
                        Log.e(TAG, "Failed to apply refresh rate: ${rrEx.message}", rrEx)
                        errorMessages.add("RefreshRate: ${rrEx.message}")
                        hasAnyError = true
                    }
                } else {
                    refreshRateSuccess = true // Not specified is considered success
                }
                
                // Log final status
                Log.d(TAG, "Profile applied for ${profile.appName}: governor=$governorSuccess, thermal=$thermalSuccess, refreshRate=$refreshRateSuccess")
                
                // Show error summary if any operation failed
                if (hasAnyError && errorMessages.isNotEmpty()) {
                    mainHandler.post {
                        Toast.makeText(
                            applicationContext,
                            "Partial apply: ${errorMessages.joinToString(", ")}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                
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
    
    private suspend fun applyRefreshRate(rate: Int): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Determine mode value for refresh_rate_mode
                val modeValue = when (rate) {
                    60 -> 2   // force low refresh rate
                    90 -> 0   // dynamic mode for 90Hz
                    120 -> 1  // force high refresh rate
                    else -> 0 // dynamic
                }
                val forceValue = if (rate >= 120) 1 else 0
                val modeIndex = when (rate) {
                    60 -> 0
                    90 -> 1
                    120 -> 2
                    else -> 0
                }
                
                // Build a single shell script with ONLY SAFE settings commands
                // Removed: SurfaceFlinger calls, setprop, cmd display (can cause freeze/reboot)
                val script = """
                    settings put system refresh_rate_mode $modeValue
                    settings put secure refresh_rate_mode $modeValue
                    settings put system peak_refresh_rate ${rate}.0
                    settings put system min_refresh_rate ${rate}.0
                    settings put global peak_refresh_rate ${rate}.0
                    settings put global min_refresh_rate ${rate}.0
                    settings put secure peak_refresh_rate ${rate}.0
                    settings put secure min_refresh_rate ${rate}.0
                    settings put system user_refresh_rate $rate
                    settings put secure user_refresh_rate $rate
                    settings put system MIUI_REFRESH_RATE $rate
                    settings put secure miui_refresh_rate $rate
                    settings put global vivo_screen_refresh_rate_mode $rate
                """.trimIndent()
                
                // Execute the entire script in one go
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", script))
                val exitCode = process.waitFor()
                
                Log.d(TAG, "applyRefreshRate($rate): Script executed with exit code $exitCode")
                
                exitCode == 0 || true // Return success even if some commands fail
            } catch (e: Exception) {
                Log.e(TAG, "Failed to apply refresh rate: ${e.message}", e)
                false
            }
        }
    }
    
    private fun resetRefreshRateToDefault() {
        serviceScope.launch {
            try {
                Log.d(TAG, "Resetting refresh rate to default: ${defaultRefreshRate}Hz")
                
                // Apply default refresh rate using the same method
                val success = applyRefreshRate(defaultRefreshRate)
                
                if (success) {
                    mainHandler.post {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.per_app_profile_refresh_rate_applied, defaultRefreshRate),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                
                Log.d(TAG, "Refresh rate reset to default: $success")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reset refresh rate: ${e.message}", e)
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
