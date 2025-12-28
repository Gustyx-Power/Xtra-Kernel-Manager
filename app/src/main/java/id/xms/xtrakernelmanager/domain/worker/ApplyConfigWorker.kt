package id.xms.xtrakernelmanager.domain.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.domain.usecase.CPUControlUseCase
import id.xms.xtrakernelmanager.domain.usecase.RAMControlUseCase
import id.xms.xtrakernelmanager.domain.usecase.ThermalControlUseCase
import kotlinx.coroutines.flow.first

class ApplyConfigWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

  private val preferencesManager = PreferencesManager(context)
  private val cpuUseCase = CPUControlUseCase()
  private val thermalUseCase = ThermalControlUseCase()
  private val ramUseCase = RAMControlUseCase()

  override suspend fun doWork(): Result {
    return try {
      // CPU cores
      for (core in 0..7) {
        val enabled = preferencesManager.isCpuCoreEnabled(core).first()
        cpuUseCase.setCoreOnline(core, enabled)
      }

      // Thermal
      val thermalPreset = preferencesManager.getThermalPreset().first()
      val setOnBoot = preferencesManager.getThermalSetOnBoot().first()
      if (thermalPreset.isNotEmpty()) {
        thermalUseCase.setThermalMode(thermalPreset, setOnBoot)
      }

      // I/O scheduler
      val ioScheduler = preferencesManager.getIOScheduler().first()
      if (ioScheduler.isNotBlank()) {
        RootManager.executeCommand("echo $ioScheduler > /sys/block/sda/queue/scheduler")
      }

      // TCP congestion
      val tcpCongestion = preferencesManager.getTCPCongestion().first()
      if (tcpCongestion.isNotBlank()) {
        RootManager.executeCommand(
            "echo $tcpCongestion > /proc/sys/net/ipv4/tcp_congestion_control"
        )
      }

      // RAM config (swappiness, ZRAM, swap, dirty, min_free)
      val ramConfig = preferencesManager.getRamConfig().first()
      ramUseCase.setSwappiness(ramConfig.swappiness)
      ramUseCase.setZRAMSize(ramConfig.zramSize.toLong() * 1024L * 1024L)
      ramUseCase.setDirtyRatio(ramConfig.dirtyRatio)
      ramUseCase.setMinFreeMem(ramConfig.minFreeMem)
      ramUseCase.setSwapFileSizeMb(ramConfig.swapSize)

      Result.success()
    } catch (e: Exception) {
      Result.retry()
    }
  }
}
