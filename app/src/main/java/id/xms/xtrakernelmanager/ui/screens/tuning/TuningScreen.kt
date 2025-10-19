package id.xms.xtrakernelmanager.ui.screens.tuning

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.utils.toFrequencyString

@Composable
fun TuningScreen(
    viewModel: TuningViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tuning Control",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row {
                    IconButton(onClick = { /* Import */ }) {
                        Icon(Icons.Default.Download, "Import")
                    }
                    IconButton(onClick = { /* Export */ }) {
                        Icon(Icons.Default.Upload, "Export")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            CpuControlSection(uiState, viewModel)
        }

        if (!uiState.isMediaTek) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                GpuControlSection(uiState, viewModel)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            RamControlSection(uiState, viewModel)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            AdditionalControlSection(uiState, viewModel)
        }
    }

    uiState.message?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }

        Snackbar(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(message)
        }
    }
}

@Composable
private fun CpuControlSection(
    uiState: TuningUiState,
    viewModel: TuningViewModel
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "CPU Control",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            uiState.cpuInfo.clusters.forEach { (clusterName, cores) ->
                Text(
                    text = "$clusterName Cluster",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                cores.forEach { coreNum ->
                    val core = uiState.cpuInfo.cores.getOrNull(coreNum)
                    core?.let {
                        CpuCoreControl(
                            coreNumber = coreNum,
                            core = it,
                            onFrequencyChange = { min, max ->
                                viewModel.setCpuFrequency(coreNum, min, max)
                            },
                            onGovernorChange = { governor ->
                                viewModel.setCpuGovernor(coreNum, governor)
                            },
                            onToggleOnline = { online ->
                                viewModel.toggleCpuCore(coreNum, online)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CpuCoreControl(
    coreNumber: Int,
    core: id.xms.xtrakernelmanager.data.model.CpuCore,
    onFrequencyChange: (Long, Long) -> Unit,
    onGovernorChange: (String) -> Unit,
    onToggleOnline: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CPU $coreNumber",
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = core.currentFreq.toFrequencyString(),
                    style = MaterialTheme.typography.bodySmall
                )

                if (coreNumber > 0) {
                    Switch(
                        checked = core.online,
                        onCheckedChange = onToggleOnline
                    )
                }
            }

            if (core.online) {
                Text(
                    text = "Governor: ${core.governor}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun GpuControlSection(
    uiState: TuningUiState,
    viewModel: TuningViewModel
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "GPU Control",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Current Frequency: ${uiState.gpuInfo.currentFreq.toFrequencyString()}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Power Level: ${uiState.gpuInfo.powerLevel}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RamControlSection(
    uiState: TuningUiState,
    viewModel: TuningViewModel
) {
    var swappiness by remember { mutableStateOf(uiState.swappiness.toFloat()) }

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "RAM Control",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Swappiness: ${swappiness.toInt()}")
            Slider(
                value = swappiness,
                onValueChange = { swappiness = it },
                onValueChangeFinished = {
                    viewModel.setSwappiness(swappiness.toInt())
                },
                valueRange = 0f..100f
            )
        }
    }
}

@Composable
private fun AdditionalControlSection(
    uiState: TuningUiState,
    viewModel: TuningViewModel
) {
    var selectedThermalMode by remember { mutableStateOf(uiState.thermalMode) }

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "Additional Control",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Thermal Mode",
                style = MaterialTheme.typography.titleMedium
            )

            listOf("Not Set", "Dynamic", "Thermal 20", "Incalls").forEach { mode ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedThermalMode == mode,
                        onClick = {
                            selectedThermalMode = mode
                            viewModel.setThermalMode(mode)
                        }
                    )
                    Text(text = mode)
                }
            }
        }
    }
}
