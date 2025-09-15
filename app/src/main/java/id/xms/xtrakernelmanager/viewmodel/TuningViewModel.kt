package id.xms.xtrakernelmanager.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.xms.xtrakernelmanager.data.repository.SystemRepository
import id.xms.xtrakernelmanager.data.repository.ThermalRepository
import id.xms.xtrakernelmanager.data.repository.TuningRepository
import id.xms.xtrakernelmanager.service.ThermalService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TuningViewModel @Inject constructor(
    private val application: Application,
    private val repo: TuningRepository,
    private val thermalRepo: ThermalRepository,
    private val systemRepo: SystemRepository
) : AndroidViewModel(application) {

    private val thermalPrefs: SharedPreferences by lazy {
        application.getSharedPreferences("thermal_settings_prefs", Context.MODE_PRIVATE)
    }
    private val KEY_LAST_APPLIED_THERMAL_INDEX = "last_applied_thermal_index"

    val cpuClusters = listOf("cpu0", "cpu4", "cpu7")
    
    // Dynamic cluster information with proper names
    private val _dynamicCpuClusters = MutableStateFlow<List<String>>(emptyList())
    val dynamicCpuClusters: StateFlow<List<String>> = _dynamicCpuClusters.asStateFlow()
    
    init {
        // Initialize dynamic cluster information
        fetchDynamicCpuClusters()
    }
    
    private fun fetchDynamicCpuClusters() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val clusters = systemRepo.getCpuClusters()
                // Map the dynamic cluster names to the corresponding cpu cluster identifiers
                val clusterNames = clusters.map { cluster ->
                    when {
                        cluster.name.contains("Little", ignoreCase = true) -> "cpu0"
                        cluster.name.contains("Big", ignoreCase = true) -> "cpu4"
                        cluster.name.contains("Prime", ignoreCase = true) -> "cpu7"
                        else -> {
                            // Fallback to original naming
                            when (cluster.name) {
                                "Efficiency Cluster" -> "cpu0"
                                "Performance Cluster" -> "cpu7"
                                else -> "cpu4" // Mid cluster
                            }
                        }
                    }
                }
                _dynamicCpuClusters.value = clusterNames
            } catch (e: Exception) {
                Log.e("TuningViewModel", "Error fetching dynamic CPU clusters", e)
                // Fallback to hardcoded values
                _dynamicCpuClusters.value = cpuClusters
            }
        }
    }

    /* ---------------- CPU ---------------- */
    private val _coreStates = MutableStateFlow(List(8) { true })
    val coreStates: StateFlow<List<Boolean>> = _coreStates.asStateFlow()

    private val _generalAvailableCpuGovernors = MutableStateFlow<List<String>>(emptyList())
    val generalAvailableCpuGovernors: StateFlow<List<String>> = _generalAvailableCpuGovernors.asStateFlow()

    private val _availableCpuFrequenciesPerClusterMap = MutableStateFlow<Map<String, List<Int>>>(emptyMap())
    private val _currentCpuGovernors = mutableMapOf<String, MutableStateFlow<String>>()
    private val _currentCpuFrequencies = mutableMapOf<String, MutableStateFlow<Pair<Int, Int>>>()

    /* ---------------- GPU ---------------- */
    private val _availableGpuGovernors = MutableStateFlow<List<String>>(emptyList())
    val availableGpuGovernors: StateFlow<List<String>> = _availableGpuGovernors.asStateFlow()

    private val _currentGpuGovernor = MutableStateFlow("...")
    val currentGpuGovernor: StateFlow<String> = _currentGpuGovernor.asStateFlow()

    private val _availableGpuFrequencies = MutableStateFlow<List<Int>>(emptyList())
    val availableGpuFrequencies: StateFlow<List<Int>> = _availableGpuFrequencies.asStateFlow()

    private val _currentGpuMinFreq = MutableStateFlow(0)
    val currentGpuMinFreq: StateFlow<Int> = _currentGpuMinFreq.asStateFlow()

    private val _currentGpuMaxFreq = MutableStateFlow(0)
    val currentGpuMaxFreq: StateFlow<Int> = _currentGpuMaxFreq.asStateFlow()

    private val _gpuPowerLevelRange = MutableStateFlow(0f to 5f)
    val gpuPowerLevelRange: StateFlow<Pair<Float, Float>> = _gpuPowerLevelRange.asStateFlow()

    private val _currentGpuPowerLevel = MutableStateFlow(0f)
    val currentGpuPowerLevel: StateFlow<Float> = _currentGpuPowerLevel.asStateFlow()

    /* ---------------- OpenGL / Vulkan / Renderer ---------------- */
    private val _currentOpenGlesDriver = MutableStateFlow("Loading...")
    val currentOpenGlesDriver: StateFlow<String> = _currentOpenGlesDriver.asStateFlow()

    private val _currentGpuRenderer = MutableStateFlow("Loading...")
    val currentGpuRenderer: StateFlow<String> = _currentGpuRenderer.asStateFlow()

    private val _vulkanApiVersion = MutableStateFlow("Loading...")
    val vulkanApiVersion: StateFlow<String> = _vulkanApiVersion.asStateFlow()

    val availableGpuRenderers = listOf(
        "Default", "OpenGL", "Vulkan", "ANGLE", "OpenGL (SKIA)", "Vulkan (SKIA)"
    )

    /* ---------------- Reboot dialog ---------------- */
    private val _showRebootConfirmationDialog = MutableStateFlow(false)
    val showRebootConfirmationDialog: StateFlow<Boolean> = _showRebootConfirmationDialog.asStateFlow()

    private val _rebootCommandFeedback = MutableSharedFlow<String>()
    val rebootCommandFeedback: SharedFlow<String> = _rebootCommandFeedback.asSharedFlow()

    /* ---------------- RAM Control ---------------- */
    private val _zramEnabled = MutableStateFlow(false)
    val zramEnabled: StateFlow<Boolean> = _zramEnabled.asStateFlow()

    private val _zramDisksize = MutableStateFlow(536870912L) // 512 MB default
    val zramDisksize: StateFlow<Long> = _zramDisksize.asStateFlow()

    private val _compressionAlgorithms = MutableStateFlow<List<String>>(emptyList())
    val compressionAlgorithms: StateFlow<List<String>> = _compressionAlgorithms.asStateFlow()

    private val _currentCompression = MutableStateFlow("")
    val currentCompression: StateFlow<String> = _currentCompression.asStateFlow()

    private val _swappiness = MutableStateFlow(60)
    val swappiness: StateFlow<Int> = _swappiness.asStateFlow()

    private val _dirtyRatio = MutableStateFlow(20)
    val dirtyRatio: StateFlow<Int> = _dirtyRatio.asStateFlow()

    private val _dirtyBackgroundRatio = MutableStateFlow(10)
    val dirtyBackgroundRatio: StateFlow<Int> = _dirtyBackgroundRatio.asStateFlow()

    private val _dirtyWriteback = MutableStateFlow(30)
    val dirtyWriteback: StateFlow<Int> = _dirtyWriteback.asStateFlow()

    private val _dirtyExpireCentisecs = MutableStateFlow(300)
    val dirtyExpireCentisecs: StateFlow<Int> = _dirtyExpireCentisecs.asStateFlow()

    private val _minFreeMemory = MutableStateFlow(128)
    val minFreeMemory: StateFlow<Int> = _minFreeMemory.asStateFlow()

    // Add swap size state for traditional swap (not ZRAM)
    private val _swapSize = MutableStateFlow(0L) // in bytes
    val swapSize: StateFlow<Long> = _swapSize.asStateFlow()

    private val _maxSwapSize = MutableStateFlow(8L * 1024 * 1024 * 1024) // 8GB max
    val maxSwapSize: StateFlow<Long> = _maxSwapSize.asStateFlow()

    // Add loading states for swap operations
    private val _isSwapLoading = MutableStateFlow(false)
    val isSwapLoading: StateFlow<Boolean> = _isSwapLoading.asStateFlow()

    private val _swapLogs = MutableStateFlow<List<String>>(emptyList())
    val swapLogs: StateFlow<List<String>> = _swapLogs.asStateFlow()

    /* Max ZRAM otomatis 6 GB untuk 8 GB RAM */
    private val _maxZramSize = MutableStateFlow(repo.calculateMaxZramSize())
    val maxZramSize: StateFlow<Long> = _maxZramSize.asStateFlow()

    /* ---------------- Thermal ---------------- */
    private val _isTuningDataLoading = MutableStateFlow(true)
    val isTuningDataLoading: StateFlow<Boolean> = _isTuningDataLoading.asStateFlow()

    private val _currentThermalModeIndex = MutableStateFlow(-1)
    val currentThermalModeIndex: StateFlow<Int> = _currentThermalModeIndex.asStateFlow()

    val currentThermalProfileName: StateFlow<String> =
        combine(_currentThermalModeIndex, _isTuningDataLoading) { idx, loading ->
            if (loading) "Loading..."
            else thermalRepo.getCurrentThermalProfileName(idx)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Loading...")

    val supportedThermalProfiles: StateFlow<List<ThermalRepository.ThermalProfile>> =
        thermalRepo.getSupportedThermalProfiles()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /* ---------------- Init ---------------- */
    init {
        Log.d("TuningVM_Init", "ViewModel initializing...")
        initializeCpuStateFlows()
        // Only fetch initial data if not already loaded
        if (_isTuningDataLoading.value) {
            fetchAllInitialData()
        }
        refreshCoreStates()
        fetchRamControlData()
        Log.d("TuningVM_Init", "ViewModel initialization complete.")
    }

    /* ---------------- CPU ---------------- */
    private fun initializeCpuStateFlows() {
        Log.d("TuningVM_Init", "Initializing CPU StateFlows for clusters: $cpuClusters")
        cpuClusters.forEach { cluster ->
            _currentCpuGovernors.getOrPut(cluster) { MutableStateFlow("...") }
            _currentCpuFrequencies.getOrPut(cluster) { MutableStateFlow(0 to 0) }
        }
    }

    private suspend fun fetchAllCpuData() {
        Log.d("TuningVM_CPU", "Fetching all CPU data...")
        val tempGovernors = mutableMapOf<String, List<String>>()
        val tempFreqs = mutableMapOf<String, List<Int>>()

        try {
            cpuClusters.forEach { cluster ->
                coroutineScope {
                    launch { 
                        try {
                            repo.getCpuGov(cluster).take(1).collect { _currentCpuGovernors[cluster]?.value = it }
                        } catch (e: Exception) {
                            Log.e("TuningVM_CPU", "Error fetching CPU governor for $cluster", e)
                        }
                    }
                    launch { 
                        try {
                            repo.getCpuFreq(cluster).take(1).collect { _currentCpuFrequencies[cluster]?.value = it }
                        } catch (e: Exception) {
                            Log.e("TuningVM_CPU", "Error fetching CPU frequency for $cluster", e)
                        }
                    }
                    launch { 
                        try {
                            repo.getAvailableCpuGovernors(cluster).collect { tempGovernors[cluster] = it }
                        } catch (e: Exception) {
                            Log.e("TuningVM_CPU", "Error fetching available CPU governors for $cluster", e)
                        }
                    }
                    launch { 
                        try {
                            repo.getAvailableCpuFrequencies(cluster).collect { tempFreqs[cluster] = it }
                        } catch (e: Exception) {
                            Log.e("TuningVM_CPU", "Error fetching available CPU frequencies for $cluster", e)
                        }
                    }
                }
            }
            _availableCpuFrequenciesPerClusterMap.value = tempFreqs
            if (tempGovernors.isNotEmpty()) _generalAvailableCpuGovernors.value = tempGovernors.values.flatten().distinct().sorted()
            Log.d("TuningVM_CPU", "Finished fetching all CPU data.")
        } catch (e: Exception) {
            Log.e("TuningVM_CPU", "Error in fetchAllCpuData", e)
        }
    }

    fun getCpuGov(cluster: String): StateFlow<String> = _currentCpuGovernors.getOrPut(cluster) { MutableStateFlow("...") }.asStateFlow()
    fun getCpuFreq(cluster: String): StateFlow<Pair<Int, Int>> = _currentCpuFrequencies.getOrPut(cluster) { MutableStateFlow(0 to 0) }.asStateFlow()
    fun getAvailableCpuFrequencies(cluster: String): StateFlow<List<Int>> = _availableCpuFrequenciesPerClusterMap.map { it[cluster] ?: emptyList() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun setCpuGov(cluster: String, gov: String) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setCpuGov(cluster, gov)) {
            repo.getCpuGov(cluster).take(1).collect { _currentCpuGovernors[cluster]?.value = it }
        }
    }

    fun setCpuFreq(cluster: String, min: Int, max: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setCpuFreq(cluster, min, max)) {
            repo.getCpuFreq(cluster).take(1).collect { _currentCpuFrequencies[cluster]?.value = it }
        }
    }

    fun toggleCore(coreId: Int) = viewModelScope.launch(Dispatchers.IO) {
        val newStates = _coreStates.value.toMutableList()
        val newState = !newStates[coreId]
        if (!newState && newStates.count { it } == 1) {
            _rebootCommandFeedback.emit("Setidaknya 1 core harus tetap online")
            return@launch
        }
        if (repo.setCoreOnline(coreId, newState)) {
            newStates[coreId] = newState
            _coreStates.value = newStates
        } else {
            Log.e("TuningVM_CPU", "Failed toggle core $coreId")
        }
    }

    fun refreshCoreStates() = viewModelScope.launch(Dispatchers.IO) {
        _coreStates.value = (0 until 8).map { repo.getCoreOnline(it) }
    }

    /* ---------------- GPU ---------------- */
    fun fetchGpuData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            _availableGpuGovernors.value = repo.getAvailableGpuGovernors().first()
            _currentGpuGovernor.value = repo.getGpuGov().first()
            _availableGpuFrequencies.value = repo.getAvailableGpuFrequencies().first()
            val (min, max) = repo.getGpuFreq().first()
            _currentGpuMinFreq.value = min
            _currentGpuMaxFreq.value = max
            Log.d("ViewModelGPU", "StateFlows updated: _currentGpuMinFreq=${_currentGpuMinFreq.value}, _currentGpuMaxFreq=${_currentGpuMaxFreq.value}")
            _gpuPowerLevelRange.value = repo.getGpuPowerLevelRange().first()
            _currentGpuPowerLevel.value = repo.getCurrentGpuPowerLevel().first()
        } catch (e: Exception) {
            Log.e("ViewModelGPU", "Error fetching GPU data", e)
        }
    }

    fun setGpuGovernor(gov: String) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setGpuGov(gov)) {
            repo.getGpuGov().take(1).collect { _currentGpuGovernor.value = it }
        }
    }

    fun setGpuMinFrequency(freqKHz: Int) = viewModelScope.launch(Dispatchers.IO) {
        repo.setGpuMinFreq(freqKHz)
        if (repo.setGpuMinFreq(freqKHz)) {
            val (min, max) = repo.getGpuFreq().first()
            _currentGpuMinFreq.value = min
            fetchGpuData()
        }
    }

    fun setGpuMaxFrequency(freqKHz: Int) = viewModelScope.launch(Dispatchers.IO) {
        repo.setGpuMaxFreq(freqKHz)
        if (repo.setGpuMaxFreq(freqKHz)) {
            val (min, max) = repo.getGpuFreq().first()
            _currentGpuMaxFreq.value = max
            fetchGpuData()
        }
    }

    fun setGpuPowerLevel(level: Float) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setGpuPowerLevel(level)) {
            repo.getCurrentGpuPowerLevel().take(1).collect { _currentGpuPowerLevel.value = it }
        }
    }

    /* ---------------- OpenGL / Vulkan ---------------- */
    internal fun fetchOpenGlesDriver() = viewModelScope.launch(Dispatchers.IO) {
        repo.getOpenGlesDriver().collect { _currentOpenGlesDriver.value = it }
    }

    internal fun fetchCurrentGpuRenderer() = viewModelScope.launch(Dispatchers.IO) {
        repo.getGpuRenderer().collect { renderer ->
            _currentGpuRenderer.value = renderer
        }
    }

    internal fun fetchVulkanApiVersion() = viewModelScope.launch(Dispatchers.IO) {
        repo.getVulkanApiVersion().collect { version ->
            _vulkanApiVersion.value = version
        }
    }

    fun userSelectedGpuRenderer(renderer: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.setGpuRenderer(renderer).collect { success ->
            if (success) {
                _currentGpuRenderer.value = renderer
                _showRebootConfirmationDialog.value = true
            } else {
                _rebootCommandFeedback.emit("Gagal mengatur GPU Renderer.")
            }
        }
    }

    fun confirmAndRebootDevice() {
        _showRebootConfirmationDialog.value = false
        viewModelScope.launch { repo.rebootDevice().collect { /* ignore, device reboot */ } }
    }

    fun cancelRebootConfirmation() {
        _showRebootConfirmationDialog.value = false
    }

    /* ---------------- RAM Control ---------------- */
    private fun fetchRamControlData() = viewModelScope.launch {
        launch(Dispatchers.IO) { repo.getZramEnabled().collect { _zramEnabled.value = it } }
        launch(Dispatchers.IO) { repo.getZramDisksize().collect { _zramDisksize.value = it } }
        launch(Dispatchers.IO) {
            repo.getCompressionAlgorithms().collect {
                _compressionAlgorithms.value = it
                repo.getCurrentCompression().firstOrNull()?.let { currentAlgo -> _currentCompression.value = currentAlgo }
            }
        }
        launch(Dispatchers.IO) { repo.getSwappiness().collect { _swappiness.value = it } }
        launch(Dispatchers.IO) { repo.getDirtyRatio().collect { _dirtyRatio.value = it } }
        launch(Dispatchers.IO) { repo.getDirtyBackgroundRatio().collect { _dirtyBackgroundRatio.value = it } }
        launch(Dispatchers.IO) { repo.getDirtyWriteback().collect { _dirtyWriteback.value = it } }
        launch(Dispatchers.IO) { repo.getDirtyExpireCentisecs().collect { _dirtyExpireCentisecs.value = it } }
        launch(Dispatchers.IO) { repo.getMinFreeMemory().collect { _minFreeMemory.value = it } }
        launch(Dispatchers.IO) { repo.getSwapSize().collect { _swapSize.value = it } }
    }

    fun setZramEnabled(enabled: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        repo.setZramEnabled(enabled).collect { _zramEnabled.value = it }
    }

    fun setZramDisksize(sizeBytes: Long) = viewModelScope.launch(Dispatchers.IO) {
        val max = repo.calculateMaxZramSize()
        if (sizeBytes < 512 * 1024 * 1024 || sizeBytes > max) {
            _rebootCommandFeedback.emit("Ukuran ZRAM tidak valid (512 MB - ${max / 1024 / 1024} MB)")
            return@launch
        }
        if (repo.setZramDisksize(sizeBytes)) {
            repo.getZramDisksize().take(1).collect { _zramDisksize.value = it }
        }
    }

    fun setCompression(algo: String) = viewModelScope.launch(Dispatchers.IO) {
        if (algo != _currentCompression.value) {
            if (repo.setCompressionAlgorithm(algo)) {
                repo.getCurrentCompression().take(1).collect { _currentCompression.value = it }
            }
        }
    }

    fun setSwappiness(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setSwappiness(value)) {
            repo.getSwappiness().take(1).collect { _swappiness.value = it }
        }
    }

    fun setDirtyRatio(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setDirtyRatio(value)) {
            repo.getDirtyRatio().take(1).collect { _dirtyRatio.value = it }
        }
    }

    fun setDirtyBackgroundRatio(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setDirtyBackgroundRatio(value)) {
            repo.getDirtyBackgroundRatio().take(1).collect { _dirtyBackgroundRatio.value = it }
        }
    }

    fun setDirtyWriteback(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setDirtyWriteback(value * 100)) {
            repo.getDirtyWriteback().take(1).collect { _dirtyWriteback.value = it }
        }
    }

    fun setDirtyExpireCentisecs(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setDirtyExpireCentisecs(value)) {
            repo.getDirtyExpireCentisecs().take(1).collect { _dirtyExpireCentisecs.value = it }
        }
    }

    fun setMinFreeMemory(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setMinFreeMemory(value * 1024)) {
            repo.getMinFreeMemory().take(1).collect { _minFreeMemory.value = it }
        }
    }

    fun setSwapSize(sizeBytes: Long) = viewModelScope.launch(Dispatchers.IO) {
        val maxSize = _maxSwapSize.value
        if (sizeBytes < 0 || sizeBytes > maxSize) {
            _rebootCommandFeedback.emit("Invalid swap size (0 - ${maxSize / 1024 / 1024 / 1024} GB)")
            return@launch
        }

        _isSwapLoading.value = true
        _swapLogs.value = emptyList() // Reset logs

        addSwapLog("🔄 Starting swap configuration...")
        delay(500)

        if (sizeBytes == 0L) {
            addSwapLog("🗑️ Disabling swap...")
            delay(300)
            addSwapLog("📋 Running: swapoff -a")
            delay(500)
        } else {
            val sizeMB = sizeBytes / 1024 / 1024
            addSwapLog("⚙️ Setting swap size to ${sizeMB}MB...")
            delay(300)
            addSwapLog("🗑️ Removing old swap file...")
            delay(400)
            addSwapLog("📋 Running: swapoff /data/swapfile")
            delay(300)
            addSwapLog("📋 Running: rm -f /data/swapfile")
            delay(500)
            addSwapLog("📝 Creating new swap file (${sizeMB}MB)...")
            delay(800)
            addSwapLog("📋 Running: dd if=/dev/zero of=/data/swapfile bs=1M count=${sizeMB}")
            delay(1000)
            addSwapLog("🔒 Setting file permissions...")
            delay(300)
            addSwapLog("📋 Running: chmod 600 /data/swapfile")
            delay(400)
            addSwapLog("🔧 Formatting as swap...")
            delay(500)
            addSwapLog("📋 Running: mkswap /data/swapfile")
            delay(600)
            addSwapLog("✅ Activating swap...")
            delay(400)
            addSwapLog("📋 Running: swapon /data/swapfile")
            delay(500)
        }

        if (repo.setSwapSize(sizeBytes)) {
            _swapSize.value = sizeBytes
            if (sizeBytes == 0L) {
                addSwapLog("✅ Swap successfully disabled!")
                _rebootCommandFeedback.emit("Swap successfully disabled")
            } else {
                addSwapLog("✅ Swap successfully set to ${sizeBytes / 1024 / 1024}MB!")
                _rebootCommandFeedback.emit("Swap size successfully set to ${sizeBytes / 1024 / 1024} MB")
            }
        } else {
            addSwapLog("❌ Failed to configure swap!")
            _rebootCommandFeedback.emit("Failed to set swap size")
        }

        delay(1000)
        _isSwapLoading.value = false
    }

    private fun addSwapLog(message: String) {
        val currentLogs = _swapLogs.value.toMutableList()
        currentLogs.add(message)
        _swapLogs.value = currentLogs
    }

    /* ---------------- Thermal ---------------- */
    private fun fetchCurrentThermalMode(isInitialLoad: Boolean = false) {
        if (isInitialLoad) _isTuningDataLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                thermalRepo.getCurrentThermalModeIndex()
                    .catch { e ->
                        Log.e("TuningVM_Thermal", "Error getting current thermal mode", e)
                        if (isInitialLoad) applyLastSavedThermalProfile() else _isTuningDataLoading.value = false
                    }
                    .collect { index ->
                        _currentThermalModeIndex.value = index
                        if (isInitialLoad) applyLastSavedThermalProfile() else _isTuningDataLoading.value = false
                    }
            } catch (e: Exception) {
                Log.e("TuningVM_Thermal", "Error in fetchCurrentThermalMode", e)
                if (isInitialLoad) {
                    applyLastSavedThermalProfile()
                } else {
                    _isTuningDataLoading.value = false
                }
            }
        }
    }

    private suspend fun applyLastSavedThermalProfile() {
        try {
            val idx = thermalPrefs.getInt(KEY_LAST_APPLIED_THERMAL_INDEX, -1)
            val profile = thermalRepo.availableThermalProfiles.find { it.index == idx }
            if (profile != null && _currentThermalModeIndex.value != idx) {
                setThermalProfileInternal(profile, isRestoring = true)
            } else {
                _isTuningDataLoading.value = false
            }
        } catch (e: Exception) {
            _isTuningDataLoading.value = false
        }
    }

    private suspend fun setThermalProfileInternal(profile: ThermalRepository.ThermalProfile, isRestoring: Boolean) {
        if (!isRestoring) _isTuningDataLoading.value = true
        thermalRepo.setThermalModeIndex(profile.index).collect { ok ->
            if (ok) {
                _currentThermalModeIndex.value = profile.index
                if (!isRestoring) thermalPrefs.edit().putInt(KEY_LAST_APPLIED_THERMAL_INDEX, profile.index).apply()
                // Mulai ThermalService untuk menjaga pengaturan di background
                if (profile.index != 0) { // Jangan mulai kalau mode Disable
                    val intent = Intent(application, ThermalService::class.java)
                    intent.putExtra("thermal_mode", profile.index)
                    application.startService(intent)
                    Log.d("TuningVM_Thermal", "Started ThermalService for mode ${profile.index}")
                } else {
                    stopThermalService()
                }
            } else {
                fetchCurrentThermalMode()
            }
            _isTuningDataLoading.value = false
        }
    }

    private fun stopThermalService() {
        val intent = Intent(application, ThermalService::class.java)
        application.stopService(intent)
        Log.d("TuningVM_Thermal", "Stopped ThermalService")
    }

    fun setThermalProfile(profile: ThermalRepository.ThermalProfile) =
        viewModelScope.launch { setThermalProfileInternal(profile, isRestoring = false) }

    /* ---------------- Init ---------------- */
    private fun fetchAllInitialData() {
        viewModelScope.launch {
            _isTuningDataLoading.value = true
            
            // Set a timeout to ensure loading doesn't get stuck
            val timeoutJob = launch {
                delay(10000) // 10 seconds timeout
                if (_isTuningDataLoading.value) {
                    Log.w("TuningVM_Init", "Data fetch timeout, forcing loading to false")
                    _isTuningDataLoading.value = false
                }
            }
            
            try {
                // Launch all data fetching operations in parallel
                val jobs = listOf(
                    launch(Dispatchers.IO) { fetchAllCpuData() },
                    launch(Dispatchers.IO) { fetchGpuData() },
                    launch(Dispatchers.IO) { fetchCurrentThermalMode(isInitialLoad = true) },
                    launch(Dispatchers.IO) { fetchOpenGlesDriver() },
                    launch(Dispatchers.IO) { fetchCurrentGpuRenderer() },
                    launch(Dispatchers.IO) { fetchVulkanApiVersion() },
                    launch(Dispatchers.IO) { fetchRamControlData() }
                )
                
                // Wait for all jobs to complete
                jobs.forEach { it.join() }
            } catch (e: Exception) {
                Log.e("TuningVM_Init", "Error during initial data fetch", e)
            } finally {
                // Cancel timeout job
                timeoutJob.cancel()
                // Always ensure loading is set to false
                _isTuningDataLoading.value = false
                Log.d("TuningVM_Init", "Initial data fetch completed, loading set to false")
            }
        }
    }
}
