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
    size: Dp = 100.dp,
    strokeWidth: Dp = 10.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = Color(0xFF1E1E1E),
    valueText: String? = null,
    subText: String? = null,
    label: String = "",
    valueFontSize: TextUnit = 20.sp,
    labelFontSize: TextUnit = 12.sp,
) {
  // Animate the value changes
  val animatedValue by
      animateFloatAsState(
          targetValue = value.coerceIn(0f, maxValue),
          animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
          label = "gauge_animation",
      )

  val sweepAngle = (animatedValue / maxValue) * 260f

  Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val strokeWidthPx = strokeWidth.toPx()
      val arcSize =
          Size(width = this.size.width - strokeWidthPx, height = this.size.height - strokeWidthPx)
      val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)

      // Background arc (darker track)
      drawArc(
          color = backgroundColor,
          startAngle = 140f,
          sweepAngle = 260f,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
      )

      // Progress arc
      if (sweepAngle > 0) {
        drawArc(
            color = primaryColor,
            startAngle = 140f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
        )
      }
    }

    // Center content
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
      Text(
          text = valueText ?: "",
          fontSize = valueFontSize, // e.g. 2.92GHz
          fontWeight = FontWeight.Bold,
          color = Color.White,
      )
      if (!subText.isNullOrEmpty()) {
        Text(
            text = subText, // e.g. 16%
            fontSize = 12.sp,
            color = Color.Gray,
        )
      }
    }

    // Label at bottom (CPU/GPU)
    Box(modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-5).dp)) {
      Text(
          text = label, // CPU / GPU
          fontSize = labelFontSize,
          fontWeight = FontWeight.Medium,
          color = primaryColor,
      )
    }
  }
}

/** CPU/GPU Gauge - Preset for hardware monitoring */
@Composable
fun HardwareGauge(
    type: HardwareGaugeType,
    loadValue: Float,
    freqValue: String,
    modifier: Modifier = Modifier,
) {
  val (color, label) =
      when (type) {
        HardwareGaugeType.CPU -> Pair(Color(0xFF00B0FF), "CPU") // Light Blue
        HardwareGaugeType.GPU -> Pair(Color(0xFFFF9100), "GPU") // Orange
      }

  CircularGauge(
      value = loadValue,
      maxValue = 100f,
      size = 90.dp,
      strokeWidth = 8.dp,
      primaryColor = color,
      backgroundColor = Color(0xFF1A1A1A),
      valueText = freqValue,
      subText = "${loadValue.toInt()}%",
      label = label,
      valueFontSize = 14.sp,
      labelFontSize = 11.sp,
      modifier = modifier,
  )
}

enum class HardwareGaugeType {
  CPU,
  GPU,
}

/** Battery Gauge - Horizontal bar style */
@Composable
fun BatteryGauge(percentage: Int, modifier: Modifier = Modifier) {
  val color =
      when {
        percentage <= 20 -> Color(0xFFF44336)
        percentage <= 50 -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
      }

  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // Battery icon with fill
    Canvas(modifier = Modifier.width(24.dp).height(12.dp)) {
      val width = size.width
      val height = size.height

      // Battery body outline
      drawRoundRect(
          color = Color.Gray,
          topLeft = Offset(0f, 0f),
          size = Size(width * 0.9f, height),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
          style = Stroke(width = 2f),
      )

      // Battery tip
      drawRect(
          color = Color.Gray,
          topLeft = Offset(width * 0.9f, height * 0.3f),
          size = Size(width * 0.1f, height * 0.4f),
      )

      // Battery fill
      val fillWidth = (width * 0.85f) * (percentage / 100f)
      drawRoundRect(
          color = color,
          topLeft = Offset(3f, 3f),
          size = Size(fillWidth, height - 6f),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f),
      )
    }

    Text(text = "$percentage%", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
  }
}
