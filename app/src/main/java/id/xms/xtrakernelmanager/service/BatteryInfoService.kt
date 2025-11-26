package id.xms.xtrakernelmanager.service

import android.app.*
import android.content.*
import android.os.*
import android.graphics.Color
import android.os.BatteryManager
import androidx.core.app.NotificationCompat
import id.xms.xtrakernelmanager.R

class BatteryInfoService : Service() {
    private val NOTIF_ID = 1001
    private val CHANNEL_ID = "battery_info"
    private var receiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerReceiver()
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

    private fun buildNotification(level: Int, charging: Boolean, temp: Int, voltage: Int, health: String): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Battery: $level%  ${if(charging) "Charging" else "Discharging"}")
            .setContentText("Temp: %.1f°C  Health: %s  Voltage: %.2fV".format(temp/10f, health, voltage/1000f))
            .setSmallIcon(if (charging) R.drawable.ic_battery_charging else R.drawable.ic_battery)
            .setOngoing(true)
            .setColor(Color.YELLOW)
            .setOnlyAlertOnce(true)
        return builder.build()
    }

    override fun onDestroy() {
        receiver?.let { unregisterReceiver(it) }
        stopForeground(true)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
}
