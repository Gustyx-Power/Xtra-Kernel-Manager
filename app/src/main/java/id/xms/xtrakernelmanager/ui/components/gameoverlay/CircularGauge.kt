package id.xms.xtrakernelmanager.ui.components.gameoverlay

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Circular Gauge Component
 * 
 * A beautiful circular progress indicator with:
 * - Animated value changes
 * - Gradient arc
 * - Center value display
 * - Label below
 */
@Composable
fun CircularGauge(
    value: Float,
    maxValue: Float = 100f,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = primaryColor.copy(alpha = 0.3f),
    backgroundColor: Color = Color(0xFF2A2A2A),
    valueText: String? = null,
    label: String = "",
    unit: String = "",
    valueFontSize: TextUnit = 18.sp,
    labelFontSize: TextUnit = 10.sp
) {
    // Animate the value changes
    val animatedValue by animateFloatAsState(
        targetValue = value.coerceIn(0f, maxValue),
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "gauge_animation"
    )
    
    val sweepAngle = (animatedValue / maxValue) * 270f // 270 degree arc
    
    // Color gradient based on value percentage
    val gradientColors = remember(primaryColor) {
        listOf(
            primaryColor.copy(alpha = 0.7f),
            primaryColor,
            primaryColor.copy(alpha = 0.9f)
        )
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            val arcSize = Size(
                width = this.size.width - strokeWidthPx,
                height = this.size.height - strokeWidthPx
            )
            val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
            
            // Background arc
            drawArc(
                color = backgroundColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round
                )
            )
            
            // Progress arc with gradient
            if (sweepAngle > 0) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = gradientColors,
                        center = Offset(this.size.width / 2, this.size.height / 2)
                    ),
                    startAngle = 135f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
        
        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = valueText ?: "${animatedValue.toInt()}$unit",
                fontSize = valueFontSize,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    fontSize = labelFontSize,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Compact Circular Gauge - Smaller version for overlay use
 */
@Composable
fun CompactCircularGauge(
    value: Float,
    maxValue: Float = 100f,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    valueText: String,
    label: String
) {
    CircularGauge(
        value = value,
        maxValue = maxValue,
        modifier = modifier,
        size = 60.dp,
        strokeWidth = 5.dp,
        primaryColor = primaryColor,
        valueText = valueText,
        label = label,
        valueFontSize = 14.sp,
        labelFontSize = 8.sp
    )
}

/**
 * CPU/GPU Gauge - Preset for hardware monitoring
 */
@Composable
fun HardwareGauge(
    type: HardwareGaugeType,
    value: Float,
    freqValue: String,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (type) {
        HardwareGaugeType.CPU -> Pair(Color(0xFF2196F3), "CPU")
        HardwareGaugeType.GPU -> Pair(Color(0xFFFF9800), "GPU")
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        CircularGauge(
            value = value,
            maxValue = 100f,
            size = 70.dp,
            strokeWidth = 6.dp,
            primaryColor = color,
            valueText = freqValue,
            label = "",
            valueFontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
        Text(
            text = "${value.toInt()}%",
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

enum class HardwareGaugeType {
    CPU, GPU
}

/**
 * Battery Gauge - Horizontal bar style
 */
@Composable
fun BatteryGauge(
    percentage: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        percentage <= 20 -> Color(0xFFF44336)
        percentage <= 50 -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Battery icon with fill
        Canvas(
            modifier = Modifier
                .width(24.dp)
                .height(12.dp)
        ) {
            val width = size.width
            val height = size.height
            
            // Battery body outline
            drawRoundRect(
                color = Color.Gray,
                topLeft = Offset(0f, 0f),
                size = Size(width * 0.9f, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                style = Stroke(width = 2f)
            )
            
            // Battery tip
            drawRect(
                color = Color.Gray,
                topLeft = Offset(width * 0.9f, height * 0.3f),
                size = Size(width * 0.1f, height * 0.4f)
            )
            
            // Battery fill
            val fillWidth = (width * 0.85f) * (percentage / 100f)
            drawRoundRect(
                color = color,
                topLeft = Offset(3f, 3f),
                size = Size(fillWidth, height - 6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
        }
        
        Text(
            text = "$percentage%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
