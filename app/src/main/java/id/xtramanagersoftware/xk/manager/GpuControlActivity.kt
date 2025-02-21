package id.xtramanagersoftware.xk.manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.IOException



@Suppress("DEPRECATION")
class GpuControlActivity : AppCompatActivity() {

    private lateinit var footer: LinearLayout
    private val gpuMaxFreqPath = "/sys/class/kgsl/kgsl-3d0/max_gpuclk"
    private val gpuMinFreqPath = "/sys/class/kgsl/kgsl-3d0/min_gpuclk"
    private val gpuPwrLevelPath = "/sys/class/kgsl/kgsl-3d0/default_pwrlevel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gpu_control)


        val spinnerMaxFreq: Spinner = findViewById(R.id.spinnerMaxGpuFreq)
        val spinnerMinFreq: Spinner = findViewById(R.id.spinnerMinGpuFreq)
        val spinnerPwrLevel: Spinner = findViewById(R.id.spinnerPwrlevel)
        val applyButton: Button = findViewById(R.id.btnApply)
        val btnCPU : ImageButton = findViewById(R.id.btnCPU)
        val btnHome : ImageButton = findViewById(R.id.btnHome)
        footer = findViewById(R.id.footer)

        val config = resources.configuration
        config.densityDpi = 420
        resources.updateConfiguration(/* config = */ config, /* metrics = */
            resources.displayMetrics
        )

        val availableFreqs = getAvailableFrequencies()
        val availablePwrLevels = getAvailablePwrLevels()

        setupSpinner(this, spinnerMaxFreq, availableFreqs)
        setupSpinner(this, spinnerMinFreq, availableFreqs)
        setupSpinner(this, spinnerPwrLevel, availablePwrLevels)

        applyButton.setOnClickListener {
            val selectedMaxFreq = spinnerMaxFreq.selectedItem.toString()
            val selectedMinFreq = spinnerMinFreq.selectedItem.toString()
            val selectedPwrLevel = spinnerPwrLevel.selectedItem.toString()

            applyGpuSettings(selectedMaxFreq, selectedMinFreq, selectedPwrLevel)
        }

        btnCPU.setOnClickListener {
            startActivity(Intent(this,CpuControl::class.java))
        }

        btnHome.setOnClickListener {
            startActivity(Intent(this,Homepage::class.java))
        }
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

    private fun setupSpinner(context: Context, spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun getAvailableFrequencies(): List<String> {
        val freqFile = File("/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies")
        return if (freqFile.exists()) {
            freqFile.readText().trim().split(" ").map { it.trim() }
        } else {
            listOf("100000000", "200000000", "300000000") // Dummy data jika gagal membaca
        }
    }

    private fun getAvailablePwrLevels(): List<String> {
        val pwrFile = File("/sys/class/kgsl/kgsl-3d0/num_pwrlevels")
        return if (pwrFile.exists()) {
            val maxLevel = pwrFile.readText().trim().toIntOrNull() ?: 6
            (0..maxLevel).map { it.toString() }
        } else {
            (0..6).map { it.toString() } // Dummy data jika gagal membaca
        }
    }

    private fun applyGpuSettings(maxFreq: String, minFreq: String, pwrLevel: String) {
        try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "echo $maxFreq > $gpuMaxFreqPath"))
            Runtime.getRuntime().exec(arrayOf("su", "-c", "echo $minFreq > $gpuMinFreqPath"))
            Runtime.getRuntime().exec(arrayOf("su", "-c", "echo $pwrLevel > $gpuPwrLevelPath"))
            Toast.makeText(this, "GPU settings applied successfully", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Failed to apply GPU settings", Toast.LENGTH_SHORT).show()
        }
    }
}


