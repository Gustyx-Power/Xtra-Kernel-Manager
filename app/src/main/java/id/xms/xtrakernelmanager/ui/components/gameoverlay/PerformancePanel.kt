package id.xms.xtrakernelmanager.ui.components.gameoverlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

/** Performance Panel - Game Control Overlay */
@Composable
fun PerformancePanel(
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
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
  Row(
      modifier =
          modifier
              .widthIn(max = 500.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFF0D1117))
              .border(1.dp, Color(0xFF30363D), RoundedCornerShape(16.dp))
  ) {
    // LEFT SIDEBAR - wrap content height
    Column(
        modifier = Modifier.width(80.dp).background(Color(0xFF161B22)).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      SideBtn(
          icon = Icons.Outlined.Speed,
          text = stringResource(R.string.game_tools_mode_monster),
          active = esportsMode,
          color = Color(0xFFFF6B6B),
          onClick = { onEsportsModeChange(!esportsMode) },
      )
      SideBtn(
          icon = Icons.Default.TouchApp,
          text = stringResource(R.string.game_tools_mistouch),
          active = touchGuard,
          color = Color(0xFF4ECDC4),
          onClick = { onTouchGuardChange(!touchGuard) },
      )
      SideBtn(
          icon = Icons.Default.CleaningServices,
          text =
              if (isClearingRam) stringResource(R.string.game_tools_clearing)
              else stringResource(R.string.game_tools_clear_ram),
          active = false,
          color = Color(0xFF4CAF50),
          onClick = { if (!isClearingRam) onClearRam() },
      )
    }

    // RIGHT CONTENT
    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(12.dp)) {
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
              tint = Color(0xFF58A6FF),
              modifier = Modifier.size(20.dp),
          )
          Spacer(Modifier.width(8.dp))
          Text(
              stringResource(R.string.game_control_title),
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Toggle position button
          IconButton(onClick = onTogglePosition, modifier = Modifier.size(28.dp)) {
            Icon(
                if (isOverlayOnRight) Icons.Default.KeyboardArrowLeft
                else Icons.Default.KeyboardArrowRight,
                contentDescription = "Toggle position",
                tint = Color(0xFF58A6FF),
                modifier = Modifier.size(18.dp),
            )
          }
          IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Default.Close,
                "Close",
                tint = Color(0xFF8B949E),
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
      Row(
          modifier =
              Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFF21262D))
                  .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        StatText("$fps FPS", Color(0xFF00E5FF))
        StatText("$temperature°C", Color(0xFFFF7B72))
        StatText(gameDuration, Color(0xFFD2A8FF))
        StatText("$batteryPercentage%", Color(0xFFFFA657))
      }

      Spacer(Modifier.height(12.dp))

      // Action Buttons (tap-only, no Switch)
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionButton(
            Icons.Outlined.NotificationsOff,
            stringResource(R.string.game_tools_block_notif).replace("\n", " "),
            blockNotifications,
        ) {
          onBlockNotificationsChange(!blockNotifications)
        }
        ActionButton(
            Icons.Default.DoNotDisturb,
            stringResource(R.string.game_tools_dnd).replace("\n", " "),
            doNotDisturb,
        ) {
          onDndChange(!doNotDisturb)
        }
        ActionButton(
            Icons.Outlined.CameraAlt,
            stringResource(R.string.game_tools_screenshot),
            false,
        ) {
          onScreenshot()
        }
      }
    }
  }
}

@Composable
private fun SideBtn(
    icon: ImageVector,
    text: String,
    active: Boolean,
    color: Color,
    onClick: () -> Unit,
) {
  val bg = if (active) color.copy(alpha = 0.2f) else Color(0xFF21262D)
  val border = if (active) color.copy(alpha = 0.5f) else Color.Transparent
  val tint = if (active) color else Color(0xFF8B949E)

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(bg)
              .border(1.dp, border, RoundedCornerShape(10.dp))
              .clickable { onClick() }
              .padding(vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
    Spacer(Modifier.height(4.dp))
    Text(text, fontSize = 9.sp, color = tint, textAlign = TextAlign.Center, lineHeight = 11.sp)
  }
}

@Composable
private fun FpsBox(fps: String) {
  Box(
      modifier =
          Modifier.size(70.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF161B22))
              .border(
                  2.dp,
                  Brush.linearGradient(listOf(Color(0xFF00D4FF), Color(0xFF0099CC))),
                  RoundedCornerShape(10.dp),
              ),
      contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
          fps,
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFFFB347),
          fontFamily = FontFamily.Monospace,
      )
      Text("FPS", fontSize = 10.sp, color = Color(0xFF58A6FF))
    }
  }
}

@Composable
private fun StatText(value: String, color: Color) {
  Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = color)
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .height(44.dp)
              .clip(RoundedCornerShape(22.dp))
              .background(if (active) Color(0xFF1F6FEB).copy(alpha = 0.2f) else Color(0xFF21262D))
              .border(
                  1.dp,
                  if (active) Color(0xFF58A6FF).copy(0.4f) else Color(0xFF30363D),
                  RoundedCornerShape(22.dp),
              )
              .clickable { onClick() }
              .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
          icon,
          null,
          tint = if (active) Color(0xFF58A6FF) else Color(0xFF8B949E),
          modifier = Modifier.size(20.dp),
      )
      Spacer(Modifier.width(12.dp))
      Text(
          label,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = if (active) Color.White else Color(0xFFC9D1D9),
      )
    }

    // Status indicator dot instead of Switch
    Box(
        modifier =
            Modifier.size(12.dp)
                .clip(CircleShape)
                .background(if (active) Color(0xFF4CAF50) else Color(0xFF6B7280))
    )
  }
}
