package id.xms.xtrakernelmanager.domain.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.MainActivity
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.splash.fetchBetaUpdateConfig
import id.xms.xtrakernelmanager.ui.splash.fetchUpdateConfig
import id.xms.xtrakernelmanager.ui.splash.isUpdateAvailable

class UpdateCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "UpdateCheckWorker"
        private const val CHANNEL_ID = "xkm_update_channel"
        private const val NOTIFICATION_ID_RELEASE = 1001
        private const val NOTIFICATION_ID_BETA = 1002
        private const val PREF_NAME = "update_notification_prefs"
        private const val KEY_LAST_NOTIFIED_RELEASE = "last_notified_release_version"
        private const val KEY_LAST_NOTIFIED_BETA = "last_notified_beta_version"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting update check...")
            
            // Create notification channel
            createNotificationChannel()
            
            // Check for release updates
            checkReleaseUpdate()
            
            // Check for beta updates
            checkBetaUpdate()
            
            Log.d(TAG, "Update check completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates: ${e.message}", e)
            Result.retry()
        }
    }

    private suspend fun checkReleaseUpdate() {
        try {
            val updateConfig = fetchUpdateConfig() ?: return
            val currentVersion = BuildConfig.VERSION_NAME
            
            // Check if update is available
            if (!isUpdateAvailable(currentVersion, updateConfig.version)) {
                Log.d(TAG, "No release update available")
                return
            }
            
            // Check if we already notified for this version
            val prefs = applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val lastNotifiedVersion = prefs.getString(KEY_LAST_NOTIFIED_RELEASE, "") ?: ""
            
            if (lastNotifiedVersion == updateConfig.version) {
                Log.d(TAG, "Already notified for release version: ${updateConfig.version}")
                return
            }
            
            // Show notification
            showUpdateNotification(
                notificationId = NOTIFICATION_ID_RELEASE,
                title = "XKM Update Available",
                message = "Version ${updateConfig.version} is now available!",
                channelType = "release"
            )
            
            // Save notified version
            prefs.edit().putString(KEY_LAST_NOTIFIED_RELEASE, updateConfig.version).apply()
            Log.d(TAG, "Notified for release update: ${updateConfig.version}")
        } catch (e: Exception) {
            Log.e(TAG, "Error checking release update: ${e.message}", e)
        }
    }

    private suspend fun checkBetaUpdate() {
        try {
            val updateConfig = fetchBetaUpdateConfig() ?: return
            val currentVersion = BuildConfig.VERSION_NAME
            
            // Check if update is available
            if (!isUpdateAvailable(currentVersion, updateConfig.version)) {
                Log.d(TAG, "No beta update available")
                return
            }
            
            // Check if we already notified for this version
            val prefs = applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val lastNotifiedVersion = prefs.getString(KEY_LAST_NOTIFIED_BETA, "") ?: ""
            
            if (lastNotifiedVersion == updateConfig.version) {
                Log.d(TAG, "Already notified for beta version: ${updateConfig.version}")
                return
            }
            
            // Show notification
            showUpdateNotification(
                notificationId = NOTIFICATION_ID_BETA,
                title = "XKM Beta Update Available",
                message = "Beta version ${updateConfig.version} is now available!",
                channelType = "beta"
            )
            
            // Save notified version
            prefs.edit().putString(KEY_LAST_NOTIFIED_BETA, updateConfig.version).apply()
            Log.d(TAG, "Notified for beta update: ${updateConfig.version}")
        } catch (e: Exception) {
            Log.e(TAG, "Error checking beta update: ${e.message}", e)
        }
    }

    private fun showUpdateNotification(
        notificationId: Int,
        title: String,
        message: String,
        channelType: String
    ) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create intent to open app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_system_info", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(if (channelType == "beta") 0xFFFF9800.toInt() else 0xFF4CAF50.toInt())
            .build()
        
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Notification shown: $title")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "XKM Updates"
            val descriptionText = "Notifications for XKM app updates"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
