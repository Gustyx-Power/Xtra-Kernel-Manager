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
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.ui.theme.NeonYellow
import java.util.Locale

@Composable
fun LiquidBatteryCard(batteryInfo: BatteryInfo, modifier: Modifier = Modifier) {
    LiquidSharedCard(modifier = modifier) {
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
                            .background(NeonYellow.copy(alpha = 0.2f), MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BatteryChargingFull,
                            contentDescription = null,
                            tint = NeonYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = stringResource(id.xms.xtrakernelmanager.R.string.liquid_battery_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = adaptiveTextColor()
                    )
                }

                Surface(
                    color = adaptiveSurfaceColor(0.1f),
                    shape = CircleShape
                ) {
                    Text(
                        text = batteryInfo.technology.takeIf { it != "Unknown" } ?: stringResource(id.xms.xtrakernelmanager.R.string.default_li_ion),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = adaptiveTextColor(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                LiquidBatterySilhouette(
                    level = batteryInfo.level / 100f,
                    isCharging = batteryInfo.status.contains("Charging", ignoreCase = true),
                    color = NeonYellow
                )

                Column {
                    Text(
                        text = "${batteryInfo.level}%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonYellow,
                        lineHeight = 1.em
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LiquidBatteryStatusChip(text = batteryInfo.status)
                        LiquidBatteryStatusChip(text = "Health ${String.format(Locale.US, "%.0f", batteryInfo.healthPercent)}%")
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
                    LiquidBatteryStatBox(label = stringResource(id.xms.xtrakernelmanager.R.string.liquid_battery_current), value = currentText, modifier = Modifier.weight(1f))
                    LiquidBatteryStatBox(label = stringResource(id.xms.xtrakernelmanager.R.string.liquid_battery_voltage), value = "${batteryInfo.voltage} mV", modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LiquidBatteryStatBox(label = stringResource(id.xms.xtrakernelmanager.R.string.liquid_battery_temperature), value = "${batteryInfo.temperature}°C", modifier = Modifier.weight(1f))
                    LiquidBatteryStatBox(label = stringResource(id.xms.xtrakernelmanager.R.string.liquid_battery_cycle_count), value = "${batteryInfo.cycleCount}", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
