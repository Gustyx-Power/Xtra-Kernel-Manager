package id.xms.xtrakernelmanager.ui.screens.functionalrom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.LottieSwitchControlled
import id.xms.xtrakernelmanager.ui.components.PillCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunctionalRomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayIntegrity: () -> Unit = {},
    onNavigateToXiaomiTouch: () -> Unit = {},
    viewModel: FunctionalRomViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    // Force refresh rate value selector state
    var showRefreshRateDialog by remember { mutableStateOf(false) }
    
    // Charging limit value selector state
    var showChargingLimitDialog by remember { mutableStateOf(false) }

    // Loading state
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = stringResource(R.string.loading_features),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            // Header with PillCard
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledIconButton(
                            onClick = onNavigateBack,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        PillCard(text = stringResource(R.string.functional_rom_title))
                    }
                }
            }

            // PlayIntegrity Category
            item {
                CategoryHeader(title = stringResource(R.string.category_play_integrity))
            }
            item {
                ClickableFeatureCard(
                    title = stringResource(R.string.play_integrity_spoofs),
                    description = stringResource(R.string.play_integrity_spoofs_desc),
                    icon = Icons.Default.Security,
                    onClick = onNavigateToPlayIntegrity,
                    enabled = uiState.isVipCommunity
                )
            }

            // Touch & Kernel Category
            item {
                Spacer(modifier = Modifier.height(8.dp))
                CategoryHeader(title = stringResource(R.string.category_touch_kernel))
            }
            item {
                ClickableFeatureCard(
                    title = stringResource(R.string.xiaomi_touch_settings),
                    description = stringResource(R.string.xiaomi_touch_settings_desc),
                    icon = Icons.Default.TouchApp,
                    onClick = onNavigateToXiaomiTouch,
                    enabled = uiState.isVipCommunity
                )
            }
            
            // Touch Boost Toggle
            item {
                ToggleFeatureCard(
                    title = stringResource(R.string.touch_boost),
                    description = if (uiState.touchBoostEnabled)
                        stringResource(R.string.touch_boost_desc_enabled)
                    else
                        stringResource(R.string.touch_boost_desc_disabled),
                    icon = Icons.Default.TouchApp,
                    checked = uiState.touchBoostEnabled,
                    onCheckedChange = { viewModel.setTouchBoost(it) },
                    enabled = uiState.isVipCommunity
                )
            }

            // Display Category
            item {
                Spacer(modifier = Modifier.height(8.dp))
                CategoryHeader(title = stringResource(R.string.category_display))
            }
            item {
                ToggleFeatureCard(
                    title = stringResource(R.string.unlock_additional_nits),
                    description = if (uiState.unlockNitsEnabled)
                        stringResource(R.string.unlock_additional_nits_desc_enabled)
                    else
                        stringResource(R.string.unlock_additional_nits_desc_disabled),
                    icon = Icons.Default.WbSunny,
                    checked = uiState.unlockNitsEnabled,
                    onCheckedChange = { viewModel.setUnlockNits(it) },
                    enabled = uiState.isVipCommunity
                )
            }
            item {
                ToggleFeatureCard(
                    title = stringResource(R.string.dynamic_refresh_rate),
                    description = if (uiState.dynamicRefreshRateEnabled)
                        stringResource(R.string.dynamic_refresh_rate_desc_enabled)
                    else
                        stringResource(R.string.dynamic_refresh_rate_desc_disabled),
                    icon = Icons.Default.Refresh,
                    checked = uiState.dynamicRefreshRateEnabled,
                    onCheckedChange = { viewModel.setDynamicRefreshRate(it) },
                    enabled = uiState.isVipCommunity
                )
            }
            item {
                ToggleFeatureCard(
                    title = stringResource(R.string.force_refresh_rate),
                    description = if (uiState.forceRefreshRateEnabled)
                        stringResource(R.string.force_refresh_rate_desc_enabled)
                    else
                        stringResource(R.string.force_refresh_rate_desc_disabled),
                    icon = Icons.Default.Speed,
                    checked = uiState.forceRefreshRateEnabled,
                    onCheckedChange = { viewModel.setForceRefreshRate(it) },
                    enabled = uiState.isVipCommunity
                )
            }
            item {
                SelectorFeatureCard(
                    title = stringResource(R.string.force_refresh_rate_value),
                    description = stringResource(R.string.force_refresh_rate_value_desc),
                    currentValue = "${uiState.forceRefreshRateValue} Hz",
                    icon = Icons.Default.Tune,
                    enabled = uiState.forceRefreshRateEnabled && uiState.isVipCommunity,
                    onClick = { showRefreshRateDialog = true }
                )
            }
            item {
                ToggleFeatureCard(
                    title = stringResource(R.string.dc_dimming),
                    description = if (uiState.dcDimmingEnabled)
                        stringResource(R.string.dc_dimming_desc_enabled)
                    else
                        stringResource(R.string.dc_dimming_desc_disabled),
                    icon = Icons.Default.Brightness4,
                    checked = uiState.dcDimmingEnabled,
                    onCheckedChange = { viewModel.setDcDimming(it) },
                    enabled = uiState.isVipCommunity
                )
            }

            // System Category
            item {
                Spacer(modifier = Modifier.height(8.dp))
                CategoryHeader(title = stringResource(R.string.category_system))
            }
            item {
                ToggleFeatureCard(
                    title = stringResource(R.string.performance_mode),
                    description = if (uiState.performanceModeEnabled)
                        stringResource(R.string.performance_mode_desc_enabled)
                    else
                        stringResource(R.string.performance_mode_desc_disabled),
                    icon = Icons.Default.RocketLaunch,
                    checked = uiState.performanceModeEnabled,
                    onCheckedChange = { viewModel.setPerformanceMode(it) },
                    enabled = uiState.isVipCommunity
                )
            }

            // Charging Category
            item {
                Spacer(modifier = Modifier.height(8.dp))
                CategoryHeader(title = stringResource(R.string.category_charging))
            }
            
            // Bypass Charging - only show if available
            if (uiState.bypassChargingAvailable) {
                item {
                    ToggleFeatureCard(
                        title = stringResource(R.string.bypass_charging),
                        description = if (uiState.bypassChargingEnabled)
                            stringResource(R.string.bypass_charging_desc_enabled)
                        else
                            stringResource(R.string.bypass_charging_desc_disabled),
                        icon = Icons.Default.BatteryChargingFull,
                        checked = uiState.bypassChargingEnabled,
                        onCheckedChange = { viewModel.setBypassCharging(it) },
                        enabled = uiState.isVipCommunity
                    )
                }
            }
            
            item {
                ToggleFeatureCard(
                    title = stringResource(R.string.smart_charging),
                    description = if (uiState.smartChargingEnabled)
                        stringResource(R.string.smart_charging_desc_enabled)
                    else
                        stringResource(R.string.smart_charging_desc_disabled),
                    icon = Icons.Default.BatteryStd,
                    checked = uiState.smartChargingEnabled,
                    onCheckedChange = { viewModel.setSmartCharging(it) },
                    enabled = uiState.isVipCommunity
                )
            }
            
            // Charging Limit - only show if available
            if (uiState.chargingLimitAvailable) {
                item {
                    ToggleFeatureCard(
                        title = stringResource(R.string.charging_limit),
                        description = if (uiState.chargingLimitEnabled)
                            stringResource(R.string.charging_limit_desc_enabled)
                        else
                            stringResource(R.string.charging_limit_desc_disabled),
                        icon = Icons.Default.BatteryAlert,
                        checked = uiState.chargingLimitEnabled,
                        onCheckedChange = { viewModel.setChargingLimit(it) },
                        enabled = uiState.isVipCommunity
                    )
                }
                item {
                    SelectorFeatureCard(
                        title = stringResource(R.string.charging_limit_value),
                        description = stringResource(R.string.charging_limit_value_desc),
                        currentValue = "${uiState.chargingLimitValue}%",
                        icon = Icons.Default.Tune,
                        enabled = uiState.chargingLimitEnabled && uiState.isVipCommunity,
                        onClick = { showChargingLimitDialog = true }
                    )
                }
            }

            // Lock Screen Category - only show DT2W if available
            if (uiState.dt2wAvailable) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryHeader(title = stringResource(R.string.category_lock_screen))
                }
                item {
                    ToggleFeatureCard(
                        title = stringResource(R.string.double_tap_wake),
                        description = if (uiState.doubleTapWakeEnabled)
                            stringResource(R.string.double_tap_wake_desc_enabled)
                        else
                            stringResource(R.string.double_tap_wake_desc_disabled),
                        icon = Icons.Default.Visibility,
                        checked = uiState.doubleTapWakeEnabled,
                        onCheckedChange = { viewModel.setDoubleTapToWake(it) },
                        enabled = uiState.isVipCommunity
                    )
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

    // Refresh Rate Selection Dialog
    if (showRefreshRateDialog) {
        val refreshRates = listOf(60, 90, 120, 144)
        AlertDialog(
            onDismissRequest = { showRefreshRateDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.force_refresh_rate_value),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    refreshRates.forEach { rate ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.forceRefreshRateValue == rate,
                                onClick = {
                                    viewModel.setForceRefreshRateValue(rate)
                                    showRefreshRateDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$rate Hz",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRefreshRateDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Charging Limit Selection Dialog
    if (showChargingLimitDialog) {
        val chargingLimits = listOf(60, 70, 80, 85, 90, 95)
        AlertDialog(
            onDismissRequest = { showChargingLimitDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.charging_limit_value),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    chargingLimits.forEach { limit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.chargingLimitValue == limit,
                                onClick = {
                                    viewModel.setChargingLimitValue(limit)
                                    showChargingLimitDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$limit%",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChargingLimitDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
private fun ClickableFeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f),
        onClick = if (enabled) onClick else null,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
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
private fun ToggleFeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f),
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            LottieSwitchControlled(
                checked = checked,
                onCheckedChange = if (enabled) onCheckedChange else null,
                width = 80.dp,
                height = 40.dp,
                scale = 2.2f,
                enabled = enabled
            )
        }
    }
}

@Composable
private fun SelectorFeatureCard(
    title: String,
    description: String,
    currentValue: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f),
        onClick = if (enabled) onClick else null,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = currentValue,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
