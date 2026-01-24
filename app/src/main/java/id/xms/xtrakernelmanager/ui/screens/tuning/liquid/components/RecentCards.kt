package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.ClusterInfo
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.data.model.RAMConfig

// Base Card Component Style
@Composable
fun RecentCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    windowContent: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp) // Fixed width for carousel feel, or fillMaxWidth() if handled by Pager
            .padding(horizontal = 8.dp) // Gap between cards handled by Pager padding actually
            .clickable(onClick = onClick)
    ) {
        // App Header (Icon + Title)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface 
                // In fidelity html it was white on raw bg, here we are in app theme.
                // Assuming MaterialTheme handles contrast.
            )
        }

        // App Window (The main content area)
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            ),
            border = null // Or slight border if needed
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Window Top Bar (Traffic Lights)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFF5F56))) // Red
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFFBD2E))) // Yellow
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF27C93F))) // Green
                }

                // Window Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    windowContent()
                }
            }
        }
    }
}

@Composable
fun RecentCPUCard(
    clusters: List<ClusterInfo>,
    onClick: () -> Unit
) {
    val emeraldColor = Color(0xFF10B981) // Emerald 500 equivalent
    
    RecentCard(
        title = stringResource(R.string.cpu_control),
        icon = Icons.Default.Memory,
        iconColor = emeraldColor,
        onClick = onClick
    ) {
        // Content mimicking HTML: Big Text for Cores
        val totalCores = clusters.sumOf { it.cores.size }
        val primaryCluster = clusters.firstOrNull()
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = emeraldColor.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, emeraldColor.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "walt", // Governor placeholder, or get from VM
                    color = emeraldColor, // emerald-300
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$totalCores Cores",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Running at optimal freq", // Placeholder
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Freq Bars
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val minFreq = primaryCluster?.minFreq ?: 0
            val maxFreq = primaryCluster?.maxFreq ?: 0
            
            FreqBar(label = "Min Freq", value = "$minFreq MHz", percent = 0.4f, color = emeraldColor)
            FreqBar(label = "Max Freq", value = "$maxFreq MHz", percent = 0.9f, color = emeraldColor)
        }
    }
}

@Composable
fun FreqBar(label: String, value: String, percent: Float, color: Color) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.labelSmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            }
            LinearProgressIndicator(
                progress = { percent },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            )
        }
    }
}

@Composable
fun RecentGPUCard(
    gpuInfo: GPUInfo,
    onClick: () -> Unit
) {
    val purpleColor = Color(0xFFA855F7) // Purple 500
    
    RecentCard(
        title = stringResource(R.string.gpu_control),
        icon = Icons.Default.Games, // Gamepad icon ideally
        iconColor = purpleColor,
        onClick = onClick
    ) {
        // Decorative background blob if possible, skipping for now to keep simple compose
        
        Column {
            Text(
                text = "RENDERER",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = gpuInfo.rendererType.ifEmpty { "Adreno 710" }, // Fallback/Placeholder
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Feature Badges
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(purpleColor.copy(alpha = 0.2f))
                        .border(1.dp, purpleColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Bolt, null, tint = purpleColor)
                        Text("Boost", style = MaterialTheme.typography.labelSmall, color = purpleColor)
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Layers, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("OpenGL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Simulated Slider
        Column {
            Text("Power Level", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
             LinearProgressIndicator(
                progress = { 0.5f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = purpleColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            )
        }
    }
}

@Composable
fun RecentThermalCard(
    thermalPreset: String,
    onClick: () -> Unit
) {
    val roseColor = Color(0xFFF43F5E) // Rose 500
    
    RecentCard(
        title = stringResource(R.string.thermal_control),
        icon = Icons.Default.Thermostat,
        iconColor = roseColor,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .border(6.dp, roseColor.copy(alpha = 0.2f), CircleShape)
            ) {
                Text(
                    text = "42°", // Placeholder
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = roseColor.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, roseColor.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = thermalPreset.ifEmpty { "Throttling OFF" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = roseColor
                    )
                    Text(
                        text = "System performance managed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = roseColor.copy(alpha = 0.7f)
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
    val blueColor = Color(0xFF3B82F6) // Blue 500
    
    RecentCard(
        title = stringResource(R.string.ram_control),
        icon = Icons.Default.Memory,
        iconColor = blueColor,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(blueColor.copy(alpha = 0.05f))
                .border(1.dp, blueColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ZRAM",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
             Text(
                text = if(ramConfig.zramSize > 0) "${ramConfig.zramSize} MB" else "Disabled",
                style = MaterialTheme.typography.titleMedium,
                color = blueColor
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Swappiness", style = MaterialTheme.typography.titleSmall)
            Box(
                Modifier
                    .width(40.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(blueColor)
            )
        }
    }
}

@Composable
fun RecentAdditionalCard(
    onClick: () -> Unit
) {
    val grayColor = Color(0xFF64748B) // Slate 500

    RecentCard(
        title = "More",
        icon = Icons.Default.MoreHoriz,
        iconColor = grayColor,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = grayColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Advanced Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = grayColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "I/O Scheduler, TCP\nPer-App Profiles",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
