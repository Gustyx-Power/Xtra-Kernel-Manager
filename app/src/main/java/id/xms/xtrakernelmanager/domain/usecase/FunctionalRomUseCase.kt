package id.xms.xtrakernelmanager.domain.usecase

import android.util.Log
import id.xms.xtrakernelmanager.domain.root.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for Functional ROM features. Handles security validation, native kernel features, and
 * property-based features.
 */
class FunctionalRomUseCase {

  companion object {
    private const val TAG = "FunctionalRomUseCase"

    // VIP Community Property
    const val VIP_COMMUNITY_PROP = "persist.sys.oosexgang.vip.community"

    // Native Kernel Nodes
    object KernelNodes {
      const val BYPASS_CHARGING = "/sys/class/power_supply/battery/input_suspend"
      const val CHARGING_LIMIT = "/sys/class/power_supply/battery/charge_control_limit"

      // Alternative paths for different devices
      val BYPASS_CHARGING_PATHS =
          listOf(
              "/sys/class/power_supply/battery/input_suspend",
              "/sys/class/power_supply/battery/charging_enabled",
              "/sys/class/power_supply/usb/device/razer_charge_limit_enable",
          )

      val DT2W_PATHS =
          listOf(
              "/sys/touchpanel/double_tap",
              "/proc/touchpanel/double_tap",
              "/sys/class/touch/touch_dev/gesture_wakeup",
              "/sys/devices/virtual/touch/touch_dev/enable_dt2w",
              "/sys/android_touch/doubletap2wake",
              "/proc/tp_gesture",
              // Additional paths for more devices
              "/sys/class/sec/tsp/dt2w_enable",
              "/sys/devices/soc/78b7000.i2c/i2c-3/3-0020/input/input0/wake_gesture",
              "/sys/devices/soc/78b7000.i2c/i2c-3/3-0038/wake_gesture",
              "/sys/devices/platform/soc/1c2c000.i2c/i2c-3/3-0038/wake_gesture",
              "/sys/devices/platform/soc/*/i2c-*/3-0038/wake_gesture",
              "/proc/touchpanel/wake_gesture",
              "/sys/devices/virtual/touch/touch_dev/dt2w",
              "/sys/class/touchscreen/touchscreen/gesture/double_tap",
              "/sys/devices/soc/*/fts_ts/dt2w_enable",
              "/sys/bus/i2c/devices/*/wake_gesture",
              "/sys/devices/platform/soc/*/fts_ts/wake_gesture",
          )

      val CHARGING_LIMIT_PATHS =
          listOf(
              "/sys/class/power_supply/battery/charge_control_limit",
              "/sys/class/power_supply/battery/constant_charge_current_max",
              "/sys/class/qcom-battery/restricted_current",
          )
    }

    // Property-based Features (Shimoku's area)
    object Properties {
      const val TOUCH_BOOST = "persist.sys.oosexgang.tch.boost"
      // Play Integrity and Xiaomi-touch properties
      const val PLAY_INTEGRITY_FIX = "persist.sys.oosexgang.pi.fix"
      const val SPOOF_BOOTLOADER = "persist.sys.oosexgang.pi.bootloader"
      const val GAME_PROPS = "persist.sys.oosexgang.pi.gameprops"
      const val UNLIMITED_PHOTOS = "persist.sys.oosexgang.pi.photos"
      const val NETFLIX_SPOOF = "persist.sys.oosexgang.pi.netflix"
      // Xiaomi Touch
      const val TOUCH_GAME_MODE = "persist.sys.oosexgang.touch.gamemode"
      const val TOUCH_ACTIVE_MODE = "persist.sys.oosexgang.touch.active"
    }
  }

  // ==================== Security Gate ====================

  /** Check if device is registered in VIP community */
  suspend fun checkVipCommunity(): Boolean =
      withContext(Dispatchers.IO) {
        val prop = RootManager.getProp(VIP_COMMUNITY_PROP)
        val isVip = prop.equals("true", ignoreCase = true) || prop == "1"
        Log.d(TAG, "VIP Community check: prop='$prop', isVip=$isVip")
        isVip
      }

  // ==================== Node Availability Detection ====================

  /** Find available bypass charging node path */
  suspend fun findBypassChargingNode(): String? =
      withContext(Dispatchers.IO) {
        KernelNodes.BYPASS_CHARGING_PATHS.find { RootManager.fileExists(it) }
      }

  /** Find available DT2W node path */
  suspend fun findDt2wNode(): String? =
      withContext(Dispatchers.IO) {
        Log.d(TAG, "Searching for DT2W node...")
        for (path in KernelNodes.DT2W_PATHS) {
          // Skip wildcard paths as fileExists doesn't support them directly
          if (path.contains("*")) {
            Log.d(TAG, "Skipping wildcard path: $path")
            continue
          }
          val exists = RootManager.fileExists(path)
          Log.d(TAG, "DT2W path check: $path -> exists=$exists")
          if (exists) {
            Log.d(TAG, "DT2W node found: $path")
            return@withContext path
          }
        }
        Log.w(TAG, "No DT2W node found on this device")
        null
      }

  /** Find available charging limit node path */
  suspend fun findChargingLimitNode(): String? =
      withContext(Dispatchers.IO) {
        KernelNodes.CHARGING_LIMIT_PATHS.find { RootManager.fileExists(it) }
      }

  // ==================== Native Features (Kernel Nodes) ====================

  /**
   * Set bypass charging state
   *
   * @param enabled true to enable bypass (suspend input), false to disable
   * @param nodePath Path to bypass charging node
   */
  suspend fun setBypassCharging(enabled: Boolean, nodePath: String): Result<Unit> {
    val value = if (enabled) "1" else "0"
    Log.d(TAG, "Setting bypass charging: enabled=$enabled, path=$nodePath")
    return RootManager.writeToNode(nodePath, value)
  }

  /** Get current bypass charging state */
  suspend fun getBypassChargingState(nodePath: String): Boolean {
    val value = RootManager.readFromNode(nodePath)
    return value == "1"
  }

  /** Set DT2W (Double Tap to Wake) state */
  suspend fun setDoubleTapToWake(enabled: Boolean, nodePath: String): Result<Unit> {
    val value = if (enabled) "1" else "0"
    Log.d(TAG, "Setting DT2W: enabled=$enabled, path=$nodePath")
    return RootManager.writeToNode(nodePath, value)
  }

  /** Get current DT2W state */
  suspend fun getDoubleTapToWakeState(nodePath: String): Boolean {
    val value = RootManager.readFromNode(nodePath)
    return value == "1"
  }

  /** Set charging limit (current limit in mA or percentage depending on node) */
  suspend fun setChargingLimit(value: Int, nodePath: String): Result<Unit> {
    Log.d(TAG, "Setting charging limit: value=$value, path=$nodePath")
    return RootManager.writeToNode(nodePath, value.toString())
  }

  /** Get current charging limit value */
  suspend fun getChargingLimitValue(nodePath: String): Int {
    val value = RootManager.readFromNode(nodePath)
    return value.toIntOrNull() ?: 0
  }

  /**
   * Set force refresh rate using Android Settings API
   *
   * @param hz Refresh rate in Hz (e.g., 60, 90, 120)
   */
  suspend fun setForceRefreshRate(hz: Int): Result<Unit> =
      withContext(Dispatchers.IO) {
        Log.d(TAG, "Setting force refresh rate: $hz Hz")
        val result1 = RootManager.executeCommand("settings put system peak_refresh_rate $hz")
        val result2 = RootManager.executeCommand("settings put system min_refresh_rate $hz")

        if (result1.isSuccess && result2.isSuccess) {
          Result.success(Unit)
        } else {
          Result.failure(Exception("Failed to set refresh rate"))
        }
      }

  /** Get current refresh rate setting */
  suspend fun getCurrentRefreshRate(): Int =
      withContext(Dispatchers.IO) {
        val result = RootManager.executeCommand("settings get system peak_refresh_rate")
        result.getOrNull()?.trim()?.toFloatOrNull()?.toInt() ?: 60
      }

  /** Reset refresh rate to auto (clear forced values) */
  suspend fun resetRefreshRate(): Result<Unit> =
      withContext(Dispatchers.IO) {
        RootManager.executeCommand("settings delete system peak_refresh_rate")
        RootManager.executeCommand("settings delete system min_refresh_rate")
        Result.success(Unit)
      }

  // ==================== Property-based Features (Shimoku's Area) ====================

  /** Set touch boost state */
  suspend fun setTouchBoost(enabled: Boolean): Result<Unit> {
    Log.d(TAG, "Setting touch boost: $enabled")
    return RootManager.setProp(Properties.TOUCH_BOOST, if (enabled) "true" else "false")
  }

  /** Get touch boost state */
  suspend fun getTouchBoostState(): Boolean {
    val prop = RootManager.getProp(Properties.TOUCH_BOOST)
    return prop.equals("true", ignoreCase = true)
  }

  /** Set Play Integrity Fix state */
  suspend fun setPlayIntegrityFix(enabled: Boolean): Result<Unit> {
    return RootManager.setProp(Properties.PLAY_INTEGRITY_FIX, if (enabled) "true" else "false")
  }

  suspend fun getPlayIntegrityFixState(): Boolean {
    return RootManager.getProp(Properties.PLAY_INTEGRITY_FIX).equals("true", ignoreCase = true)
  }

  /** Set Spoof Bootloader state */
  suspend fun setSpoofBootloader(enabled: Boolean): Result<Unit> {
    return RootManager.setProp(Properties.SPOOF_BOOTLOADER, if (enabled) "true" else "false")
  }

  suspend fun getSpoofBootloaderState(): Boolean {
    return RootManager.getProp(Properties.SPOOF_BOOTLOADER).equals("true", ignoreCase = true)
  }

  /** Set Game Props state */
  suspend fun setGameProps(enabled: Boolean): Result<Unit> {
    return RootManager.setProp(Properties.GAME_PROPS, if (enabled) "true" else "false")
  }

  suspend fun getGamePropsState(): Boolean {
    return RootManager.getProp(Properties.GAME_PROPS).equals("true", ignoreCase = true)
  }

  /** Set Unlimited Google Photos Backup state */
  suspend fun setUnlimitedPhotos(enabled: Boolean): Result<Unit> {
    return RootManager.setProp(Properties.UNLIMITED_PHOTOS, if (enabled) "true" else "false")
  }

  suspend fun getUnlimitedPhotosState(): Boolean {
    return RootManager.getProp(Properties.UNLIMITED_PHOTOS).equals("true", ignoreCase = true)
  }

  /** Set Netflix Spoof state */
  suspend fun setNetflixSpoof(enabled: Boolean): Result<Unit> {
    return RootManager.setProp(Properties.NETFLIX_SPOOF, if (enabled) "true" else "false")
  }

  suspend fun getNetflixSpoofState(): Boolean {
    return RootManager.getProp(Properties.NETFLIX_SPOOF).equals("true", ignoreCase = true)
  }

  /** Set Touch Game Mode state (Xiaomi Touch) */
  suspend fun setTouchGameMode(enabled: Boolean): Result<Unit> {
    return RootManager.setProp(Properties.TOUCH_GAME_MODE, if (enabled) "true" else "false")
  }

  suspend fun getTouchGameModeState(): Boolean {
    return RootManager.getProp(Properties.TOUCH_GAME_MODE).equals("true", ignoreCase = true)
  }

  /** Set Touch Active Mode state (Xiaomi Touch) */
  suspend fun setTouchActiveMode(enabled: Boolean): Result<Unit> {
    return RootManager.setProp(Properties.TOUCH_ACTIVE_MODE, if (enabled) "true" else "false")
  }

  suspend fun getTouchActiveModeState(): Boolean {
    return RootManager.getProp(Properties.TOUCH_ACTIVE_MODE).equals("true", ignoreCase = true)
  }
}
