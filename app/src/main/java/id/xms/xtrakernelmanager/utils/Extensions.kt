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
    val codename = getAndroidCodename(Build.VERSION.SDK_INT)
    return "Android ${Build.VERSION.RELEASE} ($codename)"
}

fun getAndroidCodename(apiLevel: Int): String {
    return when (apiLevel) {
        35 -> "Vanilla Ice Cream"
        34 -> "Upside Down Cake"
        33 -> "Tiramisu"
        32 -> "Snow Cone v2"
        31 -> "Snow Cone"
        30 -> "Red Velvet Cake"
        29 -> "Quince Tart"
        else -> if (apiLevel >= 36) "Baklava" else "API $apiLevel"
    }
}

fun getDeviceModel(): String {
    // Try to get market name first
    val marketName = getMarketName()
    return if (marketName.isNotEmpty() && marketName != Build.MODEL) {
        marketName
    } else {
        "${Build.MANUFACTURER} ${Build.MODEL}"
    }
}

private fun getMarketName(): String {
    return try {
        // Try system properties first
        val marketName = getSystemProperty("ro.product.marketname")
        if (marketName.isNotEmpty()) return marketName

        // Try vendor properties
        val vendorMarketName = getSystemProperty("ro.product.vendor.marketname")
        if (vendorMarketName.isNotEmpty()) return vendorMarketName

        // Try odm properties
        val odmMarketName = getSystemProperty("ro.product.odm.marketname")
        if (odmMarketName.isNotEmpty()) return odmMarketName

        // Xiaomi specific
        val miuiName = getSystemProperty("ro.product.mod_device")
        if (miuiName.isNotEmpty()) return miuiName

        // Try model for marketing name
        val model = getSystemProperty("ro.product.model")
        if (model.isNotEmpty() && model != Build.MODEL) return model

        ""
    } catch (e: Exception) {
        ""
    }
}

private fun getSystemProperty(key: String): String {
    return try {
        val process = Runtime.getRuntime().exec("getprop $key")
        val result = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        result
    } catch (e: Exception) {
        ""
    }
}

fun getKernelVersion(): String {
    return System.getProperty("os.version") ?: "Unknown"
}

fun getAbi(): String {
    return Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"
}
