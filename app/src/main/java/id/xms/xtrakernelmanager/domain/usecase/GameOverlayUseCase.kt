package id.xms.xtrakernelmanager.domain.usecase

import id.xms.xtrakernelmanager.domain.root.RootManager
import java.io.File

class GameOverlayUseCase {

    suspend fun getCurrentFPS(): Int {
        // Baca dari sde-crtc-0/measured_fps yang formatnya "fps: XX.XX duration: YY"
        val drmRaw = RootManager.executeCommand("cat /sys/class/drm/sde-crtc-0/measured_fps")
            .getOrNull() ?: ""

        if (drmRaw.contains("fps:")) {
            val fps = drmRaw
                .substringAfter("fps:")
                .substringBefore("duration")
                .trim()
                .toFloatOrNull()?.toInt() ?: 0
            if (fps > 0) return fps
        }

        // Fallback ke path lama jika sde-crtc-0 tidak tersedia
        val fallbackPath = "/sys/class/drm/card0/fbc/fps"
        return RootManager.readFile(fallbackPath)
            .getOrNull()?.trim()?.toIntOrNull() ?: 60
    }

    suspend fun getMaxCPUFreq(): Int {
        val freqs = mutableListOf<Int>()

        for (i in 0..7) {
            val freq = RootManager.readFile("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
                .getOrNull()?.trim()?.toIntOrNull()
            if (freq != null && freq > 0) {
                freqs.add(freq / 1000)
            }
        }

        return freqs.maxOrNull() ?: 0
    }

    suspend fun getCPULoad(): Float {
        // Perbaikan: Gunakan root access untuk membaca /proc/stat
        return try {
            val stat = RootManager.readFile("/proc/stat").getOrNull() ?: return 0f
            val cpuLine = stat.lines().firstOrNull { it.startsWith("cpu ") } ?: return 0f

            val values = cpuLine.split("\\s+".toRegex()).drop(1).mapNotNull { it.toLongOrNull() }
            if (values.size < 4) return 0f

            val idle = values[3]
            val total = values.sum()

            if (total > 0) ((total - idle).toFloat() / total.toFloat()) * 100f else 0f
        } catch (e: Exception) {
            0f
        }
    }

    suspend fun getGPULoad(): Float {
        // Berbagai path GPU untuk berbagai chipset
        val gpuPaths = listOf(
            // Adreno (Qualcomm)
            "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage",
            "/sys/class/kgsl/kgsl-3d0/devfreq/gpu_load",
            "/sys/class/devfreq/5000000.qcom,kgsl-3d0/gpu_load",
            // Mali (MediaTek/Samsung Exynos)
            "/sys/class/misc/mali0/device/utilization",
            "/sys/kernel/gpu/gpu_busy",
            "/sys/devices/platform/mali.0/utilization",
            "/sys/devices/platform/13000000.mali/utilization",
            "/sys/devices/platform/soc/5000000.qcom,kgsl-3d0/kgsl/kgsl-3d0/gpu_busy_percentage",
            // PowerVR
            "/sys/kernel/debug/pvr/status",
            // Generic devfreq paths
            "/sys/class/devfreq/gpufreq/load"
        )

        for (path in gpuPaths) {
            try {
                val result = RootManager.readFile(path).getOrNull()?.trim()
                if (result != null && result.isNotEmpty()) {
                    // Handle format "XX %" atau "XX"
                    val cleanValue = result.replace("%", "").trim().split(" ").firstOrNull()
                    val value = cleanValue?.toFloatOrNull()
                    if (value != null && value >= 0) {
                        return value
                    }
                }
            } catch (_: Exception) {
                continue
            }
        }

        // Fallback: Try to read from debugfs (requires root)
        try {
            val debugResult = RootManager.executeCommand("cat /sys/kernel/debug/kgsl/kgsl-3d0/busy_percentage 2>/dev/null")
                .getOrNull()?.trim()
            debugResult?.toFloatOrNull()?.let { return it }
        } catch (_: Exception) {}

        return 0f
    }

    /**
     * Get battery temperature as the main temperature source
     * This is more reliable across all devices
     */
    suspend fun getTemperature(): Float {
        // Priority 1: Battery temperature (most reliable)
        val batteryTempPaths = listOf(
            "/sys/class/power_supply/battery/temp",
            "/sys/class/power_supply/battery/batt_temp"
        )

        for (path in batteryTempPaths) {
            try {
                val temp = RootManager.readFile(path).getOrNull()?.trim()?.toFloatOrNull()
                if (temp != null && temp > 0) {
                    // Battery temp is in tenths of degree Celsius (e.g., 350 = 35.0°C)
                    return temp / 10f
                }
            } catch (_: Exception) {
                continue
            }
        }

        // Priority 2: Try reading without root first (for battery)
        try {
            val batteryTemp = File("/sys/class/power_supply/battery/temp")
            if (batteryTemp.exists() && batteryTemp.canRead()) {
                val temp = batteryTemp.readText().trim().toFloatOrNull()
                if (temp != null && temp > 0) {
                    return temp / 10f
                }
            }
        } catch (_: Exception) {}

        // Fallback: Thermal zones
        val thermalZones = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp"
        )

        for (zone in thermalZones) {
            val temp = RootManager.readFile(zone).getOrNull()?.trim()?.toFloatOrNull()
            if (temp != null) {
                return temp / 1000f
            }
        }

        return 0f
    }
}
