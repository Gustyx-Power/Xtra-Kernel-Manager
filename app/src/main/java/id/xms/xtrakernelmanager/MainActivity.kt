package id.xms.xtrakernelmanager

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start foreground kernel config service (persistent tuning)
        startService(Intent(this, KernelConfigService::class.java))

        setContent {
            XtraKernelManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation(preferencesManager = preferencesManager)
                }
            }
        }
    }

    override fun onDestroy() {
        // Optional: stop service jika diperlukan saat app ditutup total
        stopService(Intent(this, KernelConfigService::class.java))
        super.onDestroy()
    }
}
