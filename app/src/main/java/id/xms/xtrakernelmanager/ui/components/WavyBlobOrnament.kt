package id.xms.xtrakernelmanager.ui.components

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    blobAlpha: Float = 0.85f
) {
    val isDarkMode = isSystemInDarkTheme()
    
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

    val transition = rememberInfiniteTransition(label = "mesh_gradient_transition")

    val phase1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )
    
    val phase3 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(34000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase3"
    )

    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(60.dp)
    } else {
        Modifier
    }

    Canvas(modifier = modifier
        .fillMaxSize()
        .then(blurModifier)
        .testTag("WavyBlobOrnament")
    ) {
        val w = size.width
        val h = size.height
        
        val baseRadius = w * 0.75f

        val orb1X = w * 0.2f + sin(phase1) * (w * 0.3f)
        val orb1Y = h * 0.8f + cos(phase2) * (h * 0.2f)

        val orb2X = w * 0.8f + cos(phase2) * (w * 0.3f)
        val orb2Y = h * 0.4f + sin(phase3) * (h * 0.3f)

        val orb3X = w * 0.3f + sin(phase3) * (w * 0.4f)
        val orb3Y = h * 0.2f + cos(phase1) * (h * 0.2f)

        val orb4X = w * 0.6f + cos(phase1) * (w * 0.2f)
        val orb4Y = h * 0.7f + sin(phase2) * (h * 0.2f)

        fun drawOrb(color: Color, cx: Float, cy: Float, radius: Float) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = blobAlpha),
                        color.copy(alpha = blobAlpha * 0.6f),
                        color.copy(alpha = 0f)
                    ),
                    center = Offset(cx, cy),
                    radius = radius
                ),
                radius = radius,
                center = Offset(cx, cy)
            )
        }

        val c1 = palette.getOrElse(0) { Color.Transparent }
        val c2 = palette.getOrElse(1) { c1 }
        val c3 = palette.getOrElse(2) { c2 }
        
        drawOrb(c3, orb3X, orb3Y, baseRadius * 0.9f)
        drawOrb(c2, orb2X, orb2Y, baseRadius * 1.1f)
        drawOrb(c1, orb1X, orb1Y, baseRadius)
        
        drawOrb(c1.copy(alpha = blobAlpha * 0.8f), orb4X, orb4Y, baseRadius * 0.8f)
    }
}