package id.xms.xtrakernelmanager.ui.screens.tuning

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WavyCircularProgressIndicator(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    strokeWidth: Dp = 8.dp,
    amplitude: Dp = 4.dp, // Height of the wave
    frequency: Int = 12, // Number of waves around the circle
    text: String? = null,
) {
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val width = size.width
      val height = size.height
      val radius = (minOf(width, height) / 2) - amplitude.toPx() - strokeWidth.toPx()
      val centerX = width / 2
      val centerY = height / 2
      val ampPx = amplitude.toPx()
      val strokePx = strokeWidth.toPx()

      fun createWavyPath(endAngle: Float): Path {
        val path = Path()
        val steps = 360 // Resolution

        for (i in 0..steps) {
          val angleDeg = i.toFloat()
          if (angleDeg > endAngle) break
          val angleRad = Math.toRadians(angleDeg.toDouble() - 90)
          val waveOffset = sin(angleRad * frequency) * ampPx
          val r = radius + waveOffset
          val x = centerX + r * cos(angleRad)
          val y = centerY + r * sin(angleRad)

          if (i == 0) {
            path.moveTo(x.toFloat(), y.toFloat())
          } else {
            path.lineTo(x.toFloat(), y.toFloat())
          }
        }
        return path
      }

      // Draw Track (Full Circle)
      drawPath(
          path = createWavyPath(360f),
          color = trackColor,
          style = Stroke(width = strokePx, cap = StrokeCap.Round),
      )

      // Draw Progress
      if (progress > 0) {
        drawPath(
            path = createWavyPath(360f * progress),
            color = color,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
      }
    }

    if (text != null) {
      Text(
          text = text,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}
