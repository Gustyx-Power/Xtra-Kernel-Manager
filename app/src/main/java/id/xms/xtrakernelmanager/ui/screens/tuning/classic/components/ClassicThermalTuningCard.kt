package id.xms.xtrakernelmanager.ui.screens.tuning.classic.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.screens.home.components.classic.ClassicCard
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@Composable
fun ClassicThermalTuningCard(viewModel: TuningViewModel) {
    val prefsThermal by viewModel.preferencesManager.getThermalPreset().collectAsState(initial = "Not Set")
    val prefsOnBoot by viewModel.preferencesManager.getThermalSetOnBoot().collectAsState(initial = false)

    val presetMap = mapOf(
        "Not Set" to R.string.thermal_not_set,
        "Class 0" to R.string.thermal_class_0,
        "Extreme" to R.string.thermal_extreme,
        "Dynamic" to R.string.thermal_dynamic,
        "Incalls" to R.string.thermal_incalls,
        "Thermal 20" to R.string.thermal_20,
    )
    
    val thermalOptions = presetMap.keys.toList()

    ClassicCard(title = "Thermal Tuning", icon = Icons.Rounded.Thermostat) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            
            // Thermal Preset
            Text(
                text = "Thermal Profile",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.Secondary
            )
            
            ClassicDropdown(
                label = "Current Profile",
                options = thermalOptions,
                selectedOption = prefsThermal,
                onOptionSelected = { newProfile ->
                    viewModel.setThermalPreset(newProfile, prefsOnBoot)
                }
            )

            // Set on Boot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Set on Boot",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurface
                )
                Switch(
                    checked = prefsOnBoot,
                    onCheckedChange = { 
                        viewModel.setThermalPreset(prefsThermal, it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ClassicColors.Primary,
                        checkedTrackColor = ClassicColors.Primary.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}
