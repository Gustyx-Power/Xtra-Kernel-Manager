package id.xms.xtrakernelmanager.ui.splash

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.MainActivity
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.theme.XtraKernelManagerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.math.max
import kotlin.random.Random

class LiquidSplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            XtraKernelManagerTheme {
                val context = LocalContext.current
                
                LiquidSplashScreen(
                    onAnimationEnd = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    },
                    isInternetAvailable = { isInternetAvailable(it) },
                    checkRootAccess = { checkRootAccess() },
                    fetchUpdateConfig = { fetchUpdateConfig() },
                    isUpdateAvailable = { c, r -> isUpdateAvailable(c, r) }
                )
            }
        }
    }
}

@Composable
fun LiquidSplashScreen(
    onAnimationEnd: () -> Unit = {},
    isInternetAvailable: (Context) -> Boolean,
    checkRootAccess: suspend () -> Boolean,
    fetchUpdateConfig: suspend () -> UpdateConfig?,
    isUpdateAvailable: (String, String) -> Boolean
) {
    val context = LocalContext.current
    
    // OTA Update States
    var updateConfig by remember { mutableStateOf<UpdateConfig?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showOfflineLockDialog by remember { mutableStateOf(false) }
    var showNoRootDialog by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(true) }
    
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

    // OTA Update Check Logic
    LaunchedEffect(Unit) {
        startAnimation = true
        val minSplashTime = launch { delay(2000) }

        // Check root access first
        val hasRoot = checkRootAccess()

        if (!hasRoot) {
            minSplashTime.join()
            isChecking = false
            showNoRootDialog = true
            return@LaunchedEffect
        }

        // Check for pending updates
        val pendingUpdate = UpdatePrefs.getPendingUpdate(context)

        if (pendingUpdate != null && isUpdateAvailable(BuildConfig.VERSION_NAME, pendingUpdate.version)) {
            if (isInternetAvailable(context)) {
                minSplashTime.join()
                updateConfig = pendingUpdate
                isChecking = false
                showUpdateDialog = true

                // Try to fetch fresh config
                val freshConfig = withTimeoutOrNull(3000L) { fetchUpdateConfig() }
                if (freshConfig != null) {
                    updateConfig = freshConfig
                    UpdatePrefs.savePendingUpdate(
                        context,
                        freshConfig.version,
                        freshConfig.url,
                        freshConfig.changelog
                    )
                }
            } else {
                minSplashTime.join()
                isChecking = false
                showOfflineLockDialog = true
            }
        } else {
            // Clear old pending updates
            if (pendingUpdate != null) {
                UpdatePrefs.clear(context)
            }

            if (isInternetAvailable(context)) {
                try {
                    val config = withTimeoutOrNull(5000L) { fetchUpdateConfig() }
                    minSplashTime.join()

                    if (config != null && isUpdateAvailable(BuildConfig.VERSION_NAME, config.version)) {
                        UpdatePrefs.savePendingUpdate(context, config.version, config.url, config.changelog)
                        updateConfig = config
                        isChecking = false
                        showUpdateDialog = true
                    } else {
                        isChecking = false
                        delay(1500) // Hold animation a bit longer
                        onAnimationEnd()
                    }
                } catch (e: Exception) {
                    minSplashTime.join()
                    isChecking = false
                    delay(1500)
                    onAnimationEnd()
                }
            } else {
                minSplashTime.join()
                isChecking = false
                delay(1500)
                onAnimationEnd()
            }
        }
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
                        text = "v${BuildConfig.VERSION_NAME}",
                        color = Color(0xFF7DD3FC), // Sky 300
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                
                // Loading indicator when checking for updates
                if (isChecking) {
                    Spacer(modifier = Modifier.height(24.dp))
                    LiquidLoadingIndicator()
                }
            }
        }
        
        // Update Dialogs
        if (showUpdateDialog && updateConfig != null) {
            LiquidForceUpdateDialog(
                config = updateConfig!!,
                onUpdateClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateConfig!!.url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("OTA", "Browser error", e)
                    }
                }
            )
        }

        if (showOfflineLockDialog) {
            LiquidOfflineLockDialog(
                onRetry = {
                    val intent = (context as ComponentActivity).intent
                    context.finish()
                    context.startActivity(intent)
                }
            )
        }

        if (showNoRootDialog) {
            LiquidNoRootDialog(
                onRetry = {
                    val intent = (context as ComponentActivity).intent
                    context.finish()
                    context.startActivity(intent)
                },
                onExit = { (context as ComponentActivity).finish() }
            )
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



// --- HELPER FUNCTIONS ---
// Moved to OTAUpdateUtils.kt - using shared functions

// --- LIQUID UI COMPONENTS ---

@Composable
fun LiquidLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_loader")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Box(
        modifier = Modifier.size(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation }
        ) {
            val strokeWidth = 3.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        Color.Transparent,
                        Color(0xFF38BDF8).copy(alpha = 0.3f * pulse),
                        Color(0xFF38BDF8).copy(alpha = 0.8f * pulse),
                        Color.Transparent
                    )
                ),
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun LiquidForceUpdateDialog(config: UpdateConfig, onUpdateClick: () -> Unit) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1E293B), // Slate 800
                            Color(0xFF0F172A)  // Slate 900
                        )
                    )
                )
                .border(
                    1.dp,
                    Color(0xFF38BDF8).copy(alpha = 0.3f),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon with glow
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(20.dp)
                            .background(Color(0xFF38BDF8).copy(alpha = 0.4f), CircleShape)
                    )
                    Icon(
                        Icons.Rounded.CloudDownload,
                        null,
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFF38BDF8)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    stringResource(R.string.update_required),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    stringResource(R.string.update_new_version, config.version),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Changelog container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF334155).copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                        .border(
                            1.dp,
                            Color(0xFF475569).copy(alpha = 0.3f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.SystemUpdate,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF7DD3FC)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.update_changelog),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF7DD3FC)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            config.changelog,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Update button with liquid style
                Button(
                    onClick = onUpdateClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38BDF8)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        stringResource(R.string.update_now),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun LiquidOfflineLockDialog(onRetry: () -> Unit) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF7F1D1D), // Red 800
                            Color(0xFF450A0A)  // Red 900
                        )
                    )
                )
                .border(
                    1.dp,
                    Color(0xFFEF4444).copy(alpha = 0.3f),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(20.dp)
                            .background(Color(0xFFEF4444).copy(alpha = 0.4f), CircleShape)
                    )
                    Icon(
                        Icons.Rounded.WifiOff,
                        null,
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFFEF4444)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    stringResource(R.string.connection_required),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    stringResource(R.string.connection_required_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFE5E7EB)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        stringResource(R.string.retry_connection),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun LiquidNoRootDialog(onRetry: () -> Unit, onExit: () -> Unit) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF7F1D1D), // Red 800
                            Color(0xFF450A0A)  // Red 900
                        )
                    )
                )
                .border(
                    1.dp,
                    Color(0xFFEF4444).copy(alpha = 0.3f),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(20.dp)
                            .background(Color(0xFFEF4444).copy(alpha = 0.4f), CircleShape)
                    )
                    Icon(
                        Icons.Rounded.Warning,
                        null,
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFFEF4444)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    stringResource(R.string.root_required_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    stringResource(R.string.root_required_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFE5E7EB)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    stringResource(R.string.root_required_instructions),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start,
                    color = Color(0xFFE5E7EB).copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onExit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Exit")
                    }
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "Retry",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}