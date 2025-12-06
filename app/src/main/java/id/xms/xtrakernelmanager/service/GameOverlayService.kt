package id.xms.xtrakernelmanager.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.*

class GameOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private var params: WindowManager.LayoutParams? = null
    private var offsetX = 0
    private var offsetY = 0

    // Use cases
    private val gameOverlayUseCase = GameOverlayUseCase()
    private val gameControlUseCase by lazy { GameControlUseCase(applicationContext) }
    private val preferencesManager by lazy { PreferencesManager(applicationContext) }

    private var pollingJob: Job? = null

    // FPS monitoring states
    private var cpuUsage by mutableStateOf("0")
    private var gpuUsage by mutableStateOf("0")
    private var fpsValue by mutableStateOf("60")
    private var tempValue by mutableStateOf("0")

    // Control panel states
    private var isExpanded by mutableStateOf(false)
    private var currentPerformanceMode by mutableStateOf("balanced")
    private var isDndEnabled by mutableStateOf(false)
    private var isHideNotif by mutableStateOf(false)
    private var isClearingRam by mutableStateOf(false)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        loadPreferences()
        createOverlay()
        startPolling()
    }

    private fun loadPreferences() {
        CoroutineScope(Dispatchers.Main).launch {
            preferencesManager.isGameControlDNDEnabled().collect { isDndEnabled = it }
        }
        CoroutineScope(Dispatchers.Main).launch {
            preferencesManager.isGameControlHideNotifEnabled().collect { isHideNotif = it }
        }
        CoroutineScope(Dispatchers.Main).launch {
            currentPerformanceMode = gameControlUseCase.getCurrentPerformanceMode()
        }
    }

    private fun startPolling() {
        pollingJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                cpuUsage = withContext(Dispatchers.IO) {
                    "%.0f".format(gameOverlayUseCase.getCPULoad())
                }
                gpuUsage = withContext(Dispatchers.IO) {
                    "%.0f".format(gameOverlayUseCase.getGPULoad())
                }
                fpsValue = withContext(Dispatchers.IO) {
                    gameOverlayUseCase.getCurrentFPS().toString()
                }
                tempValue = withContext(Dispatchers.IO) {
                    "%.1f".format(gameOverlayUseCase.getTemperature())
                }
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
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 20
            y = 100
        }

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@GameOverlayService)
            setViewTreeSavedStateRegistryOwner(this@GameOverlayService)
            setContent {
                MaterialTheme {
                    GameOverlayContent()
                }
            }
        }
        windowManager.addView(overlayView, params)
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    @Composable
    private fun GameOverlayContent() {
        Column(
            modifier = Modifier.pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val lp = params ?: return@detectDragGestures
                    offsetX += dragAmount.x.toInt()
                    offsetY += dragAmount.y.toInt()
                    lp.x -= dragAmount.x.toInt()
                    lp.y += dragAmount.y.toInt()
                    windowManager.updateViewLayout(overlayView, lp)
                }
            }
        ) {
            AnimatedVisibility(
                visible = !isExpanded,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                CompactFPSView()
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ExpandedControlPanel()
            }
        }
    }

    @Composable
    private fun CompactFPSView() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { isExpanded = !isExpanded }
        ) {
            // Game icon button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(6.dp, CircleShape)
                    .background(Color(0xFF1F1F1F), CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(4.dp))

            // FPS display
            Box(
                modifier = Modifier
                    .shadow(6.dp, RoundedCornerShape(8.dp))
                    .background(Color(0xFF1F1F1F), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = fpsValue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    @Composable
    private fun ExpandedControlPanel() {
        val scrollState = rememberScrollState()
        
        Card(
            modifier = Modifier
                .width(260.dp)
                .heightIn(max = 400.dp) // Maximum height to fit screen
                .shadow(12.dp, RoundedCornerShape(16.dp))
                .clickable { }, // Prevent drag when tapping inside
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1F1F1F)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Game Control",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(
                        onClick = { isExpanded = false },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Performance info - compact 4 columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CompactInfoChip("FPS", fpsValue, Color(0xFF4CAF50))
                    CompactInfoChip("CPU", "$cpuUsage%", Color(0xFF2196F3))
                    CompactInfoChip("GPU", "$gpuUsage%", Color(0xFFFF9800))
                    CompactInfoChip("T°", "$tempValue°", Color(0xFFF44336))
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), thickness = 0.5.dp)

                // Performance Mode - Compact row
                Text(
                    text = "Performance",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CompactModeChip(
                        "🔋",
                        "Battery",
                        currentPerformanceMode == "battery",
                        Modifier.weight(1f)
                    ) { setPerformanceMode("battery") }

                    CompactModeChip(
                        "⚖️",
                        "Balance",
                        currentPerformanceMode == "balanced",
                        Modifier.weight(1f)
                    ) { setPerformanceMode("balanced") }

                    CompactModeChip(
                        "⚡",
                        "Perf",
                        currentPerformanceMode == "performance",
                        Modifier.weight(1f)
                    ) { setPerformanceMode("performance") }
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), thickness = 0.5.dp)

                // Quick Controls
                Text(
                    text = "Quick Controls",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                // DND Toggle - Compact
                CompactControlToggle(
                    label = "Do Not Disturb",
                    icon = Icons.Default.DoNotDisturb,
                    checked = isDndEnabled
                ) { setDND(it) }

                // Hide Notif Toggle - Compact
                CompactControlToggle(
                    label = "Hide Notifications",
                    icon = Icons.Default.NotificationsOff,
                    checked = isHideNotif
                ) { setHideNotifications(it) }

                // Clear RAM Button - in Quick Controls
                ClearRamButton(
                    isClearing = isClearingRam,
                    onClick = { clearRAM() }
                )
            }
        }
    }

    @Composable
    private fun ClearRamButton(
        isClearing: Boolean,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                .clickable(enabled = !isClearing) { onClick() }
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CleaningServices,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isClearing) "Clearing..." else "Clear RAM",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
            
            if (isClearing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color(0xFF4CAF50),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Run",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    @Composable
    private fun CompactInfoChip(label: String, value: String, color: Color) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color.Gray
            )
        }
    }

    @Composable
    private fun CompactModeChip(
        emoji: String,
        label: String,
        isSelected: Boolean,
        modifier: Modifier,
        onClick: () -> Unit
    ) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(44.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isSelected) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Transparent
            ),
            border = BorderStroke(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.5f)
            ),
            contentPadding = PaddingValues(4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    emoji,
                    fontSize = 14.sp
                )
                Text(
                    label,
                    fontSize = 9.sp,
                    color = if (isSelected) Color(0xFF4CAF50) else Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Composable
    private fun CompactControlToggle(
        label: String,
        icon: ImageVector,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (checked) Color(0xFF4CAF50) else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.height(24.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4CAF50),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color(0xFF3A3A3A)
                )
            )
        }
    }

    // Control functions
    private fun setPerformanceMode(mode: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                gameControlUseCase.setPerformanceMode(mode)
            }
            if (result.isSuccess) {
                currentPerformanceMode = mode
                preferencesManager.setPerfMode(mode)
                val modeLabel = when(mode) {
                    "battery" -> "Battery Saver"
                    "balanced" -> "Balanced"
                    "performance" -> "Performance"
                    else -> mode
                }
                showToast("Mode: $modeLabel")
            }
        }
    }

    private fun setDND(enabled: Boolean) {
        // Check permission first
        if (!gameControlUseCase.hasDNDPermission()) {
            showToast("Please grant DND access in Settings")
            // Open DND settings
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
                isDndEnabled = enabled
                preferencesManager.setGameControlDND(enabled)
                showToast(if (enabled) "Do Not Disturb: ON" else "Do Not Disturb: OFF")
            }.onFailure { error ->
                when (error) {
                    is GameControlUseCase.DNDPermissionException -> {
                        showToast("Grant DND permission in Settings")
                    }
                    else -> {
                        showToast("DND error: ${error.message}")
                    }
                }
            }
        }
    }

    private fun setHideNotifications(enabled: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            isHideNotif = enabled
            preferencesManager.setGameControlHideNotif(enabled)
            showToast(if (enabled) "Hide Notifications: ON" else "Hide Notifications: OFF")
        }
    }

    private fun clearRAM() {
        if (isClearingRam) return // Prevent double tap
        
        CoroutineScope(Dispatchers.Main).launch {
            isClearingRam = true
            val result = withContext(Dispatchers.IO) {
                gameControlUseCase.clearRAM()
            }
            isClearingRam = false
            
            result.onSuccess { clearResult ->
                val message = if (clearResult.freedMB > 0) {
                    "Cleared ${clearResult.freedMB} MB RAM"
                } else {
                    "RAM already optimized (${clearResult.availableMB} MB free)"
                }
                showToast(message)
            }.onFailure {
                showToast("Failed to clear RAM")
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
