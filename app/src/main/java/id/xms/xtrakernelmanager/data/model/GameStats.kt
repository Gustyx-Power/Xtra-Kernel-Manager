package id.xms.xtrakernelmanager.data.model

data class GameStats(
    val timestamp: Long,
    val fps: Int,
    val cpuFreq: Int,
    val cpuLoad: Float,
    val gpuLoad: Float,
    val temperature: Float,
)

data class GameSession(
    val startTime: Long,
    val endTime: Long,
    val stats: List<GameStats>,
    val averageFps: Float,
    val averageCpuLoad: Float,
    val averageGpuLoad: Float,
    val averageTemp: Float,
    val minFps: Int,
    val maxFps: Int,
)
