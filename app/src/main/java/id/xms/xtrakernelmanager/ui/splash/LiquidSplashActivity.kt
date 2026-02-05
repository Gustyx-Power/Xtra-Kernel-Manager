package id.xms.xtrakernelmanager.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import id.xms.xtrakernelmanager.MainActivity
import id.xms.xtrakernelmanager.ui.theme.XtraKernelManagerTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

class LiquidSplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            XtraKernelManagerTheme {
                LiquidSplashScreen(
                    onAnimationEnd = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                )
            }
        }
    }
}

@Composable
fun LiquidSplashScreen(onAnimationEnd: () -> Unit = {}) {
    // Animation Trigger
    var startAnimation by remember { mutableStateOf(false) }
    
    // Transition for content entrance
    val transition = updateTransition(targetState = startAnimation, label = "ContentEntrance")
    
    // 1. Logo Animation (Elastic Pop)
    val logoScale by transition.animateFloat(
        transitionSpec = { 
            tween(durationMillis = 1000, delayMillis = 200, easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)) 
        },
        label = "LogoScale"
    ) { if (it) 1f else 0f }
    
    val logoAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 600, delayMillis = 200) },
        label = "LogoAlpha"
    ) { if (it) 1f else 0f }

    // 2. Text Animation (Fluid Slide Up)
    val textOffset by transition.animateDp(
        transitionSpec = { 
            tween(durationMillis = 1000, delayMillis = 500, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)) 
        },
        label = "TextOffset"
    ) { if (it) 0.dp else 50.dp }
    
    val textAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, delayMillis = 500) },
        label = "TextAlpha"
    ) { if (it) 1f else 0f }

    // Start Sequence
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3500) // Hold duration
        onAnimationEnd()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050510)), // Very dark base
        contentAlignment = Alignment.Center
    ) {
        // --- 1. THE LIQUID BACKGROUND (Lava Lamp Effect) ---
        LiquidLavaBackground()

        // --- 2. MAIN CONTENT ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = (-20).dp) // Visual center adjustment
        ) {
            // Logo Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        alpha = logoAlpha
                    }
            ) {
                // Subtle Glow behind logo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(30.dp)
                        .background(Color(0x4038BDF8), shape = androidx.compose.foundation.shape.CircleShape)
                )
                
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = id.xms.xtrakernelmanager.R.drawable.logo_a),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Text Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    translationY = textOffset.toPx()
                    alpha = textAlpha
                }
            ) {
                Text(
                    text = "Xtra",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    letterSpacing = (-1.5).sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "KERNEL MANAGER", // Liquid style: Clean, spaced out
                    color = Color(0xFF94A3B8), // Slate 400
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    letterSpacing = 3.sp 
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Liquid Pill Badge
                Surface(
                    color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                    border = border(width = 1.dp, color = Color(0xFF38BDF8).copy(alpha = 0.3f), shape = androidx.compose.foundation.shape.RoundedCornerShape(50))
                ) {
                    Text(
                        text = "v3.0",
                        color = Color(0xFF7DD3FC), // Sky 300
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// Helper for the custom border since Surface border param expects BorderStroke
fun border(width: androidx.compose.ui.unit.Dp, color: Color, shape: androidx.compose.ui.graphics.Shape) = 
    androidx.compose.foundation.BorderStroke(width, color)


@Composable
fun LiquidLavaBackground() {
    // Animate blobs moving in random patterns
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidLava")

    // Blob 1: Cyan - Top Left to Center
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse),
        label = "Blob1"
    )
    
    // Blob 2: Purple - Bottom Right to Center
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(11000, easing = LinearEasing), RepeatMode.Reverse),
        label = "Blob2"
    )
    
    // Blob 3: Blue - Moving horizontally
    val offset3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "Blob3"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        val screenWidth = configuration.screenWidthDp.dp

        // Huge Blur container to merge the blobs
        Box(modifier = Modifier
            .fillMaxSize()
            .blur(60.dp) // The key to the "Liquid" look - blurring shapes together
            .alpha(0.6f)
        ) {
            // Blob 1 (Cyan)
            Box(
                modifier = Modifier
                    .offset(
                        x = (screenWidth * 0.1f) + (screenWidth * 0.4f * offset1),
                        y = (screenHeight * 0.1f) + (screenHeight * 0.3f * offset2) // Mix offset2 for randomness
                    )
                    .size(250.dp)
                    .background(Color(0xFF06B6D4), androidx.compose.foundation.shape.CircleShape) // Cyan
            )

            // Blob 2 (Purple)
            Box(
                modifier = Modifier
                    .offset(
                        x = (screenWidth * 0.6f) - (screenWidth * 0.4f * offset2),
                        y = (screenHeight * 0.6f) - (screenHeight * 0.3f * offset1)
                    )
                    .size(280.dp)
                    .background(Color(0xFF7C3AED), androidx.compose.foundation.shape.CircleShape) // Violet
            )

            // Blob 3 (Deep Blue)
            Box(
                modifier = Modifier
                    .offset(
                        x = (screenWidth * 0.2f) + (screenWidth * 0.5f * offset3),
                        y = (screenHeight * 0.4f) + (screenHeight * 0.1f * offset1)
                    )
                    .size(220.dp)
                    .background(Color(0xFF2563EB), androidx.compose.foundation.shape.CircleShape) // Blue
            )
        }
    }
}


