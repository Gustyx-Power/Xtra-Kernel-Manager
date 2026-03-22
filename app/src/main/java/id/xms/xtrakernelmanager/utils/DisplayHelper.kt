package id.xms.xtrakernelmanager.utils

import android.content.Context
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DisplayHelper {
    private const val TAG = "DisplayHelper"
    
    suspend fun isDCDimmingEnabled(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val dcDimmingPaths = listOf(
                    "/sys/devices/platform/soc/soc:qcom,dsi-display-primary/dimlayer_hbm",
                    "/sys/devices/platform/soc/soc:qcom,dsi-display-primary/dimlayer_bl_en",
                    "/sys/devices/virtual/graphics/fb0/dimlayer_hbm",
                    "/sys/class/drm/card0-DSI-1/dimlayer_hbm",
                    "/sys/class/mi_display/disp-DSI-0/dimlayer_hbm"
                )
                
                for (path in dcDimmingPaths) {
                    try {
                        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat $path"))
                        val output = process.inputStream.bufferedReader().readText().trim()
                        process.waitFor()
                        
                        if (output == "1" || output.contains("enable", ignoreCase = true)) {
                            Log.d(TAG, "DC Dimming detected as enabled at: $path")
                            return@withContext true
                        }
                    } catch (e: Exception) {
                        continue
                    }
                }
                
                val miuiDcDimming = Settings.System.getInt(
                    context.contentResolver,
                    "dc_back_light",
                    0
                )
                if (miuiDcDimming == 1) {
                    Log.d(TAG, "DC Dimming detected via MIUI settings")
                    return@withContext true
                }
                
                false
            } catch (e: Exception) {
                Log.e(TAG, "Error checking DC Dimming status", e)
                false
            }
        }
    }
    
    suspend fun getCurrentBrightness(context: Context): Int {
        return withContext(Dispatchers.IO) {
            try {
                val brightness = Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    128
                )
                brightness
            } catch (e: Exception) {
                Log.e(TAG, "Error getting brightness", e)
                128
            }
        }
    }
    
    fun isLowBrightness(brightness: Int, maxBrightness: Int = 255): Boolean {
        val percentage = (brightness.toFloat() / maxBrightness) * 100
        return percentage < 30f
    }
}
