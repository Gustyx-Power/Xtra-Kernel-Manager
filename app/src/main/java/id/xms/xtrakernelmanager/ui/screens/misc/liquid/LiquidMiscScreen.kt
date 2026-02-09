package id.xms.xtrakernelmanager.ui.screens.misc.liquid

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import id.xms.xtrakernelmanager.ui.components.PillCard
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import id.xms.xtrakernelmanager.ui.screens.misc.components.GameMonitorViewModel
import id.xms.xtrakernelmanager.ui.screens.misc.material.*
import kotlinx.coroutines.launch

@Composable
fun LiquidMiscScreen(
    viewModel: MiscViewModel,
    onNavigateToFunctionalRom: () -> Unit = {},
    onNavigateToAppPicker: () -> Unit = {},
) {
    var currentScreen by remember { mutableStateOf("main") }
    var showGameControl by remember { mutableStateOf(false) }
    var showGameMonitor by remember { mutableStateOf(false) }
    var showProcessManager by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val gameMonitorViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
        GameMonitorViewModel(context, viewModel.preferencesManager)
    }
    
    // Show screens based on state
    when {
        showGameControl -> LiquidGameControlScreen(
            viewModel = viewModel,
            gameMonitorViewModel = gameMonitorViewModel,
            onBack = { showGameControl = false }
        )
        showGameMonitor -> MaterialGameMonitorScreen(
            viewModel = gameMonitorViewModel,
            onBack = { showGameMonitor = false }
        )
        showProcessManager -> id.xms.xtrakernelmanager.ui.screens.misc.liquid.LiquidProcessManagerScreen(
            viewModel = viewModel,
            onBack = { showProcessManager = false }
        )
        else -> {
            androidx.compose.animation.AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    if (targetState != "main") {
                        slideInHorizontally { it } + fadeIn() togetherWith 
                        slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith 
                        slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "misc_screen_transition"
            ) { screen ->
                when (screen) {
                    "main" -> LiquidMiscMainScreen(
                        viewModel = viewModel,
                        onNavigateToBattery = { currentScreen = "battery" },
                        onNavigateToGameControl = { 
                            showGameControl = true
                        },
                        onNavigateToDisplay = { currentScreen = "display" },
                        onNavigateToFunctionalRom = onNavigateToFunctionalRom,
                        onNavigateToProcessManager = { showProcessManager = true }
                    )
                    "battery" -> LiquidBatteryInformationScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "main" }
                    )
                    "display" -> LiquidDisplayDetailScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "main" }
                    )
                }
            }
        }
    }
}

@Composable
fun LiquidMiscMainScreen(
    viewModel: MiscViewModel,
    onNavigateToBattery: () -> Unit,
    onNavigateToGameControl: () -> Unit,
    onNavigateToDisplay: () -> Unit,
    onNavigateToFunctionalRom: () -> Unit,
    onNavigateToProcessManager: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
   
    if (showSELinuxDialog && id.xms.xtrakernelmanager.BuildConfig.ENABLE_ROOT_FEATURES) {
        AlertDialog(
            onDismissRequest = { showSELinuxDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = if (isEnforcing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "SELinux Mode",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Current status: $selinuxStatus",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (!isRooted) {
                        Text(
                            text = "Root access required to change SELinux mode.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isEnforcing) "Enforcing" else "Permissive",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isEnforcing) "Security policies active" else "Policies not enforced",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            if (selinuxLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Switch(
                                    checked = isEnforcing,
                                    onCheckedChange = { viewModel.setSELinuxMode(it) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSELinuxDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
   
    val liquidBlobColors = listOf(
        Color(0xFF4A9B8E), 
        Color(0xFF8BA8D8), 
        Color(0xFF6BC4E8)  
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Background decoration - full size like tuning screens
        id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament(
            modifier = Modifier.fillMaxSize(),
            colors = liquidBlobColors
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            LiquidMiscHeader(modifier = Modifier.padding(bottom = 8.dp))

            // iOS-style Settings List
            LiquidSettingsGroup {
                LiquidSettingsRow(
                    icon = Icons.Default.BatteryStd,
                    iconColor = Color(0xFF34C759),
                    title = "Battery Information",
                    subtitle = "Monitor battery status and usage",
                    onClick = onNavigateToBattery
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(start = 60.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(0.08f)
                )
                
                LiquidSettingsRow(
                    icon = Icons.Default.SportsEsports,
                    iconColor = Color(0xFFAF52DE),
                    title = stringResource(R.string.game_control),
                    subtitle = "Manage game apps & overlay",
                    onClick = onNavigateToGameControl
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(start = 60.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(0.08f)
                )
                
                LiquidSettingsRow(
                    icon = Icons.Default.Palette,
                    iconColor = Color(0xFFFF9500),
                    title = stringResource(R.string.display_settings),
                    subtitle = stringResource(R.string.display_saturation_desc),
                    onClick = onNavigateToDisplay
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(start = 60.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(0.08f)
                )
                
                if (id.xms.xtrakernelmanager.BuildConfig.ENABLE_ROOT_FEATURES) {
                    LiquidSettingsRow(
                        icon = Icons.Default.Shield,
                        iconColor = if (isEnforcing) Color(0xFF34C759) else Color(0xFFFF3B30),
                        title = "SELinux",
                        subtitle = if (isRooted) selinuxStatus else "Root required",
                        badge = selinuxStatus,
                        onClick = { showSELinuxDialog = true }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 60.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(0.08f)
                    )
                }
                */
                
                LiquidSettingsRow(
                    icon = Icons.Default.Memory,
                    iconColor = Color(0xFF5856D6),
                    title = "Processes",
                    subtitle = "View & Kill Apps",
                    onClick = onNavigateToProcessManager
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(start = 60.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(0.08f)
                )
                
                LiquidSettingsRow(
                    icon = Icons.Default.Extension,
                    iconColor = Color(0xFF007AFF),
                    title = stringResource(R.string.functional_rom_card_title),
                    subtitle = stringResource(R.string.functional_rom_card_desc),
                    badge = "Universal",
                    onClick = onNavigateToFunctionalRom
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun LiquidMiscHeader(modifier: Modifier = Modifier) {
    id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        onClick = {}
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(id.xms.xtrakernelmanager.ui.theme.NeonPurple.copy(alpha = 0.85f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Miscellaneous",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    
                    // Badge
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White
                        )
                    }
                }

                // Icon
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Misc",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidSettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFF1E293B).copy(alpha = 0.85f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
fun LiquidSettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    onClick: () -> Unit
) {
    val isLightTheme = !isSystemInDarkTheme()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp), 
        horizontalArrangement = Arrangement.spacedBy(14.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with colored background
        Box(
            modifier = Modifier
                .size(36.dp) 
                .clip(RoundedCornerShape(10.dp)) 
                .background(iconColor.copy(alpha = 0.2f)), // Use standard opacity for visibility on dark
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp), 
                tint = iconColor
            )
        }
        
        // Title & Subtitle
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
        
        // Badge (optional)
        if (badge != null) {
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = CircleShape
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White
                )
            }
        }
        
        // Chevron
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(22.dp), 
            tint = Color.White.copy(alpha = 0.3f)
        )
    }
}

