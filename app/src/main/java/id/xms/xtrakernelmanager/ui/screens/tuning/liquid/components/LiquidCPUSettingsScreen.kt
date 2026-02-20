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
import androidx.compose.runtime.snapshots.SnapshotStateMap
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
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.LottieSwitchControlled
import id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialog
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialogButton
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidToggle
import id.xms.xtrakernelmanager.ui.screens.tuning.ClusterUIState
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidCPUSettingsScreen(
    viewModel: TuningViewModel, 
    onNavigateBack: () -> Unit,
    onNavigateToSmartLock: () -> Unit
) {
  val clusters by viewModel.cpuClusters.collectAsState()
  val cpuCores by viewModel.cpuCores.collectAsState()
  val clusterStates by viewModel.clusterStates.collectAsState()

  // Box container with WavyBlobOrnament background
  Box(modifier = Modifier.fillMaxSize()) {
    WavyBlobOrnament(
        modifier = Modifier.fillMaxSize(),
        colors = listOf(
            Color(0xFF0D3B66),  
            Color(0xFF1A1B4B),  
            Color(0xFF0A2F51)   
        )
    )
    
    // Foreground Layer
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
          Surface(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 24.dp, vertical = 16.dp),
              shape = CircleShape,
              color = Color(0xFF3B82F6).copy(alpha = 0.15f) // Blue glass header
          ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
              }
              Text(
                  text = stringResource(R.string.cpu_control),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
              )
              Spacer(modifier = Modifier.width(48.dp))
            }
          }
        }
    ) { paddingValues ->
      Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
          if (clusters.isEmpty()) {
            item { EmptyState() }
          } else {
            // Smart Frequency Lock Section (Global)
            item {
              SmartFrequencyLockSection(
                  clusters = clusters,
                  viewModel = viewModel,
                  onNavigateToConfig = onNavigateToSmartLock
              )
            }
            
            // CPU Set on Boot Toggle
            item {
              LiquidCPUSetOnBootCard(viewModel = viewModel)
            }
            
            // Core Management Card
            item {
              LiquidCoreControl(
                  cores = cpuCores,
                  viewModel = viewModel
              )
            }
            
            // Cluster Cards
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
        
        Box(modifier = Modifier.padding(paddingValues)) {
          CpuLockNotificationOverlay(viewModel = viewModel)
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
        tint = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.6f),
    )
    Text(
        text = stringResource(R.string.cpu_no_clusters),
        style = MaterialTheme.typography.bodyLarge,
        color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.7f),
        textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun SmartFrequencyLockSection(
    clusters: List<ClusterInfo>,
    viewModel: TuningViewModel,
    onNavigateToConfig: () -> Unit
) {
  val isLocked by viewModel.isCpuFrequencyLocked.collectAsState()
  val lockStatus by viewModel.cpuLockStatus.collectAsState()
  
  Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(24.dp),
      color = Color(0xFF10B981).copy(alpha = 0.15f) // Green glass for smart lock section
  ) {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
              modifier = Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .background(
                      if (isLocked) Color(0xFF10B981).copy(alpha = 0.5f) // Stronger green glass when locked - more visible
                      else Color.White.copy(alpha = 0.2f) // More visible white glass when unlocked
                  ),
              contentAlignment = Alignment.Center
          ) {
            Icon(
                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = null,
                tint = if (isLocked) Color(0xFF10B981) else Color.White,
                modifier = Modifier.size(24.dp)
            )
          }
          
          Column {
            Text(
                text = stringResource(R.string.liquid_smart_frequency_lock),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = if (isLocked) stringResource(R.string.liquid_smart_lock_active) else stringResource(R.string.liquid_smart_lock_inactive),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isLocked) Color(0xFF10B981) else Color.White.copy(alpha = 0.8f) // More visible when inactive
            )
          }
        }
      }
      
      // Status Info
      if (isLocked) {
      HorizontalDivider(
          color = Color.White.copy(alpha = 0.3f)
      )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Policy Type
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
                text = stringResource(R.string.liquid_smart_lock_policy_type),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f) // More visible
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF3B82F6).copy(alpha = 0.5f)
            ) {
              Text(
                  text = lockStatus.policyType.name.replace("_", " "),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
          
          // Thermal Policy
          if (lockStatus.policyType == LockPolicyType.SMART) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                  text = stringResource(R.string.liquid_smart_lock_thermal_policy),
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color.White.copy(alpha = 0.8f)
              )
              Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color(0xFF8B5CF6).copy(alpha = 0.5f)
              ) {
                Text(
                    text = lockStatus.thermalPolicy,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
              }
            }
          }
          
          // Locked Clusters
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
                text = stringResource(R.string.liquid_smart_lock_locked_clusters),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = stringResource(R.string.liquid_smart_lock_clusters_format, lockStatus.clusterCount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
          }
        }
      }
      
      HorizontalDivider(
          color = Color.White.copy(alpha = 0.3f)
      )
      
      // Action Buttons
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        if (isLocked) {
          // Unlock Button
          Button(
              onClick = { viewModel.unlockCpuFrequencies() },
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFFEF4444)
              ),
              shape = RoundedCornerShape(16.dp)
          ) {
            Icon(
                imageVector = Icons.Default.LockOpen,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.liquid_smart_lock_unlock),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
          }
          
          // Reconfigure Button
          Button(
              onClick = onNavigateToConfig,
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFF3B82F6)
              ),
              shape = RoundedCornerShape(16.dp)
          ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.liquid_smart_lock_edit),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
          }
        } else {
          // Configure Lock Button
          Button(
              onClick = onNavigateToConfig,
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFF10B981)
              ),
              shape = RoundedCornerShape(16.dp)
          ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.liquid_smart_lock_configure_lock),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SmartFrequencyLockDialog(
    clusters: List<ClusterInfo>,
    selectedPolicy: LockPolicyType,
    selectedThermalPolicy: String,
    onPolicySelected: (LockPolicyType) -> Unit,
    onThermalPolicySelected: (String) -> Unit,
    onConfirm: (Map<Int, CpuClusterLockConfig>) -> Unit,
    onDismiss: () -> Unit
) {
  // State for each cluster's frequency
  val clusterFrequencies = remember {
    mutableStateMapOf<Int, Pair<Int, Int>>().apply {
      clusters.forEach { cluster ->
        put(cluster.clusterNumber, Pair(cluster.currentMinFreq, cluster.currentMaxFreq))
      }
    }
  }
  
  var selectedClusterForFreq by remember { mutableStateOf<ClusterInfo?>(null) }
  var isSelectingMin by remember { mutableStateOf(true) }
  
  LiquidDialog(
      onDismissRequest = onDismiss,
      title = "Smart Frequency Lock",
      content = {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Cluster Frequency Settings
          item {
            Text(
                text = stringResource(R.string.liquid_smart_lock_cluster_frequencies),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
          }
          
          items(clusters.size) { index ->
            val cluster = clusters[index]
            val (minFreq, maxFreq) = clusterFrequencies[cluster.clusterNumber] 
                ?: Pair(cluster.currentMinFreq, cluster.currentMaxFreq)
            
            ClusterFrequencyCard(
                clusterIndex = index,
                clusterName = when (index) {
                  0 -> stringResource(R.string.liquid_smart_lock_performance)
                  1 -> stringResource(R.string.liquid_smart_lock_efficiency)
                  else -> stringResource(R.string.liquid_smart_lock_cluster_index_format, index)
                },
                minFreq = minFreq,
                maxFreq = maxFreq,
                onMinClick = {
                  selectedClusterForFreq = cluster
                  isSelectingMin = true
                },
                onMaxClick = {
                  selectedClusterForFreq = cluster
                  isSelectingMin = false
                }
            )
          }
          
          item {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
          }
          
          // Policy Selection
          item {
            Text(
                text = stringResource(R.string.liquid_smart_lock_lock_policy),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
          }
          
          items(LockPolicyType.values().size) { index ->
            val policy = LockPolicyType.values()[index]
            PolicySelectionCard(
                policy = policy,
                isSelected = policy == selectedPolicy,
                onSelect = { onPolicySelected(policy) }
            )
          }
        }
      },
      confirmButton = {
        LiquidDialogButton(
            text = stringResource(R.string.liquid_smart_lock_lock_all_clusters),
            onClick = {
              val configs = clusterFrequencies.map { (clusterNum, freqs) ->
                clusterNum to CpuClusterLockConfig(
                    clusterId = clusterNum,
                    minFreq = freqs.first,
                    maxFreq = freqs.second
                )
              }.toMap()
              onConfirm(configs)
            },
            isPrimary = true
        )
      },
      dismissButton = {
        LiquidDialogButton(
            text = "Cancel",
            onClick = onDismiss,
            isPrimary = false
        )
      }
  )
  
  // Frequency Selection Dialog
  selectedClusterForFreq?.let { cluster ->
    val currentFreq = if (isSelectingMin) {
      clusterFrequencies[cluster.clusterNumber]?.first ?: cluster.currentMinFreq
    } else {
      clusterFrequencies[cluster.clusterNumber]?.second ?: cluster.currentMaxFreq
    }
    
    FrequencySelectionDialog(
        title = if (isSelectingMin) "Min Frequency - Cluster ${cluster.clusterNumber}"
                else "Max Frequency - Cluster ${cluster.clusterNumber}",
        availableFrequencies = cluster.availableFrequencies,
        currentFrequency = currentFreq,
        onDismiss = { selectedClusterForFreq = null },
        onSelect = { selectedFreq ->
          val current = clusterFrequencies[cluster.clusterNumber] 
              ?: Pair(cluster.currentMinFreq, cluster.currentMaxFreq)
          
          if (isSelectingMin) {
            var newMin = selectedFreq
            var newMax = current.second
            if (newMin > newMax) newMax = newMin
            clusterFrequencies[cluster.clusterNumber] = Pair(newMin, newMax)
          } else {
            var newMin = current.first
            var newMax = selectedFreq
            if (newMax < newMin) newMin = newMax
            clusterFrequencies[cluster.clusterNumber] = Pair(newMin, newMax)
          }
          
          selectedClusterForFreq = null
        }
    )
  }
}

@Composable
internal fun ClusterFrequencyCard(
    clusterIndex: Int,
    clusterName: String,
    minFreq: Int,
    maxFreq: Int,
    onMinClick: () -> Unit,
    onMaxClick: () -> Unit
) {
  // Determine cluster-specific color
  val clusterColor = when (clusterIndex) {
    0 -> Color(0xFF10B981)
    1 -> Color(0xFF3B82F6) 
    else -> Color(0xFF8B5CF6)
  }
  
  Surface(
      shape = RoundedCornerShape(16.dp),
      color = clusterColor.copy(alpha = 0.15f),
  ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Header
      Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(clusterColor.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
          Text(
              text = "C$clusterIndex",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = clusterColor
          )
        }
        Text(
            text = clusterName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
      }
      
      // Frequency Cards
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Min Frequency
        Surface(
            onClick = onMinClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF10B981).copy(alpha = 0.15f),
        ) {
          Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
                text = stringResource(R.string.liquid_smart_lock_min),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = "$minFreq",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
            Text(
                text = stringResource(R.string.liquid_smart_lock_mhz),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF10B981).copy(alpha = 0.8f)
            )
          }
        }
        
        // Max Frequency
        Surface(
            onClick = onMaxClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
        ) {
          Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
                text = stringResource(R.string.liquid_smart_lock_max),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = "$maxFreq",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5CF6)
            )
            Text(
                text = stringResource(R.string.liquid_smart_lock_mhz),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8B5CF6).copy(alpha = 0.8f)
            )
          }
        }
      }
    }
  }
}

@Composable
internal fun PolicySelectionCard(
    policy: LockPolicyType,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
  Surface(
      onClick = onSelect,
      shape = RoundedCornerShape(12.dp),
      color = if (isSelected)
        Color(0xFF3B82F6).copy(alpha = 0.5f)
      else
        Color.White.copy(alpha = 0.1f),
  ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
      RadioButton(
          selected = isSelected,
          onClick = onSelect
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = policy.name.replace("_", " "),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = when (policy) {
              LockPolicyType.MANUAL -> stringResource(R.string.liquid_lock_policy_manual_desc)
              LockPolicyType.SMART -> stringResource(R.string.liquid_lock_policy_smart_desc)
              LockPolicyType.GAME -> stringResource(R.string.liquid_lock_policy_game_desc)
              LockPolicyType.BATTERY_SAVING -> stringResource(R.string.liquid_lock_policy_battery_saving_desc)
            },
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
      }
    }
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
  var isExpanded by remember { mutableStateOf(true) }

  // Cluster-specific colors
  val clusterColor = when (clusterIndex) {
    0 -> Color(0xFF10B981)
    1 -> Color(0xFF3B82F6) 
    else -> Color(0xFF8B5CF6)
  }

  // Sync state
    val isUserAdjusting by viewModel.isUserAdjusting().collectAsState()
    
    LaunchedEffect(currentMinFreq, currentMaxFreq) {
        // Sync state
        if (!isUserAdjusting) {
            minFreqSlider = currentMinFreq
            maxFreqSlider = currentMaxFreq
        }
    }

  // Colored Glassmorphic Card
  Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(24.dp),
      color = clusterColor.copy(alpha = 0.15f)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Cluster Header
      Row(
          modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded }.padding(20.dp),
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
                      .background(clusterColor.copy(alpha = 0.3f)),
              contentAlignment = Alignment.Center,
          ) {
            Text(
                text = "C${clusterIndex}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
          }

          Column {
            Text(
                text = clusterTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = "${cluster.minFreq} MHz - ${cluster.maxFreq} MHz",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
            )
          }
        }

        IconButton(onClick = { isExpanded = !isExpanded }) {
          Icon(
              imageVector = Icons.Rounded.KeyboardArrowDown,
              contentDescription = "Expand",
              tint = Color.White,
              modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
          )
        }
      }

      AnimatedVisibility(visible = isExpanded) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
          HorizontalDivider(color = Color.White.copy(alpha = 0.3f))

          // Frequency Controls - Clickable Cards
          var showMinFreqDialog by remember { mutableStateOf(false) }
          var showMaxFreqDialog by remember { mutableStateOf(false) }
          
          FrequencyClickableCard(
              label = stringResource(R.string.min_frequency),
              value = minFreqSlider.toInt(),
              color = Color(0xFF10B981),
              onClick = { showMinFreqDialog = true }
          )

          FrequencyClickableCard(
              label = stringResource(R.string.max_frequency),
              value = maxFreqSlider.toInt(),
              color = Color(0xFF8B5CF6),
              onClick = { showMaxFreqDialog = true }
          )
          
          // Min Frequency Dialog
          if (showMinFreqDialog) {
            FrequencySelectionDialog(
                title = stringResource(R.string.min_frequency),
                availableFrequencies = cluster.availableFrequencies,
                currentFrequency = minFreqSlider.toInt(),
                onDismiss = { showMinFreqDialog = false },
                onSelect = { selectedFreq ->
                  minFreqSlider = selectedFreq.toFloat()
                  viewModel.updateClusterUIState(cluster.clusterNumber, minFreqSlider, maxFreqSlider)
                  viewModel.setCPUFrequency(
                      cluster.clusterNumber,
                      minFreqSlider.toInt(),
                      maxFreqSlider.toInt(),
                  )
                  showMinFreqDialog = false
                }
            )
          }
          
          // Max Frequency Dialog
          if (showMaxFreqDialog) {
            FrequencySelectionDialog(
                title = stringResource(R.string.max_frequency),
                availableFrequencies = cluster.availableFrequencies,
                currentFrequency = maxFreqSlider.toInt(),
                onDismiss = { showMaxFreqDialog = false },
                onSelect = { selectedFreq ->
                  maxFreqSlider = selectedFreq.toFloat()
                  viewModel.updateClusterUIState(cluster.clusterNumber, minFreqSlider, maxFreqSlider)
                  viewModel.setCPUFrequency(
                      cluster.clusterNumber,
                      minFreqSlider.toInt(),
                      maxFreqSlider.toInt(),
                  )
                  showMaxFreqDialog = false
                }
            )
          }

          // Governor
          GovernorSelector(
              currentGovernor = currentGovernor,
              onClick = { showGovernorDialog = true },
          )
        }
      }
    }
  }

  if (showGovernorDialog) {
    GovernorSelectionLiquidDialog(
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
private fun FrequencyClickableCard(
    label: String,
    value: Int,
    color: Color,
    onClick: () -> Unit
) {
  Surface(
      onClick = onClick,
      shape = RoundedCornerShape(16.dp),
      color = color.copy(alpha = 0.12f),
      modifier = Modifier.fillMaxWidth()
  ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(
          verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.7f),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
          Text(
              text = "$value",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = color,
          )
          Text(
              text = stringResource(R.string.liquid_smart_lock_mhz),
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium,
              color = color.copy(alpha = 0.7f),
              modifier = Modifier.padding(bottom = 4.dp)
          )
        }
      }
      
      Box(
          modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(color.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
      ) {
        Icon(
            imageVector = Icons.Rounded.Tune,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
      }
    }
  }
}

@Composable
internal fun FrequencySelectionDialog(
    title: String,
    availableFrequencies: List<Int>,
    currentFrequency: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
  val sortedFreqs = remember(availableFrequencies) { 
    availableFrequencies.sorted().reversed()
  }
  
  LiquidDialog(
      onDismissRequest = onDismiss,
      title = title,
      content = {
        if (sortedFreqs.isEmpty()) {
          Text(
              text = stringResource(R.string.liquid_smart_lock_no_frequencies_available),
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White.copy(alpha = 0.7f),
              textAlign = TextAlign.Center,
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 16.dp)
          )
        } else {
          LazyColumn(
              modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(max = 400.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(sortedFreqs.size) { index ->
              val freq = sortedFreqs[index]
              val isSelected = freq == currentFrequency
              
              Surface(
                  onClick = { onSelect(freq) },
                  shape = RoundedCornerShape(12.dp),
                  color = if (isSelected) 
                      Color(0xFF10B981).copy(alpha = 0.2f)
                  else 
                      Color.White.copy(alpha = 0.08f),
              ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                  Row(
                      horizontalArrangement = Arrangement.spacedBy(12.dp),
                      verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                        text = "$freq",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) 
                            Color(0xFF10B981)
                        else 
                            Color.White,
                    )
                    Text(
                        text = stringResource(R.string.liquid_smart_lock_mhz),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) 
                            Color(0xFF10B981).copy(alpha = 0.8f)
                        else 
                            Color.White.copy(alpha = 0.7f),
                    )
                  }
                  
                  if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {
        LiquidDialogButton(
            text = stringResource(R.string.cancel),
            onClick = onDismiss,
            isPrimary = false
        )
      }
  )
}

@Composable
private fun GovernorSelector(currentGovernor: String, onClick: () -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(
        text = stringResource(R.string.cpu_governor),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = Color.White.copy(alpha = 0.7f),
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF3B82F6).copy(alpha = 0.12f),
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
              tint = Color(0xFF3B82F6),
          )
          Text(
              text = currentGovernor,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = Color.White
          )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
        )
      }
    }
  }
}

@Composable
private fun GovernorSelectionLiquidDialog(
    governors: List<String>,
    selectedGovernor: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
  LiquidDialog(
      onDismissRequest = onDismiss,
      title = stringResource(R.string.cpu_governor_dialog_title),
      content = {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(governors.size) { index ->
            val governor = governors[index]
            val isSelected = governor == selectedGovernor

            Surface(
                onClick = { onSelect(governor) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) 
                    Color(0xFF3B82F6).copy(alpha = 0.2f)
                else 
                    Color.White.copy(alpha = 0.08f),
            ) {
              Row(
                  modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 20.dp, vertical = 16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                    text = governor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) 
                        Color(0xFF3B82F6)
                    else 
                        Color.White,
                )

                if (isSelected) {
                  Icon(
                      imageVector = Icons.Rounded.CheckCircle,
                      contentDescription = null,
                      tint = Color(0xFF3B82F6),
                      modifier = Modifier.size(24.dp)
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {
        LiquidDialogButton(
            text = stringResource(R.string.cancel),
            onClick = onDismiss,
            isPrimary = false
        )
      }
  )
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
          text = if (isLocked) stringResource(R.string.liquid_smart_lock_frequency_locked) else stringResource(R.string.liquid_smart_lock_frequency_unlocked),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = if (isLocked) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurface
        )
        
        if (isLocked) {
          Text(
            text = "${lockStatus.policyType.name} • ${lockStatus.thermalPolicy}",
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
          text = if (isLocked) stringResource(R.string.liquid_smart_lock_unlock) else stringResource(R.string.liquid_smart_lock_lock)
        )
      }
    }
    
    // Show policy indicator if locked
    if (isLocked) {
      LockPolicyIndicator(
        policyType = lockStatus.policyType,
        modifier = Modifier.align(Alignment.End)
      )
    }
  }
  
  // Lock Configuration Dialog
  if (showLockDialog) {
    CPULockLiquidDialog(
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

@Composable
private fun CPULockLiquidDialog(
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
  var showMinFreqDialog by remember { mutableStateOf(false) }
  var showMaxFreqDialog by remember { mutableStateOf(false) }
  
  LiquidDialog(
    onDismissRequest = onDismiss,
    title = "Lock Cluster ${cluster.clusterNumber}",
    content = {
      Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        // Frequency Selection Cards
        Text(
          text = stringResource(R.string.liquid_smart_lock_frequency_range),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        
        // Min Frequency Card
        Surface(
          onClick = { showMinFreqDialog = true },
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = stringResource(R.string.liquid_smart_lock_min_frequency),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "$lockMinFreq MHz",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }
        
        // Max Frequency Card
        Surface(
          onClick = { showMaxFreqDialog = true },
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = stringResource(R.string.liquid_smart_lock_max_frequency),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "$lockMaxFreq MHz",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
              )
            }
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.tertiary
            )
          }
        }
        
        HorizontalDivider(
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
        
        // Policy Selection
        Text(
          text = stringResource(R.string.liquid_smart_lock_lock_policy),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          LockPolicyType.values().forEach { policy ->
            Surface(
              onClick = { onPolicySelected(policy) },
              shape = RoundedCornerShape(12.dp),
              color = if (policy == selectedPolicy)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
              else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
              border = if (policy == selectedPolicy)
                BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
              else
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                RadioButton(
                  selected = policy == selectedPolicy,
                  onClick = { onPolicySelected(policy) }
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = policy.name.replace("_", " "),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (policy == selectedPolicy) FontWeight.Bold else FontWeight.Medium
                  )
                  Text(
                    text = when (policy) {
                      LockPolicyType.MANUAL -> stringResource(R.string.liquid_lock_policy_manual_desc)
                      LockPolicyType.SMART -> stringResource(R.string.liquid_lock_policy_smart_desc)
                      LockPolicyType.GAME -> stringResource(R.string.liquid_lock_policy_game_desc)
                      LockPolicyType.BATTERY_SAVING -> stringResource(R.string.liquid_lock_policy_battery_saving_desc)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
        
        // Thermal Policy Selection (only for SMART policy)
        if (selectedPolicy == LockPolicyType.SMART) {
          HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
          )
          
          Text(
            text = stringResource(R.string.liquid_smart_lock_thermal_policy_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          
          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            ThermalPolicyPresets.getAllPolicies().forEach { policy ->
              Surface(
                onClick = { onThermalPolicySelected(policy.name) },
                shape = RoundedCornerShape(12.dp),
                color = if (policy.name == selectedThermalPolicy)
                  MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                else
                  MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                border = if (policy.name == selectedThermalPolicy)
                  BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
                else
                  BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  RadioButton(
                    selected = policy.name == selectedThermalPolicy,
                    onClick = { onThermalPolicySelected(policy.name) }
                  )
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = policy.name,
                      style = MaterialTheme.typography.bodyLarge,
                      fontWeight = if (policy.name == selectedThermalPolicy) FontWeight.Bold else FontWeight.Medium
                    )
                    Text(
                      text = stringResource(R.string.liquid_thermal_policy_format, policy.emergencyThreshold, policy.warningThreshold),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      LiquidDialogButton(
        text = stringResource(R.string.liquid_smart_lock_lock_frequencies),
        onClick = {
          onConfirm(lockMinFreq, lockMaxFreq)
        },
        isPrimary = true
      )
    },
    dismissButton = {
      LiquidDialogButton(
        text = "Cancel",
        onClick = onDismiss,
        isPrimary = false
      )
    }
  )
  
  // Min Frequency Selection Dialog
  if (showMinFreqDialog) {
    FrequencySelectionDialog(
      title = stringResource(R.string.liquid_smart_lock_select_min_frequency),
      availableFrequencies = availableFrequencies,
      currentFrequency = lockMinFreq,
      onDismiss = { showMinFreqDialog = false },
      onSelect = { selectedFreq ->
        lockMinFreq = selectedFreq
        if (lockMinFreq > lockMaxFreq) {
          lockMaxFreq = lockMinFreq
        }
        showMinFreqDialog = false
      }
    )
  }
  
  // Max Frequency Selection Dialog
  if (showMaxFreqDialog) {
    FrequencySelectionDialog(
      title = stringResource(R.string.liquid_smart_lock_select_max_frequency),
      availableFrequencies = availableFrequencies,
      currentFrequency = lockMaxFreq,
      onDismiss = { showMaxFreqDialog = false },
      onSelect = { selectedFreq ->
        lockMaxFreq = selectedFreq
        if (lockMaxFreq < lockMinFreq) {
          lockMinFreq = lockMaxFreq
        }
        showMaxFreqDialog = false
      }
    )
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

@Composable
private fun LiquidCPUSetOnBootCard(viewModel: TuningViewModel) {
  val cpuSetOnBoot by viewModel.preferencesManager.getCpuSetOnBoot().collectAsState(initial = false)

  Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      color = Color(0xFF8B5CF6).copy(alpha = 0.15f) // Purple glass for set on boot
  ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f)
      ) {
        Icon(
            imageVector = Icons.Rounded.PowerSettingsNew,
            contentDescription = null,
            tint = if (cpuSetOnBoot) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Column {
          Text(
              text = stringResource(R.string.set_on_boot),
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium,
              color = Color.White
          )
          Text(
              text = stringResource(R.string.apply_cpu_on_boot_desc),
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.7f)
          )
        }
      }
      
      LiquidToggle(
          checked = cpuSetOnBoot,
          onCheckedChange = { enabled -> viewModel.setCpuSetOnBoot(enabled) }
      )
    }
  }
}