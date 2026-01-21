package id.xms.xtrakernelmanager.data.repository

import android.content.Context
import android.util.Log
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Data class untuk satu sample arus */
data class CurrentSample(
    val timestamp: Long, // Unix timestamp in ms
    val current: Int, // mA (positive = charging, negative = discharging)
    val isCharging: Boolean,
)

/*
 * Repository untuk menyimpan dan mengambil data current flow
 * Data disimpan dalam format JSON di internal storage
 */

object CurrentFlowRepository {

  private const val TAG = "CurrentFlowRepo"
  private const val FILE_NAME = "current_flow.json"
  private const val MAX_SAMPLES = 4320 // 6 jam dengan interval 5 detik

  private val _samples = MutableStateFlow<List<CurrentSample>>(emptyList())
  val samples: StateFlow<List<CurrentSample>> = _samples.asStateFlow()

  private var isInitialized = false

  /** Initialize repository dan load data dari JSON */
  suspend fun initialize(context: Context) {
    if (isInitialized) return
    withContext(Dispatchers.IO) {
      try {
        val file = getFile(context)
        if (file.exists()) {
          val json = file.readText()
          val loadedSamples = parseJson(json)
          _samples.value = loadedSamples
          Log.d(TAG, "Loaded ${loadedSamples.size} samples from JSON")
        }
        isInitialized = true
      } catch (e: Exception) {
        Log.e(TAG, "Failed to load JSON: ${e.message}")
      }
    }
  }

  /** Tambah sample baru dan simpan ke JSON */
  suspend fun addSample(context: Context, current: Int, isCharging: Boolean) {
    withContext(Dispatchers.IO) {
      val sample =
          CurrentSample(
              timestamp = System.currentTimeMillis(),
              current = current,
              isCharging = isCharging,
          )

      val currentList = _samples.value.toMutableList()
      currentList.add(sample)

      // Trim jika melebihi max
      while (currentList.size > MAX_SAMPLES) {
        currentList.removeAt(0)
      }

      _samples.value = currentList

      // Save to JSON (debounced - setiap 10 samples)
      if (currentList.size % 10 == 0) {
        saveToJson(context, currentList)
      }
    }
  }

  /** Ambil samples untuk time range tertentu */
  fun getSamplesForRange(minutes: Int): List<CurrentSample> {
    val cutoffTime = System.currentTimeMillis() - (minutes * 60 * 1000L)
    return _samples.value.filter { it.timestamp >= cutoffTime }
  }

  /** Simpan semua data ke JSON */
  suspend fun saveToJson(context: Context, samples: List<CurrentSample>) {
    withContext(Dispatchers.IO) {
      try {
        val jsonArray = JSONArray()
        samples.forEach { sample ->
          val obj =
              JSONObject().apply {
                put("timestamp", sample.timestamp)
                put("current", sample.current)
                put("isCharging", sample.isCharging)
              }
          jsonArray.put(obj)
        }

        val json = JSONObject().apply { put("samples", jsonArray) }.toString()

        getFile(context).writeText(json)
        Log.d(TAG, "Saved ${samples.size} samples to JSON")
      } catch (e: Exception) {
        Log.e(TAG, "Failed to save JSON: ${e.message}")
      }
    }
  }

  /** Force save */
  suspend fun forceSave(context: Context) {
    saveToJson(context, _samples.value)
  }

  /** Clear all data */
  suspend fun clear(context: Context) {
    withContext(Dispatchers.IO) {
      _samples.value = emptyList()
      getFile(context).delete()
    }
  }

  private fun getFile(context: Context): File {
    return File(context.filesDir, FILE_NAME)
  }

  private fun parseJson(json: String): List<CurrentSample> {
    val result = mutableListOf<CurrentSample>()
    try {
      val root = JSONObject(json)
      val samplesArray = root.getJSONArray("samples")

      for (i in 0 until samplesArray.length()) {
        val obj = samplesArray.getJSONObject(i)
        result.add(
            CurrentSample(
                timestamp = obj.getLong("timestamp"),
                current = obj.getInt("current"),
                isCharging = obj.getBoolean("isCharging"),
            )
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to parse JSON: ${e.message}")
    }
    return result
  }
}
