package id.xms.xtrakernelmanager.domain.usecase

import android.util.Log
import id.xms.xtrakernelmanager.data.model.ClusterInfo
import id.xms.xtrakernelmanager.domain.native.NativeLib
import id.xms.xtrakernelmanager.domain.root.RootManager

class CPUControlUseCase {

    private val TAG = "CPUControlUseCase"

    /**
     * Detect CPU clusters - tries native implementation first, falls back to shell-based
     */
    suspend fun detectClusters(): List<ClusterInfo> {
        // Try native implementation first (faster, no shell overhead)
        val nativeClusters = NativeLib.detectCpuClusters()
        if (nativeClusters != null && nativeClusters.isNotEmpty()) {
            Log.d(TAG, "Using native cluster detection: ${nativeClusters.size} clusters")
            return nativeClusters
        }
        
        // Fallback to shell-based detection
        Log.d(TAG, "Falling back to shell-based cluster detection")
        return detectClustersShell()
    }

    /**
     * Original shell-based cluster detection (fallback)
     */
    private suspend fun detectClustersShell(): List<ClusterInfo> {

        val clusters = mutableListOf<ClusterInfo>()
        val availableCores = mutableListOf<Int>()
        for (i in 0..15) {
            val exists = RootManager.executeCommand("[ -d /sys/devices/system/cpu/cpu$i ] && echo exists")
                .getOrNull()?.trim() == "exists"
            if (exists) {
                availableCores.add(i)
            }
        }
        if (availableCores.isEmpty()) return emptyList()

        val coreGroups = mutableMapOf<Int, MutableList<Int>>()
        for (core in availableCores) {
            val maxFreq = RootManager.executeCommand("cat /sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_max_freq 2>/dev/null")
                .getOrNull()?.trim()?.toIntOrNull() ?: 0
            if (maxFreq > 0) {
                val group = coreGroups.getOrPut(maxFreq) { mutableListOf() }
                group.add(core)
            }
        }

        val sortedGroups = coreGroups.entries.sortedBy { it.key }
        sortedGroups.forEachIndexed { clusterIndex, (maxFreq, coresInGroup) ->
            val firstCore = coresInGroup.first()
            val basePath = "/sys/devices/system/cpu/cpu$firstCore"
            val minFreq = RootManager.executeCommand("cat $basePath/cpufreq/cpuinfo_min_freq 2>/dev/null")
                .getOrNull()?.trim()?.toIntOrNull() ?: 0
            val currentMin = RootManager.executeCommand("cat $basePath/cpufreq/scaling_min_freq 2>/dev/null")
                .getOrNull()?.trim()?.toIntOrNull() ?: minFreq
            val currentMax = RootManager.executeCommand("cat $basePath/cpufreq/scaling_max_freq 2>/dev/null")
                .getOrNull()?.trim()?.toIntOrNull() ?: maxFreq
            val governor = RootManager.executeCommand("cat $basePath/cpufreq/scaling_governor 2>/dev/null")
                .getOrNull()?.trim() ?: "schedutil"
            val availableGovs = RootManager.executeCommand("cat $basePath/cpufreq/scaling_available_governors 2>/dev/null")
                .getOrNull()?.trim()?.split(" ")?.filter { it.isNotBlank() }
                ?: listOf("schedutil", "performance", "powersave", "ondemand", "conservative")
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
                    policyPath = policyPath
                )
            )
        }
        return clusters
    }

    suspend fun setClusterFrequency(cluster: Int, minFreq: Int, maxFreq: Int): Result<Unit> {
        val clusters = detectClusters()
        val targetCluster = clusters.getOrNull(cluster) ?: return Result.failure(Exception("Cluster not found"))
        targetCluster.cores.forEach { coreNum ->
            val basePath = "/sys/devices/system/cpu/cpu$coreNum"
            RootManager.executeCommand("echo ${maxFreq * 1000} > $basePath/cpufreq/scaling_max_freq 2>/dev/null")
            RootManager.executeCommand("echo ${minFreq * 1000} > $basePath/cpufreq/scaling_min_freq 2>/dev/null")
        }
        return Result.success(Unit)
    }

    suspend fun setClusterGovernor(cluster: Int, governor: String): Result<Unit> {
        val clusters = detectClusters()
        val targetCluster = clusters.getOrNull(cluster) ?: return Result.failure(Exception("Cluster not found"))
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
        return RootManager.executeCommand("echo $value > $corePath/online 2>/dev/null")
            .map { Unit }
    }
}
