package id.xms.xtrakernelmanager.ui.screens.tuning.classic.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.model.RAMConfig
import id.xms.xtrakernelmanager.ui.screens.home.components.classic.ClassicCard
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import java.util.Locale

@Composable
fun ClassicRAMTuningCard(viewModel: TuningViewModel) {
    val persistedConfig by viewModel.preferencesManager.getRamConfig().collectAsState(initial = RAMConfig())

    var swappiness by remember(persistedConfig.swappiness) { mutableFloatStateOf(persistedConfig.swappiness.toFloat()) }
    var dirtyRatio by remember(persistedConfig.dirtyRatio) { mutableFloatStateOf(persistedConfig.dirtyRatio.toFloat()) }
    var minFreeMem by remember(persistedConfig.minFreeMem) { mutableFloatStateOf(persistedConfig.minFreeMem.toFloat()) }

    var zramEnabled by remember(persistedConfig.zramSize) { mutableStateOf(persistedConfig.zramSize > 0) }
    var zramSize by remember(persistedConfig.zramSize) { mutableFloatStateOf(if (persistedConfig.zramSize > 0) persistedConfig.zramSize.toFloat() else 2048f) }
    var compressionAlgo by remember(persistedConfig.compressionAlgorithm) { mutableStateOf(persistedConfig.compressionAlgorithm) }

    var swapEnabled by remember(persistedConfig.swapSize) { mutableStateOf(persistedConfig.swapSize > 0) }
    var swapSize by remember(persistedConfig.swapSize) { mutableFloatStateOf(if (persistedConfig.swapSize > 0) persistedConfig.swapSize.toFloat() else 2048f) }

    fun pushConfig() {
        viewModel.setRAMParameters(
            RAMConfig(
                swappiness = swappiness.toInt(),
                zramSize = if (zramEnabled) zramSize.toInt() else 0,
                swapSize = if (swapEnabled) swapSize.toInt() else 0,
                dirtyRatio = dirtyRatio.toInt(),
                minFreeMem = minFreeMem.toInt(),
                compressionAlgorithm = compressionAlgo,
            )
        )
    }

    ClassicCard(title = "Memory Tuning", icon = Icons.Rounded.Memory) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            
            // Core Parameters
            Text(
                text = "Core Parameters",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.Secondary
            )

            ClassicSlider(
                label = "Swappiness",
                value = swappiness,
                valueRange = 0f..200f,
                steps = 19,
                valueDisplay = "${swappiness.toInt()}",
                onValueChange = { swappiness = it },
                onValueFinished = { pushConfig() }
            )

            ClassicSlider(
                label = "Dirty Ratio",
                value = dirtyRatio,
                valueRange = 1f..50f,
                steps = 48,
                valueDisplay = "${dirtyRatio.toInt()}%",
                onValueChange = { dirtyRatio = it },
                onValueFinished = { pushConfig() }
            )

            ClassicSlider(
                label = "Min Free Memory",
                value = minFreeMem,
                valueRange = 0f..262144f, // Up to 256MB roughly? Or is it standard lowmemorykiller minfree? 262144KB = 256MB.
                steps = 14,
                valueDisplay = "${(minFreeMem / 1024f)} MB", 
                onValueChange = { minFreeMem = it },
                onValueFinished = { pushConfig() }
            )
            
            Divider(color = ClassicColors.SurfaceVariant)

            // ZRAM
             Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ZRAM",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.Secondary
                )
                Switch(
                    checked = zramEnabled,
                    onCheckedChange = { 
                        zramEnabled = it
                        if (it && zramSize < 512f) zramSize = 2048f
                        pushConfig() // Applying immediately might trigger re-init, better to have Apply button?
                        // Liquid uses Apply button. Here we can use simple switch logic but maybe safer to have apply.
                        // For Classic, let's keep it immediate but maybe debounced or just trigger config push.
                        // The Liquid implementation has separate Apply button which implies heavy operation.
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ClassicColors.Primary,
                        checkedTrackColor = ClassicColors.Primary.copy(alpha = 0.5f)
                    )
                )
            }

            if (zramEnabled) {
                ClassicSlider(
                    label = "ZRAM Size",
                    value = zramSize,
                    valueRange = 512f..8192f,
                    steps = 15,
                    valueDisplay = "${zramSize.toInt()} MB",
                    onValueChange = { zramSize = it },
                    onValueFinished = { pushConfig() } 
                )
                
                ClassicDropdown(
                    label = "Compression Algorithm",
                    options = listOf("lz4", "lzo", "zstd", "lzo-rle"),
                    selectedOption = compressionAlgo,
                    onOptionSelected = { 
                        compressionAlgo = it
                        pushConfig()
                    }
                )
            }
            
            Divider(color = ClassicColors.SurfaceVariant)

            // Swap
             Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Swap File",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.Secondary
                )
                Switch(
                    checked = swapEnabled,
                    onCheckedChange = { 
                        swapEnabled = it
                         if (it && swapSize < 512f) swapSize = 2048f
                        pushConfig()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ClassicColors.Primary,
                        checkedTrackColor = ClassicColors.Primary.copy(alpha = 0.5f)
                    )
                )
            }

            if (swapEnabled) {
                ClassicSlider(
                    label = "Swap Size",
                    value = swapSize,
                    valueRange = 512f..16384f,
                    steps = 31,
                    valueDisplay = "${String.format(Locale.US, "%.1f", swapSize / 1024f)} GB",
                    onValueChange = { swapSize = it },
                    onValueFinished = { pushConfig() }
                )
            }
            
            // Add a manual APPLY button for heavy operations (since ZRAM/Swap init is heavy)
            // Or rely on onValueFinished. 
            // Liquid uses explicit Apply button for ZRAM/Swap size changes.
            // Let's add a button at bottom "Apply Configuration" if we want to batch changes?
            // "pushConfig" calls setRAMParameters which likely triggers the changes.
            // In Liquid, `pushConfig` is called after Apply is clicked or param changed.
            
             Button(
                onClick = { pushConfig() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ClassicColors.Primary)
            ) {
                Text("Apply Memory Actions", color = Color.White)
            }
        }
    }
}
