package id.xms.xtrakernelmanager.ui.screens.home.components.frosted

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FrostedTempTile(
    modifier: Modifier = Modifier,
    cpuTemp: Int,
    gpuTemp: Int,
    pmicTemp: Int,
    thermalTemp: Int,
    color: Color
) {
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
    
    FrostedSharedCard(modifier = modifier, contentPadding = PaddingValues(0.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(glassBackground)
                .border(
                    width = if (isDarkTheme) 0.8.dp else 1.2.dp,
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.large
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = tileBackground,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Thermostat,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        color = tileBackground,
                        shape = CircleShape,
                        border = BorderStroke(0.8.dp, tileBorder)
                    ) {
                        Text(
                            text = "TEMP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List of Temps
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TempRow("CPU", "${cpuTemp}°C", textSecondaryColor, textColor)
                    TempRow("GPU", "${gpuTemp}°C", textSecondaryColor, textColor)
                    TempRow("PMIC", "${pmicTemp}°C", textSecondaryColor, textColor)
                    TempRow("Thermal", "${thermalTemp}°C", textSecondaryColor, textColor)
                }
            }
        }
    }
}

@Composable
private fun TempRow(label: String, value: String, labelColor: Color, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = labelColor,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}
