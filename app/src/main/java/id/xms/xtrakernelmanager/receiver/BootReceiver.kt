package id.xms.xtrakernelmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import id.xms.xtrakernelmanager.utils.PreferenceManager
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private val TAG = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Boot event received: ${intent.action}")

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                handleBootCompleted(context)
            }
        }
    }

    private fun handleBootCompleted(context: Context) {
        Log.d(TAG, "Handling boot completed event")
        // No services to start on boot now that BatteryStatsService has been removed
        Log.d(TAG, "No services to start on boot")
    }
}
