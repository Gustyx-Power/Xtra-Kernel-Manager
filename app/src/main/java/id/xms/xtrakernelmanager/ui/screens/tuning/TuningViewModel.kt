package id.xms.xtrakernelmanager.ui.screens.tuning

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.model.*
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.data.preferences.TomlConfigManager
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TuningViewModel(
    val preferencesManager: PreferencesManager,
    private val cpuUseCase: CPUControlUseCase = CPUControlUseCase(),
    private val gpuUseCase: GPUControlUseCase = GPUControlUseCase(),
    private val ramUseCase: RAMControlUseCase = RAMControlUseCase(),
    private val thermalUseCase: ThermalControlUseCase = ThermalControlUseCase(),
    private val tomlManager: TomlConfigManager = TomlConfigManager()
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
    val isRootAvailable: StateFlow<Boolean> get() = _isRootAvailable.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading.asStateFlow()

    private val _cpuClusters = MutableStateFlow<List<ClusterInfo>>(emptyList())
    val cpuClusters: StateFlow<List<ClusterInfo>> get() = _cpuClusters.asStateFlow()

    private val _gpuInfo = MutableStateFlow(GPUInfo())
    val gpuInfo: StateFlow<GPUInfo> get() = _gpuInfo.asStateFlow()

    private val _isMediatek = MutableStateFlow(false)
    val isMediatek: StateFlow<Boolean> get() = _isMediatek.asStateFlow()

    private val _currentConfig = MutableStateFlow(TuningConfig())
    val currentConfig: StateFlow<TuningConfig> get() = _currentConfig.asStateFlow()

    private val _availableIOSchedulers = MutableStateFlow<List<String>>(emptyList())
    val availableIOSchedulers: StateFlow<List<String>> get() = _availableIOSchedulers.asStateFlow()

    private val _availableTCPCongestion = MutableStateFlow<List<String>>(emptyList())
    val availableTCPCongestion: StateFlow<List<String>> get() = _availableTCPCongestion.asStateFlow()

    // Current active I/O and TCP values
    private val _currentIOScheduler = MutableStateFlow<String>("")
    val currentIOScheduler: StateFlow<String> get() = _currentIOScheduler.asStateFlow()

    private val _currentTCPCongestion = MutableStateFlow<String>("")
    val currentTCPCongestion: StateFlow<String> get() = _currentTCPCongestion.asStateFlow()

    private val _clusterStates = MutableStateFlow<Map<Int, ClusterUIState>>(emptyMap())
    val clusterStates: StateFlow<Map<Int, ClusterUIState>> get() = _clusterStates.asStateFlow()

    init {
        viewModelScope.launch {
            // UPDATED: Load preferences sebagai initial value
            _currentIOScheduler.value = preferencesManager.getIOScheduler().first()
            _currentTCPCongestion.value = preferencesManager.getTCPCongestion().first()
            
            checkRootAndLoadData()
            applySavedCoreStates()
            startAutoRefresh()
        }
    }

    private suspend fun checkRootAndLoadData() {
        _isLoading.value = true
        _isRootAvailable.value = RootManager.isRootAvailable()

        if (_isRootAvailable.value) {
            loadSystemInfo()
            refreshCurrentValues()
        }

        _isLoading.value = false
    }

    private suspend fun startAutoRefresh() {
        while (true) {
            delay(2000)
            if (_isRootAvailable.value && !_isLoading.value) {
                refreshCurrentValues()
            }
        }
    }

    private suspend fun refreshCurrentValues() {
        val updatedClusters = cpuUseCase.detectClusters()
        _cpuClusters.value = updatedClusters

        if (!_isMediatek.value) {
            _gpuInfo.value = gpuUseCase.getGPUInfo()
        }

        val states = mutableMapOf<Int, ClusterUIState>()
        updatedClusters.forEach { cluster ->
            states[cluster.clusterNumber] = ClusterUIState(
                minFreq = cluster.currentMinFreq.toFloat(),
                maxFreq = cluster.currentMaxFreq.toFloat(),
                governor = cluster.governor
            )
        }
        _clusterStates.value = states
        
        // Refresh current I/O and TCP values
        val currentIO = getCurrentIOScheduler()
        if (currentIO.isNotEmpty()) {
            _currentIOScheduler.value = currentIO
        }
        
        val currentTCP = getCurrentTCPCongestion()
        if (currentTCP.isNotEmpty()) {
            _currentTCPCongestion.value = currentTCP
        }
    }

    private suspend fun loadSystemInfo() {
        _cpuClusters.value = cpuUseCase.detectClusters()
        _isMediatek.value = detectMediatek()

        if (!_isMediatek.value) {
            _gpuInfo.value = gpuUseCase.getGPUInfo()
        }

        _availableIOSchedulers.value = getAvailableIOSchedulers()
        _availableTCPCongestion.value = getAvailableTCPCongestion()
        
        // Load current active values
        val currentIO = getCurrentIOScheduler()
        if (currentIO.isNotEmpty()) {
            _currentIOScheduler.value = currentIO
        }
        
        val currentTCP = getCurrentTCPCongestion()
        if (currentTCP.isNotEmpty()) {
            _currentTCPCongestion.value = currentTCP
        }

        refreshCurrentValues()
    }

    private suspend fun detectMediatek(): Boolean {
        val hwPlatform = RootManager.executeCommand("getprop ro.hardware").getOrNull()?.lowercase() ?: ""
        val soc = RootManager.executeCommand("getprop ro.board.platform").getOrNull()?.lowercase() ?: ""

        return hwPlatform.contains("mt") || soc.contains("mt") ||
                hwPlatform.contains("mediatek") || soc.contains("mediatek")
    }

    private suspend fun getAvailableIOSchedulers(): List<String> {
        val schedulers = RootManager.executeCommand("cat /sys/block/sda/queue/scheduler 2>/dev/null").getOrNull() ?: return emptyList()
        return schedulers.replace("[", "").replace("]", "").split("\\s+".toRegex()).filter { it.isNotBlank() }
    }

    private suspend fun getAvailableTCPCongestion(): List<String> {
        val congestion = RootManager.executeCommand("cat /proc/sys/net/ipv4/tcp_available_congestion_control 2>/dev/null").getOrNull() ?: return emptyList()
        return congestion.split("\\s+".toRegex()).filter { it.isNotBlank() }
    }

    // Get current active I/O scheduler
    private suspend fun getCurrentIOScheduler(): String {
        val output = RootManager.executeCommand("cat /sys/block/sda/queue/scheduler 2>/dev/null").getOrNull()
        Log.d("TuningViewModel", "IO Scheduler raw output: $output")
        
        if (output.isNullOrEmpty()) {
            val saved = preferencesManager.getIOScheduler().first()
            Log.d("TuningViewModel", "IO Scheduler from prefs (fallback): $saved")
            return saved
        }
        
        // Format: noop deadline [cfq] - extract the one in brackets
        val match = Regex("\\[(.*?)\\]").find(output)
        val result = match?.groupValues?.get(1) ?: ""
        Log.d("TuningViewModel", "IO Scheduler parsed: $result")
        
        return result.ifEmpty {
            preferencesManager.getIOScheduler().first()
        }
    }

    // Get current active TCP congestion
    private suspend fun getCurrentTCPCongestion(): String {
        val output = RootManager.executeCommand("cat /proc/sys/net/ipv4/tcp_congestion_control 2>/dev/null").getOrNull()?.trim()
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
        // Apply CPU cores
        for (core in 0..7) {
            val enabled = preferencesManager.isCpuCoreEnabled(core).first()
            cpuUseCase.setCoreOnline(core, enabled)
        }

        // Apply thermal config
        val thermalPreset = preferencesManager.getThermalPreset().first()
        val setOnBoot = preferencesManager.getThermalSetOnBoot().first()
        if (thermalPreset.isNotEmpty()) {
            thermalUseCase.setThermalMode(thermalPreset, setOnBoot)
        }

        // Apply I/O scheduler
        val ioScheduler = preferencesManager.getIOScheduler().first()
        if (ioScheduler.isNotBlank()) {
            RootManager.executeCommand("echo $ioScheduler > /sys/block/sda/queue/scheduler")
        }

        // Apply TCP congestion control
        val tcpCongestion = preferencesManager.getTCPCongestion().first()
        if (tcpCongestion.isNotBlank()) {
            RootManager.executeCommand("echo $tcpCongestion > /proc/sys/net/ipv4/tcp_congestion_control")
        }

        // Apply RAM config
        val ramConfig = preferencesManager.getRamConfig().first()
        ramUseCase.setSwappiness(ramConfig.swappiness)
        ramUseCase.setZRAMSize(ramConfig.zramSize.toLong() * 1024L * 1024L)
        ramUseCase.setDirtyRatio(ramConfig.dirtyRatio)
        ramUseCase.setMinFreeMem(ramConfig.minFreeMem)
        ramUseCase.setSwapFileSizeMb(ramConfig.swapSize)
    }

    fun setGPUFrequency(minFreq: Int, maxFreq: Int) {
        viewModelScope.launch {
            gpuUseCase.setGPUFrequency(minFreq, maxFreq)
        }
    }

    fun setGPUPowerLevel(level: Int) {
        viewModelScope.launch {
            gpuUseCase.setGPUPowerLevel(level)
        }
    }

    fun setGPURenderer(renderer: String) {
        viewModelScope.launch {
            gpuUseCase.setGPURenderer(renderer)
        }
    }

    suspend fun verifyRendererChange(renderer: String): Boolean {
        return gpuUseCase.verifyRendererChange(renderer).getOrDefault(false)
    }

    fun performReboot() {
        viewModelScope.launch {
            gpuUseCase.performReboot()
        }
    }

    fun setThermalPreset(preset: String, setOnBoot: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            thermalUseCase.setThermalMode(preset, setOnBoot)
            preferencesManager.setThermalConfig(preset, setOnBoot)
        }
    }

    fun setIOScheduler(scheduler: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("TuningViewModel", "Setting IO Scheduler to: $scheduler")
            
            // UPDATED: Force update UI dulu
            _currentIOScheduler.value = scheduler
            
            val result = RootManager.executeCommand("echo $scheduler > /sys/block/sda/queue/scheduler")
            Log.d("TuningViewModel", "IO Scheduler command result: ${result.isSuccess}")
            
            if (result.isSuccess) {
                preferencesManager.setIOScheduler(scheduler)
                // Delay kecil untuk ensure sistem update
                delay(200)
                // Verify dengan baca ulang
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
            
            // UPDATED: Force update UI dulu
            _currentTCPCongestion.value = congestion
            
            val result = RootManager.executeCommand("echo $congestion > /proc/sys/net/ipv4/tcp_congestion_control")
            Log.d("TuningViewModel", "TCP Congestion command result: ${result.isSuccess}")
            
            if (result.isSuccess) {
                preferencesManager.setTCPCongestion(congestion)
                // Delay kecil untuk ensure sistem update
                delay(200)
                // Verify dengan baca ulang
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
            ramUseCase.setZRAMSize(config.zramSize.toLong() * 1024L * 1024L)
            ramUseCase.setDirtyRatio(config.dirtyRatio)
            ramUseCase.setMinFreeMem(config.minFreeMem)
            ramUseCase.setSwapFileSizeMb(config.swapSize)
            preferencesManager.setRamConfig(config)
        }
    }
    

    fun setZRAMWithLiveLog(
        sizeBytes: Long,
        onLog: (String) -> Unit,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = ramUseCase.setZRAMSize(sizeBytes, onLog)
            onComplete(result.isSuccess)
        }
    }
    
    fun setSwapWithLiveLog(
        sizeMb: Int,
        onLog: (String) -> Unit,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = ramUseCase.setSwapFileSizeMb(sizeMb, onLog)
            onComplete(result.isSuccess)
        }
    }

    fun setPerfMode(mode: String) {
        viewModelScope.launch {
            val governor = when (mode) {
                "battery" -> "powersave"
                "balance" -> "schedutil"
                "performance" -> "performance"
                else -> "schedutil"
            }
            _cpuClusters.value.forEach { cluster ->
                cpuUseCase.setClusterGovernor(cluster.clusterNumber, governor)
            }
            delay(500)
            refreshCurrentValues()
        }
    }

    fun exportConfig() {
        viewModelScope.launch {
            val config = buildCurrentConfig()
            tomlManager.exportConfig(config)
        }
    }

    fun importConfig() {
        viewModelScope.launch {
            val config = tomlManager.importConfig()
            if (config != null) {
                applyConfig(config)
            }
        }
    }

    fun applyPreset(config: TuningConfig) {
        viewModelScope.launch {
            applyConfig(config)
        }
    }

    private fun buildCurrentConfig(): TuningConfig {
        val cpuConfigs = _cpuClusters.value.map { cluster ->
            CPUClusterConfig(
                cluster = cluster.clusterNumber,
                minFreq = cluster.currentMinFreq,
                maxFreq = cluster.currentMaxFreq,
                governor = cluster.governor,
                disabledCores = emptyList()
            )
        }
        return TuningConfig(
            cpuClusters = cpuConfigs,
            gpu = null,
            thermal = ThermalConfig(),
            ram = RAMConfig(),
            additional = AdditionalConfig()
        )
    }

    private suspend fun applyConfig(config: TuningConfig) {
        config.cpuClusters.forEach { clusterConfig ->
            cpuUseCase.setClusterFrequency(
                clusterConfig.cluster,
                clusterConfig.minFreq,
                clusterConfig.maxFreq
            )
            cpuUseCase.setClusterGovernor(clusterConfig.cluster, clusterConfig.governor)
            clusterConfig.disabledCores.forEach { core ->
                cpuUseCase.setCoreOnline(core, false)
            }
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

        delay(1000)
        refreshCurrentValues()
    }

    suspend fun checkMagiskAvailability(): Boolean {
        return gpuUseCase.checkMagiskAvailability()
    }

    fun refreshData() {
        viewModelScope.launch {
            refreshCurrentValues()
        }
    }
}

data class ClusterUIState(
    val minFreq: Float = 0f,
    val maxFreq: Float = 0f,
    val governor: String = ""
)