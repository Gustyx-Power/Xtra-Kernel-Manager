package id.xms.xtrakernelmanager.ui.screens.functionalrom

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.domain.usecase.FunctionalRomUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Functional ROM Screen
 */
data class FunctionalRomUiState(
    val isLoading: Boolean = true,
    val isVipCommunity: Boolean = false,
    
    // Feature availability (dynamically detected kernel nodes)
    val bypassChargingAvailable: Boolean = false,
    val bypassChargingNodePath: String? = null,
    val chargingLimitAvailable: Boolean = false,
    val chargingLimitNodePath: String? = null,
    val dt2wAvailable: Boolean = false,
    val dt2wNodePath: String? = null,
    
    // Native Feature States
    val bypassChargingEnabled: Boolean = false,
    val chargingLimitEnabled: Boolean = false,
    val chargingLimitValue: Int = 80,
    val forceRefreshRateEnabled: Boolean = false,
    val forceRefreshRateValue: Int = 60,
    val doubleTapWakeEnabled: Boolean = false,
    
    // Property-based Feature States (Shimoku's area)
    val touchBoostEnabled: Boolean = false,
    
    // Play Integrity States
    val playIntegrityFixEnabled: Boolean = false,
    val spoofBootloaderEnabled: Boolean = false,
    val gamePropsEnabled: Boolean = false,
    val unlimitedPhotosEnabled: Boolean = false,
    val netflixSpoofEnabled: Boolean = false,
    
    // Xiaomi Touch States
    val touchGameModeEnabled: Boolean = false,
    val touchActiveModeEnabled: Boolean = false,
    
    // Display Features
    val unlockNitsEnabled: Boolean = false,
    val dynamicRefreshRateEnabled: Boolean = false,
    val dcDimmingEnabled: Boolean = false,
    
    // System Features
    val performanceModeEnabled: Boolean = false,
    val smartChargingEnabled: Boolean = false
)

class FunctionalRomViewModel(
    private val preferencesManager: PreferencesManager,
    private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "FunctionalRomViewModel"
    }

    private val useCase = FunctionalRomUseCase()

    private val _uiState = MutableStateFlow(FunctionalRomUiState())
    val uiState: StateFlow<FunctionalRomUiState> = _uiState.asStateFlow()

    init {
        loadInitialState()
    }

    /**
     * Load initial state: check VIP community, detect available nodes, sync states
     */
    private fun loadInitialState() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 1. Check VIP community access
                val isVip = useCase.checkVipCommunity()
                Log.d(TAG, "VIP Community: $isVip")

                // 2. Detect available kernel nodes
                val bypassChargingNode = useCase.findBypassChargingNode()
                val chargingLimitNode = useCase.findChargingLimitNode()
                val dt2wNode = useCase.findDt2wNode()

                Log.d(TAG, "Detected nodes - Bypass: $bypassChargingNode, Limit: $chargingLimitNode, DT2W: $dt2wNode")

                // 3. Sync current states from system
                val bypassChargingState = bypassChargingNode?.let { 
                    useCase.getBypassChargingState(it) 
                } ?: false
                
                val dt2wState = dt2wNode?.let { 
                    useCase.getDoubleTapToWakeState(it) 
                } ?: false
                
                val chargingLimitValue = chargingLimitNode?.let { 
                    useCase.getChargingLimitValue(it) 
                } ?: 0
                
                val currentRefreshRate = useCase.getCurrentRefreshRate()
                val touchBoostState = useCase.getTouchBoostState()
                
                // Play Integrity states
                val playIntegrityFixState = useCase.getPlayIntegrityFixState()
                val spoofBootloaderState = useCase.getSpoofBootloaderState()
                val gamePropsState = useCase.getGamePropsState()
                val unlimitedPhotosState = useCase.getUnlimitedPhotosState()
                val netflixSpoofState = useCase.getNetflixSpoofState()
                
                // Xiaomi Touch states
                val touchGameModeState = useCase.getTouchGameModeState()
                val touchActiveModeState = useCase.getTouchActiveModeState()

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isVipCommunity = isVip,
                        // Availability
                        bypassChargingAvailable = bypassChargingNode != null,
                        bypassChargingNodePath = bypassChargingNode,
                        chargingLimitAvailable = chargingLimitNode != null,
                        chargingLimitNodePath = chargingLimitNode,
                        dt2wAvailable = dt2wNode != null,
                        dt2wNodePath = dt2wNode,
                        // Native states
                        bypassChargingEnabled = bypassChargingState,
                        doubleTapWakeEnabled = dt2wState,
                        chargingLimitValue = if (chargingLimitValue > 0) chargingLimitValue else 80,
                        forceRefreshRateValue = currentRefreshRate,
                        // Property-based states
                        touchBoostEnabled = touchBoostState,
                        playIntegrityFixEnabled = playIntegrityFixState,
                        spoofBootloaderEnabled = spoofBootloaderState,
                        gamePropsEnabled = gamePropsState,
                        unlimitedPhotosEnabled = unlimitedPhotosState,
                        netflixSpoofEnabled = netflixSpoofState,
                        touchGameModeEnabled = touchGameModeState,
                        touchActiveModeEnabled = touchActiveModeState
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading initial state: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, isVipCommunity = false) }
            }
        }
    }

    // ==================== Native Feature Actions ====================

    fun setBypassCharging(enabled: Boolean) {
        val nodePath = _uiState.value.bypassChargingNodePath ?: return
        viewModelScope.launch {
            val result = useCase.setBypassCharging(enabled, nodePath)
            if (result.isSuccess) {
                _uiState.update { it.copy(bypassChargingEnabled = enabled) }
                Log.d(TAG, "Bypass charging set to: $enabled")
            } else {
                Log.e(TAG, "Failed to set bypass charging: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun setDoubleTapToWake(enabled: Boolean) {
        val nodePath = _uiState.value.dt2wNodePath ?: return
        viewModelScope.launch {
            val result = useCase.setDoubleTapToWake(enabled, nodePath)
            if (result.isSuccess) {
                _uiState.update { it.copy(doubleTapWakeEnabled = enabled) }
                Log.d(TAG, "DT2W set to: $enabled")
            } else {
                Log.e(TAG, "Failed to set DT2W: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun setChargingLimit(enabled: Boolean) {
        _uiState.update { it.copy(chargingLimitEnabled = enabled) }
        if (enabled) {
            applyChargingLimitValue(_uiState.value.chargingLimitValue)
        }
    }

    fun setChargingLimitValue(value: Int) {
        _uiState.update { it.copy(chargingLimitValue = value) }
        if (_uiState.value.chargingLimitEnabled) {
            applyChargingLimitValue(value)
        }
    }

    private fun applyChargingLimitValue(value: Int) {
        val nodePath = _uiState.value.chargingLimitNodePath ?: return
        viewModelScope.launch {
            val result = useCase.setChargingLimit(value, nodePath)
            if (result.isSuccess) {
                Log.d(TAG, "Charging limit set to: $value")
            } else {
                Log.e(TAG, "Failed to set charging limit: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun setForceRefreshRate(enabled: Boolean) {
        _uiState.update { it.copy(forceRefreshRateEnabled = enabled) }
        viewModelScope.launch {
            if (enabled) {
                useCase.setForceRefreshRate(_uiState.value.forceRefreshRateValue)
            } else {
                useCase.resetRefreshRate()
            }
        }
    }

    fun setForceRefreshRateValue(hz: Int) {
        _uiState.update { it.copy(forceRefreshRateValue = hz) }
        if (_uiState.value.forceRefreshRateEnabled) {
            viewModelScope.launch {
                useCase.setForceRefreshRate(hz)
            }
        }
    }

    // ==================== Property-based Feature Actions ====================

    fun setTouchBoost(enabled: Boolean) {
        viewModelScope.launch {
            val result = useCase.setTouchBoost(enabled)
            if (result.isSuccess) {
                _uiState.update { it.copy(touchBoostEnabled = enabled) }
                Log.d(TAG, "Touch boost set to: $enabled")
            }
        }
    }

    fun setPlayIntegrityFix(enabled: Boolean) {
        viewModelScope.launch {
            val result = useCase.setPlayIntegrityFix(enabled)
            if (result.isSuccess) {
                _uiState.update { it.copy(playIntegrityFixEnabled = enabled) }
            }
        }
    }

    fun setSpoofBootloader(enabled: Boolean) {
        viewModelScope.launch {
            val result = useCase.setSpoofBootloader(enabled)
            if (result.isSuccess) {
                _uiState.update { it.copy(spoofBootloaderEnabled = enabled) }
            }
        }
    }

    fun setGameProps(enabled: Boolean) {
        viewModelScope.launch {
            val result = useCase.setGameProps(enabled)
            if (result.isSuccess) {
                _uiState.update { it.copy(gamePropsEnabled = enabled) }
            }
        }
    }

    fun setUnlimitedPhotos(enabled: Boolean) {
        viewModelScope.launch {
            val result = useCase.setUnlimitedPhotos(enabled)
            if (result.isSuccess) {
                _uiState.update { it.copy(unlimitedPhotosEnabled = enabled) }
            }
        }
    }

    fun setNetflixSpoof(enabled: Boolean) {
        viewModelScope.launch {
            val result = useCase.setNetflixSpoof(enabled)
            if (result.isSuccess) {
                _uiState.update { it.copy(netflixSpoofEnabled = enabled) }
            }
        }
    }

    fun setTouchGameMode(enabled: Boolean) {
        viewModelScope.launch {
            val result = useCase.setTouchGameMode(enabled)
            if (result.isSuccess) {
                _uiState.update { it.copy(touchGameModeEnabled = enabled) }
            }
        }
    }

    fun setTouchActiveMode(enabled: Boolean) {
        viewModelScope.launch {
            val result = useCase.setTouchActiveMode(enabled)
            if (result.isSuccess) {
                _uiState.update { it.copy(touchActiveModeEnabled = enabled) }
            }
        }
    }

    // ==================== UI-only toggles (no backend yet) ====================

    fun setUnlockNits(enabled: Boolean) {
        _uiState.update { it.copy(unlockNitsEnabled = enabled) }
    }

    fun setDynamicRefreshRate(enabled: Boolean) {
        _uiState.update { it.copy(dynamicRefreshRateEnabled = enabled) }
    }

    fun setDcDimming(enabled: Boolean) {
        _uiState.update { it.copy(dcDimmingEnabled = enabled) }
    }

    fun setPerformanceMode(enabled: Boolean) {
        _uiState.update { it.copy(performanceModeEnabled = enabled) }
    }

    fun setSmartCharging(enabled: Boolean) {
        _uiState.update { it.copy(smartChargingEnabled = enabled) }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared")
    }
}
