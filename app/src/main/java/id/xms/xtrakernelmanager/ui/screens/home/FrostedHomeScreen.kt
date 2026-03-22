package id.xms.xtrakernelmanager.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.model.CPUInfo
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament
import id.xms.xtrakernelmanager.ui.screens.home.components.frosted.*
import id.xms.xtrakernelmanager.ui.screens.home.HomeViewModel
import id.xms.xtrakernelmanager.ui.theme.*
import id.xms.xtrakernelmanager.ui.model.getLocalizedLabel
import kotlinx.coroutines.delay
import java.util.Locale

private fun safeDivide(numerator: Long, denominator: Long): Long {
    return if (denominator > 0) numerator / denominator else 0
}

private fun bytesToMB(bytes: Long): Long {
    return if (bytes > 0) bytes / (1024 * 1024) else 0
}

private fun safePercentage(used: Long, total: Long): Int {
    return if (total > 0 && used >= 0) {
        ((used * 100) / total).toInt().coerceIn(0, 100)
    } else 0
}

@SuppressLint("DefaultLocale")
@Composable
fun FrostedHomeScreen(
    cpuInfo: CPUInfo,
    gpuInfo: GPUInfo,
    batteryInfo: BatteryInfo,
    systemInfo: SystemInfo,
    currentProfile: String,
    onProfileChange: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onPowerAction: (id.xms.xtrakernelmanager.ui.model.PowerAction) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: HomeViewModel = viewModel()
    val dimens = rememberResponsiveDimens()
    val isCompact = dimens.screenSizeClass == ScreenSizeClass.COMPACT
    
    var showAccessibilityDialog by remember { mutableStateOf(false) }
    var hasCheckedAccessibility by remember { mutableStateOf(false) }
    var showPowerDialog by remember { mutableStateOf(false) }
    
    // uptime
    var uptime by remember { mutableStateOf(calculateUptimeString()) }
    var deepSleep by remember { mutableStateOf("0h 0m") }
    
    LaunchedEffect(Unit) {
        while (true) {
            uptime = calculateUptimeString()
            
            // Deep Sleep
            val deepSleepMillis = systemInfo.deepSleep
            val seconds = deepSleepMillis / 1000
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            deepSleep = "${hours}h ${minutes}m"
            
            delay(60000)
        }
    }
    
    LaunchedEffect(Unit) {
        if (!hasCheckedAccessibility) {
            delay(1000)
            val isEnabled = viewModel.isAccessibilityServiceEnabled(context)
            if (!isEnabled) {
                showAccessibilityDialog = true
            }
            hasCheckedAccessibility = true
        }
    }

    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val frostedBlobColors = listOf(
        Color(0xFF4A9B8E), 
        Color(0xFF8BA8D8), 
        Color(0xFF6BC4E8)  
    )

    Box(modifier = Modifier.fillMaxSize()) {
        WavyBlobOrnament(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { },
            colors = frostedBlobColors
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimens.screenHorizontalPadding)
                .graphicsLayer { }
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingLarge)
        ) {
            Spacer(modifier = Modifier.height(dimens.spacingLarge))
     
            // Header
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 0
            ) {
                FrostedHeader(
                    onSettingsClick = onSettingsClick,
                    onPowerClick = { showPowerDialog = true },
                    modifier = Modifier
                )
            }
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 100
            ) {
                FrostedDeviceCard(systemInfo = systemInfo, modifier = Modifier.fillMaxWidth())
            }
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 200
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingLarge)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacingLarge)
                    ) {
                        FrostedStatTile(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            icon = Icons.Rounded.Memory,
                            label = "UPTIME",
                            value = uptime,
                            subValue = "",
                            color = Color(0xFF4A9B8E),
                            badgeText = ""
                        )
                        
                        FrostedStatTile(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            icon = Icons.Rounded.Memory,
                            label = "DEEP SLEEP",
                            value = deepSleep,
                            subValue = "",
                            color = Color(0xFF4A9B8E),
                            badgeText = ""
                        )
                    }
                    FrostedStatTile(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.Memory,
                        label = "KERNEL REV",
                        value = systemInfo.kernelVersion,
                        subValue = "",
                        color = NeonOrange,
                        badgeText = ""
                    )
                }
            }
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 300
            ) {
                FrostedCPUCard(cpuInfo = cpuInfo, modifier = Modifier.fillMaxWidth())
            }
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 400
            ) {
                FrostedGPUCard(gpuInfo = gpuInfo, modifier = Modifier.fillMaxWidth())
            }
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 500
            ) {
                FrostedBatteryCard(batteryInfo = batteryInfo, modifier = Modifier.fillMaxWidth())
            }
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 600
            ) {
                FrostedTempTile(
                    modifier = Modifier.fillMaxWidth(),
                    cpuTemp = cpuInfo.temperature.toInt(),
                    gpuTemp = gpuInfo.temperature.toInt(),
                    pmicTemp = batteryInfo.pmicTemp.toInt(),
                    thermalTemp = batteryInfo.temperature.toInt(),
                    color = Color(0xFFFF1744)
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showAccessibilityDialog) {
            id.xms.xtrakernelmanager.ui.components.frosted.FrostedDialog(
                onDismissRequest = { showAccessibilityDialog = false },
                title = "Accessibility Service Required",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "This app requires accessibility service to function properly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                        )
                        Text(
                            text = "Please enable the accessibility service in settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                },
                confirmButton = {
                    id.xms.xtrakernelmanager.ui.components.frosted.FrostedDialogButton(
                        text = "Enable",
                        onClick = {
                            showAccessibilityDialog = false
                            val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        isPrimary = true
                    )
                },
                dismissButton = {
                    id.xms.xtrakernelmanager.ui.components.frosted.FrostedDialogButton(
                        text = "Later",
                        onClick = { showAccessibilityDialog = false },
                        isPrimary = false
                    )
                }
            )
        }
        
        if (showPowerDialog) {
            FrostedPowerMenuDialog(
                onDismissRequest = { showPowerDialog = false },
                onAction = { action ->
                    showPowerDialog = false
                    onPowerAction(action)
                }
            )
        }
    }
}

private fun calculateUptimeString(): String {
    val uptimeMillis = android.os.SystemClock.elapsedRealtime()
    return if (uptimeMillis > android.text.format.DateUtils.DAY_IN_MILLIS) {
         "${uptimeMillis / android.text.format.DateUtils.DAY_IN_MILLIS}d"
    } else {
         val hours = uptimeMillis / android.text.format.DateUtils.HOUR_IN_MILLIS
         val minutes = (uptimeMillis % android.text.format.DateUtils.HOUR_IN_MILLIS) / android.text.format.DateUtils.MINUTE_IN_MILLIS
         "${hours}h ${minutes}m"
    }
}

@Composable
fun AnimatedComponent(
    visible: Boolean,
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    LaunchedEffect(visible) {
        if (visible) {
            delay(delayMillis.toLong())
            startAnimation = true
        }
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.95f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "scale"
    )
    
    val translationY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 30f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "translationY"
    )
    
    Box(
        modifier = Modifier
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
                this.translationY = translationY
            }
    ) {
        content()
    }
}

@Composable
private fun FrostedPowerMenuDialog(
    onDismissRequest: () -> Unit,
    onAction: (id.xms.xtrakernelmanager.ui.model.PowerAction) -> Unit
) {
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    
    id.xms.xtrakernelmanager.ui.components.frosted.FrostedDialog(
        onDismissRequest = onDismissRequest,
        title = stringResource(id.xms.xtrakernelmanager.R.string.frosted_power_actions_title),
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Power Off
                PowerActionButton(
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    action = id.xms.xtrakernelmanager.ui.model.PowerAction.PowerOff,
                    color = Color(0xFFEF4444), // Red
                    isDarkTheme = isDarkTheme,
                    onClick = { onAction(id.xms.xtrakernelmanager.ui.model.PowerAction.PowerOff) }
                )
                
                // Reboot
                PowerActionButton(
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    action = id.xms.xtrakernelmanager.ui.model.PowerAction.Reboot,
                    color = Color(0xFF3B82F6), // Blue
                    isDarkTheme = isDarkTheme,
                    onClick = { onAction(id.xms.xtrakernelmanager.ui.model.PowerAction.Reboot) }
                )
                
                // Recovery
                PowerActionButton(
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    action = id.xms.xtrakernelmanager.ui.model.PowerAction.Recovery,
                    color = Color(0xFFF59E0B), // Orange
                    isDarkTheme = isDarkTheme,
                    onClick = { onAction(id.xms.xtrakernelmanager.ui.model.PowerAction.Recovery) }
                )
                
                // Bootloader
                PowerActionButton(
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    action = id.xms.xtrakernelmanager.ui.model.PowerAction.Bootloader,
                    color = Color(0xFF10B981), // Green
                    isDarkTheme = isDarkTheme,
                    onClick = { onAction(id.xms.xtrakernelmanager.ui.model.PowerAction.Bootloader) }
                )
                
                // System UI
                PowerActionButton(
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    action = id.xms.xtrakernelmanager.ui.model.PowerAction.SystemUI,
                    color = Color(0xFF8B5CF6), // Purple
                    isDarkTheme = isDarkTheme,
                    onClick = { onAction(id.xms.xtrakernelmanager.ui.model.PowerAction.SystemUI) }
                )
            }
        },
        confirmButton = {
            id.xms.xtrakernelmanager.ui.components.frosted.FrostedDialogButton(
                text = stringResource(id.xms.xtrakernelmanager.R.string.frosted_power_actions_close),
                onClick = onDismissRequest,
                isPrimary = false
            )
        }
    )
}

@Composable
private fun PowerActionButton(
    action: id.xms.xtrakernelmanager.ui.model.PowerAction,
    color: Color,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonBackground = if (isDarkTheme) {
        Color(0xFF000000).copy(alpha = 0.25f)
    } else {
        Color.White.copy(alpha = 0.45f)
    }
    
    val buttonBorder = if (isDarkTheme) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.4f)
    }
    
    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(buttonBackground)
            .border(0.8.dp, buttonBorder, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = action.getLocalizedLabel(),
                style = MaterialTheme.typography.bodyLarge,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
