package id.xms.xtrakernelmanager.service

import android.app.*
import android.content.*
import android.os.*
import android.graphics.Color
import android.os.BatteryManager
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import id.xms.xtrakernelmanager.R
import java.io.File

class BatteryInfoService : Service() {
    private val NOTIF_ID = 1001
    private val CHANNEL_ID = "battery_info"
    private var receiver: BroadcastReceiver? = null
    private var screenReceiver: BroadcastReceiver? = null

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

    override fun onCreate() {
        super.onCreate()
        serviceStartTime = SystemClock.elapsedRealtime()
        lastScreenOnTimestamp = serviceStartTime
        lastUpdateTime = System.currentTimeMillis()

        // Check initial screen state
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        isScreenOn = powerManager.isInteractive

        createNotificationChannel()
        registerReceiver()
        registerScreenReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification(0, false, 0, 0, "Unknown"))
        return START_STICKY
    }

    private fun registerReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null) return
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
                val isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL)

                // Track battery drain
                if (lastBatteryLevel != -1 && level < lastBatteryLevel && !isCharging) {
                    val drain = lastBatteryLevel - level
                    if (isScreenOn) {
                        batteryDrainWhileScreenOn += drain
                    } else {
                        batteryDrainWhileScreenOff += drain
                    }
                }
                lastBatteryLevel = level

                val healthTxt = when(health) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                    BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
                    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
                    else -> "Unknown"
                }
                val notif = buildNotification(level, isCharging, temp, voltage, healthTxt)
                startForeground(NOTIF_ID, notif)
            }
        }
        registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun registerScreenReceiver() {
        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_ON -> {
                        val now = SystemClock.elapsedRealtime()
                        if (!isScreenOn) {
                            screenOffTime += now - lastScreenOffTimestamp
                        }
                        lastScreenOnTimestamp = now
                        isScreenOn = true
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        val now = SystemClock.elapsedRealtime()
                        if (isScreenOn) {
                            screenOnTime += now - lastScreenOnTimestamp
                        }
                        lastScreenOffTimestamp = now
                        isScreenOn = false
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenReceiver, filter)
    }

    private fun buildNotification(level: Int, charging: Boolean, temp: Int, voltage: Int, health: String): Notification {
        // Calculate current times
        val currentTime = SystemClock.elapsedRealtime()
        val totalScreenOn = if (isScreenOn) {
            screenOnTime + (currentTime - lastScreenOnTimestamp)
        } else {
            screenOnTime
        }
        val totalScreenOff = if (!isScreenOn) {
            screenOffTime + (currentTime - lastScreenOffTimestamp)
        } else {
            screenOffTime
        }

        // Get deep sleep time from system
        val deepSleepTime = getDeepSleepTime()
        val awakeTime = totalScreenOff - deepSleepTime

        // Calculate drain rates (% per hour)
        val activeDrainRate = if (totalScreenOn > 0) {
            (batteryDrainWhileScreenOn.toFloat() / (totalScreenOn / 3600000f))
        } else 0f

        val idleDrainRate = if (totalScreenOff > 0) {
            (batteryDrainWhileScreenOff.toFloat() / (totalScreenOff / 3600000f))
        } else 0f

        // Format time strings
        val screenOnStr = formatTime(totalScreenOn)
        val screenOffStr = formatTime(totalScreenOff)
        val deepSleepStr = formatTime(deepSleepTime)
        val awakeStr = formatTime(awakeTime)

        // Build notification with detailed layout
        val tempStr = "%.1f".format(temp/10f)
        val voltageStr = "%.2f".format(voltage/1000f)
        val activeDrainStr = "%.2f".format(activeDrainRate)
        val idleDrainStr = "%.2f".format(idleDrainRate)

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .setBigContentTitle("Battery: $level% | $tempStr°C ${if(charging) "Charging" else "Discharging"}")
            .bigText(buildString {
                appendLine("Voltage: ${voltageStr}V")
                appendLine("Health: $health")
                appendLine("Screen On: $screenOnStr")
                appendLine("Screen Off: $screenOffStr")
                appendLine("Deep Sleep: $deepSleepStr")
                appendLine("Awake: $awakeStr")
                appendLine("Active Drain: $activeDrainStr%/h")
                append("Idle Drain: $idleDrainStr%/h")
            })

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Battery: $level% | $tempStr°C ${if(charging) "Charging" else "Discharging"}")
            .setContentText("Screen: $screenOnStr | Drain: $activeDrainStr%/h")
            .setStyle(bigTextStyle)
            .setSmallIcon(if (charging) R.drawable.ic_battery_charging else R.drawable.ic_battery)
            .setOngoing(true)
            .setColor(if (charging) Color.GREEN else Color.YELLOW)
            .setOnlyAlertOnce(true)
        return builder.build()
    }

    private fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        return "%dh %02dm %02ds".format(hours, minutes % 60, seconds % 60)
    }

    private fun getDeepSleepTime(): Long {
        return try {
            // Read deep sleep time from kernel
            val suspendTimeFile = File("/sys/power/suspend_stats/total_time")
            if (suspendTimeFile.exists()) {
                val suspendTimeStr = suspendTimeFile.readText().trim()
                (suspendTimeStr.toLongOrNull() ?: 0L) * 1000 // Convert to milliseconds
            } else {
                // Alternative: try reading from /sys/kernel/wakeup_reasons/last_suspend_time
                val altFile = File("/sys/kernel/debug/suspend_stats")
                if (altFile.exists()) {
                    0L // Would need more complex parsing
                } else {
                    0L
                }
            }
        } catch (e: Exception) {
            0L
        }
    }

    override fun onDestroy() {
        receiver?.let { unregisterReceiver(it) }
        screenReceiver?.let { unregisterReceiver(it) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Battery Info",
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = "Shows real-time battery status"
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}
