package id.xms.xtrakernelmanager.ui.screens.tuning

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.utils.toFrequencyString
import id.xms.xtrakernelmanager.utils.toMhzString
import kotlin.math.roundToInt

@Composable
fun TuningScreen(
    viewModel: TuningViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showCpuGovernorDialog by remember { mutableStateOf<Pair<Int, List<String>>?>(null) }
    var showGpuGovernorDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tuning Control",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row {
                    IconButton(onClick = { viewModel.importProfile() }) {
                        Icon(Icons.Default.Download, "Import Profile")
                    }
                    IconButton(onClick = { viewModel.exportProfile() }) {
                        Icon(Icons.Default.Upload, "Export Profile")
                    }
                }
            }
        }

        if (!uiState.hasRoot) {
            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Root access required for tuning features",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        item {
            CpuControlSection(
                uiState = uiState,
                onFrequencyChange = { core, freq -> viewModel.setCpuFrequency(core, freq) },
                onGovernorClick = { core, governors -> showCpuGovernorDialog = Pair(core, governors) },
                onCoreToggle = { core, online -> viewModel.toggleCpuCore(core, online) }
            )
        }

        if (!uiState.isMediaTek) {
            item {
                GpuControlSection(
                    uiState = uiState,
                    onFrequencyChange = { freq -> viewModel.setGpuFrequency(freq) },
                    onGovernorClick = { showGpuGovernorDialog = true }
                )
            }
        }

        item {
            RamControlSection(
                uiState = uiState,
                onSwappinessChange = { viewModel.setSwappiness(it) },
                onZramSizeChange = { viewModel.setZramSize(it) },
                onSwapSizeChange = { viewModel.setSwapSize(it) }
            )
        }

        item {
            AdditionalControlSection(
                uiState = uiState,
                onThermalModeChange = { viewModel.setThermalMode(it) }
            )
        }
    }

    // CPU Governor Dialog
    showCpuGovernorDialog?.let { (core, governors) ->
        GovernorSelectionDialog(
            title = "CPU $core Governor",
            currentGovernor = uiState.cpuInfo.cores.getOrNull(core)?.governor ?: "",
            availableGovernors = governors,
            onDismiss = { showCpuGovernorDialog = null },
            onSelect = { governor ->
                viewModel.setCpuGovernor(core, governor)
                showCpuGovernorDialog = null
            }
        )
    }

    // GPU Governor Dialog
    if (showGpuGovernorDialog) {
        GovernorSelectionDialog(
            title = "GPU Governor",
            currentGovernor = uiState.gpuInfo.governor,
            availableGovernors = uiState.gpuInfo.availableGovernors,
            onDismiss = { showGpuGovernorDialog = false },
            onSelect = { governor ->
                viewModel.setGpuGovernor(governor)
                showGpuGovernorDialog = false
            }
        )
    }

    // Snackbar for messages
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }
}

@Composable
private fun CpuControlSection(
    uiState: TuningUiState,
    onFrequencyChange: (Int, Long) -> Unit,
    onGovernorClick: (Int, List<String>) -> Unit,
    onCoreToggle: (Int, Boolean) -> Unit
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "CPU Control",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            uiState.cpuInfo.clusters.forEach { (clusterName, cores) ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "$clusterName Cluster",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = when (clusterName) {
                            "Little" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                            "Big" -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                            "Prime" -> androidx.compose.ui.graphics.Color(0xFFF44336)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )

                    cores.forEach { coreIndex ->
                        val core = uiState.cpuInfo.cores.getOrNull(coreIndex)
                        if (core != null) {
                            CpuCoreControl(
                                coreIndex = coreIndex,
                                core = core,
                                onFrequencyChange = onFrequencyChange,
                                onGovernorClick = onGovernorClick,
                                onCoreToggle = onCoreToggle,
                                hasRoot = uiState.hasRoot
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun CpuCoreControl(
    coreIndex: Int,
    core: id.xms.xtrakernelmanager.data.model.CpuCore,
    onFrequencyChange: (Int, Long) -> Unit,
    onGovernorClick: (Int, List<String>) -> Unit,
    onCoreToggle: (Int, Boolean) -> Unit,
    hasRoot: Boolean = true
) {
    var sliderPosition by remember(core.currentFreq) {
        mutableFloatStateOf(
            if (core.availableFrequencies.isNotEmpty()) {
                core.availableFrequencies.indexOf(core.currentFreq).coerceAtLeast(0).toFloat()
            } else 0f
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Core Header with Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CPU $coreIndex",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (coreIndex != 0) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (core.online)
                                androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.2f)
                            else
                                androidx.compose.ui.graphics.Color(0xFFFF5252).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (core.online) "Online" else "Offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (core.online)
                                    androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                else
                                    androidx.compose.ui.graphics.Color(0xFFFF5252),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = core.currentFreq.toFrequencyString(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    if (coreIndex != 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = core.online,
                            onCheckedChange = { if (hasRoot) onCoreToggle(coreIndex, it) },
                            enabled = hasRoot,
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }

            if (core.online) {
                // Frequency Slider
                if (core.availableFrequencies.isNotEmpty()) {
                    Column {
                        Slider(
                            value = sliderPosition,
                            onValueChange = {
                                if (hasRoot) sliderPosition = it
                            },
                            onValueChangeFinished = {
                                if (hasRoot) {
                                    val index = sliderPosition.roundToInt().coerceIn(0, core.availableFrequencies.size - 1)
                                    val selectedFreq = core.availableFrequencies[index]
                                    onFrequencyChange(coreIndex, selectedFreq)
                                }
                            },
                            enabled = hasRoot,
                            valueRange = 0f..(core.availableFrequencies.size - 1).toFloat(),
                            steps = maxOf(0, core.availableFrequencies.size - 2),
                            colors = SliderDefaults.colors(
                                thumbColor = if (hasRoot) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                activeTrackColor = if (hasRoot) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                disabledThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                disabledActiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = core.minFreq.toFrequencyString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = core.maxFreq.toFrequencyString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Governor Selection
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = hasRoot) {
                            if (core.availableGovernors.isNotEmpty()) {
                                onGovernorClick(coreIndex, core.availableGovernors)
                            }
                        },
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = if (hasRoot) 0.5f else 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Governor",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = if (hasRoot) core.governor else "Root Needed",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = if (hasRoot) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.error
                            )
                        }
                        if (hasRoot) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun GpuControlSection(
    uiState: TuningUiState,
    onFrequencyChange: (Long) -> Unit,
    onGovernorClick: () -> Unit
) {
    var sliderPosition by remember(uiState.gpuInfo.currentFreq) {
        mutableFloatStateOf(
            if (uiState.gpuInfo.availableFreqs.isNotEmpty()) {
                uiState.gpuInfo.availableFreqs.indexOf(uiState.gpuInfo.currentFreq)
                    .coerceAtLeast(0).toFloat()
            } else 0f
        )
    }

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "GPU Control",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Current Frequency Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current Frequency",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = uiState.gpuInfo.currentFreq.toMhzString(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Frequency Slider
            if (uiState.gpuInfo.availableFreqs.isNotEmpty()) {
                Column {
                    Slider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        onValueChangeFinished = {
                            val index = sliderPosition.roundToInt()
                                .coerceIn(0, uiState.gpuInfo.availableFreqs.size - 1)
                            val selectedFreq = uiState.gpuInfo.availableFreqs[index]
                            onFrequencyChange(selectedFreq)
                        },
                        valueRange = 0f..(uiState.gpuInfo.availableFreqs.size - 1).toFloat(),
                        steps = maxOf(0, uiState.gpuInfo.availableFreqs.size - 2),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uiState.gpuInfo.minFreq.toMhzString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = uiState.gpuInfo.maxFreq.toMhzString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Governor Selection
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGovernorClick() },
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Governor",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = uiState.gpuInfo.governor.ifEmpty { "N/A" },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Power Level Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Power Level",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = uiState.gpuInfo.powerLevel.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun RamControlSection(
    uiState: TuningUiState,
    onSwappinessChange: (Int) -> Unit,
    onZramSizeChange: (Long) -> Unit,
    onSwapSizeChange: (Long) -> Unit
) {
    var swappiness by remember(uiState.swappiness) { mutableFloatStateOf(uiState.swappiness.toFloat()) }
    var zramSize by remember(uiState.zramSize) { mutableFloatStateOf((uiState.zramSize / (1024 * 1024)).toFloat()) }
    var swapSize by remember(uiState.swapSize) { mutableFloatStateOf((uiState.swapSize / (1024 * 1024)).toFloat()) }

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "RAM Control",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Swappiness
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Swappiness",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = swappiness.toInt().toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Slider(
                    value = swappiness,
                    onValueChange = { swappiness = it },
                    onValueChangeFinished = { onSwappinessChange(swappiness.toInt()) },
                    valueRange = 0f..100f,
                    steps = 99
                )
                Text(
                    text = "Controls how aggressively the kernel swaps memory pages",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // ZRAM Size
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ZRAM Size",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "${zramSize.toInt()} MB",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Slider(
                    value = zramSize,
                    onValueChange = { zramSize = it },
                    onValueChangeFinished = {
                        onZramSizeChange((zramSize * 1024 * 1024).toLong())
                    },
                    valueRange = 0f..4096f,
                    steps = 63
                )
                Text(
                    text = "Compressed RAM for swap (0 to disable)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Swap Size
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Swap Size",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "${swapSize.toInt()} MB",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Slider(
                    value = swapSize,
                    onValueChange = { swapSize = it },
                    onValueChangeFinished = {
                        onSwapSizeChange((swapSize * 1024 * 1024).toLong())
                    },
                    valueRange = 0f..8192f,
                    steps = 127
                )
                Text(
                    text = "Swap file size on storage (0 to disable)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun AdditionalControlSection(
    uiState: TuningUiState,
    onThermalModeChange: (String) -> Unit
) {
    var selectedThermalMode by remember(uiState.thermalMode) { mutableStateOf(uiState.thermalMode) }

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Additional Control",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Text(
                text = "Thermal Mode",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
            )

            listOf("Not Set", "Dynamic", "Thermal 20", "Incalls").forEach { mode ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedThermalMode = mode
                            onThermalModeChange(mode)
                        },
                    color = if (selectedThermalMode == mode)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else
                        androidx.compose.ui.graphics.Color.Transparent,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedThermalMode == mode,
                            onClick = {
                                selectedThermalMode = mode
                                onThermalModeChange(mode)
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = mode,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GovernorSelectionDialog(
    title: String,
    currentGovernor: String,
    availableGovernors: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LazyColumn {
                items(availableGovernors) { governor ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(governor) },
                        color = if (governor == currentGovernor)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else
                            androidx.compose.ui.graphics.Color.Transparent,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = governor == currentGovernor,
                                onClick = { onSelect(governor) }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = governor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
