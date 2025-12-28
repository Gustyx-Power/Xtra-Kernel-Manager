package id.xms.xtrakernelmanager.data.model

data class BatteryInfo(
    val level: Int = 0,
    val temperature: Float = 0f,
    val health: String = "Unknown",
    val currentCapacity: Int = 0, // mAh
    val totalCapacity: Int = 0, // mAh (Design)
    val currentNow: Int = 0, // mA
    val cycleCount: Int = 0,
    val voltage: Int = 0, // mV
    val status: String = "Unknown",
    val technology: String = "Unknown",
    val healthPercent: Float = 100f, // Kesehatan presentase
)
