package id.xms.xtrakernelmanager.ui.screens.home.components.material

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.model.PowerInfo

/**
 * Material Design card displaying power insight information
 * Shows screen on time, screen off time, deep sleep, and drain rate
 */
@Composable
fun MaterialPowerInsightCard(
    powerInfo: PowerInfo,
    batteryInfo: BatteryInfo,
) {
    // Determine badge text and color based on charging status
    val (badgeText, badgeColor) = if (powerInfo.isCharging) {
        "Charging" to MaterialTheme.colorScheme.primaryContainer
    } else {
        "Screen On" to MaterialTheme.colorScheme.tertiaryContainer
    }
    val badgeTextColor = if (powerInfo.isCharging) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Icon + Title Group
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                                MaterialTheme.shapes.medium,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Text(
                        text = "Power Insight",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }

                // Badge (Pushed to Right)
                Surface(
                    color = badgeColor,
                    shape = CircleShape,
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            // Content: Circle + Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // SOT Circular Indicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(150.dp)
                ) {
                    // Background Track
                    WavyCircularProgressIndicator(
                        progress = 1f,
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                        strokeWidth = 16.dp,
                        amplitude = 3.dp,
                        frequency = 10,
                    )

                    // Progress (based on battery level)
                    WavyCircularProgressIndicator(
                        progress = batteryInfo.level / 100f,
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 16.dp,
                        amplitude = 3.dp,
                        frequency = 10,
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = powerInfo.formatScreenOnTime(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Stats Column
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    PowerInsightItem(
                        label = "Screen On",
                        value = powerInfo.formatScreenOnTime(),
                        icon = Icons.Rounded.LightMode,
                    )
                    PowerInsightItem(
                        label = "Screen Off",
                        value = powerInfo.formatScreenOffTime(),
                        icon = Icons.Rounded.ScreenLockPortrait,
                    )
                    PowerInsightItem(
                        label = "Deep Sleep",
                        value = powerInfo.formatDeepSleepTime(),
                        icon = Icons.Rounded.Bedtime,
                    )
                    PowerInsightItem(
                        label = "Drain Rate",
                        value = String.format("-%.1f%%/h", powerInfo.activeDrainRate),
                        icon = Icons.Rounded.BatteryAlert,
                    )
                }
            }
        }
    }
}

/**
 * Wavy circular progress indicator with animated progress
 * Used for displaying battery level and screen on time
 */
@Composable
fun WavyCircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Dp,
    amplitude: Dp = 4.dp,
    frequency: Int = 12,
) {
    val strokeWidthPx = with(LocalDensity.current) { strokeWidth.toPx() }
    val amplitudePx = with(LocalDensity.current) { amplitude.toPx() }

    // Animate progress using animateFloatAsState
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing,
        ),
        label = "progress",
    )

    Canvas(modifier = modifier) {
        val radius = (size.minDimension - strokeWidthPx - amplitudePx * 2) / 2
        val center = Offset(size.width / 2, size.height / 2)
        val path = Path()

        val startAngle = -90f
        val sweepAngle = 360f * animatedProgress

        for (angle in 0..sweepAngle.toInt()) {
            val currentAngle = startAngle + angle
            val rad = Math.toRadians(currentAngle.toDouble())

            // Wavy function: radius + amplitude * sin(frequency * angle in rads)
            val wavePhase = Math.toRadians((angle * frequency).toDouble())
            val r = radius + amplitudePx * kotlin.math.sin(wavePhase)

            val x = center.x + r * kotlin.math.cos(rad)
            val y = center.y + r * kotlin.math.sin(rad)

            if (angle == 0) {
                path.moveTo(x.toFloat(), y.toFloat())
            } else {
                path.lineTo(x.toFloat(), y.toFloat())
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round,
            ),
        )
    }
}

@Composable
fun PowerInsightItem(label: String, value: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp),
            )
        }
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
            )
        }
    }
}
