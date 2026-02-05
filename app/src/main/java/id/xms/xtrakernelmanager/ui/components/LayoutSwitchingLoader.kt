package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun LayoutSwitchingLoader(
    modifier: Modifier = Modifier,
    targetLayout: String = "Material",
    onCancel: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    // Rotation animation for the loading indicator
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Pulsing animation for the background
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Scale animation for the loading dots
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = pulse),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = pulse * 0.6f),
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = pulse * 0.3f)
                    ),
                    radius = 300f
                )
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Custom loading indicator with enhanced animation
            Canvas(
                modifier = Modifier.size(64.dp)
            ) {
                drawEnhancedLoadingIndicator(rotation, scale, this)
            }
            
            Text(
                text = "Switching to $targetLayout",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = "Applying new layout style and refreshing UI components...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            
            // Progress dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val dotScale by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_scale_$index"
                    )
                    
                    Canvas(
                        modifier = Modifier.size((8 * dotScale).dp)
                    ) {
                        drawCircle(
                            color = Color(0xFF6366F1).copy(alpha = dotScale),
                            radius = size.minDimension / 2
                        )
                    }
                }
            }
            
            // Cancel button for debugging/emergency reset
            if (onCancel != null) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private fun drawEnhancedLoadingIndicator(rotation: Float, scale: Float, drawScope: DrawScope) {
    with(drawScope) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.minDimension / 4
        
        rotate(rotation, center) {
            // Draw outer ring of circles
            for (i in 0 until 12) {
                val angle = (i * 30f) * (kotlin.math.PI / 180f)
                val radius = baseRadius * 1.2f
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
            
            // Draw inner ring of circles (counter-rotating)
            rotate(-rotation * 1.5f, center) {
                for (i in 0 until 8) {
                    val angle = (i * 45f) * (kotlin.math.PI / 180f)
                    val radius = baseRadius * 0.6f
                    val x = center.x + cos(angle) * radius
                    val y = center.y + sin(angle) * radius
                    
                    val alpha = (1f - (i / 8f)) * 0.7f + 0.3f
                    val circleRadius = (2.dp.toPx() * (alpha + 0.5f)) * scale
                    
                    drawCircle(
                        color = Color(0xFF8B5CF6).copy(alpha = alpha),
                        radius = circleRadius,
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                }
            }
        }
        
        // Center pulsing circle
        drawCircle(
            color = Color(0xFF06B6D4).copy(alpha = 0.6f),
            radius = (6.dp.toPx()) * scale,
            center = center
        )
    }
}