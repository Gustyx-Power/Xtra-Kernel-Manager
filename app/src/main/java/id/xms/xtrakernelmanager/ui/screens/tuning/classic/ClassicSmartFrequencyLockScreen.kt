package id.xms.xtrakernelmanager.ui.screens.tuning.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicSmartFrequencyLockScreen(
    viewModel: TuningViewModel,
    onNavigateBack: () -> Unit
) {
    val clusters by viewModel.cpuClusters.collectAsState()
    val currentThermalPolicy by viewModel.getCpuLockThermalPolicy().collectAsState()
    
    // State for each cluster's frequency
    val clusterFrequencies = remember {
        mutableStateMapOf<Int, Pair<Int, Int>>().apply {
            clusters.forEach { cluster ->
                put(cluster.clusterNumber, Pair(cluster.currentMinFreq, cluster.currentMaxFreq))
            }
        }
    }
    
    var selectedPolicy by remember { mutableStateOf(LockPolicyType.SMART) }
    var selectedClusterForFreq by remember { mutableStateOf<ClusterInfo?>(null) }
    var isSelectingMin by remember { mutableStateOf(true) }
    
    Scaffold(
        containerColor = ClassicColors.Background,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ClassicColors.Surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = ClassicColors.OnSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Smart Frequency Lock",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Header Card
            item {
                ClassicInfoHeaderCard()
            }
            
            // Section: Cluster Frequencies
            item {
                Text(
                    text = "CLUSTER FREQUENCIES",
                    style = MaterialTheme.typography.labelMedium,
                    color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            
            // Cluster Frequency Cards
            items(clusters.size) { index ->
                val cluster = clusters[index]
                val (minFreq, maxFreq) = clusterFrequencies[cluster.clusterNumber]
                    ?: Pair(cluster.currentMinFreq, cluster.currentMaxFreq)
                
                ClassicClusterFrequencyCard(
                    cluster = cluster,
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
            
            // Section: Lock Policy
            item {
                Text(
                    text = "LOCK POLICY",
                    style = MaterialTheme.typography.labelMedium,
                    color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            
            items(LockPolicyType.values().size) { index ->
                val policy = LockPolicyType.values()[index]
                ClassicPolicyCard(
                    policy = policy,
                    isSelected = policy == selectedPolicy,
                    onSelect = { selectedPolicy = policy }
                )
            }
            
            // Action Buttons
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable(onClick = onNavigateBack),
                        shape = RoundedCornerShape(12.dp),
                        color = ClassicColors.Surface,
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            ClassicColors.OnSurface.copy(alpha = 0.2f)
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ClassicColors.OnSurface
                            )
                        }
                    }
                    
                    // Apply Lock Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable {
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
                                    thermalPolicy = currentThermalPolicy
                                )
                                
                                onNavigateBack()
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = ClassicColors.Primary
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Apply Lock",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
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
        
        ClassicFrequencySelectionDialog(
            title = if (isSelectingMin) "Min Frequency" else "Max Frequency",
            subtitle = "Cluster ${cluster.clusterNumber}",
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
private fun ClassicInfoHeaderCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Primary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Rounded.Info,
                contentDescription = null,
                tint = ClassicColors.Primary,
                modifier = Modifier.size(24.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Advanced Frequency Control",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface
                )
                Text(
                    text = "Lock CPU frequencies with intelligent thermal management. Choose a policy that fits your usage scenario.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassicColors.OnSurface.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ClassicClusterFrequencyCard(
    cluster: ClusterInfo,
    minFreq: Int,
    maxFreq: Int,
    onMinClick: () -> Unit,
    onMaxClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when (cluster.clusterNumber) {
                                    0 -> ClassicColors.Good.copy(alpha = 0.2f)
                                    1 -> ClassicColors.Primary.copy(alpha = 0.2f)
                                    else -> ClassicColors.Accent.copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Memory,
                            contentDescription = null,
                            tint = when (cluster.clusterNumber) {
                                0 -> ClassicColors.Good
                                1 -> ClassicColors.Primary
                                else -> ClassicColors.Accent
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Cluster ${cluster.clusterNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        Text(
                            text = "CPU ${cluster.cores.first()}-${cluster.cores.last()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (cluster.clusterNumber) {
                        0 -> ClassicColors.Good.copy(alpha = 0.2f)
                        1 -> ClassicColors.Primary.copy(alpha = 0.2f)
                        else -> ClassicColors.Accent.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = when (cluster.clusterNumber) {
                            0 -> "EFFICIENCY"
                            1 -> "BALANCED"
                            else -> "PERFORMANCE"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (cluster.clusterNumber) {
                            0 -> ClassicColors.Good
                            1 -> ClassicColors.Primary
                            else -> ClassicColors.Accent
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            HorizontalDivider(
                color = ClassicColors.OnSurface.copy(alpha = 0.1f),
                thickness = 1.dp
            )
            
            // Frequency Selectors
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
                    color = ClassicColors.Background,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        ClassicColors.OnSurface.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MIN",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Icon(
                                Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                tint = ClassicColors.OnSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "$minFreq",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        Text(
                            text = "MHz",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClassicColors.OnSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                
                // Max Frequency
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onMaxClick),
                    shape = RoundedCornerShape(12.dp),
                    color = ClassicColors.Background,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        ClassicColors.OnSurface.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MAX",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Icon(
                                Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                tint = ClassicColors.OnSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "$maxFreq",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        Text(
                            text = "MHz",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClassicColors.OnSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassicPolicyCard(
    policy: LockPolicyType,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val (icon, title, description, color) = when (policy) {
        LockPolicyType.MANUAL -> Tuple4(
            Icons.Rounded.Settings,
            "Manual",
            "Full user control, no thermal override",
            ClassicColors.Secondary
        )
        LockPolicyType.SMART -> Tuple4(
            Icons.Rounded.AutoAwesome,
            "Smart",
            "Thermal-aware with intelligent overrides",
            ClassicColors.Primary
        )
        LockPolicyType.GAME -> Tuple4(
            Icons.Rounded.SportsEsports,
            "Game",
            "Optimized for gaming performance",
            ClassicColors.Accent
        )
        LockPolicyType.BATTERY_SAVING -> Tuple4(
            Icons.Rounded.BatteryChargingFull,
            "Battery Saving",
            "Power-efficient frequency management",
            ClassicColors.Good
        )
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else ClassicColors.Surface,
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, color)
        else 
            androidx.compose.foundation.BorderStroke(1.dp, ClassicColors.OnSurface.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
            }
            
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(2.dp, ClassicColors.OnSurface.copy(alpha = 0.2f), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun ClassicFrequencySelectionDialog(
    title: String,
    subtitle: String,
    availableFrequencies: List<Int>,
    currentFrequency: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ClassicColors.Surface,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 400.dp)
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
                            ClassicColors.Primary.copy(alpha = 0.15f)
                        else
                            ClassicColors.Background,
                        border = if (isSelected)
                            androidx.compose.foundation.BorderStroke(2.dp, ClassicColors.Primary)
                        else
                            androidx.compose.foundation.BorderStroke(1.dp, ClassicColors.OnSurface.copy(alpha = 0.1f))
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
                                color = if (isSelected) ClassicColors.Primary else ClassicColors.OnSurface
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = ClassicColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ClassicColors.Primary
                )
            ) {
                Text(
                    text = "Close",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// Helper data class for policy card
private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
