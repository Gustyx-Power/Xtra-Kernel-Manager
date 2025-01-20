package id.gustyx.xk.manager

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
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

        displayKernelName()
        displayRootMethod()

        runnable = Runnable {
            displayCpuInfo()
            displayCpuMaxClockSpeed()
            handler.postDelayed(runnable, 1000)
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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}
