package id.xms.xtrakernelmanager.data.model

data class CpuInfo(
    val coreCount: Int = 0,
    val onlineCores: List<Int> = emptyList(),
    val cores: List<CpuCore> = emptyList(),
    val clusters: Map<String, List<Int>> = emptyMap(),
    val load: Float = 0f,
    val temperature: Float = 0f
)

data class CpuCore(
    val coreNumber: Int,
    val online: Boolean,
    val currentFreq: Long,
    val minFreq: Long,
    val maxFreq: Long,
    val governor: String,
    val availableGovernors: List<String>,
    val availableFrequencies: List<Long>,
    val cluster: String = "Unknown",
    val clusterType: String = "Unknown"
)
