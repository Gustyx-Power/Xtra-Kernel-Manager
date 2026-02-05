package id.xms.xtrakernelmanager.ui.splash

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.graphicsLayer
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.WavyCircularProgressIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun ExpressiveSplashScreen(
    onNavigateToMain: () -> Unit,
    isInternetAvailable: (Context) -> Boolean,
    checkRootAccess: suspend () -> Boolean,
    fetchUpdateConfig: suspend () -> UpdateConfig?,
    isUpdateAvailable: (String, String) -> Boolean,
) {
  val context = LocalContext.current
  var updateConfig by remember { mutableStateOf<UpdateConfig?>(null) }
  var showUpdateDialog by remember { mutableStateOf(false) }
  var showOfflineLockDialog by remember { mutableStateOf(false) }
  var showNoRootDialog by remember { mutableStateOf(false) }
  var isChecking by remember { mutableStateOf(true) }
  var startExitAnimation by remember { mutableStateOf(false) }
  
  // Animation State
  var visible by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    visible = true // Trigger entrance animation
    val minSplashTime = launch { delay(2000) }

    // Check root access first
    val hasRoot = checkRootAccess()

    if (!hasRoot) {
      minSplashTime.join()
      isChecking = false
      showNoRootDialog = true
      return@LaunchedEffect
    }

    val pendingUpdate = UpdatePrefs.getPendingUpdate(context)

    if (
        pendingUpdate != null && isUpdateAvailable(BuildConfig.VERSION_NAME, pendingUpdate.version)
    ) {
      if (isInternetAvailable(context)) {
        minSplashTime.join()
        updateConfig = pendingUpdate
        isChecking = false
        showUpdateDialog = true

        val freshConfig = withTimeoutOrNull(3000L) { fetchUpdateConfig() }
        if (freshConfig != null) {
          updateConfig = freshConfig
          UpdatePrefs.savePendingUpdate(
              context,
              freshConfig.version,
              freshConfig.url,
              freshConfig.changelog,
          )
        }
      } else {
        minSplashTime.join()
        isChecking = false
        showOfflineLockDialog = true
      }
    } else {
      if (pendingUpdate != null) UpdatePrefs.clear(context)

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
            startExitAnimation = true
          }
        } catch (e: Exception) {
          minSplashTime.join()
          isChecking = false
          startExitAnimation = true
        }
      } else {
        minSplashTime.join()
        isChecking = false
        startExitAnimation = true
      }
    }
  }

  if (startExitAnimation) {
    LaunchedEffect(Unit) {
      visible = false // Trigger exit animation
      delay(500) // Transition duration
      onNavigateToMain()
    }
  }

  // --- MATERIAL 3 LAYOUT ---
  Box(
      modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.surface),
      contentAlignment = Alignment.Center,
  ) {
      
    // Branding Content with Animated Visibility
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.8f, animationSpec = tween(500, easing = FastOutSlowInEasing)),
        exit = fadeOut(animationSpec = tween(500)) + scaleOut(targetScale = 1.1f, animationSpec = tween(500))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Material 3 Logo Container
            Surface(
                shape = RoundedCornerShape(28.dp), // M3 Medium Radius
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(108.dp),
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_a),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(64.dp)
                            .scale(1.1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Title
            Text(
                text = "Xtra Kernel Manager",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Version Badge
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Discrete Loading Indicator
            AnimatedVisibility(visible = isChecking) {
                // Simulate indeterminate loading by rotating a fixed progress wavy indicator
                val infiniteTransition = rememberInfiniteTransition(label = "wavy_loader")
                val angle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )

                WavyCircularProgressIndicator(
                    progress = 0.5f, // Half circle
                    modifier = Modifier
                        .size(36.dp)
                        .graphicsLayer { rotationZ = angle },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeWidth = 3.dp,
                    amplitude = 2.dp, // Subtle waves
                    frequency = 8
                )
            }
        }
    }

    // Dialogs (Reused logic)
    if (showUpdateDialog && updateConfig != null) {
      ForceUpdateDialog(config = updateConfig!!, onUpdateClick = { /* ... */ })
    }
    if (showOfflineLockDialog) {
      OfflineLockDialog(
          onRetry = {
            val intent = (context as ComponentActivity).intent
            context.finish()
            context.startActivity(intent)
          }
      )
    }

    if (showNoRootDialog) {
      NoRootDialog(
          onRetry = {
            val intent = (context as ComponentActivity).intent
            context.finish()
            context.startActivity(intent)
          },
          onExit = { (context as ComponentActivity).finish() },
      )
    }
  }
}

