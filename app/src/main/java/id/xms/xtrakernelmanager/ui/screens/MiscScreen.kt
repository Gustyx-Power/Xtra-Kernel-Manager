package id.xms.xtrakernelmanager.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.xms.xtrakernelmanager.ui.components.SuperGlassCard
import id.xms.xtrakernelmanager.ui.components.GlassIntensity
import id.xms.xtrakernelmanager.viewmodel.MiscViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiscScreen(
    viewModel: MiscViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val batteryStatsEnabled by viewModel.batteryStatsEnabled.collectAsState()
    val batteryNotificationEnabled by viewModel.batteryNotificationEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Misc Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Battery Stats & Notification Card
        BatteryStatsCard(
            batteryStatsEnabled = batteryStatsEnabled,
            batteryNotificationEnabled = batteryNotificationEnabled,
            onToggleBatteryStats = { enabled ->
                viewModel.toggleBatteryStats(enabled)
            }
        )

        // Additional misc features
        SystemTweaksCard()
    }
}

@Composable
fun BatteryStatsCard(
    batteryStatsEnabled: Boolean,
    batteryNotificationEnabled: Boolean,
    onToggleBatteryStats: (Boolean) -> Unit
) {
    SuperGlassCard(
        modifier = Modifier.fillMaxWidth(),
        glassIntensity = GlassIntensity.Light
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Battery & System Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Battery Stats Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Battery Stats Service",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Monitor battery usage, charging stats, and system metrics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = batteryStatsEnabled,
                    onCheckedChange = onToggleBatteryStats
                )
            }

            // Battery Notification Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Persistent Notification",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Show detailed battery info in notification panel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = batteryNotificationEnabled,
                    onCheckedChange = onToggleBatteryStats,
                    enabled = false // This is controlled by the main battery stats toggle
                )
            }

            if (batteryStatsEnabled) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryStd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Battery Service Active",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = "Monitoring battery level, charging current, voltage, temperature, screen time, and deep sleep statistics.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (!batteryStatsEnabled) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Features Available",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("• Real-time battery level & charging status", style = MaterialTheme.typography.bodySmall)
                            Text("• Charging current & voltage monitoring", style = MaterialTheme.typography.bodySmall)
                            Text("• Battery temperature tracking", style = MaterialTheme.typography.bodySmall)
                            Text("• Screen on/off time statistics", style = MaterialTheme.typography.bodySmall)
                            Text("• Deep sleep & awake time monitoring", style = MaterialTheme.typography.bodySmall)
                            Text("• System uptime tracking", style = MaterialTheme.typography.bodySmall)
                            Text("• Battery drain rate calculation", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemTweaksCard() {
    var animationsEnabled by remember { mutableStateOf(true) }
    var debugMode by remember { mutableStateOf(false) }

    SuperGlassCard(
        modifier = Modifier.fillMaxWidth(),
        glassIntensity = GlassIntensity.Light // No blur for better readability
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "System Tweaks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("System Animations")
                    Text(
                        text = "Enable/disable system animations",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = animationsEnabled,
                    onCheckedChange = { animationsEnabled = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Debug Mode")
                    Text(
                        text = "Enable debug logging",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = debugMode,
                    onCheckedChange = { debugMode = it }
                )
            }
        }
    }
}