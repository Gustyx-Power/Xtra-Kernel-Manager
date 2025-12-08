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
        startForeground(NOTIFICATION_ID, createNotification())
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
                
                // Apply refresh rate if specified
                if (profile.refreshRate > 0) {
                    Log.d(TAG, "Applying refresh rate: ${profile.refreshRate}Hz")
                    val success = applyRefreshRate(profile.refreshRate)
                    
                    // Show additional toast for refresh rate result
                    mainHandler.post {
                        val msg = if (success) {
                            getString(R.string.per_app_profile_refresh_rate_applied, profile.refreshRate)
                        } else {
                            "Failed to set refresh rate to ${profile.refreshRate}Hz"
                        }
                        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                    }
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
    
    private suspend fun applyRefreshRate(rate: Int): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                var success = false
                
                // STEP 1: Set refresh_rate_mode FIRST (force the mode before setting values)
                // This is critical for MIUI/HyperOS/OriginOS to properly apply refresh rate
                try {
                    // refresh_rate_mode: 0=dynamic, 1=force high, 2=force low
                    // For specific rates, we need to disable dynamic mode first
                    val modeValue = when (rate) {
                        60 -> 2   // force low refresh rate
                        90 -> 0   // dynamic mode for 90Hz (some devices)
                        120 -> 1  // force high refresh rate
                        else -> 0 // dynamic
                    }
                    // Set in both system and secure
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put system refresh_rate_mode $modeValue")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put secure refresh_rate_mode $modeValue")).waitFor()
                    Log.d(TAG, "Step 1 (refresh_rate_mode): Set mode to $modeValue for ${rate}Hz")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "Step 1 failed: ${e.message}")
                }
                
                // STEP 2: Vivo/Origin OS specific - vivo_screen_refresh_rate_mode (IMPORTANT!)
                try {
                    val vivoCmd = "settings put global vivo_screen_refresh_rate_mode ${rate}"
                    Runtime.getRuntime().exec(arrayOf("su", "-c", vivoCmd)).waitFor()
                    Log.d(TAG, "Step 2 (vivo_screen_refresh_rate_mode): Applied ${rate}Hz")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "Step 2 failed: ${e.message}")
                }
                
                // STEP 3: Set peak_refresh_rate and min_refresh_rate in ALL namespaces
                try {
                    // System namespace
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put system peak_refresh_rate ${rate}.0")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put system min_refresh_rate ${rate}.0")).waitFor()
                    // Global namespace
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put global peak_refresh_rate ${rate}.0")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put global min_refresh_rate ${rate}.0")).waitFor()
                    // Secure namespace
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put secure peak_refresh_rate ${rate}.0")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put secure min_refresh_rate ${rate}.0")).waitFor()
                    Log.d(TAG, "Step 3 (peak/min_refresh_rate): Set all namespaces to ${rate}Hz")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "Step 3 failed: ${e.message}")
                }
                
                // STEP 4: MIUI/HyperOS specific settings (system namespace)
                try {
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put system user_refresh_rate ${rate}")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put system MIUI_REFRESH_RATE ${rate}")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put system screen_refresh_rate ${rate}")).waitFor()
                    Log.d(TAG, "Step 4 (MIUI system): Set user/MIUI/screen refresh rate to ${rate}Hz")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "Step 4 failed: ${e.message}")
                }
                
                // STEP 5: MIUI/HyperOS specific settings (secure namespace)
                try {
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put secure miui_refresh_rate ${rate}")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put secure user_refresh_rate ${rate}")).waitFor()
                    Log.d(TAG, "Step 5 (MIUI secure): Set miui/user refresh rate to ${rate}Hz")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "Step 5 failed: ${e.message}")
                }
                
                // STEP 6: System properties for SurfaceFlinger
                try {
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "setprop debug.sf.frame_rate_multiple_threshold ${rate}")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "setprop persist.sys.miui.refresh_rate ${rate}")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "setprop persist.vendor.display.mode ${rate}")).waitFor()
                    Log.d(TAG, "Step 6 (setprop): Set SurfaceFlinger properties to ${rate}Hz")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "Step 6 failed: ${e.message}")
                }
                
                // STEP 7: Direct SurfaceFlinger service call
                try {
                    val modeIndex = when (rate) {
                        60 -> 0
                        90 -> 1
                        120 -> 2
                        else -> 0
                    }
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "service call SurfaceFlinger 1035 i32 $modeIndex")).waitFor()
                    Log.d(TAG, "Step 7 (SurfaceFlinger): Applied mode index $modeIndex")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "Step 7 failed: ${e.message}")
                }
                
                // STEP 8: Developer option - Force peak refresh rate
                try {
                    // This is the developer option toggle for forcing peak refresh rate
                    val forceValue = if (rate >= 120) 1 else 0
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put system user_preferred_refresh_rate $rate")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put global user_preferred_refresh_rate $rate")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put secure user_preferred_refresh_rate $rate")).waitFor()
                    // Force peak refresh rate toggle (1 = force, 0 = default)
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put system force_peak_refresh_rate $forceValue")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put global force_peak_refresh_rate $forceValue")).waitFor()
                    Log.d(TAG, "Step 8 (force_peak_refresh_rate): Set to $forceValue for ${rate}Hz")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "Step 8 failed: ${e.message}")
                }
                
                // STEP 9: Vivo/Origin OS - high refresh rate enable
                try {
                    // Enable high refresh rate mode for all apps
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put global vivo_high_refresh_rate_enable 1")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put system high_refresh_rate_enable 1")).waitFor()
                    // Set the actual rate value in Vivo namespace
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put system vivo_refresh_rate ${rate}")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "settings put global vivo_refresh_rate ${rate}")).waitFor()
                    Log.d(TAG, "Step 9 (vivo_high_refresh_rate): Enabled high refresh rate for ${rate}Hz")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "Step 9 failed: ${e.message}")
                }
                
                // STEP 10: Android 13+ display command
                try {
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "cmd display set-user-preferred-display-mode 0 0 ${rate}.0")).waitFor()
                    Log.d(TAG, "Step 10 (cmd display): Set preferred display mode to ${rate}Hz")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "Step 10 failed: ${e.message}")
                }
                
                // STEP 11: Force display configuration refresh
                try {
                    // Method A: Broadcast display config change
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "am broadcast -a android.intent.action.CONFIGURATION_CHANGED")).waitFor()
                    // Method B: Reset window manager
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "wm size reset")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "wm density reset")).waitFor()
                    Log.d(TAG, "Step 11 (display config broadcast): Triggered config change")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "Step 11 failed: ${e.message}")
                }
                
                // STEP 12: Kill and restart SurfaceFlinger (AGGRESSIVE - will cause screen flicker!)
                // This forces SurfaceFlinger to re-read all display settings
                try {
                    // Option 1: Signal SurfaceFlinger to reload (safer)
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "service call SurfaceFlinger 1034 i32 1")).waitFor()
                    // Option 2: Force close and let system restart it (causes flicker but effective)
                    // Runtime.getRuntime().exec(arrayOf("su", "-c", "pkill -f surfaceflinger")).waitFor()
                    Log.d(TAG, "Step 12 (SurfaceFlinger signal): Sent reload signal")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "Step 12 failed: ${e.message}")
                }
                
                // STEP 13: Restart SystemUI to force display configuration reload
                // NOTE: This is disabled by default as it's disruptive (causes brief black screen)
                // Some ROMs like Origin OS may not respect settings changes without SystemUI restart
                // Uncomment if refresh rate changes don't take effect on your ROM
                // try {
                //     Runtime.getRuntime().exec(arrayOf("su", "-c", "pkill -f com.android.systemui")).waitFor()
                //     Log.d(TAG, "Step 13 (SystemUI restart): Triggered SystemUI restart for display refresh")
                //     success = true
                // } catch (e: Exception) {
                //     Log.e(TAG, "Step 13 failed: ${e.message}")
                // }
                
                success
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
