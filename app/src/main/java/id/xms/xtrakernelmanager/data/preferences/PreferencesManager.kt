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

    // Per-App Profile preferences
    private val APP_PROFILES = stringPreferencesKey("app_profiles")
    private val PER_APP_PROFILE_ENABLED = booleanPreferencesKey("per_app_profile_enabled")
    
    // Game apps list - apps that trigger game overlay (stored as JSON array)
    private val GAME_APPS = stringPreferencesKey("game_apps")

    // Display saturation value (0.5 - 2.0)
    private val DISPLAY_SATURATION = androidx.datastore.preferences.core.floatPreferencesKey("display_saturation")

    // GPU Lock State preferences
    private val GPU_FREQUENCY_LOCKED = booleanPreferencesKey("gpu_frequency_locked")
    private val GPU_LOCKED_MIN_FREQ = intPreferencesKey("gpu_locked_min_freq")
    private val GPU_LOCKED_MAX_FREQ = intPreferencesKey("gpu_locked_max_freq")

    // Holiday celebration preferences
    private val CHRISTMAS_SHOWN_YEAR = intPreferencesKey("christmas_shown_year")
    private val NEW_YEAR_SHOWN_YEAR = intPreferencesKey("new_year_shown_year")
    private val RAMADAN_SHOWN_YEAR = intPreferencesKey("ramadan_shown_year")
    private val EID_FITR_SHOWN_YEAR = intPreferencesKey("eid_fitr_shown_year")

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

    // Per-App Profile Functions
    suspend fun setPerAppProfileEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PER_APP_PROFILE_ENABLED] = enabled
        }
    }

    fun isPerAppProfileEnabled(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[PER_APP_PROFILE_ENABLED] ?: false
        }

    suspend fun saveAppProfiles(profilesJson: String) {
        context.dataStore.edit { prefs ->
            prefs[APP_PROFILES] = profilesJson
        }
    }

    fun getAppProfiles(): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[APP_PROFILES] ?: "[]"
        }

    // GPU Lock State Functions
    suspend fun setGpuLockState(locked: Boolean, minFreq: Int, maxFreq: Int) {
        context.dataStore.edit { prefs ->
            prefs[GPU_FREQUENCY_LOCKED] = locked
            prefs[GPU_LOCKED_MIN_FREQ] = minFreq
            prefs[GPU_LOCKED_MAX_FREQ] = maxFreq
        }
    }

    suspend fun clearGpuLockState() {
        context.dataStore.edit { prefs ->
            prefs[GPU_FREQUENCY_LOCKED] = false
            prefs[GPU_LOCKED_MIN_FREQ] = 0
            prefs[GPU_LOCKED_MAX_FREQ] = 0
        }
    }

    fun isGpuFrequencyLocked(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[GPU_FREQUENCY_LOCKED] ?: false
        }

    fun getGpuLockedMinFreq(): Flow<Int> =
        context.dataStore.data.map { prefs ->
            prefs[GPU_LOCKED_MIN_FREQ] ?: 0
        }

    fun getGpuLockedMaxFreq(): Flow<Int> =
        context.dataStore.data.map { prefs ->
            prefs[GPU_LOCKED_MAX_FREQ] ?: 0
        }
    
    // Game Apps (apps that trigger game overlay)
    suspend fun saveGameApps(jsonString: String) {
        context.dataStore.edit { prefs ->
            prefs[GAME_APPS] = jsonString
        }
    }
    
    fun getGameApps(): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[GAME_APPS] ?: "[]"
        }

    // Display Saturation
    suspend fun setDisplaySaturation(value: Float) {
        context.dataStore.edit { prefs ->
            prefs[DISPLAY_SATURATION] = value
        }
    }

    fun getDisplaySaturation(): Flow<Float> =
        context.dataStore.data.map { prefs ->
            prefs[DISPLAY_SATURATION] ?: 1.0f
        }

    // Holiday Celebration Functions
    suspend fun setChristmasShownYear(year: Int) {
        context.dataStore.edit { prefs ->
            prefs[CHRISTMAS_SHOWN_YEAR] = year
        }
    }

    fun getChristmasShownYear(): Flow<Int> =
        context.dataStore.data.map { prefs ->
            prefs[CHRISTMAS_SHOWN_YEAR] ?: 0
        }

    suspend fun setNewYearShownYear(year: Int) {
        context.dataStore.edit { prefs ->
            prefs[NEW_YEAR_SHOWN_YEAR] = year
        }
    }

    fun getNewYearShownYear(): Flow<Int> =
        context.dataStore.data.map { prefs ->
            prefs[NEW_YEAR_SHOWN_YEAR] ?: 0
        }

    suspend fun setRamadanShownYear(year: Int) {
        context.dataStore.edit { prefs ->
            prefs[RAMADAN_SHOWN_YEAR] = year
        }
    }

    fun getRamadanShownYear(): Flow<Int> =
        context.dataStore.data.map { prefs ->
            prefs[RAMADAN_SHOWN_YEAR] ?: 0
        }

    suspend fun setEidFitrShownYear(year: Int) {
        context.dataStore.edit { prefs ->
            prefs[EID_FITR_SHOWN_YEAR] = year
        }
    }

    fun getEidFitrShownYear(): Flow<Int> =
        context.dataStore.data.map { prefs ->
            prefs[EID_FITR_SHOWN_YEAR] ?: 0
        }

    // ==================== Quick Apps Preferences ====================
    private val QUICK_APPS = stringPreferencesKey("quick_apps")
    
    // Game Tools preferences
    private val ESPORTS_MODE_ENABLED = booleanPreferencesKey("esports_mode_enabled")
    private val TOUCH_GUARD_ENABLED = booleanPreferencesKey("touch_guard_enabled")
    private val AUTO_REJECT_CALLS_ENABLED = booleanPreferencesKey("auto_reject_calls_enabled")
    private val LOCK_BRIGHTNESS_ENABLED = booleanPreferencesKey("lock_brightness_enabled")
    private val QUICK_APPS_INITIALIZED = booleanPreferencesKey("quick_apps_initialized")
    
    /**
     * Get quick apps package names as JSON array string
     */
    fun getQuickApps(): Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            val jsonString = prefs[QUICK_APPS] ?: "[]"
            try {
                val jsonArray = org.json.JSONArray(jsonString)
                (0 until jsonArray.length()).map { jsonArray.getString(it) }
            } catch (e: Exception) {
                emptyList()
            }
        }
    
    /**
     * Set quick apps list
     */
    suspend fun setQuickApps(apps: List<String>) {
        val jsonArray = org.json.JSONArray(apps)
        context.dataStore.edit { prefs ->
            prefs[QUICK_APPS] = jsonArray.toString()
        }
    }
    
    /**
     * Add a quick app
     */
    suspend fun addQuickApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[QUICK_APPS] ?: "[]"
            try {
                val jsonArray = org.json.JSONArray(currentJson)
                // Check if already exists
                val exists = (0 until jsonArray.length()).any { 
                    jsonArray.getString(it) == packageName 
                }
                if (!exists) {
                    jsonArray.put(packageName)
                    prefs[QUICK_APPS] = jsonArray.toString()
                }
            } catch (e: Exception) {
                val newArray = org.json.JSONArray()
                newArray.put(packageName)
                prefs[QUICK_APPS] = newArray.toString()
            }
        }
    }
    
    /**
     * Remove a quick app
     */
    suspend fun removeQuickApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[QUICK_APPS] ?: "[]"
            try {
                val jsonArray = org.json.JSONArray(currentJson)
                val newArray = org.json.JSONArray()
                for (i in 0 until jsonArray.length()) {
                    val pkg = jsonArray.getString(i)
                    if (pkg != packageName) {
                        newArray.put(pkg)
                    }
                }
                prefs[QUICK_APPS] = newArray.toString()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    /**
     * Check if quick apps have been initialized
     */
    fun isQuickAppsInitialized(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[QUICK_APPS_INITIALIZED] ?: false
        }
    
    /**
     * Set quick apps initialized flag
     */
    suspend fun setQuickAppsInitialized(initialized: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[QUICK_APPS_INITIALIZED] = initialized
        }
    }
    
    // ==================== Game Tools Preferences ====================
    
    /**
     * Esports Mode - Maximum optimization for gaming
     */
    suspend fun setEsportsMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ESPORTS_MODE_ENABLED] = enabled
        }
    }
    
    fun isEsportsModeEnabled(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[ESPORTS_MODE_ENABLED] ?: false
        }
    
    /**
     * Touch Guard - Prevent accidental touches
     */
    suspend fun setTouchGuard(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[TOUCH_GUARD_ENABLED] = enabled
        }
    }
    
    fun isTouchGuardEnabled(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[TOUCH_GUARD_ENABLED] ?: false
        }
    
    /**
     * Auto Reject Calls during gaming
     */
    suspend fun setAutoRejectCalls(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_REJECT_CALLS_ENABLED] = enabled
        }
    }
    
    fun isAutoRejectCallsEnabled(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[AUTO_REJECT_CALLS_ENABLED] ?: false
        }
    
    /**
     * Lock Brightness during gaming
     */
    suspend fun setLockBrightness(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[LOCK_BRIGHTNESS_ENABLED] = enabled
        }
    }
    
    fun isLockBrightnessEnabled(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[LOCK_BRIGHTNESS_ENABLED] ?: false
        }
    
    // ==================== SYNC PREFERENCES ====================
    // Using SharedPreferences for simple synchronous access
    
    private val syncPrefs by lazy {
        context.getSharedPreferences("xkm_sync_prefs", Context.MODE_PRIVATE)
    }
    
    /**
     * Get boolean value synchronously
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return syncPrefs.getBoolean(key, defaultValue)
    }
    
    /**
     * Set boolean value synchronously
     */
    fun setBoolean(key: String, value: Boolean) {
        syncPrefs.edit().putBoolean(key, value).apply()
    }
    
    /**
     * Get string value synchronously
     */
    fun getString(key: String, defaultValue: String): String {
        return syncPrefs.getString(key, defaultValue) ?: defaultValue
    }
    
    /**
     * Set string value synchronously
     */
    fun setString(key: String, value: String) {
        syncPrefs.edit().putString(key, value).apply()
    }
}
