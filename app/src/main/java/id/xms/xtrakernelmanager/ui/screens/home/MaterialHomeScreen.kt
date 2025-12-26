package id.xms.xtrakernelmanager.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import java.util.Locale

/**
 * Material Home Screen - Pure Material 3 design
 * Based on mockup anu.html with clean, simple aesthetics
 */
@SuppressLint("DefaultLocale")
@Composable
fun MaterialHomeScreen(
    preferencesManager: PreferencesManager,
    viewModel: HomeViewModel = viewModel(),
    onPowerMenuClick: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Data State
    val cpuInfo by viewModel.cpuInfo.collectAsState()
    val gpuInfo by viewModel.gpuInfo.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val systemInfo by viewModel.systemInfo.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBatteryInfo(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            MaterialHeader(onPowerMenuClick = onPowerMenuClick)
        }

        // Device Info Card
        item {
            MaterialDeviceCard(
                deviceName = systemInfo.deviceModel,
                codename = systemInfo.fingerprint.substringAfterLast("/").substringBefore(":"),
                kernelVersion = systemInfo.kernelVersion.substringBefore("-").take(10)
            )
        }

        // Quick Stat Tiles (2x2 Grid)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MaterialStatTile(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.cpu_load),
                    value = "${String.format(Locale.US, "%.0f", cpuInfo.totalLoad)}%",
                    subValue = "${cpuInfo.cores.maxOfOrNull { it.currentFreq } ?: 0} MHz",
                    icon = Icons.Default.Memory,
                    iconTint = Color(0xFFF4B0B9)
                )
                MaterialStatTile(
                    modifier = Modifier.weight(1f),
                    label = "GPU Freq",
                    value = "${gpuInfo.currentFreq}",
                    subValue = "MHz",
                    icon = Icons.Default.Videocam,
                    iconTint = Color(0xFF8AB4F8)
                )
            }
        }

        // GPU Information Card
        item {
            MaterialGpuCard(
                gpuModel = gpuInfo.vendor.ifBlank { "Unknown GPU" },
                currentFreq = gpuInfo.currentFreq,
                load = 45, // Placeholder - add real GPU load if available
                renderer = gpuInfo.renderer.ifBlank { "Vulkan" }
            )
        }

        // Memory & Storage Card
        item {
            MaterialMemoryCard(
                ramUsed = (systemInfo.totalRam - systemInfo.availableRam) / (1024f * 1024f * 1024f),
                ramTotal = systemInfo.totalRam / (1024f * 1024f * 1024f),
                storageUsed = (systemInfo.totalStorage - systemInfo.availableStorage) / (1024f * 1024f * 1024f),
                storageTotal = systemInfo.totalStorage / (1024f * 1024f * 1024f),
                swapUsed = (systemInfo.swapTotal - systemInfo.swapFree) / (1024f * 1024f * 1024f),
                swapTotal = systemInfo.swapTotal / (1024f * 1024f * 1024f)
            )
        }

        // Battery Card
        item {
            MaterialBatteryCard(
                level = batteryInfo.level,
                status = batteryInfo.status,
                health = batteryInfo.health,
                healthPercent = batteryInfo.healthPercent,
                currentNow = batteryInfo.currentNow,
                voltage = batteryInfo.voltage,
                temperature = batteryInfo.temperature,
                cycleCount = batteryInfo.cycleCount
            )
        }
    }
}

@Composable
private fun MaterialHeader(
    onPowerMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "XKM",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        FilledTonalIconButton(
            onClick = onPowerMenuClick,
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.PowerSettingsNew,
                contentDescription = "Power Menu",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun MaterialDeviceCard(
    deviceName: String,
    codename: String,
    kernelVersion: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "DEVICE INFO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFB786),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = deviceName.ifBlank { "Unknown Device" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = codename.ifBlank { "Unknown" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = kernelVersion.ifBlank { "Unknown" },
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Phone Icon Placeholder
            Box(
                modifier = Modifier
                    .size(80.dp, 100.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun MaterialStatTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    iconTint: Color
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
                color = iconTint.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MaterialGpuCard(
    gpuModel: String,
    currentFreq: Int,
    load: Int,
    renderer: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.gpu_information),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = gpuModel.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFB786),
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Main Stat
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "$currentFreq MHz",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Sub Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GpuSubBox(
                    modifier = Modifier.weight(1f),
                    value = "$load%",
                    label = "Load"
                )
                GpuSubBox(
                    modifier = Modifier.weight(1f),
                    value = renderer.take(10),
                    label = "Renderer"
                )
            }
        }
    }
}

@Composable
private fun GpuSubBox(
    modifier: Modifier = Modifier,
    value: String,
    label: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun MaterialMemoryCard(
    ramUsed: Float,
    ramTotal: Float,
    storageUsed: Float,
    storageTotal: Float,
    swapUsed: Float,
    swapTotal: Float
) {
    val ramPercent = if (ramTotal > 0) (ramUsed / ramTotal).coerceIn(0f, 1f) else 0f
    val storagePercent = if (storageTotal > 0) (storageUsed / storageTotal).coerceIn(0f, 1f) else 0f
    val swapPercent = if (swapTotal > 0) (swapUsed / swapTotal).coerceIn(0f, 1f) else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.memory_storage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // RAM
            MemoryProgressBar(
                label = "RAM (${(ramPercent * 100).toInt()}%)",
                used = String.format(Locale.US, "%.1f", ramUsed),
                total = String.format(Locale.US, "%.1f GB", ramTotal),
                progress = ramPercent,
                color = Color(0xFFA7F3D0) // Mint green
            )

            // Storage
            MemoryProgressBar(
                label = "Internal Storage",
                used = String.format(Locale.US, "%.0f", storageUsed),
                total = String.format(Locale.US, "%.0f GB", storageTotal),
                progress = storagePercent,
                color = Color(0xFFBAE6FD) // Light blue
            )

            // Swap (only if exists)
            if (swapTotal > 0) {
                MemoryProgressBar(
                    label = "ZRAM / Swap",
                    used = String.format(Locale.US, "%.1f", swapUsed),
                    total = String.format(Locale.US, "%.1f GB", swapTotal),
                    progress = swapPercent,
                    color = Color(0xFFFDE047) // Yellow
                )
            }
        }
    }
}

@Composable
private fun MemoryProgressBar(
    label: String,
    used: String,
    total: String,
    progress: Float,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$used / $total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun MaterialBatteryCard(
    level: Int,
    status: String,
    health: String,
    healthPercent: Float,
    currentNow: Int,
    voltage: Int,
    temperature: Float,
    cycleCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFB786).copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = Color(0xFFFFB786),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.battery_information),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Main Battery Display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Battery Visual
                BatteryVisual(level = level)
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "$level%",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF4B0B9)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BatteryChip(text = status)
                        BatteryChip(text = "$health ${String.format(Locale.US, "%.0f", healthPercent)}%")
                    }
                }
            }

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BatteryStatBox(
                        value = if (currentNow >= 0) "+$currentNow mA" else "$currentNow mA",
                        label = stringResource(R.string.current_now)
                    )
                    BatteryStatBox(
                        value = "${String.format(Locale.US, "%.1f", temperature)}°C",
                        label = "Temperature"
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BatteryStatBox(
                        value = "$voltage mV",
                        label = stringResource(R.string.voltage)
                    )
                    BatteryStatBox(
                        value = "$cycleCount",
                        label = stringResource(R.string.cycle_count)
                    )
                }
            }
        }
    }
}

@Composable
private fun BatteryVisual(level: Int) {
    Box(
        modifier = Modifier
            .width(50.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(level / 100f)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF4B0B9))
        )
    }
}

@Composable
private fun BatteryChip(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun BatteryStatBox(
    value: String,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
