package id.xms.xtrakernelmanager.ui.screens.misc.components

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.domain.usecase.GameControlUseCase
import id.xms.xtrakernelmanager.domain.usecase.GameOverlayUseCase
import id.xms.xtrakernelmanager.domain.root.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameMonitorViewModel(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

  private val gameOverlayUseCase = GameOverlayUseCase()
  private val gameControlUseCase = GameControlUseCase(context)

  // Performance States
  private val _cpuFreq = MutableStateFlow("0")
  val cpuFreq: StateFlow<String> = _cpuFreq.asStateFlow()

  private val _cpuLoad = MutableStateFlow(0f)
  val cpuLoad: StateFlow<Float> = _cpuLoad.asStateFlow()

  private val _gpuFreq = MutableStateFlow("0")
  val gpuFreq: StateFlow<String> = _gpuFreq.asStateFlow()

  private val _gpuLoad = MutableStateFlow(0f)
  val gpuLoad: StateFlow<Float> = _gpuLoad.asStateFlow()

  private val _fpsValue = MutableStateFlow("60")
  val fpsValue: StateFlow<String> = _fpsValue.asStateFlow()

  private val _isFpsEnabled = MutableStateFlow(false)
  val isFpsEnabled: StateFlow<Boolean> = _isFpsEnabled.asStateFlow()

  private val _tempValue = MutableStateFlow("0")
  val tempValue: StateFlow<String> = _tempValue.asStateFlow()

  private val _gameDuration = MutableStateFlow("0:00")
  val gameDuration: StateFlow<String> = _gameDuration.asStateFlow()

  private val _batteryPercentage = MutableStateFlow(100)
  val batteryPercentage: StateFlow<Int> = _batteryPercentage.asStateFlow()

  // Control States
  private val _currentPerformanceMode = MutableStateFlow("balanced")
  val currentPerformanceMode: StateFlow<String> = _currentPerformanceMode.asStateFlow()

  private val _esportsMode = MutableStateFlow(false)
  val esportsMode: StateFlow<Boolean> = _esportsMode.asStateFlow()

  private val _touchGuard = MutableStateFlow(false)
  val touchGuard: StateFlow<Boolean> = _touchGuard.asStateFlow()

  private val _blockNotifications = MutableStateFlow(false)
  val blockNotifications: StateFlow<Boolean> = _blockNotifications.asStateFlow()

  private val _doNotDisturb = MutableStateFlow(false)
  val doNotDisturb: StateFlow<Boolean> = _doNotDisturb.asStateFlow()

  private val _autoRejectCalls = MutableStateFlow(false)
  val autoRejectCalls: StateFlow<Boolean> = _autoRejectCalls.asStateFlow()

  private val _lockBrightness = MutableStateFlow(false)
  val lockBrightness: StateFlow<Boolean> = _lockBrightness.asStateFlow()

  private val _isClearingRam = MutableStateFlow(false)
  val isClearingRam: StateFlow<Boolean> = _isClearingRam.asStateFlow()

  // Additional StateFlow declarations
  private val _ringerMode = MutableStateFlow(0)
  val ringerMode: StateFlow<Int> = _ringerMode.asStateFlow()

  private val _callMode = MutableStateFlow(0)
  val callMode: StateFlow<Int> = _callMode.asStateFlow()

  private val _threeFingerSwipe = MutableStateFlow(false)
  val threeFingerSwipe: StateFlow<Boolean> = _threeFingerSwipe.asStateFlow()

  private val _brightness = MutableStateFlow(0.5f)
  val brightness: StateFlow<Float> = _brightness.asStateFlow()
  
  private var maxBrightnessValue = 255

  // Shared flows for events
  private val _screenshotTrigger = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(replay = 0)
  val screenshotTrigger = _screenshotTrigger.asSharedFlow()

  private val _esportsAnimationTrigger = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(replay = 0)
  val esportsAnimationTrigger = _esportsAnimationTrigger.asSharedFlow()

  private val _toastMessage = kotlinx.coroutines.flow.MutableSharedFlow<String>(replay = 0)
  val toastMessage = _toastMessage.asSharedFlow()

  private var pollingJob: Job? = null
  private var startTime: Long = 0L

  init {
    loadPreferences()
    startPolling()
  }

  private fun loadPreferences() {
    viewModelScope.launch {
      preferencesManager.isGameControlDNDEnabled().collect { _doNotDisturb.value = it }
    }
    viewModelScope.launch {
      preferencesManager.isGameControlHideNotifEnabled().collect { _blockNotifications.value = it }
    }
    viewModelScope.launch {
      preferencesManager.isEsportsModeEnabled().collect { _esportsMode.value = it }
    }
    viewModelScope.launch {
      preferencesManager.isTouchGuardEnabled().collect { _touchGuard.value = it }
    }
    viewModelScope.launch {
      preferencesManager.isAutoRejectCallsEnabled().collect { _autoRejectCalls.value = it }
    }
    viewModelScope.launch {
      preferencesManager.isLockBrightnessEnabled().collect { _lockBrightness.value = it }
    }
    viewModelScope.launch {
        preferencesManager.getRingerMode().collect { _ringerMode.value = it }
    }
    viewModelScope.launch {
        preferencesManager.getCallMode().collect { _callMode.value = it }
    }
    viewModelScope.launch {
        preferencesManager.isThreeFingerSwipeEnabled().collect { _threeFingerSwipe.value = it }
    }
    viewModelScope.launch {
        try {
            val brightnessMethod = withContext(Dispatchers.IO) {
                gameControlUseCase.detectBrightnessControlMethod()
            }
            
            if (brightnessMethod != null) {
                Log.d("GameMonitorViewModel", "Using sysfs brightness control: $brightnessMethod")
            } else {
                Log.d("GameMonitorViewModel", "Using Android settings brightness control")
            }
            
            maxBrightnessValue = withContext(Dispatchers.IO) { 
                gameControlUseCase.getMaxBrightness() 
            }
            
            Log.d("GameMonitorViewModel", "Max brightness detected: $maxBrightnessValue")
            
            val sysBrightness = withContext(Dispatchers.IO) { gameControlUseCase.getBrightness() }
            val normalizedBrightness = if (maxBrightnessValue > 0) {
                (sysBrightness.toFloat() / maxBrightnessValue).coerceIn(0f, 1f)
            } else {
                0.5f
            }
            
            Log.d("GameMonitorViewModel", "Current brightness: $sysBrightness/$maxBrightnessValue, normalized: $normalizedBrightness")
            _brightness.value = normalizedBrightness
        } catch (e: Exception) {
            Log.e("GameMonitorViewModel", "Error initializing brightness", e)
            _brightness.value = 0.5f
        }
    }
    viewModelScope.launch {
      _currentPerformanceMode.value = gameControlUseCase.getCurrentPerformanceMode()
    }
    viewModelScope.launch {
      _isFpsEnabled.value = preferencesManager.getBoolean("fps_enabled", false)
    }
  }

  private fun startPolling() {
    startTime = System.currentTimeMillis()
    pollingJob =
        viewModelScope.launch {
          while (true) {
            while (true) {
              val cycleStart = System.currentTimeMillis()

              withContext(Dispatchers.IO) {
                // 1. Fetch all data in parallel logic (fast native calls)
                val cpuFreq =
                    gameOverlayUseCase.getMaxCPUFreq().let { freq ->
                      if (freq >= 1000) "%.1f".format(freq / 1000f) else freq.toString()
                    }
                val cpuLoad = gameOverlayUseCase.getCPULoad()

                val gpuFreq = gameOverlayUseCase.getGPUFreq().toString()
                val gpuLoad = gameOverlayUseCase.getGPULoad()

                val fps = gameOverlayUseCase.getCurrentFPS().toString()
                val temp =
                    "%.0f"
                        .format(
                            gameOverlayUseCase.getTemperature()
                        ) // Round temp to whole number for cleaner UI

                // 2. Update states (Thread-safe)
                _cpuFreq.value = cpuFreq
                _cpuLoad.value = cpuLoad
                _gpuFreq.value = gpuFreq
                _gpuLoad.value = gpuLoad
                _fpsValue.value = fps
                _tempValue.value = temp
              }

              val batteryIntent =
                  context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
              val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
              val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
              val battPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
              _batteryPercentage.value = battPct

              // Duration Calculation
              val durationMs = System.currentTimeMillis() - startTime
              val minutes = (durationMs / 60000).toInt()
              val seconds = ((durationMs % 60000) / 1000).toInt()
              _gameDuration.value =
                  if (minutes >= 60) {
                    "${minutes / 60}h ${minutes % 60}m"
                  } else {
                    "$minutes:${"%02d".format(seconds)}"
                  }

              // 3. Smart Delay: Target 250ms loop time (4Hz refresh rate)
              val elapsed = System.currentTimeMillis() - cycleStart
              val wait = (1000 - elapsed).coerceAtLeast(10) 
              delay(wait)
            }
          }
        }
  }

  fun setPerformanceMode(mode: String) {
    viewModelScope.launch {
      val result = withContext(Dispatchers.IO) { gameControlUseCase.setPerformanceMode(mode) }
      if (result.isSuccess) {
        _currentPerformanceMode.value = mode
        preferencesManager.setPerfMode(mode)
      }
    }
  }

  fun setDND(enabled: Boolean) {
    if (!gameControlUseCase.hasDNDPermission()) {
      // In a real app we might expose an event to show UI toast/intent
      try {
        val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
      } catch (e: Exception) {
        // Ignore
      }
      return
    }

    viewModelScope.launch {
      val result =
          withContext(Dispatchers.IO) {
            if (enabled) gameControlUseCase.enableDND() else gameControlUseCase.disableDND()
          }
      if (result.isSuccess) {
        _doNotDisturb.value = enabled
        preferencesManager.setGameControlDND(enabled)
      }
    }
  }

  fun setBlockNotifications(enabled: Boolean) {
    viewModelScope.launch {
      _blockNotifications.value = enabled
      preferencesManager.setGameControlHideNotif(enabled)
      withContext(Dispatchers.IO) { gameControlUseCase.hideNotifications(enabled) }
    }
  }

  fun setEsportsMode(enabled: Boolean) {
    viewModelScope.launch {
      _esportsMode.value = enabled
      preferencesManager.setEsportsMode(enabled)

      if (enabled) {
        // Trigger esports animation
        _esportsAnimationTrigger.emit(Unit)
        withContext(Dispatchers.IO) { gameControlUseCase.enableMonsterMode() }
      } else {
        withContext(Dispatchers.IO) { gameControlUseCase.disableMonsterMode() }
      }
    }
  }

  fun setTouchGuard(enabled: Boolean) {
    viewModelScope.launch {
      _touchGuard.value = enabled
      preferencesManager.setTouchGuard(enabled)
      
      withContext(Dispatchers.IO) {
          gameControlUseCase.setGestureLock(enabled)
      }
    }
  }

  fun setAutoRejectCalls(enabled: Boolean) {
    viewModelScope.launch {
      _autoRejectCalls.value = enabled
      preferencesManager.setAutoRejectCalls(enabled)
    }
  }

  fun setLockBrightness(enabled: Boolean) {
    viewModelScope.launch {
      _lockBrightness.value = enabled
      preferencesManager.setLockBrightness(enabled)
    }
  }



  fun clearRAM() {
    if (_isClearingRam.value) return

    viewModelScope.launch {
      _isClearingRam.value = true
      withContext(Dispatchers.IO) { gameControlUseCase.clearRAM() }
      _isClearingRam.value = false
    }
  }

  fun setFpsEnabled(enabled: Boolean) {
    viewModelScope.launch {
      _isFpsEnabled.value = enabled
      preferencesManager.setBoolean("fps_enabled", enabled)
    }
  }

  fun cycleRingerMode() {
      val nextMode = (_ringerMode.value + 1) % 3
      viewModelScope.launch {
          _ringerMode.value = nextMode
          withContext(Dispatchers.IO) { gameControlUseCase.setRingerMode(nextMode) }
          preferencesManager.setRingerMode(nextMode)
      }
  }

  fun cycleCallMode() {
      val nextMode = (_callMode.value + 1) % 3
      viewModelScope.launch {
          _callMode.value = nextMode
          preferencesManager.setCallMode(nextMode)
          
          when (nextMode) {
              0 -> { // Default
                  setBlockNotifications(false)
                  setAutoRejectCalls(false)
              }
              1 -> { // No Heads Up
                  setBlockNotifications(true)
                  setAutoRejectCalls(false)
              }
              2 -> { // Reject
                  setBlockNotifications(true) 
                  setAutoRejectCalls(true)
              }
          }
      }
  }

  fun toggleThreeFingerSwipe() {
      val newState = !_threeFingerSwipe.value
      viewModelScope.launch {
          _threeFingerSwipe.value = newState
          withContext(Dispatchers.IO) { gameControlUseCase.setThreeFingerSwipe(newState) }
          preferencesManager.setThreeFingerSwipeEnabled(newState)
      }
  }

  fun setBrightness(value: Float) {
      val clampedValue = value.coerceIn(0f, 1f)
      _brightness.value = clampedValue
      
      viewModelScope.launch {
          try {
              val sysVal = if (maxBrightnessValue > 0) {
                  (clampedValue * maxBrightnessValue).toInt().coerceIn(1, maxBrightnessValue)
              } else {
                  (clampedValue * 255).toInt().coerceIn(1, 255)
              }
              
              Log.d("GameMonitorViewModel", "Setting brightness: slider=$clampedValue, system=$sysVal, max=$maxBrightnessValue")
              
              val result = withContext(Dispatchers.IO) { gameControlUseCase.setBrightness(sysVal) }
              if (!result.isSuccess) {
                  Log.e("GameMonitorViewModel", "Failed to set brightness: ${result.exceptionOrNull()?.message}")
              }
          } catch (e: Exception) {
              Log.e("GameMonitorViewModel", "Error setting brightness", e)
          }
      }
  }
  fun takeScreenshot() {
      viewModelScope.launch {
          _screenshotTrigger.emit(Unit)
      }
  }

  fun performGameBoost() {
      viewModelScope.launch {
          try {
              Log.d("GameMonitorViewModel", "Starting game boost with animation")
              
              _esportsAnimationTrigger.emit(Unit)
              
              delay(100)
              
              val result = withContext(Dispatchers.IO) { gameControlUseCase.performGameBoost() }
              if (result.isSuccess) {
                  val message = result.getOrNull() ?: "Game Boost Applied!\nPerformance optimized"
                  Log.d("GameMonitorViewModel", "Game boost successful: $message")
                  _toastMessage.emit(message)
              } else {
                  val errorMsg = "Game Boost Failed!\n${result.exceptionOrNull()?.message ?: "Unknown error"}"
                  Log.e("GameMonitorViewModel", errorMsg)
                  _toastMessage.emit(errorMsg)
              }
          } catch (e: Exception) {
              val errorMsg = "Game Boost Error!\n${e.message ?: "Unknown error"}"
              Log.e("GameMonitorViewModel", errorMsg, e)
              _toastMessage.emit(errorMsg)
          }
      }
  }

  fun rejectIncomingCall() {
      viewModelScope.launch {
          try {
              _toastMessage.emit("Rejecting call...")
              val result = withContext(Dispatchers.IO) { gameControlUseCase.rejectIncomingCall() }
              if (result.isSuccess) {
                  Log.d("GameMonitorViewModel", "Call rejected successfully")
                  _toastMessage.emit("Call Rejected!\nSuccessfully ended call")
              } else {
                  val errorMsg = "Failed to reject call!\n${result.exceptionOrNull()?.message ?: "Unknown error"}"
                  Log.e("GameMonitorViewModel", errorMsg)
                  _toastMessage.emit(errorMsg)
              }
          } catch (e: Exception) {
              val errorMsg = "Error rejecting call!\n${e.message ?: "Unknown error"}"
              Log.e("GameMonitorViewModel", errorMsg, e)
              _toastMessage.emit(errorMsg)
          }
      }
  }

  fun answerIncomingCall() {
      viewModelScope.launch {
          try {
              _toastMessage.emit("Answering call...")
              val result = withContext(Dispatchers.IO) { gameControlUseCase.answerIncomingCall() }
              if (result.isSuccess) {
                  Log.d("GameMonitorViewModel", "Call answered successfully")
                  _toastMessage.emit("Call Answered!\nSuccessfully connected")
              } else {
                  val errorMsg = "Failed to answer call!\n${result.exceptionOrNull()?.message ?: "Unknown error"}"
                  Log.e("GameMonitorViewModel", errorMsg)
                  _toastMessage.emit(errorMsg)
              }
          } catch (e: Exception) {
              val errorMsg = "Error answering call!\n${e.message ?: "Unknown error"}"
              Log.e("GameMonitorViewModel", errorMsg, e)
              _toastMessage.emit(errorMsg)
          }
      }
  }

  fun testCallFunctionality() {
      viewModelScope.launch {
          try {
              _toastMessage.emit("Testing call functionality...")
              
              val testCommands = listOf(
                  "service call phone 1",
                  "input keyevent --help",
                  "am broadcast --help"
              )
              
              var workingMethods = 0
              var availableServices = mutableListOf<String>()
              
              testCommands.forEachIndexed { index, command ->
                  try {
                      val result = withContext(Dispatchers.IO) { 
                          RootManager.executeCommand(command) 
                      }
                      if (result.isSuccess) {
                          workingMethods++
                          when (index) {
                              0 -> availableServices.add("Phone Service")
                              1 -> availableServices.add("Input Commands")
                              2 -> availableServices.add("Broadcast Commands")
                          }
                      }
                  } catch (e: Exception) {
                      Log.w("GameMonitorViewModel", "Test command failed: $command", e)
                  }
              }
              
              val message = when {
                  workingMethods >= 2 -> 
                      "Call functions ready!\nAvailable: ${availableServices.joinToString(", ")}\nRoot access: OK"
                  workingMethods >= 1 -> 
                      "Partial call support!\nAvailable: ${availableServices.joinToString(", ")}\nSome methods working"
                  else -> 
                      "Call functions unavailable!\nNo services accessible\nCheck root permissions"
              }
              
              _toastMessage.emit(message)
              
          } catch (e: Exception) {
              _toastMessage.emit("Call test error: ${e.message}")
          }
      }
  }

  override fun onCleared() {
    super.onCleared()
    pollingJob?.cancel()
  }
}
