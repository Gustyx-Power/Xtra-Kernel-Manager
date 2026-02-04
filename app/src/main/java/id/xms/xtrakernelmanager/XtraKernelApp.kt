package id.xms.xtrakernelmanager

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import com.topjohnwu.superuser.Shell
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class XtraKernelApp : Application() {

  companion object {
    private const val TARGET_DENSITY_DPI = 410
    private const val TAG = "XtraKernelApp"
    private const val PREF_LAST_VERSION_CODE = "last_version_code"

    init {
      Shell.enableVerboseLogging = BuildConfig.DEBUG
      Shell.setDefaultBuilder(
          Shell.Builder.create().setFlags(Shell.FLAG_MOUNT_MASTER).setTimeout(10)
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
   * Initialize root shell with proper error handling for Magisk 28+ Uses callback to ensure the
   * superuser permission dialog appears correctly
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
    
    // Log DPI changes for debugging
    Log.d(TAG, "Configuration changed - New DPI: ${newConfig.densityDpi}, Screen: ${newConfig.screenWidthDp}x${newConfig.screenHeightDp}dp")
    
    // Re-apply density when configuration changes
    // This will respect user DPI changes from Developer Options
    setAppDensity()
  }

  /**
   * Check if app was updated and clear data if version changed This prevents crashes from
   * incompatible old data
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
                  Toast.LENGTH_LONG,
              )
              .show()
        }
      }

      // Save current version code
      prefs.edit().putInt(PREF_LAST_VERSION_CODE, currentVersionCode).apply()
      Log.d(TAG, "Saved current version code: $currentVersionCode")
    } catch (e: Exception) {
      Log.e(TAG, "Error checking app update: ${e.message}")
    }
  }

  /** Clear DataStore and other app data files */
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
    
    // Get DPI mode preference
    val preferencesManager = id.xms.xtrakernelmanager.data.preferences.PreferencesManager(this)
    var dpiMode = "SMART" // Default
    
    // Try to get preference synchronously (for app startup)
    try {
      val prefs = getSharedPreferences("xtra_settings", Context.MODE_PRIVATE)
      dpiMode = prefs.getString("dpi_mode", "SMART") ?: "SMART"
    } catch (e: Exception) {
      Log.w(TAG, "Could not read DPI preference, using SMART mode")
    }
    
    // Get original system DPI before any modifications
    val systemDensityDpi = configuration.densityDpi
    
    // Calculate screen size in inches to better detect tablets
    val widthInches = configuration.screenWidthDp / 160f
    val heightInches = configuration.screenHeightDp / 160f
    val diagonalInches = kotlin.math.sqrt(widthInches * widthInches + heightInches * heightInches)
    
    // Device detection
    val isTablet = (configuration.screenWidthDp >= 600) || 
                   (diagonalInches >= 7.0f && systemDensityDpi <= 320)
    val isHighResPhone = !isTablet && systemDensityDpi >= 400
    
    // Determine if we should force DPI based on mode and device
    val shouldForceDPI = when (dpiMode) {
      "SYSTEM" -> false // Never force, always use system DPI
      "FORCE_410" -> true // Always force 410 DPI
      "SMART" -> !isTablet && !isHighResPhone // Smart detection (default)
      else -> !isTablet && !isHighResPhone // Fallback to smart
    }
    
    if (shouldForceDPI) {
      // Set density to 410 DPI (using 420 as closest standard value)
      configuration.densityDpi = DisplayMetrics.DENSITY_420
      displayMetrics.density = TARGET_DENSITY_DPI / 160f
      displayMetrics.scaledDensity = displayMetrics.density * configuration.fontScale
      
      // Apply the configuration
      resources.updateConfiguration(configuration, displayMetrics)
      
      Log.d(TAG, "Applied 410 DPI (mode: $dpiMode, device: ${if (isTablet) "tablet" else "phone"}, originalDPI: $systemDensityDpi)")
    } else {
      Log.d(TAG, "Using system DPI (mode: $dpiMode, device: ${if (isTablet) "tablet" else "phone"}, systemDPI: $systemDensityDpi)")
    }
  }
}
