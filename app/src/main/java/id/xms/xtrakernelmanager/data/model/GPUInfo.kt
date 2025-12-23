package id.xms.xtrakernelmanager.data.model

data class GPUInfo(
    val vendor: String = "Unknown",
    val renderer: String = "Unknown",
    val openglVersion: String = "Unknown",
    val currentFreq: Int = 0,
    val minFreq: Int = 0,
    val maxFreq: Int = 0,
    val availableFreqs: List<Int> = emptyList(),
    val powerLevel: Int = 0,
    val numPwrLevels: Int = 8,
    val rendererType: String = "OpenGL"
)
