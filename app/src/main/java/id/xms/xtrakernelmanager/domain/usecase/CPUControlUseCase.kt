package id.xms.xtrakernelmanager.domain.usecase

import android.util.Log
import id.xms.xtrakernelmanager.data.model.ClusterInfo
import id.xms.xtrakernelmanager.domain.native.NativeLib
import id.xms.xtrakernelmanager.domain.native.NativeLib.ThermalZone
import id.xms.xtrakernelmanager.domain.root.RootManager

class CPUControlUseCase {

  private val TAG = "CPUControlUseCase"
  /** Detect CPU clusters - tries native implementation first, falls back to shell-based */
  suspend fun detectClusters(): List<ClusterInfo> {
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
          normalizedCluster.copy(availableFrequencies = freqs)
        } else {
          normalizedCluster
        }
      }
    }

    // Fallback to shell-based detection
    Log.d(TAG, "Falling back to shell-based cluster detection")
    return detectClustersShell()
  }

  /** Original shell-based cluster detection (fallback) */
  private suspend fun detectClustersShell(): List<ClusterInfo> {

    val clusters = mutableListOf<ClusterInfo>()
    val availableCores = mutableListOf<Int>()
    for (i in 0..15) {
      val exists =
          RootManager.executeCommand("[ -d /sys/devices/system/cpu/cpu$i ] && echo exists")
              .getOrNull()
              ?.trim() == "exists"
      if (exists) {
        availableCores.add(i)
      }
    }
    if (availableCores.isEmpty()) return emptyList()

    val coreGroups = mutableMapOf<Int, MutableList<Int>>()
    for (core in availableCores) {
      val maxFreq =
          RootManager.executeCommand(
                  "cat /sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_max_freq 2>/dev/null"
              )
              .getOrNull()
              ?.trim()
              ?.toIntOrNull() ?: 0
      if (maxFreq > 0) {
        val group = coreGroups.getOrPut(maxFreq) { mutableListOf() }
        group.add(core)
      }
    }

    val sortedGroups = coreGroups.entries.sortedBy { it.key }
    sortedGroups.forEachIndexed { clusterIndex, (maxFreq, coresInGroup) ->
      val firstCore = coresInGroup.first()
      val basePath = "/sys/devices/system/cpu/cpu$firstCore"
      val minFreq =
          RootManager.executeCommand("cat $basePath/cpufreq/cpuinfo_min_freq 2>/dev/null")
              .getOrNull()
              ?.trim()
              ?.toIntOrNull() ?: 0
      val currentMin =
          RootManager.executeCommand("cat $basePath/cpufreq/scaling_min_freq 2>/dev/null")
              .getOrNull()
              ?.trim()
              ?.toIntOrNull() ?: minFreq
      val currentMax =
          RootManager.executeCommand("cat $basePath/cpufreq/scaling_max_freq 2>/dev/null")
              .getOrNull()
              ?.trim()
              ?.toIntOrNull() ?: maxFreq
      val governor =
          RootManager.executeCommand("cat $basePath/cpufreq/scaling_governor 2>/dev/null")
              .getOrNull()
              ?.trim() ?: "schedutil"
      val availableGovs =
          RootManager.executeCommand(
                  "cat $basePath/cpufreq/scaling_available_governors 2>/dev/null"
              )
              .getOrNull()
              ?.trim()
              ?.split(" ")
              ?.filter { it.isNotBlank() }
              ?: listOf("schedutil", "performance", "powersave", "ondemand", "conservative")
      val availableFreqs =
          RootManager.executeCommand(
                  "cat $basePath/cpufreq/scaling_available_frequencies 2>/dev/null"
              )
              .getOrNull()
              ?.trim()
              ?.split(" ")
              ?.mapNotNull { it.toIntOrNull()?.div(1000) }
              ?: emptyList()

      val policyPath = "/sys/devices/system/cpu/cpufreq/policy${firstCore}"
      clusters.add(
          ClusterInfo(
              clusterNumber = clusterIndex,
              cores = coresInGroup,
              minFreq = minFreq / 1000,
              maxFreq = maxFreq / 1000,
              currentMinFreq = currentMin / 1000,
              currentMaxFreq = currentMax / 1000,
              governor = governor,
              availableGovernors = availableGovs,
              availableFrequencies = availableFreqs,
              policyPath = policyPath,
          )
      )
    }
    return clusters
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
    return Result.success(Unit)
  }

  suspend fun setCoreOnline(core: Int, online: Boolean): Result<Unit> {
    if (core == 0) return Result.success(Unit)
    val corePath = "/sys/devices/system/cpu/cpu$core"
    val value = if (online) "1" else "0"
    return RootManager.executeCommand("echo $value > $corePath/online 2>/dev/null").map { Unit }
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
}
