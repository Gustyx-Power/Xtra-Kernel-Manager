package id.xms.xtrakernelmanager.data.model

import android.graphics.drawable.Drawable

enum class BatteryUsageType {
  APP,
  SYSTEM,
}

data class AppBatteryStats(
    val uid: Int,
    val packageName: String?,
    val appName: String,
    val icon: Drawable? = null,
    val percent: Double,
    val usageType: BatteryUsageType,
)
