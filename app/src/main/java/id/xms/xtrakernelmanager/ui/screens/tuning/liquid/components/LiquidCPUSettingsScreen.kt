package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.DragInteraction
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
import id.xms.xtrakernelmanager.data.model.CpuClusterLockConfig
import id.xms.xtrakernelmanager.data.model.LockPolicyType
import id.xms.xtrakernelmanager.data.model.ThermalPolicyPresets
import id.xms.xtrakernelmanager.ui.components.LottieSwitchControlled
import id.xms.xtrakernelmanager.ui.screens.tuning.ClusterUIState
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidCPUSettingsScreen(viewModel: TuningViewModel, onNavigateBack: () -> Unit) {
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
    Box(modifier = Modifier.fillMaxSize()) {
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
      
      Box(modifier = Modifier.padding(paddingValues)) {
        CpuLockNotificationOverlay(viewModel = viewModel)
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
    val isUserAdjusting by viewModel.isUserAdjusting().collectAsState()
    
    LaunchedEffect(currentMinFreq, currentMaxFreq) {
        // Sync state
        if (!isUserAdjusting) {
            minFreqSlider = currentMinFreq
            maxFreqSlider = currentMaxFreq
        }
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
                text = "${cluster.minFreq} MHz - ${cluster.maxFreq} MHz",
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
              onValueChangeStarted = {
                viewModel.startUserAdjusting()
              },
              onValueChangeFinished = {
                viewModel.endUserAdjusting()
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
              onValueChangeStarted = {
                viewModel.startUserAdjusting()
              },
              onValueChangeFinished = {
                viewModel.setCPUFrequency(
                    cluster.clusterNumber,
                    minFreqSlider.toInt(),
                    maxFreqSlider.toInt(),
                )
                viewModel.endUserAdjusting()
              },
          )

          // Governor
          GovernorSelector(
              currentGovernor = currentGovernor,
              onClick = { showGovernorDialog = true },
          )

          // Cores - Fixed positioning
          if (cluster.cores.any { it != 0 }) {
            FixedCoreControlSection(cluster.cores, viewModel)
          }
          
          // CPU Lock Controls
          CPULockControls(
            cluster = cluster,
            clusterIndex = clusterIndex,
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth()
          )
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
    onValueChangeStarted: () -> Unit = {},
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
    
    val interactionSource = remember { MutableInteractionSource() }
    
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction: Interaction ->
            when (interaction) {
                is androidx.compose.foundation.interaction.PressInteraction.Press,
                is androidx.compose.foundation.interaction.DragInteraction.Start -> {
                    onValueChangeStarted()
                }
                is androidx.compose.foundation.interaction.PressInteraction.Release,
                is androidx.compose.foundation.interaction.PressInteraction.Cancel,
                is androidx.compose.foundation.interaction.DragInteraction.Stop,
                is androidx.compose.foundation.interaction.DragInteraction.Cancel -> {
                    onValueChangeFinished()
                }
            }
        }
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
            // onValueChangeStarted handled by interactionSource
            val freq = sortedFreqs.getOrElse(index.toInt()) { sortedFreqs.last() }
            onValueChange(freq.toFloat())
          },
          // onValueChangeFinished handled by interactionSource
          valueRange = 0f..(sortedFreqs.size - 1).toFloat(),
          steps = if (sortedFreqs.size > 1) sortedFreqs.size - 2 else 0,
          interactionSource = interactionSource,
          colors = SliderDefaults.colors(
              thumbColor = color,
              activeTrackColor = color,
              inactiveTrackColor = color.copy(alpha = 0.2f),
          )
      )
    } else {
      // Continuous Slider fallback
      Slider(
          value = value,
          onValueChange = onValueChange,
          // onValueChangeFinished handled by interactionSource
          valueRange = range,
          steps = 10,
          interactionSource = interactionSource,
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
private fun FixedCoreControlSection(
    cores: List<Int>, 
    viewModel: TuningViewModel
) {
  Column(
      modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceContainerLow)
          .border(
              1.dp,
              MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
              RoundedCornerShape(12.dp),
          )
          .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text(
        text = "Core Control",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    
    // Fixed layout grid for cores
    cores.forEachIndexed { index, coreNum ->
      if (coreNum != 0) {
        if (index > 0) {
          HorizontalDivider(
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
              modifier = Modifier.padding(vertical = 4.dp)
          )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(
              modifier = Modifier.weight(1f)
          ) {
            Text(
                text = "Core $coreNum",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (coreNum == 0) "Performance Core" else "Efficiency Core",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          
          val coreEnabled by viewModel.preferencesManager.isCpuCoreEnabled(coreNum).collectAsState(initial = true)
          Switch(
              checked = coreEnabled,
              onCheckedChange = { enabled ->
                viewModel.setCpuCoreEnabled(coreNum, !enabled)
              },
              modifier = Modifier
                  .width(60.dp),
              enabled = coreNum != 0 // Don't allow disabling core 0
          )
        }
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

@Composable
private fun CPULockControls(
    cluster: ClusterInfo,
    clusterIndex: Int,
    viewModel: TuningViewModel,
    modifier: Modifier = Modifier
) {
  val isLocked by viewModel.isCpuFrequencyLocked.collectAsState()
  val lockStatus by viewModel.cpuLockStatus.collectAsState()
  
  var showLockDialog by remember { mutableStateOf(false) }
  var selectedPolicy by remember { mutableStateOf(LockPolicyType.SMART) }
  var selectedThermalPolicy by remember { mutableStateOf("PolicyB") }
  
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text(
      text = "Smart Frequency Lock",
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Lock Status Indicator
      SmartLockIndicator(
        modifier = Modifier.size(40.dp),
        viewModel = viewModel,
        onLockClick = { showLockDialog = true },
        onUnlockClick = { viewModel.unlockCpuFrequencies() }
      )
      
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = if (isLocked) "Frequency Locked" else "Frequency Unlocked",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = if (isLocked) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurface
        )
        
        if (isLocked && lockStatus != null) {
          Text(
            text = "${lockStatus!!.policyType.name} • ${lockStatus!!.thermalPolicy}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
      
      // Lock/Unlock Button
      Button(
        onClick = { 
          if (isLocked) {
            viewModel.unlockCpuFrequencies()
          } else {
            showLockDialog = true
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isLocked) MaterialTheme.colorScheme.error
                          else MaterialTheme.colorScheme.primary
        )
      ) {
        Icon(
          imageVector = if (isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (isLocked) "Unlock" else "Lock"
        )
      }
    }
    
    // Show policy indicator if locked
    if (isLocked && lockStatus != null) {
      LockPolicyIndicator(
        policyType = lockStatus!!.policyType,
        modifier = Modifier.align(Alignment.End)
      )
    }
  }
  
  // Lock Configuration Dialog
  if (showLockDialog) {
    CPULockDialog(
      cluster = cluster,
      currentMinFreq = cluster.currentMinFreq,
      currentMaxFreq = cluster.currentMaxFreq,
      availableFrequencies = cluster.availableFrequencies,
      selectedPolicy = selectedPolicy,
      selectedThermalPolicy = selectedThermalPolicy,
      onPolicySelected = { selectedPolicy = it },
      onThermalPolicySelected = { selectedThermalPolicy = it },
      onConfirm = { minFreq, maxFreq ->
        val clusterConfig = CpuClusterLockConfig(
          clusterId = cluster.clusterNumber,
          minFreq = minFreq,
          maxFreq = maxFreq
        )
        viewModel.lockCpuFrequencies(
          clusterConfigs = mapOf(cluster.clusterNumber to clusterConfig),
          policyType = selectedPolicy,
          thermalPolicy = selectedThermalPolicy
        )
        showLockDialog = false
      },
      onDismiss = { showLockDialog = false }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CPULockDialog(
  cluster: ClusterInfo,
  currentMinFreq: Int,
  currentMaxFreq: Int,
  availableFrequencies: List<Int>,
  selectedPolicy: LockPolicyType,
  selectedThermalPolicy: String,
  onPolicySelected: (LockPolicyType) -> Unit,
  onThermalPolicySelected: (String) -> Unit,
  onConfirm: (Int, Int) -> Unit,
  onDismiss: () -> Unit
) {
  var lockMinFreq by remember { mutableIntStateOf(currentMinFreq) }
  var lockMaxFreq by remember { mutableIntStateOf(currentMaxFreq) }
  
  AlertDialog(
    onDismissRequest = onDismiss
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text(
          text = "Lock Cluster ${cluster.clusterNumber} Frequencies",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold
        )
        
        // Frequency Selection
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Frequency Range",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
          )
          
          // Min Frequency
          Text(
            text = "Min: ${lockMinFreq} MHz",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          
          if (availableFrequencies.isNotEmpty()) {
            val sortedFreqs = availableFrequencies.sorted()
            val currentIndex = sortedFreqs.indexOfFirst { it >= lockMinFreq }
              .coerceAtLeast(0)
            
            Slider(
              value = currentIndex.toFloat(),
              onValueChange = { index ->
                lockMinFreq = sortedFreqs.getOrElse(index.toInt()) { sortedFreqs.last() }
                if (lockMinFreq > lockMaxFreq) {
                  lockMaxFreq = lockMinFreq
                }
              },
              valueRange = 0f..(sortedFreqs.size - 1).toFloat(),
              steps = if (sortedFreqs.size > 1) sortedFreqs.size - 2 else 0
            )
          }
          
          // Max Frequency
          Text(
            text = "Max: ${lockMaxFreq} MHz",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          
          if (availableFrequencies.isNotEmpty()) {
            val sortedFreqs = availableFrequencies.sorted()
            val currentIndex = sortedFreqs.indexOfFirst { it >= lockMaxFreq }
              .coerceAtLeast(0)
            
            Slider(
              value = currentIndex.toFloat(),
              onValueChange = { index ->
                lockMaxFreq = sortedFreqs.getOrElse(index.toInt()) { sortedFreqs.last() }
                if (lockMaxFreq < lockMinFreq) {
                  lockMinFreq = lockMaxFreq
                }
              },
              valueRange = 0f..(sortedFreqs.size - 1).toFloat(),
              steps = if (sortedFreqs.size > 1) sortedFreqs.size - 2 else 0
            )
          }
        }
        
        // Policy Selection
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Lock Policy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
          )
          
          LockPolicyType.values().forEach { policy ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onPolicySelected(policy) }
                .padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = policy == selectedPolicy,
                onClick = { onPolicySelected(policy) }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = policy.name.replace("_", " "),
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = if (policy == selectedPolicy) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                  text = when (policy) {
                    LockPolicyType.MANUAL -> "User-controlled, no thermal override"
                    LockPolicyType.SMART -> "Thermal-aware with auto-restore (Recommended)"
                    LockPolicyType.GAME -> "Optimized for gaming performance"
                    LockPolicyType.BATTERY_SAVING -> "Power-efficient configuration"
                  },
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
        
        // Thermal Policy Selection (only for SMART policy)
        if (selectedPolicy == LockPolicyType.SMART) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "Thermal Policy",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Medium
            )
            
            ThermalPolicyPresets.getAllPolicies().forEach { policy ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { onThermalPolicySelected(policy.name) }
                  .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = policy.name == selectedThermalPolicy,
                  onClick = { onThermalPolicySelected(policy.name) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = policy.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (policy.name == selectedThermalPolicy) FontWeight.Bold else FontWeight.Normal
                  )
                  Text(
                    text = "Emergency: ${policy.emergencyThreshold}°C, Warning: ${policy.warningThreshold}°C",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
        
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              onConfirm(lockMinFreq, lockMaxFreq)
            },
            enabled = lockMinFreq <= lockMaxFreq
          ) {
            Text("Lock Frequencies")
          }
        }
      }
    }
  }
}


@Composable
fun CpuLockNotificationOverlay(viewModel: TuningViewModel) {
    val notification by viewModel.cpuLockNotifications.collectAsState()

    AnimatedVisibility(
        visible = notification != null,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = when {
                    notification?.contains("success") == true -> Color(0xFF4CAF50)
                    notification?.contains("failed") == true -> MaterialTheme.colorScheme.error
                    notification?.contains("override") == true -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.secondaryContainer
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = when {
                        notification?.contains("success") == true -> Icons.Filled.CheckCircle
                        notification?.contains("failed") == true -> Icons.Filled.Error
                        notification?.contains("override") == true -> Icons.Filled.Warning
                        else -> Icons.Filled.Info
                    },
                    contentDescription = null,
                    tint = Color.White
                )
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = notification ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    
                    Text(
                        text = "Tap to dismiss",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                
                IconButton(
                    onClick = { viewModel.clearCpuLockNotification() }
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
