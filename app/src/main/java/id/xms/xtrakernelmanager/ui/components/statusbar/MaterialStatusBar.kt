package id.xms.xtrakernelmanager.ui.components.statusbar

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Google Pixel-style Status Bar for Material Theme
 * Mimics Pixel status bar with Material You design
 */
@SuppressLint("SimpleDateFormat")
@Composable
fun MaterialStatusBar(
    modifier: Modifier = Modifier,
    batteryLevel: Int = 85,
    isCharging: Boolean = false,
    signalStrength: Int = 4, // 0-4
    wifiEnabled: Boolean = true,
) {
    var currentTime by remember { mutableStateOf(SimpleDateFormat("h:mm").format(Date())) }
    
    val backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("h:mm").format(Date())
            delay(60000) // Update every minute
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.7f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Time (Pixel style)
            Text(
                text = currentTime,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Right: Icons (Pixel order: WiFi, Signal, Battery)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WiFi Icon
                if (wifiEnabled) {
                    Icon(
                        imageVector = Icons.Rounded.Wifi,
                        contentDescription = "WiFi",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Signal Strength (Pixel bars style)
                SignalBarsPixel(strength = signalStrength)

                // Battery Icon with percentage (Pixel style)
                BatteryIndicatorPixel(
                    level = batteryLevel,
                    isCharging = isCharging
                )
            }
        }
    }
}

@Composable
private fun SignalBarsPixel(strength: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.padding(end = 2.dp)
    ) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height((3 + index * 2.5).dp)
                    .background(
                        color = if (index < strength) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

@Composable
private fun BatteryIndicatorPixel(level: Int, isCharging: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Battery icon
        Icon(
            imageVector = when {
                isCharging -> Icons.Rounded.BatteryChargingFull
                level > 90 -> Icons.Rounded.BatteryFull
                level > 70 -> Icons.Rounded.Battery6Bar
                level > 50 -> Icons.Rounded.Battery5Bar
                level > 30 -> Icons.Rounded.Battery3Bar
                level > 15 -> Icons.Rounded.Battery2Bar
                else -> Icons.Rounded.Battery1Bar
            },
            contentDescription = "Battery",
            tint = when {
                isCharging -> MaterialTheme.colorScheme.primary
                level <= 15 -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.size(18.dp)
        )

        // Battery percentage text (Pixel shows percentage)
        Text(
            text = "$level",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
