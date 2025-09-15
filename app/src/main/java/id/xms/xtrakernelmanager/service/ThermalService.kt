package id.xms.xtrakernelmanager.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.AndroidEntryPoint
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.repository.ThermalRepository
import id.xms.xtrakernelmanager.ui.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class ThermalService : Service() {
    @Inject
    lateinit var thermalRepository: ThermalRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitoringJob: Job? = null
    private val TAG = "ThermalService"
    private var isRootAvailable = false

    private val thermalDataStore: DataStore<Preferences> by preferencesDataStore(name = "thermal_settings")
    private val LAST_THERMAL_MODE = intPreferencesKey("last_thermal_mode")

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "thermal_service_channel"
        private const val MONITOR_INTERVAL = 1000L // 1 second
        private const val MAX_RETRY_COUNT = 3
    }

    override fun onCreate() {
        super.onCreate()
        checkRootAccess()
        createNotificationChannelSafely()
        startForegroundSafely()
    }

    private fun checkRootAccess() {
        isRootAvailable = Shell.isAppGrantedRoot() == true && Shell.getShell().isRoot == true
        if (!isRootAvailable) {
            Log.e(TAG, "Root access not available, service will not monitor thermal settings")
        }
    }

    private fun createNotificationChannelSafely() {
        try {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "XKM Thermal Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Maintains thermal settings"
                enableLights(false)
                enableVibration(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create notification channel", e)
        }
    }

    private fun startForegroundSafely() {
        try {
            startForeground(NOTIFICATION_ID, createNotification(
                if (isRootAvailable) "Maintaining Dynamic thermal profile"
                else "Service inactive - No root access"
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service", e)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRootAvailable) {
            Log.e(TAG, "No root access available, stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        startMonitoring()
        return START_REDELIVER_INTENT
    }

    private fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            var retryCount = 0
            while (isActive) {
                try {
                    if (!isRootAvailable) {
                        checkRootAccess()
                        if (!isRootAvailable) {
                            updateNotification("Service inactive - No root access")
                            if (++retryCount >= MAX_RETRY_COUNT) {
                                Log.e(TAG, "Max retry count reached, stopping service")
                                stopSelf()
                                break
                            }
                            delay(MONITOR_INTERVAL * 2)
                            continue
                        }
                    }

                    // Check if we're still in Dynamic mode (10)
                    val savedMode = try {
                        runBlocking {
                            thermalDataStore.data.first()[LAST_THERMAL_MODE] ?: 0
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to get saved thermal mode", e)
                        0
                    }

                    // If we're no longer in Dynamic mode, stop the service
                    if (savedMode != 10) {
                        Log.d(TAG, "No longer in Dynamic mode ($savedMode), stopping service")
                        stopSelf()
                        break
                    }

                    val currentMode = thermalRepository.getCurrentThermalModeIndex().first()

                    if (currentMode != savedMode && savedMode != 0) {
                        Log.d(TAG, "Thermal mode changed from $savedMode to $currentMode, restoring...")
                        val result = Shell.cmd("echo $savedMode > /sys/class/thermal/thermal_message/sconfig").exec()
                        if (result.isSuccess) {
                            updateNotification("Maintaining thermal mode: $savedMode")
                            retryCount = 0
                        } else {
                            Log.e(TAG, "Failed to restore thermal mode: ${result.err.joinToString()}")
                            if (++retryCount >= MAX_RETRY_COUNT) {
                                updateNotification("Failed to maintain thermal settings")
                                break
                            }
                        }
                    }
                    delay(MONITOR_INTERVAL)

                } catch (e: Exception) {
                    Log.e(TAG, "Error in monitoring loop", e)
                    if (++retryCount >= MAX_RETRY_COUNT) {
                        updateNotification("Service error - Check logs")
                        break
                    }
                    delay(MONITOR_INTERVAL * 2)
                }
            }
        }
    }

    private fun createNotification(text: String): Notification {
        val pendingIntent = try {
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create pending intent", e)
            null
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Thermal Control")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .apply {
                pendingIntent?.let { setContentIntent(it) }
            }
            .setSilent(true)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        try {
            val notification = createNotification(text)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        try {
            monitoringJob?.cancel()
            serviceScope.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up service", e)
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        try {
            val restartServiceIntent = Intent(applicationContext, this.javaClass)
            val restartServicePendingIntent = PendingIntent.getService(
                applicationContext, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, 1000, restartServicePendingIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule service restart", e)
        }
        super.onTaskRemoved(rootIntent)
    }
}
