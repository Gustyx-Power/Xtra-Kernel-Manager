package id.xtramanagersoftware.xk.manager

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Method
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class Homepage : AppCompatActivity() {
    private lateinit var cpuCoreTextViews: List<TextView>
    private lateinit var kernelNameText: TextView
    private lateinit var cpuGovText: TextView
    private lateinit var cpuMaxText: TextView
    private lateinit var buttoncpu: ImageButton
    private lateinit var buttonGpu: ImageButton
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private lateinit var cpuTemperatureText: TextView
    private lateinit var socTemperatureText: TextView
    private lateinit var gpuFrequencyText: TextView
    private lateinit var gpuMaxFrequencyText: TextView
    private lateinit var androidLogo: ImageView
    private lateinit var deviceNameText: TextView
    private lateinit var androidVersionText: TextView
    private lateinit var socModelTextView: TextView
    private lateinit var uptimeText: TextView
    private lateinit var deepSleepTimeText: TextView
    private lateinit var footer: LinearLayout
    private lateinit var gpuinfo: TextView
    private lateinit var infoOpenGL: TextView
    private lateinit var tvCPUUsageLabel: TextView
    private lateinit var progressBarCPUUsage: ProgressBar
    private lateinit var tvGPUUsageLabel: TextView
    private lateinit var progressBarGPUUsage: ProgressBar
    private lateinit var tvBatteryPercentage: TextView
    private lateinit var tvBatteryTemperature: TextView
    private lateinit var tvChargingStatus: TextView
    private lateinit var progressBarBattery: ProgressBar
    private lateinit var tvBatteryCurrent: TextView
    private lateinit var tvBatteryPower: TextView
    private lateinit var tvBatteryVoltage: TextView
    private lateinit var tvTotalRam: TextView
    private lateinit var tvFreeRam: TextView
    private lateinit var tvUsedRam: TextView


    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingInflatedId", "ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = resources.configuration
        config.densityDpi = 420
        resources.updateConfiguration(/* config = */ config, /* metrics = */
            resources.displayMetrics
        )
        setContentView(R.layout.homepage)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.show(WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }


       // Function List
        cpuCoreTextViews = listOf(
            findViewById(R.id.cpuCore0),
            findViewById(R.id.cpuCore1),
            findViewById(R.id.cpuCore2),
            findViewById(R.id.cpuCore3),
            findViewById(R.id.cpuCore4),
            findViewById(R.id.cpuCore5),
            findViewById(R.id.cpuCore6),
            findViewById(R.id.cpuCore7)
        )
        kernelNameText = findViewById(R.id.kernelName)
        cpuGovText = findViewById(R.id.cpuGov)
        cpuMaxText = findViewById(R.id.cpuMax)
        buttoncpu = findViewById(R.id.btnCPU)
        buttonGpu = findViewById(R.id.btnGPU)
        cpuTemperatureText = findViewById(R.id.cpuTemperature)
        socTemperatureText = findViewById(R.id.socTemperature)
        gpuFrequencyText = findViewById(R.id.gpuFrequency)
        gpuMaxFrequencyText = findViewById(R.id.gpuMaxFrequency)
        androidLogo = findViewById(R.id.androidLogo)
        deviceNameText = findViewById(R.id.deviceName)
        androidVersionText = findViewById(R.id.androidVersion)
        socModelTextView = findViewById(R.id.socModel)
        uptimeText = findViewById(R.id.uptime)
        deepSleepTimeText = findViewById(R.id.deepSleepTime)
        footer = findViewById(R.id.footer)
        gpuinfo = findViewById(R.id.GPUName)
        infoOpenGL = findViewById(R.id.infoOpenGL)
        tvCPUUsageLabel = findViewById(R.id.tvCPUUsageLabel)
        progressBarCPUUsage = findViewById(R.id.progressBarCPUUsage)
        tvGPUUsageLabel = findViewById(R.id.tvGPUUsageLabel)
        progressBarGPUUsage = findViewById(R.id.progressBarGPUUsage)
        tvBatteryPercentage = findViewById(R.id.tvBatteryPercentage)
        tvBatteryTemperature = findViewById(R.id.tvBatteryTemperature)
        tvChargingStatus = findViewById(R.id.tvChargingStatus)
        tvBatteryCurrent = findViewById(R.id.tvBatteryCurrent)
        tvBatteryPower = findViewById(R.id.tvBatteryPower)
        tvBatteryVoltage = findViewById(R.id.tvBatteryVoltage)
        tvTotalRam = findViewById(R.id.tvTotalRam)
        tvFreeRam = findViewById(R.id.tvFreeRam)
        tvUsedRam = findViewById(R.id.tvUsedRam)
        progressBarBattery = findViewById(R.id.progressBarBattery)

        registerReceiver(batteryInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))




        // Integer List
        displayKernelName()
        setAndroidLogo()
        displaySystemInfo()
        displayDeviceName()
        displaySocModel()
        adjustFooterHeight()
        displayGPUInfo()
        displayGraphicsDriverInfo()
        checkRootAndShowPopup()

        runnable = Runnable {
            displayCpuInfo()
            displayCpuMaxClockSpeed()
            displayTemperatures()
            displayGpuFrequencies()
            displayUptime()
            displayCpuUsage()
            displayGpuUsage()
            displayDeepSleepTime()
            updateRamInfo()
            handler.postDelayed(runnable, 500)
        }
        handler.post(runnable)

        buttoncpu.setOnClickListener {
            val intent = Intent(this, CpuControl::class.java)
            startActivity(intent)
        }

        buttonGpu.setOnClickListener {
            val intent = Intent(this, GpuControlActivity::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("PrivateApi")
    fun getSystemProperty(key: String, defaultValue: String): String {
        return try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val getMethod: Method =
                systemProperties.getMethod("get", String::class.java, String::class.java)
            getMethod.invoke(systemProperties, key, defaultValue) as String
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }


    @SuppressLint("SetTextI18n")
    private fun displayCpuInfo() {
        for (core in cpuCoreTextViews.indices) {
            try {
                val process = Runtime.getRuntime()
                    .exec("cat /sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq")
                val result =
                    process.inputStream.bufferedReader().readText().trim().toLongOrNull() ?: 0L
                val resultMHz = result / 1000
                cpuCoreTextViews[core].text = "$resultMHz MHz"
            } catch (e: Exception) {
                cpuCoreTextViews[core].text = "$core: N/A"
            }
        }
        displayCpuGovernor()
    }

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private fun adjustFooterHeight() {
        ViewCompat.setOnApplyWindowInsetsListener(footer) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val navigationBarHeight = systemBarsInsets.bottom

            val footerLayoutParams = footer.layoutParams as ViewGroup.LayoutParams

            if (navigationBarHeight > 0) {
                footerLayoutParams.height =
                    resources.getDimensionPixelSize(R.dimen.footer_height_navigation_3_buttons)
            } else {
                footerLayoutParams.height =
                    resources.getDimensionPixelSize(R.dimen.footer_height_navigation_gesture)
            }

            footer.layoutParams = footerLayoutParams
            insets
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayCpuUsage() {
        try {
            val cpuUsage = getCPUUsage()

            if (cpuUsage >= 0) {
                progressBarCPUUsage.progress = cpuUsage
                tvCPUUsageLabel.text = "Usage: $cpuUsage%"
            } else {
                progressBarCPUUsage.progress = 0
                tvCPUUsageLabel.text = "Usage: 0%"
            }
        } catch (e: Exception) {
            progressBarCPUUsage.progress = 0
            tvCPUUsageLabel.text = "Usage: 0%"
            Log.e("displayCpuUsage", "Error displaying CPU usage", e)
        }
    }

    private fun getCPUUsage(): Int {
        var totalCurFreq = 0L
        var totalMaxFreq = 0L
        try {
            for (core in 0 until 7) {

                val curFreqPath = "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq"
                val maxFreqPath = "/sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_max_freq"


                val curFreqProcess = Runtime.getRuntime().exec("cat $curFreqPath")
                val curFreq =
                    curFreqProcess.inputStream.bufferedReader().readText().trim().toLongOrNull()
                        ?: 0L

                val maxFreqProcess = Runtime.getRuntime().exec("cat $maxFreqPath")
                val maxFreq =
                    maxFreqProcess.inputStream.bufferedReader().readText().trim().toLongOrNull()
                        ?: 0L

                totalCurFreq += curFreq
                totalMaxFreq += maxFreq
            }
            if (totalMaxFreq > 0) {
                return ((totalCurFreq.toFloat() / totalMaxFreq) * 100).toInt()
            } else {
                return 0
            }

        } catch (e: IOException) {
            Log.e("CPU Info", "Error reading CPU frequency", e)
            return 0
        }

    }

    @SuppressLint("SetTextI18n")
    private fun displayCpuGovernor() {
        try {
            val process = Runtime.getRuntime()
                .exec("cat /sys/devices/system/cpu/cpu7/cpufreq/scaling_governor")
            val result = process.inputStream.bufferedReader().readText().trim()
            cpuGovText.text = "Governor : $result"
        } catch (e: Exception) {
            cpuGovText.text = "Governor : N/A"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayCpuMaxClockSpeed() {
        try {
            var maxClockSpeed = 0L
            for (core in 0 until Runtime.getRuntime().availableProcessors()) {
                val process = Runtime.getRuntime()
                    .exec("cat /sys/devices/system/cpu/cpu$core/cpufreq/scaling_max_freq")
                val result =
                    process.inputStream.bufferedReader().readText().trim().toLongOrNull() ?: 0L
                if (result > maxClockSpeed) {
                    maxClockSpeed = result
                }
            }
            val maxClockSpeedGHz = maxClockSpeed / 1_000_000.0
            cpuMaxText.text = "Max CPU: %.2f GHz".format(maxClockSpeedGHz)
        } catch (e: Exception) {
            cpuMaxText.text = "Max CPU: N/A"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayKernelName() {
        try {
            val process = Runtime.getRuntime().exec("uname -r")
            val result = process.inputStream.bufferedReader().readText().trim()
            kernelNameText.text = "Kernel :\n$result"
        } catch (e: Exception) {
            kernelNameText.text = "Kernel : N/A"
        }
    }

    @SuppressLint("SetTextI18n")


    private fun getTemperature(path: String): String {
        return try {
            val process = Runtime.getRuntime().exec("cat $path")
            val result =
                process.inputStream.bufferedReader().readText().trim().toFloatOrNull() ?: 0f
            val temperatureCelsius = result / 1000.0f
            "%.1f°C".format(temperatureCelsius)
        } catch (e: Exception) {
            "N/A"
        }
    }

    private fun executeCommandAsRoot(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val result = process.inputStream.bufferedReader().readText().trim()
            result
        } catch (e: Exception) {
            "N/A"
        }
    }

    private fun getGpuFrequency(): String {
        val paths = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpuclk",
            "/sys/class/drm/card0/device/gpuclk"
        )
        for (path in paths) {
            try {
                val result = executeCommandAsRoot("cat $path").toLongOrNull() ?: 0L
                if (result > 0) {
                    val frequencyGHz = result / 1000000.0
                    return "%.0f MHz".format(frequencyGHz)
                }
            } catch (e: Exception) {
                continue
            }
        }
        return "N/A"
    }

    private fun getGpuMaxFrequency(): String {
        val paths = listOf(
            "/sys/class/kgsl/kgsl-3d0/max_gpuclk",
            "/sys/class/drm/card0/device/max_gpuclk"
        )
        for (path in paths) {
            try {
                val result = executeCommandAsRoot("cat $path").toLongOrNull() ?: 0L
                if (result > 0) {
                    val frequencyGHz = result / 1000000.0
                    return "%.0f MHz".format(frequencyGHz)
                }
            } catch (e: Exception) {
                continue
            }
        }
        return "N/A"
    }

    @SuppressLint("SetTextI18n")
    private fun displayGpuUsage() {
        val gpuUsage = getGPUUsage()
        progressBarGPUUsage.progress = gpuUsage
        tvGPUUsageLabel.text = "GPU Usage: $gpuUsage%"
    }

    private fun getGPUUsage(): Int {
        val paths = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpubusy",
            "/sys/class/drm/card0/device/gpubusy"
        )
        return try {
            var output = 0L
            var maxOutput = 0L
            for (path in paths) {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat $path"))
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val line = reader.readLine()?.trim() ?: continue
                if (!line.contains(" ")) {
                    output = line.toLongOrNull() ?: 0L
                } else {
                    val parts = line.split(" ")
                    output = parts[0].toLongOrNull() ?: 0L
                    maxOutput = parts[1].toLongOrNull() ?: 0L
                }
            }
            if (maxOutput > 0) {
                ((output.toFloat() / maxOutput) * 100).toInt()
            } else {
                0
            }
        } catch (e: IOException) {
            Log.e("GPU Info", "Error reading GPU usage", e)
            0
        }
    }


    @SuppressLint("SetTextI18n")
    private fun displayGpuFrequencies() {
        gpuFrequencyText.text = "GPU Freq: ${getGpuFrequency()}"
        gpuMaxFrequencyText.text = "GPU Max Freq: ${getGpuMaxFrequency()}"
    }


    @SuppressLint("SetTextI18n")
    private fun displayTemperatures() {
        cpuTemperatureText.text =
            "Temp CPU: ${getTemperature("/sys/class/thermal/thermal_zone0/temp")}"
        socTemperatureText.text =
            "Temp SOC: ${getTemperature("/sys/class/thermal/thermal_zone1/temp")}"
    }


    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("SetTextI18n")
    private fun displaySystemInfo() {

        val androidVersionName = getAndroidVersionName()
        androidVersionText.text = "Android $androidVersionName"


    }

    private val socModelMap = mapOf(
        "SM8250" to "Qualcomm Snapdragon 865",
        "SM8250_AC" to "Qualcomm Snapdragon 865+",
        "SM8250_HP" to "Qualcomm Snapdragon 870",
        "AArch64 Processor rev 13 (aarch64)" to "Qualcomm Snapdragon 845",
        "SM8450" to "Qualcomm Snapdragon 8 Gen 1",
        "SDM845" to "Qualcomm Snapdragon 845",
        "SM8350" to "Qualcomm Snapdragon 888",
        "SM8350_AC" to "Qualcomm Snapdragon 888+",
        "SM6375" to "Qualcomm Snapdragon 695",
        "SM7250" to "Qualcomm Snapdragon 765G",
        "SM7225" to "Qualcomm Snapdragon 750G",
        "SDM7650" to "Qualcomm Snapdragon 765",
        "SM7150" to "Qualcomm Snapdragon 732G",
        "SM6350" to "Qualcomm Snapdragon 690",
        "SM7125" to "Qualcomm Snapdragon 720G",
        "SDM730" to "Qualcomm Snapdragon 730G",
        "MSM8998" to "Qualcomm Snapdragon 835",
        "SDM730" to "Qualcomm Snapdragon 730",
        "SM6350" to "Qualcomm Snapdragon 480",
        "SDM7125" to "Qualcomm Snapdragon 712",
        "SM6225" to "Qualcomm Snapdragon 680",
        "SDM670" to "Qualcomm Snapdragon 670",
        "SM6115" to "Qualcomm Snapdragon 678",
        "SDM710" to "Qualcomm Snapdragon 710",
        "SDM670" to "Qualcomm Snapdragon 670",
        "SDM660" to "Qualcomm Snapdragon 660",
        "SDM636" to "Qualcomm Snapdragon 636",
        "SDM632" to "Qualcomm Snapdragon 632",
        "SM8450" to "Qualcomm Snapdragon 8 Gen 1",
        "SM8475" to "Qualcomm Snapdragon 8+ Gen 1",
        "SM8550" to "Qualcomm Snapdragon 8 Gen 2",
        "SM8550_AB" to "Qualcomm Snapdragon 8 Gen 2",
        "SM8575" to "Qualcomm Snapdragon 8+ Gen 2",
        "SM7475" to "Qualcomm Snapdragon 7+ Gen 2",
        "SM6375" to "Qualcomm Snapdragon 695",
        "SDM765G" to "Qualcomm Snapdragon 765G",
        "SM7225" to "Qualcomm Snapdragon 750G",
        "SDM765" to "Qualcomm Snapdragon 765",
        "SM7150" to "Qualcomm Snapdragon 732G",
        "SM6350" to "Qualcomm Snapdragon 690",
        "SDM730" to "Qualcomm Snapdragon 730G",
        "SDM8350" to "Qualcomm Snapdragon 835",
        "SM6150" to "Qualcomm Snapdragon 675",
        "SM8250_AB" to "Qualcomm Snapdragon 865",
        "SM8250_AC" to "Qualcomm Snapdragon 865+",
        "SM8250_HP" to "Qualcomm Snapdragon 870",
        "SDM730G" to "Qualcomm Snapdragon 730G",
        "SM4350" to "Qualcomm Snapdragon 4 Gen 2",
        "SDM710" to "Qualcomm Snapdragon 710",
        "SM7450" to "Qualcomm Snapdragon 7 Gen 2",
        "SM6375" to "Qualcomm Snapdragon 695",

        // Mediatek
        "MT6753" to "MediaTek Helio P25",
        "MT6761" to "MediaTek Helio A22",
        "MT6762" to "MediaTek Helio P22",
        "MT6765" to "MediaTek Helio P35",
        "MT6768" to "MediaTek Helio P65",
        "MT6771" to "MediaTek Helio P60",
        "MT6779" to "MediaTek Helio P90",
        "MT6785" to "MediaTek Helio G90",
        "MT6889" to "MediaTek Dimensity 1000",
        "MT6873" to "MediaTek Dimensity 800",
        "MT6885" to "MediaTek Dimensity 1000+",
        "MT6833" to "MediaTek Dimensity 720/6020",
        "MT6779V" to "MediaTek Helio G95",
        "MT6877" to "MediaTek Dimensity 900",
        "MT6853" to "MediaTek Dimensity 800U",
        "MT6875" to "MediaTek Dimensity 820",
        "MT6877V" to "MediaTek Dimensity 1100",
        "MT6883" to "MediaTek Dimensity 1000C",
        "MT6879" to "MediaTek Dimensity 920",
        "MT6893" to "MediaTek Dimensity 1200",
        "MT6781" to "MediaTek Helio G88",
        "MT6779T" to "MediaTek Helio G95",
        "MT6833V" to "MediaTek Dimensity 700 5G",
        "MT6880" to "MediaTek Dimensity 1000L",
        "MT6875V" to "MediaTek Dimensity 820",
        "MT6833T" to "MediaTek Dimensity 700",
        "MT6873V" to "MediaTek Dimensity 800T",
        "MT6771T" to "MediaTek Helio P60T",
        "MT6768V" to "MediaTek Helio P65V",
        "MT6769" to "MediaTek Helio G70",
        "MT6873T" to "MediaTek Dimensity 800T",
        "MT6895" to "MediaTek Dimensity 1300",
        "MT6897" to "MediaTek Dimensity 1400",
        "MT6795" to "MediaTek Helio X10",
        "MT6797" to "MediaTek Helio X20",
        "MT6799" to "MediaTek Helio X30",
        "MT6763" to "MediaTek Helio P23",
        "MT6769T" to "MediaTek Helio G70T",
        "MT6873V/T" to "MediaTek Dimensity 800U",
        "MT6877/T" to "MediaTek Dimensity 1100U",
        "MT6781V" to "MediaTek Helio G88V",
        "MT6768T" to "MediaTek Helio P65T",
        "MT6761V" to "MediaTek Helio A22V",
        "MT6889T" to "MediaTek Dimensity 1000T",
        "MT6762V" to "MediaTek Helio P22V",
        "MT6765V" to "MediaTek Helio P35V",
        "MT6769V" to "MediaTek Helio G70V",
        "MT6885V" to "MediaTek Dimensity 1000+V",
        "MT6885T" to "MediaTek Dimensity 1000+T",
        "MT6761T" to "MediaTek Helio A22T",

        // Samsung Exynos
        "Exynos 8895" to "Samsung Exynos 8895",
        "Exynos 9810" to "Samsung Exynos 9810",
        "Exynos 9820" to "Samsung Exynos 9820",
        "Exynos 9825" to "Samsung Exynos 9825",
        "Exynos 990" to "Samsung Exynos 990",
        "Exynos 1080" to "Samsung Exynos 1080",
        "Exynos 2100" to "Samsung Exynos 2100",
        "Exynos 2200" to "Samsung Exynos 2200",
        "Exynos 7885" to "Samsung Exynos 7885",
        "Exynos 7904" to "Samsung Exynos 7904",
        "Exynos 9610" to "Samsung Exynos 9610",
        "Exynos 9611" to "Samsung Exynos 9611",
        "Exynos 880" to "Samsung Exynos 880",
        "Exynos 850" to "Samsung Exynos 850",
        "Exynos 980" to "Samsung Exynos 980",
        "Exynos 990" to "Samsung Exynos 990",
        "Exynos 1080" to "Samsung Exynos 1080",
        "Exynos 2100" to "Samsung Exynos 2100",
        "Exynos 2200" to "Samsung Exynos 2200",
        "Exynos 850" to "Samsung Exynos 850",
        "Exynos 9610" to "Samsung Exynos 9610",
        "Exynos 9820" to "Samsung Exynos 9820",
        "Exynos 7885" to "Samsung Exynos 7885",
        "Exynos 9810" to "Samsung Exynos 9810",
        "Exynos 9825" to "Samsung Exynos 9825",
        "Exynos 990" to "Samsung Exynos 990",
        "Exynos 1080" to "Samsung Exynos 1080",
        "Exynos 2100" to "Samsung Exynos 2100",
        "Exynos 2200" to "Samsung Exynos 2200",
        "Exynos 850" to "Samsung Exynos 850",
        "Exynos 9610" to "Samsung Exynos 9610",
        "Exynos 9820" to "Samsung Exynos 9820",
        "Exynos 7885" to "Samsung Exynos 7885",
        "Exynos 9810" to "Samsung Exynos 9810",
        "Exynos 9825" to "Samsung Exynos 9825",
        "Exynos 990" to "Samsung Exynos 990",
        "Exynos 9611" to "Samsung Exynos 9611",
        "Exynos 9820" to "Samsung Exynos 9820",
        "Exynos 7885" to "Samsung Exynos 7885",
        "Exynos 9610" to "Samsung Exynos 9610",
        "Exynos 9810" to "Samsung Exynos 9810",
        "Exynos 9825" to "Samsung Exynos 9825",
        "Exynos 850" to "Samsung Exynos 850",
        "Exynos 9820" to "Samsung Exynos 9820",
        "Exynos 7885" to "Samsung Exynos 7885",
        "Exynos 9610" to "Samsung Exynos 9610",
        "Exynos 9810" to "Samsung Exynos 9810",
        // Google Tensor
        "Tensor 84" to "Google Tensor 84",
        "Tensor G3" to "Google Tensor G3",

        )


    private fun getFormattedSocModel(): String {
        val socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSocModel()
        } else {
            TODO("VERSION.SDK_INT < S")
        }
        return socModelMap[socModel] ?: socModel
    }


    private fun displaySocModel() {
        socModelTextView.text = getFormattedSocModel()
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun getSocModel(): String {
        var socModel = Build.SOC_MODEL
        try {
            val file = File("/proc/cpuinfo")
            val bufferedReader = BufferedReader(FileReader(file))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                if (line!!.contains("Hardware") || line!!.contains("model name") || line!!.contains(
                        "Processor"
                    )
                ) {
                    socModel = line!!.split(":")[1].trim()
                    break
                }
            }
            bufferedReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return socModel
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun displayUptime() {
        val uptimeMillis = SystemClock.elapsedRealtime()
        val upDays = TimeUnit.MILLISECONDS.toDays(uptimeMillis)
        val upHours = TimeUnit.MILLISECONDS.toHours(uptimeMillis) % 24
        val upMinutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60
        val upSeconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMillis) % 60
        val uptimeFormatted =
            String.format("%dd %dh %dm %ds", upDays, upHours, upMinutes, upSeconds)
        uptimeText.text = "Uptime: $uptimeFormatted"

    }

    private fun calculateUptimePercentage(activeTime: Long, totalTime: Long): Int {
        return if (totalTime > 0) {
            ((activeTime.toDouble() / totalTime.toDouble()) * 100).toInt()
        } else {
            0
        }
    }

    private fun getAndroidVersionName(): String {
        return when (Build.VERSION.SDK_INT) {
            28 -> "9 (Pie)"
            29 -> "10 (Quince Tart)"
            30 -> "11 (Red Velvet)"
            31 -> "12 (SnowCone)"
            32 -> "12L (SnowCone Large)"
            33 -> "13 (Tiramisu)"
            34 -> "14 (UpsideDownCake)"
            35 -> "15 (VanillaIceCream)"
            else -> "Unknown"
        }
    }

    private fun setAndroidLogo() {
        val androidVersion = Build.VERSION.SDK_INT
        val logoResId = when (androidVersion) {
            28 -> R.drawable.android_pie_logo           // Android 9
            29 -> R.drawable.android_q_logo             // Android 10
            30 -> R.drawable.android_r_logo             // Android 11
            in 31..32 -> R.drawable.android_s_logo      // Android 12
            33 -> R.drawable.android_t_logo             // Android 13
            34 -> R.drawable.android_u_logo             // Android 14
            35 -> R.drawable.android_v_logo             // Android 15
            else -> R.drawable.ic_android_logo          // Default logo
        }
        androidLogo.setImageResource(logoResId)
    }

    private fun getDeviceName(): String {
        val manufacture = Build.MANUFACTURER
        val model = Build.MODEL
        val marketname = getSystemProperty("ro.product.vendor.marketname", "")
        return when {
            marketname.isNotBlank() -> "$manufacture $marketname"
            model.isNotBlank() -> "$manufacture $model"
            else -> "$manufacture"
        }
    }


    private fun displayDeviceName() {
        val deviceName = getDeviceName()
        deviceNameText.text = deviceName
    }


    private fun getDurationBreakdown(millis: Long): String {
        @Suppress("NAME_SHADOWING") var millis = millis
        val sb = StringBuilder(64)
        if (millis <= 0) {
            sb.append("0 min 0 s")
            return sb.toString()
        }

        val days = TimeUnit.MILLISECONDS.toDays(millis)
        millis -= TimeUnit.DAYS.toMillis(days)
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)

        if (days > 0) {
            sb.append(days)
            sb.append("d ")
        }
        if (hours > 0) {
            sb.append(hours)
            sb.append("h ")
        }
        if (minutes > 0) {
            sb.append(String.format("%02d", minutes))
            sb.append("min ")
        }
        if (seconds > 0) {
            sb.append(String.format("%02d", seconds))
            sb.append("s")
        }
        return sb.toString()
    }

    private fun calculatePercentage(deepSleepTime: Long, totalTime: Long): Int {
        return if (totalTime > 0) {
            ((deepSleepTime.toDouble() / totalTime.toDouble()) * 100).toInt()
        } else {
            0
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayDeepSleepTime() {
        val deepSleepTime = SystemClock.elapsedRealtime() - SystemClock.uptimeMillis()
        val formattedTime = getDurationBreakdown(deepSleepTime)
        val uptimeMillis = SystemClock.elapsedRealtime()
        val percentage = calculatePercentage(deepSleepTime, uptimeMillis)
        deepSleepTimeText.text = "DeepSleep: $formattedTime ($percentage%)"
    }

    @SuppressLint("SetTextI18n")
    private fun displayGPUInfo() {
        val gpuName = getGPUModel()
        gpuinfo.text = gpuName
        Log.d("GPU Info", gpuName)
    }

    private fun getGPUModel(): String {
        return try {
            val GPU_MODEL_PATH = "/sys/class/kgsl/kgsl-3d0/gpu_model"
            val file = File(GPU_MODEL_PATH)

            if (!file.exists()) {
                Log.e("getGPUModel", "File not found: $GPU_MODEL_PATH")
                return "Unknown"
            }

            val result = file.readText().trim()
            if (result.isNotEmpty()) formatGPUModel(result) else "Unknown"
        } catch (e: Exception) {
            Log.e("getGPUModel", "Error reading GPU model", e)
            "Unknown"
        }
    }

    private fun checkRootAndShowPopup() {
        if (!requestRootAccess()) {
            showRootRequiredDialog(this)
        }
    }

    private fun requestRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            process.outputStream.write("exit\n".toByteArray())
            process.outputStream.flush()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun showRootRequiredDialog(context: Context) {
        try {
            MaterialAlertDialogBuilder(context)
                .setTitle("[Root Access Required]")
                .setMessage("Xtra Kernel Manager requires root access to function properly.\nPlease grant root permission in \n - Magisk\n - KernelSU/KernelSU-Next \n - APatch\n\nOtherwise, Xtra Kernel Manager Cannot Be Used.")
                .setCancelable(false)
                .setPositiveButton("OK,I Will Grant Root") { _, _ ->
                    val intent = Intent(context, SysDestroyedEasterEgg::class.java)
                    context.startActivity(intent)
                }
                .show()
        } catch (e: Exception) {
            Log.e("DialogError", "Error showing dialog", e)
        }
    }


    private fun formatGPUModel(result: String): String {
        return when {
            result.contains("Adreno", ignoreCase = true) -> {
                val adrenoModel =
                    Regex("Adreno\\s*(\\d+)").find(result)?.groupValues?.get(1) ?: "Unknown"
                "Adreno (TM) $adrenoModel"
            }

            result.contains("Mali", ignoreCase = true) -> {
                val maliModel =
                    Regex("Mali-([A-Z]?\\d+(?: MP\\d+)?)").find(result)?.groupValues?.get(1)
                        ?: "Unknown"
                "Mali-$maliModel"
            }

            result.contains("PowerVR", ignoreCase = true) -> {
                val powerVRModel =
                    Regex("PowerVR\\s*(\\w+)").find(result)?.groupValues?.get(1) ?: "Unknown"
                "PowerVR $powerVRModel"
            }

            else -> "Unknown"
        }
    }


    @SuppressLint("SetTextI18n")
    private fun displayGraphicsDriverInfo() {
        val graphicsDriverInfo = if (Build.VERSION.SDK_INT >= 34 /* Android 14 */) {
            getREGLESVersion()
        } else {
            getOpenGLVersion(this)
        }

        infoOpenGL.text = "Driver OpenGL ES $graphicsDriverInfo"
    }

    private fun getOpenGLVersion(context: Context): String {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo

        return configurationInfo.glEsVersion ?: "Unknown"
    }

    private fun getREGLESVersion(): String {
        val vulkanInfo = StringBuilder()
        try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val configInfo = activityManager.deviceConfigurationInfo
            vulkanInfo.append("\nRenderScriptGLES : ${configInfo.glEsVersion}\n")
        } catch (e: Exception) {
            Log.e("REGLES Info", "Error retrieving REGLES info", e)
            return "Unknown REGLES Version"
        }

        return vulkanInfo.toString()
    }

    private val batteryInfoReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = (level / scale.toFloat()) * 100

            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10.0
            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) / 1000.0 // Convert mV to V
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

            val batteryCurrent = getBatteryCurrentWithRoot()
            val chargingStatus = getChargingStatus(batteryCurrent, isCharging)

            val batteryPower = calculateBatteryPower(batteryCurrent, voltage)

            tvBatteryPercentage.text = "${batteryPct.toInt()}%"
            tvBatteryTemperature.text = "  $temperature°C"
            tvChargingStatus.text = chargingStatus
            tvBatteryCurrent.text = "${batteryCurrent}mA"
            tvBatteryPower.text = "${batteryPower}Watt"
            tvBatteryVoltage.text = "Voltage: $voltage V"
            progressBarBattery.progress = batteryPct.toInt()
        }
    }

    private fun getBatteryCurrentWithRoot(): Int {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat /sys/class/power_supply/battery/current_now"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val current = reader.readLine().trim().toIntOrNull() ?: 0
            reader.close()
            Log.d("Battery Current", "Read current_now with root: $current")
            abs(current / 1000) // Convert to mA and take absolute value
        } catch (e: IOException) {
            Log.e("Battery Current", "Error reading current_now file with root", e)
            0
        }
    }

    private fun calculateBatteryPower(current: Int, voltage: Double): String {
        // Convert current to Amperes and calculate power in Watts
        val currentInAmps = current / 1000.0
        val power = (currentInAmps * voltage).toFloat()
        Log.d("Battery Power", "Calculated power: $power W")

        // Format the power to 1 or 2 decimal places
        val decimalFormat = DecimalFormat("#.#")
        return decimalFormat.format(power)
    }

    private fun getChargingStatus(current: Int, isCharging: Boolean): String {
        return when {
            !isCharging -> "Discharge"
            current < 1000 -> "Low Charge"
            current in 1000..3000 -> "Rapid Charge"
            current in 3001..4000 -> "Quick Charge"
            current > 4000 -> "Turbo Charge"
            else -> "Charge"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateRamInfo() {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalRam = memoryInfo.totalMem / (1024 * 1024) // Convert to MB
        val freeRam = memoryInfo.availMem / (1024 * 1024) // Convert to MB
        val usedRam = totalRam - freeRam

        tvTotalRam.text = "Total RAM: ${totalRam}MiB"
        tvFreeRam.text = "Free RAM: ${freeRam}MiB"
        tvUsedRam.text = "Used RAM: ${usedRam}MiB"

        Log.d("RAM Info", "Total RAM: $totalRam MB, Free RAM: $freeRam MB, Used RAM: $usedRam MB")
    }


        override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}
