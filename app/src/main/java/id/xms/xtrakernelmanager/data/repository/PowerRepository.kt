package id.xms.xtrakernelmanager.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.util.Log
import id.xms.xtrakernelmanager.data.model.PowerInfo
import id.xms.xtrakernelmanager.domain.native.NativeLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Power Insight data
 * Handles SOT calculation, drain rates, and battery statistics
 */
class PowerRepository {
    
    companion object {
        private const val TAG = "PowerRepository"
    }
    
    /**
     * Get complete power information
     */
    suspend fun getPowerInfo(context: Context): PowerInfo = withContext(Dispatchers.IO) {
        val (screenOnTime, screenOffTime) = getScreenOnOffTime(context)
        
        val drainRate = NativeLib.readDrainRate() ?: 0
        val batteryLevel = NativeLib.readBatteryLevel() ?: 0
        val batteryTemp = NativeLib.readBatteryTemp() ?: 0f
        val batteryVoltage = NativeLib.readBatteryVoltage() ?: 0f
        val isCharging = NativeLib.isCharging() ?: false
        val wakeupCount = NativeLib.readWakeupCount() ?: 0
        val suspendCount = NativeLib.readSuspendCount() ?: 0
        
        // Calculate deep sleep percentage
        val totalTime = screenOnTime + screenOffTime
        val deepSleepPercentage = if (totalTime > 0) {
            (screenOffTime.toFloat() / totalTime) * 100f
        } else {
            0f
        }
        
        // Calculate drain rates (approximate)
        // Active drain = drain rate when screen is on
        // Idle drain = lower drain when screen is off
        val activeDrainRate = if (drainRate > 0) {
            // Convert mA to %/hour (rough estimate based on typical 4000mAh battery)
            (drainRate.toFloat() / 4000f) * 100f
        } else {
            0f
        }
        val idleDrainRate = activeDrainRate * 0.1f // Idle is typically 10% of active
        
        PowerInfo(
            screenOnTime = screenOnTime,
            screenOffTime = screenOffTime,
            drainRate = drainRate,
            batteryLevel = batteryLevel,
            batteryTemp = batteryTemp,
            batteryVoltage = batteryVoltage,
            isCharging = isCharging,
            wakeupCount = wakeupCount,
            suspendCount = suspendCount,
            deepSleepPercentage = deepSleepPercentage,
            activeDrainRate = activeDrainRate,
            idleDrainRate = idleDrainRate
        )
    }
    
    /**
     * Get screen on and off time using UsageStatsManager
     * Returns Pair(screenOnTimeMs, screenOffTimeMs)
     */
    private fun getScreenOnOffTime(context: Context): Pair<Long, Long> {
        try {
            if (!hasUsageStatsPermission(context)) {
                return Pair(0L, 0L)
            }
            
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return Pair(0L, 0L)
            
            // Get events from last 24 hours
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (24 * 60 * 60 * 1000L) // 24 hours ago
            
            val events = usageStatsManager.queryEvents(startTime, endTime)
            
            if (!events.hasNextEvent()) {
                return Pair(0L, 0L)
            }
            
            var screenOnTime = 0L
            var screenOffTime = 0L
            var lastEventTime = startTime
            
            // We need to determine initial state. This is hard without previous event.
            // But we can iterate first to find the first event.
            // A safer heuristic: If first event is SCREEN_OFF, it implies it was ON before.
            // If first event is SCREEN_ON, it implies it was OFF before.
            // For simplicity and to avoid "24h" bug, we start counting ONLY after first event?
            // Or we assume OFF initially.
            var isScreenOn = false 
            
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                
                when (event.eventType) {
                    UsageEvents.Event.SCREEN_INTERACTIVE -> {
                        // Screen turned on
                        if (!isScreenOn) {
                            screenOffTime += (event.timeStamp - lastEventTime)
                        }
                        lastEventTime = event.timeStamp
                        isScreenOn = true
                    }
                    UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                        // Screen turned off
                        if (isScreenOn) {
                            screenOnTime += (event.timeStamp - lastEventTime)
                        }
                        lastEventTime = event.timeStamp
                        isScreenOn = false
                    }
                }
            }
            
            // Add remaining time
            val remaining = endTime - lastEventTime
            if (isScreenOn) {
                screenOnTime += remaining
            } else {
                screenOffTime += remaining
            }
            
            // Sanity check: if calculated SOT > 24h (impossible), clamp it.
            if (screenOnTime > 24 * 60 * 60 * 1000L) screenOnTime = 0
            
            return Pair(screenOnTime, screenOffTime)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get screen on/off time: ${e.message}")
            return Pair(0L, 0L)
        }
    }
    
    /**
     * Check if usage stats permission is granted
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return false
        
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (60 * 1000) // Last minute
        
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        
        return stats != null && stats.isNotEmpty()
    }
}
