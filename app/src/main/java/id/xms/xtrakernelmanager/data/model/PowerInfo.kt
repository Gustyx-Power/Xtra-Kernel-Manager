package id.xms.xtrakernelmanager.data.model

/**
 * Power Insight information
 */
data class PowerInfo(
    val screenOnTime: Long = 0L,           // Screen on time in milliseconds
    val screenOffTime: Long = 0L,          // Screen off time in milliseconds
    val drainRate: Int = 0,                // mA (positive = drain, negative = charge)
    val batteryLevel: Int = 0,             // 0-100%
    val batteryTemp: Float = 0f,           // Celsius
    val batteryVoltage: Float = 0f,        // Volts
    val isCharging: Boolean = false,
    val wakeupCount: Int = 0,              // Number of wakeups
    val suspendCount: Int = 0,             // Number of deep sleep entries
    val deepSleepPercentage: Float = 0f,   // Percentage of time in deep sleep
    val activeDrainRate: Float = 0f,       // %/hour drain while screen on
    val idleDrainRate: Float = 0f          // %/hour drain while screen off
) {
    /**
     * Format screen on time as human readable string
     */
    fun formatScreenOnTime(): String {
        val hours = screenOnTime / (1000 * 60 * 60)
        val minutes = (screenOnTime / (1000 * 60)) % 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }
    
    /**
     * Format screen off time as human readable string
     */
    fun formatScreenOffTime(): String {
        val hours = screenOffTime / (1000 * 60 * 60)
        val minutes = (screenOffTime / (1000 * 60)) % 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }
    
    /**
     * Calculate SOT progress (0.0 - 1.0) based on 8 hour goal
     */
    fun getSotProgress(goalHours: Int = 8): Float {
        val goalMs = goalHours * 60 * 60 * 1000L
        return (screenOnTime.toFloat() / goalMs).coerceIn(0f, 1f)
    }

    fun formatDeepSleepTime(): String {
        val deepSleepTime = (screenOffTime * (deepSleepPercentage / 100)).toLong()
        val hours = deepSleepTime / (1000 * 60 * 60)
        val minutes = (deepSleepTime / (1000 * 60)) % 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }
}
