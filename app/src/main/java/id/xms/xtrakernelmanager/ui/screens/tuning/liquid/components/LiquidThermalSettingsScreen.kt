package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialog
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialogButton
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidToggle
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@Composable
fun LiquidThermalSettingsScreen(viewModel: TuningViewModel, onNavigateBack: () -> Unit) {
    val prefsThermal by viewModel.preferencesManager.getThermalPreset().collectAsState(initial = "Not Set")
    val prefsOnBoot by viewModel.preferencesManager.getThermalSetOnBoot().collectAsState(initial = false)
    val isLightTheme = !isSystemInDarkTheme()
    
    var showDialog by remember { mutableStateOf(false) }

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
    
    val presetIcons = remember {
        mapOf(
            "Not Set" to "❌",
            "Class 0" to "❄️",
            "Extreme" to "🔥",
            "Dynamic" to "⚡",
            "Incalls" to "📞",
            "Thermal 20" to "🌡️",
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background decoration
        WavyBlobOrnament(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Header with back button
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                onClick = onNavigateBack
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp).clickable(onClick = onNavigateBack)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.thermal_control),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Current Preset Card
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isLightTheme) Color(0xFFFF3B30).copy(0.15f)
                                    else Color(0xFFFF453A).copy(0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = presetIcons[prefsThermal] ?: "🌡️",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.thermal_preset),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = stringResource(presetMap[prefsThermal] ?: R.string.thermal_not_set),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isLightTheme) Color(0xFFFF3B30) else Color(0xFFFF453A)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Change Preset Button
                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLightTheme) Color(0xFFFF3B30) else Color(0xFFFF453A),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Change Preset", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Set on Boot Card
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isLightTheme) Color(0xFF007AFF).copy(0.15f)
                                    else Color(0xFF0A84FF).copy(0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = if (isLightTheme) Color(0xFF007AFF) else Color(0xFF0A84FF)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.set_on_boot),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Apply on device startup",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    LiquidToggle(
                        checked = prefsOnBoot,
                        onCheckedChange = { viewModel.setThermalPreset(prefsThermal, it) }
                    )
                }
            }

            // Description Card
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "About Thermal Control",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Thermal presets control how your device manages heat. Choose a preset that matches your usage pattern for optimal performance and temperature management.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    
    // Preset Selection Dialog
    if (showDialog) {
        LiquidDialog(
            onDismissRequest = { showDialog = false },
            title = stringResource(R.string.thermal_select_preset),
            content = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetMap.toList()) { (preset, stringRes) ->
                        val isSelected = preset == prefsThermal
                        Surface(
                            onClick = {
                                viewModel.setThermalPreset(preset, prefsOnBoot)
                                showDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) {
                                if (isLightTheme) Color(0xFFFF3B30).copy(0.2f)
                                else Color(0xFFFF453A).copy(0.25f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest.copy(0.5f)
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = presetIcons[preset] ?: "🌡️",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = stringResource(stringRes),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                    color = if (isSelected) {
                                        if (isLightTheme) Color(0xFFFF3B30) else Color(0xFFFF453A)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (isLightTheme) Color(0xFFFF3B30) else Color(0xFFFF453A)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                LiquidDialogButton(
                    text = "Close",
                    onClick = { showDialog = false },
                    isPrimary = true
                )
            }
        )
    }
}
