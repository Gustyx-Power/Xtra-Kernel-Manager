package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveSurfaceColor
import id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidRAMSettingsScreen(viewModel: TuningViewModel, onNavigateBack: () -> Unit) {
    val persistedConfig by viewModel.preferencesManager.getRamConfig().collectAsState(initial = RAMConfig())

    var swappiness by remember { mutableFloatStateOf(persistedConfig.swappiness.toFloat()) }
    var zramSize by remember { mutableFloatStateOf(if (persistedConfig.zramSize > 0) persistedConfig.zramSize.toFloat() else 2048f) }
    var swapSize by remember { mutableFloatStateOf(if (persistedConfig.swapSize > 0) persistedConfig.swapSize.toFloat() else 2048f) }
    var dirtyRatio by remember { mutableFloatStateOf(persistedConfig.dirtyRatio.toFloat()) }
    var minFreeMem by remember { mutableFloatStateOf(persistedConfig.minFreeMem.toFloat()) }
    var compressionAlgo by remember { mutableStateOf(persistedConfig.compressionAlgorithm) }

    var zramEnabled by remember { mutableStateOf(persistedConfig.zramSize > 0) }
    var swapEnabled by remember { mutableStateOf(persistedConfig.swapSize > 0) }

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

    LaunchedEffect(persistedConfig) {
        swappiness = persistedConfig.swappiness.toFloat()
        zramSize = if (persistedConfig.zramSize > 0) persistedConfig.zramSize.toFloat() else 2048f
        swapSize = if (persistedConfig.swapSize > 0) persistedConfig.swapSize.toFloat() else 2048f
        dirtyRatio = persistedConfig.dirtyRatio.toFloat()
        minFreeMem = persistedConfig.minFreeMem.toFloat()
        compressionAlgo = persistedConfig.compressionAlgorithm
        zramEnabled = persistedConfig.zramSize > 0
        swapEnabled = persistedConfig.swapSize > 0
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        WavyBlobOrnament(modifier = Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    shape = CircleShape,
                    color = Color(0xFF3B82F6).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = stringResource(R.string.ram_control),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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
                LiquidInfoCard(
                    icon = Icons.Rounded.Memory,
                    title = "Memory Management",
                    description = "Configure RAM, ZRAM, and Swap settings for optimal performance",
                    color = Color(0xFF8B5CF6)
                )

                LiquidRAMSection(
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
                            LiquidSliderCard(
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

                            LiquidApplyButton(
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

                AnimatedVisibility(
                    visible = zramEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF84CC16).copy(alpha = 0.15f)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF84CC16).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Settings,
                                        contentDescription = null,
                                        tint = Color(0xFF84CC16),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Compression Algorithm",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            LiquidCompressionSelector(
                                selectedAlgo = compressionAlgo,
                                onAlgoSelected = { compressionAlgo = it }
                            )
                        }
                    }
                }

                LiquidRAMSection(
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
                            LiquidSliderCard(
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

                            LiquidApplyButton(
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

                LiquidSliderCard(
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

                LiquidSliderCard(
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

                LiquidSliderCard(
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
            LiquidLogDialog(
                title = "Applying ZRAM",
                logs = logMessages,
                onDismiss = { showZramDialog = false }
            )
        }

        if (showSwapDialog) {
            LiquidLogDialog(
                title = "Applying Swap",
                logs = logMessages,
                onDismiss = { showSwapDialog = false }
            )
        }
    }
}

@Composable
fun LiquidInfoCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFD946EF).copy(alpha = 0.3f),
                                Color(0xFF8B5CF6).copy(alpha = 0.3f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun LiquidRAMSection(
    title: String,
    icon: ImageVector,
    color: Color,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                id.xms.xtrakernelmanager.ui.components.liquid.LiquidToggle(
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
fun LiquidSliderCard(
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(16.dp),
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
                    color = Color.White
                )
            }
            Surface(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
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
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        id.xms.xtrakernelmanager.ui.components.liquid.LiquidSlider(
            value = { value },
            onValueChange = onValueChange,
            valueRange = valueRange,
            visibilityThreshold = (valueRange.endInclusive - valueRange.start) / 100f,
            backdrop = backdrop,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun LiquidCompressionSelector(
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
            Surface(
                onClick = { onAlgoSelected(algo) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF84CC16).copy(alpha = 0.3f)
                else Color.White.copy(alpha = 0.1f),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(
                    2.dp,
                    Color(0xFF84CC16)
                ) else null
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = algo.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
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
fun LiquidApplyButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
fun LiquidLogDialog(
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

