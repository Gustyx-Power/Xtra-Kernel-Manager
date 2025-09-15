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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import kotlin.text.isNotBlank

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

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        _isLoading.value = true
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

                viewModelScope.launch(Dispatchers.IO) {
            val systemInfoDeferred = async { systemRepo.getSystemInfo() }
            val kernelInfoDeferred = async { systemRepo.getKernelInfo() }
            val rootStatusDeferred = async { rootRepo.isRooted() }
            val cpuClustersDeferred = async { systemRepo.getCpuClusters() }
            val appVersionDeferred = async {
                try {
                    @SuppressLint("PackageManagerGetSignatures")
                    val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    pInfo.versionName
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error getting app version", e)
                    "N/A"
                }
            }

            _systemInfo.value = systemInfoDeferred.await()
            _kernelInfo.value = kernelInfoDeferred.await()
            _rootStatus.value = rootStatusDeferred.await()
            _cpuClusters.value = cpuClustersDeferred.await()
            _appVersion.value = appVersionDeferred.await()
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("HomeViewModel", "onCleared called.")
    }

    
}
