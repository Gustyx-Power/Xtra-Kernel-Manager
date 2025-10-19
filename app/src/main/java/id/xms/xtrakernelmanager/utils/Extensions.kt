package id.xms.xtrakernelmanager.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import java.text.DecimalFormat

fun Long.toFrequencyString(): String {
    val ghz = this / 1000000.0
    return DecimalFormat("#.##").format(ghz) + " GHz"
}

fun Long.toMhz(): Long = this / 1000

fun Float.format(decimals: Int = 2): String {
    return String.format("%.${decimals}f", this)
}

fun Int.toPercentage(): String = "$this%"

@Composable
fun Int.dpToPx(): Float {
    return with(LocalDensity.current) { this@dpToPx.toDp().toPx() }
}

@Composable
fun Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}

fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
}

fun getAndroidVersion(): String {
    return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
}

fun getDeviceModel(): String {
    return "${Build.MANUFACTURER} ${Build.MODEL}"
}

fun getKernelVersion(): String {
    return System.getProperty("os.version") ?: "Unknown"
}

fun getAbi(): String {
    return Build.SUPPORTED_ABIS.joinToString(", ")
}
