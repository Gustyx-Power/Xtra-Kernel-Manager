package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.LottieSwitchControlled
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidThermalSettingsScreen(viewModel: TuningViewModel, onNavigateBack: () -> Unit) {
    val prefsThermal by viewModel.preferencesManager.getThermalPreset().collectAsState(initial = "Not Set")
    val prefsOnBoot by viewModel.preferencesManager.getThermalSetOnBoot().collectAsState(initial = false)
    
    var showDialog by remember { mutableStateOf(false) }

    val presetMap = mapOf(
          "Not Set" to R.string.thermal_not_set,
          "Class 0" to R.string.thermal_class_0,
          "Extreme" to R.string.thermal_extreme,
          "Dynamic" to R.string.thermal_dynamic,
          "Incalls" to R.string.thermal_incalls,
          "Thermal 20" to R.string.thermal_20,
      )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.thermal_control), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
             // Thermal Preset Card
             ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                 Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                     Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Tune, null, tint = MaterialTheme.colorScheme.error)
                        Text(stringResource(R.string.thermal_preset), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                     }
                     Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.errorContainer) {
                        Text(
                            text = stringResource(presetMap[prefsThermal] ?: R.string.thermal_not_set),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                     }
                     
                     HorizontalDivider()
                     
                     // Change Preset Button
                     OutlinedCard(
                        modifier = Modifier.fillMaxWidth().clickable { showDialog = true },
                     ) {
                         Row(
                             modifier = Modifier.padding(16.dp).fillMaxWidth(),
                             horizontalArrangement = Arrangement.SpaceBetween,
                             verticalAlignment = Alignment.CenterVertically
                         ) {
                             Text("Change Preset", fontWeight = FontWeight.SemiBold)
                             Icon(Icons.Default.ChevronRight, null)
                         }
                     }
                     
                     // Set on Boot
                     Row(
                         modifier = Modifier.fillMaxWidth(),
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         Column {
                             Text(stringResource(R.string.set_on_boot), fontWeight = FontWeight.Medium)
                             Text("Apply on device startup", style = MaterialTheme.typography.bodySmall)
                         }
                         LottieSwitchControlled(
                             checked = prefsOnBoot,
                             onCheckedChange = { viewModel.setThermalPreset(prefsThermal, it) },
                             width = 60.dp, height = 30.dp, scale = 1.5f
                         )
                     }
                 }
             }
        }
    }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.thermal_select_preset)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    presetMap.forEach { (preset, stringRes) -> 
                        val isSelected = preset == prefsThermal
                         Card(
                            modifier = Modifier.fillMaxWidth().clickable { 
                                viewModel.setThermalPreset(preset, prefsOnBoot)
                                showDialog = false
                            },
                             colors = CardDefaults.cardColors(
                                containerColor = if(isSelected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                            )
                         ) {
                             Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                  Text(stringResource(stringRes))
                                  if(isSelected) Icon(Icons.Default.Check, null)
                             }
                         }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}
