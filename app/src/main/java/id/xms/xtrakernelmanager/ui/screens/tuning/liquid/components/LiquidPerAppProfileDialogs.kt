package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.AppProfile
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialog
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialogButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProfileDialog(
    context: Context,
    governors: List<String>,
    thermalPresets: List<String>,
    existingPackages: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (AppProfile) -> Unit,
) {
    var installedApps by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedApp by remember { mutableStateOf<Pair<String, String>?>(null) }
    var selectedGovernor by remember { mutableStateOf(governors.firstOrNull() ?: "schedutil") }
    var selectedThermal by remember { mutableStateOf(thermalPresets.first()) }
    var selectedRefreshRate by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Get device max refresh rate
    val maxRefreshRate = remember { getDeviceMaxRefreshRate(context) }
    val availableRefreshRates = remember(maxRefreshRate) { getAvailableRefreshRates(maxRefreshRate) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val apps = resolveInfos
                .map { it.activityInfo.packageName }
                .distinct()
                .filter { it !in existingPackages }
                .mapNotNull { packageName ->
                    try {
                        val appInfo = pm.getApplicationInfo(packageName, 0)
                        val appName = pm.getApplicationLabel(appInfo).toString()
                        Pair(packageName, appName)
                    } catch (e: Exception) {
                        null
                    }
                }
                .sortedBy { it.second.lowercase() }
            installedApps = apps
            isLoading = false
        }
    }

    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isEmpty()) installedApps
        else installedApps.filter {
            it.second.contains(searchQuery, ignoreCase = true) ||
                it.first.contains(searchQuery, ignoreCase = true)
        }
    }

    LiquidDialog(
        onDismissRequest = onDismiss,
        title = if (selectedApp == null) stringResource(R.string.per_app_profile_add) else selectedApp!!.second,
        content = {
            if (selectedApp == null) {
                // App selection
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Display max refresh rate info if > 60Hz
                    if (maxRefreshRate > 60) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.Speed,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = stringResource(R.string.per_app_profile_display_info, maxRefreshRate),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(stringResource(R.string.per_app_profile_search)) },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                    )

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(filteredApps) { app ->
                                val dialogContentColor = LocalContentColor.current
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { selectedApp = app },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        // App icon
                                        val appIcon = remember(app.first) {
                                            try {
                                                context.packageManager.getApplicationIcon(app.first)
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }

                                        Box(
                                            modifier = Modifier.size(40.dp).clip(CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (appIcon != null) {
                                                AsyncImage(
                                                    model = appIcon,
                                                    contentDescription = app.second,
                                                    modifier = Modifier.size(40.dp),
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier.size(40.dp)
                                                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Text(
                                                        text = app.second.take(1).uppercase(),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                    )
                                                }
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(app.second, fontWeight = FontWeight.Medium, color = dialogContentColor)
                                            Text(
                                                app.first,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = dialogContentColor.copy(alpha = 0.7f),
                                            )
                                        }
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = dialogContentColor,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Profile configuration
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Governor dropdown
                    item {
                        var governorExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = governorExpanded,
                            onExpandedChange = { governorExpanded = it },
                        ) {
                            OutlinedTextField(
                                value = selectedGovernor,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.per_app_profile_governor)) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Memory, null, tint = MaterialTheme.colorScheme.primary)
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(governorExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                            )
                            ExposedDropdownMenu(
                                expanded = governorExpanded,
                                onDismissRequest = { governorExpanded = false },
                            ) {
                                governors.forEach { gov ->
                                    DropdownMenuItem(
                                        text = { Text(gov) },
                                        onClick = {
                                            selectedGovernor = gov
                                            governorExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }

                    // Thermal dropdown
                    item {
                        var thermalExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = thermalExpanded,
                            onExpandedChange = { thermalExpanded = it },
                        ) {
                            OutlinedTextField(
                                value = selectedThermal,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.per_app_profile_thermal)) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Thermostat, null, tint = MaterialTheme.colorScheme.error)
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(thermalExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                            )
                            ExposedDropdownMenu(
                                expanded = thermalExpanded,
                                onDismissRequest = { thermalExpanded = false },
                            ) {
                                thermalPresets.forEach { preset ->
                                    DropdownMenuItem(
                                        text = { Text(preset) },
                                        onClick = {
                                            selectedThermal = preset
                                            thermalExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }

                    // Refresh rate selection (only if device supports > 60Hz)
                    if (availableRefreshRates.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = stringResource(R.string.per_app_profile_refresh_rate),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    // "Not Set" option
                                    RefreshRateChip(
                                        rate = 0,
                                        label = stringResource(R.string.per_app_profile_refresh_rate_not_set),
                                        isSelected = selectedRefreshRate == 0,
                                        onClick = { selectedRefreshRate = 0 },
                                        modifier = Modifier.weight(1f),
                                    )

                                    // Available rates
                                    availableRefreshRates.forEach { rate ->
                                        RefreshRateChip(
                                            rate = rate,
                                            label = "${rate}Hz",
                                            isSelected = selectedRefreshRate == rate,
                                            onClick = { selectedRefreshRate = rate },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }

                                // Warning about ROM compatibility
                                Text(
                                    text = stringResource(R.string.per_app_profile_refresh_rate_warning),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedApp != null) {
                LiquidDialogButton(
                    text = stringResource(R.string.per_app_profile_add_button),
                    onClick = {
                        selectedApp?.let { app ->
                            onConfirm(
                                AppProfile(
                                    packageName = app.first,
                                    appName = app.second,
                                    governor = selectedGovernor,
                                    thermalPreset = selectedThermal,
                                    refreshRate = selectedRefreshRate,
                                    enabled = true,
                                )
                            )
                        }
                    },
                    isPrimary = true
                )
            }
        },
        dismissButton = {
            LiquidDialogButton(
                text = if (selectedApp == null) stringResource(R.string.per_app_profile_cancel) 
                      else "Back",
                onClick = {
                    if (selectedApp == null) {
                        onDismiss()
                    } else {
                        selectedApp = null
                    }
                },
                isPrimary = false
            )
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    profile: AppProfile,
    governors: List<String>,
    thermalPresets: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (AppProfile) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedGovernor by remember { mutableStateOf(profile.governor) }
    var selectedThermal by remember { mutableStateOf(profile.thermalPreset) }
    var selectedRefreshRate by remember { mutableStateOf(profile.refreshRate) }

    // Get device max refresh rate
    val maxRefreshRate = remember { getDeviceMaxRefreshRate(context) }
    val availableRefreshRates = remember(maxRefreshRate) { getAvailableRefreshRates(maxRefreshRate) }

    LiquidDialog(
        onDismissRequest = onDismiss,
        title = profile.appName,
        content = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Governor dropdown
                item {
                    var governorExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = governorExpanded,
                        onExpandedChange = { governorExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = selectedGovernor,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.per_app_profile_governor)) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Memory, null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(governorExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                        )
                        ExposedDropdownMenu(
                            expanded = governorExpanded,
                            onDismissRequest = { governorExpanded = false },
                        ) {
                            governors.forEach { gov ->
                                DropdownMenuItem(
                                    text = { Text(gov) },
                                    onClick = {
                                        selectedGovernor = gov
                                        governorExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                // Thermal dropdown
                item {
                    var thermalExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = thermalExpanded,
                        onExpandedChange = { thermalExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = selectedThermal,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.per_app_profile_thermal)) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Thermostat, null, tint = MaterialTheme.colorScheme.error)
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(thermalExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                        )
                        ExposedDropdownMenu(
                            expanded = thermalExpanded,
                            onDismissRequest = { thermalExpanded = false },
                        ) {
                            thermalPresets.forEach { preset ->
                                DropdownMenuItem(
                                    text = { Text(preset) },
                                    onClick = {
                                        selectedThermal = preset
                                        thermalExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                // Refresh rate selection (only if device supports > 60Hz)
                if (availableRefreshRates.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = stringResource(R.string.per_app_profile_refresh_rate),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                // "Not Set" option
                                RefreshRateChip(
                                    rate = 0,
                                    label = stringResource(R.string.per_app_profile_refresh_rate_not_set),
                                    isSelected = selectedRefreshRate == 0,
                                    onClick = { selectedRefreshRate = 0 },
                                    modifier = Modifier.weight(1f),
                                )

                                // Available rates
                                availableRefreshRates.forEach { rate ->
                                    RefreshRateChip(
                                        rate = rate,
                                        label = "${rate}Hz",
                                        isSelected = selectedRefreshRate == rate,
                                        onClick = { selectedRefreshRate = rate },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }

                            // Warning about ROM compatibility
                            Text(
                                text = stringResource(R.string.per_app_profile_refresh_rate_warning),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            LiquidDialogButton(
                text = stringResource(R.string.per_app_profile_save_button),
                onClick = {
                    onConfirm(
                        profile.copy(
                            governor = selectedGovernor,
                            thermalPreset = selectedThermal,
                            refreshRate = selectedRefreshRate,
                        )
                    )
                },
                isPrimary = true
            )
        },
        dismissButton = {
            LiquidDialogButton(
                text = stringResource(R.string.per_app_profile_cancel),
                onClick = onDismiss,
                isPrimary = false
            )
        }
    )
}

@Composable
private fun RefreshRateChip(
    rate: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = tween(200),
        label = "bg_color",
    )
    
    val parentContentColor = LocalContentColor.current

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        animationSpec = tween(200),
        label = "border_color",
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(150),
        label = "scale",
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else parentContentColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// Helper functions
private fun getDeviceMaxRefreshRate(context: Context): Int {
    return try {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = context.display ?: windowManager.defaultDisplay
            display.supportedModes.maxOfOrNull { it.refreshRate.toInt() } ?: 60
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            display.refreshRate.toInt()
        }
    } catch (e: Exception) {
        60
    }
}

private fun getAvailableRefreshRates(maxRate: Int): List<Int> {
    return when {
        maxRate >= 120 -> listOf(60, 90, 120)
        maxRate >= 90 -> listOf(60, 90)
        else -> emptyList() // 60Hz or less = no refresh rate options
    }
}
