package id.xms.xtrakernelmanager.domain.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.domain.usecase.CPUControlUseCase
import id.xms.xtrakernelmanager.domain.usecase.ThermalControlUseCase
import kotlinx.coroutines.flow.first

class ApplyConfigWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val preferencesManager = PreferencesManager(context)
    private val cpuUseCase = CPUControlUseCase()
    private val thermalUseCase = ThermalControlUseCase()

    override suspend fun doWork(): Result {
        return try {
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

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}