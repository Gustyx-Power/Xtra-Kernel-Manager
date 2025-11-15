package id.xms.xtrakernelmanager.ui.screens.tuning

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.RAMConfig
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard

@Composable
fun RAMControlSection(viewModel: TuningViewModel) {
    GlassmorphicCard {
        Text(
            text = stringResource(R.string.ram_control),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        var swappiness by remember { mutableFloatStateOf(60f) }
        var zramSize by remember { mutableFloatStateOf(0f) }
        var dirtyRatio by remember { mutableFloatStateOf(20f) }
        var minFreeMem by remember { mutableFloatStateOf(0f) }

        Text(
            text = "${stringResource(R.string.swappiness)}: ${swappiness.toInt()}",
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = swappiness,
            onValueChange = { swappiness = it },
            onValueChangeFinished = {
                viewModel.setRAMParameters(
                    RAMConfig(
                        swappiness = swappiness.toInt(),
                        zramSize = zramSize.toInt(),
                        dirtyRatio = dirtyRatio.toInt(),
                        minFreeMem = minFreeMem.toInt()
                    )
                )
            },
            valueRange = 0f..100f,
            steps = 9
        )

        Text(
            text = "${stringResource(R.string.zram_size)}: ${zramSize.toInt()} MB",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Slider(
            value = zramSize,
            onValueChange = { zramSize = it },
            onValueChangeFinished = {
                viewModel.setRAMParameters(
                    RAMConfig(
                        swappiness = swappiness.toInt(),
                        zramSize = zramSize.toInt(),
                        dirtyRatio = dirtyRatio.toInt(),
                        minFreeMem = minFreeMem.toInt()
                    )
                )
            },
            valueRange = 0f..4096f,
            steps = 15
        )

        Text(
            text = "${stringResource(R.string.dirty_ratio)}: ${dirtyRatio.toInt()}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Slider(
            value = dirtyRatio,
            onValueChange = { dirtyRatio = it },
            onValueChangeFinished = {
                viewModel.setRAMParameters(
                    RAMConfig(
                        swappiness = swappiness.toInt(),
                        zramSize = zramSize.toInt(),
                        dirtyRatio = dirtyRatio.toInt(),
                        minFreeMem = minFreeMem.toInt()
                    )
                )
            },
            valueRange = 0f..100f,
            steps = 9
        )

        Text(
            text = "${stringResource(R.string.min_free_memory)}: ${minFreeMem.toInt()} KB",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Slider(
            value = minFreeMem,
            onValueChange = { minFreeMem = it },
            onValueChangeFinished = {
                viewModel.setRAMParameters(
                    RAMConfig(
                        swappiness = swappiness.toInt(),
                        zramSize = zramSize.toInt(),
                        dirtyRatio = dirtyRatio.toInt(),
                        minFreeMem = minFreeMem.toInt()
                    )
                )
            },
            valueRange = 0f..262144f,
            steps = 15
        )
    }
}
