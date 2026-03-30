package id.xms.xtrakernelmanager.ui.screens.home.components.classic

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.offset
import id.xms.xtrakernelmanager.data.model.CPUInfo
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import java.util.Locale

@Composable
fun ClassicCPUCard(cpuInfo: CPUInfo) {
    ClassicCard(title = "CPU Status", icon = Icons.Rounded.Speed, isLarge = true) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background speedometer icon
            Icon(
                imageVector = Icons.Rounded.Speed,
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp),
                tint = ClassicColors.OnSurfaceVariant.copy(alpha = 0.15f)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    text = "Octa-Core Processing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                // Large percentage with temperature
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Big percentage
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${String.format(Locale.US, "%.0f", cpuInfo.totalLoad)}",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp),
                            fontWeight = FontWeight.Black,
                            color = ClassicColors.Primary,
                            lineHeight = 96.sp
                        )
                        Text(
                            text = "%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = ClassicColors.Primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    
                    // Temperature
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", cpuInfo.temperature)}°C",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        Text(
                            text = "NORMAL TEMP",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClassicColors.OnSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
                
                // Core progress bars (4 bars)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cpuInfo.cores.take(4).forEach { core ->
                        val load = if (core.isOnline) (core.currentFreq.toFloat() / core.maxFreq.toFloat()) else 0f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(ClassicColors.SurfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(load)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(ClassicColors.Primary)
                            )
                        }
                    }
                }
            }
        }
    }
}
