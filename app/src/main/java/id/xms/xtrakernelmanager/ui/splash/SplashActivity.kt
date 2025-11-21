package id.xms.xtrakernelmanager.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.MainActivity
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.theme.XtraKernelManagerTheme
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            XtraKernelManagerTheme {
                ImplodeSplashScreen(
                    onTimeout = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                        // Disable default activity transition
                        overridePendingTransition(0, 0)
                    }
                )
            }
        }
    }
}

@Composable
fun ImplodeSplashScreen(onTimeout: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    
    // Monet or Green
    val hasMonet = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val loadingColor = if (hasMonet) {
        MaterialTheme.colorScheme.primary
    } else {
        if (isDark) Color(0xFF4CAF50) else Color(0xFF2E7D32)
    }
    
    var splashVisible by remember { mutableStateOf(true) }
    var logoVisible by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }
    var subtitleVisible by remember { mutableStateOf(false) }
    var loadingVisible by remember { mutableStateOf(false) }
    var versionVisible by remember { mutableStateOf(false) }
    
    var loadingStyle by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        // Enter animations
        delay(200)
        logoVisible = true
        delay(300)
        textVisible = true
        delay(200)
        subtitleVisible = true
        delay(200)
        loadingVisible = true
        delay(100)
        versionVisible = true
        
        // Loading style changes
        delay(1400)
        loadingStyle = 1
        delay(1400)
        loadingStyle = 2
        delay(1400)
        
        // Exit animation - implode!
        splashVisible = false
        delay(600) // Wait for animation to complete
        onTimeout()
    }
    
    val backgroundColor = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0D0D0D),
                Color(0xFF1A1A2E)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF8F9FA),
                Color(0xFFE9ECEF)
            )
        )
    }
    
    // Animated visibility with implode effect
    AnimatedVisibility(
        visible = splashVisible,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(400)) + scaleOut(
            targetScale = 0.3f,
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            ),
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo with Circular Background
                AnimatedVisibility(
                    visible = logoVisible,
                    enter = fadeIn(
                        animationSpec = tween(700, easing = FastOutSlowInEasing)
                    ) + scaleIn(
                        initialScale = 0.3f,
                        animationSpec = tween(700, easing = FastOutSlowInEasing)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = if (isDark) {
                                        listOf(
                                            Color(0xFF2D2D3D),
                                            Color(0xFF1A1A2E)
                                        )
                                    } else {
                                        listOf(
                                            Color(0xFFFFFFFF),
                                            Color(0xFFF0F0F0)
                                        )
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_a),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(90.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // App name
                AnimatedVisibility(
                    visible = textVisible,
                    enter = fadeIn(animationSpec = tween(600)) + 
                            slideInVertically(
                                initialOffsetY = { 20 },
                                animationSpec = tween(600)
                            )
                ) {
                    Text(
                        text = "Xtra Kernel Manager",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1A1A2E)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Subtitle
                AnimatedVisibility(
                    visible = subtitleVisible,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    Text(
                        text = stringResource(R.string.advanced_kernel_control),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isDark) Color(0xFFB0B0C0) else Color(0xFF6C757D)
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Hybrid Loading
                AnimatedVisibility(
                    visible = loadingVisible,
                    enter = fadeIn(animationSpec = tween(500)) + 
                            scaleIn(
                                initialScale = 0.8f,
                                animationSpec = tween(500)
                            )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        AnimatedContent(
                            targetState = loadingStyle,
                            transitionSpec = {
                                fadeIn(tween(500, easing = FastOutSlowInEasing)) + 
                                        scaleIn(tween(500, easing = FastOutSlowInEasing)) togetherWith
                                        fadeOut(tween(400, easing = FastOutSlowInEasing)) + 
                                        scaleOut(tween(400, easing = FastOutSlowInEasing))
                            },
                            label = "loading_style"
                        ) { style ->
                            when (style) {
                                0 -> CircularLoading(
                                    modifier = Modifier.size(48.dp),
                                    color = loadingColor
                                )
                                1 -> DotsLoading(
                                    color = loadingColor
                                )
                                else -> LinesLoading(
                                    modifier = Modifier.width(200.dp),
                                    color = loadingColor
                                )
                            }
                        }
                        
                        PulsingText(
                            text = stringResource(R.string.initializing_kernel_modules),
                            isDark = isDark
                        )
                    }
                }
            }
            
            // Version at bottom
            AnimatedVisibility(
                visible = versionVisible,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                enter = fadeIn(animationSpec = tween(400)) + 
                        slideInVertically(
                            initialOffsetY = { 30 },
                            animationSpec = tween(400)
                        )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Version ${BuildConfig.VERSION_NAME}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color(0xFF606070) else Color(0xFF868E96)
                    )
                    Text(
                        text = "Build ${BuildConfig.VERSION_CODE}",
                        fontSize = 10.sp,
                        color = if (isDark) Color(0xFF404050) else Color(0xFFADB5BD)
                    )
                }
            }
        }
    }
}

// Loading Style 1: Circular
@Composable
fun CircularLoading(
    modifier: Modifier = Modifier,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circular")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension / 6f
        
        drawCircle(
            color = color.copy(alpha = 0.15f),
            radius = (size.minDimension - strokeWidth) / 2f,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        drawArc(
            color = color,
            startAngle = rotation - 90f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

// Loading Style 2: Dots
@Composable
fun DotsLoading(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 900,
                        delayMillis = index * 250,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .scale(scale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color,
                                color.copy(alpha = 0.6f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}

// Loading Style 3: Lines
@Composable
fun LinesLoading(
    modifier: Modifier = Modifier,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lines")
    
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )
    
    Canvas(modifier = modifier.height(8.dp)) {
        val strokeWidth = size.height
        val trackColor = color.copy(alpha = 0.15f)
        
        drawLine(
            color = trackColor,
            start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        
        val progressWidth = size.width * 0.4f
        val startX = (size.width - progressWidth) * offset
        val endX = startX + progressWidth
        
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(startX, size.height / 2),
            end = androidx.compose.ui.geometry.Offset(endX.coerceAtMost(size.width), size.height / 2),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun PulsingText(text: String, isDark: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_text")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_alpha"
    )
    
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        color = if (isDark) {
            Color.White.copy(alpha = alpha * 0.7f)
        } else {
            Color(0xFF495057).copy(alpha = alpha * 0.8f)
        },
        letterSpacing = 0.3.sp
    )
}