package id.xms.xtrakernelmanager.ui.screens.tuning

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.model.CpuInfo
import id.xms.xtrakernelmanager.data.model.GpuInfo
import id.xms.xtrakernelmanager.data.repository.KernelRepository
import id.xms.xtrakernelmanager.service.MonitoringService
import id.xms.xtrakernelmanager.utils.RootUtils
import id.xms.xtrakernelmanager.utils.SysfsUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TuningUiState(
    val isLoading: Boolean = true,
    val hasRoot: Boolean = false,
    val cpuInfo: CpuInfo = CpuInfo(),
    val gpuInfo: GpuInfo = GpuInfo(),
    val isMediaTek: Boolean = false,
    val swappiness: Int = 60,
    val zramSize: Long = 0,
    val swapSize: Long = 0,
    val ioSchedulers: List<String> = emptyList(),
    val tcpAlgorithms: List<String> = emptyList(),
    val thermalMode: String = "Not Set",
    val applyOnBoot: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class TuningViewModel(
    application: Application,
    private val kernelRepository: KernelRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TuningUiState())
    val uiState: StateFlow<TuningUiState> = _uiState.asStateFlow()

    init {
        checkRootAccess()
        loadTuningData()
        startRealtimeMonitoring()
    }

    private fun checkRootAccess() {
        viewModelScope.launch {
            val hasRoot = RootUtils.isRootAvailable()
            _uiState.value = _uiState.value.copy(hasRoot = hasRoot)
        }
    }

    private fun loadTuningData() {
        viewModelScope.launch {
            try {
                val cpuInfo = kernelRepository.getCpuInfo()
                val gpuInfo = kernelRepository.getGpuInfo()
                val isMediaTek = SysfsUtils.isMediaTekDevice()
                val swappiness = kernelRepository.getSwappiness()
                val zramSize = kernelRepository.getZramSize()
                val swapSize = kernelRepository.getSwapSize()
                val thermalMode = kernelRepository.getThermalMode()

                _uiState.value = _uiState.value.copy(
                    cpuInfo = cpuInfo,
                    gpuInfo = gpuInfo,
                    isMediaTek = isMediaTek,
                    swappiness = swappiness,
                    zramSize = zramSize,
                    swapSize = swapSize,
                    thermalMode = thermalMode,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Error: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun startRealtimeMonitoring() {
        viewModelScope.launch {
            while (isActive) {
                try {
                    val cpuInfo = kernelRepository.getCpuInfo()
                    val gpuInfo = kernelRepository.getGpuInfo()

                    _uiState.value = _uiState.value.copy(
                        cpuInfo = cpuInfo,
                        gpuInfo = gpuInfo,
                        error = null
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
                delay(2000)
            }
        }
    }

    // CPU Controls
    fun setCpuFrequency(core: Int, frequency: Long) {
        viewModelScope.launch {
            val success = kernelRepository.setCpuFrequency(core, frequency)
            _uiState.value = _uiState.value.copy(
                message = if (success) "CPU $core frequency set to ${frequency / 1000} MHz"
                else "Failed to update CPU $core frequency"
            )
            if (success) {
                updateMonitoringService()
                loadTuningData()
            }
        }
    }

    fun setCpuGovernor(core: Int, governor: String) {
        viewModelScope.launch {
            val success = kernelRepository.setCpuGovernor(core, governor)
            _uiState.value = _uiState.value.copy(
                message = if (success) "CPU $core governor set to $governor"
                else "Failed to set governor"
            )
            if (success) {
                updateMonitoringService()
                loadTuningData()
            }
        }
    }

    fun toggleCpuCore(core: Int, online: Boolean) {
        viewModelScope.launch {
            if (core == 0) {
                _uiState.value = _uiState.value.copy(
                    message = "Cannot offline CPU 0"
                )
                return@launch
            }

            val success = kernelRepository.setCpuOnline(core, online)
            _uiState.value = _uiState.value.copy(
                message = if (success) "CPU $core ${if (online) "online" else "offline"}"
                else "Failed to toggle CPU $core"
            )
            if (success) loadTuningData()
        }
    }

    // GPU Controls
    fun setGpuFrequency(frequency: Long) {
        viewModelScope.launch {
            val success = kernelRepository.setGpuFrequency(frequency)
            _uiState.value = _uiState.value.copy(
                message = if (success) "GPU frequency set to ${frequency / 1000} MHz"
                else "Failed to update GPU frequency"
            )
            if (success) {
                updateMonitoringService()
                loadTuningData()
            }
        }
    }

    fun setGpuGovernor(governor: String) {
        viewModelScope.launch {
            val success = kernelRepository.setGpuGovernor(governor)
            _uiState.value = _uiState.value.copy(
                message = if (success) "GPU governor set to $governor"
                else "Failed to set GPU governor"
            )
            if (success) {
                updateMonitoringService()
                loadTuningData()
            }
        }
    }

    // RAM Controls
    fun setSwappiness(value: Int) {
        viewModelScope.launch {
            val success = kernelRepository.setSwappiness(value)
            _uiState.value = _uiState.value.copy(
                swappiness = if (success) value else _uiState.value.swappiness,
                message = if (success) "Swappiness set to $value"
                else "Failed to set swappiness"
            )
            if (success) updateMonitoringService()
        }
    }

    fun setZramSize(sizeBytes: Long) {
        viewModelScope.launch {
            val success = kernelRepository.setZramSize(sizeBytes)
            val sizeMB = sizeBytes / (1024 * 1024)
            _uiState.value = _uiState.value.copy(
                zramSize = if (success) sizeBytes else _uiState.value.zramSize,
                message = if (success) "ZRAM size set to $sizeMB MB"
                else "Failed to set ZRAM size"
            )
            if (success) updateMonitoringService()
        }
    }

    fun setSwapSize(sizeBytes: Long) {
        viewModelScope.launch {
            val success = kernelRepository.setSwapSize(sizeBytes)
            val sizeMB = sizeBytes / (1024 * 1024)
            _uiState.value = _uiState.value.copy(
                swapSize = if (success) sizeBytes else _uiState.value.swapSize,
                message = if (success) "Swap size set to $sizeMB MB"
                else "Failed to set swap size"
            )
            if (success) updateMonitoringService()
        }
    }

    // Additional Controls
    fun setThermalMode(mode: String) {
        viewModelScope.launch {
            val success = kernelRepository.setThermalMode(mode)
            _uiState.value = _uiState.value.copy(
                thermalMode = if (success) mode else _uiState.value.thermalMode,
                message = if (success) "Thermal mode set to $mode"
                else "Failed to set thermal mode"
            )
            if (success) {
                startMonitoringService(mode)
            }
        }
    }

    // Service Management
    private fun startMonitoringService(thermalMode: String) {
        val intent = Intent(getApplication(), MonitoringService::class.java).apply {
            action = MonitoringService.ACTION_START
            putExtra(MonitoringService.EXTRA_THERMAL_MODE, thermalMode)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication<Application>().startForegroundService(intent)
        } else {
            getApplication<Application>().startService(intent)
        }
    }

    private fun updateMonitoringService() {
        val intent = Intent(getApplication(), MonitoringService::class.java).apply {
            action = MonitoringService.ACTION_UPDATE_SETTINGS
            putExtra(MonitoringService.EXTRA_THERMAL_MODE, _uiState.value.thermalMode)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication<Application>().startForegroundService(intent)
        } else {
            getApplication<Application>().startService(intent)
        }
    }

    // Profile Management
    fun exportProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                message = "Export profile feature coming soon"
            )
        }
    }

    fun importProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                message = "Import profile feature coming soon"
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun refresh() {
        loadTuningData()
    }

    override fun onCleared() {
        super.onCleared()
        // Service will keep running in background
    }
}
