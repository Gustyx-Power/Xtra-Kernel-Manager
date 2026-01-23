package id.xms.xtrakernelmanager.ui.screens.home.components.liquid

import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.topjohnwu.superuser.Shell
import id.xms.xtrakernelmanager.ui.theme.NeonCyan
import id.xms.xtrakernelmanager.ui.theme.NeonPurple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * LiquidDeviceMockup - A futuristic phone mockup with:
 * 1. Neon Glow Effect - soft cyan/purple aura
 * 2. 3D Transform - tilted perspective with floating shadow
 * 3. Hologram Style - glowing edges with scan lines
 * 4. Futuristic Design - glass body with circuit patterns
 */
@Composable
fun LiquidDeviceMockup(
    modifier: Modifier = Modifier,
    size: DpSize = DpSize(160.dp, 320.dp),
    rotation: Float = -15f,
    showWallpaper: Boolean = true,
    glowColor: Color = NeonCyan,
    accentColor: Color = NeonPurple,
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    
    // Convert Dp to Px for Canvas operations
    val widthPx = with(density) { size.width.toPx() }
    val heightPx = with(density) { size.height.toPx() }
    
    // Animation for glow pulsing
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    // Animation for scan line
    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLine"
    )
    
    // Edge glow shimmer
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    // Load wallpaper
    var wallpaperBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    
    LaunchedEffect(showWallpaper) {
        if (showWallpaper) {
            val systemWallpaper = withContext(Dispatchers.IO) { loadWallpaperWithRoot() }
            wallpaperBitmap = systemWallpaper ?: withContext(Dispatchers.IO) {
                try {
                    val options = BitmapFactory.Options().apply { inSampleSize = 4 }
                    BitmapFactory.decodeResource(
                        context.resources,
                        id.xms.xtrakernelmanager.R.drawable.xms,
                        options
                    )?.asImageBitmap()
                } catch (e: Exception) { null }
            }
        }
    }
    
    Box(
        modifier = modifier.size(size.width + 60.dp, size.height + 80.dp),
        contentAlignment = Alignment.Center
    ) {
        
        // Main Phone Body
        Box(
            modifier = Modifier
                .size(size)
                .rotate(rotation)
                .graphicsLayer {
                    // 3D perspective
                    rotationX = 5f
                    cameraDistance = 12f * density.density
                    shadowElevation = 20f
                }
        ) {
            // Phone Frame with Neon Edge
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cornerRadius = 28.dp.toPx()
                val strokeWidth = 2.dp.toPx()
                
                // Glass body background
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1a1a2e),
                            Color(0xFF16213e),
                            Color(0xFF0f0f23)
                        )
                    ),
                    cornerRadius = CornerRadius(cornerRadius),
                )
                
                // Neon edge glow
                val shimmerBrush = Brush.linearGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.3f),
                        glowColor.copy(alpha = 0.8f),
                        accentColor.copy(alpha = 0.8f),
                        glowColor.copy(alpha = 0.3f)
                    ),
                    start = Offset(this.size.width * shimmerOffset, 0f),
                    end = Offset(this.size.width * (shimmerOffset + 0.5f), this.size.height)
                )
                
                drawRoundRect(
                    brush = shimmerBrush,
                    cornerRadius = CornerRadius(cornerRadius),
                    style = Stroke(width = strokeWidth)
                )
                
                // Inner glow line
                drawRoundRect(
                    color = glowColor.copy(alpha = 0.2f),
                    topLeft = Offset(strokeWidth * 2, strokeWidth * 2),
                    size = Size(
                        this.size.width - strokeWidth * 4,
                        this.size.height - strokeWidth * 4
                    ),
                    cornerRadius = CornerRadius(cornerRadius - strokeWidth),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            
            // Screen Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                // Wallpaper or Gradient
                if (wallpaperBitmap != null) {
                    Image(
                        bitmap = wallpaperBitmap!!,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF667eea),
                                        Color(0xFF764ba2),
                                        Color(0xFFf6416c)
                                    ),
                                    start = Offset.Zero,
                                    end = Offset.Infinite
                                )
                            )
                    )
                }
                
                // Glass overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.1f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.2f)
                                )
                            )
                        )
                )
                
                // Scan line effect
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scanY = this.size.height * scanLineOffset
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                glowColor.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(0f, scanY),
                        end = Offset(this.size.width, scanY),
                        strokeWidth = 2.dp.toPx()
                    )
                    
                    // Horizontal lines (hologram effect)
                    for (i in 0..10) {
                        val y = this.size.height * i / 10f
                        drawLine(
                            color = Color.White.copy(alpha = 0.03f),
                            start = Offset(0f, y),
                            end = Offset(this.size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
                
                // Camera notch
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black)
                )
                
                // Circuit pattern overlay (bottom corner)
                Canvas(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(60.dp, 80.dp)
                        .padding(8.dp)
                ) {
                    val circuitColor = glowColor.copy(alpha = 0.15f)
                    val w = this.size.width
                    val h = this.size.height
                    val lineWidth = 1.dp.toPx()
                    
                    // Simple circuit pattern
                    drawLine(circuitColor, Offset(0f, h), Offset(w * 0.3f, h), lineWidth)
                    drawLine(circuitColor, Offset(w * 0.3f, h), Offset(w * 0.3f, h * 0.7f), lineWidth)
                    drawLine(circuitColor, Offset(w * 0.3f, h * 0.7f), Offset(w * 0.6f, h * 0.7f), lineWidth)
                    drawLine(circuitColor, Offset(w * 0.6f, h * 0.7f), Offset(w * 0.6f, h * 0.4f), lineWidth)
                    drawLine(circuitColor, Offset(w * 0.6f, h * 0.4f), Offset(w, h * 0.4f), lineWidth)
                    
                    // Dots at intersections
                    val dotRadius = 3.dp.toPx()
                    drawCircle(circuitColor, dotRadius, Offset(w * 0.3f, h * 0.7f))
                    drawCircle(circuitColor, dotRadius, Offset(w * 0.6f, h * 0.4f))
                }
            }
            
            // Side buttons (power/volume)
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Power button
                drawRoundRect(
                    color = glowColor.copy(alpha = 0.3f),
                    topLeft = Offset(this.size.width - 2.dp.toPx(), this.size.height * 0.25f),
                    size = Size(3.dp.toPx(), 40.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                // Volume buttons
                drawRoundRect(
                    color = glowColor.copy(alpha = 0.2f),
                    topLeft = Offset(-1.dp.toPx(), this.size.height * 0.2f),
                    size = Size(3.dp.toPx(), 30.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                drawRoundRect(
                    color = glowColor.copy(alpha = 0.2f),
                    topLeft = Offset(-1.dp.toPx(), this.size.height * 0.32f),
                    size = Size(3.dp.toPx(), 30.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }
        }
    }
}

// Reuse wallpaper loading logic
private val WALLPAPER_PATHS = listOf(
    "/data/system/users/0/wallpaper_screenshot",
    "/data/system/users/0/wallpaper_screenshot.png",
    "/data/system/users/0/wallpaper",
    "/data/system/users/0/wallpaper.png",
    "/data/system/users/0/wallpaper_orig",
)

private fun loadWallpaperWithRoot(): ImageBitmap? {
    return try {
        val tempFile = File.createTempFile("wallpaper_liquid", ".tmp")
        tempFile.deleteOnExit()

        for (path in WALLPAPER_PATHS) {
            val result = Shell.cmd("cp '$path' '${tempFile.absolutePath}'").exec()
            if (result.isSuccess && tempFile.exists() && tempFile.length() > 0) {
                Shell.cmd("chmod 644 '${tempFile.absolutePath}'").exec()
                val options = BitmapFactory.Options().apply { inSampleSize = 4 }
                val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath, options)
                if (bitmap != null) {
                    tempFile.delete()
                    return bitmap.asImageBitmap()
                }
            }
        }
        tempFile.delete()
        null
    } catch (e: Exception) {
        null
    }
}
