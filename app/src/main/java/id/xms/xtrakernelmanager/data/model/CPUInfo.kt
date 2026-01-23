package id.xms.xtrakernelmanager.data.model

data class CPUInfo(
    val cores: List<CoreInfo> = emptyList(),
    val clusters: List<ClusterInfo> = emptyList(),
    val temperature: Float = 0f,
    val totalLoad: Float = 0f,
)

data class CoreInfo(
    val coreNumber: Int,
    val currentFreq: Int,
    val minFreq: Int,
    val maxFreq: Int,
    val governor: String,
    val isOnline: Boolean,
    val cluster: Int,
)

data class ClusterInfo(
    val clusterNumber: Int,
    val cores: List<Int>,
    val minFreq: Int,
    val maxFreq: Int,
    val currentMinFreq: Int,
    val currentMaxFreq: Int,
    val governor: String,
    val availableGovernors: List<String>,
    val availableFrequencies: List<Int> = emptyList(),
    val policyPath: String,
)
