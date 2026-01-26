package id.xms.xtrakernelmanager.data.model

import kotlinx.serialization.Serializable

/**
 * Represents state of CPU frequency locking across all clusters
 */
@Serializable
data class CPULockState(
    val isLocked: Boolean = false,
    val clusterConfigs: Map<Int, CpuClusterLockConfig> = emptyMap(),
    val policyType: LockPolicyType = LockPolicyType.MANUAL,
    val thermalPolicy: String = "PolicyB",
    val lastTemperature: Float = 0f,
    val lastUpdate: Long = System.currentTimeMillis(),
    val isThermalOverrideActive: Boolean = false,
    val originalFrequencies: Map<Int, OriginalFreqConfig> = emptyMap(),
    val retryCount: Int = 0,
    val lastRetryTime: Long = 0L
)

/**
 * Configuration for a specific CPU cluster lock
 */
@Serializable
data class CpuClusterLockConfig(
    val clusterId: Int,
    val minFreq: Int,
    val maxFreq: Int,
    val isTemporarilyUnlocked: Boolean = false,
    val unlockReason: String? = null,
    val unlockExpiry: Long? = null,
    val lastApplied: Long = System.currentTimeMillis()
)

/**
 * Original frequency configuration before locking
 */
@Serializable
data class OriginalFreqConfig(
    val minFreq: Int,
    val maxFreq: Int,
    val governor: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Types of lock policies available
 */
enum class LockPolicyType {
    MANUAL,           // User-controlled, no thermal override
    SMART,            // Thermal-aware with smart overrides
    GAME,             // Game mode optimized
    BATTERY_SAVING    // Power-efficient
}

/**
 * Thermal event types for smart locking
 */
enum class ThermalEventType {
    WARNING,             // Approaching threshold
    EMERGENCY,           // Emergency unlock required
    CRITICAL,            // Critical temperature
    RESTORE_SAFE,        // Safe to restore lock
    RESTORE_FAILED       // Restore attempt failed
}

/**
 * Thermal event data
 */
@Serializable
data class ThermalEvent(
    val type: ThermalEventType,
    val temperature: Float,
    val policy: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val affectedClusters: List<Int> = emptyList()
)

/**
 * Lock status information for UI
 */
data class LockStatus(
    val isLocked: Boolean,
    val policyType: LockPolicyType,
    val thermalPolicy: String,
    val isThermalOverrideActive: Boolean,
    val lastTemperature: Float,
    val lastUpdate: Long,
    val clusterCount: Int,
    val lockedClusters: List<Int>,
    val retryCount: Int,
    val canRetry: Boolean
) {
    val isHealthy: Boolean
        get() = !isThermalOverrideActive && lastTemperature < 70f
    
    val needsAttention: Boolean
        get() = isThermalOverrideActive || lastTemperature > 75f
}

/**
 * Smart lock operation result
 */
sealed class SmartLockResult {
    object Success : SmartLockResult()
    data class SuccessWithWarning(val message: String) : SmartLockResult()
    data class Error(val message: String, val throwable: Throwable? = null) : SmartLockResult()
    data class ThermalOverrideActivated(val temperature: Float, val policy: String) : SmartLockResult()
    object AlreadyLocked : SmartLockResult()
    object NotLocked : SmartLockResult()
    data class PartialSuccess(val successClusters: List<Int>, val failedClusters: List<Int>) : SmartLockResult()
    data class RetryExceeded(val retryCount: Int, val nextRetryTime: Long) : SmartLockResult()
}

/**
 * Frequency range helper
 */
data class FrequencyRange(
    val min: Int,
    val max: Int
) {
    val isValid: Boolean
        get() = min > 0 && max > 0 && max >= min
    
    val size: Int
        get() = max - min
    
    fun contains(freq: Int): Boolean = freq in min..max
    
    override fun toString(): String = "${min}MHz - ${max}MHz"
}