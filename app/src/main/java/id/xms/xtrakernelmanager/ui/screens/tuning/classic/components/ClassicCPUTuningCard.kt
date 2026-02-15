package id.xms.xtrakernelmanager.ui.screens.tuning.classic.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.screens.home.components.classic.ClassicCard
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@Composable
fun ClassicCPUTuningCard(viewModel: TuningViewModel) {
    val clusters by viewModel.cpuClusters.collectAsState()
    val clusterStates by viewModel.clusterStates.collectAsState()

    ClassicCard(title = "CPU Tuning", icon = Icons.Rounded.Memory) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (clusters.isEmpty()) {
                Text(
                    text = "No clusters detected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurfaceVariant
                )
            } else {
                clusters.forEach { cluster ->
                    val currentState = clusterStates[cluster.clusterNumber]
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Cluster ${cluster.clusterNumber} (${cluster.cores.joinToString(", ")})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.Secondary
                        )
                        
                        // Governor
                        ClassicDropdown(
                            label = "Governor",
                            options = cluster.availableGovernors,
                            selectedOption = currentState?.governor ?: cluster.governor,
                            onOptionSelected = { newGovernor ->
                                viewModel.setCpuClusterGovernor(cluster.clusterNumber, newGovernor)
                            }
                        )
                        
                        // Min Frequency
                        val currentMinFreq = currentState?.minFreq?.toInt() ?: cluster.currentMinFreq
                        ClassicDropdown(
                            label = "Min Frequency",
                            options = cluster.availableFrequencies.map { "${it / 1000} MHz" },
                            selectedOption = "${currentMinFreq / 1000} MHz",
                            onOptionSelected = { newFreqString ->
                                val newFreq = newFreqString.replace(" MHz", "").toInt() * 1000
                                val currentMax = currentState?.maxFreq?.toInt() ?: cluster.currentMaxFreq
                                viewModel.setCpuClusterFrequency(cluster.clusterNumber, newFreq, currentMax)
                            }
                        )
                        
                        // Max Frequency
                        val currentMaxFreq = currentState?.maxFreq?.toInt() ?: cluster.currentMaxFreq
                        ClassicDropdown(
                            label = "Max Frequency",
                            options = cluster.availableFrequencies.map { "${it / 1000} MHz" },
                            selectedOption = "${currentMaxFreq / 1000} MHz",
                            onOptionSelected = { newFreqString ->
                                val newFreq = newFreqString.replace(" MHz", "").toInt() * 1000
                                val currentMin = currentState?.minFreq?.toInt() ?: cluster.currentMinFreq
                                viewModel.setCpuClusterFrequency(cluster.clusterNumber, currentMin, newFreq)
                            }
                        )
                        
                        Divider(color = ClassicColors.SurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}
