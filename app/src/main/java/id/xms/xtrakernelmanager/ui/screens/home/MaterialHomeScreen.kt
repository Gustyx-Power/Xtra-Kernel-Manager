package id.xms.xtrakernelmanager.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.border
import androidx.compose.ui.unit.em
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.ui.components.DeviceSilhouette
import id.xms.xtrakernelmanager.ui.components.WavyProgressIndicator
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import java.util.Locale

/**
 * Material Home Screen - Restored Layout with Dynamic Colors (Material You)
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialHomeScreen(
    preferencesManager: PreferencesManager,
    viewModel: HomeViewModel = viewModel(),
    onPowerAction: (PowerAction) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Bottom Sheet State
    @OptIn(ExperimentalMaterial3Api::class)
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    
    // Data State
    val cpuInfo by viewModel.cpuInfo.collectAsState()
    val gpuInfo by viewModel.gpuInfo.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val systemInfo by viewModel.systemInfo.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBatteryInfo(context)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Rounded.PowerSettingsNew,
                    contentDescription = "Power Menu"
                )
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(paddingValues)
                    .padding(vertical = 24.dp), // Added vertical padding here since contentPadding is gone
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                StaggeredEntry(delayMillis = 0) {
                    MaterialHeader(onSettingsClick = onSettingsClick)
                }

                // Device Info Card
                StaggeredEntry(delayMillis = 100) {
                    MaterialDeviceCard(systemInfo = systemInfo)
                }

                // CPU & GPU Tiles Row
                StaggeredEntry(delayMillis = 200) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max), // Force equal height
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MaterialStatTile(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            icon = Icons.Rounded.Memory, // Chip icon
                            label = "Load",
                            value = "${String.format(Locale.US, "%.0f", cpuInfo.totalLoad)}%",
                            subValue = "${cpuInfo.cores.maxOfOrNull { it.currentFreq } ?: 0} MHz",
                            color = MaterialTheme.colorScheme.primary,
                            badgeText = "CPU"
                        )
                        
                        MaterialStatTile(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            icon = Icons.Rounded.Videocam,
                            label = "Freq",
                            value = "${gpuInfo.currentFreq}",
                            subValue = "MHz",
                            color = MaterialTheme.colorScheme.tertiary,
                            badgeText = "GPU"
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
                
                // Power Insight Card (New)
                StaggeredEntry(delayMillis = 600) {
                     MaterialPowerInsightCard()
                }
                
                // Bottom Spacing
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    )
    
    // Power Menu Sheet (Same as before)
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            PowerMenuContent(
                onAction = { 
                    showBottomSheet = false
                    onPowerAction(it)
                }
            )
        }
    }
}

@Composable
fun MaterialHeader(onSettingsClick: () -> Unit) {
    val view = androidx.compose.ui.platform.LocalView.current
    var isShortTitle by remember { mutableStateOf(false) }
    var clickCount by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(clickCount) {
        if (clickCount > 0) {
            delay(500)
            clickCount = 0
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null 
            ) {
                clickCount++
                if (clickCount >= 3) {
                    isShortTitle = !isShortTitle
                    clickCount = 0
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                    } else {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                    }
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedContent(
            targetState = isShortTitle,
            transitionSpec = {
                (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
            },
            label = "HeaderTitle"
        ) { short ->
            Text(
                text = if (short) "XKM" else "Xtra Kernel Manager",
                style = if (short) MaterialTheme.typography.displayMedium else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MaterialDeviceCard(systemInfo: SystemInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                   Text(
                        text = android.os.Build.MANUFACTURER.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Smart Badge: Board/SoC
                    Surface(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f), // Tonal on primary
                        shape = RoundedCornerShape(50), // Fully rounded pill
                    ) {
                        Text(
                            text = android.os.Build.BOARD.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = systemInfo.deviceModel.replace(android.os.Build.MANUFACTURER, "", ignoreCase = true).trim().ifBlank { "Unknown Model" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = android.os.Build.DEVICE.ifBlank { "Unknown" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 100.dp) // Increased padding to shift left away from silhouette
                ) {
                     Text(
                        text = systemInfo.kernelVersion.ifBlank { "Unknown" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
            
            // Device Silhouette
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp)
                    .offset(y = 40.dp) // Push down slightly more for large cards
            ) {
               DeviceSilhouette(
                   color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f)
               )
            }
        }
    }
}

@Composable
fun MaterialStatTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String,
    color: Color,
    badgeText: String? = null // Optional badge (e.g., "CPU", "GPU")
) {
    Card(
        modifier = modifier.animateContentSize(),
        shape = RoundedCornerShape(24.dp), // "Kotak tapi membulat" (Squarish but rounded)
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 12.dp), // Lift header slightly
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp), 
                    color = color.copy(alpha = 0.1f),
                    modifier = Modifier.size(36.dp) 
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp) // Slightly smaller icon
                        )
                    }
                }

                // Optional Smart Badge
                if (badgeText != null) {
                    Surface(
                        color = color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy((-2).dp) 
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                         platformStyle = PlatformTextStyle(includeFontPadding = false),
                         lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both
                        )
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    lineHeight = 32.sp // Tight line height for headlineMedium (usually 36)
                )
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.bodySmall.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun MaterialGPUCard(gpuInfo: id.xms.xtrakernelmanager.data.model.GPUInfo) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GPU",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                // Smart Badge: Vendor
                Surface(
                   color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                   shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "ADRENO", // Hardcoded placeholder or derive from renderer
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = "${gpuInfo.currentFreq} MHz",
                    style = MaterialTheme.typography.displaySmall, // Expressive Typography
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max), // Ensure equal height
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Inner Card 1: Load
                Surface(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${gpuInfo.gpuLoad}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Load",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Inner Card 2: GPU Name
                Surface(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = gpuInfo.renderer,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 1.2.em
                        )
                        Text(
                            text = "GPU",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialBatteryCard(batteryInfo: BatteryInfo) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                   verticalAlignment = Alignment.CenterVertically,
                   horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BatteryChargingFull,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Battery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Smart Badge: Status
                val isCharging = batteryInfo.status.contains("Charging", ignoreCase = true)
                Surface(
                    color = if(isCharging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(50),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                         if(isCharging) {
                            Icon(Icons.Rounded.Bolt, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onPrimary)
                         }
                         Text(
                            text = batteryInfo.status,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                BatterySilhouette(
                    level = batteryInfo.level / 100f,
                    isCharging = batteryInfo.status.contains("Charging", ignoreCase = true),
                    color = MaterialTheme.colorScheme.primary
                )

                Column {
                    Text(
                        text = "${batteryInfo.level}%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 1.em
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BatteryStatusChip(text = batteryInfo.status)
                        BatteryStatusChip(text = "Health ${String.format(Locale.US, "%.0f", batteryInfo.healthPercent)}%")
                    }
                }
            }

            // Stats Grid (2x2) - 4 Individual Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Row 1
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val currentText = if (batteryInfo.currentNow >= 0) "+${batteryInfo.currentNow} mA" else "${batteryInfo.currentNow} mA"
                    BatteryStatBox(
                        label = "Current",
                        value = currentText,
                        modifier = Modifier.weight(1f)
                    )
                    BatteryStatBox(
                        label = "Voltage",
                        value = "${batteryInfo.voltage} mV",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    BatteryStatBox(
                        label = "Temperature",
                        value = "${batteryInfo.temperature}°C",
                        modifier = Modifier.weight(1f)
                    )
                    BatteryStatBox(
                        label = "Cycle Count",
                        value = "${batteryInfo.cycleCount}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun BatterySilhouette(
    level: Float,
    isCharging: Boolean,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp) // Gap between cap and body
    ) {
        // Battery Cap (Nub)
        Box(
            modifier = Modifier
                .size(20.dp, 4.dp)
                .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
        )
        
        // Main Body
        Box(
            modifier = Modifier
                .size(50.dp, 80.dp)
                .border(4.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .padding(4.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(level)
                    .background(color, RoundedCornerShape(6.dp))
            )
        }
    }
}

@Composable
fun BatteryStatusChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant, 
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun BatteryStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.6f) // Higher contrast
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Dot removed for cleaner look

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}
@Composable
fun MaterialMemoryCard(systemInfo: SystemInfo) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Memory",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // RAM Section
            val ramUsed = (systemInfo.totalRam - systemInfo.availableRam)
            val ramTotal = systemInfo.totalRam
            val ramProgress = if (ramTotal > 0) ramUsed.toFloat() / ramTotal.toFloat() else 0f
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "RAM",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "${formatFileSize(ramUsed)} / ${formatFileSize(ramTotal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                WavyProgressIndicator(
                    progress = ramProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp), // Height for the wave
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    strokeWidth = 4.dp,
                    amplitude = 4.dp
                )
            }

            // ZRAM / Swap Section (Show if Swap OR ZRAM exists)
            val showZram = systemInfo.swapTotal > 0 || systemInfo.zramSize > 0
            
            if (showZram) {
                // Prefer Swap stats if available, otherwise fallback to ZRAM capacity with 0 usage
                val swapTotal = if (systemInfo.swapTotal > 0) systemInfo.swapTotal else systemInfo.zramSize
                val swapUsed = if (systemInfo.swapTotal > 0) (systemInfo.swapTotal - systemInfo.swapFree) else 0L
                val swapProgress = if (swapTotal > 0) swapUsed.toFloat() / swapTotal.toFloat() else 0f
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = if (systemInfo.zramSize > 0) "ZRAM" else "Swap",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "${formatFileSize(swapUsed)} / ${formatFileSize(swapTotal)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    
                    WavyProgressIndicator(
                        progress = swapProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        strokeWidth = 4.dp,
                        amplitude = 4.dp
                    )
                }
            }

            // Internal Storage Section
            val storageUsed = (systemInfo.totalStorage - systemInfo.availableStorage)
            val storageTotal = systemInfo.totalStorage
            val storageProgress = if (storageTotal > 0) storageUsed.toFloat() / storageTotal.toFloat() else 0f
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Internal Storage",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "${formatFileSize(storageUsed)} / ${formatFileSize(storageTotal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                WavyProgressIndicator(
                    progress = storageProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    strokeWidth = 4.dp,
                    amplitude = 4.dp
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 1.0) {
        String.format(Locale.US, "%.1f GB", gb)
    } else {
        val mb = bytes / (1024.0 * 1024.0)
        String.format(Locale.US, "%.0f MB", mb)
    }
}

@Composable
fun PowerMenuContent(onAction: (PowerAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Power Menu",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val actions = listOf(
            PowerAction.PowerOff,
            PowerAction.Reboot,
            PowerAction.Recovery,
            PowerAction.Bootloader,
            PowerAction.SystemUI
        )
        
        actions.forEach { action ->
            PowerMenuItem(action = action, onClick = { onAction(action) })
        }
    }
}

@Composable
fun PowerMenuItem(action: PowerAction, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = action.getLocalizedLabel(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StaggeredEntry(
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
            animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            initialOffsetY = { 100 }
        ),
        exit = fadeOut()
    ) {
        content()
    }
}

@Composable
fun MaterialPowerInsightCard() {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween, // Push end items to right
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon + Title Group
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Power Insight",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                // Badge (Pushed to Right)
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                         text = "Screen On",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Content: Circle + Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // SOT Circular Indicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(150.dp) // Bigger Size
                ) {
                    // Background Track
                    WavyCircularProgressIndicator(
                        progress = 1f,
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                        strokeWidth = 16.dp, // Thicker
                        amplitude = 3.dp,    // Subtle wave
                        frequency = 10       // Calmer frequency
                    )
                    
                    // Progress (Dummy: 65% for ~6h SOT goal)
                    WavyCircularProgressIndicator(
                        progress = 0.65f,
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 16.dp, // Thicker
                        amplitude = 3.dp,    // Subtle wave
                        frequency = 10       // Calmer frequency
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "5h 24m", // Dummy Data
                            style = MaterialTheme.typography.headlineMedium, // Bigger Font
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Stats Column
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    PowerInsightItem(label = "Deep Sleep", value = "85%", icon = Icons.Rounded.Bedtime)
                    PowerInsightItem(label = "Active Drain", value = "-10%/h", icon = Icons.Rounded.TrendingDown)
                    PowerInsightItem(label = "Idle Drain", value = "-0.8%/h", icon = Icons.Rounded.PhoneAndroid)
                }
            }
        }
    }
}

@Composable
fun WavyCircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: androidx.compose.ui.unit.Dp,
    amplitude: androidx.compose.ui.unit.Dp = 4.dp,
    frequency: Int = 12
) {
    val strokeWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { strokeWidth.toPx() }
    val amplitudePx = with(androidx.compose.ui.platform.LocalDensity.current) { amplitude.toPx() }
    
    // Animate progress using animateFloatAsState
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing), 
        label = "progress"
    )

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val radius = (size.minDimension - strokeWidthPx - amplitudePx * 2) / 2
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        val path = androidx.compose.ui.graphics.Path()

        val startAngle = -90f
        val sweepAngle = 360f * animatedProgress
        
        // Resolution of the path (step size in degrees). Smaller = smoother.
        val step = 1f 
        
        for (angle in 0..sweepAngle.toInt()) {
            val currentAngle = startAngle + angle
            val rad = Math.toRadians(currentAngle.toDouble())
            
            // Wavy function: radius + amplitude * sin(frequency * angle in rads)
            // Use angle relative to start to maintain wave phase
            val wavePhase = Math.toRadians((angle * frequency).toDouble())
            val r = radius + amplitudePx * kotlin.math.sin(wavePhase)
            
            val x = center.x + r * kotlin.math.cos(rad)
            val y = center.y + r * kotlin.math.sin(rad)
            
            if (angle == 0) {
                path.moveTo(x.toFloat(), y.toFloat())
            } else {
                path.lineTo(x.toFloat(), y.toFloat())
            }
        }

        drawPath(
            path = path,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round
            )
        )
    }
}

@Composable
fun PowerInsightItem(label: String, value: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
        }
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

