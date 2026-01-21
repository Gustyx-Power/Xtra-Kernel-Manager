package id.xms.xtrakernelmanager.data.repository

import android.content.Context
import android.util.Log
import id.xms.xtrakernelmanager.data.model.HourBucket
import id.xms.xtrakernelmanager.data.model.HourlyStats
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object HistoryRepository {
  private const val FILENAME = "battery_history.json"
  private var file: File? = null

  private val _hourlyStats = MutableStateFlow(HourlyStats(getCurrentDate()))
  val hourlyStats = _hourlyStats.asStateFlow()

  private val json = Json { ignoreUnknownKeys = true }

  fun init(context: Context) {
    file = File(context.filesDir, FILENAME)
    loadData()
  }

  private fun getCurrentDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
  }

  private fun loadData() {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        if (file?.exists() == true) {
          val content = file?.readText() ?: return@launch
          val stats = json.decodeFromString<HourlyStats>(content)

          // Check if date changed
          if (stats.date != getCurrentDate()) {
            Log.d("HistoryRepository", "New day detected. Resetting stats.")
            _hourlyStats.value = HourlyStats(getCurrentDate())
            saveData()
          } else {
            _hourlyStats.value = stats
          }
        } else {
          _hourlyStats.value = HourlyStats(getCurrentDate())
        }
      } catch (e: Exception) {
        Log.e("HistoryRepository", "Failed to load history", e)
        _hourlyStats.value = HourlyStats(getCurrentDate())
      }
    }
  }

  private fun saveData() {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val content = json.encodeToString(_hourlyStats.value)
        file?.writeText(content)
      } catch (e: Exception) {
        Log.e("HistoryRepository", "Failed to save history", e)
      }
    }
  }

  fun incrementScreenOn(millis: Long) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    updateBucket(currentHour) { it.screenOnMs += millis }
  }

  fun addDrain(percent: Int) {
    if (percent <= 0) return
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    updateBucket(currentHour) { it.drainPercent += percent }
  }

  private fun updateBucket(hour: Int, update: (HourBucket) -> Unit) {
    val currentStats = _hourlyStats.value

    // Auto-reset if midnight passed while app was running
    if (currentStats.date != getCurrentDate()) {
      _hourlyStats.value = HourlyStats(getCurrentDate())
      // Recursively call to update the new bucket
      updateBucket(hour, update)
      return
    }

    // Create a deep copy to trigger StateFlow emission
    val newBuckets = currentStats.buckets.map { it.copy() }.toMutableList()

    if (hour in newBuckets.indices) {
      val bucket = newBuckets[hour]
      update(bucket)
      newBuckets[hour] = bucket

      _hourlyStats.value = currentStats.copy(buckets = newBuckets)
      saveData()
    }
  }
}
