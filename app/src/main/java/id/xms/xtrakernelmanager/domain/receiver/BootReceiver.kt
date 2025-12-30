package id.xms.xtrakernelmanager.domain.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import id.xms.xtrakernelmanager.domain.worker.ApplyConfigWorker

class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
      val workRequest = OneTimeWorkRequestBuilder<ApplyConfigWorker>().build()
      WorkManager.getInstance(context).enqueue(workRequest)
    }
  }
}
