package id.xms.xtrakernelmanager.ui.screens.misc.material

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.components.gameoverlay.HardwareGauge
import id.xms.xtrakernelmanager.ui.components.gameoverlay.HardwareGaugeType
import id.xms.xtrakernelmanager.ui.screens.misc.components.GameMonitorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialGameMonitorScreen(viewModel: GameMonitorViewModel, onBack: () -> Unit) {
  val cpuFreq by viewModel.cpuFreq.collectAsState()
  val cpuLoad by viewModel.cpuLoad.collectAsState()
  val gpuFreq by viewModel.gpuFreq.collectAsState()
  val gpuLoad by viewModel.gpuLoad.collectAsState()
  val fpsValue by viewModel.fpsValue.collectAsState()
  val tempValue by viewModel.tempValue.collectAsState()
  val gameDuration by viewModel.gameDuration.collectAsState()
  val batteryPercentage by viewModel.batteryPercentage.collectAsState()

  val esportsMode by viewModel.esportsMode.collectAsState()
  val touchGuard by viewModel.touchGuard.collectAsState()
  val blockNotifications by viewModel.blockNotifications.collectAsState()
  val doNotDisturb by viewModel.doNotDisturb.collectAsState()
  val autoRejectCalls by viewModel.autoRejectCalls.collectAsState()
  val lockBrightness by viewModel.lockBrightness.collectAsState()
  val isClearingRam by viewModel.isClearingRam.collectAsState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Performance Monitor", fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
              IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            },
        )
      }
  ) { paddingValues ->
    Box(modifier = Modifier.padding(paddingValues)) {
      MaterialGameMonitorContent(viewModel = viewModel)
    }
  }
}

@Composable
fun MaterialGameMonitorContent(viewModel: GameMonitorViewModel, modifier: Modifier = Modifier) {
  val cpuFreq by viewModel.cpuFreq.collectAsState()
  val cpuLoad by viewModel.cpuLoad.collectAsState()
  val gpuFreq by viewModel.gpuFreq.collectAsState()
  val gpuLoad by viewModel.gpuLoad.collectAsState()
  val fpsValue by viewModel.fpsValue.collectAsState()
  val tempValue by viewModel.tempValue.collectAsState()
  val gameDuration by viewModel.gameDuration.collectAsState()
  val batteryPercentage by viewModel.batteryPercentage.collectAsState()

  val esportsMode by viewModel.esportsMode.collectAsState()
  val touchGuard by viewModel.touchGuard.collectAsState()
  val blockNotifications by viewModel.blockNotifications.collectAsState()
  val doNotDisturb by viewModel.doNotDisturb.collectAsState()
  val autoRejectCalls by viewModel.autoRejectCalls.collectAsState()
  val lockBrightness by viewModel.lockBrightness.collectAsState()
  val isClearingRam by viewModel.isClearingRam.collectAsState()

  Column(
      modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Performance Cards
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      // CPU Card
      ElevatedCard(modifier = Modifier.weight(1f), colors = CardDefaults.elevatedCardColors()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text("CPU", style = MaterialTheme.typography.titleSmall)
          Spacer(Modifier.height(8.dp))
          HardwareGauge(HardwareGaugeType.CPU, cpuLoad, cpuFreq)
        }
      }

      // GPU Card
      ElevatedCard(modifier = Modifier.weight(1f), colors = CardDefaults.elevatedCardColors()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text("GPU", style = MaterialTheme.typography.titleSmall)
          Spacer(Modifier.height(8.dp))
          HardwareGauge(HardwareGaugeType.GPU, gpuLoad, gpuFreq)
        }
      }
    }

    // Stats Row
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
      Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        StatItem(fpsValue, "FPS", Color(0xFF00E5FF))
        StatItem("$tempValue°C", "Temp", Color(0xFFFF7B72))
        StatItem(gameDuration, "Time", Color(0xFFD2A8FF))
        StatItem("$batteryPercentage%", "Batt", Color(0xFFFFA657))
      }
    }

    // Esports Mode
    EsportsCard(isEnabled = esportsMode, onToggle = { viewModel.setEsportsMode(it) })

    Text("Game Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

    // Tools Grid
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToolCard(
            icon = Icons.Outlined.TouchApp,
            label = "Touch Guard",
            isActive = touchGuard,
            onClick = { viewModel.setTouchGuard(!touchGuard) },
            modifier = Modifier.weight(1f),
        )
        ToolCard(
            icon = Icons.Outlined.NotificationsOff,
            label = "Block Notif",
            isActive = blockNotifications,
            onClick = { viewModel.setBlockNotifications(!blockNotifications) },
            modifier = Modifier.weight(1f),
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToolCard(
            icon = Icons.Outlined.DoNotDisturbOn,
            label = "DND",
            isActive = doNotDisturb,
            onClick = { viewModel.setDND(!doNotDisturb) },
            modifier = Modifier.weight(1f),
        )
        ToolCard(
            icon = Icons.Outlined.PhoneDisabled,
            label = "Reject Calls",
            isActive = autoRejectCalls,
            onClick = { viewModel.setAutoRejectCalls(!autoRejectCalls) },
            modifier = Modifier.weight(1f),
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToolCard(
            icon = Icons.Outlined.LightMode,
            label = "Lock Brightness",
            isActive = lockBrightness,
            onClick = { viewModel.setLockBrightness(!lockBrightness) },
            modifier = Modifier.weight(1f),
        )
        ToolCard(
            icon = Icons.Outlined.CleaningServices,
            label = if (isClearingRam) "Clearing..." else "Clear RAM",
            isActive = false, // Always simple button
            onClick = { viewModel.clearRAM() },
            modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
fun StatItem(value: String, label: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
        text = value,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = color,
    )
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
fun EsportsCard(isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
  Card(
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (isEnabled) MaterialTheme.colorScheme.errorContainer
                  else MaterialTheme.colorScheme.surfaceVariant
          ),
      onClick = { onToggle(!isEnabled) },
  ) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Bolt,
            contentDescription = null,
            tint =
                if (isEnabled) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(16.dp))
        Column {
          Text(
              "Esports Mode",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color =
                  if (isEnabled) MaterialTheme.colorScheme.onErrorContainer
                  else MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
              "Maximize performance",
              style = MaterialTheme.typography.bodySmall,
              color =
                  if (isEnabled) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                  else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Switch(checked = isEnabled, onCheckedChange = onToggle)
    }
  }
}

@Composable
fun ToolCard(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Card(
      modifier = modifier,
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (isActive) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surfaceContainer
          ),
      onClick = onClick,
  ) {
    Column(
        modifier = Modifier.padding(12.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
      Icon(
          icon,
          contentDescription = null,
          tint =
              if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
              else MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(Modifier.height(8.dp))
      Text(
          label,
          style = MaterialTheme.typography.labelMedium,
          color =
              if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
              else MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
      )
    }
  }
}
