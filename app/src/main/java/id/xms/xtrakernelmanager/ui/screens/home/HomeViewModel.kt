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
              val bat = BatteryRepository.getBatteryInfo(it)
              val pwrRepo = powerRepository.getPowerInfo(it)
              
              // Prefer Service-tracked real-time data if available (no permission needed)
              val serviceState = BatteryRepository.batteryState.value
              val useServiceData = serviceState.screenOnTime > 0 || serviceState.screenOffTime > 0
              
              val pwr = if (useServiceData) {
                  pwrRepo.copy(
                      screenOnTime = serviceState.screenOnTime,
                      screenOffTime = serviceState.screenOffTime,
                      deepSleepPercentage = 
                          if (serviceState.screenOnTime + serviceState.screenOffTime > 0) 
                              (serviceState.deepSleepTime.toFloat() / serviceState.screenOffTime.coerceAtLeast(1) * 100f).coerceIn(0f, 100f)
                          else 0f,
                      activeDrainRate = serviceState.activeDrainRate,
                      idleDrainRate = serviceState.idleDrainRate,
                      drainRate = serviceState.currentNow, // Uses native mA
                      batteryLevel = serviceState.level,
                      batteryTemp = serviceState.temp / 10f,
                      batteryVoltage = serviceState.voltage / 1000f,
                      isCharging = serviceState.isCharging
                  )
              } else {
                  pwrRepo
              }
              
              _batteryInfo.value = bat
              _powerInfo.value = pwr
            }
          } catch (e: Exception) {
            e.printStackTrace()
          }

          delay(500)
        }
      }
    }
  }

  /** Check if usage stats permission is granted */
  fun hasUsageStatsPermission(context: Context): Boolean {
    return powerRepository.hasUsageStatsPermission(context)
  }

  /** Check if accessibility service (Game Monitor) is enabled */
  fun isAccessibilityServiceEnabled(context: Context): Boolean {
    try {
      val accessibilityEnabled = android.provider.Settings.Secure.getInt(
          context.contentResolver,
          android.provider.Settings.Secure.ACCESSIBILITY_ENABLED,
          0
      )
      
      android.util.Log.d("HomeViewModel", "Accessibility enabled flag: $accessibilityEnabled")
      
      if (accessibilityEnabled == 0) return false
      
      val enabledServices = android.provider.Settings.Secure.getString(
          context.contentResolver,
          android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
      )
      
      android.util.Log.d("HomeViewModel", "Enabled services: $enabledServices")
      
      if (enabledServices.isNullOrEmpty()) return false
      
      // Check for both possible formats:
      // 1. Full component name: id.xms.xtrakernelmanager/id.xms.xtrakernelmanager.service.GameMonitorService
      // 2. Short format: id.xms.xtrakernelmanager/.service.GameMonitorService
      val packageName = context.packageName
      val serviceName = "GameMonitorService"
      
      val isEnabled = enabledServices.contains(packageName) && 
                      enabledServices.contains(serviceName)
      
      android.util.Log.d("HomeViewModel", "GameMonitorService enabled: $isEnabled")
      
      return isEnabled
    } catch (e: Exception) {
      android.util.Log.e("HomeViewModel", "Error checking accessibility: ${e.message}")
      return false
    }
  }
}
