package id.xms.xtrakernelmanager.ui.components.gameoverlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R

/**
 * Liquid Game Tools Panel - Glassmorphism Light Mode
 */
@Composable
fun LiquidGameToolsPanel(
    toolState: GameToolState,
    onEsportsModeChange: (Boolean) -> Unit,
    onTouchGuardChange: (Boolean) -> Unit,
    onBlockNotificationsChange: (Boolean) -> Unit,
    onDndChange: (Boolean) -> Unit,
    onAutoRejectCallsChange: (Boolean) -> Unit,
    onLockBrightnessChange: (Boolean) -> Unit,
    onScreenshot: () -> Unit,
    onScreenRecord: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF007AFF), // iOS Blue
) {
  val scrollState = rememberScrollState()

  Column(
      modifier = modifier.fillMaxSize().verticalScroll(scrollState),
      verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // Esports Mode - Highlighted
    LiquidEsportsModeCard(
        isEnabled = toolState.esportsMode,
        onToggle = onEsportsModeChange,
        accentColor = accentColor,
    )

    // Toggle Tools Grid
    Text(
        text = stringResource(R.string.game_tools_title),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF64748B),
        modifier = Modifier.padding(top = 4.dp),
    )

    // Row 1
    Row(
        modifier = Modifier.fillMaxWidth(), 
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      LiquidGameToolToggleCard(
          icon = Icons.Outlined.TouchApp,
          selectedIcon = Icons.Filled.TouchApp,
          label = stringResource(R.string.game_tools_mistouch),
          isEnabled = toolState.touchGuard,
          onToggle = onTouchGuardChange,
          accentColor = accentColor,
          modifier = Modifier.weight(1f),
      )

      LiquidGameToolToggleCard(
          icon = Icons.Outlined.NotificationsOff,
          selectedIcon = Icons.Filled.NotificationsOff,
          label = stringResource(R.string.game_tools_block_notif),
          isEnabled = toolState.blockNotifications,
          onToggle = onBlockNotificationsChange,
          accentColor = accentColor,
          modifier = Modifier.weight(1f),
      )
    }

    // Row 2
    Row(
        modifier = Modifier.fillMaxWidth(), 
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      LiquidGameToolToggleCard(
          icon = Icons.Outlined.DoNotDisturbOn,
          selectedIcon = Icons.Filled.DoNotDisturbOn,
          label = stringResource(R.string.game_tools_dnd),
          isEnabled = toolState.doNotDisturb,
          onToggle = onDndChange,
          accentColor = accentColor,
          modifier = Modifier.weight(1f),
      )

      LiquidGameToolToggleCard(
          icon = Icons.Outlined.PhoneDisabled,
          selectedIcon = Icons.Filled.PhoneDisabled,
          label = stringResource(R.string.game_tools_reject_calls),
          isEnabled = toolState.autoRejectCalls,
          onToggle = onAutoRejectCallsChange,
          accentColor = accentColor,
          modifier = Modifier.weight(1f),
      )
    }

    // Row 3
    Row(
        modifier = Modifier.fillMaxWidth(), 
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      LiquidGameToolToggleCard(
          icon = Icons.Outlined.LightMode,
          selectedIcon = Icons.Filled.LightMode,
          label = stringResource(R.string.game_tools_lock_brightness),
          isEnabled = toolState.lockBrightness,
          onToggle = onLockBrightnessChange,
          accentColor = accentColor,
          modifier = Modifier.weight(1f),
      )

      Spacer(modifier = Modifier.weight(1f))
    }

    // Divider
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.4f),
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 4.dp),
    )

    // Action Buttons
    Text(
        text = stringResource(R.string.game_tools_action),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF64748B),
    )

    Row(
        modifier = Modifier.fillMaxWidth(), 
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      LiquidGameToolActionButton(
          icon = Icons.Outlined.Screenshot,
          label = stringResource(R.string.game_tools_screenshot),
          onClick = onScreenshot,
          accentColor = accentColor,
          modifier = Modifier.weight(1f),
      )

      LiquidGameToolActionButton(
          icon = Icons.Outlined.Videocam,
          label = stringResource(R.string.game_tools_screen_record),
          onClick = onScreenRecord,
          accentColor = accentColor,
          modifier = Modifier.weight(1f),
      )
    }
  }
}

/** Liquid Esports Mode Card */
@Composable
private fun LiquidEsportsModeCard(
    isEnabled: Boolean, 
    onToggle: (Boolean) -> Unit, 
    accentColor: Color
) {
  val backgroundColor = if (isEnabled) {
      Color(0xFFEF4444).copy(alpha = 0.2f)
  } else {
      Color.White.copy(alpha = 0.3f)
  }

  val borderColor = if (isEnabled) {
      Color(0xFFEF4444).copy(alpha = 0.6f)
  } else {
      Color.White.copy(alpha = 0.5f)
  }

  GlassmorphicCardLight(
      modifier = Modifier.fillMaxWidth(),
      cornerRadius = 16.dp,
      backgroundColor = backgroundColor,
      borderColor = borderColor,
      borderWidth = if (isEnabled) 2.dp else 1.5.dp,
  ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isEnabled) }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
            imageVector = if (isEnabled) Icons.Filled.Bolt else Icons.Outlined.Bolt,
            contentDescription = null,
            tint = if (isEnabled) Color(0xFFEF4444) else Color(0xFF64748B),
            modifier = Modifier.size(28.dp),
        )
        Column {
          Text(
              text = stringResource(R.string.esports_mode),
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = if (isEnabled) Color(0xFFDC2626) else Color(0xFF1E293B),
          )
          Text(
              text = stringResource(R.string.esports_mode_desc),
              fontSize = 10.sp,
              color = Color(0xFF64748B),
          )
        }
      }

      EsportsAnimatedSwitch(
          checked = isEnabled,
          onCheckedChange = onToggle,
          activeColor = Color(0xFFEF4444),
      )
    }
  }
}

/** Liquid Game Tool Toggle Card */
@Composable
private fun LiquidGameToolToggleCard(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
  val backgroundColor = if (isEnabled) {
      accentColor.copy(alpha = 0.15f)
  } else {
      Color.White.copy(alpha = 0.3f)
  }

  val borderColor = if (isEnabled) {
      accentColor.copy(alpha = 0.5f)
  } else {
      Color.White.copy(alpha = 0.5f)
  }

  GlassmorphicCardLight(
      modifier = modifier,
      cornerRadius = 14.dp,
      backgroundColor = backgroundColor,
      borderColor = borderColor,
      borderWidth = if (isEnabled) 2.dp else 1.5.dp,
  ) {
    Column(
        modifier = Modifier
            .clickable { onToggle(!isEnabled) }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
      Icon(
          imageVector = if (isEnabled) selectedIcon else icon,
          contentDescription = label,
          tint = if (isEnabled) accentColor else Color(0xFF64748B),
          modifier = Modifier.size(24.dp),
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
          text = label,
          fontSize = 9.sp,
          fontWeight = if (isEnabled) FontWeight.SemiBold else FontWeight.Normal,
          color = if (isEnabled) accentColor else Color(0xFF475569),
          lineHeight = 11.sp,
          maxLines = 2,
      )
    }
  }
}

/** Liquid Game Tool Action Button */
@Composable
private fun LiquidGameToolActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
  GlassmorphicCardLight(
      modifier = modifier,
      cornerRadius = 14.dp,
      backgroundColor = Color.White.copy(alpha = 0.35f),
      borderColor = Color.White.copy(alpha = 0.6f),
  ) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
          imageVector = icon,
          contentDescription = label,
          tint = accentColor,
          modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
          text = label, 
          fontSize = 11.sp, 
          fontWeight = FontWeight.Medium, 
          color = Color(0xFF1E293B)
      )
    }
  }
}
