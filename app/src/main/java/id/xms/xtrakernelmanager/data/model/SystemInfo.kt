package id.xms.xtrakernelmanager.data.model

data class SystemInfo(
    val androidVersion: String = "",
    val kernelVersion: String = "",
    val deviceModel: String = "",
    val abi: String = "",
    val fingerprint: String = "",
    val selinuxStatus: String = "",
    val totalRam: Long = 0,
    val availableRam: Long = 0,
    val totalStorage: Long = 0,
    val availableStorage: Long = 0,
    val zramSize: Long = 0,
    val swappiness: Int = 0
)
