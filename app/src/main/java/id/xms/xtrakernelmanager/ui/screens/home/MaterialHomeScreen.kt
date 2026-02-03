package id.xms.xtrakernelmanager.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.model.PowerAction
import id.xms.xtrakernelmanager.ui.screens.home.components.ExpandablePowerFab
import id.xms.xtrakernelmanager.ui.screens.home.components.SettingsSheet
import id.xms.xtrakernelmanager.ui.screens.home.components.material.*
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Material Home Screen - Modular Layout with Dynamic Colors (Material You)
 * All components are separated into individual files for better maintainability
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialHomeScreen(
    preferencesManager: PreferencesManager,
    viewModel: HomeViewModel = viewModel(),
    currentProfile: String = "Balance",
    onProfileChange: (String) -> Unit = {},
    onPowerAction: (PowerAction) -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    val context = LocalContext.current
    
    // Remove custom status bar - use system status bar instead

    // Bottom Sheet State
    val powerSheetState = rememberModalBottomSheetState()
    var showPowerBottomSheet by remember { mutableStateOf(false) }

    val settingsSheetState = rememberModalBottomSheetState()
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    
    // Check accessibility service status
    var showAccessibilityDialog by remember { mutableStateOf(false) }
    var hasCheckedAccessibility by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (!hasCheckedAccessibility) {
            delay(1000) // Wait 1 second after screen loads
            val isEnabled = viewModel.isAccessibilityServiceEnabled(context)
            if (!isEnabled) {
                showAccessibilityDialog = true
            }
            hasCheckedAccessibility = true
        }
    }

    // Data State
    val cpuInfo by viewModel.cpuInfo.collectAsState()
    val gpuInfo by viewModel.gpuInfo.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val systemInfo by viewModel.systemInfo.collectAsState()
    val powerInfo by viewModel.powerInfo.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadBatteryInfo(context) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExpandablePowerFab(onPowerAction = { action -> onPowerAction(action) })
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply system padding for status bar
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Remove custom status bar - use system status bar instead
                    Spacer(modifier = Modifier.height(16.dp)) // Standard top padding
                
                    // Header
                    StaggeredEntry(delayMillis = 0) {
                        MaterialHeader(onSettingsClick = { showSettingsBottomSheet = true })
                    }

                    // Device Info Card
                    StaggeredEntry(delayMillis = 100) {
                        MaterialDeviceCard(systemInfo = systemInfo)
                    }

                    // CPU & Temperature Tiles Row (matching Liquid layout)
                    StaggeredEntry(delayMillis = 200) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            // CPU Tile
                            MaterialStatTile(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                icon = Icons.Rounded.Memory,
                                label = "CPU",
                                value = "${(cpuInfo.cores.maxOfOrNull { it.currentFreq } ?: 0) / 1000} MHz",
                                subValue = cpuInfo.cores.firstOrNull { it.isOnline }?.governor ?: "Unknown",
                                color = MaterialTheme.colorScheme.primary,
                                badgeText = "${String.format(Locale.US, "%.0f", cpuInfo.totalLoad)}%",
                            )

                            // Temperature Tile
                            MaterialTempTile(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                cpuTemp = cpuInfo.temperature.toInt(),
                                gpuTemp = gpuInfo.temperature.toInt(),
                                pmicTemp = batteryInfo.pmicTemp.toInt(),
                                thermalTemp = batteryInfo.temperature.toInt(),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    // GPU Information Card
                    StaggeredEntry(delayMillis = 300) {
                        MaterialGPUCard(gpuInfo = gpuInfo)
                    }

                    // Memory & Storage Card
                    StaggeredEntry(delayMillis = 400) {
                        MaterialMemoryCard(systemInfo = systemInfo)
                    }

                    // Battery Information Card
                    StaggeredEntry(delayMillis = 500) {
                        MaterialBatteryCard(batteryInfo = batteryInfo)
                    }

                    // Power Insight Card
                    StaggeredEntry(delayMillis = 600) {
                        MaterialPowerInsightCard(powerInfo, batteryInfo)
                    }

                    // App Info Section
                    StaggeredEntry(delayMillis = 700) {
                        MaterialAppInfoSection()
                    }

                    // Bottom Spacing
                    Spacer(modifier = Modifier.height(80.dp))
                }
        },
    )

    // Power Menu Sheet
    if (showPowerBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPowerBottomSheet = false },
            sheetState = powerSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            PowerMenuContent(
                onAction = {
                    showPowerBottomSheet = false
                    onPowerAction(it)
                }
            )
        }
    }

    // Settings Sheet
    if (showSettingsBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsBottomSheet = false },
            sheetState = settingsSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            SettingsSheet(
                preferencesManager = preferencesManager,
                onDismiss = { showSettingsBottomSheet = false },
            )
        }
    }
    
    // Accessibility Service Dialog
    if (showAccessibilityDialog) {
        AlertDialog(
            onDismissRequest = { showAccessibilityDialog = false },
            icon = {
                Icon(
                    Icons.Rounded.Accessibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    stringResource(id.xms.xtrakernelmanager.R.string.accessibility_service_disabled),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    stringResource(id.xms.xtrakernelmanager.R.string.accessibility_service_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAccessibilityDialog = false
                        val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    }
                ) {
                    Text(stringResource(id.xms.xtrakernelmanager.R.string.accessibility_service_enable))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAccessibilityDialog = false }
                ) {
                    Text(stringResource(id.xms.xtrakernelmanager.R.string.accessibility_service_later))
                }
            }
        )
    }
}

/**
 * Staggered entry animation for home screen components
 * Creates a cascading fade-in and slide-up effect
 */
@Composable
fun StaggeredEntry(delayMillis: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(500)) +
                slideInVertically(
                    animationSpec = androidx.compose.animation.core.tween(
                        500,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                    ),
                    initialOffsetY = { 100 },
                ),
        exit = fadeOut(),
    ) {
        content()
    }
}
