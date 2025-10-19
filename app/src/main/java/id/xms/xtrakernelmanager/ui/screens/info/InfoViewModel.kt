package id.xms.xtrakernelmanager.ui.screens.info

import android.app.AppOpsManager
import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.data.repository.SystemInfoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ScreenOnTimeData(
    val timestamp: Long,
    val screenOnTime: Long
)

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalTimeInForeground: Long,
    val percentage: Float,
    val lastTimeUsed: Long
)

data class InfoUiState(
    val screenOnTime: Long = 0,
    val screenOnHistory: List<ScreenOnTimeData> = emptyList(),
    val lastChargeFull: Long = 0,
    val appUsageList: List<AppUsageInfo> = emptyList(),
    val hasUsageStatsPermission: Boolean = false,
    val isLoading: Boolean = true,
    val message: String? = null
)

class InfoViewModel(
    application: Application,
    private val systemInfoRepository: SystemInfoRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(InfoUiState())
    val uiState: StateFlow<InfoUiState> = _uiState.asStateFlow()

    private val context: Context
        get() = getApplication<Application>().applicationContext

    init {
        checkUsageStatsPermission()
        loadScreenOnTime()
        loadAppUsageStats()
        startRealtimeMonitoring()
    }

    private fun startRealtimeMonitoring() {
        viewModelScope.launch {
            while (isActive) {
                checkUsageStatsPermission()
                loadScreenOnTime()
                if (_uiState.value.hasUsageStatsPermission) {
                    loadAppUsageStats()
                }
                delay(5000)
            }
        }
    }

    private fun checkUsageStatsPermission() {
        val hasPermission = hasUsageStatsPermission()
        _uiState.value = _uiState.value.copy(hasUsageStatsPermission = hasPermission)
    }

    private fun hasUsageStatsPermission(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    private fun loadScreenOnTime() {
        viewModelScope.launch {
            try {
                if (!hasUsageStatsPermission()) {
                    _uiState.value = _uiState.value.copy(
                        screenOnTime = 0L,
                        screenOnHistory = emptyList(),
                        isLoading = false
                    )
                    return@launch
                }

                // Get last full charge time from SharedPreferences
                val prefs = context.getSharedPreferences("xtra_kernel_prefs", Context.MODE_PRIVATE)
                var lastFullChargeTime = prefs.getLong("last_full_charge", 0L)

                // Check current battery status
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryStatus = context.registerReceiver(null, intentFilter)

                val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

                // If battery is 100% and charging/full, update last full charge time
                if (level >= 100 && (status == BatteryManager.BATTERY_STATUS_FULL ||
                            status == BatteryManager.BATTERY_STATUS_CHARGING)) {
                    val currentTime = System.currentTimeMillis()
                    if (lastFullChargeTime == 0L || currentTime - lastFullChargeTime > 30 * 60 * 1000) {
                        lastFullChargeTime = currentTime
                        prefs.edit().putLong("last_full_charge", lastFullChargeTime).apply()
                    }
                }

                // If no charge recorded yet, use boot time
                if (lastFullChargeTime == 0L) {
                    lastFullChargeTime = System.currentTimeMillis() - SystemClock.elapsedRealtime()
                    prefs.edit().putLong("last_full_charge", lastFullChargeTime).apply()
                }

                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val endTime = System.currentTimeMillis()
                val startTime = lastFullChargeTime

                val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
                var totalScreenOnTime = 0L
                var lastScreenOnTime = 0L

                // Build history data (hourly intervals)
                val historyMap = mutableMapOf<Long, Long>()
                var currentHourScreenTime = 0L

                while (usageEvents.hasNextEvent()) {
                    val event = android.app.usage.UsageEvents.Event()
                    usageEvents.getNextEvent(event)

                    when (event.eventType) {
                        android.app.usage.UsageEvents.Event.SCREEN_INTERACTIVE -> {
                            lastScreenOnTime = event.timeStamp
                        }
                        android.app.usage.UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                            if (lastScreenOnTime > 0) {
                                val sessionTime = event.timeStamp - lastScreenOnTime
                                totalScreenOnTime += sessionTime

                                // Add to hourly bucket
                                val hourBucket = (event.timeStamp / (60 * 60 * 1000)) * (60 * 60 * 1000)
                                historyMap[hourBucket] = (historyMap[hourBucket] ?: 0L) + sessionTime
                            }
                            lastScreenOnTime = 0L
                        }
                    }
                }

                // If screen is currently on, add current session
                if (lastScreenOnTime > 0) {
                    val currentSessionTime = System.currentTimeMillis() - lastScreenOnTime
                    totalScreenOnTime += currentSessionTime

                    val hourBucket = (System.currentTimeMillis() / (60 * 60 * 1000)) * (60 * 60 * 1000)
                    historyMap[hourBucket] = (historyMap[hourBucket] ?: 0L) + currentSessionTime
                }

                // Convert history map to sorted list
                val history = historyMap.entries
                    .sortedBy { it.key }
                    .map { ScreenOnTimeData(it.key, it.value) }

                _uiState.value = _uiState.value.copy(
                    screenOnTime = totalScreenOnTime,
                    screenOnHistory = history,
                    lastChargeFull = lastFullChargeTime,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    screenOnTime = 0L,
                    screenOnHistory = emptyList(),
                    isLoading = false,
                    message = "Error loading screen time: ${e.message}"
                )
            }
        }
    }


    fun loadAppUsageStats() {
        viewModelScope.launch {
            try {
                if (!hasUsageStatsPermission()) {
                    _uiState.value = _uiState.value.copy(
                        appUsageList = emptyList(),
                        hasUsageStatsPermission = false
                    )
                    return@launch
                }

                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val endTime = System.currentTimeMillis()
                val startTime = endTime - (24 * 60 * 60 * 1000)

                val usageStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime
                )

                if (usageStats.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        appUsageList = emptyList(),
                        hasUsageStatsPermission = true
                    )
                    return@launch
                }

                // Group by package name and sum usage time (fix duplicates)
                val groupedStats = usageStats
                    .filter { it.totalTimeInForeground > 0 }
                    .groupBy { it.packageName }
                    .map { (packageName, stats) ->
                        val totalTime = stats.sumOf { it.totalTimeInForeground }
                        val lastUsed = stats.maxOf { it.lastTimeUsed }

                        val appName = try {
                            val pm = context.packageManager
                            pm.getApplicationLabel(
                                pm.getApplicationInfo(packageName, 0)
                            ).toString()
                        } catch (e: Exception) {
                            packageName
                        }

                        Triple(packageName, appName, Pair(totalTime, lastUsed))
                    }
                    .sortedByDescending { it.third.first }
                    .take(20)

                // Calculate total usage time for percentage
                val totalUsageTime = groupedStats.sumOf { it.third.first }

                val appUsageList = groupedStats.map { (packageName, appName, timeData) ->
                    val (totalTime, lastUsed) = timeData
                    val percentage = if (totalUsageTime > 0) {
                        (totalTime.toFloat() / totalUsageTime * 100)
                    } else 0f

                    AppUsageInfo(
                        packageName = packageName,
                        appName = appName,
                        totalTimeInForeground = totalTime,
                        percentage = percentage,
                        lastTimeUsed = lastUsed
                    )
                }

                _uiState.value = _uiState.value.copy(
                    appUsageList = appUsageList,
                    hasUsageStatsPermission = true,
                    message = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    appUsageList = emptyList(),
                    message = "Error loading app usage: ${e.message}"
                )
            }
        }
    }

    fun requestUsageStatsPermission() {
        viewModelScope.launch {
            try {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)

                _uiState.value = _uiState.value.copy(
                    message = "Please enable usage access for Xtra Kernel Manager"
                )
            } catch (e: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (ex: Exception) {
                    _uiState.value = _uiState.value.copy(
                        message = "Could not open settings"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun refresh() {
        loadScreenOnTime()
        loadAppUsageStats()
    }
}
