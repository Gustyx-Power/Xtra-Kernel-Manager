package id.xms.xtrakernelmanager.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.domain.usecase.GameControlUseCase
import id.xms.xtrakernelmanager.domain.usecase.GameOverlayUseCase
import id.xms.xtrakernelmanager.ui.components.gameoverlay.*
import androidx.lifecycle.lifecycleScope
import id.xms.xtrakernelmanager.ui.screens.misc.components.GameMonitorViewModel
import id.xms.xtrakernelmanager.ui.theme.XtraKernelManagerTheme
import kotlinx.coroutines.*

class GameOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

  companion object {
    private const val TAG = "GameOverlayService"
    private const val CHANNEL_ID = "game_overlay_channel"
    private const val NOTIFICATION_ID = 2003
  }

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
  private var isDockedToEdge by mutableStateOf(true)
  private var screenWidth = 0

  override val lifecycle: Lifecycle
    get() = lifecycleRegistry

  override val savedStateRegistry: SavedStateRegistry
    get() = savedStateRegistryController.savedStateRegistry

  override fun onCreate() {
    super.onCreate()
    savedStateRegistryController.performRestore(null)
    lifecycleRegistry.currentState = Lifecycle.State.CREATED

    windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

    // Create notification channel
    createNotificationChannel()

    // Start as foreground service with notification
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      startForeground(
          NOTIFICATION_ID,
          createNotification(),
          ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
      )
    } else {
      startForeground(NOTIFICATION_ID, createNotification())
    }

    // Check overlay permission
    if (!Settings.canDrawOverlays(this)) {
      showToast("Please grant overlay permission for Game Overlay")
      stopSelf()
      return
    }

    // Mark overlay as running
    preferencesManager.setBoolean("game_overlay_running", true)

    // Initialize ViewModel
    viewModel = GameMonitorViewModel(application, preferencesManager)

    // Observe Screenshot Trigger
    lifecycleScope.launch {
        viewModel.screenshotTrigger.collect {
            performHiddenScreenshot()
        }
    }

    // Observe Esports Animation Trigger
    lifecycleScope.launch {
        viewModel.esportsAnimationTrigger.collect {
            showEsportsAnimation()
        }
    }

    // Observe Toast Messages
    lifecycleScope.launch {
        viewModel.toastMessage.collect { message ->
            showToast(message)
        }
    }

    createOverlay()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel =
          NotificationChannel(
                  CHANNEL_ID,
                  "Game Overlay",
                  NotificationManager.IMPORTANCE_LOW,
              )
              .apply {
                description = "Shows game overlay controls during gameplay"
                setShowBadge(false)
              }
      val notificationManager = getSystemService(NotificationManager::class.java)
      notificationManager.createNotificationChannel(channel)
    }
  }

  private fun createNotification(): Notification {
    return NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Game Overlay Active")
        .setContentText("Overlay controls available during gameplay")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build()
  }

  private fun performHiddenScreenshot() {
      lifecycleScope.launch(Dispatchers.Main) {
          try {
              overlayView?.visibility = View.INVISIBLE
              
              delay(150)
              
              val result = withContext(Dispatchers.IO) {
                  gameControlUseCase.takeScreenshot()
              }
              
              delay(500)
              
              overlayView?.visibility = View.VISIBLE
              
              if (result.isSuccess) {
                  showToast("Screenshot captured!\nSaved to Pictures/Screenshots/")
              } else {
                  val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                  showToast("Screenshot failed!\n$errorMsg")
              }
              
          } catch (e: Exception) {
              overlayView?.visibility = View.VISIBLE
              showToast("Screenshot error: ${e.message}")
              Log.e("GameOverlayService", "Screenshot error", e)
          }
      }
  }

  private fun showEsportsAnimation() {
      lifecycleScope.launch(Dispatchers.Main) {
          try {
              val animationView = ComposeView(this@GameOverlayService).apply {
                  setViewTreeLifecycleOwner(this@GameOverlayService)
                  setViewTreeSavedStateRegistryOwner(this@GameOverlayService)
                  
                  setContent {
                      MaterialTheme(
                          colorScheme = darkColorScheme(
                              primary = Color(0xFF5C6BC0),
                              secondary = Color(0xFF7986CB),
                              surface = Color(0xFF1E1E1E),
                              background = Color(0xFF121212),
                          )
                      ) {
                          EsportsActivationAnimation(
                              modifier = Modifier.fillMaxSize(),
                              onAnimationComplete = {
                                  lifecycleScope.launch {
                                      delay(500)
                                      try {
                                          windowManager.removeView(this@apply)
                                      } catch (e: Exception) {
                                          Log.e("GameOverlayService", "Error removing animation view", e)
                                      }
                                  }
                              }
                          )
                      }
                  }
              }

              val animationParams = WindowManager.LayoutParams(
                  WindowManager.LayoutParams.MATCH_PARENT,
                  WindowManager.LayoutParams.MATCH_PARENT,
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                  } else {
                      @Suppress("DEPRECATION")
                      WindowManager.LayoutParams.TYPE_PHONE
                  },
                  WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                          WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                          WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                          WindowManager.LayoutParams.FLAG_FULLSCREEN,
                  PixelFormat.TRANSLUCENT
              ).apply {
                  gravity = Gravity.CENTER
              }

              windowManager.addView(animationView, animationParams)
              
              Log.d("GameOverlayService", "Esports animation started successfully")
              
          } catch (e: Exception) {
              Log.e("GameOverlayService", "Error showing esports animation", e)
          }
      }
  }

  private fun showToast(message: String) {
    Handler(Looper.getMainLooper()).post {
      Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
  }

  private var dragAccumulatorX = 0f
  private var dragAccumulatorY = 0f

  private fun createOverlay() {
    val metrics = windowManager.maximumWindowMetrics
    screenWidth = metrics.bounds.width()

    isOverlayOnRight = preferencesManager.getBoolean("overlay_position_right", true)
    val savedY = preferencesManager.getInt("overlay_y_pos", 100)

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
              y = savedY
            }

    overlayView =
        ComposeView(this).apply {
          setViewTreeLifecycleOwner(this@GameOverlayService)
          setViewTreeSavedStateRegistryOwner(this@GameOverlayService)
          setContent { 
            val layoutStyle by preferencesManager.getLayoutStyle().collectAsState(initial = "liquid")
            if (layoutStyle == "liquid") {
              LiquidGameOverlayTheme { GameOverlayContent() }
            } else {
              GameOverlayTheme { GameOverlayContent() }
            }
          }
        }
    windowManager.addView(overlayView, params)
    lifecycleRegistry.currentState = Lifecycle.State.STARTED
  }

  @Composable
  private fun GameOverlayContent() {
    val context = LocalContext.current
    val isFpsEnabled by viewModel.isFpsEnabled.collectAsState()
    val layoutStyle by preferencesManager.getLayoutStyle().collectAsState(initial = "liquid")
    val isLiquidUI = layoutStyle == "liquid"

    Box(modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.TopStart) {
      if (isExpanded) {
        if (isLiquidUI) {
          // Liquid UI - Glassmorphism Light Mode
          LiquidGamePanelCard(
              viewModel = viewModel,
              isFpsEnabled = isFpsEnabled,
              onFpsToggle = { viewModel.setFpsEnabled(!isFpsEnabled) },
              onCollapse = { isExpanded = false },
              onMoveSide = { toggleOverlayPosition() },
              onDrag = { dx, dy ->
                params?.let { p ->
                  // Accumulate drag deltas
                  dragAccumulatorX += dx
                  dragAccumulatorY += dy
                  
                  val moveY = dragAccumulatorY.toInt()
                  val moveX = dragAccumulatorX.toInt()
                  
                  if (moveY != 0) {
                     p.y = (p.y + moveY).coerceIn(0, 2500)
                     dragAccumulatorY -= moveY // Keep remainder
                  }

                  if (moveX != 0) {
                      val actualMoveX = if (isOverlayOnRight) -moveX else moveX
                      p.x = (p.x + actualMoveX).coerceIn(0, screenWidth / 2)
                      dragAccumulatorX -= moveX // Keep remainder
                  }
                  
                  val snapThreshold = 60
                  isDockedToEdge = p.x < snapThreshold
                  
                  if (p.x > screenWidth / 3) {
                      toggleOverlayPosition()
                      p.x = 0
                      isDockedToEdge = true
                      dragAccumulatorX = 0f // Reset
                  }

                  try { windowManager.updateViewLayout(overlayView, p) } catch (e: Exception) {}
                }
              },
              onDragEnd = {
                  params?.let { p ->
                      preferencesManager.setInt("overlay_y_pos", p.y)
                  }
              },
          )
        } else {
          // Material UI - Dark Mode
          GamePanelCard(
              viewModel = viewModel,
              isFpsEnabled = isFpsEnabled,
              onFpsToggle = { viewModel.setFpsEnabled(!isFpsEnabled) },
              onCollapse = { isExpanded = false },
              onMoveSide = { toggleOverlayPosition() },
              onDrag = { dx, dy ->
                params?.let { p ->
                  // Accumulate drag deltas
                  dragAccumulatorX += dx
                  dragAccumulatorY += dy
                  
                  val moveY = dragAccumulatorY.toInt()
                  val moveX = dragAccumulatorX.toInt()
                  
                  if (moveY != 0) {
                     p.y = (p.y + moveY).coerceIn(0, 2500)
                     dragAccumulatorY -= moveY // Keep remainder
                  }

                  if (moveX != 0) {
                      val actualMoveX = if (isOverlayOnRight) -moveX else moveX
                      p.x = (p.x + actualMoveX).coerceIn(0, screenWidth / 2)
                      dragAccumulatorX -= moveX // Keep remainder
                  }
                  
                  val snapThreshold = 60
                  isDockedToEdge = p.x < snapThreshold
                  
                  if (p.x > screenWidth / 3) {
                      toggleOverlayPosition()
                      p.x = 0
                      isDockedToEdge = true
                      dragAccumulatorX = 0f // Reset
                  }

                  try { windowManager.updateViewLayout(overlayView, p) } catch (e: Exception) {}
                }
              },
              onDragEnd = {
                  params?.let { p ->
                      preferencesManager.setInt("overlay_y_pos", p.y)
                  }
              },
          )
        }
      } else {
        val fpsVal by viewModel.fpsValue.collectAsState()

        if (isLiquidUI) {
          // Liquid UI - Glassmorphism Light Mode
          LiquidGameSidebar(
              isExpanded = isExpanded,
              overlayOnRight = isOverlayOnRight,
              isDockedToEdge = isDockedToEdge,
              fps = if (isFpsEnabled) fpsVal else null,
              onToggleExpand = { isExpanded = true },
              onDrag = { dx, dy ->
                params?.let { p ->
                  // Accumulate drag deltas
                  dragAccumulatorX += dx
                  dragAccumulatorY += dy
                  
                  val moveY = dragAccumulatorY.toInt()
                  val moveX = dragAccumulatorX.toInt()
                  
                  if (moveY != 0) {
                      p.y = (p.y + moveY).coerceIn(0, 2500)
                      dragAccumulatorY -= moveY
                  }

                  if (moveX != 0) {
                      val actualMoveX = if (isOverlayOnRight) -moveX else moveX
                      p.x = (p.x + actualMoveX).coerceIn(0, screenWidth / 2)
                      dragAccumulatorX -= moveX
                  }
                  
                  // Auto-snap threshold: 60px from edge
                  val snapThreshold = 60
                  isDockedToEdge = p.x < snapThreshold
                  
                  // If dragged far enough, switch sides
                  if (p.x > screenWidth / 3) {
                      toggleOverlayPosition()
                      p.x = 0
                      isDockedToEdge = true
                      dragAccumulatorX = 0f
                  }

                  try {
                    windowManager.updateViewLayout(overlayView, p)
                  } catch (e: Exception) {}
                }
              },
              onDragEnd = {
                  params?.let { p ->
                      // Auto-snap to edge if close
                      val snapThreshold = 100
                      if (p.x < snapThreshold) {
                          p.x = 0
                          isDockedToEdge = true
                          try { windowManager.updateViewLayout(overlayView, p) } catch (e: Exception) {}
                      }
                      preferencesManager.setInt("overlay_y_pos", p.y)
                  }
              }
          )
        } else {
          // Material UI - Dark Mode
          GameSidebar(
              isExpanded = isExpanded,
              overlayOnRight = isOverlayOnRight,
              isDockedToEdge = isDockedToEdge,
              fps = if (isFpsEnabled) fpsVal else null,
              onToggleExpand = { isExpanded = true },
              onDrag = { dx, dy ->
                params?.let { p ->
                  // Accumulate drag deltas
                  dragAccumulatorX += dx
                  dragAccumulatorY += dy
                  
                  val moveY = dragAccumulatorY.toInt()
                  val moveX = dragAccumulatorX.toInt()
                  
                  if (moveY != 0) {
                      p.y = (p.y + moveY).coerceIn(0, 2500)
                      dragAccumulatorY -= moveY
                  }

                  if (moveX != 0) {
                      val actualMoveX = if (isOverlayOnRight) -moveX else moveX
                      p.x = (p.x + actualMoveX).coerceIn(0, screenWidth / 2)
                      dragAccumulatorX -= moveX
                  }
                  
                  // Auto-snap threshold: 60px from edge
                  val snapThreshold = 60
                  isDockedToEdge = p.x < snapThreshold
                  
                  // If dragged far enough, switch sides
                  if (p.x > screenWidth / 3) {
                      toggleOverlayPosition()
                      p.x = 0
                      isDockedToEdge = true
                      dragAccumulatorX = 0f
                  }

                  try {
                    windowManager.updateViewLayout(overlayView, p)
                  } catch (e: Exception) {}
                }
              },
              onDragEnd = {
                  params?.let { p ->
                      // Auto-snap to edge if close
                      val snapThreshold = 100
                      if (p.x < snapThreshold) {
                          p.x = 0
                          isDockedToEdge = true
                          try { windowManager.updateViewLayout(overlayView, p) } catch (e: Exception) {}
                      }
                      preferencesManager.setInt("overlay_y_pos", p.y)
                  }
              }
          )
        }
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
  }

  override fun onDestroy() {
    super.onDestroy()
    lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    
    // Mark overlay as not running
    preferencesManager.setBoolean("game_overlay_running", false)
    
    try {
      overlayView?.let { windowManager.removeView(it) }
    } catch (_: Exception) {}
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
