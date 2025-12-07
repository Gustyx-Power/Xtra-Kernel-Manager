package id.xms.xtrakernelmanager.domain.usecase

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import id.xms.xtrakernelmanager.domain.root.RootManager

class GameControlUseCase(private val context: Context) {

    companion object {
        private const val TAG = "GameControlUseCase"
    }

    /**
     * Check if DND permission is granted
     */
    fun hasDNDPermission(): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }

    /**
     * Enable Do Not Disturb mode
     * Returns Result with specific error messages
     */
    suspend fun enableDND(): Result<Boolean> {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                    Log.d(TAG, "DND enabled successfully")
                    Result.success(true)
                } else {
                    Log.e(TAG, "DND permission not granted")
                    Result.failure(DNDPermissionException("DND permission not granted. Please grant access in Settings."))
                }
            } else {
                Result.failure(Exception("DND not supported on Android below Marshmallow"))
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception enabling DND", e)
            Result.failure(DNDPermissionException("Security error: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling DND", e)
            Result.failure(e)
        }
    }

    /**
     * Disable Do Not Disturb mode
     */
    suspend fun disableDND(): Result<Boolean> {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    Log.d(TAG, "DND disabled successfully")
                    Result.success(true)
                } else {
                    Log.e(TAG, "DND permission not granted")
                    Result.failure(DNDPermissionException("DND permission not granted"))
                }
            } else {
                Result.success(false)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception disabling DND", e)
            Result.failure(DNDPermissionException("Security error: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error disabling DND", e)
            Result.failure(e)
        }
    }

    fun isDNDEnabled(): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
        } else {
            false
        }
    }

    /**
     * Custom exception for DND permission issues
     */
    class DNDPermissionException(message: String) : Exception(message)

    // Clear RAM with freed amount calculation
    suspend fun clearRAM(): Result<ClearRamResult> {
        return try {
            // Get available RAM before clearing
            val memInfoBefore = RootManager.executeCommand("cat /proc/meminfo | grep MemAvailable")
                .getOrNull()?.let { parseMemValue(it) } ?: 0L
            
            // Execute clear commands
            val commands = listOf(
                "sync",
                "echo 3 > /proc/sys/vm/drop_caches",
                "am kill-all"
            )

            var success = false
            for (cmd in commands) {
                val result = RootManager.executeCommand(cmd)
                if (result.isSuccess) success = true
            }

            // Small delay to let system update
            kotlinx.coroutines.delay(500)

            // Get available RAM after clearing
            val memInfoAfter = RootManager.executeCommand("cat /proc/meminfo | grep MemAvailable")
                .getOrNull()?.let { parseMemValue(it) } ?: 0L

            // Calculate freed RAM in MB
            val freedMB = ((memInfoAfter - memInfoBefore) / 1024).coerceAtLeast(0)

            if (success) {
                Result.success(ClearRamResult(freedMB, memInfoAfter / 1024))
            } else {
                Result.failure(Exception("Failed to clear RAM"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Parse memory value from /proc/meminfo (format: "MemAvailable:    1234567 kB")
    private fun parseMemValue(line: String): Long {
        return try {
            line.split(Regex("\\s+"))
                .getOrNull(1)
                ?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    data class ClearRamResult(
        val freedMB: Long,      // RAM yang berhasil dibersihkan (MB)
        val availableMB: Long   // RAM tersedia sekarang (MB)
    )

    // Performance Mode
    suspend fun setPerformanceMode(mode: String): Result<Boolean> {
        val governorPath = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"

        val governor = when (mode) {
            "performance" -> "performance"
            "balanced" -> "schedutil"
            "battery" -> "powersave"
            else -> "schedutil"
        }

        // Set thermal preset based on mode
        val thermalPreset = when (mode) {
            "performance" -> "Dynamic"
            "balanced" -> "Thermal 20"
            "battery" -> "Incalls"
            else -> "Dynamic"
        }

        // Apply governor
        val governorResult = RootManager.executeCommand("echo $governor > $governorPath")
        
        // Apply thermal preset
        val thermalUseCase = ThermalControlUseCase()
        try {
            thermalUseCase.setThermalMode(thermalPreset, false)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set thermal preset: ${e.message}")
        }

        return governorResult.map { true }
    }

    suspend fun getCurrentPerformanceMode(): String {
        val governorPath = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"
        val governor = RootManager.readFile(governorPath).getOrNull()?.trim() ?: "unknown"

        return when (governor) {
            "performance" -> "performance"
            "powersave" -> "battery"
            "schedutil", "interactive", "ondemand" -> "balanced"
            else -> "balanced"
        }
    }
}
