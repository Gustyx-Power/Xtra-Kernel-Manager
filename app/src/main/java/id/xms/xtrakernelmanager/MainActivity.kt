package id.xms.xtrakernelmanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
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

class MainActivity : ComponentActivity() {
  private val preferencesManager by lazy { PreferencesManager(this) }

  companion object {
    private const val TARGET_DENSITY_DPI = 410
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Check and request fullscreen permissions for various ROMs
    checkFullscreenPermissions()
    
    // Hide system status bar completely for fullscreen experience
    androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
    androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).apply {
      hide(androidx.core.view.WindowInsetsCompat.Type.statusBars())
      systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    
    // Set system bars to transparent
    window.statusBarColor = android.graphics.Color.TRANSPARENT
    window.navigationBarColor = android.graphics.Color.TRANSPARENT
    
    // Additional flags for fullscreen compatibility (OOS, MIUI, AOSP)
    @Suppress("DEPRECATION")
    window.decorView.systemUiVisibility = (
      android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
      or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
      or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    )

    // Start foreground kernel config service (persistent tuning)
    startService(Intent(this, KernelConfigService::class.java))
    // Start battery info service to populate real-time stats
    startService(Intent(this, id.xms.xtrakernelmanager.service.BatteryInfoService::class.java))
    // Start GameMonitorService
    startService(Intent(this, id.xms.xtrakernelmanager.service.GameMonitorService::class.java))
    // Start AppProfileService
    startService(Intent(this, id.xms.xtrakernelmanager.service.AppProfileService::class.java))

    setContent {
      XtraKernelManagerTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          Navigation(preferencesManager = preferencesManager)
        }
      }
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

    configuration.densityDpi = DisplayMetrics.DENSITY_420
    displayMetrics.density = TARGET_DENSITY_DPI / 160f
    displayMetrics.scaledDensity = displayMetrics.density * configuration.fontScale

    return context.createConfigurationContext(configuration)
  }

  override fun onDestroy() {
    stopService(Intent(this, KernelConfigService::class.java))
    super.onDestroy()
  }
  
  private fun checkFullscreenPermissions() {
    // Log ROM type for debugging
    val romType = id.xms.xtrakernelmanager.utils.FullscreenPermissionHelper.getRomType()
    android.util.Log.d("MainActivity", "Detected ROM: $romType")
    
    // Check overlay permission (optional, only if needed)
    if (!id.xms.xtrakernelmanager.utils.FullscreenPermissionHelper.hasOverlayPermission(this)) {
      android.util.Log.w("MainActivity", "Overlay permission not granted")
      // Uncomment to auto-request:
      // FullscreenPermissionHelper.requestOverlayPermission(this)
    }
  }
}
