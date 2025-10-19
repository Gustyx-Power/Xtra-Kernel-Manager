package id.xms.xtrakernelmanager.ui.screens.misc

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.repository.BatteryRepository
import id.xms.xtrakernelmanager.domain.service.OverlayService
import id.xms.xtrakernelmanager.utils.RootUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalTimeInForeground: Long,
    val lastTimeUsed: Long
)

data class GameOverlayState(
    val isActive: Boolean = false,
    val fps: Int = 0,
    val cpuFreq: Long = 0,
    val cpuLoad: Float = 0f,
    val gpuLoad: Float = 0f,
    val batteryTemp: Float = 0f,
    val performanceMode: String = "Balance",
    val dndEnabled: Boolean = false,
    val fpsHistory: List<Int> = emptyList()
)

data class MiscUiState(
    val hasRoot: Boolean = false,
    val batteryInfo: BatteryInfo = BatteryInfo(),
    val appUsageList: List<AppUsageInfo> = emptyList(),
    val overlayState: GameOverlayState = GameOverlayState(),
    val overlayEnabled: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val message: String? = null
)

class MiscViewModel(
    application: Application,
    private val batteryRepository: BatteryRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MiscUiState())
    val uiState: StateFlow<MiscUiState> = _uiState.asStateFlow()

    private val context: Context
        get() = getApplication<Application>().applicationContext

    init {
        checkRoot()
        checkOverlayPermission()
        loadBatteryInfo()
        loadAppUsageStats()
        startRealtimeMonitoring()
    }

    private fun checkRoot() {
        viewModelScope.launch {
            val hasRoot = RootUtils.isRootAvailable()
            _uiState.value = _uiState.value.copy(hasRoot = hasRoot)
        }
    }

    private fun checkOverlayPermission() {
        viewModelScope.launch {
            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
            _uiState.value = _uiState.value.copy(hasOverlayPermission = hasPermission)
        }
    }

    private fun startRealtimeMonitoring() {
        viewModelScope.launch {
            while (isActive) {
                try {
                    loadBatteryInfo()
                } catch (e: Exception) {
                    // Handle error silently
                }
                delay(2000)
            }
        }
    }

    private fun loadBatteryInfo() {
        viewModelScope.launch {
            try {
                val batteryInfo = batteryRepository.getBatteryInfo()
                _uiState.value = _uiState.value.copy(batteryInfo = batteryInfo)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Error loading battery info: ${e.message}"
                )
            }
        }
    }

    fun loadAppUsageStats() {
        viewModelScope.launch {
            try {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val endTime = System.currentTimeMillis()
                val startTime = endTime - (24 * 60 * 60 * 1000) // Last 24 hours

                val usageStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime
                )

                if (usageStats.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        message = "No usage stats available. Please grant usage access permission."
                    )
                    return@launch
                }

                val appUsageList = usageStats
                    .filter { it.totalTimeInForeground > 0 }
                    .sortedByDescending { it.totalTimeInForeground }
                    .take(20)
                    .map { stats ->
                        val appName = try {
                            val pm = context.packageManager
                            pm.getApplicationLabel(
                                pm.getApplicationInfo(stats.packageName, 0)
                            ).toString()
                        } catch (e: Exception) {
                            stats.packageName
                        }

                        AppUsageInfo(
                            packageName = stats.packageName,
                            appName = appName,
                            totalTimeInForeground = stats.totalTimeInForeground,
                            lastTimeUsed = stats.lastTimeUsed
                        )
                    }

                _uiState.value = _uiState.value.copy(appUsageList = appUsageList)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Error loading app usage: ${e.message}"
                )
            }
        }
    }

    fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun toggleOverlay() {
        viewModelScope.launch {
            // Check overlay permission (NO ROOT CHECK HERE!)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context)) {
                    // Request permission
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)

                    _uiState.value = _uiState.value.copy(
                        message = "Please grant overlay permission"
                    )
                    return@launch
                }
            }

            // Toggle overlay service (WORKS WITH OR WITHOUT ROOT)
            val intent = Intent(context, OverlayService::class.java)

            if (_uiState.value.overlayEnabled) {
                // Stop overlay
                intent.action = OverlayService.ACTION_STOP
                context.stopService(intent)

                _uiState.value = _uiState.value.copy(
                    overlayEnabled = false,
                    overlayState = _uiState.value.overlayState.copy(isActive = false),
                    message = "Overlay disabled"
                )
            } else {
                // Start overlay
                intent.action = OverlayService.ACTION_START

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }

                _uiState.value = _uiState.value.copy(
                    overlayEnabled = true,
                    overlayState = _uiState.value.overlayState.copy(isActive = true),
                    message = "Overlay enabled"
                )
            }
        }
    }

    fun startGameOverlay() {
        viewModelScope.launch {
            // Check overlay permission only (NO ROOT CHECK!)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context)) {
                    _uiState.value = _uiState.value.copy(
                        message = "Overlay permission required. Please grant permission."
                    )

                    // Auto open permission settings
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return@launch
                }
            }

            val intent = Intent(context, OverlayService::class.java).apply {
                action = OverlayService.ACTION_START
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }

            _uiState.value = _uiState.value.copy(
                overlayState = _uiState.value.overlayState.copy(isActive = true),
                overlayEnabled = true,
                message = "Game overlay started (FPS may show display refresh rate on non-root)"
            )
        }
    }


    fun stopGameOverlay() {
        viewModelScope.launch {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = OverlayService.ACTION_STOP
            }
            context.stopService(intent)

            _uiState.value = _uiState.value.copy(
                overlayState = _uiState.value.overlayState.copy(isActive = false),
                overlayEnabled = false,
                message = "Game overlay stopped"
            )
        }
    }

    fun setPerformanceMode(mode: String) {
        viewModelScope.launch {
            // Apply performance mode settings
            when (mode) {
                "Performance" -> {
                    // Set CPU to performance governor
                    if (_uiState.value.hasRoot) {
                        RootUtils.executeCommand("echo performance > /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor")
                    }
                }
                "Balance" -> {
                    // Set CPU to balanced governor
                    if (_uiState.value.hasRoot) {
                        RootUtils.executeCommand("echo schedutil > /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor")
                    }
                }
                "Battery Saver" -> {
                    // Set CPU to powersave governor
                    if (_uiState.value.hasRoot) {
                        RootUtils.executeCommand("echo powersave > /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor")
                    }
                }
            }

            _uiState.value = _uiState.value.copy(
                overlayState = _uiState.value.overlayState.copy(performanceMode = mode),
                message = "Performance mode set to $mode"
            )
        }
    }

    fun toggleDnd(enabled: Boolean) {
        viewModelScope.launch {
            // Toggle Do Not Disturb via shell if root available
            if (_uiState.value.hasRoot) {
                val mode = if (enabled) "1" else "0"
                RootUtils.executeCommand("settings put global zen_mode $mode")

                _uiState.value = _uiState.value.copy(
                    overlayState = _uiState.value.overlayState.copy(dndEnabled = enabled),
                    message = if (enabled) "Do Not Disturb enabled" else "Do Not Disturb disabled"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    message = "Root required for DND control"
                )
            }
        }
    }

    fun clearRam() {
        viewModelScope.launch {
            if (!_uiState.value.hasRoot) {
                _uiState.value = _uiState.value.copy(
                    message = "Root required to clear RAM"
                )
                return@launch
            }

            val result = RootUtils.executeCommand("sync && echo 3 > /proc/sys/vm/drop_caches")
            _uiState.value = _uiState.value.copy(
                message = if (result.isSuccess) "RAM cleared successfully" else "Failed to clear RAM"
            )

            // Reload battery info to show updated RAM
            delay(500)
            loadBatteryInfo()
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            if (!_uiState.value.hasRoot) {
                _uiState.value = _uiState.value.copy(
                    message = "Root required to clear cache"
                )
                return@launch
            }

            val result = RootUtils.executeCommand("sync && rm -rf /data/dalvik-cache/* && rm -rf /cache/*")
            _uiState.value = _uiState.value.copy(
                message = if (result.isSuccess) "Cache cleared successfully" else "Failed to clear cache"
            )
        }
    }

    fun killBackgroundApps() {
        viewModelScope.launch {
            if (!_uiState.value.hasRoot) {
                _uiState.value = _uiState.value.copy(
                    message = "Root required to kill background apps"
                )
                return@launch
            }

            // Get list of running apps and kill them (except system apps)
            val result = RootUtils.executeCommand("am kill-all")
            _uiState.value = _uiState.value.copy(
                message = if (result.isSuccess) "Background apps killed" else "Failed to kill apps"
            )
        }
    }

    fun enableFastCharging(enabled: Boolean) {
        viewModelScope.launch {
            if (!_uiState.value.hasRoot) {
                _uiState.value = _uiState.value.copy(
                    message = "Root required for fast charging control"
                )
                return@launch
            }

            // Try different paths for fast charging
            val paths = listOf(
                "/sys/class/power_supply/battery/allow_hvdcp3",
                "/sys/class/power_supply/battery/fast_charge",
                "/sys/class/qcom-battery/restricted_charging"
            )

            val value = if (enabled) "1" else "0"
            var success = false

            for (path in paths) {
                val result = RootUtils.writeFile(path, value)
                if (result) {
                    success = true
                    break
                }
            }

            _uiState.value = _uiState.value.copy(
                message = if (success) {
                    if (enabled) "Fast charging enabled" else "Fast charging disabled"
                } else {
                    "Fast charging not supported on this device"
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun refresh() {
        viewModelScope.launch {
            loadBatteryInfo()
            loadAppUsageStats()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Stop overlay if active
        if (_uiState.value.overlayEnabled) {
            stopGameOverlay()
        }
    }
}
