package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BottomNavItem(val route: String, val icon: ImageVector, val label: Int)

@Composable
fun ModernBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    items: List<BottomNavItem>,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
      visible = isVisible,
      enter = slideInVertically(
          initialOffsetY = { it },
          animationSpec = spring(
              dampingRatio = Spring.DampingRatioMediumBouncy,
              stiffness = Spring.StiffnessMedium
          )
      ),
      exit = slideOutVertically(
          targetOffsetY = { it },
          animationSpec = tween(300)
      ),
      modifier = modifier,
  ) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp,
    ) {
      Row(
          modifier = Modifier
              .fillMaxWidth()
              .windowInsetsPadding(WindowInsets.navigationBars)
              .height(64.dp) 
              .padding(horizontal = 8.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        items.forEach { item ->
          val selected = currentRoute == item.route
          AnimatedNavItem(
              item = item,
              isSelected = selected,
              onClick = { onNavigate(item.route) },
              modifier = Modifier.weight(1f)
          )
        }
      }
    }
  }
}

@Composable
private fun AnimatedNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    // Smooth animation specs
    val smoothSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val colorSpring = spring<Color>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // Scale animation - subtle bounce
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.95f,
        animationSpec = smoothSpring,
        label = "scale"
    )

    // Background pill animation
    val pillColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.secondaryContainer
        else 
            Color.Transparent,
        animationSpec = colorSpring,
        label = "pillColor"
    )

    // Icon color animation
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.onSecondaryContainer
        else 
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = colorSpring,
        label = "iconColor"
    )

    // Text color animation
    val textColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.onSecondaryContainer
        else 
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = colorSpring,
        label = "textColor"
    )

    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(pillColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(
                        androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove
                    )
                    onClick()
                }
            )
            .scale(scale)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Icon(
            imageVector = item.icon,
            contentDescription = stringResource(item.label),
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        
        // Spacer between icon and text
        if (isSelected) {
            Spacer(modifier = Modifier.width(6.dp))
            
            // Label - only show when selected
            Text(
                text = stringResource(item.label),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 1,
                fontSize = 11.sp
            )
        }
    }
}
