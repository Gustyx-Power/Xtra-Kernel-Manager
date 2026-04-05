package id.xms.xtrakernelmanager.ui.screens.tuning.classic

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.RAMConfig
import id.xms.xtrakernelmanager.domain.usecase.RAMControlUseCase
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicMemoryTuningScreen(
    viewModel: TuningViewModel,
    onNavigateBack: () -> Unit
) {
    // Collect States
    val ramConfig by viewModel.preferencesManager.getRamConfig().collectAsState(initial = RAMConfig())
    val memoryStats by viewModel.memoryStats.collectAsState()
    val zramStatus by viewModel.zramStatus.collectAsState()
    val blockDeviceStates by viewModel.blockDeviceStates.collectAsState()
    val availableAlgorithms by viewModel.availableCompressionAlgorithms.collectAsState()
    val ramSetOnBoot by viewModel.preferencesManager.getRAMSetOnBoot().collectAsState(initial = false)

    Scaffold(
        containerColor = ClassicColors.Background,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ClassicColors.Surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = ClassicColors.OnSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Memory Tuning",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // RAM Overview Card
            ClassicRAMOverviewCard(
                memoryStats = memoryStats,
                ramConfig = ramConfig
            )

            // ZRAM Card
            ClassicZRAMCard(
                ramConfig = ramConfig,
                zramStatus = zramStatus,
                availableAlgorithms = availableAlgorithms,
                onConfigChange = { viewModel.setRAMParameters(it) }
            )

            // Swap Card
            ClassicSwapCard(
                ramConfig = ramConfig,
                onConfigChange = { viewModel.setRAMParameters(it) }
            )

            // I/O Scheduler Card
            ClassicIOSchedulerCard(
                blockDeviceStates = blockDeviceStates,
                onDeviceSchedulerChange = { device, scheduler ->
                    viewModel.setDeviceIOScheduler(device, scheduler)
                }
            )

            // Set on Boot Card
            ClassicSetOnBootCard(
                isEnabled = ramSetOnBoot,
                onToggle = { viewModel.setRAMSetOnBoot(it) }
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}


@Composable
private fun ClassicRAMOverviewCard(
    memoryStats: RAMControlUseCase.MemoryStats,
    ramConfig: RAMConfig
) {
    val totalRamGb = memoryStats.totalRamMb / 1024f
    val usedRamMb = memoryStats.totalRamMb - memoryStats.availableRamMb
    val usedRamGb = usedRamMb / 1024f
    val availableRamGb = memoryStats.availableRamMb / 1024f
    val cachedRamGb = memoryStats.cachedMb / 1024f
    val usagePercent = if (memoryStats.totalRamMb > 0) usedRamMb.toFloat() / memoryStats.totalRamMb else 0f

    val healthStatus = when {
        usagePercent < 0.5f -> "Healthy"
        usagePercent < 0.75f -> "Moderate"
        usagePercent < 0.9f -> "High"
        else -> "Critical"
    }

    val healthColor = when {
        usagePercent < 0.5f -> ClassicColors.Good
        usagePercent < 0.75f -> ClassicColors.Moderate
        else -> ClassicColors.Error
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
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
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ClassicColors.Primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Memory,
                            contentDescription = null,
                            tint = ClassicColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "RAM Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        Text(
                            text = "System memory status",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = healthColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = healthStatus.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = healthColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        letterSpacing = 1.sp
                    )
                }
            }

            HorizontalDivider(
                color = ClassicColors.OnSurface.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            // Usage Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${(usagePercent * 100).toInt()}%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                    Text(
                        text = "MEMORY USED",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "%.1f GB / %.1f GB".format(usedRamGb, totalRamGb),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                    Text(
                        text = "Total RAM",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ClassicColors.OnSurface.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(usagePercent.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(healthColor)
                )
            }

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClassicMemoryStatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.CheckCircle,
                    label = "Available",
                    value = "%.1f GB".format(availableRamGb),
                    color = ClassicColors.Good
                )
                ClassicMemoryStatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Cached,
                    label = "Cached",
                    value = "%.1f GB".format(cachedRamGb),
                    color = ClassicColors.Secondary
                )
            }
        }
    }
}

@Composable
private fun ClassicMemoryStatItem(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ClassicColors.Background
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}


@Composable
private fun ClassicZRAMCard(
    ramConfig: RAMConfig,
    zramStatus: RAMControlUseCase.ZramStatus,
    availableAlgorithms: List<String>,
    onConfigChange: (RAMConfig) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    var localZramSize by remember(ramConfig.zramSize) { 
        mutableFloatStateOf(ramConfig.zramSize.toFloat()) 
    }
    var localSwappiness by remember(ramConfig.swappiness) { 
        mutableFloatStateOf(ramConfig.swappiness.toFloat()) 
    }
    var localDirtyRatio by remember(ramConfig.dirtyRatio) { 
        mutableFloatStateOf(ramConfig.dirtyRatio.toFloat()) 
    }
    var localAlgorithm by remember(ramConfig.compressionAlgorithm) {
        mutableStateOf(ramConfig.compressionAlgorithm)
    }

    val configuredSizeMb = ramConfig.zramSize
    val zramUsagePercent = if (configuredSizeMb > 0) zramStatus.usedMb.toFloat() / configuredSizeMb else 0f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
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
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ClassicColors.Secondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Compress,
                            contentDescription = null,
                            tint = ClassicColors.Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "ZRAM",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        Text(
                            text = "Compressed RAM",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (zramStatus.isActive) 
                        ClassicColors.Good.copy(alpha = 0.2f)
                    else 
                        ClassicColors.Error.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (zramStatus.isActive) "ACTIVE" else "DISABLED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (zramStatus.isActive) ClassicColors.Good else ClassicColors.Error,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        letterSpacing = 1.sp
                    )
                }
            }

            HorizontalDivider(
                color = ClassicColors.OnSurface.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Size Display
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (configuredSizeMb >= 1024) 
                            "${configuredSizeMb / 1024} GB"
                        else 
                            "$configuredSizeMb MB",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                    Text(
                        text = "CONFIGURED SIZE",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Usage Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(ClassicColors.OnSurface.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(zramUsagePercent.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(ClassicColors.Secondary)
                        )
                    }
                }
                
                // Stats Grid
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClassicZRAMStatRow(
                        label = "Ratio",
                        value = "%.1fx".format(zramStatus.compressionRatio),
                        color = ClassicColors.Good
                    )
                    ClassicZRAMStatRow(
                        label = "Original",
                        value = "${zramStatus.usedMb} MB",
                        color = ClassicColors.Primary
                    )
                    ClassicZRAMStatRow(
                        label = "Compressed",
                        value = "${zramStatus.compressedMb} MB",
                        color = ClassicColors.Secondary
                    )
                }
            }

            // Expanded Configuration
            if (expanded) {
                HorizontalDivider(
                    color = ClassicColors.OnSurface.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ClassicColors.Background
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "CONFIGURATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        
                        // ZRAM Size Slider
                        ClassicSliderControl(
                            label = "Size",
                            value = localZramSize,
                            valueRange = 0f..4096f,
                            steps = 15,
                            onValueChange = { localZramSize = it },
                            onValueChangeFinished = {
                                onConfigChange(ramConfig.copy(zramSize = localZramSize.toInt()))
                            },
                            valueFormatter = { 
                                if (it.toInt() == 0) "Disabled" else "${it.toInt()} MB"
                            }
                        )
                        
                        // Compression Algorithm Dropdown
                        ClassicAlgorithmSelector(
                            label = "Algorithm",
                            options = availableAlgorithms,
                            selectedOption = localAlgorithm,
                            onOptionSelected = { algorithm ->
                                localAlgorithm = algorithm
                                onConfigChange(ramConfig.copy(compressionAlgorithm = algorithm))
                            }
                        )
                        
                        // Swappiness Slider
                        ClassicSliderControl(
                            label = "Swappiness",
                            value = localSwappiness,
                            valueRange = 0f..100f,
                            steps = 19,
                            onValueChange = { localSwappiness = it },
                            onValueChangeFinished = {
                                onConfigChange(ramConfig.copy(swappiness = localSwappiness.toInt()))
                            },
                            valueFormatter = { "${it.toInt()}%" }
                        )
                        
                        // Dirty Ratio Slider
                        ClassicSliderControl(
                            label = "Dirty Ratio",
                            value = localDirtyRatio,
                            valueRange = 0f..100f,
                            steps = 19,
                            onValueChange = { localDirtyRatio = it },
                            onValueChangeFinished = {
                                onConfigChange(ramConfig.copy(dirtyRatio = localDirtyRatio.toInt()))
                            },
                            valueFormatter = { "${it.toInt()}%" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassicZRAMStatRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ClassicColors.OnSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun ClassicSliderControl(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueFormatter: (Float) -> String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = ClassicColors.OnSurface
            )
            Text(
                text = valueFormatter(value),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.Primary
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = ClassicColors.Primary,
                activeTrackColor = ClassicColors.Primary,
                inactiveTrackColor = ClassicColors.OnSurface.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
private fun ClassicAlgorithmSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = ClassicColors.OnSurface
        )
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(8.dp),
            color = ClassicColors.Surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                ClassicColors.OnSurface.copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedOption,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.Primary
                )
                Icon(
                    Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    tint = ClassicColors.OnSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ClassicColors.Surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontWeight = if (option == selectedOption) FontWeight.Bold else FontWeight.Normal,
                            color = if (option == selectedOption) ClassicColors.Primary else ClassicColors.OnSurface
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
private fun ClassicSwapCard(
    ramConfig: RAMConfig,
    onConfigChange: (RAMConfig) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var localSwapSize by remember(ramConfig.swapSize) { 
        mutableFloatStateOf(ramConfig.swapSize.toFloat()) 
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
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
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ClassicColors.Accent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.SdCard,
                            contentDescription = null,
                            tint = ClassicColors.Accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Swap File",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        Text(
                            text = "Virtual memory extension",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (ramConfig.swapSize > 0) 
                        ClassicColors.Good.copy(alpha = 0.2f)
                    else 
                        ClassicColors.Error.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (ramConfig.swapSize > 0) "${ramConfig.swapSize} MB" else "DISABLED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (ramConfig.swapSize > 0) ClassicColors.Good else ClassicColors.Error,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        letterSpacing = 1.sp
                    )
                }
            }

            if (!expanded) {
                HorizontalDivider(
                    color = ClassicColors.OnSurface.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
                
                Text(
                    text = "Tap to configure swap file size",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassicColors.OnSurface.copy(alpha = 0.5f)
                )
            }

            // Expanded Configuration
            if (expanded) {
                HorizontalDivider(
                    color = ClassicColors.OnSurface.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ClassicColors.Background
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "SWAP SIZE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        
                        ClassicSliderControl(
                            label = "Size",
                            value = localSwapSize,
                            valueRange = 0f..8192f,
                            steps = 31,
                            onValueChange = { localSwapSize = it },
                            onValueChangeFinished = {
                                onConfigChange(ramConfig.copy(swapSize = localSwapSize.toInt()))
                            },
                            valueFormatter = { 
                                if (it.toInt() == 0) "Disabled" else "${it.toInt()} MB"
                            }
                        )
                        
                        // Info
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Rounded.Info,
                                contentDescription = null,
                                tint = ClassicColors.OnSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Swap extends RAM using storage. May impact performance.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ClassicColors.OnSurface.copy(alpha = 0.5f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassicIOSchedulerCard(
    blockDeviceStates: List<TuningViewModel.BlockDeviceState>,
    onDeviceSchedulerChange: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ClassicColors.Primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Storage,
                        contentDescription = null,
                        tint = ClassicColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "I/O Scheduler",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                    Text(
                        text = if (expanded) "Tap to collapse" else "Manage disk I/O",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                    )
                }
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = ClassicColors.OnSurface.copy(alpha = 0.4f)
                )
            }

            if (expanded) {
                HorizontalDivider(
                    color = ClassicColors.OnSurface.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
                
                if (blockDeviceStates.isEmpty()) {
                    Text(
                        text = "No block devices detected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClassicColors.OnSurface.copy(alpha = 0.5f)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        blockDeviceStates.forEach { state ->
                            ClassicDeviceIOSection(
                                deviceName = state.name.uppercase(),
                                currentScheduler = state.currentScheduler,
                                availableSchedulers = state.availableSchedulers,
                                onSchedulerChange = { scheduler ->
                                    onDeviceSchedulerChange(state.name, scheduler)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassicDeviceIOSection(
    deviceName: String,
    currentScheduler: String,
    availableSchedulers: List<String>,
    onSchedulerChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ClassicColors.Background
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Device Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ClassicColors.Good)
                    )
                    Text(
                        text = deviceName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                }
            }
            
            // Scheduler Selector
            Text(
                text = "SCHEDULER",
                style = MaterialTheme.typography.labelSmall,
                color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                shape = RoundedCornerShape(8.dp),
                color = ClassicColors.Surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    ClassicColors.OnSurface.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentScheduler,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.Primary
                    )
                    Icon(
                        Icons.Rounded.ArrowDropDown,
                        contentDescription = null,
                        tint = ClassicColors.OnSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(ClassicColors.Surface)
            ) {
                availableSchedulers.forEach { scheduler ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = scheduler,
                                fontWeight = if (scheduler == currentScheduler) FontWeight.Bold else FontWeight.Normal,
                                color = if (scheduler == currentScheduler) ClassicColors.Primary else ClassicColors.OnSurface
                            )
                        },
                        onClick = {
                            onSchedulerChange(scheduler)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassicSetOnBootCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ClassicColors.Primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.PowerSettingsNew,
                        contentDescription = null,
                        tint = ClassicColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Set on Boot",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                    Text(
                        text = "Apply settings on startup",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ClassicColors.Primary,
                    checkedTrackColor = ClassicColors.Primary.copy(alpha = 0.5f),
                    uncheckedThumbColor = ClassicColors.OnSurface.copy(alpha = 0.4f),
                    uncheckedTrackColor = ClassicColors.OnSurface.copy(alpha = 0.2f)
                )
            )
        }
    }
}
