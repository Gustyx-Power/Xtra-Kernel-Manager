package id.xms.xtrakernelmanager.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import id.xms.xtrakernelmanager.MainActivity
import id.xms.xtrakernelmanager.R

object DonationNotificationHelper {
    private const val CHANNEL_ID = "donation_reminder_v2" // Changed to v2 for new settings
    private const val NOTIFICATION_ID = 1001
    private const val TAG = "DonationNotif"

    fun createNotificationChannel(context: Context) {
        Log.d(TAG, "Creating notification channel...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.donation_notification_channel_name)
            val descriptionText = context.getString(R.string.donation_notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH // Changed to HIGH for visibility
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(true)
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created: $name with importance HIGH")
        }
    }

    fun showDonationNotification(context: Context) {
        Log.d(TAG, "Showing donation notification")
        
        try {
            createNotificationChannel(context)

            // Intent to open MainActivity with donation flag
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("show_donation_dialog", true)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.donation_notification_title))
                .setContentText(context.getString(R.string.donation_notification_message))
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(context.getString(R.string.donation_dialog_message))
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false) // Don't auto-dismiss when tapped
                .setOngoing(false) // User can swipe to dismiss
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVibrate(longArrayOf(0, 250, 250, 250))
                .addAction(
                    R.drawable.ic_launcher_foreground,
                    context.getString(R.string.donation_support_button),
                    pendingIntent
                )
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
            
            Log.d(TAG, "Notification posted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification: ${e.message}", e)
        }
    }

    fun cancelDonationNotification(context: Context) {
        Log.d(TAG, "Cancelling donation notification")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        Log.d(TAG, "Notification cancelled")
    }
}
