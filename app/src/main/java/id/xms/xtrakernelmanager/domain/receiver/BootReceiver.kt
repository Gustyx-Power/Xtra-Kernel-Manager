package id.xms.xtrakernelmanager.domain.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import id.xms.xtrakernelmanager.domain.worker.ApplyConfigWorker
import id.xms.xtrakernelmanager.service.AppProfileService

class BootReceiver : BroadcastReceiver() {
  companion object {
    private const val TAG = "BootReceiver"
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
      Log.d(TAG, "Boot completed, starting services...")

      // Apply kernel config on boot
      val workRequest = OneTimeWorkRequestBuilder<ApplyConfigWorker>().build()
      WorkManager.getInstance(context).enqueue(workRequest)

      // Start AppProfileService for per-app profiles and game overlay
      try {
        val serviceIntent = Intent(context, AppProfileService::class.java)
        context.startForegroundService(serviceIntent)
        Log.d(TAG, "AppProfileService started on boot")
      } catch (e: Exception) {
        Log.e(TAG, "Failed to start AppProfileService on boot: ${e.message}")
      }
    }
  }
}
