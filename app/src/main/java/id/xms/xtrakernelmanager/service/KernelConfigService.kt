package id.xms.xtrakernelmanager.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.*
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.domain.usecase.CPUControlUseCase
import id.xms.xtrakernelmanager.domain.usecase.ThermalControlUseCase
import kotlinx.coroutines.flow.first

class KernelConfigService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var preferencesManager: PreferencesManager
    private val cpuUseCase = CPUControlUseCase()
    private val thermalUseCase = ThermalControlUseCase()

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(applicationContext)
        startForegroundService()
        startSyncRoutine()
    }

    private fun startForegroundService() {
        val channelId = "kernel_config_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Kernel Config Persistent",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
        } else {
            Notification.Builder(this)
        }
            .setContentTitle("Kernel Config Active")
            .setContentText("Persisting kernel tuning in background")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .build()
        startForeground(99, notification)
    }

    private fun startSyncRoutine() {
        serviceScope.launch {
            while (isActive) {
                applyAllConfigurations()
                delay(5000) // setiap 5 detik
            }
        }
    }

    private suspend fun applyAllConfigurations() {
        // Apply semua state CPU core (0..7, pastikan cluster/VM memasukkan semua)
        for (core in 0..7) {
            val enabled = preferencesManager.isCpuCoreEnabled(core).first()
            cpuUseCase.setCoreOnline(core, enabled)
        }
        val thermalPreset = preferencesManager.getThermalPreset().first()
        val setOnBoot = preferencesManager.getThermalSetOnBoot().first()
        if (thermalPreset.isNotEmpty()) {
            thermalUseCase.setThermalMode(thermalPreset, setOnBoot)
        }
        val ioScheduler = preferencesManager.getIOScheduler().first()
        if (ioScheduler.isNotBlank()) {
            RootManager.executeCommand("echo $ioScheduler > /sys/block/sda/queue/scheduler")
        }
        val tcpCongestion = preferencesManager.getTCPCongestion().first()
        if (tcpCongestion.isNotBlank()) {
            RootManager.executeCommand("echo $tcpCongestion > /proc/sys/net/ipv4/tcp_congestion_control")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }
}
