package id.xms.xtrakernelmanager.domain.native

import android.util.Log
import id.xms.xtrakernelmanager.data.model.ClusterInfo
import org.json.JSONArray

/**
 * Native library wrapper for Rust-based system operations.
 * Provides faster sysfs/procfs reading without shell overhead.
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
    
    /**
     * Check if native library is available
     */
    fun isAvailable(): Boolean = isLoaded
    
    /**
     * Detect CPU clusters using native code
     * Returns list of ClusterInfo or null if native lib not available
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
     * Read battery current in milliamps using native code
     * Positive = charging, Negative = discharging
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
    
    /**
     * Read CPU load percentage using native code
     * Returns null if native lib not available
     */
    fun readCpuLoad(): Float? {
        if (!isLoaded) return null
        
        return try {
            readCpuLoadNative()
        } catch (e: Exception) {
            Log.e(TAG, "Native readCpuLoad failed: ${e.message}")
            null
        }
    }
    
    /**
     * Read CPU temperature using native code
     * Returns null if native lib not available
     */
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
     * Read GPU frequency in MHz using native code
     * Returns null if native lib not available
     */
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
    

    /**
     * Read battery level percentage (0-100)
     */
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
    
    /**
     * Read battery drain rate in mA (positive = discharging, negative = charging)
     */
    fun readDrainRate(): Int? {
        if (!isLoaded) return null
        return try {
            readDrainRateNative()
        } catch (e: Exception) {
            Log.e(TAG, "Native readDrainRate failed: ${e.message}")
            null
        }
    }
    
    /**
     * Read wakeup count (number of wakeups from sleep)
     */
    fun readWakeupCount(): Int? {
        if (!isLoaded) return null
        return try {
            readWakeupCountNative()
        } catch (e: Exception) {
            Log.e(TAG, "Native readWakeupCount failed: ${e.message}")
            null
        }
    }
    
    /**
     * Read suspend count (number of times device entered deep sleep)
     */
    fun readSuspendCount(): Int? {
        if (!isLoaded) return null
        return try {
            readSuspendCountNative()
        } catch (e: Exception) {
            Log.e(TAG, "Native readSuspendCount failed: ${e.message}")
            null
        }
    }
    
    /**
     * Check if device is currently charging
     */
    fun isCharging(): Boolean? {
        if (!isLoaded) return null
        return try {
            isChargingNative()
        } catch (e: Exception) {
            Log.e(TAG, "Native isCharging failed: ${e.message}")
            null
        }
    }
    
    /**
     * Read battery temperature in Celsius (returns Float, e.g., 35.0)
     */
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
    
    /**
     * Read battery voltage in volts (returns Float, e.g., 4.2)
     */
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
    
    
    // CPU Module
    private external fun detectCpuClustersNative(): String
    private external fun readBatteryCurrentNative(): Int
    private external fun readCpuLoadNative(): Float
    private external fun readCpuTemperatureNative(): Float
    
    // GPU Module
    private external fun readGpuFreqNative(): Int
    private external fun readGpuBusyNative(): Int
    
    // Power Module
    private external fun readBatteryLevelNative(): Int
    private external fun readDrainRateNative(): Int
    private external fun readWakeupCountNative(): Int
    private external fun readSuspendCountNative(): Int
    private external fun isChargingNative(): Boolean
    private external fun readBatteryTempNative(): Int
    private external fun readBatteryVoltageNative(): Int
    
    /**
     * Parse JSON string from Rust into ClusterInfo list
     */
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
                        policyPath = obj.getString("policy_path")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse clusters JSON: ${e.message}")
        }
        
        return clusters
    }
}
