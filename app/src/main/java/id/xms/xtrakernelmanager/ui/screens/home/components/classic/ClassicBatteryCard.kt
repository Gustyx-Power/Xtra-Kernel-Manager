package id.xms.xtrakernelmanager.ui.screens.home.components.classic

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import java.util.Locale

@Composable
fun ClassicBatteryCard(batteryInfo: BatteryInfo) {
    // Detect charging status - use equals instead of contains to avoid false positives
    val isCharging = batteryInfo.status.equals("Charging", ignoreCase = true) || 
                     batteryInfo.status.equals("Full", ignoreCase = true)
    
    // Fast charging detection: current > 1000mA while charging
    val isFastCharging = isCharging && batteryInfo.currentNow > 1000
    
    // Calculate estimated time to full charge
    val estimatedTime = if (isCharging && batteryInfo.currentNow > 0 && batteryInfo.totalCapacity > 0) {
        val remainingCapacity = batteryInfo.totalCapacity * (100 - batteryInfo.level) / 100
        val hoursToFull = remainingCapacity.toFloat() / batteryInfo.currentNow
        val minutesToFull = (hoursToFull * 60).toInt()
        
        when {
            minutesToFull <= 0 -> null
            minutesToFull < 60 -> "${minutesToFull}m to full"
            minutesToFull < 120 -> "1h ${minutesToFull % 60}m to full"
            else -> "${minutesToFull / 60}h ${minutesToFull % 60}m to full"
        }
    } else null
    
    ClassicCard(title = "", icon = Icons.Rounded.BatteryFull, isLarge = true, hideHeader = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large percentage with bolt icon
            Box(contentAlignment = Alignment.TopEnd) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = "${batteryInfo.level}",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 120.sp),
                        fontWeight = FontWeight.Black,
                        color = ClassicColors.Primary,
                        lineHeight = 120.sp
                    )
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 72.sp),
                        fontWeight = FontWeight.Black,
                        color = ClassicColors.Primary,
                        lineHeight = 72.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                if (isCharging) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = ClassicColors.Primary,
                        modifier = Modifier
                            .size(48.dp)
                            .offset(x = 16.dp, y = (-8).dp)
                    )
                }
            }
            
            // Charging status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = when {
                        isFastCharging -> "Fast Charging"
                        isCharging -> "Charging"
                        else -> batteryInfo.status
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface
                )
                
                if (estimatedTime != null) {
                    Text(
                        text = "Estimated $estimatedTime",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClassicColors.OnSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Stats grid - 2 columns x 3 rows
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Row 1: Health | Technology
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BatteryStatItem(
                        label = "HEALTH",
                        value = batteryInfo.health,
                        modifier = Modifier.weight(1f)
                    )
                    BatteryStatItem(
                        label = "TECHNOLOGY",
                        value = batteryInfo.technology,
                        modifier = Modifier.weight(1f),
                        align = Alignment.End
                    )
                }
                
                // Row 2: Cycles | Temp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BatteryStatItem(
                        label = "CYCLES",
                        value = "${batteryInfo.cycleCount}",
                        modifier = Modifier.weight(1f)
                    )
                    BatteryStatItem(
                        label = "TEMP",
                        value = "${String.format(Locale.US, "%.1f", batteryInfo.temperature)}°C",
                        modifier = Modifier.weight(1f),
                        isHighlight = true,
                        align = Alignment.End
                    )
                }
                
                // Row 3: Voltage | Current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BatteryStatItem(
                        label = "VOLTAGE",
                        value = "${batteryInfo.voltage} mV",
                        modifier = Modifier.weight(1f)
                    )
                    BatteryStatItem(
                        label = "CURRENT",
                        value = "${batteryInfo.currentNow} mA",
                        modifier = Modifier.weight(1f),
                        align = Alignment.End
                    )
                }
            }
        }
    }
}

@Composable
private fun BatteryStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false,
    align: Alignment.Horizontal = Alignment.Start
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = align
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ClassicColors.OnSurfaceVariant,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) ClassicColors.Primary else ClassicColors.OnSurface
        )
    }
}
