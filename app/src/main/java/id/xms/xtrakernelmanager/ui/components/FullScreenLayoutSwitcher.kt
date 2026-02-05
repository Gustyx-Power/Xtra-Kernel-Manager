package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FullScreenLayoutSwitcher(
    isVisible: Boolean,
    targetLayout: String,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        // Full screen overlay with stronger blur background
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.92f),
                            Color.Black.copy(alpha = 0.88f),
                            Color.Black.copy(alpha = 0.95f)
                        ),
                        radius = 800f
                    )
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    // Consume clicks to prevent interaction with underlying UI
                },
            contentAlignment = Alignment.Center
        ) {
            // Additional blur layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .blur(radius = 12.dp)
            )
            
            // Content card on top of blur
            SwitchingContentCard(targetLayout = targetLayout)
        }
    }
}

@Composable
private fun SwitchingContentCard(targetLayout: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "switching_animation")
    
    // Pulsing animation for the card
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Scale animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(300.dp, 220.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.90f),
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Loading indicator dengan scale yang lebih smooth
            FullScreenLoadingIndicator(scale = scale * pulse)
            
            // Switching text dengan animasi yang lebih halus
            Text(
                text = "Menyiapkan layout $targetLayout",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = pulse)
            )
            
            Text(
                text = "Mohon tunggu sebentar...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f * pulse)
            )
            
            // Progress dots dengan efek yang lebih menarik
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val dotScale by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, delayMillis = index * 250, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_scale_$index"
                    )
                    
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, delayMillis = index * 250, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_alpha_$index"
                    )
                    
                    Canvas(
                        modifier = Modifier.size((12 * dotScale).dp)
                    ) {
                        drawCircle(
                            color = Color(0xFF6366F1).copy(alpha = dotAlpha * pulse),
                            radius = size.minDimension / 2
                        )
                        // Inner glow effect
                        drawCircle(
                            color = Color.White.copy(alpha = dotAlpha * 0.3f),
                            radius = size.minDimension / 4
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScreenLoadingIndicator(scale: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_indicator")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Canvas(
        modifier = Modifier.size(64.dp)
    ) {
        drawFullScreenIndicator(rotation, scale, this)
    }
}

private fun drawFullScreenIndicator(rotation: Float, scale: Float, drawScope: DrawScope) {
    with(drawScope) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.minDimension / 5
        
        rotate(rotation, center) {
            // Outer ring - 12 circles
            for (i in 0 until 12) {
                val angle = (i * 30f) * (kotlin.math.PI / 180f)
                val radius = baseRadius * 1.4f
                val x = center.x + cos(angle) * radius
                val y = center.y + sin(angle) * radius
                
                val alpha = (1f - (i / 12f)) * 0.9f + 0.1f
                val circleRadius = (3.dp.toPx() * (alpha + 0.3f)) * scale
                
                drawCircle(
                    color = Color(0xFF6366F1).copy(alpha = alpha),
                    radius = circleRadius,
                    center = Offset(x.toFloat(), y.toFloat())
                )
            }
            
            // Middle ring - 8 circles (counter-rotating)
            rotate(-rotation * 1.5f, center) {
                for (i in 0 until 8) {
                    val angle = (i * 45f) * (kotlin.math.PI / 180f)
                    val radius = baseRadius * 0.9f
                    val x = center.x + cos(angle) * radius
                    val y = center.y + sin(angle) * radius
                    
                    val alpha = (1f - (i / 8f)) * 0.7f + 0.3f
                    val circleRadius = (2.5.dp.toPx() * (alpha + 0.4f)) * scale
                    
                    drawCircle(
                        color = Color(0xFF8B5CF6).copy(alpha = alpha),
                        radius = circleRadius,
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                }
            }
            
            // Inner ring - 6 circles
            rotate(rotation * 2f, center) {
                for (i in 0 until 6) {
                    val angle = (i * 60f) * (kotlin.math.PI / 180f)
                    val radius = baseRadius * 0.5f
                    val x = center.x + cos(angle) * radius
                    val y = center.y + sin(angle) * radius
                    
                    val alpha = (1f - (i / 6f)) * 0.6f + 0.4f
                    val circleRadius = (2.dp.toPx() * (alpha + 0.5f)) * scale
                    
                    drawCircle(
                        color = Color(0xFF06B6D4).copy(alpha = alpha),
                        radius = circleRadius,
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                }
            }
        }
        
        // Center pulsing circle
        drawCircle(
            color = Color(0xFF10B981).copy(alpha = 0.8f),
            radius = (4.dp.toPx()) * scale,
            center = center
        )
    }
}