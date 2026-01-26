package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.model.ClusterInfo
import id.xms.xtrakernelmanager.data.model.CpuClusterLockConfig
import id.xms.xtrakernelmanager.data.model.LockPolicyType
import id.xms.xtrakernelmanager.data.model.ThermalPolicyPresets
import id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartFrequencyLockScreen(
    viewModel: TuningViewModel,
    onNavigateBack: () -> Unit
) {
    val clusters by viewModel.cpuClusters.collectAsState()
    
    // State for each cluster's frequency
    val clusterFrequencies = remember {
        mutableStateMapOf<Int, Pair<Int, Int>>().apply {
            clusters.forEach { cluster ->
                put(cluster.clusterNumber, Pair(cluster.currentMinFreq, cluster.currentMaxFreq))
            }
        }
    }
    
    var selectedPolicy by remember { mutableStateOf(LockPolicyType.SMART) }
    var selectedThermalPolicy by remember { mutableStateOf("PolicyB") }
    var selectedClusterForFreq by remember { mutableStateOf<ClusterInfo?>(null) }
    var isSelectingMin by remember { mutableStateOf(true) }
    
    // Box container with WavyBlobOrnament background
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Layer
        WavyBlobOrnament(
            modifier = Modifier.fillMaxSize()
        )
        
        // Foreground Layer
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Smart Frequency Lock",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                    ),
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Cluster Frequency Settings
                item {
                    Text(
                        text = "Cluster Frequencies",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
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
                            0 -> "Performance"
                            1 -> "Efficiency"
                            else -> "Cluster $index"
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
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Policy Selection
                item {
                    Text(
                        text = "Lock Policy",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                items(LockPolicyType.values().size) { index ->
                    val policy = LockPolicyType.values()[index]
                    PolicySelectionCard(
                        policy = policy,
                        isSelected = policy == selectedPolicy,
                        onSelect = { selectedPolicy = policy }
                    )
                }
                
                // Thermal Policy (only for SMART)
                if (selectedPolicy == LockPolicyType.SMART) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    item {
                        Text(
                            text = "Thermal Policy",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    items(ThermalPolicyPresets.getAllPolicies().size) { index ->
                        val policy = ThermalPolicyPresets.getAllPolicies()[index]
                        ThermalPolicyCard(
                            policy = policy,
                            isSelected = policy.name == selectedThermalPolicy,
                            onSelect = { selectedThermalPolicy = policy.name }
                        )
                    }
                }
                
                // Action Buttons
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel Button
                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        // Apply Lock Button
                        Button(
                            onClick = {
                                // Build lock configs from current state
                                val lockConfigs = clusterFrequencies.mapValues { (clusterNum, freqs) ->
                                    CpuClusterLockConfig(
                                        clusterId = clusterNum,
                                        minFreq = freqs.first,
                                        maxFreq = freqs.second
                                    )
                                }
                                
                                // Apply the lock
                                viewModel.lockCpuFrequencies(
                                    clusterConfigs = lockConfigs,
                                    policyType = selectedPolicy,
                                    thermalPolicy = if (selectedPolicy == LockPolicyType.SMART) {
                                        selectedThermalPolicy
                                    } else "PolicyB"
                                )
                                
                                onNavigateBack()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = "Apply Lock",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
    
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
