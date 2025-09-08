package id.xms.xtrakernelmanager.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import kotlin.math.abs

@Singleton
class BatteryRepository @Inject constructor(
    private val context: Context
) {
    fun getBatteryInfo(): Flow<BatteryInfo> = flow {
        while (true) {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            batteryIntent?.let { intent ->
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
                val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000.0f
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                
                // Get charging current
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val current = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                val currentMa = if (current != Int.MIN_VALUE) current.toFloat() / 1000f else 0f
                val chargingWattage = abs(voltage * current / 1000000f) // Convert to watts

                // Log values for debugging
                Log.d("BatteryRepository", "Battery Status: $status, Plugged: $plugged, Current: $currentMa mA")
                Log.d("BatteryRepository", "Status Constants - CHARGING: ${BatteryManager.BATTERY_STATUS_CHARGING}, DISCHARGING: ${BatteryManager.BATTERY_STATUS_DISCHARGING}, FULL: ${BatteryManager.BATTERY_STATUS_FULL}, NOT_CHARGING: ${BatteryManager.BATTERY_STATUS_NOT_CHARGING}")

                // Determine charging status using multiple indicators for accuracy
                // Some devices may report inconsistent values, so we'll use a more robust approach
                val isCharging = when {
                    // If status explicitly says charging or full, trust it
                    status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL -> true
                    // If status explicitly says discharging or not charging, trust it
                    status == BatteryManager.BATTERY_STATUS_DISCHARGING || status == BatteryManager.BATTERY_STATUS_NOT_CHARGING -> false
                    // If plugged is 0, definitely not charging
                    plugged == 0 -> false
                    // If we have current data, use it as additional indicator
                    current != Int.MIN_VALUE -> {
                        // On most devices, positive current means charging, negative means discharging
                        // However, some devices use opposite convention, so we'll use a threshold
                        current > 5000 // 5mA threshold to account for small fluctuations
                    }
                    // If plugged but we can't determine from other sources, assume charging
                    plugged != 0 -> true
                    // Default fallback
                    else -> false
                }

                Log.d("BatteryRepository", "Calculated isCharging: $isCharging")

                val batteryInfo = BatteryInfo(
                    level = (level * 100 / scale.toFloat()).toInt(),
                    temp = temp,
                    voltage = voltage,
                    isCharging = isCharging,
                    current = currentMa, // Convert to mA
                    chargingWattage = if (isCharging) chargingWattage else 0f,
                    technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "",
                    health = when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
                        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failed"
                        else -> "Unknown"
                    },
                    status = when (status) {
                        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                        BatteryManager.BATTERY_STATUS_FULL -> "Full"
                        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                        else -> "Unknown"
                    }
                )
                emit(batteryInfo)
            }
            kotlinx.coroutines.delay(1000) // Update every second
        }
    }.flowOn(Dispatchers.IO)
}