package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class BottomNavItem(val route: String, val icon: ImageVector, val label: Int)

@Composable
fun ModernBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    items: List<BottomNavItem>,
    isVisible: Boolean = true,
) {
  AnimatedVisibility(
      visible = isVisible,
      enter = slideInVertically { it },
      exit = slideOutVertically { it },
  ) {
    Surface(
        modifier =
            Modifier.fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 8.dp,
    ) {
      Row(
          modifier =
              Modifier.fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 12.dp)
                  .navigationBarsPadding()
                  .height(56.dp), // Tinggi standar modern
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        items.forEach { item ->
          val selected = currentRoute == item.route

          PillNavItem(item = item, isSelected = selected, onClick = { onNavigate(item.route) })
        }
      }
    }
  }
}

@Composable
private fun PillNavItem(item: BottomNavItem, isSelected: Boolean, onClick: () -> Unit) {
  val haptic = LocalHapticFeedback.current
  val interactionSource = remember { MutableInteractionSource() }
  val animationSpec =
      spring<Color>(
          dampingRatio = Spring.DampingRatioNoBouncy,
          stiffness = Spring.StiffnessMediumLow,
      )

  val backgroundColor by
      animateColorAsState(
          targetValue =
              if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
          animationSpec = animationSpec,
          label = "bg",
      )

  val contentColor by
      animateColorAsState(
          targetValue =
              if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
              else MaterialTheme.colorScheme.onSurfaceVariant,
          animationSpec = animationSpec,
          label = "content",
      )

  Box(
      modifier =
          Modifier.clip(CircleShape)
              .background(backgroundColor)
              .clickable(
                  interactionSource = interactionSource,
                  indication = null,
                  onClick = {
                    haptic.performHapticFeedback(
                        androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove
                    )
                    onClick()
                  },
              )
              .animateContentSize(
                  animationSpec =
                      spring(
                          dampingRatio = Spring.DampingRatioNoBouncy,
                          stiffness = Spring.StiffnessMediumLow,
                      )
              )
              .padding(horizontal = 16.dp, vertical = 10.dp),
      contentAlignment = Alignment.Center,
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Icon(
          imageVector = item.icon,
          contentDescription = stringResource(item.label),
          tint = contentColor,
          modifier = Modifier.size(22.dp),
      )

      // Teks Label
      if (isSelected) {
        Text(
            text = stringResource(item.label),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}
