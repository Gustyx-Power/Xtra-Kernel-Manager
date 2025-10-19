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

    suspend fun setCpuFrequency(core: Int, minFreq: Long, maxFreq: Long): Boolean {
        return SysfsUtils.setCpuFrequency(core, minFreq, maxFreq)
    }

    suspend fun setCpuGovernor(core: Int, governor: String): Boolean {
        return SysfsUtils.setCpuGovernor(core, governor)
    }

    suspend fun setCpuOnline(core: Int, online: Boolean): Boolean {
        return SysfsUtils.setCpuOnline(core, online)
    }

    suspend fun getGpuInfo(): GpuInfo = withContext(Dispatchers.IO) {
        GpuInfo(
            currentFreq = SysfsUtils.getGpuFreq() ?: 0,
            minFreq = SysfsUtils.readSysfsFile("/sys/class/kgsl/kgsl-3d0/devfreq/min_freq")
                ?.toLongOrNull() ?: 0,
            maxFreq = SysfsUtils.getGpuMaxFreq() ?: 0,
            availableFreqs = SysfsUtils.getGpuAvailableFreqs(),
            governor = SysfsUtils.readSysfsFile("/sys/class/kgsl/kgsl-3d0/devfreq/governor") ?: "",
            powerLevel = SysfsUtils.readSysfsFile("/sys/class/kgsl/kgsl-3d0/max_pwrlevel")
                ?.toIntOrNull() ?: 0
        )
    }

    suspend fun setGpuFrequency(minFreq: Long, maxFreq: Long): Boolean {
        return SysfsUtils.setGpuFreq(minFreq, maxFreq)
    }

    suspend fun setGpuPowerLevel(level: Int): Boolean {
        return SysfsUtils.writeSysfsFile("/sys/class/kgsl/kgsl-3d0/max_pwrlevel", level.toString())
    }

    suspend fun setGpuRenderer(renderer: String): Boolean = withContext(Dispatchers.IO) {
        val propCommand = when (renderer.lowercase()) {
            "vulkan" -> "setprop debug.hwui.renderer vulkan"
            "opengl", "skiagl" -> "setprop debug.hwui.renderer skiagl"
            else -> return@withContext false
        }
        RootUtils.executeCommand(propCommand).isSuccess
    }

    suspend fun setSwappiness(value: Int): Boolean {
        return SysfsUtils.setSwappiness(value)
    }

    suspend fun setZramSize(sizeBytes: Long): Boolean = withContext(Dispatchers.IO) {
        val commands = arrayOf(
            "swapoff /dev/block/zram0",
            "echo 1 > /sys/block/zram0/reset",
            "echo $sizeBytes > /sys/block/zram0/disksize",
            "mkswap /dev/block/zram0",
            "swapon /dev/block/zram0"
        )
        RootUtils.executeCommands(*commands).isSuccess
    }

    suspend fun setDirtyRatio(ratio: Int): Boolean {
        return SysfsUtils.writeSysfsFile("/proc/sys/vm/dirty_ratio", ratio.toString())
    }

    suspend fun setMinFreeKbytes(kbytes: Int): Boolean {
        return SysfsUtils.writeSysfsFile("/proc/sys/vm/min_free_kbytes", kbytes.toString())
    }

    suspend fun setIoScheduler(device: String, scheduler: String): Boolean {
        return SysfsUtils.writeSysfsFile("/sys/block/$device/queue/scheduler", scheduler)
    }

    suspend fun setTcpCongestion(algorithm: String): Boolean {
        return SysfsUtils.writeSysfsFile("/proc/sys/net/ipv4/tcp_congestion_control", algorithm)
    }

    suspend fun setThermalMode(mode: String): Boolean = withContext(Dispatchers.IO) {
        val index = when (mode) {
            "Dynamic" -> 10
            "Thermal 20" -> 20
            "Not Set" -> 0
            "Incalls" -> 8
            else -> 0
        }
        SysfsUtils.writeSysfsFile("/sys/module/msm_thermal/parameters/enabled", index.toString())
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
