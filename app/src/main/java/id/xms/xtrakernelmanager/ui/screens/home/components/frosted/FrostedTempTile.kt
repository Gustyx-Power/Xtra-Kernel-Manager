package id.xms.xtrakernelmanager.ui.screens.home.components.frosted

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun FrostedTempTile(
    modifier: Modifier = Modifier,
    cpuTemp: Int,
    gpuTemp: Int,
    pmicTemp: Int,
    thermalTemp: Int,
    color: Color
) {
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
    
    // Carousel state
    val tempData = listOf(
        "CPU" to cpuTemp,
        "GPU" to gpuTemp,
        "PMIC" to pmicTemp,
        "Thermal" to thermalTemp
    )
    
    var currentIndex by remember { mutableStateOf(0) }
    
    // Auto carousel animation
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // Change every 3 seconds
            currentIndex = (currentIndex + 1) % tempData.size
        }
    }
    
    FrostedSharedCard(modifier = modifier, contentPadding = PaddingValues(0.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(glassBackground)
                .border(
                    width = if (isDarkTheme) 0.8.dp else 1.2.dp,
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.large
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated temperature display
                AnimatedContent(
                    targetState = currentIndex,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> -height } + fadeOut()
                        )
                    },
                    label = "tempCarousel"
                ) { index ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Large temperature
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${tempData[index].second}",
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp),
                                fontWeight = FontWeight.Black,
                                color = textColor,
                                lineHeight = 96.sp
                            )
                            Text(
                                text = "°C",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = textColor,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        
                        // Label
                        Text(
                            text = "${tempData[index].first} Temperature",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = textSecondaryColor
                        )
                        
                        // Status indicator
                        Surface(
                            shape = CircleShape,
                            color = if (tempData[index].second > 60) Color(0xFFff716c).copy(alpha = 0.2f) else Color(0xFF84f5e8).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (tempData[index].second > 60) "HIGH" else "NORMAL",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (tempData[index].second > 60) Color(0xFFff716c) else Color(0xFF84f5e8),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Carousel indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tempData.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(if (currentIndex == index) 8.dp else 6.dp)
                                .background(
                                    color = if (currentIndex == index) textColor else textSecondaryColor.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}
