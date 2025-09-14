package id.xms.xtrakernelmanager.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class RealtimeGpuInfo(
    val usagePercentage: Float? = null,  // GPU usage percentage
    val currentFreq: Int = 0,            // Current frequency in MHz
    val maxFreq: Int = 0,                // Maximum frequency in MHz
    val model: String = "N/A"            // GPU model name
)