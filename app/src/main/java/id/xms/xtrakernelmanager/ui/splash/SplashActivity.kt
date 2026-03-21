package id.xms.xtrakernelmanager.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import id.xms.xtrakernelmanager.MainActivity
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.theme.XtraKernelManagerTheme

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      XtraKernelManagerTheme {
        val context = LocalContext.current
        val prefsManager = remember { PreferencesManager(context) }
        val layoutStyle by prefsManager.getLayoutStyle().collectAsState(initial = null)

        val navigateToMain: () -> Unit = {
          startActivity(Intent(this, MainActivity::class.java))
          finish()
          overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        ModernSplashScreen(onNavigateToMain = navigateToMain)
      }
    }
  }
}
