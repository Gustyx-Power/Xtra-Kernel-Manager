package id.xms.xtrakernelmanager.ui.components.gameoverlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R

/**
 * Liquid Performance Panel - Glassmorphism Light Mode
 */
@Composable
fun LiquidPerformancePanel(
    cpuFreq: String,
    cpuLoad: Float,
    gpuFreq: String,
    gpuLoad: Float,
    fps: String,
    temperature: String,
    gameDuration: String,
    batteryPercentage: Int,
    currentPerformanceMode: String,
    esportsMode: Boolean,
    touchGuard: Boolean,
    blockNotifications: Boolean,
    doNotDisturb: Boolean,
    isClearingRam: Boolean,
    onPerformanceModeChange: (String) -> Unit,
    onEsportsModeChange: (Boolean) -> Unit,
    onTouchGuardChange: (Boolean) -> Unit,
    onBlockNotificationsChange: (Boolean) -> Unit,
    onDndChange: (Boolean) -> Unit,
    onClearRam: () -> Unit,
    onScreenshot: () -> Unit,
    onToolsClick: () -> Unit,
    onClose: () -> Unit,
    onTogglePosition: () -> Unit = {},
    isOverlayOnRight: Boolean = true,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF007AFF), // iOS Blue
) {
  Row(
      modifier = modifier
          .widthIn(max = 500.dp)
          .clip(RoundedCornerShape(24.dp))
  ) {
    // LEFT SIDEBAR
    GlassmorphicCardLight(
        modifier = Modifier.width(80.dp),
        cornerRadius = 24.dp,
        backgroundColor = Color.White.copy(alpha = 0.35f),
        borderColor = Color.White.copy(alpha = 0.6f),
    ) {
      Column(
          modifier = Modifier.padding(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        LiquidSideBtn(
            icon = Icons.Outlined.Speed,
            text = stringResource(R.string.game_tools_mode_monster),
            active = esportsMode,
            color = Color(0xFFEF4444),
            onClick = { onEsportsModeChange(!esportsMode) },
        )
        LiquidSideBtn(
            icon = Icons.Default.TouchApp,
            text = stringResource(R.string.game_tools_mistouch),
            active = touchGuard,
            color = Color(0xFF06B6D4),
            onClick = { onTouchGuardChange(!touchGuard) },
        )
        LiquidSideBtn(
            icon = Icons.Default.CleaningServices,
            text = if (isClearingRam) stringResource(R.string.game_tools_clearing)
                   else stringResource(R.string.game_tools_boost),
            active = false,
            color = Color(0xFF10B981),
            onClick = { if (!isClearingRam) onClearRam() },
        )
      }
    }

    Spacer(modifier = Modifier.width(8.dp))

    // RIGHT CONTENT
    GlassmorphicCardLightGradient(
        modifier = Modifier.weight(1f),
        cornerRadius = 24.dp,
        backgroundColor = Color.White.copy(alpha = 0.3f),
        borderGradient = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.7f),
                Color.White.copy(alpha = 0.4f),
                Color.White.copy(alpha = 0.7f)
            )
        ),
    ) {
      Column(
          modifier = Modifier
              .verticalScroll(rememberScrollState())
              .padding(12.dp)
      ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.SportsEsports,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.game_control_title),
                color = Color(0xFF1E293B),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onTogglePosition, modifier = Modifier.size(28.dp)) {
              Icon(
                  if (isOverlayOnRight) Icons.Default.KeyboardArrowLeft
                  else Icons.Default.KeyboardArrowRight,
                  contentDescription = "Toggle position",
                  tint = accentColor,
                  modifier = Modifier.size(18.dp),
              )
            }
            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
              Icon(
                  Icons.Default.Close,
                  "Close",
                  tint = Color(0xFF64748B),
                  modifier = Modifier.size(16.dp),
              )
            }
          }
        }

        Spacer(Modifier.height(12.dp))

        // CPU & GPU Gauges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          HardwareGauge(HardwareGaugeType.CPU, cpuLoad, cpuFreq)
          HardwareGauge(HardwareGaugeType.GPU, gpuLoad, gpuFreq)
        }

        Spacer(Modifier.height(12.dp))

        // Stats Bar
        GlassmorphicCardLight(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            backgroundColor = Color.White.copy(alpha = 0.4f),
            borderColor = Color.White.copy(alpha = 0.6f),
        ) {
          Row(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            LiquidStatText("$fps FPS", Color(0xFF007AFF))
            LiquidStatText("$temperature°C", Color(0xFFEF4444))
            LiquidStatText(gameDuration, Color(0xFF8B5CF6))
            LiquidStatText("$batteryPercentage%", Color(0xFFF59E0B))
          }
        }

        Spacer(Modifier.height(12.dp))

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          LiquidActionButton(
              Icons.Outlined.NotificationsOff,
              stringResource(R.string.game_tools_block_notif).replace("\n", " "),
              blockNotifications,
              accentColor,
          ) {
            onBlockNotificationsChange(!blockNotifications)
          }
          LiquidActionButton(
              Icons.Default.DoNotDisturb,
              stringResource(R.string.game_tools_dnd).replace("\n", " "),
              doNotDisturb,
              accentColor,
          ) {
            onDndChange(!doNotDisturb)
          }
          LiquidActionButton(
              Icons.Outlined.CameraAlt,
              stringResource(R.string.game_tools_screenshot),
              false,
              accentColor,
          ) {
            onScreenshot()
          }
        }
      }
    }
  }
}

@Composable
private fun LiquidSideBtn(
    icon: ImageVector,
    text: String,
    active: Boolean,
    color: Color,
    onClick: () -> Unit,
) {
  val bg = if (active) color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.3f)
  val border = if (active) color.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.5f)
  val tint = if (active) color else Color(0xFF64748B)

  GlassmorphicCardLight(
      modifier = Modifier.fillMaxWidth(),
      cornerRadius = 14.dp,
      backgroundColor = bg,
      borderColor = border,
  ) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
      Spacer(Modifier.height(4.dp))
      Text(
          text, 
          fontSize = 9.sp, 
          color = tint, 
          textAlign = TextAlign.Center, 
          lineHeight = 11.sp,
          fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
      )
    }
  }
}

@Composable
private fun LiquidStatText(value: String, color: Color) {
  Text(
      value, 
      fontSize = 15.sp, 
      fontWeight = FontWeight.Bold, 
      color = color,
      letterSpacing = 0.sp
  )
}

@Composable
private fun LiquidActionButton(
    icon: ImageVector, 
    label: String, 
    active: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
  GlassmorphicCardLight(
      modifier = Modifier.fillMaxWidth().height(44.dp),
      cornerRadius = 22.dp,
      backgroundColor = if (active) accentColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.35f),
      borderColor = if (active) accentColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.6f),
  ) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            null,
            tint = if (active) accentColor else Color(0xFF64748B),
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (active) Color(0xFF1E293B) else Color(0xFF475569),
        )
      }

      // Status indicator
      Box(
          modifier = Modifier
              .size(12.dp)
              .clip(CircleShape)
              .background(if (active) Color(0xFF10B981) else Color(0xFFCBD5E1))
      )
    }
  }
}
