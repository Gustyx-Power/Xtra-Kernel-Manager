package id.xms.xtrakernelmanager.ui.components.gameoverlay

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Glassmorphic Card Component
 * 
 * A card with glass-like appearance featuring:
 * - Semi-transparent background
 * - Subtle border
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = Color(0xFF1A1A1A).copy(alpha = 0.85f),
    borderColor: Color = Color.White.copy(alpha = 0.1f),
    borderWidth: Dp = 1.dp,
    blurRadius: Float = 25f,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor, shape)
            .border(borderWidth, borderColor, shape)
    ) {
        content()
    }
}

/**
 * Glassmorphic Surface - Alternative using Material3 Surface
 */
@Composable
fun GlassmorphicSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = Color(0xFF1A1A1A).copy(alpha = 0.9f),
    borderColor: Color = Color.White.copy(alpha = 0.08f),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    Surface(
        modifier = modifier
            .clip(shape)
            .border(1.dp, borderColor, shape),
        color = backgroundColor,
        shape = shape,
        tonalElevation = 2.dp
    ) {
        content()
    }
}

/**
 * Glassmorphic Sidebar Card - Optimized for sidebar panels
 */
@Composable
fun GlassmorphicSidebarCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    GlassmorphicCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        backgroundColor = Color(0xFF0D0D0D).copy(alpha = 0.92f),
        borderColor = Color.White.copy(alpha = 0.06f),
        content = content
    )
}

/**
 * Glassmorphic Control Item - For individual control buttons/toggles
 */
@Composable
fun GlassmorphicControlItem(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable BoxScope.() -> Unit
) {
    val backgroundColor = if (isActive) {
        accentColor.copy(alpha = 0.15f)
    } else {
        Color(0xFF2A2A2A).copy(alpha = 0.8f)
    }
    
    val borderColor = if (isActive) {
        accentColor.copy(alpha = 0.4f)
    } else {
        Color.White.copy(alpha = 0.05f)
    }
    
    GlassmorphicCard(
        modifier = modifier,
        cornerRadius = 12.dp,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        content = content
    )
}
