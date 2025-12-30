package id.xms.xtrakernelmanager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import id.xms.xtrakernelmanager.R

/**
 * Custom Lottie-based switch component that replaces the standard Material3 Switch.
 *
 * The switch.json animation:
 * - Frames 0-60: OFF to ON animation (forward)
 * - Frames 60-120: ON to OFF animation (backward)
 *
 * @param checked Current toggle state
 * @param onCheckedChange Callback when toggle state changes
 * @param modifier Modifier for the switch
 * @param width Width of the switch container
 * @param height Height of the switch container
 * @param scale Scale factor to zoom into the animation
 * @param enabled Whether the switch is interactive
 */
@Composable
fun LottieSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    width: Dp = 80.dp,
    height: Dp = 40.dp,
    scale: Float = 2.2f,
    enabled: Boolean = true,
) {
  // Load Lottie composition from raw resource
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.switch_anim))

  // Target progress based on checked state
  // Animation frames: 0-60 = OFF->ON, 60-120 = ON->OFF
  // We only use 0-60 frames (0.0f - 0.5f progress) for the toggle
  val targetProgress = if (checked) 0.5f else 0f

  // Animate to target progress
  var currentProgress by remember { mutableFloatStateOf(if (checked) 0.5f else 0f) }

  val animationState =
      animateLottieCompositionAsState(
          composition = composition,
          isPlaying = true,
          speed = if (checked) 1f else -1f,
          iterations = 1,
          restartOnPlay = false,
      )

  // Update progress based on checked state with smooth animation
  LaunchedEffect(checked) { currentProgress = targetProgress }

  Box(
      modifier =
          modifier.width(width).height(height).clipToBounds().clickable(
              enabled = enabled && onCheckedChange != null,
              indication = null,
              interactionSource = remember { MutableInteractionSource() },
          ) {
            onCheckedChange?.invoke(!checked)
          },
      contentAlignment = Alignment.Center,
  ) {
    LottieAnimation(
        composition = composition,
        progress = {
          if (checked) {
            animationState.progress.coerceIn(0f, 0.5f).let {
              if (it < 0.5f) animationState.progress else 0.5f
            }
          } else {
            if (animationState.progress > 0.5f) {
              animationState.progress
            } else {
              0f
            }
          }
        },
        modifier =
            Modifier.size(height * scale).graphicsLayer {
              scaleX = scale
              scaleY = scale
            },
    )
  }
}

/**
 * Simplified LottieSwitch that uses direct progress control for smoother animation. This version
 * provides more predictable behavior.
 */
@Composable
fun LottieSwitchSimple(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    width: Dp = 80.dp,
    height: Dp = 40.dp,
    scale: Float = 2.2f,
    enabled: Boolean = true,
) {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.switch_anim))

  // Animate progress smoothly between states
  val progress by
      animateLottieCompositionAsState(
          composition = composition,
          isPlaying = true,
          speed = if (checked) 1.5f else -1.5f,
          iterations = 1,
          restartOnPlay = false,
          clipSpec = com.airbnb.lottie.compose.LottieClipSpec.Progress(min = 0f, max = 0.5f),
      )

  Box(
      modifier =
          modifier.width(width).height(height).clipToBounds().clickable(
              enabled = enabled && onCheckedChange != null,
              indication = null,
              interactionSource = remember { MutableInteractionSource() },
          ) {
            onCheckedChange?.invoke(!checked)
          },
      contentAlignment = Alignment.Center,
  ) {
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier =
            Modifier.size(height * scale).graphicsLayer {
              scaleX = scale
              scaleY = scale
            },
    )
  }
}

/**
 * Most reliable version - uses frame-based progress control. This provides the smoothest and most
 * predictable toggle animation.
 *
 * @param checked Current toggle state
 * @param onCheckedChange Callback when toggle state changes
 * @param modifier Modifier for the switch
 * @param width Width of the switch container (default 80.dp for horizontal switch)
 * @param height Height of the switch container (default 40.dp)
 * @param scale Scale factor to zoom into the animation (default 2.2f)
 * @param enabled Whether the switch is interactive
 */
@Composable
fun LottieSwitchControlled(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    width: Dp = 80.dp,
    height: Dp = 40.dp,
    scale: Float = 2.2f,
    enabled: Boolean = true,
) {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.switch_anim))

  // Direct progress control - most reliable approach
  // Frame 0 = OFF, Frame 60 = ON (which is 0.5 progress in 120-frame animation)
  val targetProgress = if (checked) 0.5f else 0f

  val animatedProgress by
      androidx.compose.animation.core.animateFloatAsState(
          targetValue = targetProgress,
          animationSpec =
              androidx.compose.animation.core.tween(
                  durationMillis = 400,
                  easing = androidx.compose.animation.core.FastOutSlowInEasing,
              ),
          label = "switch_progress",
      )

  Box(
      modifier =
          modifier.width(width).height(height).clipToBounds().clickable(
              enabled = enabled && onCheckedChange != null,
              indication = null,
              interactionSource = remember { MutableInteractionSource() },
          ) {
            onCheckedChange?.invoke(!checked)
          },
      contentAlignment = Alignment.Center,
  ) {
    LottieAnimation(
        composition = composition,
        progress = { animatedProgress },
        modifier =
            Modifier.size(height * scale).graphicsLayer {
              scaleX = scale
              scaleY = scale
            },
    )
  }
}
