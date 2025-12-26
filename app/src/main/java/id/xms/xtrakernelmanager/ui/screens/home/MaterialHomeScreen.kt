package id.xms.xtrakernelmanager.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.border
import androidx.compose.ui.unit.em
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.ui.components.DeviceSilhouette
import id.xms.xtrakernelmanager.ui.components.WavyProgressIndicator
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import java.util.Locale

/**
 * Material Home Screen - Restored Layout with Dynamic Colors (Material You)
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialHomeScreen(
    preferencesManager: PreferencesManager,
    viewModel: HomeViewModel = viewModel(),
    onPowerAction: (PowerAction) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Bottom Sheet State
    @OptIn(ExperimentalMaterial3Api::class)
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    
    // Data State
    val cpuInfo by viewModel.cpuInfo.collectAsState()
    val gpuInfo by viewModel.gpuInfo.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val systemInfo by viewModel.systemInfo.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBatteryInfo(context)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Rounded.PowerSettingsNew,
                    contentDescription = "Power Menu"
                )
            }
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    MaterialHeader(onSettingsClick = onSettingsClick)
                }

                // Device Info Card
                item {
                    MaterialDeviceCard(systemInfo = systemInfo)
                }

                // CPU & GPU Tiles Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max), // Force equal height
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MaterialStatTile(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            icon = Icons.Rounded.Memory, // Chip icon
                            label = "Load",
                            value = "${String.format(Locale.US, "%.0f", cpuInfo.totalLoad)}%",
                            subValue = "${cpuInfo.cores.maxOfOrNull { it.currentFreq } ?: 0} MHz",
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        MaterialStatTile(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            icon = Icons.Rounded.Videocam,
                            label = "GPU Freq",
                            value = "${gpuInfo.currentFreq}",
                            subValue = "MHz",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // GPU Information Card
                item {
                    MaterialGPUCard(gpuInfo = gpuInfo)
                }
                
                // Memory & Storage Card
                item {
                    MaterialMemoryCard(systemInfo = systemInfo)
                }

                // Battery Information Card
                item {
                    MaterialBatteryCard(batteryInfo = batteryInfo)
                }
                
                // Bottom Spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    )
    
    // Power Menu Sheet (Same as before)
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            PowerMenuContent(
                onAction = { 
                    showBottomSheet = false
                    onPowerAction(it)
                }
            )
        }
    }
}

@Composable
fun MaterialHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "XKM",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MaterialDeviceCard(systemInfo: SystemInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = android.os.Build.MANUFACTURER.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = systemInfo.deviceModel.replace(android.os.Build.MANUFACTURER, "", ignoreCase = true).trim().ifBlank { "Unknown Model" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = android.os.Build.DEVICE.ifBlank { "Unknown" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Unknown", // Codename placeholder replaced by Build.DEVICE above, this can be Board or similar
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = systemInfo.kernelVersion.ifBlank { "5.10.247" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace
                )
            }
            
            // Device Silhouette
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp)
                    .offset(y = 20.dp) // Push down to cut off bottom
            ) {
               DeviceSilhouette(
                   color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
               )
            }
        }
    }
}

@Composable
fun MaterialStatTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String,
    color: Color
) {
    Card(
        modifier = modifier, // Height controlled by caller (e.g. fillMaxHeight or fixed)
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun MaterialGPUCard(gpuInfo: id.xms.xtrakernelmanager.data.model.GPUInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GPU Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "QUALCOMM",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Column {
                Text(
                    text = "${gpuInfo.currentFreq} MHz",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max), // Ensure equal height
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Inner Card 1: Load
                Surface(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "45%", // Placeholder
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Load",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Inner Card 2: GPU Name
                Surface(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = gpuInfo.renderer,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 1.2.em
                        )
                        Text(
                            text = "GPU",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialBatteryCard(batteryInfo: BatteryInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BatteryChargingFull,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Battery Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                BatterySilhouette(
                    level = batteryInfo.level / 100f,
                    isCharging = batteryInfo.status.contains("Charging", ignoreCase = true),
                    color = MaterialTheme.colorScheme.primary
                )

                Column {
                    Text(
                        text = "${batteryInfo.level}%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 1.em
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BatteryStatusChip(text = batteryInfo.status)
                        BatteryStatusChip(text = "Health ${String.format(Locale.US, "%.0f", batteryInfo.healthPercent)}%")
                    }
                }
            }

            // Stats Grid (2x2) - 4 Individual Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Row 1
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val currentText = if (batteryInfo.currentNow >= 0) "+${batteryInfo.currentNow} mA" else "${batteryInfo.currentNow} mA"
                    BatteryStatBox(
                        label = "Current",
                        value = currentText,
                        modifier = Modifier.weight(1f)
                    )
                    BatteryStatBox(
                        label = "Voltage",
                        value = "${batteryInfo.voltage} mV",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    BatteryStatBox(
                        label = "Temperature",
                        value = "${batteryInfo.temperature}°C",
                        modifier = Modifier.weight(1f)
                    )
                    BatteryStatBox(
                        label = "Cycle Count",
                        value = "${batteryInfo.cycleCount}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun BatterySilhouette(
    level: Float,
    isCharging: Boolean,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(50.dp, 80.dp)
            .border(4.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(4.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(level)
                .background(color, RoundedCornerShape(6.dp))
        )
    }
}

@Composable
fun BatteryStatusChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant, 
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun BatteryStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.6f) // Higher contrast
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Dot removed for cleaner look

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}
@Composable
fun MaterialMemoryCard(systemInfo: SystemInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Memory Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // RAM Section
            val ramUsed = (systemInfo.totalRam - systemInfo.availableRam)
            val ramTotal = systemInfo.totalRam
            val ramProgress = if (ramTotal > 0) ramUsed.toFloat() / ramTotal.toFloat() else 0f
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "RAM",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "${formatFileSize(ramUsed)} / ${formatFileSize(ramTotal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                WavyProgressIndicator(
                    progress = ramProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp), // Height for the wave
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    strokeWidth = 4.dp,
                    amplitude = 4.dp
                )
            }

            // ZRAM / Swap Section (Show if Swap OR ZRAM exists)
            val showZram = systemInfo.swapTotal > 0 || systemInfo.zramSize > 0
            
            if (showZram) {
                // Prefer Swap stats if available, otherwise fallback to ZRAM capacity with 0 usage
                val swapTotal = if (systemInfo.swapTotal > 0) systemInfo.swapTotal else systemInfo.zramSize
                val swapUsed = if (systemInfo.swapTotal > 0) (systemInfo.swapTotal - systemInfo.swapFree) else 0L
                val swapProgress = if (swapTotal > 0) swapUsed.toFloat() / swapTotal.toFloat() else 0f
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = if (systemInfo.zramSize > 0) "ZRAM" else "Swap",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "${formatFileSize(swapUsed)} / ${formatFileSize(swapTotal)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    
                    WavyProgressIndicator(
                        progress = swapProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        strokeWidth = 4.dp,
                        amplitude = 4.dp
                    )
                }
            }

            // Internal Storage Section
            val storageUsed = (systemInfo.totalStorage - systemInfo.availableStorage)
            val storageTotal = systemInfo.totalStorage
            val storageProgress = if (storageTotal > 0) storageUsed.toFloat() / storageTotal.toFloat() else 0f
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Internal Storage",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "${formatFileSize(storageUsed)} / ${formatFileSize(storageTotal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                WavyProgressIndicator(
                    progress = storageProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    strokeWidth = 4.dp,
                    amplitude = 4.dp
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 1.0) {
        String.format(Locale.US, "%.1f GB", gb)
    } else {
        val mb = bytes / (1024.0 * 1024.0)
        String.format(Locale.US, "%.0f MB", mb)
    }
}

@Composable
fun PowerMenuContent(onAction: (PowerAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Power Menu",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val actions = listOf(
            PowerAction.PowerOff,
            PowerAction.Reboot,
            PowerAction.Recovery,
            PowerAction.Bootloader,
            PowerAction.SystemUI
        )
        
        actions.forEach { action ->
            PowerMenuItem(action = action, onClick = { onAction(action) })
        }
    }
}

@Composable
fun PowerMenuItem(action: PowerAction, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = action.getLocalizedLabel(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
