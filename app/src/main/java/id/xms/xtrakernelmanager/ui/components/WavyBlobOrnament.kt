package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated cleaner WavyBlobOrnament implementation with Liquid Glass effect.
 * Draws large, organic shapes with:
 * - Volume gradients (Radial) for 3D liquid feel
 * - Specular highlights (Glossy reflection)
 * - Subtle morphing animation
 */
@Composable
fun WavyBlobOrnament(
    modifier: Modifier = Modifier,
    colors: List<Color>? = null,
    strokeColor: Color = Color.Black.copy(alpha = 0.8f),
    strokeWidth: Dp = 2.5.dp,
    blobAlpha: Float = 0.75f // Slightly higher alpha for glass volume
) {
    val colorScheme = MaterialTheme.colorScheme
    
    // Smooth, harmonious Monet colors
    val palette = colors ?: listOf(
        colorScheme.primaryContainer,     // 0: Top Right
        colorScheme.tertiaryContainer,    // 1: Center Left
        colorScheme.secondaryContainer,   // 2: Bottom Right
        colorScheme.primary.copy(0.4f),   // 3: Middle Floating
    )

    val strokeWidthPx = with(LocalDensity.current) { strokeWidth.toPx() }

    // --- Animation State ---
    val transition = rememberInfiniteTransition(label = "liquid_ornament")
    
    // Phase 1: Slow breathing cycle (12 seconds)
    val phase1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    // Phase 2: Slightly faster, offset cycle (7 seconds) for complexity
    val phase2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(17000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Dynamic animation offsets based on screen size
        fun waveOffset(phase: Float, mult: Float = 1f): Float {
            return sin(phase) * (w * 0.03f * mult)
        }
        
        fun waveCosOffset(phase: Float, mult: Float = 1f): Float {
            return cos(phase) * (h * 0.02f * mult)
        }

        // Helper to draw a LIQUID shape
        fun drawLiquidShape(path: Path, color: Color, centerX: Float, centerY: Float) {
            
            // 1. Volume Fill (Radial Gradient for 3D effect)
            // Center is slightly offset to give direction to the volume
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = blobAlpha),      // Center: Pure color
                        color.copy(alpha = blobAlpha * 0.3f) // Edge: Translucent
                    ),
                    center = Offset(centerX, centerY),
                    radius = w * 0.6f // Large radius for soft falloff
                )
            )

            // 2. Specular Highlight (Glossy Reflection)
            // White gradient overlay from top-left (assuming light source)
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f), // Shiny reflection
                        Color.Transparent,
                        Color.Transparent
                    ),
                    start = Offset(centerX - w * 0.2f, centerY - h * 0.2f),
                    end = Offset(centerX + w * 0.2f, centerY + h * 0.2f)
                )
            )

            // 3. Rim Stroke (Dark Outline)
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(width = strokeWidthPx)
            )
        }

        // 1. TOP RIGHT: Animated
        val topRightPath = Path().apply {
            moveTo(w, 0f)
            lineTo(w * 0.2f, 0f)
            
            val p1x = w * 0.25f + waveOffset(phase1)
            val p1y = h * 0.15f + waveCosOffset(phase1)
            val p2x = w * 0.5f + waveOffset(phase2, 0.5f)
            val p2y = h * 0.05f + waveCosOffset(phase2, 0.5f)
            val end1x = w * 0.6f + waveOffset(phase1 + 1f)
            val end1y = h * 0.2f + waveCosOffset(phase1 + 1f)
            
            cubicTo(p1x, p1y, p2x, p2y, end1x, end1y)
            
            val p3x = w * 0.7f + waveOffset(phase2 + 2f)
            val p3y = h * 0.35f + waveCosOffset(phase2 + 2f)
            val p4x = w * 0.9f + waveOffset(phase1 + 3f)
            val p4y = h * 0.3f + waveCosOffset(phase1 + 3f)
            
            cubicTo(p3x, p3y, p4x, p4y, w, h * 0.45f)
            close()
        }
        drawLiquidShape(topRightPath, palette.getOrElse(0) { Color.Gray }, w * 0.8f, h * 0.2f)


        // 2. MIDDLE LEFT: Animated
        val midLeftPath = Path().apply {
            moveTo(0f, h * 0.25f)
            
            val p1x = w * 0.3f + waveOffset(phase2)
            val p1y = h * 0.2f + waveCosOffset(phase2)
            val p2x = w * 0.5f + waveOffset(phase1, 1.2f)
            val p2y = h * 0.35f + waveCosOffset(phase1)
            val end1x = w * 0.4f + waveOffset(phase2 + 1f)
            val end1y = h * 0.5f + waveCosOffset(phase2 + 1f)
            
            cubicTo(p1x, p1y, p2x, p2y, end1x, end1y)
            
            val p3x = w * 0.3f + waveOffset(phase1 + 2f)
            val p3y = h * 0.65f + waveCosOffset(phase1 + 2f)
            val p4x = w * 0.15f + waveOffset(phase2 + 3f)
            val p4y = h * 0.6f + waveCosOffset(phase2 + 3f)
            
            cubicTo(p3x, p3y, p4x, p4y, 0f, h * 0.7f)
            close()
        }
        drawLiquidShape(midLeftPath, palette.getOrElse(1) { Color.Gray }, w * 0.2f, h * 0.4f)
        
        
        // 3. BOTTOM RIGHT: Animated
        val bottomRightPath = Path().apply {
            moveTo(w, h)
            lineTo(w * 0.4f, h)
            
            val p1x = w * 0.45f + waveOffset(phase1 + 0.5f)
            val p1y = h * 0.85f + waveCosOffset(phase1 + 0.5f)
            val p2x = w * 0.6f + waveOffset(phase2 + 0.5f)
            val p2y = h * 0.9f + waveCosOffset(phase2 + 0.5f)
            val end1x = w * 0.7f + waveOffset(phase1 + 2.5f)
            val end1y = h * 0.8f + waveCosOffset(phase1 + 2.5f)

            cubicTo(p1x, p1y, p2x, p2y, end1x, end1y)
            
            val p3x = w * 0.8f + waveOffset(phase2 + 3.5f)
            val p3y = h * 0.7f + waveCosOffset(phase2 + 3.5f)
            val p4x = w * 0.95f + waveOffset(phase1 + 4.5f)
            val p4y = h * 0.75f + waveCosOffset(phase1 + 4.5f)
            
            cubicTo(p3x, p3y, p4x, p4y, w, h * 0.6f)
            close()
        }
        drawLiquidShape(bottomRightPath, palette.getOrElse(2) { Color.Gray }, w * 0.8f, h * 0.8f)

        
        // 4. CENTER FLOATING BLOB: More movement
        val centerBlobPath = Path().apply {
            // Center point moves slightly
            val cx = w * 0.75f + waveOffset(phase2, 0.5f)
            val cy = h * 0.6f + waveCosOffset(phase1, 0.5f)
            val r = w * 0.15f
            
            // Breathing radius
            val breath = r + sin(phase1 * 2f) * (r * 0.05f) 
            
            moveTo(cx, cy - breath)
            cubicTo(
                cx + breath, cy - breath,
                cx + breath * 1.5f, cy + breath,
                cx, cy + breath
            )
            cubicTo(
                cx - breath * 1.2f, cy + breath * 0.8f,
                cx - breath, cy - breath * 0.5f,
                cx, cy - breath
            )
            close()
        }
        // Floating blob gets center position for radial effect
        drawLiquidShape(centerBlobPath, palette.getOrElse(3) { Color.Gray }, w * 0.75f, h * 0.6f)
    }
}
