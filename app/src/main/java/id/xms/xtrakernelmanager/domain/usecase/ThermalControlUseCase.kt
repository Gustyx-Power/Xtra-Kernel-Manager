package id.xms.xtrakernelmanager.domain.usecase

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class ThermalControlUseCase {

    suspend fun setThermalMode(preset: String, setOnBoot: Boolean): Result<Unit> {
        val index = when (preset) {
            "Class 0" -> 11
            "Extreme" -> 2
            "Dynamic" -> 10
            "Incalls" -> 8
            "Thermal 20" -> 20
            else -> 0
        }

        val sconfigPath = "/sys/class/thermal/thermal_message/sconfig"
        val logTag = "ThermalControlUseCase"

        try {
            Log.d(logTag, "CHMOD sconfig to 666")
            Runtime.getRuntime().exec(arrayOf("su", "-c", "chmod 666 $sconfigPath")).waitFor()

            val cmd = "echo $index | tee $sconfigPath"
            Log.d(logTag, "Set thermal mode with: $cmd")
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val error = BufferedReader(InputStreamReader(process.errorStream)).readText()
            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val exitCode = process.waitFor()
            Log.d(logTag, "Set thermal tee output: $output")
            Log.d(logTag, "Set thermal tee error: $error")
            Log.d(logTag, "Set thermal tee exitCode: $exitCode")

            Log.d(logTag, "CHMOD sconfig back to 444")
            Runtime.getRuntime().exec(arrayOf("su", "-c", "chmod 444 $sconfigPath")).waitFor()

            if (exitCode != 0 || error.isNotBlank()) {
                return Result.failure(Exception("Thermal write failed: $error"))
            }
        } catch (e: Exception) {
            Log.e(logTag, "Exception during thermal mode set", e)
            return Result.failure(e)
        }

        return Result.success(Unit)
    }
}
