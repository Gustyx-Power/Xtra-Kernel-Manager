package id.xms.xtrakernelmanager.ui.screens.tuning.classic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import id.xms.xtrakernelmanager.data.model.CoreInfo
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicCPUTuningScreen(
    viewModel: TuningViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSmartLock: () -> Unit = {}
) {
    val cpuClusters by viewModel.cpuClusters.collectAsState()
    val cpuCores by viewModel.cpuCores.collectAsState()
    val clusterStates by viewModel.clusterStates.collectAsState()

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
                        text = "CPU Tuning",
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
            // System Performance Header
            item {
                ClassicSystemPerformanceCard()
            }

            // Smart Frequency Lock Card
            item {
                ClassicSmartFrequencyLockCard(onNavigateToSmartLock = onNavigateToSmartLock)
            }

            // Core Management Card
            item {
                ClassicCoreManagementCard(
                    cores = cpuCores,
                    viewModel = viewModel
                )
            }

            // Cluster Cards
            items(cpuClusters) { cluster ->
                ClassicClusterCard(
                    cluster = cluster,
                    viewModel = viewModel
                )
            }

            // Set on Boot Card
            item {
                ClassicSetOnBootCard(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ClassicSystemPerformanceCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SYSTEM PERFORMANCE",
                style = MaterialTheme.typography.labelSmall,
                color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                letterSpacing = 1.2.sp
            )
            Text(
                text = "CPU Tuning",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface,
                lineHeight = 32.sp
            )
            Text(
                text = "Precise control over your processor's performance and frequency. Optimize for ultimate efficiency.",
                style = MaterialTheme.typography.bodyMedium,
                color = ClassicColors.OnSurface.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ClassicSmartFrequencyLockCard(onNavigateToSmartLock: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToSmartLock),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Primary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ClassicColors.Primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = ClassicColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Smart Frequency Lock",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                    Text(
                        text = "Advanced frequency control",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = ClassicColors.OnSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun ClassicCoreManagementCard(
    cores: List<CoreInfo>,
    viewModel: TuningViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
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
                            .background(ClassicColors.Secondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Memory,
                            contentDescription = null,
                            tint = ClassicColors.Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Core Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        Text(
                            text = "${cores.count { it.isOnline }}/${cores.size} cores online",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = ClassicColors.OnSurface.copy(alpha = 0.4f)
                )
            }

            // Expanded Content
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cores.forEach { core ->
                        ClassicCoreItem(
                            core = core,
                            onToggle = { enabled ->
                                viewModel.setCpuCoreOnline(core.coreNumber, enabled)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClassicCoreItem(
    core: CoreInfo,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ClassicColors.Background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Core ${core.coreNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = ClassicColors.OnSurface
                )
                Text(
                    text = if (core.isOnline) "${core.currentFreq} MHz" else "Offline",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                )
            }
            Switch(
                checked = core.isOnline,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ClassicColors.Primary,
                    checkedTrackColor = ClassicColors.Primary.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun ClassicClusterCard(
    cluster: ClusterInfo,
    viewModel: TuningViewModel
) {
    val clusterStates by viewModel.clusterStates.collectAsState()
    val clusterState = clusterStates[cluster.clusterNumber]
    val currentGovernor = clusterState?.governor ?: cluster.governor
    
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
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
                            if (cluster.clusterNumber == 2) Icons.Rounded.Star else Icons.Rounded.Speed,
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Cluster ${cluster.clusterNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ClassicColors.OnSurface
                            )
                            if (cluster.clusterNumber == 2) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = ClassicColors.Accent.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "Prime",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ClassicColors.Accent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "CPU ${cluster.cores.first()}-${cluster.cores.last()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = ClassicColors.Good.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "OPTIMAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassicColors.Good,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Governor
            ClassicGovernorSelector(
                currentGovernor = currentGovernor,
                availableGovernors = cluster.availableGovernors,
                onGovernorSelected = { newGov ->
                    viewModel.setCpuClusterGovernor(cluster.clusterNumber, newGov)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Frequencies
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClassicFrequencyTile(
                    modifier = Modifier.weight(1f),
                    label = "MIN FREQUENCY",
                    value = "${cluster.currentMinFreq} MHz",
                    options = cluster.availableFrequencies.sortedDescending().map { "$it MHz" },
                    onValueChange = { selectedStr ->
                        val freq = selectedStr.removeSuffix(" MHz").toIntOrNull() ?: cluster.minFreq
                        viewModel.setCpuClusterFrequency(cluster.clusterNumber, freq, cluster.currentMaxFreq)
                    }
                )
                ClassicFrequencyTile(
                    modifier = Modifier.weight(1f),
                    label = "MAX FREQUENCY",
                    value = "${cluster.currentMaxFreq} MHz",
                    options = cluster.availableFrequencies.sortedDescending().map { "$it MHz" },
                    onValueChange = { selectedStr ->
                        val freq = selectedStr.removeSuffix(" MHz").toIntOrNull() ?: cluster.maxFreq
                        viewModel.setCpuClusterFrequency(cluster.clusterNumber, cluster.currentMinFreq, freq)
                    }
                )
            }
        }
    }
}

@Composable
fun ClassicGovernorSelector(
    currentGovernor: String,
    availableGovernors: List<String>,
    onGovernorSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        color = ClassicColors.Background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GOVERNOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentGovernor,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = ClassicColors.OnSurface
                    )
                }
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = ClassicColors.OnSurface.copy(alpha = 0.4f)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    availableGovernors.forEach { gov ->
                        val isSelected = gov == currentGovernor
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onGovernorSelected(gov)
                                    expanded = false
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) ClassicColors.Primary.copy(alpha = 0.1f)
                            else Color.Transparent
                        ) {
                            Text(
                                text = gov,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ClassicColors.Primary else ClassicColors.OnSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassicFrequencyTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.clickable { expanded = true },
        shape = RoundedCornerShape(12.dp),
        color = ClassicColors.Background
    ) {
        Box {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = ClassicColors.OnSurface
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(ClassicColors.Surface)
                    .heightIn(max = 250.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option == value
                    DropdownMenuItem(
                        text = {
                            Text(
                                option,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ClassicColors.Primary else ClassicColors.OnSurface
                            )
                        },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ClassicSetOnBootCard(viewModel: TuningViewModel) {
    val cpuSetOnBoot by viewModel.preferencesManager.getCpuSetOnBoot().collectAsState(initial = false)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
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
                        .background(ClassicColors.Primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.PowerSettingsNew,
                        contentDescription = null,
                        tint = ClassicColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Set on Boot",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                    Text(
                        text = "Apply CPU settings on boot",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Switch(
                checked = cpuSetOnBoot,
                onCheckedChange = { enabled -> viewModel.setCpuSetOnBoot(enabled) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ClassicColors.Primary,
                    checkedTrackColor = ClassicColors.Primary.copy(alpha = 0.5f)
                )
            )
        }
    }
}
