package id.xms.xtrakernelmanager.data.repository

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil

@Singleton
class TuningRepository @Inject constructor(
    private val context: Context
) {

    private val TAG = "TuningRepository"

    // Thermal
    private val thermalSysfsNode = "/sys/class/thermal/thermal_message/sconfig"

    // CPU
    private val cpuBaseSysfsPath = "/sys/devices/system/cpu"
    private val coreOnlinePath = "$cpuBaseSysfsPath/cpu%d/online"
    private val cpuGovPath = "$cpuBaseSysfsPath/%s/cpufreq/scaling_governor"
    private val cpuMinFreqPath = "$cpuBaseSysfsPath/%s/cpufreq/scaling_min_freq"
    private val cpuMaxFreqPath = "$cpuBaseSysfsPath/%s/cpufreq/scaling_max_freq"
    private val cpuAvailableGovsPath = "$cpuBaseSysfsPath/%s/cpufreq/scaling_available_governors"
    private val cpuAvailableFreqsPath = "$cpuBaseSysfsPath/%s/cpufreq/scaling_available_frequencies"

    // GPU
    private val gpuBaseSysfsPath = "/sys/class/kgsl/kgsl-3d0"
    private val gpuGovPath = "$gpuBaseSysfsPath/devfreq/governor"
    private val gpuAvailableGovsPath = "$gpuBaseSysfsPath/devfreq/available_governors"
    private val gpuMinFreqPath = "$gpuBaseSysfsPath/devfreq/min_freq"
    private val gpuMaxFreqPath = "$gpuBaseSysfsPath/devfreq/max_freq"
    private val gpuCurFreqPath = "$gpuBaseSysfsPath/devfreq/cur_freq"
    private val gpuAvailableFreqsPath = "$gpuBaseSysfsPath/devfreq/available_frequencies"
    private val gpuCurrentPowerLevelPath = "$gpuBaseSysfsPath/default_pwrlevel"
    private val gpuMinPowerLevelPath = "$gpuBaseSysfsPath/min_pwrlevel"
    private val gpuMaxPowerLevelPath = "$gpuBaseSysfsPath/max_pwrlevel"

    // RAM
    private val zramControlPath = "/sys/block/zram0"
    private val zramResetPath = "$zramControlPath/reset"
    private val zramDisksizePath = "$zramControlPath/disksize"
    private val zramCompAlgorithmPath = "$zramControlPath/comp_algorithm"
    private val zramInitStatePath = "$zramControlPath/initstate"
    private val swappinessPath = "/proc/sys/vm/swappiness"
    private val dirtyRatioPath = "/proc/sys/vm/dirty_ratio"
    private val dirtyBackgroundRatioPath = "/proc/sys/vm/dirty_background_ratio"
    private val dirtyWritebackCentisecsPath = "/proc/sys/vm/dirty_writeback_centisecs"
    private val dirtyExpireCentisecsPath = "/proc/sys/vm/dirty_expire_centisecs"
    private val minFreeKbytesPath = "/proc/sys/vm/min_free_kbytes"
    private val minFreeMemoryPath = "/proc/meminfo"

    /* ----------------------------------------------------------
       Helper Shell
       ---------------------------------------------------------- */
    private var isSuShellWorking = true
    private fun runTuningCommand(cmd: String): Boolean {
        Log.d(TAG, "[Shell RW TUNING] Preparing to execute: '$cmd'")
        val originalSelinuxMode = getSelinuxModeInternal()
        val needsSelinuxChange = originalSelinuxMode.equals("Enforcing", ignoreCase = true)

        if (needsSelinuxChange) {
            Log.i(TAG, "[Shell RW TUNING] Current SELinux mode: $originalSelinuxMode. Setting to Permissive.")
            if (!setSelinuxModeInternal(false)) {
                Log.e(TAG, "[Shell RW TUNING] Failed to set SELinux to Permissive. Command will run in current mode.")

            }
        }

        val success = executeShellCommand(cmd)
        if (needsSelinuxChange) {
            Log.i(TAG, "[Shell RW TUNING] Restoring SELinux mode to Enforcing.")
            if (!setSelinuxModeInternal(true)) { // Set kembali ke Enforcing (1)
                Log.w(TAG, "[Shell RW TUNING] Failed to restore SELinux to Enforcing. System might remain in Permissive mode.")
            }
        }
        return success
    }
    private fun executeShellCommand(cmd: String): Boolean {
        Log.d(TAG, "[Shell EXEC] Executing: '$cmd'")
        if (isSuShellWorking) {
            try {
                val result = Shell.cmd(cmd).exec()
                return if (result.isSuccess) {
                    Log.i(TAG, "[Shell EXEC] Success (libsu, code ${result.code}): '$cmd'")
                    true
                } else {
                    Log.e(TAG, "[Shell EXEC] Failed (libsu, code ${result.code}): '$cmd'. Err: ${result.err.joinToString("\n")}")
                    isSuShellWorking = false
                    executeShellCommandFallback(cmd)
                }
            } catch (e: Exception) {
                Log.e(TAG, "[Shell EXEC] Exception with libsu for cmd: '$cmd'. Trying fallback.", e)
                isSuShellWorking = false
                return executeShellCommandFallback(cmd)
            }
        } else {
            return executeShellCommandFallback(cmd)
        }
    }
    private fun runShellCommand(cmd: String): Boolean {
        return runTuningCommand(cmd)
    }


    private fun readShellCommand(cmd: String): String {
        Log.d(TAG, "[Shell R] Executing: '$cmd'")
        if (isSuShellWorking) {
            try {
                val result = Shell.cmd(cmd).exec()
                return if (result.isSuccess) result.out.joinToString("\n").trim() else {
                    isSuShellWorking = false
                    readShellCommandFallback(cmd)
                }
            } catch (e: Exception) {
                Log.e(TAG, "[Shell R] Exception with libsu for cmd: '$cmd'. Trying fallback.", e)
                isSuShellWorking = false
                return readShellCommandFallback(cmd)
            }
        } else {
            return readShellCommandFallback(cmd)
        }
    }

    /* ----------------------------------------------------------
       SELinux Specific Helpers (Internal)
       ---------------------------------------------------------- */
    private fun setSelinuxModeInternal(enforcing: Boolean): Boolean {
        val mode = if (enforcing) "1" else "0"
        Log.i(TAG, "[SELinux] Setting mode to: ${if (enforcing) "Enforcing" else "Permissive"} ($mode)")
        val success = executeShellCommand("setenforce $mode")
        if (!success) {
            Log.e(TAG, "[SELinux] Failed to set SELinux mode to $mode")
        }
        return success
    }

    private fun getSelinuxModeInternal(): String {
        // Gunakan readShellCommand karena ini adalah operasi baca
        val result = readShellCommand("getenforce").trim()
        Log.i(TAG, "[SELinux] Current mode: $result")
        return result // Akan mengembalikan "Enforcing", "Permissive"
    }

    // Public functions for SELinux if needed by ViewModel/UI, though likely not directly
    fun setSelinuxEnforcing(): Flow<Boolean> = flow {
        emit(setSelinuxModeInternal(true))
    }.flowOn(Dispatchers.IO)

    fun setSelinuxPermissive(): Flow<Boolean> = flow {
        emit(setSelinuxModeInternal(false))
    }.flowOn(Dispatchers.IO)

    fun getCurrentSelinuxMode(): Flow<String> = flow {
        emit(getSelinuxModeInternal())
    }.flowOn(Dispatchers.IO)


    /* ----------------------------------------------------------
       RAM detection
       ---------------------------------------------------------- */
    private val totalRamBytes: Long
        get() {
            val memInfo = ActivityManager.MemoryInfo()
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.getMemoryInfo(memInfo)
            return memInfo.totalMem
        }

    fun calculateMaxZramSize(): Long {
        val ramGB = ceil(totalRamBytes / (1024.0 * 1024.0 * 1024.0)).toLong()
        return when {
            ramGB <= 3L -> 1_073_741_824L   // 1 GB
            ramGB <= 4L -> 2_147_483_648L   // 2 GB
            ramGB <= 6L -> 4_294_967_296L   // 4 GB
            ramGB <= 8L -> 9_663_676_416L   // 9 GB
            ramGB <= 12L -> 15_032_385_536L // 14 GB
            ramGB <= 16L -> 19_327_352_832L // 18 GB
            else -> totalRamBytes / 4       // 25%
        }
    }
    private fun resizeZramSafely(newSizeBytes: Long): Boolean {
        val originalSelinuxMode = getSelinuxModeInternal()
        val needsSelinuxChange = originalSelinuxMode.equals("Enforcing", ignoreCase = true)
        var overallSuccess = true

        if (needsSelinuxChange) {
            Log.i(TAG, "[ZRAM SELinux] Current SELinux mode: $originalSelinuxMode. Setting to Permissive for ZRAM ops.")
            if (!setSelinuxModeInternal(false)) {
                Log.e(TAG, "[ZRAM SELinux] Failed to set SELinux to Permissive for ZRAM ops.")
            }
        }

        if (!executeShellCommand("swapoff /dev/block/zram0 2>/dev/null || true")) overallSuccess = false
        if (!executeShellCommand("echo 1 > $zramResetPath")) overallSuccess = false
        if (!executeShellCommand("echo $newSizeBytes > $zramDisksizePath")) overallSuccess = false
        if (!executeShellCommand("mkswap /dev/block/zram0 2>/dev/null || true")) overallSuccess = false
        if (!executeShellCommand("swapon /dev/block/zram0 2>/dev/null || true")) overallSuccess = false

        if (needsSelinuxChange) {
            Log.i(TAG, "[ZRAM SELinux] Restoring SELinux mode to Enforcing after ZRAM ops.")
            if (!setSelinuxModeInternal(true)) {
                Log.w(TAG, "[ZRAM SELinux] Failed to restore SELinux to Enforcing after ZRAM ops.")
            }
        }
        return overallSuccess
    }

    fun setZramEnabled(enabled: Boolean): Flow<Boolean> = flow {
        resizeZramSafely(if (enabled) calculateMaxZramSize() else 0)
        emit(readShellCommand("cat $zramInitStatePath").trim() == "1")
    }.flowOn(Dispatchers.IO)

    fun setZramDisksize(sizeBytes: Long): Boolean {
        return resizeZramSafely(sizeBytes)
    }

    fun getZramDisksize(): Flow<Long> = flow {
        emit(readShellCommand("cat $zramDisksizePath").toLongOrNull() ?: 0L)
    }.flowOn(Dispatchers.IO)

    fun getZramEnabled(): Flow<Boolean> = flow {
        emit(readShellCommand("cat $zramInitStatePath").trim() == "1")
    }.flowOn(Dispatchers.IO)

    fun setCompressionAlgorithm(algo: String): Boolean {
        val currentSize = readShellCommand("cat $zramDisksizePath").toLongOrNull() ?: 0L
        if (readShellCommand("if [ -e $zramControlPath ]; then echo 1; else echo 0; fi").trim() != "1") {
            Log.w(TAG, "ZRAM node $zramControlPath does not exist. Cannot set compression algorithm.")
            return false
        }

        // Tangani SELinux untuk blok perintah ini
        val originalSelinuxMode = getSelinuxModeInternal()
        val needsSelinuxChange = originalSelinuxMode.equals("Enforcing", ignoreCase = true)

        if (needsSelinuxChange) {
            setSelinuxModeInternal(false)
        }

        executeShellCommand("chmod 666 $zramCompAlgorithmPath")

        val commands = if (currentSize > 0) {
            """
            swapoff /dev/block/zram0 2>/dev/null || true
            echo 1 > $zramResetPath
            echo $algo > $zramCompAlgorithmPath
            echo $currentSize > $zramDisksizePath
            mkswap /dev/block/zram0 2>/dev/null || true
            swapon /dev/block/zram0 2>/dev/null || true
            """.trimIndent()
        } else {
            "echo $algo > $zramCompAlgorithmPath"
        }

        val success = executeShellCommand(commands)

        if (needsSelinuxChange) {
            setSelinuxModeInternal(true)
        }
        return success
    }

    fun getCompressionAlgorithms(): Flow<List<String>> = flow {
        emit(readShellCommand("cat $zramCompAlgorithmPath").split(" ").filter { it.isNotBlank() }.sorted())
    }.flowOn(Dispatchers.IO)

    fun getCurrentCompression(): Flow<String> = flow {
        val raw = readShellCommand("cat $zramCompAlgorithmPath").trim()
        val active = raw.split(" ").firstOrNull { it.startsWith("[") }
            ?.removeSurrounding("[", "]")
            ?: raw.split(" ").firstOrNull()
            ?: "lz4"
        emit(active)
    }.flowOn(Dispatchers.IO)
    fun setSwappiness(value: Int): Boolean =
        runTuningCommand("echo $value > $swappinessPath")

    fun getSwappiness(): Flow<Int> = flow {
        emit(readShellCommand("cat $swappinessPath").toIntOrNull() ?: 60)
    }.flowOn(Dispatchers.IO)

    fun setDirtyRatio(value: Int): Boolean =
        runTuningCommand("echo $value > $dirtyRatioPath")

    fun getDirtyRatio(): Flow<Int> = flow {
        emit(readShellCommand("cat $dirtyRatioPath").toIntOrNull() ?: 20)
    }.flowOn(Dispatchers.IO)

    fun setDirtyBackgroundRatio(value: Int): Boolean =
        runTuningCommand("echo $value > $dirtyBackgroundRatioPath")

    fun getDirtyBackgroundRatio(): Flow<Int> = flow {
        emit(readShellCommand("cat $dirtyBackgroundRatioPath").toIntOrNull() ?: 10)
    }.flowOn(Dispatchers.IO)

    fun setDirtyWriteback(valueCentisecs: Int): Boolean =
        runTuningCommand("echo $valueCentisecs > $dirtyWritebackCentisecsPath")

    fun getDirtyWriteback(): Flow<Int> = flow {
        emit((readShellCommand("cat $dirtyWritebackCentisecsPath").toIntOrNull() ?: 3000) / 100)
    }.flowOn(Dispatchers.IO)

    fun setDirtyExpireCentisecs(valueCentisecsInput: Int): Boolean =
        runTuningCommand("echo $valueCentisecsInput > $dirtyExpireCentisecsPath")

    fun getDirtyExpireCentisecs(): Flow<Int> = flow {
        emit((readShellCommand("cat $dirtyExpireCentisecsPath").toIntOrNull() ?: 3000) / 100)
    }.flowOn(Dispatchers.IO)

    fun setMinFreeMemory(valueKBytes: Int): Boolean =
        runTuningCommand("echo $valueKBytes > $minFreeKbytesPath")

    fun getMinFreeMemory(): Flow<Int> = flow {
        emit(readShellCommand("cat $minFreeKbytesPath").toIntOrNull() ?: (128 * 1024))
    }.flowOn(Dispatchers.IO)


    /* ----------------------------------------------------------
       CPU
       ---------------------------------------------------------- */
    fun getCpuGov(cluster: String): Flow<String> = flow {
        emit(readShellCommand("cat ${cpuGovPath.format(cluster)}"))
    }.flowOn(Dispatchers.IO)

    fun setCpuGov(cluster: String, gov: String): Boolean {
        val chmodSuccess = runTuningCommand("chmod 666 ${cpuGovPath.format(cluster)}")
        return chmodSuccess && runTuningCommand("echo $gov > ${cpuGovPath.format(cluster)}")
    }

    fun getCpuFreq(cluster: String): Flow<Pair<Int, Int>> = flow {
        val min = readShellCommand("cat ${cpuMinFreqPath.format(cluster)}").toIntOrNull() ?: 0
        val max = readShellCommand("cat ${cpuMaxFreqPath.format(cluster)}").toIntOrNull() ?: 0
        emit(min to max)
    }.flowOn(Dispatchers.IO)

    fun setCpuFreq(cluster: String, min: Int, max: Int): Boolean {
        val chmodMinSuccess = runTuningCommand("chmod 666 ${cpuMinFreqPath.format(cluster)}")
        val chmodMaxSuccess = runTuningCommand("chmod 666 ${cpuMaxFreqPath.format(cluster)}")
        val setMinSuccess = runTuningCommand("echo $min > ${cpuMinFreqPath.format(cluster)}")
        val setMaxSuccess = runTuningCommand("echo $max > ${cpuMaxFreqPath.format(cluster)}")
        return chmodMinSuccess && chmodMaxSuccess && setMinSuccess && setMaxSuccess
    }

    fun getAvailableCpuGovernors(cluster: String): Flow<List<String>> = flow {
        emit(readShellCommand("cat ${cpuAvailableGovsPath.format(cluster)}")
            .split(" ")
            .filter { it.isNotBlank() }
            .sorted())
    }.flowOn(Dispatchers.IO)

    fun getAvailableCpuFrequencies(cluster: String): Flow<List<Int>> = flow {
        emit(readShellCommand("cat ${cpuAvailableFreqsPath.format(cluster)}")
            .split(" ")
            .mapNotNull { it.toIntOrNull() }
            .sorted())
    }.flowOn(Dispatchers.IO)

    fun setCoreOnline(coreId: Int, isOnline: Boolean): Boolean {
        val value = if (isOnline) 1 else 0
        // Chmod mungkin diperlukan dan harus berada dalam blok SELinux jika digunakan
        var chmodSuccess = true
        if (coreId >= 4) {
            chmodSuccess = runTuningCommand("chmod 666 ${coreOnlinePath.format(coreId)}")
        }
        return chmodSuccess && runTuningCommand("echo $value > ${coreOnlinePath.format(coreId)}")
    }

    fun getCoreOnline(coreId: Int): Boolean =
        readShellCommand("cat ${coreOnlinePath.format(coreId)}").trim() == "1"


    fun getNumberOfCores(): Int {
        val presentCores = readShellCommand("cat /sys/devices/system/cpu/present") // e.g., "0-7"
        if (presentCores.contains("-")) {
            try {
                return presentCores.split("-").last().toInt() + 1
            } catch (e: NumberFormatException) {
            }
        }

        var count = 0
        while (readShellCommand("if [ -d /sys/devices/system/cpu/cpu$count ]; then echo 1; else echo 0; fi").trim() == "1") {
            count++
        }
        return if (count > 0) count else 8
    }


    /* ----------------------------------------------------------
       GPU
       ---------------------------------------------------------- */
    fun getGpuGov(): Flow<String> = flow {
        emit(readShellCommand("cat $gpuGovPath"))
    }.flowOn(Dispatchers.IO)

    fun setGpuGov(gov: String): Boolean {
        val chmodSuccess = runTuningCommand("chmod 666 $gpuGovPath")
        return chmodSuccess && runTuningCommand("echo $gov > $gpuGovPath")
    }

    fun getGpuFreq(): Flow<Pair<Int, Int>> = flow {
        Log.d("MyGpuDebug", "getGpuFreq: Memulai pembacaan frekuensi GPU.")

        Log.d("MyGpuDebug", "getGpuFreq: Membaca min_freq dari path: $gpuMinFreqPath")
        val rawMinOutput = readShellCommand("cat $gpuMinFreqPath").trim()
        Log.d("MyGpuDebug", "getGpuFreq: rawMinOutput dari readShellCommand: [$rawMinOutput]")

        Log.d("MyGpuDebug", "getGpuFreq: Membaca max_freq dari path: $gpuMaxFreqPath")
        val rawMaxOutput = readShellCommand("cat $gpuMaxFreqPath").trim()
        Log.d("MyGpuDebug", "getGpuFreq: rawMaxOutput dari readShellCommand: [$rawMaxOutput]")

        val minHz = rawMinOutput.toIntOrNull() ?: 0
        Log.d("MyGpuDebug", "getGpuFreq: minHz setelah toIntOrNull (default 0): $minHz")

        val maxHz = rawMaxOutput.toIntOrNull() ?: 0
        Log.d("MyGpuDebug", "getGpuFreq: maxHz setelah toIntOrNull (default 0): $maxHz")

        val minMhzResult = if (minHz == 0) 0 else minHz / 1000000
        val maxMhzResult = if (maxHz == 0) 0 else maxHz / 1000000
        Log.d("MyGpuDebug", "getGpuFreq: Mengirimkan pasangan (minMhz, maxMhz): ($minMhzResult, $maxMhzResult)")

        emit(minMhzResult to maxMhzResult)
    }.flowOn(Dispatchers.IO)


    fun getCurrentGpuFreq(): Flow<Int> = flow {
        Log.d("MyGpuDebug", "getCurrentGpuFreq: Membaca frekuensi GPU saat ini dari path: $gpuCurFreqPath")
        val rawOutput = readShellCommand("cat $gpuCurFreqPath").trim()
        Log.d("MyGpuDebug", "getCurrentGpuFreq: rawOutput dari readShellCommand: [$rawOutput]")

        val freqHz = rawOutput.toIntOrNull() ?: 0
        Log.d("MyGpuDebug", "getCurrentGpuFreq: freqHz setelah toIntOrNull (default 0): $freqHz")

        val freqMhzResult = if (freqHz == 0) 0 else freqHz / 1000000
        Log.d("MyGpuDebug", "getCurrentGpuFreq: freqMhzResult setelah konversi ke MHz: $freqMhzResult")

        emit(freqMhzResult)
    }.flowOn(Dispatchers.IO)


    fun setGpuMinFreq(freqMHz: Int): Boolean {
        Log.d(TAG, "setGpuMinFreq: Menyetel ke $freqMHz MHz (nilai Hz: ${freqMHz * 1000000})")
        val success = runTuningCommand("echo ${freqMHz * 1000000} > $gpuMinFreqPath")
        if (success) {
            val valueAfterSet = readShellCommand("cat $gpuMinFreqPath").trim()
            Log.i(TAG, "setGpuMinFreq: VERIFIKASI - Nilai di $gpuMinFreqPath setelah set: [$valueAfterSet]")
        } else {
            Log.e(TAG, "setGpuMinFreq: Gagal menjalankan runTuningCommand")
        }
        return success
    }

    fun setGpuMaxFreq(freqMHz: Int): Boolean {
        Log.d(TAG, "setGpuMaxFreq: Menyetel ke $freqMHz MHz (nilai Hz: ${freqMHz * 1000000})")
        val success = runTuningCommand("echo ${freqMHz * 1000000} > $gpuMaxFreqPath")
        if (success) {
            val valueAfterSet = readShellCommand("cat $gpuMaxFreqPath").trim()
            Log.i(TAG, "setGpuMaxFreq: VERIFIKASI - Nilai di $gpuMaxFreqPath setelah set: [$valueAfterSet]")
        } else {
            Log.e(TAG, "setGpuMaxFreq: Gagal menjalankan runTuningCommand")
        }
        return success
    }



    fun getAvailableGpuGovernors(): Flow<List<String>> = flow {
        emit(readShellCommand("cat $gpuAvailableGovsPath")
            .split(" ")
            .filter { it.isNotBlank() }
            .sorted())
    }.flowOn(Dispatchers.IO)

    fun getAvailableGpuFrequencies(): Flow<List<Int>> = flow {
        emit(readShellCommand("cat $gpuAvailableFreqsPath")
            .split(" ")
            .mapNotNull { it.toIntOrNull() }
            .map { freqHz -> if (freqHz > 100000) freqHz / 1000000 else freqHz }
            .sorted())
    }.flowOn(Dispatchers.IO)

    fun getGpuPowerLevelRange(): Flow<Pair<Float, Float>> = flow {
        // Asumsi nilai power level adalah integer
        val min = readShellCommand("cat $gpuMinPowerLevelPath").toFloatOrNull() ?: 0f
        val max = readShellCommand("cat $gpuMaxPowerLevelPath").toFloatOrNull() ?: 0f
        emit(min to max)
    }.flowOn(Dispatchers.IO)

    fun getCurrentGpuPowerLevel(): Flow<Float> = flow {
        emit(readShellCommand("cat $gpuCurrentPowerLevelPath").toFloatOrNull() ?: 0f)
    }.flowOn(Dispatchers.IO)

    fun setGpuPowerLevel(level: Float): Boolean =
        runTuningCommand("echo ${level.toInt()} > $gpuCurrentPowerLevelPath")


    /* ----------------------------------------------------------
       Thermal
       ---------------------------------------------------------- */
    fun getCurrentThermalModeIndex(): Flow<Int> = flow {
        // Jika pembacaan gagal, -1 adalah indikator yang baik
        emit(readShellCommand("cat $thermalSysfsNode").toIntOrNull() ?: -1)
    }.flowOn(Dispatchers.IO)

    fun setThermalModeIndex(modeIndex: Int): Flow<Boolean> = flow {
        val preChmodOk = runTuningCommand("chmod 0666 $thermalSysfsNode")
        val writeOk = runTuningCommand("echo $modeIndex > $thermalSysfsNode")
        val postChmodOk = runTuningCommand("chmod 0666 $thermalSysfsNode")
        emit(preChmodOk && writeOk && postChmodOk)
    }.flowOn(Dispatchers.IO)

    /* ----------------------------------------------------------
       OpenGL / Vulkan / Renderer
       ---------------------------------------------------------- */
    fun getOpenGlesDriver(): Flow<String> = flow {
        val rawOutput = readShellCommand("dumpsys SurfaceFlinger | grep \"GLES:\"")
        val glesInfo = rawOutput.substringAfter("GLES:", "").trim()
        emit(
            if (glesInfo.isNotBlank()) glesInfo else "N/A"
        )
    }.flowOn(Dispatchers.IO)

    fun getGpuRenderer(): Flow<String> = flow {
        // First check runtime property
        val rendererProp = readShellCommand("getprop debug.hwui.renderer").trim()

        // Also check persistent settings to ensure consistency
        val persistentRenderer = getPersistentGpuRenderer()

        Log.d(TAG, "Current renderer prop: '$rendererProp', persistent: '$persistentRenderer'")

        // Use persistent setting if runtime property is empty or default
        val effectiveRenderer = if (rendererProp.isEmpty() || rendererProp == "null") {
            persistentRenderer.ifEmpty { "" }
        } else {
            rendererProp
        }

        // Map system property values to user-friendly names
        val currentRenderer = when {
            effectiveRenderer.isEmpty() || effectiveRenderer == "null" -> "Default"
            effectiveRenderer.equals("opengl", ignoreCase = true) -> "OpenGL"
            effectiveRenderer.equals("vulkan", ignoreCase = true) -> "Vulkan"
            effectiveRenderer.equals("skiagl", ignoreCase = true) -> "OpenGL (SKIA)"
            effectiveRenderer.equals("skiavk", ignoreCase = true) -> "Vulkan (SKIA)"
            effectiveRenderer.equals("angle", ignoreCase = true) -> "ANGLE"
            else -> {
                Log.w(TAG, "Unknown renderer property: '$effectiveRenderer', defaulting to OpenGL")
                "OpenGL"
            }
        }

        Log.d(TAG, "Mapped renderer: '$currentRenderer'")
        emit(currentRenderer)
    }.flowOn(Dispatchers.IO)

    private fun getPersistentGpuRenderer(): String {
        // Check multiple sources for persistent settings
        val sources = listOf(
            "/system/build.prop",
            "/system/etc/system.prop",
            "/vendor/build.prop"
        )

        for (source in sources) {
            try {
                val content = readShellCommand("cat $source 2>/dev/null || echo ''")
                if (content.isNotEmpty()) {
                    val lines = content.lines()
                    val rendererLine = lines.find { it.trim().startsWith("debug.hwui.renderer=") }
                    if (rendererLine != null) {
                        val value = rendererLine.substringAfter("=").trim()
                        if (value.isNotEmpty()) {
                            Log.d(TAG, "Found persistent renderer in $source: $value")
                            return value
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read $source", e)
            }
        }

        return ""
    }

    fun setGpuRenderer(renderer: String): Flow<Boolean> = flow {
        Log.d(TAG, "Setting GPU renderer to: '$renderer'")

        // Map user-friendly names to system property values
        val propertyValue = when (renderer) {
            "Default" -> ""
            "OpenGL" -> "opengl"
            "Vulkan" -> "vulkan"
            "OpenGL (SKIA)" -> "skiagl"
            "Vulkan (SKIA)" -> "skiavk"
            "ANGLE" -> "angle"
            else -> {
                Log.e(TAG, "Unknown renderer type: '$renderer'")
                emit(false)
                return@flow
            }
        }

        var success = true

        try {
            // First, set runtime properties for immediate effect
            val clearCommands = listOf(
                "setprop debug.hwui.renderer \"\"",
                "setprop debug.hwui.skia_atrace_enabled \"\"",
                "setprop ro.hwui.use_vulkan \"\"",
                "setprop debug.angle.backend \"\""
            )

            clearCommands.forEach { cmd ->
                if (!executeShellCommand(cmd)) {
                    Log.w(TAG, "Failed to clear property with command: $cmd")
                }
            }

            // Set the new renderer property for immediate effect
            if (propertyValue.isNotEmpty()) {
                val setCommand = "setprop debug.hwui.renderer \"$propertyValue\""
                val result = executeShellCommand(setCommand)

                // Set additional properties for specific renderers - SAFER approach for custom ROMs
                when (renderer) {
                    "Vulkan", "Vulkan (SKIA)" -> {
                        // Only set essential Vulkan properties to avoid bootloop
                        executeShellCommand("setprop ro.hwui.use_vulkan true")
                        Log.i(TAG, "Set basic Vulkan properties. Additional settings will be applied via vendor prop.")
                    }
                    "ANGLE" -> {
                        executeShellCommand("setprop debug.angle.backend opengl")
                    }
                }

                if (!result) {
                    success = false
                }
            }

            // Now make the settings persistent by modifying system files
            val persistentSuccess = makePersistentGpuRendererSettings(renderer, propertyValue)
            if (!persistentSuccess) {
                Log.w(TAG, "Failed to make GPU renderer settings persistent, but runtime settings applied")
            }

            if (renderer.contains("Vulkan") && success) {
                Log.i(TAG, "Vulkan settings applied successfully via vendor prop")
                Log.i(TAG, "IMPORTANT: Please REBOOT your device to fully activate Vulkan render engine")
                Log.i(TAG, "Do NOT restart SurfaceFlinger manually as it may cause bootloop on your custom ROM")
            }

            if (success) {
                Log.i(TAG, "Successfully set GPU renderer to: '$renderer' (property: '$propertyValue')")
                // Verify the setting was applied
                val verifyCommand = "getprop debug.hwui.renderer"
                val actualValue = readShellCommand(verifyCommand).trim()
                Log.d(TAG, "Verification - actual property value: '$actualValue'")
            } else {
                Log.e(TAG, "Failed to set GPU renderer to: '$renderer'")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception while setting GPU renderer", e)
            success = false
        }

        emit(success)
    }.flowOn(Dispatchers.IO)

    private fun makePersistentGpuRendererSettings(renderer: String, propertyValue: String): Boolean {
        Log.d(TAG, "Making GPU renderer settings persistent for: $renderer")

        try {
            val vendorSuccess = setPersistentViaVendorProp(renderer, propertyValue)
            if (vendorSuccess) {
                Log.i(TAG, "Successfully applied GPU settings via vendor.prop - this is the recommended approach for your custom ROM")
                return true
            }

            Log.w(TAG, "Vendor prop approach failed, trying alternative methods...")
            val approaches = listOf(
                // Approach 2: build.prop modification
                ::setPersistentViaBuildProp,
                // Approach 3: system.prop in /system/etc/
                ::setPersistentViaSystemProp,
                // Approach 4: Create init.d script (for devices with init.d support)
                ::setPersistentViaInitD
            )

            var anySuccess = false
            approaches.forEach { approach ->
                try {
                    if (approach(renderer, propertyValue)) {
                        anySuccess = true
                        Log.i(TAG, "Successfully applied persistent setting via ${approach.javaClass.simpleName}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Approach ${approach.javaClass.simpleName} failed", e)
                }
            }

            return anySuccess

        } catch (e: Exception) {
            Log.e(TAG, "Exception in makePersistentGpuRendererSettings", e)
            return false
        }
    }

    private fun setPersistentViaBuildProp(renderer: String, propertyValue: String): Boolean {
        val buildPropPath = "/system/build.prop"
        val tempPath = "/data/local/tmp/build.prop.tmp"

        try {
            // Make system partition writable
            if (!executeShellCommand("mount -o remount,rw /system")) {
                Log.w(TAG, "Failed to remount /system as rw")
                return false
            }

            // Read current build.prop
            val currentContent = readShellCommand("cat $buildPropPath")
            if (currentContent.isEmpty()) {
                Log.w(TAG, "Failed to read build.prop")
                return false
            }

            // Remove existing GPU renderer properties
            val cleanedContent = currentContent.lines()
                .filterNot { line ->
                    line.trim().startsWith("debug.hwui.renderer=") ||
                    line.trim().startsWith("ro.hwui.use_vulkan=") ||
                    line.trim().startsWith("debug.angle.backend=")
                }
                .joinToString("\n")

            // Add new properties if not default
            val newContent = if (propertyValue.isNotEmpty()) {
                val additionalProps = buildString {
                    appendLine("debug.hwui.renderer=$propertyValue")
                    when (renderer) {
                        "Vulkan", "Vulkan (SKIA)" -> {
                            appendLine("ro.hwui.use_vulkan=true")
                        }
                        "ANGLE" -> {
                            appendLine("debug.angle.backend=opengl")
                        }
                    }
                }
                "$cleanedContent\n$additionalProps"
            } else {
                cleanedContent
            }

            // Write to temp file first
            val writeSuccess = executeShellCommand("echo '$newContent' > $tempPath")
            if (!writeSuccess) {
                Log.w(TAG, "Failed to write temp file")
                return false
            }

            // Copy temp file to build.prop
            val copySuccess = executeShellCommand("cp $tempPath $buildPropPath")
            if (!copySuccess) {
                Log.w(TAG, "Failed to copy to build.prop")
                return false
            }
            // Set proper permissions
            executeShellCommand("chmod 644 $buildPropPath")
            executeShellCommand("chown root:root $buildPropPath")
            // Cleanup
            executeShellCommand("rm -f $tempPath")
            // Remount system as readonly
            executeShellCommand("mount -o remount,ro /system")

            Log.i(TAG, "Successfully updated build.prop for GPU renderer persistence")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Exception in setPersistentViaBuildProp", e)
            // Try to remount system as readonly in case of error
            executeShellCommand("mount -o remount,ro /system")
            executeShellCommand("rm -f $tempPath")
            return false
        }
    }

    private fun setPersistentViaSystemProp(renderer: String, propertyValue: String): Boolean {
        val systemPropPath = "/system/etc/system.prop"
        val tempPath = "/data/local/tmp/system.prop.tmp"

        try {
            // Make system partition writable
            if (!executeShellCommand("mount -o remount,rw /system")) {
                return false
            }
            // Read current system.prop if exists
            val currentContent = readShellCommand("cat $systemPropPath 2>/dev/null || echo ''")
            // Remove existing GPU renderer properties
            val cleanedContent = currentContent.lines()
                .filterNot { line ->
                    line.trim().startsWith("debug.hwui.renderer=") ||
                    line.trim().startsWith("ro.hwui.use_vulkan=") ||
                    line.trim().startsWith("debug.angle.backend=")
                }
                .joinToString("\n")

            // Add new properties if not default
            val newContent = if (propertyValue.isNotEmpty()) {
                val additionalProps = buildString {
                    appendLine("debug.hwui.renderer=$propertyValue")
                    when (renderer) {
                        "Vulkan", "Vulkan (SKIA)" -> {
                            appendLine("ro.hwui.use_vulkan=true")
                        }
                        "ANGLE" -> {
                            appendLine("debug.angle.backend=opengl")
                        }
                    }
                }
                if (cleanedContent.isBlank()) additionalProps else "$cleanedContent\n$additionalProps"
            } else {
                cleanedContent
            }

            // Write the content
            val writeSuccess = executeShellCommand("echo '$newContent' > $tempPath && cp $tempPath $systemPropPath")
            if (writeSuccess) {
                executeShellCommand("chmod 644 $systemPropPath")
                executeShellCommand("chown root:root $systemPropPath")
                executeShellCommand("rm -f $tempPath")
                executeShellCommand("mount -o remount,ro /system")
                Log.i(TAG, "Successfully updated system.prop")
                return true
            }

            executeShellCommand("mount -o remount,ro /system")
            executeShellCommand("rm -f $tempPath")
            return false

        } catch (e: Exception) {
            Log.e(TAG, "Exception in setPersistentViaSystemProp", e)
            executeShellCommand("mount -o remount,ro /system")
            executeShellCommand("rm -f $tempPath")
            return false
        }
    }

    private fun setPersistentViaVendorProp(renderer: String, propertyValue: String): Boolean {
        val vendorPropPath = "/vendor/build.prop"
        val tempPath = "/data/local/tmp/vendor.prop.tmp"

        try {
            // Check if vendor partition exists and is writable
            if (!executeShellCommand("test -f $vendorPropPath")) {
                return false
            }

            if (!executeShellCommand("mount -o remount,rw /vendor")) {
                return false
            }

            val currentContent = readShellCommand("cat $vendorPropPath")
            if (currentContent.isEmpty()) {
                executeShellCommand("mount -o remount,ro /vendor")
                return false
            }

            // Process similar to build.prop
            val cleanedContent = currentContent.lines()
                .filterNot { line ->
                    line.trim().startsWith("debug.hwui.renderer=") ||
                    line.trim().startsWith("ro.hwui.use_vulkan=") ||
                    line.trim().startsWith("debug.angle.backend=")
                }
                .joinToString("\n")

            val newContent = if (propertyValue.isNotEmpty()) {
                val additionalProps = buildString {
                    appendLine("debug.hwui.renderer=$propertyValue")
                    when (renderer) {
                        "Vulkan", "Vulkan (SKIA)" -> {
                            appendLine("ro.hwui.use_vulkan=true")
                        }
                        "ANGLE" -> {
                            appendLine("debug.angle.backend=opengl")
                        }
                    }
                }
                "$cleanedContent\n$additionalProps"
            } else {
                cleanedContent
            }

            val success = executeShellCommand("echo '$newContent' > $tempPath && cp $tempPath $vendorPropPath")
            if (success) {
                executeShellCommand("chmod 644 $vendorPropPath")
                executeShellCommand("chown root:root $vendorPropPath")
            }

            executeShellCommand("rm -f $tempPath")
            executeShellCommand("mount -o remount,ro /vendor")

            return success

        } catch (e: Exception) {
            Log.e(TAG, "Exception in setPersistentViaVendorProp", e)
            executeShellCommand("mount -o remount,ro /vendor")
            executeShellCommand("rm -f $tempPath")
            return false
        }
    }

    private fun setPersistentViaInitD(renderer: String, propertyValue: String): Boolean {
        val initdPath = "/system/etc/init.d"
        val scriptPath = "$initdPath/99gpu_renderer"

        try {
            // Check if init.d is supported
            if (!executeShellCommand("test -d $initdPath")) {
                // Try to create init.d directory
                if (!executeShellCommand("mount -o remount,rw /system && mkdir -p $initdPath")) {
                    return false
                }
            } else {
                if (!executeShellCommand("mount -o remount,rw /system")) {
                    return false
                }
            }

            // Create script content
            val scriptContent = if (propertyValue.isNotEmpty()) {
                buildString {
                    appendLine("#!/system/bin/sh")
                    appendLine("# GPU Renderer Configuration Script")
                    appendLine("# Generated by Xtra Kernel Manager")
                    appendLine("")
                    appendLine("setprop debug.hwui.renderer \"$propertyValue\"")
                    when (renderer) {
                        "Vulkan", "Vulkan (SKIA)" -> {
                            appendLine("setprop ro.hwui.use_vulkan true")
                        }
                        "ANGLE" -> {
                            appendLine("setprop debug.angle.backend opengl")
                        }
                    }
                    appendLine("")
                    appendLine("# End of GPU Renderer Configuration")
                }
            } else {
                // Create empty script or remove existing properties
                buildString {
                    appendLine("#!/system/bin/sh")
                    appendLine("# GPU Renderer Configuration Script - Reset to Default")
                    appendLine("# Generated by Xtra Kernel Manager")
                    appendLine("")
                    appendLine("setprop debug.hwui.renderer \"\"")
                    appendLine("setprop ro.hwui.use_vulkan \"\"")
                    appendLine("setprop debug.angle.backend \"\"")
                }
            }

            // Write script
            val tempScriptPath = "/data/local/tmp/99gpu_renderer.tmp"
            val writeSuccess = executeShellCommand("echo '$scriptContent' > $tempScriptPath")
            if (!writeSuccess) {
                executeShellCommand("mount -o remount,ro /system")
                return false
            }

            // Copy to init.d and set permissions
            val copySuccess = executeShellCommand("cp $tempScriptPath $scriptPath")
            if (copySuccess) {
                executeShellCommand("chmod 755 $scriptPath")
                executeShellCommand("chown root:root $scriptPath")
                Log.i(TAG, "Successfully created init.d script for GPU renderer")
            }

            executeShellCommand("rm -f $tempScriptPath")
            executeShellCommand("mount -o remount,ro /system")

            return copySuccess

        } catch (e: Exception) {
            Log.e(TAG, "Exception in setPersistentViaInitD", e)
            executeShellCommand("mount -o remount,ro /system")
            executeShellCommand("rm -f /data/local/tmp/99gpu_renderer.tmp")
            return false
        }
    }

    fun getVulkanApiVersion(): Flow<String> = flow {
        emit(readShellCommand("getprop ro.hardware.vulkan.version").ifEmpty { "N/A" })
    }.flowOn(Dispatchers.IO)

    fun rebootDevice(): Flow<Boolean> = flow {
        emit(executeShellCommand("reboot"))
    }.flowOn(Dispatchers.IO)

    /**
     * Restart SurfaceFlinger service to apply Vulkan settings properly
     * This is especially needed for Android 16 custom ROMs
     */
    private fun restartSurfaceFlinger(): Boolean {
        Log.i(TAG, "Attempting to restart SurfaceFlinger for Vulkan initialization")

        return try {
            // Method 1: Stop and start SurfaceFlinger service
            val stopSuccess = executeShellCommand("stop surfaceflinger")
            if (stopSuccess) {
                // Wait a moment for service to fully stop
                Thread.sleep(1000)
                val startSuccess = executeShellCommand("start surfaceflinger")
                if (startSuccess) {
                    Log.i(TAG, "SurfaceFlinger restart via stop/start successful")
                    return true
                }
            }

            // Method 2: Kill SurfaceFlinger process (it will auto-restart)
            Log.w(TAG, "Stop/start method failed, trying process kill method")
            val killSuccess = executeShellCommand("pkill -f surfaceflinger || killall surfaceflinger")
            if (killSuccess) {
                Log.i(TAG, "SurfaceFlinger restart via process kill successful")
                return true
            }

            // Method 3: Use service command
            Log.w(TAG, "Process kill method failed, trying service command")
            val serviceSuccess = executeShellCommand("service call SurfaceFlinger 1008")
            if (serviceSuccess) {
                Log.i(TAG, "SurfaceFlinger restart via service call successful")
                return true
            }

            Log.e(TAG, "All SurfaceFlinger restart methods failed")
            false

        } catch (e: Exception) {
            Log.e(TAG, "Exception during SurfaceFlinger restart", e)
            false
        }
    }

    /**
     * Check if Vulkan render engine is actually enabled in SurfaceFlinger
     * This addresses the issue where properties show true but dumpsys shows false
     */
    private fun checkVulkanRenderEngineStatus(): Boolean {
        return try {
            val dumpsysOutput = readShellCommand("dumpsys SurfaceFlinger")

            // Check for various indicators that Vulkan is enabled
            val vulkanIndicators = listOf(
                "vulkan_renderengine: true",
                "vulkan_renderengine:true",
                "RenderEngine: vulkan",
                "Vulkan API",
                "VkInstance",
                "Vulkan render engine active"
            )

            val vulkanEnabled = vulkanIndicators.any { indicator ->
                dumpsysOutput.contains(indicator, ignoreCase = true)
            }

            // Also check for negative indicators
            val negativeIndicators = listOf(
                "vulkan_renderengine: false",
                "vulkan_renderengine:false",
                "RenderEngine: gl",
                "RenderEngine: gles"
            )

            val vulkanDisabled = negativeIndicators.any { indicator ->
                dumpsysOutput.contains(indicator, ignoreCase = true)
            }

            // Log detailed information for debugging
            if (vulkanEnabled && !vulkanDisabled) {
                Log.i(TAG, "Vulkan render engine confirmed as ACTIVE in SurfaceFlinger")
                // Log which indicator was found
                vulkanIndicators.forEach { indicator ->
                    if (dumpsysOutput.contains(indicator, ignoreCase = true)) {
                        Log.d(TAG, "Found Vulkan indicator: $indicator")
                    }
                }
            } else if (vulkanDisabled) {
                Log.w(TAG, "Vulkan render engine confirmed as DISABLED in SurfaceFlinger")
                // Log which negative indicator was found
                negativeIndicators.forEach { indicator ->
                    if (dumpsysOutput.contains(indicator, ignoreCase = true)) {
                        Log.d(TAG, "Found negative Vulkan indicator: $indicator")
                    }
                }
            } else {
                Log.w(TAG, "Vulkan render engine status is UNCLEAR from SurfaceFlinger output")
            }

            vulkanEnabled && !vulkanDisabled

        } catch (e: Exception) {
            Log.e(TAG, "Exception while checking Vulkan render engine status", e)
            false
        }
    }

    /**
     * Get detailed SurfaceFlinger and Vulkan status for debugging
     * This helps diagnose Android 16 custom ROM compatibility issues
     */
    fun getVulkanRenderEngineStatus(): Flow<Map<String, String>> = flow {
        val statusMap = mutableMapOf<String, String>()

        try {
            // Get basic properties
            statusMap["debug.hwui.renderer"] = readShellCommand("getprop debug.hwui.renderer").ifEmpty { "not_set" }
            statusMap["ro.hwui.use_vulkan"] = readShellCommand("getprop ro.hwui.use_vulkan").ifEmpty { "not_set" }
            statusMap["debug.hwui.force_vulkan"] = readShellCommand("getprop debug.hwui.force_vulkan").ifEmpty { "not_set" }
            statusMap["ro.surface_flinger.use_vk_drivers"] = readShellCommand("getprop ro.surface_flinger.use_vk_drivers").ifEmpty { "not_set" }

            // Get Vulkan API version and hardware info
            statusMap["ro.hardware.vulkan.version"] = readShellCommand("getprop ro.hardware.vulkan.version").ifEmpty { "not_available" }
            statusMap["ro.hardware.vulkan.level"] = readShellCommand("getprop ro.hardware.vulkan.level").ifEmpty { "not_available" }

            // Check SurfaceFlinger dumpsys output
            val dumpsysOutput = readShellCommand("dumpsys SurfaceFlinger")
            val vulkanEngineMatch = Regex("vulkan_renderengine:\\s*(true|false)", RegexOption.IGNORE_CASE)
                .find(dumpsysOutput)
            statusMap["surfaceflinger_vulkan_engine"] = vulkanEngineMatch?.groupValues?.get(1) ?: "not_found"

            // Check for render engine type
            val renderEngineMatch = Regex("RenderEngine:\\s*(\\w+)", RegexOption.IGNORE_CASE)
                .find(dumpsysOutput)
            statusMap["render_engine_type"] = renderEngineMatch?.groupValues?.get(1) ?: "not_found"

            // Check Android version and build info for custom ROM detection
            statusMap["android_version"] = readShellCommand("getprop ro.build.version.release").ifEmpty { "unknown" }
            statusMap["android_sdk"] = readShellCommand("getprop ro.build.version.sdk").ifEmpty { "unknown" }
            statusMap["build_type"] = readShellCommand("getprop ro.build.type").ifEmpty { "unknown" }
            statusMap["build_tags"] = readShellCommand("getprop ro.build.tags").ifEmpty { "unknown" }

            // Check for custom ROM indicators
            val customRomIndicators = listOf(
                "ro.modversion",
                "ro.build.display.id",
                "ro.custom.build.version",
                "ro.lineage.version",
                "ro.arrow.version"
            )

            customRomIndicators.forEach { prop ->
                val value = readShellCommand("getprop $prop")
                if (value.isNotEmpty()) {
                    statusMap["custom_rom_$prop"] = value
                }
            }

            // Check SELinux status as it can affect Vulkan
            statusMap["selinux_status"] = getSelinuxModeInternal()

            Log.d(TAG, "Vulkan status check completed. Found ${statusMap.size} properties")

        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting Vulkan render engine status", e)
            statusMap["error"] = e.message ?: "unknown_error"
        }

        emit(statusMap.toMap())
    }.flowOn(Dispatchers.IO)
    /* ----------------------------------------------------------
       Fallback Shell using Runtime.exec
       ---------------------------------------------------------- */
    private fun executeShellCommandFallback(cmd: String): Boolean {
        Log.d(TAG, "[Shell EXEC Fallback] Executing: '$cmd'")
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            process.inputStream.bufferedReader().use { it.readText() }
            val errorOutput = process.errorStream.bufferedReader().use { it.readText() }

            val exitCode = process.waitFor()
            return if (exitCode == 0) {
                Log.i(TAG, "[Shell EXEC Fallback] Success (code $exitCode): '$cmd'")
                true
            } else {
                Log.e(TAG, "[Shell EXEC Fallback] Failed (code $exitCode): '$cmd'. Err: $errorOutput")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "[Shell EXEC Fallback] Exception for cmd: '$cmd'", e)
            return false
        }
    }

    private fun readShellCommandFallback(cmd: String): String {
        Log.d(TAG, "[Shell R Fallback] Executing: '$cmd'")
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val errorOutput = process.errorStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()

            return if (exitCode == 0) {
                output.trim()
            } else {
                Log.e(TAG, "[Shell R Fallback] Failed (code $exitCode) for cmd: '$cmd'. Err: $errorOutput")
                ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "[Shell R Fallback] Exception for cmd: '$cmd'", e)
            return ""
        }
    }

    // Traditional swap size management (not ZRAM)
    fun setSwapSize(sizeBytes: Long): Boolean {
        return try {
            if (sizeBytes == 0L) {
                // Disable swap
                executeShellCommand("swapoff -a")
            } else {
                // Create or resize swap file
                val swapFile = "/data/swapfile"
                val sizeMB = sizeBytes / 1024 / 1024

                // Remove existing swap file
                executeShellCommand("swapoff $swapFile 2>/dev/null || true")
                executeShellCommand("rm -f $swapFile")

                if (sizeMB > 0) {
                    // Create new swap file
                    val success = executeShellCommand("dd if=/dev/zero of=$swapFile bs=1M count=$sizeMB") &&
                                 executeShellCommand("chmod 600 $swapFile") &&
                                 executeShellCommand("mkswap $swapFile") &&
                                 executeShellCommand("swapon $swapFile")
                    success
                } else {
                    true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting swap size: ${e.message}")
            false
        }
    }

    fun getSwapSize(): Flow<Long> = flow {
        val swapInfo = readShellCommand("cat /proc/swaps")
        val swapFile = "/data/swapfile"
        val lines = swapInfo.split("\n")

        for (line in lines) {
            if (line.contains(swapFile)) {
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size >= 3) {
                    val sizeKB = parts[2].toLongOrNull() ?: 0L
                    emit(sizeKB * 1024) // Convert KB to bytes
                    return@flow
                }
            }
        }
        emit(0L) // No swap file found
    }.flowOn(Dispatchers.IO)
}
