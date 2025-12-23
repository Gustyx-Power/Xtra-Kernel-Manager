package id.xms.xtrakernelmanager

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class XtraKernelApp : Application() {

    companion object {
        private const val TARGET_DENSITY_DPI = 410
        private const val TAG = "XtraKernelApp"
        private const val PREF_LAST_VERSION_CODE = "last_version_code"

        init {
            Shell.enableVerboseLogging = BuildConfig.DEBUG
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_MOUNT_MASTER)
                    .setTimeout(10)
            )
        }
    }

    private val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        // Check for app update and clear data if needed
        checkAndHandleAppUpdate()

        // Force app to use 410 DPI
        setAppDensity()

        // Initialize root shell in background with callback
        // This is important for Magisk 28+ compatibility
        initializeRootShell()
    }

    /**
     * Initialize root shell with proper error handling for Magisk 28+
     * Uses callback to ensure the superuser permission dialog appears correctly
     */
    private fun initializeRootShell() {
        Shell.getShell { shell ->
            if (shell.isRoot) {
                Log.d(TAG, "Root shell initialized successfully")
            } else {
                Log.w(TAG, "Root access not available or denied")
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Re-apply density when configuration changes
        setAppDensity()
    }

    /**
     * Check if app was updated and clear data if version changed
     * This prevents crashes from incompatible old data
     */
    private fun checkAndHandleAppUpdate() {
        try {
            val currentVersionCode = BuildConfig.VERSION_CODE
            val prefs = getSharedPreferences("app_version_prefs", Context.MODE_PRIVATE)
            val lastVersionCode = prefs.getInt(PREF_LAST_VERSION_CODE, 0)

            Log.d(TAG, "Current version: $currentVersionCode, Last version: $lastVersionCode")

            if (lastVersionCode != 0 && lastVersionCode != currentVersionCode) {
                // App was updated, clear data to prevent crashes
                Log.w(TAG, "App updated from $lastVersionCode to $currentVersionCode, clearing data...")
                clearAppData()

                // Show toast on main thread
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        this@XtraKernelApp,
                        getString(R.string.app_updated_reset),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            // Save current version code
            prefs.edit().putInt(PREF_LAST_VERSION_CODE, currentVersionCode).apply()
            Log.d(TAG, "Saved current version code: $currentVersionCode")

        } catch (e: Exception) {
            Log.e(TAG, "Error checking app update: ${e.message}")
        }
    }

    /**
     * Clear DataStore and other app data files
     */
    private fun clearAppData() {
        appScope.launch {
            try {
                // Clear DataStore files
                val dataStoreDir = File(filesDir, "datastore")
                if (dataStoreDir.exists()) {
                    dataStoreDir.listFiles()?.forEach { file ->
                        file.delete()
                        Log.d(TAG, "Deleted DataStore file: ${file.name}")
                    }
                }

                // Clear shared preferences (except version prefs)
                val prefsDir = File(applicationInfo.dataDir, "shared_prefs")
                if (prefsDir.exists()) {
                    prefsDir.listFiles()?.forEach { file ->
                        if (!file.name.contains("app_version_prefs")) {
                            file.delete()
                            Log.d(TAG, "Deleted SharedPrefs file: ${file.name}")
                        }
                    }
                }

                Log.d(TAG, "App data cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing app data: ${e.message}")
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    @Suppress("DEPRECATION")
    private fun setAppDensity() {
        val displayMetrics = resources.displayMetrics
        val configuration = resources.configuration

        // Set density to 410 DPI (using 420 as closest standard value)
        configuration.densityDpi = DisplayMetrics.DENSITY_420
        displayMetrics.density = TARGET_DENSITY_DPI / 160f
        displayMetrics.scaledDensity = displayMetrics.density * configuration.fontScale

        // Apply the configuration
        resources.updateConfiguration(configuration, displayMetrics)
    }
}
