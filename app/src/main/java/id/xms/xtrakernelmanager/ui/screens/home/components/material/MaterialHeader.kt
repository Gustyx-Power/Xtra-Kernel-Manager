package id.xms.xtrakernelmanager.ui.screens.home.components.material

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MaterialHeader(onSettingsClick: () -> Unit) {
  val view = androidx.compose.ui.platform.LocalView.current
  var isShortTitle by remember { mutableStateOf(false) }
  var clickCount by remember { mutableIntStateOf(0) }

  LaunchedEffect(clickCount) {
    if (clickCount > 0) {
      delay(500)
      clickCount = 0
    }
  }

  Row(
      modifier =
          Modifier.fillMaxWidth().clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
          ) {
            clickCount++
            if (clickCount >= 3) {
              isShortTitle = !isShortTitle
              clickCount = 0
              if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
              } else {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
              }
            }
          },
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    AnimatedContent(
        targetState = isShortTitle,
        transitionSpec = {
          (slideInVertically { height -> height } + fadeIn()).togetherWith(
              slideOutVertically { height -> -height } + fadeOut()
          )
        },
        label = "HeaderTitle",
    ) { short ->
      Text(
          text = if (short) "XKM" else "Xtra Kernel Manager",
          style =
              if (short) MaterialTheme.typography.displayMedium
              else MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onBackground,
      )
    }

    IconButton(
        onClick = onSettingsClick,
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
    ) {
      Icon(
          imageVector = Icons.Rounded.Settings,
          contentDescription = "Settings",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
