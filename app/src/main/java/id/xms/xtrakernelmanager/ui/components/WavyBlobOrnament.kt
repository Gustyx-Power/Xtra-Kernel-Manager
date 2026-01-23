package id.xms.xtrakernelmanager.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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

/**
 * cleaner WavyBlobOrnament implementation.
 * Draws large, organic, liquid-like shapes with Monet gradients and black strokes.
 */
@Composable
fun WavyBlobOrnament(
    modifier: Modifier = Modifier,
    colors: List<Color>? = null,
    strokeColor: Color = Color.Black.copy(alpha = 0.8f),
    strokeWidth: Dp = 2.5.dp,
    blobAlpha: Float = 0.6f
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

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Helper to draw a shape with gradient fill and stroke
        fun drawOrganicShape(path: Path, color: Color) {
            // Fill with subtle gradient for depth
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = blobAlpha),
                        color.copy(alpha = blobAlpha * 0.7f)
                    ),
                    startY = 0f,
                    endY = h
                )
            )
            // Stroke outline
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(width = strokeWidthPx)
            )
        }

        // 1. TOP RIGHT: Large flowing wave coming from top-right corner
        val topRightPath = Path().apply {
            moveTo(w, 0f)             // Start top right
            lineTo(w * 0.2f, 0f)      // Go left along top edge
            cubicTo(
                w * 0.25f, h * 0.15f, // Control 1
                w * 0.5f, h * 0.05f,  // Control 2
                w * 0.6f, h * 0.2f    // End of first curve
            )
            cubicTo(
                w * 0.7f, h * 0.35f,  // Control 1
                w * 0.9f, h * 0.3f,   // Control 2
                w, h * 0.45f          // Hit right edge
            )
            close()
        }
        drawOrganicShape(topRightPath, palette.getOrElse(0) { Color.Gray })


        // 2. MIDDLE LEFT: Large wave flowing from left side
        val midLeftPath = Path().apply {
            moveTo(0f, h * 0.25f)
            cubicTo(
                w * 0.3f, h * 0.2f,
                w * 0.5f, h * 0.35f,
                w * 0.4f, h * 0.5f
            )
            cubicTo(
                w * 0.3f, h * 0.65f,
                w * 0.15f, h * 0.6f,
                0f, h * 0.7f
            )
            close()
        }
        drawOrganicShape(midLeftPath, palette.getOrElse(1) { Color.Gray })
        
        
        // 3. BOTTOM RIGHT: Large wave flowing from bottom right
        val bottomRightPath = Path().apply {
            moveTo(w, h)
            lineTo(w * 0.4f, h)
            cubicTo(
                w * 0.45f, h * 0.85f,
                w * 0.6f, h * 0.9f,
                w * 0.7f, h * 0.8f
            )
            cubicTo(
                w * 0.8f, h * 0.7f,
                w * 0.95f, h * 0.75f,
                w, h * 0.6f
            )
            close()
        }
        drawOrganicShape(bottomRightPath, palette.getOrElse(2) { Color.Gray })

        
        // 4. CENTER FLOATING BLOB (Optional, for depth)
        // Positioned in the empty space between the other shapes
        val centerBlobPath = Path().apply {
            val cx = w * 0.75f
            val cy = h * 0.6f
            val r = w * 0.15f // Radius-ish
            
            moveTo(cx, cy - r)
            cubicTo(
                cx + r, cy - r,
                cx + r * 1.5f, cy + r,
                cx, cy + r
            )
            cubicTo(
                cx - r * 1.2f, cy + r * 0.8f,
                cx - r, cy - r * 0.5f,
                cx, cy - r
            )
            close()
        }
        drawOrganicShape(centerBlobPath, palette.getOrElse(3) { Color.Gray })
    }
}
