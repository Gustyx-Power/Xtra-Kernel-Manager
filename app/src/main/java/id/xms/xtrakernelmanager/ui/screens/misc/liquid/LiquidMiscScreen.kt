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
    var showGameSpace by remember { mutableStateOf(false) }
    var showGameMonitor by remember { mutableStateOf(false) }
    var showProcessManager by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val gameMonitorViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
        GameMonitorViewModel(context, viewModel.preferencesManager)
    }
    
    // Temporary: Show MaterialGameSpaceScreen until liquid version is ready
    when {
        showGameSpace -> MaterialGameSpaceScreen(
            viewModel = viewModel,
            onBack = { showGameSpace = false },
            onAddGames = { onNavigateToAppPicker() },
            onGameMonitorClick = { showGameMonitor = true }
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
                            // Temporary: Show material game space
                            showGameSpace = true
                        },
                        onNavigateToDisplay = { currentScreen = "display" },
                        onNavigateToFunctionalRom = onNavigateToFunctionalRom,
                        onNavigateToProcessManager = { showProcessManager = true }
                    )
                    "battery" -> LiquidBatteryDetailScreen(
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
    val useCase = remember { id.xms.xtrakernelmanager.domain.usecase.FunctionalRomUseCase() }
    
    var isVipCommunity by remember { mutableStateOf<Boolean?>(null) }
    var showSecurityWarning by remember { mutableStateOf(false) }
    var showSELinuxDialog by remember { mutableStateOf(false) }
    
    val selinuxStatus by viewModel.selinuxStatus.collectAsState()
    val isRooted by viewModel.isRootAvailable.collectAsState()
    val selinuxLoading by viewModel.selinuxLoading.collectAsState()
    val isEnforcing = selinuxStatus.equals("Enforcing", ignoreCase = true)

    LaunchedEffect(Unit) {
        isVipCommunity = useCase.checkVipCommunity()
    }

    // Security Warning Dialog
    if (showSecurityWarning) {
        AlertDialog(
            onDismissRequest = { showSecurityWarning = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.security_warning_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.security_warning_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showSecurityWarning = false }) {
                    Text(stringResource(R.string.security_warning_button))
                }
            }
        )
    }
    
    // SELinux Toggle Dialog
    if (showSELinuxDialog) {
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
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Background decoration - full size like tuning screens
        id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament(
            modifier = Modifier.fillMaxSize()
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
                    title = stringResource(R.string.battery_guru),
                    subtitle = stringResource(R.string.battery_guru_desc),
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
                    badge = if (isVipCommunity == true) "VIP" else if (isVipCommunity == false) "Locked" else null,
                    onClick = {
                        if (isVipCommunity == true) {
                            onNavigateToFunctionalRom()
                        } else if (isVipCommunity == false) {
                            showSecurityWarning = true
                        }
                    }
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Badge
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Icon
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Misc",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
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
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
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
            .padding(horizontal = 16.dp, vertical = 14.dp), // Increased from 12dp to 14dp
        horizontalArrangement = Arrangement.spacedBy(14.dp), // Increased from 12dp to 14dp
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with colored background
        Box(
            modifier = Modifier
                .size(36.dp) // Increased from 32dp to 36dp
                .clip(RoundedCornerShape(10.dp)) // Increased from 8dp to 10dp
                .background(iconColor.copy(alpha = if (isLightTheme) 0.15f else 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp), // Increased from 18dp to 20dp
                tint = iconColor
            )
        }
        
        // Title & Subtitle
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp) // Increased from 2dp to 3dp
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        // Badge (optional)
        if (badge != null) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = CircleShape
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Chevron
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(22.dp), // Increased from 20dp to 22dp
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}
