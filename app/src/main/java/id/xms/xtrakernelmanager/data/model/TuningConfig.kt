package id.xms.xtrakernelmanager.data.model

@Serializable
data class TuningConfig(
    val cpuClusters: List<CPUClusterConfig> = emptyList(),
    val gpu: GPUConfig? = null,
    val thermal: ThermalConfig = ThermalConfig(),
    val ram: RAMConfig = RAMConfig(),
    val additional: AdditionalConfig = AdditionalConfig(),
    val cpuSetOnBoot: Boolean = false,
)

annotation class Serializable

@Serializable
data class CPUClusterConfig(
    val cluster: Int,
    val minFreq: Int,
    val maxFreq: Int,
    val governor: String,
    val disabledCores: List<Int> = emptyList(),
    val setOnBoot: Boolean = false,
)

@Serializable
data class GPUConfig(val minFreq: Int, val maxFreq: Int, val powerLevel: Int, val renderer: String)

@Serializable
data class ThermalConfig(val preset: String = "Not Set", val setOnBoot: Boolean = false)

@Serializable
data class RAMConfig(
    val swappiness: Int = 60, // /proc/sys/vm/swappiness
    val zramSize: Int = 0, // MB
    val swapSize: Int = 0, // MB (0 = disable, max 16384)
    val dirtyRatio: Int = 20, // /proc/sys/vm/dirty_ratio
    val minFreeMem: Int = 0, // /proc/sys/vm/min_free_kbytes
    val compressionAlgorithm: String = "lz4", // ZRAM compression algorithm
    val setOnBoot: Boolean = false,
)

@Serializable
data class AdditionalConfig(
    val ioScheduler: String = "",
    val tcpCongestion: String = "",
    val perfMode: String = "balance",
    val setOnBoot: Boolean = false,
)
