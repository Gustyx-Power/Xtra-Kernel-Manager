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

    // Start foreground kernel config service (persistent tuning)
    startService(Intent(this, KernelConfigService::class.java))

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
}
