package id.xms.xtrakernelmanager.ui.screens.info

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.repository.SystemInfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScreenOnTimeData(
    val timestamp: Long,
    val screenOnTime: Long
)

data class InfoUiState(
    val screenOnTime: Long = 0,
    val screenOnHistory: List<ScreenOnTimeData> = emptyList(),
    val lastChargeFull: Long = 0,
    val isLoading: Boolean = true
)

class InfoViewModel(
    application: Application,
    private val systemInfoRepository: SystemInfoRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(InfoUiState())
    val uiState: StateFlow<InfoUiState> = _uiState.asStateFlow()

    private val context: Context
        get() = getApplication<Application>().applicationContext

    init {
        loadScreenOnTime()
    }

    private fun loadScreenOnTime() {
        viewModelScope.launch {
            try {
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

                // Get battery status
                val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryStatus = context.registerReceiver(null, intentFilter)

                val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                // TODO: Implement actual screen on time tracking
                _uiState.value = _uiState.value.copy(
                    screenOnTime = 0L,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun refresh() {
        loadScreenOnTime()
    }
}
