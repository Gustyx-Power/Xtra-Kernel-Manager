package id.xms.xtrakernelmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.service.BatteryInfoService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed received")

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Start BatteryInfoService if enabled
                    startBatteryServiceIfEnabled(context)
                    
                    // Activate swap file
                    activateSwapFile()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
    
    private suspend fun startBatteryServiceIfEnabled(context: Context) {
        try {
            val preferencesManager = PreferencesManager(context)
            val showBatteryNotif = preferencesManager.isShowBatteryNotif().first()
            
            if (showBatteryNotif) {
                Log.d("BootReceiver", "Battery notification enabled, starting service...")
                val serviceIntent = Intent(context, BatteryInfoService::class.java)
                
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    Log.d("BootReceiver", "BatteryInfoService started on boot")
                } catch (e: android.app.ForegroundServiceStartNotAllowedException) {
                    // Android 16+ has stricter restrictions on foreground service start from boot
                    Log.w("BootReceiver", "ForegroundService not allowed from boot on Android 16+: ${e.message}")
                    // Service will be started when user opens the app
                }
            } else {
                Log.d("BootReceiver", "Battery notification disabled, skipping service start")
            }
        } catch (e: Exception) {
            Log.e("BootReceiver", "Failed to start BatteryInfoService: ${e.message}")
        }
    }

    private suspend fun activateSwapFile() {
        val swapPath = "/data/swap/swapfile"

        // Check if swap file exists
        val checkExists = RootManager.executeCommand("test -f $swapPath && echo exists || echo notfound")
        if (checkExists.getOrNull()?.trim() != "exists") {
            Log.d("BootReceiver", "Swap file not found, skipping activation")
            return
        }

        // Check if swap is already active
        val checkActive = RootManager.executeCommand("grep -q '$swapPath' /proc/swaps && echo active || echo inactive")
        if (checkActive.getOrNull()?.trim() == "active") {
            Log.d("BootReceiver", "Swap file already active")
            return
        }

        // Activate swap file
        Log.d("BootReceiver", "Activating swap file...")
        val result = RootManager.executeCommand("swapon $swapPath 2>&1")

        if (result.isSuccess) {
            Log.d("BootReceiver", "Swap file activated successfully")
        } else {
            Log.e("BootReceiver", "Failed to activate swap file: ${result.exceptionOrNull()?.message}")
        }
    }
}
