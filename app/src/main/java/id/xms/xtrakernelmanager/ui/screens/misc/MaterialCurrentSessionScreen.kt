package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialCurrentSessionScreen(viewModel: MiscViewModel, onBack: () -> Unit) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0 = Charging, 1 = Discharging

  // Collect real data
  val batteryInfo by viewModel.batteryInfo.collectAsState()
  val screenOnTime by viewModel.screenOnTime.collectAsState()
  val screenOffTime by viewModel.screenOffTime.collectAsState()
  val deepSleepTime by viewModel.deepSleepTime.collectAsState()
  val drainRate by viewModel.drainRate.collectAsState()

  // Calculate session values
  val isCharging = batteryInfo.status.contains("Charging", ignoreCase = true)
  val currentMa = kotlin.math.abs(batteryInfo.currentNow)
  val voltageV = batteryInfo.voltage / 1000f
  val powerW = voltageV * (currentMa / 1000f)
  val batteryLevel = batteryInfo.level

  // Expressive Palette
  val surfaceColor = MaterialTheme.colorScheme.surface
  val primaryColor = MaterialTheme.colorScheme.primary
  val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
  val onSecondaryContainer = MaterialTheme.colorScheme.onSecondaryContainer

  // Background Gradient for a "Premium" feel
  val backgroundBrush =
      Brush.verticalGradient(
          colors =
              listOf(
                  MaterialTheme.colorScheme.surface,
                  MaterialTheme.colorScheme.surfaceContainer, // Slightly lighter bottom
              )
      )

  Scaffold(
      containerColor = Color.Transparent,
      contentColor = MaterialTheme.colorScheme.onSurface,
      topBar = {
        TopAppBar(
            title = { Text("Session Insight", fontWeight = FontWeight.SemiBold, fontSize = 24.sp) },
            navigationIcon = {
              IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
        )
      },
  ) { paddingValues ->
    Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
      LazyColumn(
          modifier =
              Modifier.fillMaxSize()
                  .padding(paddingValues)
                  .padding(horizontal = 24.dp), // More breathing room
          verticalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        item {
          SessionHeroCard(
              time = if (selectedTab == 0) screenOnTime else screenOffTime,
              status = if (selectedTab == 0) "Charging Time" else "Usage Time",
              progress = batteryLevel / 100f,
              accentColor =
                  if (selectedTab == 0) Color(0xFF4CAF50) else Color(0xFFFF5252), // Green/Red
          )
        }

        item {
          ExpressiveSegmentedControl(
              selectedTab = selectedTab,
              onTabSelected = { selectedTab = it },
          )
        }

        item {
          Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
              ExpressiveStatCard(
                  title = "Rate",
                  value = drainRate,
                  subValue = "~$currentMa mA",
                  icon = Icons.Rounded.Speed,
                  accentColor = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.weight(1f),
              )

              ExpressiveStatCard(
                  title = if (isCharging) "Gained" else "Lost",
                  value = "$batteryLevel%",
                  subValue = "$currentMa mA",
                  icon =
                      if (isCharging) Icons.Rounded.BatteryChargingFull
                      else Icons.Rounded.BatteryAlert,
                  accentColor = MaterialTheme.colorScheme.tertiary,
                  modifier = Modifier.weight(1f),
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
              CompactStatCard(
                  label = "Screen On",
                  value = screenOnTime,
                  icon = Icons.Rounded.WbSunny,
                  containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                  modifier = Modifier.weight(1f),
              )
              CompactStatCard(
                  label = "Screen Off",
                  value = screenOffTime,
                  icon = Icons.Rounded.NightsStay,
                  containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                  modifier = Modifier.weight(1f),
              )
            }
          }
        }

        // 4. Detailed "Bento" Grid
        item {
          Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            Text(
                text = "Deep Dive",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 4.dp),
            )

            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f)
                    ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                  modifier = Modifier.padding(16.dp),
                  verticalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                DetailRowItem(
                    label = "Est. Capacity",
                    value = "${batteryInfo.totalCapacity} mAh",
                    icon = Icons.Rounded.BatteryStd,
                )
                DetailRowItem(
                    label = "Temperature",
                    value = "%.1f°C".format(batteryInfo.temperature),
                    icon = Icons.Rounded.Thermostat,
                )
                DetailRowItem(
                    label = "Deep Sleep",
                    value = deepSleepTime,
                    icon = Icons.Rounded.Bedtime,
                )
                DetailRowItem(
                    label = "Voltage",
                    value = "%.2f V".format(voltageV),
                    icon = Icons.Rounded.ElectricBolt,
                )
                DetailRowItem(
                    label = "Power",
                    value = "%.1f W".format(powerW),
                    icon = Icons.Rounded.FlashOn,
                )
              }
            }
          }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
      }
    }
  }
}

@Composable
fun SessionHeroCard(time: String, status: String, progress: Float, accentColor: Color) {
  // Gradient for the card background to avoid "pitch black" look
  val brush =
      Brush.verticalGradient(
          colors =
              listOf(
                  MaterialTheme.colorScheme.surfaceContainerHigh,
                  MaterialTheme.colorScheme.surfaceContainer,
              )
      )

  Card(
      colors =
          CardDefaults.cardColors(
              containerColor = Color.Transparent
          ), // Use Box with background for gradient
      shape = RoundedCornerShape(32.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Slight elevation for depth
      modifier =
          Modifier.fillMaxWidth()
              .border(
                  1.dp,
                  MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                  RoundedCornerShape(32.dp),
              ),
  ) {
    Box(modifier = Modifier.background(brush).padding(24.dp)) {
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = status.uppercase(),
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.5.sp,
              color = MaterialTheme.colorScheme.primary,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
              text = time,
              style =
                  MaterialTheme.typography.displayLarge.copy(
                      fontSize = 58.sp
                  ), // Slightly smaller, cleaner
              fontWeight = FontWeight.Bold, // Reduced from Black to Bold for cleaner look
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 64.sp,
          )
        }

        // Wavy Circular Progress Indicator
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
          WavyCircularProgressIndicator(
              progress = progress,
              modifier = Modifier.fillMaxSize(),
              color = accentColor,
              trackColor = accentColor.copy(alpha = 0.15f),
              strokeWidth = 10.dp,
              amplitude = 4.dp,
              frequency = 8, // Reduced frequency for "fewer curves" look
          )
          Text(
              text = "${(progress * 100).roundToInt()}%",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface,
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
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 8.dp,
    amplitude: Dp = 4.dp, // Wave height
    frequency: Int = 12, // Number of waves
) {
  Canvas(modifier = modifier) {
    val width = size.width
    val height = size.height
    val radius = (width.coerceAtMost(height) - strokeWidth.toPx() - amplitude.toPx() * 2) / 2f
    val center = Offset(width / 2f, height / 2f)
    val waveAmplitudePx = amplitude.toPx()

    // Helper to create wavy path
    fun createWavyPath(startAngle: Float, sweepAngle: Float): androidx.compose.ui.graphics.Path {
      val path = androidx.compose.ui.graphics.Path()
      val step = 1f // degree step

      for (angle in 0..sweepAngle.toInt()) {
        val currentAngle = startAngle + angle
        val theta = Math.toRadians(currentAngle.toDouble())

        // r = R + A * sin(N * theta)
        val r = radius + waveAmplitudePx * kotlin.math.sin(frequency * theta)

        val x = center.x + r * kotlin.math.cos(theta)
        val y = center.y + r * kotlin.math.sin(theta)

        if (angle == 0) {
          path.moveTo(x.toFloat(), y.toFloat())
        } else {
          path.lineTo(x.toFloat(), y.toFloat())
        }
      }
      return path
    }

    // Draw Track (Full Circle)
    val trackPath = createWavyPath(-90f, 360f)
    drawPath(
        path = trackPath,
        color = trackColor,
        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
    )

    // Draw Progress
    if (progress > 0) {
      val progressPath = createWavyPath(-90f, 360f * progress)
      drawPath(
          path = progressPath,
          color = color,
          style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
      )
    }
  }
}

@Composable
fun ExpressiveSegmentedControl(selectedTab: Int, onTabSelected: (Int) -> Unit) {
  Surface(
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      shape = RoundedCornerShape(50),
      modifier = Modifier.fillMaxWidth().height(56.dp),
  ) {
    Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
      TabButton(
          text = "Charging",
          isSelected = selectedTab == 0,
          onClick = { onTabSelected(0) },
          modifier = Modifier.weight(1f),
      )
      TabButton(
          text = "Discharging",
          isSelected = selectedTab == 1,
          onClick = { onTabSelected(1) },
          modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val containerColor by
      animateColorAsState(
          targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
          label = "tabContainer",
      )
  val contentColor by
      animateColorAsState(
          targetValue =
              if (isSelected) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.onSurfaceVariant,
          label = "tabContent",
      )
  val shadowElevation by
      animateFloatAsState(targetValue = if (isSelected) 2f else 0f, label = "tabShadow")

  Surface(
      color = containerColor,
      contentColor = contentColor,
      shape = RoundedCornerShape(50),
      shadowElevation = shadowElevation.dp,
      onClick = onClick,
      modifier = modifier.fillMaxHeight(),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
  }
}

@Composable
fun ExpressiveStatCard(
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
  Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      shape = RoundedCornerShape(28.dp),
      modifier = modifier.aspectRatio(0.9f), // Taller aspect
  ) {
    Column(
        modifier = Modifier.padding(20.dp).fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Icon Pill
      Surface(
          color = accentColor.copy(alpha = 0.15f),
          shape = CircleShape,
          modifier = Modifier.size(40.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
              imageVector = icon,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(20.dp),
          )
        }
      }

      Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall, // Responsive size
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = subValue,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        )
      }
    }
  }
}

@Composable
fun CompactStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
  Card(
      colors = CardDefaults.cardColors(containerColor = containerColor),
      shape = RoundedCornerShape(20.dp),
      modifier = modifier,
  ) {
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      // Tiny Icon
      Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
  }
}

@Composable
fun DetailRowItem(label: String, value: String, icon: ImageVector) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(MaterialTheme.colorScheme.surfaceContainerLow)
              .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      // Icon
      Box(
          modifier =
              Modifier.size(32.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surfaceContainerHigh),
          contentAlignment = Alignment.Center,
      ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Spacer(modifier = Modifier.width(16.dp))
      Text(
          text = label,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
      )
    }

    Text(
        text = value,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
  }
}
