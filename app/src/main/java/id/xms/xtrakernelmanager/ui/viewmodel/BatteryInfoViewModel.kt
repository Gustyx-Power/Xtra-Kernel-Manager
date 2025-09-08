package id.xms.xtrakernelmanager.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BatteryInfoViewModel @Inject constructor(
    private val application: Application
) : AndroidViewModel(application) {

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                updateBatteryInfo(intent)
            }
        }
    }

    init {
        // Register battery receiver
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        application.registerReceiver(batteryReceiver, filter)

        // Get initial battery info
        val batteryIntent = application.registerReceiver(null, filter)
        batteryIntent?.let { updateBatteryInfo(it) }
    }

    private fun updateBatteryInfo(intent: Intent) {
        viewModelScope.launch {
            try {
                val batteryManager = application.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

                // Basic battery information
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
                val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

                // Enhanced battery information
                val chargingStatus = getChargingStatus(status, plugged)
                val powerSource = getPowerSource(plugged)
                val healthString = getBatteryHealth(health)
                val healthPercentage = getBatteryHealthPercentage()
                val cycleCount = getBatteryCycleCount()
                val capacity = getBatteryCapacity()
                val currentCapacity = getCurrentCapacity()
                
                // Get current (in µA) to help determine charging state more accurately
                val current = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                val currentMa = if (current != Int.MIN_VALUE) current.toFloat() / 1000f else 0f
                
                // Log values for debugging
                Log.d("BatteryInfoViewModel", "Battery Status: $status, Plugged: $plugged, Current: $currentMa mA")
                Log.d("BatteryInfoViewModel", "Status Constants - CHARGING: ${BatteryManager.BATTERY_STATUS_CHARGING}, DISCHARGING: ${BatteryManager.BATTERY_STATUS_DISCHARGING}, FULL: ${BatteryManager.BATTERY_STATUS_FULL}, NOT_CHARGING: ${BatteryManager.BATTERY_STATUS_NOT_CHARGING}")
                
                // Determine charging status using multiple indicators for accuracy
                // Some devices may report inconsistent values, so we'll use a more robust approach
                val isCharging = when {
                    // If status explicitly says charging or full, trust it
                    status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL -> true
                    // If status explicitly says discharging or not charging, trust it
                    status == BatteryManager.BATTERY_STATUS_DISCHARGING || status == BatteryManager.BATTERY_STATUS_NOT_CHARGING -> false
                    // If plugged is 0, definitely not charging
                    plugged == 0 -> false
                    // If we have current data, use it as an additional indicator.
                    // Negative current means charging, positive means discharging.
                    current != Int.MIN_VALUE -> {
                        current < -5000 // Use a threshold (e.g., -5mA) to account for small fluctuations
                    }
                    // If plugged but we can't determine from other sources, assume charging
                    plugged != 0 -> true
                    // Default fallback
                    else -> false
                }
                
                Log.d("BatteryInfoViewModel", "Calculated isCharging: $isCharging")

                // Calculate charging wattage
                val chargingWattage = if (current != Int.MIN_VALUE) {
                    (Math.abs(current) * voltage) / 1000000f // Convert to watts
                } else {
                    0f
                }

                // Log real-time battery health calculation for debugging
                if (capacity > 0 && currentCapacity > 0) {
                    Log.d("BatteryInfoViewModel", "Real-time Battery Health Calculation:")
                    Log.d("BatteryInfoViewModel", "Design Capacity: ${capacity} mAh")
                    Log.d("BatteryInfoViewModel", "Current Capacity: ${currentCapacity} mAh")
                    Log.d("BatteryInfoViewModel", "Health Percentage: ${healthPercentage}% = (${currentCapacity} / ${capacity}) × 100%")
                }

                _batteryInfo.value = BatteryInfo(
                    level = (level * 100) / scale,
                    temp = temperature, // Changed from temperature to temp
                    voltage = voltage,
                    chargingWattage = if (isCharging) chargingWattage else 0f,
                    isCharging = isCharging,
                    current = currentMa, // Current in mA
                    technology = technology,
                    health = healthString,
                    status = chargingStatus, // Changed from chargingStatus to status
                    powerSource = powerSource,
                    healthPercentage = healthPercentage,
                    cycleCount = cycleCount,
                    capacity = capacity,
                    currentCapacity = currentCapacity,
                    plugged = plugged,
                    chargingType = powerSource // Added chargingType for compatibility
                )
            } catch (e: Exception) {
                // Handle errors gracefully
                e.printStackTrace()
            }
        }
    }

    private fun getChargingStatus(status: Int, plugged: Int): String {
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {
                if (plugged != 0) "Not Charging" else "Discharging"
            }
            else -> "Unknown"
        }
    }

    private fun getPowerSource(plugged: Int): String {
        return when (plugged) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            0 -> "Battery"
            else -> "Unknown"
        }
    }

    private fun getBatteryHealth(health: Int): String {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
    }

    private fun getBatteryHealthPercentage(): Int {
        try {
            // Try to read battery health percentage from various system files
            val healthPaths = listOf(
                "/sys/class/power_supply/battery/health",
                "/sys/class/power_supply/battery/capacity_level",
                "/sys/class/power_supply/bms/battery_health",
                "/proc/battinfo"
            )

            for (path in healthPaths) {
                try {
                    val file = File(path)
                    if (file.exists() && file.canRead()) {
                        val content = file.readText().trim()
                        // Try to extract percentage if available
                        val percentage = content.toIntOrNull()
                        if (percentage != null && percentage in 0..100) {
                            return percentage
                        }
                    }
                } catch (e: Exception) {
                    // Continue to next path
                }
            }

            // Fallback: estimate health based on capacity if available
            val designCapacity = getBatteryCapacity()
            val currentCapacity = getCurrentCapacity()
            if (designCapacity > 0 && currentCapacity > 0) {
                return ((currentCapacity.toFloat() / designCapacity) * 100).toInt()
            }
        } catch (e: Exception) {
            // Handle errors gracefully
        }
        return 0 // Unknown
    }

    private fun getBatteryCycleCount(): Int {
        try {
            val cyclePaths = listOf(
                "/sys/class/power_supply/battery/cycle_count",
                "/sys/class/power_supply/bms/cycle_count",
                "/proc/driver/battery_cycle"
            )

            for (path in cyclePaths) {
                try {
                    val file = File(path)
                    if (file.exists() && file.canRead()) {
                        val content = file.readText().trim()
                        val cycles = content.toIntOrNull()
                        if (cycles != null && cycles >= 0) {
                            return cycles
                        }
                    }
                } catch (e: Exception) {
                    // Continue to next path
                }
            }
        } catch (e: Exception) {
            // Handle errors gracefully
        }
        return 0 // Unknown
    }

    private fun getBatteryCapacity(): Int {
        try {
            // Try to get design capacity from BatteryManager first
            val batteryManager = application.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
            if (capacity != Int.MIN_VALUE && capacity > 0) {
                return capacity / 1000 // Convert to mAh
            }

            // Try reading from system files
            val capacityPaths = listOf(
                "/sys/class/power_supply/battery/charge_full_design",
                "/sys/class/power_supply/battery/energy_full_design",
                "/sys/class/power_supply/bms/charge_full_design",
                "/proc/driver/battery_capacity"
            )

            for (path in capacityPaths) {
                try {
                    val file = File(path)
                    if (file.exists() && file.canRead()) {
                        val content = file.readText().trim()
                        val cap = content.toIntOrNull()
                        if (cap != null && cap > 0) {
                            return if (cap > 10000) cap / 1000 else cap // Convert µAh to mAh if needed
                        }
                    }
                } catch (e: Exception) {
                    // Continue to next path
                }
            }
        } catch (e: Exception) {
            // Handle errors gracefully
        }
        return 0 // Unknown
    }

    private fun getCurrentCapacity(): Int {
        try {
            val capacityPaths = listOf(
                "/sys/class/power_supply/battery/charge_full",
                "/sys/class/power_supply/battery/energy_full",
                "/sys/class/power_supply/bms/charge_full",
                "/proc/driver/battery_current_capacity"
            )

            for (path in capacityPaths) {
                try {
                    val file = File(path)
                    if (file.exists() && file.canRead()) {
                        val content = file.readText().trim()
                        val cap = content.toIntOrNull()
                        if (cap != null && cap > 0) {
                            return if (cap > 10000) cap / 1000 else cap // Convert µAh to mAh if needed
                        }
                    }
                } catch (e: Exception) {
                    // Continue to next path
                }
            }
        } catch (e: Exception) {
            // Handle errors gracefully
        }
        return 0 // Unknown
    }

    override fun onCleared() {
        super.onCleared()
        try {
            application.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Handle gracefully
        }
    }
}
