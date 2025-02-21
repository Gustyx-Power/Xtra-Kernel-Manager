package id.xtramanagersoftware.xk.manager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

@Suppress("DEPRECATION")
class CpuControl : AppCompatActivity() {

    private lateinit var spinnerMaxLittle: Spinner
    private lateinit var spinnerMinLittle: Spinner
    private lateinit var spinnerGovLittle: Spinner
    private lateinit var spinnerMaxBig: Spinner
    private lateinit var spinnerMinBig: Spinner
    private lateinit var spinnerGovBig: Spinner
    private lateinit var spinnerMaxPrime: Spinner
    private lateinit var spinnerMinPrime: Spinner
    private lateinit var spinnerGovPrime: Spinner
    private lateinit var applyButton: ImageButton
    private lateinit var thermalApplyButton: ImageButton
    private lateinit var thermalProfileSpinner: Spinner
    private lateinit var footer: LinearLayout
    private lateinit var labelMaxPrime: TextView
    private lateinit var labelMinPrime: TextView
    private lateinit var labelGovPrime: TextView
    private lateinit var btnGPU: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cpu_clock)

        // Set DPI jika dibutuhkan
        val config = resources.configuration
        config.densityDpi = 420
        resources.updateConfiguration(config, resources.displayMetrics)

        // Tombol navigasi
        findViewById<ImageButton>(R.id.btnHome).setOnClickListener { navigateTo(Homepage::class.java) }
        btnGPU = findViewById(R.id.btnGPU)
        btnGPU.setOnClickListener { navigateTo(GpuControlActivity::class.java) }

        setSelinuxMode("permissive")

        // Inisialisasi UI
        initUI()
        setupCpuSpinners()
        setupThermalProfileSpinner()
        adjustFooterHeight()

        applyButton.setOnClickListener {
            applyCpuSettings()
            showToast("Change CPU Clock & Governor\nSuccessfully")
        }
        thermalApplyButton.setOnClickListener {
            showToast("Change Thermal\nSuccessfully")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        setSelinuxMode("enforcing")
    }

    private fun initUI() {
        spinnerMaxLittle = findViewById(R.id.spinnerMaxLittle)
        spinnerMinLittle = findViewById(R.id.spinnerMinLittle)
        spinnerGovLittle = findViewById(R.id.spinnerGovLittle)
        spinnerMaxBig = findViewById(R.id.spinnerMaxBig)
        spinnerMinBig = findViewById(R.id.spinnerMinBig)
        spinnerGovBig = findViewById(R.id.spinnerGovBig)
        spinnerMaxPrime = findViewById(R.id.spinnerMaxPrime)
        spinnerMinPrime = findViewById(R.id.spinnerMinPrime)
        spinnerGovPrime = findViewById(R.id.spinnerGovPrime)
        applyButton = findViewById(R.id.applybutton)
        footer = findViewById(R.id.footer)
        thermalApplyButton = findViewById(R.id.thermalApplyButton)
        thermalProfileSpinner = findViewById(R.id.thermalProfileSpinner)
    }

    private fun navigateTo(activity: Class<*>) {
        val intent = Intent(this, activity)
        startActivity(intent)
    }

    private fun adjustFooterHeight() {
        ViewCompat.setOnApplyWindowInsetsListener(footer) { _, insets ->
            val navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val footerHeight = if (navigationBarHeight > 0)
                R.dimen.footer_height_navigation_3_buttons
            else
                R.dimen.footer_height_navigation_gesture

            footer.layoutParams.height = resources.getDimensionPixelSize(footerHeight)
            insets
        }
    }

    private fun applyCpuSettings() {
        showToast("Applying CPU settings...")

        CoroutineScope(Dispatchers.IO).launch {
            executeShellCommand("echo ${spinnerMaxLittle.selectedItem} > /sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq")
            executeShellCommand("echo ${spinnerMinLittle.selectedItem} > /sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq")
            executeShellCommand("echo ${spinnerGovLittle.selectedItem} > /sys/devices/system/cpu/cpufreq/policy0/scaling_governor")

            executeShellCommand("echo ${spinnerMaxBig.selectedItem} > /sys/devices/system/cpu/cpufreq/policy4/scaling_max_freq")
            executeShellCommand("echo ${spinnerMinBig.selectedItem} > /sys/devices/system/cpu/cpufreq/policy4/scaling_min_freq")
            executeShellCommand("echo ${spinnerGovBig.selectedItem} > /sys/devices/system/cpu/cpufreq/policy4/scaling_governor")

            if (File("/sys/devices/system/cpu/cpufreq/policy7/").exists()) {
                executeShellCommand("echo ${spinnerMaxPrime.selectedItem} > /sys/devices/system/cpu/cpufreq/policy7/scaling_max_freq")
                executeShellCommand("echo ${spinnerMinPrime.selectedItem} > /sys/devices/system/cpu/cpufreq/policy7/scaling_min_freq")
                executeShellCommand("echo ${spinnerGovPrime.selectedItem} > /sys/devices/system/cpu/cpufreq/policy7/scaling_governor")
            }
        }
    }


    private fun setupCpuSpinners() {
        val setupSpinner: suspend (Spinner, String) -> Unit = { spinner, path ->
            val options = getAvailableOptions(path)
            withContext(Dispatchers.Main) {
                spinner.adapter = ArrayAdapter(this@CpuControl, android.R.layout.simple_spinner_item, options)
                    .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            setupSpinner(spinnerMaxLittle, "/sys/devices/system/cpu/cpufreq/policy0/scaling_available_frequencies")
            setupSpinner(spinnerMinLittle, "/sys/devices/system/cpu/cpufreq/policy0/scaling_available_frequencies")
            setupSpinner(spinnerGovLittle, "/sys/devices/system/cpu/cpufreq/policy0/scaling_available_governors")
            setupSpinner(spinnerMaxBig, "/sys/devices/system/cpu/cpufreq/policy4/scaling_available_frequencies")
            setupSpinner(spinnerMinBig, "/sys/devices/system/cpu/cpufreq/policy4/scaling_available_frequencies")
            setupSpinner(spinnerGovBig, "/sys/devices/system/cpu/cpufreq/policy4/scaling_available_governors")

            if (File("/sys/devices/system/cpu/cpufreq/policy7/").exists()) {
                setupSpinner(spinnerMaxPrime, "/sys/devices/system/cpu/cpufreq/policy7/scaling_available_frequencies")
                setupSpinner(spinnerMinPrime, "/sys/devices/system/cpu/cpufreq/policy7/scaling_available_frequencies")
                setupSpinner(spinnerGovPrime, "/sys/devices/system/cpu/cpufreq/policy7/scaling_available_governors")
            } else {
                hidePrimeCluster()
            }
        }
    }

    private fun hidePrimeCluster() {
        runOnUiThread {
            listOf(spinnerMaxPrime, spinnerMinPrime, spinnerGovPrime).forEach { it.visibility = View.GONE }
        }
    }

    private fun setupThermalProfileSpinner() {
        val profiles = mapOf("Dynamic" to "10", "In-Calls" to "8", "Thermal 20" to "20", "Disable" to "0")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, profiles.keys.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        thermalProfileSpinner.adapter = adapter
        thermalProfileSpinner.setSelection(0)

        thermalProfileSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyThermalProfile(profiles.values.elementAt(position))
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun applyThermalProfile(value: String) {
        executeShellCommand("echo $value > /sys/class/thermal/thermal_message/sconfig")
    }

    private fun setSelinuxMode(mode: String) {
        executeShellCommand("setenforce $mode")
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }


    private fun getAvailableOptions(path: String): List<String> {
        val file = File(path)
        return if (file.exists()) {
            file.readText().trim().split("\\s+".toRegex())
        } else {
            emptyList()
        }
    }


    private fun executeShellCommand(command: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Runtime.getRuntime().exec(arrayOf("su", "-c", command)).waitFor()
            } catch (e: IOException) {
                Log.e("Shell", "Failed to execute: $command", e)
            }
        }
    }
}
