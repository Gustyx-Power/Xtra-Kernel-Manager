package id.xms.xtrakernelmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DeviceSilhouette(modifier: Modifier = Modifier, color: Color? = null) {
  // Front View - Screen Silhouette with Punch-hole (Monet Themed)
  val frameColor =
      color ?: MaterialTheme.colorScheme.onSurfaceVariant // Darker for better visibility
  val bezelColor = color ?: MaterialTheme.colorScheme.surfaceVariant
  val screenTopColor =
      color?.copy(alpha = 0.6f) ?: MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
  val screenBottomColor =
      color?.copy(alpha = 0.8f) ?: MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)

  Box(
      modifier =
          modifier
              .width(86.dp)
              .height(110.dp)
              .clip(
                  RoundedCornerShape(
                      topStart = 16.dp,
                      topEnd = 16.dp,
                      bottomEnd = 0.dp,
                      bottomStart = 0.dp,
                  )
              )
              .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val cornerRadius = 16.dp.toPx()
                val inset = strokeWidth / 2 // Inset to keep stroke fully inside

                // Draw Bezel Background
                drawRect(color = bezelColor, style = androidx.compose.ui.graphics.drawscope.Fill)

                // Draw Frame Border (Top, Left, Right)
                drawPath(
                    path =
                        androidx.compose.ui.graphics.Path().apply {
                          // Start from bottom-left
                          moveTo(inset, size.height)

                          // Line up to start of top-left corner
                          lineTo(inset, cornerRadius)

                          // Top-Left Corner (180 to 270 degrees)
                          arcTo(
                              rect =
                                  androidx.compose.ui.geometry.Rect(
                                      left = inset,
                                      top = inset,
                                      right = (cornerRadius * 2) - inset,
                                      bottom = (cornerRadius * 2) - inset,
                                  ),
                              startAngleDegrees = 180f,
                              sweepAngleDegrees = 90f,
                              forceMoveTo = false,
                          )

                          // Top Edge (short line if width > 2*radius)
                          lineTo(size.width - cornerRadius, inset)

                          // Top-Right Corner (270 to 0/360 degrees)
                          arcTo(
                              rect =
                                  androidx.compose.ui.geometry.Rect(
                                      left = size.width - (cornerRadius * 2) + inset,
                                      top = inset,
                                      right = size.width - inset,
                                      bottom = (cornerRadius * 2) - inset,
                                  ),
                              startAngleDegrees = 270f,
                              sweepAngleDegrees = 90f,
                              forceMoveTo = false,
                          )

                          // Line down to bottom-right
                          lineTo(size.width - inset, size.height)
                        },
                    color = frameColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
                )
              }
              .padding(4.dp) // Bezel thickness
  ) {
    // Screen Area (Monet Gradient)
    Box(
        modifier =
            Modifier.fillMaxSize()
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomEnd = 0.dp,
                        bottomStart = 0.dp,
                    )
                )
                .background(
                    brush =
                        Brush.linearGradient(
                            colors = listOf(screenTopColor, screenBottomColor),
                            start = androidx.compose.ui.geometry.Offset.Zero,
                            end = androidx.compose.ui.geometry.Offset.Infinite,
                        )
                )
    ) {
      // Punch-hole Camera
      Box(
          modifier =
              Modifier.align(Alignment.TopCenter)
                  .padding(top = 6.dp)
                  .size(8.dp) // Punch hole size
                  .clip(CircleShape)
                  .background(Color.Black)
      )

      // Subtle Reflection
      Box(
          modifier =
              Modifier.align(Alignment.TopEnd)
                  .fillMaxHeight()
                  .fillMaxWidth(0.5f)
                  .background(
                      brush =
                          Brush.horizontalGradient(
                              colors =
                                  listOf(
                                      Color.Transparent,
                                      MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                  )
                          )
                  )
      )
    }
  }
}
