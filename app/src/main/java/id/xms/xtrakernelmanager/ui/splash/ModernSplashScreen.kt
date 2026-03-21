package id.xms.xtrakernelmanager.ui.splash

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun ModernSplashScreen(onNavigateToMain: () -> Unit) {
  val context = LocalContext.current
  var updateConfig by remember { mutableStateOf<UpdateConfig?>(null) }
  var showUpdateDialog by remember { mutableStateOf(false) }
  var showOfflineLockDialog by remember { mutableStateOf(false) }
  var isChecking by remember { mutableStateOf(true) }
  var startAnimation by remember { mutableStateOf(false) }

  val logoScale by animateFloatAsState(
    targetValue = if (startAnimation) 1f else 0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "logo_scale"
  )

  val textAlpha by animateFloatAsState(
    targetValue = if (startAnimation) 1f else 0f,
    animationSpec = tween(durationMillis = 800, delayMillis = 300),
    label = "text_alpha"
  )

  val progressAlpha by animateFloatAsState(
    targetValue = if (isChecking) 1f else 0f,
    animationSpec = tween(durationMillis = 400),
    label = "progress_alpha"
  )

  LaunchedEffect(Unit) {
    startAnimation = true
    val minSplashTime = launch { delay(2000) }

    val pendingUpdate = UpdatePrefs.getPendingUpdate(context)
    if (pendingUpdate != null && isUpdateAvailable(BuildConfig.VERSION_NAME, pendingUpdate.version)) {
      if (isInternetAvailable(context)) {
        minSplashTime.join()
        updateConfig = pendingUpdate
        isChecking = false
        showUpdateDialog = true

        val freshConfig = withTimeoutOrNull(3000L) { fetchUpdateConfig() }
        if (freshConfig != null) {
          updateConfig = freshConfig
          UpdatePrefs.savePendingUpdate(context, freshConfig.version, freshConfig.url, freshConfig.changelog)
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
            delay(500)
            onNavigateToMain()
          }
        } catch (e: Exception) {
          minSplashTime.join()
          isChecking = false
          delay(500)
          onNavigateToMain()
        }
      } else {
        minSplashTime.join()
        isChecking = false
        delay(500)
        onNavigateToMain()
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0A0E1A)),
    contentAlignment = Alignment.Center
  ) {
    AnimatedBackgroundCircles()

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.offset(y = (-40).dp)
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(140.dp)
          .graphicsLayer {
            scaleX = logoScale
            scaleY = logoScale
          }
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .blur(40.dp)
            .background(
              Color(0xFF38BDF8).copy(alpha = 0.3f),
              shape = CircleShape
            )
        )
        
        Image(
          painter = painterResource(id = R.drawable.logo_a),
          contentDescription = "Xtra Kernel Manager Logo",
          modifier = Modifier.size(110.dp)
        )
      }

      Spacer(modifier = Modifier.height(32.dp))

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer { alpha = textAlpha }
      ) {
        Text(
          text = "XTRA KERNEL MANAGER",
          style = MaterialTheme.typography.headlineMedium,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          letterSpacing = 4.sp,
          fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "EXPERIENCE KERNEL ANDROID TOOLS",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF94A3B8),
          letterSpacing = 2.sp,
          fontSize = 10.sp
        )
      }

      Spacer(modifier = Modifier.height(80.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .graphicsLayer { alpha = progressAlpha },
        contentAlignment = Alignment.BottomCenter
      ) {
        if (isChecking) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "SYNCHRONIZING ENVIRONMENT",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF64748B),
              letterSpacing = 1.5.sp,
              fontSize = 9.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            MinimalProgressBar()
          }
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
        }
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
  }
}
