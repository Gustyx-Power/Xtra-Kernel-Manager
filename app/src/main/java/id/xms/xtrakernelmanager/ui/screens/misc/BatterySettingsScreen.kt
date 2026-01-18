package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import id.xms.xtrakernelmanager.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatterySettingsScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Collect states
    val notifIconType by viewModel.batteryNotifIconType.collectAsState()
    val notifRefreshRate by viewModel.batteryNotifRefreshRate.collectAsState()
    val notifSecureLockScreen by viewModel.batteryNotifSecureLockScreen.collectAsState()
    val notifHighPriority by viewModel.batteryNotifHighPriority.collectAsState()
    val notifForceOnTop by viewModel.batteryNotifForceOnTop.collectAsState()
    val notifDontUpdateScreenOff by viewModel.batteryNotifDontUpdateScreenOff.collectAsState()
    
    val statsActiveIdle by viewModel.batteryStatsActiveIdle.collectAsState()
    val statsScreen by viewModel.batteryStatsScreen.collectAsState()
    val statsAwakeSleep by viewModel.batteryStatsAwakeSleep.collectAsState()
    


    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.battery_settings_title),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp), 
        ) {
            // Notification Settings
            SettingsCategoryHeader(title = stringResource(R.string.battery_notif_category_title))
            
            SettingsGroupCard {
                SettingsListPreference(
                    title = stringResource(R.string.battery_notif_icon_type_title),
                    currentValue = notifIconType,
                    entries = mapOf(
                        "app_icon" to stringResource(R.string.battery_notif_icon_type_app_icon),
                        "circle_percent" to stringResource(R.string.battery_notif_icon_type_circle_percent),
                        "percent_only" to stringResource(R.string.battery_notif_icon_type_percent_only),
                        "temp" to stringResource(R.string.battery_notif_icon_type_temp),
                        "percent_temp" to stringResource(R.string.battery_notif_icon_type_percent_temp),
                        "current" to stringResource(R.string.battery_notif_icon_type_current),
                        "voltage" to stringResource(R.string.battery_notif_icon_type_voltage),
                        "power" to stringResource(R.string.battery_notif_icon_type_power),
                        "transparent" to stringResource(R.string.battery_notif_icon_type_transparent)
                    ),
                    onValueChange = { viewModel.setBatteryNotifIconType(it) }
                )
                
                 SettingsListPreference(
                    title = stringResource(R.string.battery_notif_refresh_rate_title),
                    currentValue = notifRefreshRate.toString(),
                    entries = mapOf(
                        "1" to stringResource(R.string.battery_notif_refresh_rate_1s),
                        "5" to stringResource(R.string.battery_notif_refresh_rate_5s),
                        "10" to stringResource(R.string.battery_notif_refresh_rate_10s),
                        "15" to stringResource(R.string.battery_notif_refresh_rate_15s),
                        "30" to stringResource(R.string.battery_notif_refresh_rate_30s),
                        "60" to stringResource(R.string.battery_notif_refresh_rate_1m)
                    ),
                    onValueChange = { viewModel.setBatteryNotifRefreshRate(it.toInt()) }
                )

                SettingsSwitchPreference(
                    title = stringResource(R.string.battery_notif_secure_lock_screen),
                    description = stringResource(R.string.battery_notif_secure_lock_screen_desc),
                    checked = notifSecureLockScreen,
                    onCheckedChange = { viewModel.setBatteryNotifSecureLockScreen(it) }
                )

                SettingsSwitchPreference(
                    title = stringResource(R.string.battery_notif_high_priority),
                    description = stringResource(R.string.battery_notif_high_priority_desc),
                    checked = notifHighPriority,
                    onCheckedChange = { viewModel.setBatteryNotifHighPriority(it) }
                )

                SettingsSwitchPreference(
                    title = stringResource(R.string.battery_notif_force_on_top),
                    description = stringResource(R.string.battery_notif_force_on_top_desc),
                    checked = notifForceOnTop,
                    onCheckedChange = { viewModel.setBatteryNotifForceOnTop(it) }
                )

                SettingsSwitchPreference(
                    title = stringResource(R.string.battery_stats_dont_update_screen_off),
                    description = null,
                    checked = notifDontUpdateScreenOff,
                    onCheckedChange = { viewModel.setBatteryNotifDontUpdateScreenOff(it) }
                )
            }

            // Statistics Settings
            SettingsCategoryHeader(title = "Statistics settings")
            
            SettingsGroupCard {
                SettingsSwitchPreference(
                    title = stringResource(R.string.battery_stats_active_idle),
                    description = "Configure what statistics are displayed", // Reusing user screenshot text/context
                    checked = statsActiveIdle,
                    onCheckedChange = { viewModel.setBatteryStatsActiveIdle(it) }
                )
                SettingsSwitchPreference(
                    title = stringResource(R.string.battery_stats_screen),
                    description = null,
                    checked = statsScreen,
                    onCheckedChange = { viewModel.setBatteryStatsScreen(it) }
                )
                SettingsSwitchPreference(
                    title = stringResource(R.string.battery_stats_awake_sleep),
                    description = null,
                    checked = statsAwakeSleep,
                    onCheckedChange = { viewModel.setBatteryStatsAwakeSleep(it) }
                )
            }


        }
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 24.dp, bottom = 12.dp, top = 24.dp)
    )
}

@Composable
fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(24.dp) // Expressive shape
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsSwitchPreference(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { 
            Text(
                title, 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            ) 
        },
        supportingContent = if (description != null) {
            { Text(description, style = MaterialTheme.typography.bodyMedium) }
        } else null,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null // Handled by ListItem click
            )
        },
        modifier = Modifier
            .clickable { onCheckedChange(!checked) }
            .fillMaxWidth(),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun SettingsListPreference(
    title: String,
    currentValue: String,
    entries: Map<String, String>,
    onValueChange: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { 
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            ) 
        },
        supportingContent = {
            Text(
                text = entries[currentValue] ?: currentValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier
            .clickable { showDialog = true }
            .fillMaxWidth(),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )

    if (showDialog) {
        SettingsListDialog(
            title = title,
            currentValue = currentValue,
            entries = entries,
            onDismiss = { showDialog = false },
            onValueChange = {
                onValueChange(it)
                showDialog = false
            }
        )
    }
}

@Composable
fun SettingsListDialog(
    title: String,
    currentValue: String,
    entries: Map<String, String>,
    onDismiss: () -> Unit,
    onValueChange: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    entries.forEach { (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onValueChange(key) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (key == currentValue),
                                onClick = null // Handled by Row click
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                
                Row(
                   modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                   horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(android.R.string.cancel))
                    }
                }
            }
        }
    }
}
