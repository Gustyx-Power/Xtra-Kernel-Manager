package id.xms.xtrakernelmanager.ui.screens.functionalrom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.LottieSwitchControlled
import id.xms.xtrakernelmanager.ui.components.PillCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XiaomiTouchSettingsScreen(
    onNavigateBack: () -> Unit
) {
    // UI State for toggles (UI only, no backend logic)
    var touchGameModeEnabled by remember { mutableStateOf(false) }
    var touchActiveModeEnabled by remember { mutableStateOf(false) }
    var touchUpThresholdEnabled by remember { mutableStateOf(false) }
    var touchToleranceEnabled by remember { mutableStateOf(false) }
    var touchAimSensitivityEnabled by remember { mutableStateOf(false) }
    var touchTapStabilityEnabled by remember { mutableStateOf(false) }
    var touchExpertModeEnabled by remember { mutableStateOf(false) }
    var touchEdgeFilterEnabled by remember { mutableStateOf(false) }
    var touchGripModeEnabled by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with PillCard - consistent with other screens
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = onNavigateBack,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    PillCard(text = stringResource(R.string.xiaomi_touch_settings_title))
                }
            }
        }

        // All toggles wrapped in a single GlassmorphicCard
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Touch Game Mode
                    TouchSettingsToggleItem(
                        title = stringResource(R.string.touch_game_mode),
                        description = if (touchGameModeEnabled)
                            stringResource(R.string.touch_game_mode_desc_enabled)
                        else
                            stringResource(R.string.touch_game_mode_desc_disabled),
                        checked = touchGameModeEnabled,
                        onCheckedChange = { touchGameModeEnabled = it }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Touch Active Mode
                    TouchSettingsToggleItem(
                        title = stringResource(R.string.touch_active_mode),
                        description = if (touchActiveModeEnabled)
                            stringResource(R.string.touch_active_mode_desc_enabled)
                        else
                            stringResource(R.string.touch_active_mode_desc_disabled),
                        checked = touchActiveModeEnabled,
                        onCheckedChange = { touchActiveModeEnabled = it }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Touch Up Threshold
                    TouchSettingsToggleItem(
                        title = stringResource(R.string.touch_up_threshold),
                        description = if (touchUpThresholdEnabled)
                            stringResource(R.string.touch_up_threshold_desc_enabled)
                        else
                            stringResource(R.string.touch_up_threshold_desc_disabled),
                        checked = touchUpThresholdEnabled,
                        onCheckedChange = { touchUpThresholdEnabled = it }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Touch Tolerance
                    TouchSettingsToggleItem(
                        title = stringResource(R.string.touch_tolerance),
                        description = if (touchToleranceEnabled)
                            stringResource(R.string.touch_tolerance_desc_enabled)
                        else
                            stringResource(R.string.touch_tolerance_desc_disabled),
                        checked = touchToleranceEnabled,
                        onCheckedChange = { touchToleranceEnabled = it }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Touch Aim Sensitivity
                    TouchSettingsToggleItem(
                        title = stringResource(R.string.touch_aim_sensitivity),
                        description = if (touchAimSensitivityEnabled)
                            stringResource(R.string.touch_aim_sensitivity_desc_enabled)
                        else
                            stringResource(R.string.touch_aim_sensitivity_desc_disabled),
                        checked = touchAimSensitivityEnabled,
                        onCheckedChange = { touchAimSensitivityEnabled = it }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Touch Tap Stability
                    TouchSettingsToggleItem(
                        title = stringResource(R.string.touch_tap_stability),
                        description = if (touchTapStabilityEnabled)
                            stringResource(R.string.touch_tap_stability_desc_enabled)
                        else
                            stringResource(R.string.touch_tap_stability_desc_disabled),
                        checked = touchTapStabilityEnabled,
                        onCheckedChange = { touchTapStabilityEnabled = it }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Touch Expert Mode
                    TouchSettingsToggleItem(
                        title = stringResource(R.string.touch_expert_mode),
                        description = if (touchExpertModeEnabled)
                            stringResource(R.string.touch_expert_mode_desc_enabled)
                        else
                            stringResource(R.string.touch_expert_mode_desc_disabled),
                        checked = touchExpertModeEnabled,
                        onCheckedChange = { touchExpertModeEnabled = it }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Touch Edge Filter
                    TouchSettingsToggleItem(
                        title = stringResource(R.string.touch_edge_filter),
                        description = if (touchEdgeFilterEnabled)
                            stringResource(R.string.touch_edge_filter_desc_enabled)
                        else
                            stringResource(R.string.touch_edge_filter_desc_disabled),
                        checked = touchEdgeFilterEnabled,
                        onCheckedChange = { touchEdgeFilterEnabled = it }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Touch Grip Mode
                    TouchSettingsToggleItem(
                        title = stringResource(R.string.touch_grip_mode),
                        description = if (touchGripModeEnabled)
                            stringResource(R.string.touch_grip_mode_desc_enabled)
                        else
                            stringResource(R.string.touch_grip_mode_desc_disabled),
                        checked = touchGripModeEnabled,
                        onCheckedChange = { touchGripModeEnabled = it }
                    )
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TouchSettingsToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        LottieSwitchControlled(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            width = 80.dp,
            height = 40.dp,
            scale = 2.2f,
            enabled = enabled
        )
    }
}
