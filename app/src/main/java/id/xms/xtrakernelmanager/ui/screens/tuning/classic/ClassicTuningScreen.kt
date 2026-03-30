package id.xms.xtrakernelmanager.ui.screens.tuning.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.screens.tuning.classic.components.*
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@Composable
fun ClassicTuningScreen(
    viewModel: TuningViewModel,
    preferencesManager: PreferencesManager,
    onNavigate: (String) -> Unit,
    onExportConfig: () -> Unit,
    onImportConfig: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClassicColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.classic_system_tuning_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface
            )
            
            // Menu Dropdown
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = ClassicColors.OnSurface
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = ClassicColors.Surface
                ) {
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = stringResource(R.string.import_profile),
                                color = ClassicColors.OnSurface
                            ) 
                        },
                        onClick = {
                            showMenu = false
                            onImportConfig()
                        },
                        leadingIcon = { 
                            Icon(
                                Icons.Rounded.FolderOpen, 
                                contentDescription = null,
                                tint = ClassicColors.Primary
                            ) 
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = stringResource(R.string.export_profile),
                                color = ClassicColors.OnSurface
                            ) 
                        },
                        onClick = {
                            showMenu = false
                            onExportConfig()
                        },
                        leadingIcon = { 
                            Icon(
                                Icons.Rounded.Save, 
                                contentDescription = null,
                                tint = ClassicColors.Primary
                            ) 
                        }
                    )
                }
            }
        }

        // Performance Hub Header Card
        ClassicPerformanceHubCard()
        
        // CPU Tuning Feature Card
        ClassicTuningFeatureCard(
            icon = Icons.Rounded.Speed,
            titleRes = R.string.classic_cpu_tuning_title,
            descriptionRes = R.string.classic_cpu_tuning_description,
            statusLabelRes = R.string.classic_cpu_tuning_status,
            statusColor = ClassicColors.Primary,
            onClick = { onNavigate("cpu_tuning") }
        )
        
        // GPU Tuning Feature Card
        ClassicTuningFeatureCard(
            icon = Icons.Rounded.Tune,
            titleRes = R.string.classic_gpu_tuning_title,
            descriptionRes = R.string.classic_gpu_tuning_description,
            statusLabelRes = R.string.classic_gpu_tuning_status,
            statusColor = ClassicColors.Good,
            onClick = { /* GPU detail screen - to be implemented */ }
        )
        
        // Memory Tuning Feature Card
        ClassicTuningFeatureCard(
            icon = Icons.Rounded.Memory,
            titleRes = R.string.classic_memory_tuning_title,
            descriptionRes = R.string.classic_memory_tuning_description,
            statusLabelRes = R.string.classic_memory_tuning_status,
            statusColor = ClassicColors.Secondary,
            onClick = { onNavigate("memory_tuning") }
        )
        
        // Thermal Tuning Feature Card
        ClassicTuningFeatureCard(
            icon = Icons.Rounded.Thermostat,
            titleRes = R.string.classic_thermal_tuning_title,
            descriptionRes = R.string.classic_thermal_tuning_description,
            statusLabelRes = R.string.classic_thermal_tuning_status,
            statusColor = ClassicColors.Accent,
            onClick = { onNavigate("material_thermal_settings") }
        )
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}
