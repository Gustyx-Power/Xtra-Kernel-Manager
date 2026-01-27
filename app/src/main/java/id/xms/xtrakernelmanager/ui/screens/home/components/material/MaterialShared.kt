package id.xms.xtrakernelmanager.ui.screens.home.components.material

import android.graphics.Matrix
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import kotlinx.coroutines.delay

/**
 * Shared components and utilities for Material Design home screen
 * Contains reusable UI elements, icons, and shapes
 */

// ============ Animation Components ============

/**
 * Staggered entry animation for list items
 * Creates a fade-in and slide-up effect with delay
 */
@Composable
fun StaggeredEntry(delayMillis: Int, content: @Composable () -> Unit) {
  var visible by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(delayMillis.toLong())
    visible = true
  }

  AnimatedVisibility(
      visible = visible,
      enter =
          fadeIn(animationSpec = tween(durationMillis = 400)) +
              slideInVertically(
                  initialOffsetY = { it / 4 }, animationSpec = tween(durationMillis = 400)
              ),
  ) {
    content()
  }
}

// ============ Battery Components ============

/**
 * Battery status chip displaying charging or discharging state
 */
@Composable
fun BatteryStatusChip(text: String) {
  Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
    )
  }
}

/**
 * Battery stat box displaying a single battery metric
 */
@Composable
fun BatteryStatBox(label: String, value: String, modifier: Modifier = Modifier) {
  Card(
      modifier = modifier,
      shape = MaterialTheme.shapes.large,
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ),
  ) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
      Text(
          text = value,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

// GitHub Icon
val GithubIcon: ImageVector
    get() {
        if (_GithubIcon != null) return _GithubIcon!!
        _GithubIcon = ImageVector.Builder(
            name = "Github",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12.0f, 1.27f)
                curveTo(5.97f, 1.27f, 1.08f, 6.04f, 1.08f, 11.93f)
                curveTo(1.08f, 16.64f, 4.23f, 20.64f, 8.5f, 22.01f)
                curveTo(9.05f, 22.11f, 9.25f, 21.78f, 9.25f, 21.5f)
                curveTo(9.25f, 21.25f, 9.24f, 20.59f, 9.24f, 19.71f)
                curveTo(6.18f, 20.36f, 5.54f, 18.27f, 5.54f, 18.27f)
                curveTo(5.04f, 17.03f, 4.31f, 16.7f, 4.31f, 16.7f)
                curveTo(3.31f, 16.03f, 4.38f, 16.04f, 4.38f, 16.04f)
                curveTo(5.49f, 16.12f, 6.07f, 17.15f, 6.07f, 17.15f)
                curveTo(7.05f, 18.79f, 8.64f, 18.32f, 9.27f, 18.04f)
                curveTo(9.36f, 17.35f, 9.64f, 16.89f, 9.95f, 16.63f)
                curveTo(7.51f, 16.36f, 4.95f, 15.44f, 4.95f, 11.36f)
                curveTo(4.95f, 10.2f, 5.38f, 9.24f, 6.08f, 8.49f)
                curveTo(5.97f, 8.22f, 5.59f, 7.13f, 6.18f, 5.67f)
                curveTo(6.18f, 5.67f, 7.1f, 5.38f, 9.19f, 6.76f)
                curveTo(10.06f, 6.52f, 11.0f, 6.4f, 11.93f, 6.4f)
                curveTo(12.86f, 6.4f, 13.8f, 6.52f, 14.67f, 6.76f)
                curveTo(16.76f, 5.38f, 17.67f, 5.67f, 17.67f, 5.67f)
                curveTo(18.27f, 7.13f, 17.89f, 8.22f, 17.78f, 8.49f)
                curveTo(18.48f, 9.24f, 18.91f, 10.2f, 18.91f, 11.36f)
                curveTo(18.91f, 15.45f, 16.34f, 16.35f, 13.89f, 16.62f)
                curveTo(14.28f, 16.95f, 14.63f, 17.61f, 14.63f, 18.61f)
                curveTo(14.63f, 20.04f, 14.61f, 21.2f, 14.61f, 21.5f)
                curveTo(14.61f, 21.79f, 14.81f, 22.12f, 15.37f, 22.01f)
                curveTo(19.62f, 20.63f, 22.78f, 16.64f, 22.78f, 11.93f)
                curveTo(22.78f, 6.04f, 17.88f, 1.27f, 11.93f, 1.27f)
                close()
            }
        }.build()
        return _GithubIcon!!
    }

private var _GithubIcon: ImageVector? = null

// Cookie Shape
val CookieShape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: androidx.compose.ui.unit.Density,
    ): Outline {
        val p = Path()
        val androidPath = p.asAndroidPath()
        val matrix = Matrix()

        val polygon = RoundedPolygon.star(
            numVerticesPerRadius = 6,
            innerRadius = 0.75f,
            rounding = CornerRounding(0.5f),
            innerRounding = CornerRounding(0.5f),
        )

        val minSize = minOf(size.width, size.height)
        val scale = minSize / 2f
        val cx = size.width / 2f
        val cy = size.height / 2f

        matrix.setScale(scale, scale)
        matrix.postTranslate(cx, cy)

        polygon.toPath(androidPath)
        androidPath.transform(matrix)

        return Outline.Generic(p)
    }
}
