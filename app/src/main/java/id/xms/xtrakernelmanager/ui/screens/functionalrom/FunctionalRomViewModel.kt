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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI State for Functional ROM Screen */
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
    val maxBrightnessAvailable: Boolean = false,
    val maxBrightnessNodePath: String? = null,

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

    // System Features - Fix DT2W 2-step installation
    val fixDt2wEnabled: Boolean = false,
    val fixDt2wInstalling: Boolean = false,
    val overlayfsInstalled: Boolean = false,
    val fixDt2wModuleInstalled: Boolean = false,
    val showFixDt2wDialog: Boolean = false,
    val showFixDt2wUninstallDialog: Boolean = false,
    val fixDt2wStep: Int = 0, // 0 = complete, 1 = need overlayfs, 2 = need fix_dt2w
    val smartChargingEnabled: Boolean = false,
)

class FunctionalRomViewModel(
    private val preferencesManager: PreferencesManager,
    private val context: Context,
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

  /** Load initial state: check VIP community, detect available nodes, sync states */
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
        val maxBrightnessNode = useCase.findMaxBrightnessNode()

        Log.d(
            TAG,
            "Detected nodes - Bypass: $bypassChargingNode, Limit: $chargingLimitNode, DT2W: $dt2wNode, MaxBrightness: $maxBrightnessNode",
        )

        // 3. Sync current states from system
        val bypassChargingState =
            bypassChargingNode?.let { useCase.getBypassChargingState(it) } ?: false

        val dt2wState = dt2wNode?.let { useCase.getDoubleTapToWakeState(it) } ?: false

        val chargingLimitValue = chargingLimitNode?.let { useCase.getChargingLimitValue(it) } ?: 0

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

        // Load UI-only toggle states from preferences
        val savedUnlockNitsState = preferencesManager.getFunctionalRomUnlockNits().first()
        // If max brightness node is available, sync from device; otherwise use saved preference
        val unlockNitsState =
            maxBrightnessNode?.let { useCase.isUnlockNitsEnabled(it) } ?: savedUnlockNitsState
        val dynamicRefreshState = preferencesManager.getFunctionalRomDynamicRefresh().first()
        val forceRefreshState = preferencesManager.getFunctionalRomForceRefresh().first()
        val forceRefreshValue = preferencesManager.getFunctionalRomForceRefreshValue().first()
        val dcDimmingState = preferencesManager.getFunctionalRomDcDimming().first()
        // 2-step Fix DT2W: check each module separately
        val overlayfsInstalled = useCase.isOverlayfsInstalled()
        val fixDt2wModuleInstalled = useCase.isFixDt2wModuleInstalled()
        val fixDt2wStep =
            when {
              fixDt2wModuleInstalled -> 0 // Complete
              overlayfsInstalled -> 2 // Step 2: need fix_dt2w
              else -> 1 // Step 1: need overlayfs
            }
        val smartChargingState = preferencesManager.getFunctionalRomSmartCharging().first()
        val chargingLimitEnabledState = preferencesManager.getFunctionalRomChargingLimit().first()
        val savedChargingLimitValue =
            preferencesManager.getFunctionalRomChargingLimitValue().first()

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
              maxBrightnessAvailable = maxBrightnessNode != null,
              maxBrightnessNodePath = maxBrightnessNode,
              // Native states
              bypassChargingEnabled = bypassChargingState,
              doubleTapWakeEnabled = dt2wState,
              chargingLimitValue =
                  if (savedChargingLimitValue > 0) savedChargingLimitValue
                  else if (chargingLimitValue > 0) chargingLimitValue else 80,
              chargingLimitEnabled = chargingLimitEnabledState,
              forceRefreshRateValue =
                  if (forceRefreshValue > 0) forceRefreshValue else currentRefreshRate,
              forceRefreshRateEnabled = forceRefreshState,
              // Property-based states
              touchBoostEnabled = touchBoostState,
              playIntegrityFixEnabled = playIntegrityFixState,
              spoofBootloaderEnabled = spoofBootloaderState,
              gamePropsEnabled = gamePropsState,
              unlimitedPhotosEnabled = unlimitedPhotosState,
              netflixSpoofEnabled = netflixSpoofState,
              touchGameModeEnabled = touchGameModeState,
              touchActiveModeEnabled = touchActiveModeState,
              // UI-only states from preferences
              unlockNitsEnabled = unlockNitsState,
              dynamicRefreshRateEnabled = dynamicRefreshState,
              dcDimmingEnabled = dcDimmingState,
              overlayfsInstalled = overlayfsInstalled,
              fixDt2wModuleInstalled = fixDt2wModuleInstalled,
              fixDt2wEnabled = fixDt2wModuleInstalled,
              fixDt2wStep = fixDt2wStep,
              smartChargingEnabled = smartChargingState,
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
    viewModelScope.launch { preferencesManager.setFunctionalRomChargingLimit(enabled) }
    if (enabled) {
      applyChargingLimitValue(_uiState.value.chargingLimitValue)
    }
  }

  fun setChargingLimitValue(value: Int) {
    _uiState.update { it.copy(chargingLimitValue = value) }
    viewModelScope.launch { preferencesManager.setFunctionalRomChargingLimitValue(value) }
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
      preferencesManager.setFunctionalRomForceRefresh(enabled)
      if (enabled) {
        useCase.setForceRefreshRate(_uiState.value.forceRefreshRateValue)
      } else {
        useCase.resetRefreshRate()
      }
    }
  }

  fun setForceRefreshRateValue(hz: Int) {
    _uiState.update { it.copy(forceRefreshRateValue = hz) }
    viewModelScope.launch {
      preferencesManager.setFunctionalRomForceRefreshValue(hz)
      if (_uiState.value.forceRefreshRateEnabled) {
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

  // ==================== UI-only toggles (save to preferences) ====================

  fun setUnlockNits(enabled: Boolean) {
    // Update UI state immediately so toggle responds
    _uiState.update { it.copy(unlockNitsEnabled = enabled) }

    val nodePath = _uiState.value.maxBrightnessNodePath
    viewModelScope.launch {
      // Save preference
      preferencesManager.setFunctionalRomUnlockNits(enabled)

      // Try to apply to kernel if node is available
      if (nodePath != null) {
        val result = useCase.setUnlockNits(enabled, nodePath)
        if (result.isSuccess) {
          Log.d(TAG, "Unlock nits set to: $enabled (${if (enabled) "1000 nits" else "500 nits"})")
        } else {
          Log.e(TAG, "Failed to write unlock nits to kernel: ${result.exceptionOrNull()?.message}")
          // UI state already updated, user can continue
        }
      } else {
        Log.w(TAG, "Max brightness node not available, only saving preference")
      }
    }
  }

  fun setDynamicRefreshRate(enabled: Boolean) {
    _uiState.update { it.copy(dynamicRefreshRateEnabled = enabled) }
    viewModelScope.launch { preferencesManager.setFunctionalRomDynamicRefresh(enabled) }
  }

  fun setDcDimming(enabled: Boolean) {
    _uiState.update { it.copy(dcDimmingEnabled = enabled) }
    viewModelScope.launch { preferencesManager.setFunctionalRomDcDimming(enabled) }
  }

  // ==================== Fix DT2W 2-Step Installation ====================

  /** Show dialog when toggle is tapped */
  fun onFixDt2wToggle() {
    val step = _uiState.value.fixDt2wStep
    if (step == 0) {
      Log.d(TAG, "Fix DT2W already complete")
      return
    }
    // Show dialog for current step
    _uiState.update { it.copy(showFixDt2wDialog = true) }
  }

  /** Dismiss dialog */
  fun dismissFixDt2wDialog() {
    _uiState.update { it.copy(showFixDt2wDialog = false) }
  }

  /** Confirm and proceed with installation */
  fun confirmFixDt2wInstall() {
    _uiState.update { it.copy(showFixDt2wDialog = false, fixDt2wInstalling = true) }

    val currentStep = _uiState.value.fixDt2wStep

    viewModelScope.launch {
      try {
        val cacheDir = context.cacheDir

        if (currentStep == 1) {
          // Step 1: Install overlayfs
          Log.d(TAG, "Step 1: Installing meta-overlayfs...")

          val metaOverlayfsFile =
              java.io.File(
                  cacheDir,
                  FunctionalRomUseCase.Companion.ModuleConstants.META_OVERLAYFS_ASSET,
              )
          context.assets
              .open(FunctionalRomUseCase.Companion.ModuleConstants.META_OVERLAYFS_ASSET)
              .use { input ->
                metaOverlayfsFile.outputStream().use { output -> input.copyTo(output) }
              }

          val result = useCase.installOverlayfsModule(metaOverlayfsFile.absolutePath)
          metaOverlayfsFile.delete()

          if (result.isSuccess) {
            Log.d(TAG, "Overlayfs installed, rebooting...")
            _uiState.update {
              it.copy(fixDt2wInstalling = false, overlayfsInstalled = true, fixDt2wStep = 2)
            }
            useCase.rebootForModules()
          } else {
            Log.e(TAG, "Failed to install overlayfs")
            _uiState.update { it.copy(fixDt2wInstalling = false) }
          }
        } else if (currentStep == 2) {
          // Step 2: Install fix_dt2w
          Log.d(TAG, "Step 2: Installing fix_dt2w...")

          val fixDt2wFile =
              java.io.File(cacheDir, FunctionalRomUseCase.Companion.ModuleConstants.FIX_DT2W_ASSET)
          context.assets.open(FunctionalRomUseCase.Companion.ModuleConstants.FIX_DT2W_ASSET).use {
              input ->
            fixDt2wFile.outputStream().use { output -> input.copyTo(output) }
          }

          val result = useCase.installFixDt2wModule(fixDt2wFile.absolutePath)
          fixDt2wFile.delete()

          if (result.isSuccess) {
            Log.d(TAG, "Fix DT2W installed, rebooting...")
            _uiState.update {
              it.copy(
                  fixDt2wInstalling = false,
                  fixDt2wModuleInstalled = true,
                  fixDt2wEnabled = true,
                  fixDt2wStep = 0,
              )
            }
            useCase.rebootForModules()
          } else {
            Log.e(TAG, "Failed to install fix_dt2w")
            _uiState.update { it.copy(fixDt2wInstalling = false) }
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error during installation: ${e.message}", e)
        _uiState.update { it.copy(fixDt2wInstalling = false) }
      }
    }
  }

  // Wrapper function for toggle - handles both install and uninstall
  fun setFixDt2w(enabled: Boolean) {
    if (enabled) {
      // Installing - show install dialog
      onFixDt2wToggle()
    } else {
      // Uninstalling - show uninstall dialog if modules are installed
      if (_uiState.value.fixDt2wStep == 0) {
        _uiState.update { it.copy(showFixDt2wUninstallDialog = true) }
      }
    }
  }

  /** Dismiss uninstall dialog */
  fun dismissFixDt2wUninstallDialog() {
    _uiState.update { it.copy(showFixDt2wUninstallDialog = false) }
  }

  /** Confirm and proceed with module removal */
  fun confirmFixDt2wUninstall() {
    _uiState.update { it.copy(showFixDt2wUninstallDialog = false, fixDt2wInstalling = true) }

    viewModelScope.launch {
      try {
        Log.d(TAG, "Removing Fix DT2W modules...")

        val result = useCase.removeFixDt2wModules()

        if (result.isSuccess) {
          Log.d(TAG, "Modules removed, rebooting...")
          _uiState.update {
            it.copy(
                fixDt2wInstalling = false,
                fixDt2wEnabled = false,
                fixDt2wModuleInstalled = false,
                overlayfsInstalled = false,
                fixDt2wStep = 1,
            )
          }
          useCase.rebootForModules()
        } else {
          Log.e(TAG, "Failed to remove modules")
          _uiState.update { it.copy(fixDt2wInstalling = false) }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error during uninstall: ${e.message}", e)
        _uiState.update { it.copy(fixDt2wInstalling = false) }
      }
    }
  }

  fun setSmartCharging(enabled: Boolean) {
    _uiState.update { it.copy(smartChargingEnabled = enabled) }
    viewModelScope.launch { preferencesManager.setFunctionalRomSmartCharging(enabled) }
  }

  override fun onCleared() {
    super.onCleared()
    Log.d(TAG, "ViewModel cleared")
  }
}
