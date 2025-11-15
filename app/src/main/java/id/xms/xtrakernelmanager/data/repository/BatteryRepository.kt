package id.xms.xtrakernelmanager.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.domain.root.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BatteryRepository {

    suspend fun getBatteryInfo(context: Context? = null): BatteryInfo = withContext(Dispatchers.IO) {
        val batteryStatus = context?.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val percentage = if (scale > 0) (level * 100 / scale) else 0

        val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = temperature / 10f

        val health = when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        val status = when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }

        val technology = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
        val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0

        // Kapasitas saat ini dan design
        val currentCapacity = RootManager.readFile("/sys/class/power_supply/battery/charge_full")
            .getOrNull()?.trim()?.toIntOrNull()?.div(1000) ?: 0 // mAh
        val totalCapacity = RootManager.readFile("/sys/class/power_supply/battery/charge_full_design")
            .getOrNull()?.trim()?.toIntOrNull()?.div(1000) ?: 0 // mAh

        val currentNow = RootManager.readFile("/sys/class/power_supply/battery/current_now")
            .getOrNull()?.trim()?.toIntOrNull()?.div(1000) ?: 0
        val cycleCount = RootManager.readFile("/sys/class/power_supply/battery/cycle_count")
            .getOrNull()?.trim()?.toIntOrNull() ?: 0

        // Health presentase
        val healthPercent = if (totalCapacity > 0) (currentCapacity.toFloat() / totalCapacity.toFloat()) * 100f else 100f

        BatteryInfo(
            level = percentage,
            temperature = tempCelsius,
            health = health,
            currentCapacity = currentCapacity,
            totalCapacity = totalCapacity,
            currentNow = currentNow,
            cycleCount = cycleCount,
            voltage = voltage,
            status = status,
            technology = technology,
            healthPercent = healthPercent
        )
    }
}
