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
import id.xms.xtrakernelmanager.utils.AccessibilityServiceHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
  private val preferencesManager by lazy { PreferencesManager(this) }

  companion object {
    private const val TARGET_DENSITY_DPI = 410
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Use system status bar with proper theming
    androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, true)
    
    // Set navigation bar to transparent for edge-to-edge content
    window.navigationBarColor = android.graphics.Color.TRANSPARENT

    // Start foreground kernel config service (persistent tuning)
    startService(Intent(this, KernelConfigService::class.java))
    
    // Start battery info service conditionally
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
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
