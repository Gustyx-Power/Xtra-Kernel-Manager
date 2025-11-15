package id.xms.xtrakernelmanager.domain.usecase

import id.xms.xtrakernelmanager.domain.root.RootManager

class GameOverlayUseCase {

    suspend fun getCurrentFPS(): Int {
        val drmPath = "/sys/class/drm/card0/fbc/fps"

        return RootManager.readFile(drmPath)
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
        val gpuBusyPath = "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage"

        return RootManager.readFile(gpuBusyPath)
            .getOrNull()?.trim()?.toFloatOrNull() ?: 0f
    }

    suspend fun getTemperature(): Float {
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
