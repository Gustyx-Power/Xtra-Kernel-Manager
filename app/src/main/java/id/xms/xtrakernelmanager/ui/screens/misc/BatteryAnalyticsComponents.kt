package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.repository.CurrentFlowRepository
import id.xms.xtrakernelmanager.ui.components.CurrentFlowChart
import kotlin.math.abs

/**
 * Main Battery Analytics Status Card Shows: Charging/Discharging tabs, Big Current Value, Voltage &
 * Power badges
 */
@Composable
fun BatteryAnalyticsStatusCard(viewModel: MiscViewModel) {
  val batteryInfo by viewModel.batteryInfo.collectAsState()
  val isCharging =
      batteryInfo.status.contains("Charging", ignoreCase = true) ||
          batteryInfo.status.contains("Full", ignoreCase = true)

  val voltageV = batteryInfo.voltage / 1000f
  val currentA = batteryInfo.currentNow / 1000f
  val powerW = abs(voltageV * currentA)

  Card(
      modifier = Modifier.fillMaxWidth().height(220.dp),
      shape = RoundedCornerShape(32.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF535C91)),
      elevation = CardDefaults.cardElevation(0.dp),
  ) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
      Column {
        // Top Row: Tabs & Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
              color = Color.Black.copy(alpha = 0.2f),
              shape = RoundedCornerShape(50),
          ) {
            Row(modifier = Modifier.padding(4.dp)) {
              BadgeTab(text = "Charging", selected = isCharging)
              BadgeTab(
                  text = "Discharging",
                  selected = !isCharging,
                  activeColor = Color(0xFFE57373),
              )
            }
          }

          Surface(
              color = Color.White.copy(alpha = 0.2f),
              shape = CircleShape,
              modifier = Modifier.size(40.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Rounded.Bolt, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
          }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Current Value
        Row(verticalAlignment = Alignment.Bottom) {
          Text(
              text = String.format("%,d", abs(batteryInfo.currentNow)),
              style = MaterialTheme.typography.displayMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
              text = "mA",
              style = MaterialTheme.typography.titleMedium,
              color = Color.White.copy(alpha = 0.7f),
              modifier = Modifier.padding(bottom = 8.dp),
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Stats: Voltage & Power
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          ParamChip(icon = Icons.Rounded.Speed, text = "%.1f V".format(voltageV))
          ParamChip(icon = Icons.Rounded.Bolt, text = "%.1f W".format(powerW))
        }
      }
    }
  }
}

@Composable
fun BadgeTab(text: String, selected: Boolean, activeColor: Color = Color.White) {
  if (selected) {
    Surface(
        color = activeColor,
        shape = RoundedCornerShape(50),
    ) {
      Text(
          text = text,
          style = MaterialTheme.typography.labelMedium,
          color = if (activeColor == Color.White) Color.Black else Color.White,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
      )
    }
  } else {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White.copy(alpha = 0.5f),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
    )
  }
}

@Composable
fun ParamChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
  Surface(color = Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
      Icon(icon, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text(
          text = text,
          style = MaterialTheme.typography.labelLarge,
          color = Color.White,
          fontWeight = FontWeight.Bold,
      )
    }
  }
}

/** Current Flow Card with actual chart */
@Composable
fun CurrentFlowCard() {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  // Selected time range in minutes
  var selectedMinutes by remember { mutableIntStateOf(10) }

  // Collect samples from repository
  val allSamples by CurrentFlowRepository.samples.collectAsState()

  // Initialize repository
  LaunchedEffect(Unit) { CurrentFlowRepository.initialize(context) }

  // Filter samples for selected time range
  val filteredSamples =
      remember(allSamples, selectedMinutes) {
        CurrentFlowRepository.getSamplesForRange(selectedMinutes)
      }

  Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(32.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
      elevation = CardDefaults.cardElevation(0.dp),
  ) {
    Column(modifier = Modifier.padding(24.dp)) {
      // Header
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            "Current Flow",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Tabs
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          TimeTab("10m", selectedMinutes == 10) { selectedMinutes = 10 }
          TimeTab("1h", selectedMinutes == 60) { selectedMinutes = 60 }
          TimeTab("6h", selectedMinutes == 360) { selectedMinutes = 360 }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Chart
      Box(
          modifier =
              Modifier.fillMaxWidth()
                  .height(150.dp)
                  .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                  .padding(8.dp),
          contentAlignment = Alignment.Center,
      ) {
        if (filteredSamples.isEmpty()) {
          Text(
              "Collecting data...",
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
          )
        } else {
          CurrentFlowChart(samples = filteredSamples, modifier = Modifier.fillMaxSize())
        }
      }
    }
  }
}

@Composable
fun TimeTab(text: String, selected: Boolean, onClick: () -> Unit) {
  Surface(
      onClick = onClick,
      color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
      shape = RoundedCornerShape(50),
      border =
          if (!selected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
          else null,
  ) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color =
            if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
    )
  }
}

/** Stats Row (Avg, Min, Max) */
@Composable
fun BatteryStatsRow(viewModel: MiscViewModel) {
  val min by viewModel.minCurrent.collectAsState()
  val max by viewModel.maxCurrent.collectAsState()
  val avg by viewModel.avgCurrent.collectAsState()

  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
    CompactStatCard("Avg", "$avg mA", Modifier.weight(1f))
    CompactStatCard("Min", "$min mA", Modifier.weight(1f))
    CompactStatCard("Max", "$max mA", Modifier.weight(1f))
  }
}

@Composable
fun CompactStatCard(label: String, value: String, modifier: Modifier = Modifier) {
  Card(
      modifier = modifier,
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
