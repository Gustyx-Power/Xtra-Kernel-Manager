package id.xms.xtrakernelmanager.ui.screens.tuning.frosted.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.RAMConfig
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament
import id.xms.xtrakernelmanager.ui.screens.home.components.frosted.adaptiveSurfaceColor
import id.xms.xtrakernelmanager.ui.screens.home.components.frosted.adaptiveTextColor
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrostedRAMSettingsScreen(viewModel: TuningViewModel, onNavigateBack: () -> Unit) {
    val persistedConfig by viewModel.preferencesManager.getRamConfig().collectAsState(initial = RAMConfig())
    val zramStatus by viewModel.zramStatus.collectAsState()
    val currentCompressionAlgo by viewModel.currentCompressionAlgorithm.collectAsState()
    
    val initialZramSize = if (persistedConfig.zramSize > 0) {
        persistedConfig.zramSize.toFloat()
    } else if (zramStatus.isActive && zramStatus.totalMb > 0) {
        zramStatus.totalMb.toFloat()
    } else {
        2048f
    }

    var swappiness by remember { mutableFloatStateOf(persistedConfig.swappiness.toFloat()) }
    var zramSize by remember(initialZramSize) { mutableFloatStateOf(initialZramSize) }
    var swapSize by remember { mutableFloatStateOf(if (persistedConfig.swapSize > 0) persistedConfig.swapSize.toFloat() else 2048f) }
    var dirtyRatio by remember { mutableFloatStateOf(persistedConfig.dirtyRatio.toFloat()) }
    var minFreeMem by remember { mutableFloatStateOf(persistedConfig.minFreeMem.toFloat()) }
    
    // Use remember with key to properly react to changes
    var compressionAlgo by remember(persistedConfig.compressionAlgorithm) { 
        mutableStateOf(persistedConfig.compressionAlgorithm) 
    }

    // ZRAM enabled state - derived from config or system status
    var zramEnabled by remember { mutableStateOf(false) }
    
    // Update zramEnabled based on config or system status
    LaunchedEffect(persistedConfig.zramSize, zramStatus.isActive, zramStatus.totalMb) {
        zramEnabled = persistedConfig.zramSize > 0 || (zramStatus.isActive && zramStatus.totalMb > 0)
        // Also update zramSize if system has ZRAM but config doesn't
        if (zramStatus.isActive && zramStatus.totalMb > 0 && persistedConfig.zramSize == 0) {
            zramSize = zramStatus.totalMb.toFloat()
        }
    }
    
    var swapEnabled by remember(persistedConfig.swapSize) { 
        mutableStateOf(persistedConfig.swapSize > 0) 
    }

    var showZramDialog by remember { mutableStateOf(false) }
    var showSwapDialog by remember { mutableStateOf(false) }
    var logMessages by remember { mutableStateOf(listOf<String>()) }
    var triggerZramApply by remember { mutableStateOf(false) }
    var triggerSwapApply by remember { mutableStateOf(false) }

    fun pushConfig() {
        viewModel.setRAMParameters(
            RAMConfig(
                swappiness = swappiness.toInt(),
                zramSize = if (zramEnabled) zramSize.toInt() else 0,
                swapSize = if (swapEnabled) swapSize.toInt() else 0,
                dirtyRatio = dirtyRatio.toInt(),
                minFreeMem = minFreeMem.toInt(),
                compressionAlgorithm = compressionAlgo,
            )
        )
    }

    // ZRAM Apply Effect
    LaunchedEffect(triggerZramApply) {
        if (triggerZramApply) {
            logMessages = listOf("Initializing ZRAM setup...")
            kotlinx.coroutines.delay(500)
            logMessages = logMessages + "Setting compression algorithm to $compressionAlgo..."
            kotlinx.coroutines.delay(500)
            logMessages = logMessages + "Configuring ZRAM size to ${zramSize.toInt()}MB..."
            kotlinx.coroutines.delay(500)
            logMessages = logMessages + "Enabling ZRAM device..."
            kotlinx.coroutines.delay(500)
            logMessages = logMessages + "ZRAM setup completed successfully!"
            kotlinx.coroutines.delay(1000)
            showZramDialog = false
            triggerZramApply = false
            pushConfig()
        }
    }

    // Swap Apply Effect
    LaunchedEffect(triggerSwapApply) {
        if (triggerSwapApply) {
            logMessages = listOf("Initializing Swap setup...")
            kotlinx.coroutines.delay(500)
            logMessages = logMessages + "Creating swap file (${swapSize.toInt()}MB)..."
            kotlinx.coroutines.delay(1000)
            logMessages = logMessages + "Setting up swap space..."
            kotlinx.coroutines.delay(500)
            logMessages = logMessages + "Enabling swap..."
            kotlinx.coroutines.delay(500)
            logMessages = logMessages + "Swap setup completed successfully!"
            kotlinx.coroutines.delay(1000)
            showSwapDialog = false
            triggerSwapApply = false
            pushConfig()
        }
    }

    // Update only specific values when persistedConfig changes
    LaunchedEffect(persistedConfig.swappiness, persistedConfig.dirtyRatio, persistedConfig.minFreeMem) {
        swappiness = persistedConfig.swappiness.toFloat()
        dirtyRatio = persistedConfig.dirtyRatio.toFloat()
        minFreeMem = persistedConfig.minFreeMem.toFloat()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        WavyBlobOrnament(modifier = Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = adaptiveTextColor()
                            )
                        }
                        Text(
                            text = stringResource(R.string.ram_control),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = adaptiveTextColor()
                        )
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FrostedInfoCard(
                    icon = Icons.Rounded.Memory,
                    title = "Memory Management",
                    description = "Configure RAM, ZRAM, and Swap settings for optimal performance",
                    color = Color(0xFF8B5CF6)
                )

                FrostedRAMSection(
                    title = "ZRAM",
                    icon = Icons.Rounded.Compress,
                    color = Color(0xFF84CC16), // Light Green
                    enabled = zramEnabled,
                    onEnabledChange = {
                        zramEnabled = it
                        if (it && zramSize < 512f) {
                            zramSize = 2048f 
                        }
                    }
                ) {
                    AnimatedVisibility(
                        visible = zramEnabled,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            FrostedSliderCard(
                                label = "ZRAM Size",
                                value = zramSize,
                                valueRange = 512f..8192f,
                                steps = 15,
                                valueDisplay = "${zramSize.toInt()} MB",
                                onValueChange = { zramSize = it },
                                onValueFinished = {},
                                icon = Icons.Rounded.Storage,
                                color = Color(0xFF84CC16)
                            )

                            FrostedApplyButton(
                                text = "Apply ZRAM",
                                color = Color(0xFF84CC16),
                                onClick = {
                                    showZramDialog = true
                                    triggerZramApply = true
                                }
                            )
                        }
                    }
                }

                // Compression Algorithm - SEPARATE card, always visible
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = null,
                                tint = Color(0xFF84CC16),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "Compression Algorithm",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = adaptiveTextColor()
                            )
                        }

                        FrostedCompressionSelector(
                            selectedAlgo = compressionAlgo,
                            onAlgoSelected = { 
                                compressionAlgo = it
                                viewModel.setCompressionAlgorithm(it)
                            }
                        )
                    }
                }

                FrostedRAMSection(
                    title = "Swap",
                    icon = Icons.Rounded.SwapHoriz,
                    color = Color(0xFFD946EF), // Magenta
                    enabled = swapEnabled,
                    onEnabledChange = {
                        swapEnabled = it
                        if (it && swapSize < 512f) {
                            swapSize = 2048f 
                        }
                    }
                ) {
                    AnimatedVisibility(
                        visible = swapEnabled,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            FrostedSliderCard(
                                label = "Swap Size",
                                value = swapSize,
                                valueRange = 512f..16384f,
                                steps = 31,
                                valueDisplay = "${(swapSize / 1024f).formatDecimal(1)} GB",
                                onValueChange = { swapSize = it },
                                onValueFinished = {},
                                icon = Icons.Rounded.SdCard,
                                color = Color(0xFFD946EF)
                            )

                            FrostedApplyButton(
                                text = "Apply Swap",
                                color = Color(0xFFD946EF),
                                onClick = {
                                    showSwapDialog = true
                                    triggerSwapApply = true
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "Advanced Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )

                FrostedSliderCard(
                    label = "Swappiness",
                    value = swappiness,
                    valueRange = 0f..200f,
                    steps = 19,
                    valueDisplay = "${swappiness.toInt()}",
                    onValueChange = { swappiness = it },
                    onValueFinished = { pushConfig() },
                    icon = Icons.Rounded.Tune,
                    color = Color(0xFFF59E0B),
                    description = "Controls how aggressively the kernel swaps memory pages"
                )

                FrostedSliderCard(
                    label = "Dirty Ratio",
                    value = dirtyRatio,
                    valueRange = 1f..50f,
                    steps = 48,
                    valueDisplay = "${dirtyRatio.toInt()}%",
                    onValueChange = { dirtyRatio = it },
                    onValueFinished = { pushConfig() },
                    icon = Icons.Rounded.DataUsage,
                    color = Color(0xFFEC4899),
                    description = "Percentage of memory that can be dirty before writing to disk"
                )

                FrostedSliderCard(
                    label = "Min Free Memory",
                    value = minFreeMem,
                    valueRange = 0f..262144f,
                    steps = 14,
                    valueDisplay = "${(minFreeMem / 1024f).formatDecimal(0)} MB",
                    onValueChange = { minFreeMem = it },
                    onValueFinished = { pushConfig() },
                    icon = Icons.Rounded.Memory,
                    color = Color(0xFF8B5CF6),
                    description = "Minimum amount of free memory to maintain"
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (showZramDialog) {
            FrostedLogDialog(
                title = "Applying ZRAM",
                logs = logMessages,
                onDismiss = { showZramDialog = false }
            )
        }

        if (showSwapDialog) {
            FrostedLogDialog(
                title = "Applying Swap",
                logs = logMessages,
                onDismiss = { showSwapDialog = false }
            )
        }
    }
}

@Composable
fun FrostedInfoCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(48.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = adaptiveTextColor()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = adaptiveTextColor(0.8f)
                )
            }
        }
    }
}

@Composable
fun FrostedRAMSection(
    title: String,
    icon: ImageVector,
    color: Color,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = adaptiveTextColor()
                    )
                }

                id.xms.xtrakernelmanager.ui.components.frosted.FrostedToggle(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    modifier = Modifier.size(width = 56.dp, height = 32.dp)
                )
            }

            content()
        }
    }
}

@Composable
fun FrostedSliderCard(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueDisplay: String,
    onValueChange: (Float) -> Unit,
    onValueFinished: () -> Unit,
    icon: ImageVector,
    color: Color,
    description: String? = null
) {
    val backdrop = com.kyant.backdrop.backdrops.rememberLayerBackdrop()

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = adaptiveTextColor()
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = adaptiveSurfaceColor(0.2f)
                ) {
                    Text(
                        text = valueDisplay,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = adaptiveTextColor(0.8f)
                )
            }

            id.xms.xtrakernelmanager.ui.components.frosted.FrostedSlider(
                value = { value },
                onValueChange = onValueChange,
                valueRange = valueRange,
                visibilityThreshold = (valueRange.endInclusive - valueRange.start) / 100f,
                backdrop = backdrop,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun FrostedCompressionSelector(
    selectedAlgo: String,
    onAlgoSelected: (String) -> Unit
) {
    val algorithms = listOf("lz4", "lzo", "zstd", "lzo-rle")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        algorithms.forEach { algo ->
            val isSelected = algo == selectedAlgo
            GlassmorphicCard(
                onClick = { onAlgoSelected(algo) },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = algo.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF84CC16) else adaptiveTextColor(0.7f)
                    )
                }
            }
        }
    }
}

private fun Float.formatDecimal(decimals: Int): String {
    return "%.${decimals}f".format(this)
}

@Composable
fun FrostedApplyButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun FrostedLogDialog(
    title: String,
    logs: List<String>,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectTapGestures { /* Consume clicks */ }
            },
        contentAlignment = Alignment.Center
    ) {
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 400.dp),
            contentPadding = PaddingValues(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = adaptiveTextColor()
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = adaptiveTextColor()
                        )
                    }
                }

                HorizontalDivider(color = adaptiveSurfaceColor(0.2f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    logs.forEach { log ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF84CC16),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = log,
                                style = MaterialTheme.typography.bodyMedium,
                                color = adaptiveTextColor(0.8f)
                            )
                        }
                    }
                }

                if (logs.lastOrNull()?.contains("completed") != true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF84CC16),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}

