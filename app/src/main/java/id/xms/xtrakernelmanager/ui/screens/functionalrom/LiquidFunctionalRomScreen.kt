package id.xms.xtrakernelmanager.ui.screens.functionalrom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.PhoneAndroid
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.* // already there but good to be safe if I am touching imports
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.HideAccessibilityConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidFunctionalRomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShimokuRom: () -> Unit,
    onNavigateToHideAccessibility: () -> Unit,
    onNavigateToDisplaySize: () -> Unit,
    viewModel: FunctionalRomViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = androidx.compose.ui.platform.LocalContext.current


    // iOS Theme Colors
    val isDark = isSystemInDarkTheme()
    val iosBackgroundColor = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7) // Black in Dark, Light Grey in Light
    val iosGroupColor = if (isDark) Color(0xFF1C1C1E) else Color.White // Dark Grey in Dark, White in Light
    val iosTextColor = if (isDark) Color.White else Color.Black
    val iosSubtextColor = Color(0xFF8E8E93) // Standard iOS Grey
    val iosDividerColor = if (isDark) Color(0xFF38383A) else Color(0xFFC6C6C8)
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = iosBackgroundColor,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.functional_rom_title),
                        fontWeight = FontWeight.SemiBold,
                        color = iosTextColor
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Ideally should be a simple chevron for iOS
                                contentDescription = "Back",
                                tint = Color(0xFF007AFF) // iOS Blue
                            )
                            Text("Back", color = Color(0xFF007AFF), fontSize = 17.sp)
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ROM Info Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                uiState.romInfo?.let { romInfo ->
                    LiquidGroup(backgroundColor = iosGroupColor) {
                        LiquidInfoCell(
                            title = romInfo.displayName,
                            subtitle = "Android ${romInfo.androidVersion} • ${romInfo.systemBrand}",
                            icon = if (romInfo.isShimokuRom) Icons.Default.Verified else Icons.Default.PhoneAndroid,
                            iconColor = if (romInfo.isShimokuRom) Color(0xFF34C759) else Color(0xFF8E8E93),
                            titleColor = iosTextColor,
                            subtitleColor = iosSubtextColor
                        )
                    }
                }
            }

            // Universal Features Group
            item {
                Text(
                    text = "UNIVERSAL FEATURES",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                
                LiquidGroup(backgroundColor = iosGroupColor) {
                    LiquidNavigationCell(
                        title = "Developer Options",
                        status = if (checkDeveloperOptionsEnabled()) "On" else "Off",
                        icon = Icons.Default.DeveloperMode,
                        iconColor = Color(0xFF5856D6), // Indigo
                        onClick = { handleDeveloperOptionsClick(context) },
                        showDivider = true,
                        titleColor = iosTextColor,
                        statusColor = iosSubtextColor,
                        dividerColor = iosDividerColor
                    )
                    
                    LiquidNavigationCell(
                        title = "Display Zoom", // "DPI Changer" in iOS terms basically
                        status = "${androidx.compose.ui.platform.LocalContext.current.resources.configuration.smallestScreenWidthDp} dp",
                        icon = Icons.Default.PhoneAndroid,
                        iconColor = Color(0xFF007AFF), // Blue
                        onClick = onNavigateToDisplaySize,
                        showDivider = true,
                        titleColor = iosTextColor,
                        statusColor = iosSubtextColor,
                        dividerColor = iosDividerColor
                    )
                    
                    LiquidNavigationCell(
                        title = "Hide Accessibility",
                        status = if (uiState.hideAccessibilityConfig.isEnabled) "On" else "Off",
                        icon = Icons.Default.VisibilityOff,
                        iconColor = Color(0xFFFF3B30), // Red
                        onClick = onNavigateToHideAccessibility,
                        showDivider = false,
                        titleColor = iosTextColor,
                        statusColor = iosSubtextColor,
                        dividerColor = iosDividerColor
                    )
                }
                
                Text(
                    text = "Manage system accessibility features visibility.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
            }

            // ROM Specific Features Group
            item {
                Text(
                    text = "ROM FEATURES",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )

                LiquidGroup(backgroundColor = iosGroupColor) {
                    LiquidNavigationCell(
                        title = "Shimoku Features",
                        status = if (uiState.isShimokuRom) "Available" else "Locked",
                        icon = Icons.Default.Verified,
                        iconColor = Color(0xFF34C759), // Green
                        onClick = onNavigateToShimokuRom,
                        showDivider = false,
                        titleColor = iosTextColor,
                        statusColor = iosSubtextColor,
                        dividerColor = iosDividerColor
                    )
                }
            }
             item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun LiquidGroup(
    backgroundColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor),
        content = content
    )
}

@Composable
private fun LiquidInfoCell(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    titleColor: Color,
    subtitleColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = iconColor,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                color = titleColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 15.sp,
                color = subtitleColor
            )
        }
    }
}

@Composable
private fun LiquidNavigationCell(
    title: String,
    status: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    showDivider: Boolean,
    titleColor: Color,
    statusColor: Color,
    dividerColor: Color
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = iconColor,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 17.sp,
                color = titleColor,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 17.sp,
                color = statusColor
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFC7C7CC),
                modifier = Modifier.size(16.dp)
            )
        }
        
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 60.dp),
                color = dividerColor,
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
private fun checkDeveloperOptionsEnabled(): Boolean {
    val context = androidx.compose.ui.platform.LocalContext.current
    return android.provider.Settings.Global.getInt(
        context.contentResolver,
        android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
        0
    ) == 1
}

@Composable
private fun getTotalSelectedApps(config: HideAccessibilityConfig): Int {
  return config.appsToHide.size + config.detectorApps.size
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
        // Need root to enable via shell or guide user
        // Using simplified approach - just attempt to open settings or show toast
        android.widget.Toast.makeText(context, "Please enable Developer Options in Settings > About Phone", android.widget.Toast.LENGTH_LONG).show()
    }
}

// Reusing the DPI Dialog logic removed since we moved to a dedicated screen

