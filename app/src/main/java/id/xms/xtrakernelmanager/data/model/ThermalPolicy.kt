package id.xms.xtrakernelmanager.data.model

/**
 * Thermal policy for CPU frequency locking
 */
data class ThermalPolicy(
    val name: String,
    val emergencyThreshold: Float,    // °C - Immediate unlock
    val warningThreshold: Float,      // °C - Cooldown activation
    val restoreThreshold: Float,       // °C - Safe to relock
    val criticalThreshold: Float,     // °C - Emergency action
    val restoreDelay: Long,           // milliseconds
    val warningCooldown: Long,        // milliseconds
    val behavior: ThermalBehavior
)

/**
 * Thermal behavior configuration
 */
data class ThermalBehavior(
    val actionOnEmergency: ThermalAction = ThermalAction.UNLOCK_ALL,
    val actionOnCritical: ThermalAction = ThermalAction.UNLOCK_AND_GOVERNOR_POWERSAVE,
    val autoRestoreEnabled: Boolean = true,
    val maxRetriesPerHour: Int = 3,
    val notifyUser: Boolean = true
)

/**
 * Actions to take on thermal events
 */
enum class ThermalAction {
    NONE,
    UNLOCK_ALL,                    // Restore original frequencies
    UNLOCK_AND_GOVERNOR_POWERSAVE, // Unlock + set powersave governor
    EMERGENCY_SHUTDOWN             // Last resort
}

/**
 * Predefined thermal policies for different use cases
 */
object ThermalPolicyPresets {
    
    /**
     * Policy A (Performance) - Higher thresholds for performance-focused usage
     */
    val POLICY_A_PERFORMANCE = ThermalPolicy(
        name = "Policy A (Performance)",
        emergencyThreshold = 85f,
        warningThreshold = 75f,
        restoreThreshold = 70f,
        criticalThreshold = 90f,
        restoreDelay = 15_000L,        // 15 seconds
        warningCooldown = 5_000L,      // 5 seconds
        behavior = ThermalBehavior(
            actionOnEmergency = ThermalAction.UNLOCK_ALL,
            actionOnCritical = ThermalAction.UNLOCK_AND_GOVERNOR_POWERSAVE,
            autoRestoreEnabled = true,
            maxRetriesPerHour = 5,
            notifyUser = true
        )
    )

    /**
     * Policy B (Balanced) - Default balanced approach (USER SELECTED)
     */
    val POLICY_B_BALANCED = ThermalPolicy(
        name = "Policy B (Balanced)",
        emergencyThreshold = 82f,        // USER SELECTED: 82°C
        warningThreshold = 72f,        // USER SELECTED: 72°C
        restoreThreshold = 68f,        // USER SELECTED: 68°C
        criticalThreshold = 87f,        // USER SELECTED: 87°C
        restoreDelay = 10_000L,        // USER SELECTED: 10 seconds
        warningCooldown = 3_000L,      // USER SELECTED: 3 seconds
        behavior = ThermalBehavior(
            actionOnEmergency = ThermalAction.UNLOCK_ALL,
            actionOnCritical = ThermalAction.UNLOCK_AND_GOVERNOR_POWERSAVE,
            autoRestoreEnabled = true,
            maxRetriesPerHour = 3,
            notifyUser = true
        )
    )

    /**
     * Policy C (Conservative) - Lower thresholds for safety-focused usage
     */
    val POLICY_C_CONSERVATIVE = ThermalPolicy(
        name = "Policy C (Conservative)",
        emergencyThreshold = 78f,
        warningThreshold = 68f,
        restoreThreshold = 65f,
        criticalThreshold = 85f,
        restoreDelay = 8_000L,         // 8 seconds
        warningCooldown = 2_000L,      // 2 seconds
        behavior = ThermalBehavior(
            actionOnEmergency = ThermalAction.UNLOCK_AND_GOVERNOR_POWERSAVE,
            actionOnCritical = ThermalAction.UNLOCK_AND_GOVERNOR_POWERSAVE,
            autoRestoreEnabled = true,
            maxRetriesPerHour = 2,
            notifyUser = true
        )
    )

    /**
     * Custom policy for gaming with higher thermal tolerance
     */
    val POLICY_GAMING = ThermalPolicy(
        name = "Gaming Mode",
        emergencyThreshold = 86f,
        warningThreshold = 76f,
        restoreThreshold = 72f,
        criticalThreshold = 92f,
        restoreDelay = 20_000L,        // 20 seconds
        warningCooldown = 4_000L,      // 4 seconds
        behavior = ThermalBehavior(
            actionOnEmergency = ThermalAction.UNLOCK_ALL,
            actionOnCritical = ThermalAction.UNLOCK_AND_GOVERNOR_POWERSAVE,
            autoRestoreEnabled = true,
            maxRetriesPerHour = 8,     // More retries for gaming
            notifyUser = true
        )
    )

    /**
     * Custom policy for battery saving with lower thresholds
     */
    val POLICY_BATTERY_SAVER = ThermalPolicy(
        name = "Battery Saver",
        emergencyThreshold = 75f,
        warningThreshold = 65f,
        restoreThreshold = 62f,
        criticalThreshold = 80f,
        restoreDelay = 5_000L,         // 5 seconds
        warningCooldown = 1_500L,      // 1.5 seconds
        behavior = ThermalBehavior(
            actionOnEmergency = ThermalAction.UNLOCK_AND_GOVERNOR_POWERSAVE,
            actionOnCritical = ThermalAction.UNLOCK_AND_GOVERNOR_POWERSAVE,
            autoRestoreEnabled = false,  // Don't auto-restore for battery saver
            maxRetriesPerHour = 1,
            notifyUser = true
        )
    )

    /**
     * Get all available policies
     */
    fun getAllPolicies(): List<ThermalPolicy> = listOf(
        POLICY_A_PERFORMANCE,
        POLICY_B_BALANCED,
        POLICY_C_CONSERVATIVE,
        POLICY_GAMING,
        POLICY_BATTERY_SAVER
    )

    /**
     * Get policy by name
     */
    fun getPolicyByName(name: String): ThermalPolicy? {
        return getAllPolicies().find { it.name == name }
    }

    /**
     * Get recommended policy based on lock type
     */
    fun getRecommendedPolicy(lockPolicyType: LockPolicyType): ThermalPolicy {
        return when (lockPolicyType) {
            LockPolicyType.MANUAL -> POLICY_B_BALANCED  // Default for manual
            LockPolicyType.SMART -> POLICY_B_BALANCED   // Smart uses balanced
            LockPolicyType.GAME -> POLICY_GAMING        // Gaming optimized
            LockPolicyType.BATTERY_SAVING -> POLICY_BATTERY_SAVER  // Battery optimized
        }
    }

    /**
     * Validate temperature thresholds are logical
     */
    fun isValidPolicy(policy: ThermalPolicy): Boolean {
        return policy.criticalThreshold > policy.emergencyThreshold &&
               policy.emergencyThreshold > policy.warningThreshold &&
               policy.warningThreshold > policy.restoreThreshold &&
               policy.restoreDelay > 0 &&
               policy.warningCooldown > 0 &&
               policy.behavior.maxRetriesPerHour > 0
    }
}

/**
 * Thermal state helper
 */
data class ThermalState(
    val currentTemperature: Float,
    val policy: ThermalPolicy,
    val isOverrideActive: Boolean = false,
    val lastEvent: ThermalEvent? = null
) {
    val severity: ThermalSeverity
        get() = when {
            currentTemperature >= policy.criticalThreshold -> ThermalSeverity.CRITICAL
            currentTemperature >= policy.emergencyThreshold -> ThermalSeverity.EMERGENCY
            currentTemperature >= policy.warningThreshold -> ThermalSeverity.WARNING
            else -> ThermalSeverity.NORMAL
        }

    val needsAction: Boolean
        get() = severity != ThermalSeverity.NORMAL

    val canRestore: Boolean
        get() = currentTemperature <= policy.restoreThreshold && !isOverrideActive
}

/**
 * Thermal severity levels
 */
enum class ThermalSeverity {
    NORMAL,      // Below warning threshold
    WARNING,     // Above warning threshold
    EMERGENCY,   // Above emergency threshold
    CRITICAL     // Above critical threshold
}