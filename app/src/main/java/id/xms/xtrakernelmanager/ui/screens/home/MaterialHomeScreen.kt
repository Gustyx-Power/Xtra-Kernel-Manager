package id.xms.xtrakernelmanager.ui.screens.home

import android.annotation.SuppressLint
import android.graphics.Matrix
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.DeviceSilhouette
import id.xms.xtrakernelmanager.ui.components.WavyProgressIndicator
import id.xms.xtrakernelmanager.ui.screens.home.components.ExpandablePowerFab
import id.xms.xtrakernelmanager.ui.screens.home.components.SettingsSheet
import java.util.Locale
import kotlinx.coroutines.delay
import id.xms.xtrakernelmanager.ui.model.PowerAction
import id.xms.xtrakernelmanager.ui.model.getLocalizedLabel

/** Material Home Screen - Restored Layout with Dynamic Colors (Material You) */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialHomeScreen(
    preferencesManager: PreferencesManager,
    viewModel: HomeViewModel = viewModel(),
    onPowerAction: (PowerAction) -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
  val context = LocalContext.current

  // Bottom Sheet State
  @OptIn(ExperimentalMaterial3Api::class) val powerSheetState = rememberModalBottomSheetState()
  var showPowerBottomSheet by remember { mutableStateOf(false) }

  @OptIn(ExperimentalMaterial3Api::class) val settingsSheetState = rememberModalBottomSheetState()
  var showSettingsBottomSheet by remember { mutableStateOf(false) }

  // Data State
  val cpuInfo by viewModel.cpuInfo.collectAsState()
  val gpuInfo by viewModel.gpuInfo.collectAsState()
  val batteryInfo by viewModel.batteryInfo.collectAsState()
  val systemInfo by viewModel.systemInfo.collectAsState()
  val powerInfo by viewModel.powerInfo.collectAsState()

  LaunchedEffect(Unit) { viewModel.loadBatteryInfo(context) }

  Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      floatingActionButton = {
        ExpandablePowerFab(onPowerAction = { action -> onPowerAction(action) })
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .offset(y = (-16).dp)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          // Header
          StaggeredEntry(delayMillis = 0) {
            MaterialHeader(onSettingsClick = { showSettingsBottomSheet = true })
          }

          // Device Info Card
          StaggeredEntry(delayMillis = 100) { MaterialDeviceCard(systemInfo = systemInfo) }

          // CPU & GPU Tiles Row
          StaggeredEntry(delayMillis = 200) {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), // Force equal height
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
              MaterialStatTile(
                  modifier = Modifier.weight(1f).fillMaxHeight(),
                  icon = Icons.Rounded.Memory, // Chip icon
                  label = "Load",
                  value = "${String.format(Locale.US, "%.0f", cpuInfo.totalLoad)}%",
                  subValue = "${(cpuInfo.cores.maxOfOrNull { it.currentFreq } ?: 0) / 1000} MHz",
                  color = MaterialTheme.colorScheme.primary,
                  badgeText = "CPU",
              )

              MaterialStatTile(
                  modifier = Modifier.weight(1f).fillMaxHeight(),
                  icon = Icons.Rounded.Videocam,
                  label = "Freq",
                  value = "${gpuInfo.currentFreq}",
                  subValue = "MHz",
                  color = MaterialTheme.colorScheme.tertiary,
                  badgeText = "GPU",
              )
            }
          }

          // GPU Information Card
          StaggeredEntry(delayMillis = 300) { MaterialGPUCard(gpuInfo = gpuInfo) }

          // Memory & Storage Card
          StaggeredEntry(delayMillis = 400) { MaterialMemoryCard(systemInfo = systemInfo) }

          // Battery Information Card
          StaggeredEntry(delayMillis = 500) { MaterialBatteryCard(batteryInfo = batteryInfo) }

          // Power Insight Card (New)
          StaggeredEntry(delayMillis = 600) { MaterialPowerInsightCard(powerInfo, batteryInfo) }

          // App Info Section
          StaggeredEntry(delayMillis = 700) { MaterialAppInfoSection() }

          // Bottom Spacing
          Spacer(modifier = Modifier.height(80.dp))
        }
      },
  )

  // Power Menu Sheet
  if (showPowerBottomSheet) {
    ModalBottomSheet(
        onDismissRequest = { showPowerBottomSheet = false },
        sheetState = powerSheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
      PowerMenuContent(
          onAction = {
            showPowerBottomSheet = false
            onPowerAction(it)
          }
      )
    }
  }

  // Settings Sheet
  if (showSettingsBottomSheet) {
    ModalBottomSheet(
        onDismissRequest = { showSettingsBottomSheet = false },
        sheetState = settingsSheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
      SettingsSheet(
          preferencesManager = preferencesManager,
          onDismiss = { showSettingsBottomSheet = false },
      )
    }
  }
}

@Composable
fun MaterialHeader(onSettingsClick: () -> Unit) {
  val view = androidx.compose.ui.platform.LocalView.current
  var isShortTitle by remember { mutableStateOf(false) }
  var clickCount by remember { mutableIntStateOf(0) }

  LaunchedEffect(clickCount) {
    if (clickCount > 0) {
      delay(500)
      clickCount = 0
    }
  }

  Row(
      modifier =
          Modifier.fillMaxWidth().clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
          ) {
            clickCount++
            if (clickCount >= 3) {
              isShortTitle = !isShortTitle
              clickCount = 0
              if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
              } else {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
              }
            }
          },
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    AnimatedContent(
        targetState = isShortTitle,
        transitionSpec = {
          (slideInVertically { height -> height } + fadeIn()).togetherWith(
              slideOutVertically { height -> -height } + fadeOut()
          )
        },
        label = "HeaderTitle",
    ) { short ->
      Text(
          text = if (short) "XKM" else "Xtra Kernel Manager",
          style =
              if (short) MaterialTheme.typography.displayMedium
              else MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onBackground,
      )
    }

    IconButton(
        onClick = onSettingsClick,
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
    ) {
      Icon(
          imageVector = Icons.Rounded.Settings,
          contentDescription = "Settings",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
fun MaterialDeviceCard(systemInfo: SystemInfo) {
  Card(
      modifier = Modifier.fillMaxWidth().animateContentSize(),
      shape = MaterialTheme.shapes.extraLarge,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              text = android.os.Build.MANUFACTURER.uppercase(),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              letterSpacing = 2.sp,
              fontWeight = FontWeight.Bold,
          )

          // Smart Badge: Board/SoC
          Surface(
              color =
                  MaterialTheme.colorScheme.onPrimaryContainer.copy(
                      alpha = 0.1f
                  ), // Tonal on primary
              shape = CircleShape, // Fully rounded pill
          ) {
            Text(
                text = android.os.Build.BOARD.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text =
                systemInfo.deviceModel
                    .replace(android.os.Build.MANUFACTURER, "", ignoreCase = true)
                    .trim()
                    .ifBlank { "Unknown Model" },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        Text(
            text = android.os.Build.DEVICE.ifBlank { "Unknown" },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.05f),
            shape = MaterialTheme.shapes.large,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(end = 100.dp), // Increased padding to shift left away from silhouette
        ) {
          Text(
              text = systemInfo.kernelVersion.ifBlank { "Unknown" },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
              fontFamily = FontFamily.Monospace,
              maxLines = 1,
              overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          )
        }
      }

      // Device Silhouette with Wallpaper
      Box(
          modifier =
              Modifier.align(Alignment.BottomEnd)
                  .padding(end = 24.dp)
                  .offset(y = 12.dp) // Raised up to show more phone
      ) {
        DeviceSilhouette(
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f),
            showWallpaper = true,
        )
      }
    }
  }
}

@Composable
fun MaterialStatTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String,
    color: Color,
    badgeText: String? = null, // Optional badge (e.g., "CPU", "GPU")
) {
  Card(
      modifier = modifier.animateContentSize(),
      shape = MaterialTheme.shapes.extraLarge,
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
          ),
  ) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                    top = 12.dp,
                ), // Lift header slightly
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
      ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(36.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp), // Slightly smaller icon
            )
          }
        }

        // Optional Smart Badge
        if (badgeText != null) {
          Surface(
              color = color.copy(alpha = 0.1f),
              shape = CircleShape,
          ) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Column(verticalArrangement = Arrangement.spacedBy((-2).dp)) {
        Text(
            text = label,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value,
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle =
                        LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both,
                        ),
                ),
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            lineHeight = 32.sp, // Tight line height for headlineMedium (usually 36)
        )
        Text(
            text = subValue,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
        )
      }
    }
  }
}

@Composable
fun MaterialGPUCard(gpuInfo: id.xms.xtrakernelmanager.data.model.GPUInfo) {
  Card(
      modifier = Modifier.fillMaxWidth().animateContentSize(),
      shape = MaterialTheme.shapes.extraLarge,
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
  ) {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = "GPU",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )

        // Smart Badge: GPU Renderer
        Surface(
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
            shape = CircleShape,
        ) {
          val gpuBadge =
              remember(gpuInfo.renderer) {
                when {
                  gpuInfo.renderer.contains("Adreno", true) -> "Adreno"
                  gpuInfo.renderer.contains("Mali", true) -> "Mali"
                  gpuInfo.renderer.contains("PowerVR", true) -> "PowerVR"
                  gpuInfo.renderer.contains("NVIDIA", true) -> "NVIDIA"
                  gpuInfo.renderer != "Unknown" -> gpuInfo.renderer.take(12)
                  else -> gpuInfo.vendor.ifEmpty { "GPU" }
                }
              }
          Text(
              text = gpuBadge.uppercase(),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      Column {
        Text(
            text = "${gpuInfo.currentFreq} MHz",
            style = MaterialTheme.typography.displaySmall, // Expressive Typography
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
            text = "Frequency",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
        )
      }

      Row(
          modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), // Ensure equal height
          horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Inner Card 1: Load
        Surface(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.large,
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${gpuInfo.gpuLoad}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Load",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
          }
        }

        // Inner Card 2: GPU Name
        Surface(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.large,
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            val cleanGpuName =
                remember(gpuInfo.renderer) {
                  when {
                    gpuInfo.renderer.contains("Adreno", ignoreCase = true) -> {
                      val match = Regex("Adreno.*?(\\d{3})").find(gpuInfo.renderer)
                      match?.let { "Adreno ${it.groupValues[1]}" } ?: gpuInfo.renderer
                    }
                    gpuInfo.renderer.contains("Mali", ignoreCase = true) -> {
                      val match = Regex("Mali[- ]?(G\\d+|T\\d+)?").find(gpuInfo.renderer)
                      match?.value?.trim() ?: gpuInfo.renderer
                    }
                    else -> gpuInfo.renderer
                  }
                }
            Text(
                text = cleanGpuName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 1.2.em,
            )
            Text(
                text = "GPU",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
          }
        }
      }
    }
  }
}

@Composable
fun MaterialBatteryCard(batteryInfo: BatteryInfo) {
  Card(
      modifier = Modifier.fillMaxWidth().animateContentSize(),
      shape = MaterialTheme.shapes.extraLarge,
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer,
          ),
  ) {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
      // Header
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Box(
              modifier =
                  Modifier.size(36.dp)
                      .background(
                          MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                          MaterialTheme.shapes.medium,
                      ),
              contentAlignment = Alignment.Center,
          ) {
            Icon(
                imageVector = Icons.Rounded.BatteryChargingFull,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
          }
          Text(
              text = "Battery",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
          )
        }

        // Smart Badge: Technology
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = CircleShape,
        ) {
          Text(
              text = batteryInfo.technology.takeIf { it != "Unknown" } ?: "Li-ion",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          )
        }
      }

      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        BatterySilhouette(
            level = batteryInfo.level / 100f,
            isCharging = batteryInfo.status.contains("Charging", ignoreCase = true),
            color = MaterialTheme.colorScheme.primary,
        )

        Column {
          Text(
              text = "${batteryInfo.level}%",
              style = MaterialTheme.typography.displayMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              lineHeight = 1.em,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BatteryStatusChip(text = batteryInfo.status)
            BatteryStatusChip(
                text = "Health ${String.format(Locale.US, "%.0f", batteryInfo.healthPercent)}%"
            )
          }
        }
      }

      // Stats Grid (2x2) - 4 Individual Cards
      Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Row 1
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          val currentText =
              if (batteryInfo.currentNow >= 0) "+${batteryInfo.currentNow} mA"
              else "${batteryInfo.currentNow} mA"
          BatteryStatBox(label = "Current", value = currentText, modifier = Modifier.weight(1f))
          BatteryStatBox(
              label = "Voltage",
              value = "${batteryInfo.voltage} mV",
              modifier = Modifier.weight(1f),
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          BatteryStatBox(
              label = "Temperature",
              value = "${batteryInfo.temperature}°C",
              modifier = Modifier.weight(1f),
          )
          BatteryStatBox(
              label = "Cycle Count",
              value = "${batteryInfo.cycleCount}",
              modifier = Modifier.weight(1f),
          )
        }
      }
    }
  }
}

@Composable
fun BatterySilhouette(level: Float, isCharging: Boolean, color: Color) {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(2.dp), // Gap between cap and body
  ) {
    // Battery Cap (Nub)
    Box(
        modifier =
            Modifier.size(20.dp, 4.dp)
                .background(
                    MaterialTheme.colorScheme.outlineVariant,
                    MaterialTheme.shapes.extraSmall,
                )
    )

    // Main Body
    Box(
        modifier =
            Modifier.size(50.dp, 80.dp)
                .border(4.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                .padding(4.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
      Box(
          modifier =
              Modifier.fillMaxWidth()
                  .fillMaxHeight(level)
                  .background(color, MaterialTheme.shapes.small)
      )
    }
  }
}

@Composable
fun BatteryStatusChip(text: String) {
  Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
    )
  }
}

@Composable
fun BatteryStatBox(label: String, value: String, modifier: Modifier = Modifier) {
  Card(
      modifier = modifier,
      shape = MaterialTheme.shapes.large,
      colors =
          CardDefaults.cardColors(
              containerColor =
                  MaterialTheme.colorScheme.background.copy(alpha = 0.6f) // Higher contrast
          ),
  ) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
      // Dot removed for cleaner look

      Text(
          text = value,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
      )
    }
  }
}

@Composable
fun MaterialMemoryCard(systemInfo: SystemInfo) {
  Card(
      modifier = Modifier.fillMaxWidth().animateContentSize(),
      shape = MaterialTheme.shapes.extraLarge,
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
  ) {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
      // Header
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier.size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                        MaterialTheme.shapes.medium,
                    ),
            contentAlignment = Alignment.Center,
        ) {
          Icon(
              imageVector = Icons.Rounded.Storage,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Memory",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
      }

      // RAM Section
      val ramUsed = (systemInfo.totalRam - systemInfo.availableRam)
      val ramTotal = systemInfo.totalRam
      val ramProgress = if (ramTotal > 0) ramUsed.toFloat() / ramTotal.toFloat() else 0f

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
          Text(
              text = "RAM",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
          )
          Text(
              text = "${formatFileSize(ramUsed)} / ${formatFileSize(ramTotal)}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
          )
        }

        WavyProgressIndicator(
            progress = ramProgress,
            modifier = Modifier.fillMaxWidth().height(16.dp), // Height for the wave
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
            strokeWidth = 4.dp,
            amplitude = 4.dp,
        )
      }

      // ZRAM / Swap Section (Show if Swap OR ZRAM exists)
      val showZram = systemInfo.swapTotal > 0 || systemInfo.zramSize > 0

      if (showZram) {
        // Prefer Swap stats if available, otherwise fallback to ZRAM capacity with 0 usage
        val swapTotal = if (systemInfo.swapTotal > 0) systemInfo.swapTotal else systemInfo.zramSize
        val swapUsed =
            if (systemInfo.swapTotal > 0) (systemInfo.swapTotal - systemInfo.swapFree)
            else systemInfo.zramUsed
        val swapProgress = if (swapTotal > 0) swapUsed.toFloat() / swapTotal.toFloat() else 0f

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Bottom,
          ) {
            Text(
                text = if (systemInfo.zramSize > 0) "ZRAM" else "Swap",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = "${formatFileSize(swapUsed)} / ${formatFileSize(swapTotal)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
            )
          }

          WavyProgressIndicator(
              progress = swapProgress,
              modifier = Modifier.fillMaxWidth().height(16.dp),
              color = MaterialTheme.colorScheme.tertiary,
              trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
              strokeWidth = 4.dp,
              amplitude = 4.dp,
          )
        }
      }

      // Internal Storage Section
      val storageUsed = (systemInfo.totalStorage - systemInfo.availableStorage)
      val storageTotal = systemInfo.totalStorage
      val storageProgress =
          if (storageTotal > 0) storageUsed.toFloat() / storageTotal.toFloat() else 0f

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
          Text(
              text = "Internal Storage",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
          )
          Text(
              text = "${formatFileSize(storageUsed)} / ${formatFileSize(storageTotal)}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
          )
        }

        WavyProgressIndicator(
            progress = storageProgress,
            modifier = Modifier.fillMaxWidth().height(16.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
            strokeWidth = 4.dp,
            amplitude = 4.dp,
        )
      }
    }
  }
}

private fun formatFileSize(bytes: Long): String {
  val gb = bytes / (1024.0 * 1024.0 * 1024.0)
  return if (gb >= 1.0) {
    String.format(Locale.US, "%.1f GB", gb)
  } else {
    val mb = bytes / (1024.0 * 1024.0)
    String.format(Locale.US, "%.0f MB", mb)
  }
}

@Composable
fun PowerMenuContent(onAction: (PowerAction) -> Unit) {
  Column(
      modifier = Modifier.fillMaxWidth().padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
        text = "Power Menu",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(8.dp))

    val actions =
        listOf(
            PowerAction.PowerOff,
            PowerAction.Reboot,
            PowerAction.Recovery,
            PowerAction.Bootloader,
            PowerAction.SystemUI,
        )

    actions.forEach { action -> PowerMenuItem(action = action, onClick = { onAction(action) }) }
  }
}

@Composable
fun PowerMenuItem(action: PowerAction, onClick: () -> Unit) {
  Surface(
      onClick = onClick,
      shape = MaterialTheme.shapes.large,
      color = MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier.fillMaxWidth(),
  ) {
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      Icon(
          imageVector = action.icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
      )
      Spacer(modifier = Modifier.width(16.dp))
      Text(
          text = action.getLocalizedLabel(),
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
fun StaggeredEntry(delayMillis: Int, content: @Composable () -> Unit) {
  var visible by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(delayMillis.toLong())
    visible = true
  }

  AnimatedVisibility(
      visible = visible,
      enter =
          fadeIn(animationSpec = tween(500)) +
              slideInVertically(
                  animationSpec =
                      tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                  initialOffsetY = { 100 },
              ),
      exit = fadeOut(),
  ) {
    content()
  }
}

@Composable
fun MaterialPowerInsightCard(
    powerInfo: id.xms.xtrakernelmanager.data.model.PowerInfo,
    batteryInfo: BatteryInfo,
) {
  // Determine badge text and color based on charging status
  val (badgeText, badgeColor) =
      if (powerInfo.isCharging) {
        "Charging" to MaterialTheme.colorScheme.primaryContainer
      } else {
        "Screen On" to MaterialTheme.colorScheme.tertiaryContainer
      }
  val badgeTextColor =
      if (powerInfo.isCharging) {
        MaterialTheme.colorScheme.onPrimaryContainer
      } else {
        MaterialTheme.colorScheme.onTertiaryContainer
      }

  Card(
      modifier = Modifier.fillMaxWidth().animateContentSize(),
      shape = MaterialTheme.shapes.extraLarge,
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
          ),
  ) {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
      // Header
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
      ) {
        // Icon + Title Group
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Box(
              modifier =
                  Modifier.size(36.dp)
                      .background(
                          MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                          MaterialTheme.shapes.medium,
                      ),
              contentAlignment = Alignment.Center,
          ) {
            Icon(
                imageVector = Icons.Rounded.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
          }
          Text(
              text = "Power Insight",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
          )
        }

        // Badge (Pushed to Right)
        Surface(
            color = badgeColor,
            shape = CircleShape,
        ) {
          Text(
              text = badgeText,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = badgeTextColor,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }

      // Content: Circle + Stats
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        // SOT Circular Indicator
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
          // Background Track
          WavyCircularProgressIndicator(
              progress = 1f,
              modifier = Modifier.fillMaxSize(),
              color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
              strokeWidth = 16.dp,
              amplitude = 3.dp,
              frequency = 10,
          )

          // Progress (based on 8 hour SOT goal)
          WavyCircularProgressIndicator(
              progress = batteryInfo.level / 100f,
              modifier = Modifier.fillMaxSize(),
              color = MaterialTheme.colorScheme.primary,
              strokeWidth = 16.dp,
              amplitude = 3.dp,
              frequency = 10,
          )

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = powerInfo.formatScreenOnTime(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
            )
          }
        }

        // Stats Column
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
          PowerInsightItem(
              label = "Screen On",
              value = powerInfo.formatScreenOnTime(),
              icon = Icons.Rounded.LightMode,
          )
          PowerInsightItem(
              label = "Screen Off",
              value = powerInfo.formatScreenOffTime(),
              icon = Icons.Rounded.ScreenLockPortrait,
          )
          PowerInsightItem(
              label = "Deep Sleep",
              value = powerInfo.formatDeepSleepTime(),
              icon = Icons.Rounded.Bedtime,
          )
          PowerInsightItem(
              label = "Drain Rate",
              value = String.format("-%.1f%%/h", powerInfo.activeDrainRate),
              icon = Icons.Rounded.BatteryAlert,
          )
        }
      }
    }
  }
}

@Composable
fun WavyCircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: androidx.compose.ui.unit.Dp,
    amplitude: androidx.compose.ui.unit.Dp = 4.dp,
    frequency: Int = 12,
) {
  val strokeWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { strokeWidth.toPx() }
  val amplitudePx = with(androidx.compose.ui.platform.LocalDensity.current) { amplitude.toPx() }

  // Animate progress using animateFloatAsState
  val animatedProgress by
      animateFloatAsState(
          targetValue = progress,
          animationSpec =
              tween(
                  durationMillis = 1000,
                  easing = androidx.compose.animation.core.FastOutSlowInEasing,
              ),
          label = "progress",
      )

  androidx.compose.foundation.Canvas(modifier = modifier) {
    val radius = (size.minDimension - strokeWidthPx - amplitudePx * 2) / 2
    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
    val path = androidx.compose.ui.graphics.Path()

    val startAngle = -90f
    val sweepAngle = 360f * animatedProgress

    // Resolution of the path (step size in degrees). Smaller = smoother.
    val step = 1f

    for (angle in 0..sweepAngle.toInt()) {
      val currentAngle = startAngle + angle
      val rad = Math.toRadians(currentAngle.toDouble())

      // Wavy function: radius + amplitude * sin(frequency * angle in rads)
      // Use angle relative to start to maintain wave phase
      val wavePhase = Math.toRadians((angle * frequency).toDouble())
      val r = radius + amplitudePx * kotlin.math.sin(wavePhase)

      val x = center.x + r * kotlin.math.cos(rad)
      val y = center.y + r * kotlin.math.sin(rad)

      if (angle == 0) {
        path.moveTo(x.toFloat(), y.toFloat())
      } else {
        path.lineTo(x.toFloat(), y.toFloat())
      }
    }

    drawPath(
        path = path,
        color = color,
        style =
            androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round,
            ),
    )
  }
}

@Composable
fun PowerInsightItem(label: String, value: String, icon: ImageVector) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Box(
        modifier =
            Modifier.size(32.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
      Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.size(16.dp),
      )
    }
    Column {
      Text(
          text = value,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
      )
      Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
      )
    }
  }
}

@Composable
fun MaterialAppInfoSection() {
  Card(
      modifier = Modifier.fillMaxWidth().animateContentSize(),
      shape = MaterialTheme.shapes.extraLarge,
      colors =
          CardDefaults.cardColors(
              containerColor =
                  MaterialTheme.colorScheme.surfaceVariant.copy(
                      alpha = 0.5f
                  ) // Slightly distinct background
          ),
  ) {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
      // Top Row: Maintainer & Release
      Row(
          modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), // Ensure equal height
          horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Maintainer Card
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight(), // Fill available height
            shape = MaterialTheme.shapes.extraLarge,
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ),
        ) {
          Column(
              modifier = Modifier.padding(16.dp).fillMaxSize(), // Fill column to distribute space
              verticalArrangement = Arrangement.SpaceBetween, // Push content to edges
          ) {
            // Avatar & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
              SubcomposeAsyncImage(
                  model =
                      ImageRequest.Builder(LocalContext.current)
                          .data("https://github.com/Xtra-Manager-Software.png")
                          .crossfade(true)
                          .build(),
                  contentDescription = "Maintainer Avatar",
                  loading = {
                    Box(
                        modifier =
                            Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                      Text(
                          "XT",
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.onPrimaryContainer,
                      )
                    }
                  },
                  error = {
                    Box(
                        modifier =
                            Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                      Text(
                          "XT",
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.onPrimaryContainer,
                      )
                    }
                  },
                  modifier = Modifier.size(64.dp).clip(CookieShape),
              )

              Surface(
                  color = MaterialTheme.colorScheme.secondaryContainer,
                  shape = MaterialTheme.shapes.small,
              ) {
                Text(
                    text = "TEAM",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
              }
            }

            Column {
              Text(
                  text = "MAINTAINER",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                  letterSpacing = 1.sp,
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "XMS Team",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.Verified,
                    contentDescription = "Verified",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
              }
              Text(
                  text = "@Xtra-Manager-Software",
                  style = MaterialTheme.typography.bodySmall,
                  color =
                      MaterialTheme.colorScheme.onSurfaceVariant.copy(
                          alpha = 0.7f
                      ), // Reduced alpha for handle
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        // Release Card
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight(), // Fill available height
            shape = MaterialTheme.shapes.extraLarge,
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ),
        ) {
          Column(
              modifier = Modifier.padding(16.dp).fillMaxSize(), // Fill column
              verticalArrangement = Arrangement.SpaceBetween, // Distribute vertical space
          ) {
            val fullVersion = BuildConfig.VERSION_NAME
            val isDebug = fullVersion.contains("Dev", ignoreCase = true)
            val badgeText = if (isDebug) "DEBUG" else "STABLE"
            val badgeColor =
                if (isDebug) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary

            val versionSplit = fullVersion.split("-", limit = 2)
            val mainVersion = versionSplit.getOrNull(0) ?: fullVersion
            // Remove hyphen from suffix
            val versionSuffix = versionSplit.getOrNull(1) ?: ""

            // Icon & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
              Box(
                  modifier =
                      Modifier.size(40.dp)
                          .clip(MaterialTheme.shapes.medium)
                          .background(badgeColor.copy(alpha = 0.2f)), // Match badge color
                  contentAlignment = Alignment.Center,
              ) {
                Icon(
                    imageVector = Icons.Rounded.Inventory, // Cardboard Box "Kardus"
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(20.dp), // Smaller icon
                )
              }

              Surface(color = badgeColor.copy(alpha = 0.1f), shape = CircleShape) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
              }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 12.dp),
            ) {
              Text(
                  text = "VERSION",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                  letterSpacing = 1.sp,
              )
              Text(
                  text = mainVersion,
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
              )
              if (versionSuffix.isNotEmpty()) {
                Text(
                    text = versionSuffix,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
              }
              val relativeTime =
                  try {
                    android.text.format.DateUtils.getRelativeTimeSpanString(
                            BuildConfig.BUILD_TIMESTAMP,
                            System.currentTimeMillis(),
                            android.text.format.DateUtils.MINUTE_IN_MILLIS,
                        )
                        .toString()
                  } catch (e: Exception) {
                    BuildConfig.BUILD_DATE // Fallback
                  }
              Text(
                  text = relativeTime,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontFamily = FontFamily.Monospace,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.padding(top = 4.dp),
              )
            }
          }
        }
      }

      // Project Info Card
      Card(
          modifier = Modifier.fillMaxWidth(),
          shape = MaterialTheme.shapes.extraLarge,
          colors =
              CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
              ),
      ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "ABOUT XKM",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
            Text(
                text =
                    "Xtra Kernel Manager is a free and open-source Kernel Manager designed to give you full control over your device's kernel. Built with Kotlin Jetpack Compose for a smooth and responsive user experience.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                lineHeight = 20.sp,
            )
          }

          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            // GitHub Button
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
            OutlinedButton(
                onClick = { uriHandler.openUri("https://github.com/Xtra-Manager-Software") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                border =
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                    ),
            ) {
              // Custom GitHub Icon
              Icon(
                  imageVector = GithubIcon,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                  tint = MaterialTheme.colorScheme.onSurface,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                  "GitHub",
                  color = MaterialTheme.colorScheme.onSurface,
                  style = MaterialTheme.typography.labelLarge,
              )
            }

            // Credits Button
            FilledTonalButton(
                onClick = {
                  uriHandler.openUri("https://github.com/Xtra-Manager-Software/Xtra-Kernel-Manager")
                },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
            ) {
              Icon(Icons.Rounded.Info, null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Credit", style = MaterialTheme.typography.labelLarge)
            }
          }
        }
      }
    }
  }
}

private val GithubIcon: ImageVector
  get() {
    if (_GithubIcon != null) return _GithubIcon!!
    _GithubIcon =
        ImageVector.Builder(
                name = "Github",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
            .apply {
              path(
                  fill = SolidColor(Color.Black),
                  fillAlpha = 1f,
                  stroke = null,
                  strokeAlpha = 1f,
                  strokeLineWidth = 1.0f,
                  strokeLineCap = StrokeCap.Butt,
                  strokeLineJoin = StrokeJoin.Miter,
                  strokeLineMiter = 1.0f,
                  pathFillType = PathFillType.NonZero,
              ) {
                moveTo(12.0f, 1.27f)
                curveTo(5.97f, 1.27f, 1.08f, 6.04f, 1.08f, 11.93f)
                curveTo(1.08f, 16.64f, 4.23f, 20.64f, 8.5f, 22.01f)
                curveTo(9.05f, 22.11f, 9.25f, 21.78f, 9.25f, 21.5f)
                curveTo(9.25f, 21.25f, 9.24f, 20.59f, 9.24f, 19.71f)
                curveTo(6.18f, 20.36f, 5.54f, 18.27f, 5.54f, 18.27f)
                curveTo(5.04f, 17.03f, 4.31f, 16.7f, 4.31f, 16.7f)
                curveTo(3.31f, 16.03f, 4.38f, 16.04f, 4.38f, 16.04f)
                curveTo(5.49f, 16.12f, 6.07f, 17.15f, 6.07f, 17.15f)
                curveTo(7.05f, 18.79f, 8.64f, 18.32f, 9.27f, 18.04f)
                curveTo(9.36f, 17.35f, 9.64f, 16.89f, 9.95f, 16.63f)
                curveTo(7.51f, 16.36f, 4.95f, 15.44f, 4.95f, 11.36f)
                curveTo(4.95f, 10.2f, 5.38f, 9.24f, 6.08f, 8.49f)
                curveTo(5.97f, 8.22f, 5.59f, 7.13f, 6.18f, 5.67f)
                curveTo(6.18f, 5.67f, 7.1f, 5.38f, 9.19f, 6.76f)
                curveTo(10.06f, 6.52f, 11.0f, 6.4f, 11.93f, 6.4f)
                curveTo(12.86f, 6.4f, 13.8f, 6.52f, 14.67f, 6.76f)
                curveTo(16.76f, 5.38f, 17.67f, 5.67f, 17.67f, 5.67f)
                curveTo(18.27f, 7.13f, 17.89f, 8.22f, 17.78f, 8.49f)
                curveTo(18.48f, 9.24f, 18.91f, 10.2f, 18.91f, 11.36f)
                curveTo(18.91f, 15.45f, 16.34f, 16.35f, 13.89f, 16.62f)
                curveTo(14.28f, 16.95f, 14.63f, 17.61f, 14.63f, 18.61f)
                curveTo(14.63f, 20.04f, 14.61f, 21.2f, 14.61f, 21.5f)
                curveTo(14.61f, 21.79f, 14.81f, 22.12f, 15.37f, 22.01f)
                curveTo(19.62f, 20.63f, 22.78f, 16.64f, 22.78f, 11.93f)
                curveTo(22.78f, 6.04f, 17.88f, 1.27f, 11.93f, 1.27f)
                close()
              }
            }
            .build()
    return _GithubIcon!!
  }

private var _GithubIcon: ImageVector? = null

private val CookieShape =
    object : androidx.compose.ui.graphics.Shape {
      override fun createOutline(
          size: androidx.compose.ui.geometry.Size,
          layoutDirection: androidx.compose.ui.unit.LayoutDirection,
          density: androidx.compose.ui.unit.Density,
      ): androidx.compose.ui.graphics.Outline {
        val p = androidx.compose.ui.graphics.Path()
        val androidPath = p.asAndroidPath()
        val matrix = Matrix()

        val polygon =
            RoundedPolygon.star(
                numVerticesPerRadius = 6,
                innerRadius = 0.75f,
                rounding = CornerRounding(0.5f),
                innerRounding = CornerRounding(0.5f),
            )

        val minSize = minOf(size.width, size.height)
        val scale = minSize / 2f
        val cx = size.width / 2f
        val cy = size.height / 2f

        matrix.setScale(scale, scale)
        matrix.postTranslate(cx, cy)

        polygon.toPath(androidPath)
        androidPath.transform(matrix)

        return androidx.compose.ui.graphics.Outline.Generic(p)
      }
    }
