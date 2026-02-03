package id.xms.xtrakernelmanager.ui.screens.tuning.material.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.ClusterInfo
import id.xms.xtrakernelmanager.data.model.CpuClusterLockConfig
import id.xms.xtrakernelmanager.data.model.LockPolicyType
import id.xms.xtrakernelmanager.data.model.ThermalPolicyPresets
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialSmartFrequencyLockScreen(
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
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.liquid_smart_frequency_lock),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Header
            item {
                Text(
                    text = stringResource(R.string.material_cpu_cluster_frequencies),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Cluster Frequency Cards
            items(clusters.size) { index ->
                val cluster = clusters[index]
                val (minFreq, maxFreq) = clusterFrequencies[cluster.clusterNumber]
                    ?: Pair(cluster.currentMinFreq, cluster.currentMaxFreq)
                
                MaterialClusterFrequencyCard(
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
            
            // Policy Selection Section
            item {
                Text(
                    text = stringResource(R.string.material_cpu_lock_policy),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            items(LockPolicyType.values().size) { index ->
                val policy = LockPolicyType.values()[index]
                MaterialPolicySelectionCard(
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
                        text = stringResource(R.string.material_cpu_thermal_policy),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                items(ThermalPolicyPresets.getAllPolicies().size) { index ->
                    val policy = ThermalPolicyPresets.getAllPolicies()[index]
                    MaterialThermalPolicyCard(
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
                            text = stringResource(R.string.material_cpu_cancel),
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
                            text = stringResource(R.string.material_cpu_apply_lock),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
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
        
        MaterialFrequencySelectionDialog(
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
private fun MaterialClusterFrequencyCard(
    clusterIndex: Int,
    clusterName: String,
    minFreq: Int,
    maxFreq: Int,
    onMinClick: () -> Unit,
    onMaxClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Memory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = clusterName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cluster $clusterIndex",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            // Frequency Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Min Frequency
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onMinClick),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.material_gpu_min_frequency),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "$minFreq MHz",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Max Frequency
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onMaxClick),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.material_gpu_max_frequency),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "$maxFreq MHz",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialPolicySelectionCard(
    policy: LockPolicyType,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val (icon, title, description) = when (policy) {
        LockPolicyType.MANUAL -> Triple(
            Icons.Rounded.Settings,
            "Manual",
            "User-controlled, no thermal override"
        )
        LockPolicyType.SMART -> Triple(
            Icons.Rounded.AutoAwesome,
            "Smart",
            "Thermal-aware with smart overrides"
        )
        LockPolicyType.GAME -> Triple(
            Icons.Rounded.SportsEsports,
            "Game",
            "Game mode optimized"
        )
        LockPolicyType.BATTERY_SAVING -> Triple(
            Icons.Rounded.BatteryChargingFull,
            "Battery Saving",
            "Power-efficient"
        )
    }
    
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer
            else 
                MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        border = if (isSelected) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun MaterialThermalPolicyCard(
    policy: id.xms.xtrakernelmanager.data.model.ThermalPolicy,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val description = "Emergency: ${policy.emergencyThreshold}°C, Warning: ${policy.warningThreshold}°C"
    
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Thermostat,
                    contentDescription = null,
                    tint = if (isSelected)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = policy.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun MaterialFrequencySelectionDialog(
    title: String,
    availableFrequencies: List<Int>,
    currentFrequency: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableFrequencies.size) { index ->
                    val freq = availableFrequencies[index]
                    val isSelected = freq == currentFrequency
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(freq) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$freq MHz",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
