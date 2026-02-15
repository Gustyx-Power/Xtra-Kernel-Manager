package id.xms.xtrakernelmanager.ui.screens.home.components.classic

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.model.CPUInfo
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import java.util.Locale

@Composable
fun ClassicCPUCard(cpuInfo: CPUInfo) {
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
                    text = "${String.format(Locale.US, "%.1f", cpuInfo.temperature)}°C",
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
                                // Convert KHz to MHz
                                val freqMhz = if (core.isOnline) "${core.currentFreq / 1000} MHz" else "OFF"
                                
                                Text(
                                    text = freqMhz,
                                    color = if (isHigh) ClassicColors.Secondary else ClassicColors.OnSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isHigh) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.width(70.dp)
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
}
