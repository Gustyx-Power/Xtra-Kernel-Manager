package id.xms.xtrakernelmanager.ui.screens.misc.section

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel

data class SaturationPreset(
    val name: String,
    val value: Float,
    val descRes: Int
)

@Composable
fun DisplaySection(
    viewModel: MiscViewModel
) {
    val currentSaturation by viewModel.displaySaturation.collectAsState()
    val isRootAvailable by viewModel.isRootAvailable.collectAsState()
    val applyStatus by viewModel.saturationApplyStatus.collectAsState()

    // Local slider state for smooth interaction
    var sliderValue by remember(currentSaturation) { mutableFloatStateOf(currentSaturation) }

    val presets = remember {
        listOf(
            SaturationPreset("sRGB", 1.0f, R.string.display_mode_srgb_desc),
            SaturationPreset("P3", 1.1f, R.string.display_mode_p3_desc),
            SaturationPreset("Vivid", 1.3f, R.string.display_mode_vivid_desc)
        )
    }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.display_settings),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.display_saturation_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // --- Layout Style Selector ---
            LayoutStyleSelector(viewModel = viewModel)

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Status indicator
            if (applyStatus.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (applyStatus == "Failed" || applyStatus == "Root required")
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = applyStatus,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (applyStatus == "Failed" || applyStatus == "Root required")
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Saturation value display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.display_saturation_level),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = String.format("%.2f", sliderValue),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Saturation slider
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = {
                    viewModel.setDisplaySaturation(sliderValue)
                },
                valueRange = 0.5f..2.0f,
                steps = 29, // 0.05 increments
                enabled = isRootAvailable,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Range labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0.5",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "1.0 (Default)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "2.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Quick presets
            Text(
                text = stringResource(R.string.display_quick_presets),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = kotlin.math.abs(sliderValue - preset.value) < 0.01f
                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                        label = "preset_bg"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurface,
                        label = "preset_content"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(backgroundColor)
                            .clickable(enabled = isRootAvailable) {
                                sliderValue = preset.value
                                viewModel.setDisplaySaturation(preset.value)
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                            Text(
                                text = String.format("%.1f", preset.value),
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Root warning if not available
            if (!isRootAvailable) {
                Text(
                    text = stringResource(R.string.display_requires_root),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Layout Style Selector - Toggle between Legacy (glassmorphic) and Material (pure M3) UI
 */
@Composable
private fun LayoutStyleSelector(
    viewModel: MiscViewModel
) {
    val layoutStyle by viewModel.layoutStyle.collectAsState()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.layout_style),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Text(
            text = stringResource(R.string.layout_style_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Legacy Button
            LayoutStyleButton(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.layout_legacy),
                description = stringResource(R.string.layout_legacy_desc),
                isSelected = layoutStyle == "legacy",
                onClick = { viewModel.setLayoutStyle("legacy") }
            )

            // Material Button
            LayoutStyleButton(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.layout_material),
                description = stringResource(R.string.layout_material_desc),
                isSelected = layoutStyle == "material",
                onClick = { viewModel.setLayoutStyle("material") }
            )
        }
    }
}

@Composable
private fun LayoutStyleButton(
    modifier: Modifier = Modifier,
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceContainerHighest,
        label = "layout_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.onPrimary
        else
            MaterialTheme.colorScheme.onSurface,
        label = "layout_content"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f),
                maxLines = 2
            )
        }
    }
}
