package id.xms.xtrakernelmanager.ui.components.gameoverlay

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Liquid Expanded Sidebar Layout - Glassmorphism Light Mode
 */
@Composable
fun LiquidExpandedSidebarLayout(
    isExpanded: Boolean,
    currentTab: SidebarTab,
    onTabSelected: (SidebarTab) -> Unit,
    onClose: () -> Unit,
    mainContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF007AFF), // iOS Blue
) {
  AnimatedVisibility(
      visible = isExpanded,
      enter = fadeIn(tween(200)) + expandHorizontally(expandFrom = Alignment.End),
      exit = fadeOut(tween(200)) + shrinkHorizontally(shrinkTowards = Alignment.End),
      modifier = modifier,
  ) {
    GlassmorphicCardLightGradient(
        modifier = Modifier
            .width(280.dp)
            .heightIn(max = 450.dp),
        cornerRadius = 24.dp,
        backgroundColor = Color.White.copy(alpha = 0.35f),
        borderGradient = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.7f),
                Color.White.copy(alpha = 0.4f),
                Color.White.copy(alpha = 0.7f)
            )
        ),
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Header with tabs
        LiquidOverlayHeader(
            currentTab = currentTab,
            onTabSelected = onTabSelected,
            onClose = onClose,
            accentColor = accentColor,
        )

        // Content area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          mainContent()
        }
      }
    }
  }
}

/** Liquid Overlay Header with Tab Bar */
@Composable
private fun LiquidOverlayHeader(
    currentTab: SidebarTab,
    onTabSelected: (SidebarTab) -> Unit,
    onClose: () -> Unit,
    accentColor: Color,
) {
  Column(
      modifier = Modifier
          .fillMaxWidth()
          .background(Color.White.copy(alpha = 0.2f))
  ) {
    // Title row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
            Icons.Default.SportsEsports,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = "Game Control",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
        )
      }

      GlassmorphicCardLight(
          modifier = Modifier.size(26.dp),
          cornerRadius = 13.dp,
          backgroundColor = Color.White.copy(alpha = 0.4f),
          borderColor = Color.White.copy(alpha = 0.6f),
      ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClose() },
            contentAlignment = Alignment.Center,
        ) {
          Icon(
              Icons.Default.Close,
              contentDescription = "Close",
              tint = Color(0xFF64748B),
              modifier = Modifier.size(14.dp),
          )
        }
      }
    }

    // Tab bar
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      LiquidTabButton(
          icon = Icons.Outlined.Speed,
          selectedIcon = Icons.Filled.Speed,
          label = "Kinerja",
          isSelected = currentTab == SidebarTab.PERFORMANCE,
          accentColor = accentColor,
          onClick = { onTabSelected(SidebarTab.PERFORMANCE) },
          modifier = Modifier.weight(1f),
      )

      LiquidTabButton(
          icon = Icons.Outlined.Build,
          selectedIcon = Icons.Filled.Build,
          label = "Alat",
          isSelected = currentTab == SidebarTab.GAME_TOOLS,
          accentColor = accentColor,
          onClick = { onTabSelected(SidebarTab.GAME_TOOLS) },
          modifier = Modifier.weight(1f),
      )
    }
  }
}

/** Liquid Tab Button */
@Composable
private fun LiquidTabButton(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val backgroundColor by animateColorAsState(
      targetValue = if (isSelected) Color.White.copy(alpha = 0.5f) else Color.Transparent,
      animationSpec = tween(200),
      label = "tab_bg",
  )

  val borderColor by animateColorAsState(
      targetValue = if (isSelected) Color.White.copy(alpha = 0.7f) else Color.Transparent,
      animationSpec = tween(200),
      label = "tab_border",
  )

  GlassmorphicCardLight(
      modifier = modifier,
      cornerRadius = 12.dp,
      backgroundColor = backgroundColor,
      borderColor = borderColor,
  ) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
          imageVector = if (isSelected) selectedIcon else icon,
          contentDescription = label,
          tint = if (isSelected) accentColor else Color(0xFF64748B),
          modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
          text = label,
          fontSize = 9.sp,
          fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
          color = if (isSelected) accentColor else Color(0xFF64748B),
      )
    }
  }
}
