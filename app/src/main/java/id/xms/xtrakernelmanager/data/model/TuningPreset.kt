package id.xms.xtrakernelmanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TuningPreset(
    val name: String,
    val cpuConfig: CpuConfig? = null,
    val gpuConfig: GpuConfig? = null,
    val ramConfig: RamConfig? = null,
    val additionalConfig: AdditionalConfig? = null
)

@Serializable
data class CpuConfig(
    val coreConfigs: Map<Int, CoreConfig> = emptyMap(),
    val clusterConfigs: Map<String, ClusterConfig> = emptyMap()
)

@Serializable
data class CoreConfig(
    val minFreq: Long,
    val maxFreq: Long,
    val governor: String,
    val online: Boolean = true
)

@Serializable
data class ClusterConfig(
    val minFreq: Long,
    val maxFreq: Long,
    val governor: String
)

@Serializable
data class GpuConfig(
    val minFreq: Long,
    val maxFreq: Long,
    val powerLevel: Int,
    val renderer: String = "default"
)

@Serializable
data class RamConfig(
    val swappiness: Int,
    val zramSize: Long,
    val dirtyRatio: Int,
    val minFreeKbytes: Int
)

@Serializable
data class AdditionalConfig(
    val ioScheduler: String,
    val tcpCongestion: String,
    val thermalMode: String
)
