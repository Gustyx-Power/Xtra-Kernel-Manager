package id.xms.xtrakernelmanager.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import id.xms.xtrakernelmanager.data.model.RAMConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "xtra_settings")

class PreferencesManager(private val context: Context) {

    // Theme and boot preference
    private val THEME_MODE = intPreferencesKey("theme_mode")
    private val SET_ON_BOOT = booleanPreferencesKey("set_on_boot")

    // CPU core enable/disable keys
    private fun cpuCoreKey(core: Int) = booleanPreferencesKey("cpu_core_$core")

    // Thermal configuration keys
    private val THERMAL_PRESET = stringPreferencesKey("thermal_preset")
    private val THERMAL_SET_ON_BOOT = booleanPreferencesKey("thermal_set_on_boot")

    // I/O scheduler key
    private val IO_SCHEDULER = stringPreferencesKey("io_scheduler")

    // TCP congestion control key
    private val TCP_CONGESTION = stringPreferencesKey("tcp_congestion")

    // Performance mode key
    private val PERF_MODE = stringPreferencesKey("perf_mode")

    // Miscellaneous - Battery Notification & Game Overlay keys
    private val SHOW_BATTERY_NOTIF = booleanPreferencesKey("show_battery_notif")
    private val ENABLE_GAME_OVERLAY = booleanPreferencesKey("enable_game_overlay")

    // RAM configuration keys
    private val RAM_SWAPPINESS = intPreferencesKey("ram_swappiness")
    private val RAM_ZRAM_SIZE = intPreferencesKey("ram_zram_size")   // MB
    private val RAM_SWAP_SIZE = intPreferencesKey("ram_swap_size")   // MB
    private val RAM_DIRTY_RATIO = intPreferencesKey("ram_dirty_ratio")
    private val RAM_MIN_FREE_MEM = intPreferencesKey("ram_min_free_mem") // kB

    // Flow for basic preferences
    val themeMode: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: 0
    }

    val setOnBoot: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SET_ON_BOOT] ?: false
    }

    // CPU core enable/disable preferences
    suspend fun setCpuCoreEnabled(core: Int, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[cpuCoreKey(core)] = enabled
        }
    }

    fun isCpuCoreEnabled(core: Int): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[cpuCoreKey(core)] ?: true
        }

    // Thermal configuration
    suspend fun setThermalConfig(preset: String, setOnBoot: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[THERMAL_PRESET] = preset
            prefs[THERMAL_SET_ON_BOOT] = setOnBoot
        }
    }

    fun getThermalPreset(): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[THERMAL_PRESET] ?: ""
        }

    fun getThermalSetOnBoot(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[THERMAL_SET_ON_BOOT] ?: false
        }

    // I/O scheduler
    suspend fun setIOScheduler(scheduler: String) {
        context.dataStore.edit { prefs ->
            prefs[IO_SCHEDULER] = scheduler
        }
    }

    fun getIOScheduler(): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[IO_SCHEDULER] ?: ""
        }

    // TCP congestion control
    suspend fun setTCPCongestion(congestion: String) {
        context.dataStore.edit { prefs ->
            prefs[TCP_CONGESTION] = congestion
        }
    }

    fun getTCPCongestion(): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[TCP_CONGESTION] ?: ""
        }

    // Performance Mode
    suspend fun setPerfMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[PERF_MODE] = mode
        }
    }

    fun getPerfMode(): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[PERF_MODE] ?: "balance"  // Default: balance
        }

    // Miscellaneous ===> Battery Info Notification
    suspend fun setShowBatteryNotif(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SHOW_BATTERY_NOTIF] = enabled
        }
    }
    fun isShowBatteryNotif(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[SHOW_BATTERY_NOTIF] ?: false
        }

    // Miscellaneous ===> Game Control Overlay
    suspend fun setEnableGameOverlay(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ENABLE_GAME_OVERLAY] = enabled
        }
    }
    fun isEnableGameOverlay(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[ENABLE_GAME_OVERLAY] ?: false
        }

    // RAM configuration: simpan & baca RAMConfig
    suspend fun setRamConfig(config: RAMConfig) {
        context.dataStore.edit { prefs ->
            prefs[RAM_SWAPPINESS] = config.swappiness
            prefs[RAM_ZRAM_SIZE] = config.zramSize
            prefs[RAM_SWAP_SIZE] = config.swapSize
            prefs[RAM_DIRTY_RATIO] = config.dirtyRatio
            prefs[RAM_MIN_FREE_MEM] = config.minFreeMem
        }
    }

    fun getRamConfig(): Flow<RAMConfig> =
        context.dataStore.data.map { prefs ->
            RAMConfig(
                swappiness = prefs[RAM_SWAPPINESS] ?: 60,
                zramSize = prefs[RAM_ZRAM_SIZE] ?: 0,
                swapSize = prefs[RAM_SWAP_SIZE] ?: 0,
                dirtyRatio = prefs[RAM_DIRTY_RATIO] ?: 20,
                minFreeMem = prefs[RAM_MIN_FREE_MEM] ?: 0
            )
        }

    // Additional helpers for theme and boot, if needed
    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
        }
    }

    suspend fun setSetOnBoot(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SET_ON_BOOT] = enabled
        }
    }
}
