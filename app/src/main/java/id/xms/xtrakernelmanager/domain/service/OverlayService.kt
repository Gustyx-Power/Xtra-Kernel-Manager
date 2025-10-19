package id.xms.xtrakernelmanager.domain.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.screens.misc.GameControlOverlay

class OverlayService : Service() {

    private var overlay: GameControlOverlay? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_OVERLAY -> startOverlay()
            ACTION_STOP_OVERLAY -> stopOverlay()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startOverlay() {
        if (overlay == null) {
            overlay = GameControlOverlay(this)
            overlay?.show()
        }
    }

    private fun stopOverlay() {
        overlay?.hide()
        overlay = null
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Game Control Overlay"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Xtra Kernel Manager")
            .setContentText("Game overlay is active")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopOverlay()
    }

    companion object {
        private const val CHANNEL_ID = "overlay_service_channel"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_START_OVERLAY = "id.xms.xtrakernelmanager.START_OVERLAY"
        const val ACTION_STOP_OVERLAY = "id.xms.xtrakernelmanager.STOP_OVERLAY"
    }
}
