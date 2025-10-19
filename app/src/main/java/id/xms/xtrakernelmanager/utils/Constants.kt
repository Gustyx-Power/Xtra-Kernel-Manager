package id.xms.xtrakernelmanager.utils

object Constants {
    // System Paths
    const val SYS_CPU = "/sys/devices/system/cpu"
    const val SYS_CPU_POLICY = "/sys/devices/system/cpu/cpufreq"
    const val SYS_BATTERY = "/sys/class/power_supply/battery"
    const val SYS_GPU_QCOM = "/sys/class/kgsl/kgsl-3d0"
    const val SYS_THERMAL = "/sys/class/thermal"
    const val SYS_BLOCK = "/sys/block"

    // CPU Paths
    const val CPU_PRESENT = "$SYS_CPU/present"
    const val CPU_ONLINE = "$SYS_CPU/online"
    const val CPU_POSSIBLE = "$SYS_CPU/possible"

    // Battery Paths
    const val BATTERY_CAPACITY = "$SYS_BATTERY/capacity"
    const val BATTERY_TEMP = "$SYS_BATTERY/temp"
    const val BATTERY_VOLTAGE = "$SYS_BATTERY/voltage_now"
    const val BATTERY_CURRENT = "$SYS_BATTERY/current_now"
    const val BATTERY_HEALTH = "$SYS_BATTERY/health"
    const val BATTERY_STATUS = "$SYS_BATTERY/status"
    const val BATTERY_CYCLE = "$SYS_BATTERY/cycle_count"
    const val BATTERY_CHARGE_FULL = "$SYS_BATTERY/charge_full"
    const val BATTERY_CHARGE_FULL_DESIGN = "$SYS_BATTERY/charge_full_design"

    // GPU Paths
    const val GPU_FREQ = "$SYS_GPU_QCOM/devfreq/cur_freq"
    const val GPU_MAX_FREQ = "$SYS_GPU_QCOM/devfreq/max_freq"
    const val GPU_MIN_FREQ = "$SYS_GPU_QCOM/devfreq/min_freq"
    const val GPU_AVAILABLE_FREQS = "$SYS_GPU_QCOM/devfreq/available_frequencies"
    const val GPU_GOVERNOR = "$SYS_GPU_QCOM/devfreq/governor"
    const val GPU_POWER_LEVEL = "$SYS_GPU_QCOM/max_pwrlevel"

    // VM Paths
    const val VM_SWAPPINESS = "/proc/sys/vm/swappiness"
    const val VM_DIRTY_RATIO = "/proc/sys/vm/dirty_ratio"
    const val VM_DIRTY_BACKGROUND_RATIO = "/proc/sys/vm/dirty_background_ratio"

    // Thermal Config
    const val THERMAL_CONFIG = "/vendor/etc/thermal-engine.conf"
    const val THERMAL_SCONFIG = "/vendor/etc/thermal-engine-normal.conf"

    // Preferences Keys
    const val PREF_THEME_MODE = "theme_mode"
    const val PREF_THEME_STYLE = "theme_style"
    const val PREF_DYNAMIC_COLOR = "dynamic_color"
    const val PREF_BOOT_APPLY = "apply_on_boot"

    // Theme Modes
    const val THEME_MODE_SYSTEM = 0
    const val THEME_MODE_LIGHT = 1
    const val THEME_MODE_DARK = 2

    // Theme Styles
    const val THEME_GLASS = 0
    const val THEME_SOLID = 1
    const val THEME_GLASS_MATERIAL = 2

    // Thermal Presets
    val THERMAL_PRESETS = mapOf(
        "Not Set" to 0,
        "Incalls" to 8,
        "Dynamic" to 10,
        "Thermal 20" to 20
    )
}
