package id.xms.xtrakernelmanager.utils

import java.util.Locale
import kotlin.math.pow

fun Long.toReadableSize(): String {
    if (this < 1024) return "$this B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(this.toDouble()) / Math.log10(1024.0)).toInt()

    val value = this / 1024.0.pow(digitGroups.toDouble())
    return String.format(Locale.US, "%.2f %s", value, units[digitGroups])
}

fun Int.toMHz(): String = "$this MHz"

fun Int.toGHz(): String = String.format(Locale.US, "%.2f GHz", this / 1000.0)

fun Float.toCelsius(): String = String.format(Locale.US, "%.1f°C", this)

fun Int.toPercent(): String = "$this%"

fun Float.toPercentString(): String = String.format(Locale.US, "%.1f%%", this)
