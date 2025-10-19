package id.xms.xtrakernelmanager.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import id.xms.xtrakernelmanager.MainActivity
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.repository.KernelRepository
import kotlinx.coroutines.*

class MonitoringService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var kernelRepository: KernelRepository

    private var savedThermalMode: String = "Not Set"
    private var savedCpuSettings: MutableMap<Int, Pair<Long, String>> = mutableMapOf()
    private var savedGpuFreq: Long = 0
    private var savedGpuGovernor: String = ""

    companion object {
        private const val CHANNEL_ID = "xtra_kernel_monitor"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_START = "action_start"
        const val ACTION_STOP = "action_stop"
        const val ACTION_UPDATE_SETTINGS = "action_update_settings"

        const val EXTRA_THERMAL_MODE = "thermal_mode"
        const val EXTRA_CPU_SETTINGS = "cpu_settings"
        const val EXTRA_GPU_FREQ = "gpu_freq"
        const val EXTRA_GPU_GOVERNOR = "gpu_governor"
    }

    override fun onCreate() {
        super.onCreate()
        kernelRepository = KernelRepository()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, createNotification())
                startMonitoring()
            }
            ACTION_STOP -> {
                stopMonitoring()
                stopSelf()
            }
            ACTION_UPDATE_SETTINGS -> {
                updateSavedSettings(intent)
            }
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        serviceScope.launch {
            while (isActive) {
                try {
                    // Reapply thermal mode if changed
                    if (savedThermalMode != "Not Set") {
                        val currentMode = kernelRepository.getThermalMode()
                        if (currentMode != savedThermalMode) {
                            kernelRepository.setThermalMode(savedThermalMode)
                        }
                    }

                    // Reapply CPU settings if changed
                    savedCpuSettings.forEach { (core, settings) ->
                        val (freq, governor) = settings
                        kernelRepository.setCpuFrequency(core, freq)
                        kernelRepository.setCpuGovernor(core, governor)
                    }

                    // Reapply GPU settings
                    if (savedGpuFreq > 0) {
                        kernelRepository.setGpuFrequency(savedGpuFreq)
                    }
                    if (savedGpuGovernor.isNotEmpty()) {
                        kernelRepository.setGpuGovernor(savedGpuGovernor)
                    }

                } catch (e: Exception) {
                    // Log error
                }
                delay(5000) // Check every 5 seconds
            }
        }
    }

    private fun stopMonitoring() {
        serviceScope.cancel()
    }

    private fun updateSavedSettings(intent: Intent) {
        savedThermalMode = intent.getStringExtra(EXTRA_THERMAL_MODE) ?: savedThermalMode
        savedGpuFreq = intent.getLongExtra(EXTRA_GPU_FREQ, savedGpuFreq)
        savedGpuGovernor = intent.getStringExtra(EXTRA_GPU_GOVERNOR) ?: savedGpuGovernor
        // Update notification to show active monitoring
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Xtra Kernel Manager")
            .setContentText("Monitoring and applying kernel settings")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Kernel Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors and reapplies kernel settings"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
    }
}
