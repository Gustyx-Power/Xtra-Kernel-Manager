package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.viewmodel.TuningViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun CpuGovernorCard(
    vm: TuningViewModel,
) {
    val clusters = vm.cpuClusters
    val availableGovernors by vm.generalAvailableCpuGovernors.collectAsState()
    val coreStates by vm.coreStates.collectAsState()

    var showGovernorDialogForCluster by remember { mutableStateOf<String?>(null) }
    var showFreqDialogForCluster by remember { mutableStateOf<String?>(null) }
    var showCoreDialogForCluster by remember { mutableStateOf<String?>(null) }

    var isExpanded by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "DropdownRotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "CPU Control",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                    )
                }
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Configure CPU governor and frequency settings for each cluster",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
                exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (availableGovernors.isEmpty() && clusters.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Loading CPU data...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (clusters.isNotEmpty()) {
                        clusters.forEachIndexed { index, clusterName ->
                            CpuClusterCard(
                                clusterName = clusterName,
                                vm = vm,
                                onGovernorClick = { showGovernorDialogForCluster = clusterName },
                                onFrequencyClick = { showFreqDialogForCluster = clusterName },
                                onCoreClick = { showCoreDialogForCluster = clusterName }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showGovernorDialogForCluster != null) {
        GovernorSelectionDialog(
            clusterName = showGovernorDialogForCluster!!,
            availableGovernors = availableGovernors,
            currentSelectedGovernor = vm.getCpuGov(showGovernorDialogForCluster!!).collectAsState().value,
            onGovernorSelected = { selectedGov ->
                vm.setCpuGov(showGovernorDialogForCluster!!, selectedGov)
                showGovernorDialogForCluster = null
            },
            onDismiss = { showGovernorDialogForCluster = null }
        )
    }

    if (showFreqDialogForCluster != null) {
        val clusterName = showFreqDialogForCluster!!
        val availableFrequenciesForCluster by vm.getAvailableCpuFrequencies(clusterName).collectAsState()
        val currentFreqPair by vm.getCpuFreq(clusterName).collectAsState()

        // Show dialog even if data is not perfect - let user see what's happening
        if (availableFrequenciesForCluster.isNotEmpty()) {
            val systemMinFreq = availableFrequenciesForCluster.minOrNull() ?: currentFreqPair.first
            val systemMaxFreq = availableFrequenciesForCluster.maxOrNull() ?: currentFreqPair.second

            // More lenient condition - allow dialog to show even with imperfect data
            if (systemMinFreq > 0 && systemMaxFreq > 0 && systemMinFreq <= systemMaxFreq) {
                FrequencySelectionDialog(
                    clusterName = clusterName,
                    currentMinFreq = currentFreqPair.first.coerceAtLeast(systemMinFreq),
                    currentMaxFreq = currentFreqPair.second.coerceAtMost(systemMaxFreq),
                    minSystemFreq = systemMinFreq,
                    maxSystemFreq = systemMaxFreq,
                    allAvailableFrequencies = availableFrequenciesForCluster.sorted(),
                    onFrequencySelected = { newMin, newMax ->
                        vm.setCpuFreq(clusterName, newMin, newMax)
                        showFreqDialogForCluster = null
                    },
                    onDismiss = { showFreqDialogForCluster = null }
                )
            } else {
                // Show an error dialog instead of silently dismissing
                AlertDialog(
                    onDismissRequest = { showFreqDialogForCluster = null },
                    title = { Text("Frequency Error") },
                    text = {
                        Text("Cannot adjust frequency for $clusterName.\nInvalid frequency range: $systemMinFreq - $systemMaxFreq MHz")
                    },
                    confirmButton = {
                        TextButton(onClick = { showFreqDialogForCluster = null }) {
                            Text("OK")
                        }
                    }
                )
            }
        } else {
            // Show loading or error dialog instead of silently dismissing
            AlertDialog(
                onDismissRequest = { showFreqDialogForCluster = null },
                title = { Text("Loading Frequencies") },
                text = {
                    Column {
                        if (currentFreqPair.first == 0 && currentFreqPair.second == 0) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                Text("Loading frequency data for $clusterName...")
                            }
                        } else {
                            Text("No available frequencies found for $clusterName.\nCurrent: ${currentFreqPair.first/1000} - ${currentFreqPair.second/1000} MHz")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFreqDialogForCluster = null }) {
                        Text("OK")
                    }
                }
            )
        }
    }

    if (showCoreDialogForCluster != null) {
        CoreStatusDialog(
            clusterName = showCoreDialogForCluster!!,
            coreStates = coreStates,
            onCoreToggled = { coreId ->
                vm.toggleCore(coreId)
            },
            onDismiss = { showCoreDialogForCluster = null }
        )
    }
}

@Composable
private fun GovernorSelectionDialog(
    clusterName: String,
    availableGovernors: List<String>,
    currentSelectedGovernor: String,
    onGovernorSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Governor for ${clusterName.replaceFirstChar { it.titlecase() }}",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                availableGovernors.sorted().forEach { governor -> // Urutkan daftar governor
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onGovernorSelected(governor) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (governor == currentSelectedGovernor),
                            onClick = { onGovernorSelected(governor) },
                            modifier = Modifier.size(20.dp),
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            governor,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (governor == currentSelectedGovernor) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Cancel", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = null // Tidak perlu dismiss button eksplisit, onDismissRequest sudah cukup
    )
}

@Composable
private fun FrequencySelectionDialog(
    clusterName: String,
    currentMinFreq: Int,
    currentMaxFreq: Int,
    minSystemFreq: Int,
    maxSystemFreq: Int,
    allAvailableFrequencies: List<Int>,
    onFrequencySelected: (min: Int, max: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    if (allAvailableFrequencies.isEmpty() || minSystemFreq >= maxSystemFreq) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    var sliderMinValue by remember(currentMinFreq, minSystemFreq, maxSystemFreq, allAvailableFrequencies) {
        mutableFloatStateOf(
            findClosestFrequency(
                currentMinFreq.coerceIn(minSystemFreq, maxSystemFreq),
                allAvailableFrequencies
            ).toFloat()
        )
    }
    var sliderMaxValue by remember(currentMaxFreq, minSystemFreq, maxSystemFreq, allAvailableFrequencies) {
        mutableFloatStateOf(
            findClosestFrequency(
                currentMaxFreq.coerceIn(minSystemFreq, maxSystemFreq),
                allAvailableFrequencies
            ).toFloat()
        )
    }

    LaunchedEffect(currentMinFreq, currentMaxFreq, minSystemFreq, maxSystemFreq, allAvailableFrequencies) {
        val newInitialMin = findClosestFrequency(currentMinFreq.coerceIn(minSystemFreq, maxSystemFreq), allAvailableFrequencies).toFloat()
        val newInitialMax = findClosestFrequency(currentMaxFreq.coerceIn(minSystemFreq, maxSystemFreq), allAvailableFrequencies).toFloat()
        sliderMinValue = newInitialMin.coerceAtMost(newInitialMax)
        sliderMaxValue = newInitialMax.coerceAtLeast(newInitialMin)
    }

    val sliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
        inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Frequency", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                Text(
                    clusterName.replaceFirstChar { it.titlecase() },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp, top = 4.dp)
                )

                // Min Frequency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Min Frequency", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "${sliderMinValue.roundToInt() / 1000} MHz",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = sliderMinValue,
                    onValueChange = { newValue ->
                        sliderMinValue = newValue.coerceIn(minSystemFreq.toFloat(), sliderMaxValue)
                    },
                    valueRange = minSystemFreq.toFloat()..maxSystemFreq.toFloat(),
                    steps = if (allAvailableFrequencies.size > 1) (allAvailableFrequencies.size - 2).coerceAtLeast(0) else 0,
                    onValueChangeFinished = {
                        sliderMinValue = findClosestFrequency(sliderMinValue.roundToInt(), allAvailableFrequencies).toFloat()
                        if (sliderMinValue > sliderMaxValue) sliderMinValue = sliderMaxValue
                    },
                    colors = sliderColors
                )
                Spacer(Modifier.height(24.dp))

                // Max Frequency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Max Frequency", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "${sliderMaxValue.roundToInt() / 1000} MHz",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = sliderMaxValue,
                    onValueChange = { newValue ->
                        sliderMaxValue = newValue.coerceIn(sliderMinValue, maxSystemFreq.toFloat())
                    },
                    valueRange = minSystemFreq.toFloat()..maxSystemFreq.toFloat(),
                    steps = if (allAvailableFrequencies.size > 1) (allAvailableFrequencies.size - 2).coerceAtLeast(0) else 0,
                    onValueChangeFinished = {
                        sliderMaxValue = findClosestFrequency(sliderMaxValue.roundToInt(), allAvailableFrequencies).toFloat()
                        if (sliderMaxValue < sliderMinValue) sliderMaxValue = sliderMinValue
                    },
                    colors = sliderColors
                )
                Spacer(Modifier.height(16.dp))
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }

                FilledTonalButton(
                    onClick = {
                        val finalMin = findClosestFrequency(sliderMinValue.roundToInt(), allAvailableFrequencies)
                        val finalMax = findClosestFrequency(sliderMaxValue.roundToInt(), allAvailableFrequencies)
                        if (finalMin <= finalMax) {
                            onFrequencySelected(finalMin, finalMax)
                        } else {
                            onFrequencySelected(finalMin, finalMin)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Apply",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            // Kosong karena sudah diimplementasikan di confirmButton
        }
    )
}

@Composable
private fun CoreStatusDialog(
    clusterName: String,
    coreStates: List<Boolean>,
    onCoreToggled: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val GreenOnline = MaterialTheme.colorScheme.primary
    val RedOffline = MaterialTheme.colorScheme.error

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Core Status", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                Text(
                    clusterName.replaceFirstChar { it.titlecase() },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
                )
                coreStates.forEachIndexed { index, isOnline ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onCoreToggled(index) }
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Custom Button with rounded shape instead of RadioButton
                        Button(
                            onClick = { onCoreToggled(index) },
                            shape = RoundedCornerShape(50), // Fully rounded
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isOnline) GreenOnline else RedOffline,
                                contentColor = Color.White // Text color for the button
                            ),
                            modifier = Modifier
                                .size(24.dp) // Maintain size
                                .padding(0.dp), // Ensure no extra padding inside the button
                        ) {
                            // Empty content as the color itself indicates the state
                            // Alternatively, you can put a small check icon or something similar
                        }

                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = "Core $index",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = if (isOnline) "Online" else "Offline",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isOnline) GreenOnline else RedOffline,
                                fontWeight = FontWeight.SemiBold
                            ),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Done", fontWeight = FontWeight.Medium)
            }
        }
    )
}

private fun findClosestFrequency(target: Int, availableFrequencies: List<Int>): Int {
    if (availableFrequencies.isEmpty()) return target.coerceAtLeast(0)
    if (target in availableFrequencies) return target
    return availableFrequencies.minByOrNull { abs(it - target) } ?: target.coerceAtLeast(0)
}

@Composable
fun CpuClusterCard(
    clusterName: String,
    vm: TuningViewModel,
    onGovernorClick: () -> Unit,
    onFrequencyClick: () -> Unit,
    onCoreClick: () -> Unit
) {
    val currentGovernor by vm.getCpuGov(clusterName).collectAsState()
    val currentFreqPair by vm.getCpuFreq(clusterName).collectAsState()
    val availableFrequenciesForCluster by vm.getAvailableCpuFrequencies(clusterName).collectAsState()
    val coreStates by vm.coreStates.collectAsState()
    
    // Map cluster identifiers to display names
    val displayClusterName = when (clusterName) {
        "cpu0" -> "Little Cluster"
        "cpu4" -> "Big Cluster"
        "cpu7" -> "Prime Cluster"
        else -> clusterName.uppercase()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enhanced Header with cluster-specific styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cluster icon with themed background
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = displayClusterName.uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Cluster Control",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Status indicator
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                ) {
                    Text(
                        text = if (currentGovernor != "..." && currentGovernor != "Error") "ACTIVE" else "LOADING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (currentGovernor != "..." && currentGovernor != "Error")
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Enhanced Control Sections
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Governor Section
                ControlSection(
                    icon = Icons.Default.Tune,
                    title = "Governor",
                    value = if (currentGovernor == "..." || currentGovernor == "Error") currentGovernor else currentGovernor,
                    isLoading = currentGovernor == "..." || currentGovernor == "Error",
                    themeColor = MaterialTheme.colorScheme.primary,
                    onClick = onGovernorClick,
                    enabled = currentGovernor != "..." && currentGovernor != "Error"
                )

                // Frequency Section
                val freqText = when {
                    currentGovernor == "..." || currentGovernor == "Error" -> currentGovernor
                    currentFreqPair.first == 0 && currentFreqPair.second == 0 && availableFrequenciesForCluster.isEmpty() -> "Loading..."
                    currentFreqPair.first == 0 && currentFreqPair.second == -1 -> "Error"
                    else -> "${currentFreqPair.first / 1000} - ${currentFreqPair.second / 1000} MHz"
                }

                ControlSection(
                    icon = Icons.Default.Speed,
                    title = "Frequency",
                    value = freqText,
                    isLoading = freqText == "Loading..." || freqText == "Error",
                    themeColor = MaterialTheme.colorScheme.primary,
                    onClick = onFrequencyClick,
                    enabled = availableFrequenciesForCluster.isNotEmpty()
                )

                // Core Status Section
                ControlSection(
                    icon = Icons.Default.Memory,
                    title = "Core Status",
                    value = "${coreStates.count { it }}/${coreStates.size} Online",
                    isLoading = false,
                    themeColor = MaterialTheme.colorScheme.primary,
                    onClick = onCoreClick,
                    enabled = true
                )
            }
        }
    }
}

@Composable
private fun ControlSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    isLoading: Boolean,
    themeColor: Color,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon with themed background
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (enabled) themeColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Arrow indicator
            if (enabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Expand",
                    tint = themeColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
