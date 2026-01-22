package id.xms.xtrakernelmanager.domain.usecase

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import id.xms.xtrakernelmanager.domain.root.RootManager

class GameControlUseCase(private val context: Context) {

  companion object {
    private const val TAG = "GameControlUseCase"
  }

  
  fun hasDNDPermission(): Boolean {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      notificationManager.isNotificationPolicyAccessGranted
    } else {
      true
    }
  }

  suspend fun enableDND(): Result<Boolean> {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (notificationManager.isNotificationPolicyAccessGranted) {
          notificationManager.setInterruptionFilter(
              NotificationManager.INTERRUPTION_FILTER_PRIORITY
          )
          Log.d(TAG, "DND enabled successfully")
          Result.success(true)
        } else {
          Log.e(TAG, "DND permission not granted")
          Result.failure(
              DNDPermissionException("DND permission not granted. Please grant access in Settings.")
          )
        }
      } else {
        Result.failure(Exception("DND not supported on Android below Marshmallow"))
      }
    } catch (e: SecurityException) {
      Log.e(TAG, "Security exception enabling DND", e)
      Result.failure(DNDPermissionException("Security error: ${e.message}"))
    } catch (e: Exception) {
      Log.e(TAG, "Error enabling DND", e)
      Result.failure(e)
    }
  }

  suspend fun disableDND(): Result<Boolean> {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (notificationManager.isNotificationPolicyAccessGranted) {
          notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
          Log.d(TAG, "DND disabled successfully")
          Result.success(true)
        } else {
          Log.e(TAG, "DND permission not granted")
          Result.failure(DNDPermissionException("DND permission not granted"))
        }
      } else {
        Result.success(false)
      }
    } catch (e: SecurityException) {
      Log.e(TAG, "Security exception disabling DND", e)
      Result.failure(DNDPermissionException("Security error: ${e.message}"))
    } catch (e: Exception) {
      Log.e(TAG, "Error disabling DND", e)
      Result.failure(e)
    }
  }

  fun isDNDEnabled(): Boolean {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
    } else {
      false
    }
  }

  class DNDPermissionException(message: String) : Exception(message)

  suspend fun clearRAM(): Result<ClearRamResult> {
    return try {
      val memInfoBefore =
          RootManager.executeCommand("cat /proc/meminfo | grep MemAvailable").getOrNull()?.let {
            parseMemValue(it)
          } ?: 0L

      val commands = listOf("sync", "echo 3 > /proc/sys/vm/drop_caches", "am kill-all")

      var success = false
      for (cmd in commands) {
        val result = RootManager.executeCommand(cmd)
        if (result.isSuccess) success = true
      }

      kotlinx.coroutines.delay(500)

      val memInfoAfter =
          RootManager.executeCommand("cat /proc/meminfo | grep MemAvailable").getOrNull()?.let {
            parseMemValue(it)
          } ?: 0L

      // Calculate freed RAM in MB
      val freedMB = ((memInfoAfter - memInfoBefore) / 1024).coerceAtLeast(0)

      if (success) {
        Result.success(ClearRamResult(freedMB, memInfoAfter / 1024))
      } else {
        Result.failure(Exception("Failed to clear RAM"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun parseMemValue(line: String): Long {
    return try {
      line.split(Regex("\\s+")).getOrNull(1)?.toLongOrNull() ?: 0L
    } catch (e: Exception) {
      0L
    }
  }

  data class ClearRamResult(
      val freedMB: Long, 
      val availableMB: Long, 
  )


  suspend fun setPerformanceMode(mode: String): Result<Boolean> {
    val governorPath = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"

    val governor =
        when (mode) {
          "performance" -> "performance"
          "balanced" -> "schedutil"
          "battery" -> "powersave"
          else -> "schedutil"
        }

    val thermalPreset =
        when (mode) {
          "performance" -> "Dynamic"
          "balanced" -> "Thermal 20"
          "battery" -> "Incalls"
          else -> "Dynamic"
        }

    // Apply governor
    val governorResult = RootManager.executeCommand("echo $governor > $governorPath")

    // Apply thermal preset
    val thermalUseCase = ThermalControlUseCase()
    try {
      thermalUseCase.setThermalMode(thermalPreset, false)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to set thermal preset: ${e.message}")
    }

    return governorResult.map { true }
  }

  suspend fun getCurrentPerformanceMode(): String {
    val governorPath = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"
    val governor = RootManager.readFile(governorPath).getOrNull()?.trim() ?: "unknown"

    return when (governor) {
      "performance" -> "performance"
      "powersave" -> "battery"
      "schedutil",
      "interactive",
      "ondemand" -> "balanced"
      else -> "balanced"
    }
  }

  suspend fun enableMonsterMode(): Result<Boolean> {
    return try {
      val commands = mutableListOf<String>()

      for (i in 0..7) {
        val basePath = "/sys/devices/system/cpu/cpu$i/cpufreq"
        commands.add("cat $basePath/cpuinfo_max_freq > $basePath/scaling_max_freq 2>/dev/null")
        commands.add("cat $basePath/cpuinfo_max_freq > $basePath/scaling_min_freq 2>/dev/null")
        commands.add("echo performance > $basePath/scaling_governor 2>/dev/null")
      }

      commands.addAll(
          listOf(
              "echo 1 > /sys/class/kgsl/kgsl-3d0/force_clk_on 2>/dev/null",
              "cat /sys/class/kgsl/kgsl-3d0/max_gpuclk > /sys/class/kgsl/kgsl-3d0/gpuclk 2>/dev/null",
              "echo 0 > /sys/class/kgsl/kgsl-3d0/throttling 2>/dev/null",
              "echo 1 > /sys/class/kgsl/kgsl-3d0/force_bus_on 2>/dev/null",
              "echo 1 > /sys/class/kgsl/kgsl-3d0/force_rail_on 2>/dev/null",
          )
      )

      // Execute all commands
      for (cmd in commands) {
        RootManager.executeCommand(cmd)
      }

      // 3. Apply thermal dynamic via ThermalControlUseCase
      try {
        val thermalUseCase = ThermalControlUseCase()
        thermalUseCase.setThermalMode("Dynamic", false)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to set thermal: ${e.message}")
      }

      Log.d(TAG, "Monster Mode enabled")
      Result.success(true)
    } catch (e: Exception) {
      Log.e(TAG, "Error enabling Monster Mode", e)
      Result.failure(e)
    }
  }

  suspend fun disableMonsterMode(): Result<Boolean> {
    return try {
      for (i in 0..7) {
        val basePath = "/sys/devices/system/cpu/cpu$i/cpufreq"
        RootManager.executeCommand("echo schedutil > $basePath/scaling_governor 2>/dev/null")
      }

      RootManager.executeCommand("echo 0 > /sys/class/kgsl/kgsl-3d0/force_clk_on 2>/dev/null")
      RootManager.executeCommand("echo 0 > /sys/class/kgsl/kgsl-3d0/force_bus_on 2>/dev/null")
      RootManager.executeCommand("echo 0 > /sys/class/kgsl/kgsl-3d0/force_rail_on 2>/dev/null")

      Log.d(TAG, "Monster Mode disabled")
      Result.success(true)
    } catch (e: Exception) {
      Log.e(TAG, "Error disabling Monster Mode", e)
      Result.failure(e)
    }
  }

  /** Set Immersive Mode - Lock statusbar and navigation */
  suspend fun setImmersiveMode(enabled: Boolean): Result<Boolean> {
    return try {
      if (enabled) {
        RootManager.executeCommand("settings put global policy_control immersive.full=*")
        RootManager.executeCommand("settings put global force_immersive_on_apps *")
      } else {
        RootManager.executeCommand("settings put global policy_control null")
        RootManager.executeCommand("settings put global force_immersive_on_apps null")
      }
      Log.d(TAG, "Immersive mode: $enabled")
      Result.success(true)
    } catch (e: Exception) {
      Log.e(TAG, "Error setting immersive mode", e)
      Result.failure(e)
    }
  }

  /** Hide Notifications - Block notification popup */
  suspend fun hideNotifications(enabled: Boolean): Result<Boolean> {
    return try {
      val command =
          if (enabled) {
            // Disable heads-up and popup notifications
            "settings put global heads_up_notifications_enabled 0"
          } else {
            // Enable heads-up notifications
            "settings put global heads_up_notifications_enabled 1"
          }
      RootManager.executeCommand(command)
      Log.d(TAG, "Hide notifications: $enabled")
      Result.success(true)
    } catch (e: Exception) {
      Log.e(TAG, "Error hiding notifications", e)
      Result.failure(e)
    }
  }

  suspend fun startScreenRecord(): Result<Boolean> {
    return try {
      val miuiResult =
          RootManager.executeCommand(
              "am start -a android.intent.action.MAIN -n com.miui.screenrecorder/com.miui.screenrecorder.ui.ScreenRecorderActivity"
          )

      if (miuiResult.isFailure) {
        RootManager.executeCommand(
            "am broadcast -a com.miui.screenrecorder.action.START_SCREENRECORD"
        )
      }

      Log.d(TAG, "Screen record triggered")
      Result.success(true)
    } catch (e: Exception) {
      Log.e(TAG, "Error starting screen record", e)
      Result.failure(e)
    }
  }

  suspend fun setRingerMode(mode: Int): Result<Boolean> {
    return try {
        
        val am = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasDNDPermission()) {
             val ringerVal = when(mode) {
                 0 -> 2 // Normal
                 1 -> 1 // Vibrate
                 2 -> 0 // Silent
                 else -> 2
             }
             val cmdVal = when(mode) {
                 0 -> 2 // Normal -> 2
                 1 -> 1 // Vibrate -> 1
                 2 -> 0 // Silent -> 0
                 else -> 2
             }
             return RootManager.executeCommand("settings put global mode_ringer $cmdVal").map { true }
        }

        when (mode) {
            0 -> am.ringerMode = android.media.AudioManager.RINGER_MODE_NORMAL
            1 -> am.ringerMode = android.media.AudioManager.RINGER_MODE_VIBRATE
            2 -> am.ringerMode = android.media.AudioManager.RINGER_MODE_SILENT
        }
        Result.success(true)
    } catch (e: Exception) {
      Log.e(TAG, "Error setting ringer mode", e)
      try {
           val cmdVal = when(mode) {
                 0 -> 2 // Normal
                 1 -> 1 // Vibrate
                 2 -> 0 // Silent
                 else -> 2
           }
           RootManager.executeCommand("settings put global mode_ringer $cmdVal").map { true }
      } catch (ex: Exception) {
          Result.failure(ex)
      }
    }
  }


  suspend fun setThreeFingerSwipe(enabled: Boolean): Result<Boolean> {
      return try {
          val valStr = if (enabled) "1" else "0"
          RootManager.executeCommand("settings put system three_gesture_touch $valStr")
          
          Result.success(true)
      } catch (e: Exception) {
          Result.failure(e)
      }
  }
  
  suspend fun setBrightness(value: Int): Result<Boolean> {
      return try {
          RootManager.executeCommand("settings put system screen_brightness $value")
          Result.success(true)
      } catch (e: Exception) {
          Result.failure(e)
      }
  }

  suspend fun getBrightness(): Int {
      return try {
          val result = RootManager.executeCommand("settings get system screen_brightness")
          result.getOrNull()?.trim()?.toIntOrNull() ?: 128
      } catch (e: Exception) {
          128
      }
  }

  suspend fun takeScreenshot(): Result<Boolean> {
      return try {
          RootManager.executeCommand("input keyevent 120") 
          Result.success(true)
      } catch (e: Exception) {
          Result.failure(e)
      }
  }


  suspend fun setGestureLock(enabled: Boolean): Result<Boolean> {
      return try {
          val value = if (enabled) "1" else "0"
          
          RootManager.executeCommand("settings put system gamespace_lock_gesture $value")
          
          val edgeValue = if (enabled) "0" else "2"
          RootManager.executeCommand("settings put secure back_gesture_inset_scale_left $edgeValue")
          RootManager.executeCommand("settings put secure back_gesture_inset_scale_right $edgeValue")
          
          Log.d(TAG, "Gesture lock: $enabled")
          Result.success(true)
      } catch (e: Exception) {
          Log.e(TAG, "Error setting gesture lock", e)
          Result.failure(e)
      }
  }
}
