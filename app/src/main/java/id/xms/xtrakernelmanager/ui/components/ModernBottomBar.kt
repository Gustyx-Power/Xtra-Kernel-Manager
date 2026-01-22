package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.ui.theme.rememberResponsiveDimens

data class BottomNavItem(val route: String, val icon: ImageVector, val label: Int)

@Composable
fun ModernBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    items: List<BottomNavItem>,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier,
) {
  val dimens = rememberResponsiveDimens()

  AnimatedVisibility(
      visible = isVisible,
      enter = slideInVertically { it },
      exit = slideOutVertically { it },
      modifier = modifier,
  ) {
    Surface(
        modifier =
            Modifier.widthIn(max = 360.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                ),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
    ) {
      Row(
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp).fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        items.forEach { item ->
          val selected = currentRoute == item.route
          DockNavItem(
              item = item,
              isSelected = selected,
              onClick = { onNavigate(item.route) },
              modifier = Modifier.weight(1f),
          )
        }
      }
    }
  }
}

@Composable
private fun DockNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  val interactionSource = remember { MutableInteractionSource() }

  val animationSpec =
      spring<Color>(
          dampingRatio = Spring.DampingRatioNoBouncy,
          stiffness = Spring.StiffnessMediumLow,
      )

  val iconBoxColor by
      animateColorAsState(
          targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
          animationSpec = animationSpec,
          label = "iconBoxBg",
      )

  val iconColor by
      animateColorAsState(
          targetValue =
              if (isSelected) MaterialTheme.colorScheme.onPrimary
              else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
          animationSpec = animationSpec,
          label = "iconTint",
      )

  val textColor by
      animateColorAsState(
          targetValue =
              if (isSelected) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
          animationSpec = animationSpec,
          label = "textTint",
      )

  val scale by
      animateFloatAsState(
          targetValue = if (isSelected) 1f else 0.95f,
          animationSpec = spring(dampingRatio = 0.6f),
          label = "scale",
      )

  Column(
      modifier =
          modifier
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
              .scale(scale),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    // Icon Box
    Box(
        modifier =
            Modifier.width(56.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(50))
                .background(iconBoxColor),
        contentAlignment = Alignment.Center,
    ) {
      Icon(
          imageVector = item.icon,
          contentDescription = stringResource(item.label),
          tint = iconColor,
          modifier = Modifier.size(20.dp),
      )
    }

    // Label
    Text(
        text = stringResource(item.label),
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
        color = textColor,
        maxLines = 1,
        letterSpacing = 0.5.sp,
    )
  }
}
