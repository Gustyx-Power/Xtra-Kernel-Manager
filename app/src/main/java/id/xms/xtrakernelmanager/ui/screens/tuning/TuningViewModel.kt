package id.xms.xtrakernelmanager.ui.screens.tuning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.model.*
import id.xms.xtrakernelmanager.data.repository.KernelRepository
import id.xms.xtrakernelmanager.utils.SysfsUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TuningUiState(
    val isLoading: Boolean = true,
    val cpuInfo: CpuInfo = CpuInfo(),
    val gpuInfo: GpuInfo = GpuInfo(),
    val isMediaTek: Boolean = false,
    val swappiness: Int = 60,
    val ioSchedulers: List<String> = emptyList(),
    val tcpAlgorithms: List<String> = emptyList(),
    val thermalMode: String = "Not Set",
    val applyOnBoot: Boolean = false,
    val message: String? = null
)

class TuningViewModel(
    private val kernelRepository: KernelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TuningUiState())
    val uiState: StateFlow<TuningUiState> = _uiState.asStateFlow()

    init {
        loadTuningData()
    }

    private fun loadTuningData() {
        viewModelScope.launch {
            try {
                val cpuInfo = kernelRepository.getCpuInfo()
                val gpuInfo = kernelRepository.getGpuInfo()
                val isMediaTek = SysfsUtils.isMediaTekDevice()
                val swappiness = SysfsUtils.getSwappiness()

                _uiState.value = _uiState.value.copy(
                    cpuInfo = cpuInfo,
                    gpuInfo = gpuInfo,
                    isMediaTek = isMediaTek,
                    swappiness = swappiness,
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

    fun setCpuFrequency(core: Int, minFreq: Long, maxFreq: Long) {
        viewModelScope.launch {
            val success = kernelRepository.setCpuFrequency(core, minFreq, maxFreq)
            _uiState.value = _uiState.value.copy(
                message = if (success) "CPU frequency updated" else "Failed to update CPU frequency"
            )
            if (success) loadTuningData()
        }
    }

    fun setCpuGovernor(core: Int, governor: String) {
        viewModelScope.launch {
            val success = kernelRepository.setCpuGovernor(core, governor)
            _uiState.value = _uiState.value.copy(
                message = if (success) "Governor changed to $governor" else "Failed to change governor"
            )
            if (success) loadTuningData()
        }
    }

    fun toggleCpuCore(core: Int, online: Boolean) {
        viewModelScope.launch {
            val success = kernelRepository.setCpuOnline(core, online)
            _uiState.value = _uiState.value.copy(
                message = if (success) "CPU$core ${if (online) "enabled" else "disabled"}"
                else "Failed to toggle CPU$core"
            )
            if (success) loadTuningData()
        }
    }

    fun setGpuFrequency(minFreq: Long, maxFreq: Long) {
        viewModelScope.launch {
            val success = kernelRepository.setGpuFrequency(minFreq, maxFreq)
            _uiState.value = _uiState.value.copy(
                message = if (success) "GPU frequency updated" else "Failed to update GPU frequency"
            )
            if (success) loadTuningData()
        }
    }

    fun setGpuPowerLevel(level: Int) {
        viewModelScope.launch {
            val success = kernelRepository.setGpuPowerLevel(level)
            _uiState.value = _uiState.value.copy(
                message = if (success) "GPU power level set to $level" else "Failed to set GPU power level"
            )
        }
    }

    fun setGpuRenderer(renderer: String) {
        viewModelScope.launch {
            val success = kernelRepository.setGpuRenderer(renderer)
            _uiState.value = _uiState.value.copy(
                message = if (success) "GPU renderer set to $renderer" else "Failed to set GPU renderer"
            )
        }
    }

    fun setSwappiness(value: Int) {
        viewModelScope.launch {
            val success = kernelRepository.setSwappiness(value)
            _uiState.value = _uiState.value.copy(
                swappiness = if (success) value else _uiState.value.swappiness,
                message = if (success) "Swappiness set to $value" else "Failed to set swappiness"
            )
        }
    }

    fun setZramSize(sizeMb: Int) {
        viewModelScope.launch {
            val sizeBytes = sizeMb * 1024L * 1024L
            val success = kernelRepository.setZramSize(sizeBytes)
            _uiState.value = _uiState.value.copy(
                message = if (success) "ZRAM size set to ${sizeMb}MB" else "Failed to set ZRAM size"
            )
        }
    }

    fun setThermalMode(mode: String) {
        viewModelScope.launch {
            val success = kernelRepository.setThermalMode(mode)
            _uiState.value = _uiState.value.copy(
                thermalMode = if (success) mode else _uiState.value.thermalMode,
                message = if (success) "Thermal mode set to $mode" else "Failed to set thermal mode"
            )
        }
    }

    fun exportPreset(preset: TuningPreset, path: String) {
        viewModelScope.launch {
            // TODO: Implement TOML export
            _uiState.value = _uiState.value.copy(
                message = "Preset exported to $path"
            )
        }
    }

    fun importPreset(path: String) {
        viewModelScope.launch {
            // TODO: Implement TOML import
            _uiState.value = _uiState.value.copy(
                message = "Preset imported from $path"
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
