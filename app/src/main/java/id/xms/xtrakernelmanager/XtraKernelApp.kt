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
    
    Log.d(TAG, "XtraKernelApp onCreate() called")

    // Initialize Firebase with manual configuration for debug/release
    initializeFirebaseManually()
    
    Log.d(TAG, "After Firebase initialization")

    // Check for app update and clear data if needed
    checkAndHandleAppUpdate()

    // Force app to use 410 DPI
    setAppDensity()
    
    // Configure Coil for memory optimization
    configureCoilImageLoader()

    // Initialize root shell in background with callback
    // This is important for Magisk 28+ compatibility
    initializeRootShell()
    
    // Schedule donation reminder notification
    scheduleDonationReminder()
    
    // Schedule update check notification
    scheduleUpdateCheck()
  }
  
  /**
   * Initialize Firebase manually with proper configuration
   */
  private fun initializeFirebaseManually() {
    try {
      Log.d(TAG, "Starting Firebase initialization...")
      
      // Check if Firebase is already initialized
      val existingApps = com.google.firebase.FirebaseApp.getApps(this)
      Log.d(TAG, "Existing Firebase apps: ${existingApps.size}")
      
      if (existingApps.isNotEmpty()) {
        Log.d(TAG, "Firebase already initialized")
        val app = com.google.firebase.FirebaseApp.getInstance()
        val options = app.options
        Log.d(TAG, "Firebase - Project: ${options.projectId}, Database: ${options.databaseUrl}")
        return
      }
      
      // Try to initialize with default first (from google-services.json)
      Log.d(TAG, "Trying to initialize Firebase from google-services.json...")
      var app = com.google.firebase.FirebaseApp.initializeApp(this)
      
      // If default initialization failed, initialize manually
      if (app == null) {
        Log.w(TAG, "Default Firebase initialization failed, trying manual initialization...")
        
        // Manual initialization for debug build
        val options = com.google.firebase.FirebaseOptions.Builder()
            .setProjectId(if (BuildConfig.DEBUG) "xkm-debug" else "xtrakernelmanager")
            .setApplicationId(if (BuildConfig.DEBUG) "1:411675796401:android:eb444772b7301fa0599390" else "1:234012707950:android:c4e3a64566e834515fea7f")
            .setApiKey(if (BuildConfig.DEBUG) "AIzaSyDvrLwap3VC2XRj1BXu5-yF-j0TX4APk-M" else "AIzaSyAA9l6eKBgLmLO9R8woNuKU1E_Xps3Fb6o")
            .setDatabaseUrl(if (BuildConfig.DEBUG) "https://xkm-debug-default-rtdb.asia-southeast1.firebasedatabase.app" else "https://xtrakernelmanager-default-rtdb.asia-southeast1.firebasedatabase.app")
            .setStorageBucket(if (BuildConfig.DEBUG) "xkm-debug.firebasestorage.app" else "xtrakernelmanager.firebasestorage.app")
            .build()
        
        app = com.google.firebase.FirebaseApp.initializeApp(this, options)
        Log.d(TAG, "Manual Firebase initialization completed")
      }
      
      if (app != null) {
        val options = app.options
        Log.d(TAG, "Firebase initialized successfully!")
        Log.d(TAG, "  Project ID: ${options.projectId}")
        Log.d(TAG, "  Database URL: ${options.databaseUrl}")
        Log.d(TAG, "  API Key: ${options.apiKey}")
        Log.d(TAG, "  App ID: ${options.applicationId}")
        
        // Configure Realtime Database
        val database = com.google.firebase.database.FirebaseDatabase.getInstance()
        database.setPersistenceEnabled(false)
        Log.d(TAG, "Firebase Realtime Database configured successfully")
      } else {
        Log.e(TAG, "Firebase initialization still returned null after manual attempt!")
      }
    } catch (e: Exception) {
      Log.e(TAG, "CRITICAL ERROR initializing Firebase: ${e.message}", e)
      e.printStackTrace()
    }
  }
  
  /**
   * Configure Coil image loader with memory optimization
   * Limits memory cache to 15% of app memory to reduce RAM usage
   */
  private fun configureCoilImageLoader() {
    try {
      val imageLoader = coil.ImageLoader.Builder(this)
          .memoryCache {
            coil.memory.MemoryCache.Builder(this)
                .maxSizePercent(0.15) // Limit to 15% of app memory
                .build()
          }
          .diskCache {
            coil.disk.DiskCache.Builder()
                .directory(cacheDir.resolve("image_cache"))
                .maxSizeBytes(50 * 1024 * 1024) // 50MB disk cache
                .build()
          }
          .crossfade(true)
          .build()
      coil.Coil.setImageLoader(imageLoader)
      Log.d(TAG, "Coil image loader configured with memory optimization")
    } catch (e: Exception) {
      Log.e(TAG, "Error configuring Coil: ${e.message}", e)
    }
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
  
  /**
   * Schedule donation reminder notification
   * Will check every 6 hours if 3 days have passed since last shown
   */
  private fun scheduleDonationReminder() {
    try {
      Log.d(TAG, "Scheduling Donation Reminder")
      id.xms.xtrakernelmanager.utils.DonationReminderScheduler.scheduleDonationReminder(this)
      Log.d(TAG, "Donation reminder scheduled")
    } catch (e: Exception) {
      Log.e(TAG, "Error scheduling donation reminder: ${e.message}", e)
    }
  }
  
  /**
   * Schedule update check notification
   * Will check every 6 hours for new release or beta updates
   */
  private fun scheduleUpdateCheck() {
    try {
      Log.d(TAG, "Scheduling Update Check")
      val workRequest = androidx.work.PeriodicWorkRequestBuilder<id.xms.xtrakernelmanager.domain.worker.UpdateCheckWorker>(
          6, java.util.concurrent.TimeUnit.HOURS
      )
          .setConstraints(
              androidx.work.Constraints.Builder()
                  .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                  .build()
          )
          .build()
      
      androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
          "update_check_worker",
          androidx.work.ExistingPeriodicWorkPolicy.KEEP,
          workRequest
      )
      Log.d(TAG, "Update check scheduled successfully")
    } catch (e: Exception) {
      Log.e(TAG, "Error scheduling update check: ${e.message}", e)
    }
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    
    Log.d(TAG, "Configuration changed - New DPI: ${newConfig.densityDpi}, Screen: ${newConfig.screenWidthDp}x${newConfig.screenHeightDp}dp")
    
    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
      setAppDensity()
    }, 100)
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
    
    val preferencesManager = id.xms.xtrakernelmanager.data.preferences.PreferencesManager(this)
    var dpiMode = "SMART"
    
    try {
      val prefs = getSharedPreferences("xtra_settings", Context.MODE_PRIVATE)
      dpiMode = prefs.getString("dpi_mode", "SMART") ?: "SMART"
    } catch (e: Exception) {
      Log.w(TAG, "Could not read DPI preference, using SMART mode")
    }
    
    val systemDensityDpi = configuration.densityDpi
    
    val widthInches = configuration.screenWidthDp / 160f
    val heightInches = configuration.screenHeightDp / 160f
    val diagonalInches = kotlin.math.sqrt(widthInches * widthInches + heightInches * heightInches)
    
    val isTablet = (configuration.screenWidthDp >= 600) || 
                   (diagonalInches >= 7.0f && systemDensityDpi <= 320)
    val isHighResPhone = !isTablet && systemDensityDpi >= 400
    
    val shouldForceDPI = when (dpiMode) {
      "SYSTEM" -> false
      "FORCE_410" -> true
      "SMART" -> !isTablet && !isHighResPhone
      else -> !isTablet && !isHighResPhone
    }
    
    if (shouldForceDPI) {
      configuration.densityDpi = DisplayMetrics.DENSITY_420
      displayMetrics.density = TARGET_DENSITY_DPI / 160f
      displayMetrics.scaledDensity = displayMetrics.density * configuration.fontScale
      
      try {
        resources.updateConfiguration(configuration, displayMetrics)
        
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
          resources.updateConfiguration(configuration, displayMetrics)
        }, 50)
      } catch (e: Exception) {
        Log.e(TAG, "Error applying DPI configuration", e)
      }
      
      Log.d(TAG, "Applied 410 DPI (mode: $dpiMode, device: ${if (isTablet) "tablet" else "phone"}, originalDPI: $systemDensityDpi)")
    } else {
      Log.d(TAG, "Using system DPI (mode: $dpiMode, device: ${if (isTablet) "tablet" else "phone"}, systemDPI: $systemDensityDpi)")
    }
  }
}
