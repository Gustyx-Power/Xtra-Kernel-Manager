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
  
  suspend fun getMaxBrightness(): Int {
      return try {
          val commonPaths = listOf(
              "/sys/class/backlight/panel0-backlight/max_brightness",
              "/sys/class/backlight/panel1-backlight/max_brightness", 
              "/sys/class/leds/lcd-backlight/max_brightness",
              "/sys/class/backlight/backlight/max_brightness",
              "/sys/class/backlight/*/max_brightness"
          )
          
          for (path in commonPaths) {
              try {
                  val result = if (path.contains("*")) {
                      val findResult = RootManager.executeCommand("find /sys/class/backlight/ -name max_brightness 2>/dev/null | head -1")
                      val actualPath = findResult.getOrNull()?.trim()
                      if (!actualPath.isNullOrEmpty()) {
                          RootManager.readFile(actualPath)
                      } else null
                  } else {
                      RootManager.readFile(path)
                  }
                  
                  val maxVal = result?.getOrNull()?.trim()?.toIntOrNull()
                  if (maxVal != null && maxVal > 0) {
                      Log.d(TAG, "Found max brightness: $maxVal from path: $path")
                      return maxVal
                  }
              } catch (e: Exception) {
                  continue
              }
          }
          
          val currentResult = RootManager.executeCommand("settings get system screen_brightness")
          val current = currentResult.getOrNull()?.trim()?.toIntOrNull()
          
          val maxBrightness = when {
              current != null && current > 1000 -> 2047
              current != null && current > 255 -> 1023
              else -> 255
          }
          
          Log.d(TAG, "Using fallback max brightness: $maxBrightness (current: $current)")
          maxBrightness
          
      } catch (e: Exception) {
          Log.e(TAG, "Error getting max brightness, using default 255", e)
          255
      }
  }

  suspend fun setBrightness(value: Int): Result<Boolean> {
      return try {
          val clampedValue = value.coerceAtLeast(1)
          
          Log.d(TAG, "Setting brightness to: $clampedValue")
          
          val sysfsResult = setSysfsBrightness(clampedValue)
          if (sysfsResult.isSuccess) {
              Log.d(TAG, "Brightness set via sysfs successfully to: $clampedValue")
              return sysfsResult
          }
          
          Log.d(TAG, "Sysfs failed, trying Android settings method")
          val result = RootManager.executeCommand("settings put system screen_brightness $clampedValue")
          
          if (result.isSuccess) {
              RootManager.executeCommand("settings put system screen_brightness_mode 0")
              Log.d(TAG, "Brightness set via settings successfully to: $clampedValue")
              Result.success(true)
          } else {
              Log.e(TAG, "Failed to set brightness via both methods: ${result.exceptionOrNull()?.message}")
              Result.failure(result.exceptionOrNull() ?: Exception("Both sysfs and settings methods failed"))
          }
      } catch (e: Exception) {
          Log.e(TAG, "Error setting brightness", e)
          Result.failure(e)
      }
  }
  
  private suspend fun setSysfsBrightness(value: Int): Result<Boolean> {
      val commonPaths = listOf(
          "/sys/class/backlight/panel0-backlight/brightness",
          "/sys/class/backlight/panel1-backlight/brightness",
          "/sys/class/leds/lcd-backlight/brightness",
          "/sys/class/backlight/backlight/brightness"
      )
      
      for (path in commonPaths) {
          try {
              val checkResult = RootManager.executeCommand("test -f $path && echo exists || echo notfound")
              if (checkResult.getOrNull()?.trim() == "exists") {
                  val writeResult = RootManager.executeCommand("echo $value > $path")
                  if (writeResult.isSuccess) {
                      Log.d(TAG, "Successfully set brightness via sysfs: $path = $value")
                      return Result.success(true)
                  } else {
                      Log.w(TAG, "Failed to write to $path: ${writeResult.exceptionOrNull()?.message}")
                  }
              }
          } catch (e: Exception) {
              Log.w(TAG, "Error trying sysfs path $path: ${e.message}")
              continue
          }
      }
      
      try {
          val findResult = RootManager.executeCommand("find /sys/class/backlight/ -name brightness 2>/dev/null | head -1")
          val actualPath = findResult.getOrNull()?.trim()
          if (!actualPath.isNullOrEmpty()) {
              val writeResult = RootManager.executeCommand("echo $value > $actualPath")
              if (writeResult.isSuccess) {
                  Log.d(TAG, "Successfully set brightness via discovered sysfs: $actualPath = $value")
                  return Result.success(true)
              }
          }
      } catch (e: Exception) {
          Log.w(TAG, "Error in wildcard sysfs discovery: ${e.message}")
      }
      
      return Result.failure(Exception("No working sysfs brightness path found"))
  }

  suspend fun getBrightness(): Int {
      return try {
          val sysfsResult = getSysfsBrightness()
          if (sysfsResult != null) {
              Log.d(TAG, "Current brightness from sysfs: $sysfsResult")
              return sysfsResult
          }
          
          Log.d(TAG, "Sysfs failed, trying Android settings method")
          val result = RootManager.executeCommand("settings get system screen_brightness")
          val brightness = result.getOrNull()?.trim()?.toIntOrNull()
          
          if (brightness != null && brightness >= 0) {
              Log.d(TAG, "Current brightness from settings: $brightness")
              brightness
          } else {
              Log.w(TAG, "Could not get current brightness from either method, using default 128")
              128
          }
      } catch (e: Exception) {
          Log.e(TAG, "Error getting brightness", e)
          128
      }
  }
  
  private suspend fun getSysfsBrightness(): Int? {
      val commonPaths = listOf(
          "/sys/class/backlight/panel0-backlight/brightness",
          "/sys/class/backlight/panel1-backlight/brightness",
          "/sys/class/leds/lcd-backlight/brightness",
          "/sys/class/backlight/backlight/brightness"
      )
      
      for (path in commonPaths) {
          try {
              val result = RootManager.readFile(path)
              val brightness = result.getOrNull()?.trim()?.toIntOrNull()
              if (brightness != null && brightness >= 0) {
                  Log.d(TAG, "Found current brightness: $brightness from $path")
                  return brightness
              }
          } catch (e: Exception) {
              continue
          }
      }
      
      try {
          val findResult = RootManager.executeCommand("find /sys/class/backlight/ -name brightness 2>/dev/null | head -1")
          val actualPath = findResult.getOrNull()?.trim()
          if (!actualPath.isNullOrEmpty()) {
              val result = RootManager.readFile(actualPath)
              val brightness = result.getOrNull()?.trim()?.toIntOrNull()
              if (brightness != null && brightness >= 0) {
                  Log.d(TAG, "Found current brightness via discovery: $brightness from $actualPath")
                  return brightness
              }
          }
      } catch (e: Exception) {
          Log.w(TAG, "Error in wildcard brightness discovery: ${e.message}")
      }
      
      return null
  }
  
  suspend fun detectBrightnessControlMethod(): String? {
      val commonPaths = listOf(
          "/sys/class/backlight/panel0-backlight/brightness",
          "/sys/class/backlight/panel1-backlight/brightness", 
          "/sys/class/leds/lcd-backlight/brightness",
          "/sys/class/backlight/backlight/brightness"
      )
      
      for (path in commonPaths) {
          try {
              val readResult = RootManager.readFile(path)
              val currentValue = readResult.getOrNull()?.trim()?.toIntOrNull()
              
              if (currentValue != null && currentValue >= 0) {
                  val writeResult = RootManager.executeCommand("echo $currentValue > $path")
                  if (writeResult.isSuccess) {
                      Log.d(TAG, "Detected working brightness control: $path")
                      return path
                  }
              }
          } catch (e: Exception) {
              continue
          }
      }
      
      try {
          val findResult = RootManager.executeCommand("find /sys/class/backlight/ -name brightness 2>/dev/null | head -1")
          val actualPath = findResult.getOrNull()?.trim()
          if (!actualPath.isNullOrEmpty()) {
              val readResult = RootManager.readFile(actualPath)
              val currentValue = readResult.getOrNull()?.trim()?.toIntOrNull()
              
              if (currentValue != null && currentValue >= 0) {
                  val writeResult = RootManager.executeCommand("echo $currentValue > $actualPath")
                  if (writeResult.isSuccess) {
                      Log.d(TAG, "Detected working brightness control via discovery: $actualPath")
                      return actualPath
                  }
              }
          }
      } catch (e: Exception) {
          Log.w(TAG, "Error in brightness control detection: ${e.message}")
      }
      
      Log.d(TAG, "No sysfs brightness control detected, will use Android settings method")
      return null
  }

  suspend fun performGameBoost(): Result<String> {
      return try {
          Log.d(TAG, "Performing game boost - switching to performance mode with dynamic thermal")
          
          val perfResult = setPerformanceMode("performance")
          if (!perfResult.isSuccess) {
              Log.w(TAG, "Performance mode switch failed: ${perfResult.exceptionOrNull()?.message}")
          }
          
          val thermalUseCase = ThermalControlUseCase()
          try {
              thermalUseCase.setThermalMode("Dynamic", false)
              Log.d(TAG, "Dynamic thermal mode applied")
          } catch (e: Exception) {
              Log.e(TAG, "Failed to set dynamic thermal: ${e.message}")
          }
          
          val monsterResult = enableMonsterMode()
          if (!monsterResult.isSuccess) {
              Log.w(TAG, "Monster mode failed: ${monsterResult.exceptionOrNull()?.message}")
          }
          
          val ramResult = clearRAM()
          val freedMB = if (ramResult.isSuccess) {
              ramResult.getOrNull()?.freedMB ?: 0L
          } else {
              Log.w(TAG, "RAM clearing failed: ${ramResult.exceptionOrNull()?.message}")
              0L
          }
          
          val message = "Game Boost Activated!\nPerformance Mode + Dynamic Thermal\nFreed ${freedMB}MB RAM"
          
          Log.d(TAG, "Game boost completed successfully")
          Result.success(message)
          
      } catch (e: Exception) {
          Log.e(TAG, "Failed to perform game boost", e)
          Result.failure(e)
      }
  }

  suspend fun takeScreenshot(): Result<Boolean> {
      return try {
          Log.d(TAG, "Taking screenshot")
          
          val createDirResult = RootManager.executeCommand("mkdir -p /sdcard/Pictures/Screenshots")
          
          val timestamp = System.currentTimeMillis()
          val screenshotPath = "/sdcard/Pictures/Screenshots/XKM_screenshot_$timestamp.png"
          
          val result1 = RootManager.executeCommand("screencap -p $screenshotPath")
          if (result1.isSuccess) {
              kotlinx.coroutines.delay(100)
              
              val verifyResult = RootManager.executeCommand("test -f $screenshotPath && echo 'exists' || echo 'missing'")
              if (verifyResult.getOrNull()?.trim() == "exists") {
                  Log.d(TAG, "Screenshot saved to $screenshotPath")
                  RootManager.executeCommand("am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file://$screenshotPath")
                  return Result.success(true)
              }
          }
          
          val altPath = "/sdcard/DCIM/Screenshots/XKM_screenshot_$timestamp.png"
          RootManager.executeCommand("mkdir -p /sdcard/DCIM/Screenshots")
          val result2 = RootManager.executeCommand("screencap -p $altPath")
          if (result2.isSuccess) {
              kotlinx.coroutines.delay(100)
              val verifyResult = RootManager.executeCommand("test -f $altPath && echo 'exists' || echo 'missing'")
              if (verifyResult.getOrNull()?.trim() == "exists") {
                  Log.d(TAG, "Screenshot saved to $altPath")
                  RootManager.executeCommand("am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file://$altPath")
                  return Result.success(true)
              }
          }
          
          val result3 = RootManager.executeCommand("am start -a android.intent.action.MAIN -n com.android.systemui/.screenshot.ScreenshotServiceActivity")
          if (result3.isSuccess) {
              Log.d(TAG, "Screenshot triggered via system UI")
              return Result.success(true)
          }
          
          Log.w(TAG, "All safe screenshot methods failed")
          Result.failure(Exception("Screenshot methods failed - check root permissions"))
          
      } catch (e: Exception) {
          Log.e(TAG, "Failed to take screenshot", e)
          Result.failure(e)
      }
  }

  suspend fun rejectIncomingCall(): Result<Boolean> {
      return try {
          Log.d(TAG, "Rejecting incoming call")
          
          val result1 = RootManager.executeCommand("service call phone 5")
          if (result1.isSuccess) {
              Log.d(TAG, "Call rejected via telephony service")
              return Result.success(true)
          }
          
          val result2 = RootManager.executeCommand("service call phone 1 i32 0")
          if (result2.isSuccess) {
              Log.d(TAG, "Call rejected via alternative service")
              return Result.success(true)
          }
          
          val result3 = RootManager.executeCommand("input keyevent KEYCODE_HEADSETHOOK")
          if (result3.isSuccess) {
              Log.d(TAG, "Call rejected via headset hook")
              return Result.success(true)
          }
          
          val result4 = RootManager.executeCommand("input keyevent KEYCODE_ENDCALL")
          if (result4.isSuccess) {
              Log.d(TAG, "Call rejected via end call button")
              return Result.success(true)
          }
          
          Log.w(TAG, "All safe call rejection methods failed")
          Result.failure(Exception("Call rejection methods failed - check permissions"))
          
      } catch (e: Exception) {
          Log.e(TAG, "Failed to reject call", e)
          Result.failure(e)
      }
  }

  suspend fun answerIncomingCall(): Result<Boolean> {
      return try {
          Log.d(TAG, "Answering incoming call")
          
          val result1 = RootManager.executeCommand("service call phone 2")
          if (result1.isSuccess) {
              Log.d(TAG, "Call answered via telephony service")
              return Result.success(true)
          }
          
          val result2 = RootManager.executeCommand("input keyevent KEYCODE_HEADSETHOOK")
          if (result2.isSuccess) {
              Log.d(TAG, "Call answered via headset hook")
              return Result.success(true)
          }
          
          val result3 = RootManager.executeCommand("input keyevent KEYCODE_CALL")
          if (result3.isSuccess) {
              Log.d(TAG, "Call answered via call button")
              return Result.success(true)
          }
          
          val result4 = RootManager.executeCommand("service call phone 1 i32 1")
          if (result4.isSuccess) {
              Log.d(TAG, "Call answered via alternative service")
              return Result.success(true)
          }
          
          Log.w(TAG, "All safe call answer methods failed")
          Result.failure(Exception("Call answer methods failed - check permissions"))
          
      } catch (e: Exception) {
          Log.e(TAG, "Failed to answer call", e)
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
