package id.xms.xtrakernelmanager.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.service.BatteryInfoService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
        private const val BATTERY_SERVICE_DELAY_MS = 15000L // 15 seconds delay for Android 15+
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed received")

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Start BatteryInfoService if enabled (with delay for Android 15+)
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
                Log.d(TAG, "Battery notification enabled, scheduling service start...")
                
                // For Android 15+ (API 35+), dataSync FGS cannot start directly from BOOT_COMPLETED
                // We need to delay the start to avoid ForegroundServiceStartNotAllowedException
                if (Build.VERSION.SDK_INT >= 35) {
                    Log.d(TAG, "Android 15+ detected, using delayed start (${BATTERY_SERVICE_DELAY_MS}ms)")
                    scheduleDelayedServiceStart(context)
                } else {
                    // For older versions, start immediately
                    startBatteryServiceDirect(context)
                }
            } else {
                Log.d(TAG, "Battery notification disabled, skipping service start")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start BatteryInfoService: ${e.message}")
        }
    }
    
    private fun scheduleDelayedServiceStart(context: Context) {
        // Use Handler with delay for more reliable start on Android 15+
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val preferencesManager = PreferencesManager(context)
                        val showBatteryNotif = preferencesManager.isShowBatteryNotif().first()
                        
                        if (showBatteryNotif) {
                            startBatteryServiceDirect(context)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Delayed service start failed: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Handler post failed: ${e.message}")
            }
        }, BATTERY_SERVICE_DELAY_MS)
    }
    
    private fun startBatteryServiceDirect(context: Context) {
        try {
            val serviceIntent = Intent(context, BatteryInfoService::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d(TAG, "BatteryInfoService started successfully")
        } catch (e: android.app.ForegroundServiceStartNotAllowedException) {
            // Android 15+ has stricter restrictions - this should be caught now
            Log.w(TAG, "ForegroundService not allowed: ${e.message}")
            Log.w(TAG, "Service will be started when user opens the app")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start BatteryInfoService: ${e.message}")
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
