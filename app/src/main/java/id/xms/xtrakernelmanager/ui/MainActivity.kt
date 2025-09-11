package id.xms.xtrakernelmanager.ui

import InfoScreen
import TuningScreen
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.*
import dagger.hilt.android.AndroidEntryPoint
import id.xms.xtrakernelmanager.data.repository.RootRepository
import id.xms.xtrakernelmanager.data.repository.ThermalRepository
import id.xms.xtrakernelmanager.service.ThermalService
import id.xms.xtrakernelmanager.ui.components.BottomNavBar
import id.xms.xtrakernelmanager.ui.components.ExpressiveBackground
import id.xms.xtrakernelmanager.ui.components.KernelVerificationDialog
import id.xms.xtrakernelmanager.ui.components.RootRequiredDialog
import id.xms.xtrakernelmanager.ui.dialog.BatteryOptDialog
import id.xms.xtrakernelmanager.ui.screens.*
import id.xms.xtrakernelmanager.ui.theme.XtraTheme
import id.xms.xtrakernelmanager.util.BatteryOptimizationChecker
import id.xms.xtrakernelmanager.util.LanguageManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var rootRepo: RootRepository

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    @Inject
    lateinit var thermalRepository: ThermalRepository

    @Inject
    lateinit var languageManager: LanguageManager

    private lateinit var batteryOptChecker: BatteryOptimizationChecker
    private var showBatteryOptDialog by mutableStateOf(false)
    private var showRootRequiredDialog by mutableStateOf(false)
    private var showKernelVerificationDialog by mutableStateOf(false)
    private var permissionDenialCount by mutableIntStateOf(0)
    private val MAX_PERMISSION_RETRIES = 2

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force dark mode always - ignore system theme setting
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        // Initialize batteryOptChecker regardless of root status for consistency
        batteryOptChecker = BatteryOptimizationChecker(this)
        
        // Check root first, then kernel
        if (!rootRepo.isRooted()) {
            showRootRequiredDialog = true
        } else if (!isKernelSupported()) {
            showKernelVerificationDialog = true
        } else {
            // Only check permissions if device is rooted and kernel is supported
            checkAndHandlePermissions()
        }

        enableEdgeToEdge() // Enable edge-to-edge display for Android 16-like experience

        // Observe language changes
        lifecycleScope.launch {
            languageManager.currentLanguage.collect()
        }

        setContent {
            XtraTheme {
                val navController = rememberNavController()
                val items = listOf("Home", "Tuning", "Misc", "Info")

                // Use Surface instead of ExpressiveBackground to avoid potential composition issues
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showKernelVerificationDialog) {
                        KernelVerificationDialog(onDismiss = { finish() })
                    }
                    if (showRootRequiredDialog) {
                        RootRequiredDialog(onDismiss = { finish() })
                    }
                    // Only show permission dialog if device is rooted
                    if (showBatteryOptDialog && rootRepo.isRooted()) {
                        BatteryOptDialog(
                            onDismiss = {
                                // Only allow dismiss if we haven't exceeded retry limit
                                if (permissionDenialCount < MAX_PERMISSION_RETRIES) {
                                    showBatteryOptDialog = false
                                }
                            },
                            onConfirm = {
                                showBatteryOptDialog = false
                                batteryOptChecker.checkAndRequestPermissions(this@MainActivity)
                            },
                            onExit = { finish() },
                            showExitButton = permissionDenialCount >= MAX_PERMISSION_RETRIES
                        )
                    }

                    Scaffold(
                        containerColor = Color.Transparent,
                        bottomBar = { BottomNavBar(navController, items) },
                        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)
                    ) { innerPadding ->
                        // Add ExpressiveBackground here to wrap the navigation content
                        ExpressiveBackground {
                            NavHost(
                                navController = navController,
                                startDestination = "home",
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable("home") { HomeScreen(navController = navController) }
                                composable("tuning") { TuningScreen() }
                                composable("misc") { MiscScreen() }
                                composable("info") { InfoScreen() }
                                composable("settings") { SettingsScreen(navController = navController) }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkAndHandlePermissions() {
        // Only check permissions if device is rooted
        if (rootRepo.isRooted() && !batteryOptChecker.hasRequiredPermissions()) {
            showBatteryOptDialog = true
        } else if (rootRepo.isRooted()) {
            // Only start service if we have permissions and device is rooted
            startForegroundService(Intent(this, ThermalService::class.java))
        }
    }

    private fun isKernelSupported(): Boolean {
        // Check if the kernel has one of the supported signatures
        // This approach focuses on checking /proc/version which is more reliable
        val supportedSignatures = listOf(
            "bimoalfarrabi@github.com",
            "N0Kontzzz"
        )
        
        try {
            // Try to read /proc/version with root access
            var versionLine: String? = null
            
            try {
                val versionProcess = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat /proc/version"))
                val versionReader = java.io.BufferedReader(java.io.InputStreamReader(versionProcess.inputStream))
                versionLine = versionReader.readLine()
                versionReader.close()
                versionProcess.waitFor()
            } catch (e: Exception) {
                // If we can't read the file, assume kernel is not supported
                return false
            }
            
            // Check if the version string contains any supported signature
            if (versionLine != null) {
                for (signature in supportedSignatures) {
                    if (versionLine.contains(signature, ignoreCase = true)) {
                        return true
                    }
                }
            }
            
            // If no supported signature is found, kernel is not supported
            return false
        } catch (e: Exception) {
            // If we can't determine, assume it's not supported for security
            return false
        }
    }

    override fun onResume() {
        super.onResume()
        
        // Don't check permissions if kernel verification dialog is shown
        if (showKernelVerificationDialog) {
            return
        }
        
        // Only check permissions if device is rooted
        if (rootRepo.isRooted()) {
            // Check if permissions were denied
            if (!batteryOptChecker.hasRequiredPermissions()) {
                permissionDenialCount++
                if (permissionDenialCount >= MAX_PERMISSION_RETRIES) {
                    // Show dialog with exit button after max retries
                    showBatteryOptDialog = true
                } else if (!showBatteryOptDialog) {
                    // Show normal dialog if not already showing
                    showBatteryOptDialog = true
                }
            } else {
                // Reset counter if permissions are granted
                permissionDenialCount = 0
                showBatteryOptDialog = false

                // Ensure service is running if permissions are granted
                startForegroundService(Intent(this, ThermalService::class.java))
            }
        }
    }
}