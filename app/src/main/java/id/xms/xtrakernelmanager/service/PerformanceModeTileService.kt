package id.xms.xtrakernelmanager.service

import android.content.ComponentName
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.domain.usecase.GameControlUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Quick Settings Tile for Performance Mode control.
 * Allows cycling through performance modes from the notification shade:
 * balanced → performance → battery → balanced
 */
class PerformanceModeTileService : TileService() {

    companion object {
        private const val TAG = "PerfModeTile"
        
        // Mode cycle order
        private val MODE_CYCLE = listOf("balanced", "performance", "battery")
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val gameControlUseCase by lazy { GameControlUseCase(applicationContext) }
    private val preferencesManager by lazy { PreferencesManager(applicationContext) }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        Log.d(TAG, "Tile clicked")
        
        // Cycle to next mode
        serviceScope.launch {
            try {
                val currentMode = getCurrentMode()
                val nextMode = getNextMode(currentMode)
                
                Log.d(TAG, "Changing mode: $currentMode → $nextMode")
                
                val result = gameControlUseCase.setPerformanceMode(nextMode)
                if (result.isSuccess) {
                    preferencesManager.setPerfMode(nextMode)
                    Log.d(TAG, "Mode set successfully to: $nextMode")
                } else {
                    Log.e(TAG, "Failed to set mode: ${result.exceptionOrNull()?.message}")
                }
                
                updateTile()
            } catch (e: Exception) {
                Log.e(TAG, "Error changing mode", e)
            }
        }
    }

    override fun onTileAdded() {
        super.onTileAdded()
        Log.d(TAG, "Tile added")
        updateTile()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        Log.d(TAG, "Tile removed")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        
        serviceScope.launch {
            try {
                val currentMode = getCurrentMode()
                
                tile.state = Tile.STATE_ACTIVE
                tile.label = getModeLabel(currentMode)
                tile.subtitle = getModeSubtitle(currentMode)
                tile.contentDescription = "Performance Mode: ${getModeLabel(currentMode)}"
                
                // Set icon based on mode
                tile.icon = Icon.createWithResource(applicationContext, getModeIcon(currentMode))
                
                tile.updateTile()
                Log.d(TAG, "Tile updated: $currentMode")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating tile", e)
            }
        }
    }

    private suspend fun getCurrentMode(): String {
        return try {
            // First try to get from preferences
            val savedMode = preferencesManager.getPerfMode().first()
            if (savedMode.isNotBlank()) {
                savedMode
            } else {
                // Fallback to reading from system
                gameControlUseCase.getCurrentPerformanceMode()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current mode", e)
            "balanced"
        }
    }

    private fun getNextMode(currentMode: String): String {
        val currentIndex = MODE_CYCLE.indexOf(currentMode)
        val nextIndex = if (currentIndex == -1) 0 else (currentIndex + 1) % MODE_CYCLE.size
        return MODE_CYCLE[nextIndex]
    }

    private fun getModeLabel(mode: String): String {
        return when (mode) {
            "performance" -> getString(R.string.tile_perf_performance)
            "battery" -> getString(R.string.tile_perf_battery)
            "balanced" -> getString(R.string.tile_perf_balanced)
            else -> getString(R.string.tile_perf_balanced)
        }
    }

    private fun getModeSubtitle(mode: String): String {
        return when (mode) {
            "performance" -> getString(R.string.tile_perf_performance_sub)
            "battery" -> getString(R.string.tile_perf_battery_sub)
            "balanced" -> getString(R.string.tile_perf_balanced_sub)
            else -> getString(R.string.tile_perf_balanced_sub)
        }
    }

    private fun getModeIcon(mode: String): Int {
        return when (mode) {
            "performance" -> R.drawable.ic_tile_performance
            "battery" -> R.drawable.ic_tile_battery
            "balanced" -> R.drawable.ic_tile_balanced
            else -> R.drawable.ic_tile_balanced
        }
    }
}
