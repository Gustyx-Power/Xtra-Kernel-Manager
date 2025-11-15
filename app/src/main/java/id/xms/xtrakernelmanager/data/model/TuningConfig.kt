package id.xms.xtrakernelmanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TuningConfig(
    val cpuClusters: List<CPUClusterConfig> = emptyList(),
    val gpu: GPUConfig? = null,
    val thermal: ThermalConfig = ThermalConfig(),
    val ram: RAMConfig = RAMConfig(),
    val additional: AdditionalConfig = AdditionalConfig()
)

@Serializable
data class CPUClusterConfig(
    val cluster: Int,
    val minFreq: Int,
    val maxFreq: Int,
    val governor: String,
    val disabledCores: List<Int> = emptyList()
)

@Serializable
data class GPUConfig(
    val minFreq: Int,
    val maxFreq: Int,
    val powerLevel: Int,
    val renderer: String
)

@Serializable
data class ThermalConfig(
    val preset: String = "Not Set",
    val setOnBoot: Boolean = false
)

@Serializable
data class RAMConfig(
    val swappiness: Int = 60,
    val zramSize: Int = 0,
    val swapSize: Int = 0,
    val dirtyRatio: Int = 20,
    val minFreeMem: Int = 0
)

@Serializable
data class AdditionalConfig(
    val ioScheduler: String = "",
    val tcpCongestion: String = "",
    val perfMode: String = "balance"
)
