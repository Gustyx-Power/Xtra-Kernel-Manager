package id.xms.xtrakernelmanager.ui.screens.misc

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.data.repository.BatteryRepository
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.domain.usecase.GameControlUseCase
import id.xms.xtrakernelmanager.service.GameOverlayService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MiscViewModel(
    private val preferencesManager: PreferencesManager,
    private val context: Context,
) : ViewModel() {

  private val batteryRepository = BatteryRepository()
  private val gameControlUseCase = GameControlUseCase(context)

  private val _isRootAvailable = MutableStateFlow(false)
  val isRootAvailable: StateFlow<Boolean> = _isRootAvailable.asStateFlow()

  private val _batteryInfo = MutableStateFlow(BatteryInfo())
  val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

  private val _performanceMode = MutableStateFlow("balanced")
  val performanceMode: StateFlow<String> = _performanceMode.asStateFlow()

  private val _clearRAMStatus = MutableStateFlow("")
  val clearRAMStatus: StateFlow<String> = _clearRAMStatus.asStateFlow()

  val showBatteryNotif =
      preferencesManager
          .isShowBatteryNotif()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  val enableGameOverlay =
      preferencesManager
          .isEnableGameOverlay()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  val gameControlDND =
      preferencesManager
          .isGameControlDNDEnabled()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  val gameControlHideNotif =
      preferencesManager
          .isGameControlHideNotifEnabled()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  // Game apps list (apps that trigger game overlay)
  val gameApps =
      preferencesManager
          .getGameApps()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "[]")

  // Display saturation value (0.5 - 2.0)
  val displaySaturation =
      preferencesManager
          .getDisplaySaturation()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

  // Layout style (legacy = glassmorphic, material = pure M3)
  val layoutStyle =
      preferencesManager
          .getLayoutStyle()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "legacy")

  private val _saturationApplyStatus = MutableStateFlow("")
  val saturationApplyStatus: StateFlow<String> = _saturationApplyStatus.asStateFlow()

  init {
    checkRoot()
    loadCurrentPerformanceMode()
  }

  private fun checkRoot() {
    viewModelScope.launch {
      _isRootAvailable.value = RootManager.isRootAvailable()
      Log.d("MiscViewModel", "Root available: ${_isRootAvailable.value}")
    }
  }

  fun loadBatteryInfo(context: Context) {
    viewModelScope.launch { _batteryInfo.value = batteryRepository.getBatteryInfo(context) }
  }

  fun setShowBatteryNotification(enabled: Boolean) {
    viewModelScope.launch {
      preferencesManager.setShowBatteryNotif(enabled)
      Log.d("MiscViewModel", "Battery notification: $enabled")
    }
  }

  // State for overlay permission request
  private val _needsOverlayPermission = MutableStateFlow(false)
  val needsOverlayPermission: StateFlow<Boolean> = _needsOverlayPermission.asStateFlow()

  fun clearOverlayPermissionRequest() {
    _needsOverlayPermission.value = false
  }

  fun hasOverlayPermission(): Boolean {
    return Settings.canDrawOverlays(context)
  }

  fun setEnableGameOverlay(enabled: Boolean) {
    viewModelScope.launch {
      if (enabled && !Settings.canDrawOverlays(context)) {
        // Need permission first
        _needsOverlayPermission.value = true
        Log.d("MiscViewModel", "Game overlay needs permission")
        return@launch
      }

      preferencesManager.setEnableGameOverlay(enabled)
      Log.d("MiscViewModel", "Game overlay: $enabled")

      // Start/Stop GameOverlayService
      if (enabled) {
        startGameOverlayService()
      } else {
        stopGameOverlayService()
      }
    }
  }

  private fun startGameOverlayService() {
    try {
      // Double check permission before starting
      if (!Settings.canDrawOverlays(context)) {
        Log.e("MiscViewModel", "Cannot start GameOverlayService: No overlay permission")
        return
      }
      val intent = Intent(context, GameOverlayService::class.java)
      context.startService(intent)
      Log.d("MiscViewModel", "GameOverlayService started")
    } catch (e: Exception) {
      Log.e("MiscViewModel", "Failed to start GameOverlayService: ${e.message}")
    }
  }

  private fun stopGameOverlayService() {
    try {
      val intent = Intent(context, GameOverlayService::class.java)
      context.stopService(intent)
      Log.d("MiscViewModel", "GameOverlayService stopped")
    } catch (e: Exception) {
      Log.e("MiscViewModel", "Failed to stop GameOverlayService: ${e.message}")
    }
  }

  // Game Control Functions
  fun setPerformanceMode(mode: String) {
    viewModelScope.launch {
      val result = gameControlUseCase.setPerformanceMode(mode)
      if (result.isSuccess) {
        _performanceMode.value = mode
        preferencesManager.setPerfMode(mode)
        Log.d("MiscViewModel", "Performance mode set to: $mode")
      } else {
        Log.e(
            "MiscViewModel",
            "Failed to set performance mode: ${result.exceptionOrNull()?.message}",
        )
      }
    }
  }

  private fun loadCurrentPerformanceMode() {
    viewModelScope.launch {
      val mode = gameControlUseCase.getCurrentPerformanceMode()
      _performanceMode.value = mode
    }
  }

  fun setDND(enabled: Boolean) {
    viewModelScope.launch {
      val result =
          if (enabled) {
            gameControlUseCase.enableDND()
          } else {
            gameControlUseCase.disableDND()
          }

      if (result.isSuccess) {
        preferencesManager.setGameControlDND(enabled)
        Log.d("MiscViewModel", "DND ${if (enabled) "enabled" else "disabled"}")
      } else {
        Log.e("MiscViewModel", "Failed to set DND: ${result.exceptionOrNull()?.message}")
      }
    }
  }

  fun setHideNotifications(enabled: Boolean) {
    viewModelScope.launch {
      preferencesManager.setGameControlHideNotif(enabled)
      Log.d("MiscViewModel", "Hide notifications: $enabled")
    }
  }

  fun clearRAM() {
    viewModelScope.launch {
      _clearRAMStatus.value = "Clearing..."
      val result = gameControlUseCase.clearRAM()

      if (result.isSuccess) {
        _clearRAMStatus.value = "RAM Cleared!"
        Log.d("MiscViewModel", "RAM cleared successfully")

        // Refresh battery info after clearing RAM
        loadBatteryInfo(context)

        // Reset status after 3 seconds
        kotlinx.coroutines.delay(3000)
        _clearRAMStatus.value = ""
      } else {
        _clearRAMStatus.value = "Failed"
        Log.e("MiscViewModel", "Failed to clear RAM: ${result.exceptionOrNull()?.message}")

        kotlinx.coroutines.delay(3000)
        _clearRAMStatus.value = ""
      }
    }
  }

  // Game Apps Functions
  suspend fun saveGameApps(jsonString: String) {
    preferencesManager.saveGameApps(jsonString)
    Log.d("MiscViewModel", "Game apps saved: $jsonString")
  }

  // Display Saturation Functions
  fun setDisplaySaturation(value: Float) {
    viewModelScope.launch {
      if (!_isRootAvailable.value) {
        Log.e("MiscViewModel", "Cannot set saturation: Root not available")
        _saturationApplyStatus.value = "Root required"
        kotlinx.coroutines.delay(2000)
        _saturationApplyStatus.value = ""
        return@launch
      }

      val saturationValue = String.format(java.util.Locale.US, "%.2f", value)

      try {
        // Use service call SurfaceFlinger 1022 for immediate effect
        // 0.0 = grayscale, 1.0 = default, >1.0 = more saturated
        val surfaceFlingerResult =
            RootManager.executeCommand("service call SurfaceFlinger 1022 f $saturationValue")

        if (surfaceFlingerResult.isSuccess) {
          // Also set the system property for persistence across reboots
          RootManager.executeCommand("setprop persist.sys.sf.color_saturation $saturationValue")

          preferencesManager.setDisplaySaturation(value)
          _saturationApplyStatus.value = "Applied: $saturationValue"
          Log.d("MiscViewModel", "Display saturation set to: $saturationValue (SurfaceFlinger)")
        } else {
          _saturationApplyStatus.value = "Failed"
          Log.e(
              "MiscViewModel",
              "Failed to set saturation: ${surfaceFlingerResult.exceptionOrNull()?.message}",
          )
        }
      } catch (e: Exception) {
        _saturationApplyStatus.value = "Failed"
        Log.e("MiscViewModel", "Exception setting saturation: ${e.message}")
      }

      kotlinx.coroutines.delay(2000)
      _saturationApplyStatus.value = ""
    }
  }

  // Layout Style Function
  fun setLayoutStyle(style: String) {
    viewModelScope.launch {
      preferencesManager.setLayoutStyle(style)
      Log.d("MiscViewModel", "Layout style set to: $style")
    }
  }

  override fun onCleared() {
    super.onCleared()
    Log.d("MiscViewModel", "ViewModel cleared")
  }
}
