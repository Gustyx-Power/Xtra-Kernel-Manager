package id.xms.xtrakernelmanager.data.repository

import id.xms.xtrakernelmanager.data.model.GameSession
import id.xms.xtrakernelmanager.data.model.GameStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameStatsRepository {

    private val sessionStats = mutableListOf<GameStats>()

    suspend fun addStat(stat: GameStats) = withContext(Dispatchers.IO) {
        sessionStats.add(stat)
    }

    suspend fun endSession(): GameSession = withContext(Dispatchers.IO) {
        if (sessionStats.isEmpty()) {
            return@withContext GameSession(
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                stats = emptyList(),
                averageFps = 0f,
                averageCpuLoad = 0f,
                averageGpuLoad = 0f,
                averageTemp = 0f,
                minFps = 0,
                maxFps = 0
            )
        }

        val session = GameSession(
            startTime = sessionStats.first().timestamp,
            endTime = sessionStats.last().timestamp,
            stats = sessionStats.toList(),
            averageFps = sessionStats.map { it.fps }.average().toFloat(),
            averageCpuLoad = sessionStats.map { it.cpuLoad }.average().toFloat(),
            averageGpuLoad = sessionStats.map { it.gpuLoad }.average().toFloat(),
            averageTemp = sessionStats.map { it.temperature }.average().toFloat(),
            minFps = sessionStats.minOfOrNull { it.fps } ?: 0,
            maxFps = sessionStats.maxOfOrNull { it.fps } ?: 0
        )

        sessionStats.clear()
        session
    }
}
