package id.xms.xtrakernelmanager.utils

object Constants {
    const val CPU_BASE_PATH = "/sys/devices/system/cpu"
    const val GPU_KGSL_PATH = "/sys/class/kgsl/kgsl-3d0"
    const val THERMAL_PATH = "/sys/class/thermal"
    const val BATTERY_PATH = "/sys/class/power_supply/battery"

    const val THERMAL_SCONFIG = "/sys/class/thermal/thermal_message/sconfig"

    const val MIN_CORES_ONLINE = 1
    const val DEFAULT_SWAPPINESS = 60
    const val DEFAULT_DIRTY_RATIO = 20

    val THERMAL_PRESETS = mapOf(
        "Not Set" to 0,
        "Dynamic" to 10,
        "Incalls" to 8,
        "Thermal 20" to 20
    )
}
