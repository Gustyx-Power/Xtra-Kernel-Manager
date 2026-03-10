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
     
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 0
            ) {
                FrostedHeader(
                    onSettingsClick = onSettingsClick,
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
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingLarge)
                ) {
                    FrostedStatTile(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        icon = Icons.Rounded.Memory,
                        label = "CPU",
                        value = "${safeDivide((cpuInfo.cores.maxOfOrNull { it.currentFreq } ?: 0).toLong(), 1000)} MHz",
                        subValue = cpuInfo.cores.firstOrNull { it.isOnline }?.governor ?: "Unknown",
                        color = NeonGreen,
                        badgeText = "${String.format(Locale.US, "%.0f", cpuInfo.totalLoad)}%"
                    )
                    
                    FrostedTempTile(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        cpuTemp = cpuInfo.temperature.toInt(),
                        gpuTemp = gpuInfo.temperature.toInt(),
                        pmicTemp = batteryInfo.pmicTemp.toInt(),
                        thermalTemp = batteryInfo.temperature.toInt(),
                        color = Color(0xFFFF1744)
                    )
                }
            }

            AnimatedComponent(
                visible = isVisible,
                delayMillis = 300
            ) {
                FrostedGPUCard(gpuInfo = gpuInfo, modifier = Modifier.fillMaxWidth())
            }

            AnimatedComponent(
                visible = isVisible,
                delayMillis = 400
            ) {
                FrostedBatteryCard(batteryInfo = batteryInfo, modifier = Modifier.fillMaxWidth())
            }
            
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 500
            ) {
                FrostedStatTile(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Filled.Memory,
                    label = "RAM",
                    value = "${bytesToMB(maxOf(systemInfo.totalRam, 0))} MB",
                    subValue = "${bytesToMB(maxOf(systemInfo.availableRam, 0))} MB Free",
                    color = NeonBlue,
                    badgeText = "${safePercentage(maxOf(systemInfo.totalRam - systemInfo.availableRam, 0), maxOf(systemInfo.totalRam, 1))}%"
                )
            }
            
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 550
            ) {
                FrostedStatTile(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Filled.Folder,
                    label = "Storage",
                    value = "${bytesToMB(maxOf(systemInfo.totalStorage, 0))} MB",
                    subValue = "${bytesToMB(maxOf(systemInfo.availableStorage, 0))} MB Free",
                    color = NeonOrange,
                    badgeText = "${safePercentage(maxOf(systemInfo.totalStorage - systemInfo.availableStorage, 0), maxOf(systemInfo.totalStorage, 1))}%"
                )
            }
            
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 600
            ) {
                FrostedProfileCard(
                    currentProfile = currentProfile,
                    onProfileChange = onProfileChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            AnimatedComponent(
                visible = isVisible,
                delayMillis = 650
            ) {
                FrostedPowerMenu(
                    onAction = onPowerAction,
                    modifier = Modifier.fillMaxWidth()
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
