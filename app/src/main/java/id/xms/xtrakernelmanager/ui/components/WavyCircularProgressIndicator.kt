package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WavyCircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 8.dp,
    amplitude: Dp = 4.dp,
    frequency: Int = 12,
) {
  val strokeWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { strokeWidth.toPx() }
  val amplitudePx = with(androidx.compose.ui.platform.LocalDensity.current) { amplitude.toPx() }

  // Animate progress using animateFloatAsState
  val animatedProgress by
      animateFloatAsState(
          targetValue = progress,
          animationSpec =
              tween(
                  durationMillis = 1000,
                  easing = FastOutSlowInEasing,
              ),
          label = "progress",
      )

  Canvas(modifier = modifier) {
    val radius = (size.minDimension - strokeWidthPx - amplitudePx * 2) / 2
    val center = Offset(size.width / 2, size.height / 2)
    val path = androidx.compose.ui.graphics.Path()
    val trackPath = androidx.compose.ui.graphics.Path()

    val startAngle = -90f
    val sweepAngle = 360f

    // Draw Track (Full Circle)
    for (angle in 0..sweepAngle.toInt()) {
      val currentAngle = startAngle + angle
      val rad = Math.toRadians(currentAngle.toDouble())

      val wavePhase = Math.toRadians((angle * frequency).toDouble())
      val r = radius + amplitudePx * kotlin.math.sin(wavePhase)

      val x = center.x + r * kotlin.math.cos(rad)
      val y = center.y + r * kotlin.math.sin(rad)

      if (angle == 0) {
        trackPath.moveTo(x.toFloat(), y.toFloat())
      } else {
        trackPath.lineTo(x.toFloat(), y.toFloat())
      }
    }

    drawPath(
        path = trackPath,
        color = trackColor,
        style =
            Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round,
            ),
    )

    // Draw Progress
    if (animatedProgress > 0f) {
      val progressSweep = 360f * animatedProgress
      val step = 1f

      for (angle in 0..progressSweep.toInt()) {
        val currentAngle = startAngle + angle
        val rad = Math.toRadians(currentAngle.toDouble())

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
          style =
              Stroke(
                  width = strokeWidthPx,
                  cap = StrokeCap.Round,
              ),
      )
    }
  }
}
