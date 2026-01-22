package id.xms.xtrakernelmanager.ui.screens.home.components.liquid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.data.model.CPUInfo
import id.xms.xtrakernelmanager.ui.theme.NeonBlue
import id.xms.xtrakernelmanager.ui.theme.NeonGreen
import id.xms.xtrakernelmanager.ui.theme.NeonRose
import java.util.Locale

@Composable
fun LiquidCPUCard(cpuInfo: CPUInfo, modifier: Modifier = Modifier) {
    LiquidSharedCard(modifier = modifier) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text("CPU", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Text(
                text = cpuInfo.cores.firstOrNull()?.governor ?: "unknown",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                color = NeonGreen,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .border(1.dp, NeonGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Main Number
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format(Locale.US, "%.0f", cpuInfo.totalLoad),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, fontSize = 36.sp),
                    color = Color.White
                )
                Text(
                    text = "%",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.Gray),
                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                )
            }
            Text(
                text = "TOTAL LOAD",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                color = Color.Gray
            )
        }

        // Footer Stats
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .drawBehind {
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Temp", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(
                    "${cpuInfo.temperature}°C",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold),
                    color = NeonRose
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Active", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(
                    "${cpuInfo.cores.count { it.isOnline }}/${cpuInfo.cores.size}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold),
                    color = NeonGreen
                )
            }
        }
    }
}

@Composable
fun LiquidExpandedCoresCard(cpuInfo: CPUInfo, modifier: Modifier = Modifier) {
    LiquidSharedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
             Text("CPU Cores", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
             Spacer(modifier = Modifier.height(16.dp))
             
             // Simple grid logic for 8 cores
             val rows = cpuInfo.cores.chunked(4)
             Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                 rows.forEach { rowCores ->
                     Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                         rowCores.forEach { core ->
                            val load = if(core.maxFreq > 0) core.currentFreq.toFloat() / core.maxFreq.toFloat() else 0f
                            val isPerf = core.coreNumber >= 4 // Rough guess 
                            SingleCoreBar(coreNumber = core.coreNumber, load = load, isPerformance = isPerf)
                         }
                     }
                 }
             }
        }
    }
}

@Composable
fun SingleCoreBar(coreNumber: Int, load: Float, isPerformance: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
        Box(
            modifier = Modifier
                .height(40.dp)
                .width(8.dp)
                .clip(RoundedCornerShape(100))
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = load.coerceIn(0.1f, 1f))
                    .background(
                        if (isPerformance) NeonGreen else NeonBlue,
                        RoundedCornerShape(100)
                    )
            )
        }
        Text(
            text = "$coreNumber",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
