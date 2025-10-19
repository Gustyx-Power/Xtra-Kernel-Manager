package id.xms.xtrakernelmanager.data.model

data class ThermalInfo(
    val mode: String = "Not Set",
    val zones: List<ThermalZone> = emptyList()
)

data class ThermalZone(
    val name: String,
    val temperature: Float,
    val type: String
)
