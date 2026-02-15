package id.xms.xtrakernelmanager.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.model.CPUInfo
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.ui.model.PowerAction
import id.xms.xtrakernelmanager.ui.model.getLocalizedLabel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import java.util.Locale

@Composable
fun ClassicHomeScreen(
    cpuInfo: CPUInfo,
    gpuInfo: GPUInfo,
    batteryInfo: BatteryInfo,
    systemInfo: SystemInfo,
    currentProfile: String,
    onProfileChange: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onPowerAction: (PowerAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClassicColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        ClassicHeader(onSettingsClick)

        // Device Info
        ClassicCard(title = "Device Info", icon = Icons.Rounded.Smartphone) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ClassicInfoRow("Model", systemInfo.deviceModel)
                ClassicInfoRow("Android", systemInfo.androidVersion)
                ClassicInfoRow("Kernel", systemInfo.kernelVersion.substringBefore("-"))
            }
        }

        // CPU Info
        ClassicCard(title = "CPU Status", icon = Icons.Rounded.Memory) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.0f", cpuInfo.totalLoad)}%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                    Text(
                        text = "${cpuInfo.temperature}°C",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (cpuInfo.temperature > 65) ClassicColors.Critical else ClassicColors.Good
                    )
                }
                
                Divider(color = ClassicColors.SurfaceVariant)
                
                // Cores - Simple Grid
                if (cpuInfo.cores.isNotEmpty()) {
                    val maxFreq = cpuInfo.cores.maxOfOrNull { it.currentFreq } ?: 1
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        cpuInfo.cores.chunked(4).forEach { rowCores ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                rowCores.forEach { core ->
                                    val isHigh = core.currentFreq == maxFreq && core.isOnline
                                    Text(
                                        text = if (core.isOnline) "${core.currentFreq}" else "OFF",
                                        color = if (isHigh) ClassicColors.Secondary else ClassicColors.OnSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isHigh) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.width(60.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                     Text("Governor", style = MaterialTheme.typography.bodySmall, color = ClassicColors.OnSurfaceVariant)
                     Text(cpuInfo.cores.firstOrNull()?.governor ?: "Unknown", style = MaterialTheme.typography.bodySmall, color = ClassicColors.OnSurface, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        // GPU Info
        ClassicCard(title = "GPU Status", icon = Icons.Rounded.Videocam) {
             Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 Column {
                     Text(
                        text = "${gpuInfo.gpuLoad}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                    Text("Load", style = MaterialTheme.typography.labelSmall, color = ClassicColors.OnSurfaceVariant)
                 }
                 
                 Column(horizontalAlignment = Alignment.End) {
                     Text(
                        text = "${gpuInfo.currentFreq} MHz",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.Primary
                    )
                    Text(
                        text = "Current Freq",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassicColors.OnSurfaceVariant
                    )
                 }
            }
        }

        // Memory (RAM & Storage)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // RAM
            Box(modifier = Modifier.weight(1f)) {
                ClassicCard(title = "RAM", icon = Icons.Rounded.Memory) {
                    val usedRam = systemInfo.totalRam - systemInfo.availableRam
                    val totalRam = systemInfo.totalRam
                    val percent = if (totalRam > 0) (usedRam * 100 / totalRam).toInt() else 0
                    
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                         Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        LinearProgressIndicator(
                            progress = { percent / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = ClassicColors.Primary,
                            trackColor = ClassicColors.SurfaceVariant,
                        )
                        Text(
                            text = "${usedRam / 1024 / 1024} / ${totalRam / 1024 / 1024} MB",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClassicColors.OnSurfaceVariant
                        )
                    }
                }
            }
            
            // Storage
            Box(modifier = Modifier.weight(1f)) {
                 ClassicCard(title = "Storage", icon = Icons.Rounded.Storage) {
                    val usedStorage = systemInfo.totalStorage - systemInfo.availableStorage
                    val totalStorage = systemInfo.totalStorage
                    val percent = if (totalStorage > 0) (usedStorage * 100 / totalStorage).toInt() else 0
                    
                     Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                         Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        LinearProgressIndicator(
                            progress = { percent / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = ClassicColors.Accent,
                            trackColor = ClassicColors.SurfaceVariant,
                        )
                        Text(
                            text = "${usedStorage / 1024 / 1024 / 1024} / ${totalStorage / 1024 / 1024 / 1024} GB",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClassicColors.OnSurfaceVariant
                        )
                    }
                }
            }
        }

        // Battery
        ClassicCard(title = "Battery Info", icon = Icons.Rounded.BatteryFull) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                         Text(
                            text = "${batteryInfo.level}%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        Text(
                            text = batteryInfo.status,
                            style = MaterialTheme.typography.titleMedium,
                            color = ClassicColors.Secondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
                
                Divider(color = ClassicColors.SurfaceVariant)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("${batteryInfo.temperature}°C", fontWeight = FontWeight.Bold, color = ClassicColors.OnSurface)
                        Text("Temp", style = MaterialTheme.typography.labelSmall, color = ClassicColors.OnSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("${batteryInfo.voltage}mV", fontWeight = FontWeight.Bold, color = ClassicColors.OnSurface)
                        Text("Voltage", style = MaterialTheme.typography.labelSmall, color = ClassicColors.OnSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("${batteryInfo.currentNow}mA", fontWeight = FontWeight.Bold, color = ClassicColors.OnSurface)
                        Text("Current", style = MaterialTheme.typography.labelSmall, color = ClassicColors.OnSurfaceVariant)
                    }
                }
            }
        }

        // Profile Selector
        Text(
            text = "Performance Profile",
            style = MaterialTheme.typography.titleMedium,
            color = ClassicColors.Secondary,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ClassicColors.Surface, RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val profiles = listOf("powersave", "balance", "performance")
            profiles.forEach { profile ->
                val isSelected = currentProfile == profile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) ClassicColors.Primary else Color.Transparent)
                        .clickable { onProfileChange(profile) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        color = if (isSelected) ClassicColors.OnSurface else ClassicColors.OnSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Power Menu Grid
         Text(
            text = "Power Menu",
            style = MaterialTheme.typography.titleMedium,
            color = ClassicColors.Secondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val actions = PowerAction.values().toList().chunked(2)
            actions.forEach { rowActions ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowActions.forEach { action ->
                        Button(
                            onClick = { onPowerAction(action) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ClassicColors.Surface,
                                contentColor = ClassicColors.OnSurface
                            )
                        ) {
                             Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(action.icon, null, modifier = Modifier.size(20.dp), tint = ClassicColors.Secondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(action.getLocalizedLabel(), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                    if (rowActions.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ClassicHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Xtra Kernel Manager",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface
            )
            Text(
                text = "Classic Mode",
                style = MaterialTheme.typography.labelMedium,
                color = ClassicColors.Secondary
            )
        }
        
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = ClassicColors.OnSurface
            )
        }
    }
}

@Composable
fun ClassicCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ClassicColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(icon, null, tint = ClassicColors.Primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface
                )
            }
            content()
        }
    }
}

@Composable
fun ClassicInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = ClassicColors.OnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, color = ClassicColors.OnSurface, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
