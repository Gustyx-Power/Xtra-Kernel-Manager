package id.xms.xtrakernelmanager.data.model

data class BatteryInfo(
    val level: Int = 0,
    val temperature: Float = 0f,
    val voltage: Float = 0f,
    val current: Float = 0f,
    val health: String = "Unknown",
    val status: String = "Unknown",
    val cycleCount: Int = 0,
    val chargeFull: Long = 0,
    val chargeFullDesign: Long = 0,
    val capacity: Int = 0,
    val healthPercentage: Float = 0f
) {
    val capacityMah: Long
        get() = chargeFull / 1000

    val designCapacityMah: Long
        get() = chargeFullDesign / 1000
}
