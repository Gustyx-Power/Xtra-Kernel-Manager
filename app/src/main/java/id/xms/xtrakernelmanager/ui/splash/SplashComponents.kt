package id.xms.xtrakernelmanager.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedBackgroundCircles() {
  val infiniteTransition = rememberInfiniteTransition(label = "bg_circles")

  val offset1 by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(8000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "offset1"
  )

  val offset2 by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(11000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "offset2"
  )

  val offset3 by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(9000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "offset3"
  )

  Box(modifier = Modifier.fillMaxSize()) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    Box(
      modifier = Modifier
        .fillMaxSize()
        .blur(80.dp)
        .alpha(0.4f)
    ) {
      Box(
        modifier = Modifier
          .offset(
            x = (screenWidth * 0.1f) + (screenWidth * 0.4f * offset1),
            y = (screenHeight * 0.1f) + (screenHeight * 0.3f * offset2)
          )
          .size(280.dp)
          .background(Color(0xFF06B6D4), CircleShape)
      )

      Box(
        modifier = Modifier
          .offset(
            x = (screenWidth * 0.6f) - (screenWidth * 0.4f * offset2),
            y = (screenHeight * 0.6f) - (screenHeight * 0.3f * offset1)
          )
          .size(300.dp)
          .background(Color(0xFF7C3AED), CircleShape)
      )

      Box(
        modifier = Modifier
          .offset(
            x = (screenWidth * 0.2f) + (screenWidth * 0.5f * offset3),
            y = (screenHeight * 0.4f) + (screenHeight * 0.1f * offset1)
          )
          .size(240.dp)
          .background(Color(0xFF2563EB), CircleShape)
      )
    }
  }
}

@Composable
fun MinimalProgressBar() {
  val infiniteTransition = rememberInfiniteTransition(label = "progress")
  
  val progress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(2000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "progress"
  )

  Box(
    modifier = Modifier
      .fillMaxWidth(0.6f)
      .height(2.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF1E293B))
    )
    
    Box(
      modifier = Modifier
        .fillMaxWidth(progress)
        .fillMaxHeight()
        .background(
          Brush.horizontalGradient(
            listOf(
              Color(0xFF38BDF8).copy(alpha = 0.3f),
              Color(0xFF38BDF8),
              Color(0xFF7DD3FC)
            )
          )
        )
    )
  }
}
