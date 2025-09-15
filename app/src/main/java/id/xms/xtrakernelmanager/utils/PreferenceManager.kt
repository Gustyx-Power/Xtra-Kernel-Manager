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
        private const val KEY_TARGET_GAME_PACKAGES = "target_game_packages"
        private const val KEY_KGSL_SKIP_ZEROING = "kgsl_skip_zeroing"
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
