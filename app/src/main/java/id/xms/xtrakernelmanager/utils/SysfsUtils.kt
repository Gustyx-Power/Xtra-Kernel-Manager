package id.xms.xtrakernelmanager.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object SysfsUtils {

    suspend fun readSysfsFile(path: String): String? = withContext(Dispatchers.IO) {
        try {
            if (File(path).exists()) {
                File(path).readText().trim()
            } else {
                RootUtils.readFile(path)
            }
        } catch (e: Exception) {
            RootUtils.readFile(path)
        }
    }

    suspend fun writeSysfsFile(path: String, value: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(path).writeText(value)
            true
        } catch (e: Exception) {
            RootUtils.writeFile(path, value)
        }
    }

    suspend fun getCpuCoreCount(): Int = withContext(Dispatchers.IO) {
        val present = readSysfsFile(Constants.CPU_PRESENT) ?: return@withContext 0
        val range = present.split("-")
        if (range.size == 2) {
            range[1].toIntOrNull()?.plus(1) ?: 0
        } else {
            0
        }
    }

    suspend fun getOnlineCores(): List<Int> = withContext(Dispatchers.IO) {
        val online = readSysfsFile(Constants.CPU_ONLINE) ?: return@withContext emptyList()
        parseCpuRange(online)
    }

    suspend fun getCpuFreq(core: Int): Long? = withContext(Dispatchers.IO) {
        readSysfsFile("${Constants.SYS_CPU}/cpu$core/cpufreq/scaling_cur_freq")?.toLongOrNull()
    }

    suspend fun getCpuMaxFreq(core: Int): Long? = withContext(Dispatchers.IO) {
        readSysfsFile("${Constants.SYS_CPU}/cpu$core/cpufreq/scaling_max_freq")?.toLongOrNull()
    }

    suspend fun getCpuMinFreq(core: Int): Long? = withContext(Dispatchers.IO) {
        readSysfsFile("${Constants.SYS_CPU}/cpu$core/cpufreq/scaling_min_freq")?.toLongOrNull()
    }

    suspend fun getCpuGovernor(core: Int): String? = withContext(Dispatchers.IO) {
        readSysfsFile("${Constants.SYS_CPU}/cpu$core/cpufreq/scaling_governor")
    }

    suspend fun getAvailableGovernors(core: Int): List<String> = withContext(Dispatchers.IO) {
        readSysfsFile("${Constants.SYS_CPU}/cpu$core/cpufreq/scaling_available_governors")
            ?.split(" ")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    suspend fun getAvailableGpuGovernors(): List<String> = withContext(Dispatchers.IO) {
        try {
            val governorsPath = "/sys/class/kgsl/kgsl-3d0/devfreq/available_governors"
            val governors = readSysfsFile(governorsPath)
            governors?.trim()?.split("\\s+".toRegex()) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun getAvailableFrequencies(core: Int): List<Long> = withContext(Dispatchers.IO) {
        readSysfsFile("${Constants.SYS_CPU}/cpu$core/cpufreq/scaling_available_frequencies")
            ?.split(" ")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
    }

    suspend fun setCpuFrequency(core: Int, minFreq: Long, maxFreq: Long): Boolean =
        withContext(Dispatchers.IO) {
            val minResult = writeSysfsFile(
                "${Constants.SYS_CPU}/cpu$core/cpufreq/scaling_min_freq",
                minFreq.toString()
            )
            val maxResult = writeSysfsFile(
                "${Constants.SYS_CPU}/cpu$core/cpufreq/scaling_max_freq",
                maxFreq.toString()
            )
            minResult && maxResult
        }

    suspend fun setCpuGovernor(core: Int, governor: String): Boolean = withContext(Dispatchers.IO) {
        writeSysfsFile("${Constants.SYS_CPU}/cpu$core/cpufreq/scaling_governor", governor)
    }

    suspend fun setCpuOnline(core: Int, online: Boolean): Boolean = withContext(Dispatchers.IO) {
        if (core == 0) return@withContext false // Cannot disable CPU0
        writeSysfsFile("${Constants.SYS_CPU}/cpu$core/online", if (online) "1" else "0")
    }

    suspend fun detectCpuClusters(): Map<String, List<Int>> = withContext(Dispatchers.IO) {
        val clusters = mutableMapOf<String, MutableList<Int>>()
        val coreCount = getCpuCoreCount()

        // Group cores by max frequency
        val frequencyGroups = mutableMapOf<Long, MutableList<Int>>()

        for (core in 0 until coreCount) {
            val maxFreq = getCpuMaxFreq(core) ?: continue
            frequencyGroups.getOrPut(maxFreq) { mutableListOf() }.add(core)
        }

        // Sort frequency groups (ascending)
        val sortedGroups = frequencyGroups.entries.sortedBy { it.key }

        when (sortedGroups.size) {
            1 -> {
                // All cores same frequency (homogeneous)
                clusters["Little"] = sortedGroups[0].value
            }
            2 -> {
                // 2 clusters: Little and Big
                clusters["Little"] = sortedGroups[0].value
                clusters["Big"] = sortedGroups[1].value
            }
            3 -> {
                // 3 clusters: Little, Big, Prime
                clusters["Little"] = sortedGroups[0].value
                clusters["Big"] = sortedGroups[1].value
                clusters["Prime"] = sortedGroups[2].value
            }
            else -> {
                // More than 3 clusters (rare), group by performance
                // Assume first group is Little, rest are Big
                clusters["Little"] = sortedGroups[0].value
                val bigCores = mutableListOf<Int>()
                for (i in 1 until sortedGroups.size) {
                    bigCores.addAll(sortedGroups[i].value)
                }
                clusters["Big"] = bigCores
            }
        }

        // Sort cores in each cluster
        clusters.forEach { (key, value) ->
            clusters[key] = value.sorted().toMutableList()
        }

        clusters
    }


    suspend fun getBatteryCapacity(): Int = withContext(Dispatchers.IO) {
        readSysfsFile(Constants.BATTERY_CAPACITY)?.toIntOrNull() ?: 0
    }

    suspend fun getBatteryTemp(): Float = withContext(Dispatchers.IO) {
        val temp = readSysfsFile(Constants.BATTERY_TEMP)?.toFloatOrNull() ?: 0f
        temp / 10f // Convert from decidegrees to degrees
    }

    suspend fun getBatteryVoltage(): Float = withContext(Dispatchers.IO) {
        val voltage = readSysfsFile(Constants.BATTERY_VOLTAGE)?.toLongOrNull() ?: 0L
        voltage / 1000000f // Convert to volts
    }

    suspend fun getBatteryCurrent(): Float = withContext(Dispatchers.IO) {
        val current = readSysfsFile(Constants.BATTERY_CURRENT)?.toLongOrNull() ?: 0L
        current / 1000000f // Convert to amps
    }

    suspend fun getBatteryHealth(): String = withContext(Dispatchers.IO) {
        readSysfsFile(Constants.BATTERY_HEALTH) ?: "Unknown"
    }

    suspend fun getBatteryCycleCount(): Int = withContext(Dispatchers.IO) {
        readSysfsFile(Constants.BATTERY_CYCLE)?.toIntOrNull() ?: 0
    }

    suspend fun getBatteryChargeFull(): Long = withContext(Dispatchers.IO) {
        readSysfsFile(Constants.BATTERY_CHARGE_FULL)?.toLongOrNull() ?: 0L
    }

    suspend fun getBatteryChargeFullDesign(): Long = withContext(Dispatchers.IO) {
        readSysfsFile(Constants.BATTERY_CHARGE_FULL_DESIGN)?.toLongOrNull() ?: 0L
    }

    suspend fun getGpuFreq(): Long? = withContext(Dispatchers.IO) {
        readSysfsFile(Constants.GPU_FREQ)?.toLongOrNull()
    }

    suspend fun getGpuMaxFreq(): Long? = withContext(Dispatchers.IO) {
        readSysfsFile(Constants.GPU_MAX_FREQ)?.toLongOrNull()
    }

    suspend fun getGpuAvailableFreqs(): List<Long> = withContext(Dispatchers.IO) {
        readSysfsFile(Constants.GPU_AVAILABLE_FREQS)
            ?.split(" ")?.mapNotNull { it.toLongOrNull() }?.sorted() ?: emptyList()
    }

    suspend fun setGpuFreq(minFreq: Long, maxFreq: Long): Boolean = withContext(Dispatchers.IO) {
        val minResult = writeSysfsFile(Constants.GPU_MIN_FREQ, minFreq.toString())
        val maxResult = writeSysfsFile(Constants.GPU_MAX_FREQ, maxFreq.toString())
        minResult && maxResult
    }

    suspend fun setSwappiness(value: Int): Boolean = withContext(Dispatchers.IO) {
        writeSysfsFile(Constants.VM_SWAPPINESS, value.toString())
    }

    suspend fun getSwappiness(): Int = withContext(Dispatchers.IO) {
        readSysfsFile(Constants.VM_SWAPPINESS)?.toIntOrNull() ?: 60
    }

    suspend fun isMediaTekDevice(): Boolean = withContext(Dispatchers.IO) {
        val hardware = readSysfsFile("/proc/cpuinfo")?.lowercase() ?: ""
        hardware.contains("mediatek") || hardware.contains("mtk")
    }

    private fun parseCpuRange(range: String): List<Int> {
        val result = mutableListOf<Int>()
        range.split(",").forEach { part ->
            if (part.contains("-")) {
                val (start, end) = part.split("-").map { it.toInt() }
                result.addAll(start..end)
            } else {
                result.add(part.toInt())
            }
        }
        return result
    }
}
