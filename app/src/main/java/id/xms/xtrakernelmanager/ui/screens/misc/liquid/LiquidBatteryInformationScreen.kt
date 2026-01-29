package id.xms.xtrakernelmanager.ui.screens.misc.liquid

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import coil.compose.rememberAsyncImagePainter
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.AppBatteryStats
import id.xms.xtrakernelmanager.data.model.BatteryUsageType
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidToggle
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel

@Composable
fun LiquidBatteryInformationScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val showBatteryNotif by viewModel.showBatteryNotif.collectAsState()
    val isLightTheme = !isSystemInDarkTheme()
    
    var selectedTab by rememberSaveable { mutableStateOf(0) } // 0 = Monitor, 1 = Settings

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PermissionChecker.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            hasNotificationPermission = isGranted
        }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background decoration
        id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament(
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Header with back button
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                onClick = onBack
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp).clickable(onClick = onBack)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = "Battery Information",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // iOS-style segmented control
            IOSSegmentedControl(
                selectedIndex = selectedTab,
                items = listOf("Monitor", "Settings"),
                onItemSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content based on selected tab
            when (selectedTab) {
                0 -> BatteryMonitorContent(viewModel = viewModel)
                1 -> BatterySettingsContent(
                    viewModel = viewModel,
                    showBatteryNotif = showBatteryNotif,
                    hasNotificationPermission = hasNotificationPermission,
                    notificationPermissionLauncher = notificationPermissionLauncher,
                    isLightTheme = isLightTheme
                )
            }
        }
    }
}

@Composable
fun IOSSegmentedControl(
    selectedIndex: Int,
    items: List<String>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightTheme = !isSystemInDarkTheme()
    
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) {
                                if (isLightTheme) Color.White.copy(alpha = 0.9f)
                                else Color.White.copy(alpha = 0.15f)
                            } else Color.Transparent
                        )
                        .clickable { onItemSelected(index) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) {
                            if (isLightTheme) Color.Black else Color.White
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BatterySettingsContent(
    viewModel: MiscViewModel,
    showBatteryNotif: Boolean,
    hasNotificationPermission: Boolean,
    notificationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    isLightTheme: Boolean
) {
    // Collect all settings states
    val notifIconType by viewModel.batteryNotifIconType.collectAsState()
    val notifRefreshRate by viewModel.batteryNotifRefreshRate.collectAsState()
    val notifSecureLockScreen by viewModel.batteryNotifSecureLockScreen.collectAsState()
    val notifHighPriority by viewModel.batteryNotifHighPriority.collectAsState()
    val notifForceOnTop by viewModel.batteryNotifForceOnTop.collectAsState()
    val notifDontUpdateScreenOff by viewModel.batteryNotifDontUpdateScreenOff.collectAsState()
    val statsActiveIdle by viewModel.batteryStatsActiveIdle.collectAsState()
    val statsScreen by viewModel.batteryStatsScreen.collectAsState()
    val statsAwakeSleep by viewModel.batteryStatsAwakeSleep.collectAsState()
    
    var showIconTypeDialog by remember { mutableStateOf(false) }
    var showRefreshRateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main toggle card
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isLightTheme) Color(0xFF34C759).copy(0.15f)
                                else Color(0xFF30D158).copy(0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = if (isLightTheme) Color(0xFF34C759) else Color(0xFF30D158),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Notification",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Show battery info in status bar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }

                LiquidToggle(
                    checked = showBatteryNotif,
                    onCheckedChange = { viewModel.setShowBatteryNotif(it) }
                )
            }
        }

        // Permission warning
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isLightTheme) Color(0xFFFF3B30) else Color(0xFFFF453A),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.grant_notification_permission),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isLightTheme) Color(0xFFFF3B30) else Color(0xFFFF453A),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = if (isLightTheme) Color(0xFFFF3B30) else Color(0xFFFF453A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Notification Settings Section
        IOSSettingsSectionHeader(text = "Notification Settings")
        
        IOSSettingsGroup {
            IOSListSetting(
                title = "Icon Type",
                value = getIconTypeLabel(notifIconType),
                onClick = { showIconTypeDialog = true }
            )
            
            IOSDivider()
            
            IOSListSetting(
                title = "Refresh Rate",
                value = getRefreshRateLabel(notifRefreshRate),
                onClick = { showRefreshRateDialog = true }
            )
            
            IOSDivider()
            
            IOSSwitchSetting(
                title = "Secure Lock Screen",
                subtitle = "Show on secure lock screen",
                checked = notifSecureLockScreen,
                onCheckedChange = { viewModel.setBatteryNotifSecureLockScreen(it) }
            )
            
            IOSDivider()
            
            IOSSwitchSetting(
                title = "High Priority",
                subtitle = "Keep notification at top",
                checked = notifHighPriority,
                onCheckedChange = { viewModel.setBatteryNotifHighPriority(it) }
            )
            
            IOSDivider()
            
            IOSSwitchSetting(
                title = "Force On Top",
                subtitle = "Always show at top of list",
                checked = notifForceOnTop,
                onCheckedChange = { viewModel.setBatteryNotifForceOnTop(it) }
            )
            
            IOSDivider()
            
            IOSSwitchSetting(
                title = "Pause When Screen Off",
                subtitle = "Don't update when screen is off",
                checked = notifDontUpdateScreenOff,
                onCheckedChange = { viewModel.setBatteryNotifDontUpdateScreenOff(it) }
            )
        }

        // Statistics Settings Section
        IOSSettingsSectionHeader(text = "Statistics Settings")
        
        IOSSettingsGroup {
            IOSSwitchSetting(
                title = "Active/Idle Stats",
                subtitle = "Show active and idle drain rates",
                checked = statsActiveIdle,
                onCheckedChange = { viewModel.setBatteryStatsActiveIdle(it) }
            )
            
            IOSDivider()
            
            IOSSwitchSetting(
                title = "Screen Stats",
                subtitle = "Show screen on/off statistics",
                checked = statsScreen,
                onCheckedChange = { viewModel.setBatteryStatsScreen(it) }
            )
            
            IOSDivider()
            
            IOSSwitchSetting(
                title = "Awake/Sleep Stats",
                subtitle = "Show awake and deep sleep time",
                checked = statsAwakeSleep,
                onCheckedChange = { viewModel.setBatteryStatsAwakeSleep(it) }
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
    
    // Icon Type Dialog
    if (showIconTypeDialog) {
        IOSSelectionDialog(
            title = "Icon Type",
            currentValue = notifIconType,
            options = mapOf(
                "battery_icon" to "Battery Icon",
                "circle_percent" to "Circle Percent",
                "percent_only" to "Percent Only",
                "temp" to "Temperature",
                "percent_temp" to "Percent + Temp",
                "current" to "Current",
                "voltage" to "Voltage",
                "power" to "Power"
            ),
            onDismiss = { showIconTypeDialog = false },
            onSelect = { 
                viewModel.setBatteryNotifIconType(it)
                showIconTypeDialog = false
            }
        )
    }
    
    // Refresh Rate Dialog
    if (showRefreshRateDialog) {
        IOSSelectionDialog(
            title = "Refresh Rate",
            currentValue = notifRefreshRate.toString(),
            options = mapOf(
                "500" to "0.5s",
                "1000" to "1s",
                "2000" to "2s",
                "5000" to "5s"
            ),
            onDismiss = { showRefreshRateDialog = false },
            onSelect = { 
                viewModel.setBatteryNotifRefreshRate(it.toLong())
                showRefreshRateDialog = false
            }
        )
    }
}

@Composable
fun IOSSelectionDialog(
    title: String,
    currentValue: String,
    options: Map<String, String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val isLightTheme = !isSystemInDarkTheme()
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    options.forEach { (key, label) ->
                        val isSelected = key == currentValue
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelect(key) }
                                .background(
                                    if (isSelected) {
                                        if (isLightTheme) Color(0xFF007AFF).copy(alpha = 0.1f)
                                        else Color(0xFF0A84FF).copy(alpha = 0.15f)
                                    } else Color.Transparent
                                )
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) {
                                    if (isLightTheme) Color(0xFF007AFF) else Color(0xFF0A84FF)
                                } else MaterialTheme.colorScheme.onSurface
                            )
                            
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isLightTheme) Color(0xFF007AFF) else Color(0xFF0A84FF)
                                )
                            }
                        }
                        
                        if (key != options.keys.last()) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Cancel",
                        color = if (isLightTheme) Color(0xFF007AFF) else Color(0xFF0A84FF)
                    )
                }
            }
        }
    }
}

@Composable
fun IOSSettingsSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

@Composable
fun IOSSettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun IOSSwitchSetting(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        LiquidToggle(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun IOSListSetting(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun IOSDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    )
}

private fun getIconTypeLabel(type: String): String {
    return when (type) {
        "battery_icon" -> "Battery Icon"
        "circle_percent" -> "Circle Percent"
        "percent_only" -> "Percent Only"
        "temp" -> "Temperature"
        "percent_temp" -> "Percent + Temp"
        "current" -> "Current"
        "voltage" -> "Voltage"
        "power" -> "Power"
        else -> type
    }
}

private fun getRefreshRateLabel(rate: Long): String {
    return when (rate) {
        500L -> "0.5s"
        1000L -> "1s"
        2000L -> "2s"
        5000L -> "5s"
        else -> "${rate}ms"
    }
}

@Composable
fun BatteryMonitorContent(viewModel: MiscViewModel) {
    val context = LocalContext.current
    val isLightTheme = !isSystemInDarkTheme()
    
    // Load battery data when screen is opened
    LaunchedEffect(Unit) {
        viewModel.loadBatteryInfo(context)
        viewModel.loadAppBatteryUsage(context)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // History Chart Card - iOS Style
        IOSHistoryChartCard()

        // Stats Grid - iOS Style
        IOSStatsGrid(viewModel)

        // Current Session Card - iOS Style
        IOSCurrentSessionCard(viewModel)

        // App Battery Usage List - iOS Style
        IOSAppBatteryUsageList(viewModel)

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun IOSHistoryChartCard() {
    val state by id.xms.xtrakernelmanager.data.repository.HistoryRepository.hourlyStats.collectAsState()
    val isLightTheme = !isSystemInDarkTheme()
    var showScreenOn by remember { mutableStateOf(true) }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // iOS-style toggle
                Surface(
                    color = if (isLightTheme) Color(0xFFE5E5EA) else Color(0xFF2C2C2E),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        IOSToggleButton(
                            text = "Screen",
                            isSelected = showScreenOn,
                            onClick = { showScreenOn = true },
                            color = if (isLightTheme) Color(0xFF34C759) else Color(0xFF30D158)
                        )
                        IOSToggleButton(
                            text = "Drain",
                            isSelected = !showScreenOn,
                            onClick = { showScreenOn = false },
                            color = if (isLightTheme) Color(0xFFFF3B30) else Color(0xFFFF453A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bar Chart
            val buckets = state.buckets
            val maxVal = if (showScreenOn) 60f else buckets.maxOfOrNull { it.drainPercent }?.toFloat()?.coerceAtLeast(10f) ?: 10f

            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val primaryColor = if (showScreenOn) {
                    if (isLightTheme) Color(0xFF34C759) else Color(0xFF30D158)
                } else {
                    if (isLightTheme) Color(0xFFFF3B30) else Color(0xFFFF453A)
                }

                buckets.forEachIndexed { index, bucket ->
                    val value = if (showScreenOn) (bucket.screenOnMs / 60000f) else bucket.drainPercent.toFloat()
                    val heightPercent = (value / maxVal).coerceIn(0.05f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight(heightPercent)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (value > 0) primaryColor.copy(alpha = 0.8f)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (index % 4 == 0) {
                            Text(
                                text = "%02d".format(index),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Summary
            val totalStr = if (showScreenOn) {
                val totalMs = buckets.sumOf { it.screenOnMs }
                formatDuration(totalMs)
            } else {
                "${buckets.sumOf { it.drainPercent }}%"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isLightTheme) Color(0xFFF2F2F7) else Color(0xFF1C1C1E)
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Total Today: $totalStr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun IOSToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    val isLightTheme = !isSystemInDarkTheme()
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isSelected) color else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun IOSStatsGrid(viewModel: MiscViewModel) {
    val batteryState by id.xms.xtrakernelmanager.data.repository.BatteryRepository.batteryState.collectAsState()
    val isLightTheme = !isSystemInDarkTheme()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Battery Health Card
        GlassmorphicCard(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.BatteryStd,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isLightTheme) Color(0xFF34C759) else Color(0xFF30D158)
                )
                
                val design = if (batteryState.totalCapacity > 0) batteryState.totalCapacity else 5000
                val current = if (batteryState.currentCapacity > 0) batteryState.currentCapacity else 4500
                val healthPercent = ((current.toFloat() / design.toFloat()) * 100).toInt().coerceIn(0, 100)
                
                Text(
                    text = "$healthPercent%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Battery Health",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Electric Current Card
        GlassmorphicCard(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (batteryState.currentNow > 0) {
                        if (isLightTheme) Color(0xFF34C759) else Color(0xFF30D158)
                    } else {
                        if (isLightTheme) Color(0xFFFF9500) else Color(0xFFFF9F0A)
                    }
                )
                
                Text(
                    text = "${batteryState.currentNow} mA",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Current Flow",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Voltage Card
        GlassmorphicCard(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ElectricBolt,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isLightTheme) Color(0xFFFF9500) else Color(0xFFFF9F0A)
                )
                
                Text(
                    text = "${batteryState.voltage} mV",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Voltage",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Power Card
        GlassmorphicCard(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Power,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isLightTheme) Color(0xFF007AFF) else Color(0xFF0A84FF)
                )
                
                val watts = (kotlin.math.abs(batteryState.currentNow) / 1000f) * (batteryState.voltage / 1000f)
                Text(
                    text = "%.1f W".format(watts),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Power",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun IOSCurrentSessionCard(viewModel: MiscViewModel) {
    val screenOnTime by viewModel.screenOnTime.collectAsState()
    val screenOffTime by viewModel.screenOffTime.collectAsState()
    val deepSleepTime by viewModel.deepSleepTime.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val sessionState by id.xms.xtrakernelmanager.data.repository.BatteryRepository.batteryState.collectAsState()
    val isLightTheme = !isSystemInDarkTheme()

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Current Session",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Stats rows
            IOSStatRow(
                icon = Icons.Rounded.WbSunny,
                label = "Screen On",
                value = screenOnTime,
                iconColor = if (isLightTheme) Color(0xFFFF9500) else Color(0xFFFF9F0A)
            )
            
            IOSStatRow(
                icon = Icons.Rounded.NightsStay,
                label = "Screen Off",
                value = screenOffTime,
                iconColor = if (isLightTheme) Color(0xFF5856D6) else Color(0xFF5E5CE6)
            )
            
            IOSStatRow(
                icon = Icons.Rounded.Bedtime,
                label = "Deep Sleep",
                value = deepSleepTime,
                iconColor = if (isLightTheme) Color(0xFF007AFF) else Color(0xFF0A84FF)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            // Drain rates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.2f%%/h".format(sessionState.activeDrainRate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Active Drain",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.2f%%/h".format(sessionState.idleDrainRate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Idle Drain",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun IOSStatRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = iconColor
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun IOSAppBatteryUsageList(viewModel: MiscViewModel) {
    val appUsageList by viewModel.appBatteryUsage.collectAsState()
    val isLoading by viewModel.isLoadingAppUsage.collectAsState()
    val isLightTheme = !isSystemInDarkTheme()
    var showSystem by rememberSaveable { mutableStateOf(false) }

    val filteredList = remember(appUsageList, showSystem) {
        if (showSystem) {
            appUsageList.filter { it.usageType == BatteryUsageType.SYSTEM }
        } else {
            appUsageList.filter { it.usageType == BatteryUsageType.APP }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Battery Usage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            IOSFilterButton(
                isSystem = showSystem,
                onFilterChange = { showSystem = it }
            )
        }

        if (isLoading && appUsageList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = if (isLightTheme) Color(0xFF007AFF) else Color(0xFF0A84FF)
                )
            }
        } else if (filteredList.isEmpty()) {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No usage data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    filteredList.forEachIndexed { index, app ->
                        IOSAppUsageItem(app)
                        if (index < filteredList.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IOSFilterButton(
    isSystem: Boolean,
    onFilterChange: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isLightTheme = !isSystemInDarkTheme()

    Box {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isLightTheme) Color(0xFFE5E5EA) else Color(0xFF2C2C2E),
            modifier = Modifier.clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isSystem) "Systems" else "Apps",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = if (isLightTheme) Color(0xFFF2F2F7) else Color(0xFF2C2C2E)
        ) {
            DropdownMenuItem(
                text = { Text("Apps") },
                onClick = {
                    onFilterChange(false)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Systems") },
                onClick = {
                    onFilterChange(true)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun IOSAppUsageItem(app: AppBatteryStats) {
    val isLightTheme = !isSystemInDarkTheme()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App Icon
        if (app.icon != null) {
            androidx.compose.foundation.Image(
                painter = rememberAsyncImagePainter(model = app.icon),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isLightTheme) Color(0xFFE5E5EA) else Color(0xFF3A3A3C)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (app.usageType == BatteryUsageType.SYSTEM) 
                        Icons.Rounded.Android else Icons.Rounded.Apps,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Battery usage",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        Text(
            text = "${app.percent.toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return if (hours > 0) "%dh %02dm".format(hours, minutes % 60)
    else "%dm %02ds".format(minutes, seconds % 60)
}
