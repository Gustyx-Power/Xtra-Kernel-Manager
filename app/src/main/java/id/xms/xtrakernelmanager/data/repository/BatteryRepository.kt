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

  companion object {
    private const val DESIGN_CYCLES = 800f
    private const val CYCLE_DEGRADATION_AT_DESIGN = 20f
    private const val MAX_CYCLE_PENALTY = 35f
    private const val CAPACITY_WEIGHT = 0.7f
    private const val CYCLE_WEIGHT = 0.3f
  }

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
          // Native first: Try using NativeLib for capacity (reads charge_full_design and charge_full internally)
          val nativeCapacityLevel = id.xms.xtrakernelmanager.domain.native.NativeLib.readBatteryCapacityLevel()s
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
          cachedCycleCount =
              id.xms.xtrakernelmanager.domain.native.NativeLib.readCycleCount()
                  ?: RootManager.readFile("/sys/class/power_supply/battery/cycle_count")
                      .getOrNull()
                      ?.trim()
                      ?.toIntOrNull()
                  ?: 0
        }


        // Current: Try fast NativeLib first, Fallback to RootManager
        var currentNow = id.xms.xtrakernelmanager.domain.native.NativeLib.readDrainRate() ?: 0
        if (currentNow == 0) {
          // Determine sign based on status
          val raw =
              RootManager.readFile("/sys/class/power_supply/battery/current_now")
                  .getOrNull()
                  ?.trim()
                  ?.toIntOrNull()
                  ?.div(1000) ?: 0 // uA -> mA

          // Kernel conventions vary. Re-align with charging status.
          val isCharging = statusText == "Charging" || statusText == "Full"
          currentNow =
              if (isCharging) {
                kotlin.math.abs(raw)
              } else {
                -kotlin.math.abs(raw)
              }
        }

        val capacityHealth =
            if (cachedTotalCapacity > 0 && cachedCurrentCapacity > 0) {
                  (cachedCurrentCapacity.toFloat() / cachedTotalCapacity.toFloat()) * 100f
                } else {
                  100f // Fallback if still 0
                }
                .coerceIn(0f, 100f)

        val cycleHealth =
            if (cachedCycleCount > 0) {
              val normalizedCycles = (cachedCycleCount.toFloat() / DESIGN_CYCLES)
              val degradationFromCycles =
                  (normalizedCycles * CYCLE_DEGRADATION_AT_DESIGN).coerceAtMost(MAX_CYCLE_PENALTY)

              (100f - degradationFromCycles).coerceIn(0f, 100f)
            } else {
              100f
            }

        // If we have actual capacity data, calculate combined health. Else show 0 or 100?
        // User saw 0% because capacity was 0. If Root read fails, it will still be 0.
        // But now we TRY to read it via Root.

        val combinedHealth =
            if (cachedTotalCapacity > 0) {
              (capacityHealth * CAPACITY_WEIGHT + cycleHealth * CYCLE_WEIGHT).coerceIn(0f, 100f)
            } else {
              0f // Or 100f? If we can't read capacity, 0% is honest but scary.
              // Previous code returned 100f if 0.
              // But users prefer to see 0% "Unknown" than fake 100%.
              // Let's stick to calculated logic. Use 0f if no data.
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
