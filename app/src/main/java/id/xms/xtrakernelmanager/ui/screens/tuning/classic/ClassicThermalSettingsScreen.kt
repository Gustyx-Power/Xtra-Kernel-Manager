package id.xms.xtrakernelmanager.ui.screens.tuning.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.ThermalPolicyPresets
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicThermalSettingsScreen(
    viewModel: TuningViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToIndexSelection: () -> Unit,
    onNavigateToPolicySelection: () -> Unit
) {
    val selectedThermalPolicy by viewModel.getCpuLockThermalPolicy().collectAsState(initial = "Policy B (Balanced)")
    val prefsThermal by viewModel.preferencesManager.getThermalPreset().collectAsState(initial = "Not Set")
    val prefsOnBoot by viewModel.preferencesManager.getThermalSetOnBoot().collectAsState(initial = false)

    Scaffold(
        containerColor = ClassicColors.Background,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ClassicColors.Surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = ClassicColors.OnSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Thermal Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Hero Section
            item(key = "hero_card") {
                ClassicHeroThermalCard(
                    prefsThermal = prefsThermal,
                    selectedPolicy = selectedThermalPolicy
                )
            }
            
            // Thermal Index Section
            item(key = "thermal_index") {
                ClassicThermalIndexCard(
                    prefsThermal = prefsThermal,
                    prefsOnBoot = prefsOnBoot,
                    onShowDialog = onNavigateToIndexSelection,
                    viewModel = viewModel
                )
            }
            
            // Thermal Policy Section
            item(key = "thermal_policy") {
                ClassicThermalPolicyCard(
                    selectedPolicy = selectedThermalPolicy,
                    onShowDialog = onNavigateToPolicySelection
                )
            }
            
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun ClassicHeroThermalCard(
    prefsThermal: String,
    selectedPolicy: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Primary.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Central Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(ClassicColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = null,
                    tint = ClassicColors.Background,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Text(
                text = "Thermal Control Center",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface
            )
            
            // Status indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ClassicStatusChip(
                    label = "Index",
                    value = prefsThermal,
                    color = ClassicColors.Error
                )
                ClassicStatusChip(
                    label = "Policy",
                    value = selectedPolicy.take(8) + if (selectedPolicy.length > 8) "..." else "",
                    color = ClassicColors.Primary
                )
            }
        }
    }
}

@Composable
private fun ClassicStatusChip(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp, 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = ClassicColors.OnSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ClassicThermalIndexCard(
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ClassicColors.Error.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = thermalIcon,
                        contentDescription = null,
                        tint = ClassicColors.Error,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Thermal Index",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                    Text(
                        text = stringResource(presetMap[prefsThermal] ?: R.string.thermal_not_set),
                        style = MaterialTheme.typography.bodyLarge,
                        color = ClassicColors.Error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            HorizontalDivider(
                color = ClassicColors.OnSurface.copy(alpha = 0.1f),
                thickness = 1.dp
            )
            
            // Interactive Selection Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowDialog() },
                shape = RoundedCornerShape(12.dp),
                color = ClassicColors.Error.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    ClassicColors.Error.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                            tint = ClassicColors.Error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Change Thermal Index",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = ClassicColors.OnSurface
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = ClassicColors.Error
                    )
                }
            }
            
            // Set on Boot Toggle
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ClassicColors.Background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = ClassicColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.set_on_boot),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = ClassicColors.OnSurface
                            )
                            Text(
                                text = "Apply thermal index on device startup",
                                style = MaterialTheme.typography.bodySmall,
                                color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Switch(
                        checked = prefsOnBoot,
                        onCheckedChange = { viewModel.setThermalPreset(prefsThermal, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ClassicColors.Primary,
                            checkedTrackColor = ClassicColors.Primary.copy(alpha = 0.5f),
                            uncheckedThumbColor = ClassicColors.OnSurface.copy(alpha = 0.4f),
                            uncheckedTrackColor = ClassicColors.OnSurface.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassicThermalPolicyCard(
    selectedPolicy: String,
    onShowDialog: () -> Unit
) {
    val currentPolicy = remember(selectedPolicy) { 
        ThermalPolicyPresets.getPolicyByName(selectedPolicy) 
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ClassicColors.Primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = ClassicColors.Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Thermal Policy",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                    Text(
                        text = selectedPolicy,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ClassicColors.Primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            HorizontalDivider(
                color = ClassicColors.OnSurface.copy(alpha = 0.1f),
                thickness = 1.dp
            )
            
            // Policy Details
            currentPolicy?.let { policy ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ClassicColors.Background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Emergency threshold
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
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = ClassicColors.Error
                                )
                                Text(
                                    text = "Emergency",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ClassicColors.OnSurface.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = "${policy.emergencyThreshold}°C",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ClassicColors.Error
                            )
                        }
                        
                        // Warning threshold
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
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = ClassicColors.Moderate
                                )
                                Text(
                                    text = "Warning",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ClassicColors.OnSurface.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = "${policy.warningThreshold}°C",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ClassicColors.Moderate
                            )
                        }
                        
                        // Restore threshold
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
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = ClassicColors.Secondary
                                )
                                Text(
                                    text = "Restore",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ClassicColors.OnSurface.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = "${policy.restoreThreshold}°C",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ClassicColors.Secondary
                            )
                        }
                        
                        // Critical threshold
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
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = ClassicColors.Error
                                )
                                Text(
                                    text = "Critical",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ClassicColors.OnSurface.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = "${policy.criticalThreshold}°C",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ClassicColors.Error
                            )
                        }
                    }
                }
            }
            
            // Change Policy Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowDialog() },
                shape = RoundedCornerShape(12.dp),
                color = ClassicColors.Primary.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    ClassicColors.Primary.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                            tint = ClassicColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Change Thermal Policy",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = ClassicColors.OnSurface
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = ClassicColors.Primary
                    )
                }
            }
        }
    }
}
