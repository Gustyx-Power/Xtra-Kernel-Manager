package id.xms.xtrakernelmanager.ui.screens.home.components.classic

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import java.util.Locale

@Composable
fun ClassicBatteryCard(batteryInfo: BatteryInfo) {
    ClassicCard(title = "Battery Info", icon = Icons.Rounded.BatteryFull) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header: Level & Status
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
            
            // Detailed Stats Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ClassicInfoRow(label = "Health", value = batteryInfo.health)
                ClassicInfoRow(label = "Technology", value = batteryInfo.technology)
                ClassicInfoRow(label = "Cycles", value = "${batteryInfo.cycleCount}")
                
                Spacer(modifier = Modifier.height(4.dp))
                Divider(color = ClassicColors.SurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                
                // Live Stats
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("${String.format(Locale.US, "%.1f", batteryInfo.temperature / 10f)}°C", fontWeight = FontWeight.Bold, color = ClassicColors.OnSurface)
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
    }
}
