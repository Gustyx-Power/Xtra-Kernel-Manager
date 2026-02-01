package id.xms.xtrakernelmanager.data.repository

import android.util.Log
import id.xms.xtrakernelmanager.data.model.*
import id.xms.xtrakernelmanager.domain.native.NativeLib
import id.xms.xtrakernelmanager.domain.root.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KernelRepository {

  private val TAG = "KernelRepository"
  private var cachedClusters: List<ClusterInfo>? = null
  private var cachedGpuFreqPath: String? = null
  private var cachedGpuBusyPath: String? = null
  private var cachedGpuBasePath: String? = null
  private var cachedCpuThermalPath: String? = null
  private var cachedGpuThermalZone: String? = null

  suspend fun getCPUInfo(): CPUInfo =
      withContext(Dispatchers.IO) {
        val clusters =
            cachedClusters
                ?: run {
                  val nativeClusters = NativeLib.detectCpuClusters()
                  if (nativeClusters != null && nativeClusters.isNotEmpty()) {
                    nativeClusters.also { cachedClusters = it }
                  } else {
                    detectClustersAdvanced().also { cachedClusters = it }
                  }
                }

        val nativeCoreData = NativeLib.readCoreData()

        val cores =
            if (nativeCoreData != null && nativeCoreData.isNotEmpty()) {
              nativeCoreData.map { coreData ->
                val cluster = clusters.find { coreData.core in it.cores }
                CoreInfo(
                    coreNumber = coreData.core,
                    currentFreq = coreData.freq,
                    minFreq = cluster?.minFreq ?: 0,
                    maxFreq = cluster?.maxFreq ?: 0,
                    governor = coreData.governor,
                    isOnline = coreData.online,
                    cluster = cluster?.clusterNumber ?: 0,
                )
              }
            } else {
              clusters.flatMap { cluster ->
                cluster.cores.map { coreNum -> getCoreInfo(coreNum, cluster.clusterNumber) }
              }
            }

        val temp = NativeLib.readCpuTemperature() ?: getCPUTemperature()
        val nativeLoad = NativeLib.readCpuLoad()
        val load = if (nativeLoad != null && nativeLoad > 0.1f) nativeLoad else getCPULoad()

        CPUInfo(cores = cores, clusters = clusters, temperature = temp, totalLoad = load)
      }

  private suspend fun detectClustersAdvanced(): List<ClusterInfo> {
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
              ?.split("\\s+".toRegex())
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

  private suspend fun getCoreInfo(coreNum: Int, cluster: Int): CoreInfo {
    val basePath = "/sys/devices/system/cpu/cpu$coreNum"
    val exists =
        RootManager.executeCommand("[ -d $basePath ] && echo exists").getOrNull()?.trim() ==
            "exists"
    if (!exists) {
      return CoreInfo(
          coreNumber = coreNum,
          currentFreq = 0,
          minFreq = 0,
          maxFreq = 0,
          governor = "offline",
          isOnline = false,
          cluster = cluster,
      )
    }
    val isOnline =
        if (coreNum == 0) {
          true
        } else {
          RootManager.executeCommand("cat $basePath/online 2>/dev/null || echo 1")
              .getOrNull()
              ?.trim() == "1"
        }
    if (!isOnline) {
      return CoreInfo(
          coreNumber = coreNum,
          currentFreq = 0,
          minFreq = 0,
          maxFreq = 0,
          governor = "offline",
          isOnline = false,
          cluster = cluster,
      )
    }
    val currentFreq =
        RootManager.executeCommand("cat $basePath/cpufreq/scaling_cur_freq 2>/dev/null")
            .getOrNull()
            ?.trim()
            ?.toIntOrNull() ?: 0
    val minFreq =
        RootManager.executeCommand("cat $basePath/cpufreq/cpuinfo_min_freq 2>/dev/null")
            .getOrNull()
            ?.trim()
            ?.toIntOrNull() ?: 0
    val maxFreq =
        RootManager.executeCommand("cat $basePath/cpufreq/cpuinfo_max_freq 2>/dev/null")
            .getOrNull()
            ?.trim()
            ?.toIntOrNull() ?: 0
    val governor =
        RootManager.executeCommand("cat $basePath/cpufreq/scaling_governor 2>/dev/null")
            .getOrNull()
            ?.trim() ?: "unknown"
    return CoreInfo(
        coreNumber = coreNum,
        currentFreq = currentFreq / 1000,
        minFreq = minFreq / 1000,
        maxFreq = maxFreq / 1000,
        governor = governor,
        isOnline = true,
        cluster = cluster,
    )
  }

  private suspend fun getCPUTemperature(): Float {
    if (cachedCpuThermalPath != null) {
      val temp = RootManager.executeCommand("cat $cachedCpuThermalPath 2>/dev/null")
          .getOrNull()?.trim()?.toFloatOrNull()
      if (temp != null && temp > 0) {
        return if (temp > 1000) temp / 1000f else temp
      }
    }
    
    val thermalZones = listOf(
        "/sys/class/thermal/thermal_zone0/temp",
        "/sys/devices/virtual/thermal/thermal_zone0/temp",
        "/sys/class/hwmon/hwmon0/temp1_input",
        "/sys/class/hwmon/hwmon1/temp1_input",
    )
    for (zone in thermalZones) {
      val temp = RootManager.executeCommand("cat $zone 2>/dev/null")
          .getOrNull()?.trim()?.toFloatOrNull()
      if (temp != null && temp > 0) {
        cachedCpuThermalPath = zone
        return if (temp > 1000) temp / 1000f else temp
      }
    }
    return 0f
  }

  // Store previous CPU stats for differential calculation
  private var prevCpuIdle: Long = 0L
  private var prevCpuTotal: Long = 0L

  private suspend fun getCPULoad(): Float {
    return try {
      val stat = RootManager.executeCommand("cat /proc/stat").getOrNull() ?: return 0f
      val cpuLine = stat.lines().firstOrNull { it.startsWith("cpu ") } ?: return 0f
      val values = cpuLine.split("\\s+".toRegex()).drop(1).mapNotNull { it.toLongOrNull() }
      if (values.size < 4) return 0f

      val idle = values[3] + (values.getOrNull(4) ?: 0L) // idle + iowait
      val total = values.sum()

      // Calculate differential (delta) load
      val diffIdle = idle - prevCpuIdle
      val diffTotal = total - prevCpuTotal

      // Store current values for next calculation
      prevCpuIdle = idle
      prevCpuTotal = total

      // Return differential CPU load percentage
      if (diffTotal > 0) {
        ((diffTotal - diffIdle).toFloat() / diffTotal.toFloat() * 100f).coerceIn(0f, 100f)
      } else {
        0f
      }
    } catch (e: Exception) {
      Log.e(TAG, "getCPULoad failed: ${e.message}")
      0f
    }
  }

  private suspend fun readProcStatValues(): Pair<Long, Long>? {
    val stat = RootManager.executeCommand("cat /proc/stat").getOrNull() ?: return null
    val cpuLine = stat.lines().firstOrNull { it.startsWith("cpu ") } ?: return null
    val values = cpuLine.split("\\s+".toRegex()).drop(1).mapNotNull { it.toLongOrNull() }
    if (values.size < 5) return null

    val idle = values[3] + values[4] // idle + iowait
    val total = values.sum()
    return Pair(total, idle)
  }

  suspend fun getGPUInfo(): GPUInfo =
      withContext(Dispatchers.IO) {
        var vendor = NativeLib.getGpuVendor()
        var renderer = NativeLib.getGpuModel()

        var openglVersion = "Unknown"
        if (vendor == null || renderer == null) {
          val glesRaw =
              RootManager.executeCommand("dumpsys SurfaceFlinger | grep GLES").getOrNull()?.trim()
                  ?: ""
          if (glesRaw.contains("GLES:")) {
            val clean = glesRaw.substringAfter("GLES:").trim()
            val parts = clean.split(",").map { it.trim() }
            openglVersion =
                parts.firstOrNull { it.contains("OpenGL", ignoreCase = true) } ?: "Unknown"
            if (renderer == null) {
              renderer =
                  parts.firstOrNull {
                    Regex("adreno|mali|powervr|nvidia", RegexOption.IGNORE_CASE).containsMatchIn(it)
                  } ?: (parts.getOrNull(1) ?: "Unknown")
            }
            if (vendor == null) {
              vendor =
                  parts.firstOrNull {
                    Regex("qualcomm|arm|nvidia|imagination", RegexOption.IGNORE_CASE)
                        .containsMatchIn(it)
                  }
                      ?: when {
                        renderer?.contains("Adreno", true) == true -> "Qualcomm"
                        renderer?.contains("Mali", true) == true -> "ARM"
                        renderer?.contains("PowerVR", true) == true -> "Imagination"
                        renderer?.contains("NVIDIA", true) == true -> "NVIDIA"
                        else -> {
                          val soc = NativeLib.getSystemProperty("ro.hardware")?.lowercase() ?: ""
                          when {
                            soc.contains("qcom") -> "Qualcomm"
                            soc.contains("mt") -> "ARM"
                            soc.contains("powervr") -> "Imagination"
                            soc.contains("exynos") -> "ARM"
                            soc.contains("nvidia") -> "NVIDIA"
                            else -> "Unknown"
                          }
                        }
                      }
            }
          }
        }

        val basePath = cachedGpuBasePath ?: run {
          val path = listOf("/sys/class/kgsl/kgsl-3d0", "/sys/kernel/gpu").firstOrNull {
            RootManager.executeCommand("[ -d $it ] && echo exists").getOrNull()?.trim() == "exists"
          } ?: "/sys/class/kgsl/kgsl-3d0"
          cachedGpuBasePath = path
          path
        }

        val availableFreqs =
            RootManager.executeCommand("cat $basePath/gpu_available_frequencies 2>/dev/null")
                .getOrNull()
                ?.split(" ")
                ?.mapNotNull { it.toIntOrNull()?.div(1000000) } ?: emptyList()

        var currentFreq = NativeLib.readGpuFreq() ?: 0
        if (currentFreq == 0) {
          if (cachedGpuFreqPath != null) {
             val rawValue = RootManager.executeCommand("cat $cachedGpuFreqPath 2>/dev/null").getOrNull()?.trim()
             val valueLong = rawValue?.toLongOrNull()
             if (valueLong != null) {
                 currentFreq = when {
                    valueLong > 1_000_000 -> (valueLong / 1_000_000).toInt()
                    valueLong > 1_000 -> (valueLong / 1_000).toInt()
                    else -> valueLong.toInt()
                 }
             }
          } else {
              val freqPaths = listOf(
                  "/sys/class/kgsl/kgsl-3d0/gpuclk",
                  "$basePath/gpuclk",
                  "$basePath/clock_mhz",
                  "/sys/kernel/gpu/gpu_clock",
                  "/sys/class/devfreq/5000000.qcom,kgsl-3d0/cur_freq",
                  "/sys/class/devfreq/gpufreq/cur_freq"
              )
              for (path in freqPaths) {
                val rawValue = RootManager.executeCommand("cat $path 2>/dev/null").getOrNull()?.trim()
                val valueLong = rawValue?.toLongOrNull() ?: continue
                val foundFreq = when {
                  valueLong > 1_000_000 -> (valueLong / 1_000_000).toInt()
                  valueLong > 1_000 -> (valueLong / 1_000).toInt()
                  else -> valueLong.toInt()
                }
                if (foundFreq > 0) {
                    currentFreq = foundFreq
                    cachedGpuFreqPath = path
                    break
                }
              }
          }
        }

        val maxFreq = availableFreqs.maxOrNull() ?: 0
        val minFreq = availableFreqs.minOrNull() ?: 0

        var gpuLoad = NativeLib.readGpuBusy() ?: 0
        if (gpuLoad == 0) {
             if (cachedGpuBusyPath != null) {
                  gpuLoad = RootManager.executeCommand("cat $cachedGpuBusyPath 2>/dev/null")
                        .getOrNull()?.trim()?.split(" ")?.firstOrNull()?.toIntOrNull() ?: 0
             } else {
                 val loadPaths = listOf(
                     "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage",
                     "$basePath/gpu_busy_percentage",
                     "/sys/kernel/gpu/gpu_busy"
                 )
                 for (path in loadPaths) {
                      val loadVal = RootManager.executeCommand("cat $path 2>/dev/null")
                            .getOrNull()?.trim()?.split(" ")?.firstOrNull()?.toIntOrNull()
                      if (loadVal != null) {
                          gpuLoad = loadVal
                          cachedGpuBusyPath = path
                          break
                      }
                 }
             }
        }

        val gpuTemp = if (cachedGpuThermalZone != null) {
          val temp = RootManager.executeCommand("cat $cachedGpuThermalZone/temp 2>/dev/null")
              .getOrNull()?.trim()?.toFloatOrNull()
          if (temp != null && temp > 0) {
            if (temp > 1000) temp / 1000f else temp
          } else {
            // Cache invalid, reset it
            cachedGpuThermalZone = null
            0f
          }
        } else {
          val thermalZones = NativeLib.readThermalZones()
          Log.d("KernelRepository", "Available thermal zones: ${thermalZones.map { it.name }}")
          
          // Try to find GPU-specific thermal zone
          var gpuZone = thermalZones.find { zone ->
            val name = zone.name.lowercase()
            name.contains("gpu") || name.contains("adreno") || name.contains("mali") || name.contains("3d")
          }
          
          // Fallback: try to find zones with "gpuss" or similar patterns
          if (gpuZone == null) {
            gpuZone = thermalZones.find { zone ->
              val name = zone.name.lowercase()
              name.contains("gpuss") || name.contains("gpu-") || name.contains("kgsl")
            }
          }
          
          // Fallback: try common GPU zone numbers (zones 5-15 are often GPU on Qualcomm)
          if (gpuZone == null) {
            gpuZone = thermalZones.find { zone ->
              val zoneNum = zone.name.split(":").firstOrNull()?.toIntOrNull()
              zoneNum != null && zoneNum in 5..15 && zone.temp > 0
            }
          }
          
          if (gpuZone != null) {
            // Extract zone number from format "zone_number:type"
            val zoneNumber = gpuZone.name.split(":").firstOrNull()
            if (zoneNumber != null) {
              cachedGpuThermalZone = "/sys/class/thermal/thermal_zone$zoneNumber"
              Log.d("KernelRepository", "Found GPU thermal zone: ${gpuZone.name} -> $cachedGpuThermalZone, temp: ${gpuZone.temp}°C")
            }
            gpuZone.temp
          } else {
            Log.w("KernelRepository", "No GPU thermal zone found")
            0f
          }
        }

        GPUInfo(
            vendor = vendor ?: "Unknown",
            renderer = renderer ?: "Unknown",
            openglVersion = openglVersion,
            currentFreq = currentFreq,
            minFreq = minFreq,
            maxFreq = maxFreq,
            availableFreqs = availableFreqs,
            gpuLoad = gpuLoad,
            temperature = gpuTemp,
        )
      }

  private suspend fun getSwapTotalSize(): Long {
    return try {
      val output = RootManager.executeCommand("cat /proc/swaps").getOrNull() ?: ""
      val lines = output.lines()
      val totalKb =
          lines
              .drop(1)
              .mapNotNull { line ->
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size >= 3) parts[2].toLongOrNull() else null
              }
              .sum()
      totalKb * 1024L
    } catch (e: Exception) {
      0L
    }
  }

  private suspend fun getSwapUsedSize(): Long {
    return try {
      val output = RootManager.executeCommand("cat /proc/swaps").getOrNull() ?: ""
      val lines = output.lines()
      val usedKb =
          lines
              .drop(1)
              .mapNotNull { line ->
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size >= 4) parts[3].toLongOrNull() else null
              }
              .sum()
      usedKb * 1024L
    } catch (e: Exception) {
      0L
    }
  }

  suspend fun getSystemInfo(): SystemInfo =
      withContext(Dispatchers.IO) {
        val androidVersion = android.os.Build.VERSION.RELEASE
        val abi = android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
        val kernelVersion = System.getProperty("os.version") ?: "unknown"

        // Brand detection and cleanup
        val rawManufacturer = android.os.Build.MANUFACTURER
        val rawModel = android.os.Build.MODEL
        val rawBrand = android.os.Build.BRAND
        val rawProduct = android.os.Build.PRODUCT
        val rawDevice = android.os.Build.DEVICE

        // Clean up device model - remove redundant "Xiaomi" prefix for POCO/Redmi
        val cleanedModel =
            when {
              rawModel.contains("POCO", ignoreCase = true) -> rawModel
              rawModel.contains("Redmi", ignoreCase = true) -> rawModel
              rawManufacturer.equals("OnePlus", ignoreCase = true) -> rawModel
              else -> "$rawManufacturer $rawModel"
            }

        // Determine clean brand name
        val cleanBrand =
            when {
              rawModel.contains("POCO", ignoreCase = true) -> "POCO"
              rawModel.contains("Redmi", ignoreCase = true) -> "Redmi"
              rawBrand.equals("OnePlus", ignoreCase = true) ||
                  rawManufacturer.equals("OnePlus", ignoreCase = true) -> "OnePlus"
              rawBrand.equals("Xiaomi", ignoreCase = true) ||
                  rawManufacturer.equals("Xiaomi", ignoreCase = true) -> "Xiaomi"
              else -> rawBrand.ifEmpty { rawManufacturer }
            }

        val deviceModel = cleanedModel
        val fingerprint = android.os.Build.FINGERPRINT
        val fingerprintType = if (fingerprint.contains("test-keys")) "test-key" else "release-key"
        val selinux =
            RootManager.executeCommand("getenforce 2>/dev/null").getOrNull()?.trim() ?: "Unknown"

        // Use native MemInfo for faster reading
        val memInfo = NativeLib.readMemInfo()
        val (totalRam, availableRam) =
            if (memInfo != null) {
              Pair(memInfo.totalKb, memInfo.availableKb)
            } else {
              // Fallback to shell if native fails
              try {
                val memInfoRaw = RootManager.executeCommand("cat /proc/meminfo").getOrNull() ?: ""
                val total =
                    memInfoRaw
                        .lines()
                        .firstOrNull { it.startsWith("MemTotal:") }
                        ?.split("\\s+".toRegex())
                        ?.get(1)
                        ?.toLongOrNull() ?: 0L
                val available =
                    memInfoRaw
                        .lines()
                        .firstOrNull { it.startsWith("MemAvailable:") }
                        ?.split("\\s+".toRegex())
                        ?.get(1)
                        ?.toLongOrNull() ?: 0L
                Pair(total, available)
              } catch (e: Exception) {
                Pair(0L, 0L)
              }
            }

        // Use native ZRAM reading
        val zramSize =
            NativeLib.readZramSize()
                ?: RootManager.executeCommand("cat /sys/block/zram0/disksize 2>/dev/null")
                    .getOrNull()
                    ?.trim()
                    ?.toLongOrNull()
                ?: 0L

        val statFs = android.os.StatFs(android.os.Environment.getDataDirectory().path)
        val totalStorage = statFs.totalBytes
        val availableStorage = statFs.availableBytes

        // Use native swap info
        val swapTotalBytes =
            if (memInfo != null) {
              memInfo.swapTotalKb * 1024
            } else {
              getSwapTotalSize()
            }
        val swapFreeBytes =
            if (memInfo != null) {
              memInfo.swapFreeKb * 1024
            } else {
              swapTotalBytes - getSwapUsedSize()
            }

        val zramUsed =
            try {
              val mmStat =
                  RootManager.executeCommand("cat /sys/block/zram0/mm_stat 2>/dev/null")
                      .getOrNull()
                      ?.trim()
              if (!mmStat.isNullOrBlank()) {
                mmStat.split("\\s+".toRegex()).firstOrNull()?.toLongOrNull() ?: 0L
              } else {
                0L
              }
            } catch (e: Exception) {
              0L
            }

        SystemInfo(
            androidVersion = androidVersion,
            abi = abi,
            kernelVersion = kernelVersion,
            deviceModel = deviceModel,
            brand = cleanBrand,
            product = rawProduct,
            marketName = rawDevice,
            fingerprint = fingerprintType,
            selinux = selinux,
            totalRam = totalRam * 1024,
            availableRam = availableRam * 1024,
            zramSize = zramSize,
            zramUsed = zramUsed,
            totalStorage = totalStorage,
            availableStorage = availableStorage,
            swapTotal = swapTotalBytes,
            swapFree = swapFreeBytes,
            deepSleep = android.os.SystemClock.elapsedRealtime() - android.os.SystemClock.uptimeMillis(),
        )
      }
}
