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

fun Long.toMhzString(): String {
    val mhz = this / 1000
    return "$mhz MHz"
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
        36 -> "Baklava"
        35 -> "Vanilla Ice Cream"
        34 -> "Upside Down Cake"
        33 -> "Tiramisu"
        32 -> "Snow Cone v2"
        31 -> "Snow Cone"
        30 -> "Red Velvet Cake"
        29 -> "Quince Tart"
        else -> if (apiLevel >= 37) "Baklava" else "API $apiLevel"
    }
}

fun getDeviceModel(): String {
    val model = Build.MODEL

    // Check device model mapping first
    val mappedName = DEVICE_MODEL_MAPPING[model]
    if (mappedName != null) return mappedName

    // Try to get market name from system properties
    val marketName = getMarketName()
    if (marketName.isNotEmpty() && marketName != model) {
        return marketName
    }

    // Fallback to manufacturer + model
    return "${Build.MANUFACTURER} ${model}"
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

/**
 * Device Model Mapping
 * Map internal model codes to marketing names
 */
private val DEVICE_MODEL_MAPPING = mapOf(
    // Vivo/iQOO Devices
    "I2219" to "iQOO Z9x 5G",
    "I2217" to "iQOO Z9",
    "I2302" to "iQOO 12",
    "I2301" to "iQOO 12 Pro",
    "I2214" to "iQOO Neo 9 Pro",
    "V2245" to "Vivo X90 Pro",
    "V2227A" to "Vivo X80 Pro",
    "V2141" to "Vivo X70 Pro+",

    // Xiaomi/POCO Devices
    // POCO F Series
    "M1805E10A" to "POCO F1",
    "M2004J11G" to "POCO F2 Pro",
    "M2012K11AG" to "POCO F3",
    "21121210G" to "POCO F4",
    "22021211RG" to "POCO F4 GT",
    "23013RK75C" to "POCO F5 Pro",
    "23049PCD8G" to "POCO F5 5G",
    "24076PCD2G" to "POCO F6",
    "24076PCD2I" to "POCO F6 5G",
    "2407FPN8EG" to "POCO F6 Pro",
    "25017RK27G" to "POCO F7",
    "25017RK27I" to "POCO F7 5G",

    // Redmi Note Series
    "23021RAAEG" to "Redmi Note 12 Pro",
    "23021RAA2Y" to "Redmi Note 12 Pro 5G",
    "2211133C" to "Redmi Note 12",
    "22101316C" to "Redmi 12C",
    "22101316G" to "POCO C55",
    "23090RA98C" to "Redmi Note 13 Pro",
    "23090RA98G" to "Redmi Note 13 Pro 5G",
    "23124RN87C" to "Redmi Note 13",
    "23049RAD8C" to "Redmi Note 12 Turbo",
    "M1908C3JH" to "Redmi Note 8",
    "M1908C3JG" to "Redmi Note 8",
    "M1908C3JI" to "Redmi Note 8",
    "M2003J6B2G" to "Redmi Note 9 Pro",

    // Redmi C Series
    "24095RN6EC" to "Redmi 14C",
    "24095RN6EG" to "Redmi 14C",
    "24095RN6EI" to "Redmi 14C 5G",

    // Xiaomi Flagship
    "23078RKD5C" to "Xiaomi 13T",
    "23078RKD5G" to "Xiaomi 13T Pro",
    "2304FPN6DC" to "Xiaomi 13 Ultra",
    "2312DRA50C" to "Xiaomi 14",
    "23127PN0CC" to "Xiaomi 14 Pro",
    "24031PN0DC" to "Xiaomi 14 Ultra",
    "2409ARN2DC" to "Xiaomi 15",
    "2409ARN3DC" to "Xiaomi 15 Pro",

    // Redmi K Series
    "2311DRN39C" to "Redmi K70",
    "23113RKC6C" to "Redmi K70 Pro",
    "2311BRN47C" to "Redmi K70E",
    "24030PN60C" to "Redmi K70 Ultra",
    "2409ARK66C" to "Redmi K80",
    "2409ARK77C" to "Redmi K80 Pro",

    // Samsung Devices
    "SM-S918B" to "Galaxy S23 Ultra",
    "SM-S918N" to "Galaxy S23 Ultra 5G",
    "SM-S916B" to "Galaxy S23+",
    "SM-S911B" to "Galaxy S23",
    "SM-S928B" to "Galaxy S24 Ultra",
    "SM-S926B" to "Galaxy S24+",
    "SM-S921B" to "Galaxy S24",
    "SM-S938B" to "Galaxy S25 Ultra",
    "SM-S936B" to "Galaxy S25+",
    "SM-S931B" to "Galaxy S25",
    "SM-A546B" to "Galaxy A54 5G",
    "SM-A346B" to "Galaxy A34 5G",
    "SM-A245F" to "Galaxy A24",
    "SM-A556B" to "Galaxy A55 5G",
    "SM-A356B" to "Galaxy A35 5G",

    // Realme Devices
    "RMX3710" to "Realme 11 Pro+",
    "RMX3708" to "Realme 11 Pro",
    "RMX3686" to "Realme 11",
    "RMX3785" to "Realme 11x 5G",
    "RMX3830" to "Realme GT 5",
    "RMX3700" to "Realme GT Neo 5",
    "RMX3363" to "Realme GT 2 Pro",
    "RMX3360" to "Realme GT 2",
    "RMX3031" to "Realme GT Master Edition",
    "RMX2202" to "Realme GT Neo 2",
    "RMX3301" to "Realme GT Neo 3",
    "RMX3302" to "Realme GT Neo 3T",
    "RMX3491" to "Realme 10 Pro+",
    "RMX5010" to "Realme GT 7 Pro (China)",
    "RMX5011" to "Realme GT 7 Pro (Global)",

    // OnePlus Devices
    "CPH2573" to "OnePlus 12",
    "CPH2583" to "OnePlus 12R",
    "CPH2449" to "OnePlus 11",
    "CPH2451" to "OnePlus 11 5G",
    "CPH2305" to "OnePlus Nord 3",
    "CPH2413" to "OnePlus Nord CE 3",
    "CPH2455" to "OnePlus Nord CE 3 Lite",
    "CPH2617" to "OnePlus 13",
    "CPH2609" to "OnePlus 13R",
    "LE2123" to "OnePlus 9 Pro",
    "LE2121" to "OnePlus 9",
    "MT2111" to "OnePlus 9RT",
    "NE2213" to "OnePlus Nord 2T",

    // OPPO Devices
    "CPH2481" to "OPPO Find X6 Pro",
    "CPH2487" to "OPPO Find X6",
    "CPH2363" to "OPPO Reno 10 Pro+",
    "CPH2525" to "OPPO Reno 10 Pro",
    "CPH2531" to "OPPO Reno 10",
    "CPH2599" to "OPPO A78 5G",
    "CPH2609" to "OPPO Find X7",
    "CPH2611" to "OPPO Find X7 Pro",
    "CPH2591" to "OPPO Reno 11 Pro",
    "CPH2595" to "OPPO Reno 11",

    // Infinix Devices
    "X6739" to "Infinix Note 40 Pro",
    "X6837" to "Infinix Zero 30",
    "X6833B" to "Infinix Hot 40 Pro",
    "X6871" to "Infinix Note 40",
    "X6853" to "Infinix Zero 40",

    // Tecno Devices
    "CK8n" to "Tecno Camon 30",
    "CL6" to "Tecno Phantom X2 Pro",
    "CK6n" to "Tecno Camon 20 Pro",
    "CK7n" to "Tecno Camon 20 Premier"
)
