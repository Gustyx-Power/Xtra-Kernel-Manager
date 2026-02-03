package id.xms.xtrakernelmanager.ui.components.gameoverlay

import android.os.Build
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.xms.xtrakernelmanager.ui.screens.misc.components.GameMonitorViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@Composable
fun GameOverlayTheme(content: @Composable () -> Unit) {
  val context = LocalContext.current
  val colorScheme =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context)
      } else {
        darkColorScheme(
            primary = Color(0xFF5C6BC0),
            secondary = Color(0xFF7986CB),
            surface = Color(0xFF1E1E1E),
            surfaceContainer = Color(0xFF252525),
            background = Color(0xFF121212),
        )
      }

  MaterialTheme(colorScheme = colorScheme, typography = Typography(), content = content)
}

@Composable
fun GameSidebar(
    isExpanded: Boolean,
    overlayOnRight: Boolean,
    isDockedToEdge: Boolean = true, // NEW: Controls shape
    fps: String? = null,
    onToggleExpand: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val pillColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)
  val contentColor = MaterialTheme.colorScheme.primary

  // Shape: Half-rounded when docked to edge, Circle when floating
  val shape = if (isDockedToEdge) {
      if (overlayOnRight) {
        RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
      } else {
        RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)
      }
  } else {
      CircleShape // Full circle when floating in the middle
  }

  // Size: Wider when docked, Square when floating (circle)
  val width = if (isDockedToEdge) 52.dp else 40.dp
  val height = if (isDockedToEdge) 36.dp else 40.dp

  Surface(
      color = pillColor,
      shape = shape,
      tonalElevation = 2.dp,
      shadowElevation = 4.dp,
      modifier =
          modifier
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
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      // Action Content (Icon OR FPS)
      if (fps != null) {
        Text(
            text = fps,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
        )
      } else {
        Icon(
            imageVector = Icons.Rounded.SportsEsports,
            contentDescription = "Expand",
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

@Composable
fun GamePanelCard(
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

  // Performance Stats
  val fps by viewModel.fpsValue.collectAsStateWithLifecycle()
  val cpuLoad by viewModel.cpuLoad.collectAsStateWithLifecycle()
  val gpuLoad by viewModel.gpuLoad.collectAsStateWithLifecycle()

  Card(
      shape = RoundedCornerShape(28.dp), // Slightly tighter radius
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier =
          modifier
              .width(312.dp) // COMPACT: Reduced from 360dp for less width
              .wrapContentHeight()
              .padding(8.dp), // Less outer padding
  ) {
    Column(
        modifier = Modifier.padding(20.dp), // Reduced inner padding from 24dp
        verticalArrangement = Arrangement.spacedBy(16.dp), // Tighter spacing
    ) {
      // 1. EXPRESSIVE HEADER
      Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier =
              Modifier.fillMaxWidth()
                  .pointerInput(Unit) {
                      detectDragGestures(onDragEnd = onDragEnd) { change, dragAmount ->
                          change.consume()
                          onDrag(dragAmount.x, dragAmount.y)
                      }
                  }
                  .clickable(
                      interactionSource = remember { MutableInteractionSource() },
                      indication = null, // No ripple
                  ) {
                    onCollapse()
                  },
      ) {
        // Big Bold Clock (Smaller)
        Text(
            text = time,
            style = MaterialTheme.typography.headlineMedium, // Reduced from displaySmall
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = (-1).sp,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Status Pills (More compact)
        StatusPill(icon = Icons.Rounded.Thermostat, text = "$temp°C", isWarning = true)
        Spacer(modifier = Modifier.width(6.dp))
        StatusPill(icon = Icons.Rounded.BatteryStd, text = "$batteryLevel%")

        // Close button removed, click header to close
      }

      // 2. BRIGHTNESS (Compact slider)
      BrightnessControlExpressive(viewModel)

      // 3. PERFORMANCE WIDGET (Bento Box)
      PerformanceBento(fps, cpuLoad, gpuLoad)

      // 4. GAME MODE SELECTOR (Text Only)
      ExpressiveModeSelector(viewModel)

      // 5. TOOLS GRID (Slidable Row)
      ToolsGridExpressive(viewModel, isFpsEnabled, onFpsToggle)
    }
  }
}

@Composable
fun StatusPill(icon: ImageVector, text: String, isWarning: Boolean = false) {
  Surface(
      color = if (isWarning) Color(0xFF2E1A1A) else Color(0xFF1A261A), // Darker muted backgrounds
      contentColor = if (isWarning) Color(0xFFEF9A9A) else Color(0xFFA5D6A7),
      shape = CircleShape,
      modifier = Modifier.height(28.dp), // Reduced height
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 10.dp),
    ) {
      Icon(icon, null, modifier = Modifier.size(12.dp))
      Spacer(modifier = Modifier.width(4.dp))
      Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
  }
}

@Composable
fun PerformanceBento(fps: String, cpu: Float, gpu: Float) {
  Row(
      modifier =
          Modifier.fillMaxWidth().height(86.dp), // Reduced height from 96dp for better proportions
      horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    // FPS Card (Left - Big)
    Card(
        modifier = Modifier.weight(1.3f).fillMaxHeight(),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(32.dp), // Reverted to Rounded Rect
    ) {
      Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          val fpsVal = fps.toFloatOrNull()?.toInt() ?: 60
          Text(
              text = "$fpsVal",
              style = MaterialTheme.typography.headlineMedium, // Reduced from displaySmall
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              lineHeight = 32.sp,
          )
          Text(
              text = "FPS",
              style = MaterialTheme.typography.labelSmall, // Reduced from labelMedium
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
          )
        }
      }
    }

    // Load Stats (Right - Stacked)
    Column(
        modifier = Modifier.weight(1f).fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      LoadChip(
          label = "CPU",
          value = cpu,
          color = MaterialTheme.colorScheme.tertiary,
          modifier = Modifier.weight(1f),
      )
      LoadChip(
          label = "GPU",
          value = gpu,
          color = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
fun LoadChip(label: String, value: Float, color: Color, modifier: Modifier = Modifier) {
  Surface(
      color = MaterialTheme.colorScheme.surfaceContainer,
      shape = RoundedCornerShape(14.dp),
      modifier = modifier.fillMaxWidth(),
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "${value.toInt()}%",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
      }
    }
  }
}

@Composable
fun ExpressiveModeSelector(viewModel: GameMonitorViewModel) {
  val currentMode by viewModel.currentPerformanceMode.collectAsStateWithLifecycle()
  val modes =
      listOf("powersave" to "Power Save", "balanced" to "Balance", "performance" to "Performance")

  Row(
      modifier =
          Modifier.fillMaxWidth()
              .height(48.dp) // Reduced height from 56dp
              .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
              .padding(4.dp)
  ) {
    modes.forEach { (modeKey, modeLabel) ->
      val isSelected = currentMode == modeKey
      val animatedColor by
          animateColorAsState(
              if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
          )

      Box(
          modifier =
              Modifier.weight(1f)
                  .fillMaxHeight()
                  .clip(CircleShape)
                  .background(animatedColor)
                  .clickable { viewModel.setPerformanceMode(modeKey) },
          contentAlignment = Alignment.Center,
      ) {
        // TEXT ONLY as requested
        Text(
            text = modeLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else Color.Gray,
            maxLines = 1,
        )
      }
    }
  }
}

@Composable
fun BrightnessControlExpressive(viewModel: GameMonitorViewModel) {
  val vmBrightness by viewModel.brightness.collectAsStateWithLifecycle()
  var localSliderValue by remember { mutableFloatStateOf(vmBrightness) }
  var isDragging by remember { mutableStateOf(false) }
  val displayValue = if (isDragging) localSliderValue else vmBrightness

  LaunchedEffect(vmBrightness) {
    if (!isDragging) {
      localSliderValue = vmBrightness
    }
  }

  Surface(
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      shape = CircleShape,
      modifier = Modifier.fillMaxWidth().height(44.dp),
  ) {
    Box(contentAlignment = Alignment.CenterStart) {
      // Track Fill
      Box(
          modifier =
              Modifier.fillMaxWidth(displayValue.coerceIn(0.01f, 1f))
                  .fillMaxHeight()
                  .background(MaterialTheme.colorScheme.tertiary, CircleShape)
      )

      // Interaction overlay (invisible slider)
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
          colors =
              SliderDefaults.colors(
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Icon(
            Icons.Rounded.BrightnessHigh,
            null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}

@Composable
fun ToolsGridExpressive(
    viewModel: GameMonitorViewModel,
    isFpsEnabled: Boolean,
    onFpsToggle: () -> Unit,
) {
  val dnd by viewModel.doNotDisturb.collectAsStateWithLifecycle()
  val ringerMode by viewModel.ringerMode.collectAsStateWithLifecycle()
  val callMode by viewModel.callMode.collectAsStateWithLifecycle()
  val threeFingerSwipe by viewModel.threeFingerSwipe.collectAsStateWithLifecycle()

  androidx.compose.foundation.lazy.LazyRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(horizontal = 2.dp),
  ) {
    item {
      ToolButtonExpressive(
          icon = Icons.Rounded.Speed, // FPS Icon
          label = "FPS",
          isActive = isFpsEnabled,
      ) {
        onFpsToggle()
      }
    }
    item {
        val (icon, label) = when(ringerMode) {
             1 -> Icons.Rounded.Vibration to "Vibrate"
             2 -> Icons.Rounded.NotificationsOff to "Silent"
             else -> Icons.Rounded.Notifications to "Ring"
        }
        ToolButtonExpressive(
            icon = icon,
            label = label,
            isActive = ringerMode != 0, // Active color if not Normal
        ) {
            viewModel.cycleRingerMode()
        }
    }
    item {
        val (icon, label) = when(callMode) {
             1 -> Icons.Rounded.NotificationsPaused to "No HeadsUp"
             2 -> Icons.Rounded.CallEnd to "Reject" // or PhoneDisabled
             else -> Icons.Rounded.Call to "Call"
        }
        ToolButtonExpressive(
            icon = icon,
            label = label,
            isActive = callMode != 0,
        ) {
            if (callMode == 2) {
                // If in reject mode, test the call functionality
                viewModel.testCallFunctionality()
            } else {
                viewModel.cycleCallMode()
            }
        }
    }

    item {
        val isActive = threeFingerSwipe
        val icon = if (isActive) Icons.Rounded.TouchApp else Icons.Rounded.DoNotDisturb // or Block
        ToolButtonExpressive(
            icon = icon,
            label = if (isActive) "Swipe On" else "Swipe Off",
            isActive = isActive,
        ) {
            viewModel.toggleThreeFingerSwipe()
        }
    }


    item {
      ToolButtonExpressive(
          icon = Icons.Rounded.DoNotDisturb,
          label = "DND",
          isActive = dnd,
      ) {
        viewModel.setDND(!dnd)
      }
    }

    item {
        val isTouchGuard by viewModel.touchGuard.collectAsStateWithLifecycle()
        ToolButtonExpressive(
            icon = if (isTouchGuard) Icons.Rounded.BackHand else Icons.Rounded.PanTool, // Hand icon
            label = "Disable Gesture", 
            isActive = isTouchGuard,
        ) {
            viewModel.setTouchGuard(!isTouchGuard)
        }
    }

    item {
      ToolButtonExpressive(
          icon = Icons.Rounded.RocketLaunch,
          label = "Boost",
          isActive = false,
      ) {
        viewModel.performGameBoost()
      }
    }

    item {
      ToolButtonExpressive(
          icon = Icons.Rounded.Screenshot,
          label = "Screenshot",
          isActive = false,
      ) { 
        viewModel.takeScreenshot()
      }
    }


  }
}

@Composable
fun ToolButtonExpressive(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
  val bgColor =
      if (isActive) MaterialTheme.colorScheme.primary
      else MaterialTheme.colorScheme.surfaceContainer
  val iconColor =
      if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

  Column(
      modifier = modifier,
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp), 
        color = bgColor,
        modifier = Modifier.size(56.dp).aspectRatio(1f), 
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp)) 
      }
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = Color.LightGray,
        fontSize = 11.sp,
    )
  }
}
