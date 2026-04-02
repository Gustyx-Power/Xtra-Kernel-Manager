package id.xms.xtrakernelmanager.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import id.xms.xtrakernelmanager.domain.worker.DonationReminderWorker
import java.util.concurrent.TimeUnit

object DonationReminderScheduler {
    private const val WORK_NAME = "donation_reminder_work"
    private const val TAG = "DonationReminderScheduler"

    fun scheduleDonationReminder(context: Context) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .build()

            // Schedule periodic work every 6 hours (will check internally if 3 days passed)
            // More frequent checks to catch date changes faster
            val workRequest = PeriodicWorkRequestBuilder<DonationReminderWorker>(
                6, TimeUnit.HOURS,
                1, TimeUnit.HOURS // Flex interval
            )
                .setConstraints(constraints)
                .setInitialDelay(5, TimeUnit.MINUTES) // Check 5 minutes after boot/app start
                .addTag("donation_reminder")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
                workRequest
            )

            Log.d(TAG, "Donation reminder scheduled successfully (every 6 hours)")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling donation reminder", e)
        }
    }

    fun cancelDonationReminder(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Donation reminder cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling donation reminder", e)
        }
    }

    fun checkAndShowImmediately(context: Context) {
        try {
            // Create one-time work to check immediately
            val workRequest = OneTimeWorkRequestBuilder<DonationReminderWorker>()
                .setInitialDelay(2, TimeUnit.SECONDS)
                .addTag("donation_immediate_check")
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
            Log.d(TAG, "Immediate donation check scheduled")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling immediate check", e)
        }
    }
    
    /**
     * Force show donation notification for testing
     * This will show notification regardless of last shown time
     */
    fun forceShowNotificationForTesting(context: Context) {
        try {
            Log.d(TAG, "Force showing donation notification for testing")
            id.xms.xtrakernelmanager.utils.DonationNotificationHelper.showDonationNotification(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error force showing notification", e)
        }
    }
}
