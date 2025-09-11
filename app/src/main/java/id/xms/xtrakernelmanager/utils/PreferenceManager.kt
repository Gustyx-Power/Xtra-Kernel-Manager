package id.xms.xtrakernelmanager.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("xkm_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BATTERY_STATS_ENABLED = "battery_stats_enabled"
        private const val KEY_BATTERY_NOTIFICATION_AUTO_START = "battery_notification_auto_start"
        private const val KEY_SERVICE_AUTO_START = "service_auto_start"
        private const val KEY_TARGET_GAME_PACKAGES = "target_game_packages"
        private const val KEY_KGSL_SKIP_ZEROING = "kgsl_skip_zeroing"
    }

    fun setBatteryStatsEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_BATTERY_STATS_ENABLED, enabled)
            .putBoolean(KEY_BATTERY_NOTIFICATION_AUTO_START, enabled) // Enable auto-start when battery stats is enabled
            .apply()
    }

    fun getBatteryStatsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BATTERY_STATS_ENABLED, false)
    }

    fun setBatteryNotificationAutoStart(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_BATTERY_NOTIFICATION_AUTO_START, enabled)
            .apply()
    }

    fun getBatteryNotificationAutoStart(): Boolean {
        return sharedPreferences.getBoolean(KEY_BATTERY_NOTIFICATION_AUTO_START, true) // Default to true for auto-start
    }

    fun setServiceAutoStart(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_SERVICE_AUTO_START, enabled)
            .apply()
    }

    fun getServiceAutoStart(): Boolean {
        return sharedPreferences.getBoolean(KEY_SERVICE_AUTO_START, true)
    }

    fun setTargetGamePackages(packages: Set<String>) {
        sharedPreferences.edit()
            .putStringSet(KEY_TARGET_GAME_PACKAGES, packages)
            .apply()
    }

    fun getTargetGamePackages(): Set<String> {
        return sharedPreferences.getStringSet(KEY_TARGET_GAME_PACKAGES, emptySet()) ?: emptySet()
    }

    fun setKgslSkipZeroing(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_KGSL_SKIP_ZEROING, enabled)
            .apply()
    }

    fun getKgslSkipZeroing(): Boolean {
        return sharedPreferences.getBoolean(KEY_KGSL_SKIP_ZEROING, false)
    }
}
