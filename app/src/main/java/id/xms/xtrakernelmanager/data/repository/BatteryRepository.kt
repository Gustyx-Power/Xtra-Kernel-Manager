package id.xms.xtrakernelmanager.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.domain.native.NativeLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

// State for Real-time Monitor (Screen On/Off, Live Current) - Pushed by Service
data class BatteryRealtimeState(
    val level: Int = 0,
    val isCharging: Boolean = false,
    val temp: Int = 0,
    val voltage: Int = 0,
    val currentNow: Int = 0, // mA
    val health: String = "Unknown",
    val screenOnTime: Long = 0L,
    val screenOffTime: Long = 0L,
    val deepSleepTime: Long = 0L,
    val activeDrainRate: Float = 0f,
    val idleDrainRate: Float = 0f
)

object BatteryRepository {

    // Shared State for Material UI
    private val _batteryState = MutableStateFlow(BatteryRealtimeState())
    val batteryState: StateFlow<BatteryRealtimeState> = _batteryState.asStateFlow()

    fun updateState(newState: BatteryRealtimeState) {
        _batteryState.value = newState
    }

    private const val DESIGN_CYCLES = 800f
    private const val CYCLE_DEGRADATION_AT_DESIGN = 20f
    private const val MAX_CYCLE_PENALTY = 35f
    private const val CAPACITY_WEIGHT = 0.7f
    private const val CYCLE_WEIGHT = 0.3f

    private var cachedTotalCapacity = 0
    private var cachedCurrentCapacity = 0
    private var cachedCycleCount = 0

    suspend fun getBatteryInfo(context: Context? = null): BatteryInfo =
        withContext(Dispatchers.IO) {
            val batteryStatus =
                context?.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
            val percentage = if (scale > 0) (level * 100 / scale) else 0

            val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            val tempCelsius = temperature / 10f

            val healthText =
                when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                    BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                    BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                    else -> "Unknown"
                }

            val statusText =
                when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                    BatteryManager.BATTERY_STATUS_FULL -> "Full"
                    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                    else -> "Unknown"
                }

            val technology = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
            val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0

            // Cache capacity values (slow to read via Root, changes rarely)
            if (cachedTotalCapacity == 0 || cachedCurrentCapacity == 0) {
                // Native first
                val nativeCapacityLevel = NativeLib.readBatteryCapacityLevel()
                if (nativeCapacityLevel != null && nativeCapacityLevel > 0f) {
                    cachedTotalCapacity =
                        RootManager.readFile("/sys/class/power_supply/battery/charge_full_design")
                            .getOrNull()
                            ?.trim()
                            ?.toIntOrNull()
                            ?.div(1000) ?: 0

                    cachedCurrentCapacity =
                        RootManager.readFile("/sys/class/power_supply/battery/charge_full")
                            .getOrNull()
                            ?.trim()
                            ?.toIntOrNull()
                            ?.div(1000) ?: 0
                } else {
                    // Fallback: Shell-only reads
                    cachedTotalCapacity =
                        RootManager.readFile("/sys/class/power_supply/battery/charge_full_design")
                            .getOrNull()
                            ?.trim()
                            ?.toIntOrNull()
                            ?.div(1000) ?: 0

                    cachedCurrentCapacity =
                        RootManager.readFile("/sys/class/power_supply/battery/charge_full")
                            .getOrNull()
                            ?.trim()
                            ?.toIntOrNull()
                            ?.div(1000) ?: 0
                }

                // Cycle count: Native first (fast), shell fallback
                cachedCycleCount = NativeLib.readCycleCount()
                    ?: RootManager.readFile("/sys/class/power_supply/battery/cycle_count")
                        .getOrNull()
                        ?.trim()
                        ?.toIntOrNull()
                    ?: 0
            }

            // Current: Try fast NativeLib first, Fallback to RootManager
            var currentNow = NativeLib.readDrainRate() ?: 0
            if (currentNow == 0) {
                // Determine sign based on status
                val raw =
                    RootManager.readFile("/sys/class/power_supply/battery/current_now")
                        .getOrNull()
                        ?.trim()
                        ?.toIntOrNull()
                        ?.div(1000) ?: 0 // uA -> mA

                val isCharging = statusText == "Charging" || statusText == "Full"
                currentNow = if (isCharging) kotlin.math.abs(raw) else -kotlin.math.abs(raw)
            }

            val capacityHealth =
                if (cachedTotalCapacity > 0 && cachedCurrentCapacity > 0) {
                    (cachedCurrentCapacity.toFloat() / cachedTotalCapacity.toFloat()) * 100f
                } else {
                    100f
                }.coerceIn(0f, 100f)

            val cycleHealth =
                if (cachedCycleCount > 0) {
                    val normalizedCycles = (cachedCycleCount.toFloat() / DESIGN_CYCLES)
                    val degradationFromCycles =
                        (normalizedCycles * CYCLE_DEGRADATION_AT_DESIGN).coerceAtMost(
                            MAX_CYCLE_PENALTY
                        )
                    (100f - degradationFromCycles).coerceIn(0f, 100f)
                } else {
                    100f
                }

            val combinedHealth =
                if (cachedTotalCapacity > 0) {
                    (capacityHealth * CAPACITY_WEIGHT + cycleHealth * CYCLE_WEIGHT).coerceIn(0f, 100f)
                } else {
                    0f
                }

            BatteryInfo(
                level = percentage,
                temperature = tempCelsius,
                health = healthText,
                currentCapacity = cachedCurrentCapacity,
                totalCapacity = cachedTotalCapacity,
                currentNow = currentNow,
                cycleCount = cachedCycleCount,
                voltage = voltage,
                status = statusText,
                technology = technology,
                healthPercent = combinedHealth,
            )
        }
}
