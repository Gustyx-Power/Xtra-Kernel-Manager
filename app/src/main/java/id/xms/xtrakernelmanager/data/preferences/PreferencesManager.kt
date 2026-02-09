package id.xms.xtrakernelmanager.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import id.xms.xtrakernelmanager.data.model.CPULockState
import id.xms.xtrakernelmanager.data.model.CpuClusterLockConfig
import id.xms.xtrakernelmanager.data.model.LockPolicyType
import id.xms.xtrakernelmanager.data.model.OriginalFreqConfig
import id.xms.xtrakernelmanager.data.model.RAMConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by
    preferencesDataStore(name = "xtra_settings")

class PreferencesManager(private val context: Context) {
  
  // Expose context for use cases that need it
  fun getContext(): Context = context

  // DPI Mode Functions
  suspend fun setDPIMode(mode: String) {
    context.dataStore.edit { prefs -> prefs[DPI_MODE] = mode }
  }

  fun getDPIMode(): Flow<String> =
      context.dataStore.data.map { prefs -> prefs[DPI_MODE] ?: "SMART" }

  // Theme and boot preference
  private val THEME_MODE = intPreferencesKey("theme_mode")
  private val SET_ON_BOOT = booleanPreferencesKey("set_on_boot")
  
  // DPI Mode preference
  private val DPI_MODE = stringPreferencesKey("dpi_mode")

  // Performance mode
  private val PERF_MODE = stringPreferencesKey("perf_mode")

  // CPU core enable/disable keys
  private fun cpuCoreKey(core: Int) = booleanPreferencesKey("cpu_core_$core")

  // CPU cluster configuration keys
  private fun clusterMinFreqKey(cluster: Int) = intPreferencesKey("cluster_${cluster}_min_freq")
  private fun clusterMaxFreqKey(cluster: Int) = intPreferencesKey("cluster_${cluster}_max_freq")
  private fun clusterGovernorKey(cluster: Int) = stringPreferencesKey("cluster_${cluster}_governor")

  // Thermal configuration keys
  private val THERMAL_PRESET = stringPreferencesKey("thermal_preset")
  private val THERMAL_SET_ON_BOOT = booleanPreferencesKey("thermal_set_on_boot")

  // CPU Set on Boot
  private val CPU_SET_ON_BOOT = booleanPreferencesKey("cpu_set_on_boot")

  // I/O scheduler key
  private val IO_SCHEDULER = stringPreferencesKey("io_scheduler")
  private val IO_SET_ON_BOOT = booleanPreferencesKey("io_set_on_boot")

  // TCP congestion control key
  private val TCP_CONGESTION = stringPreferencesKey("tcp_congestion")
  private val TCP_SET_ON_BOOT = booleanPreferencesKey("tcp_set_on_boot")

  // RAM configuration keys
  private val RAM_SWAPPINESS = intPreferencesKey("ram_swappiness")
  private val RAM_ZRAM_SIZE = intPreferencesKey("ram_zram_size")
  private val RAM_SWAP_SIZE = intPreferencesKey("ram_swap_size")
  private val RAM_DIRTY_RATIO = intPreferencesKey("ram_dirty_ratio")
  private val RAM_MIN_FREE_MEM = intPreferencesKey("ram_min_free_mem")
  private val RAM_SET_ON_BOOT = booleanPreferencesKey("ram_set_on_boot")

  // Additional/Network configuration keys
  private val ADDITIONAL_SET_ON_BOOT = booleanPreferencesKey("additional_set_on_boot")

  // Misc features
  private val SHOW_BATTERY_NOTIF = booleanPreferencesKey("show_battery_notif")
  private val ENABLE_GAME_OVERLAY = booleanPreferencesKey("enable_game_overlay")

  // Game Control preferences
  private val IS_SETUP_COMPLETE = booleanPreferencesKey("is_setup_complete")

  val isSetupComplete: Flow<Boolean> =
      context.dataStore.data.map { preferences -> preferences[IS_SETUP_COMPLETE] ?: false }

  suspend fun setSetupComplete(complete: Boolean) {
    context.dataStore.edit { preferences -> preferences[IS_SETUP_COMPLETE] = complete }
  }

  private val GAME_CONTROL_DND_ENABLED = booleanPreferencesKey("game_control_dnd_enabled")
  private val GAME_CONTROL_HIDE_NOTIF = booleanPreferencesKey("game_control_hide_notif")

  // Functional ROM preferences
  private val FUNCTIONAL_ROM_UNLOCK_NITS = booleanPreferencesKey("functional_rom_unlock_nits")
  private val FUNCTIONAL_ROM_DYNAMIC_REFRESH =
      booleanPreferencesKey("functional_rom_dynamic_refresh")
  private val FUNCTIONAL_ROM_FORCE_REFRESH = booleanPreferencesKey("functional_rom_force_refresh")
  private val FUNCTIONAL_ROM_FORCE_REFRESH_VALUE =
      intPreferencesKey("functional_rom_force_refresh_value")
  private val FUNCTIONAL_ROM_DC_DIMMING = booleanPreferencesKey("functional_rom_dc_dimming")
  private val FUNCTIONAL_ROM_PERFORMANCE_MODE =
      booleanPreferencesKey("functional_rom_performance_mode")
  private val FUNCTIONAL_ROM_SMART_CHARGING = booleanPreferencesKey("functional_rom_smart_charging")
  private val FUNCTIONAL_ROM_CHARGING_LIMIT = booleanPreferencesKey("functional_rom_charging_limit")
  private val FUNCTIONAL_ROM_CHARGING_LIMIT_VALUE =
      intPreferencesKey("functional_rom_charging_limit_value")

  // Per-App Profile preferences
  private val APP_PROFILES = stringPreferencesKey("app_profiles")
  private val PER_APP_PROFILE_ENABLED = booleanPreferencesKey("per_app_profile_enabled")

  // Game apps list - apps that trigger game overlay (stored as JSON array)
  private val GAME_APPS = stringPreferencesKey("game_apps")

  // Display saturation value (0.5 - 2.0)
  private val DISPLAY_SATURATION =
      androidx.datastore.preferences.core.floatPreferencesKey("display_saturation")

  // Layout style (liquid = glassmorphic, material = pure M3)
  private val LAYOUT_STYLE = stringPreferencesKey("layout_style")
  
  // Layout switching loading state
  private val LAYOUT_SWITCHING = booleanPreferencesKey("layout_switching")

  // GPU Lock State preferences
  private val GPU_FREQUENCY_LOCKED = booleanPreferencesKey("gpu_frequency_locked")
  private val GPU_LOCKED_MIN_FREQ = intPreferencesKey("gpu_locked_min_freq")
  private val GPU_LOCKED_MAX_FREQ = intPreferencesKey("gpu_locked_max_freq")

  // CPU Lock State preferences
  private val CPU_FREQUENCY_LOCKED = booleanPreferencesKey("cpu_frequency_locked")
  private val CPU_LOCKED_CLUSTERS = stringPreferencesKey("cpu_locked_clusters") // JSON
  private val CPU_LOCK_POLICY_TYPE = stringPreferencesKey("cpu_lock_policy_type")
  private val CPU_LOCK_THERMAL_POLICY = stringPreferencesKey("cpu_lock_thermal_policy")
  private val CPU_LOCK_ORIGINAL_FREQS = stringPreferencesKey("cpu_lock_original_freqs") // JSON
  private val CPU_LOCK_THERMAL_OVERRIDE = booleanPreferencesKey("cpu_lock_thermal_override")
  private val CPU_LOCK_LAST_UPDATE = longPreferencesKey("cpu_lock_last_update")
  private val CPU_LOCK_RETRY_COUNT = intPreferencesKey("cpu_lock_retry_count")
  private val CPU_LOCK_LAST_RETRY_TIME = longPreferencesKey("cpu_lock_last_retry_time")
  private val CPU_LOCK_LAST_TEMPERATURE = floatPreferencesKey("cpu_lock_last_temperature")

  // Holiday celebration preferences
  private val CHRISTMAS_SHOWN_YEAR = intPreferencesKey("christmas_shown_year")
  private val NEW_YEAR_SHOWN_YEAR = intPreferencesKey("new_year_shown_year")
  private val RAMADAN_SHOWN_YEAR = intPreferencesKey("ramadan_shown_year")
  private val EID_FITR_SHOWN_YEAR = intPreferencesKey("eid_fitr_shown_year")

  // Notification Settings
  private val BATTERY_NOTIF_ICON_TYPE =
      stringPreferencesKey("battery_notif_icon_type") // icon_only, circle_percent, etc.
  private val BATTERY_NOTIF_REFRESH_RATE =
      intPreferencesKey("battery_notif_refresh_rate") // Seconds
  private val BATTERY_NOTIF_SECURE_LOCK_SCREEN =
      booleanPreferencesKey("battery_notif_secure_lock_screen")
  private val BATTERY_NOTIF_HIGH_PRIORITY = booleanPreferencesKey("battery_notif_high_priority")
  private val BATTERY_NOTIF_FORCE_ON_TOP = booleanPreferencesKey("battery_notif_force_on_top")
  private val BATTERY_NOTIF_DONT_UPDATE_SCREEN_OFF =
      booleanPreferencesKey("battery_notif_dont_update_screen_off")

  // Statistics Settings
  private val BATTERY_STATS_ACTIVE_IDLE = booleanPreferencesKey("battery_stats_active_idle")
  private val BATTERY_STATS_SCREEN = booleanPreferencesKey("battery_stats_screen")
  private val BATTERY_STATS_AWAKE_SLEEP = booleanPreferencesKey("battery_stats_awake_sleep")

  val themeMode: Flow<Int> = context.dataStore.data.map { prefs -> prefs[THEME_MODE] ?: 0 }

  val setOnBoot: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[SET_ON_BOOT] ?: false }

  suspend fun setPerfMode(mode: String) {
    context.dataStore.edit { prefs -> prefs[PERF_MODE] = mode }
  }

  fun getPerfMode(): Flow<String> =
      context.dataStore.data.map { prefs -> prefs[PERF_MODE] ?: "balanced" }

  suspend fun setCpuCoreEnabled(core: Int, enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[cpuCoreKey(core)] = enabled }
  }

  fun isCpuCoreEnabled(core: Int): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[cpuCoreKey(core)] ?: true }

  suspend fun setThermalConfig(preset: String, setOnBoot: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[THERMAL_PRESET] = preset
      prefs[THERMAL_SET_ON_BOOT] = setOnBoot
    }
  }

  fun getThermalPreset(): Flow<String> =
      context.dataStore.data.map { prefs -> prefs[THERMAL_PRESET] ?: "" }

  fun getThermalSetOnBoot(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[THERMAL_SET_ON_BOOT] ?: false }

  // CPU Set on Boot
  suspend fun setCpuSetOnBoot(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[CPU_SET_ON_BOOT] = enabled }
  }

  fun getCpuSetOnBoot(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[CPU_SET_ON_BOOT] ?: false }

  suspend fun setIOScheduler(scheduler: String) {
    context.dataStore.edit { prefs -> prefs[IO_SCHEDULER] = scheduler }
  }

  fun getIOScheduler(): Flow<String> =
      context.dataStore.data.map { prefs -> prefs[IO_SCHEDULER] ?: "" }

  suspend fun setTCPCongestion(congestion: String) {
    context.dataStore.edit { prefs -> prefs[TCP_CONGESTION] = congestion }
  }

  fun getTCPCongestion(): Flow<String> =
      context.dataStore.data.map { prefs -> prefs[TCP_CONGESTION] ?: "" }

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
            minFreeMem = prefs[RAM_MIN_FREE_MEM] ?: 0,
        )
      }

  suspend fun setShowBatteryNotif(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[SHOW_BATTERY_NOTIF] = enabled }
  }

  fun isShowBatteryNotif(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[SHOW_BATTERY_NOTIF] ?: false }

  suspend fun setEnableGameOverlay(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[ENABLE_GAME_OVERLAY] = enabled }
  }

  fun isEnableGameOverlay(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[ENABLE_GAME_OVERLAY] ?: false }

  // Game Control DND
  suspend fun setGameControlDND(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[GAME_CONTROL_DND_ENABLED] = enabled }
  }

  fun isGameControlDNDEnabled(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[GAME_CONTROL_DND_ENABLED] ?: false }

  // Game Control Hide Notifications
  suspend fun setGameControlHideNotif(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[GAME_CONTROL_HIDE_NOTIF] = enabled }
  }

  fun isGameControlHideNotifEnabled(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[GAME_CONTROL_HIDE_NOTIF] ?: false }

  suspend fun setThemeMode(mode: Int) {
    context.dataStore.edit { prefs -> prefs[THEME_MODE] = mode }
  }

  suspend fun setSetOnBoot(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[SET_ON_BOOT] = enabled }
  }

  // Per-App Profile Functions
  suspend fun setPerAppProfileEnabled(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[PER_APP_PROFILE_ENABLED] = enabled }
  }

  fun isPerAppProfileEnabled(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[PER_APP_PROFILE_ENABLED] ?: false }

  suspend fun saveAppProfiles(profilesJson: String) {
    context.dataStore.edit { prefs -> prefs[APP_PROFILES] = profilesJson }
  }

  fun getAppProfiles(): Flow<String> =
      context.dataStore.data.map { prefs -> prefs[APP_PROFILES] ?: "[]" }

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
      context.dataStore.data.map { prefs -> prefs[GPU_FREQUENCY_LOCKED] ?: false }

  fun getGpuLockedMinFreq(): Flow<Int> =
      context.dataStore.data.map { prefs -> prefs[GPU_LOCKED_MIN_FREQ] ?: 0 }

  fun getGpuLockedMaxFreq(): Flow<Int> =
      context.dataStore.data.map { prefs -> prefs[GPU_LOCKED_MAX_FREQ] ?: 0 }

  // CPU Lock State Functions
  suspend fun setCpuLockState(
      locked: Boolean,
      clusterConfigs: Map<Int, CpuClusterLockConfig>,
      policyType: LockPolicyType,
      thermalPolicy: String,
      originalFreqs: Map<Int, OriginalFreqConfig>
  ) {
    context.dataStore.edit { prefs ->
      prefs[CPU_FREQUENCY_LOCKED] = locked
      prefs[CPU_LOCKED_CLUSTERS] = Json.encodeToString(clusterConfigs)
      prefs[CPU_LOCK_POLICY_TYPE] = policyType.name
      prefs[CPU_LOCK_THERMAL_POLICY] = thermalPolicy
      prefs[CPU_LOCK_ORIGINAL_FREQS] = Json.encodeToString(originalFreqs)
      prefs[CPU_LOCK_LAST_UPDATE] = System.currentTimeMillis()
      prefs[CPU_LOCK_THERMAL_OVERRIDE] = false
    }
  }

  suspend fun clearCpuLockState() {
    context.dataStore.edit { prefs ->
      prefs[CPU_FREQUENCY_LOCKED] = false
      prefs[CPU_LOCKED_CLUSTERS] = "{}"
      prefs[CPU_LOCK_POLICY_TYPE] = LockPolicyType.MANUAL.name
      prefs[CPU_LOCK_THERMAL_POLICY] = "PolicyB"
      prefs[CPU_LOCK_ORIGINAL_FREQS] = "{}"
      prefs[CPU_LOCK_THERMAL_OVERRIDE] = false
      prefs[CPU_LOCK_RETRY_COUNT] = 0
      prefs[CPU_LOCK_LAST_RETRY_TIME] = 0L
      prefs[CPU_LOCK_LAST_UPDATE] = 0L
      prefs[CPU_LOCK_LAST_TEMPERATURE] = 0f
    }
  }

  suspend fun updateCpuLockThermalOverride(overrideActive: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[CPU_LOCK_THERMAL_OVERRIDE] = overrideActive
    }
  }

  suspend fun updateCpuLockTemperature(temperature: Float) {
    context.dataStore.edit { prefs ->
      prefs[CPU_LOCK_LAST_TEMPERATURE] = temperature
    }
  }

  suspend fun incrementCpuLockRetry() {
    context.dataStore.edit { prefs ->
      val currentCount = prefs[CPU_LOCK_RETRY_COUNT] ?: 0
      val lastRetryTime = prefs[CPU_LOCK_LAST_RETRY_TIME] ?: 0L
      val now = System.currentTimeMillis()
      
      // Reset count if more than an hour has passed
      if (now - lastRetryTime > 3600_000L) {
        prefs[CPU_LOCK_RETRY_COUNT] = 1
      } else {
        prefs[CPU_LOCK_RETRY_COUNT] = currentCount + 1
      }
      prefs[CPU_LOCK_LAST_RETRY_TIME] = now
    }
  }

  fun isCpuFrequencyLocked(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[CPU_FREQUENCY_LOCKED] ?: false }

  fun getCpuLockState(): Flow<CPULockState> =
      context.dataStore.data.map { prefs ->
        try {
          val locked = prefs[CPU_FREQUENCY_LOCKED] ?: false
          val clusterConfigsJson = prefs[CPU_LOCKED_CLUSTERS] ?: "{}"
          val policyType = prefs[CPU_LOCK_POLICY_TYPE] ?: LockPolicyType.MANUAL.name
          val thermalPolicy = prefs[CPU_LOCK_THERMAL_POLICY] ?: "PolicyB"
          val originalFreqsJson = prefs[CPU_LOCK_ORIGINAL_FREQS] ?: "{}"
          val thermalOverride = prefs[CPU_LOCK_THERMAL_OVERRIDE] ?: false
          val lastUpdate = prefs[CPU_LOCK_LAST_UPDATE] ?: 0L
          val lastTemperature = prefs[CPU_LOCK_LAST_TEMPERATURE] ?: 0f
          val retryCount = prefs[CPU_LOCK_RETRY_COUNT] ?: 0
          val lastRetryTime = prefs[CPU_LOCK_LAST_RETRY_TIME] ?: 0L

          val clusterConfigs = Json.decodeFromString<Map<Int, CpuClusterLockConfig>>(clusterConfigsJson)
          val originalFreqs = Json.decodeFromString<Map<Int, OriginalFreqConfig>>(originalFreqsJson)

          CPULockState(
              isLocked = locked,
              clusterConfigs = clusterConfigs,
              policyType = LockPolicyType.valueOf(policyType),
              thermalPolicy = thermalPolicy,
              lastUpdate = lastUpdate,
              lastTemperature = lastTemperature,
              isThermalOverrideActive = thermalOverride,
              originalFrequencies = originalFreqs,
              retryCount = retryCount,
              lastRetryTime = lastRetryTime
          )
        } catch (e: Exception) {
          // Return default state on error
          CPULockState()
        }
      }

  fun getCpuLockRetryCount(): Flow<Int> =
      context.dataStore.data.map { prefs -> prefs[CPU_LOCK_RETRY_COUNT] ?: 0 }

  fun getCpuLockLastRetryTime(): Flow<Long> =
      context.dataStore.data.map { prefs -> prefs[CPU_LOCK_LAST_RETRY_TIME] ?: 0L }

  fun getCpuLockLastTemperature(): Flow<Float> =
      context.dataStore.data.map { prefs -> prefs[CPU_LOCK_LAST_TEMPERATURE] ?: 0f }

  fun getCpuLockPolicyType(): Flow<LockPolicyType> =
      context.dataStore.data.map { prefs ->
        val policyName = prefs[CPU_LOCK_POLICY_TYPE] ?: LockPolicyType.MANUAL.name
        try {
          LockPolicyType.valueOf(policyName)
        } catch (e: Exception) {
          LockPolicyType.MANUAL
        }
      }

  fun getCpuLockThermalPolicy(): Flow<String> =
      context.dataStore.data.map { prefs -> prefs[CPU_LOCK_THERMAL_POLICY] ?: "PolicyB" }

  suspend fun setCpuLockThermalPolicy(policy: String) {
    context.dataStore.edit { prefs -> prefs[CPU_LOCK_THERMAL_POLICY] = policy }
  }

  // Game Apps (apps that trigger game overlay)
  suspend fun saveGameApps(jsonString: String) {
    context.dataStore.edit { prefs -> prefs[GAME_APPS] = jsonString }
  }

  fun getGameApps(): Flow<String> = context.dataStore.data.map { prefs -> prefs[GAME_APPS] ?: "[]" }

  // Display Saturation
  suspend fun setDisplaySaturation(value: Float) {
    context.dataStore.edit { prefs -> prefs[DISPLAY_SATURATION] = value }
  }

  fun getDisplaySaturation(): Flow<Float> =
      context.dataStore.data.map { prefs -> prefs[DISPLAY_SATURATION] ?: 1.0f }

  // Layout Style Functions
  suspend fun setLayoutStyle(style: String) {
    android.util.Log.d("PreferencesManager", "Starting layout switch to: $style")
    
    try {
      // Set switching state to true first
      context.dataStore.edit { prefs -> 
        prefs[LAYOUT_SWITCHING] = true
      }
      android.util.Log.d("PreferencesManager", "Layout switching state set to true")
      
      // Small delay to ensure UI updates
      kotlinx.coroutines.delay(100)
      
      // Update the layout style
      context.dataStore.edit { prefs -> 
        prefs[LAYOUT_STYLE] = style 
      }
      android.util.Log.d("PreferencesManager", "Layout style updated to: $style")
      
      // Simulate loading time for layout switching
      kotlinx.coroutines.delay(1500)
      android.util.Log.d("PreferencesManager", "Layout switching delay completed")
      
    } catch (e: Exception) {
      // Log error but continue to reset loading state
      android.util.Log.e("PreferencesManager", "Error setting layout style: ${e.message}", e)
    } finally {
      // Always set switching state to false, even if there's an error
      try {
        context.dataStore.edit { prefs -> 
          prefs[LAYOUT_SWITCHING] = false 
        }
        android.util.Log.d("PreferencesManager", "Layout switching state set to false")
      } catch (e: Exception) {
        android.util.Log.e("PreferencesManager", "Error resetting layout switching state: ${e.message}", e)
      }
    }
  }

  fun getLayoutStyle(): Flow<String> =
      context.dataStore.data.map { prefs -> prefs[LAYOUT_STYLE] ?: "liquid" }
      
  fun isLayoutSwitching(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[LAYOUT_SWITCHING] ?: false }
      
  // Emergency function to reset layout switching state
  suspend fun resetLayoutSwitching() {
    try {
      context.dataStore.edit { prefs -> 
        prefs[LAYOUT_SWITCHING] = false 
      }
    } catch (e: Exception) {
      android.util.Log.e("PreferencesManager", "Error resetting layout switching: ${e.message}")
    }
  }

  // Holiday Celebration Functions
  suspend fun setChristmasShownYear(year: Int) {
    context.dataStore.edit { prefs -> prefs[CHRISTMAS_SHOWN_YEAR] = year }
  }

  fun getChristmasShownYear(): Flow<Int> =
      context.dataStore.data.map { prefs -> prefs[CHRISTMAS_SHOWN_YEAR] ?: 0 }

  suspend fun setNewYearShownYear(year: Int) {
    context.dataStore.edit { prefs -> prefs[NEW_YEAR_SHOWN_YEAR] = year }
  }

  fun getNewYearShownYear(): Flow<Int> =
      context.dataStore.data.map { prefs -> prefs[NEW_YEAR_SHOWN_YEAR] ?: 0 }

  suspend fun setRamadanShownYear(year: Int) {
    context.dataStore.edit { prefs -> prefs[RAMADAN_SHOWN_YEAR] = year }
  }

  fun getRamadanShownYear(): Flow<Int> =
      context.dataStore.data.map { prefs -> prefs[RAMADAN_SHOWN_YEAR] ?: 0 }

  suspend fun setEidFitrShownYear(year: Int) {
    context.dataStore.edit { prefs -> prefs[EID_FITR_SHOWN_YEAR] = year }
  }

  fun getEidFitrShownYear(): Flow<Int> =
      context.dataStore.data.map { prefs -> prefs[EID_FITR_SHOWN_YEAR] ?: 0 }

  suspend fun setBatteryNotifIconType(type: String) {
    context.dataStore.edit { prefs -> prefs[BATTERY_NOTIF_ICON_TYPE] = type }
  }

  fun getBatteryNotifIconType(): Flow<String> =
      context.dataStore.data.map { prefs -> prefs[BATTERY_NOTIF_ICON_TYPE] ?: "temp" }

  // Changed to Long for milliseconds support (Real-time = 500ms)
  private val BATTERY_NOTIF_REFRESH_RATE_MS =
      androidx.datastore.preferences.core.longPreferencesKey("battery_notif_refresh_rate_ms")

  suspend fun setBatteryNotifRefreshRate(ms: Long) {
    context.dataStore.edit { prefs -> prefs[BATTERY_NOTIF_REFRESH_RATE_MS] = ms }
  }

  fun getBatteryNotifRefreshRate(): Flow<Long> =
      context.dataStore.data.map { prefs ->
        prefs[BATTERY_NOTIF_REFRESH_RATE_MS] ?: 5000L
      } // Default 5s

  suspend fun setBatteryNotifSecureLockScreen(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[BATTERY_NOTIF_SECURE_LOCK_SCREEN] = enabled }
  }

  fun getBatteryNotifSecureLockScreen(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[BATTERY_NOTIF_SECURE_LOCK_SCREEN] ?: false }

  suspend fun setBatteryNotifHighPriority(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[BATTERY_NOTIF_HIGH_PRIORITY] = enabled }
  }

  fun getBatteryNotifHighPriority(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[BATTERY_NOTIF_HIGH_PRIORITY] ?: false }

  suspend fun setBatteryNotifForceOnTop(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[BATTERY_NOTIF_FORCE_ON_TOP] = enabled }
  }

  fun getBatteryNotifForceOnTop(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[BATTERY_NOTIF_FORCE_ON_TOP] ?: false }

  suspend fun setBatteryNotifDontUpdateScreenOff(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[BATTERY_NOTIF_DONT_UPDATE_SCREEN_OFF] = enabled }
  }

  fun getBatteryNotifDontUpdateScreenOff(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[BATTERY_NOTIF_DONT_UPDATE_SCREEN_OFF] ?: true }

  suspend fun setBatteryStatsActiveIdle(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[BATTERY_STATS_ACTIVE_IDLE] = enabled }
  }

  fun getBatteryStatsActiveIdle(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[BATTERY_STATS_ACTIVE_IDLE] ?: true }

  suspend fun setBatteryStatsScreen(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[BATTERY_STATS_SCREEN] = enabled }
  }

  fun getBatteryStatsScreen(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[BATTERY_STATS_SCREEN] ?: true }

  suspend fun setBatteryStatsAwakeSleep(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[BATTERY_STATS_AWAKE_SLEEP] = enabled }
  }

  fun getBatteryStatsAwakeSleep(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[BATTERY_STATS_AWAKE_SLEEP] ?: true }

  // ==================== Quick Apps Preferences ====================
  private val QUICK_APPS = stringPreferencesKey("quick_apps")

  // Game Tools preferences
  private val ESPORTS_MODE_ENABLED = booleanPreferencesKey("esports_mode_enabled")
  private val TOUCH_GUARD_ENABLED = booleanPreferencesKey("touch_guard_enabled")
  private val AUTO_REJECT_CALLS_ENABLED = booleanPreferencesKey("auto_reject_calls_enabled")
  private val LOCK_BRIGHTNESS_ENABLED = booleanPreferencesKey("lock_brightness_enabled")
  private val QUICK_APPS_INITIALIZED = booleanPreferencesKey("quick_apps_initialized")

  /** Get quick apps package names as JSON array string */
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

  /** Set quick apps list */
  suspend fun setQuickApps(apps: List<String>) {
    val jsonArray = org.json.JSONArray(apps)
    context.dataStore.edit { prefs -> prefs[QUICK_APPS] = jsonArray.toString() }
  }

  /** Add a quick app */
  suspend fun addQuickApp(packageName: String) {
    context.dataStore.edit { prefs ->
      val currentJson = prefs[QUICK_APPS] ?: "[]"
      try {
        val jsonArray = org.json.JSONArray(currentJson)
        // Check if already exists
        val exists = (0 until jsonArray.length()).any { jsonArray.getString(it) == packageName }
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

  /** Remove a quick app */
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

  /** Check if quick apps have been initialized */
  fun isQuickAppsInitialized(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[QUICK_APPS_INITIALIZED] ?: false }

  /** Set quick apps initialized flag */
  suspend fun setQuickAppsInitialized(initialized: Boolean) {
    context.dataStore.edit { prefs -> prefs[QUICK_APPS_INITIALIZED] = initialized }
  }

  // ==================== Game Tools Preferences ====================

  /** Esports Mode - Maximum optimization for gaming */
  suspend fun setEsportsMode(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[ESPORTS_MODE_ENABLED] = enabled }
  }

  fun isEsportsModeEnabled(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[ESPORTS_MODE_ENABLED] ?: false }

  /** Touch Guard - Prevent accidental touches */
  suspend fun setTouchGuard(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[TOUCH_GUARD_ENABLED] = enabled }
  }

  fun isTouchGuardEnabled(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[TOUCH_GUARD_ENABLED] ?: false }

  /** Auto Reject Calls during gaming */
  suspend fun setAutoRejectCalls(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[AUTO_REJECT_CALLS_ENABLED] = enabled }
  }

  fun isAutoRejectCallsEnabled(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[AUTO_REJECT_CALLS_ENABLED] ?: false }

  /** Lock Brightness during gaming */
  suspend fun setLockBrightness(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[LOCK_BRIGHTNESS_ENABLED] = enabled }
  }

  fun isLockBrightnessEnabled(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[LOCK_BRIGHTNESS_ENABLED] ?: false }

  // New Features: Ringer, Call Mode, Gestures
  private val RINGER_MODE = intPreferencesKey("ringer_mode") 
  private val CALL_MODE = intPreferencesKey("call_mode") 
  private val THREE_FINGER_SWIPE_ENABLED = booleanPreferencesKey("three_finger_swipe_enabled")
  
  // Banking Mode - Disable accessibility service for banking security
  private val BANKING_MODE_ENABLED = booleanPreferencesKey("banking_mode_enabled")

  suspend fun setRingerMode(mode: Int) {
    context.dataStore.edit { prefs -> prefs[RINGER_MODE] = mode }
  }

  fun getRingerMode(): Flow<Int> =
      context.dataStore.data.map { prefs -> prefs[RINGER_MODE] ?: 0 }

  suspend fun setCallMode(mode: Int) {
    context.dataStore.edit { prefs -> prefs[CALL_MODE] = mode }
  }

  fun getCallMode(): Flow<Int> =
      context.dataStore.data.map { prefs -> prefs[CALL_MODE] ?: 0 }

  suspend fun setThreeFingerSwipeEnabled(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[THREE_FINGER_SWIPE_ENABLED] = enabled }
  }

  fun isThreeFingerSwipeEnabled(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[THREE_FINGER_SWIPE_ENABLED] ?: true } // Default ON

  // Banking Mode Functions
  suspend fun setBankingModeEnabled(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[BANKING_MODE_ENABLED] = enabled }
  }

  fun getBankingModeEnabled(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[BANKING_MODE_ENABLED] ?: false }

  // ==================== SYNC PREFERENCES ====================
  // Using SharedPreferences for simple synchronous access
  // Also accessible by Xposed module via XSharedPreferences

  @Suppress("DEPRECATION")
  private val syncPrefs by lazy {
    // Try to use MODE_WORLD_READABLE for Xposed compatibility
    try {
      context.getSharedPreferences("xkm_sync_prefs", Context.MODE_WORLD_READABLE)
    } catch (e: SecurityException) {
      // Fallback to MODE_PRIVATE if MODE_WORLD_READABLE is not allowed
      android.util.Log.w("PreferencesManager", "MODE_WORLD_READABLE not allowed, using MODE_PRIVATE")
      context.getSharedPreferences("xkm_sync_prefs", Context.MODE_PRIVATE)
    }
  }
  
  /**
   * Make the sync prefs file world-readable for Xposed module access.
   * Should be called after saving important settings that Xposed needs.
   */
  fun makePrefsWorldReadable() {
    try {
      val prefsDir = java.io.File(context.applicationInfo.dataDir, "shared_prefs")
      val prefsFile = java.io.File(prefsDir, "xkm_sync_prefs.xml")
      if (prefsFile.exists()) {
        prefsFile.setReadable(true, false)
        prefsDir.setExecutable(true, false)
        android.util.Log.d("PreferencesManager", "Made prefs world-readable: ${prefsFile.absolutePath}")
      }
    } catch (e: Exception) {
      android.util.Log.e("PreferencesManager", "Failed to make prefs world-readable: ${e.message}")
    }
  }

  /** Get boolean value synchronously */
  fun getBoolean(key: String, defaultValue: Boolean): Boolean {
    return syncPrefs.getBoolean(key, defaultValue)
  }

  /** Set boolean value synchronously */
  fun setBoolean(key: String, value: Boolean) {
    syncPrefs.edit().putBoolean(key, value).apply()
    makePrefsWorldReadable()
  }

  /** Get string value synchronously */
  fun getString(key: String, defaultValue: String): String {
    return syncPrefs.getString(key, defaultValue) ?: defaultValue
  }

  /** Set string value synchronously */
  fun setString(key: String, value: String) {
    syncPrefs.edit().putString(key, value).apply()
    makePrefsWorldReadable()
  }

  /** Get int value synchronously */
  fun getInt(key: String, defaultValue: Int): Int {
    return syncPrefs.getInt(key, defaultValue)
  }

  /** Set int value synchronously */
  fun setInt(key: String, value: Int) {
    syncPrefs.edit().putInt(key, value).apply()
    makePrefsWorldReadable()
  }

  suspend fun setFunctionalRomUnlockNits(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[FUNCTIONAL_ROM_UNLOCK_NITS] = enabled }
  }

  fun getFunctionalRomUnlockNits(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[FUNCTIONAL_ROM_UNLOCK_NITS] ?: false }

  suspend fun setFunctionalRomDynamicRefresh(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[FUNCTIONAL_ROM_DYNAMIC_REFRESH] = enabled }
  }

  fun getFunctionalRomDynamicRefresh(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[FUNCTIONAL_ROM_DYNAMIC_REFRESH] ?: false }

  suspend fun setFunctionalRomForceRefresh(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[FUNCTIONAL_ROM_FORCE_REFRESH] = enabled }
  }

  fun getFunctionalRomForceRefresh(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[FUNCTIONAL_ROM_FORCE_REFRESH] ?: false }

  suspend fun setFunctionalRomForceRefreshValue(value: Int) {
    context.dataStore.edit { prefs -> prefs[FUNCTIONAL_ROM_FORCE_REFRESH_VALUE] = value }
  }

  fun getFunctionalRomForceRefreshValue(): Flow<Int> =
      context.dataStore.data.map { prefs -> prefs[FUNCTIONAL_ROM_FORCE_REFRESH_VALUE] ?: 60 }

  suspend fun setFunctionalRomDcDimming(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[FUNCTIONAL_ROM_DC_DIMMING] = enabled }
  }

  fun getFunctionalRomDcDimming(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[FUNCTIONAL_ROM_DC_DIMMING] ?: false }

  suspend fun setFunctionalRomPerformanceMode(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[FUNCTIONAL_ROM_PERFORMANCE_MODE] = enabled }
  }

  fun getFunctionalRomPerformanceMode(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[FUNCTIONAL_ROM_PERFORMANCE_MODE] ?: false }

  suspend fun setFunctionalRomSmartCharging(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[FUNCTIONAL_ROM_SMART_CHARGING] = enabled }
  }

  fun getFunctionalRomSmartCharging(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[FUNCTIONAL_ROM_SMART_CHARGING] ?: false }

  suspend fun setFunctionalRomChargingLimit(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[FUNCTIONAL_ROM_CHARGING_LIMIT] = enabled }
  }

  fun getFunctionalRomChargingLimit(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[FUNCTIONAL_ROM_CHARGING_LIMIT] ?: false }

  suspend fun setFunctionalRomChargingLimitValue(value: Int) {
    context.dataStore.edit { prefs -> prefs[FUNCTIONAL_ROM_CHARGING_LIMIT_VALUE] = value }
  }

  fun getFunctionalRomChargingLimitValue(): Flow<Int> =
      context.dataStore.data.map { prefs -> prefs[FUNCTIONAL_ROM_CHARGING_LIMIT_VALUE] ?: 80 }

  // CPU Cluster Configuration Methods
  suspend fun setClusterMinFreq(cluster: Int, minFreq: Int) {
    context.dataStore.edit { prefs -> prefs[clusterMinFreqKey(cluster)] = minFreq }
  }

  fun getClusterMinFreq(cluster: Int): Flow<Int> =
      context.dataStore.data.map { prefs -> prefs[clusterMinFreqKey(cluster)] ?: 0 }

  suspend fun setClusterMaxFreq(cluster: Int, maxFreq: Int) {
    context.dataStore.edit { prefs -> prefs[clusterMaxFreqKey(cluster)] = maxFreq }
  }

  fun getClusterMaxFreq(cluster: Int): Flow<Int> =
      context.dataStore.data.map { prefs -> prefs[clusterMaxFreqKey(cluster)] ?: 0 }

  suspend fun setClusterGovernor(cluster: Int, governor: String) {
    context.dataStore.edit { prefs -> prefs[clusterGovernorKey(cluster)] = governor }
  }

  fun getClusterGovernor(cluster: Int): Flow<String> =
      context.dataStore.data.map { prefs -> prefs[clusterGovernorKey(cluster)] ?: "" }

  suspend fun saveClusterConfiguration(cluster: Int, minFreq: Int, maxFreq: Int, governor: String) {
    context.dataStore.edit { prefs ->
      prefs[clusterMinFreqKey(cluster)] = minFreq
      prefs[clusterMaxFreqKey(cluster)] = maxFreq
      prefs[clusterGovernorKey(cluster)] = governor
    }
  }

  // I/O Scheduler Set on Boot
  suspend fun setIOSetOnBoot(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[IO_SET_ON_BOOT] = enabled }
  }

  fun getIOSetOnBoot(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[IO_SET_ON_BOOT] ?: false }

  // TCP Congestion Set on Boot
  suspend fun setTCPSetOnBoot(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[TCP_SET_ON_BOOT] = enabled }
  }

  fun getTCPSetOnBoot(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[TCP_SET_ON_BOOT] ?: false }

  // RAM Set on Boot
  suspend fun setRAMSetOnBoot(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[RAM_SET_ON_BOOT] = enabled }
  }

  fun getRAMSetOnBoot(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[RAM_SET_ON_BOOT] ?: false }

  // Additional/Network Set on Boot
  suspend fun setAdditionalSetOnBoot(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[ADDITIONAL_SET_ON_BOOT] = enabled }
  }

  fun getAdditionalSetOnBoot(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[ADDITIONAL_SET_ON_BOOT] ?: false }

  private val HIDE_ACCESSIBILITY_ENABLED = booleanPreferencesKey("hide_accessibility_enabled")
  private val HIDE_ACCESSIBILITY_TAB = stringPreferencesKey("hide_accessibility_tab")
  private val HIDE_ACCESSIBILITY_APPS_TO_HIDE = stringPreferencesKey("hide_accessibility_apps_to_hide")
  private val HIDE_ACCESSIBILITY_DETECTOR_APPS = stringPreferencesKey("hide_accessibility_detector_apps")
  
  suspend fun setHideAccessibilityEnabled(enabled: Boolean) {
    context.dataStore.edit { prefs -> prefs[HIDE_ACCESSIBILITY_ENABLED] = enabled }
    setBoolean("xkm_hide_accessibility_enabled", enabled)
  }
  
  fun getHideAccessibilityEnabled(): Flow<Boolean> =
      context.dataStore.data.map { prefs -> prefs[HIDE_ACCESSIBILITY_ENABLED] ?: false }
  
  /** Save current tab selection */
  suspend fun setHideAccessibilityTab(tab: String) {
    context.dataStore.edit { prefs -> prefs[HIDE_ACCESSIBILITY_TAB] = tab }
  }
  
  fun getHideAccessibilityTab(): Flow<String> =
      context.dataStore.data.map { prefs -> prefs[HIDE_ACCESSIBILITY_TAB] ?: "apps_to_hide" }
  
  suspend fun setHideAccessibilityAppsToHide(jsonString: String) {
    context.dataStore.edit { prefs -> prefs[HIDE_ACCESSIBILITY_APPS_TO_HIDE] = jsonString }
    setString("hide_accessibility_apps_to_hide", jsonString)
  }
  
  fun getHideAccessibilityAppsToHide(): Flow<String> =
      context.dataStore.data.map { prefs -> prefs[HIDE_ACCESSIBILITY_APPS_TO_HIDE] ?: "[]" }
  
  suspend fun setHideAccessibilityDetectorApps(jsonString: String) {
    context.dataStore.edit { prefs -> prefs[HIDE_ACCESSIBILITY_DETECTOR_APPS] = jsonString }
    setString("hide_accessibility_detector_apps", jsonString)
  }
  
  fun getHideAccessibilityDetectorApps(): Flow<String> =
      context.dataStore.data.map { prefs -> prefs[HIDE_ACCESSIBILITY_DETECTOR_APPS] ?: "[]" }
}
