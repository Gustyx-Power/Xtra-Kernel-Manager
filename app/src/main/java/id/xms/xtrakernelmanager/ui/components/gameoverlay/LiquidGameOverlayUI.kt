package id.xms.xtrakernelmanager.ui.components.gameoverlay

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.xms.xtrakernelmanager.ui.screens.misc.components.GameMonitorViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

/**
 * Liquid Game Overlay Theme - Light Mode Glassmorphism
 * Using iOS-style blue as primary color
 */
@Composable
fun LiquidGameOverlayTheme(content: @Composable () -> Unit) {
  val colorScheme = lightColorScheme(
      primary = Color(0xFF007AFF), // iOS Blue
      secondary = Color(0xFF8B5CF6), // Purple
      tertiary = Color(0xFFEC4899), // Pink
      surface = Color.White.copy(alpha = 0.3f),
      surfaceContainer = Color.White.copy(alpha = 0.25f),
      surfaceContainerHigh = Color.White.copy(alpha = 0.35f),
      background = Color.Transparent,
  )

  MaterialTheme(colorScheme = colorScheme, typography = Typography(), content = content)
}

/**
 * Liquid Game Sidebar - Glassmorphism Light Mode
 */
@Composable
fun LiquidGameSidebar(
    isExpanded: Boolean,
    overlayOnRight: Boolean,
    isDockedToEdge: Boolean = true,
    fps: String? = null,
    onToggleExpand: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val pillColor = Color.White.copy(alpha = 0.35f)
  val contentColor = Color(0xFF007AFF) // iOS Blue

  val shape = if (isDockedToEdge) {
      if (overlayOnRight) {
        RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
      } else {
        RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
      }
  } else {
      CircleShape
  }

  val width = if (isDockedToEdge) 56.dp else 44.dp
  val height = if (isDockedToEdge) 40.dp else 44.dp

  GlassmorphicCardLight(
      modifier = modifier
          .width(width)
          .height(height)
          .pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = onDragEnd,
            ) { change, dragAmount ->
              change.consume()
              onDrag(dragAmount.x, dragAmount.y)
            }
          }
          .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
          ) {
            onToggleExpand()
          },
      cornerRadius = if (isDockedToEdge) 20.dp else 22.dp,
      backgroundColor = pillColor,
      borderColor = Color.White.copy(alpha = 0.6f),
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      if (fps != null) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
              text = fps,
              color = Color.White,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              maxLines = 1,
          )
        }
      } else {
        Icon(
            imageVector = Icons.Rounded.SportsEsports,
            contentDescription = "Expand",
            tint = contentColor,
            modifier = Modifier.size(22.dp),
        )
      }
    }
  }
}

/**
 * Liquid Game Panel Card - Glassmorphism Light Mode
 */
@Composable
fun LiquidGamePanelCard(
    viewModel: GameMonitorViewModel,
    isFpsEnabled: Boolean,
    onFpsToggle: () -> Unit,
    onCollapse: () -> Unit,
    onMoveSide: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val time by
      produceState(initialValue = "") {
        while (true) {
          value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
          delay(1000)
        }
      }

  val batteryLevel by viewModel.batteryPercentage.collectAsStateWithLifecycle()
  val temp by viewModel.tempValue.collectAsStateWithLifecycle()
  val fps by viewModel.fpsValue.collectAsStateWithLifecycle()
  val cpuLoad by viewModel.cpuLoad.collectAsStateWithLifecycle()
  val gpuLoad by viewModel.gpuLoad.collectAsStateWithLifecycle()

  GlassmorphicCardLightGradient(
      modifier = modifier
          .width(320.dp)
          .wrapContentHeight()
          .padding(8.dp),
      cornerRadius = 32.dp,
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
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Header dengan Tools di kiri
      LiquidGameHeaderWithTools(
          time = time,
          temp = temp,
          batteryLevel = batteryLevel,
          viewModel = viewModel,
          isFpsEnabled = isFpsEnabled,
          onFpsToggle = onFpsToggle,
          onDrag = onDrag,
          onDragEnd = onDragEnd,
          onCollapse = onCollapse,
      )

      // Brightness Control
      LiquidBrightnessControl(viewModel)

      // Performance Bento
      LiquidPerformanceBento(fps, cpuLoad, gpuLoad)

      // Mode Selector
      LiquidModeSelector(viewModel)
    }
  }
}

@Composable
private fun LiquidGameHeaderWithTools(
    time: String,
    temp: String,
    batteryLevel: Int,
    viewModel: GameMonitorViewModel,
    isFpsEnabled: Boolean,
    onFpsToggle: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    onCollapse: () -> Unit,
) {
  Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Row 1: Time + Status Pills (draggable & clickable to collapse)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures(onDragEnd = onDragEnd) { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
              onCollapse()
            },
    ) {
      Text(
          text = time,
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Black,
          color = Color(0xFF1E293B),
          letterSpacing = (-1).sp,
      )

      Spacer(modifier = Modifier.weight(1f))

      LiquidStatusPill(icon = Icons.Rounded.Thermostat, text = "$temp°C", isWarning = true)
      Spacer(modifier = Modifier.width(6.dp))
      LiquidStatusPill(icon = Icons.Rounded.BatteryStd, text = "$batteryLevel%")
    }
    
    // Row 2: Scrollable Tools Icons (icon only, no labels)
    LiquidCompactToolsRow(viewModel, isFpsEnabled, onFpsToggle)
  }
}

@Composable
private fun LiquidCompactToolsRow(
    viewModel: GameMonitorViewModel,
    isFpsEnabled: Boolean,
    onFpsToggle: () -> Unit,
) {
  val dnd by viewModel.doNotDisturb.collectAsStateWithLifecycle()
  val ringerMode by viewModel.ringerMode.collectAsStateWithLifecycle()
  val callMode by viewModel.callMode.collectAsStateWithLifecycle()
  val threeFingerSwipe by viewModel.threeFingerSwipe.collectAsStateWithLifecycle()
  val touchGuard by viewModel.touchGuard.collectAsStateWithLifecycle()

  androidx.compose.foundation.lazy.LazyRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(horizontal = 0.dp),
  ) {
    item {
      LiquidCompactToolButton(
          icon = Icons.Rounded.Speed,
          isActive = isFpsEnabled,
          onClick = { onFpsToggle() }
      )
    }
    item {
        val icon = when(ringerMode) {
             1 -> Icons.Rounded.Vibration
             2 -> Icons.Rounded.NotificationsOff
             else -> Icons.Rounded.Notifications
        }
        LiquidCompactToolButton(
            icon = icon,
            isActive = ringerMode != 0,
            onClick = { viewModel.cycleRingerMode() }
        )
    }
    item {
        val icon = when(callMode) {
             1 -> Icons.Rounded.NotificationsPaused
             2 -> Icons.Rounded.CallEnd
             else -> Icons.Rounded.Call
        }
        LiquidCompactToolButton(
            icon = icon,
            isActive = callMode != 0,
            onClick = { viewModel.cycleCallMode() }
        )
    }
    item {
        LiquidCompactToolButton(
            icon = if (threeFingerSwipe) Icons.Rounded.TouchApp else Icons.Rounded.DoNotDisturb,
            isActive = threeFingerSwipe,
            onClick = { viewModel.toggleThreeFingerSwipe() }
        )
    }
    item {
      LiquidCompactToolButton(
          icon = Icons.Rounded.DoNotDisturb,
          isActive = dnd,
          onClick = { viewModel.setDND(!dnd) }
      )
    }
    item {
        LiquidCompactToolButton(
            icon = if (touchGuard) Icons.Rounded.BackHand else Icons.Rounded.PanTool,
            isActive = touchGuard,
            onClick = { viewModel.setTouchGuard(!touchGuard) }
        )
    }
    item {
      LiquidCompactToolButton(
          icon = Icons.Rounded.RocketLaunch,
          isActive = false,
          onClick = { viewModel.clearRAM() }
      )
    }
    item {
      LiquidCompactToolButton(
          icon = Icons.Rounded.Screenshot,
          isActive = false,
          onClick = { viewModel.takeScreenshot() }
      )
    }
  }
}

@Composable
private fun LiquidCompactToolButton(
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
) {
  val bgColor = if (isActive) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.3f)
  val iconColor = if (isActive) Color(0xFF007AFF) else Color(0xFF64748B)
  val borderColor = if (isActive) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.5f)

  GlassmorphicCardLight(
      modifier = Modifier.size(40.dp), // Compact size
      cornerRadius = 12.dp,
      backgroundColor = bgColor,
      borderColor = borderColor,
      borderWidth = if (isActive) 2.dp else 1.5.dp,
  ) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }
    ) {
      Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
    }
  }
}

@Composable
private fun LiquidStatusPill(icon: ImageVector, text: String, isWarning: Boolean = false) {
  GlassmorphicCardLight(
      modifier = Modifier.height(32.dp),
      cornerRadius = 16.dp,
      backgroundColor = if (isWarning) Color(0xFFFEE2E2).copy(alpha = 0.5f) else Color(0xFFDCFCE7).copy(alpha = 0.5f),
      borderColor = if (isWarning) Color(0xFFFCA5A5).copy(alpha = 0.6f) else Color(0xFF86EFAC).copy(alpha = 0.6f),
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 12.dp),
    ) {
      Icon(
          icon, 
          null, 
          modifier = Modifier.size(14.dp),
          tint = if (isWarning) Color(0xFFDC2626) else Color(0xFF16A34A)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
          text, 
          style = MaterialTheme.typography.labelMedium, 
          fontWeight = FontWeight.Bold,
          color = if (isWarning) Color(0xFFDC2626) else Color(0xFF16A34A),
          fontSize = 13.sp
      )
    }
  }
}

@Composable
private fun LiquidPerformanceBento(fps: String, cpu: Float, gpu: Float) {
  Row(
      modifier = Modifier.fillMaxWidth().height(90.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    // FPS Card - iOS Blue dengan background hitam untuk angka
    GlassmorphicCardLightGradient(
        modifier = Modifier.weight(1.3f).fillMaxHeight(),
        cornerRadius = 24.dp,
        backgroundColor = Color(0xFF007AFF).copy(alpha = 0.15f),
        borderGradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF007AFF).copy(alpha = 0.5f),
                Color(0xFF0051D5).copy(alpha = 0.5f)
            )
        ),
    ) {
      Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          val fpsVal = fps.toFloatOrNull()?.toInt() ?: 60
          // FPS number dengan background hitam semi-transparan
          Box(
              modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.Black.copy(alpha = 0.3f))
                  .padding(horizontal = 12.dp, vertical = 4.dp)
          ) {
            Text(
                text = "$fpsVal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                lineHeight = 32.sp,
                fontSize = 36.sp
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
              text = "FPS",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF007AFF).copy(alpha = 0.7f),
              fontSize = 12.sp
          )
        }
      }
    }

    // Load Stats
    Column(
        modifier = Modifier.weight(1f).fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      LiquidLoadChip(
          label = "CPU",
          value = cpu,
          color = Color(0xFFEC4899),
          modifier = Modifier.weight(1f),
      )
      LiquidLoadChip(
          label = "GPU",
          value = gpu,
          color = Color(0xFF8B5CF6),
          modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun LiquidLoadChip(label: String, value: Float, color: Color, modifier: Modifier = Modifier) {
  GlassmorphicCardLight(
      modifier = modifier.fillMaxWidth(),
      cornerRadius = 16.dp,
      backgroundColor = Color.White.copy(alpha = 0.4f),
      borderColor = Color.White.copy(alpha = 0.6f),
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
          label, 
          style = MaterialTheme.typography.labelMedium, 
          color = Color(0xFF64748B),
          fontWeight = FontWeight.SemiBold,
          fontSize = 12.sp
      )
      Text(
          "${value.toInt()}%",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = color,
          fontSize = 16.sp
      )
    }
  }
}

@Composable
private fun LiquidModeSelector(viewModel: GameMonitorViewModel) {
  val currentMode by viewModel.currentPerformanceMode.collectAsStateWithLifecycle()
  val modes = listOf(
      "powersave" to "Power Save",
      "balanced" to "Balance",
      "performance" to "Performance"
  )

  GlassmorphicCardLight(
      modifier = Modifier.fillMaxWidth().height(50.dp),
      cornerRadius = 25.dp,
      backgroundColor = Color.White.copy(alpha = 0.35f),
      borderColor = Color.White.copy(alpha = 0.6f),
  ) {
    Row(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      modes.forEach { (modeKey, modeLabel) ->
        val isSelected = currentMode == modeKey
        val animatedColor by animateColorAsState(
            if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Transparent
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(21.dp))
                .background(animatedColor)
                .clickable { viewModel.setPerformanceMode(modeKey) },
            contentAlignment = Alignment.Center,
        ) {
          Text(
              text = modeLabel,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color(0xFF1E293B) else Color(0xFF64748B),
              maxLines = 1,
              fontSize = 12.sp
          )
        }
      }
    }
  }
}

@Composable
private fun LiquidBrightnessControl(viewModel: GameMonitorViewModel) {
  val vmBrightness by viewModel.brightness.collectAsStateWithLifecycle()
  var localSliderValue by remember { mutableFloatStateOf(0f) }
  var isDragging by remember { mutableStateOf(false) }
  val displayValue = if (isDragging) localSliderValue else vmBrightness

  GlassmorphicCardLight(
      modifier = Modifier.fillMaxWidth().height(46.dp),
      cornerRadius = 23.dp,
      backgroundColor = Color.White.copy(alpha = 0.35f),
      borderColor = Color.White.copy(alpha = 0.6f),
  ) {
    Box(contentAlignment = Alignment.CenterStart) {
      // Track Fill
      Box(
          modifier = Modifier
              .fillMaxWidth(displayValue.coerceIn(0.01f, 1f))
              .fillMaxHeight()
              .background(
                  Brush.horizontalGradient(
                      colors = listOf(
                          Color(0xFFFBBF24).copy(alpha = 0.4f),
                          Color(0xFFF59E0B).copy(alpha = 0.5f)
                      )
                  ),
                  RoundedCornerShape(23.dp)
              )
      )

      // Slider
      Slider(
          value = displayValue,
          onValueChange = { 
              isDragging = true
              localSliderValue = it
          },
          onValueChangeFinished = {
              viewModel.setBrightness(localSliderValue) 
              isDragging = false 
          },
          colors = SliderDefaults.colors(
              thumbColor = Color.Transparent,
              activeTrackColor = Color.Transparent,
              inactiveTrackColor = Color.Transparent,
          ),
          modifier = Modifier.fillMaxWidth(),
      )

      Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
            Icons.Rounded.BrightnessLow,
            null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(18.dp),
        )
        Icon(
            Icons.Rounded.BrightnessHigh,
            null,
            tint = Color(0xFF1E293B),
            modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}
