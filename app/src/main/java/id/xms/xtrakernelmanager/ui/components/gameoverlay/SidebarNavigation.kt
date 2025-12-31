package id.xms.xtrakernelmanager.ui.components.gameoverlay

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Sidebar Tab Types */
enum class SidebarTab {
  PERFORMANCE,
  GAME_TOOLS,
}

/** Expanded Overlay Panel */
@Composable
fun ExpandedSidebarLayout(
    isExpanded: Boolean,
    currentTab: SidebarTab,
    onTabSelected: (SidebarTab) -> Unit,
    onClose: () -> Unit,
    mainContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
  AnimatedVisibility(
      visible = isExpanded,
      enter = fadeIn(tween(200)) + expandHorizontally(expandFrom = Alignment.End),
      exit = fadeOut(tween(200)) + shrinkHorizontally(shrinkTowards = Alignment.End),
      modifier = modifier,
  ) {
    Box(
        modifier =
            Modifier.width(280.dp)
                .heightIn(max = 450.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0D0D0D).copy(alpha = 0.95f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Header with tabs
        OverlayHeader(
            currentTab = currentTab,
            onTabSelected = onTabSelected,
            onClose = onClose,
            accentColor = accentColor,
        )

        // Content area
        Box(
            modifier =
                Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          mainContent()
        }
      }
    }
  }
}

/** Overlay Header with Tab Bar */
@Composable
private fun OverlayHeader(
    currentTab: SidebarTab,
    onTabSelected: (SidebarTab) -> Unit,
    onClose: () -> Unit,
    accentColor: Color,
) {
  Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A))) {
    // Title row
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
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
            color = Color.White,
        )
      }

      Box(
          modifier =
              Modifier.size(26.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.1f))
                  .clickable { onClose() },
          contentAlignment = Alignment.Center,
      ) {
        Icon(
            Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.White,
            modifier = Modifier.size(14.dp),
        )
      }
    }

    // Tab bar
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      TabButton(
          icon = Icons.Outlined.Speed,
          selectedIcon = Icons.Filled.Speed,
          label = "Kinerja",
          isSelected = currentTab == SidebarTab.PERFORMANCE,
          accentColor = accentColor,
          onClick = { onTabSelected(SidebarTab.PERFORMANCE) },
          modifier = Modifier.weight(1f),
      )

      TabButton(
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

/** Tab Button */
@Composable
private fun TabButton(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val backgroundColor by
      animateColorAsState(
          targetValue = if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent,
          animationSpec = tween(200),
          label = "tab_bg",
      )

  Column(
      modifier =
          modifier
              .clip(RoundedCornerShape(8.dp))
              .background(backgroundColor)
              .clickable { onClick() }
              .padding(vertical = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Icon(
        imageVector = if (isSelected) selectedIcon else icon,
        contentDescription = label,
        tint = if (isSelected) accentColor else Color.Gray,
        modifier = Modifier.size(18.dp),
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
        text = label,
        fontSize = 9.sp,
        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
        color = if (isSelected) accentColor else Color.Gray,
    )
  }
}
