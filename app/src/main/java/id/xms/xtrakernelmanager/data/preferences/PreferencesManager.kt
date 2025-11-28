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

    // Performance mode
    private val PERF_MODE = stringPreferencesKey("perf_mode")

    // CPU core enable/disable keys
    private fun cpuCoreKey(core: Int) = booleanPreferencesKey("cpu_core_$core")

    // Thermal configuration keys
    private val THERMAL_PRESET = stringPreferencesKey("thermal_preset")
    private val THERMAL_SET_ON_BOOT = booleanPreferencesKey("thermal_set_on_boot")

    // I/O scheduler key
    private val IO_SCHEDULER = stringPreferencesKey("io_scheduler")

    // TCP congestion control key
    private val TCP_CONGESTION = stringPreferencesKey("tcp_congestion")

    // RAM configuration keys
    private val RAM_SWAPPINESS = intPreferencesKey("ram_swappiness")
    private val RAM_ZRAM_SIZE = intPreferencesKey("ram_zram_size")
    private val RAM_SWAP_SIZE = intPreferencesKey("ram_swap_size")
    private val RAM_DIRTY_RATIO = intPreferencesKey("ram_dirty_ratio")
    private val RAM_MIN_FREE_MEM = intPreferencesKey("ram_min_free_mem")

    // Misc features
    private val SHOW_BATTERY_NOTIF = booleanPreferencesKey("show_battery_notif")
    private val ENABLE_GAME_OVERLAY = booleanPreferencesKey("enable_game_overlay")

    // Game Control preferences
    private val GAME_CONTROL_DND_ENABLED = booleanPreferencesKey("game_control_dnd_enabled")
    private val GAME_CONTROL_HIDE_NOTIF = booleanPreferencesKey("game_control_hide_notif")

    val themeMode: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: 0
    }

    val setOnBoot: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SET_ON_BOOT] ?: false
    }

    suspend fun setPerfMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[PERF_MODE] = mode
        }
    }

    fun getPerfMode(): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[PERF_MODE] ?: "balanced"
        }

    suspend fun setCpuCoreEnabled(core: Int, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[cpuCoreKey(core)] = enabled
        }
    }

    fun isCpuCoreEnabled(core: Int): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[cpuCoreKey(core)] ?: true
        }

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

    suspend fun setIOScheduler(scheduler: String) {
        context.dataStore.edit { prefs ->
            prefs[IO_SCHEDULER] = scheduler
        }
    }

    fun getIOScheduler(): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[IO_SCHEDULER] ?: ""
        }

    suspend fun setTCPCongestion(congestion: String) {
        context.dataStore.edit { prefs ->
            prefs[TCP_CONGESTION] = congestion
        }
    }

    fun getTCPCongestion(): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[TCP_CONGESTION] ?: ""
        }

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

    suspend fun setShowBatteryNotif(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SHOW_BATTERY_NOTIF] = enabled
        }
    }

    fun isShowBatteryNotif(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[SHOW_BATTERY_NOTIF] ?: false
        }

    suspend fun setEnableGameOverlay(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ENABLE_GAME_OVERLAY] = enabled
        }
    }

    fun isEnableGameOverlay(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[ENABLE_GAME_OVERLAY] ?: false
        }

    // Game Control DND
    suspend fun setGameControlDND(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[GAME_CONTROL_DND_ENABLED] = enabled
        }
    }

    fun isGameControlDNDEnabled(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[GAME_CONTROL_DND_ENABLED] ?: false
        }

    // Game Control Hide Notifications
    suspend fun setGameControlHideNotif(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[GAME_CONTROL_HIDE_NOTIF] = enabled
        }
    }

    fun isGameControlHideNotifEnabled(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[GAME_CONTROL_HIDE_NOTIF] ?: false
        }

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
