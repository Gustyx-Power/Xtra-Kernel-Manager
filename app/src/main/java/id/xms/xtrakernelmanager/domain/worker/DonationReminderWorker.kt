package id.xms.xtrakernelmanager.domain.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.utils.DonationNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DonationReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "DonationWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "Donation Reminder Worker STARTED")
        Log.d(TAG, "========================================")
        
        try {
            val preferencesManager = PreferencesManager(applicationContext)
            Log.d(TAG, "PreferencesManager created")
            
            // Check if should show donation dialog
            val shouldShow = preferencesManager.shouldShowDonationDialog()
            Log.d(TAG, "shouldShowDonationDialog result: $shouldShow")
            
            if (shouldShow) {
                Log.d(TAG, "Attempting to show donation notification...")
                DonationNotificationHelper.showDonationNotification(applicationContext)
                Log.d(TAG, "Notification shown successfully!")
            } else {
                Log.d(TAG, "Not time to show donation notification yet")
            }
            
            Log.d(TAG, "========================================")
            Log.d(TAG, "Donation Reminder Worker FINISHED")
            Log.d(TAG, "========================================")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "ERROR in donation reminder worker!")
            Log.e(TAG, "Error message: ${e.message}")
            Log.e(TAG, "Stack trace:", e)
            Log.e(TAG, "========================================")
            Result.failure()
        }
    }
}
