package id.xms.xtrakernelmanager.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.model.CPUInfo
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.data.model.PowerInfo
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.data.repository.BatteryRepository
import id.xms.xtrakernelmanager.data.repository.KernelRepository
import id.xms.xtrakernelmanager.data.repository.PowerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

  private val kernelRepository = KernelRepository()
  private val batteryRepository = BatteryRepository()
  private val powerRepository = PowerRepository()

  private val _cpuInfo = MutableStateFlow(CPUInfo())
  val cpuInfo: StateFlow<CPUInfo> = _cpuInfo.asStateFlow()

  private val _gpuInfo = MutableStateFlow(GPUInfo())
  val gpuInfo: StateFlow<GPUInfo> = _gpuInfo.asStateFlow()

  private val _batteryInfo = MutableStateFlow(BatteryInfo())
  val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

  private val _systemInfo = MutableStateFlow(SystemInfo())
  val systemInfo: StateFlow<SystemInfo> = _systemInfo.asStateFlow()

  private val _powerInfo = MutableStateFlow(PowerInfo())
  val powerInfo: StateFlow<PowerInfo> = _powerInfo.asStateFlow()

  private var context: Context? = null

  init {
    startMonitoring()
  }

  fun loadBatteryInfo(context: Context) {
    this.context = context
  }

  private fun startMonitoring() {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        while (true) {
          try {
            val cpu = kernelRepository.getCPUInfo()
            val gpu = kernelRepository.getGPUInfo()
            val sys = kernelRepository.getSystemInfo()

            // Update state (StateFlow is thread-safe)
            _cpuInfo.value = cpu
            _gpuInfo.value = gpu
            _systemInfo.value = sys

            context?.let {
              val bat = batteryRepository.getBatteryInfo(it)
              val pwr = powerRepository.getPowerInfo(it)
              _batteryInfo.value = bat
              _powerInfo.value = pwr
            }
          } catch (e: Exception) {
            e.printStackTrace()
          }

          delay(1000)
        }
      }
    }
  }

  /** Check if usage stats permission is granted */
  fun hasUsageStatsPermission(context: Context): Boolean {
    return powerRepository.hasUsageStatsPermission(context)
  }
}
