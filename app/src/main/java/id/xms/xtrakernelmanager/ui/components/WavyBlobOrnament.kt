package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WavyBlobOrnament(
    modifier: Modifier = Modifier,
    colors: List<Color>? = null,
    strokeColor: Color? = null,
    strokeWidth: Dp = 0.dp,
    blobAlpha: Float = 0.9f
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkMode = isSystemInDarkTheme()
    
    // iOS 16 inspired color palette - Teal/Green and Periwinkle Blue
    val palette = colors ?: if (isDarkMode) {
        listOf(
            Color(0xFF4A9B8E), 
            Color(0xFF8BA8D8), 
            Color(0xFF6BC4E8)  
        )
    } else {
        listOf(
            Color(0xFFB8D4CE), 
            Color(0xFFC5D5E8), 
            Color(0xFFD0E8F5)  
        )
    }
    val strokeWidthPx = with(LocalDensity.current) { strokeWidth.toPx() }

    // --- Animation State ---
    val transition = rememberInfiniteTransition(label = "ios_petal_ornament")
        val phase1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )
    val phase2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    Canvas(modifier = modifier
        .fillMaxSize()
        .testTag("WavyBlobOrnament")
    ) {
        val w = size.width
        val h = size.height
        
        // Very subtle animation offsets
        fun waveOffset(phase: Float, mult: Float = 1f): Float {
            return sin(phase) * (w * 0.005f * mult)
        }
        
        fun waveCosOffset(phase: Float, mult: Float = 1f): Float {
            return cos(phase) * (h * 0.004f * mult)
        }
        fun drawSmoothPetal(
            path: Path, 
            colorStart: Color,
            colorEnd: Color,
            centerX: Float,
            centerY: Float,
            radius: Float
        ) {
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(
                        colorStart.copy(alpha = blobAlpha),
                        colorStart.copy(alpha = blobAlpha * 0.85f),
                        colorEnd.copy(alpha = blobAlpha * 0.7f)
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius
                )
            )
        }

        // 1. TOP-LEFT BLOB (Teal/Green) - Menempel di pojok kiri atas
        val topLeftBlob = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.35f + waveOffset(phase1), 0f)
            
            // Top-right curve
            cubicTo(
                w * 0.4f, h * 0.02f + waveCosOffset(phase2),
                w * 0.43f + waveOffset(phase2), h * 0.06f,
                w * 0.44f, h * 0.11f + waveCosOffset(phase1)
            )
            
            // Right side curve
            cubicTo(
                w * 0.45f + waveOffset(phase1), h * 0.16f,
                w * 0.44f, h * 0.21f + waveCosOffset(phase2),
                w * 0.41f + waveOffset(phase2), h * 0.25f
            )
            
            // Bottom-right curve
            cubicTo(
                w * 0.38f, h * 0.29f + waveCosOffset(phase1),
                w * 0.33f + waveOffset(phase1), h * 0.31f,
                w * 0.28f, h * 0.32f + waveCosOffset(phase2)
            )
            
            // Bottom curve
            cubicTo(
                w * 0.2f + waveOffset(phase2), h * 0.33f,
                w * 0.12f, h * 0.32f + waveCosOffset(phase1),
                w * 0.05f + waveOffset(phase1), h * 0.29f
            )
            
            // Bottom-left curve
            cubicTo(
                w * 0.02f, h * 0.27f + waveCosOffset(phase2),
                0f, h * 0.24f,
                0f, h * 0.2f + waveCosOffset(phase1)
            )
            
            close()
        }
        
        drawSmoothPetal(
            topLeftBlob,
            palette[0].copy(alpha = 0.92f),
            palette[0].copy(alpha = 0.68f),
            w * 0.22f,
            h * 0.16f,
            w * 0.35f
        )

        // 2. CENTER-RIGHT BLOB (Periwinkle Blue) - Menempel di sisi kanan
        val centerRightBlob = Path().apply {
            moveTo(w, h * 0.25f + waveCosOffset(phase1))
            
            // Top-right corner
            cubicTo(
                w * 0.98f, h * 0.28f + waveCosOffset(phase2),
                w * 0.95f + waveOffset(phase1), h * 0.3f,
                w * 0.91f, h * 0.32f + waveCosOffset(phase1)
            )
            
            // Top-left curve
            cubicTo(
                w * 0.86f + waveOffset(phase2), h * 0.34f,
                w * 0.81f, h * 0.35f + waveCosOffset(phase2),
                w * 0.76f + waveOffset(phase1), h * 0.37f
            )
            
            // Left side curve
            cubicTo(
                w * 0.71f, h * 0.39f + waveCosOffset(phase1),
                w * 0.68f + waveOffset(phase2), h * 0.43f,
                w * 0.67f, h * 0.48f + waveCosOffset(phase2)
            )
            
            // Bottom-left curve
            cubicTo(
                w * 0.66f + waveOffset(phase1), h * 0.53f,
                w * 0.68f, h * 0.58f + waveCosOffset(phase1),
                w * 0.71f + waveOffset(phase2), h * 0.62f
            )
            
            // Bottom curve
            cubicTo(
                w * 0.76f, h * 0.67f + waveCosOffset(phase2),
                w * 0.82f + waveOffset(phase1), h * 0.7f,
                w * 0.88f, h * 0.71f + waveCosOffset(phase1)
            )
            
            // Bottom-right curve
            cubicTo(
                w * 0.93f + waveOffset(phase2), h * 0.72f,
                w * 0.97f, h * 0.71f + waveCosOffset(phase2),
                w, h * 0.69f
            )
            
            close()
        }
        
        drawSmoothPetal(
            centerRightBlob,
            palette[1].copy(alpha = 0.86f),
            palette[1].copy(alpha = 0.58f),
            w * 0.83f,
            h * 0.5f,
            w * 0.28f
        )
        val bottomLeftBlob = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.75f + waveCosOffset(phase1))
            
            // Top-left curve
            cubicTo(
                w * 0.02f, h * 0.72f + waveCosOffset(phase2),
                w * 0.05f + waveOffset(phase1), h * 0.7f,
                w * 0.09f, h * 0.69f + waveCosOffset(phase1)
            )
            
            // Top-right curve
            cubicTo(
                w * 0.14f + waveOffset(phase2), h * 0.68f,
                w * 0.19f, h * 0.68f + waveCosOffset(phase2),
                w * 0.24f + waveOffset(phase1), h * 0.7f
            )
            
            // Right side curve
            cubicTo(
                w * 0.28f, h * 0.72f + waveCosOffset(phase1),
                w * 0.31f + waveOffset(phase2), h * 0.75f,
                w * 0.32f, h * 0.79f + waveCosOffset(phase2)
            )
            
            // Bottom-right curve
            cubicTo(
                w * 0.33f + waveOffset(phase1), h * 0.84f,
                w * 0.31f, h * 0.88f + waveCosOffset(phase1),
                w * 0.28f + waveOffset(phase2), h * 0.91f
            )
            
            // Bottom curve
            cubicTo(
                w * 0.23f, h * 0.95f + waveCosOffset(phase2),
                w * 0.17f + waveOffset(phase1), h * 0.97f,
                w * 0.11f, h * 0.98f + waveCosOffset(phase1)
            )
            
            // Bottom-left corner
            cubicTo(
                w * 0.06f + waveOffset(phase2), h * 0.99f,
                w * 0.02f, h,
                0f, h
            )
            
            close()
        }
        
        drawSmoothPetal(
            bottomLeftBlob,
            palette[2].copy(alpha = 0.8f),
            palette[2].copy(alpha = 0.58f),
            w * 0.16f,
            h * 0.84f,
            w * 0.25f
        )
    }
}