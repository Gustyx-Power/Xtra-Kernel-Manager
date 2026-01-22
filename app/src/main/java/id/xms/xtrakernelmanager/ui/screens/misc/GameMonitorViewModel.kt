package id.xms.xtrakernelmanager.ui.screens.misc

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.domain.usecase.GameControlUseCase
import id.xms.xtrakernelmanager.domain.usecase.GameOverlayUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
              val wait = (250 - elapsed).coerceAtLeast(10) // Min 10ms wait to prevent CPU hogs
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

  override fun onCleared() {
    super.onCleared()
    pollingJob?.cancel()
  }
}
