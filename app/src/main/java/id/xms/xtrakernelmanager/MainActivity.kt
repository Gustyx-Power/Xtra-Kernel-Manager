package id.xms.xtrakernelmanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.service.KernelConfigService
import id.xms.xtrakernelmanager.ui.navigation.Navigation
import id.xms.xtrakernelmanager.ui.theme.XtraKernelManagerTheme
import id.xms.xtrakernelmanager.utils.AccessibilityServiceHelper
import id.xms.xtrakernelmanager.utils.DisplayHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
  private val preferencesManager by lazy { PreferencesManager(this) }
  private var isDCDimmingActive = false
  private var isLowBrightnessMode = false
  private var shouldShowDonationDialog = false

  companion object {
    private const val TARGET_DENSITY_DPI = 410
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Request notification permission for Android 13+
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != 
          android.content.pm.PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
      }
    }
    
    // Check if opened from donation notification
    shouldShowDonationDialog = intent?.getBooleanExtra("show_donation_dialog", false) ?: false
    
    CoroutineScope(Dispatchers.IO).launch {
        isDCDimmingActive = DisplayHelper.isDCDimmingEnabled(this@MainActivity)
        val brightness = DisplayHelper.getCurrentBrightness(this@MainActivity)
        isLowBrightnessMode = DisplayHelper.isLowBrightness(brightness)
        
        Handler(Looper.getMainLooper()).post {
            setupWindowForDCDimming()
        }
    }
    
    enableEdgeToEdge()
    
    androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, true)
    
    window.navigationBarColor = android.graphics.Color.TRANSPARENT

    startService(Intent(this, KernelConfigService::class.java))
    
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val enabled = preferencesManager.isShowBatteryNotif().first()
            if (enabled) {
                startService(Intent(this@MainActivity, id.xms.xtrakernelmanager.service.BatteryInfoService::class.java))
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to check battery notif pref: ${e.message}")
        }
    }
    checkGameMonitorServiceStatus()
    startService(Intent(this, id.xms.xtrakernelmanager.service.AppProfileService::class.java))
    
    // Schedule donation reminder
    id.xms.xtrakernelmanager.utils.DonationReminderScheduler.scheduleDonationReminder(this)
    
    setContent {
      val layoutStyle by preferencesManager.getLayoutStyle().collectAsState(initial = "liquid")
      
      // XKM default dark mode only
      XtraKernelManagerTheme(
        darkTheme = true,
        dynamicColor = layoutStyle != "liquid"
      ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          Navigation(
              preferencesManager = preferencesManager,
              shouldShowDonationDialog = shouldShowDonationDialog
          )
        }
      }
    }
  }
  
  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    // Handle new intent when app is already running
    shouldShowDonationDialog = intent.getBooleanExtra("show_donation_dialog", false)
  }
  
  private fun setupWindowForDCDimming() {
    if (isDCDimmingActive && isLowBrightnessMode) {
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        
        window.attributes = window.attributes.apply {
            flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            flags = flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
        }
        
        Handler(Looper.getMainLooper()).postDelayed({
            window.decorView.invalidate()
        }, 100)
    }
  }

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(updateDensity(newBase))
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    updateDensity(this)
  }

  @SuppressLint("DiscouragedApi")
  @Suppress("DEPRECATION")
  private fun updateDensity(context: Context): Context {
    val configuration = Configuration(context.resources.configuration)
    val displayMetrics = context.resources.displayMetrics
    
    // Get DPI mode preference
    var dpiMode = "SMART" // Default
    try {
      val prefs = getSharedPreferences("xtra_settings", Context.MODE_PRIVATE)
      dpiMode = prefs.getString("dpi_mode", "SMART") ?: "SMART"
    } catch (e: Exception) {
      // Fallback to SMART mode
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
      configuration.densityDpi = DisplayMetrics.DENSITY_420
      displayMetrics.density = TARGET_DENSITY_DPI / 160f
      displayMetrics.scaledDensity = displayMetrics.density * configuration.fontScale
    }
    // For other modes, keep the system DPI (including user modifications)

    return context.createConfigurationContext(configuration)
  }

  override fun onDestroy() {
    stopService(Intent(this, KernelConfigService::class.java))
    super.onDestroy()
  }
  
  private fun checkGameMonitorServiceStatus() {
    try {
      val isEnabled = AccessibilityServiceHelper.isGameMonitorServiceEnabled(this)
      if (isEnabled) {
        android.util.Log.d("MainActivity", "GameMonitorService accessibility is enabled")
      } else {
        android.util.Log.w("MainActivity", "GameMonitorService accessibility is not enabled. User must enable it manually in Settings > Accessibility")
        android.util.Log.i("MainActivity", "Service name: ${AccessibilityServiceHelper.getServiceName(this)}")
      }
    } catch (e: Exception) {
      android.util.Log.e("MainActivity", "Failed to check GameMonitorService status: ${e.message}")
    }
  }
  
  private fun isAccessibilityServiceEnabled(): Boolean {
    return AccessibilityServiceHelper.isGameMonitorServiceEnabled(this)
  }
}
