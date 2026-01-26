package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.ClusterInfo
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.data.model.RAMConfig
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard


@Composable
fun RecentCPUCard(
    clusters: List<ClusterInfo>,
    cpuInfo: id.xms.xtrakernelmanager.data.model.CPUInfo,
    temperature: Float,
    cpuLoad: Float,
    onClick: () -> Unit
) {
    val emeraldColor = Color(0xFF10B981)
    val totalCores = clusters.sumOf { it.cores.size }
    val onlineCores = cpuInfo.cores.count { it.isOnline }
    
    // Cluster 0 is the performance cluster (CPU7 - highest frequency core)
    val performanceCluster = clusters.firstOrNull { it.clusterNumber == 0 }
    
    // Get actual running frequency from online cores in performance cluster (cluster 0)
    // Find the highest running frequency among all online cores in cluster 0
    val performanceCores = cpuInfo.cores.filter { core -> 
        core.isOnline && performanceCluster?.cores?.contains(core.coreNumber) == true 
    }
    val highestRunningFreq = performanceCores.maxOfOrNull { it.currentFreq } ?: 0
    
    // ClusterInfo frequencies are in MHz (already converted from KHz)
    // CoreInfo frequencies are in KHz (raw from system)
    // Convert both to GHz for display
    val currentFreqGHz = highestRunningFreq.toFloat() / 1000000f  // KHz to GHz
    val maxFreqGHz = (performanceCluster?.maxFreq?.toFloat() ?: 0f) / 1000f  // MHz to GHz
    
    // Format to 1 decimal place
    val currentFreqText = String.format("%.1f", currentFreqGHz)
    val maxFreqText = String.format("%.1f", maxFreqGHz)
    
    // Temperature status and color
    val (tempStatus, tempColor) = when {
        temperature < 40f -> "Cool" to Color(0xFF3B82F6) // Blue
        temperature < 60f -> "Normal" to emeraldColor // Green
        temperature < 75f -> "Warm" to Color(0xFFF59E0B) // Orange
        else -> "Hot" to Color(0xFFEF4444) // Red
    }
    
    // Get SOC information
    val socPlatform = id.xms.xtrakernelmanager.domain.native.NativeLib.getSystemProperty("ro.board.platform") ?: "Unknown"
    val socChipname = id.xms.xtrakernelmanager.domain.native.NativeLib.getSystemProperty("ro.hardware.chipname")
        ?: id.xms.xtrakernelmanager.domain.native.NativeLib.getSystemProperty("ro.soc.model")
        ?: socPlatform
    
    // Determine process node based on SOC
    val processNode = getProcessNode(socPlatform.lowercase(), socChipname.lowercase())
    
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxHeight()
            .width(340.dp),
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        contentPadding = PaddingValues(24.dp)
    ) {
        // Large Icon with Background
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            emeraldColor.copy(alpha = 0.3f),
                            emeraldColor.copy(alpha = 0.15f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = "CPU Control",
                tint = emeraldColor,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Title & Governor
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.cpu_control),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = emeraldColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = performanceCluster?.governor?.uppercase() ?: "WALT",
                    color = emeraldColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Main Stats - Large Display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Cores Online/Total
            Column {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "$onlineCores",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = emeraldColor
                    )
                    Text(
                        text = "/$totalCores",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Text(
                    text = "Cores Online",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Frequency
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = currentFreqText,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "GHz",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Text(
                    text = "Current Frequency",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Frequency Range Bar
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Frequency Range",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Max $maxFreqText GHz",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(
                            if (maxFreqGHz > 0) currentFreqGHz / maxFreqGHz else 0.5f
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    emeraldColor.copy(alpha = 0.8f),
                                    emeraldColor
                                )
                            )
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // CPU Load
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = emeraldColor.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = emeraldColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${cpuLoad.toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = emeraldColor
                    )
                    Text(
                        text = "CPU Load",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Temperature with status
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = tempColor.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = tempColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${temperature.toInt()}°C",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tempColor
                    )
                    Text(
                        text = tempStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Additional Info Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Clusters count
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${clusters.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Clusters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Governors count
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${performanceCluster?.availableGovernors?.size ?: 0}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Governors",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // SOC and Process Node Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // SOC Hardware
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = emeraldColor.copy(alpha = 0.05f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.DeveloperBoard,
                        contentDescription = null,
                        tint = emeraldColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = socChipname.uppercase().take(10),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = emeraldColor,
                        maxLines = 1
                    )
                    Text(
                        text = "SOC",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Process Node
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = emeraldColor.copy(alpha = 0.05f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = emeraldColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = processNode,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = emeraldColor
                    )
                    Text(
                        text = "Process",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
fun RecentGPUCard(
    gpuInfo: GPUInfo,
    onClick: () -> Unit
) {
    val purpleColor = Color(0xFFA855F7)
    
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxHeight()
            .width(340.dp),
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        contentPadding = PaddingValues(24.dp)
    ) {
        // Large Icon with Background
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            purpleColor.copy(alpha = 0.3f),
                            purpleColor.copy(alpha = 0.15f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Games,
                contentDescription = "GPU Control",
                tint = purpleColor,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Title
        Text(
            text = stringResource(R.string.gpu_control),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Renderer Name
        Text(
            text = gpuInfo.renderer.ifEmpty { "Adreno 710" },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = purpleColor
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Main Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // GPU Load
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = purpleColor.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = purpleColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${gpuInfo.gpuLoad}%",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = purpleColor
                    )
                    Text(
                        text = "GPU Load",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Current Frequency
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = purpleColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${gpuInfo.currentFreq}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "MHz",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Power Level
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Power Level",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${gpuInfo.powerLevel} / ${gpuInfo.numPwrLevels}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = purpleColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(
                            if (gpuInfo.numPwrLevels > 0) 
                                gpuInfo.powerLevel.toFloat() / gpuInfo.numPwrLevels 
                            else 0.5f
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    purpleColor.copy(alpha = 0.8f),
                                    purpleColor
                                )
                            )
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Features
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = purpleColor.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = purpleColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Boost",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = purpleColor
                    )
                }
            }
            
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OpenGL",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RecentThermalCard(
    thermalPreset: String,
    onClick: () -> Unit
) {
    val roseColor = Color(0xFFF43F5E)
    
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxHeight()
            .width(340.dp),
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        contentPadding = PaddingValues(24.dp)
    ) {
        // Large Icon with Background
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            roseColor.copy(alpha = 0.3f),
                            roseColor.copy(alpha = 0.15f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Thermostat,
                contentDescription = "Thermal Control",
                tint = roseColor,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Title
        Text(
            text = stringResource(R.string.thermal_control),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Temperature Display - Large
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "42",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = roseColor
                )
                Text(
                    text = "°C",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = roseColor.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(roseColor)
                    )
                    Text(
                        text = "Normal Temperature",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = roseColor
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Thermal Preset
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Thermal Preset",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = thermalPreset.ifEmpty { "Throttling OFF" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = roseColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Quick Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Stable",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cooling",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun RecentRAMCard(
    ramConfig: RAMConfig,
    onClick: () -> Unit
) {
    val blueColor = Color(0xFF3B82F6)
    
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxHeight()
            .width(340.dp),
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        contentPadding = PaddingValues(24.dp)
    ) {
        // Large Icon with Background
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            blueColor.copy(alpha = 0.3f),
                            blueColor.copy(alpha = 0.15f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = "RAM Control",
                tint = blueColor,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Title & Compression
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.ram_control),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (ramConfig.zramSize > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = blueColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = ramConfig.compressionAlgorithm.uppercase(),
                        color = blueColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // ZRAM Display - Large
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = blueColor.copy(alpha = 0.1f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ZRAM",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if(ramConfig.zramSize > 0) "${ramConfig.zramSize}" else "0",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = blueColor
                    )
                    Text(
                        text = " MB",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (ramConfig.zramSize == 0) {
                    Text(
                        text = "Disabled",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Memory Settings
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Swappiness
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = blueColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Swappiness",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "${ramConfig.swappiness}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = blueColor
                    )
                }
            }
            
            // Dirty Ratio
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = blueColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Dirty Ratio",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "${ramConfig.dirtyRatio}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = blueColor
                    )
                }
            }
        }
    }
}

@Composable
fun RecentAdditionalCard(
    onClick: () -> Unit
) {
    val grayColor = Color(0xFF64748B)

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxHeight()
            .width(340.dp),
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        contentPadding = PaddingValues(24.dp)
    ) {
        // Large Icon with Background
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            grayColor.copy(alpha = 0.3f),
                            grayColor.copy(alpha = 0.15f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Advanced Settings",
                tint = grayColor,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Title
        Text(
            text = "Advanced Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Fine-tune system performance",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Settings List - Compact
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // I/O Scheduler
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = grayColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "I/O Scheduler",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // TCP Congestion
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = null,
                            tint = grayColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "TCP Congestion",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Per-App Profiles
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = grayColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Per-App Profiles",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Performance Mode - Highlighted
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = grayColor.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = grayColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Performance Mode",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = grayColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = grayColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


// Helper function to determine process node based on SOC
private fun getProcessNode(platform: String, chipname: String): String {
    return when {
        // Snapdragon 8 Gen 3 - 4nm
        platform.contains("pineapple") || chipname.contains("8650") || chipname.contains("sm8650") -> "4nm"
        
        // Snapdragon 8 Gen 2 - 4nm
        platform.contains("kalama") || chipname.contains("8550") || chipname.contains("sm8550") -> "4nm"
        
        // Snapdragon 8 Gen 1 - 4nm
        platform.contains("taro") || chipname.contains("8475") || chipname.contains("sm8475") -> "4nm"
        platform.contains("waipio") || chipname.contains("8450") || chipname.contains("sm8450") -> "4nm"
        
        // Snapdragon 888 - 5nm
        platform.contains("lahaina") || chipname.contains("888") || chipname.contains("sm8350") -> "5nm"
        
        // Snapdragon 870/865 - 7nm
        platform.contains("kona") || chipname.contains("865") || chipname.contains("870") || chipname.contains("sm8250") -> "7nm"
        
        // Snapdragon 855 - 7nm
        platform.contains("msmnile") || chipname.contains("855") || chipname.contains("sm8150") -> "7nm"
        
        // Snapdragon 845 - 10nm
        platform.contains("sdm845") || chipname.contains("845") -> "10nm"
        
        // Snapdragon 7 series Gen 3 - 4nm
        platform.contains("parrot") || chipname.contains("7s") && chipname.contains("gen3") -> "4nm"
        
        // Snapdragon 7 series Gen 2 - 4nm
        chipname.contains("7+") && chipname.contains("gen2") -> "4nm"
        chipname.contains("7") && chipname.contains("gen2") -> "4nm"
        
        // Snapdragon 7 series Gen 1 - 4nm
        chipname.contains("7+") && chipname.contains("gen1") -> "4nm"
        chipname.contains("7") && chipname.contains("gen1") -> "4nm"
        
        // Snapdragon 6 series - 4nm/6nm
        chipname.contains("6") && chipname.contains("gen1") -> "4nm"
        chipname.contains("695") || chipname.contains("690") -> "6nm"
        chipname.contains("680") || chipname.contains("685") -> "6nm"
        
        // MediaTek Dimensity 9000 series - 4nm
        chipname.contains("mt6985") || chipname.contains("9300") -> "4nm"
        chipname.contains("mt6983") || chipname.contains("9200") -> "4nm"
        chipname.contains("mt6893") || chipname.contains("9000") -> "4nm"
        
        // MediaTek Dimensity 8000 series - 4nm/6nm
        chipname.contains("mt6895") || chipname.contains("8200") -> "4nm"
        chipname.contains("mt6891") || chipname.contains("8100") -> "5nm"
        chipname.contains("mt6877") || chipname.contains("8050") -> "6nm"
        
        // MediaTek Dimensity 7000 series - 4nm/6nm
        chipname.contains("mt6879") || chipname.contains("7050") -> "6nm"
        chipname.contains("mt6878") || chipname.contains("7200") -> "4nm"
        
        // MediaTek Dimensity 6000 series - 6nm/7nm
        chipname.contains("mt6833") || chipname.contains("6020") -> "7nm"
        chipname.contains("mt6835") || chipname.contains("6080") -> "6nm"
        
        // Exynos 2400/2200 - 4nm
        chipname.contains("s5e9945") || chipname.contains("2400") -> "4nm"
        chipname.contains("s5e9925") || chipname.contains("2200") -> "4nm"
        
        // Exynos 2100/990 - 5nm/7nm
        chipname.contains("s5e9840") || chipname.contains("2100") -> "5nm"
        chipname.contains("s5e9830") || chipname.contains("990") -> "7nm"
        
        // Tensor G3/G2/G1 - 4nm/5nm
        chipname.contains("gs201") || chipname.contains("tensor") && chipname.contains("g3") -> "4nm"
        chipname.contains("gs101") || chipname.contains("tensor") && chipname.contains("g2") -> "5nm"
        chipname.contains("tensor") -> "5nm"
        
        // Default fallback
        else -> "N/A"
    }
}
