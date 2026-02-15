package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
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
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CompactMorphingSwitcher(
    isLoading: Boolean,
    targetLayout: String,
    modifier: Modifier = Modifier
) {
    // Height animation
    val targetHeight = if (isLoading) 80.dp else 0.dp
    
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "height_animation"
    )
    
    if (animatedHeight > 0.dp) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(animatedHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Loading state
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompactLoadingIndicator()
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Switching to $targetLayout",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Please wait...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "compact_loading")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Canvas(
        modifier = Modifier.size(24.dp)
    ) {
        drawCompactIndicator(rotation, this)
    }
}

private fun drawCompactIndicator(rotation: Float, drawScope: DrawScope) {
    with(drawScope) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 4
        
        rotate(rotation, center) {
            // Draw 6 dots in circle
            for (i in 0 until 6) {
                val angle = (i * 60f) * (kotlin.math.PI / 180f)
                val x = center.x + cos(angle) * radius
                val y = center.y + sin(angle) * radius
                
                val alpha = (1f - (i / 6f)) * 0.8f + 0.2f
                val dotRadius = 2.dp.toPx() * (alpha + 0.3f)
                
                drawCircle(
                    color = Color(0xFF6366F1).copy(alpha = alpha),
                    radius = dotRadius,
                    center = Offset(x.toFloat(), y.toFloat())
                )
            }
        }
    }
}