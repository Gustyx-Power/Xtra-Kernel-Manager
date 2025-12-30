package id.xms.xtrakernelmanager.ui.components.gameoverlay

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import id.xms.xtrakernelmanager.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Custom animated switch for Esports mode without Lottie
 * Features: Glow effect, pulse animation, color transition
 */
@Composable
fun EsportsAnimatedSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFFFF5722),
    inactiveColor: Color = Color(0xFF444444)
) {
    // Track position animation
    val thumbPosition by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "thumbPosition"
    )
    
    // Background color animation
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) activeColor.copy(alpha = 0.3f) else inactiveColor.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "bgColor"
    )
    
    // Thumb color animation
    val thumbColor by animateColorAsState(
        targetValue = if (checked) activeColor else Color.Gray,
        animationSpec = tween(250),
        label = "thumbColor"
    )
    
    // Glow intensity animation
    val glowAlpha by animateFloatAsState(
        targetValue = if (checked) 0.6f else 0f,
        animationSpec = tween(300),
        label = "glow"
    )
    
    // Pulse animation for active state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    val effectiveScale = if (checked) pulseScale else 1f
    
    Box(
        modifier = modifier
            .width(50.dp)
            .height(26.dp)
            .clickable { onCheckedChange(!checked) }
    ) {
        // Glow effect layer (behind the switch)
        if (checked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.2f)
                    .blur(8.dp)
                    .background(
                        activeColor.copy(alpha = glowAlpha * 0.5f),
                        RoundedCornerShape(13.dp)
                    )
            )
        }
        
        // Track
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(13.dp))
                .background(backgroundColor)
        ) {
            // Thumb
            Box(
                modifier = Modifier
                    .padding(3.dp)
                    .offset(x = (thumbPosition * 24).dp)
                    .size(20.dp)
                    .scale(effectiveScale)
            ) {
                // Outer glow
                if (checked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(1.3f)
                            .blur(4.dp)
                            .background(activeColor.copy(alpha = 0.4f), CircleShape)
                    )
                }
                // Main thumb
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    thumbColor,
                                    thumbColor.copy(alpha = 0.8f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (checked) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class Particle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val lifetime: Float
)

private val particleColors = listOf(
    Color(0xFFFF5722),
    Color(0xFFFF9800),
    Color(0xFFFFEB3B),
    Color(0xFFE91E63)
)

/**
 * Fullscreen Esports Mode activation animation without Lottie
 * Features: Particle explosion, expanding rings, glowing icon, text reveal
 */
@Composable
fun EsportsActivationAnimation(
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit = {},
    modeMonsterText: String = stringResource(R.string.mode_monster),
    activatedText: String = stringResource(R.string.mode_monster_activated)
) {
    val animationProgress = remember { Animatable(0f) }
    
    // Particles data - created once
    val particles = remember {
        List(40) {
            Particle(
                angle = Random.nextFloat() * 360f,
                speed = Random.nextFloat() * 400f + 200f,
                size = Random.nextFloat() * 8f + 4f,
                color = particleColors[Random.nextInt(particleColors.size)],
                lifetime = Random.nextFloat() * 0.3f + 0.5f
            )
        }
    }
    
    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(5000, easing = FastOutSlowInEasing)
        )
        onAnimationComplete()
    }
    
    val progress = animationProgress.value
    
    // Icon scale animation - synced with fade out
    val iconScale = when {
        progress < 0.2f -> progress / 0.2f * 1.2f
        progress < 0.4f -> 1.2f - (progress - 0.2f) / 0.2f * 0.2f
        progress < 0.85f -> 1f
        else -> 1f - (progress - 0.85f) / 0.15f * 0.3f  // Shrink slightly when fading
    }
    
    // Icon alpha - synced with text and lightning
    val iconAlpha = when {
        progress < 0.15f -> progress / 0.15f
        progress < 0.85f -> 1f
        progress < 0.95f -> 1f - (progress - 0.85f) / 0.1f
        else -> 0f
    }
    
    // Ring animations
    val ring1Alpha = when {
        progress < 0.2f -> 0f
        progress < 0.6f -> ((progress - 0.2f) / 0.4f).coerceIn(0f, 0.8f) * (1f - (progress - 0.2f) / 0.8f)
        else -> 0f
    }
    
    val ring2Alpha = when {
        progress < 0.3f -> 0f
        progress < 0.7f -> ((progress - 0.3f) / 0.4f).coerceIn(0f, 0.6f) * (1f - (progress - 0.3f) / 0.8f)
        else -> 0f
    }
    
    val ring3Alpha = when {
        progress < 0.4f -> 0f
        progress < 0.8f -> ((progress - 0.4f) / 0.4f).coerceIn(0f, 0.4f) * (1f - (progress - 0.4f) / 0.8f)
        else -> 0f
    }
    
    // Text reveal - synced with icon and lightning fade out
    val textAlpha = when {
        progress < 0.35f -> 0f
        progress < 0.5f -> (progress - 0.35f) / 0.15f
        progress < 0.85f -> 1f
        progress < 0.95f -> 1f - (progress - 0.85f) / 0.1f
        else -> 0f
    }
    
    // Background fade
    val bgAlpha = when {
        progress < 0.1f -> progress / 0.1f
        progress < 0.85f -> 1f
        else -> 1f - (progress - 0.85f) / 0.15f
    }
    
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Horizontal Lightning Effects from left and right
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerY = size.height / 2
            val centerX = size.width / 2
            
            // Lightning timing - appears longer during animation
            val lightningProgress = when {
                progress < 0.15f -> 0f
                progress < 0.5f -> (progress - 0.15f) / 0.35f
                progress < 0.85f -> 1f
                progress < 0.95f -> 1f - (progress - 0.85f) / 0.1f
                else -> 0f
            }
            
            if (lightningProgress > 0f) {
                val lightningAlpha = lightningProgress.coerceIn(0f, 1f)
                
                // Left lightning bolts
                drawLightningBolt(
                    startX = 0f,
                    endX = centerX - 80f,
                    centerY = centerY - 30f,
                    progress = lightningProgress,
                    color = Color(0xFFFF5722).copy(alpha = lightningAlpha * 0.9f),
                    isFromLeft = true
                )
                drawLightningBolt(
                    startX = 0f,
                    endX = centerX - 100f,
                    centerY = centerY + 40f,
                    progress = lightningProgress,
                    color = Color(0xFFFF9800).copy(alpha = lightningAlpha * 0.7f),
                    isFromLeft = true
                )
                drawLightningBolt(
                    startX = 0f,
                    endX = centerX - 60f,
                    centerY = centerY,
                    progress = lightningProgress,
                    color = Color(0xFFFFEB3B).copy(alpha = lightningAlpha * 0.6f),
                    isFromLeft = true
                )
                
                // Right lightning bolts  
                drawLightningBolt(
                    startX = size.width,
                    endX = centerX + 80f,
                    centerY = centerY + 20f,
                    progress = lightningProgress,
                    color = Color(0xFFFF5722).copy(alpha = lightningAlpha * 0.9f),
                    isFromLeft = false
                )
                drawLightningBolt(
                    startX = size.width,
                    endX = centerX + 100f,
                    centerY = centerY - 50f,
                    progress = lightningProgress,
                    color = Color(0xFFFF9800).copy(alpha = lightningAlpha * 0.7f),
                    isFromLeft = false
                )
                drawLightningBolt(
                    startX = size.width,
                    endX = centerX + 70f,
                    centerY = centerY + 60f,
                    progress = lightningProgress,
                    color = Color(0xFFFFEB3B).copy(alpha = lightningAlpha * 0.6f),
                    isFromLeft = false
                )
                
                // Glow effect behind lightning
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF5722).copy(alpha = lightningAlpha * 0.3f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = 200f
                    ),
                    radius = 200f,
                    center = Offset(centerX, centerY)
                )
            }
        }
        
        // Particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            
            particles.forEach { particle ->
                val particleProgress = (progress - (1f - particle.lifetime)) / particle.lifetime
                if (particleProgress in 0f..1f) {
                    val distance = particle.speed * particleProgress
                    val angleRad = particle.angle * PI.toFloat() / 180f
                    val x = center.x + cos(angleRad) * distance
                    val y = center.y + sin(angleRad) * distance
                    val alpha = (1f - particleProgress).coerceIn(0f, 1f)
                    
                    drawCircle(
                        color = particle.color.copy(alpha = alpha),
                        radius = particle.size * (1f - particleProgress * 0.5f),
                        center = Offset(x, y)
                    )
                }
            }
        }
        
        // Expanding rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.minDimension / 2
            
            // Ring 1
            if (ring1Alpha > 0f) {
                val ring1Radius = maxRadius * ((progress - 0.2f) / 0.6f).coerceIn(0f, 1f)
                drawCircle(
                    color = Color(0xFFFF5722).copy(alpha = ring1Alpha),
                    radius = ring1Radius,
                    center = center,
                    style = Stroke(width = 4f)
                )
            }
            
            // Ring 2
            if (ring2Alpha > 0f) {
                val ring2Radius = maxRadius * 0.7f * ((progress - 0.3f) / 0.5f).coerceIn(0f, 1f)
                drawCircle(
                    color = Color(0xFFFF9800).copy(alpha = ring2Alpha),
                    radius = ring2Radius,
                    center = center,
                    style = Stroke(width = 3f)
                )
            }
            
            // Ring 3
            if (ring3Alpha > 0f) {
                val ring3Radius = maxRadius * 0.5f * ((progress - 0.4f) / 0.4f).coerceIn(0f, 1f)
                drawCircle(
                    color = Color(0xFFFFEB3B).copy(alpha = ring3Alpha),
                    radius = ring3Radius,
                    center = center,
                    style = Stroke(width = 2f)
                )
            }
        }
        
        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Speedometer behind icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                // Animated speedometer
                if (iconAlpha > 0f) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val radius = size.minDimension / 2 - 10f
                        
                        // Speedometer arc background
                        drawArc(
                            color = Color(0xFF333333).copy(alpha = iconAlpha * 0.5f),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 12f, cap = StrokeCap.Round),
                            topLeft = Offset(centerX - radius, centerY - radius),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                        )
                        
                        // Colored segments (green -> yellow -> orange -> red)
                        val segmentColors = listOf(
                            Color(0xFF4CAF50), // Green
                            Color(0xFF8BC34A), // Light Green
                            Color(0xFFFFEB3B), // Yellow
                            Color(0xFFFF9800), // Orange
                            Color(0xFFFF5722)  // Red/Orange
                        )
                        val segmentAngle = 270f / segmentColors.size
                        
                        segmentColors.forEachIndexed { index, color ->
                            drawArc(
                                color = color.copy(alpha = iconAlpha * 0.7f),
                                startAngle = 135f + index * segmentAngle,
                                sweepAngle = segmentAngle - 3f,
                                useCenter = false,
                                style = Stroke(width = 8f, cap = StrokeCap.Round),
                                topLeft = Offset(centerX - radius + 8f, centerY - radius + 8f),
                                size = androidx.compose.ui.geometry.Size((radius - 8f) * 2, (radius - 8f) * 2)
                            )
                        }
                        
                        // Animated needle - oscillates based on progress
                        val needleOscillation = when {
                            progress < 0.3f -> progress / 0.3f
                            progress < 0.5f -> 1f - (progress - 0.3f) / 0.2f * 0.3f
                            progress < 0.7f -> 0.7f + (progress - 0.5f) / 0.2f * 0.3f
                            progress < 0.85f -> 1f
                            else -> 1f - (progress - 0.85f) / 0.15f
                        }
                        
                        val needleAngle = 135f + 270f * needleOscillation
                        val needleLength = radius - 25f
                        val needleEndX = centerX + cos(Math.toRadians(needleAngle.toDouble())).toFloat() * needleLength
                        val needleEndY = centerY + sin(Math.toRadians(needleAngle.toDouble())).toFloat() * needleLength
                        
                        // Needle glow
                        drawLine(
                            color = Color(0xFFFF5722).copy(alpha = iconAlpha * 0.4f),
                            start = Offset(centerX, centerY),
                            end = Offset(needleEndX, needleEndY),
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                        
                        // Main needle
                        drawLine(
                            color = Color(0xFFFF5722).copy(alpha = iconAlpha),
                            start = Offset(centerX, centerY),
                            end = Offset(needleEndX, needleEndY),
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                        
                        // Needle center dot
                        drawCircle(
                            color = Color(0xFFFF5722).copy(alpha = iconAlpha),
                            radius = 8f,
                            center = Offset(centerX, centerY)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = iconAlpha),
                            radius = 4f,
                            center = Offset(centerX, centerY)
                        )
                        
                        // Speed tick marks
                        for (i in 0..10) {
                            val tickAngle = 135f + (270f / 10) * i
                            val tickStartRadius = radius - 20f
                            val tickEndRadius = radius - 10f
                            val tickStartX = centerX + cos(Math.toRadians(tickAngle.toDouble())).toFloat() * tickStartRadius
                            val tickStartY = centerY + sin(Math.toRadians(tickAngle.toDouble())).toFloat() * tickStartRadius
                            val tickEndX = centerX + cos(Math.toRadians(tickAngle.toDouble())).toFloat() * tickEndRadius
                            val tickEndY = centerY + sin(Math.toRadians(tickAngle.toDouble())).toFloat() * tickEndRadius
                            
                            drawLine(
                                color = Color.White.copy(alpha = iconAlpha * 0.6f),
                                start = Offset(tickStartX, tickStartY),
                                end = Offset(tickEndX, tickEndY),
                                strokeWidth = if (i % 2 == 0) 2f else 1f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
                
                // Glowing icon with black stroke (on top of speedometer)
                Box(contentAlignment = Alignment.Center) {
                    // Glow layers
                if (progress > 0.1f && iconAlpha > 0f) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = Color(0xFFFF5722).copy(alpha = 0.3f * iconAlpha),
                        modifier = Modifier
                            .size((80 * iconScale).dp)
                            .blur(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = Color(0xFFFF9800).copy(alpha = 0.5f * iconAlpha),
                        modifier = Modifier
                            .size((70 * iconScale).dp)
                            .blur(8.dp)
                    )
                }
                
                // Black stroke layer for icon (multiple offset copies)
                if (iconAlpha > 0f) {
                    val strokeOffsets = listOf(
                        Offset(-2f, -2f), Offset(2f, -2f), Offset(-2f, 2f), Offset(2f, 2f),
                        Offset(-2f, 0f), Offset(2f, 0f), Offset(0f, -2f), Offset(0f, 2f)
                    )
                    strokeOffsets.forEach { offset ->
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = Color.Black.copy(alpha = iconAlpha),
                            modifier = Modifier
                                .size((60 * iconScale).dp)
                                .offset(x = offset.x.dp, y = offset.y.dp)
                        )
                    }
                    
                    // Main icon
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = Color(0xFFFF5722).copy(alpha = iconAlpha),
                        modifier = Modifier
                            .size((60 * iconScale).dp)
                    )
                }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Text with black stroke - "MODE MONSTER"
            Box(contentAlignment = Alignment.Center) {
                // Black stroke for text
                val textStrokeOffsets = listOf(
                    Offset(-1.5f, -1.5f), Offset(1.5f, -1.5f), Offset(-1.5f, 1.5f), Offset(1.5f, 1.5f),
                    Offset(-1.5f, 0f), Offset(1.5f, 0f), Offset(0f, -1.5f), Offset(0f, 1.5f)
                )
                textStrokeOffsets.forEach { offset ->
                    Text(
                        text = modeMonsterText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = textAlpha),
                        letterSpacing = 4.sp,
                        modifier = Modifier.offset(x = offset.x.dp, y = offset.y.dp)
                    )
                }
                Text(
                    text = modeMonsterText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5722).copy(alpha = textAlpha),
                    letterSpacing = 4.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Text with black stroke - "DIAKTIFKAN"
            Box(contentAlignment = Alignment.Center) {
                // Black stroke for text
                val textStrokeOffsets = listOf(
                    Offset(-1f, -1f), Offset(1f, -1f), Offset(-1f, 1f), Offset(1f, 1f),
                    Offset(-1f, 0f), Offset(1f, 0f), Offset(0f, -1f), Offset(0f, 1f)
                )
                textStrokeOffsets.forEach { offset ->
                    Text(
                        text = activatedText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black.copy(alpha = textAlpha * 0.8f),
                        letterSpacing = 8.sp,
                        modifier = Modifier.offset(x = offset.x.dp, y = offset.y.dp)
                    )
                }
                Text(
                    text = activatedText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = textAlpha * 0.8f),
                    letterSpacing = 8.sp
                )
            }
        }
    }
}

/**
 * Rotating energy ring decoration
 */
@Composable
fun EnergyRing(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFF5722),
    strokeWidth: Dp = 2.dp,
    segments: Int = 8
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2 - strokeWidth.toPx()
        val segmentAngle = 360f / segments
        val gapAngle = 15f
        
        rotate(rotation) {
            for (i in 0 until segments) {
                val startAngle = i * segmentAngle
                val sweepAngle = segmentAngle - gapAngle
                drawArc(
                    color = color.copy(alpha = 0.6f + (i % 2) * 0.2f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                    topLeft = Offset(
                        (size.width - radius * 2) / 2,
                        (size.height - radius * 2) / 2
                    ),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                )
            }
        }
    }
}

/**
 * Extension function to draw a jagged lightning bolt
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLightningBolt(
    startX: Float,
    endX: Float,
    centerY: Float,
    progress: Float,
    color: Color,
    isFromLeft: Boolean
) {
    val path = Path()
    val totalLength = kotlin.math.abs(endX - startX)
    val currentLength = totalLength * progress.coerceIn(0f, 1f)
    
    // Number of segments for jagged effect
    val segments = 8
    val segmentLength = totalLength / segments
    
    // Starting point
    val startPoint = if (isFromLeft) startX else startX
    path.moveTo(startPoint, centerY)
    
    // Create jagged lightning path
    for (i in 1..segments) {
        val segmentProgress = (currentLength - (i - 1) * segmentLength) / segmentLength
        if (segmentProgress <= 0f) break
        
        val effectiveProgress = segmentProgress.coerceIn(0f, 1f)
        val x = if (isFromLeft) {
            startX + i * segmentLength * effectiveProgress + (i - 1) * segmentLength
        } else {
            startX - i * segmentLength * effectiveProgress - (i - 1) * segmentLength
        }
        
        // Jagged offset (alternating up/down)
        val yOffset = when {
            i % 4 == 1 -> -25f
            i % 4 == 2 -> 20f
            i % 4 == 3 -> -15f
            else -> 30f
        }
        
        val y = centerY + yOffset * (1f - (i.toFloat() / segments) * 0.3f)
        
        if (i == segments && effectiveProgress >= 1f) {
            // End at center Y for clean finish
            path.lineTo(if (isFromLeft) endX else endX, centerY)
        } else {
            path.lineTo(x.coerceIn(minOf(startX, endX), maxOf(startX, endX)), y)
        }
    }
    
    // Draw the main lightning bolt
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    
    // Draw a thinner bright core
    drawPath(
        path = path,
        color = Color.White.copy(alpha = color.alpha * 0.7f),
        style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}
