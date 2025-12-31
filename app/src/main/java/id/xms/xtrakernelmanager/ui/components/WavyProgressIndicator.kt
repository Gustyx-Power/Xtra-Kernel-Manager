package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun WavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 4.dp,
    amplitude: Dp = 4.dp,
    frequency: Float = 0.1f, // Waves per pixel (roughly)
) {
  // Animate progress
  val animatedProgress by
      animateFloatAsState(
          targetValue = progress.coerceIn(0f, 1f),
          animationSpec =
              spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
          label = "progress",
      )

  Canvas(modifier = modifier) {
    val width = size.width
    val height = size.height
    val midHeight = height / 2f
    val waveAmplitude = amplitude.toPx()
    val strokePx = strokeWidth.toPx()

    val progressWidth = width * animatedProgress

    // Draw Track (Straight Line from progressWidth to width)
    if (animatedProgress < 1f) {
      drawLine(
          color = trackColor,
          start = Offset(progressWidth, midHeight),
          end = Offset(width, midHeight),
          strokeWidth = strokePx,
          cap = StrokeCap.Round,
      )
    }

    // 2. Draw Progress (Wave)
    if (animatedProgress > 0f) {
      val path = Path()
      path.moveTo(0f, midHeight)

      var currentX = 0f
      while (currentX <= progressWidth) {
        val y = midHeight + waveAmplitude * sin(currentX * frequency)
        path.lineTo(currentX, y)
        currentX += 1f
      }

      drawPath(path = path, color = color, style = Stroke(width = strokePx, cap = StrokeCap.Round))
    }
  }
}
