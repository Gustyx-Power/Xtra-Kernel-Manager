package id.xms.xtrakernelmanager.domain.native

import android.util.Log
import id.xms.xtrakernelmanager.data.model.ClusterInfo
import org.json.JSONArray

/**
 * Native library wrapper for Rust-based system operations. Provides faster sysfs/procfs reading
 * without shell overhead.
 */
object NativeLib {

  private const val TAG = "NativeLib"
  private var isLoaded = false

  init {
    try {
      System.loadLibrary("xkm_native")
      isLoaded = true
      Log.d(TAG, "Native library loaded successfully")
    } catch (e: UnsatisfiedLinkError) {
      Log.e(TAG, "Failed to load native library: ${e.message}")
      isLoaded = false
    }
  }

  /** Check if native library is available */
  fun isAvailable(): Boolean = isLoaded

  /**
   * Detect CPU clusters using native code Returns list of ClusterInfo or null if native lib not
   * available
   */
  fun detectCpuClusters(): List<ClusterInfo>? {
    if (!isLoaded) return null

    return try {
      val json = detectCpuClustersNative()
      parseClustersFromJson(json)
    } catch (e: Exception) {
      Log.e(TAG, "Native detectCpuClusters failed: ${e.message}")
      null
    }
  }

  /**
   * Read battery current in milliamps using native code Positive = charging, Negative = discharging
   * Returns null if native lib not available
   */
  fun readBatteryCurrent(): Int? {
    if (!isLoaded) return null

    return try {
      readBatteryCurrentNative()
    } catch (e: Exception) {
      Log.e(TAG, "Native readBatteryCurrent failed: ${e.message}")
      null
    }
  }

  /** Read CPU load percentage using native code Returns null if native lib not available */
  fun readCpuLoad(): Float? {
    if (!isLoaded) return null

    return try {
      readCpuLoadNative()
    } catch (e: Exception) {
      Log.e(TAG, "Native readCpuLoad failed: ${e.message}")
      null
    }
  }

  /** Read CPU temperature using native code Returns null if native lib not available */
  fun readCpuTemperature(): Float? {
    if (!isLoaded) return null

    return try {
      readCpuTemperatureNative()
    } catch (e: Exception) {
      Log.e(TAG, "Native readCpuTemperature failed: ${e.message}")
      null
    }
  }

  /**
   * Read dynamic data for all CPU cores (frequency, online status, governor) Returns list of
   * CoreData objects or null if native lib not available
   */
  data class CoreData(val core: Int, val online: Boolean, val freq: Int, val governor: String)

  fun readCoreData(): List<CoreData>? {
    if (!isLoaded) return null

    return try {
      val json = readCoreDataNative()
      parseCoreDataFromJson(json)
    } catch (e: Exception) {
      Log.e(TAG, "Native readCoreData failed: ${e.message}")
      null
    }
  }

  private fun parseCoreDataFromJson(json: String): List<CoreData> {
    val coreDataList = mutableListOf<CoreData>()
    try {
      val jsonArray = JSONArray(json)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        coreDataList.add(
            CoreData(
                core = obj.getInt("core"),
                online = obj.getBoolean("online"),
                freq = obj.getInt("freq"),
                governor = obj.getString("governor"),
            )
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to parse core data JSON: ${e.message}")
    }
    return coreDataList
  }

  /** Read GPU frequency in MHz using native code Returns null if native lib not available */
  fun readGpuFreq(): Int? {
    if (!isLoaded) return null

    return try {
      val freq = readGpuFreqNative()
      if (freq > 0) freq else null
    } catch (e: Exception) {
      Log.e(TAG, "Native readGpuFreq failed: ${e.message}")
      null
    }
  }

  fun readGpuBusy(): Int? {
    if (!isLoaded) return null

    return try {
      val busy = readGpuBusyNative()
      if (busy >= 0) busy else null
    } catch (e: Exception) {
      Log.e(TAG, "Native readGpuBusy failed: ${e.message}")
      null
    }
  }

  fun resetGpuStats() {
    if (!isLoaded) return
    try {
      resetGpuStatsNative()
    } catch (e: Exception) {
      Log.e(TAG, "Native resetGpuStats failed: ${e.message}")
    }
  }

  /** Read battery level percentage (0-100) */
  fun readBatteryLevel(): Int? {
    if (!isLoaded) return null
    return try {
      val level = readBatteryLevelNative()
      if (level in 0..100) level else null
    } catch (e: Exception) {
      Log.e(TAG, "Native readBatteryLevel failed: ${e.message}")
      null
    }
  }

  /** Read battery drain rate in mA (positive = discharging, negative = charging) */
  fun readDrainRate(): Int? {
    if (!isLoaded) return null
    return try {
      readDrainRateNative()
    } catch (e: Exception) {
      Log.e(TAG, "Native readDrainRate failed: ${e.message}")
      null
    }
  }

  /** Read wakeup count (number of wakeups from sleep) */
  fun readWakeupCount(): Int? {
    if (!isLoaded) return null
    return try {
      readWakeupCountNative()
    } catch (e: Exception) {
      Log.e(TAG, "Native readWakeupCount failed: ${e.message}")
      null
    }
  }

  /** Read suspend count (number of times device entered deep sleep) */
  fun readSuspendCount(): Int? {
    if (!isLoaded) return null
    return try {
      readSuspendCountNative()
    } catch (e: Exception) {
      Log.e(TAG, "Native readSuspendCount failed: ${e.message}")
      null
    }
  }

  /** Check if device is currently charging */
  fun isCharging(): Boolean? {
    if (!isLoaded) return null
    return try {
      isChargingNative() == 1
    } catch (e: Exception) {
      Log.e(TAG, "Native isCharging failed: ${e.message}")
      null
    }
  }

  /** Read battery temperature in Celsius (returns Float, e.g., 35.0) */
  fun readBatteryTemp(): Float? {
    if (!isLoaded) return null
    return try {
      val temp = readBatteryTempNative()
      if (temp > 0) temp / 10.0f else null // Convert deciCelsius to Celsius
    } catch (e: Exception) {
      Log.e(TAG, "Native readBatteryTemp failed: ${e.message}")
      null
    }
  }

  /** Read battery voltage in volts (returns Float, e.g., 4.2) */
  fun readBatteryVoltage(): Float? {
    if (!isLoaded) return null
    return try {
      val mv = readBatteryVoltageNative()
      if (mv > 0) mv / 1000.0f else null // Convert mV to V
    } catch (e: Exception) {
      Log.e(TAG, "Native readBatteryVoltage failed: ${e.message}")
      null
    }
  }

  /** Memory info data class */
  data class MemInfo(
      val totalKb: Long,
      val availableKb: Long,
      val freeKb: Long,
      val cachedKb: Long,
      val buffersKb: Long,
      val swapTotalKb: Long,
      val swapFreeKb: Long,
  )

  /** Read memory info from /proc/meminfo using native code */
  fun readMemInfo(): MemInfo? {
    if (!isLoaded) return null
    return try {
      val json = readMemInfoNative()
      parseMemInfoFromJson(json)
    } catch (e: Exception) {
      Log.e(TAG, "Native readMemInfo failed: ${e.message}")
      null
    }
  }

  private fun parseMemInfoFromJson(json: String): MemInfo? {
    return try {
      val obj = org.json.JSONObject(json)
      MemInfo(
          totalKb = obj.optLong("total_kb", 0),
          availableKb = obj.optLong("available_kb", 0),
          freeKb = obj.optLong("free_kb", 0),
          cachedKb = obj.optLong("cached_kb", 0),
          buffersKb = obj.optLong("buffers_kb", 0),
          swapTotalKb = obj.optLong("swap_total_kb", 0),
          swapFreeKb = obj.optLong("swap_free_kb", 0),
      )
    } catch (e: Exception) {
      Log.e(TAG, "Failed to parse MemInfo JSON: ${e.message}")
      null
    }
  }

  /** Read ZRAM disk size in bytes */
  fun readZramSize(): Long? {
    if (!isLoaded) return null
    return try {
      val size = readZramSizeNative()
      if (size > 0) size else null
    } catch (e: Exception) {
      Log.e(TAG, "Native readZramSize failed: ${e.message}")
      null
    }
  }

  // CPU Module
  private external fun detectCpuClustersNative(): String

  private external fun readBatteryCurrentNative(): Int

  private external fun readCpuLoadNative(): Float

  private external fun readCpuTemperatureNative(): Float

  private external fun readCoreDataNative(): String

  // GPU Module
  private external fun readGpuFreqNative(): Int

  private external fun readGpuBusyNative(): Int

  private external fun resetGpuStatsNative()

  // Power Module
  private external fun readBatteryLevelNative(): Int

  private external fun readDrainRateNative(): Int

  private external fun readWakeupCountNative(): Int

  private external fun readSuspendCountNative(): Int

  private external fun isChargingNative(): Int

  private external fun readBatteryTempNative(): Int

  private external fun readBatteryVoltageNative(): Int

  // Memory Module
  private external fun readMemInfoNative(): String

  private external fun readZramSizeNative(): Long

  private external fun readThermalZonesNative(): String

  /** Read all thermal zones */
  data class ThermalZone(val name: String, val temp: Float)

  fun readThermalZones(): List<ThermalZone> {
    if (!isLoaded) return emptyList()
    return try {
      val json = readThermalZonesNative()
      val list = mutableListOf<ThermalZone>()
      val jsonArray = JSONArray(json)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(ThermalZone(name = obj.getString("name"), temp = obj.getDouble("temp").toFloat()))
      }
      list
    } catch (e: Exception) {
      Log.e(TAG, "Native readThermalZones failed: ${e.message}")
      emptyList()
    }
  }

  /** Parse JSON string from Rust into ClusterInfo list */
  private fun parseClustersFromJson(json: String): List<ClusterInfo> {
    val clusters = mutableListOf<ClusterInfo>()

    try {
      val jsonArray = JSONArray(json)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)

        // Parse cores array
        val coresArray = obj.getJSONArray("cores")
        val cores = mutableListOf<Int>()
        for (j in 0 until coresArray.length()) {
          cores.add(coresArray.getInt(j))
        }

        // Parse available governors array
        val govsArray = obj.getJSONArray("available_governors")
        val governors = mutableListOf<String>()
        for (j in 0 until govsArray.length()) {
          governors.add(govsArray.getString(j))
        }

        clusters.add(
            ClusterInfo(
                clusterNumber = obj.getInt("cluster_number"),
                cores = cores,
                minFreq = obj.getInt("min_freq"),
                maxFreq = obj.getInt("max_freq"),
                currentMinFreq = obj.getInt("current_min_freq"),
                currentMaxFreq = obj.getInt("current_max_freq"),
                governor = obj.getString("governor"),
                availableGovernors = governors,
                policyPath = obj.getString("policy_path"),
            )
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to parse clusters JSON: ${e.message}")
    }

    return clusters
  }

  /** Get system property value (100x faster than shell getprop) */
  fun getSystemProperty(key: String): String? {
    if (!isLoaded) return null
    return try {
      val value = getSystemPropertyNative(key)
      if (value.isNotEmpty()) value else null
    } catch (e: Exception) {
      Log.e(TAG, "Native getSystemProperty failed: ${e.message}")
      null
    }
  }

  private external fun getSystemPropertyNative(key: String): String

  /** Get GPU vendor (Qualcomm, ARM, etc.) */
  fun getGpuVendor(): String? {
    if (!isLoaded) return null
    return try {
      val vendor = getGpuVendorNative()
      if (vendor.isNotEmpty() && vendor != "Unknown") vendor else null
    } catch (e: Exception) {
      Log.e(TAG, "Native getGpuVendor failed: ${e.message}")
      null
    }
  }

  /** Get GPU model (Adreno 725, Mali-G710, etc.) */
  fun getGpuModel(): String? {
    if (!isLoaded) return null
    return try {
      val model = getGpuModelNative()
      if (model.isNotEmpty() && model != "Unknown") model else null
    } catch (e: Exception) {
      Log.e(TAG, "Native getGpuModel failed: ${e.message}")
      null
    }
  }

  private external fun getGpuVendorNative(): String

  private external fun getGpuModelNative(): String

  // ============== NEW: Battery Extended Functions ==============

  /** Read battery cycle count */
  fun readCycleCount(): Int? {
    if (!isLoaded) return null
    return try {
      val count = readCycleCountNative()
      if (count >= 0) count else null
    } catch (e: Exception) {
      Log.e(TAG, "Native readCycleCount failed: ${e.message}")
      null
    }
  }

  /** Read battery health status string */
  fun readBatteryHealth(): String? {
    if (!isLoaded) return null
    return try {
      val health = readBatteryHealthNative()
      if (health.isNotEmpty()) health else null
    } catch (e: Exception) {
      Log.e(TAG, "Native readBatteryHealth failed: ${e.message}")
      null
    }
  }

  /** Read battery capacity level (current/design ratio as percentage) */
  fun readBatteryCapacityLevel(): Float? {
    if (!isLoaded) return null
    return try {
      val level = readBatteryCapacityLevelNative()
      if (level > 0f) level else null
    } catch (e: Exception) {
      Log.e(TAG, "Native readBatteryCapacityLevel failed: ${e.message}")
      null
    }
  }

  private external fun readCycleCountNative(): Int

  private external fun readBatteryHealthNative(): String

  private external fun readBatteryCapacityLevelNative(): Float

  // ============== NEW: Memory Extended Functions ==============

  /** Get ZRAM compression ratio (e.g., 2.87x) */
  fun getZramCompressionRatio(): Float? {
    if (!isLoaded) return null
    return try {
      val ratio = getZramCompressionRatioNative()
      if (ratio > 0f) ratio else null
    } catch (e: Exception) {
      Log.e(TAG, "Native getZramCompressionRatio failed: ${e.message}")
      null
    }
  }

  /** Get ZRAM compressed size in bytes */
  fun getZramCompressedSize(): Long? {
    if (!isLoaded) return null
    return try {
      val size = getZramCompressedSizeNative()
      if (size > 0) size else null
    } catch (e: Exception) {
      Log.e(TAG, "Native getZramCompressedSize failed: ${e.message}")
      null
    }
  }

  /** Get current ZRAM algorithm (lz4, lzo, zstd, etc.) */
  fun getZramAlgorithm(): String? {
    if (!isLoaded) return null
    return try {
      val algo = getZramAlgorithmNative()
      if (algo.isNotEmpty()) algo else null
    } catch (e: Exception) {
      Log.e(TAG, "Native getZramAlgorithm failed: ${e.message}")
      null
    }
  }

  /** Get swappiness value (0-200) */
  fun getSwappiness(): Int? {
    if (!isLoaded) return null
    return try {
      val value = getSwappinessNative()
      if (value >= 0) value else null
    } catch (e: Exception) {
      Log.e(TAG, "Native getSwappiness failed: ${e.message}")
      null
    }
  }

  /** Get memory pressure (0.0-1.0) */
  fun getMemoryPressure(): Float? {
    if (!isLoaded) return null
    return try {
      val pressure = getMemoryPressureNative()
      if (pressure >= 0f) pressure else null
    } catch (e: Exception) {
      Log.e(TAG, "Native getMemoryPressure failed: ${e.message}")
      null
    }
  }

  private external fun getZramCompressionRatioNative(): Float

  private external fun getZramCompressedSizeNative(): Long

  private external fun getZramOrigDataSizeNative(): Long

  private external fun getZramAlgorithmNative(): String

  private external fun getSwappinessNative(): Int

  private external fun getMemoryPressureNative(): Float

  /** Get ZRAM original data size in bytes (uncompressed data stored in ZRAM) */
  fun getZramOrigDataSize(): Long? {
    if (!isLoaded) return null
    return try {
      val size = getZramOrigDataSizeNative()
      if (size > 0) size else null
    } catch (e: Exception) {
      Log.e(TAG, "Native getZramOrigDataSize failed: ${e.message}")
      null
    }
  }

  /** Get available ZRAM compression algorithms */
  fun getAvailableZramAlgorithms(): List<String>? {
    if (!isLoaded) return null
    return try {
      val json = getAvailableZramAlgorithmsNative()
      val list = mutableListOf<String>()
      val jsonArray = JSONArray(json)
      for (i in 0 until jsonArray.length()) {
        list.add(jsonArray.getString(i))
      }
      list
    } catch (e: Exception) {
      Log.e(TAG, "Native getAvailableZramAlgorithms failed: ${e.message}")
      null
    }
  }

  private external fun getAvailableZramAlgorithmsNative(): String

  private external fun getGpuAvailableFrequenciesNative(): String

  private external fun getGpuAvailablePoliciesNative(): String

  private external fun getGpuDriverInfoNative(): String

  private external fun readZramDeviceStatsNative(device: Int): String

  // Wrappers for new GPU functions
  fun getGpuAvailableFrequencies(): List<Int> {
    if (!isLoaded) return emptyList()
    return try {
      val jsonString = getGpuAvailableFrequenciesNative()
      if (jsonString.isBlank() || jsonString == "[]") return emptyList()
      val jsonArray = JSONArray(jsonString)
      List(jsonArray.length()) { i -> jsonArray.getInt(i) }
    } catch (e: Exception) {
      Log.e(TAG, "Native getGpuAvailableFrequencies failed: ${e.message}")
      emptyList()
    }
  }

  fun getGpuAvailablePolicies(): List<String> {
    if (!isLoaded) return emptyList()
    return try {
      val jsonString = getGpuAvailablePoliciesNative()
      if (jsonString.isBlank() || jsonString == "[]") return emptyList()
      val jsonArray = JSONArray(jsonString)
      List(jsonArray.length()) { i -> jsonArray.getString(i) }
    } catch (e: Exception) {
      Log.e(TAG, "Native getGpuAvailablePolicies failed: ${e.message}")
      emptyList()
    }
  }

  fun getGpuDriverInfo(): String {
    if (!isLoaded) return "unknown"
    return try {
      getGpuDriverInfoNative()
    } catch (e: Exception) {
      "unknown"
    }
  }
}
