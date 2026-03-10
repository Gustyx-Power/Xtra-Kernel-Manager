package id.xms.xtrakernelmanager.ui.screens.home.components.frosted

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import java.util.Locale

@Composable
fun FrostedBatteryCard(batteryInfo: BatteryInfo, modifier: Modifier = Modifier) {
    val isDarkTheme = isSystemInDarkTheme()
    
    val glassBackground = if (isDarkTheme) {
        Color(0xFF000000).copy(alpha = 0.35f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.45f)
    }
    
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
    
    val tileBackground = if (isDarkTheme) {
        Color(0xFF000000).copy(alpha = 0.35f)
    } else {
        Color.White.copy(alpha = 0.55f)
    }
    
    val tileBorder = if (isDarkTheme) {
        Color.White.copy(alpha = 0.18f)
    } else {
        Color.White.copy(alpha = 0.5f)
    }
    
    FrostedSharedCard(
        modifier = modifier,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(glassBackground)
                .border(
                    width = if (isDarkTheme) 0.8.dp else 1.2.dp,
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.large
                )
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
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
                                .background(tileBackground, MaterialTheme.shapes.medium)
                                .border(0.8.dp, tileBorder, MaterialTheme.shapes.medium),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BatteryChargingFull,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = stringResource(id.xms.xtrakernelmanager.R.string.frosted_battery_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    Surface(
                        color = tileBackground,
                        shape = CircleShape
                    ) {
                        Text(
                            text = batteryInfo.technology.takeIf { it != "Unknown" } ?: stringResource(id.xms.xtrakernelmanager.R.string.default_li_ion),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    val batteryColor = when {
                        batteryInfo.level >= 80 -> Color(0xFF4CAF50)
                        batteryInfo.level >= 60 -> Color(0xFFFFEB3B)
                        batteryInfo.level >= 30 -> Color(0xFFFF9800)
                        else -> Color(0xFFD32F2F)
                    }
                    
                    FrostedBatterySilhouette(
                        level = batteryInfo.level / 100f,
                        isCharging = batteryInfo.status.contains("Charging", ignoreCase = true),
                        color = batteryColor
                    )

                    Column {
                        Text(
                            text = "${batteryInfo.level}%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            lineHeight = 1.em
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(color = tileBackground, shape = MaterialTheme.shapes.small) {
                                Text(
                                    text = batteryInfo.status,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = textColor,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                            Surface(color = tileBackground, shape = MaterialTheme.shapes.small) {
                                Text(
                                    text = "Health ${String.format(Locale.US, "%.0f", batteryInfo.healthPercent)}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = textColor,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val currentText = if (batteryInfo.currentNow >= 0) "+${batteryInfo.currentNow} mA" else "${batteryInfo.currentNow} mA"
                        WhiteBatteryStatBox(
                            label = stringResource(id.xms.xtrakernelmanager.R.string.frosted_battery_current), 
                            value = currentText, 
                            textColor = textColor,
                            textSecondaryColor = textSecondaryColor,
                            tileBackground = tileBackground,
                            tileBorder = tileBorder,
                            modifier = Modifier.weight(1f)
                        )
                        WhiteBatteryStatBox(
                            label = stringResource(id.xms.xtrakernelmanager.R.string.frosted_battery_voltage), 
                            value = "${batteryInfo.voltage} mV", 
                            textColor = textColor,
                            textSecondaryColor = textSecondaryColor,
                            tileBackground = tileBackground,
                            tileBorder = tileBorder,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        WhiteBatteryStatBox(
                            label = stringResource(id.xms.xtrakernelmanager.R.string.frosted_battery_temperature), 
                            value = "${batteryInfo.temperature}°C", 
                            textColor = textColor,
                            textSecondaryColor = textSecondaryColor,
                            tileBackground = tileBackground,
                            tileBorder = tileBorder,
                            modifier = Modifier.weight(1f)
                        )
                        WhiteBatteryStatBox(
                            label = stringResource(id.xms.xtrakernelmanager.R.string.frosted_battery_cycle_count), 
                            value = "${batteryInfo.cycleCount}", 
                            textColor = textColor,
                            textSecondaryColor = textSecondaryColor,
                            tileBackground = tileBackground,
                            tileBorder = tileBorder,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WhiteBatteryStatBox(
    label: String, 
    value: String, 
    textColor: Color,
    textSecondaryColor: Color,
    tileBackground: Color,
    tileBorder: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(tileBackground)
            .border(0.8.dp, tileBorder, MaterialTheme.shapes.large)
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
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textSecondaryColor
            )
        }
    }
}
