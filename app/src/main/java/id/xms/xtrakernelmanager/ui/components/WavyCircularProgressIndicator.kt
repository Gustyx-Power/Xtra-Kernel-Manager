package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WavyCircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 8.dp,
    amplitude: Float = 0.1f, // Percentage of radius
    frequency: Int = 12, // Number of waves
) {
  val animatedProgress by
      animateFloatAsState(
          targetValue = progress.coerceIn(0f, 1f),
          animationSpec =
              spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
          label = "progress",
      )

  // Continuous rotation for aesthetic
  val infiniteTransition = rememberInfiniteTransition(label = "spin")
  val rotation by
      infiniteTransition.animateFloat(
          initialValue = 0f,
          targetValue = 360f,
          animationSpec = infiniteRepeatable(animation = tween(10000, easing = LinearEasing)),
          label = "rotation",
      )

  Canvas(modifier = modifier) {
    val radius = size.minDimension / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    val strokePx = strokeWidth.toPx()

    // Effective radius to keep stroke inside bounds
    val rBase = radius - strokePx - (radius * amplitude)
    val rAmp = radius * amplitude

    fun getWavyPath(sweep: Float): Path {
      val path = Path()
      val steps = 360 // Resolution

      for (i in 0..steps) {
        val theta = i.toFloat() // Degrees
        // Only draw up to the sweep angle
        if (theta > sweep * 360f) break

        val thetaRad = theta * (PI / 180f).toFloat() - (PI / 2f).toFloat() // Start from top

        // Radius varies with sine wave
        // frequency * thetaRad gives the waves
        val currentR = rBase + rAmp * sin(thetaRad * frequency)

        val x = center.x + currentR * cos(thetaRad)
        val y = center.y + currentR * sin(thetaRad)

        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
      }
      return path
    }

    // Draw Track (Full Circle)
    // We draw the full wavy path 0..360
    val trackPath = Path()
    for (i in 0..360) {
      val thetaRad = i * (PI / 180f).toFloat() - (PI / 2f).toFloat()
      val currentR = rBase + rAmp * sin(thetaRad * frequency)
      val x = center.x + currentR * cos(thetaRad)
      val y = center.y + currentR * sin(thetaRad)
      if (i == 0) trackPath.moveTo(x, y) else trackPath.lineTo(x, y)
    }
    trackPath.close()

    drawPath(
        path = trackPath,
        color = trackColor,
        style = Stroke(width = strokePx, cap = StrokeCap.Round),
    )

    // Draw Progress
    if (animatedProgress > 0f) {
      val progressPath = getWavyPath(animatedProgress)
      drawPath(
          path = progressPath,
          color = color,
          style = Stroke(width = strokePx, cap = StrokeCap.Round),
      )
    }
  }
}
