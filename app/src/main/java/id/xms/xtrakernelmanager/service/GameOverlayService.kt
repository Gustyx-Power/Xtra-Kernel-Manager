package id.xms.xtrakernelmanager.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.domain.usecase.GameControlUseCase
import id.xms.xtrakernelmanager.domain.usecase.GameOverlayUseCase
import id.xms.xtrakernelmanager.ui.components.gameoverlay.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.*
import id.xms.xtrakernelmanager.ui.screens.misc.GameMonitorViewModel

/**
 * Game Overlay Service - Redesigned
 *
 * Features:
 * - Sidebar navigation with Performance Panel and Game Tools tabs
 * - Quick Apps with floating window capability
 * - Material You / Monet dynamic colors with dark blue fallback
 * - Glassmorphic UI design
 */
class GameOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

  private lateinit var windowManager: WindowManager
  private var overlayView: ComposeView? = null
  private val lifecycleRegistry = LifecycleRegistry(this)
  private val savedStateRegistryController = SavedStateRegistryController.create(this)

  private var params: WindowManager.LayoutParams? = null

  // Use cases
  private val gameOverlayUseCase = GameOverlayUseCase()
  private val gameControlUseCase by lazy { GameControlUseCase(applicationContext) }
  private val preferencesManager by lazy { PreferencesManager(applicationContext) }

  // ViewModel instance
  private lateinit var viewModel: GameMonitorViewModel

  // States
  private var isExpanded by mutableStateOf(false)
  private var isOverlayOnRight by mutableStateOf(true)

  override val lifecycle: Lifecycle
    get() = lifecycleRegistry

  override val savedStateRegistry: SavedStateRegistry
    get() = savedStateRegistryController.savedStateRegistry

  override fun onCreate() {
    super.onCreate()
    savedStateRegistryController.performRestore(null)
    lifecycleRegistry.currentState = Lifecycle.State.CREATED

    windowManager = getSystemService(WINDOW_SERVICE) as WindowManager


    // Check overlay permission
    if (!Settings.canDrawOverlays(this)) {
      showToast("Please grant overlay permission for Game Overlay")
      stopSelf()
      return
    }

    // Initialize ViewModel
    viewModel = GameMonitorViewModel(application, preferencesManager)

    createOverlay()
  }





  private fun showToast(message: String) {
    Handler(Looper.getMainLooper()).post {
      Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
  }

  private fun createOverlay() {
    // Load overlay position preference (default: right)
    isOverlayOnRight = preferencesManager.getBoolean("overlay_position_right", true)

    params =
        WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT,
            )
            .apply {
              gravity = Gravity.TOP or if (isOverlayOnRight) Gravity.END else Gravity.START
              x = 0
              y = 100
            }

    overlayView =
        ComposeView(this).apply {
          setViewTreeLifecycleOwner(this@GameOverlayService)
          setViewTreeSavedStateRegistryOwner(this@GameOverlayService)
          setContent { GameOverlayTheme { GameOverlayContent() } }
        }
    windowManager.addView(overlayView, params)
    lifecycleRegistry.currentState = Lifecycle.State.STARTED
  }



  @Composable
  private fun GameOverlayContent() {
    val context = LocalContext.current
    var isFpsEnabled by remember { mutableStateOf(false) } // This would normally come from ViewModel/Prefs

    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.TopStart
    ) {
        if (isExpanded) {
            GamePanelCard(
                viewModel = viewModel,
                isFpsEnabled = isFpsEnabled,
                onFpsToggle = { isFpsEnabled = !isFpsEnabled },
                onCollapse = { isExpanded = false },
                onMoveSide = { toggleOverlayPosition() }
            )
        } else {
            val fpsVal by viewModel.fpsValue.collectAsState()
            
            GameSidebar(
                isExpanded = isExpanded,
                overlayOnRight = isOverlayOnRight,
                fps = if (isFpsEnabled) fpsVal else null,
                onToggleExpand = { isExpanded = true },
                onDrag = { dx, dy ->
                    params?.let { p ->
                        p.y = (p.y + dy.toInt()).coerceIn(0, 2500)
                        
                        val dragX = dx.toInt()
                        
                        if (isOverlayOnRight) {
                          
                            p.x -= dragX 
                            
                            if (p.x > 500) {
                                toggleOverlayPosition()
                                p.x = 0 // Reset to edge
                            }
                        } else {
                            p.x += dragX
                            
                            if (p.x > 500) {
                                toggleOverlayPosition()
                                p.x = 0 // Reset to edge
                            }
                        }
                        
                        if (p.x < 0) p.x = 0
                        
                        try {
                            windowManager.updateViewLayout(overlayView, p)
                        } catch (e: Exception) {
                        }
                    }
                }
            )
        }
    }
  }



  private fun toggleOverlayPosition() {
    isOverlayOnRight = !isOverlayOnRight
    preferencesManager.setBoolean("overlay_position_right", isOverlayOnRight)

    // Update window layout with new gravity
    params?.let { p ->
      p.gravity = Gravity.TOP or if (isOverlayOnRight) Gravity.END else Gravity.START
      p.x = 0
      try {
        windowManager.updateViewLayout(overlayView, p)
      } catch (e: Exception) {
        // View may not be attached
      }
    }
    showToast(if (isOverlayOnRight) "Posisi: Kanan" else "Posisi: Kiri")
  }



  override fun onDestroy() {
    super.onDestroy()
    lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    try {
      overlayView?.let { windowManager.removeView(it) }
    } catch (_: Exception) {}
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
