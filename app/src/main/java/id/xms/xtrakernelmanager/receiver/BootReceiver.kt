package id.xms.xtrakernelmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import id.xms.xtrakernelmanager.domain.root.RootManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed, activating swap file...")

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    activateSwapFile()
                } finally {
                    pendingResult.finish()
                }
            }
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

