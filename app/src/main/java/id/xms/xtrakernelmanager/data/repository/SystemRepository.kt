package id.xms.xtrakernelmanager.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.vector.path
// Hapus impor yang tidak terpakai jika ada, seperti:
// import androidx.compose.ui.geometry.isEmpty
// import androidx.compose.ui.graphics.vector.path
// import kotlin.io.path.inputStream // Ini juga sepertinya tidak digunakan, File.inputStream() lebih umum

import id.xms.xtrakernelmanager.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose // Diperlukan untuk callbackFlow
import kotlinx.coroutines.channels.ChannelResult // Untuk memeriksa hasil trySend
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
// import kotlinx.coroutines.flow.first // Tidak digunakan di kode yang Anda berikan
// import kotlinx.coroutines.flow.mapLatest // Tidak digunakan di kode yang Anda berikan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.io.BufferedReader
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.inputStream


@Suppress("UNREACHABLE_CODE")
@Singleton
class SystemRepository @Inject constructor(
    private val context: Context,
) {

    companion object {
        private const val TAG = "SystemRepository"
        private const val VALUE_NOT_AVAILABLE = "N/A"
        private const val VALUE_UNKNOWN = "Unknown"
        private const val REALTIME_UPDATE_INTERVAL_MS = 1000L
    }

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var cachedSystemInfo: SystemInfo? = null
    private suspend fun getCachedSystemInfo(): SystemInfo {
        // Menggunakan double-checked locking untuk thread-safety sederhana jika diakses dari coroutine berbeda
        // Meskipun dalam kasus ini, kemungkinan besar akan dipanggil dari scope callbackFlow yang sama.
        return cachedSystemInfo ?: synchronized(this) {
            cachedSystemInfo ?: getSystemInfoInternal().also { cachedSystemInfo = it }
        }
    }

    private fun readFileToString(filePath: String, fileDescription: String, attemptSu: Boolean = true): String? {
        val file = File(filePath)
        try {
            if (file.exists() && file.canRead()) {
                val content = file.readText().trim()
                if (content.isNotBlank()) {
                    return content
                } else {
                    Log.w(TAG, "'$fileDescription': File kosong (langsung). Path: $filePath")
                    if (!attemptSu) return null
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "'$fileDescription': SecurityException (langsung). Path: $filePath. Mencoba SU.", e)
        } catch (e: FileNotFoundException) {
            Log.w(TAG, "'$fileDescription': FileNotFoundException (langsung). Path: $filePath. Mencoba SU.", e)
        } catch (e: IOException) {
            Log.e(TAG, "'$fileDescription': IOException (langsung). Path: $filePath.", e)
            return null
        } catch (e: Exception) {
            Log.e(TAG, "'$fileDescription': Exception tidak diketahui (langsung). Path: $filePath.", e)
            return null
        }

        if (attemptSu) {
            var process: Process? = null
            try {
                process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat \"$filePath\""))
                // Membaca output dan error stream dalam coroutine terpisah untuk menghindari deadlock
                // Namun, untuk kesederhanaan di sini, kita jaga seperti sebelumnya,
                // asumsikan output tidak terlalu besar.
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                val output = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
                val exitCode = process.waitFor()
                val errorOutput = StringBuilder()
                while (errorReader.readLine().also { line = it } != null) {
                    errorOutput.append(line).append("\n")
                }

                if (errorOutput.isNotBlank()) {
                    Log.w(TAG, "'$fileDescription': Error stream dari 'su cat \"$filePath\"' (exit: $exitCode):\n${errorOutput.toString().trim()}")
                }

                if (exitCode == 0) {
                    val contentSu = output.toString().trim()
                    if (contentSu.isNotBlank()) {
                        return contentSu
                    } else {
                        Log.w(TAG, "'$fileDescription': File kosong (via SU). Path: $filePath")
                        return null
                    }
                } else {
                    Log.e(TAG, "'$fileDescription': Perintah 'su cat \"$filePath\"' gagal dengan exit code $exitCode.")
                }
            } catch (e: IOException) {
                Log.e(TAG, "'$fileDescription': IOException saat menjalankan 'su cat \"$filePath\"'", e)
            } catch (e: InterruptedException) {
                Log.e(TAG, "'$fileDescription': InterruptedException saat 'su cat \"$filePath\"'", e)
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                Log.e(TAG, "'$fileDescription': Error tidak diketahui saat 'su cat \"$filePath\"'", e)
            } finally {
                process?.destroy()
            }
        }
        return null
    }

    // Variabel untuk menyimpan data CPU sebelumnya untuk perhitungan load
    private var previousCpuData: List<LongArray>? = null

    private suspend fun getCpuRealtimeInternal(): RealtimeCpuInfo {
        val cores = Runtime.getRuntime().availableProcessors()
        val governor = readFileToString("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor", "CPU0 Governor") ?: VALUE_UNKNOWN

        val frequencies = List(cores) { coreIndex ->
            val freqStr = readFileToString("/sys/devices/system/cpu/cpu$coreIndex/cpufreq/scaling_cur_freq", "CPU$coreIndex Current Freq")
            (freqStr?.toLongOrNull()?.div(1000))?.toInt() ?: 0
        }

        val tempStr = readFileToString("/sys/class/thermal/thermal_zone0/temp", "Thermal Zone0 Temp")
        val temperature = (tempStr?.toFloatOrNull()?.div(1000)) ?: 0f // Asumsi temp dalam mili-Celsius

        // Menghitung CPU load percentage
        val cpuLoadPercentage = calculateCpuLoadPercentage()

        val systemInfo = getCachedSystemInfo() // Dapatkan info SoC

        return RealtimeCpuInfo(
            cores = cores,
            governor = governor,
            freqs = frequencies,
            temp = temperature,
            soc = systemInfo.soc, // Menambahkan kembali socModel
            cpuLoadPercentage = cpuLoadPercentage
        )
    }

    private fun calculateCpuLoadPercentage(): Float? {
        try {
            // Membaca data CPU dari /proc/stat
            val cpuStat = readFileToString("/proc/stat", "CPU Stat")?.lines()?.firstOrNull { it.startsWith("cpu ") }
            if (cpuStat == null) {
                Log.w(TAG, "Failed to read CPU stat from /proc/stat")
                return null
            }

            // Parsing data CPU
            val cpuData = cpuStat.trim().split("\\s+".toRegex()).drop(1).map { it.toLong() }.toLongArray()
            if (cpuData.size < 4) {
                Log.w(TAG, "Invalid CPU stat data format")
                return null
            }

            // Jika ini adalah pembacaan pertama, simpan data dan kembalikan null
            val previousData = previousCpuData
            if (previousData == null) {
                previousCpuData = listOf(cpuData)
                return null
            }

            // Menghitung perbedaan antara data saat ini dan sebelumnya
            val prevData = previousData.first()
            val diffData = LongArray(cpuData.size) { i -> cpuData[i] - prevData[i] }

            // Menghitung total waktu dan waktu idle
            val total = diffData.sum()
            val idle = diffData[3] + (if (diffData.size > 4) diffData[4] else 0) // idle + iowait

            // Menghitung persentase penggunaan CPU
            val usage = if (total > 0) ((total - idle).toDouble() / total.toDouble() * 100.0) else 0.0
            
            // Simpan data saat ini untuk perhitungan berikutnya
            previousCpuData = listOf(cpuData)

            return usage.toFloat().coerceIn(0f, 100f)
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating CPU load percentage", e)
            return null
        }
    }

    fun getCpuRealtime(): RealtimeCpuInfo {
        Log.w(TAG, "Panggilan getCpuRealtime() sinkron. Disarankan menggunakan Flow untuk update realtime.")
        return runBlocking { getCpuRealtimeInternal() }
    }

    private fun getBatteryLevelFromApi(): Int {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatusIntent = context.applicationContext.registerReceiver(null, intentFilter)
        if (batteryStatusIntent == null) {
            Log.w(TAG, "Gagal mendapatkan BatteryStatusIntent.")
            return -1
        }
        val level = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        return if (level != -1 && scale != -1 && scale != 0) {
            (level / scale.toFloat() * 100).toInt()
        } else -1
    }

    private fun getBatteryInfoInternal(statusFromIntent: Int = -1): BatteryInfo {
        val batteryDir = "/sys/class/power_supply/battery"
        val batteryLevelStr = readFileToString("$batteryDir/capacity", "Battery Level Percent from File")
        val finalLevel = batteryLevelStr?.toIntOrNull() ?: getBatteryLevelFromApi().let { if (it == -1) 0 else it }

        var tempStr = readFileToString("$batteryDir/temp", "Battery Temperature")
        var tempSource = "$batteryDir/temp"
        if (tempStr == null) {
            val thermalZoneDirs = File("/sys/class/thermal/").listFiles { dir, name ->
                dir.isDirectory && name.startsWith("thermal_zone")
            }
            thermalZoneDirs?.sortedBy { it.name }?.forEach thermalLoop@{ zoneDir ->
                val type = readFileToString("${zoneDir.path}/type", "Thermal Zone Type (${zoneDir.name})", attemptSu = false)
                if (type != null && (type.contains("battery", ignoreCase = true) || type.contains("แบตเตอรี่") || type.contains("case_therm", ignoreCase = true) || type.contains("ibat_therm", ignoreCase = true))) {
                    tempStr = readFileToString("${zoneDir.path}/temp", "Battery Temperature from ${zoneDir.name} ($type)")
                    if (tempStr != null) {
                        tempSource = "${zoneDir.path}/temp (type: $type)"
                        return@thermalLoop
                    }
                }
            }
        }
        val finalTemperature = tempStr?.toFloatOrNull()?.let { rawTemp ->
            // Jika dari thermal_zone, biasanya dalam mili-Celsius, jika dari power_supply, bisa deci-Celsius
            if (rawTemp > 1000 && (tempSource.contains("thermal_zone") || tempSource.contains("temp_input"))) rawTemp / 1000 else rawTemp / 10
        } ?: 0f

        val cycleCountStr = readFileToString("$batteryDir/cycle_count", "Battery Cycle Count")
        val finalCycleCount = cycleCountStr?.toIntOrNull() ?: run {
            // Try alternative paths for cycle count
            val altCyclePaths = listOf(
                "/sys/class/power_supply/bms/cycle_count",
                "/sys/class/power_supply/battery/cycle_count_summary",
                "/proc/driver/battery_cycle",
                "/proc/battinfo"
            )

            for (altPath in altCyclePaths) {
                val altCycleStr = readFileToString(altPath, "Alternative Battery Cycle Count ($altPath)")
                val cycles = altCycleStr?.toIntOrNull()
                if (cycles != null && cycles > 0) {
                    Log.d(TAG, "Found cycle count from alternative path $altPath: $cycles")
                    return@run cycles
                }
            }
            0 // Default if no cycle count found
        }

        // Try multiple paths for battery capacity information
        val designCapacityUahStr = readFileToString("$batteryDir/charge_full_design", "Battery Design Capacity (uAh)")
        val designCapacityUah = designCapacityUahStr?.toLongOrNull() ?: run {
            // Try alternative paths for design capacity
            val altCapacityPaths = listOf(
                "/sys/class/power_supply/bms/charge_full_design",
                "/sys/class/power_supply/battery/energy_full_design",
                "/proc/driver/battery_capacity"
            )

            for (altPath in altCapacityPaths) {
                val altCapStr = readFileToString(altPath, "Alternative Battery Design Capacity ($altPath)")
                val cap = altCapStr?.toLongOrNull()
                if (cap != null && cap > 0) {
                    Log.d(TAG, "Found design capacity from alternative path $altPath: $cap")
                    return@run cap
                }
            }
            null
        }

        val finalDesignCapacityMah = if (designCapacityUah != null && designCapacityUah > 0) {
            // Convert microAh to mAh, handle different units
            when {
                designCapacityUah > 10000000 -> (designCapacityUah / 1000).toInt() // µAh to mAh
                designCapacityUah > 10000 -> (designCapacityUah / 1000).toInt() // µAh to mAh
                else -> designCapacityUah.toInt() // Already in mAh
            }
        } else 0

        var calculatedSohPercentage: Int = 0
        var currentCapacityMah: Int = 0

        if (finalDesignCapacityMah > 0 && designCapacityUah != null) {
            val currentFullUahStr = readFileToString("$batteryDir/charge_full", "Battery Current Full Capacity (uAh)")
            val currentFullUah = currentFullUahStr?.toLongOrNull() ?: run {
                // Try alternative paths for current capacity
                val altCurrentCapPaths = listOf(
                    "/sys/class/power_supply/bms/charge_full",
                    "/sys/class/power_supply/battery/energy_full",
                    "/proc/driver/battery_current_capacity"
                )

                for (altPath in altCurrentCapPaths) {
                    val altCapStr = readFileToString(altPath, "Alternative Battery Current Capacity ($altPath)")
                    val cap = altCapStr?.toLongOrNull()
                    if (cap != null && cap > 0) {
                        Log.d(TAG, "Found current capacity from alternative path $altPath: $cap")
                        return@run cap
                    }
                }
                null
            }

            if (currentFullUah != null && currentFullUah > 0) {
                // Convert microAh to mAh, handle different units
                currentCapacityMah = when {
                    currentFullUah > 10000000 -> (currentFullUah / 1000).toInt() // µAh to mAh
                    currentFullUah > 10000 -> (currentFullUah / 1000).toInt() // µAh to mAh
                    else -> currentFullUah.toInt() // Already in mAh
                }

                // Calculate real battery health percentage: (Current Capacity / Design Capacity) × 100%
                val sohDouble = (currentCapacityMah.toDouble() / finalDesignCapacityMah.toDouble()) * 100.0
                calculatedSohPercentage = sohDouble.toInt().coerceIn(0, 100)

                Log.d(TAG, "Real Battery Health Calculation:")
                Log.d(TAG, "Design Capacity: ${finalDesignCapacityMah} mAh")
                Log.d(TAG, "Current Capacity: ${currentCapacityMah} mAh")
                Log.d(TAG, "Health Percentage: ${calculatedSohPercentage}% = (${currentCapacityMah} / ${finalDesignCapacityMah}) × 100%")
            } else {
                // If we can't read current capacity, try to get health directly from system
                val healthPercentageStr = readFileToString("$batteryDir/health", "Direct Battery Health")
                calculatedSohPercentage = healthPercentageStr?.toIntOrNull()?.coerceIn(0, 100) ?: run {
                    // As last resort, try to estimate from BatteryManager health status
                    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    val health = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN) ?: BatteryManager.BATTERY_HEALTH_UNKNOWN

                    when (health) {
                        BatteryManager.BATTERY_HEALTH_GOOD -> 100
                        BatteryManager.BATTERY_HEALTH_OVERHEAT -> 85
                        BatteryManager.BATTERY_HEALTH_COLD -> 90
                        BatteryManager.BATTERY_HEALTH_DEAD -> 0
                        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 75
                        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 50
                        else -> 100 // Default to 100% for unknown
                    }
                }
                currentCapacityMah = (finalDesignCapacityMah * calculatedSohPercentage / 100.0).toInt()
                Log.d(TAG, "Estimated Battery Health: ${calculatedSohPercentage}% (no direct capacity measurement)")
            }
        } else {
            // If no design capacity is available, try to get approximate values
            Log.w(TAG, "No battery capacity information available from system files")

            // Try to get some capacity info from BatteryManager properties
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val energyCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)

            if (energyCounter != Int.MIN_VALUE && energyCounter > 0) {
                // Energy counter is in nWh, convert to approximate mAh
                // Assuming average voltage of 3.7V: mAh ≈ nWh / (3.7 * 1000000)
                val estimatedCapacityMah = (energyCounter / (3.7 * 1000000)).toInt()
                if (estimatedCapacityMah > 0) {
                    currentCapacityMah = estimatedCapacityMah
                    // Assume this is 100% health since we don't have design capacity
                    calculatedSohPercentage = 100
                    Log.d(TAG, "Estimated capacity from energy counter: ${currentCapacityMah} mAh")
                }
            }
        }

        // Determine health status string based on percentage
        val healthStatus = when {
            calculatedSohPercentage >= 90 -> "Excellent"
            calculatedSohPercentage >= 80 -> "Good"
            calculatedSohPercentage >= 70 -> "Fair"
            calculatedSohPercentage >= 60 -> "Poor"
            calculatedSohPercentage > 0 -> "Critical"
            else -> "Unknown"
        }

        val finalVoltageStr = readFileToString("$batteryDir/voltage_now", "Battery Voltage Now")
        val finalVoltage = finalVoltageStr?.toFloatOrNull()

        var finalCurrent: Float? = null
        val currentPaths = listOf(
            "$batteryDir/current_now",
            "$batteryDir/current_avg",
            "/sys/class/power_supply/bms/current_now",
            "/sys/class/power_supply/usb/current_now"
        )
        for (path in currentPaths) {
            val currentStr = readFileToString(path, "Battery Current from $path")
            if (currentStr != null) {
                finalCurrent = currentStr.toFloatOrNull()
                // Some kernels add extra characters, so let's be safe
                if (finalCurrent != null) {
                    Log.d(TAG, "Found battery current from $path: $finalCurrent")
                    break // Found a valid value, stop searching
                }
            }
        }

        val finalWattageStr = readFileToString("$batteryDir/power_now", "Battery Power Now")
        val finalWattage = finalWattageStr?.toFloatOrNull()

        val finalTechnology = readFileToString("$batteryDir/technology", "Battery Technology")

        val statusString = readFileToString("$batteryDir/status", "Battery Status")
        Log.d(TAG, "Battery Status String: $statusString, Current: $finalCurrent")

        // Determine charging status
        val isCharging = when {
            // Prioritize the status from the broadcast intent if it's valid and not unknown
            statusFromIntent != -1 && statusFromIntent != BatteryManager.BATTERY_STATUS_UNKNOWN -> {
                Log.d(TAG, "Using API status from intent: $statusFromIntent")
                statusFromIntent == BatteryManager.BATTERY_STATUS_CHARGING || statusFromIntent == BatteryManager.BATTERY_STATUS_FULL
            }
            // Then fall back to the file-based logic
            finalCurrent != null -> {
                Log.d(TAG, "Using file-based current: $finalCurrent")
                finalCurrent < -1000f // Negative current means charging
            }
            else -> {
                Log.d(TAG, "Using file-based status string: $statusString")
                statusString?.contains("Charging", ignoreCase = true) == true ||
                statusString?.contains("Full", ignoreCase = true) == true
            }
        }
        
        Log.d(TAG, "Calculated isCharging from file: $isCharging")

        val finalStatus = when {
            statusString.isNullOrBlank() -> ""
            statusString.contains("Charging", ignoreCase = true) -> "Charging"
            statusString.contains("Discharging", ignoreCase = true) -> "Discharging"
            statusString.contains("Full", ignoreCase = true) -> "Full"
            else -> statusString
        }

        // Normalize current for display: positive for charging, negative for discharging
        val displayCurrent = finalCurrent?.let {
            val absCurrent = kotlin.math.abs(it)
            if (isCharging) absCurrent else -absCurrent
        } ?: 0f

        return BatteryInfo(
            level = finalLevel,
            temp = finalTemperature,
            voltage = finalVoltage ?: 0f,
            isCharging = isCharging,
            current = displayCurrent,
            chargingWattage = finalWattage ?: 0f,
            technology = finalTechnology ?: "Unknown",
            health = healthStatus, // Use the calculated health status
            status = finalStatus,
            chargingType = getChargingTypeFromStatus(statusString),
            powerSource = getChargingTypeFromStatus(statusString),
            healthPercentage = calculatedSohPercentage,
            cycleCount = finalCycleCount, // Use the actual cycle count
            capacity = finalDesignCapacityMah, // Use the actual design capacity
            currentCapacity = currentCapacityMah, // Use the actual current capacity
            plugged = 0
        )
    }

    fun getBatteryInfo(): BatteryInfo {
        Log.w(TAG, "Panggilan getBatteryInfo() sinkron. Disarankan menggunakan Flow untuk update realtime.")
        return getBatteryInfoInternal()
    }

    private fun getMemoryInfoInternal(): MemoryInfo {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            MemoryInfo(
                used = memoryInfo.totalMem - memoryInfo.availMem,
                total = memoryInfo.totalMem,
                free = memoryInfo.availMem
            )
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengambil MemoryInfo", e)
            MemoryInfo(0, 0, 0)
        }
    }

    fun getMemoryInfo(): MemoryInfo {
        Log.w(TAG, "Panggilan getMemoryInfo() sinkron. Disarankan menggunakan Flow untuk update realtime.")
        return getMemoryInfoInternal()
    }

    private fun getUptimeMillisInternal(): Long {
        return android.os.SystemClock.elapsedRealtime()
    }

    private fun getDeepSleepMillisInternal(): Long {
        val uptime = android.os.SystemClock.elapsedRealtime()
        val awakeTime = android.os.SystemClock.uptimeMillis()
        return uptime - awakeTime
    }

    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    fun getDeepSleepInfo(): DeepSleepInfo {
        Log.w(TAG, "Panggilan getDeepSleepInfo() sinkron. Disarankan menggunakan Flow untuk update realtime.")
        return DeepSleepInfo(getUptimeMillisInternal(), getDeepSleepMillisInternal())
    }

    private fun getSystemInfoInternal(): SystemInfo {
        Log.d(TAG, "Mengambil SystemInfo (API based)...")

        // Improved SoC detection with multiple property sources
        var socName = VALUE_UNKNOWN
        try {
            // Try multiple property sources for SoC detection
            val socProperties = listOf(
                "ro.soc.manufacturer" to "ro.soc.model",
                "ro.hardware" to null,
                "ro.product.board" to null,
                "ro.chipname" to null,
                "ro.board.platform" to null,
                "vendor.product.cpu" to null
            )

            var manufacturer: String? = null
            var model: String? = null

            // Try each property pair
            for ((manufacturerProp, modelProp) in socProperties) {
                if (manufacturer.isNullOrBlank()) {
                    manufacturer = getSystemProperty(manufacturerProp)
                }

                if (modelProp != null && model.isNullOrBlank()) {
                    model = getSystemProperty(modelProp)
                }

                // If we have both, break early
                if (!manufacturer.isNullOrBlank() && !model.isNullOrBlank()) {
                    break
                }
            }

            // Additional fallback checks
            if (manufacturer.isNullOrBlank()) {
                manufacturer = getSystemProperty("ro.product.cpu.abi")?.let { abi ->
                    when {
                        abi.contains("arm64") || abi.contains("aarch64") -> "ARM"
                        abi.contains("x86") -> "Intel"
                        else -> null
                    }
                }
            }

            // Parse hardware string for additional info
            val hardware = getSystemProperty("ro.hardware")
            if (!hardware.isNullOrBlank()) {
                when {
                    hardware.startsWith("qcom", ignoreCase = true) -> {
                        if (manufacturer.isNullOrBlank()) manufacturer = "QTI"
                        if (model.isNullOrBlank()) model = hardware.uppercase()
                    }
                    hardware.contains("mtk", ignoreCase = true) || hardware.contains("mediatek", ignoreCase = true) -> {
                        if (manufacturer.isNullOrBlank()) manufacturer = "Mediatek"
                        if (model.isNullOrBlank()) model = hardware
                    }
                    hardware.contains("exynos", ignoreCase = true) -> {
                        if (manufacturer.isNullOrBlank()) manufacturer = "Samsung"
                        if (model.isNullOrBlank()) model = hardware
                    }
                }
            }

            if (!manufacturer.isNullOrBlank() && !model.isNullOrBlank()) {
                socName = when {
                    manufacturer.equals("QTI", ignoreCase = true) && model.equals("SM7475", ignoreCase = true) -> "Qualcomm® Snapdragon™ 7+ Gen 2"
                    manufacturer.equals("QTI", ignoreCase = true) && model.equals("SM8650", ignoreCase = true) -> "Qualcomm® Snapdragon™ 8 Gen 3"
                    manufacturer.equals("QTI", ignoreCase = true) && model.equals("SM8635", ignoreCase = true) -> "Qualcomm® Snapdragon™ 8s Gen 3"
                    manufacturer.equals("QTI", ignoreCase = true) && (model.equals("SDM845", ignoreCase = true) || model.equals("sdm845", ignoreCase = true)) -> "Qualcomm® Snapdragon™ 845"
                    manufacturer.equals("QTI", ignoreCase = true) && model.equals("SM8250", ignoreCase = true) -> "Qualcomm® Snapdragon™ 870"
                    manufacturer.equals("QTI", ignoreCase = true) && model.equals("SM8150", ignoreCase = true) -> "Qualcomm® Snapdragon™ 860"
                    manufacturer.equals("QTI", ignoreCase = true) && (model.equals("SM7435-AB", ignoreCase = true) || model.equals("SM7435", ignoreCase = true)) -> "Qualcomm® Snapdragon™ 7s Gen 2"
                    manufacturer.equals("QTI", ignoreCase = true) && (model.equals("SM8735", ignoreCase = true) || model.equals("sm8735", ignoreCase = true)) -> "Qualcomm® Snapdragon™ 8s Gen 4"
                    manufacturer.equals("QTI", ignoreCase = true) && (model.equals("SDM665", ignoreCase = true) || model.equals("sdm665", ignoreCase = true)) -> "Qualcomm® Snapdragon™ 665"
                    manufacturer.equals("QTI", ignoreCase = true) && (model.equals("SDM660", ignoreCase = true) || model.equals("sdm660", ignoreCase = true)) -> "Qualcomm® Snapdragon™ 660"
                    manufacturer.equals("QTI", ignoreCase = true) && model.equals("SM8750", ignoreCase = true) -> "Qualcomm® Snapdragon™ 8 Elite"
                    manufacturer.equals("Mediatek", ignoreCase = true) && (model.equals("MT6785V/CD", ignoreCase = true) || model.equals("MT6785", ignoreCase = true)) -> "MediaTek Helio G95"
                    manufacturer.equals("Mediatek", ignoreCase = true) && (model.equals("MT6877V/TTZA", ignoreCase = true) || model.equals("MT6877V", ignoreCase = true)) -> "MediaTek Dimensity 1080"
                    manufacturer.equals("Mediatek", ignoreCase = true) && model.equals("MT6833GP", ignoreCase = true) -> "MediaTek Dimensity 6080"
                    manufacturer.equals("Mediatek", ignoreCase = true) && model.equals("MT6769Z", ignoreCase = true) -> "MediaTek Helio G85"
                    manufacturer.equals("Mediatek", ignoreCase = true) && model.equals("MT6989W", ignoreCase = true) -> "MediaTek Dimensity 9300+"
                    else -> "$manufacturer $model"
                }
            } else if (!manufacturer.isNullOrBlank()) {
                socName = manufacturer
            } else if (!model.isNullOrBlank()) {
                socName = model
            }

            Log.d(TAG, "Detected SoC: manufacturer=$manufacturer, model=$model, final=$socName")
        } catch (e: Exception) {
            Log.w(TAG, "Gagal mendapatkan info SOC dari getprop", e)
        }

        // Get actual display information
        val displayInfo = getDisplayInfo()

        return SystemInfo(
            model = android.os.Build.MODEL ?: VALUE_UNKNOWN,
            codename = android.os.Build.DEVICE ?: VALUE_UNKNOWN,
            androidVersion = android.os.Build.VERSION.RELEASE ?: VALUE_UNKNOWN,
            sdk = android.os.Build.VERSION.SDK_INT,
            fingerprint = android.os.Build.FINGERPRINT ?: VALUE_UNKNOWN,
            soc = socName,
            screenResolution = displayInfo.resolution,
            displayTechnology = displayInfo.technology,
            refreshRate = displayInfo.refreshRate,
            screenDpi = displayInfo.dpi,
            gpuRenderer = getGpuRenderer()
        )
    }

    private fun getSystemProperty(property: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $property")
            val result = BufferedReader(InputStreamReader(process.inputStream)).readLine()?.trim()
            process.waitFor()
            process.destroy()
            if (result.isNullOrBlank()) null else result
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get property $property", e)
            null
        }
    }

    private data class DisplayInfo(
        val resolution: String,
        val technology: String,
        val refreshRate: String,
        val dpi: String
    )

    private fun getDisplayInfo(): DisplayInfo {
        try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            val display = windowManager.defaultDisplay
            val metrics = android.util.DisplayMetrics()
            display.getRealMetrics(metrics)

            val resolution = "${metrics.widthPixels}x${metrics.heightPixels}"
            val dpi = "${metrics.densityDpi}"

            // Get refresh rate
            var refreshRate = "60Hz" // fallback
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val mode = display.mode
                    refreshRate = "${mode.refreshRate.toInt()}Hz"
                } else {
                    refreshRate = "${display.refreshRate.toInt()}Hz"
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get refresh rate", e)

                // Try alternative methods
                try {
                    // Try reading from system properties
                    val propRefreshRate = getSystemProperty("ro.display.refresh_rate")
                        ?: getSystemProperty("persist.vendor.display.refresh_rate")
                        ?: getSystemProperty("debug.sf.frame_rate_multiple_threshold")

                    if (!propRefreshRate.isNullOrBlank()) {
                        val rate = propRefreshRate.toFloatOrNull()?.toInt()
                        if (rate != null && rate > 0) {
                            refreshRate = "${rate}Hz"
                        }
                    }
                } catch (ex: Exception) {
                    Log.w(TAG, "Failed to get refresh rate from properties", ex)
                }
            }

            // Detect display technology
            var technology = "LCD" // fallback
            try {
                val displayTech = getSystemProperty("ro.sf.lcd_density")
                    ?: getSystemProperty("ro.hardware.display")
                    ?: getSystemProperty("vendor.display.type")

                technology = when {
                    displayTech?.contains("amoled", ignoreCase = true) == true -> "AMOLED"
                    displayTech?.contains("oled", ignoreCase = true) == true -> "OLED"
                    displayTech?.contains("ips", ignoreCase = true) == true -> "IPS LCD"
                    displayTech?.contains("tft", ignoreCase = true) == true -> "TFT LCD"
                    android.os.Build.MANUFACTURER.equals("Samsung", ignoreCase = true) -> "AMOLED"
                    android.os.Build.MANUFACTURER.equals("OnePlus", ignoreCase = true) -> "AMOLED"
                    android.os.Build.MANUFACTURER.equals("Google", ignoreCase = true) -> "OLED"
                    else -> "LCD"
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to detect display technology", e)
            }

            return DisplayInfo(resolution, technology, refreshRate, dpi)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get display info", e)
            return DisplayInfo(VALUE_UNKNOWN, "LCD", "60Hz", VALUE_UNKNOWN)
        }
    }

    private fun getGpuRenderer(): String {
        return try {
            // Try to get GPU info from system properties
            val gpuInfo = getSystemProperty("ro.hardware.gpu")
                ?: getSystemProperty("ro.opengles.version")
                ?: getSystemProperty("ro.gpu.driver")

            when {
                gpuInfo?.contains("adreno", ignoreCase = true) == true -> "Qualcomm Adreno"
                gpuInfo?.contains("mali", ignoreCase = true) == true -> "ARM Mali"
                gpuInfo?.contains("powervr", ignoreCase = true) == true -> "PowerVR"
                else -> gpuInfo ?: VALUE_UNKNOWN
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get GPU renderer", e)
            VALUE_UNKNOWN
        }
    }

    private fun getChargingTypeFromStatus(statusString: String?): String {
        return when {
            statusString.isNullOrBlank() -> "Unknown"
            statusString.contains("Charging", ignoreCase = true) -> "AC/USB"
            statusString.contains("Wireless", ignoreCase = true) -> "Wireless"
            statusString.contains("Fast", ignoreCase = true) -> "Fast Charging"
            statusString.contains("Quick", ignoreCase = true) -> "Quick Charge"
            statusString.contains("Turbo", ignoreCase = true) -> "Turbo Charging"
            statusString.contains("Super", ignoreCase = true) -> "Super Charging"
            statusString.contains("Warp", ignoreCase = true) -> "Warp Charging"
            statusString.contains("Dash", ignoreCase = true) -> "Dash Charging"
            statusString.contains("Full", ignoreCase = true) -> "Not Charging"
            statusString.contains("Discharging", ignoreCase = true) -> "Not Charging"
            else -> "Unknown"
        }
    }

    fun getSystemInfo(): SystemInfo {
        return runBlocking { getCachedSystemInfo() }
    }

    fun getKernelInfo(): KernelInfo {
        Log.d(TAG, "Getting kernel information...")

        // Get kernel version
        val version = readFileToString("/proc/version", "Kernel Version")
            ?: android.os.Build.VERSION.RELEASE

        // Improved GKI type detection with version-based detection
        val gkiType = when {
            // Check for specific GKI patterns first (more specific)
            version.contains("gki", ignoreCase = true) ||
            version.contains("generic kernel image", ignoreCase = true) ||
            android.os.Build.VERSION.RELEASE.contains("gki", ignoreCase = true) -> "Generic Kernel Image (GKI)"

            // Check for Android Common Kernel patterns
            version.contains("android-mainline", ignoreCase = true) ||
            version.contains("android-common", ignoreCase = true) -> "Android Common Kernel (ACK)"

            // GKI version detection based on Linux kernel version
            version.contains("Linux version", ignoreCase = true) -> {
                // Extract kernel version number
                val versionRegex = """Linux version (\d+\.\d+)""".toRegex()
                val kernelVersionMatch = versionRegex.find(version)
                val kernelVersion = kernelVersionMatch?.groupValues?.get(1)?.toFloatOrNull()

                when {
                    kernelVersion != null && kernelVersion >= 6.6f -> "Generic Kernel Image (GKI 2.0)"
                    kernelVersion != null && kernelVersion >= 5.15f -> "Generic Kernel Image (GKI 2.0)"
                    kernelVersion != null && kernelVersion >= 5.10f -> "Generic Kernel Image (GKI 2.0)"
                    kernelVersion != null && kernelVersion >= 5.4f -> "Generic Kernel Image (GKI 1.0)"
                    kernelVersion != null && kernelVersion >= 4.19f &&
                    (version.contains("android", ignoreCase = true) ||
                     android.os.Build.VERSION.SDK_INT >= 29) -> "Generic Kernel Image (GKI)"
                    version.contains("android", ignoreCase = true) -> "Android Kernel"
                    else -> "Linux Kernel $kernelVersion"
                }
            }

            // Check build fingerprint for additional clues
            android.os.Build.FINGERPRINT.contains("gki", ignoreCase = true) -> "Generic Kernel Image (GKI)"

            // Fallback check for android (less specific) - moved to lower priority
            version.contains("android", ignoreCase = true) -> "Android Kernel"

            else -> "Custom/OEM Kernel"
        }

        // Get scheduler information with better fallback paths
        val scheduler = readFileToString("/sys/block/sda/queue/scheduler", "I/O Scheduler")
            ?.let { schedulerLine ->
                // Extract currently active scheduler (marked with brackets)
                val activeSchedulerRegex = """\[([^\]]+)\]""".toRegex()
                activeSchedulerRegex.find(schedulerLine)?.groupValues?.get(1) ?: schedulerLine.trim()
            } ?: run {
                // Try alternative block devices
                val alternativeDevices = listOf("mmcblk0", "nvme0n1", "sdb", "sdc")
                for (device in alternativeDevices) {
                    val altScheduler = readFileToString("/sys/block/$device/queue/scheduler", "I/O Scheduler ($device)")
                    if (altScheduler != null) {
                        val activeSchedulerRegex = """\[([^\]]+)\]""".toRegex()
                        val result = activeSchedulerRegex.find(altScheduler)?.groupValues?.get(1) ?: altScheduler.trim()
                        if (result.isNotBlank()) return@run result
                    }
                }
                "Unknown"
            }

        // Get SELinux status
        val selinuxStatus = readFileToString("/sys/fs/selinux/enforce", "SELinux Status")
            ?.let { enforceValue ->
                when (enforceValue.trim()) {
                    "1" -> "Enforcing"
                    "0" -> "Permissive"
                    else -> "Unknown"
                }
            } ?: run {
            // Fallback: try getenforce command
            try {
                val process = Runtime.getRuntime().exec("getenforce")
                val result = BufferedReader(InputStreamReader(process.inputStream)).readLine()?.trim()
                process.waitFor()
                process.destroy()
                result ?: "Unknown"
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get SELinux status", e)
                "Unknown"
            }
        }

        // Get ABI
        val abi = android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"

        // Get architecture
        val architecture = when {
            abi.contains("arm64") || abi.contains("aarch64") -> "ARM64"
            abi.contains("arm") -> "ARM"
            abi.contains("x86_64") -> "x86_64"
            abi.contains("x86") -> "x86"
            else -> abi
        }

        // Enhanced KernelSU detection - improved with better logging and error handling
        val kernelSuStatus = when {
            // Method 1: Check kernel version for KernelSU signature (primary method)
            version.contains("KernelSU", ignoreCase = true) -> {
                // Extract KernelSU version if available
                val ksuVersionRegex = """KernelSU[ -]?v?(\d+\.\d+\.\d+)""".toRegex()
                val ksuMatch = ksuVersionRegex.find(version)
                if (ksuMatch != null) {
                    "Active (${ksuMatch.groupValues[1]})"
                } else {
                    "Active"
                }
            }

            // Method 2: Check KernelSU directory
            File("/data/adb/ksu").exists() -> "Active"

            // Method 3: Check for KernelSU binary
            File("/system/bin/ksu").exists() -> "Active"

            // Method 4: Try various detection methods
            else -> {
                // Helper function for additional KernelSU checks
                fun checkOtherKsuMethods(): String {
                    // Check kernel cmdline
                    val cmdline = readFileToString("/proc/cmdline", "Kernel Command Line")
                    if (cmdline?.contains("ksu", ignoreCase = true) == true) {
                        return "Active"
                    }

                    // Check for KernelSU manager app
                    try {
                        context.packageManager.getPackageInfo("me.weishu.kernelsu", 0)
                        return "Detected (Manager Installed)"
                    } catch (e: Exception) {
                        // Ignore and continue
                    }

                    // Check system properties
                    if (getSystemProperty("ro.kernel.su")?.isNotEmpty() == true) {
                        return "Active"
                    }

                    // Default case - not detected
                    return "Not Detected"
                }

                // Enhanced function to execute KernelSU commands with better error handling
                fun executeKsuCommand(command: Array<String>, description: String): String? {
                    var process: Process? = null
                    try {
                        process = Runtime.getRuntime().exec(command)
                        val reader = BufferedReader(InputStreamReader(process.inputStream))
                        val errorReader = BufferedReader(InputStreamReader(process.errorStream))

                        val output = StringBuilder()
                        val errorOutput = StringBuilder()
                        var line: String?

                        // Read output
                        while (reader.readLine().also { line = it } != null) {
                            output.append(line).append("\n")
                        }

                        // Read error stream
                        while (errorReader.readLine().also { line = it } != null) {
                            errorOutput.append(line).append("\n")
                        }

                        val exitCode = process.waitFor()

                        Log.d(TAG, "$description - Exit Code: $exitCode")
                        Log.d(TAG, "$description - Output: ${output.toString().trim()}")
                        if (errorOutput.isNotEmpty()) {
                            Log.d(TAG, "$description - Error: ${errorOutput.toString().trim()}")
                        }

                        reader.close()
                        errorReader.close()

                        if (exitCode == 0) {
                            val result = output.toString().trim()
                            return if (result.isNotBlank()) result else null
                        }

                    } catch (e: Exception) {
                        Log.w(TAG, "$description - Exception: ${e.message}", e)
                    } finally {
                        process?.destroy()
                    }
                    return null
                }

                // Try ksu -V command first
                Log.d(TAG, "KernelSU Detection: Trying ksu -V command")
                val ksuVOutput = executeKsuCommand(arrayOf("ksu", "-V"), "ksu -V")
                if (ksuVOutput != null) {
                    Log.d(TAG, "KernelSU Detection: Found via ksu -V: $ksuVOutput")
                    "Active ($ksuVOutput)"
                } else {
                    // Try su -c "ksu -V" command
                    Log.d(TAG, "KernelSU Detection: Trying su -c 'ksu -V' command")
                    val suKsuVOutput = executeKsuCommand(arrayOf("su", "-c", "ksu -V"), "su -c ksu -V")
                    if (suKsuVOutput != null) {
                        Log.d(TAG, "KernelSU Detection: Found via su -c ksu -V: $suKsuVOutput")
                        "Active ($suKsuVOutput)"
                    } else {
                        // Try /data/adb/ksud --version command
                        Log.d(TAG, "KernelSU Detection: Trying su -c '/data/adb/ksud --version' command")
                        val ksudOutput = executeKsuCommand(arrayOf("su", "-c", "/data/adb/ksud --version"), "su -c /data/adb/ksud --version")
                        if (ksudOutput != null) {
                            Log.d(TAG, "KernelSU Detection: Found via ksud --version: $ksudOutput")
                            "Active ($ksudOutput)"
                        } else {
                            // Try alternative ksud paths
                            val alternativeKsudPaths = listOf(
                                "/data/adb/ksud version",
                                "/data/adb/ksu/bin/ksud --version",
                                "/data/adb/modules/kernelsu/bin/ksud --version"
                            )

                            var foundOutput: String? = null
                            for (ksudPath in alternativeKsudPaths) {
                                Log.d(TAG, "KernelSU Detection: Trying alternative path: $ksudPath")
                                val altOutput = executeKsuCommand(arrayOf("su", "-c", ksudPath), "su -c $ksudPath")
                                if (altOutput != null) {
                                    Log.d(TAG, "KernelSU Detection: Found via alternative path: $altOutput")
                                    foundOutput = altOutput
                                    break
                                }
                            }

                            if (foundOutput != null) {
                                "Active ($foundOutput)"
                            } else {
                                // Check if we can find ksud binary directly
                                Log.d(TAG, "KernelSU Detection: Checking for ksud binary existence")
                                val ksudPaths = listOf(
                                    "/data/adb/ksud",
                                    "/data/adb/ksu/bin/ksud",
                                    "/data/adb/modules/kernelsu/bin/ksud"
                                )

                                var binaryFound = false
                                for (ksudPath in ksudPaths) {
                                    if (File(ksudPath).exists()) {
                                        Log.d(TAG, "KernelSU Detection: Found ksud binary at: $ksudPath")
                                        binaryFound = true
                                        break
                                    }
                                }

                                if (binaryFound) {
                                    "Active (Binary Found)"
                                } else {
                                    // Final fallback checks
                                    Log.d(TAG, "KernelSU Detection: Trying fallback methods")
                                    checkOtherKsuMethods()
                                }
                            }
                        }
                    }
                }
            }
        }

        return KernelInfo(
            version = version,
            gkiType = gkiType,
            scheduler = scheduler,
            selinuxStatus = selinuxStatus,
            abi = abi,
            architecture = architecture,
            kernelSuStatus = kernelSuStatus
        )
    }

    fun getCpuClusters(): List<CpuCluster> {
        Log.d(TAG, "Getting CPU cluster information...")

        val clusters = mutableListOf<CpuCluster>()
        val cores = Runtime.getRuntime().availableProcessors()

        // Group cores by their frequency ranges to identify clusters
        val coreFreqRanges = mutableMapOf<Int, Pair<Int, Int>>() // core -> (min, max)
        val coreGovernors = mutableMapOf<Int, String>() // core -> governor
        val coreAvailableGovernors = mutableMapOf<Int, List<String>>() // core -> available governors

        for (coreIndex in 0 until cores) {
            val minFreqStr = readFileToString("/sys/devices/system/cpu/cpu$coreIndex/cpufreq/cpuinfo_min_freq", "CPU$coreIndex Min Freq")
            val maxFreqStr = readFileToString("/sys/devices/system/cpu/cpu$coreIndex/cpufreq/cpuinfo_max_freq", "CPU$coreIndex Max Freq")
            val governor = readFileToString("/sys/devices/system/cpu/cpu$coreIndex/cpufreq/scaling_governor", "CPU$coreIndex Governor")
            val availableGovernorsStr = readFileToString("/sys/devices/system/cpu/cpu$coreIndex/cpufreq/scaling_available_governors", "CPU$coreIndex Available Governors")

            val minFreq = (minFreqStr?.toLongOrNull()?.div(1000))?.toInt() ?: 0 // Convert kHz to MHz
            val maxFreq = (maxFreqStr?.toLongOrNull()?.div(1000))?.toInt() ?: 0 // Convert kHz to MHz

            if (minFreq > 0 && maxFreq > 0) {
                coreFreqRanges[coreIndex] = Pair(minFreq, maxFreq)
                coreGovernors[coreIndex] = governor ?: "Unknown"
                coreAvailableGovernors[coreIndex] = availableGovernorsStr?.split("\\s+".toRegex())?.filter { it.isNotBlank() } ?: emptyList()
            }
        }

        // Group cores with similar frequency ranges into clusters
        val frequencyGroups = coreFreqRanges.values.distinct().sortedBy { it.second } // Sort by max frequency

        frequencyGroups.forEachIndexed { index, (minFreq, maxFreq) ->
            val coresInCluster = coreFreqRanges.filter { it.value == Pair(minFreq, maxFreq) }.keys

            if (coresInCluster.isNotEmpty()) {
                val representativeCore = coresInCluster.first()
                val clusterName = when (index) {
                    0 -> "Efficiency Cluster" // Lowest frequency cluster
                    frequencyGroups.size - 1 -> "Performance Cluster" // Highest frequency cluster
                    else -> "Mid Cluster ${index + 1}"
                }

                val governor = coreGovernors[representativeCore] ?: "Unknown"
                val availableGovernors = coreAvailableGovernors[representativeCore] ?: emptyList()

                clusters.add(
                    CpuCluster(
                        name = clusterName,
                        minFreq = minFreq,
                        maxFreq = maxFreq,
                        governor = governor,
                        availableGovernors = availableGovernors
                    )
                )
            }
        }

        // If no clusters found (fallback), create a single cluster
        if (clusters.isEmpty()) {
            val fallbackGovernor = readFileToString("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor", "CPU0 Governor") ?: "Unknown"
            val fallbackAvailableGovernors = readFileToString("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors", "CPU0 Available Governors")
                ?.split("\\s+".toRegex())?.filter { it.isNotBlank() } ?: emptyList()

            clusters.add(
                CpuCluster(
                    name = "CPU Cluster",
                    minFreq = 0,
                    maxFreq = 0,
                    governor = fallbackGovernor,
                    availableGovernors = fallbackAvailableGovernors
                )
            )
        }

        Log.d(TAG, "Found ${clusters.size} CPU clusters")
        return clusters
    }

        @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val realtimeAggregatedInfoFlow: Flow<RealtimeAggregatedInfo> = callbackFlow {
        Log.d(TAG, "callbackFlow started for realtimeAggregatedInfoFlow")

        val lastState = java.util.concurrent.atomic.AtomicReference<RealtimeAggregatedInfo>(null)

        // Initial full data fetch
        launch(Dispatchers.IO) {
            val initialData = RealtimeAggregatedInfo(
                cpuInfo = getCpuRealtimeInternal(),
                batteryInfo = getBatteryInfoInternal(),
                memoryInfo = getMemoryInfoInternal(),
                uptimeMillis = getUptimeMillisInternal(),
                deepSleepMillis = getDeepSleepMillisInternal()
            )
            lastState.set(initialData)
            trySend(initialData)
        }

        // Decoupled battery update via BroadcastReceiver
        val batteryReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
                    Log.d(TAG, "Received ACTION_BATTERY_CHANGED, triggering isolated battery update.")
                    val currentState = lastState.get()
                    if (currentState != null) {
                        launch(Dispatchers.IO) {
                            val newBatteryInfo = getBatteryInfoInternal(status)
                            val newState = currentState.copy(batteryInfo = newBatteryInfo)
                            lastState.set(newState)
                            trySend(newState)
                        }
                    }
                }
            }
        }
        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // Decoupled polling for other system stats
        val pollingJob = launch(Dispatchers.IO) {
            while (isActive) {
                delay(REALTIME_UPDATE_INTERVAL_MS)
                val currentState = lastState.get()
                if (currentState != null) {
                    // Fetch non-battery stats
                    val newCpuInfo = getCpuRealtimeInternal()
                    val newMemoryInfo = getMemoryInfoInternal()
                    val newUptime = getUptimeMillisInternal()
                    val newDeepSleep = getDeepSleepMillisInternal()

                    // Create new state by copying the last one and updating polled values
                    val newState = currentState.copy(
                        cpuInfo = newCpuInfo,
                        memoryInfo = newMemoryInfo,
                        uptimeMillis = newUptime,
                        deepSleepMillis = newDeepSleep
                    )
                    lastState.set(newState)
                    trySend(newState)
                }
            }
        }

        awaitClose {
            Log.d(TAG, "Flow is closing. Unregistering receiver and cancelling polling job.")
            context.unregisterReceiver(batteryReceiver)
            pollingJob.cancel()
        }
    }.shareIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1
    )

    fun onDestroy() {
        Log.d(TAG, "SystemRepository onDestroy() called, cancelling repositoryScope.")
        repositoryScope.cancel()
    }
}
