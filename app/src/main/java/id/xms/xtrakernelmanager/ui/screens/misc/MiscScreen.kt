package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.utils.format

@Composable
fun MiscScreen(
    viewModel: MiscViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBatteryInfo by remember { mutableStateOf(false) }
    var showGameControl by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, bottom = 80.dp)
    ) {
        item {
            Text(
                text = "Miscellaneous",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            MiscFeatureCard(
                title = "Battery Information",
                description = "Detailed battery statistics and health",
                icon = Icons.Default.BatteryChargingFull,
                requiresRoot = false,
                onClick = { showBatteryInfo = true }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            MiscFeatureCard(
                title = "Game Control",
                description = "FPS meter and performance overlay",
                icon = Icons.Default.Gamepad,
                requiresRoot = true,
                enabled = uiState.hasRoot,
                onClick = { showGameControl = true }
            )
        }
    }

    if (showBatteryInfo) {
        BatteryInfoDialog(
            batteryInfo = uiState.batteryInfo,
            onDismiss = { showBatteryInfo = false }
        )
    }

    if (showGameControl) {
        GameControlDialog(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { showGameControl = false }
        )
    }
}

@Composable
private fun MiscFeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    requiresRoot: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        backgroundColor = if (enabled) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (enabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    if (requiresRoot) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "ROOT",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                if (requiresRoot && !enabled) {
                    Text(
                        text = "Requires root access",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun BatteryInfoDialog(
    batteryInfo: id.xms.xtrakernelmanager.data.model.BatteryInfo,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Battery Information") },
        text = {
            Column {
                BatteryInfoRow("Level", "${batteryInfo.level}%")
                BatteryInfoRow("Temperature", "${batteryInfo.temperature.format(1)}°C")
                BatteryInfoRow("Voltage", "${batteryInfo.voltage.format(2)}V")
                BatteryInfoRow("Current", "${batteryInfo.current.format(2)}A")
                BatteryInfoRow("Health", batteryInfo.health)
                BatteryInfoRow("Status", batteryInfo.status)
                BatteryInfoRow("Cycle Count", batteryInfo.cycleCount.toString())
                BatteryInfoRow("Capacity", "${batteryInfo.capacityMah} mAh")
                BatteryInfoRow("Design Capacity", "${batteryInfo.designCapacityMah} mAh")
                BatteryInfoRow("Health Percentage", "${batteryInfo.healthPercentage.format(1)}%")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun BatteryInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun GameControlDialog(
    uiState: MiscUiState,
    viewModel: MiscViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Game Control") },
        text = {
            Column {
                Text(
                    text = "Performance Overlay",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Overlay Active")
                    Switch(
                        checked = uiState.overlayState.isActive,
                        onCheckedChange = {
                            if (it) viewModel.startGameOverlay()
                            else viewModel.stopGameOverlay()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Performance Mode",
                    style = MaterialTheme.typography.titleSmall
                )

                listOf("Battery", "Balance", "Performance").forEach { mode ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.overlayState.performanceMode == mode,
                            onClick = { viewModel.setPerformanceMode(mode) }
                        )
                        Text(text = mode)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Do Not Disturb")
                    Switch(
                        checked = uiState.overlayState.dndEnabled,
                        onCheckedChange = { viewModel.toggleDnd(it) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.clearRam() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear RAM")
                }

                if (uiState.overlayState.isActive) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Overlay Stats",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text("FPS: ${uiState.overlayState.fps}")
                    Text("CPU: ${uiState.overlayState.cpuFreq / 1000} MHz")
                    Text("Load: ${uiState.overlayState.cpuLoad.format(1)}%")
                    Text("Battery Temp: ${uiState.overlayState.batteryTemp.format(1)}°C")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
