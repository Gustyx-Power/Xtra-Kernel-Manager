package id.xms.xtrakernelmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.utils.DonationNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Receiver to detect date/time changes and check if donation notification should be shown
 * Triggers on:
 * - TIME_SET (when user changes time/date manually)
 * - DATE_CHANGED (when date changes at midnight)
 * - TIMEZONE_CHANGED (when timezone changes)
 */
class DateChangeReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "DateChangeReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "DateChangeReceiver triggered: ${intent.action}")
        
        // Use goAsync for background work
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                when (intent.action) {
                    Intent.ACTION_TIME_CHANGED,
                    Intent.ACTION_DATE_CHANGED,
                    Intent.ACTION_TIMEZONE_CHANGED -> {
                        // Small delay to ensure system is ready
                        delay(2000)
                        checkAndShowDonationNotification(context)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in onReceive", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
    
    private suspend fun checkAndShowDonationNotification(context: Context) {
        try {
            val preferencesManager = PreferencesManager(context)
            val shouldShow = preferencesManager.shouldShowDonationDialog()
            
            if (shouldShow) {
                Log.d(TAG, "Showing donation notification")
                DonationNotificationHelper.showDonationNotification(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking donation notification", e)
        }
    }
}
