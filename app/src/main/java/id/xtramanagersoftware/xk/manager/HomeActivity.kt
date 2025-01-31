package id.xtramanagersoftware.xk.manager

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.RandomAccessFile
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit

class HomeActivity : AppCompatActivity() {
    private lateinit var cpuCoreTextViews: List<TextView>
    private lateinit var kernelNameText: TextView
    private lateinit var rootStatusText: TextView
    private lateinit var rootProviderText: TextView
    private lateinit var cpuGovText: TextView
    private lateinit var cpuMaxText: TextView
    private lateinit var buttoncpu: ImageButton
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private lateinit var cpuTemperatureText: TextView
    private lateinit var socTemperatureText: TextView
    private lateinit var gpuFrequencyText: TextView
    private lateinit var gpuMaxFrequencyText: TextView
    private lateinit var rootProviderLogo: ImageView
    private lateinit var androidLogo: ImageView
    private lateinit var deviceNameText: TextView
    private lateinit var androidVersionText: TextView
    private lateinit var socModelTextView: TextView
    private lateinit var uptimeText: TextView
    private lateinit var deepSleepTimeText: TextView




    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

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
        rootStatusText = findViewById(R.id.rootStatus)
        rootProviderText = findViewById(R.id.rootProvider)
        cpuGovText = findViewById(R.id.cpuGov)
        cpuMaxText = findViewById(R.id.cpuMax)
        buttoncpu = findViewById(R.id.btnCPU)
        cpuTemperatureText = findViewById(R.id.cpuTemperature)
        socTemperatureText = findViewById(R.id.socTemperature)
        gpuFrequencyText = findViewById(R.id.gpuFrequency)
        gpuMaxFrequencyText = findViewById(R.id.gpuMaxFrequency)
        rootProviderLogo = findViewById(R.id.rootProviderLogo)
        androidLogo = findViewById(R.id.androidLogo)
        deviceNameText = findViewById(R.id.deviceName)
        androidVersionText = findViewById(R.id.androidVersion)
        socModelTextView = findViewById(R.id.socModel)
        uptimeText = findViewById(R.id.uptime)
        deepSleepTimeText = findViewById(R.id.deepSleepTime)



        displayKernelName()
        displayRootMethod()
        setAndroidLogo()
        displaySystemInfo()
        displayDeviceName()
        displayDeepSleepTime()
        displayUptime()
        displaySocModel()


        runnable = Runnable {
            displayCpuInfo()
            displayCpuMaxClockSpeed()
            displayTemperatures()
            displayGpuFrequencies()
            displayUptime()
            displayDeepSleepTime()
            handler.postDelayed(runnable, 1000)
        }
        handler.post(runnable)

        buttoncpu.setOnClickListener {
            val intent = Intent(this, CpuActivity::class.java)
            startActivity(intent)
        }
    }


    @SuppressLint("PrivateApi")
    fun getSystemProperty(key: String, defaultValue: String): String {
        return try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val getMethod: Method = systemProperties.getMethod("get", String::class.java, String::class.java)
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
                val process = Runtime.getRuntime().exec("cat /sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq")
                val result = process.inputStream.bufferedReader().readText().trim().toLongOrNull() ?: 0L
                val resultMHz = result / 1000
                cpuCoreTextViews[core].text = "$resultMHz MHz"
            } catch (e: Exception) {
                cpuCoreTextViews[core].text = "$core: N/A"
            }
        }
        displayCpuGovernor()
    }

    @SuppressLint("SetTextI18n")
    private fun displayCpuGovernor() {
        try {
            val process = Runtime.getRuntime().exec("cat /sys/devices/system/cpu/cpu7/cpufreq/scaling_governor")
            val result = process.inputStream.bufferedReader().readText().trim()
            cpuGovText.text = "CPU Governor : $result"
        } catch (e: Exception) {
            cpuGovText.text = "CPU Governor : N/A"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayCpuMaxClockSpeed() {
        try {
            var maxClockSpeed = 0L
            for (core in 0 until Runtime.getRuntime().availableProcessors()) {
                val process = Runtime.getRuntime().exec("cat /sys/devices/system/cpu/cpu$core/cpufreq/scaling_max_freq")
                val result = process.inputStream.bufferedReader().readText().trim().toLongOrNull() ?: 0L
                if (result > maxClockSpeed) {
                    maxClockSpeed = result
                }
            }
            val maxClockSpeedGHz = maxClockSpeed / 1_000_000.0
            cpuMaxText.text = "CPU Max: %.2f GHz".format(maxClockSpeedGHz)
        } catch (e: Exception) {
            cpuMaxText.text = "CPU Max: N/A"
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
    private fun displayRootMethod() {
        var rootStatus = "N/A"
        var rootProvider = "N/A"
       if (File("/system/xbin/su").exists() || File("/system/bin/su").exists()) {
            rootStatus = "Granted"
            rootProvider = getSU()
            setRootProviderLogo("KernelSU")

        } else {
            setRootProviderLogo("")
        }
        rootStatusText.text = "Access : $rootStatus"
        rootProviderText.text = "Mounted : $rootProvider"
    }


    private fun getSU(): String {
        val versionFile = File("/sys/kernel/debug/kernelsu/version")
        val managerVersionFile = File("/system/xbin/su")
        return try {
            val process = if (versionFile.exists()) {
                Runtime.getRuntime().exec("cat /sys/kernel/debug/kernelsu/version")
            } else if (managerVersionFile.exists()) {
                Runtime.getRuntime().exec("/system/xbin/su --version")
            } else {
                null
            }
            process?.inputStream?.bufferedReader()?.readText()?.trim()?.ifEmpty { "Rooted" } ?: "KernelSU,Magisk,APatch"
        } catch (e: Exception) {
            "Null"
        }
    }
    private fun getTemperature(path: String): String {
        return try {
            val process = Runtime.getRuntime().exec("cat $path")
            val result = process.inputStream.bufferedReader().readText().trim().toFloatOrNull() ?: 0f
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
                    val frequencyMHz = result / 1000
                    return "$frequencyMHz MHz"
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
                    val frequencyMHz = result / 1000
                    return "$frequencyMHz MHz"
                }
            } catch (e: Exception) {
                continue
            }
        }
        return "N/A"
    }


    @SuppressLint("SetTextI18n")
    private fun displayGpuFrequencies() {
        gpuFrequencyText.text = "GPU Freq: ${getGpuFrequency()}"
        gpuMaxFrequencyText.text = "GPU Max Freq: ${getGpuMaxFrequency()}"
    }




    @SuppressLint("SetTextI18n")
    private fun displayTemperatures() {
        cpuTemperatureText.text = "Temp CPU: ${getTemperature("/sys/class/thermal/thermal_zone0/temp")}"
        socTemperatureText.text = "Temp SOC: ${getTemperature("/sys/class/thermal/thermal_zone1/temp")}"
    }

    private fun setRootProviderLogo(provider: String) {
        val logoResId = when (provider) {
            "Magisk" -> R.drawable.magisk_logo
            "KernelSU" -> R.drawable.kernelsu_logo
            else -> 0
        }
        if (logoResId != 0) {
            rootProviderLogo.setImageResource(logoResId)
            rootProviderLogo.visibility = View.VISIBLE
        } else {
            rootProviderLogo.visibility = View.GONE
        }
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
            "SM7125" to "Qualcomm Snapdragon 720G",
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
                "MT6833" to "MediaTek Dimensity 720",
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
                "MT6833V" to "MediaTek Dimensity 720",
                "MT6880" to "MediaTek Dimensity 1000L",
                "MT6875V" to "MediaTek Dimensity 820",
                "MT6833T" to "MediaTek Dimensity 720T",
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
                    "Exynos 9810" to "Samsung Exynos 9810"

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
                if (line!!.contains("Hardware") || line!!.contains("model name") || line!!.contains("Processor")) {
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
        val uptimeFormatted = String.format("%d days, %d hours, %d minutes", upDays, upHours, upMinutes)
        uptimeText.text = "Uptime: $uptimeFormatted"

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
        return "$manufacture $marketname ($model)"
    }


    private fun displayDeviceName() {
        val deviceName = getDeviceName()
        deviceNameText.text = deviceName
    }


    private fun getDeepSleepTime(): Long {
        var idleTime: Long = 0
        try {
            val statFile = RandomAccessFile("/proc/stat", "r")
            var load = statFile.readLine()
            while (load != null) {
                if (load.startsWith("cpu ")) {
                    val toks = load.split(" +".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    idleTime = toks[4].toLong()
                    break
                }
                load = statFile.readLine()
            }
            statFile.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return idleTime
    }

    private fun calculatePercentage(deepSleepTime: Long, totalTime: Long): Int {
        return if (totalTime > 0) {
            ((deepSleepTime.toDouble() / totalTime.toDouble()) * 100).toInt()
        } else {
            0
        }
    }


    private fun formatTime(deepSleepMillis: Long, totalMillis: Long): String {
        val seconds = deepSleepMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        val remainingSeconds = seconds % 60
        val percentage = calculatePercentage(deepSleepMillis, totalMillis)

        return String.format("%dh, %dm, %ds (%d%%)", hours, remainingMinutes, remainingSeconds, percentage)
    }



    private fun getUptimeMillis(): Long {
        var uptime: Long = 0
        try {
            val uptimeFile = RandomAccessFile("/proc/uptime", "r")
            val uptimeLine = uptimeFile.readLine()
            val toks = uptimeLine.split(" +".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            uptime = (toks[0].toDouble() * 1000).toLong()
            uptimeFile.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return uptime
    }

    @SuppressLint("SetTextI18n")
    private fun displayDeepSleepTime() {
        val deepSleepTime = getDeepSleepTime()
        val uptimeMillis = getUptimeMillis()
        val formattedTime = formatTime(deepSleepTime * 10, uptimeMillis)
        deepSleepTimeText.text = "Deep Sleep : $formattedTime"
    }





    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}
