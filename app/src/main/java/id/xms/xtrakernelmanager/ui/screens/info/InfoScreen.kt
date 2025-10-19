package id.xms.xtrakernelmanager.ui.screens.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset


@Composable
fun InfoScreen(
    viewModel: InfoViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Information",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            ScreenOnTimeCard(
                screenOnTime = uiState.screenOnTime,
                screenOnHistory = uiState.screenOnHistory
            )
        }

        // App Usage Statistics Section
        item {
            Text(
                text = "App Usage Statistics",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (!uiState.hasUsageStatsPermission) {
            // Show permission request card
            item {
                PermissionRequestCard(
                    onRequestPermission = { viewModel.requestUsageStatsPermission() }
                )
            }
        } else {
            if (uiState.appUsageList.isEmpty()) {
                // No data available
                item {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No usage data available",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "App usage data will appear here after using apps",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                // Show app usage list
                items(uiState.appUsageList.size) { index ->
                    val app = uiState.appUsageList[index]
                    AppUsageItem(
                        appUsageInfo = app,
                        rank = index + 1
                    )
                }
            }
        }
    }

    // Show message snackbar
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }
}

@Composable
private fun ScreenOnTimeCard(
    screenOnTime: Long,
    screenOnHistory: List<ScreenOnTimeData>
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Screen On Time",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Since last full charge",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = formatDuration(screenOnTime),
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Line Chart
            if (screenOnHistory.isNotEmpty()) {
                ScreenOnTimeChart(
                    history = screenOnHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Collecting data...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenOnTimeChart(
    history: List<ScreenOnTimeData>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        if (history.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val padding = 20f

        // Calculate max value for scaling
        val maxValue = history.maxOfOrNull { it.screenOnTime } ?: 1L

        // Draw grid lines (horizontal)
        val gridLineCount = 4
        for (i in 0..gridLineCount) {
            val y = padding + (height - 2 * padding) * i / gridLineCount
            drawLine(
                color = surfaceColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )
        }

        // Calculate points
        val points = history.mapIndexed { index, data ->
            val x = padding + (width - 2 * padding) * index / (history.size - 1).coerceAtLeast(1)
            val normalizedValue = data.screenOnTime.toFloat() / maxValue
            val y = height - padding - (height - 2 * padding) * normalizedValue
            Offset(x, y)
        }

        // Draw line
        if (points.size > 1) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }

            drawPath(
                path = path,
                color = primaryColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 3f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        }

        // Draw fill gradient
        if (points.size > 1) {
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(points[0].x, height - padding)
                lineTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
                lineTo(points.last().x, height - padding)
                close()
            }

            drawPath(
                path = fillPath,
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.3f),
                        primaryColor.copy(alpha = 0.05f)
                    )
                )
            )
        }

        // Draw points
        points.forEach { point ->
            drawCircle(
                color = primaryColor,
                radius = 4f,
                center = point
            )
            drawCircle(
                color = androidx.compose.ui.graphics.Color.White,
                radius = 2f,
                center = point
            )
        }
    }
}


@Composable
private fun PermissionRequestCard(
    onRequestPermission: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Usage Access Required",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Grant usage access permission to view detailed app usage statistics",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Grant Permission")
            }
        }
    }
}

@Composable
private fun AppUsageItem(
    appUsageInfo: AppUsageInfo,
    rank: Int
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Surface(
                shape = MaterialTheme.shapes.small,
                color = when {
                    rank == 1 -> androidx.compose.ui.graphics.Color(0xFFFFD700).copy(alpha = 0.2f)
                    rank == 2 -> androidx.compose.ui.graphics.Color(0xFFC0C0C0).copy(alpha = 0.2f)
                    rank == 3 -> androidx.compose.ui.graphics.Color(0xFFCD7F32).copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = when {
                        rank == 1 -> androidx.compose.ui.graphics.Color(0xFFFFD700)
                        rank == 2 -> androidx.compose.ui.graphics.Color(0xFFC0C0C0)
                        rank == 3 -> androidx.compose.ui.graphics.Color(0xFFCD7F32)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // App info (only app name, no package name)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appUsageInfo.appName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatDuration(appUsageInfo.totalTimeInForeground),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${String.format("%.1f", appUsageInfo.percentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Progress circle or bar
            CircularProgressIndicator(
                progress = appUsageInfo.percentage / 100f,
                modifier = Modifier.size(40.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60

    return when {
        hours > 0 -> String.format("%dh %02dm", hours, minutes)
        minutes > 0 -> String.format("%dm", minutes)
        else -> "< 1m"
    }
}
