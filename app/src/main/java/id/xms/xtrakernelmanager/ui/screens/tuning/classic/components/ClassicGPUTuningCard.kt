package id.xms.xtrakernelmanager.ui.screens.tuning.classic.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.screens.home.components.classic.ClassicCard
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@Composable
fun ClassicGPUTuningCard(viewModel: TuningViewModel) {
    val gpuInfo by viewModel.gpuInfo.collectAsState()
    val isMediatek by viewModel.isMediatek.collectAsState()
    val isFrequencyLocked by viewModel.isGpuFrequencyLocked.collectAsState()
    val lockedMinFreq by viewModel.lockedGpuMinFreq.collectAsState()
    val lockedMaxFreq by viewModel.lockedGpuMaxFreq.collectAsState()

    ClassicCard(title = "GPU Tuning", icon = Icons.Rounded.Gamepad) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (isMediatek) {
                Text(
                    text = "GPU tuning is not available for MediaTek devices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurfaceVariant
                )
            } else {
                // Frequency Control
                if (gpuInfo.availableFreqs.isNotEmpty()) {
                    Text(
                        text = "Frequency Control",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.Secondary
                    )
                    
                    val currentMin = if (isFrequencyLocked && lockedMinFreq > 0) lockedMinFreq else gpuInfo.minFreq
                    val currentMax = if (isFrequencyLocked && lockedMaxFreq > 0) lockedMaxFreq else gpuInfo.maxFreq

                    ClassicDropdown(
                        label = "Min Frequency",
                        options = gpuInfo.availableFreqs.map { "$it MHz" },
                        selectedOption = "$currentMin MHz",
                        onOptionSelected = { newFreqString ->
                            val newFreq = newFreqString.replace(" MHz", "").toInt()
                            if (isFrequencyLocked) {
                                viewModel.lockGPUFrequency(newFreq, currentMax)
                            } else {
                                viewModel.setGPUFrequency(newFreq, currentMax)
                            }
                        }
                    )
                    
                    ClassicDropdown(
                        label = "Max Frequency",
                        options = gpuInfo.availableFreqs.map { "$it MHz" },
                        selectedOption = "$currentMax MHz",
                        onOptionSelected = { newFreqString ->
                            val newFreq = newFreqString.replace(" MHz", "").toInt()
                            if (isFrequencyLocked) {
                                viewModel.lockGPUFrequency(currentMin, newFreq)
                            } else {
                                viewModel.setGPUFrequency(currentMin, newFreq)
                            }
                        }
                    )
                    
                    // Lock Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lock Frequency",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ClassicColors.OnSurface
                        )
                        Switch(
                            checked = isFrequencyLocked,
                            onCheckedChange = { locked ->
                                if (locked) {
                                    viewModel.lockGPUFrequency(currentMin, currentMax)
                                } else {
                                    viewModel.unlockGPUFrequency()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ClassicColors.Primary,
                                checkedTrackColor = ClassicColors.Primary.copy(alpha = 0.5f)
                            )
                        )
                    }
                    Divider(color = ClassicColors.SurfaceVariant)
                }
                
                // Power Level
                if (gpuInfo.numPwrLevels > 0) {
                     Text(
                        text = "Power Level",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.Secondary
                    )
                    
                    val levels = (0 until gpuInfo.numPwrLevels).map { it.toString() }
                    ClassicDropdown(
                        label = "Current Level",
                        options = levels,
                        selectedOption = gpuInfo.powerLevel.toString(),
                        onOptionSelected = { newLevel ->
                            viewModel.setGPUPowerLevel(newLevel.toInt())
                        }
                    )
                    Divider(color = ClassicColors.SurfaceVariant)
                }

                 // Renderer
                 Text(
                    text = "Renderer Code",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.Secondary
                )
                
                ClassicDropdown(
                    label = "Current Renderer",
                    options = listOf("vulkan", "skiavk", "opengl"), // simplified list, ideally from ViewMode/Constants
                    selectedOption = gpuInfo.rendererType,
                    onOptionSelected = { newRenderer ->
                         // For Classic, we might just set it directly or show a dialog like Liquid does.
                         // For simplicity, let's assume direct set or handle the complex flow via ViewModel if needed.
                         // But Liquid has a whole flow with reboot dialog. 
                         // To keep it simple for now, we'll just call setGPURenderer.
                         // Ideally we should replicate the reboot dialog flow.
                         viewModel.setGPURenderer(newRenderer)
                    }
                )
            }
        }
    }
}
