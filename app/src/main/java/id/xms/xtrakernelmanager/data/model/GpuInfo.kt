package id.xms.xtrakernelmanager.data.model

data class GpuInfo(
    val currentFreq: Long = 0,
    val minFreq: Long = 0,
    val maxFreq: Long = 0,
    val availableFreqs: List<Long> = emptyList(),
    val governor: String = "",
    val availableGovernors: List<String> = emptyList(),
    val powerLevel: Int = 0,
    val renderer: String = "",
    val vendor: String = "",
    val openGLVersion: String = "",
    val vulkanVersion: String = "",
    val vulkanSupported: Boolean = false
)
