package id.xms.xtrakernelmanager.data.model

import kotlinx.serialization.Serializable

/**
 * Model for per-app profile configuration
 */
@Serializable
data class AppProfile(
    val packageName: String,
    val appName: String,
    val governor: String = "schedutil",
    val thermalPreset: String = "Not Set",
    val refreshRate: Int = 0, // 0 = Not Set, 60 = 60Hz, 90 = 90Hz, 120 = 120Hz
    val enabled: Boolean = true
)

/**
 * Container for all app profiles
 */
@Serializable
data class AppProfilesContainer(
    val profiles: List<AppProfile> = emptyList()
)
