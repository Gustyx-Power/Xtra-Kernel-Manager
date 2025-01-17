package id.gustyx.xk.manager

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var littleMaxFreqSpinner: Spinner
    private lateinit var littleMinFreqSpinner: Spinner
    private lateinit var bigMaxFreqSpinner: Spinner
    private lateinit var bigMinFreqSpinner: Spinner
    private lateinit var primeMaxFreqSpinner: Spinner
    private lateinit var primeMinFreqSpinner: Spinner
    private lateinit var primeClusterLabel: TextView
    private lateinit var primeMaxFreqLabel: TextView
    private lateinit var primeMinFreqLabel: TextView
    private lateinit var littleGovernorSpinner: Spinner
    private lateinit var bigGovernorSpinner: Spinner
    private lateinit var primeGovernorSpinner: Spinner
    private lateinit var btnPowerSaving: Button
    private lateinit var btnBalanced: Button
    private lateinit var btnPerformance: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        littleMaxFreqSpinner = findViewById(R.id.littleMaxFreqSpinner)
        littleMinFreqSpinner = findViewById(R.id.littleMinFreqSpinner)
        bigMaxFreqSpinner = findViewById(R.id.bigMaxFreqSpinner)
        bigMinFreqSpinner = findViewById(R.id.bigMinFreqSpinner)
        primeMaxFreqSpinner = findViewById(R.id.primeMaxFreqSpinner)
        primeMinFreqSpinner = findViewById(R.id.primeMinFreqSpinner)
        primeClusterLabel = findViewById(R.id.primeClusterLabel)
        primeMaxFreqLabel = findViewById(R.id.primeMaxFreqLabel)
        primeMinFreqLabel = findViewById(R.id.primeMinFreqLabel)
        littleGovernorSpinner = findViewById(R.id.littleGovernorSpinner)
        bigGovernorSpinner = findViewById(R.id.bigGovernorSpinner)
        primeGovernorSpinner = findViewById(R.id.primeGovernorSpinner)
        btnPowerSaving = findViewById(R.id.btnPowerSaving)
        btnBalanced = findViewById(R.id.btnBalanced)
        btnPerformance = findViewById(R.id.btnPerformance)

        val littleFrequencies = getAvailableFrequencies("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies")
        val bigFrequencies = getAvailableFrequencies("/sys/devices/system/cpu/cpu4/cpufreq/scaling_available_frequencies")
        val primeFrequencies = getAvailableFrequencies("/sys/devices/system/cpu/cpu7/cpufreq/scaling_available_frequencies")
        val littleGovernors = getAvailableGovernors("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors")
        val bigGovernors = getAvailableGovernors("/sys/devices/system/cpu/cpu4/cpufreq/scaling_available_governors")
        val primeGovernors = getAvailableGovernors("/sys/devices/system/cpu/cpu7/cpufreq/scaling_available_governors")

        setupSpinner(littleMaxFreqSpinner, littleFrequencies)
        setupSpinner(littleMinFreqSpinner, littleFrequencies)
        setupSpinner(bigMaxFreqSpinner, bigFrequencies)
        setupSpinner(bigMinFreqSpinner, bigFrequencies)
        setupSpinner(littleGovernorSpinner, littleGovernors)
        setupSpinner(bigGovernorSpinner, bigGovernors)

        if (primeFrequencies.isNotEmpty()) {
            primeClusterLabel.visibility = View.VISIBLE
            primeMaxFreqLabel.visibility = View.VISIBLE
            primeMinFreqLabel.visibility = View.VISIBLE
            primeMaxFreqSpinner.visibility = View.VISIBLE
            primeMinFreqSpinner.visibility = View.VISIBLE
            primeGovernorSpinner.visibility = View.VISIBLE
            setupSpinner(primeMaxFreqSpinner, primeFrequencies)
            setupSpinner(primeMinFreqSpinner, primeFrequencies)
            setupSpinner(primeGovernorSpinner, primeGovernors)
        }


        littleMaxFreqSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedFreq = littleFrequencies[position]
                executeRootCommand("echo $selectedFreq > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq")
                showToast("Little Cluster Max Frequency set to $selectedFreq")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        littleMinFreqSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedFreq = littleFrequencies[position]
                executeRootCommand("echo $selectedFreq > /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq")
                showToast("Little Cluster Min Frequency set to $selectedFreq")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        bigMaxFreqSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedFreq = bigFrequencies[position]
                executeRootCommand("echo $selectedFreq > /sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq")
                showToast("Big Cluster Max Frequency set to $selectedFreq")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        bigMinFreqSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedFreq = bigFrequencies[position]
                executeRootCommand("echo $selectedFreq > /sys/devices/system/cpu/cpu4/cpufreq/scaling_min_freq")
                showToast("Big Cluster Min Frequency set to $selectedFreq")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        primeMaxFreqSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedFreq = primeFrequencies[position]
                executeRootCommand("echo $selectedFreq > /sys/devices/system/cpu/cpu7/cpufreq/scaling_max_freq")
                showToast("Prime Cluster Max Frequency set to $selectedFreq")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        primeMinFreqSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedFreq = primeFrequencies[position]
                executeRootCommand("echo $selectedFreq > /sys/devices/system/cpu/cpu7/cpufreq/scaling_min_freq")
                showToast("Prime Cluster Min Frequency set to $selectedFreq")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        littleGovernorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedGovernor = littleGovernors[position]
                executeRootCommand("echo $selectedGovernor > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
                showToast("Little Cluster Governor set to $selectedGovernor")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        bigGovernorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedGovernor = bigGovernors[position]
                executeRootCommand("echo $selectedGovernor > /sys/devices/system/cpu/cpu4/cpufreq/scaling_governor")
                showToast("Big Cluster Governor set to $selectedGovernor")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        primeGovernorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedGovernor = primeGovernors[position]
                executeRootCommand("echo $selectedGovernor > /sys/devices/system/cpu/cpu7/cpufreq/scaling_governor")
                showToast("Prime Cluster Governor set to $selectedGovernor")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnPowerSaving.setOnClickListener {
            setGovernorMode("powersave")
        }

        btnBalanced.setOnClickListener {
            setGovernorMode("schedutil")
        }

        btnPerformance.setOnClickListener {
            setGovernorMode("performance")
        }
    }

    private fun setGovernorMode(mode: String) {
        // Set governor mode for all clusters
        executeRootCommand("echo $mode > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
        executeRootCommand("echo $mode > /sys/devices/system/cpu/cpu4/cpufreq/scaling_governor")
        executeRootCommand("echo $mode > /sys/devices/system/cpu/cpu7/cpufreq/scaling_governor")
        showToast("Governor mode set to $mode")
    }

    private fun getAvailableFrequencies(path: String): List<String> {
        return try {
            val file = File(path)
            if (file.exists()) {
                file.readText().trim().split("\\s+".toRegex())
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getAvailableGovernors(path: String): List<String> {
        return try {
            val file = File(path)
            if (file.exists()) {
                file.readText().trim().split("\\s+".toRegex())
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun executeRootCommand(command: String) {
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = process.outputStream
            val writer = os.writer()
            writer.write("$command\n")
            writer.write("exit\n")
            writer.flush()
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

