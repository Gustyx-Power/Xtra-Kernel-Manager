package id.xms.xtrakernelmanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class HourlyStats(
    val date: String, // Format: YYYY-MM-DD
    val buckets: List<HourBucket> = List(24) { HourBucket(it) },
)

@Serializable
data class HourBucket(
    val hour: Int, // 0-23
    var screenOnMs: Long = 0L,
    var drainPercent: Int = 0,
)
