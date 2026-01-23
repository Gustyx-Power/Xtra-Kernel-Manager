package id.xms.xtrakernelmanager.ui.screens.home

import androidx.compose.ui.draw.blur
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Folder

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.ui.components.utils.layerBackdrop
import id.xms.xtrakernelmanager.ui.components.utils.rememberLayerBackdrop
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.CPUInfo
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.LocalBackdrop
import id.xms.xtrakernelmanager.ui.screens.home.components.SettingsSheet
import id.xms.xtrakernelmanager.utils.Holiday
import id.xms.xtrakernelmanager.utils.HolidayChecker
import java.io.DataOutputStream
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
        preferencesManager: PreferencesManager,
        viewModel: HomeViewModel = viewModel(),
        onNavigateToSettings: () -> Unit = {},
) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        val layoutStyle by preferencesManager.getLayoutStyle().collectAsState(initial = null)

        // Data State
        val cpuInfo by viewModel.cpuInfo.collectAsState()
        val gpuInfo by viewModel.gpuInfo.collectAsState()
        val batteryInfo by viewModel.batteryInfo.collectAsState()
        val systemInfo by viewModel.systemInfo.collectAsState()

        // UI State untuk Power Menu
        var showPowerMenu by remember { mutableStateOf(false) }
        var activePowerAction by remember { mutableStateOf<PowerAction?>(null) }

        // UI State untuk Settings Sheet (Legacy)
        @OptIn(ExperimentalMaterial3Api::class)
        val settingsSheetState = rememberModalBottomSheetState()
        var showSettingsBottomSheet by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) { viewModel.loadBatteryInfo(context) }

        // --- DIALOGS ---

        // 1. Power Menu Selection
        if (showPowerMenu) {
                PowerMenuDialog(
                        onDismiss = { showPowerMenu = false },
                        onActionSelected = { action ->
                                showPowerMenu = false
                                if (action == PowerAction.LockScreen) {
                                        scope.launch { RootShell.execute(action.command) }
                                } else {
                                        activePowerAction = action
                                }
                        },
                )
        }

        // 2. Countdown Execution
        activePowerAction?.let { action ->
                CountdownRebootDialog(
                        action = action,
                        onCancel = { activePowerAction = null },
                        onFinished = {
                                scope.launch {
                                        RootShell.execute(action.command)
                                        activePowerAction = null
                                }
                        },
                )
        }

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

        if (layoutStyle == null) {
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(MaterialTheme.colorScheme.background)
                )
        } else {
                when (layoutStyle) {
                        "material" -> {
                                // Material 3 Home Screen
                                MaterialHomeScreen(
                                        preferencesManager = preferencesManager,
                                        viewModel = viewModel,
                                        onPowerAction = { action ->
                                                if (action == PowerAction.LockScreen) {
                                                        scope.launch {
                                                                RootShell.execute(action.command)
                                                        }
                                                } else {
                                                        activePowerAction = action
                                                }
                                        },
                                        onSettingsClick = onNavigateToSettings,
                                )
                        }
                        else -> {
                                // Liquid Home Screen (Formerly Legacy/Glass UI)
                                val backdrop = rememberLayerBackdrop()

                                Box(modifier = Modifier.fillMaxSize()) {
                                        // Background Layer - Captures content for glass effect
                                        Box(modifier = Modifier.fillMaxSize()) {
                                                // Base gradient background
                                                Box(
                                                        modifier = Modifier.fillMaxSize()
                                                                .background(
                                                                        brush = androidx.compose.ui
                                                                                .graphics
                                                                                .Brush
                                                                                .verticalGradient(
                                                                                        colors = listOf(
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .primaryContainer,
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .tertiaryContainer,
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .background
                                                                                        )
                                                                                )
                                                                )
                                                )
                                                
                                                // Wavy blob ornament overlay with Monet colors
                                                id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament(
                                                        modifier = Modifier.fillMaxSize(),
                                                        colors = listOf(
                                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f),
                                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                                                        ),
                                                        strokeColor = Color.Black.copy(alpha = 0.6f),
                                                        blobAlpha = 0.55f
                                                )
                                                
                                                // Capture layer for backdrop
                                                Box(modifier = Modifier.fillMaxSize().layerBackdrop(backdrop))
                                        }


                                        // Content Layer
                                        CompositionLocalProvider(LocalBackdrop provides backdrop) {
                                                Scaffold(
                                                        containerColor = Color.Transparent, 
                                                ) { paddingValues ->
                                                        Box(Modifier.padding(paddingValues)) {
                                                                LiquidHomeScreen(
                                                                        cpuInfo = cpuInfo,
                                                                        gpuInfo = gpuInfo,
                                                                        batteryInfo = batteryInfo,
                                                                        systemInfo = systemInfo,
                                                                        onSettingsClick = {
                                                                                showSettingsBottomSheet =
                                                                                        true
                                                                        },
                                                                        onPowerClick = {
                                                                                showPowerMenu = true
                                                                        }
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
}

// POWER MENU & ROOT EXECUTION LOGIC
enum class PowerAction(val labelRes: Int, val icon: ImageVector, val command: String) {
        Reboot(R.string.reboot, Icons.Rounded.RestartAlt, "su -c reboot"),
        PowerOff(R.string.power_off, Icons.Rounded.PowerSettingsNew, "su -c reboot -p"),
        Recovery(
                R.string.recovery,
                Icons.Rounded.SystemSecurityUpdateWarning,
                "su -c reboot recovery"
        ),
        Bootloader(
                R.string.bootloader,
                Icons.Rounded.SettingsSystemDaydream,
                "su -c reboot bootloader"
        ),
        SystemUI(R.string.restart_ui, Icons.Rounded.Refresh, "su -c pkill -f com.android.systemui"),
        LockScreen(
                R.string.lockscreen,
                Icons.Rounded.Lock,
                "su -c input keyevent 26",
        ); // 26 = Power Button (Toggle Screen)

        @Composable
        fun getLabel(): String {
                return stringResource(id = labelRes)
        }
}

@Composable
fun PowerAction.getLocalizedLabel(): String {
        return stringResource(id = this.labelRes)
}

object RootShell {
        suspend fun execute(command: String) =
                withContext(Dispatchers.IO) {
                        try {
                                // Menggunakan Runtime.exec untuk menjalankan 'su'
                                val process = Runtime.getRuntime().exec("su")
                                val os = DataOutputStream(process.outputStream)

                                // Menulis perintah
                                os.writeBytes(command + "\n")
                                os.writeBytes("exit\n")
                                os.flush()

                                // Menunggu proses selesai
                                process.waitFor()
                        } catch (e: Exception) {
                                e.printStackTrace()
                        }
                }
}

@Composable
fun PowerMenuDialog(onDismiss: () -> Unit, onActionSelected: (PowerAction) -> Unit) {
        AlertDialog(
                onDismissRequest = onDismiss,
                icon = { Icon(Icons.Rounded.PowerSettingsNew, null) },
                title = { Text(text = "Power Menu", textAlign = TextAlign.Center) },
                text = {
                        LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                        ) {
                                items(PowerAction.values()) { action ->
                                        FilledTonalButton(
                                                onClick = { onActionSelected(action) },
                                                shape = RoundedCornerShape(16.dp),
                                                contentPadding = PaddingValues(vertical = 16.dp),
                                                colors =
                                                        ButtonDefaults.filledTonalButtonColors(
                                                                containerColor =
                                                                        if (action ==
                                                                                        PowerAction
                                                                                                .PowerOff
                                                                        )
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .errorContainer
                                                                        else
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .secondaryContainer,
                                                                contentColor =
                                                                        if (action ==
                                                                                        PowerAction
                                                                                                .PowerOff
                                                                        )
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onErrorContainer
                                                                        else
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onSecondaryContainer,
                                                        ),
                                        ) {
                                                Column(
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Icon(
                                                                action.icon,
                                                                null,
                                                                modifier = Modifier.size(28.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(
                                                                action.getLocalizedLabel(),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .labelMedium,
                                                                fontWeight = FontWeight.SemiBold,
                                                        )
                                                }
                                        }
                                }
                        }
                },
                confirmButton = {
                        TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                },
        )
}

@Composable
fun CountdownRebootDialog(action: PowerAction, onCancel: () -> Unit, onFinished: () -> Unit) {
        var countdown by remember { mutableIntStateOf(5) }
        val progress by animateFloatAsState(targetValue = countdown / 5f, label = "Countdown")

        LaunchedEffect(Unit) {
                while (countdown > 0) {
                        delay(1000)
                        countdown--
                }
                onFinished()
        }

        Dialog(
                onDismissRequest = {},
                properties =
                        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        ) {
                Card(
                        shape = RoundedCornerShape(28.dp),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor =
                                                MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                        modifier = Modifier.padding(16.dp),
                ) {
                        Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                                Text(
                                        "${action.getLocalizedLabel()}...",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                )
                                Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(120.dp)
                                ) {
                                        CircularProgressIndicator(
                                                progress = { 1f },
                                                modifier = Modifier.fillMaxSize(),
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                strokeWidth = 10.dp,
                                                trackColor = Color.Transparent,
                                        )
                                        CircularProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.fillMaxSize(),
                                                color =
                                                        if (countdown <= 2)
                                                                MaterialTheme.colorScheme.error
                                                        else MaterialTheme.colorScheme.primary,
                                                strokeWidth = 10.dp,
                                                strokeCap = StrokeCap.Round,
                                        )
                                        Text(
                                                text = if (countdown > 0) "$countdown" else "!",
                                                style = MaterialTheme.typography.displayLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                        )
                                }
                                Text(
                                        stringResource(R.string.processing_action),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Button(
                                        onClick = onCancel,
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme
                                                                        .surfaceVariant,
                                                        contentColor =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                ),
                                        modifier = Modifier.fillMaxWidth(),
                                ) { Text(stringResource(id = R.string.cancel)) }
                        }
                }
        }
}

// COMPONENTS (Card & Visuals)

/**
 * Modern Material Design 3 CPU Information Card Features: Circular load gauge, temperature badge,
 * core grid visualization No dropdown - all content always visible
 */
@SuppressLint("DefaultLocale")
@Composable
fun CPUInfoCardNoDropdown(cpuInfo: CPUInfo) {
        var isExpanded by remember { mutableStateOf(true) }

        // Arrow rotation animation
        val arrowRotation by
                animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        animationSpec =
                                spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow,
                                ),
                        label = "arrowRotation",
                )

        // Icon scale animation on toggle
        var iconPressed by remember { mutableStateOf(false) }
        val iconScale by
                animateFloatAsState(
                        targetValue = if (iconPressed) 0.85f else 1f,
                        animationSpec =
                                spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium,
                                ),
                        label = "iconScale",
                )

        // Header icon glow animation
        val headerIconScale by
                animateFloatAsState(
                        targetValue = if (isExpanded) 1.1f else 1f,
                        animationSpec =
                                spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow,
                                ),
                        label = "headerIconScale",
                )

        GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                        iconPressed = true
                        isExpanded = !isExpanded
                },
        ) {
                // Reset icon press state after animation
                LaunchedEffect(iconPressed) {
                        if (iconPressed) {
                                delay(150)
                                iconPressed = false
                        }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                        ) {
                                Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                ) {
                                        Surface(
                                                shape = MaterialTheme.shapes.medium,
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                tonalElevation = 2.dp,
                                                modifier = Modifier.scale(headerIconScale),
                                        ) {
                                                Icon(
                                                        Icons.Default.Memory,
                                                        null,
                                                        modifier =
                                                                Modifier.padding(8.dp).size(24.dp),
                                                        tint =
                                                                MaterialTheme.colorScheme
                                                                        .onPrimaryContainer,
                                                )
                                        }
                                        Text(
                                                stringResource(R.string.cpu_information),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                        )
                                }
                                IconButton(
                                        onClick = {
                                                iconPressed = true
                                                isExpanded = !isExpanded
                                        },
                                        modifier = Modifier.scale(iconScale),
                                ) {
                                        Icon(
                                                Icons.Default.KeyboardArrowDown,
                                                null,
                                                modifier =
                                                        Modifier.graphicsLayer {
                                                                rotationZ = arrowRotation
                                                        },
                                        )
                                }
                        }
                        AnimatedVisibility(
                                visible = isExpanded,
                                enter =
                                        expandVertically(
                                                animationSpec =
                                                        spring(
                                                                dampingRatio =
                                                                        Spring.DampingRatioLowBouncy,
                                                                stiffness =
                                                                        Spring.StiffnessMediumLow,
                                                        ),
                                                expandFrom = Alignment.Top,
                                        ) +
                                                fadeIn(animationSpec = tween(200)) +
                                                slideInVertically(
                                                        animationSpec =
                                                                spring(
                                                                        dampingRatio =
                                                                                Spring.DampingRatioLowBouncy,
                                                                        stiffness =
                                                                                Spring.StiffnessMediumLow,
                                                                ),
                                                        initialOffsetY = { -it / 4 },
                                                ),
                                exit =
                                        shrinkVertically(animationSpec = tween(150)) +
                                                fadeOut(animationSpec = tween(100)),
                                label = "CPU",
                        ) {
                                Column(
                                        modifier = Modifier.padding(top = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                                InfoChipCompact(
                                                        Icons.Default.Thermostat,
                                                        "${cpuInfo.temperature}°C"
                                                )
                                                InfoChipCompact(
                                                        Icons.Default.Speed,
                                                        stringResource(
                                                                R.string.load,
                                                                String.format(
                                                                        Locale.US,
                                                                        "%.0f",
                                                                        cpuInfo.totalLoad
                                                                )
                                                        ),
                                                )
                                                InfoChipCompact(
                                                        Icons.Default.Dashboard,
                                                        cpuInfo.cores.firstOrNull()?.governor
                                                                ?: stringResource(R.string.unknown),
                                                )
                                        }
                                        HorizontalDivider(
                                                color =
                                                        MaterialTheme.colorScheme.outlineVariant
                                                                .copy(alpha = 0.5f)
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text(
                                                        stringResource(
                                                                R.string.clockspeed_per_core
                                                        ),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                )
                                                val rows = cpuInfo.cores.chunked(4)
                                                val maxFreq =
                                                        cpuInfo.cores.maxOfOrNull { it.currentFreq }
                                                                ?: 0
                                                rows.forEach { rowCores ->
                                                        Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement =
                                                                        Arrangement.SpaceBetween,
                                                        ) {
                                                                rowCores.forEach { core ->
                                                                        val isHot =
                                                                                core.isOnline &&
                                                                                        core.currentFreq ==
                                                                                                maxFreq
                                                                        FreqItemCompact(
                                                                                freq =
                                                                                        core.currentFreq,
                                                                                label =
                                                                                        "CPU${core.coreNumber}",
                                                                                isActive = isHot,
                                                                                isOffline =
                                                                                        !core.isOnline,
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                        ) {
                                                Text(
                                                        text =
                                                                "Active Cores: ${cpuInfo.cores.count { it.isOnline }} / ${cpuInfo.cores.size}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                )
                                        }
                                }
                        }
                }
        }
}

@Composable
private fun InfoChipCompact(
        icon: ImageVector,
        text: String,
        modifier: Modifier = Modifier,
        isSingleLine: Boolean = true,
) {
        Surface(
                modifier = modifier,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                shape = RoundedCornerShape(8.dp),
                border =
                        BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
        ) {
                Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                        Icon(
                                icon,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                                text,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                maxLines = if (isSingleLine) 1 else 3,
                                overflow = TextOverflow.Ellipsis,
                        )
                }
        }
}

@Composable
private fun FreqItemCompact(
        freq: Int,
        label: String,
        isActive: Boolean,
        isOffline: Boolean = false,
) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(70.dp)
        ) {
                Box(
                        modifier =
                                Modifier.size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                                when {
                                                        isOffline ->
                                                                MaterialTheme.colorScheme.outline
                                                        isActive ->
                                                                MaterialTheme.colorScheme.primary
                                                        else ->
                                                                MaterialTheme.colorScheme
                                                                        .surfaceVariant
                                                }
                                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                        "$freq",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color =
                                if (isActive) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                )
        }
}

@Composable
fun BatteryStatItemVertical(
        icon: ImageVector,
        label: String,
        value: String,
        modifier: Modifier = Modifier,
) {
        Surface(
                modifier = modifier,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                shape = RoundedCornerShape(12.dp),
        ) {
                Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                        Icon(
                                icon,
                                null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                        value,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                        label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                )
                        }
                }
        }
}

@Composable
fun LinearUsageItemDetailed(
        title: String,
        used: String,
        total: String,
        progress: Float,
        color: Color,
) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                ) {
                        Text(
                                title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                                "$used / $total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                }
                LinearProgressIndicator(
                        progress = { progress },
                        modifier =
                                Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(50)),
                        color = color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round,
                )
                Text(
                        "${(progress * 100).toInt()}% Used",
                        style = MaterialTheme.typography.bodySmall,
                        color = color,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.End),
                )
        }
}

@Composable
private fun InfoCard(
        title: String,
        icon: ImageVector,
        defaultExpanded: Boolean = true,
        content: @Composable ColumnScope.() -> Unit,
) {
        var isExpanded by remember { mutableStateOf(defaultExpanded) }

        // Arrow rotation animation
        val arrowRotation by
                animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        animationSpec =
                                spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow,
                                ),
                        label = "arrowRotation",
                )

        // Icon scale animation on toggle
        var iconPressed by remember { mutableStateOf(false) }
        val iconScale by
                animateFloatAsState(
                        targetValue = if (iconPressed) 0.85f else 1f,
                        animationSpec =
                                spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium,
                                ),
                        label = "iconScale",
                )

        // Header icon glow animation
        val headerIconScale by
                animateFloatAsState(
                        targetValue = if (isExpanded) 1.1f else 1f,
                        animationSpec =
                                spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow,
                                ),
                        label = "headerIconScale",
                )

        GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                        iconPressed = true
                        isExpanded = !isExpanded
                },
        ) {
                // Reset icon press state after animation
                LaunchedEffect(iconPressed) {
                        if (iconPressed) {
                                delay(150)
                                iconPressed = false
                        }
                }

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                ) {
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                        ) {
                                Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color =
                                                if (isExpanded)
                                                        MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.secondaryContainer,
                                        tonalElevation = 2.dp,
                                        modifier = Modifier.scale(headerIconScale),
                                ) {
                                        Icon(
                                                icon,
                                                null,
                                                modifier = Modifier.padding(8.dp).size(24.dp),
                                                tint =
                                                        if (isExpanded)
                                                                MaterialTheme.colorScheme
                                                                        .onPrimaryContainer
                                                        else
                                                                MaterialTheme.colorScheme
                                                                        .onSecondaryContainer,
                                        )
                                }
                                Text(
                                        title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                )
                        }
                        IconButton(
                                onClick = {
                                        iconPressed = true
                                        isExpanded = !isExpanded
                                },
                                modifier = Modifier.scale(iconScale),
                        ) {
                                Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        null,
                                        modifier =
                                                Modifier.graphicsLayer {
                                                        rotationZ = arrowRotation
                                                },
                                )
                        }
                }
                AnimatedVisibility(
                        visible = isExpanded,
                        enter =
                                expandVertically(
                                        animationSpec =
                                                spring(
                                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                                        stiffness = Spring.StiffnessMediumLow,
                                                ),
                                        expandFrom = Alignment.Top,
                                ) +
                                        fadeIn(animationSpec = tween(200)) +
                                        slideInVertically(
                                                animationSpec =
                                                        spring(
                                                                dampingRatio =
                                                                        Spring.DampingRatioLowBouncy,
                                                                stiffness =
                                                                        Spring.StiffnessMediumLow,
                                                        ),
                                                initialOffsetY = { -it / 4 },
                                        ),
                        exit =
                                shrinkVertically(animationSpec = tween(150)) +
                                        fadeOut(animationSpec = tween(100)),
                ) {
                        Column(
                                modifier = Modifier.padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) { content() }
                }
        }
}

@Composable
fun InfoIconRow(icon: ImageVector, label: String, value: String) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
        ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                                icon,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                                label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                }
                Text(
                        value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                )
        }
}

@Composable
private fun BatteryLevelIndicator(level: Int, status: String, modifier: Modifier = Modifier) {
        val clamped = level.coerceIn(0, 100)
        val fillColor =
                when {
                        clamped <= 15 -> MaterialTheme.colorScheme.error
                        clamped <= 40 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                }
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                        modifier =
                                Modifier.width(24.dp)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                        modifier = Modifier.width(40.dp).height(70.dp),
                        contentAlignment = Alignment.BottomCenter
                ) {
                        Box(
                                modifier =
                                        Modifier.matchParentSize()
                                                .border(
                                                        2.dp,
                                                        MaterialTheme.colorScheme.outlineVariant,
                                                        RoundedCornerShape(8.dp)
                                                )
                        )
                        Box(
                                modifier =
                                        Modifier.padding(4.dp)
                                                .fillMaxWidth()
                                                .fillMaxHeight(clamped / 100f)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(fillColor)
                        )
                        if (status.contains("Charging")) {
                                Icon(
                                        Icons.Default.Bolt,
                                        null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.align(Alignment.Center),
                                )
                        }
                }
        }
}

@Composable
private fun MemoryTagChip(icon: ImageVector, label: String, value: String) {
        InfoChipCompact(icon = icon, text = "$label: $value")
}

/** Holiday decoration row with animated emojis */
@Composable
private fun HolidayDecorationRow(holiday: Holiday) {
        val infiniteTransition = rememberInfiniteTransition(label = "holiday_decor")

        val bounce by
                infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 8f,
                        animationSpec =
                                infiniteRepeatable(
                                        animation =
                                                tween(
                                                        durationMillis = 600,
                                                        easing = FastOutSlowInEasing
                                                ),
                                        repeatMode = RepeatMode.Reverse,
                                ),
                        label = "bounce",
                )

        val (emojis, colors) =
                when (holiday) {
                        Holiday.CHRISTMAS ->
                                Pair(
                                        listOf("🎅", "❄️", "🎄", "🎁", "⛄", "❄️", "🎄", "🎅"),
                                        listOf(
                                                Color(0xFFE53935),
                                                Color(0xFFFFFFFF),
                                                Color(0xFF43A047)
                                        ),
                                )
                        Holiday.NEW_YEAR ->
                                Pair(
                                        listOf("🎆", "✨", "🎇", "🥳", "🎉", "✨", "🎆", "🎊"),
                                        listOf(
                                                Color(0xFFFFD700),
                                                Color(0xFFFF6B6B),
                                                Color(0xFF4ECDC4)
                                        ),
                                )
                        Holiday.RAMADAN ->
                                Pair(
                                        listOf("🌙", "⭐", "🕌", "✨", "🤲", "⭐", "🌙", "🕋"),
                                        listOf(
                                                Color(0xFFFFD700),
                                                Color(0xFF4CAF50),
                                                Color(0xFF2196F3)
                                        ),
                                )
                        Holiday.EID_FITR ->
                                Pair(
                                        listOf("🎉", "🕌", "✨", "🤲", "🎊", "✨", "🕌", "🎉"),
                                        listOf(
                                                Color(0xFF4CAF50),
                                                Color(0xFFFFD700),
                                                Color(0xFF8BC34A)
                                        ),
                                )
                }

        Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors[0].copy(alpha = 0.1f),
                border = BorderStroke(1.dp, colors[0].copy(alpha = 0.3f)),
        ) {
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                ) {
                        emojis.forEachIndexed { index, emoji ->
                                val offset = if (index % 2 == 0) bounce else -bounce
                                Text(
                                        text = emoji,
                                        fontSize = 24.sp,
                                        modifier = Modifier.offset(y = offset.dp)
                                )
                        }
                }
        }
}
