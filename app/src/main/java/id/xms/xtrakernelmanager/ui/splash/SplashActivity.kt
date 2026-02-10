package id.xms.xtrakernelmanager.ui.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.MainActivity
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.theme.XtraKernelManagerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

// --- PREFERENCES MANAGER (Untuk menyimpan info update) ---
// Moved to OTAUpdateUtils.kt

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      XtraKernelManagerTheme {
        val context = LocalContext.current
        val prefsManager = remember { PreferencesManager(context) }
        val layoutStyle by prefsManager.getLayoutStyle().collectAsState(initial = null)

        val navigateToMain: () -> Unit = {
          startActivity(Intent(this, MainActivity::class.java))
          finish()
          overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        when (layoutStyle) {
            "material" -> {
                ExpressiveSplashScreen(
                    onNavigateToMain = navigateToMain,
                    isInternetAvailable = { isInternetAvailable(it) },
                    checkRootAccess = { checkRootAccess() },
                    fetchUpdateConfig = { fetchUpdateConfig() },
                    isUpdateAvailable = { c, r -> isUpdateAvailable(c, r) },
                )
            }
            "liquid" -> {
                LaunchedEffect(Unit) {
                    val intent = Intent(context, LiquidSplashActivity::class.java)
                    context.startActivity(intent)
                    (context as? android.app.Activity)?.finish()
                    (context as? android.app.Activity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
            "legacy" -> {
                 SplashScreenContent(onNavigateToMain = navigateToMain)
            }
            null -> {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            }
            else -> {
                 SplashScreenContent(onNavigateToMain = navigateToMain)
            }
        }
      }
    }
  }
}

@Composable
fun SplashScreenContent(onNavigateToMain: () -> Unit) {
  val context = LocalContext.current

  var updateConfig by remember { mutableStateOf<UpdateConfig?>(null) }
  var showUpdateDialog by remember { mutableStateOf(false) }
  var showOfflineLockDialog by remember { mutableStateOf(false) }
  var showNoRootDialog by remember { mutableStateOf(false) }
  var isChecking by remember { mutableStateOf(true) }
  var checkingStatus by remember { mutableStateOf(context.getString(R.string.splash_initializing)) }
  var startExitAnimation by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    val minSplashTime = launch { delay(2000) }

    // Check root access first
    checkingStatus = context.getString(R.string.splash_initializing)
    val hasRoot = checkRootAccess()

    if (!hasRoot) {
      minSplashTime.join()
      isChecking = false
      showNoRootDialog = true
      return@LaunchedEffect
    }

    checkingStatus = context.getString(R.string.splash_checking_updates)

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
          // Update info dialog dengan data terbaru
          updateConfig = freshConfig
          // Update penyimpanan lokal
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
      delay(500)
      onNavigateToMain()
    }
  }

  Box(
      modifier =
          Modifier.fillMaxSize()
              .background(
                  Brush.verticalGradient(
                      listOf(
                          MaterialTheme.colorScheme.surface,
                          MaterialTheme.colorScheme.surfaceContainerLowest,
                      )
                  )
              ),
      contentAlignment = Alignment.Center,
  ) {
    BackgroundCircles()

    AnimatedVisibility(
        visible = !startExitAnimation,
        enter = fadeIn(),
        exit = fadeOut() + scaleOut(targetScale = 1.5f),
    ) {
      val dimens = id.xms.xtrakernelmanager.ui.theme.rememberResponsiveDimens()
      val isCompact =
          dimens.screenSizeClass == id.xms.xtrakernelmanager.ui.theme.ScreenSizeClass.COMPACT

      val logoSize = if (isCompact) 100.dp else 140.dp
      val logoImageSize = if (isCompact) 65.dp else 90.dp
      val logoCornerRadius = if (isCompact) 24.dp else 36.dp

      Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
      ) {
        // Logo with glow effect
        Box(
            modifier =
                Modifier.size(logoSize)
                    .clip(RoundedCornerShape(logoCornerRadius))
                    .background(
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                )
                        )
                    )
                    .border(
                        2.dp,
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                )
                        ),
                        RoundedCornerShape(logoCornerRadius),
                    ),
            contentAlignment = Alignment.Center,
        ) {
          Image(
              painter = painterResource(id = R.drawable.logo_a),
              contentDescription = "Logo",
              modifier = Modifier.size(logoImageSize).scale(1.2f),
          )
        }

        Spacer(modifier = Modifier.height(dimens.spacingLarge))

        // App name and version in chip style
        Surface(
            shape = RoundedCornerShape(if (isCompact) 14.dp else 20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 2.dp,
            tonalElevation = 4.dp,
        ) {
          Column(
              modifier =
                  Modifier.padding(horizontal = dimens.cardPadding, vertical = dimens.spacingSmall),
              horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
                text = "Xtra Kernel Manager",
                style =
                    if (isCompact) MaterialTheme.typography.titleMedium
                    else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "v${BuildConfig.VERSION_NAME}-${BuildConfig.BUILD_DATE}",
                style =
                    if (isCompact) MaterialTheme.typography.labelSmall
                    else MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
          }
        }

        Spacer(modifier = Modifier.height(dimens.spacingLarge * 2))

        if (isChecking) {
          ModernLoader(isCompact = isCompact)
          Spacer(modifier = Modifier.height(dimens.spacingMedium))
          Text(
              text = checkingStatus,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }

    if (showUpdateDialog && updateConfig != null) {
      ForceUpdateDialog(
          config = updateConfig!!,
          onUpdateClick = {
            try {
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateConfig!!.url))
              context.startActivity(intent)
            } catch (e: Exception) {
              Log.e("OTA", "Browser error", e)
            }
          },
      )
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

/**
 * Check if the device has root access using libsu This is more compatible with Magisk 28+ than
 * Runtime.exec
 */
private suspend fun checkRootAccessLocal(): Boolean =
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
      try {
        // Check debug bypass first
        val model = android.os.Build.MODEL
        val isDebugBuild = BuildConfig.DEBUG
        if (isDebugBuild && model == "I2219") {
          Log.d("RootCheck", "Debug bypass active for Vivo I2219")
          return@withContext true
        }

        // Use libsu Shell which properly handles Magisk 28+ root requests
        val shell = com.topjohnwu.superuser.Shell.getShell()
        val isRoot = shell.isRoot
        Log.d("RootCheck", "Root check via libsu: $isRoot")
        isRoot
      } catch (e: Exception) {
        Log.e("RootCheck", "Root check failed: ${e.message}")
        false
      }
    }

// --- HELPERS ---
// Other helper functions moved to OTAUpdateUtils.kt

// --- UI COMPONENTS ---

@Composable
fun ForceUpdateDialog(config: UpdateConfig, onUpdateClick: () -> Unit) {
  Dialog(
      onDismissRequest = {},
      properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
  ) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Box(
            modifier =
                Modifier.size(72.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
          Icon(
              Icons.Rounded.CloudDownload,
              null,
              modifier = Modifier.size(36.dp),
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.update_required),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            stringResource(R.string.update_new_version, config.version),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(16.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.SystemUpdate,
                null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(R.string.update_changelog),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
              config.changelog,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onUpdateClick,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors =
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
          Text(stringResource(R.string.update_now))
        }
      }
    }
  }
}

@Composable
fun OfflineLockDialog(onRetry: () -> Unit) {
  Dialog(
      onDismissRequest = {},
      properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
  ) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Icon(
            Icons.Rounded.WifiOff,
            null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.connection_required),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.connection_required_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
          Text(stringResource(R.string.retry_connection))
        }
      }
    }
  }
}

@Composable
fun NoRootDialog(onRetry: () -> Unit, onExit: () -> Unit) {
  Dialog(
      onDismissRequest = {},
      properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
  ) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Icon(
            Icons.Rounded.Warning,
            null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.root_required_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.root_required_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.root_required_instructions),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          OutlinedButton(
              onClick = onExit,
              modifier = Modifier.weight(1f),
          ) {
            Text("Exit")
          }
          Button(
              onClick = onRetry,
              modifier = Modifier.weight(1f),
              colors =
                  ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          ) {
            Text("Retry")
          }
        }
      }
    }
  }
}

@Composable
fun ModernLoader(isCompact: Boolean = false) {
  val infiniteTransition = rememberInfiniteTransition(label = "loader")

  // Main rotation
  val rotation by
      infiniteTransition.animateFloat(
          initialValue = 0f,
          targetValue = 360f,
          animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
          label = "rotation",
      )

  // Secondary rotation (opposite direction)
  val rotation2 by
      infiniteTransition.animateFloat(
          initialValue = 360f,
          targetValue = 0f,
          animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
          label = "rotation2",
      )

  // Pulse animation for glow effect
  val pulse by
      infiniteTransition.animateFloat(
          initialValue = 0.6f,
          targetValue = 1f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(800, easing = FastOutSlowInEasing),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "pulse",
      )

  // Scale breathing effect
  val scale by
      infiniteTransition.animateFloat(
          initialValue = 0.95f,
          targetValue = 1.05f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(1000, easing = FastOutSlowInEasing),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "scale",
      )

  val primaryColor = MaterialTheme.colorScheme.primary
  val secondaryColor = MaterialTheme.colorScheme.secondary
  val tertiaryColor = MaterialTheme.colorScheme.tertiary

  // Responsive sizes
  val boxSize = if (isCompact) 48.dp else 64.dp
  val outerRingSize = if (isCompact) 48.dp else 64.dp
  val middleRingSize = if (isCompact) 40.dp else 52.dp
  val innerRingSize = if (isCompact) 30.dp else 40.dp
  val centerDotSize = if (isCompact) 9.dp else 12.dp
  val outerStroke = if (isCompact) 6.dp else 8.dp
  val middleStroke = if (isCompact) 3.dp else 4.dp
  val innerStroke = if (isCompact) 4.dp else 5.dp

  Box(modifier = Modifier.size(boxSize), contentAlignment = Alignment.Center) {
    // Outer glow ring
    Canvas(modifier = Modifier.size(outerRingSize).scale(scale)) {
      drawArc(
          brush =
              Brush.sweepGradient(
                  listOf(
                      Color.Transparent,
                      primaryColor.copy(alpha = 0.2f * pulse),
                      primaryColor.copy(alpha = 0.4f * pulse),
                      Color.Transparent,
                  )
              ),
          startAngle = rotation2,
          sweepAngle = 270f,
          useCenter = false,
          style = Stroke(width = outerStroke.toPx(), cap = StrokeCap.Round),
      )
    }

    // Middle ring
    Canvas(modifier = Modifier.size(middleRingSize)) {
      drawArc(
          brush =
              Brush.sweepGradient(
                  listOf(
                      Color.Transparent,
                      secondaryColor.copy(alpha = 0.3f),
                      secondaryColor.copy(alpha = 0.7f),
                      Color.Transparent,
                  )
              ),
          startAngle = rotation2 + 45f,
          sweepAngle = 180f,
          useCenter = false,
          style = Stroke(width = middleStroke.toPx(), cap = StrokeCap.Round),
      )
    }

    // Inner spinning ring (main)
    Canvas(modifier = Modifier.size(innerRingSize)) {
      drawArc(
          brush =
              Brush.sweepGradient(
                  listOf(
                      Color.Transparent,
                      Color.Transparent,
                      primaryColor.copy(alpha = 0.8f),
                      primaryColor,
                  )
              ),
          startAngle = rotation,
          sweepAngle = 240f,
          useCenter = false,
          style = Stroke(width = innerStroke.toPx(), cap = StrokeCap.Round),
      )
    }

    // Center dot with pulse
    Canvas(modifier = Modifier.size(centerDotSize)) {
      drawCircle(color = tertiaryColor.copy(alpha = pulse), radius = size.minDimension / 2)
    }
  }
}

@Composable
fun BackgroundCircles() {
  val infiniteTransition = rememberInfiniteTransition(label = "bg_circles")

  // Floating animation for top-right circle
  val offsetY1 by
      infiniteTransition.animateFloat(
          initialValue = 0f,
          targetValue = 30f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(3000, easing = FastOutSlowInEasing),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "offsetY1",
      )

  val offsetX1 by
      infiniteTransition.animateFloat(
          initialValue = 0f,
          targetValue = -20f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(4000, easing = FastOutSlowInEasing),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "offsetX1",
      )

  // Floating animation for bottom-left circle
  val offsetY2 by
      infiniteTransition.animateFloat(
          initialValue = 0f,
          targetValue = -25f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(3500, easing = FastOutSlowInEasing),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "offsetY2",
      )

  val offsetX2 by
      infiniteTransition.animateFloat(
          initialValue = 0f,
          targetValue = 20f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(2800, easing = FastOutSlowInEasing),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "offsetX2",
      )

  // Scale breathing for circles
  val scale1 by
      infiniteTransition.animateFloat(
          initialValue = 1f,
          targetValue = 1.1f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(4000, easing = FastOutSlowInEasing),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "scale1",
      )

  val scale2 by
      infiniteTransition.animateFloat(
          initialValue = 1f,
          targetValue = 1.15f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(3200, easing = FastOutSlowInEasing),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "scale2",
      )

  // Alpha pulsing
  val alpha1 by
      infiniteTransition.animateFloat(
          initialValue = 0.25f,
          targetValue = 0.4f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(2500, easing = FastOutSlowInEasing),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "alpha1",
      )

  val alpha2 by
      infiniteTransition.animateFloat(
          initialValue = 0.08f,
          targetValue = 0.18f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(3000, easing = FastOutSlowInEasing),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "alpha2",
      )

  // Third floating circle
  val offsetY3 by
      infiniteTransition.animateFloat(
          initialValue = 0f,
          targetValue = 40f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(5000, easing = FastOutSlowInEasing),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "offsetY3",
      )

  val primaryColor = MaterialTheme.colorScheme.primaryContainer
  val secondaryColor = MaterialTheme.colorScheme.secondaryContainer
  val tertiaryColor = MaterialTheme.colorScheme.tertiaryContainer

  Canvas(modifier = Modifier.fillMaxSize()) {
    // Top-right large circle with gradient
    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        primaryColor.copy(alpha = alpha1),
                        primaryColor.copy(alpha = alpha1 * 0.5f),
                        Color.Transparent,
                    ),
                center = androidx.compose.ui.geometry.Offset(size.width + offsetX1, offsetY1),
                radius = size.width * 0.6f * scale1,
            ),
        center = androidx.compose.ui.geometry.Offset(size.width + offsetX1, offsetY1),
        radius = size.width * 0.6f * scale1,
    )

    // Bottom-left circle
    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        secondaryColor.copy(alpha = alpha2),
                        secondaryColor.copy(alpha = alpha2 * 0.3f),
                        Color.Transparent,
                    ),
                center = androidx.compose.ui.geometry.Offset(offsetX2, size.height + offsetY2),
                radius = size.width * 0.45f * scale2,
            ),
        center = androidx.compose.ui.geometry.Offset(offsetX2, size.height + offsetY2),
        radius = size.width * 0.45f * scale2,
    )

    // Center-right floating orb
    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        tertiaryColor.copy(alpha = 0.15f),
                        tertiaryColor.copy(alpha = 0.05f),
                        Color.Transparent,
                    ),
                center =
                    androidx.compose.ui.geometry.Offset(
                        size.width * 0.8f,
                        size.height * 0.4f + offsetY3,
                    ),
                radius = size.width * 0.25f,
            ),
        center =
            androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.4f + offsetY3),
        radius = size.width * 0.25f,
    )
  }
}
