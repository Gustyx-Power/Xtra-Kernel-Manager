package id.xms.xtrakernelmanager.ui.screens.tuning.material

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import id.xms.xtrakernelmanager.data.model.ClusterInfo
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.screens.tuning.material.components.CoreControlCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CPUTuningScreen(
    viewModel: TuningViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSmartLock: () -> Unit = {}
) {

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("CPU Tuning", fontWeight = FontWeight.SemiBold, fontSize = 24.sp) },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
        )
      },
  ) { paddingValues ->
    Box(modifier = Modifier.padding(paddingValues)) {
      ClusterTuningContent(
          viewModel = viewModel,
          onNavigateToSmartLock = onNavigateToSmartLock,
          modifier = Modifier.fillMaxSize()
      )
    }
  }
}

@Composable
fun ClusterTuningContent(
    viewModel: TuningViewModel,
    onNavigateToSmartLock: () -> Unit = {},
    modifier: Modifier = Modifier
) {
  val cpuClusters by viewModel.cpuClusters.collectAsState()
  val cpuCores by viewModel.cpuCores.collectAsState()
  val clusterStates by viewModel.clusterStates.collectAsState()

  LazyColumn(
      modifier = modifier,
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Smart Frequency Lock Card
    item { SmartFrequencyLockCard(onNavigateToSmartLock = onNavigateToSmartLock) }
    
    // Core Management Card (NEW - Separated from clusters)
    item { 
      CoreControlCard(
          cores = cpuCores,
          viewModel = viewModel
      )
    }
    
    items(cpuClusters) { cluster -> ClusterCard(cluster = cluster, viewModel = viewModel) }

    // Set on Boot Toggle
    item { SetOnBootCard(viewModel = viewModel) }
  }
}

@Composable
fun ClusterCard(cluster: ClusterInfo, viewModel: TuningViewModel) {
  Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
      // Header
      Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth(),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
              modifier =
                  Modifier.size(40.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
              contentAlignment = Alignment.Center,
          ) {
            Icon(Icons.Rounded.Memory, null, tint = MaterialTheme.colorScheme.primary)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
                text = "Cluster ${cluster.clusterNumber}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = "CPU ${cluster.cores.first()}-${cluster.cores.last()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
          Text(
              text = "ONLINE",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      // Frequencies
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FrequencyDropdownTile(
            modifier = Modifier.weight(1f),
            label = "Min Frequency",
            value = "${cluster.currentMinFreq} MHz",
            options = (cluster.minFreq..cluster.maxFreq step 100).map { "$it MHz" },
            onValueChange = { selectedStr ->
              val freq = selectedStr.removeSuffix(" MHz").toIntOrNull() ?: cluster.minFreq
              viewModel.setCpuClusterFrequency(cluster.clusterNumber, freq, cluster.currentMaxFreq)
            },
        )
        FrequencyDropdownTile(
            modifier = Modifier.weight(1f),
            label = "Max Frequency",
            value = "${cluster.currentMaxFreq} MHz",
            options = (cluster.minFreq..cluster.maxFreq step 100).map { "$it MHz" },
            onValueChange = { selectedStr ->
              val freq = selectedStr.removeSuffix(" MHz").toIntOrNull() ?: cluster.maxFreq
              viewModel.setCpuClusterFrequency(cluster.clusterNumber, cluster.currentMinFreq, freq)
            },
        )
      }

      GovernorCard(
          currentGovernor = cluster.governor,
          availableGovernors = cluster.availableGovernors,
          onGovernorSelected = { newGov ->
            viewModel.setCpuClusterGovernor(cluster.clusterNumber, newGov)
          },
      )
    }
  }
}

@Composable
fun FrequencyDropdownTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    options: List<String> = emptyList(),
    onValueChange: (String) -> Unit = {},
) {
  var expanded by remember { mutableStateOf(false) }
  var rowSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

  Surface(
      modifier =
          modifier
              .onGloballyPositioned { coordinates -> rowSize = coordinates.size.toSize() }
              .clickable { expanded = true },
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), // Inner Surface
      shape = RoundedCornerShape(12.dp),
  ) {
    Column {
      Column(modifier = Modifier.padding(12.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
              value,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.primary,
          )
          Icon(
              Icons.Rounded.ArrowDropDown,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      DropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false },
          modifier =
              Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
                  .width(
                      with(androidx.compose.ui.platform.LocalDensity.current) {
                        rowSize.width.toDp()
                      }
                  )
                  .heightIn(max = 250.dp),
          shape = RoundedCornerShape(12.dp),
      ) {
        options.forEach { option ->
          val isSelected = option == value
          DropdownMenuItem(
              text = {
                Text(
                    option,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
              },
              colors =
                  MenuDefaults.itemColors(
                      textColor =
                          if (isSelected) MaterialTheme.colorScheme.primary
                          else MaterialTheme.colorScheme.onSurface,
                  ),
              onClick = {
                onValueChange(option)
                expanded = false
              },
          )
        }
      }
    }
  }
}

@Composable
fun GovernorCard(
    currentGovernor: String,
    availableGovernors: List<String>,
    onGovernorSelected: (String) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }

  Surface(
      modifier = Modifier.fillMaxWidth().animateContentSize().clickable { expanded = !expanded },
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.secondaryContainer,
  ) {
    Column {
      // Header Row
      Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
              modifier =
                  Modifier.size(40.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
              contentAlignment = Alignment.Center,
          ) {
            Icon(Icons.Rounded.Speed, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
          }
          Spacer(modifier = Modifier.width(16.dp))
          Column {
            Text(
                "Governor",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
            )
            Text(
                currentGovernor,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }
        }
      }

      // Expanded Content
      AnimatedVisibility(visible = expanded) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        ) {
          Column(modifier = Modifier.padding(8.dp)) {
            availableGovernors.forEach { gov ->
              val isSelected = gov == currentGovernor
              Row(
                  modifier =
                      Modifier.fillMaxWidth()
                          .clip(RoundedCornerShape(8.dp))
                          .clickable {
                            onGovernorSelected(gov)
                            expanded = false
                          }
                          .background(
                              if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                              else androidx.compose.ui.graphics.Color.Transparent
                          )
                          .padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                    text = gov,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color =
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun SetOnBootCard(viewModel: TuningViewModel) {
  val cpuSetOnBoot by viewModel.preferencesManager.getCpuSetOnBoot().collectAsState(initial = false)

  Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
  ) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Box(
            modifier =
                Modifier.size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
          Icon(Icons.Rounded.PowerSettingsNew, null, tint = MaterialTheme.colorScheme.primary)
        }
        Column {
          Text(
              "Set on Boot",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
              "Apply CPU settings on startup",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Switch(
          checked = cpuSetOnBoot,
          onCheckedChange = { enabled -> viewModel.setCpuSetOnBoot(enabled) },
      )
    }
  }
}


@Composable
fun SmartFrequencyLockCard(onNavigateToSmartLock: () -> Unit) {
  Surface(
      modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onNavigateToSmartLock),
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.tertiaryContainer,
  ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
          Icon(
              Icons.Rounded.Lock,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.tertiary,
              modifier = Modifier.size(24.dp)
          )
        }
        Column {
          Text(
              "Smart Frequency Lock",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onTertiaryContainer,
          )
          Text(
              "Configure advanced frequency locking",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
          )
        }
      }
      Icon(
          Icons.Rounded.ChevronRight,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onTertiaryContainer,
      )
    }
  }
}
