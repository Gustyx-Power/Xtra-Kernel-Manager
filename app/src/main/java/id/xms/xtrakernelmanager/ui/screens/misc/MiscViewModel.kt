package id.xms.xtrakernelmanager.ui.screens.misc

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.model.AppBatteryStats
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
import org.json.JSONArray
import org.json.JSONException

class MiscViewModel(
    val preferencesManager: PreferencesManager,
    private val context: Context,
) : ViewModel() {

  private val gameControlUseCase = GameControlUseCase(context)

  private val _isRootAvailable = MutableStateFlow(false)
  val isRootAvailable: StateFlow<Boolean> = _isRootAvailable.asStateFlow()

  private val _batteryInfo = MutableStateFlow(BatteryInfo())
  val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

  // Placeholder Stats for Power Insight
  private val _screenOnTime = MutableStateFlow("--h --m")
  val screenOnTime: StateFlow<String> = _screenOnTime.asStateFlow()

  private val _screenOffTime = MutableStateFlow("--h --m")
  val screenOffTime: StateFlow<String> = _screenOffTime.asStateFlow()

  private val _deepSleepTime = MutableStateFlow("--h --m")
  val deepSleepTime: StateFlow<String> = _deepSleepTime.asStateFlow()

  private val _drainRate = MutableStateFlow("0%/h")
  val drainRate: StateFlow<String> = _drainRate.asStateFlow()

  // Current Stats for Analytics
  private val _minCurrent = MutableStateFlow(0)
  val minCurrent: StateFlow<Int> = _minCurrent.asStateFlow()

  private val _maxCurrent = MutableStateFlow(0)
  val maxCurrent: StateFlow<Int> = _maxCurrent.asStateFlow()

  private val _avgCurrent = MutableStateFlow(0)
  val avgCurrent: StateFlow<Int> = _avgCurrent.asStateFlow()

  private var currentSamples = 0
  private var totalCurrent = 0L

  private fun updateCurrentStats(current: Int) {
    val absCurrent = kotlin.math.abs(current)
    if (absCurrent == 0) return

    if (_minCurrent.value == 0 || absCurrent < _minCurrent.value) {
      _minCurrent.value = absCurrent
    }
    if (absCurrent > _maxCurrent.value) {
      _maxCurrent.value = absCurrent
    }

    totalCurrent += absCurrent
    currentSamples++
    if (currentSamples > 0) {
      _avgCurrent.value = (totalCurrent / currentSamples).toInt()
    }
  }

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

  // Layout style (liquid = glassmorphic, material = pure M3)
  val layoutStyle =
      preferencesManager
          .getLayoutStyle()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "liquid")

  private val _saturationApplyStatus = MutableStateFlow("")
  val saturationApplyStatus: StateFlow<String> = _saturationApplyStatus.asStateFlow()

  // App Battery Usage
  private val _appBatteryUsage = MutableStateFlow<List<AppBatteryStats>>(emptyList())
  val appBatteryUsage: StateFlow<List<AppBatteryStats>> = _appBatteryUsage.asStateFlow()

  private val _isLoadingAppUsage = MutableStateFlow(false)
  val isLoadingAppUsage: StateFlow<Boolean> = _isLoadingAppUsage.asStateFlow()

  // SELinux State - REMOVED to prevent Play Protect detection
  // private val _selinuxStatus = MutableStateFlow("Unknown")
  // val selinuxStatus: StateFlow<String> = _selinuxStatus.asStateFlow()

  // private val _selinuxLoading = MutableStateFlow(false)
  // val selinuxLoading: StateFlow<Boolean> = _selinuxLoading.asStateFlow()

  // Game Space
  private val _callOverlay = MutableStateFlow(true)
  val callOverlay: StateFlow<Boolean> = _callOverlay.asStateFlow()

  private val _disableAutoBrightness = MutableStateFlow(true)
  val disableAutoBrightness: StateFlow<Boolean> = _disableAutoBrightness.asStateFlow()

  private val _disableThreeFingerSwipe = MutableStateFlow(false)
  val disableThreeFingerSwipe: StateFlow<Boolean> = _disableThreeFingerSwipe.asStateFlow()
  
  // Hide Accessibility (Banking Hidden Mode) - Xposed Module
  val hideAccessibilityEnabled =
      preferencesManager
          .getHideAccessibilityEnabled()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
  
  private val _lsposedModuleActive = MutableStateFlow(false)
  val lsposedModuleActive: StateFlow<Boolean> = _lsposedModuleActive.asStateFlow()

  init {
    checkRoot()
    loadCurrentPerformanceMode()
    checkLSPosedModuleStatus()
    // loadSELinuxStatus() - REMOVED to prevent Play Protect detection
  }

  private fun checkRoot() {
    viewModelScope.launch {
      _isRootAvailable.value = RootManager.isRootAvailable()
      Log.d("MiscViewModel", "Root available: ${_isRootAvailable.value}")
    }
  }
  
  /**
   * Check if the XKM LSPosed module is active
   * Uses YukiHookAPI status check or file-based detection
   */
  private fun checkLSPosedModuleStatus() {
    viewModelScope.launch {
      try {
        // Method 1: Check if YukiHookAPI is active
        val isActive = try {
          com.highcapable.yukihookapi.YukiHookAPI.Status.isXposedModuleActive
        } catch (e: Exception) {
          false
        }
        
        // Method 2: Alternative check via Xposed.isXposedException (fallback)
        val isActiveAlt = try {
          val xposedBridge = Class.forName("de.robv.android.xposed.XposedBridge")
          xposedBridge != null
        } catch (e: ClassNotFoundException) {
          false
        } catch (e: Exception) {
          false
        }
        
        _lsposedModuleActive.value = isActive || isActiveAlt
        Log.d("MiscViewModel", "LSPosed module active: ${_lsposedModuleActive.value}")
      } catch (e: Exception) {
        _lsposedModuleActive.value = false
        Log.e("MiscViewModel", "Failed to check LSPosed status: ${e.message}")
      }
    }
  }
  
  /**
   * Enable or disable the Hide Accessibility feature
   * Note: This only saves the preference. The actual hooking is done by LSPosed.
   */
  fun setHideAccessibility(enabled: Boolean) {
    viewModelScope.launch {
      try {
        preferencesManager.setHideAccessibilityEnabled(enabled)
        
        // Also save to sync preferences for Xposed module access
        preferencesManager.setString("hide_accessibility_enabled", enabled.toString())
        
        Log.d("MiscViewModel", "Hide Accessibility enabled: $enabled")
        
        if (enabled && !_lsposedModuleActive.value) {
          Log.w("MiscViewModel", "Warning: Hide Accessibility enabled but LSPosed module is not active")
        }
      } catch (e: Exception) {
        Log.e("MiscViewModel", "Error setting hide accessibility: ${e.message}")
      }
    }
  }
  
  /**
   * Refresh LSPosed module status
   */
  fun refreshLSPosedStatus() {
    checkLSPosedModuleStatus()
  }

  // SELinux Functions - COMPLETELY REMOVED to prevent Play Protect detection
  /*
  fun loadSELinuxStatus() {
    viewModelScope.launch {
      // Disable SELinux functionality for release builds
      if (!id.xms.xtrakernelmanager.BuildConfig.ENABLE_ROOT_FEATURES) {
        _selinuxStatus.value = "Disabled"
        Log.d("MiscViewModel", "SELinux functionality disabled for release build")
        return@launch
      }
      
      val result = RootManager.executeCommand("getenforce")
      _selinuxStatus.value = result.getOrNull()?.trim() ?: "Unknown"
      Log.d("MiscViewModel", "SELinux status: ${_selinuxStatus.value}")
    }
  }

  fun setSELinuxMode(enforcing: Boolean) {
    viewModelScope.launch {
      // Disable SELinux functionality for release builds
      if (!id.xms.xtrakernelmanager.BuildConfig.ENABLE_ROOT_FEATURES) {
        Log.d("MiscViewModel", "SELinux functionality disabled for release build")
        return@launch
      }
      
      if (!_isRootAvailable.value) {
        Log.e("MiscViewModel", "Cannot set SELinux: Root not available")
        return@launch
      }

      _selinuxLoading.value = true
      val mode = if (enforcing) "1" else "0"
      val result = RootManager.executeCommand("setenforce $mode")

      if (result.isSuccess) {
        _selinuxStatus.value = if (enforcing) "Enforcing" else "Permissive"
        Log.d("MiscViewModel", "SELinux set to: ${_selinuxStatus.value}")
      } else {
        Log.e("MiscViewModel", "Failed to set SELinux: ${result.exceptionOrNull()?.message}")
      }

      _selinuxLoading.value = false
    }
  }
  */

  fun loadBatteryInfo(context: Context) {
    viewModelScope.launch {
      _batteryInfo.value = BatteryRepository.getBatteryInfo(context)

      // Observe Realtime Stats
      launch {
        BatteryRepository.batteryState.collect { state ->
          // Format times
          _screenOnTime.value = formatTime(state.screenOnTime)
          _screenOffTime.value = formatTime(state.screenOffTime)
          _deepSleepTime.value = formatTime(state.deepSleepTime)
          _drainRate.value = "%.1f%%/h".format(state.activeDrainRate)

          // Sync Realtime State to BatteryInfo for Analytics UI
          _batteryInfo.value =
              _batteryInfo.value.copy(
                  level = state.level,
                  currentNow = state.currentNow,
                  voltage = state.voltage,
                  temperature = state.temp / 10f,
                  status =
                      when (state.status) {
                        android.os.BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                        android.os.BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                        android.os.BatteryManager.BATTERY_STATUS_FULL -> "Full"
                        android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING ->
                            if (state.plugged > 0) "Plugged" else "Not Charging"
                        else -> "Unknown"
                      },
              )

          // Update current stats
          updateCurrentStats(state.currentNow)
        }
      }

      // Load App Battery Usage
      loadAppBatteryUsage(context)
    }
  }

  private fun formatTime(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return if (hours > 0) {
      "%dh %02dm".format(hours, minutes % 60)
    } else {
      "%dm".format(minutes)
    }
  }

  fun loadAppBatteryUsage(context: Context) {
    viewModelScope.launch {
      _isLoadingAppUsage.value = true
      try {
        val stats =
            id.xms.xtrakernelmanager.data.repository.AppBatteryRepository.getAppBatteryUsage(
                context
            )
        _appBatteryUsage.value = stats
      } catch (e: Exception) {
        Log.e("MiscViewModel", "Failed to load app battery usage", e)
        _appBatteryUsage.value = emptyList()
      } finally {
        _isLoadingAppUsage.value = false
      }
    }
  }

  fun setShowBatteryNotif(enabled: Boolean) {
    viewModelScope.launch {
      preferencesManager.setShowBatteryNotif(enabled)
      Log.d("MiscViewModel", "Battery notification: $enabled")

      if (enabled) {
        try {
          val intent =
              Intent(context, id.xms.xtrakernelmanager.service.BatteryInfoService::class.java)
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
          } else {
            context.startService(intent)
          }
        } catch (e: Exception) {
          Log.e("MiscViewModel", "Failed to start BatteryInfoService: ${e.message}")
        }
      } else {
        try {
          // Explicitly cancel notification first for immediate feedback
          val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
          nm.cancel(id.xms.xtrakernelmanager.service.BatteryInfoService.NOTIF_ID)
          
          val intent =
              Intent(context, id.xms.xtrakernelmanager.service.BatteryInfoService::class.java)
          context.stopService(intent)
        } catch (e: Exception) {
          Log.e("MiscViewModel", "Failed to stop BatteryInfoService: ${e.message}")
        }
      }
    }
  }

  // Battery Notification Settings
  val batteryNotifIconType =
      preferencesManager
          .getBatteryNotifIconType()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "temp")

  fun setBatteryNotifIconType(type: String) {
    viewModelScope.launch {
      preferencesManager.setBatteryNotifIconType(type)
      Log.d("MiscViewModel", "Battery notification icon type: $type")
    }
  }

  val batteryNotifRefreshRate =
      preferencesManager
          .getBatteryNotifRefreshRate()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5000L)

  fun setBatteryNotifRefreshRate(ms: Long) {
    viewModelScope.launch {
      preferencesManager.setBatteryNotifRefreshRate(ms)
      Log.d("MiscViewModel", "Battery notification refresh rate set to: ${ms}ms")
    }
  }

  val batteryNotifSecureLockScreen =
      preferencesManager
          .getBatteryNotifSecureLockScreen()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  fun setBatteryNotifSecureLockScreen(enabled: Boolean) {
    viewModelScope.launch {
      preferencesManager.setBatteryNotifSecureLockScreen(enabled)
      Log.d("MiscViewModel", "Battery notification secure lock screen: $enabled")
    }
  }

  val batteryNotifHighPriority =
      preferencesManager
          .getBatteryNotifHighPriority()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  fun setBatteryNotifHighPriority(enabled: Boolean) {
    viewModelScope.launch {
      preferencesManager.setBatteryNotifHighPriority(enabled)
      Log.d("MiscViewModel", "Battery notification high priority: $enabled")
    }
  }

  val batteryNotifForceOnTop =
      preferencesManager
          .getBatteryNotifForceOnTop()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  fun setBatteryNotifForceOnTop(enabled: Boolean) {
    viewModelScope.launch {
      preferencesManager.setBatteryNotifForceOnTop(enabled)
      Log.d("MiscViewModel", "Battery notification force on top: $enabled")
    }
  }

  val batteryNotifDontUpdateScreenOff =
      preferencesManager
          .getBatteryNotifDontUpdateScreenOff()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

  fun setBatteryNotifDontUpdateScreenOff(enabled: Boolean) {
    viewModelScope.launch {
      preferencesManager.setBatteryNotifDontUpdateScreenOff(enabled)
      Log.d("MiscViewModel", "Battery notification don't update screen off: $enabled")
    }
  }

  // Battery Statistics Settings
  val batteryStatsActiveIdle =
      preferencesManager
          .getBatteryStatsActiveIdle()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

  fun setBatteryStatsActiveIdle(enabled: Boolean) {
    viewModelScope.launch {
      preferencesManager.setBatteryStatsActiveIdle(enabled)
      Log.d("MiscViewModel", "Battery stats active/idle: $enabled")
    }
  }

  val batteryStatsScreen =
      preferencesManager
          .getBatteryStatsScreen()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

  fun setBatteryStatsScreen(enabled: Boolean) {
    viewModelScope.launch {
      preferencesManager.setBatteryStatsScreen(enabled)
      Log.d("MiscViewModel", "Battery stats screen: $enabled")
    }
  }

  val batteryStatsAwakeSleep =
      preferencesManager
          .getBatteryStatsAwakeSleep()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

  fun setBatteryStatsAwakeSleep(enabled: Boolean) {
    viewModelScope.launch {
      preferencesManager.setBatteryStatsAwakeSleep(enabled)
      Log.d("MiscViewModel", "Battery stats awake/sleep: $enabled")
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

  fun setCallOverlay(enabled: Boolean) {
    _callOverlay.value = enabled
  }



  fun setDisableAutoBrightness(enabled: Boolean) {
    _disableAutoBrightness.value = enabled
  }

  fun setDisableThreeFingerSwipe(enabled: Boolean) {
    _disableThreeFingerSwipe.value = enabled
  }

  // New states for expandable lists
  private val _inGameCallAction = MutableStateFlow("no_action")
  val inGameCallAction: StateFlow<String> = _inGameCallAction.asStateFlow()

  private val _inGameRingerMode = MutableStateFlow("no_change")
  val inGameRingerMode: StateFlow<String> = _inGameRingerMode.asStateFlow()

  fun setInGameCallAction(action: String) {
    _inGameCallAction.value = action
  }

  fun setInGameRingerMode(mode: String) {
    _inGameRingerMode.value = mode
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

  fun addGameApp(packageName: String) {
    viewModelScope.launch {
      val currentApps =
          try {
            JSONArray(gameApps.value)
          } catch (e: JSONException) {
            JSONArray()
          }

      // Check if already exists
      var exists = false
      for (i in 0 until currentApps.length()) {
        val item = currentApps.opt(i)
        val existingPackage = when (item) {
          is String -> item
          is org.json.JSONObject -> item.optString("packageName")
          else -> null
        }
        if (existingPackage == packageName) {
          exists = true
          break
        }
      }

      if (!exists) {
        // Get app name from package manager
        val appName = try {
          val pm = context.packageManager
          val appInfo = pm.getApplicationInfo(packageName, 0)
          appInfo.loadLabel(pm).toString()
        } catch (e: Exception) {
          packageName
        }
        
        // Add as JSON object with structure matching LiquidGameControlScreen
        val gameObj = org.json.JSONObject().apply {
          put("packageName", packageName)
          put("appName", appName)
          put("enabled", true)
        }
        currentApps.put(gameObj)
        saveGameApps(currentApps.toString())
      }
    }
  }

  fun removeGameApp(packageName: String) {
    viewModelScope.launch {
      val currentApps =
          try {
            JSONArray(gameApps.value)
          } catch (e: JSONException) {
            JSONArray()
          }

      val newApps = JSONArray()
      for (i in 0 until currentApps.length()) {
        val item = currentApps.opt(i)
        val existingPackage = when (item) {
          is String -> item
          is org.json.JSONObject -> item.optString("packageName")
          else -> null
        }
        if (existingPackage != packageName) {
          newApps.put(currentApps.get(i))
        }
      }

      saveGameApps(newApps.toString())
    }
  }

  fun toggleGameApp(packageName: String) {
    if (isGameApp(packageName)) {
      removeGameApp(packageName)
    } else {
      addGameApp(packageName)
    }
  }

  fun isGameApp(packageName: String): Boolean {
    val currentApps =
        try {
          JSONArray(gameApps.value)
        } catch (e: JSONException) {
          JSONArray()
        }

    for (i in 0 until currentApps.length()) {
      val item = currentApps.opt(i)
      val existingPackage = when (item) {
        is String -> item
        is org.json.JSONObject -> item.optString("packageName")
        else -> null
      }
      if (existingPackage == packageName) {
        return true
      }
    }
    return false
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

  // App Picker State
  private val _installedApps =
      MutableStateFlow<List<id.xms.xtrakernelmanager.data.model.AppInfo>>(emptyList())
  val installedApps: StateFlow<List<id.xms.xtrakernelmanager.data.model.AppInfo>> =
      _installedApps.asStateFlow()

  private val _isLoadingApps = MutableStateFlow(false)
  val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

  fun loadInstalledApps() {
    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
      _isLoadingApps.value = true
      try {
        val pm = context.packageManager
        val apps =
            pm.getInstalledPackages(0)
                .filter {
                  val appInfo = it.applicationInfo
                  appInfo != null &&
                      (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM == 0 ||
                          (appInfo.flags and
                              android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)
                }
                .map { pkg ->
                  val appInfo = pkg.applicationInfo
                  id.xms.xtrakernelmanager.data.model.AppInfo(
                      packageName = pkg.packageName,
                      label = appInfo?.loadLabel(pm)?.toString() ?: pkg.packageName,
                      icon = appInfo?.loadIcon(pm),
                      isSelected = isGameApp(pkg.packageName),
                  )
                }
                .sortedBy { it.label.lowercase() }

        _installedApps.value = apps
      } catch (e: Exception) {
        Log.e("MiscViewModel", "Failed to load installed apps", e)
      } finally {
        _isLoadingApps.value = false
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    Log.d("MiscViewModel", "ViewModel cleared")
  }
}
