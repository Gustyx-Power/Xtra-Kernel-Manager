package id.xms.xtrakernelmanager.ui.screens.misc

import android.app.Application
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.repository.BatteryRepository
import id.xms.xtrakernelmanager.utils.RootUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        loadBatteryInfo()
        loadAppUsageStats()
    }

    private fun checkRoot() {
        viewModelScope.launch {
            val hasRoot = RootUtils.isRootAvailable()
            _uiState.value = _uiState.value.copy(hasRoot = hasRoot)
        }
    }

    private fun loadBatteryInfo() {
        viewModelScope.launch {
            try {
                val batteryInfo = batteryRepository.getBatteryInfo()
                _uiState.value = _uiState.value.copy(batteryInfo = batteryInfo)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Error loading battery info: ${e.message}")
            }
        }
    }

    private fun loadAppUsageStats() {
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

                val appUsageList = usageStats
                    .filter { it.totalTimeInForeground > 0 }
                    .sortedByDescending { it.totalTimeInForeground }
                    .take(20)
                    .map { stats ->
                        val appName = try {
                            val pm = context.packageManager
                            pm.getApplicationLabel(pm.getApplicationInfo(stats.packageName, 0)).toString()
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
                _uiState.value = _uiState.value.copy(message = "Error loading app usage: ${e.message}")
            }
        }
    }

    fun startGameOverlay() {
        viewModelScope.launch {
            // TODO: Start overlay service
            _uiState.value = _uiState.value.copy(
                overlayState = _uiState.value.overlayState.copy(isActive = true),
                message = "Game overlay started"
            )
        }
    }

    fun stopGameOverlay() {
        viewModelScope.launch {
            // TODO: Stop overlay service
            _uiState.value = _uiState.value.copy(
                overlayState = _uiState.value.overlayState.copy(isActive = false),
                message = "Game overlay stopped"
            )
        }
    }

    fun setPerformanceMode(mode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                overlayState = _uiState.value.overlayState.copy(performanceMode = mode),
                message = "Performance mode set to $mode"
            )
        }
    }

    fun toggleDnd(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                overlayState = _uiState.value.overlayState.copy(dndEnabled = enabled)
            )
        }
    }

    fun clearRam() {
        viewModelScope.launch {
            val result = RootUtils.executeCommand("sync && echo 3 > /proc/sys/vm/drop_caches")
            _uiState.value = _uiState.value.copy(
                message = if (result.isSuccess) "RAM cleared" else "Failed to clear RAM"
            )
        }
    }
}
