package id.xms.xtrakernelmanager.ui.screens.home.components.frosted

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.res.stringResource
import java.util.Locale

@Composable
fun FrostedCPUCard(cpuInfo: CPUInfo, modifier: Modifier = Modifier) {
    val isDarkTheme = isSystemInDarkTheme()
    
    val textColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.95f)
    } else {
        Color(0xFF2C2C2C).copy(alpha = 0.85f)
    }
    
    val textSecondaryColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.65f)
    } else {
        Color(0xFF5A5A5A).copy(alpha = 0.7f)
    }
    
    val accentColor = if (isDarkTheme) {
        Color(0xFF4CAF50)
    } else {
        Color(0xFF2E7D32)
    }
    
    val iconBackground = if (isDarkTheme) {
        Color(0xFF000000).copy(alpha = 0.35f)
    } else {
        Color.White.copy(alpha = 0.55f)
    }
    
    val iconBorder = if (isDarkTheme) {
        Color.White.copy(alpha = 0.18f)
    } else {
        Color.White.copy(alpha = 0.5f)
    }
    
    FrostedSharedCard(modifier = modifier) {
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
                        .background(iconBackground)
                        .border(0.8.dp, iconBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    stringResource(id.xms.xtrakernelmanager.R.string.cpu), 
                    style = MaterialTheme.typography.titleSmall, 
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            Text(
                text = cpuInfo.cores.firstOrNull()?.governor ?: stringResource(id.xms.xtrakernelmanager.R.string.unknown),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                color = accentColor,
                modifier = Modifier
                    .background(iconBackground, RoundedCornerShape(4.dp))
                    .border(0.8.dp, iconBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format(Locale.US, "%.0f", cpuInfo.totalLoad),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, fontSize = 36.sp),
                    color = textColor
                )
                Text(
                    text = "%",
                    style = MaterialTheme.typography.titleMedium.copy(color = textSecondaryColor),
                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                )
            }
            Text(
                text = stringResource(id.xms.xtrakernelmanager.R.string.total_load),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                color = textSecondaryColor
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .drawBehind {
                    drawLine(
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(id.xms.xtrakernelmanager.R.string.temp), 
                    style = MaterialTheme.typography.labelSmall, 
                    color = textSecondaryColor
                )
                Text(
                    "${cpuInfo.temperature}°C",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold),
                    color = if (isDarkTheme) Color(0xFFFF6B6B) else Color(0xFFD32F2F)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(id.xms.xtrakernelmanager.R.string.active), 
                    style = MaterialTheme.typography.labelSmall, 
                    color = textSecondaryColor
                )
                Text(
                    "${cpuInfo.cores.count { it.isOnline }}/${cpuInfo.cores.size}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold),
                    color = accentColor
                )
            }
        }
    }
}

@Composable
fun FrostedExpandedCoresCard(cpuInfo: CPUInfo, modifier: Modifier = Modifier) {
    val isDarkTheme = isSystemInDarkTheme()
    
    val textColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.95f)
    } else {
        Color(0xFF2C2C2C).copy(alpha = 0.85f)
    }
    
    FrostedSharedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
             Text(
                 stringResource(id.xms.xtrakernelmanager.R.string.cpu_cores), 
                 style = MaterialTheme.typography.titleMedium, 
                 fontWeight = FontWeight.Bold, 
                 color = textColor
             )
             Spacer(modifier = Modifier.height(16.dp))
             
             val rows = cpuInfo.cores.chunked(4)
             Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                 rows.forEach { rowCores ->
                     Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                         rowCores.forEach { core ->
                            val load = if(core.maxFreq > 0) core.currentFreq.toFloat() / core.maxFreq.toFloat() else 0f
                            val isPerf = core.coreNumber >= 4
                            SingleCoreBar(coreNumber = core.coreNumber, load = load, isPerformance = isPerf, isDarkTheme = isDarkTheme)
                         }
                     }
                 }
             }
        }
    }
}

@Composable
fun SingleCoreBar(coreNumber: Int, load: Float, isPerformance: Boolean, isDarkTheme: Boolean) {
    val barBackground = if (isDarkTheme) {
        Color(0xFF000000).copy(alpha = 0.3f)
    } else {
        Color.White.copy(alpha = 0.5f)
    }
    
    val barColor = if (isPerformance) {
        if (isDarkTheme) Color(0xFF4CAF50) else Color(0xFF2E7D32)
    } else {
        if (isDarkTheme) Color(0xFF2196F3) else Color(0xFF1565C0)
    }
    
    val textColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.65f)
    } else {
        Color(0xFF5A5A5A).copy(alpha = 0.7f)
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
        Box(
            modifier = Modifier
                .height(40.dp)
                .width(8.dp)
                .clip(RoundedCornerShape(100))
                .background(barBackground),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = load.coerceIn(0.1f, 1f))
                    .background(barColor, RoundedCornerShape(100))
            )
        }
        Text(
            text = "$coreNumber",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
            color = textColor,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
