package id.xms.xtrakernelmanager.ui.screens.tuning

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.model.*
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.data.preferences.TomlConfigManager
import id.xms.xtrakernelmanager.domain.native.NativeLib
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.domain.usecase.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TuningViewModel(
    val preferencesManager: PreferencesManager,
    private val cpuUseCase: CPUControlUseCase = CPUControlUseCase(),
    private val gpuUseCase: GPUControlUseCase = GPUControlUseCase(),
    private val ramUseCase: RAMControlUseCase = RAMControlUseCase(),
    private val thermalUseCase: ThermalControlUseCase = ThermalControlUseCase(),
    private val overlayUseCase: id.xms.xtrakernelmanager.domain.usecase.GameOverlayUseCase =
        id.xms.xtrakernelmanager.domain.usecase.GameOverlayUseCase(),
    private val tomlManager: TomlConfigManager = TomlConfigManager(),
) : ViewModel() {

  class Factory(private val preferencesManager: PreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(TuningViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return TuningViewModel(preferencesManager) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }

  private val _isRootAvailable = MutableStateFlow(false)
  val isRootAvailable: StateFlow<Boolean>
    get() = _isRootAvailable.asStateFlow()

  private val _isLoading = MutableStateFlow(true)
  val isLoading: StateFlow<Boolean>
    get() = _isLoading.asStateFlow()

  private val _isImporting = MutableStateFlow(false)
  val isImporting: StateFlow<Boolean>
    get() = _isImporting.asStateFlow()

  private val _cpuClusters = MutableStateFlow<List<ClusterInfo>>(emptyList())
  val cpuClusters: StateFlow<List<ClusterInfo>>
    get() = _cpuClusters.asStateFlow()

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

  // Store device's default governor (captured at init)
  private var defaultGovernor: String = "schedutil"

  fun applyGlobalProfile(profile: String) {
    viewModelScope.launch(Dispatchers.IO) {
      _selectedProfile.value = profile

      // Map profile to CPU governor - Balance uses device default
      val governor =
          when (profile) {
            "Performance" -> "performance"
            "Balance" -> defaultGovernor // Use device's original governor
            "Powersave" -> "powersave"
            "Battery" -> "conservative"
            else -> defaultGovernor
          }

      // Apply governor to all CPU clusters
      _cpuClusters.value.forEach { cluster ->
        cpuUseCase.setClusterGovernor(cluster.clusterNumber, governor)
      }

      // Refresh values after applying
      refreshDynamicValues()
    }
  }

  // Call this after first cluster detection to capture default governor
  private fun captureDefaultGovernor() {
    val firstCluster = _cpuClusters.value.firstOrNull()
    if (firstCluster != null && firstCluster.governor.isNotEmpty()) {
      defaultGovernor = firstCluster.governor
    }
  }

  // CPU Frequency Control - sets min/max freq for a specific cluster
  fun setCpuClusterFrequency(clusterIndex: Int, minFreqMhz: Int, maxFreqMhz: Int) {
    viewModelScope.launch(Dispatchers.IO) {
      cpuUseCase.setClusterFrequency(clusterIndex, minFreqMhz, maxFreqMhz)
      refreshDynamicValues()
    }
  }

  // CPU Governor Control - sets governor for a specific cluster
  fun setCpuClusterGovernor(clusterIndex: Int, governor: String) {
    viewModelScope.launch(Dispatchers.IO) {
      cpuUseCase.setClusterGovernor(clusterIndex, governor)
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
      val hostname = RootManager.executeCommand("getprop net.hostname").getOrNull()?.trim() ?: ""
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

  // Auto-refresh control
  private val _isRefreshEnabled = MutableStateFlow(true)

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
      refreshCurrentValues()
      startNetworkMonitoring()
    }

    _isLoading.value = false
  }

  private suspend fun startAutoRefresh() {
    while (true) {
      delay(5000) // Optimized: 5 seconds instead of 2
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

    if (!_isMediatek.value) {
      _gpuInfo.value = gpuUseCase.getGPUInfo()
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

    // Only load static data once
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
  }

  // Lightweight refresh - only dynamic data (called every 5s)
  private suspend fun refreshDynamicValues() {
    val updatedClusters = cpuUseCase.detectClusters()
    _cpuClusters.value = updatedClusters

    if (!_isMediatek.value) {
      _gpuInfo.value = gpuUseCase.getGPUInfo()
    }

    // Update temperature
    _cpuTemperature.value = overlayUseCase.getTemperature()

    // Update CPU load via JNI
    _cpuLoad.value = NativeLib.readCpuLoad() ?: 0f

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
  }

  private suspend fun loadSystemInfo() {
    _cpuClusters.value = cpuUseCase.detectClusters()
    captureDefaultGovernor() // Save device's default governor for Balance profile
    _isMediatek.value = detectMediatek()

    if (!_isMediatek.value) {
      _gpuInfo.value = gpuUseCase.getGPUInfo()
    }

    _availableIOSchedulers.value = getAvailableIOSchedulers()
    _availableTCPCongestion.value = getAvailableTCPCongestion()
    _availableCompressionAlgorithms.value = ramUseCase.getAvailableCompressionAlgorithms()

    val currentIO = getCurrentIOScheduler()
    if (currentIO.isNotEmpty()) {
      _currentIOScheduler.value = currentIO
    }

    val currentTCP = getCurrentTCPCongestion()
    if (currentTCP.isNotEmpty()) {
      _currentTCPCongestion.value = currentTCP
    }

    val currentComp = ramUseCase.getCurrentCompressionAlgorithm()
    if (currentComp.isNotEmpty()) {
      _currentCompressionAlgorithm.value = currentComp
    }

    refreshCurrentValues()
  }

  private suspend fun detectMediatek(): Boolean {
    val hwPlatform =
        RootManager.executeCommand("getprop ro.hardware").getOrNull()?.lowercase() ?: ""
    val soc = RootManager.executeCommand("getprop ro.board.platform").getOrNull()?.lowercase() ?: ""

    return hwPlatform.contains("mt") ||
        soc.contains("mt") ||
        hwPlatform.contains("mediatek") ||
        soc.contains("mediatek")
  }

  private suspend fun getAvailableIOSchedulers(): List<String> {
    val schedulers =
        RootManager.executeCommand("cat /sys/block/sda/queue/scheduler 2>/dev/null").getOrNull()
            ?: return emptyList()
    return schedulers.replace("[", "").replace("]", "").split("\\s+".toRegex()).filter {
      it.isNotBlank()
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
    val output =
        RootManager.executeCommand("cat /sys/block/sda/queue/scheduler 2>/dev/null").getOrNull()
    Log.d("TuningViewModel", "IO Scheduler raw output: $output")

    if (output.isNullOrEmpty()) {
      val saved = preferencesManager.getIOScheduler().first()
      Log.d("TuningViewModel", "IO Scheduler from prefs (fallback): $saved")
      return saved
    }

    val match = Regex("\\[(.*?)]").find(output)
    val result = match?.groupValues?.get(1) ?: ""
    Log.d("TuningViewModel", "IO Scheduler parsed: $result")

    return result.ifEmpty { preferencesManager.getIOScheduler().first() }
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
      }
    }
  }

  fun setCPUGovernor(cluster: Int, governor: String) {
    viewModelScope.launch {
      val result = cpuUseCase.setClusterGovernor(cluster, governor)
      if (result.isSuccess) {
        updateClusterGovernor(cluster, governor)
      }
    }
  }

  fun disableCPUCore(core: Int, disable: Boolean) {
    viewModelScope.launch {
      Log.d("TuningViewModel", "Set core $core online = ${!disable}")
      val result = cpuUseCase.setCoreOnline(core, !disable)
      Log.d("TuningViewModel", "Result of setCoreOnline for core $core: $result")
      preferencesManager.setCpuCoreEnabled(core, !disable)
      delay(500)
      refreshCurrentValues()
    }
  }

  private suspend fun applySavedCoreStates() {
    for (core in 0..7) {
      val enabled = preferencesManager.isCpuCoreEnabled(core).first()
      Log.d("TuningViewModel", "Applying core $core state: enabled=$enabled")
      cpuUseCase.setCoreOnline(core, enabled)
    }
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
      RootManager.executeCommand("echo $ioScheduler > /sys/block/sda/queue/scheduler")
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

  fun setIOScheduler(scheduler: String) {
    viewModelScope.launch(Dispatchers.IO) {
      Log.d("TuningViewModel", "Setting IO Scheduler to: $scheduler")

      _currentIOScheduler.value = scheduler

      val result = RootManager.executeCommand("echo $scheduler > /sys/block/sda/queue/scheduler")
      Log.d("TuningViewModel", "IO Scheduler command result: ${result.isSuccess}")

      if (result.isSuccess) {
        preferencesManager.setIOScheduler(scheduler)
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
      ramUseCase.setSwappiness(config.swappiness)
      ramUseCase.setZRAMSize(config.zramSize.toLong() * 1024L * 1024L, config.compressionAlgorithm)
      ramUseCase.setDirtyRatio(config.dirtyRatio)
      ramUseCase.setMinFreeMem(config.minFreeMem)
      ramUseCase.setSwapFileSizeMb(config.swapSize)
      preferencesManager.setRamConfig(config)
      _currentCompressionAlgorithm.value = config.compressionAlgorithm
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

    val soc =
        RootManager.executeCommand("getprop ro.board.platform").getOrNull()?.trim()?.lowercase()
            ?: "unknownsoc"
    val codename =
        RootManager.executeCommand("getprop ro.product.device").getOrNull()?.trim()?.lowercase()
            ?: "unknowncode"
    val model =
        RootManager.executeCommand("getprop ro.product.model")
            .getOrNull()
            ?.trim()
            ?.replace("\\s+".toRegex(), "")
            ?.uppercase() ?: "UNKNOWN"

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
                return@withContext ImportResult.Warning(
                    config = parseResult.config,
                    warning = parseResult.compatibilityWarning,
                )
              }

              applyConfig(parseResult.config)
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

    _isImporting.value = false
    return result
  }

  fun applyPreset(config: TuningConfig) {
    viewModelScope.launch { applyConfig(config) }
  }

  fun buildCurrentConfig(): TuningConfig {
    val cpuConfigs =
        _cpuClusters.value.map { cluster ->
          CPUClusterConfig(
              cluster = cluster.clusterNumber,
              minFreq = cluster.currentMinFreq,
              maxFreq = cluster.currentMaxFreq,
              governor = cluster.governor,
              disabledCores = emptyList(),
          )
        }

    val gpu =
        if (!_isMediatek.value && _gpuInfo.value.minFreq > 0) {
          GPUConfig(
              minFreq = _gpuInfo.value.minFreq,
              maxFreq = _gpuInfo.value.maxFreq,
              powerLevel = 0,
              renderer = "auto",
          )
        } else {
          null
        }

    return TuningConfig(
        cpuClusters = cpuConfigs,
        gpu = gpu,
        thermal = ThermalConfig(),
        ram = RAMConfig(),
        additional =
            AdditionalConfig(
                ioScheduler = _currentIOScheduler.value,
                tcpCongestion = _currentTCPCongestion.value,
                perfMode = _currentPerfMode.value,
            ),
    )
  }

  private suspend fun applyConfig(config: TuningConfig) {
    config.cpuClusters.forEach { clusterConfig ->
      cpuUseCase.setClusterFrequency(
          clusterConfig.cluster,
          clusterConfig.minFreq,
          clusterConfig.maxFreq,
      )
      cpuUseCase.setClusterGovernor(clusterConfig.cluster, clusterConfig.governor)
      clusterConfig.disabledCores.forEach { core -> cpuUseCase.setCoreOnline(core, false) }
    }

    config.gpu?.let { gpu ->
      if (!_isMediatek.value) {
        gpuUseCase.setGPUFrequency(gpu.minFreq, gpu.maxFreq)
        gpuUseCase.setGPUPowerLevel(gpu.powerLevel)
        gpuUseCase.setGPURenderer(gpu.renderer)
      }
    }

    thermalUseCase.setThermalMode(config.thermal.preset, config.thermal.setOnBoot)
    preferencesManager.setThermalConfig(config.thermal.preset, config.thermal.setOnBoot)

    setRAMParameters(config.ram)

    config.additional.ioScheduler.takeIf { it.isNotBlank() }?.let { setIOScheduler(it) }
    config.additional.tcpCongestion.takeIf { it.isNotBlank() }?.let { setTCPCongestion(it) }
    config.additional.perfMode.takeIf { it.isNotBlank() }?.let { setPerfMode(it) }

    delay(1000)
    refreshCurrentValues()
  }

  suspend fun checkMagiskAvailability(): Boolean {
    return gpuUseCase.checkMagiskAvailability()
  }

  fun refreshData() {
    viewModelScope.launch { refreshCurrentValues() }
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
