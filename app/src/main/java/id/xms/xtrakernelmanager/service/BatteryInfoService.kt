package id.xms.xtrakernelmanager.service

import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.os.*
import android.os.BatteryManager
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BatteryInfoService : Service() {
  companion object {
    const val NOTIF_ID = 1001
    const val CHANNEL_ID = "battery_info"
  }
  
  private var receiver: BroadcastReceiver? = null
  private var screenReceiver: BroadcastReceiver? = null
  private val serviceJob = kotlinx.coroutines.SupervisorJob()
  private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

  // Tracking variables
  private var screenOnTime: Long = 0
  private var screenOffTime: Long = 0
  private var lastScreenOnTimestamp: Long = 0
  private var lastScreenOffTimestamp: Long = 0
  private var serviceStartTime: Long = 0
  private var lastBatteryLevel: Int = -1
  private var lastUpdateTime: Long = 0
  private var batteryDrainWhileScreenOn: Int = 0
  private var batteryDrainWhileScreenOff: Int = 0
  private var isScreenOn: Boolean = true

  // Cached values for notification (to prevent showing 0 on service restart)
  private var cachedLevel: Int = -1
  private var cachedIsCharging: Boolean = false
  private var cachedTemp: Int = 0
  private var cachedVoltage: Int = 0
  private var cachedHealth: String = "Unknown"
  private var cachedCurrent: Int = 0
  private var cachedStatus: Int = -1
  private var cachedPlugged: Int = 0
  private var cachedStatusPath: String? = null
  private var currentIconType: String = "battery_icon"
  private var refreshRateMs: Long = 1000 // Default 1s
  private var isSecureLockScreen: Boolean = false
  private var isHighPriority: Boolean = false
  private var isForceOnTop: Boolean = false
  private var dontUpdateScreenOff: Boolean = true // Default true
  
  // Battery Statistics Settings
  private var showActiveIdle: Boolean = true
  private var showScreen: Boolean = true
  private var showAwakeSleep: Boolean = true
  private var smoothedCurrent: Double = 0.0
  private var isCurrentInitialized: Boolean = false
  private fun getEmaAlpha(): Double = when {
    refreshRateMs <= 500 -> 0.05   // 0.5s refresh: very aggressive smoothing
    refreshRateMs <= 1000 -> 0.08  // 1s refresh: aggressive smoothing
    refreshRateMs <= 2000 -> 0.15  // 2s refresh: moderate smoothing
    else -> 0.25                   // 5s+ refresh: light smoothing
  }

  // Deep sleep tracking
  private var initialDeepSleep: Long = 0L
  private var initialElapsedRealtime: Long = 0L

  // Track if we've reset stats at 100% (to prevent repeated resets)
  private var hasResetAtFullCharge: Boolean = false

  override fun onCreate() {
    super.onCreate()
    serviceStartTime = SystemClock.elapsedRealtime()
    lastScreenOnTimestamp = serviceStartTime
    lastUpdateTime = System.currentTimeMillis()

    // Check initial screen state
    val powerManager = getSystemService(POWER_SERVICE) as PowerManager
    isScreenOn = powerManager.isInteractive

    // Initialize deep sleep baseline
    initialElapsedRealtime = SystemClock.elapsedRealtime()
    initialDeepSleep = getSystemDeepSleepTime()

    createNotificationChannel()

    // Initialize History Repository
    id.xms.xtrakernelmanager.data.repository.HistoryRepository.init(applicationContext)

    registerReceiver()
    registerScreenReceiver()

    // Observe Preference Changes
    val prefs = id.xms.xtrakernelmanager.data.preferences.PreferencesManager(applicationContext)
    // Use serviceScope instead of local scope
    val scope = serviceScope

    // Master Toggle
    scope.launch {
      prefs.isShowBatteryNotif().collect { enabled ->
        if (!enabled) {
          stopForeground(STOP_FOREGROUND_REMOVE)
          stopSelf()
        }
      }
    }

    // Icon Type
    scope.launch {
      prefs.getBatteryNotifIconType().collect { type ->
        currentIconType = type
        if (cachedLevel != -1) refreshState()
      }
    }

    // Refresh Rate
    scope.launch { prefs.getBatteryNotifRefreshRate().collect { ms -> refreshRateMs = ms } }

    // Secure Lock Screen
    scope.launch {
      prefs.getBatteryNotifSecureLockScreen().collect { enabled ->
        isSecureLockScreen = enabled
        if (cachedLevel != -1) refreshState()
      }
    }

    // High Priority
    scope.launch {
      prefs.getBatteryNotifHighPriority().collect { enabled ->
        isHighPriority = enabled
        createNotificationChannel() // Recreate channel with new importance
        if (cachedLevel != -1) refreshState()
      }
    }

    // Force On Top
    scope.launch {
      prefs.getBatteryNotifForceOnTop().collect { enabled ->
        isForceOnTop = enabled
        if (cachedLevel != -1) refreshState()
      }
    }

    // Screen Off Updates
    scope.launch {
      prefs.getBatteryNotifDontUpdateScreenOff().collect { enabled ->
        dontUpdateScreenOff = enabled
      }
    }
    
    // Battery Statistics Settings
    scope.launch {
      prefs.getBatteryStatsActiveIdle().collect { enabled ->
        showActiveIdle = enabled
        if (cachedLevel != -1) refreshState()
      }
    }
    
    scope.launch {
      prefs.getBatteryStatsScreen().collect { enabled ->
        showScreen = enabled
        if (cachedLevel != -1) refreshState()
      }
    }
    
    scope.launch {
      prefs.getBatteryStatsAwakeSleep().collect { enabled ->
        showAwakeSleep = enabled
        if (cachedLevel != -1) refreshState()
      }
    }

    // Start Native Polling for Real-time Battery Data
    startNativePolling(scope)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.d("BatteryInfoService", "Service started with flags: $flags, startId: $startId")

    // Use cached values if available, otherwise use defaults
    // This prevents showing 0 values when service is restarted
    val notif =
        if (cachedLevel >= 0) {
          Log.d("BatteryInfoService", "Using cached battery values: level=$cachedLevel")
          buildNotification(
              cachedLevel,
              cachedIsCharging,
              cachedTemp,
              cachedVoltage,
              cachedHealth,
              cachedCurrent,
          )
        } else {
          Log.d("BatteryInfoService", "No cached values, using defaults")
          buildNotification(0, false, 0, 0, "Unknown", 0)
        }
    // SDK 34+ requires foreground service type - use SPECIAL_USE for long-running monitoring
    // services
    // DATA_SYNC has a 6-hour timeout on Android 14+, SPECIAL_USE has no timeout
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      startForeground(NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
    } else {
      startForeground(NOTIF_ID, notif)
    }

    // Safety: Verify we should actually be running
    CoroutineScope(Dispatchers.IO).launch {
      val prefs = id.xms.xtrakernelmanager.data.preferences.PreferencesManager(applicationContext)
      try {
        if (!prefs.isShowBatteryNotif().first()) {
           Log.w("BatteryInfoService", "Service started but pref is disabled! Stopping.")
           stopForeground(STOP_FOREGROUND_REMOVE)
           stopSelf()
        }
      } catch (e: Exception) {
        Log.e("BatteryInfoService", "Failed to verify startup state: ${e.message}")
      }
    }

    return START_STICKY
  }

  /** Called when the app is removed from recents. We restart the service to keep it running. */
  override fun onTaskRemoved(rootIntent: Intent?) {
    Log.d("BatteryInfoService", "Task removed, restarting service...")

    // Check if notification is still enabled before restarting
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val preferencesManager = PreferencesManager(applicationContext)
        val isEnabled = preferencesManager.isShowBatteryNotif().first()

        if (isEnabled) {
          val restartIntent = Intent(applicationContext, BatteryInfoService::class.java)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(restartIntent)
          } else {
            applicationContext.startService(restartIntent)
          }
          Log.d("BatteryInfoService", "Service restart scheduled")
        } else {
          Log.d("BatteryInfoService", "Service disabled, not restarting")
        }
      } catch (e: Exception) {
        Log.e("BatteryInfoService", "Failed to restart service: ${e.message}")
      }
    }

    super.onTaskRemoved(rootIntent)
  }

  /**
   * Safely update the foreground notification with SDK 36 compatibility. Uses
   * NotificationManager.notify() for updates since startForeground() is only needed once.
   */
  private fun updateNotificationSafe(notification: Notification) {
    try {
      val notificationManager = getSystemService(NotificationManager::class.java)
      notificationManager.notify(NOTIF_ID, notification)
    } catch (e: Exception) {
      Log.e("BatteryInfoService", "Failed to update notification: ${e.message}")
    }
  }

  private fun registerReceiver() {
    receiver =
        object : BroadcastReceiver() {
          override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
            val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            val isCharging =
                (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL)

            // Read current_now from multiple sources with fallback
            val currentNow = getBatteryCurrent(isCharging)

            // Reset battery stats when battery reaches 100%
            if (level >= 100 && isCharging && !hasResetAtFullCharge) {
              resetBatteryStats()
              hasResetAtFullCharge = true
              Log.d("BatteryInfoService", "Battery stats reset at 100%")

              // Show toast notification
              android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(
                        applicationContext,
                        "Battery stats reset at 100%",
                        android.widget.Toast.LENGTH_SHORT,
                    )
                    .show()
              }
            }

            // Reset flag when battery level drops below 100 (new discharge cycle)
            if (level < 100 && hasResetAtFullCharge) {
              hasResetAtFullCharge = false
            }

            // Track battery drain
            if (lastBatteryLevel != -1 && level < lastBatteryLevel && !isCharging) {
              val drain = lastBatteryLevel - level

              // Feed to History
              id.xms.xtrakernelmanager.data.repository.HistoryRepository.addDrain(drain)

              if (isScreenOn) {
                batteryDrainWhileScreenOn += drain
              } else {
                batteryDrainWhileScreenOff += drain
              }
            }
            lastBatteryLevel = level

            val healthTxt =
                when (health) {
                  BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                  BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                  BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                  BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
                  BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
                  else -> "Unknown"
                }

            // Cache values for potential service restart
            cachedLevel = level
            cachedIsCharging = isCharging
            cachedTemp = temp
            cachedVoltage = voltage
            cachedHealth = healthTxt
            cachedCurrent = currentNow
            cachedStatus = status
            cachedPlugged = plugged

            // Throttling Logic
            val now = SystemClock.elapsedRealtime()
            if (now - lastUpdateTime < refreshRateMs) {
              // Skip update if within throttle window, unless urgent (e.g. plugged status changed)
              if (isCharging == cachedIsCharging) {
                return
              }
            }
            lastUpdateTime = now

            // Screen Off Logic
            if (!isScreenOn && dontUpdateScreenOff) {
              // Still update internal cache but skip notification?
              // Actually we want to record history, just not spam notification
              // Let refreshState handle notification suppression
            }

            refreshState()
          }
        }
    registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
  }

  private fun registerScreenReceiver() {
    screenReceiver =
        object : BroadcastReceiver() {
          override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
              Intent.ACTION_SCREEN_ON -> {
                val now = SystemClock.elapsedRealtime()
                if (!isScreenOn) {
                  screenOffTime += now - lastScreenOffTimestamp
                }
                lastScreenOnTimestamp = now
                isScreenOn = true

                // Start history tracking session
                lastHistoryUpdateParams = now

                refreshState()
              }
              Intent.ACTION_SCREEN_OFF -> {
                val now = SystemClock.elapsedRealtime()
                if (isScreenOn) {
                  screenOnTime += now - lastScreenOnTimestamp

                  // Finalize history tracking for this session
                  updateHistoryValues(now)
                }
                lastScreenOffTimestamp = now
                isScreenOn = false
                refreshState()
              }
            }
          }
        }
    val filter =
        IntentFilter().apply {
          addAction(Intent.ACTION_SCREEN_ON)
          addAction(Intent.ACTION_SCREEN_OFF)
        }
    registerReceiver(screenReceiver, filter)
  }

  private fun refreshState() {
    if (cachedLevel == -1) return

    val currentTime = SystemClock.elapsedRealtime()
    val totalScreenOn =
        if (isScreenOn) {
          screenOnTime + (currentTime - lastScreenOnTimestamp)
        } else {
          screenOnTime
        }
    val totalScreenOff =
        if (!isScreenOn) {
          screenOffTime + (currentTime - lastScreenOffTimestamp)
        } else {
          screenOffTime
        }

    val deepSleepTime = getDeepSleepTime()

    val activeDrainRate =
        if (totalScreenOn > 0) {
          (batteryDrainWhileScreenOn.toFloat() / (totalScreenOn / 3600000f))
        } else 0f

    val idleDrainRate =
        if (totalScreenOff > 0) {
          (batteryDrainWhileScreenOff.toFloat() / (totalScreenOff / 3600000f))
        } else 0f

    // Update Repository
    val newState =
        id.xms.xtrakernelmanager.data.repository.BatteryRealtimeState(
            level = cachedLevel,
            isCharging = cachedIsCharging,
            temp = cachedTemp,
            voltage = cachedVoltage,
            currentNow = cachedCurrent,
            health = cachedHealth,
            screenOnTime = totalScreenOn,
            screenOffTime = totalScreenOff,
            deepSleepTime = deepSleepTime,
            activeDrainRate = activeDrainRate,
            idleDrainRate = idleDrainRate,
            totalCapacity =
                id.xms.xtrakernelmanager.data.repository.BatteryRepository.getCachedTotalCapacity(),
            currentCapacity =
                id.xms.xtrakernelmanager.data.repository.BatteryRepository
                    .getCachedCurrentCapacity(),
            status = cachedStatus,
            plugged = cachedPlugged,
        )
    id.xms.xtrakernelmanager.data.repository.BatteryRepository.updateState(newState)

    // Record Current Flow Sample for Analytics Chart
    CoroutineScope(Dispatchers.IO).launch {
      id.xms.xtrakernelmanager.data.repository.CurrentFlowRepository.addSample(
          applicationContext,
          cachedCurrent,
          cachedIsCharging,
      )
    }

    // Update Notification
    // Check Screen Off preference
    if (isScreenOn || !dontUpdateScreenOff) {
      val notif =
          buildNotification(
              cachedLevel,
              cachedIsCharging,
              cachedTemp,
              cachedVoltage,
              cachedHealth,
              cachedCurrent,
          )
      updateNotificationSafe(notif)
    }

    // Update history if screen is on
    if (isScreenOn) {
      updateHistoryValues(currentTime)
    }
  }

  // Track last history update time to calculate deltas
  private var lastHistoryUpdateParams: Long = 0L

  private fun updateHistoryValues(now: Long) {
    if (lastHistoryUpdateParams == 0L) {
      lastHistoryUpdateParams = now
      return
    }

    val delta = now - lastHistoryUpdateParams
    if (delta > 0) {
      id.xms.xtrakernelmanager.data.repository.HistoryRepository.incrementScreenOn(delta)
      lastHistoryUpdateParams = now
    }
  }

  private fun buildNotification(
      level: Int,
      charging: Boolean,
      temp: Int,
      voltage: Int,
      health: String,
      currentNow: Int,
  ): Notification {
    // Calculate current times
    val currentTime = SystemClock.elapsedRealtime()
    val totalScreenOn =
        if (isScreenOn) {
          screenOnTime + (currentTime - lastScreenOnTimestamp)
        } else {
          screenOnTime
        }
    val totalScreenOff =
        if (!isScreenOn) {
          screenOffTime + (currentTime - lastScreenOffTimestamp)
        } else {
          screenOffTime
        }

    // Get deep sleep time from system
    val deepSleepTime = getDeepSleepTime()
    
    // Calculate awake time
    // Awake time = Screen On + Screen Off (when CPU is awake but screen off)
    // Deep sleep only happens during screen off when CPU is suspended
    // So: Screen Off Time = Awake (screen off) + Deep Sleep
    // Therefore: Awake (screen off) = Screen Off Time - Deep Sleep
    val awakeScreenOff = (totalScreenOff - deepSleepTime).coerceAtLeast(0L)
    val totalAwakeTime = totalScreenOn + awakeScreenOff

    // Calculate drain rates (% per hour)
    val activeDrainRate =
        if (totalScreenOn > 0) {
          (batteryDrainWhileScreenOn.toFloat() / (totalScreenOn / 3600000f))
        } else 0f

    val idleDrainRate =
        if (totalScreenOff > 0) {
          (batteryDrainWhileScreenOff.toFloat() / (totalScreenOff / 3600000f))
        } else 0f

    // Format time strings (Compact: 1h 20m)
    val screenOnStr = formatTimeCompact(totalScreenOn)
    val screenOffStr = formatTimeCompact(totalScreenOff)
    val deepSleepStr = formatTimeCompact(deepSleepTime)
    val awakeStr = formatTimeCompact(totalAwakeTime)

    // Format numbers
    val tempStr = "%.1f°C".format(temp / 10f)
    val activeDrainStr = "%.1f".format(activeDrainRate)
    val idleDrainStr = "%.1f".format(idleDrainRate)

    // Calculate Watts (P = V * I)
    // Voltage is in mV, Current is in mA
    // Watts = (mV / 1000) * (mA / 1000)
    val watts = (kotlin.math.abs(voltage) / 1000f) * (kotlin.math.abs(currentNow) / 1000f)
    val wattsStr = "%.1f".format(watts)

    // Format current (Always signed) with Watts if charging
    val absCurrent = kotlin.math.abs(currentNow)
    val sign = if (currentNow > 0) "+ " else if (currentNow < 0) "- " else ""
    val currentStr = "$sign$absCurrent mA"
    val powerStr = if (charging && watts > 0) "($wattsStr W)" else ""
    
    // Charging/Discharging status
    val statusStr = if (charging) "Charging" else "Discharging"

    // Calculate percentages based on total elapsed time since service started
    val totalElapsedTime = currentTime - serviceStartTime
    
    val screenOnPercent = if (totalElapsedTime > 0) {
      ((totalScreenOn.toFloat() / totalElapsedTime) * 100).toInt().coerceIn(0, 100)
    } else 0
    
    val screenOffPercent = if (totalElapsedTime > 0) {
      ((totalScreenOff.toFloat() / totalElapsedTime) * 100).toInt().coerceIn(0, 100)
    } else 0
    
    val deepSleepPercent = if (totalElapsedTime > 0) {
      ((deepSleepTime.toFloat() / totalElapsedTime) * 100).toInt().coerceIn(0, 100)
    } else 0
    
    val awakePercent = if (totalElapsedTime > 0) {
      ((totalAwakeTime.toFloat() / totalElapsedTime) * 100).toInt().coerceIn(0, 100)
    } else 0

    // Title: 55% • Charging • +351 mA (1.4 W) • 32.6°C
    val title = "$level% • $statusStr • $currentStr $powerStr • $tempStr"

    // Use Native Builder for O+ to avoid Compat issues with dynamic icons
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val bigTextStyle =
          Notification.BigTextStyle()
              .setBigContentTitle(title)
              .bigText(
                  buildString {
                    if (showScreen) {
                      appendLine("Screen On: $screenOnStr ($screenOnPercent%)")
                      appendLine("Screen Off: $screenOffStr ($screenOffPercent%)")
                    }
                    if (showActiveIdle) {
                      appendLine("Active Drain: $activeDrainStr%/h")
                      appendLine("Idle Drain: $idleDrainStr%/h")
                    }
                    if (showAwakeSleep) {
                      appendLine("Awake: $awakeStr ($awakePercent%)")
                      append("Deep Sleep: $deepSleepStr ($deepSleepPercent%)")
                    }
                    if (!showScreen && !showActiveIdle && !showAwakeSleep) {
                      append("Battery monitoring active")
                    }
                  }
              )

      val builder =
          Notification.Builder(this, CHANNEL_ID)
              .setContentTitle(title)
              .setContentText(
                  if (showScreen) "Screen On: $screenOnStr • Charge: $level%"
                  else "Battery: $level% • ${if (charging) "Charging" else "Discharging"}"
              )
              .setStyle(bigTextStyle)
              .setOngoing(true)
              .setColor(if (charging) Color.parseColor("#4CAF50") else Color.parseColor("#FFC107"))
              .setOnlyAlertOnce(true)
              .setVisibility(
                  if (isSecureLockScreen) Notification.VISIBILITY_PUBLIC
                  else Notification.VISIBILITY_SECRET
              )
      
      // Category Service
      if (isForceOnTop) {
        builder.setCategory(Notification.CATEGORY_SERVICE)
      }

      // Small Icon
      val nativeIcon =
          NotificationHelper.generateNativeIcon(
              this,
              currentIconType,
              level,
              charging,
              temp,
              currentNow,
              voltage,
          )
      builder.setSmallIcon(nativeIcon)

      return builder.build()
    }

    // Fallback: NotificationCompat for Pre-Oreo
    val bigTextStyle =
        NotificationCompat.BigTextStyle()
            .setBigContentTitle(title)
            .bigText(
                buildString {
                  if (showScreen) {
                    appendLine("Screen On: $screenOnStr ($screenOnPercent%)")
                    appendLine("Screen Off: $screenOffStr ($screenOffPercent%)")
                  }
                  if (showActiveIdle) {
                    appendLine("Active Drain: $activeDrainStr%/h")
                    appendLine("Idle Drain: $idleDrainStr%/h")
                  }
                  if (showAwakeSleep) {
                    appendLine("Awake: $awakeStr ($awakePercent%)")
                    append("Deep Sleep: $deepSleepStr ($deepSleepPercent%)")
                  }
                  if (!showScreen && !showActiveIdle && !showAwakeSleep) {
                    append("Battery monitoring active")
                  }
                }
            )

    val builder =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(
                if (showScreen) "Screen On: $screenOnStr • Charge: $level%"
                else "Battery: $level% • ${if (charging) "Charging" else "Discharging"}"
            ) // Collapsed state
            .setStyle(bigTextStyle)
            .setOngoing(true)
            .setColor(
                if (charging) Color.parseColor("#4CAF50") else Color.parseColor("#FFC107")
            ) // Material Green/Amber
            .setOnlyAlertOnce(true)
            .setVisibility(
                if (isSecureLockScreen) NotificationCompat.VISIBILITY_PUBLIC
                else NotificationCompat.VISIBILITY_SECRET
            )
            .setPriority(
                when {
                    isForceOnTop -> NotificationCompat.PRIORITY_MAX
                    isHighPriority -> NotificationCompat.PRIORITY_HIGH
                    else -> NotificationCompat.PRIORITY_LOW
                }
            )

    // Force On Top (Ongoing + High Priority usually does it)
    if (isForceOnTop) {
      builder.setCategory(NotificationCompat.CATEGORY_SERVICE)
    }

    // Set dynamic small icon using NotificationHelper
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Log.d("BatteryInfoService", "Building COMPAT notification with icon: '$currentIconType'")
      val icon =
          NotificationHelper.generateIcon(
              this,
              currentIconType,
              level,
              charging,
              temp,
              currentNow,
              voltage,
          )
      builder.setSmallIcon(icon)
    } else {
      // Fallback for older APIs (though minSdk is likely higher)
      builder.setSmallIcon(if (charging) R.drawable.ic_battery_charging else R.drawable.ic_battery)
    }

    return builder.build()
  }

  private fun formatTimeCompact(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return if (hours > 0) {
      "%dh %02dm".format(hours, minutes % 60)
    } else {
      "%dm %02ds".format(minutes, seconds % 60)
    }
  }

  private fun formatTime(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return "%dh %02dm %02ds".format(hours, minutes % 60, seconds % 60)
  }

  /**
   * Get battery current from multiple sources with fallback. Returns positive value when charging,
   * negative when discharging.
   */
  private fun getBatteryCurrent(isCharging: Boolean): Int {
    // List of possible paths for battery current (in microamps)
    val currentPaths =
        listOf(
            "/sys/class/power_supply/battery/current_now",
            "/sys/class/power_supply/bms/current_now",
            "/sys/class/power_supply/Battery/current_now",
            "/sys/class/power_supply/main/current_now",
            "/sys/class/power_supply/usb/current_now",
            "/sys/class/power_supply/ac/current_now",
        )

    // Try reading from sysfs paths first
    for (path in currentPaths) {
      try {
        val file = File(path)
        if (file.exists() && file.canRead()) {
          val rawValue = file.readText().trim().toLongOrNull() ?: continue
          if (rawValue != 0L) {
            // Convert from microamps to milliamps
            var currentMa = (rawValue / 1000).toInt()

            // Determine sign based on charging state and raw value
            // Some devices report positive when charging, some report negative
            // We normalize: positive = charging, negative = discharging
            currentMa =
                if (isCharging) {
                  kotlin.math.abs(currentMa) // Always positive when charging
                } else {
                  -kotlin.math.abs(currentMa) // Always negative when discharging
                }

            Log.d("BatteryInfoService", "Current from $path: ${currentMa}mA")
            return currentMa
          }
        }
      } catch (e: Exception) {
        Log.d("BatteryInfoService", "Failed to read $path: ${e.message}")
      }
    }

    // Fallback: Use BatteryManager API (Android 5.0+)
    try {
      val batteryManager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
      if (batteryManager != null) {
        // BATTERY_PROPERTY_CURRENT_NOW returns microamps
        val currentMicroAmps =
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        if (currentMicroAmps != Int.MIN_VALUE && currentMicroAmps != 0) {
          var currentMa = currentMicroAmps / 1000

          // Normalize sign based on charging state
          currentMa =
              if (isCharging) {
                kotlin.math.abs(currentMa)
              } else {
                -kotlin.math.abs(currentMa)
              }

          Log.d("BatteryInfoService", "Current from BatteryManager API: ${currentMa}mA")
          return currentMa
        }
      }
    } catch (e: Exception) {
      Log.e("BatteryInfoService", "BatteryManager API failed: ${e.message}")
    }

    // Fallback: Try reading with root access
    try {
      val process =
          Runtime.getRuntime()
              .exec(arrayOf("su", "-c", "cat /sys/class/power_supply/battery/current_now"))
      val result = process.inputStream.bufferedReader().readText().trim()
      process.waitFor()

      val rawValue = result.toLongOrNull()
      if (rawValue != null && rawValue != 0L) {
        var currentMa = (rawValue / 1000).toInt()
        currentMa =
            if (isCharging) {
              kotlin.math.abs(currentMa)
            } else {
              -kotlin.math.abs(currentMa)
            }
        Log.d("BatteryInfoService", "Current from root: ${currentMa}mA")
        return currentMa
      }
    } catch (e: Exception) {
      Log.d("BatteryInfoService", "Root read failed: ${e.message}")
    }

    Log.w("BatteryInfoService", "Could not read battery current from any source, using cached value")
    return if (cachedCurrent != 0) {
      if (isCharging) kotlin.math.abs(cachedCurrent) else -kotlin.math.abs(cachedCurrent)
    } else {
      0
    }
  }

  /**
   * Calculate deep sleep time since service started. Deep sleep = Total elapsed realtime - CPU
   * uptime This works because uptimeMillis doesn't count when CPU is in suspend state.
   */
  private fun getDeepSleepTime(): Long {
    return try {
      // elapsedRealtime counts even during deep sleep
      // uptimeMillis only counts when CPU is awake
      val currentElapsedRealtime = SystemClock.elapsedRealtime()
      val currentUptime = SystemClock.uptimeMillis()

      // Total deep sleep since boot = elapsedRealtime - uptimeMillis
      val totalDeepSleepSinceBoot = currentElapsedRealtime - currentUptime

      // Deep sleep since service started
      val deepSleepSinceServiceStart = totalDeepSleepSinceBoot - initialDeepSleep

      if (deepSleepSinceServiceStart > 0L) {
        deepSleepSinceServiceStart
      } else {
        // Fallback to total deep sleep since boot if calculation gives negative
        totalDeepSleepSinceBoot.coerceAtLeast(0L)
      }
    } catch (e: Exception) {
      Log.e("BatteryInfoService", "Error calculating deep sleep", e)
      0L
    }
  }

  /** Get initial deep sleep time from system. Used to establish a baseline when service starts. */
  private fun getSystemDeepSleepTime(): Long {
    return try {
      // Calculate from system clocks
      val elapsedRealtime = SystemClock.elapsedRealtime()
      val uptime = SystemClock.uptimeMillis()
      (elapsedRealtime - uptime).coerceAtLeast(0L)
    } catch (e: Exception) {
      Log.e("BatteryInfoService", "Error getting system deep sleep", e)
      0L
    }
  }

  /**
   * Reset all battery statistics to start fresh. Called when battery reaches 100% to start a new
   * usage cycle.
   */
  private fun resetBatteryStats() {
    val now = SystemClock.elapsedRealtime()

    // Reset screen time tracking
    screenOnTime = 0
    screenOffTime = 0
    lastScreenOnTimestamp = now
    lastScreenOffTimestamp = now

    // Reset drain tracking
    batteryDrainWhileScreenOn = 0
    batteryDrainWhileScreenOff = 0

    // Reset deep sleep baseline
    initialElapsedRealtime = now
    initialDeepSleep = getSystemDeepSleepTime()

    // Reset last battery level (will be set on next update)
    lastBatteryLevel = -1
    lastUpdateTime = System.currentTimeMillis()

    Log.d(
        "BatteryInfoService",
        "Battery stats reset: screen times, drain stats, and deep sleep baseline cleared",
    )
  }

  override fun onDestroy() {
    receiver?.let { unregisterReceiver(it) }
    screenReceiver?.let { unregisterReceiver(it) }
    
    // Cancel all coroutines
    serviceJob.cancel()
    
    // Explicitly remove notification and stop foreground
    try {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIF_ID)
    } catch (e: Exception) {
        Log.e("BatteryInfoService", "Failed to cancel notification: ${e.message}")
    }
    stopForeground(STOP_FOREGROUND_REMOVE)
    super.onDestroy()
  }

  override fun onBind(intent: Intent?) = null

  private fun createNotificationChannel() {
    val importance =
        if (isHighPriority) NotificationManager.IMPORTANCE_HIGH
        else NotificationManager.IMPORTANCE_LOW
    val channel = NotificationChannel(CHANNEL_ID, "Battery Info", importance)
    channel.description = "Shows real-time battery status"
    channel.setShowBadge(false)
    val notificationManager = getSystemService(NotificationManager::class.java)
    notificationManager.createNotificationChannel(channel)
  }

  private fun startNativePolling(scope: CoroutineScope) {
    scope.launch(Dispatchers.IO) {
      Log.d("BatteryInfoService", "Starting polling with rate: ${refreshRateMs}ms")
      while (isActive) {
        try {
          updateRealtimeData()
        } catch (e: Exception) {
          Log.e("BatteryInfoService", "Error in polling loop: ${e.message}")
        }
        kotlinx.coroutines.delay(refreshRateMs)
      }
    }
  }

  /**
   * Reads battery data from available sources (Native -> Root -> System)
   * updates the cached values, and triggers a state refresh.
   */
  private suspend fun updateRealtimeData() {
    var level = -1
    var current = 0
    var voltage = 0
    var temp = 0
    var isCharging = false
    var health = "Unknown"
    
    var dataRead = false

    // 1. Try Native Library (Fastest)
    if (id.xms.xtrakernelmanager.domain.native.NativeLib.isAvailable()) {
        try {
            val nLevel = id.xms.xtrakernelmanager.domain.native.NativeLib.readBatteryLevel()
            if (nLevel != null) {
                level = nLevel
                current = id.xms.xtrakernelmanager.domain.native.NativeLib.readBatteryCurrent() ?: 0
                voltage = (id.xms.xtrakernelmanager.domain.native.NativeLib.readBatteryVoltage()?.times(1000))?.toInt() ?: 0
                temp = (id.xms.xtrakernelmanager.domain.native.NativeLib.readBatteryTemp()?.times(10))?.toInt() ?: 0
                health = id.xms.xtrakernelmanager.domain.native.NativeLib.readBatteryHealth() ?: "Unknown"
                
                // Native isCharging check
                val nCharging = id.xms.xtrakernelmanager.domain.native.NativeLib.isCharging()
                if (nCharging != null) isCharging = nCharging
                
                dataRead = level != -1 && voltage != 0
            }
        } catch (e: Exception) {
            Log.d("BatteryInfoService", "Native read failed: ${e.message}")
        }
    }

    // 2. Try Root Fallback (If native failed or unavailable)
    if (!dataRead) {
         try {
            // Read Level
            val rootLevel = id.xms.xtrakernelmanager.domain.root.RootManager.readFile(
                "/sys/class/power_supply/battery/capacity"
            ).getOrNull()?.replace("\n", "")?.toIntOrNull()
            
            if (rootLevel != null && rootLevel > 0) {
                level = rootLevel
                
                // Read Current
                val rootCurrent = id.xms.xtrakernelmanager.domain.root.RootManager.readFile(
                    "/sys/class/power_supply/battery/current_now"
                ).getOrNull()?.replace("\n", "")?.toIntOrNull()
                
                if (rootCurrent != null) {
                    // Convert uA to mA, take abs (sign handled later)
                    current = kotlin.math.abs(rootCurrent / 1000)
                }

                // Read Voltage
                val rootVoltage = id.xms.xtrakernelmanager.domain.root.RootManager.readFile(
                    "/sys/class/power_supply/battery/voltage_now"
                ).getOrNull()?.replace("\n", "")?.toIntOrNull()
                
                if (rootVoltage != null) voltage = rootVoltage / 1000 // uV to mV
                
                // Read Temp
                val rootTemp = id.xms.xtrakernelmanager.domain.root.RootManager.readFile(
                    "/sys/class/power_supply/battery/temp"
                ).getOrNull()?.replace("\n", "")?.toIntOrNull()
                
                if (rootTemp != null) temp = rootTemp



                // Read Status (Optimized with Caching & Direct Read)
                // 1. Resolve Path if not cached
                if (cachedStatusPath == null) {
                    val statusPaths = listOf(
                        "/sys/class/power_supply/battery/status",
                        "/sys/class/power_supply/bms/status",
                        "/sys/class/power_supply/usb/status",
                        "/sys/class/power_supply/main/status"
                    )
                    
                    for (path in statusPaths) {
                        try {
                             val file = File(path)
                             if (file.exists() && file.canRead()) {
                                 cachedStatusPath = path
                                 break
                             } else {
                                 // Try root check
                                 val check = id.xms.xtrakernelmanager.domain.root.RootManager.executeCommand(
                                     "[ -f $path ] && echo exists"
                                 ).getOrNull()?.trim()
                                 if (check == "exists") {
                                     cachedStatusPath = path
                                     break
                                 }
                             }
                        } catch (e: Exception) { continue }
                    }
                }

                // 2. Read Status String
                var statusStr: String? = null
                if (cachedStatusPath != null) {
                     // Try Direct Read First (Fastest)
                     try {
                         statusStr = File(cachedStatusPath!!).readText().trim()
                     } catch (e: Exception) {
                         // Fallback to Root Read
                         statusStr = id.xms.xtrakernelmanager.domain.root.RootManager.readFile(
                             cachedStatusPath!!
                         ).getOrNull()?.trim()
                     }
                }

                if (statusStr != null) {
                   // Map to BatteryManager constants
                   cachedStatus = when {
                       statusStr.equals("Charging", ignoreCase = true) -> BatteryManager.BATTERY_STATUS_CHARGING
                       statusStr.equals("Discharging", ignoreCase = true) -> BatteryManager.BATTERY_STATUS_DISCHARGING
                       statusStr.equals("Not charging", ignoreCase = true) -> BatteryManager.BATTERY_STATUS_NOT_CHARGING
                       statusStr.equals("Full", ignoreCase = true) -> BatteryManager.BATTERY_STATUS_FULL
                       else -> BatteryManager.BATTERY_STATUS_UNKNOWN
                   }

                   // Update Plugged State based on Status
                   cachedPlugged = if (cachedStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                                       cachedStatus == BatteryManager.BATTERY_STATUS_FULL || 
                                       cachedStatus == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                                       1 // Generic plugged
                                   } else {
                                       0 // Unplugged/Discharging
                                   }

                   isCharging = cachedStatus == BatteryManager.BATTERY_STATUS_CHARGING || 
                                cachedStatus == BatteryManager.BATTERY_STATUS_FULL
                }

                dataRead = true
                Log.d("BatteryInfoService", "Used Root Fallback: L=$level, C=$current")
            }
         } catch (e: Exception) {
             Log.d("BatteryInfoService", "Root fallback failed: ${e.message}")
         }
    }

    // 3. System API Fallback (Last Resort)
    if (!dataRead) {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        if (batteryManager != null) {
            level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            isCharging = batteryManager.isCharging
            
            val microAmps = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            if (microAmps != Int.MIN_VALUE) current = kotlin.math.abs(microAmps / 1000)
            
            // Cannot reliably get Temp/Voltage/Health via BatteryManager without Intent
            // So we partially update or rely on the last BroadcastReceiver intent values
            if (cachedTemp != 0) temp = cachedTemp
            if (cachedVoltage != 0) voltage = cachedVoltage
            if (cachedHealth != "Unknown") health = cachedHealth
            
            dataRead = true
        }
    }

    if (dataRead) {
        // Apply logic for current sign based on charging status
        current = if (isCharging) kotlin.math.abs(current) else -kotlin.math.abs(current)

        // Smoothing
        if (current != 0) {
          if (!isCurrentInitialized) {
            smoothedCurrent = current.toDouble()
            isCurrentInitialized = true
          } else {
            val alpha = getEmaAlpha()
            smoothedCurrent = alpha * current + (1 - alpha) * smoothedCurrent
          }
          current = smoothedCurrent.toInt()
        } else if (isCurrentInitialized) {
          current = smoothedCurrent.toInt()
        }

        // Update Cache
        cachedLevel = level
        cachedCurrent = current
        cachedVoltage = voltage
        cachedTemp = temp
        cachedIsCharging = isCharging
        cachedHealth = health

        refreshState()
    }
  }
}
