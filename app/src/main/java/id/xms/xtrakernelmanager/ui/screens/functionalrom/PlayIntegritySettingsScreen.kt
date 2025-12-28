package id.xms.xtrakernelmanager.ui.screens.functionalrom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.LottieSwitchControlled
import id.xms.xtrakernelmanager.ui.components.PillCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayIntegritySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: FunctionalRomViewModel? = null
) {
    // Collect state from ViewModel if available, otherwise use local state
    val uiState = viewModel?.uiState?.collectAsState()?.value
    
    // Local UI State for toggles (fallback when no ViewModel)
    var playIntegrityFixEnabled by remember { mutableStateOf(uiState?.playIntegrityFixEnabled ?: false) }
    var spoofBootloaderEnabled by remember { mutableStateOf(uiState?.spoofBootloaderEnabled ?: false) }
    var gamePropsEnabled by remember { mutableStateOf(uiState?.gamePropsEnabled ?: false) }
    var unlimitedPhotosEnabled by remember { mutableStateOf(uiState?.unlimitedPhotosEnabled ?: false) }
    var netflixSpoofEnabled by remember { mutableStateOf(uiState?.netflixSpoofEnabled ?: false) }
    // Auto Update PIF - UI only for now
    var autoUpdatePifEnabled by remember { mutableStateOf(false) }

    // Sync with ViewModel state
    LaunchedEffect(uiState) {
        uiState?.let {
            playIntegrityFixEnabled = it.playIntegrityFixEnabled
            spoofBootloaderEnabled = it.spoofBootloaderEnabled
            gamePropsEnabled = it.gamePropsEnabled
            unlimitedPhotosEnabled = it.unlimitedPhotosEnabled
            netflixSpoofEnabled = it.netflixSpoofEnabled
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with PillCard - consistent with other screens
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
                    PillCard(text = stringResource(R.string.play_integrity_settings_title))
                }
            }
        }

        // All toggles wrapped in a single GlassmorphicCard
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Play Integrity Fix
                    SettingsToggleItem(
                        title = stringResource(R.string.play_integrity_fix),
                        description = if (playIntegrityFixEnabled)
                            stringResource(R.string.play_integrity_fix_desc_enabled)
                        else
                            stringResource(R.string.play_integrity_fix_desc_disabled),
                        checked = playIntegrityFixEnabled,
                        onCheckedChange = { 
                            playIntegrityFixEnabled = it
                            viewModel?.setPlayIntegrityFix(it)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Spoof Bootloader Status
                    SettingsToggleItem(
                        title = stringResource(R.string.spoof_bootloader),
                        description = if (spoofBootloaderEnabled)
                            stringResource(R.string.spoof_bootloader_desc_enabled)
                        else
                            stringResource(R.string.spoof_bootloader_desc_disabled),
                        checked = spoofBootloaderEnabled,
                        onCheckedChange = { 
                            spoofBootloaderEnabled = it
                            viewModel?.setSpoofBootloader(it)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Auto Update PIF.apk
                    SettingsToggleItem(
                        title = stringResource(R.string.auto_update_pif),
                        description = if (autoUpdatePifEnabled)
                            stringResource(R.string.auto_update_pif_desc_enabled)
                        else
                            stringResource(R.string.auto_update_pif_desc_disabled),
                        checked = autoUpdatePifEnabled,
                        onCheckedChange = { autoUpdatePifEnabled = it }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Game Props
                    SettingsToggleItem(
                        title = stringResource(R.string.game_props),
                        description = if (gamePropsEnabled)
                            stringResource(R.string.game_props_desc_enabled)
                        else
                            stringResource(R.string.game_props_desc_disabled),
                        checked = gamePropsEnabled,
                        onCheckedChange = { 
                            gamePropsEnabled = it
                            viewModel?.setGameProps(it)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Unlimited Google Photos Backup
                    SettingsToggleItem(
                        title = stringResource(R.string.unlimited_photos_backup),
                        description = if (unlimitedPhotosEnabled)
                            stringResource(R.string.unlimited_photos_backup_desc_enabled)
                        else
                            stringResource(R.string.unlimited_photos_backup_desc_disabled),
                        checked = unlimitedPhotosEnabled,
                        onCheckedChange = { 
                            unlimitedPhotosEnabled = it
                            viewModel?.setUnlimitedPhotos(it)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Netflix Spoof
                    SettingsToggleItem(
                        title = stringResource(R.string.netflix_spoof),
                        description = if (netflixSpoofEnabled)
                            stringResource(R.string.netflix_spoof_desc_enabled)
                        else
                            stringResource(R.string.netflix_spoof_desc_disabled),
                        checked = netflixSpoofEnabled,
                        onCheckedChange = { 
                            netflixSpoofEnabled = it
                            viewModel?.setNetflixSpoof(it)
                        }
                    )
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
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
        Spacer(modifier = Modifier.width(16.dp))
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
