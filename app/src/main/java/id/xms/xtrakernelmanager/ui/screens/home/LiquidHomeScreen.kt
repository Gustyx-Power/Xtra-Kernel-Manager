package id.xms.xtrakernelmanager.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.model.CPUInfo
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament
import id.xms.xtrakernelmanager.ui.screens.home.components.liquid.*
import kotlinx.coroutines.delay
import java.util.Locale

@SuppressLint("DefaultLocale")
@Composable
fun LiquidHomeScreen(
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
    val viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val dimens = id.xms.xtrakernelmanager.ui.theme.rememberResponsiveDimens()
    val isCompact =
          dimens.screenSizeClass == id.xms.xtrakernelmanager.ui.theme.ScreenSizeClass.COMPACT
    
    // Remove custom status bar - use system status bar instead

    // Check accessibility service status
    var showAccessibilityDialog by remember { mutableStateOf(false) }
    var hasCheckedAccessibility by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (!hasCheckedAccessibility) {
            delay(1000) // Wait 1 second after screen loads
            val isEnabled = viewModel.isAccessibilityServiceEnabled(context)
            if (!isEnabled) {
                showAccessibilityDialog = true
            }
            hasCheckedAccessibility = true
        }
    }

    // Animation states for each component
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100) // Small delay before starting animations
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Content Scrollable Column with pure Liquid Backdrop
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Remove custom status bar - use system status bar instead
            Spacer(modifier = Modifier.height(16.dp)) // Standard top padding
     
            // 1. Header (Redesigned Liquid Header) - Index 0
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 0
            ) {
                LiquidHeader(
                    onSettingsClick = onSettingsClick,
                    modifier = Modifier
                )
            }

            // 2. Liquid Device Card - Index 1
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 100
            ) {
                LiquidDeviceCard(systemInfo = systemInfo, modifier = Modifier.fillMaxWidth())
            }

            // 3. CPU & GPU Tiles (Rich Tiles) - Index 2
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 200
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LiquidStatTile(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        icon = Icons.Rounded.Memory,
                        label = "CPU",
                        value = "${(cpuInfo.cores.maxOfOrNull { it.currentFreq } ?: 0) / 1000} MHz",
                        subValue = cpuInfo.cores.firstOrNull { it.isOnline }?.governor ?: "Unknown",
                        color = id.xms.xtrakernelmanager.ui.theme.NeonGreen,
                        badgeText = "${String.format(Locale.US, "%.0f", cpuInfo.totalLoad)}%"
                    )
                    
                    LiquidTempTile(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        cpuTemp = cpuInfo.temperature.toInt(),
                        gpuTemp = gpuInfo.temperature.toInt(),
                        pmicTemp = batteryInfo.pmicTemp.toInt(),
                        thermalTemp = batteryInfo.temperature.toInt(),
                        color = id.xms.xtrakernelmanager.ui.theme.NeonPurple
                    )
                }
            }

            // 4. Liquid GPU Card (Detailed) - Index 3
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 300
            ) {
                LiquidGPUCard(gpuInfo = gpuInfo, modifier = Modifier.fillMaxWidth())
            }

            // 5. Liquid Battery Card (Detailed) - Index 4
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 400
            ) {
                LiquidBatteryCard(batteryInfo = batteryInfo, modifier = Modifier.fillMaxWidth())
            }
            
            // 6. Memory & Storage Row - Index 5
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 500
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val totalMem = systemInfo.totalRam
                    val availMem = systemInfo.availableRam
                    val usedMem = totalMem - availMem
                    val memProgress = if (totalMem > 0) usedMem.toFloat() / totalMem.toFloat() else 0f
                    val usedGb = String.format("%.1f GB", usedMem / (1024f * 1024f * 1024f))

                    LiquidCircularStatsCard(
                        title = "RAM",
                        value = usedGb,
                        progress = memProgress,
                        color = id.xms.xtrakernelmanager.ui.theme.NeonBlue,
                        modifier = Modifier.weight(1f)
                    )

                    val totalStorage = systemInfo.totalStorage
                    val availStorage = systemInfo.availableStorage
                    val usedStorage = totalStorage - availStorage
                    val storageProgress = if (totalStorage > 0) usedStorage.toFloat() / totalStorage.toFloat() else 0f
                    val usedStorageGb = String.format("%.0f GB", usedStorage / (1024f * 1024f * 1024f))

                    LiquidCircularStatsCard(
                        title = "ROM",
                        value = usedStorageGb,
                        progress = storageProgress,
                        color = id.xms.xtrakernelmanager.ui.theme.NeonPurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // 7. Actions Row - Index 6
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 600
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LiquidPowerMenu(
                        onAction = onPowerAction,
                        modifier = Modifier.weight(1f).height(140.dp)
                    )
                    
                    LiquidProfileCard(
                        currentProfile = currentProfile,
                        onNextProfile = {
                            val next = when (currentProfile) {
                                "Balance" -> "Performance"
                                "Performance" -> "Battery"
                                else -> "Balance"
                            }
                            onProfileChange(next)
                        },
                        modifier = Modifier.weight(1f).height(140.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    
    // Accessibility Service Dialog
    if (showAccessibilityDialog) {
        id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialog(
            onDismissRequest = { showAccessibilityDialog = false },
            title = "Accessibility Service Disabled",
            content = {
                Text(
                    text = "XKM Game Monitor accessibility service is disabled. This service is required for game detection and statistics tracking.\n\nWould you like to enable it now?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialogButton(
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
                id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialogButton(
                    text = "Later",
                    onClick = { showAccessibilityDialog = false },
                    isPrimary = false
                )
            }
        )
    }
}

@Composable
private fun AnimatedComponent(
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
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val translationY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 50f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
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
