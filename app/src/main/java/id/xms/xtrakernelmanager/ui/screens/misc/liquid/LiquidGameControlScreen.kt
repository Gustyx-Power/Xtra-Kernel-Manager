package id.xms.xtrakernelmanager.ui.screens.misc.liquid

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import coil.compose.AsyncImage
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.service.GameMonitorService
import id.xms.xtrakernelmanager.service.GameOverlayService
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.gameoverlay.HardwareGauge
import id.xms.xtrakernelmanager.ui.components.gameoverlay.HardwareGaugeType
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidToggle
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import id.xms.xtrakernelmanager.ui.screens.misc.components.GameMonitorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun LiquidGameControlScreen(
    viewModel: MiscViewModel,
    gameMonitorViewModel: GameMonitorViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isLightTheme = !isSystemInDarkTheme()

    val gameAppsJson by viewModel.gameApps.collectAsState()
    var showAddGameDialog by remember { mutableStateOf(false) }

    val gameApps = remember(gameAppsJson) { parseGameApps(gameAppsJson) }
    val enabledCount = gameApps.count { it.enabled }

    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasUsageAccessPermission by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    var hasAccessibilityPermission by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = Settings.canDrawOverlays(context)
                hasUsageAccessPermission = hasUsageStatsPermission(context)
                hasAccessibilityPermission = isAccessibilityServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasOverlayPermission = Settings.canDrawOverlays(context)
    }

    val usageAccessLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasUsageAccessPermission = hasUsageStatsPermission(context)
    }

    val accessibilityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasAccessibilityPermission = isAccessibilityServiceEnabled(context)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        // Background decoration
        id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament(
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Header
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                onClick = onBack
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp).clickable(onClick = onBack)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.game_control),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    // Badge
                    if (enabledCount > 0) {
                        Surface(
                            color = if (isLightTheme) Color(0xFFAF52DE).copy(0.2f) else Color(0xFFBF5AF2).copy(0.25f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "$enabledCount games",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (isLightTheme) Color(0xFFAF52DE) else Color(0xFFBF5AF2),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 1. Game Library Section
            LiquidGameLibrarySection(
                gameApps = gameApps,
                hasOverlayPermission = hasOverlayPermission,
                hasUsageAccessPermission = hasUsageAccessPermission,
                hasAccessibilityPermission = hasAccessibilityPermission,
                context = context,
                isLightTheme = isLightTheme,
                viewModel = viewModel,
                overlayPermissionLauncher = overlayPermissionLauncher,
                usageAccessLauncher = usageAccessLauncher,
                accessibilityLauncher = accessibilityLauncher,
                onShowAddDialog = { showAddGameDialog = true }
            )

            // 2. Notifications Section
            LiquidNotificationsSection(
                viewModel = viewModel,
                isLightTheme = isLightTheme
            )

            // 3. Display & Gestures Section
            LiquidDisplayGesturesSection(
                viewModel = viewModel,
                isLightTheme = isLightTheme
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showAddGameDialog) {
        AddGameDialog(
            context = context,
            existingPackages = gameApps.map { it.packageName },
            onDismiss = { showAddGameDialog = false },
            onConfirm = { newGame ->
                val updatedList = gameApps + newGame
                scope.launch { viewModel.saveGameApps(serializeGameApps(updatedList)) }
                showAddGameDialog = false
            }
        )
    }
}

// Section 1: Game Library
@Composable
private fun LiquidGameLibrarySection(
    gameApps: List<GameApp>,
    hasOverlayPermission: Boolean,
    hasUsageAccessPermission: Boolean,
    hasAccessibilityPermission: Boolean,
    context: Context,
    isLightTheme: Boolean,
    viewModel: MiscViewModel,
    overlayPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
    usageAccessLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
    accessibilityLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
    onShowAddDialog: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Game Library",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
            modifier = Modifier.padding(start = 4.dp)
        )
        
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Game count and add button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${gameApps.size} Games",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Registered in ${stringResource(R.string.game_control)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                    }

                    Button(
                        onClick = onShowAddDialog,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLightTheme) Color(0xFFAF52DE) else Color(0xFFBF5AF2)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add")
                    }
                }

                // Performance Monitor Card
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        // Check all required permissions
                        if (!hasAccessibilityPermission) {
                            android.widget.Toast.makeText(
                                context,
                                "Please enable Game Monitor in Accessibility settings",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            accessibilityLauncher.launch(intent)
                            return@GlassmorphicCard
                        }
                        
                        if (!hasOverlayPermission) {
                            android.widget.Toast.makeText(
                                context,
                                "Please grant Overlay permission",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                "package:${context.packageName}".toUri()
                            )
                            overlayPermissionLauncher.launch(intent)
                            return@GlassmorphicCard
                        }

                        if (!hasUsageAccessPermission) {
                            android.widget.Toast.makeText(
                                context,
                                "Please enable Usage Access permission",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            usageAccessLauncher.launch(intent)
                            return@GlassmorphicCard
                        }

                        android.widget.Toast.makeText(
                            context,
                            "All permissions granted! Game monitoring is active",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        // Start overlay service
                        val serviceIntent = Intent(context, GameOverlayService::class.java)
                        context.startService(serviceIntent)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isLightTheme) Color(0xFF00D4FF).copy(0.2f)
                                        else Color(0xFF00E5FF).copy(0.25f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Speed,
                                    contentDescription = null,
                                    tint = if (isLightTheme) Color(0xFF00D4FF) else Color(0xFF00E5FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    "Performance Monitor",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    if (hasAccessibilityPermission && hasOverlayPermission && hasUsageAccessPermission) {
                                        "All permissions granted"
                                    } else {
                                        "Tap to configure permissions"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                )
                            }
                        }
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                        )
                    }
                }

                // Game list
                if (gameApps.isNotEmpty()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                    
                    gameApps.forEach { game ->
                        GameAppItemCard(
                            game = game,
                            context = context,
                            isLightTheme = isLightTheme,
                            onToggle = { newEnabled ->
                                val updatedList = gameApps.map {
                                    if (it.packageName == game.packageName) it.copy(enabled = newEnabled)
                                    else it
                                }
                                scope.launch { viewModel.saveGameApps(serializeGameApps(updatedList)) }
                            },
                            onDelete = {
                                val updatedList = gameApps.filter { it.packageName != game.packageName }
                                scope.launch { viewModel.saveGameApps(serializeGameApps(updatedList)) }
                            }
                        )
                    }
                }
            }
        }
    }
}

// Section 2: Notifications
@Composable
private fun LiquidNotificationsSection(
    viewModel: MiscViewModel,
    isLightTheme: Boolean
) {
    val callOverlay by viewModel.callOverlay.collectAsState()
    val inGameCallAction by viewModel.inGameCallAction.collectAsState()
    val inGameRingerMode by viewModel.inGameRingerMode.collectAsState()
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
            modifier = Modifier.padding(start = 4.dp)
        )
        
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Call overlay
                LiquidSwitchRow(
                    icon = Icons.Rounded.Call,
                    title = "Call overlay",
                    subtitle = "Show minimal call overlay to answer/reject calls",
                    checked = callOverlay,
                    onCheckedChange = { viewModel.setCallOverlay(it) },
                    isLightTheme = isLightTheme
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

                // In-game call action
                LiquidExpandableRow(
                    icon = Icons.Rounded.PhoneInTalk,
                    title = "In-game call",
                    subtitle = when (inGameCallAction) {
                        "no_action" -> "No action"
                        "answer" -> "Auto Answer"
                        "reject" -> "Auto Reject"
                        else -> "No action"
                    },
                    options = listOf(
                        "no_action" to "No action",
                        "answer" to "Auto Answer",
                        "reject" to "Auto Reject"
                    ),
                    selectedOption = inGameCallAction,
                    onOptionSelected = { viewModel.setInGameCallAction(it) },
                    isLightTheme = isLightTheme
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

                // In-game ringer mode
                LiquidExpandableRow(
                    icon = Icons.Rounded.VolumeUp,
                    title = "In-game ringer mode",
                    subtitle = when (inGameRingerMode) {
                        "no_change" -> "Do not change"
                        "silent" -> "Silent"
                        "vibrate" -> "Vibrate"
                        else -> "Do not change"
                    },
                    options = listOf(
                        "no_change" to "Do not change",
                        "silent" to "Silent",
                        "vibrate" to "Vibrate"
                    ),
                    selectedOption = inGameRingerMode,
                    onOptionSelected = { viewModel.setInGameRingerMode(it) },
                    isLightTheme = isLightTheme
                )
            }
        }
    }
}

// Section 3: Display & Gestures
@Composable
private fun LiquidDisplayGesturesSection(
    viewModel: MiscViewModel,
    isLightTheme: Boolean
) {
    val disableAutoBrightness by viewModel.disableAutoBrightness.collectAsState()
    val disableThreeFingerSwipe by viewModel.disableThreeFingerSwipe.collectAsState()
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Display & Gestures",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
            modifier = Modifier.padding(start = 4.dp)
        )
        
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Disable auto-brightness
                LiquidSwitchRow(
                    icon = Icons.Rounded.BrightnessAuto,
                    title = "Disable auto-brightness",
                    subtitle = "Keep brightness settled while in-game",
                    checked = disableAutoBrightness,
                    onCheckedChange = { viewModel.setDisableAutoBrightness(it) },
                    isLightTheme = isLightTheme
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

                // Disable three finger swipe
                LiquidSwitchRow(
                    icon = Icons.Rounded.Gesture,
                    title = "Disable three fingers swipe gesture",
                    subtitle = "Temporary disable three fingers swipe gesture while in-game",
                    checked = disableThreeFingerSwipe,
                    onCheckedChange = { viewModel.setDisableThreeFingerSwipe(it) },
                    isLightTheme = isLightTheme
                )
            }
        }
    }
}

// Reusable Components
@Composable
private fun LiquidSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isLightTheme: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        }

        LiquidToggle(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun LiquidExpandableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    options: List<Pair<String, String>>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    isLightTheme: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLightTheme) Color(0xFFAF52DE) else Color(0xFFBF5AF2),
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(0.4f)
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    options.forEach { (key, label) ->
                        val isSelected = (key == selectedOption)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) {
                                        if (isLightTheme) Color(0xFFAF52DE).copy(0.15f)
                                        else Color(0xFFBF5AF2).copy(0.2f)
                                    } else Color.Transparent
                                )
                                .clickable {
                                    onOptionSelected(key)
                                    expanded = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) {
                                    if (isLightTheme) Color(0xFFAF52DE) else Color(0xFFBF5AF2)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )

                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = if (isLightTheme) Color(0xFFAF52DE) else Color(0xFFBF5AF2),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun GameAppItemCard(
    game: GameApp,
    context: Context,
    isLightTheme: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val appIcon = remember(game.packageName) {
            try {
                context.packageManager.getApplicationIcon(game.packageName)
            } catch (e: Exception) {
                null
            }
        }

        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (appIcon != null) {
                AsyncImage(
                    model = appIcon,
                    contentDescription = game.appName,
                    modifier = Modifier.size(44.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (game.enabled) {
                                if (isLightTheme) Color(0xFFAF52DE) else Color(0xFFBF5AF2)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = game.appName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (game.enabled) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = game.appName,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = if (game.enabled) "Overlay enabled" else "Disabled",
                style = MaterialTheme.typography.bodySmall,
                color = if (game.enabled) {
                    if (isLightTheme) Color(0xFFAF52DE) else Color(0xFFBF5AF2)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(0.5f)
                }
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        }

        LiquidToggle(
            checked = game.enabled,
            onCheckedChange = onToggle
        )
    }
}

@Composable
private fun AddGameDialog(
    context: Context,
    existingPackages: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (GameApp) -> Unit
) {
    var installedApps by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            installedApps = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
                    .filter { appInfo ->
                        (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 &&
                            !existingPackages.contains(appInfo.packageName) &&
                            pm.getLaunchIntentForPackage(appInfo.packageName) != null
                    }
                    .map { appInfo ->
                        appInfo.packageName to pm.getApplicationLabel(appInfo).toString()
                    }
                    .sortedBy { it.second.lowercase() }
            }
            isLoading = false
        }
    }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.second.contains(searchQuery, ignoreCase = true) ||
                it.first.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(0.92f).padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Game",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search apps") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Box(modifier = Modifier.heightIn(max = 300.dp)) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredApps) { app ->
                                AppDialogItem(
                                    app = app,
                                    context = context,
                                    onClick = { onConfirm(GameApp(app.first, app.second, enabled = true)) }
                                )
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun AppDialogItem(
    app: Pair<String, String>,
    context: Context,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(0.5f))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val appIcon = remember(app.first) {
                try {
                    context.packageManager.getApplicationIcon(app.first)
                } catch (e: Exception) {
                    null
                }
            }

            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    AsyncImage(
                        model = appIcon,
                        contentDescription = app.second,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.second.take(1).uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.second,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = app.first,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
            }

            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}


// Helper functions
private fun startGameMonitorService(context: Context) {
    // GameMonitorService is an AccessibilityService, not a regular service
    // It cannot be started with startService() or startForegroundService()
    // It must be enabled through Settings > Accessibility
    // This function is kept for compatibility but does nothing
}

private fun stopGameMonitorService(context: Context) {
    // GameMonitorService is an AccessibilityService
    // It cannot be stopped programmatically
    // This function is kept for compatibility but does nothing
}

private fun parseGameApps(json: String): List<GameApp> {
    return try {
        val jsonArray = JSONArray(json)
        val apps = mutableListOf<GameApp>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            apps.add(
                GameApp(
                    packageName = obj.getString("packageName"),
                    appName = obj.getString("appName"),
                    enabled = obj.optBoolean("enabled", true)
                )
            )
        }
        apps
    } catch (e: Exception) {
        emptyList()
    }
}

private fun serializeGameApps(apps: List<GameApp>): String {
    val jsonArray = JSONArray()
    apps.forEach { app ->
        val obj = JSONObject().apply {
            put("packageName", app.packageName)
            put("appName", app.appName)
            put("enabled", app.enabled)
        }
        jsonArray.put(obj)
    }
    return jsonArray.toString()
}

private fun hasUsageStatsPermission(context: Context): Boolean {
    return try {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        mode == android.app.AppOpsManager.MODE_ALLOWED
    } catch (e: Exception) {
        false
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedComponentName = "${context.packageName}/${GameMonitorService::class.java.name}"
    val enabledServicesSetting = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServicesSetting.contains(expectedComponentName)
}
