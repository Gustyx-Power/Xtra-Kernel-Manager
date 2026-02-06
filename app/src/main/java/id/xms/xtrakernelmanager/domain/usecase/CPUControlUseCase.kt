package id.xms.xtrakernelmanager.domain.usecase

import android.util.Log
import id.xms.xtrakernelmanager.data.model.ClusterInfo
import id.xms.xtrakernelmanager.data.model.CoreInfo
import id.xms.xtrakernelmanager.domain.native.NativeLib
import id.xms.xtrakernelmanager.domain.native.NativeLib.ThermalZone
import id.xms.xtrakernelmanager.domain.root.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class CPUControlUseCase {

  private val TAG = "CPUControlUseCase"
  
  private var cachedClusters: List<ClusterInfo>? = null
  private var clusterCacheTime: Long = 0L
  
  companion object {
    private const val CLUSTER_CACHE_TTL_MS = 30000L
  }
  
  fun invalidateClusterCache() {
    cachedClusters = null
    clusterCacheTime = 0L
  }
  
  suspend fun detectClusters(): List<ClusterInfo> {
    val now = System.currentTimeMillis()
    cachedClusters?.let { cached ->
      if (now - clusterCacheTime < CLUSTER_CACHE_TTL_MS) {
        return cached
      }
    }
    
    val clusters = detectClustersInternal()
    cachedClusters = clusters
    clusterCacheTime = now
    return clusters
  }
  
  private suspend fun detectClustersInternal(): List<ClusterInfo> {
    // Try native implementation first (faster, no shell overhead)
    val nativeClusters = NativeLib.detectCpuClusters()
    if (nativeClusters != null && nativeClusters.isNotEmpty()) {
      Log.d(TAG, "Using native cluster detection: ${nativeClusters.size} clusters")
      
      // Enrich with available frequencies if missing AND normalize to MHz
      return nativeClusters.map { cluster ->
        // Normalize kHz to MHz for native clusters (sysfs returns kHz)
        val needsNormalization = cluster.maxFreq > 10000
        
        val normalizedCluster = if (needsNormalization) {
           cluster.copy(
             minFreq = cluster.minFreq / 1000,
             maxFreq = cluster.maxFreq / 1000,
             currentMinFreq = cluster.currentMinFreq / 1000,
             currentMaxFreq = cluster.currentMaxFreq / 1000,
             availableFrequencies = cluster.availableFrequencies.map { it / 1000 }
           )
        } else {
           cluster
        }

        if (normalizedCluster.availableFrequencies.isEmpty()) {
          val firstCore = normalizedCluster.cores.firstOrNull() ?: 0
          val freqs =
              RootManager.executeCommand(
                      "cat /sys/devices/system/cpu/cpu$firstCore/cpufreq/scaling_available_frequencies 2>/dev/null"
                  )
                  .getOrNull()
                  ?.trim()
                  ?.split("\\s+".toRegex())
                  ?.mapNotNull { it.toIntOrNull()?.div(1000) }
                  ?: emptyList()
          
          // Apply device-specific frequency overrides
          val enhancedFreqs = applyDeviceSpecificFrequencies(freqs, normalizedCluster)
          normalizedCluster.copy(availableFrequencies = enhancedFreqs)
        } else {
          // Apply device-specific frequency overrides even if frequencies exist
          val enhancedFreqs = applyDeviceSpecificFrequencies(normalizedCluster.availableFrequencies, normalizedCluster)
          normalizedCluster.copy(availableFrequencies = enhancedFreqs)
        }
      }
    }

    // Fallback to shell-based detection
    Log.d(TAG, "Falling back to shell-based cluster detection")
    return detectClustersShell()
  }

  /** Optimized shell-based cluster detection with parallel execution */
  private suspend fun detectClustersShell(): List<ClusterInfo> = withContext(Dispatchers.IO) {
    val clusters = mutableListOf<ClusterInfo>()
    
    // Step 1: Detect available cores in parallel (much faster)
    val availableCores = (0..15).map { coreNum ->
      async {
        val exists = try {
          java.io.File("/sys/devices/system/cpu/cpu$coreNum").exists()
        } catch (e: Exception) {
          false
        }
        if (exists) coreNum else null
      }
    }.awaitAll().filterNotNull()
    
    if (availableCores.isEmpty()) return@withContext emptyList()
    Log.d(TAG, "Found ${availableCores.size} CPU cores: $availableCores")

    // Step 2: Get max frequencies in parallel to group cores into clusters
    val coreMaxFreqs = availableCores.map { core ->
      async {
        val maxFreq = try {
          java.io.File("/sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_max_freq")
            .readText().trim().toIntOrNull() ?: 0
        } catch (e: Exception) {
          // Fallback to shell command if direct file read fails
          RootManager.executeCommand(
            "cat /sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_max_freq 2>/dev/null"
          ).getOrNull()?.trim()?.toIntOrNull() ?: 0
        }
        core to maxFreq
      }
    }.awaitAll()

    // Group cores by max frequency (same max freq = same cluster)
    val coreGroups = mutableMapOf<Int, MutableList<Int>>()
    coreMaxFreqs.forEach { (core, maxFreq) ->
      if (maxFreq > 0) {
        val group = coreGroups.getOrPut(maxFreq) { mutableListOf() }
        group.add(core)
      }
    }

    // Step 3: Build cluster info in parallel
    val sortedGroups = coreGroups.entries.sortedBy { it.key }
    val clusterInfos = sortedGroups.mapIndexed { clusterIndex, (maxFreq, coresInGroup) ->
      async {
        val firstCore = coresInGroup.first()
        
        // Try direct file reads first (much faster), fallback to shell commands
        val minFreq = try {
          java.io.File("/sys/devices/system/cpu/cpu$firstCore/cpufreq/cpuinfo_min_freq")
            .readText().trim().toIntOrNull() ?: 0
        } catch (e: Exception) {
          RootManager.executeCommand("cat /sys/devices/system/cpu/cpu$firstCore/cpufreq/cpuinfo_min_freq 2>/dev/null")
            .getOrNull()?.trim()?.toIntOrNull() ?: 0
        }
        
        val currentMin = try {
          java.io.File("/sys/devices/system/cpu/cpu$firstCore/cpufreq/scaling_min_freq")
            .readText().trim().toIntOrNull() ?: minFreq
        } catch (e: Exception) {
          RootManager.executeCommand("cat /sys/devices/system/cpu/cpu$firstCore/cpufreq/scaling_min_freq 2>/dev/null")
            .getOrNull()?.trim()?.toIntOrNull() ?: minFreq
        }
        
        val currentMax = try {
          java.io.File("/sys/devices/system/cpu/cpu$firstCore/cpufreq/scaling_max_freq")
            .readText().trim().toIntOrNull() ?: maxFreq
        } catch (e: Exception) {
          RootManager.executeCommand("cat /sys/devices/system/cpu/cpu$firstCore/cpufreq/scaling_max_freq 2>/dev/null")
            .getOrNull()?.trim()?.toIntOrNull() ?: maxFreq
        }
        
        val governor = try {
          java.io.File("/sys/devices/system/cpu/cpu$firstCore/cpufreq/scaling_governor")
            .readText().trim()
        } catch (e: Exception) {
          RootManager.executeCommand("cat /sys/devices/system/cpu/cpu$firstCore/cpufreq/scaling_governor 2>/dev/null")
            .getOrNull()?.trim() ?: "schedutil"
        }
        
        val availableGovs = try {
          java.io.File("/sys/devices/system/cpu/cpu$firstCore/cpufreq/scaling_available_governors")
            .readText().trim().split(" ").filter { it.isNotBlank() }
        } catch (e: Exception) {
          RootManager.executeCommand("cat /sys/devices/system/cpu/cpu$firstCore/cpufreq/scaling_available_governors 2>/dev/null")
            .getOrNull()?.trim()?.split(" ")?.filter { it.isNotBlank() }
            ?: listOf("schedutil", "performance", "powersave", "ondemand", "conservative")
        }
        
        val availableFreqs = try {
          java.io.File("/sys/devices/system/cpu/cpu$firstCore/cpufreq/scaling_available_frequencies")
            .readText().trim().split(" ").mapNotNull { it.toIntOrNull()?.div(1000) }
        } catch (e: Exception) {
          RootManager.executeCommand("cat /sys/devices/system/cpu/cpu$firstCore/cpufreq/scaling_available_frequencies 2>/dev/null")
            .getOrNull()?.trim()?.split(" ")?.mapNotNull { it.toIntOrNull()?.div(1000) }
            ?: emptyList()
        }

        // Apply device-specific frequency overrides
        val enhancedFreqs = applyDeviceSpecificFrequencies(availableFreqs, null)

        val policyPath = "/sys/devices/system/cpu/cpufreq/policy${firstCore}"
        ClusterInfo(
          clusterNumber = clusterIndex,
          cores = coresInGroup,
          minFreq = minFreq / 1000,
          maxFreq = maxFreq / 1000,
          currentMinFreq = currentMin / 1000,
          currentMaxFreq = currentMax / 1000,
          governor = governor,
          availableGovernors = availableGovs,
          availableFrequencies = enhancedFreqs,
          policyPath = policyPath,
        )
      }
    }.awaitAll()
    
    Log.d(TAG, "Shell-based detection completed: ${clusterInfos.size} clusters")
    clusterInfos
  }

  suspend fun setClusterFrequency(cluster: Int, minFreq: Int, maxFreq: Int): Result<Unit> {
    val clusters = detectClusters()
    val targetCluster =
        clusters.getOrNull(cluster) ?: return Result.failure(Exception("Cluster not found"))
    targetCluster.cores.forEach { coreNum ->
      val basePath = "/sys/devices/system/cpu/cpu$coreNum"
      RootManager.executeCommand(
          "echo ${maxFreq * 1000} > $basePath/cpufreq/scaling_max_freq 2>/dev/null"
      )
      RootManager.executeCommand(
          "echo ${minFreq * 1000} > $basePath/cpufreq/scaling_min_freq 2>/dev/null"
      )
    }
    invalidateClusterCache()
    return Result.success(Unit)
  }

  suspend fun setClusterGovernor(cluster: Int, governor: String): Result<Unit> {
    val clusters = detectClusters()
    val targetCluster =
        clusters.getOrNull(cluster) ?: return Result.failure(Exception("Cluster not found"))
    targetCluster.cores.forEach { coreNum ->
      val basePath = "/sys/devices/system/cpu/cpu$coreNum"
      RootManager.executeCommand("echo $governor > $basePath/cpufreq/scaling_governor 2>/dev/null")
    }
    invalidateClusterCache()
    return Result.success(Unit)
  }

  suspend fun setCoreOnline(core: Int, online: Boolean): Result<Unit> {
    if (core == 0) return Result.success(Unit)
    val corePath = "/sys/devices/system/cpu/cpu$core"
    val value = if (online) "1" else "0"
    return RootManager.executeCommand("echo $value > $corePath/online 2>/dev/null").map { Unit }
  }

  /**
   * Get detailed information about all CPU cores
   */
  suspend fun getAllCoreInfo(): List<CoreInfo> {
    val clusters = detectClusters()
    val coreInfoList = mutableListOf<CoreInfo>()
    
    clusters.forEach { cluster ->
      cluster.cores.forEach { coreNum ->
        val basePath = "/sys/devices/system/cpu/cpu$coreNum"
        
        // Check if core is online
        val isOnline = if (coreNum == 0) {
          true // Core 0 is always online
        } else {
          RootManager.executeCommand("cat $basePath/online 2>/dev/null")
            .getOrNull()
            ?.trim() == "1"
        }
        
        // Get current frequency (only if online)
        val currentFreq = if (isOnline) {
          RootManager.executeCommand("cat $basePath/cpufreq/scaling_cur_freq 2>/dev/null")
            .getOrNull()
            ?.trim()
            ?.toIntOrNull()?.div(1000) ?: 0
        } else {
          0
        }
        
        coreInfoList.add(
          CoreInfo(
            coreNumber = coreNum,
            currentFreq = currentFreq,
            minFreq = cluster.minFreq,
            maxFreq = cluster.maxFreq,
            governor = cluster.governor,
            isOnline = isOnline,
            cluster = cluster.clusterNumber
          )
        )
      }
    }
    
    return coreInfoList
  }

  /**
   * Get core info for a specific cluster
   */
  suspend fun getCoreInfoByCluster(clusterNumber: Int): List<CoreInfo> {
    return getAllCoreInfo().filter { it.cluster == clusterNumber }
  }

  // CPU Lock Management Methods
  
  /**
   * Check if governors are compatible with frequency locking
   */
  suspend fun checkGovernorCompatibility(cluster: Int): Result<List<String>> {
    return try {
      val clusters = detectClusters()
      val targetCluster = clusters.getOrNull(cluster) 
          ?: return Result.failure(Exception("Cluster not found"))
      
      val compatibleGovernors = listOf("userspace", "performance", "schedutil", "interactive", "ondemand")
      val availableGovernors = targetCluster.availableGovernors
      val compatible = availableGovernors.filter { it in compatibleGovernors }
      
      if (compatible.isNotEmpty()) {
        Result.success(compatible)
      } else {
        Result.failure(Exception("No compatible governors available"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Lock cluster frequency with enhanced verification
   */
  suspend fun lockClusterFrequency(
    cluster: Int, 
    minFreq: Int, 
    maxFreq: Int,
    storeOriginal: Boolean = true
  ): Result<Unit> {
    // First check governor compatibility
    val compatibleResult = checkGovernorCompatibility(cluster)
    if (compatibleResult.isFailure) {
      return Result.failure(
        Exception("Governor compatibility check failed: ${compatibleResult.exceptionOrNull()?.message}")
      )
    }
    
    val result = setClusterFrequency(cluster, minFreq, maxFreq)
    
    if (result.isSuccess) {
      // Verify the lock was applied successfully
      val verificationResult = verifyFrequencyApplication(cluster, minFreq, maxFreq)
      if (verificationResult.isFailure) {
        Log.w(TAG, "Frequency lock verification failed: ${verificationResult.exceptionOrNull()?.message}")
        return Result.failure(Exception("Frequency lock could not be verified"))
      }
      Log.d(TAG, "Successfully locked cluster $cluster to ${minFreq}-${maxFreq}MHz")
    }
    
    return result
  }

  /**
   * Unlock cluster frequency and restore original values
   */
  suspend fun unlockClusterFrequency(cluster: Int): Result<Unit> {
    val clusters = detectClusters()
    val targetCluster = clusters.getOrNull(cluster) 
        ?: return Result.failure(Exception("Cluster not found"))
    
    return setClusterFrequency(
        cluster, 
        targetCluster.minFreq, 
        targetCluster.maxFreq
    )
  }

  /**
   * Get current CPU temperature for thermal monitoring
   */
  suspend fun getCurrentCpuTemperature(): Float {
    return try {
      // Use thermal zones from existing native implementation
      val thermalZones = NativeLib.readThermalZones()
      val cpuZones = thermalZones.filter { 
        it.name.lowercase().contains("cpu") || 
        it.name.lowercase().contains("tsens") ||
        it.name.lowercase().contains("thermal") ||
        it.name.lowercase().contains("soc")
      }
      
      if (cpuZones.isNotEmpty()) {
        val avgTemp = cpuZones.map { it.temp }.average().toFloat()
        Log.d(TAG, "CPU temperature from ${cpuZones.size} zones: ${avgTemp}°C")
        avgTemp
      } else {
        // Fallback to first available zone
        val firstZone = thermalZones.firstOrNull()
        if (firstZone != null) {
          Log.d(TAG, "CPU temperature fallback: ${firstZone.temp}°C (${firstZone.name})")
          firstZone.temp
        } else {
          Log.w(TAG, "No thermal zones available")
          0f
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to get CPU temperature", e)
      0f
    }
  }

  /**
   * Verify that frequency changes were applied successfully
   */
  private suspend fun verifyFrequencyApplication(
    cluster: Int, 
    targetMin: Int, 
    targetMax: Int,
    timeoutMs: Long = 3000
  ): Result<Unit> {
    val startTime = System.currentTimeMillis()
    var attempts = 0
    val maxAttempts = 10
    
    while (System.currentTimeMillis() - startTime < timeoutMs && attempts < maxAttempts) {
      try {
        val currentCluster = detectClusters().getOrNull(cluster)
        if (currentCluster != null) {
          val currentMin = currentCluster.currentMinFreq
          val currentMax = currentCluster.currentMaxFreq
          
          // Allow small tolerance for rounding differences
          val minMatches = kotlin.math.abs(currentMin - targetMin) <= 50
          val maxMatches = kotlin.math.abs(currentMax - targetMax) <= 50
          
          if (minMatches && maxMatches) {
            Log.d(TAG, "Frequency verification successful: $currentMin-$currentMax MHz")
            return Result.success(Unit)
          } else {
            Log.d(TAG, "Frequency verification attempt $attempts: got $currentMin-$currentMax, want $targetMin-$targetMax")
          }
        }
      } catch (e: Exception) {
        Log.w(TAG, "Error during frequency verification attempt $attempts", e)
      }
      
      attempts++
      kotlinx.coroutines.delay(300) // Wait 300ms between attempts
    }
    
    return Result.failure(
      Exception("Frequency verification failed after $attempts attempts")
    )
  }

  /**
   * Get all available thermal zones for monitoring
   */
  suspend fun getAllThermalZones(): List<ThermalZone> {
    return try {
      NativeLib.readThermalZones()
    } catch (e: Exception) {
      Log.e(TAG, "Failed to read thermal zones", e)
      emptyList()
    }
  }

  /**
   * Check if a cluster is currently within a safe temperature range
   */
  suspend fun isClusterSafe(cluster: Int, maxTemp: Float = 75f): Boolean {
    return try {
      val cpuTemp = getCurrentCpuTemperature()
      val isSafe = cpuTemp <= maxTemp
      Log.d(TAG, "Cluster $cluster safety check: $cpuTemp°C <= $maxTemp°C = $isSafe")
      isSafe
    } catch (e: Exception) {
      Log.e(TAG, "Failed to check cluster safety", e)
      true // Assume safe on error
    }
  }

  /**
   * Enhanced frequency detection that reads from multiple kernel sources
   * This fixes issues where scaling_available_frequencies doesn't show all frequencies
   */
  private suspend fun applyDeviceSpecificFrequencies(
    originalFreqs: List<Int>, 
    cluster: ClusterInfo?
  ): List<Int> {
    if (cluster == null) return originalFreqs
    
    Log.d(TAG, "Enhanced frequency detection for cluster ${cluster.clusterNumber}")
    
    val enhancedFreqs = mutableSetOf<Int>()
    enhancedFreqs.addAll(originalFreqs)
    
    // Method 1: Read from cpuinfo_max_freq and cpuinfo_min_freq (hardware limits)
    val hardwareFreqs = readHardwareFrequencyLimits(cluster)
    enhancedFreqs.addAll(hardwareFreqs)
    
    // Method 2: Read from OPP (Operating Performance Points) table
    val oppFreqs = readOppTableFrequencies(cluster)
    enhancedFreqs.addAll(oppFreqs)
    
    // Method 3: Read from cpufreq policy files
    val policyFreqs = readPolicyFrequencies(cluster)
    enhancedFreqs.addAll(policyFreqs)
    
    // Method 4: Read from devfreq if available
    val devfreqFreqs = readDevfreqFrequencies(cluster)
    enhancedFreqs.addAll(devfreqFreqs)
    
    // Method 5: Parse from cpufreq stats
    val statsFreqs = readCpufreqStats(cluster)
    enhancedFreqs.addAll(statsFreqs)
    
    // Method 6: Read from thermal cooling device frequencies
    val thermalFreqs = readThermalCoolingFrequencies(cluster)
    enhancedFreqs.addAll(thermalFreqs)
    
    val finalFreqs = enhancedFreqs.filter { it > 0 }.sorted()
    
    Log.d(TAG, "Frequency detection results:")
    Log.d(TAG, "  Original: ${originalFreqs.size} frequencies (${originalFreqs.minOrNull()}-${originalFreqs.maxOrNull()} MHz)")
    Log.d(TAG, "  Enhanced: ${finalFreqs.size} frequencies (${finalFreqs.minOrNull()}-${finalFreqs.maxOrNull()} MHz)")
    Log.d(TAG, "  Added: ${finalFreqs.size - originalFreqs.size} new frequencies")
    
    return finalFreqs
  }
  
  /**
   * Read hardware frequency limits from cpuinfo files
   */
  private suspend fun readHardwareFrequencyLimits(cluster: ClusterInfo): List<Int> {
    val frequencies = mutableSetOf<Int>()
    
    cluster.cores.forEach { coreNum ->
      val basePath = "/sys/devices/system/cpu/cpu$coreNum/cpufreq"
      
      // Read hardware max frequency
      val maxFreq = RootManager.executeCommand("cat $basePath/cpuinfo_max_freq 2>/dev/null")
        .getOrNull()?.trim()?.toIntOrNull()?.div(1000)
      if (maxFreq != null && maxFreq > 0) {
        frequencies.add(maxFreq)
      }
      
      // Read hardware min frequency
      val minFreq = RootManager.executeCommand("cat $basePath/cpuinfo_min_freq 2>/dev/null")
        .getOrNull()?.trim()?.toIntOrNull()?.div(1000)
      if (minFreq != null && minFreq > 0) {
        frequencies.add(minFreq)
      }
    }
    
    return frequencies.toList()
  }
  
  /**
   * Read frequencies from OPP (Operating Performance Points) table
   */
  private suspend fun readOppTableFrequencies(cluster: ClusterInfo): List<Int> {
    val frequencies = mutableSetOf<Int>()
    
    // Try different OPP table locations
    val oppPaths = listOf(
      "/sys/devices/system/cpu/cpu${cluster.cores.first()}/cpufreq/opp_table",
      "/sys/kernel/debug/opp/cpu${cluster.cores.first()}",
      "/proc/cpufreq/MT_CPU_DVFS_L", // MediaTek
      "/proc/cpufreq/MT_CPU_DVFS_B", // MediaTek
      "/sys/devices/platform/soc/soc:qcom,cpufreq-hw/opp_table" // Qualcomm
    )
    
    oppPaths.forEach { path ->
      val oppData = RootManager.executeCommand("cat $path 2>/dev/null")
        .getOrNull()
      
      if (!oppData.isNullOrBlank()) {
        // Parse OPP table format (frequency voltage pairs)
        val freqPattern = Regex("""(\d+)\s*Hz""")
        val matches = freqPattern.findAll(oppData)
        matches.forEach { match ->
          val freqHz = match.groupValues[1].toLongOrNull()
          if (freqHz != null) {
            val freqMhz = (freqHz / 1000000).toInt()
            if (freqMhz > 0) frequencies.add(freqMhz)
          }
        }
      }
    }
    
    return frequencies.toList()
  }
  
  /**
   * Read frequencies from cpufreq policy files
   */
  private suspend fun readPolicyFrequencies(cluster: ClusterInfo): List<Int> {
    val frequencies = mutableSetOf<Int>()
    
    val policyPath = "/sys/devices/system/cpu/cpufreq/policy${cluster.cores.first()}"
    
    // Read from policy directory
    val policyFiles = listOf(
      "$policyPath/scaling_available_frequencies",
      "$policyPath/scaling_boost_frequencies",
      "$policyPath/cpuinfo_max_freq",
      "$policyPath/cpuinfo_min_freq"
    )
    
    policyFiles.forEach { file ->
      val content = RootManager.executeCommand("cat $file 2>/dev/null")
        .getOrNull()?.trim()
      
      if (!content.isNullOrBlank()) {
        content.split("\\s+".toRegex()).forEach { freqStr ->
          val freq = freqStr.toIntOrNull()?.div(1000)
          if (freq != null && freq > 0) {
            frequencies.add(freq)
          }
        }
      }
    }
    
    return frequencies.toList()
  }
  
  /**
   * Read frequencies from devfreq (if CPU uses devfreq)
   */
  private suspend fun readDevfreqFrequencies(cluster: ClusterInfo): List<Int> {
    val frequencies = mutableSetOf<Int>()
    
    // Check devfreq paths
    val devfreqPaths = listOf(
      "/sys/class/devfreq/soc:qcom,cpufreq-hw-cpu${cluster.cores.first()}",
      "/sys/class/devfreq/cpufreq-cpu${cluster.cores.first()}",
      "/sys/devices/platform/soc/18321000.qcom,cpufreq-hw/devfreq"
    )
    
    devfreqPaths.forEach { basePath ->
      val availableFreqs = RootManager.executeCommand("cat $basePath/available_frequencies 2>/dev/null")
        .getOrNull()?.trim()
      
      if (!availableFreqs.isNullOrBlank()) {
        availableFreqs.split("\\s+".toRegex()).forEach { freqStr ->
          val freq = freqStr.toIntOrNull()?.div(1000)
          if (freq != null && freq > 0) {
            frequencies.add(freq)
          }
        }
      }
    }
    
    return frequencies.toList()
  }
  
  /**
   * Read frequencies from cpufreq stats
   */
  private suspend fun readCpufreqStats(cluster: ClusterInfo): List<Int> {
    val frequencies = mutableSetOf<Int>()
    
    cluster.cores.forEach { coreNum ->
      val statsPath = "/sys/devices/system/cpu/cpu$coreNum/cpufreq/stats/time_in_state"
      val statsContent = RootManager.executeCommand("cat $statsPath 2>/dev/null")
        .getOrNull()
      
      if (!statsContent.isNullOrBlank()) {
        statsContent.lines().forEach { line ->
          val parts = line.trim().split("\\s+".toRegex())
          if (parts.size >= 2) {
            val freq = parts[0].toIntOrNull()?.div(1000)
            if (freq != null && freq > 0) {
              frequencies.add(freq)
            }
          }
        }
      }
    }
    
    return frequencies.toList()
  }
  
  /**
   * Read frequencies from thermal cooling device
   */
  private suspend fun readThermalCoolingFrequencies(cluster: ClusterInfo): List<Int> {
    val frequencies = mutableSetOf<Int>()
    
    // Find thermal cooling devices for CPU
    val coolingDevices = RootManager.executeCommand("find /sys/class/thermal -name 'cooling_device*' -type d 2>/dev/null")
      .getOrNull()?.lines() ?: emptyList()
    
    coolingDevices.forEach { devicePath ->
      val type = RootManager.executeCommand("cat $devicePath/type 2>/dev/null")
        .getOrNull()?.trim()
      
      if (type != null && (type.contains("cpu") || type.contains("cluster"))) {
        // Read available frequencies from thermal cooling device
        val freqTable = RootManager.executeCommand("cat $devicePath/user_vote 2>/dev/null")
          .getOrNull()
        
        if (!freqTable.isNullOrBlank()) {
          val freqPattern = Regex("""(\d+)""")
          val matches = freqPattern.findAll(freqTable)
          matches.forEach { match ->
            val freq = match.groupValues[1].toIntOrNull()?.div(1000)
            if (freq != null && freq > 0) {
              frequencies.add(freq)
            }
          }
        }
      }
    }
    
    return frequencies.toList()
  }
  
  /**
   * Debug function to test frequency detection on current device
   * This can be called from UI for troubleshooting
   */
  suspend fun debugFrequencyDetection(): String {
    val clusters = detectClusters()
    val debugInfo = StringBuilder()
    
    debugInfo.appendLine("=== CPU Frequency Detection Debug ===")
    debugInfo.appendLine("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
    debugInfo.appendLine("Hardware: ${android.os.Build.HARDWARE}")
    debugInfo.appendLine("Board: ${android.os.Build.BOARD}")
    debugInfo.appendLine()
    
    clusters.forEachIndexed { index, cluster ->
      debugInfo.appendLine("--- Cluster $index (Cores: ${cluster.cores.joinToString()}) ---")
      debugInfo.appendLine("Original frequencies: ${cluster.availableFrequencies.size} found")
      debugInfo.appendLine("Range: ${cluster.availableFrequencies.minOrNull()}-${cluster.availableFrequencies.maxOrNull()} MHz")
      debugInfo.appendLine()
      
      // Test each detection method
      val hardwareFreqs = readHardwareFrequencyLimits(cluster)
      debugInfo.appendLine("Hardware limits: ${hardwareFreqs.size} frequencies")
      debugInfo.appendLine("  ${hardwareFreqs.joinToString()}")
      
      val oppFreqs = readOppTableFrequencies(cluster)
      debugInfo.appendLine("OPP table: ${oppFreqs.size} frequencies")
      debugInfo.appendLine("  ${oppFreqs.joinToString()}")
      
      val policyFreqs = readPolicyFrequencies(cluster)
      debugInfo.appendLine("Policy files: ${policyFreqs.size} frequencies")
      debugInfo.appendLine("  ${policyFreqs.joinToString()}")
      
      val devfreqFreqs = readDevfreqFrequencies(cluster)
      debugInfo.appendLine("Devfreq: ${devfreqFreqs.size} frequencies")
      debugInfo.appendLine("  ${devfreqFreqs.joinToString()}")
      
      val statsFreqs = readCpufreqStats(cluster)
      debugInfo.appendLine("Stats: ${statsFreqs.size} frequencies")
      debugInfo.appendLine("  ${statsFreqs.joinToString()}")
      
      val thermalFreqs = readThermalCoolingFrequencies(cluster)
      debugInfo.appendLine("Thermal: ${thermalFreqs.size} frequencies")
      debugInfo.appendLine("  ${thermalFreqs.joinToString()}")
      
      // Show enhanced result
      val enhancedFreqs = applyDeviceSpecificFrequencies(cluster.availableFrequencies, cluster)
      debugInfo.appendLine()
      debugInfo.appendLine("Enhanced result: ${enhancedFreqs.size} frequencies")
      debugInfo.appendLine("Range: ${enhancedFreqs.minOrNull()}-${enhancedFreqs.maxOrNull()} MHz")
      debugInfo.appendLine("Added: ${enhancedFreqs.size - cluster.availableFrequencies.size} new frequencies")
      debugInfo.appendLine()
    }
    
    return debugInfo.toString()
  }
}
