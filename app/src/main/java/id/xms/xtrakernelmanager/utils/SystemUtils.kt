package id.xms.xtrakernelmanager.utils

import android.content.Context
import android.os.Build
import id.xms.xtrakernelmanager.domain.root.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SystemUtils {

    suspend fun getKernelVersion(): String = withContext(Dispatchers.IO) {
        System.getProperty("os.version") ?: "Unknown"
    }

    suspend fun getDeviceModel(): String = withContext(Dispatchers.IO) {
        "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    suspend fun getAndroidVersion(): String = withContext(Dispatchers.IO) {
        Build.VERSION.RELEASE
    }

    suspend fun getSELinuxStatus(): String = withContext(Dispatchers.IO) {
        RootManager.executeCommand("getenforce").getOrNull()?.trim() ?: "Unknown"
    }

    suspend fun isMediaTekDevice(): Boolean = withContext(Dispatchers.IO) {
        val soc = RootManager.readFile("/sys/devices/soc0/soc_id")
            .getOrNull()?.lowercase() ?: ""
        soc.contains("mediatek") || soc.contains("mt") || Build.HARDWARE.lowercase().contains("mt")
    }

    fun formatUptime(uptimeMillis: Long): String {
        val seconds = uptimeMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "${days}d ${hours % 24}h"
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
}
