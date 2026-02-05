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
import androidx.compose.ui.draw.scale
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
fun MorphingLayoutSwitcher(
    isLoading: Boolean,
    targetLayout: String,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSuccess by remember { mutableStateOf(false) }
    var animationPhase by remember { mutableStateOf(AnimationPhase.HIDDEN) }
    
    // Track loading state changes
    LaunchedEffect(isLoading) {
        if (isLoading) {
            animationPhase = AnimationPhase.MORPHING_UP
            delay(300) // Wait for morph animation
            animationPhase = AnimationPhase.LOADING
        } else if (animationPhase == AnimationPhase.LOADING) {
            animationPhase = AnimationPhase.SUCCESS
            showSuccess = true
            delay(1500) // Show success for 1.5 seconds
            animationPhase = AnimationPhase.MORPHING_DOWN
            delay(300) // Wait for morph down animation
            animationPhase = AnimationPhase.HIDDEN
            showSuccess = false
            onComplete()
        }
    }
    
    // Morph animation
    val morphProgress by animateFloatAsState(
        targetValue = when (animationPhase) {
            AnimationPhase.HIDDEN -> 0f
            AnimationPhase.MORPHING_UP -> 1f
            AnimationPhase.LOADING -> 1f
            AnimationPhase.SUCCESS -> 1f
            AnimationPhase.MORPHING_DOWN -> 0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "morph_progress"
    )
    
    // Scale animation for success state
    val successScale by animateFloatAsState(
        targetValue = if (showSuccess) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "success_scale"
    )
    
    if (morphProgress > 0f) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .scale(successScale)
                .offset(y = (50.dp * (1f - morphProgress)))
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = if (showSuccess) {
                            listOf(
                                Color(0xFF10B981).copy(alpha = 0.9f),
                                Color(0xFF059669).copy(alpha = 0.8f)
                            )
                        } else {
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = showSuccess,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                },
                label = "content_animation"
            ) { success ->
                if (success) {
                    // Success state
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Switched to $targetLayout!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                } else {
                    // Loading state
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MorphingLoadingIndicator()
                        
                        Column {
                            Text(
                                text = "Switching to $targetLayout",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Applying new layout...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MorphingLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Canvas(
        modifier = Modifier.size(32.dp)
    ) {
        drawMorphingIndicator(rotation, pulse, this)
    }
}

private fun drawMorphingIndicator(rotation: Float, pulse: Float, drawScope: DrawScope) {
    with(drawScope) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.minDimension / 6
        
        rotate(rotation, center) {
            // Outer ring
            for (i in 0 until 8) {
                val angle = (i * 45f) * (kotlin.math.PI / 180f)
                val radius = baseRadius * 1.5f
                val x = center.x + cos(angle) * radius
                val y = center.y + sin(angle) * radius
                
                val alpha = (1f - (i / 8f)) * 0.8f + 0.2f
                val circleRadius = (2.5.dp.toPx() * (alpha + 0.3f)) * pulse
                
                drawCircle(
                    color = Color(0xFF6366F1).copy(alpha = alpha),
                    radius = circleRadius,
                    center = Offset(x.toFloat(), y.toFloat())
                )
            }
            
            // Inner ring (counter-rotating)
            rotate(-rotation * 1.5f, center) {
                for (i in 0 until 6) {
                    val angle = (i * 60f) * (kotlin.math.PI / 180f)
                    val radius = baseRadius * 0.8f
                    val x = center.x + cos(angle) * radius
                    val y = center.y + sin(angle) * radius
                    
                    val alpha = (1f - (i / 6f)) * 0.6f + 0.4f
                    val circleRadius = (1.5.dp.toPx() * (alpha + 0.5f)) * pulse
                    
                    drawCircle(
                        color = Color(0xFF8B5CF6).copy(alpha = alpha),
                        radius = circleRadius,
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                }
            }
        }
        
        // Center dot
        drawCircle(
            color = Color(0xFF06B6D4).copy(alpha = 0.8f),
            radius = (3.dp.toPx()) * pulse,
            center = center
        )
    }
}

private enum class AnimationPhase {
    HIDDEN,
    MORPHING_UP,
    LOADING,
    SUCCESS,
    MORPHING_DOWN
}