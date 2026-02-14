package id.xms.xtrakernelmanager.ui.screens.home.components.liquid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.draw.clip
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.ui.theme.NeonYellow
import java.util.Locale

@Composable
fun LiquidBatteryCard(batteryInfo: BatteryInfo, modifier: Modifier = Modifier) {
    LiquidSharedCard(
        modifier = modifier,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0b5394).copy(alpha = 0.85f))
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.2f), MaterialTheme.shapes.medium),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BatteryChargingFull,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = stringResource(id.xms.xtrakernelmanager.R.string.liquid_battery_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = batteryInfo.technology.takeIf { it != "Unknown" } ?: stringResource(id.xms.xtrakernelmanager.R.string.default_li_ion),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    val batteryColor = when {
                        batteryInfo.level >= 80 -> Color(0xFF4CAF50) // Green
                        batteryInfo.level >= 60 -> Color(0xFFFFEB3B) // Yellow
                        batteryInfo.level >= 30 -> Color(0xFFFF9800) // Orange
                        else -> Color(0xFFD32F2F) // Dark Red
                    }
                    
                    LiquidBatterySilhouette(
                        level = batteryInfo.level / 100f,
                        isCharging = batteryInfo.status.contains("Charging", ignoreCase = true),
                        color = batteryColor
                    )

                    Column {
                        Text(
                            text = "${batteryInfo.level}%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 1.em
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Local implementation of Chip
                            Surface(color = Color.White.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                                Text(
                                    text = batteryInfo.status,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                            Surface(color = Color.White.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                                Text(
                                    text = "Health ${String.format(Locale.US, "%.0f", batteryInfo.healthPercent)}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Stats Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val currentText = if (batteryInfo.currentNow >= 0) "+${batteryInfo.currentNow} mA" else "${batteryInfo.currentNow} mA"
                        WhiteBatteryStatBox(label = stringResource(id.xms.xtrakernelmanager.R.string.liquid_battery_current), value = currentText, modifier = Modifier.weight(1f))
                        WhiteBatteryStatBox(label = stringResource(id.xms.xtrakernelmanager.R.string.liquid_battery_voltage), value = "${batteryInfo.voltage} mV", modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        WhiteBatteryStatBox(label = stringResource(id.xms.xtrakernelmanager.R.string.liquid_battery_temperature), value = "${batteryInfo.temperature}°C", modifier = Modifier.weight(1f))
                        WhiteBatteryStatBox(label = stringResource(id.xms.xtrakernelmanager.R.string.liquid_battery_cycle_count), value = "${batteryInfo.cycleCount}", modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun WhiteBatteryStatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
