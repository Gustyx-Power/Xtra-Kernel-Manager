package id.xms.xtrakernelmanager.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
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
                
                // Get charging current
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val current = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                val chargingWattage = abs(voltage * current / 1000000f) // Convert to watts

                // Determine charging status based on both status and current
                // Some devices report current as negative when discharging and positive when charging
                // But this can vary by manufacturer, so we'll use the status as primary indicator
                val isCharging = if (status == BatteryManager.BATTERY_STATUS_CHARGING || 
                                   status == BatteryManager.BATTERY_STATUS_FULL) {
                    true
                } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                    false
                } else {
                    // For other statuses, use current as secondary indicator
                    // If current is significantly positive, consider it charging
                    current > 10000 // 10mA threshold to avoid false positives from noise
                }

                val batteryInfo = BatteryInfo(
                    level = (level * 100 / scale.toFloat()).toInt(),
                    temp = temp,
                    voltage = voltage,
                    isCharging = isCharging,
                    current = current.toFloat() / 1000f, // Convert to mA
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
