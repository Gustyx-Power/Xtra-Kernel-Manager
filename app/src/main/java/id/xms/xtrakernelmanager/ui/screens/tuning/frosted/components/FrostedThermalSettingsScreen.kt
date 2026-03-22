package id.xms.xtrakernelmanager.ui.screens.tuning.frosted.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.ThermalPolicyPresets
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament
import id.xms.xtrakernelmanager.ui.components.frosted.FrostedDialog
import id.xms.xtrakernelmanager.ui.components.frosted.FrostedDialogButton
import id.xms.xtrakernelmanager.ui.components.frosted.FrostedToggle
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrostedThermalSettingsScreen(
    viewModel: TuningViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToIndexSelection: () -> Unit,
    onNavigateToPolicySelection: () -> Unit
) {
    val selectedThermalPolicy by viewModel.getCpuLockThermalPolicy().collectAsState(initial = "Policy B (Balanced)")
    val prefsThermal by viewModel.preferencesManager.getThermalPreset().collectAsState(initial = "Not Set")
    val prefsOnBoot by viewModel.preferencesManager.getThermalSetOnBoot().collectAsState(initial = false)

    Box(modifier = Modifier.fillMaxSize()) {
        WavyBlobOrnament(modifier = Modifier.fillMaxSize())
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ModernTopBar(
                    title = stringResource(R.string.thermal_management),
                    onNavigateBack = onNavigateBack
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp)
            ) {
                // Hero Section
                item {
                    HeroThermalCard(
                        prefsThermal = prefsThermal,
                        selectedPolicy = selectedThermalPolicy
                    )
                }
                
                // Thermal Index Section
                item {
                    ModernThermalIndexCard(
                        prefsThermal = prefsThermal,
                        prefsOnBoot = prefsOnBoot,
                        onShowDialog = onNavigateToIndexSelection,
                        viewModel = viewModel
                    )
                }
                
                // Thermal Policy Section
                item {
                    ModernThermalPolicyCard(
                        selectedPolicy = selectedThermalPolicy,
                        onShowDialog = onNavigateToPolicySelection
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernTopBar(
    title: String,
    onNavigateBack: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
private fun HeroThermalCard(
    prefsThermal: String,
    selectedPolicy: String
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        contentPadding = PaddingValues(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Thermostat,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = stringResource(R.string.thermal_control_center),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatusChip(
                    label = stringResource(R.string.thermal_index_label),
                    value = prefsThermal
                )
                StatusChip(
                    label = stringResource(R.string.thermal_policy_label),
                    value = selectedPolicy.take(8) + if (selectedPolicy.length > 8) "..." else ""
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ModernThermalIndexCard(
    prefsThermal: String,
    prefsOnBoot: Boolean,
    onShowDialog: () -> Unit,
    viewModel: TuningViewModel
) {
    val presetMap = remember {
        mapOf(
            "Not Set" to R.string.thermal_not_set,
            "Class 0" to R.string.thermal_class_0,
            "Extreme" to R.string.thermal_extreme,
            "Dynamic" to R.string.thermal_dynamic,
            "Incalls" to R.string.thermal_incalls,
            "Thermal 20" to R.string.thermal_20,
        )
    }
    
    val thermalIcon = remember(prefsThermal) {
        when (prefsThermal) {
            "Class 0" -> Icons.Default.Speed
            "Extreme" -> Icons.Default.Whatshot
            "Dynamic" -> Icons.Default.AutoMode
            "Incalls" -> Icons.Default.Call
            "Thermal 20" -> Icons.Default.LocalFireDepartment
            else -> Icons.Default.Block
        }
    }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = thermalIcon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.thermal_index),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(presetMap[prefsThermal] ?: R.string.thermal_not_set),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            GlassmorphicCard(
                onClick = onShowDialog,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.thermal_change_index),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null
                    )
                }
            }
            
            ModernToggleCard(
                title = stringResource(R.string.set_on_boot),
                subtitle = stringResource(R.string.thermal_apply_index_on_boot),
                checked = prefsOnBoot,
                onCheckedChange = { viewModel.setThermalPreset(prefsThermal, it) },
                icon = Icons.Default.PowerSettingsNew
            )
        }
    }
}

@Composable
private fun ModernThermalPolicyCard(
    selectedPolicy: String,
    onShowDialog: () -> Unit
) {
    val currentPolicy = remember(selectedPolicy) { 
        ThermalPolicyPresets.getPolicyByName(selectedPolicy) 
    }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.thermal_policy),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedPolicy,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Usage note
            Text(
                text = stringResource(R.string.thermal_policy_usage_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            currentPolicy?.let { policy ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThermalDetailRow(
                        icon = Icons.Default.Warning,
                        label = stringResource(R.string.emergency_temp),
                        value = "${policy.emergencyThreshold}°C"
                    )
                    ThermalDetailRow(
                        icon = Icons.Default.Info,
                        label = stringResource(R.string.warning_temp),
                        value = "${policy.warningThreshold}°C"
                    )
                    ThermalDetailRow(
                        icon = Icons.Default.Restore,
                        label = stringResource(R.string.restore_temp),
                        value = "${policy.restoreThreshold}°C"
                    )
                    ThermalDetailRow(
                        icon = Icons.Default.Error,
                        label = stringResource(R.string.critical_temp),
                        value = "${policy.criticalThreshold}°C"
                    )
                }
            }
            
            GlassmorphicCard(
                onClick = onShowDialog,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.change_thermal_policy),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun ThermalDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ModernToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            FrostedToggle(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

