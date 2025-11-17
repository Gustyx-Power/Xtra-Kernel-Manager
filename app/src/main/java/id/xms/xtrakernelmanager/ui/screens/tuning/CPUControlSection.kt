package id.xms.xtrakernelmanager.ui.screens.tuning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CPUControlSection(viewModel: TuningViewModel) {
    val clusters by viewModel.cpuClusters.collectAsState()
    val clusterStates by viewModel.clusterStates.collectAsState()
    var expanded by remember { mutableStateOf(true) }

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "CPU Control",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = if (expanded)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            Text(
                text = "Fine tune your CPU clusters for performance, efficiency, or battery life by controlling clock, governor, and core status per cluster.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                modifier = Modifier.padding(top = 1.dp, bottom = 8.dp)
            )

            if (!expanded) return@GlassmorphicCard

            if (clusters.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "No CPU clusters detected",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                clusters.forEachIndexed { index, cluster ->

                    val clusterName = when (index) {
                        0 -> "Cluster 1 (CPU ${cluster.cores.joinToString(", ")})"
                        1 -> "Cluster 2 (CPU ${cluster.cores.joinToString(", ")})"
                        2 -> "Cluster 3 (CPU ${cluster.cores.joinToString(", ")})"
                        else -> "Cluster $index (CPU ${cluster.cores.joinToString(", ")})"
                    }

                    val uiState = clusterStates[cluster.clusterNumber]
                    val currentMinFreq = uiState?.minFreq ?: cluster.currentMinFreq.toFloat()
                    val currentMaxFreq = uiState?.maxFreq ?: cluster.currentMaxFreq.toFloat()
                    val currentGovernor = uiState?.governor?.takeIf { it.isNotBlank() } ?: cluster.governor

                    var governorExpanded by remember { mutableStateOf(false) }
                    var minFreqSlider by remember { mutableFloatStateOf(currentMinFreq) }
                    var maxFreqSlider by remember { mutableFloatStateOf(currentMaxFreq) }
                    LaunchedEffect(currentMinFreq, currentMaxFreq) {
                        minFreqSlider = currentMinFreq
                        maxFreqSlider = currentMaxFreq
                    }
                    var selectedGovernor by remember { mutableStateOf(currentGovernor) }
                    LaunchedEffect(currentGovernor) {
                        selectedGovernor = currentGovernor
                    }

                    Spacer(Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.37f),
                                shape = RoundedCornerShape(11.dp)
                            )
                            .padding(vertical = 8.dp, horizontal = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = clusterName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${cluster.minFreq}-${cluster.maxFreq} MHz",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "Min Frequency: ${minFreqSlider.toInt()} MHz",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Slider(
                            value = minFreqSlider,
                            onValueChange = {
                                minFreqSlider = it
                                viewModel.updateClusterUIState(cluster.clusterNumber, it, maxFreqSlider)
                            },
                            onValueChangeFinished = {
                                viewModel.setCPUFrequency(
                                    cluster.clusterNumber,
                                    minFreqSlider.toInt(),
                                    maxFreqSlider.toInt()
                                )
                            },
                            valueRange = cluster.minFreq.toFloat()..cluster.maxFreq.toFloat(),
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Text(
                            text = "Max Frequency: ${maxFreqSlider.toInt()} MHz",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Slider(
                            value = maxFreqSlider,
                            onValueChange = {
                                maxFreqSlider = it
                                viewModel.updateClusterUIState(cluster.clusterNumber, minFreqSlider, it)
                            },
                            onValueChangeFinished = {
                                viewModel.setCPUFrequency(
                                    cluster.clusterNumber,
                                    minFreqSlider.toInt(),
                                    maxFreqSlider.toInt()
                                )
                            },
                            valueRange = cluster.minFreq.toFloat()..cluster.maxFreq.toFloat(),
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Text(
                            text = "Governor: $selectedGovernor",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 5.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = governorExpanded,
                            onExpandedChange = { governorExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedGovernor,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = governorExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = governorExpanded,
                                onDismissRequest = { governorExpanded = false }
                            ) {
                                cluster.availableGovernors.forEach { governor ->
                                    DropdownMenuItem(
                                        text = { Text(governor) },
                                        onClick = {
                                            selectedGovernor = governor
                                            viewModel.setCPUGovernor(cluster.clusterNumber, governor)
                                            governorExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Core Control",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        cluster.cores.forEach { coreNum ->
                            if (coreNum != 0) {
                                val coreEnabled by viewModel.preferencesManager.isCpuCoreEnabled(coreNum)
                                    .collectAsState(initial = true)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 1.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "CPU $coreNum",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Switch(
                                        checked = coreEnabled,
                                        onCheckedChange = {
                                            viewModel.disableCPUCore(coreNum, !it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                    if (index < clusters.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 13.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        )
                    }
                }
            }
        }
    }
}
