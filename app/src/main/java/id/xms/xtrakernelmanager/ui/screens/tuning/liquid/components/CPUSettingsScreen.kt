package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.ClusterInfo
import id.xms.xtrakernelmanager.ui.components.LottieSwitchControlled
import id.xms.xtrakernelmanager.ui.screens.tuning.ClusterUIState
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CPUSettingsScreen(viewModel: TuningViewModel, onNavigateBack: () -> Unit) {
  val clusters by viewModel.cpuClusters.collectAsState()
  val clusterStates by viewModel.clusterStates.collectAsState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  stringResource(R.string.cpu_control),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
              )
            },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
        )
      }
  ) { paddingValues ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
      if (clusters.isEmpty()) {
        item { EmptyState() }
      } else {
        items(clusters.size) { index ->
          val cluster = clusters[index]
          ModernClusterCard(
              cluster = cluster,
              clusterIndex = index,
              uiState = clusterStates[cluster.clusterNumber],
              viewModel = viewModel,
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyState() {
  Column(
      modifier = Modifier.fillMaxWidth().padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Icon(
        imageVector = Icons.Rounded.SentimentDissatisfied,
        contentDescription = null,
        modifier = Modifier.size(48.dp),
        tint = MaterialTheme.colorScheme.outline,
    )
    Text(
        text = stringResource(R.string.cpu_no_clusters),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.outline,
        textAlign = TextAlign.Center,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernClusterCard(
    cluster: ClusterInfo,
    clusterIndex: Int,
    uiState: ClusterUIState?,
    viewModel: TuningViewModel,
) {
  val clusterTitle =
      when (clusterIndex) {
        0 -> "Performance Cores"
        1 -> "Efficiency Cores"
        else -> "Cluster ${clusterIndex + 1}"
      }.let {
        if (clusterIndex < 3)
            stringResource(
                when (clusterIndex) {
                  0 -> R.string.cpu_cluster_0
                  1 -> R.string.cpu_cluster_1
                  else -> R.string.cpu_cluster_2
                }
            )
        else stringResource(R.string.cpu_cluster_generic, clusterIndex + 1)
      }

  val currentMinFreq = uiState?.minFreq ?: cluster.currentMinFreq.toFloat()
  val currentMaxFreq = uiState?.maxFreq ?: cluster.currentMaxFreq.toFloat()
  val currentGovernor = uiState?.governor?.takeIf { it.isNotBlank() } ?: cluster.governor

  var minFreqSlider by remember(cluster.clusterNumber) { mutableFloatStateOf(currentMinFreq) }
  var maxFreqSlider by remember(cluster.clusterNumber) { mutableFloatStateOf(currentMaxFreq) }
  var showGovernorDialog by remember { mutableStateOf(false) }
  var isExpanded by remember { mutableStateOf(true) } // Default expanded in detailed view

  // Sync state
  LaunchedEffect(currentMinFreq, currentMaxFreq) {
    minFreqSlider = currentMinFreq
    maxFreqSlider = currentMaxFreq
  }

  OutlinedCard(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors =
          CardDefaults.outlinedCardColors(
              containerColor = MaterialTheme.colorScheme.surface,
              contentColor = MaterialTheme.colorScheme.onSurface,
          ),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Cluster Header
      Row(
          modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded }.padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
              modifier =
                  Modifier.size(40.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.secondaryContainer),
              contentAlignment = Alignment.Center,
          ) {
            Text(
                text = "C${clusterIndex}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }

          Column {
            Text(
                text = clusterTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "${cluster.minFreq}MHz - ${cluster.maxFreq}MHz",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        IconButton(onClick = { isExpanded = !isExpanded }) {
          Icon(
              imageVector = Icons.Rounded.KeyboardArrowDown,
              contentDescription = "Expand",
              modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
          )
        }
      }

      AnimatedVisibility(visible = isExpanded) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

          // Frequency Sliders
          FrequencyControl(
              label = stringResource(R.string.min_frequency),
              value = minFreqSlider,
              range = cluster.minFreq.toFloat()..cluster.maxFreq.toFloat(),
              availableFrequencies = cluster.availableFrequencies,
              color = MaterialTheme.colorScheme.primary,
              onValueChange = {
                minFreqSlider = it
                viewModel.updateClusterUIState(cluster.clusterNumber, it, maxFreqSlider)
              },
              onValueChangeFinished = {
                viewModel.setCPUFrequency(
                    cluster.clusterNumber,
                    minFreqSlider.toInt(),
                    maxFreqSlider.toInt(),
                )
              },
          )

          FrequencyControl(
              label = stringResource(R.string.max_frequency),
              value = maxFreqSlider,
              range = cluster.minFreq.toFloat()..cluster.maxFreq.toFloat(),
              availableFrequencies = cluster.availableFrequencies,
              color = MaterialTheme.colorScheme.tertiary,
              onValueChange = {
                maxFreqSlider = it
                viewModel.updateClusterUIState(cluster.clusterNumber, minFreqSlider, it)
              },
              onValueChangeFinished = {
                viewModel.setCPUFrequency(
                    cluster.clusterNumber,
                    minFreqSlider.toInt(),
                    maxFreqSlider.toInt(),
                )
              },
          )

          // Governor
          GovernorSelector(
              currentGovernor = currentGovernor,
              onClick = { showGovernorDialog = true },
          )

          // Cores
          if (cluster.cores.any { it != 0 }) {
            CoreControl(cluster.cores, viewModel)
          }
        }
      }
    }
  }

  if (showGovernorDialog) {
    GovernorSelectionDialog(
        governors = cluster.availableGovernors,
        selectedGovernor = currentGovernor,
        onDismiss = { showGovernorDialog = false },
        onSelect = {
          viewModel.setCPUGovernor(cluster.clusterNumber, it)
          showGovernorDialog = false
        },
    )
  }
}

@Composable
private fun FrequencyControl(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    availableFrequencies: List<Int>,
    color: Color,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          text = label,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
          text = "${value.toInt()} MHz",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = color,
      )
    }

    if (availableFrequencies.isNotEmpty()) {
      // Discrete Slider for available frequencies
      val sortedFreqs = remember(availableFrequencies) { availableFrequencies.sorted() }
      // Find closest index for current value
      val currentIndex =
          remember(value, sortedFreqs) {
            val idx = sortedFreqs.indexOfFirst { it >= value }
            if (idx == -1) sortedFreqs.lastIndex else idx
          }

      Slider(
          value = currentIndex.toFloat(),
          onValueChange = { index ->
            val freq = sortedFreqs.getOrElse(index.toInt()) { sortedFreqs.last() }
            onValueChange(freq.toFloat())
          },
          onValueChangeFinished = onValueChangeFinished,
          valueRange = 0f..(sortedFreqs.size - 1).toFloat(),
          steps = if (sortedFreqs.size > 1) sortedFreqs.size - 2 else 0,
          colors =
              SliderDefaults.colors(
                  thumbColor = color,
                  activeTrackColor = color,
                  inactiveTrackColor = color.copy(alpha = 0.2f),
              ),
      )
    } else {
      // Continuous Slider fallback
      Slider(
          value = value,
          onValueChange = onValueChange,
          onValueChangeFinished = onValueChangeFinished,
          valueRange = range,
          steps = 10,
          colors =
              SliderDefaults.colors(
                  thumbColor = color,
                  activeTrackColor = color,
                  inactiveTrackColor = color.copy(alpha = 0.2f),
              ),
      )
    }
  }
}

@Composable
private fun GovernorSelector(currentGovernor: String, onClick: () -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(
        text = stringResource(R.string.cpu_governor),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
      Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
              imageVector = Icons.Rounded.Settings,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
          )
          Text(
              text = currentGovernor,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
          )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun CoreControl(cores: List<Int>, viewModel: TuningViewModel) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(
        text = stringResource(R.string.core_control),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Column(
        modifier =
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp),
                )
    ) {
      cores.forEachIndexed { index, coreNum ->
        if (coreNum != 0) {
          val coreEnabled by
              viewModel.preferencesManager.isCpuCoreEnabled(coreNum).collectAsState(initial = true)

          if (index > 0) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
          }

          Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                  text = "Core $coreNum",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
              )
              Text(
                  text = if (coreEnabled) "Online" else "Offline",
                  style = MaterialTheme.typography.bodySmall,
                  color =
                      if (coreEnabled) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.error,
              )
            }

            LottieSwitchControlled(
                checked = coreEnabled,
                onCheckedChange = { viewModel.disableCPUCore(coreNum, !it) },
                width = 50.dp,
                height = 25.dp,
                scale = 1.8f,
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GovernorSelectionDialog(
    governors: List<String>,
    selectedGovernor: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
  AlertDialog(
      onDismissRequest = onDismiss,
  ) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = stringResource(R.string.cpu_governor_dialog_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(governors) { governor ->
            val isSelected = governor == selectedGovernor

            Surface(
                onClick = { onSelect(governor) },
                shape = RoundedCornerShape(12.dp),
                color =
                    if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                    else Color.Transparent,
                border =
                    if (isSelected) null
                    else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
              Row(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                    text = governor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color =
                        if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurface,
                )

                if (isSelected) {
                  Icon(
                      imageVector = Icons.Rounded.Check,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onSecondaryContainer,
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
      }
    }
  }
}
