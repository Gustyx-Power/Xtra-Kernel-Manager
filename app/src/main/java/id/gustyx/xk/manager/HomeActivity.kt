package id.gustyx.xk.manager

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class HomeActivity : AppCompatActivity() {
    private lateinit var cpuCoreTextViews: List<TextView>
    private lateinit var kernelNameText: TextView
    private lateinit var rootStatusText: TextView
    private lateinit var rootProviderText: TextView
    private lateinit var cpuGovText: TextView
    private lateinit var cpuMaxText: TextView
    private lateinit var btnOpenSettings: Button
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private lateinit var cpuTemperatureText: TextView
    private lateinit var socTemperatureText: TextView
    private lateinit var gpuFrequencyText: TextView
    private lateinit var gpuMaxFrequencyText: TextView
    private lateinit var rootProviderLogo: ImageView



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
        btnOpenSettings = findViewById(R.id.btnOpenSettings)
        cpuTemperatureText = findViewById(R.id.cpuTemperature)
        socTemperatureText = findViewById(R.id.socTemperature)
        gpuFrequencyText = findViewById(R.id.gpuFrequency)
        gpuMaxFrequencyText = findViewById(R.id.gpuMaxFrequency)
        rootProviderLogo = findViewById(R.id.rootProviderLogo)


        displayKernelName()
        displayRootMethod()

        runnable = Runnable {
            displayCpuInfo()
            displayCpuMaxClockSpeed()
            displayTemperatures()
            displayGpuFrequencies()
            handler.postDelayed(runnable, 100)
        }
        handler.post(runnable)

        btnOpenSettings.setOnClickListener {
            val intent = Intent(this, ClockspeedActivity::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayCpuInfo() {
        for (core in cpuCoreTextViews.indices) {
            try {
                val process = Runtime.getRuntime().exec("cat /sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq")
                val result = process.inputStream.bufferedReader().readText().trim().toLongOrNull() ?: 0L
                val resultMHz = result / 1000
                cpuCoreTextViews[core].text = "Core $core: $resultMHz MHz"
            } catch (e: Exception) {
                cpuCoreTextViews[core].text = "Core $core: N/A"
            }
        }
        displayCpuGovernor()
    }

    @SuppressLint("SetTextI18n")
    private fun displayCpuGovernor() {
        try {
            val process = Runtime.getRuntime().exec("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
            val result = process.inputStream.bufferedReader().readText().trim()
            cpuGovText.text = "CPU Gov: $result"
        } catch (e: Exception) {
            cpuGovText.text = "CPU Gov: N/A"
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
            kernelNameText.text = "Kernel Name: $result"
        } catch (e: Exception) {
            kernelNameText.text = "Kernel Name: N/A"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayRootMethod() {
        var rootStatus = "N/A"
        var rootProvider = "N/A"
        if (File("/sbin/.magisk").exists()) {
            rootStatus = "Granted Succesfully"
            rootProvider = getMagiskVersion()
        } else if (File("/system/xbin/su").exists() || File("/system/bin/su").exists()) {
            rootStatus = "Granted Succesfully"
            rootProvider = getKernelSUVersion()
        } else if (File("/sys/kernel/debug/kernelsu").exists() || File("/data/adb/modules/Kernelsu").exists() || File("/su/bin/kernelsu").exists()) {
            rootStatus = "Granted Succesfully"
            rootProvider = getKernelSUVersion()
        }
        rootStatusText.text = "Root Access: $rootStatus"
        rootProviderText.text = "Superuser By: $rootProvider"
    }

    private fun getMagiskVersion(): String {
        return try {
            val process = Runtime.getRuntime().exec("magisk -v")
            process.inputStream.bufferedReader().readText().trim().ifEmpty { "Magisk" }
        } catch (e: Exception) {
            "Magisk"
        }
    }

    private fun getKernelSUVersion(): String {
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
            process?.inputStream?.bufferedReader()?.readText()?.trim()?.ifEmpty { "KernelSU" } ?: "KernelSU"
        } catch (e: Exception) {
            "KernelSU/KernelSU-Next"
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


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}
