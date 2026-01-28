package id.xms.xtrakernelmanager.ui.components.statusbar

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.telephony.TelephonyManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

data class StatusBarData(
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val signalStrength: Int = 4,
    val wifiEnabled: Boolean = true
)

@Composable
fun rememberStatusBarData(): StatusBarData {
    val context = LocalContext.current
    var statusBarData by remember { mutableStateOf(StatusBarData()) }

    LaunchedEffect(Unit) {
        while (true) {
            statusBarData = getStatusBarData(context)
            delay(5000) // Update every 5 seconds
        }
    }

    return statusBarData
}

private fun getStatusBarData(context: Context): StatusBarData {
    val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    
    // Battery info
    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 100
    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
    val batteryPct = if (scale > 0) (level * 100 / scale) else 100
    
    val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                     status == BatteryManager.BATTERY_STATUS_FULL

    // WiFi info
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    val network = connectivityManager?.activeNetwork
    val capabilities = connectivityManager?.getNetworkCapabilities(network)
    val wifiEnabled = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

    // Signal strength (simplified - returns 0-4)
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    val signalStrength = try {
        // This is a simplified version, actual signal strength requires more complex logic
        when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> 4
            else -> 0
        }
    } catch (e: Exception) {
        4 // Default to full signal
    }

    return StatusBarData(
        batteryLevel = batteryPct,
        isCharging = isCharging,
        signalStrength = signalStrength,
        wifiEnabled = wifiEnabled
    )
}
