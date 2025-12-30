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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.domain.usecase.GameControlUseCase
import id.xms.xtrakernelmanager.domain.usecase.GameOverlayUseCase
import id.xms.xtrakernelmanager.domain.root.RootManager
import id.xms.xtrakernelmanager.R

import id.xms.xtrakernelmanager.ui.components.gameoverlay.*

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

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


    private var pollingJob: Job? = null
    private var gameStartTime: Long = 0L
    private var mediaPlayer: MediaPlayer? = null

    // States
    private var isExpanded by mutableStateOf(false)
    
    // Performance monitoring states
    private var cpuFreq by mutableStateOf("0")
    private var cpuLoad by mutableFloatStateOf(0f)
    private var gpuFreq by mutableStateOf("0")
    private var gpuLoad by mutableFloatStateOf(0f)
    private var fpsValue by mutableStateOf("60")
    private var tempValue by mutableStateOf("0")
    private var gameDuration by mutableStateOf("0:00")
    private var batteryPercentage by mutableIntStateOf(100)
    
    // Control states
    private var currentPerformanceMode by mutableStateOf("balanced")
    private var isClearingRam by mutableStateOf(false)
    
    // Game Tools states
    private var gameToolState by mutableStateOf(GameToolState())
    
    // Esports Mode animation state
    private var showEsportsModeAnimation by mutableStateOf(false)
    
    // Overlay position: true = right (default), false = left
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
        gameStartTime = System.currentTimeMillis()

        // Check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            showToast("Please grant overlay permission for Game Overlay")
            stopSelf()
            return
        }

        loadPreferences()

        createOverlay()
        startPolling()
    }

    private fun loadPreferences() {
        CoroutineScope(Dispatchers.Main).launch {
            // Load DND state
            preferencesManager.isGameControlDNDEnabled().collect { enabled ->
                gameToolState = gameToolState.copy(doNotDisturb = enabled)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            // Load Hide Notifications state
            preferencesManager.isGameControlHideNotifEnabled().collect { enabled ->
                gameToolState = gameToolState.copy(blockNotifications = enabled)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            // Load Esports Mode state
            preferencesManager.isEsportsModeEnabled().collect { enabled ->
                gameToolState = gameToolState.copy(esportsMode = enabled)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            // Load Touch Guard state
            preferencesManager.isTouchGuardEnabled().collect { enabled ->
                gameToolState = gameToolState.copy(touchGuard = enabled)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            // Load Auto Reject Calls state
            preferencesManager.isAutoRejectCallsEnabled().collect { enabled ->
                gameToolState = gameToolState.copy(autoRejectCalls = enabled)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            // Load Lock Brightness state
            preferencesManager.isLockBrightnessEnabled().collect { enabled ->
                gameToolState = gameToolState.copy(lockBrightness = enabled)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            currentPerformanceMode = gameControlUseCase.getCurrentPerformanceMode()
        }
    }



    private fun startPolling() {
        pollingJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                // CPU monitoring
                cpuFreq = withContext(Dispatchers.IO) {
                    val freq = gameOverlayUseCase.getMaxCPUFreq()
                    if (freq >= 1000) "%.2f".format(freq / 1000f) else freq.toString()
                }
                cpuLoad = withContext(Dispatchers.IO) {
                    gameOverlayUseCase.getCPULoad()
                }
                
                // GPU monitoring
                gpuFreq = withContext(Dispatchers.IO) {
                    val freq = gameOverlayUseCase.getGPUFreq()
                    freq.toString()
                }
                gpuLoad = withContext(Dispatchers.IO) {
                    gameOverlayUseCase.getGPULoad()
                }
                
                // FPS
                fpsValue = withContext(Dispatchers.IO) {
                    gameOverlayUseCase.getCurrentFPS().toString()
                }
                
                // Temperature
                tempValue = withContext(Dispatchers.IO) {
                    "%.1f".format(gameOverlayUseCase.getTemperature())
                }
                
                // Game duration
                val durationMs = System.currentTimeMillis() - gameStartTime
                val minutes = (durationMs / 60000).toInt()
                val seconds = ((durationMs % 60000) / 1000).toInt()
                gameDuration = if (minutes >= 60) {
                    "${minutes / 60}h ${minutes % 60}m"
                } else {
                    "$minutes:${"%02d".format(seconds)}"
                }
                
                // Battery
                val batteryIntent = registerReceiver(null, 
                    android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val level = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
                batteryPercentage = if (level >= 0 && scale > 0) {
                    (level * 100 / scale)
                } else 100
                
                delay(1000)
            }
        }
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createOverlay() {
        // Load overlay position preference (default: right)
        isOverlayOnRight = preferencesManager.getBoolean("overlay_position_right", true)
        
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or if (isOverlayOnRight) Gravity.END else Gravity.START
            x = 0
            y = 100
        }

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@GameOverlayService)
            setViewTreeSavedStateRegistryOwner(this@GameOverlayService)
            setContent {
                GameOverlayTheme {
                    GameOverlayContent()
                }
            }
        }
        windowManager.addView(overlayView, params)
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    @Composable
    private fun GameOverlayTheme(content: @Composable () -> Unit) {
        // Use Material You colors on Android 12+, fallback to dark blue theme
        val colorScheme = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(LocalContext.current)
        } else {
            darkColorScheme(
                primary = Color(0xFF5C6BC0),      // Indigo 400
                onPrimary = Color.White,
                primaryContainer = Color(0xFF1A237E), // Indigo 900
                secondary = Color(0xFF7986CB),
                surface = Color(0xFF121212),
                onSurface = Color.White,
                background = Color(0xFF0D0D0D)
            )
        }
        
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }

    @Composable
    private fun GameOverlayContent() {
        val accentColor = MaterialTheme.colorScheme.primary
        
        Box(
            modifier = Modifier.pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val lp = params ?: return@detectDragGestures
                    lp.x -= dragAmount.x.toInt()
                    lp.y += dragAmount.y.toInt()
                    windowManager.updateViewLayout(overlayView, lp)
                }
            }
        ) {
            // Trigger Handle (collapsed state)
            TriggerHandle(
                fpsValue = fpsValue,
                isExpanded = isExpanded,
                onToggleExpanded = { isExpanded = !isExpanded },
                onDrag = { _, dy ->
                    // Update window position when dragging
                    params?.let { p ->
                        p.y = (p.y + dy.toInt()).coerceIn(0, 800)
                        try {
                            windowManager.updateViewLayout(overlayView, p)
                        } catch (e: Exception) {
                            // View may not be attached
                        }
                    }
                },
                accentColor = accentColor
            )
            
            // Expanded Overlay
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f)
            ) {
                PerformancePanel(
                    cpuFreq = "${cpuFreq}GHz",
                    cpuLoad = cpuLoad,
                    gpuFreq = "${gpuFreq}MHz",
                    gpuLoad = gpuLoad,
                    fps = fpsValue,
                    temperature = tempValue,
                    gameDuration = gameDuration,
                    batteryPercentage = batteryPercentage,
                    currentPerformanceMode = currentPerformanceMode,
                    
                    // Game Tools States
                    esportsMode = gameToolState.esportsMode,
                    touchGuard = gameToolState.touchGuard,
                    blockNotifications = gameToolState.blockNotifications,
                    doNotDisturb = gameToolState.doNotDisturb,
                    isClearingRam = isClearingRam,
                    
                    // Actions
                    onPerformanceModeChange = { setPerformanceMode(it) },
                    onEsportsModeChange = { setEsportsMode(it) },
                    onTouchGuardChange = { setTouchGuard(it) },
                    onBlockNotificationsChange = { setBlockNotifications(it) },
                    onDndChange = { setDND(it) },
                    onClearRam = { clearRAM() },
                    onScreenshot = { takeScreenshot() },
                    onToolsClick = { showToast("Alat: Fitur segera hadir!") },
                    onClose = { isExpanded = false },
                    onTogglePosition = { toggleOverlayPosition() },
                    isOverlayOnRight = isOverlayOnRight,
                    
                    accentColor = accentColor
                )
            }
        }
    }
       
    private fun setPerformanceMode(mode: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                gameControlUseCase.setPerformanceMode(mode)
            }
            if (result.isSuccess) {
                currentPerformanceMode = mode
                preferencesManager.setPerfMode(mode)
                val modeLabel = when(mode) {
                    "battery" -> "Hemat Baterai"
                    "balanced" -> "Seimbang"
                    "performance" -> "Monster"
                    else -> mode
                }
                showToast("Mode: $modeLabel")
            }
        }
    }
    
    private fun setDND(enabled: Boolean) {
        if (!gameControlUseCase.hasDNDPermission()) {
            showToast("Please grant DND access in Settings")
            try {
                val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                applicationContext.startActivity(intent)
            } catch (e: Exception) {
                showToast("Could not open Settings")
            }
            return
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                if (enabled) gameControlUseCase.enableDND()
                else gameControlUseCase.disableDND()
            }
            result.onSuccess {
                gameToolState = gameToolState.copy(doNotDisturb = enabled)
                preferencesManager.setGameControlDND(enabled)
                showToast(if (enabled) "Jangan Ganggu: ON" else "Jangan Ganggu: OFF")
            }.onFailure { error ->
                showToast("DND error: ${error.message}")
            }
        }
    }
    
    private fun setBlockNotifications(enabled: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            gameToolState = gameToolState.copy(blockNotifications = enabled)
            preferencesManager.setGameControlHideNotif(enabled)
            
            // Actually hide notifications via root
            withContext(Dispatchers.IO) {
                gameControlUseCase.hideNotifications(enabled)
            }
            showToast(if (enabled) "Blokir Notifikasi: ON" else "Blokir Notifikasi: OFF")
        }
    }
    
    private fun setEsportsMode(enabled: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            gameToolState = gameToolState.copy(esportsMode = enabled)
            preferencesManager.setEsportsMode(enabled)
            
            if (enabled) {
                // Close overlay first
                isExpanded = false
                
                // Play vroom sound effect
                try {
                    mediaPlayer?.release()
                    mediaPlayer = MediaPlayer.create(this@GameOverlayService, R.raw.vroom)
                    mediaPlayer?.setOnCompletionListener { mp -> mp.release() }
                    mediaPlayer?.start()
                } catch (e: Exception) {
                    // Ignore sound errors
                }
                
                // Show fullscreen animation overlay
                showFullscreenEsportsAnimation()
                
                // Enable Monster Mode: max clocks
                withContext(Dispatchers.IO) {
                    gameControlUseCase.enableMonsterMode()
                }
            } else {
                // Disable Monster Mode: restore defaults
                withContext(Dispatchers.IO) {
                    gameControlUseCase.disableMonsterMode()
                }
                showToast("Mode Monster: OFF")
            }
        }
    }
    
    private fun showFullscreenEsportsAnimation() {
        // Create fullscreen overlay params
        val fullscreenParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        
        val animationView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@GameOverlayService)
            setViewTreeSavedStateRegistryOwner(this@GameOverlayService)
            setContent {
                EsportsActivationAnimation(
                    onAnimationComplete = {
                        // Will be removed after animation
                    }
                )
            }
        }
        
        // Add fullscreen animation view
        try {
            windowManager.addView(animationView, fullscreenParams)
        } catch (e: Exception) {
            return
        }
        
        // Remove after 4.5 seconds (matches animation duration + buffer)
        CoroutineScope(Dispatchers.Main).launch {
            delay(5500)
            try {
                windowManager.removeView(animationView)
            } catch (e: Exception) {
            }
        }
    }
    
    private fun setTouchGuard(enabled: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            gameToolState = gameToolState.copy(touchGuard = enabled)
            preferencesManager.setTouchGuard(enabled)
            showToast(if (enabled) "Pencegah Salah Sentuh: ON" else "Pencegah Salah Sentuh: OFF")
        }
    }
    
    private fun setAutoRejectCalls(enabled: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            gameToolState = gameToolState.copy(autoRejectCalls = enabled)
            preferencesManager.setAutoRejectCalls(enabled)
            showToast(if (enabled) "Tolak Panggilan: ON" else "Tolak Panggilan: OFF")
        }
    }
    
    private fun setLockBrightness(enabled: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            gameToolState = gameToolState.copy(lockBrightness = enabled)
            preferencesManager.setLockBrightness(enabled)
            showToast(if (enabled) "Kunci Kecerahan: ON" else "Kunci Kecerahan: OFF")
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
    
    private fun clearRAM() {
        if (isClearingRam) return
        
        CoroutineScope(Dispatchers.Main).launch {
            isClearingRam = true
            val result = withContext(Dispatchers.IO) {
                gameControlUseCase.clearRAM()
            }
            isClearingRam = false
            
            result.onSuccess { clearResult ->
                val message = if (clearResult.freedMB > 0) {
                    "Dibersihkan ${clearResult.freedMB} MB RAM"
                } else {
                    "RAM sudah optimal (${clearResult.availableMB} MB free)"
                }
                showToast(message)
            }.onFailure {
                showToast("Gagal membersihkan RAM")
            }
        }
    }
    
    private fun takeScreenshot() {
        CoroutineScope(Dispatchers.Main).launch {
            // Hide overlay briefly for clean screenshot
            val wasExpanded = isExpanded
            isExpanded = false
            delay(300)
            
            // Use screencap with timestamp filename
            val timestamp = System.currentTimeMillis()
            val filename = "/sdcard/Pictures/Screenshot_$timestamp.png"
            
            withContext(Dispatchers.IO) {
                // Take screenshot using screencap
                RootManager.executeCommand("screencap -p $filename")
                // Trigger media scan so it appears in gallery
                RootManager.executeCommand("am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file://$filename")
            }
            
            showToast("Screenshot disimpan: Pictures/Screenshot_$timestamp.png")
            
            // Restore overlay state after screenshot
            delay(300)
            isExpanded = wasExpanded
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


