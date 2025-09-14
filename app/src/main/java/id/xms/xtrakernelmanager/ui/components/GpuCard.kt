package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.RealtimeGpuInfo
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

const val MAX_GPU_HISTORY_POINTS = 50
const val SIMULATE_GPU_LOAD_TOGGLE = false // Keep for existing logic

@Composable
fun GpuCard(
    info: RealtimeGpuInfo,
    modifier: Modifier = Modifier
) {
    var graphDataHistory by remember { mutableStateOf(listOf<Float>()) }
    
    LaunchedEffect(info) {
        val currentDataPoint: Float = info.usagePercentage?.coerceIn(0f, 100f) ?: 0f
        graphDataHistory = (graphDataHistory + currentDataPoint).takeLast(MAX_GPU_HISTORY_POINTS)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GpuHeaderSection(info)

            GpuStatsSection(info = info, graphDataHistory = graphDataHistory)

            EnhancedGpuGraph(
                graphDataHistory = graphDataHistory,
                primaryColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun GpuHeaderSection(info: RealtimeGpuInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (info.model.isNotBlank() && info.model != "N/A" && info.model != "Graphics Processing Unit (GPU)") {
                    // Truncate long GPU model names but make sure we show meaningful info
                    val modelName = info.model
                    if (modelName.length > 25) {
                        modelName.substring(0, 22) + "..."
                    } else {
                        modelName
                    }
                } else {
                    stringResource(R.string.graphics_processing_unit_gpu)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    text = stringResource(R.string.gpu_label),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Simplified GPU icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = "GPU Icon",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun GpuStatsSection(
    info: RealtimeGpuInfo,
    graphDataHistory: List<Float>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardDefaults.shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.system_stats_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ) {
                StatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Speed,
                    label = stringResource(R.string.gpu_current_freq),
                    value = if (info.currentFreq > 0) "${info.currentFreq} MHz" else "N/A",
                    iconColor = MaterialTheme.colorScheme.primary
                )

                StatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Memory,
                    label = stringResource(R.string.gpu_max_freq),
                    value = if (info.maxFreq > 0) "${info.maxFreq} MHz" else "N/A",
                    iconColor = MaterialTheme.colorScheme.primary
                )

                StatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.BarChart,
                    label = stringResource(R.string.gpu_usage),
                    value = if (info.usagePercentage != null) "${info.usagePercentage?.toInt() ?: 0}%" else "N/A",
                    iconColor = when {
                        info.usagePercentage?.toInt() ?: 0 > 80 -> MaterialTheme.colorScheme.error
                        info.usagePercentage?.toInt() ?: 0 > 60 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun EnhancedGpuGraph(
    graphDataHistory: List<Float>,
    primaryColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (graphDataHistory.size > 1) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val yAxisMin = 0f
                    val yAxisMax = 100f
                    val effectiveYRange = (yAxisMax - yAxisMin).coerceAtLeast(1f)
                    val stepX = size.width / (MAX_GPU_HISTORY_POINTS - 1).coerceAtLeast(1).toFloat()

                    val path = Path()
                    val fillPath = Path()

                    graphDataHistory.forEachIndexed { index, dataPoint ->
                        val x = size.width - (graphDataHistory.size - 1 - index) * stepX
                        val normalizedData = ((dataPoint.coerceAtLeast(yAxisMin) - yAxisMin) / effectiveYRange).coerceIn(0f, 1f)
                        val y = size.height * (1 - normalizedData)

                        val clampedY = y.coerceIn(0f, size.height)
                        if (index == 0) {
                            path.moveTo(x, clampedY)
                            fillPath.moveTo(x, size.height)
                            fillPath.lineTo(x, clampedY)
                        } else {
                            path.lineTo(x, clampedY)
                            fillPath.lineTo(x, clampedY)
                        }
                    }

                    if (graphDataHistory.isNotEmpty()) {
                        fillPath.lineTo(size.width, size.height)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.3f),
                                    primaryColor.copy(alpha = 0.05f)
                                )
                            )
                        )
                    }

                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Draw a small circle for the last data point
                    if (graphDataHistory.isNotEmpty()) {
                        val lastDataPoint = graphDataHistory.last()
                        val lastX = size.width
                        val lastNormalizedData = ((lastDataPoint.coerceAtLeast(yAxisMin) - yAxisMin) / effectiveYRange).coerceIn(0f, 1f)
                        val lastY = size.height * (1 - lastNormalizedData)
                         drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = Offset(lastX, lastY.coerceIn(0f, size.height))
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.gathering_data_placeholder),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}