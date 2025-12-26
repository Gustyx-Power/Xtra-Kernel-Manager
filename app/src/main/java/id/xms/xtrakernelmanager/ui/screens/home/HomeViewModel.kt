package id.xms.xtrakernelmanager.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.model.CPUInfo
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.data.repository.BatteryRepository
import id.xms.xtrakernelmanager.data.repository.KernelRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val kernelRepository = KernelRepository()
    private val batteryRepository = BatteryRepository()

    private val _cpuInfo = MutableStateFlow(CPUInfo())
    val cpuInfo: StateFlow<CPUInfo> = _cpuInfo.asStateFlow()

    private val _gpuInfo = MutableStateFlow(GPUInfo())
    val gpuInfo: StateFlow<GPUInfo> = _gpuInfo.asStateFlow()

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val _systemInfo = MutableStateFlow(SystemInfo())
    val systemInfo: StateFlow<SystemInfo> = _systemInfo.asStateFlow()

    private var context: Context? = null

    init {
        startMonitoring()
    }

    fun loadBatteryInfo(context: Context) {
        this.context = context
    }

    private fun startMonitoring() {
        viewModelScope.launch {
            while (true) {
                _cpuInfo.value = kernelRepository.getCPUInfo()
                _gpuInfo.value = kernelRepository.getGPUInfo()
                _systemInfo.value = kernelRepository.getSystemInfo()
                context?.let {
                    _batteryInfo.value = batteryRepository.getBatteryInfo(it)
                }

                delay(1000)
            }
        }
    }
}
