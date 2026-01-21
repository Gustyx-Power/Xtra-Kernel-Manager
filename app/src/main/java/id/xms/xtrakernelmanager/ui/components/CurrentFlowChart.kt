package id.xms.xtrakernelmanager.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.repository.CurrentSample
import kotlin.math.abs
import kotlin.math.max

@Composable
fun CurrentFlowChart(
    samples: List<CurrentSample>,
    modifier: Modifier = Modifier,
    chargingColor: Color = Color(0xFF4CAF50),
    dischargingColor: Color = Color(0xFFE57373),
    gridColor: Color = Color.White.copy(alpha = 0.1f),
    zeroLineColor: Color = Color.White.copy(alpha = 0.3f),
) {
  val textMeasurer = rememberTextMeasurer()
  val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

  Canvas(modifier = modifier.fillMaxSize()) {
    if (samples.isEmpty()) {
      drawEmptyState(labelColor)
      return@Canvas
    }

    val width = size.width
    val height = size.height
    val padding = 40.dp.toPx()

    val chartWidth = width - padding
    val chartHeight = height - padding
    val chartLeft = padding / 2
    val chartTop = padding / 4

    val maxCurrent = samples.maxOfOrNull { it.current } ?: 0
    val minCurrent = samples.minOfOrNull { it.current } ?: 0
    val absMax = max(abs(maxCurrent), abs(minCurrent)).coerceAtLeast(100)

    val hasPositive = maxCurrent > 0
    val hasNegative = minCurrent < 0

    val zeroY =
        when {
          hasPositive && hasNegative -> chartTop + chartHeight / 2
          hasPositive -> chartTop + chartHeight - 10.dp.toPx()
          else -> chartTop + 10.dp.toPx()
        }

    drawGridLines(chartLeft, chartTop, chartWidth, chartHeight, gridColor)

    drawLine(
        color = zeroLineColor,
        start = Offset(chartLeft, zeroY),
        end = Offset(chartLeft + chartWidth, zeroY),
        strokeWidth = 2.dp.toPx(),
    )

    // Draw the line chart
    if (samples.size >= 2) {
      val path = Path()
      val timeRange = samples.last().timestamp - samples.first().timestamp

      samples.forEachIndexed { index, sample ->
        val x =
            if (timeRange > 0) {
              chartLeft +
                  ((sample.timestamp - samples.first().timestamp).toFloat() / timeRange) *
                      chartWidth
            } else {
              chartLeft + (index.toFloat() / (samples.size - 1)) * chartWidth
            }

        // Scale current to chart height
        val normalizedCurrent = sample.current.toFloat() / absMax
        val y = zeroY - (normalizedCurrent * (chartHeight / 2 - 20.dp.toPx()))

        if (index == 0) {
          path.moveTo(x, y)
        } else {
          path.lineTo(x, y)
        }
      }

      // Determine line color based on average
      val avgCurrent = samples.map { it.current }.average()
      val lineColor = if (avgCurrent >= 0) chargingColor else dischargingColor

      drawPath(
          path = path,
          color = lineColor,
          style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
      )

      // Draw gradient fill under the line
      val fillPath = Path()
      fillPath.addPath(path)

      // Close the path to zero line
      val lastX =
          if (timeRange > 0) {
            chartLeft + chartWidth
          } else {
            chartLeft + chartWidth
          }
      fillPath.lineTo(lastX, zeroY)
      fillPath.lineTo(chartLeft, zeroY)
      fillPath.close()

      drawPath(path = fillPath, color = lineColor.copy(alpha = 0.2f))
    }

    // Draw current value labels
    drawCurrentLabels(
        absMax,
        chartLeft,
        chartTop,
        chartHeight,
        zeroY,
        labelColor,
        hasPositive,
        hasNegative,
    )
  }
}

private fun DrawScope.drawEmptyState(color: Color) {
  // Just draw a faint "No Data" indication via grid lines
  val centerY = size.height / 2
  drawLine(
      color = color,
      start = Offset(0f, centerY),
      end = Offset(size.width, centerY),
      strokeWidth = 1.dp.toPx(),
  )
}

private fun DrawScope.drawGridLines(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    color: Color,
) {
  // Horizontal grid lines (3 lines)
  for (i in 0..3) {
    val y = top + (height * i / 3)
    drawLine(
        color = color,
        start = Offset(left, y),
        end = Offset(left + width, y),
        strokeWidth = 1.dp.toPx(),
    )
  }

  // Vertical grid lines (5 lines)
  for (i in 0..4) {
    val x = left + (width * i / 4)
    drawLine(
        color = color,
        start = Offset(x, top),
        end = Offset(x, top + height),
        strokeWidth = 1.dp.toPx(),
    )
  }
}

private fun DrawScope.drawCurrentLabels(
    maxValue: Int,
    left: Float,
    top: Float,
    height: Float,
    zeroY: Float,
    color: Color,
    hasPositive: Boolean,
    hasNegative: Boolean,
) {
  // We'll skip text drawing for simplicity - can add with TextMeasurer if needed
  // The chart is self-explanatory with the zero line and colors
}
