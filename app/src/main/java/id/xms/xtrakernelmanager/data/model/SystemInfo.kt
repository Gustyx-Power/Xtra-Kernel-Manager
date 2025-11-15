package id.xms.xtrakernelmanager.data.model

data class SystemInfo(
    val androidVersion: String = "",
    val abi: String = "",
    val kernelVersion: String = "",
    val deviceModel: String = "",
    val fingerprint: String = "",
    val selinux: String = "",
    val thermalMode: String = "Not Set",
    val totalRam: Long = 0,
    val swapTotal: Long = 0,
    val availableRam: Long = 0,
    val zramSize: Long = 0,
    val totalStorage: Long = 0,
    val availableStorage: Long = 0
)
