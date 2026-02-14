package id.xms.xtrakernelmanager.ui.screens.functionalrom

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions

import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.HideAccessibilityConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialFunctionalRomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShimokuRom: () -> Unit,
    onNavigateToHideAccessibility: () -> Unit,
    onNavigateToDisplaySize: () -> Unit,
    viewModel: FunctionalRomViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = androidx.compose.ui.platform.LocalContext.current

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.functional_rom_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ROM Info Section
            item {
                uiState.romInfo?.let { romInfo ->
                    RomInfoCard(
                        romName = romInfo.displayName,
                        androidVersion = romInfo.androidVersion,
                        brand = romInfo.systemBrand,
                        isShimoku = romInfo.isShimokuRom
                    )
                }
            }

            // Universal Features Group
            item {
                Text(
                    text = "Universal Features",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        MaterialSettingsItem(
                            title = "Developer Options",
                            subtitle = if (checkDeveloperOptionsEnabled()) "Enabled" else "Disabled",
                            icon = Icons.Default.DeveloperMode,
                            iconTint = Color(0xFF5C6BC0), // Indigo
                            onClick = { 
                                handleDeveloperOptionsClick(context)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        
                        DPIChangerItem(onClick = onNavigateToDisplaySize)
                        
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        MaterialSettingsItem(
                            title = "Hide Accessibility Service",
                            subtitle = if (uiState.hideAccessibilityConfig.isEnabled) {
                                "${uiState.hideAccessibilityConfig.currentTab.displayName} • ${getTotalSelectedApps(uiState.hideAccessibilityConfig)} apps"
                            } else "Disabled",
                            icon = Icons.Default.VisibilityOff,
                            iconTint = Color(0xFFEF5350), // Red
                            onClick = onNavigateToHideAccessibility
                        )
                    }
                }
            }

            // ROM Specific Features Group
            item {
                Text(
                    text = "ROM-Specific Features",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MaterialSettingsItem(
                        title = "Shimoku ROM Features",
                        subtitle = if (uiState.isShimokuRom) "Available" else "Locked",
                        icon = Icons.Default.Verified,
                        iconTint = Color(0xFF66BB6A), // Green
                        onClick = onNavigateToShimokuRom,
                        enabled = true // Always clickable to show lock message
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun RomInfoCard(
    romName: String,
    androidVersion: String,
    brand: String,
    isShimoku: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isShimoku) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isShimoku) Icons.Default.Verified else Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (isShimoku) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = romName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isShimoku) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Android $androidVersion • $brand",
                    style = MaterialTheme.typography.bodyMedium,
                    color = (if (isShimoku) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer).copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun MaterialSettingsItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = iconTint.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

// Helpers duplicated/adapted from original file for standalone functionality within this module
@Composable
private fun DPIChangerItem(onClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val currentSmallestWidth = remember {
        context.resources.configuration.smallestScreenWidthDp
    }
    
    MaterialSettingsItem(
        title = "Display Size Changer",
        subtitle = "Current: $currentSmallestWidth dp",
        icon = Icons.Default.AspectRatio, // Changed icon for variety
        iconTint = Color(0xFF26A69A), // Teal
        onClick = onClick
    )
}

// Reusing the DPI Dialog logic removed since we moved to a dedicated screen


@Composable
private fun checkDeveloperOptionsEnabled(): Boolean {
    val context = androidx.compose.ui.platform.LocalContext.current
    return android.provider.Settings.Global.getInt(
        context.contentResolver,
        android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
        0
    ) == 1
}

private fun handleDeveloperOptionsClick(context: android.content.Context) {
    val isDeveloperEnabled = android.provider.Settings.Global.getInt(
        context.contentResolver,
        android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
        0
    ) == 1

    if (isDeveloperEnabled) {
        try {
            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            context.startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Unable to open Developer Options", android.widget.Toast.LENGTH_SHORT).show()
        }
    } else {
        android.widget.Toast.makeText(context, "Please enable Developer Options in Settings > About Phone", android.widget.Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun getTotalSelectedApps(config: HideAccessibilityConfig): Int {
  return config.appsToHide.size + config.detectorApps.size
}
