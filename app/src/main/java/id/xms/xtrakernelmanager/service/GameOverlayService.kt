package id.xms.xtrakernelmanager.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import id.xms.xtrakernelmanager.domain.usecase.GameOverlayUseCase
import kotlinx.coroutines.*

class GameOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private var params: WindowManager.LayoutParams? = null
    private var offsetX = 0
    private var offsetY = 0

    // Realtime polling variables
    private val gameOverlayUseCase = GameOverlayUseCase()
    private var pollingJob: Job? = null

    private var cpuUsage by mutableStateOf("Loading...")
    private var gpuUsage by mutableStateOf("Loading...")
    private var fpsValue by mutableStateOf("Loading...")
    private var tempValue by mutableStateOf("Loading...")

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createOverlay()
        startPolling()
    }

    private fun startPolling() {
        pollingJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                cpuUsage = withContext(Dispatchers.IO) {
                    "%.1f%%".format(gameOverlayUseCase.getCPULoad())
                }
                gpuUsage = withContext(Dispatchers.IO) {
                    "%.0f%%".format(gameOverlayUseCase.getGPULoad())
                }
                fpsValue = withContext(Dispatchers.IO) {
                    gameOverlayUseCase.getCurrentFPS().toString()
                }
                tempValue = withContext(Dispatchers.IO) {
                    "%.1f°C".format(gameOverlayUseCase.getTemperature())
                }
                delay(1000) // Update setiap 1 detik
            }
        }
    }

    private fun createOverlay() {
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@GameOverlayService)
            setViewTreeSavedStateRegistryOwner(this@GameOverlayService)
            setContent {
                MaterialTheme {
                    DraggableOverlayCard()
                }
            }
        }
        windowManager.addView(overlayView, params)
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    @Composable
    private fun DraggableOverlayCard() {
        Card(
            modifier = Modifier
                .shadow(8.dp)
                .background(Color(0xCC222222))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val lp = params ?: return@detectDragGestures
                        offsetX += dragAmount.x.toInt()
                        offsetY += dragAmount.y.toInt()
                        lp.x += dragAmount.x.toInt()
                        lp.y += dragAmount.y.toInt()
                        windowManager.updateViewLayout(overlayView, lp)
                    }
                }
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Performance Monitor",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                    Text("CPU: $cpuUsage", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    Text("GPU: $gpuUsage", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    Text("FPS: $fpsValue", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    Text("Temp: $tempValue", color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        pollingJob?.cancel()
        try {
            overlayView?.let { windowManager.removeView(it) }
        } catch (_: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
