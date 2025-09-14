package id.xms.xtrakernelmanager.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log // Import Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.xms.xtrakernelmanager.data.model.*
import id.xms.xtrakernelmanager.data.repository.RootRepository
import id.xms.xtrakernelmanager.data.repository.SystemRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val systemRepo: SystemRepository,
    private val rootRepo: RootRepository
) : ViewModel() {


    private val _cpuInfo = MutableStateFlow(
        RealtimeCpuInfo(cores = 0, governor = "N/A", freqs = emptyList(), temp = 0f, soc = "N/A", cpuLoadPercentage = null)
    )
    val cpuInfo: StateFlow<RealtimeCpuInfo> = _cpuInfo.asStateFlow()

    private val _gpuInfo = MutableStateFlow(
        RealtimeGpuInfo(usagePercentage = null, currentFreq = 0, maxFreq = 0)
    )
    val gpuInfo: StateFlow<RealtimeGpuInfo> = _gpuInfo.asStateFlow()

    private val _batteryInfo = MutableStateFlow<BatteryInfo?>(null)
    val batteryInfo: StateFlow<BatteryInfo?> = _batteryInfo.asStateFlow()

    private val _memoryInfo = MutableStateFlow<MemoryInfo?>(null)
    val memoryInfo: StateFlow<MemoryInfo?> = _memoryInfo.asStateFlow()

    private val _deepSleep = MutableStateFlow<DeepSleepInfo?>(null)
    val deepSleep: StateFlow<DeepSleepInfo?> = _deepSleep.asStateFlow()


    private val _kernelInfo = MutableStateFlow<KernelInfo?>(null)
    val kernelInfo: StateFlow<KernelInfo?> = _kernelInfo.asStateFlow()

    private val _rootStatus = MutableStateFlow<Boolean?>(null)
    val rootStatus: StateFlow<Boolean?> = _rootStatus.asStateFlow()

    private val _appVersion = MutableStateFlow<String?>("N/A")
    val appVersion: StateFlow<String?> = _appVersion.asStateFlow()

    private val _systemInfo = MutableStateFlow<SystemInfo?>(null)
    val systemInfo: StateFlow<SystemInfo?> = _systemInfo.asStateFlow()

    private val _isTitleAnimationDone = MutableStateFlow(false)
    val isTitleAnimationDone: StateFlow<Boolean> = _isTitleAnimationDone.asStateFlow()

    fun onTitleAnimationFinished() {
        _isTitleAnimationDone.value = true
    }

    private val _cpuClusters = MutableStateFlow<List<CpuCluster>>(emptyList())
    val cpuClusters: StateFlow<List<CpuCluster>> = _cpuClusters.asStateFlow()

    init {
        viewModelScope.launch {
            systemRepo.realtimeAggregatedInfoFlow
                .catch { e ->
                    Log.e("HomeViewModel", "Error in realtimeAggregatedInfoFlow: ${e.message}", e)

                }
                .collect { aggregatedInfo ->
                    _cpuInfo.value = aggregatedInfo.cpuInfo
                    _gpuInfo.value = aggregatedInfo.gpuInfo
                    _batteryInfo.value = aggregatedInfo.batteryInfo
                    _memoryInfo.value = aggregatedInfo.memoryInfo
                    _deepSleep.value = DeepSleepInfo(
                        uptime = aggregatedInfo.uptimeMillis,
                        deepSleep = aggregatedInfo.deepSleepMillis
                    )
                }
        }

        viewModelScope.launch {
            _systemInfo.value = systemRepo.getSystemInfo()
            _kernelInfo.value = systemRepo.getKernelInfo()
            _rootStatus.value = rootRepo.isRooted()
            _cpuClusters.value = systemRepo.getCpuClusters()

            try {
                @SuppressLint("PackageManagerGetSignatures")
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                _appVersion.value = pInfo.versionName
            } catch (e: Exception) {
                _appVersion.value = "N/A"
                Log.e("HomeViewModel", "Error getting app version", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("HomeViewModel", "onCleared called.")
    }
}
