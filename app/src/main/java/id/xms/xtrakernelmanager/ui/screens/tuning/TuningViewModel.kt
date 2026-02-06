package id.xms.xtrakernelmanager.ui.screens.tuning

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.model.*
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.data.preferences.TomlConfigManager
import id.xms.xtrakernelmanager.domain.SmartCPULocker
import id.xms.xtrakernelmanager.domain.native.NativeLib
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.domain.usecase.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class TuningViewModel(
    val preferencesManager: PreferencesManager,
    private val cpuUseCase: CPUControlUseCase = CPUControlUseCase(),
    private val gpuUseCase: GPUControlUseCase = GPUControlUseCase(),
    private val ramUseCase: RAMControlUseCase = RAMControlUseCase(),
    private val thermalUseCase: ThermalControlUseCase = ThermalControlUseCase(),
    private val overlayUseCase: id.xms.xtrakernelmanager.domain.usecase.GameOverlayUseCase =
        id.xms.xtrakernelmanager.domain.usecase.GameOverlayUseCase(),
    private val tomlManager: TomlConfigManager = TomlConfigManager(),
    private val kernelRepository: id.xms.xtrakernelmanager.data.repository.KernelRepository =
        id.xms.xtrakernelmanager.data.repository.KernelRepository(),
) : ViewModel() {

  // CPU Lock State - Smart CPU Locker instance
  private val smartCpuLocker = SmartCPULocker(cpuUseCase, thermalUseCase)

  class Factory(private val preferencesManager: PreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(TuningViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return TuningViewModel(preferencesManager) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }

  data class BlockDeviceState(
      val name: String,
      val currentScheduler: String,
      val availableSchedulers: List<String>,
  )

  private val _isRootAvailable = MutableStateFlow(false)
  val isRootAvailable: StateFlow<Boolean>
    get() = _isRootAvailable.asStateFlow()

  private val _blockDeviceStates = MutableStateFlow<List<BlockDeviceState>>(emptyList())
  val blockDeviceStates: StateFlow<List<BlockDeviceState>>
    get() = _blockDeviceStates.asStateFlow()

  private val _isLoading = MutableStateFlow(true)
  val isLoading: StateFlow<Boolean>
    get() = _isLoading.asStateFlow()

  private val _isImporting = MutableStateFlow(false)
  val isImporting: StateFlow<Boolean>
    get() = _isImporting.asStateFlow()

  private val _cpuClusters = MutableStateFlow<List<ClusterInfo>>(emptyList())
  val cpuClusters: StateFlow<List<ClusterInfo>>
    get() = _cpuClusters.asStateFlow()

  private val _cpuCores = MutableStateFlow<List<CoreInfo>>(emptyList())
  val cpuCores: StateFlow<List<CoreInfo>>
    get() = _cpuCores.asStateFlow()

  private val _cpuInfo = MutableStateFlow(id.xms.xtrakernelmanager.data.model.CPUInfo())
  val cpuInfo: StateFlow<id.xms.xtrakernelmanager.data.model.CPUInfo>
    get() = _cpuInfo.asStateFlow()

  private val _gpuInfo = MutableStateFlow(GPUInfo())
  val gpuInfo: StateFlow<GPUInfo>
    get() = _gpuInfo.asStateFlow()

  private val _cpuTemperature = MutableStateFlow(0f)
  val cpuTemperature: StateFlow<Float>
    get() = _cpuTemperature.asStateFlow()

  private val _cpuLoad = MutableStateFlow(0f)
  val cpuLoad: StateFlow<Float>
    get() = _cpuLoad.asStateFlow()

  private val _isMediatek = MutableStateFlow(false)
  val isMediatek: StateFlow<Boolean>
    get() = _isMediatek.asStateFlow()

  private val _thermalZones = MutableStateFlow<List<NativeLib.ThermalZone>>(emptyList())
  val thermalZones: StateFlow<List<NativeLib.ThermalZone>>
    get() = _thermalZones.asStateFlow()

  private val _currentThermalPreset = MutableStateFlow("class0")
  val currentThermalPreset: StateFlow<String>
    get() = _currentThermalPreset.asStateFlow()

  private val _isThermalSetOnBoot = MutableStateFlow(false)
  val isThermalSetOnBoot: StateFlow<Boolean>
    get() = _isThermalSetOnBoot.asStateFlow()

  private val _currentConfig = MutableStateFlow(TuningConfig())
  val currentConfig: StateFlow<TuningConfig>
    get() = _currentConfig.asStateFlow()

  private val _availableIOSchedulers = MutableStateFlow<List<String>>(emptyList())
  val availableIOSchedulers: StateFlow<List<String>>
    get() = _availableIOSchedulers.asStateFlow()

  private val _availableTCPCongestion = MutableStateFlow<List<String>>(emptyList())
  val availableTCPCongestion: StateFlow<List<String>>
    get() = _availableTCPCongestion.asStateFlow()

  // ============ MEMORY STATS ============

  private val _currentSwappiness = MutableStateFlow(60)
  val currentSwappiness: StateFlow<Int>
    get() = _currentSwappiness.asStateFlow()

  private val _currentDirtyRatio = MutableStateFlow(20)
  val currentDirtyRatio: StateFlow<Int>
    get() = _currentDirtyRatio.asStateFlow()

  private val _currentMinFreeMem = MutableStateFlow(8192)
  val currentMinFreeMem: StateFlow<Int>
    get() = _currentMinFreeMem.asStateFlow()

  // Initialize with cached value if available (instant UI response)
  private val _zramStatus =
      MutableStateFlow(
          ramUseCase.getZramStatusCached() ?: RAMControlUseCase.ZramStatus(false, 0, 0, 0, 0f)
      )
  val zramStatus: StateFlow<RAMControlUseCase.ZramStatus>
    get() = _zramStatus.asStateFlow()

  private val _swapFileStatus = MutableStateFlow(RAMControlUseCase.SwapFileStatus(false, 0, 0))
  val swapFileStatus: StateFlow<RAMControlUseCase.SwapFileStatus>
    get() = _swapFileStatus.asStateFlow()

  private val _memoryStats = MutableStateFlow(RAMControlUseCase.MemoryStats(0, 0, 0, 0, 0, 0, 0))
  val memoryStats: StateFlow<RAMControlUseCase.MemoryStats>
    get() = _memoryStats.asStateFlow()

  // Private DNS
  private val _currentDNS = MutableStateFlow("Automatic")
  val currentDNS: StateFlow<String>
    get() = _currentDNS.asStateFlow()

  // Network Status
  private val _networkStatus = MutableStateFlow("WiFi: Connected")
  val networkStatus: StateFlow<String>
    get() = _networkStatus.asStateFlow()

  val availableDNS =
      listOf(
          "Automatic" to "",
          "Off" to "off",
          "Google" to "dns.google",
          "Cloudflare" to "1dot1dot1dot1.cloudflare-dns.com",
          "AdGuard" to "dns.adguard.com",
          "Quad9" to "dns.quad9.net",
      )

  // Global Profile State
  private val _selectedProfile = MutableStateFlow("Balance")
  val selectedProfile: StateFlow<String>
    get() = _selectedProfile.asStateFlow()

  val availableProfiles = listOf("Performance", "Balance", "Powersave", "Battery")

  fun applyGlobalProfile(profile: String) {
    viewModelScope.launch(Dispatchers.IO) {
      _selectedProfile.value = profile

      // Map profile to CPU governor
      val profileGovernor =
          when (profile) {
            "Performance" -> "performance"
            "Powersave" -> "powersave"
            "Battery" -> "conservative"
            else -> null // Balance logic determined per cluster
          }

      // Apply governor to all CPU clusters
      _cpuClusters.value.forEach { cluster ->
        val targetGovernor = profileGovernor ?: determineBalanceGovernor(cluster.availableGovernors)
        cpuUseCase.setClusterGovernor(cluster.clusterNumber, targetGovernor)
      }

      // Refresh values after applying
      refreshDynamicValues()
    }
  }

  // Determine best balanced governor from available options
  private fun determineBalanceGovernor(available: List<String>): String {
    return when {
      available.contains("schedutil") -> "schedutil"
      available.contains("walt") -> "walt" // Qualcomm/Google
      available.contains("pixutil") -> "pixutil" // Pixel
      available.contains("interactive") -> "interactive"
      available.contains("ondemand") -> "ondemand"
      else ->
          available.firstOrNull { it != "performance" && it != "powersave" && it != "conservative" }
              ?: "performance"
    }
  }

  // CPU Frequency Control - sets min/max freq for a specific cluster
  fun setCpuClusterFrequency(clusterIndex: Int, minFreqMhz: Int, maxFreqMhz: Int) {
    viewModelScope.launch(Dispatchers.IO) {
      // Validate frequencies against available frequencies
      val cluster = _cpuClusters.value.find { it.clusterNumber == clusterIndex }
      if (cluster != null) {
        val validMinFreq = if (cluster.availableFrequencies.contains(minFreqMhz)) {
          minFreqMhz
        } else {
          // Find closest valid frequency
          cluster.availableFrequencies.minByOrNull { abs(it - minFreqMhz) } ?: minFreqMhz
        }
        
        val validMaxFreq = if (cluster.availableFrequencies.contains(maxFreqMhz)) {
          maxFreqMhz
        } else {
          // Find closest valid frequency
          cluster.availableFrequencies.minByOrNull { abs(it - maxFreqMhz) } ?: maxFreqMhz
        }
        
        cpuUseCase.setClusterFrequency(clusterIndex, validMinFreq, validMaxFreq)
        
        // Save configuration if set-on-boot is enabled
        val cpuSetOnBoot = preferencesManager.getCpuSetOnBoot().first()
        if (cpuSetOnBoot) {
          preferencesManager.setClusterMinFreq(clusterIndex, validMinFreq)
          preferencesManager.setClusterMaxFreq(clusterIndex, validMaxFreq)
        }
      } else {
        // Fallback to original behavior if cluster not found
        cpuUseCase.setClusterFrequency(clusterIndex, minFreqMhz, maxFreqMhz)
        
        val cpuSetOnBoot = preferencesManager.getCpuSetOnBoot().first()
        if (cpuSetOnBoot) {
          preferencesManager.setClusterMinFreq(clusterIndex, minFreqMhz)
          preferencesManager.setClusterMaxFreq(clusterIndex, maxFreqMhz)
        }
      }
      
      refreshDynamicValues()
    }
  }

  // CPU Governor Control - sets governor for a specific cluster
  fun setCpuClusterGovernor(clusterIndex: Int, governor: String) {
    viewModelScope.launch(Dispatchers.IO) {
      cpuUseCase.setClusterGovernor(clusterIndex, governor)
      
      // Save configuration if set-on-boot is enabled
      val cpuSetOnBoot = preferencesManager.getCpuSetOnBoot().first()
      if (cpuSetOnBoot) {
        preferencesManager.setClusterGovernor(clusterIndex, governor)
      }
      
      refreshDynamicValues()
    }
  }

  // CPU Core Online/Offline Control
  fun setCpuCoreOnline(coreId: Int, online: Boolean) {
    viewModelScope.launch(Dispatchers.IO) {
      cpuUseCase.setCoreOnline(coreId, online)
      refreshDynamicValues()
    }
  }

  fun setPrivateDNS(name: String, hostname: String) {
    viewModelScope.launch(Dispatchers.IO) {
      if (hostname == "off") {
        RootManager.executeCommand("settings put global private_dns_mode off")
      } else if (hostname.isEmpty()) {
        RootManager.executeCommand("settings put global private_dns_mode opportunistic")
      } else {
        RootManager.executeCommand("settings put global private_dns_mode hostname")
        RootManager.executeCommand("settings put global private_dns_specifier $hostname")
      }
      _currentDNS.value = name
    }
  }

  private fun loadDNS() {
    viewModelScope.launch(Dispatchers.IO) {
      val mode =
          RootManager.executeCommand("settings get global private_dns_mode").getOrNull()?.trim()
              ?: "off"
      val specifier =
          RootManager.executeCommand("settings get global private_dns_specifier")
              .getOrNull()
              ?.trim() ?: ""

      val dnsName =
          when {
            mode == "off" -> "Off"
            mode == "hostname" -> availableDNS.find { it.second == specifier }?.first ?: "Custom"
            else -> "Automatic"
          }
      _currentDNS.value = dnsName
    }
  }

  private fun startNetworkMonitoring() {
    viewModelScope.launch(Dispatchers.IO) {
      while (true) {
        val route = RootManager.executeCommand("ip route get 8.8.8.8").getOrNull() ?: ""
        val status =
            when {
              route.contains("dev wlan") -> "WiFi: Connected"
              route.contains("dev rmnet") ||
                  route.contains("dev ccmni") ||
                  route.contains("dev vvlan") -> "Mobile Data"
              route.contains("via") -> "Online"
              else -> "Offline"
            }
        _networkStatus.value = status
        delay(5000)
      }
    }
  }

  // Device Hostname
  private val _currentHostname = MutableStateFlow("")
  val currentHostname: StateFlow<String>
    get() = _currentHostname.asStateFlow()

  fun loadHostname() {
    viewModelScope.launch(Dispatchers.IO) {
      val hostname = NativeLib.getSystemProperty("net.hostname") ?: ""
      _currentHostname.value = hostname
    }
  }

  fun setHostname(hostname: String) {
    viewModelScope.launch(Dispatchers.IO) {
      if (hostname.isNotBlank()) {
        RootManager.executeCommand("setprop net.hostname $hostname")
        _currentHostname.value = hostname
      }
    }
  }

  private val _availableCompressionAlgorithms = MutableStateFlow<List<String>>(emptyList())
  val availableCompressionAlgorithms: StateFlow<List<String>>
    get() = _availableCompressionAlgorithms.asStateFlow()

  private val _currentIOScheduler = MutableStateFlow<String>("")
  val currentIOScheduler: StateFlow<String>
    get() = _currentIOScheduler.asStateFlow()

  private val _currentTCPCongestion = MutableStateFlow<String>("")
  val currentTCPCongestion: StateFlow<String>
    get() = _currentTCPCongestion.asStateFlow()

  private val _currentCompressionAlgorithm = MutableStateFlow<String>("lz4")
  val currentCompressionAlgorithm: StateFlow<String>
    get() = _currentCompressionAlgorithm.asStateFlow()

  private val _currentPerfMode = MutableStateFlow("balance")
  val currentPerfMode: StateFlow<String>
    get() = _currentPerfMode.asStateFlow()

  private val _clusterStates = MutableStateFlow<Map<Int, ClusterUIState>>(emptyMap())
  val clusterStates: StateFlow<Map<Int, ClusterUIState>>
    get() = _clusterStates.asStateFlow()

  // GPU Lock State - persists across UI changes
  private val _isGpuFrequencyLocked = MutableStateFlow(false)
  val isGpuFrequencyLocked: StateFlow<Boolean>
    get() = _isGpuFrequencyLocked.asStateFlow()

  private val _lockedGpuMinFreq = MutableStateFlow(0)
  val lockedGpuMinFreq: StateFlow<Int>
    get() = _lockedGpuMinFreq.asStateFlow()

  private val _lockedGpuMaxFreq = MutableStateFlow(0)
  val lockedGpuMaxFreq: StateFlow<Int>
    get() = _lockedGpuMaxFreq.asStateFlow()

  // CPU Lock State - persists across UI changes
  private val _isCpuFrequencyLocked = MutableStateFlow(false)
  val isCpuFrequencyLocked: StateFlow<Boolean>
    get() = _isCpuFrequencyLocked.asStateFlow()

  // Create safe default instances to prevent null access
  private val defaultLockStatus = LockStatus(
      isLocked = false,
      policyType = LockPolicyType.MANUAL,
      thermalPolicy = "PolicyB",
      isThermalOverrideActive = false,
      lastTemperature = 0f,
      lastUpdate = System.currentTimeMillis(),
      clusterCount = 0,
      lockedClusters = emptyList(),
      retryCount = 0,
      canRetry = false
  )

  private val _cpuLockStatus = MutableStateFlow(defaultLockStatus)
  val cpuLockStatus: StateFlow<LockStatus>
    get() = _cpuLockStatus.asStateFlow()

  private val _thermalEvents = MutableStateFlow<ThermalEvent?>(null)
  val thermalEvents: StateFlow<ThermalEvent?>
    get() = _thermalEvents.asStateFlow()

  // User notifications
  private val _cpuLockNotifications = MutableStateFlow<String?>(null)
  val cpuLockNotifications: StateFlow<String?>
    get() = _cpuLockNotifications.asStateFlow()

  // Auto-refresh control
  private val _isRefreshEnabled = MutableStateFlow(true)

  // User interaction control to prevent refresh conflicts
  private val _isUserAdjusting = MutableStateFlow(false)
  private val _isApplyingConfig = MutableStateFlow(false) // New flag to prevent refresh during config application

  // Static data cache flags
  private var staticDataLoaded = false

  private var deviceInfoCache: Triple<String, String, String>? = null

  init {
    viewModelScope.launch {
      _currentIOScheduler.value = preferencesManager.getIOScheduler().first()
      _currentTCPCongestion.value = preferencesManager.getTCPCongestion().first()
      _currentPerfMode.value = preferencesManager.getPerfMode().first()
      _isThermalSetOnBoot.value = preferencesManager.getThermalSetOnBoot().first()

      // Load thermal preset - prefer system detection, fallback to saved
      val savedThermal = preferencesManager.getThermalPreset().first()
      if (savedThermal == "class0" || savedThermal.isEmpty()) {
        // Detect current thermal mode from system
        withContext(Dispatchers.IO) {
          _currentThermalPreset.value = thermalUseCase.getCurrentThermalMode()
        }
      } else {
        _currentThermalPreset.value = savedThermal
      }

      // Load DNS state
      loadDNS()
      loadHostname()

      // Load saved GPU lock state
      loadGpuLockState()

      // Load CPU lock state
      loadCpuLockState()

      checkRootAndLoadData()
      applySavedCoreStates()
      startAutoRefresh()
    }
  }

  private suspend fun loadGpuLockState() {
    val isLocked = preferencesManager.isGpuFrequencyLocked().first()
    val minFreq = preferencesManager.getGpuLockedMinFreq().first()
    val maxFreq = preferencesManager.getGpuLockedMaxFreq().first()

    if (isLocked && minFreq > 0 && maxFreq > 0) {
      // Restore state to ViewModel
      _isGpuFrequencyLocked.value = true
      _lockedGpuMinFreq.value = minFreq
      _lockedGpuMaxFreq.value = maxFreq

      // Re-apply the lock to the system
      gpuUseCase.lockGPUFrequency(minFreq, maxFreq)
    }
  }

  private suspend fun checkRootAndLoadData() {
    _isLoading.value = true
    _isRootAvailable.value = RootManager.isRootAvailable()

    if (_isRootAvailable.value) {
      loadSystemInfo()
      // Show UI now (clusters/GPU loaded), CPU load will update when ready
      _isLoading.value = false
      refreshCurrentValues()
      startNetworkMonitoring()
    } else {
      _isLoading.value = false
    }
  }

  private suspend fun startAutoRefresh() {
    while (true) {
      delay(5000) // 5 seconds refresh interval
      if (_isRootAvailable.value && !_isLoading.value && _isRefreshEnabled.value) {
        refreshDynamicValues() // Only refresh dynamic data
      }
    }
  }

  // Pause/Resume auto-refresh for screen lifecycle
  fun pauseAutoRefresh() {
    _isRefreshEnabled.value = false
  }

  fun resumeAutoRefresh() {
    _isRefreshEnabled.value = true
    viewModelScope.launch { refreshDynamicValues() }
  }

  // Full refresh - includes static data (called on init)
  private suspend fun refreshCurrentValues() {
    val updatedClusters = cpuUseCase.detectClusters()
    _cpuClusters.value = updatedClusters
    
    // Load core info
    val updatedCores = cpuUseCase.getAllCoreInfo()
    _cpuCores.value = updatedCores

    if (!_isMediatek.value) {
      _gpuInfo.value = gpuUseCase.getGPUDynamicInfo(preferencesManager.getContext())
    }

    val states = mutableMapOf<Int, ClusterUIState>()
    updatedClusters.forEach { cluster ->
      states[cluster.clusterNumber] =
          ClusterUIState(
              minFreq = cluster.currentMinFreq.toFloat(),
              maxFreq = cluster.currentMaxFreq.toFloat(),
              governor = cluster.governor,
          )
    }
    _clusterStates.value = states

    // Load thermal zones first (fast native call)
    _thermalZones.value = NativeLib.readThermalZones()

    // Load static data once (IO schedulers, TCP congestion)
    if (!staticDataLoaded) {
      val currentIO = getCurrentIOScheduler()
      if (currentIO.isNotEmpty()) {
        _currentIOScheduler.value = currentIO
      }

      val currentTCP = getCurrentTCPCongestion()
      if (currentTCP.isNotEmpty()) {
        _currentTCPCongestion.value = currentTCP
      }
      staticDataLoaded = true
    }

    // Load CPU load/temp (can be slow, but UI is already visible)
    val cpuInfo = kernelRepository.getCPUInfo()
    _cpuInfo.value = cpuInfo
    _cpuLoad.value = cpuInfo.totalLoad
    _cpuTemperature.value = cpuInfo.temperature
  }

  // Lightweight refresh - only dynamic data (called every 5s)
  private suspend fun refreshDynamicValues() {
    // Skip refresh if user is currently adjusting values or applying config
    if (_isUserAdjusting.value || _isApplyingConfig.value) {
      return
    }
    
    val updatedClusters = cpuUseCase.detectClusters()
    _cpuClusters.value = updatedClusters
    Log.d("TuningViewModel", "Refreshed clusters: ${updatedClusters.size} clusters")
    
    // Refresh core info
    val updatedCores = cpuUseCase.getAllCoreInfo()
    _cpuCores.value = updatedCores

    if (!_isMediatek.value) {
      _gpuInfo.value = gpuUseCase.getGPUDynamicInfo(preferencesManager.getContext())
    }

    // Update temperature
    _cpuTemperature.value = overlayUseCase.getTemperature()

    // Update CPU load and info - use KernelRepository like MaterialHomeScreen (proper delta calc)
    val cpuInfo = kernelRepository.getCPUInfo()
    _cpuInfo.value = cpuInfo
    _cpuLoad.value = cpuInfo.totalLoad

    val states = mutableMapOf<Int, ClusterUIState>()
    updatedClusters.forEach { cluster ->
      states[cluster.clusterNumber] =
          ClusterUIState(
              minFreq = cluster.currentMinFreq.toFloat(),
              maxFreq = cluster.currentMaxFreq.toFloat(),
              governor = cluster.governor,
          )
    }
    _clusterStates.value = states

    _thermalZones.value = NativeLib.readThermalZones()

    // Update memory stats for realtime monitoring
    _zramStatus.value = ramUseCase.getZramStatus()
    _swapFileStatus.value = ramUseCase.getSwapFileStatus()
    _memoryStats.value = ramUseCase.getMemoryStats()
  }

  private suspend fun loadSystemInfo() =
      withContext(Dispatchers.IO) {
        // Launch independent tasks in parallel
        val clustersDeferred = async { cpuUseCase.detectClusters() }
        val mediatekDeferred = async { detectMediatek() }
        val blockDevicesDeferred = async { loadBlockDeviceStatesParallel() }
        val tcpDeferred = async { getCurrentTCPCongestion() }
        val availableTCPDeferred = async { getAvailableTCPCongestion() }
        val compAlgoDeferred = async { ramUseCase.getCurrentCompressionAlgorithm() }
        val memoryInfoDeferred = async { loadMemoryInfoParallel() }
        val availableAlgorithmsDeferred = async { ramUseCase.getAvailableCompressionAlgorithms() }

        // Await results
        val clusters = clustersDeferred.await()
        val isMtk = mediatekDeferred.await()

        _cpuClusters.value = clusters
        _isMediatek.value = isMtk

        // GPU Info depends on Mediatek check
        if (!isMtk) {
          val staticInfo = gpuUseCase.getGPUStaticInfo()
          _gpuInfo.value = gpuUseCase.getGPUDynamicInfo(preferencesManager.getContext())
        }
        _blockDeviceStates.value = blockDevicesDeferred.await()
        
        // Sync IO schedulers to liquid flows
        val states = _blockDeviceStates.value
        val sda = states.find { it.name == "sda" } ?: states.firstOrNull()
        if (sda != null) {
          _availableIOSchedulers.value = sda.availableSchedulers
          _currentIOScheduler.value = sda.currentScheduler
        }

        val currentTCP = tcpDeferred.await()
        if (currentTCP.isNotEmpty()) {
          _currentTCPCongestion.value = currentTCP
        }

        val availableTCP = availableTCPDeferred.await()
        if (availableTCP.isNotEmpty()) {
          _availableTCPCongestion.value = availableTCP
        }

        val currentComp = compAlgoDeferred.await()
        if (currentComp.isNotEmpty()) {
          _currentCompressionAlgorithm.value = currentComp
        }

        _availableCompressionAlgorithms.value = availableAlgorithmsDeferred.await()

        // Wait for memory info (it updates stateflows internally)
        memoryInfoDeferred.await()

        refreshCurrentValues()
      }

  /** Load current memory settings from system (Parallelized internal calls) */
  private suspend fun loadMemoryInfoParallel() = coroutineScope {
    launch { _currentSwappiness.value = ramUseCase.getSwappiness() }
    launch { _currentDirtyRatio.value = ramUseCase.getDirtyRatio() }
    launch { _currentMinFreeMem.value = ramUseCase.getMinFreeMem() }
    launch { _zramStatus.value = ramUseCase.getZramStatus() }
    launch { _swapFileStatus.value = ramUseCase.getSwapFileStatus() }
    launch { _memoryStats.value = ramUseCase.getMemoryStats() }
  }

  // Deprecated sequential version, kept if needed but unused in new flow
  private suspend fun loadMemoryInfo() {
    loadMemoryInfoParallel()
  }

  private suspend fun detectMediatek(): Boolean {
    val hwPlatform = NativeLib.getSystemProperty("ro.hardware")?.lowercase() ?: ""
    val soc = NativeLib.getSystemProperty("ro.board.platform")?.lowercase() ?: ""

    return hwPlatform.contains("mt") ||
        soc.contains("mt") ||
        hwPlatform.contains("mediatek") ||
        soc.contains("mediatek")
  }

  private suspend fun loadBlockDeviceStatesParallel(): List<BlockDeviceState> = coroutineScope {
    val devices = listOf("sda", "sdb", "sdc", "sdd", "sde", "sdf", "mmcblk0", "dm-0")

    // Check all devices in parallel
    devices
        .map { device ->
          async {
            // Try direct file read first (faster), fallback to root if needed
            var output: String? =
                try {
                  java.io.File("/sys/block/$device/queue/scheduler").readText().trim()
                } catch (e: Exception) {
                  null
                }

            if (output == null) {
              output =
                  RootManager.executeCommand("cat /sys/block/$device/queue/scheduler 2>/dev/null")
                      .getOrNull()
            }

            if (!output.isNullOrBlank()) {
              val match = Regex("\\[(.*?)]").find(output)
              var current = match?.groupValues?.get(1) ?: ""
              val available =
                  output.replace("[", "").replace("]", "").split("\\s+".toRegex()).filter {
                    it.isNotBlank()
                  }

              if (current.isEmpty() && available.size == 1) {
                current = available.first()
              }

              if (available.isNotEmpty()) {
                BlockDeviceState(device, current, available)
              } else null
            } else null
          }
        }
        .awaitAll()
        .filterNotNull()
  }

  // Wrapper for existing call to maintain compatibility if called elsewhere
  private suspend fun loadBlockDeviceStates() {
    _blockDeviceStates.value = loadBlockDeviceStatesParallel()

    // Sync liquid flows
    val states = _blockDeviceStates.value
    val sda = states.find { it.name == "sda" } ?: states.firstOrNull()
    if (sda != null) {
      _availableIOSchedulers.value = sda.availableSchedulers
      _currentIOScheduler.value = sda.currentScheduler
    }
  }

  fun setDeviceIOScheduler(device: String, scheduler: String) {
    viewModelScope.launch(Dispatchers.IO) {
      RootManager.executeCommand("echo $scheduler > /sys/block/$device/queue/scheduler")
      loadBlockDeviceStates()
    }
  }

  private suspend fun getAvailableTCPCongestion(): List<String> {
    val congestion =
        RootManager.executeCommand(
                "cat /proc/sys/net/ipv4/tcp_available_congestion_control 2>/dev/null"
            )
            .getOrNull() ?: return emptyList()
    return congestion.split("\\s+".toRegex()).filter { it.isNotBlank() }
  }

  private suspend fun getCurrentIOScheduler(): String {
    // Try multiple block devices
    val blockDevices = listOf("sda", "sdb", "sdc", "sdd", "sde", "sdf", "mmcblk0", "dm-0")
    for (device in blockDevices) {
      val output =
          RootManager.executeCommand("cat /sys/block/$device/queue/scheduler 2>/dev/null")
              .getOrNull()
      if (!output.isNullOrBlank()) {
        val match = Regex("\\[(.*?)]").find(output)
        val result = match?.groupValues?.get(1) ?: ""
        if (result.isNotEmpty()) {
          Log.d("TuningViewModel", "IO Scheduler from $device: $result")
          return result
        }
      }
    }

    // Fallback to saved preference
    val saved = preferencesManager.getIOScheduler().first()
    Log.d("TuningViewModel", "IO Scheduler from prefs (fallback): $saved")
    return saved
  }

  private suspend fun getCurrentTCPCongestion(): String {
    val output =
        RootManager.executeCommand("cat /proc/sys/net/ipv4/tcp_congestion_control 2>/dev/null")
            .getOrNull()
            ?.trim()
    Log.d("TuningViewModel", "TCP Congestion raw output: $output")

    if (output.isNullOrEmpty()) {
      val saved = preferencesManager.getTCPCongestion().first()
      Log.d("TuningViewModel", "TCP Congestion from prefs (fallback): $saved")
      return saved
    }

    return output
  }

  fun updateClusterUIState(cluster: Int, minFreq: Float, maxFreq: Float) {
    val currentStates = _clusterStates.value.toMutableMap()
    val currentState = currentStates[cluster] ?: ClusterUIState()
    currentStates[cluster] = currentState.copy(minFreq = minFreq, maxFreq = maxFreq)
    _clusterStates.value = currentStates
  }

  fun updateClusterGovernor(cluster: Int, governor: String) {
    val currentStates = _clusterStates.value.toMutableMap()
    val currentState = currentStates[cluster] ?: ClusterUIState()
    currentStates[cluster] = currentState.copy(governor = governor)
    _clusterStates.value = currentStates
  }

  fun setCPUFrequency(cluster: Int, minFreq: Int, maxFreq: Int) {
    viewModelScope.launch {
      val result = cpuUseCase.setClusterFrequency(cluster, minFreq, maxFreq)
      if (result.isSuccess) {
        updateClusterUIState(cluster, minFreq.toFloat(), maxFreq.toFloat())
        
        // Save configuration if set-on-boot is enabled
        val cpuSetOnBoot = preferencesManager.getCpuSetOnBoot().first()
        if (cpuSetOnBoot) {
          preferencesManager.setClusterMinFreq(cluster, minFreq)
          preferencesManager.setClusterMaxFreq(cluster, maxFreq)
        }
      }
    }
  }

  fun setCPUGovernor(cluster: Int, governor: String) {
    viewModelScope.launch {
      val result = cpuUseCase.setClusterGovernor(cluster, governor)
      if (result.isSuccess) {
        updateClusterGovernor(cluster, governor)
        
        // Save configuration if set-on-boot is enabled
        val cpuSetOnBoot = preferencesManager.getCpuSetOnBoot().first()
        if (cpuSetOnBoot) {
          preferencesManager.setClusterGovernor(cluster, governor)
        }
      }
    }
  }

  fun disableCPUCore(core: Int, disable: Boolean) {
    viewModelScope.launch {
      preferencesManager.setCpuCoreEnabled(core, !disable)
      // Apply immediately to system
      cpuUseCase.setCoreOnline(core, !disable)
      
      // Note: Core enable/disable state is already saved in preferences above
      // and will be applied on boot regardless of CPU set-on-boot setting
    }
  }

  fun setCpuCoreEnabled(core: Int, enabled: Boolean) {
    viewModelScope.launch {
      preferencesManager.setCpuCoreEnabled(core, enabled)
      // Apply immediately to system
      cpuUseCase.setCoreOnline(core, enabled)
    }
  }

  private suspend fun applySavedCoreStates() = coroutineScope {
    (0..7)
        .map { core ->
          launch(Dispatchers.IO) {
            val enabled = preferencesManager.isCpuCoreEnabled(core).first()
            cpuUseCase.setCoreOnline(core, enabled)
          }
        }
        .joinAll()
    refreshCurrentValues()
  }

  suspend fun applyAllConfigurations() {
    for (core in 0..7) {
      val enabled = preferencesManager.isCpuCoreEnabled(core).first()
      cpuUseCase.setCoreOnline(core, enabled)
    }

    val thermalPreset = preferencesManager.getThermalPreset().first()
    val setOnBoot = preferencesManager.getThermalSetOnBoot().first()
    if (thermalPreset.isNotEmpty()) {
      thermalUseCase.setThermalMode(thermalPreset, setOnBoot)
    }

    val ioScheduler = preferencesManager.getIOScheduler().first()
    if (ioScheduler.isNotBlank()) {
      // Apply to all block devices
      listOf("sda", "sdb", "sdc", "sdd", "sde", "sdf", "mmcblk0", "dm-0").forEach { device ->
        RootManager.executeCommand(
            "echo $ioScheduler > /sys/block/$device/queue/scheduler 2>/dev/null"
        )
      }
    }

    val tcpCongestion = preferencesManager.getTCPCongestion().first()
    if (tcpCongestion.isNotBlank()) {
      RootManager.executeCommand("echo $tcpCongestion > /proc/sys/net/ipv4/tcp_congestion_control")
    }

    val ramConfig = preferencesManager.getRamConfig().first()
    ramUseCase.setSwappiness(ramConfig.swappiness)
    ramUseCase.setZRAMSize(ramConfig.zramSize.toLong() * 1024L * 1024L)
    ramUseCase.setDirtyRatio(ramConfig.dirtyRatio)
    ramUseCase.setMinFreeMem(ramConfig.minFreeMem)
    ramUseCase.setSwapFileSizeMb(ramConfig.swapSize)
  }

  fun setGPUFrequency(minFreq: Int, maxFreq: Int) {
    viewModelScope.launch { gpuUseCase.setGPUFrequency(minFreq, maxFreq) }
  }

  fun setGPUPowerLevel(level: Int) {
    viewModelScope.launch { gpuUseCase.setGPUPowerLevel(level) }
  }

  fun lockGPUFrequency(minFreq: Int, maxFreq: Int) {
    viewModelScope.launch {
      gpuUseCase.lockGPUFrequency(minFreq, maxFreq)
      _lockedGpuMinFreq.value = minFreq
      _lockedGpuMaxFreq.value = maxFreq
      _isGpuFrequencyLocked.value = true
      // Save to DataStore for persistence
      preferencesManager.setGpuLockState(true, minFreq, maxFreq)
    }
  }

  fun unlockGPUFrequency() {
    viewModelScope.launch {
      gpuUseCase.unlockGPUFrequency()
      _isGpuFrequencyLocked.value = false
      _lockedGpuMinFreq.value = 0
      _lockedGpuMaxFreq.value = 0
      // Clear from DataStore
      preferencesManager.clearGpuLockState()
    }
  }

  fun setGPURenderer(renderer: String) {
    viewModelScope.launch { gpuUseCase.setGPURenderer(renderer) }
  }

  suspend fun verifyRendererChange(renderer: String): Boolean {
    return gpuUseCase.verifyRendererChange(renderer).getOrDefault(false)
  }

  fun performReboot() {
    viewModelScope.launch { gpuUseCase.performReboot() }
  }

  fun setThermalPreset(preset: String, setOnBoot: Boolean) {
    viewModelScope.launch(Dispatchers.IO) {
      thermalUseCase.setThermalMode(preset, setOnBoot)
      preferencesManager.setThermalConfig(preset, setOnBoot)
      _currentThermalPreset.value = preset
      _isThermalSetOnBoot.value = setOnBoot
    }
  }

  fun setCpuSetOnBoot(enabled: Boolean) {
    viewModelScope.launch(Dispatchers.IO) { preferencesManager.setCpuSetOnBoot(enabled) }
  }

  // I/O Set on Boot
  fun setIOSetOnBoot(enabled: Boolean) {
    viewModelScope.launch(Dispatchers.IO) { preferencesManager.setIOSetOnBoot(enabled) }
  }

  // TCP Set on Boot
  fun setTCPSetOnBoot(enabled: Boolean) {
    viewModelScope.launch(Dispatchers.IO) { preferencesManager.setTCPSetOnBoot(enabled) }
  }

  // RAM Set on Boot
  fun setRAMSetOnBoot(enabled: Boolean) {
    viewModelScope.launch(Dispatchers.IO) { preferencesManager.setRAMSetOnBoot(enabled) }
  }

  // Additional Set on Boot
  fun setAdditionalSetOnBoot(enabled: Boolean) {
    viewModelScope.launch(Dispatchers.IO) { preferencesManager.setAdditionalSetOnBoot(enabled) }
  }

  fun setIOScheduler(scheduler: String) {
    viewModelScope.launch(Dispatchers.IO) {
      Log.d("TuningViewModel", "Setting IO Scheduler to: $scheduler")

      _currentIOScheduler.value = scheduler

      // Apply to all available block devices
      val blockDevices = listOf("sda", "sdb", "sdc", "sdd", "sde", "sdf", "mmcblk0", "dm-0")
      var anySuccess = false
      for (device in blockDevices) {
        val result =
            RootManager.executeCommand(
                "echo $scheduler > /sys/block/$device/queue/scheduler 2>/dev/null"
            )
        if (result.isSuccess) {
          Log.d("TuningViewModel", "IO Scheduler set for $device")
          anySuccess = true
        }
      }

      if (anySuccess) {
        preferencesManager.setIOScheduler(scheduler)
        
        // Save set-on-boot configuration if enabled
        val ioSetOnBoot = preferencesManager.getIOSetOnBoot().first()
        if (ioSetOnBoot) {
          Log.d("TuningViewModel", "I/O set-on-boot enabled, configuration will be applied on boot")
        }
        
        delay(200)
        val verified = getCurrentIOScheduler()
        Log.d("TuningViewModel", "Verified IO Scheduler: $verified")
        if (verified.isNotEmpty()) {
          _currentIOScheduler.value = verified
        }
      }
    }
  }

  fun setTCPCongestion(congestion: String) {
    viewModelScope.launch(Dispatchers.IO) {
      Log.d("TuningViewModel", "Setting TCP Congestion to: $congestion")

      _currentTCPCongestion.value = congestion

      val result =
          RootManager.executeCommand("echo $congestion > /proc/sys/net/ipv4/tcp_congestion_control")
      Log.d("TuningViewModel", "TCP Congestion command result: ${result.isSuccess}")

      if (result.isSuccess) {
        preferencesManager.setTCPCongestion(congestion)
        
        // Save set-on-boot configuration if enabled
        val tcpSetOnBoot = preferencesManager.getTCPSetOnBoot().first()
        if (tcpSetOnBoot) {
          Log.d("TuningViewModel", "TCP set-on-boot enabled, configuration will be applied on boot")
        }
        
        delay(200)
        val verified = getCurrentTCPCongestion()
        Log.d("TuningViewModel", "Verified TCP Congestion: $verified")
        if (verified.isNotEmpty()) {
          _currentTCPCongestion.value = verified
        }
      }
    }
  }

  fun setRAMParameters(config: RAMConfig) {
    viewModelScope.launch(Dispatchers.IO) {
      // Save to preferences FIRST for immediate UI feedback
      preferencesManager.setRamConfig(config)
      _currentCompressionAlgorithm.value = config.compressionAlgorithm

      // Save set-on-boot configuration if enabled
      val ramSetOnBoot = preferencesManager.getRAMSetOnBoot().first()
      if (ramSetOnBoot) {
        Log.d("TuningViewModel", "RAM set-on-boot enabled, configuration will be applied on boot")
      }

      // Then apply to system (these operations can take time)
      ramUseCase.setSwappiness(config.swappiness)
      ramUseCase.setZRAMSize(config.zramSize.toLong() * 1024L * 1024L, config.compressionAlgorithm)
      ramUseCase.setDirtyRatio(config.dirtyRatio)
      ramUseCase.setMinFreeMem(config.minFreeMem)
      ramUseCase.setSwapFileSizeMb(config.swapSize)

      // Refresh status after operations complete
      RAMControlUseCase.invalidateZramCache()
      _zramStatus.value = ramUseCase.getZramStatus()
      _swapFileStatus.value = ramUseCase.getSwapFileStatus()
      _memoryStats.value = ramUseCase.getMemoryStats()
    }
  }

  fun setZRAMWithLiveLog(
      sizeBytes: Long,
      compressionAlgorithm: String = "lz4",
      onLog: (String) -> Unit,
      onComplete: (Boolean) -> Unit,
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      val result = ramUseCase.setZRAMSize(sizeBytes, compressionAlgorithm, onLog)
      if (result.isSuccess) {
        _currentCompressionAlgorithm.value = compressionAlgorithm
      }
      onComplete(result.isSuccess)
    }
  }

  fun setSwapWithLiveLog(sizeMb: Int, onLog: (String) -> Unit, onComplete: (Boolean) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      val result = ramUseCase.setSwapFileSizeMb(sizeMb, onLog)
      onComplete(result.isSuccess)
    }
  }

  fun setPerfMode(mode: String) {
    viewModelScope.launch {
      Log.d("TuningViewModel", "Setting Performance Mode to: $mode")

      _currentPerfMode.value = mode

      val governor =
          when (mode) {
            "battery" -> "powersave"
            "balance" -> "schedutil"
            "performance" -> "performance"
            else -> "schedutil"
          }

      _cpuClusters.value.forEach { cluster ->
        cpuUseCase.setClusterGovernor(cluster.clusterNumber, governor)
      }

      preferencesManager.setPerfMode(mode)

      delay(500)
      refreshCurrentValues()

      Log.d("TuningViewModel", "Performance Mode set to: $mode")
    }
  }

  suspend fun getExportFileName(): String {
    deviceInfoCache?.let { (soc, codename, model) ->
      return "tuning-$soc-$codename-$model.toml"
    }

    val soc = NativeLib.getSystemProperty("ro.board.platform")?.lowercase() ?: "unknownsoc"
    val codename = NativeLib.getSystemProperty("ro.product.device")?.lowercase() ?: "unknowncode"
    val model =
        NativeLib.getSystemProperty("ro.product.model")?.replace("\\s+".toRegex(), "")?.uppercase()
            ?: "UNKNOWN"

    deviceInfoCache = Triple(soc, codename, model)
    return "tuning-$soc-$codename-$model.toml"
  }

  suspend fun exportConfigToUri(context: Context, uri: Uri): Boolean {
    return withContext(Dispatchers.IO) {
      try {
        val config = buildCurrentConfig()
        val tomlString = tomlManager.configToTomlString(config)

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
          OutputStreamWriter(outputStream).use { writer ->
            writer.write(tomlString)
            writer.flush()
          }
        }
        Log.d("TuningViewModel", "Config exported successfully to $uri")
        true
      } catch (e: Exception) {
        Log.e("TuningViewModel", "Export failed", e)
        false
      }
    }
  }

  suspend fun importConfigFromUri(context: Context, uri: Uri): ImportResult {
    _isImporting.value = true

    val result =
        withContext(Dispatchers.IO) {
          try {
            val tomlString =
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                  BufferedReader(InputStreamReader(inputStream)).use { reader -> reader.readText() }
                } ?: return@withContext ImportResult.Error("Failed to read file")

            Log.d("TuningViewModel", "Read config from $uri")

            val parseResult = tomlManager.tomlStringToConfig(tomlString)
            if (parseResult != null) {
              if (!parseResult.isCompatible && parseResult.compatibilityWarning != null) {
                // Still apply the config even with warnings, but return warning result
                applyConfig(parseResult.config)
                
                // Wait a bit more after import to ensure settings are stable
                delay(1000)
                
                Log.d("TuningViewModel", "Config imported with compatibility warnings")
                return@withContext ImportResult.Warning(
                    config = parseResult.config,
                    warning = parseResult.compatibilityWarning,
                )
              }

              applyConfig(parseResult.config)
              
              // Wait a bit more after import to ensure settings are stable
              delay(1000)
              
              Log.d("TuningViewModel", "Config imported and applied successfully")
              ImportResult.Success
            } else {
              Log.e("TuningViewModel", "Failed to parse config")
              ImportResult.Error("Failed to parse configuration file")
            }
          } catch (e: Exception) {
            Log.e("TuningViewModel", "Import failed", e)
            ImportResult.Error(e.message ?: "Unknown error")
          }
        }

    // Only set importing to false after everything is complete
    _isImporting.value = false
    return result
  }

  fun applyPreset(config: TuningConfig) {
    viewModelScope.launch { applyConfig(config) }
  }

  suspend fun buildCurrentConfig(): TuningConfig {
    // Build CPU cluster configs with proper state
    val cpuConfigs = _cpuClusters.value.map { cluster ->
      // Get disabled cores for this cluster
      val disabledCores = mutableListOf<Int>()
      cluster.cores.forEach { coreNum ->
        val isEnabled = preferencesManager.isCpuCoreEnabled(coreNum).first()
        if (!isEnabled) {
          disabledCores.add(coreNum)
        }
      }
      
      CPUClusterConfig(
          cluster = cluster.clusterNumber,
          // Use locked frequencies if available, otherwise current frequencies
          minFreq = if (_isCpuFrequencyLocked.value) {
            // Get from CPU lock state's cluster configs
            val lockState = preferencesManager.getCpuLockState().first()
            lockState.clusterConfigs[cluster.clusterNumber]?.minFreq ?: cluster.currentMinFreq
          } else {
            cluster.currentMinFreq
          },
          maxFreq = if (_isCpuFrequencyLocked.value) {
            // Get from CPU lock state's cluster configs
            val lockState = preferencesManager.getCpuLockState().first()
            lockState.clusterConfigs[cluster.clusterNumber]?.maxFreq ?: cluster.currentMaxFreq
          } else {
            cluster.currentMaxFreq
          },
          governor = cluster.governor,
          disabledCores = disabledCores,
          setOnBoot = preferencesManager.getCpuSetOnBoot().first()
      )
    }

    // Build GPU config with proper state
    val gpu = if (!_isMediatek.value && _gpuInfo.value.minFreq > 0) {
      GPUConfig(
          // Use locked frequencies if available, otherwise current frequencies
          minFreq = if (_isGpuFrequencyLocked.value && _lockedGpuMinFreq.value > 0) {
            _lockedGpuMinFreq.value
          } else {
            _gpuInfo.value.minFreq
          },
          maxFreq = if (_isGpuFrequencyLocked.value && _lockedGpuMaxFreq.value > 0) {
            _lockedGpuMaxFreq.value
          } else {
            _gpuInfo.value.maxFreq
          },
          powerLevel = _gpuInfo.value.powerLevel,
          renderer = _gpuInfo.value.rendererType
      )
    } else {
      null
    }

    // Build thermal config with current settings
    val thermal = ThermalConfig(
        preset = _currentThermalPreset.value,
        setOnBoot = _isThermalSetOnBoot.value
    )

    // Build RAM config with current settings
    val ramConfig = preferencesManager.getRamConfig().first()
    val ram = RAMConfig(
        swappiness = _currentSwappiness.value,
        zramSize = _zramStatus.value.totalMb,
        swapSize = _swapFileStatus.value.sizeMb,
        dirtyRatio = _currentDirtyRatio.value,
        minFreeMem = _currentMinFreeMem.value,
        compressionAlgorithm = _currentCompressionAlgorithm.value,
        setOnBoot = preferencesManager.getRAMSetOnBoot().first()
    )

    // Build additional config with current settings
    val additional = AdditionalConfig(
        ioScheduler = _currentIOScheduler.value,
        tcpCongestion = _currentTCPCongestion.value,
        perfMode = _currentPerfMode.value,
        setOnBoot = preferencesManager.getAdditionalSetOnBoot().first()
    )

    return TuningConfig(
        cpuClusters = cpuConfigs,
        gpu = gpu,
        thermal = thermal,
        ram = ram,
        additional = additional,
        cpuSetOnBoot = preferencesManager.getCpuSetOnBoot().first()
    )
  }

  private suspend fun applyConfig(config: TuningConfig) {
    Log.d("TuningViewModel", "Applying imported configuration...")
    
    // Set flag to prevent refresh during config application
    _isApplyingConfig.value = true
    
    try {
      // Apply CPU cluster configurations with proper error handling
      config.cpuClusters.forEach { clusterConfig ->
        Log.d("TuningViewModel", "Applying CPU cluster ${clusterConfig.cluster}: ${clusterConfig.minFreq}-${clusterConfig.maxFreq}MHz, governor: ${clusterConfig.governor}")
        
        // Set frequency first and wait for result
        val freqResult = cpuUseCase.setClusterFrequency(
            clusterConfig.cluster,
            clusterConfig.minFreq,
            clusterConfig.maxFreq,
        )
        
        if (freqResult.isFailure) {
          Log.w("TuningViewModel", "Failed to set cluster ${clusterConfig.cluster} frequency: ${freqResult.exceptionOrNull()?.message}")
        }
        
        // Set governor and wait for result
        val govResult = cpuUseCase.setClusterGovernor(clusterConfig.cluster, clusterConfig.governor)
        if (govResult.isFailure) {
          Log.w("TuningViewModel", "Failed to set cluster ${clusterConfig.cluster} governor: ${govResult.exceptionOrNull()?.message}")
        }
        
        // Apply disabled cores
        clusterConfig.disabledCores.forEach { core -> 
          Log.d("TuningViewModel", "Disabling CPU core $core")
          val coreResult = cpuUseCase.setCoreOnline(core, false)
          if (coreResult.isSuccess) {
            preferencesManager.setCpuCoreEnabled(core, false)
          } else {
            Log.w("TuningViewModel", "Failed to disable core $core: ${coreResult.exceptionOrNull()?.message}")
          }
        }
        
        // Save to preferences if setOnBoot is enabled
        if (clusterConfig.setOnBoot) {
          preferencesManager.setClusterMinFreq(clusterConfig.cluster, clusterConfig.minFreq)
          preferencesManager.setClusterMaxFreq(clusterConfig.cluster, clusterConfig.maxFreq)
          preferencesManager.setClusterGovernor(clusterConfig.cluster, clusterConfig.governor)
        }
        
        // Small delay between clusters to avoid overwhelming the system
        delay(200)
      }

      // Apply GPU configuration
      config.gpu?.let { gpu ->
        if (!_isMediatek.value) {
          Log.d("TuningViewModel", "Applying GPU config: ${gpu.minFreq}-${gpu.maxFreq}MHz, power level: ${gpu.powerLevel}, renderer: ${gpu.renderer}")
          
          // Set GPU frequencies
          val gpuFreqResult = gpuUseCase.setGPUFrequency(gpu.minFreq, gpu.maxFreq)
          if (gpuFreqResult.isFailure) {
            Log.w("TuningViewModel", "Failed to set GPU frequency: ${gpuFreqResult.exceptionOrNull()?.message}")
          }
          
          // Set power level
          val powerResult = gpuUseCase.setGPUPowerLevel(gpu.powerLevel)
          if (powerResult.isFailure) {
            Log.w("TuningViewModel", "Failed to set GPU power level: ${powerResult.exceptionOrNull()?.message}")
          }
          
          // Set renderer (this may require reboot)
          if (gpu.renderer != "auto" && gpu.renderer.isNotBlank()) {
            val rendererResult = gpuUseCase.setGPURenderer(gpu.renderer)
            if (rendererResult.isFailure) {
              Log.w("TuningViewModel", "Failed to set GPU renderer: ${rendererResult.exceptionOrNull()?.message}")
            }
          }
          
          delay(300) // Wait for GPU changes to take effect
        }
      }

      // Apply thermal configuration
      if (config.thermal.preset.isNotBlank() && config.thermal.preset != "Not Set") {
        Log.d("TuningViewModel", "Applying thermal config: ${config.thermal.preset}, setOnBoot: ${config.thermal.setOnBoot}")
        
        val thermalResult = thermalUseCase.setThermalMode(config.thermal.preset, config.thermal.setOnBoot)
        if (thermalResult.isSuccess) {
          preferencesManager.setThermalConfig(config.thermal.preset, config.thermal.setOnBoot)
        } else {
          Log.w("TuningViewModel", "Failed to set thermal mode: ${thermalResult.exceptionOrNull()?.message}")
        }
        
        delay(200)
      }

      // Apply RAM configuration
      Log.d("TuningViewModel", "Applying RAM config: swappiness=${config.ram.swappiness}, zram=${config.ram.zramSize}MB, swap=${config.ram.swapSize}MB")
      setRAMParameters(config.ram)
      
      // Save RAM set-on-boot setting
      preferencesManager.setRAMSetOnBoot(config.ram.setOnBoot)
      delay(300)

      // Apply additional configurations
      Log.d("TuningViewModel", "Applying additional config: IO=${config.additional.ioScheduler}, TCP=${config.additional.tcpCongestion}, perf=${config.additional.perfMode}")
      
      // Apply I/O scheduler
      config.additional.ioScheduler.takeIf { it.isNotBlank() }?.let { 
        setIOScheduler(it)
        preferencesManager.setIOSetOnBoot(config.additional.setOnBoot)
      }
      
      // Apply TCP congestion
      config.additional.tcpCongestion.takeIf { it.isNotBlank() }?.let { 
        setTCPCongestion(it)
        preferencesManager.setTCPSetOnBoot(config.additional.setOnBoot)
      }
      
      // Apply performance mode
      config.additional.perfMode.takeIf { it.isNotBlank() }?.let { 
        setPerfMode(it)
      }
      
      // Save additional set-on-boot setting
      preferencesManager.setAdditionalSetOnBoot(config.additional.setOnBoot)

      // Apply CPU set-on-boot setting
      preferencesManager.setCpuSetOnBoot(config.cpuSetOnBoot)

      // Wait longer for all changes to take effect
      Log.d("TuningViewModel", "Waiting for all changes to take effect...")
      delay(2000)
      
      // Force refresh CPU clusters to get updated values
      cpuUseCase.invalidateClusterCache()
      
      // Refresh all values multiple times to ensure they're updated
      repeat(3) { attempt ->
        Log.d("TuningViewModel", "Refreshing values (attempt ${attempt + 1}/3)")
        refreshCurrentValues()
        delay(500)
      }
      
      // Update UI states to match applied config
      updateUIStatesAfterImport(config)
      
      Log.d("TuningViewModel", "Configuration applied and verified successfully")
      
    } catch (e: Exception) {
      Log.e("TuningViewModel", "Error applying configuration", e)
      throw e
    } finally {
      // Always reset the flag, even if there was an error
      _isApplyingConfig.value = false
    }
  }
  
  private suspend fun updateUIStatesAfterImport(config: TuningConfig) {
    Log.d("TuningViewModel", "Updating UI states after import...")
    
    // Update CPU states
    config.cpuClusters.forEach { clusterConfig ->
      // Update cluster frequency states if they exist
      val clusterIndex = clusterConfig.cluster
      if (clusterIndex < _cpuClusters.value.size) {
        val currentCluster = _cpuClusters.value[clusterIndex]
        val updatedCluster = currentCluster.copy(
          currentMinFreq = clusterConfig.minFreq,
          currentMaxFreq = clusterConfig.maxFreq,
          governor = clusterConfig.governor
        )
        
        // Update the cluster in the list
        val updatedClusters = _cpuClusters.value.toMutableList()
        updatedClusters[clusterIndex] = updatedCluster
        _cpuClusters.value = updatedClusters
      }
    }
    
    // Update cluster UI states to match imported config
    val states = mutableMapOf<Int, ClusterUIState>()
    config.cpuClusters.forEach { clusterConfig ->
      states[clusterConfig.cluster] = ClusterUIState(
        minFreq = clusterConfig.minFreq.toFloat(),
        maxFreq = clusterConfig.maxFreq.toFloat(),
        governor = clusterConfig.governor,
      )
    }
    _clusterStates.value = states
    
    // Update GPU states
    config.gpu?.let { gpu ->
      if (!_isMediatek.value) {
        _gpuInfo.value = _gpuInfo.value.copy(
          minFreq = gpu.minFreq,
          maxFreq = gpu.maxFreq,
          powerLevel = gpu.powerLevel,
          rendererType = gpu.renderer
        )
        
        // Update locked GPU frequency states
        _lockedGpuMinFreq.value = gpu.minFreq
        _lockedGpuMaxFreq.value = gpu.maxFreq
      }
    }
    
    // Update thermal states
    if (config.thermal.preset.isNotBlank() && config.thermal.preset != "Not Set") {
      _currentThermalPreset.value = config.thermal.preset
      _isThermalSetOnBoot.value = config.thermal.setOnBoot
    }
    
    // Update RAM states
    _currentSwappiness.value = config.ram.swappiness
    _currentDirtyRatio.value = config.ram.dirtyRatio
    _currentMinFreeMem.value = config.ram.minFreeMem
    _currentCompressionAlgorithm.value = config.ram.compressionAlgorithm
    
    // Update additional states
    _currentIOScheduler.value = config.additional.ioScheduler
    _currentTCPCongestion.value = config.additional.tcpCongestion
    _currentPerfMode.value = config.additional.perfMode
    
    Log.d("TuningViewModel", "UI states updated successfully")
  }

  suspend fun checkMagiskAvailability(): Boolean {
    return gpuUseCase.checkMagiskAvailability()
  }

  // Job for real-time monitoring
  private var monitoringJob: kotlinx.coroutines.Job? = null

  /** Start real-time monitoring loop (called when screen becomes active) */
  fun startRealTimeMonitoring() {
    if (monitoringJob?.isActive == true) return
    NativeLib.resetGpuStats()

    monitoringJob =
        viewModelScope.launch(Dispatchers.IO) {
          while (true) {
            refreshDynamicValues()
            delay(300)
          }
        }
  }

  /** Stop real-time monitoring (called when screen minimized/closed) */
  fun stopRealTimeMonitoring() {
    monitoringJob?.cancel()
    monitoringJob = null
  }



  fun refreshData() {
    // Force a full refresh
    viewModelScope.launch { refreshCurrentValues() }
  }

  // CPU Lock State Management
  private suspend fun loadCpuLockState() {
    val lockState = preferencesManager.getCpuLockState().first()
    
    // Subscribe to smart locker events
    viewModelScope.launch {
      smartCpuLocker.thermalEvents.collect { event ->
        _thermalEvents.value = event
        // Update preferences with thermal override status
        preferencesManager.updateCpuLockThermalOverride(
          event.type in listOf(ThermalEventType.EMERGENCY, ThermalEventType.CRITICAL)
        )
        preferencesManager.updateCpuLockTemperature(event.temperature)
      }
    }
    
    viewModelScope.launch {
      smartCpuLocker.lockState.collect { state ->
        _cpuLockStatus.value = smartCpuLocker.getLockStatus()
      }
    }
    
    if (lockState.isLocked) {
      _isCpuFrequencyLocked.value = true
      
      // Restore lock state to system
      val clusterConfigs = lockState.clusterConfigs.mapValues { (_, config) ->
        config
      }
      val result = smartCpuLocker.lockCpuFrequencies(
        clusterConfigs,
        lockState.policyType,
        lockState.thermalPolicy
      )
      
      when (result) {
        is SmartLockResult.Success,
        is SmartLockResult.SuccessWithWarning -> {
          _cpuLockStatus.value = smartCpuLocker.getLockStatus()
          Log.d("TuningViewModel", "CPU lock state restored successfully")
        }
        is SmartLockResult.Error -> {
          Log.e("TuningViewModel", "Failed to restore CPU lock", result.throwable)
          // Clear invalid state
          preferencesManager.clearCpuLockState()
          _isCpuFrequencyLocked.value = false
        }
        is SmartLockResult.PartialSuccess -> {
          _cpuLockStatus.value = smartCpuLocker.getLockStatus()
          Log.w("TuningViewModel", "CPU lock partially restored: ${result.successClusters}")
        }
        else -> { /* Handle other cases */ }
      }
    }
  }

  fun lockCpuFrequencies(
    clusterConfigs: Map<Int, CpuClusterLockConfig>,
    policyType: LockPolicyType = LockPolicyType.MANUAL,
    thermalPolicy: String = "PolicyB"
  ) {
    viewModelScope.launch {
      val result = smartCpuLocker.lockCpuFrequencies(
        clusterConfigs,
        policyType,
        thermalPolicy
      )
      
      when (result) {
        is SmartLockResult.Success,
        is SmartLockResult.SuccessWithWarning -> {
          _isCpuFrequencyLocked.value = true
          
          // Save to preferences
          val originalFreqs = getCurrentFrequencies()
          preferencesManager.setCpuLockState(
            locked = true,
            clusterConfigs = clusterConfigs,
            policyType = policyType,
            thermalPolicy = thermalPolicy,
            originalFreqs = originalFreqs
          )
          
          _cpuLockStatus.value = smartCpuLocker.getLockStatus()
          Log.d("TuningViewModel", "CPU frequencies locked successfully")
        }
        is SmartLockResult.Error -> {
          Log.e("TuningViewModel", "CPU lock failed", result.throwable)
          // Could show user notification here
        }
        is SmartLockResult.AlreadyLocked -> {
          Log.d("TuningViewModel", "CPU already locked")
        }
        is SmartLockResult.PartialSuccess -> {
          _isCpuFrequencyLocked.value = true
          // Save partial state
          val successfulConfigs = clusterConfigs.filterKeys { it !in result.failedClusters }
          val originalFreqs = getCurrentFrequencies()
          preferencesManager.setCpuLockState(
            locked = true,
            clusterConfigs = successfulConfigs,
            policyType = policyType,
            thermalPolicy = thermalPolicy,
            originalFreqs = originalFreqs
          )
          Log.w("TuningViewModel", "CPU lock partially successful: ${result.successClusters}")
        }
        is SmartLockResult.RetryExceeded -> {
          Log.w("TuningViewModel", "CPU lock retry exceeded: ${result.retryCount}")
          preferencesManager.incrementCpuLockRetry()
        }
        else -> { /* Handle other cases */ }
      }
      
      // Refresh UI state
      refreshDynamicValues()
    }
  }

  fun unlockCpuFrequencies() {
    viewModelScope.launch {
      val result = smartCpuLocker.unlockCpuFrequencies()
      
      when (result) {
        is SmartLockResult.Success,
        is SmartLockResult.SuccessWithWarning -> {
          _isCpuFrequencyLocked.value = false
          preferencesManager.clearCpuLockState()
          _cpuLockStatus.value = defaultLockStatus
          Log.d("TuningViewModel", "CPU frequencies unlocked successfully")
        }
        is SmartLockResult.Error -> {
          Log.e("TuningViewModel", "CPU unlock failed", result.throwable)
        }
        is SmartLockResult.NotLocked -> {
          Log.d("TuningViewModel", "CPU not locked")
        }
        else -> { /* Handle other cases */ }
      }
      
      // Refresh UI state
      refreshDynamicValues()
    }
  }

  private suspend fun getCurrentFrequencies(): Map<Int, OriginalFreqConfig> {
    val clusters = cpuUseCase.detectClusters()
    return clusters.associate { cluster ->
      cluster.clusterNumber to OriginalFreqConfig(
        minFreq = cluster.currentMinFreq,
        maxFreq = cluster.currentMaxFreq,
        governor = cluster.governor
      )
    }
  }

  fun getCpuLockPolicyType(): StateFlow<LockPolicyType> {
    return preferencesManager.getCpuLockPolicyType()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LockPolicyType.MANUAL)
  }

  fun getCpuLockThermalPolicy(): StateFlow<String> {
    return preferencesManager.getCpuLockThermalPolicy()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
  }

  fun setCpuLockThermalPolicy(policy: String) {
    viewModelScope.launch {
      preferencesManager.setCpuLockThermalPolicy(policy)
    }
  }

  fun getCpuLockRetryCount(): StateFlow<Int> {
    val flow = preferencesManager.getCpuLockRetryCount()
    return flow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
  }

  // User interaction control methods
  fun startUserAdjusting() {
    _isUserAdjusting.value = true
  }

  fun endUserAdjusting() {
    _isUserAdjusting.value = false
    // Refresh once after user finishes adjusting
    viewModelScope.launch {
      delay(500) // Wait a bit for hardware to apply
      refreshDynamicValues()
    }
  }

  fun isUserAdjusting(): StateFlow<Boolean> = _isUserAdjusting.asStateFlow()

  // Notification methods
  fun showCpuLockNotification(message: String) {
    _cpuLockNotifications.value = message
  }

  fun clearCpuLockNotification() {
    _cpuLockNotifications.value = null
  }


}

sealed class ImportResult {
  object Success : ImportResult()

  data class Warning(val config: TuningConfig, val warning: String) : ImportResult()

  data class Error(val message: String) : ImportResult()
}

data class ClusterUIState(
    val minFreq: Float = 0f,
    val maxFreq: Float = 0f,
    val governor: String = "",
)
