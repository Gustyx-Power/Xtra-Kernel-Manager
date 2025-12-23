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
    
    // Native method declarations
    private external fun detectCpuClustersNative(): String
    private external fun readBatteryCurrentNative(): Int
    private external fun readCpuLoadNative(): Float
    private external fun readCpuTemperatureNative(): Float
    
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
