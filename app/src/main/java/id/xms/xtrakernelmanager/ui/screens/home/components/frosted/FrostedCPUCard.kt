package id.xms.xtrakernelmanager.ui.screens.home.components.frosted

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.data.model.CPUInfo
import androidx.compose.ui.res.stringResource
import java.util.Locale
import kotlin.math.sin

@Composable
fun FrostedCPUCard(cpuInfo: CPUInfo, modifier: Modifier = Modifier) {
    val isDarkTheme = isSystemInDarkTheme()
    
    val glassBackground = if (isDarkTheme) {
        Color(0xFF000000).copy(alpha = 0.35f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.45f)
    }
    
    val textColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.95f)
    } else {
        Color(0xFF2C2C2C).copy(alpha = 0.85f)
    }
    
    val textSecondaryColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.65f)
    } else {
        Color(0xFF5A5A5A).copy(alpha = 0.7f)
    }
    
    val accentColor = Color(0xFF2196F3)
    
    val borderColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.6f)
    }
    
    val governorName = cpuInfo.cores.firstOrNull { it.isOnline }?.governor?.uppercase() ?: "SCHEDUTIL"    
    val onlineCores = cpuInfo.cores.filter { it.isOnline }
    val currentMinFreq = onlineCores.minOfOrNull { it.currentFreq } ?: 0
    val currentMaxFreq = onlineCores.maxOfOrNull { it.currentFreq } ?: 0
    
    // CPU Load percentage
    val cpuLoadPercent = cpuInfo.totalLoad.toInt().coerceIn(0, 100)
    var chartData by remember { mutableStateOf(List(20) { 0f }) }
    
    LaunchedEffect(cpuInfo.totalLoad) {
        chartData = chartData.drop(1) + (cpuInfo.totalLoad / 100f)
    }
    
    FrostedSharedCard(
        modifier = modifier,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(glassBackground)
                .border(
                    width = if (isDarkTheme) 0.8.dp else 1.2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = governorName,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp
                            ),
                            color = textColor
                        )
                        Text(
                            text = "CPU Clusters Performance",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp
                            ),
                            color = textSecondaryColor
                        )
                    }
                    
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgress(
                            percentage = cpuLoadPercent,
                            color = accentColor,
                            backgroundColor = textSecondaryColor.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "$cpuLoadPercent%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = textColor
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    CPULineChart(
                        data = chartData,
                        color = accentColor,
                        backgroundColor = accentColor.copy(alpha = 0.1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "MIN: ${currentMinFreq / 1000}MHZ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textSecondaryColor
                    )
                    Text(
                        text = "MAX: ${String.format(Locale.US, "%.2f", currentMaxFreq / 1000000f)}GHZ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textSecondaryColor
                    )
                }
            }
        }
    }
}

@Composable
private fun CircularProgress(
    percentage: Int,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 8.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val radius = diameter / 2
        val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
        
        drawArc(
            color = backgroundColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = (percentage / 100f) * 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun CPULineChart(
    data: List<Float>,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedData = data.map { value ->
        val animatedValue = remember { androidx.compose.animation.core.Animatable(value) }
        LaunchedEffect(value) {
            animatedValue.animateTo(
                targetValue = value,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        animatedValue.value
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        if (animatedData.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val spacing = width / (animatedData.size - 1).coerceAtLeast(1)
        val linePath = Path().apply {
            animatedData.forEachIndexed { index, value ->
                val x = index * spacing
                val y = height - (value * height * 0.8f) - (height * 0.1f)
                
                if (index == 0) {
                    moveTo(x, y)
                } else {
                    // Use cubic bezier for smooth curves
                    val prevX = (index - 1) * spacing
                    val prevY = height - (animatedData[index - 1] * height * 0.8f) - (height * 0.1f)
                    
                    val controlX1 = prevX + spacing * 0.5f
                    val controlY1 = prevY
                    val controlX2 = x - spacing * 0.5f
                    val controlY2 = y
                    
                    cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                }
            }
        }
        
        val fillPath = Path().apply {
            moveTo(0f, height)
            animatedData.forEachIndexed { index, value ->
                val x = index * spacing
                val y = height - (value * height * 0.8f) - (height * 0.1f)
                
                if (index == 0) {
                    lineTo(x, y)
                } else {
                    val prevX = (index - 1) * spacing
                    val prevY = height - (animatedData[index - 1] * height * 0.8f) - (height * 0.1f)
                    
                    val controlX1 = prevX + spacing * 0.5f
                    val controlY1 = prevY
                    val controlX2 = x - spacing * 0.5f
                    val controlY2 = y
                    
                    cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                }
            }
            lineTo(width, height)
            close()
        }
        
        drawPath(
            path = fillPath,
            color = backgroundColor
        )
        drawPath(
            path = linePath,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun FrostedExpandedCoresCard(cpuInfo: CPUInfo, modifier: Modifier = Modifier) {
    val isDarkTheme = isSystemInDarkTheme()
    
    val textColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.95f)
    } else {
        Color(0xFF2C2C2C).copy(alpha = 0.85f)
    }
    
    FrostedSharedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
             Text(
                 stringResource(id.xms.xtrakernelmanager.R.string.cpu_cores), 
                 style = MaterialTheme.typography.titleMedium, 
                 fontWeight = FontWeight.Bold, 
                 color = textColor
             )
             Spacer(modifier = Modifier.height(16.dp))
             
             val rows = cpuInfo.cores.chunked(4)
             Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                 rows.forEach { rowCores ->
                     Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                         rowCores.forEach { core ->
                            val load = if(core.maxFreq > 0) core.currentFreq.toFloat() / core.maxFreq.toFloat() else 0f
                            val isPerf = core.coreNumber >= 4
                            SingleCoreBar(coreNumber = core.coreNumber, load = load, isPerformance = isPerf, isDarkTheme = isDarkTheme)
                         }
                     }
                 }
             }
        }
    }
}

@Composable
fun SingleCoreBar(coreNumber: Int, load: Float, isPerformance: Boolean, isDarkTheme: Boolean) {
    val barBackground = if (isDarkTheme) {
        Color(0xFF000000).copy(alpha = 0.3f)
    } else {
        Color.White.copy(alpha = 0.5f)
    }
    
    val barColor = if (isPerformance) {
        if (isDarkTheme) Color(0xFF4CAF50) else Color(0xFF2E7D32)
    } else {
        if (isDarkTheme) Color(0xFF2196F3) else Color(0xFF1565C0)
    }
    
    val textColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.65f)
    } else {
        Color(0xFF5A5A5A).copy(alpha = 0.7f)
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
        Box(
            modifier = Modifier
                .height(40.dp)
                .width(8.dp)
                .clip(RoundedCornerShape(100))
                .background(barBackground),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = load.coerceIn(0.1f, 1f))
                    .background(barColor, RoundedCornerShape(100))
            )
        }
        Text(
            text = "$coreNumber",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
            color = textColor,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
