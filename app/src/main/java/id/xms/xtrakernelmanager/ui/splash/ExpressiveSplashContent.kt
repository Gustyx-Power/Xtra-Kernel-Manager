package id.xms.xtrakernelmanager.ui.splash

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.BuildConfig
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
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

  LaunchedEffect(Unit) {
    val minSplashTime = launch { delay(2500) } // Slightly longer for animation to play

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
      delay(600) // Allow exit transition
      onNavigateToMain()
    }
  }

  Box(
      modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
      contentAlignment = Alignment.Center,
  ) {
    AnimatedVisibility(
        visible = !startExitAnimation,
        exit =
            fadeOut(animationSpec = tween(500)) +
                scaleOut(targetScale = 1.2f, animationSpec = tween(500)),
    ) {
      Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        ShapeMorphingLoader()
      }
    }

    // Dialogs
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

/* ===========================
 *  MAIN COMPOSABLE
 * =========================== */
@Composable
fun ShapeMorphingLoader(modifier: Modifier = Modifier, sizeDp: Int = 96) {
  val dimens = id.xms.xtrakernelmanager.ui.theme.rememberResponsiveDimens()
  val isCompact = dimens.screenSizeClass == id.xms.xtrakernelmanager.ui.theme.ScreenSizeClass.COMPACT
  val actualSize = if (isCompact) 72 else sizeDp
  // 1. Endless Rotation & Breathing (Independent)
  val infinite = rememberInfiniteTransition(label = "loader_effects")

  val rotation by
      infinite.animateFloat(
          initialValue = 0f,
          targetValue = 360f,
          animationSpec = infiniteRepeatable(animation = tween(4000, easing = LinearEasing)),
          label = "rotation",
      )

  val scale by
      infinite.animateFloat(
          initialValue = 0.85f,
          targetValue = 1.1f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(700, easing = FastOutSlowInEasing),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "scale",
      )

  val interestingShapes = remember {
    ShapeType.values().filter {
      it != ShapeType.Circle && it != ShapeType.Oval && it != ShapeType.Pill
    }
  }

  var currentShape by remember { mutableStateOf(interestingShapes.random()) }
  var nextShape by remember { mutableStateOf(interestingShapes.random()) }
  val morphProgress = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    // Initial random start
    currentShape = interestingShapes.random()

    while (true) {
      // Pick a DIFFERENT random shape
      var candidate: ShapeType
      do {
        candidate = interestingShapes.random()
      } while (candidate == currentShape)
      nextShape = candidate

      // Animate 0 -> 1 with smoother timing (400ms)
      morphProgress.snapTo(0f)
      morphProgress.animateTo(
          targetValue = 1f,
          animationSpec = tween(400, easing = FastOutSlowInEasing),
      )

      // Swap state
      currentShape = nextShape

      // Short delay to pause on the shape briefly
      delay(100)
    }
  }

  // FIX: Extract color access outside of Canvas draw scope
  val primaryColor = MaterialTheme.colorScheme.primary

  Canvas(modifier = modifier.size(actualSize.dp).scale(scale)) {
    val cx = size.width / 2
    val cy = size.height / 2
    val R = size.minDimension / 2
    val points = 360 // Increased resolution for smoother curves

    val t = morphProgress.value

    val path = Path()

    repeat(points + 1) { i ->
      val theta = (2 * Math.PI * i / points).toFloat()
      val rot = Math.toRadians(rotation.toDouble()).toFloat()

      // Smooth interpolation between shapes
      val r = lerp(getRadius(currentShape, theta, R), getRadius(nextShape, theta, R), t)

      val x = cx + r * cos(theta + rot)
      val y = cy + r * sin(theta + rot)

      if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }

    path.close()
    drawPath(path, primaryColor)
  }
}

enum class ShapeType {
  Arch,
  Arrow,
  Bun,
  Burst,
  Circle,
  ClamShell,
  Clover4Leaf,
  Clover8Leaf,
  Cookie4Sided,
  Cookie6Sided,
  Cookie7Sided,
  Cookie9Sided,
  Cookie12Sided,
  Diamond,
  Fan,
  Flower,
  Gem,
  Ghostish,
  Heart,
  Oval,
  Pentagon,
  Pill,
  PixelCircle,
  Puffy,
  PuffyDiamond,
  SemiCircle,
  Slanted,
  SoftBoom,
  SoftBurst,
  Square,
  Sunny,
  Triangle,
  VerySunny,
}

fun getRadius(type: ShapeType, theta: Float, R: Float): Float {
  return when (type) {
    ShapeType.Circle -> R
    ShapeType.Oval -> R * (0.9f + 0.1f * cos(2 * theta))
    ShapeType.Pill -> R * (0.8f + 0.2f * cos(2 * theta)) // Similar to Oval but narrower

    ShapeType.Triangle -> R * (0.8f + 0.2f * cos(3 * theta))
    ShapeType.Square -> R * (0.85f + 0.15f * cos(4 * theta))
    ShapeType.Pentagon -> R * (0.9f + 0.1f * cos(5 * theta))

    // Cookies (Rounded Polygons)
    ShapeType.Cookie4Sided -> R * (0.85f + 0.15f * cos(4 * theta))
    ShapeType.Cookie6Sided -> R * (0.9f + 0.1f * cos(6 * theta))
    ShapeType.Cookie7Sided -> R * (0.9f + 0.1f * cos(7 * theta))
    ShapeType.Cookie9Sided -> R * (0.9f + 0.1f * cos(9 * theta))
    ShapeType.Cookie12Sided -> R * (0.95f + 0.05f * cos(12 * theta))

    // Nature / Floral
    ShapeType.Clover4Leaf -> R * (0.8f + 0.2f * cos(4 * theta))
    ShapeType.Clover8Leaf -> R * (0.85f + 0.15f * cos(8 * theta))
    ShapeType.Flower -> R * (0.75f + 0.25f * cos(6 * theta))
    ShapeType.Sunny -> R * (0.8f + 0.2f * cos(8 * theta))
    ShapeType.VerySunny -> R * (0.8f + 0.2f * cos(16 * theta))

    // Bursts / Spikes
    ShapeType.Burst -> R * (0.7f + 0.3f * cos(12 * theta))
    ShapeType.SoftBurst -> R * (0.8f + 0.2f * cos(12 * theta))
    ShapeType.SoftBoom -> R * (0.7f + 0.3f * cos(5 * theta))

    // Abstract / Geometric
    ShapeType.Diamond ->
        R * (0.7f + 0.3f * abs(cos(theta)) * abs(sin(theta)) + 0.5f) // Experimental diamond
    ShapeType.Gem -> R * (0.85f + 0.15f * cos(6 * theta))
    ShapeType.Arrow -> R * (0.7f + 0.3f * cos(3 * theta + 1.57f)) // Rotate to point up/down

    // Fun / Organic
    ShapeType.Ghostish -> R * (0.8f + 0.1f * sin(5 * theta) + 0.1f * cos(theta))
    ShapeType.Puffy -> R * (0.85f + 0.15f * sin(4 * theta))
    ShapeType.PuffyDiamond -> R * (0.8f + 0.2f * cos(2 * theta))
    ShapeType.Bun -> R * (0.8f + 0.15f * cos(2 * theta) - 0.05f * cos(4 * theta))
    ShapeType.ClamShell -> R * (0.8f + 0.2f * cos(10 * theta) * (sin(theta) + 1) / 2)
    ShapeType.Fan -> R * (0.7f + 0.3f * cos(3 * theta))
    ShapeType.Slanted -> R * (0.9f + 0.1f * sin(2 * theta))

    // Pixels / Tech
    ShapeType.PixelCircle -> if ((theta * 10).toInt() % 2 == 0) R else R * 0.9f

    // ShapeType.Heart approx (difficult in pure polar, using 2-lobe approximation)
    ShapeType.Heart -> R * (0.8f - 0.2f * sin(theta) + 0.1f * cos(2 * theta))

    ShapeType.Arch -> R * (0.7f + 0.3f * sin(theta))
    ShapeType.SemiCircle -> if (sin(theta) > 0) R else R * 0.5f

    else -> R
  }
}

fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
