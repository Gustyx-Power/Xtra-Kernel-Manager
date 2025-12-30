package id.xms.xtrakernelmanager.ui.screens.home.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.CPUInfo
import java.util.Locale

/**
 * CPU Card - Compact box layout with large load display
 * No empty spaces, CPU icon + load in center
 */
@SuppressLint("DefaultLocale")
@Composable
fun PlayfulCPUCard(cpuInfo: CPUInfo) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    val animatedLoad by animateFloatAsState(
        targetValue = cpuInfo.totalLoad,
        animationSpec = tween(500, easing = EaseOutCubic),
        label = "cpuLoad"
    )
    
    val tempColor = when {
        cpuInfo.temperature > 70 -> MaterialTheme.colorScheme.error
        cpuInfo.temperature > 50 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    val maxFreq = cpuInfo.cores.filter { it.isOnline }.maxOfOrNull { it.currentFreq } ?: 0
    val minFreq = cpuInfo.cores.filter { it.isOnline }.minOfOrNull { it.currentFreq } ?: 0

    PlayfulInfoCard(
        title = stringResource(R.string.cpu_information),
        icon = Icons.Default.Memory,
        accentColor = primaryColor
    ) {
        // Compact header: CPU box + load + temp + governor in one row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // CPU Icon + Load (left side)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Memory,
                        null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Column {
                        Text(
                            text = "${animatedLoad.toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "CPU Load",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Temp + Governor (right side, stacked compact)
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Temperature
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = tempColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Thermostat,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = tempColor
                            )
                            Text(
                                text = "${cpuInfo.temperature}°C",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = tempColor
                            )
                        }
                    }
                    // Governor
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = cpuInfo.cores.firstOrNull()?.governor ?: "Unknown",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Cores header with active count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.clockspeed_per_core),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = onSurfaceVariant
            )
            Text(
                text = "${cpuInfo.cores.count { it.isOnline }}/${cpuInfo.cores.size} Active",
                style = MaterialTheme.typography.labelSmall,
                color = primaryColor
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Cores grid - compact
        val rows = cpuInfo.cores.chunked(4)
        rows.forEach { rowCores ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowCores.forEach { core ->
                    val freqRatio = if (maxFreq > minFreq && core.isOnline) {
                        ((core.currentFreq - minFreq).toFloat() / (maxFreq - minFreq).toFloat()).coerceIn(0f, 1f)
                    } else 0f
                    
                    CoreFreqItemMaterial(
                        coreNumber = core.coreNumber,
                        frequency = core.currentFreq,
                        isOnline = core.isOnline,
                        freqRatio = freqRatio,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - rowCores.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

/**
 * Core frequency item - compact with contrast based on freq
 */
@Composable
fun CoreFreqItemMaterial(
    coreNumber: Int,
    frequency: Int,
    isOnline: Boolean,
    freqRatio: Float,
    modifier: Modifier = Modifier
) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    val bgColor = when {
        !isOnline -> surfaceVariant.copy(alpha = 0.3f)
        else -> androidx.compose.ui.graphics.lerp(
            surfaceVariant.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.primaryContainer,
            freqRatio
        )
    }
    
    val textColor = when {
        !isOnline -> onSurfaceVariant.copy(alpha = 0.5f)
        freqRatio > 0.7f -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> onSurface
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "C$coreNumber",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = onSurfaceVariant
            )
            Text(
                text = if (isOnline) "$frequency" else "OFF",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            if (isOnline) {
                Text(
                    text = "MHz",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = onSurfaceVariant
                )
            }
        }
    }
}

/**
 * GPU Card - Material You styled
 */
@Composable
fun PlayfulGPUCard(gpuInfo: id.xms.xtrakernelmanager.data.model.GPUInfo) {
    val primaryColor = MaterialTheme.colorScheme.primary
    
    val animatedFreq by animateIntAsState(
        targetValue = gpuInfo.currentFreq,
        animationSpec = tween(300),
        label = "gpuFreq"
    )

    PlayfulInfoCard(
        title = stringResource(R.string.gpu_information),
        icon = Icons.Default.Videocam,
        accentColor = primaryColor
    ) {
        // Compact header with freq and load
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Videocam,
                        null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$animatedFreq",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = " MHz",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.current_frequency),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                if (gpuInfo.gpuLoad > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${gpuInfo.gpuLoad}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Load",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Frequency range bar
        if (gpuInfo.maxFreq > 0) {
            val progress = gpuInfo.currentFreq.toFloat() / gpuInfo.maxFreq.toFloat()
            ProgressBarWithLabel(
                label = "Freq Range",
                progress = progress,
                usedText = "${gpuInfo.minFreq}",
                totalText = "${gpuInfo.maxFreq} MHz",
                color = primaryColor
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Renderer info row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Renderer",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = gpuInfo.renderer.take(16),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Vendor",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = gpuInfo.vendor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Battery Card - Horizontal battery bar layout, compact
 */
@SuppressLint("DefaultLocale")
@Composable
fun PlayfulBatteryCard(batteryInfo: id.xms.xtrakernelmanager.data.model.BatteryInfo) {
    val isCharging = batteryInfo.status.contains("Charging", ignoreCase = true)
    
    val batteryColor = when {
        isCharging -> MaterialTheme.colorScheme.tertiary
        batteryInfo.level <= 20 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    
    val animatedLevel by animateFloatAsState(
        targetValue = batteryInfo.level.toFloat(),
        animationSpec = tween(500),
        label = "batteryLevel"
    )

    PlayfulInfoCard(
        title = stringResource(R.string.battery_information),
        icon = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
        accentColor = batteryColor
    ) {
        // Top Section: Battery Bar & Status
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp), // Less rounded (was 20.dp)
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Circular Progress or Icon
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedLevel / 100f },
                        modifier = Modifier.size(64.dp),
                        color = batteryColor,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Icon(
                        if (isCharging) Icons.Default.FlashOn else Icons.Default.BatteryStd,
                        null,
                        modifier = Modifier.size(24.dp),
                        tint = batteryColor
                    )
                }

                // Text Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${animatedLevel.toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = batteryInfo.status,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    
                    // Health Percentage Explicitly Shown
                    Text(
                        text = "Health: ${batteryInfo.health} (${String.format(Locale.US, "%.0f", batteryInfo.healthPercent)}%)",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (batteryInfo.healthPercent < 80) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Detailed Stats Grid - Tighter Spacing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp) // Very tight spacing
        ) {
            val currentText = if (batteryInfo.currentNow >= 0) "+${batteryInfo.currentNow}" else "${batteryInfo.currentNow}"
            StatChipMaterial(stringResource(R.string.current_now), "$currentText mA", Modifier.weight(1f))
            StatChipMaterial("Temp", "${batteryInfo.temperature}°C", Modifier.weight(1f))
            StatChipMaterial(stringResource(R.string.voltage), "${batteryInfo.voltage} mV", Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Extra info row - Tight spacing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
             StatChipMaterial(stringResource(R.string.cycle_count), "${batteryInfo.cycleCount}", Modifier.weight(1f))
             StatChipMaterial(stringResource(R.string.technology), batteryInfo.technology, Modifier.weight(1.5f))
        }
    }
}

@Composable
fun StatChipMaterial(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp), // Rounded square (was 12.dp)
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp), // Tighter padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium, // Smaller font for compactness
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 9.sp // Slightly smaller label
            )
        }
    }
}

/**
 * Memory & Storage Card - With ZRAM/Swap progress bar like Material layout
 */
@SuppressLint("DefaultLocale")
@Composable
fun PlayfulMemoryCard(systemInfo: id.xms.xtrakernelmanager.data.model.SystemInfo) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    val totalRamGB = systemInfo.totalRam / (1024f * 1024f * 1024f)
    val availRamGB = systemInfo.availableRam / (1024f * 1024f * 1024f)
    val usedRamGB = totalRamGB - availRamGB
    val ramProgress = if (totalRamGB > 0) (usedRamGB / totalRamGB).coerceIn(0f, 1f) else 0f
    
    val totalStorageGB = systemInfo.totalStorage / (1024f * 1024f * 1024f)
    val availStorageGB = systemInfo.availableStorage / (1024f * 1024f * 1024f)
    val usedStorageGB = totalStorageGB - availStorageGB
    val storageProgress = if (totalStorageGB > 0) (usedStorageGB / totalStorageGB).coerceIn(0f, 1f) else 0f

    PlayfulInfoCard(
        title = stringResource(R.string.memory_storage),
        icon = Icons.Default.Storage,
        accentColor = primaryColor
    ) {
        // RAM progress - Standard Layout
        ProgressBarWithLabel(
            label = "RAM",
            progress = ramProgress,
            usedText = String.format(Locale.US, "%.1f GB", usedRamGB),
            totalText = String.format(Locale.US, "%.1f GB", totalRamGB),
            color = primaryColor
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Storage progress
        ProgressBarWithLabel(
            label = stringResource(R.string.storage),
            progress = storageProgress,
            usedText = String.format(Locale.US, "%.1f GB", usedStorageGB),
            totalText = String.format(Locale.US, "%.1f GB", totalStorageGB),
            color = tertiaryColor
        )
        
        // ZRAM / Swap Section
        val showZramSwap = systemInfo.zramSize > 0 || systemInfo.swapTotal > 0
        if (showZramSwap) {
            Spacer(modifier = Modifier.height(16.dp))
            
            val swapTotal = if (systemInfo.swapTotal > 0) systemInfo.swapTotal else systemInfo.zramSize
            val swapUsed = if (systemInfo.swapTotal > 0) (systemInfo.swapTotal - systemInfo.swapFree) else 0L
            val swapProgress = if (swapTotal > 0) swapUsed.toFloat() / swapTotal.toFloat() else 0f
            val swapLabel = if (systemInfo.zramSize > 0) "ZRAM" else "Swap"
            
            val usedText = formatBytesToString(swapUsed)
            val totalText = formatBytesToString(swapTotal)
            
            ProgressBarWithLabel(
                label = swapLabel,
                progress = swapProgress,
                usedText = usedText,
                totalText = totalText,
                color = secondaryColor
            )
        }
    }
}

/**
 * Helper function to format bytes to readable string
 */
private fun formatBytesToString(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 1.0) {
        String.format(Locale.US, "%.1f GB", gb)
    } else {
        val mb = bytes / (1024.0 * 1024.0)
        String.format(Locale.US, "%.0f MB", mb)
    }
}

/**
 * System Info Card - With Background Logo & Cleaner Layout
 */
@Composable
fun PlayfulSystemCard(systemInfo: id.xms.xtrakernelmanager.data.model.SystemInfo) {
    val primaryColor = MaterialTheme.colorScheme.primary

    PlayfulInfoCard(
        title = stringResource(R.string.system_information),
        icon = Icons.Default.PhoneAndroid,
        accentColor = primaryColor
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Watermark Logo
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = null,
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 30.dp) // Bleed out slightly
                    .alpha(0.05f), // Very subtle watermark
                tint = MaterialTheme.colorScheme.primary
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 1. Device Header with Brand Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Brand-specific logo
                    val brandLogoRes = when {
                        systemInfo.brand.equals("OnePlus", ignoreCase = true) -> R.drawable.oneplus_logo
                        systemInfo.brand.equals("POCO", ignoreCase = true) -> R.drawable.poco_logo
                        systemInfo.brand.equals("Redmi", ignoreCase = true) -> R.drawable.redmi_logo
                        systemInfo.brand.equals("Xiaomi", ignoreCase = true) -> R.drawable.mi_logo
                        else -> null
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            if (brandLogoRes != null) {
                                Image(
                                    painter = painterResource(id = brandLogoRes),
                                    contentDescription = "${systemInfo.brand} Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Icon(
                                    Icons.Default.PhoneAndroid,
                                    null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Column {
                        Text(
                            text = systemInfo.deviceModel,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Android ${systemInfo.androidVersion} (${systemInfo.abi})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
                
                // 2. Info List (Clean rows)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Kernel
                    InfoRow(
                        label = "Kernel",
                        value = systemInfo.kernelVersion.substringBefore("-"),
                        icon = Icons.Default.Memory // Or any chip icon
                    )
                    
                    // SELinux
                    InfoRow(
                        label = "SELinux",
                        value = systemInfo.selinux,
                        icon = Icons.Default.Security,
                        valueColor = if (systemInfo.selinux.equals("Enforcing", true)) 
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    
                    // Build / Fingerprint
                    InfoRow(
                        label = "Build",
                        value = systemInfo.fingerprint.takeLast(20), // Truncate cleanly
                        icon = Icons.Default.Build
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f)
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
