package id.xms.xtrakernelmanager.data.repository

import id.xms.xtrakernelmanager.data.model.*
import id.xms.xtrakernelmanager.utils.RootUtils
import id.xms.xtrakernelmanager.utils.SysfsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KernelRepository {

    suspend fun getCpuInfo(): CpuInfo = withContext(Dispatchers.IO) {
        val coreCount = SysfsUtils.getCpuCoreCount()
        val onlineCores = SysfsUtils.getOnlineCores()
        val clusters = SysfsUtils.detectCpuClusters()

        val cores = (0 until coreCount).map { core ->
            CpuCore(
                coreNumber = core,
                online = onlineCores.contains(core),
                currentFreq = SysfsUtils.getCpuFreq(core) ?: 0,
                minFreq = SysfsUtils.getCpuMinFreq(core) ?: 0,
                maxFreq = SysfsUtils.getCpuMaxFreq(core) ?: 0,
                governor = SysfsUtils.getCpuGovernor(core) ?: "",
                availableGovernors = SysfsUtils.getAvailableGovernors(core),
                availableFrequencies = SysfsUtils.getAvailableFrequencies(core)
            )
        }

        val load = calculateCpuLoad()
        val temp = getCpuTemperature()

        CpuInfo(
            coreCount = coreCount,
            onlineCores = onlineCores,
            cores = cores,
            clusters = clusters,
            load = load,
            temperature = temp
        )
    }

    suspend fun setCpuFrequency(core: Int, frequency: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val maxFreqPath = "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_max_freq"
            val minFreqPath = "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_min_freq"

            RootUtils.writeFile(maxFreqPath, frequency.toString()) &&
                    RootUtils.writeFile(minFreqPath, frequency.toString())
        } catch (e: Exception) {
            false
        }
    }

    suspend fun setCpuGovernor(core: Int, governor: String): Boolean = withContext(Dispatchers.IO) {
        SysfsUtils.setCpuGovernor(core, governor)
    }

    suspend fun setCpuOnline(core: Int, online: Boolean): Boolean = withContext(Dispatchers.IO) {
        SysfsUtils.setCpuOnline(core, online)
    }

    suspend fun getGpuInfo(): GpuInfo = withContext(Dispatchers.IO) {
        val currentFreq = SysfsUtils.getGpuFreq() ?: 0
        val minFreq = SysfsUtils.readSysfsFile("/sys/class/kgsl/kgsl-3d0/devfreq/min_freq")
            ?.toLongOrNull() ?: 0
        val maxFreq = SysfsUtils.getGpuMaxFreq() ?: 0
        val availableFreqs = SysfsUtils.getGpuAvailableFreqs()
        val governor = SysfsUtils.readSysfsFile("/sys/class/kgsl/kgsl-3d0/devfreq/governor") ?: ""
        val availableGovernors = SysfsUtils.getAvailableGpuGovernors()
        val powerLevel = SysfsUtils.readSysfsFile("/sys/class/kgsl/kgsl-3d0/max_pwrlevel")
            ?.toIntOrNull() ?: 0

        GpuInfo(
            currentFreq = currentFreq,
            minFreq = minFreq,
            maxFreq = maxFreq,
            availableFreqs = availableFreqs,
            governor = governor,
            availableGovernors = availableGovernors,
            powerLevel = powerLevel
        )
    }

    suspend fun setGpuFrequency(frequency: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val maxFreqPath = "/sys/class/kgsl/kgsl-3d0/devfreq/max_freq"
            val minFreqPath = "/sys/class/kgsl/kgsl-3d0/devfreq/min_freq"

            RootUtils.writeFile(maxFreqPath, frequency.toString()) &&
                    RootUtils.writeFile(minFreqPath, frequency.toString())
        } catch (e: Exception) {
            false
        }
    }

    suspend fun setGpuGovernor(governor: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val path = "/sys/class/kgsl/kgsl-3d0/devfreq/governor"
            RootUtils.writeFile(path, governor)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun setGpuPowerLevel(level: Int): Boolean = withContext(Dispatchers.IO) {
        SysfsUtils.writeSysfsFile("/sys/class/kgsl/kgsl-3d0/max_pwrlevel", level.toString())
    }

    suspend fun setGpuRenderer(renderer: String): Boolean = withContext(Dispatchers.IO) {
        val propCommand = when (renderer.lowercase()) {
            "vulkan" -> "setprop debug.hwui.renderer vulkan"
            "opengl", "skiagl" -> "setprop debug.hwui.renderer skiagl"
            else -> return@withContext false
        }

        RootUtils.executeCommand(propCommand).isSuccess
    }

    suspend fun getSwappiness(): Int = withContext(Dispatchers.IO) {
        SysfsUtils.getSwappiness()
    }

    suspend fun setSwappiness(value: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            RootUtils.writeFile("/proc/sys/vm/swappiness", value.toString())
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getZramSize(): Long = withContext(Dispatchers.IO) {
        SysfsUtils.readSysfsFile("/sys/block/zram0/disksize")?.toLongOrNull() ?: 0L
    }

    suspend fun setZramSize(sizeBytes: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val commands = arrayOf(
                "swapoff /dev/block/zram0",
                "echo 1 > /sys/block/zram0/reset",
                "echo $sizeBytes > /sys/block/zram0/disksize",
                "mkswap /dev/block/zram0",
                "swapon /dev/block/zram0"
            )

            RootUtils.executeCommands(*commands).isSuccess
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getSwapSize(): Long = withContext(Dispatchers.IO) {
        try {
            val swapInfo = RootUtils.executeCommand("cat /proc/swaps").getOrNull()
            // Parse swap file size from output
            // Format: Filename Type Size Used Priority
            swapInfo?.lines()?.getOrNull(1)?.split("\\s+".toRegex())?.getOrNull(2)?.toLongOrNull()?.times(1024) ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    suspend fun setSwapSize(sizeBytes: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val swapFile = "/data/swap/swapfile"
            val sizeMB = sizeBytes / (1024 * 1024)

            if (sizeBytes == 0L) {
                // Disable swap
                RootUtils.executeCommand("swapoff $swapFile")
                RootUtils.executeCommand("rm -f $swapFile")
            } else {
                // Create swap file
                val commands = arrayOf(
                    "mkdir -p /data/swap",
                    "dd if=/dev/zero of=$swapFile bs=1M count=$sizeMB",
                    "chmod 600 $swapFile",
                    "mkswap $swapFile",
                    "swapon $swapFile"
                )
                RootUtils.executeCommands(*commands).isSuccess
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getThermalMode(): String = withContext(Dispatchers.IO) {
        try {
            val mode = RootUtils.readFile("/sys/class/thermal/thermal_message/sconfig")
            when (mode?.trim()) {
                "10" -> "Dynamic"
                "20" -> "Thermal 20"
                "8" -> "Incalls"
                else -> "Not Set"
            }
        } catch (e: Exception) {
            "Not Set"
        }
    }

    suspend fun setThermalMode(mode: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val value = when (mode) {
                "Dynamic" -> "10"
                "Thermal 20" -> "20"
                "Incalls" -> "8"
                else -> "0" // Not Set
            }

            RootUtils.writeFile("/sys/class/thermal/thermal_message/sconfig", value)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun setDirtyRatio(ratio: Int): Boolean = withContext(Dispatchers.IO) {
        SysfsUtils.writeSysfsFile("/proc/sys/vm/dirty_ratio", ratio.toString())
    }

    suspend fun setMinFreeKbytes(kbytes: Int): Boolean = withContext(Dispatchers.IO) {
        SysfsUtils.writeSysfsFile("/proc/sys/vm/min_free_kbytes", kbytes.toString())
    }

    suspend fun setIoScheduler(device: String, scheduler: String): Boolean = withContext(Dispatchers.IO) {
        SysfsUtils.writeSysfsFile("/sys/block/$device/queue/scheduler", scheduler)
    }

    suspend fun setTcpCongestion(algorithm: String): Boolean = withContext(Dispatchers.IO) {
        SysfsUtils.writeSysfsFile("/proc/sys/net/ipv4/tcp_congestion_control", algorithm)
    }

    private suspend fun calculateCpuLoad(): Float = withContext(Dispatchers.IO) {
        val loadAvg = SysfsUtils.readSysfsFile("/proc/loadavg")
        loadAvg?.split(" ")?.firstOrNull()?.toFloatOrNull() ?: 0f
    }

    private suspend fun getCpuTemperature(): Float = withContext(Dispatchers.IO) {
        val temp = SysfsUtils.readSysfsFile("/sys/class/thermal/thermal_zone0/temp")
        temp?.toFloatOrNull()?.div(1000) ?: 0f
    }
}
