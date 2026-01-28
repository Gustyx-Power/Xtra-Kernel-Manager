package id.xms.xtrakernelmanager.ui.components.statusbar

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
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
 * iOS-style Status Bar for Liquid Theme
 * Mimics iOS status bar with signal, WiFi, battery, and time
 */
@SuppressLint("SimpleDateFormat")
@Composable
fun LiquidStatusBar(
    modifier: Modifier = Modifier,
    batteryLevel: Int = 85,
    isCharging: Boolean = false,
    signalStrength: Int = 4, // 0-4
    wifiEnabled: Boolean = true,
) {
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm").format(Date())) }
    
    // Use theme-aware colors
    val contentColor = MaterialTheme.colorScheme.onBackground

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm").format(Date())
            delay(60000) // Update every minute
        }
    }

    // iOS-style blur background - extends to top edge
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        // Glassmorphic background layer - extends from absolute top (y=0)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .offset(y = 0.dp) // Ensure starts at y=0
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
        )
        
        // Content - centered vertically in the background
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Time (iOS style - centered on notch devices)
            Text(
                text = currentTime,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp
                ),
                color = contentColor
            )

            // Right: Signal, WiFi, Battery
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Signal Strength (iOS dots style)
                SignalDotsIOS(strength = signalStrength, color = contentColor)

                // WiFi Icon
                if (wifiEnabled) {
                    Icon(
                        imageVector = Icons.Rounded.Wifi,
                        contentDescription = "WiFi",
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Battery Icon with percentage
                BatteryIndicatorIOS(
                    level = batteryLevel,
                    isCharging = isCharging,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
private fun SignalDotsIOS(strength: Int, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((4 + index * 2).dp)
                    .background(
                        color = if (index < strength) color else color.copy(alpha = 0.3f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
}

@Composable
private fun BatteryIndicatorIOS(level: Int, isCharging: Boolean, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Battery percentage text
        Text(
            text = "$level%",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            color = color
        )

        // iOS-style Battery icon (custom drawable)
        Box(
            modifier = Modifier.size(width = 24.dp, height = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Battery outline
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val strokeWidth = 1.5.dp.toPx()
                val cornerRadius = 2.dp.toPx()
                
                // Battery body
                drawRoundRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                    size = androidx.compose.ui.geometry.Size(
                        width = size.width * 0.85f,
                        height = size.height
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                )
                
                // Battery terminal (tip)
                drawRoundRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        size.width * 0.88f,
                        size.height * 0.25f
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        width = size.width * 0.12f,
                        height = size.height * 0.5f
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius * 0.5f),
                    style = androidx.compose.ui.graphics.drawscope.Fill
                )
                
                // Battery fill level
                val fillWidth = (size.width * 0.85f - strokeWidth * 2) * (level / 100f)
                val fillColor = when {
                    isCharging -> Color(0xFF34C759) // iOS green
                    level <= 20 -> Color(0xFFFF3B30) // iOS red
                    else -> color
                }
                
                if (fillWidth > 0) {
                    drawRoundRect(
                        color = fillColor,
                        topLeft = androidx.compose.ui.geometry.Offset(strokeWidth, strokeWidth),
                        size = androidx.compose.ui.geometry.Size(
                            width = fillWidth,
                            height = size.height - strokeWidth * 2
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius * 0.5f),
                        style = androidx.compose.ui.graphics.drawscope.Fill
                    )
                }
            }
            
            // Charging bolt icon
            if (isCharging) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = "Charging",
                    tint = Color.White,
                    modifier = Modifier.size(8.dp)
                )
            }
        }
    }
}
