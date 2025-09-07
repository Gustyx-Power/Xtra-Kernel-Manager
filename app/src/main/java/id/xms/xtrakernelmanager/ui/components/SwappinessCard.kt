package id.xms.xtrakernelmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import id.xms.xtrakernelmanager.viewmodel.TuningViewModel
import kotlin.math.roundToInt

@Composable
fun SwappinessCard(
    vm: TuningViewModel,
) {
    val zramEnabled by vm.zramEnabled.collectAsState()
    val zramDisksize by vm.zramDisksize.collectAsState()
    val maxZramSize by vm.maxZramSize.collectAsState()
    val swappiness by vm.swappiness.collectAsState()
    val compressionAlgorithms by vm.compressionAlgorithms.collectAsState()
    val currentCompression by vm.currentCompression.collectAsState()
    val dirtyRatio by vm.dirtyRatio.collectAsState()
    val dirtyBackgroundRatio by vm.dirtyBackgroundRatio.collectAsState()
    val dirtyWriteback by vm.dirtyWriteback.collectAsState()
    val dirtyExpireCentisecs by vm.dirtyExpireCentisecs.collectAsState()
    val minFreeMemory by vm.minFreeMemory.collectAsState()
    val swapSize by vm.swapSize.collectAsState()
    val maxSwapSize by vm.maxSwapSize.collectAsState()
    val isSwapLoading by vm.isSwapLoading.collectAsState()
    val swapLogs by vm.swapLogs.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }

    // Dialog visibility states
    var showCompressionDialog by remember { mutableStateOf(false) }
    var showZramSizeDialog by remember { mutableStateOf(false) }
    var showSwappinessDialog by remember { mutableStateOf(false) }
    var showDirtyRatioDialog by remember { mutableStateOf(false) }
    var showDirtyBgRatioDialog by remember { mutableStateOf(false) }
    var showDirtyWritebackDialog by remember { mutableStateOf(false) }
    var showDirtyExpireDialog by remember { mutableStateOf(false) }
    var showMinFreeMemoryDialog by remember { mutableStateOf(false) }
    var showAdjustSwapSizeDialog by remember { mutableStateOf(false) }

    // Animation untuk pulse effect
    val infiniteTransition = rememberInfiniteTransition(label = "ram_control_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = Modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // RAM Control Header Section
            RamControlHeaderSection(
                zramEnabled = zramEnabled,
                zramDisksize = zramDisksize,
                pulseAlpha = pulseAlpha,
                isExpanded = isExpanded,
                onExpandClick = { isExpanded = !isExpanded }
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ZRAM Toggle Section
                    RamZramToggleSection(
                        zramEnabled = zramEnabled,
                        onZramToggle = { vm.setZramEnabled(!zramEnabled) }
                    )

                    AnimatedVisibility(visible = zramEnabled) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // ZRAM Settings
                            RamSettingItem(
                                icon = Icons.Default.Storage,
                                title = "ZRAM Size",
                                value = "${zramDisksize / (1024 * 1024)}MB",
                                description = "Adjust compressed RAM size",
                                color = MaterialTheme.colorScheme.primary,
                                onClick = { showZramSizeDialog = true }
                            )

                            RamSettingItem(
                                icon = Icons.Default.Compress,
                                title = "Compression",
                                value = currentCompression,
                                description = "Compression algorithm",
                                color = MaterialTheme.colorScheme.primary,
                                onClick = { showCompressionDialog = true }
                            )
                        }
                    }

                    // Always visible RAM settings (not dependent on ZRAM)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RamSettingItem(
                            icon = Icons.Default.Speed,
                            title = "Swappiness",
                            value = "$swappiness%",
                            description = "Memory swap aggressiveness",
                            color = MaterialTheme.colorScheme.primary,
                            onClick = { showSwappinessDialog = true }
                        )

                        RamSettingItem(
                            icon = Icons.Default.DataUsage,
                            title = "Dirty Ratio",
                            value = "$dirtyRatio%",
                            description = "Page cache dirty data threshold",
                            color = MaterialTheme.colorScheme.primary,
                            onClick = { showDirtyRatioDialog = true }
                        )

                        RamSettingItem(
                            icon = Icons.Default.Analytics,
                            title = "Dirty Background Ratio",
                            value = "$dirtyBackgroundRatio%",
                            description = "Background writeback threshold",
                            color = MaterialTheme.colorScheme.primary,
                            onClick = { showDirtyBgRatioDialog = true }
                        )

                        RamSettingItem(
                            icon = Icons.Default.Timer,
                            title = "Dirty Writeback",
                            value = "${dirtyWriteback}s",
                            description = "Writeback interval time",
                            color = MaterialTheme.colorScheme.primary,
                            onClick = { showDirtyWritebackDialog = true }
                        )

                        RamSettingItem(
                            icon = Icons.Default.Schedule,
                            title = "Dirty Expire",
                            value = "${dirtyExpireCentisecs}cs",
                            description = "Page expiration time",
                            color = MaterialTheme.colorScheme.primary,
                            onClick = { showDirtyExpireDialog = true }
                        )

                        RamSettingItem(
                            icon = Icons.Default.Memory,
                            title = "Min Free Memory",
                            value = "${minFreeMemory}MB",
                            description = "Minimum free memory reserve",
                            color = MaterialTheme.colorScheme.primary,
                            onClick = { showMinFreeMemoryDialog = true }
                        )

                        RamSettingItem(
                            icon = Icons.Default.SwapHoriz,
                            title = "Swap Size",
                            value = if (swapSize == 0L) "Disabled" else "${swapSize / (1024 * 1024)}MB",
                            description = "Virtual memory swap file size",
                            color = MaterialTheme.colorScheme.primary,
                            onClick = { showAdjustSwapSizeDialog = true }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGS ---
    if (showZramSizeDialog && zramEnabled) {
        ZramSizeDialog(
            currentSize = zramDisksize,
            maxSize = maxZramSize,
            onDismiss = { showZramSizeDialog = false },
            onConfirm = { newSizeInBytes: Long ->
                vm.setZramDisksize(newSizeInBytes)
                showZramSizeDialog = false
            }
        )
    }

    if (showCompressionDialog && zramEnabled) {
        CompressionAlgorithmDialog(
            compressionAlgorithms = compressionAlgorithms,
            currentCompression = currentCompression,
            onDismiss = { showCompressionDialog = false },
            onAlgorithmSelected = { algo: String ->
                if (algo != currentCompression) {
                    vm.setCompression(algo)
                }
                showCompressionDialog = false
            }
        )
    }

    if (showSwappinessDialog) {
        SliderSettingDialog(
            showDialog = showSwappinessDialog,
            title = "Set Swappiness",
            currentValue = swappiness,
            valueSuffix = "%",
            valueRange = 0f..100f,
            steps = 99,
            onDismissRequest = { showSwappinessDialog = false },
            onApplyClicked = { newValue: Int ->
                vm.setSwappiness(newValue)
                showSwappinessDialog = false
            }
        )
    }

    if (showDirtyRatioDialog) {
        SliderSettingDialog(
            showDialog = showDirtyRatioDialog,
            title = "Set Dirty Ratio",
            currentValue = dirtyRatio,
            valueSuffix = "%",
            valueRange = 0f..100f,
            steps = 99,
            onDismissRequest = { showDirtyRatioDialog = false },
            onApplyClicked = { newValue: Int ->
                vm.setDirtyRatio(newValue)
                showDirtyRatioDialog = false
            }
        )
    }

    if (showDirtyBgRatioDialog) {
        SliderSettingDialog(
            showDialog = showDirtyBgRatioDialog,
            title = "Set Dirty Background Ratio",
            currentValue = dirtyBackgroundRatio,
            valueSuffix = "%",
            valueRange = 0f..100f,
            steps = 99,
            onDismissRequest = { showDirtyBgRatioDialog = false },
            onApplyClicked = { newValue: Int ->
                vm.setDirtyBackgroundRatio(newValue)
                showDirtyBgRatioDialog = false
            }
        )
    }

    if (showDirtyWritebackDialog) {
        SliderSettingDialog(
            showDialog = showDirtyWritebackDialog,
            title = "Set Dirty Writeback",
            currentValue = dirtyWriteback,
            valueSuffix = " sec",
            valueRange = 0f..300f,
            steps = 299,
            onDismissRequest = { showDirtyWritebackDialog = false },
            onApplyClicked = { newValue: Int ->
                vm.setDirtyWriteback(newValue)
                showDirtyWritebackDialog = false
            }
        )
    }

    if (showDirtyExpireDialog) {
        SliderSettingDialog(
            showDialog = showDirtyExpireDialog,
            title = "Set Dirty Expire",
            currentValue = dirtyExpireCentisecs,
            valueSuffix = " cs",
            valueRange = 0f..30000f,
            steps = 29999,
            onDismissRequest = { showDirtyExpireDialog = false },
            onApplyClicked = { newValue: Int ->
                vm.setDirtyExpireCentisecs(newValue)
                showDirtyExpireDialog = false
            }
        )
    }

    if (showMinFreeMemoryDialog) {
        SliderSettingDialog(
            showDialog = showMinFreeMemoryDialog,
            title = "Set Min Free Memory",
            currentValue = minFreeMemory,
            valueSuffix = " MB",
            valueRange = 0f..1024f,
            steps = 1023,
            onDismissRequest = { showMinFreeMemoryDialog = false },
            onApplyClicked = { newValue: Int ->
                vm.setMinFreeMemory(newValue)
                showMinFreeMemoryDialog = false
            }
        )
    }

    if (showAdjustSwapSizeDialog) {
        SwapSizeDialog(
            currentSize = swapSize,
            maxSize = maxSwapSize,
            onDismiss = { showAdjustSwapSizeDialog = false },
            onConfirm = { newSizeInBytes: Long ->
                vm.setSwapSize(newSizeInBytes)
                showAdjustSwapSizeDialog = false
            }
        )
    }

    // Show loading dialog when swap operation is in progress
    if (isSwapLoading) {
        SwapLoadingDialog(
            logs = swapLogs,
            onDismissRequest = { } // Cannot dismiss while loading
        )
    }
}

@Composable
fun RamControlHeaderSection(
    zramEnabled: Boolean,
    zramDisksize: Long,
    pulseAlpha: Float,
    isExpanded: Boolean,
    onExpandClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "RAM Control",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            // RAM Status Box
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (zramEnabled) "ZRAM: ${zramDisksize / (1024 * 1024)}MB Active" else "ZRAM: Disabled",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }

        // Animated RAM Icon with pulse effect
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = "Memory",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Icon(
                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun RamZramToggleSection(
    zramEnabled: Boolean,
    onZramToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (zramEnabled) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                }
            )
            .clickable { onZramToggle() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    text = "ZRAM State",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (zramEnabled) "Compressed RAM enabled" else "Compressed RAM disabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Text(
            text = if (zramEnabled) "ON" else "OFF",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = if (zramEnabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            }
        )
    }
}

@Composable
fun RamSettingItem(
    icon: ImageVector,
    title: String,
    value: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(20.dp),
                tint = color
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Configure",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}


// Enhanced Slider Dialog with SuperGlassCard design
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderSettingDialog(
    showDialog: Boolean,
    title: String,
    currentValue: Int,
    valueSuffix: String = "",
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onDismissRequest: () -> Unit,
    onApplyClicked: (Int) -> Unit,
    additionalInfo: String? = null
) {
    if (showDialog) {
        var sliderTempValue by remember(currentValue) { mutableStateOf(currentValue.toFloat()) }

        // Animation for value changes
        val animatedValue by animateFloatAsState(
            targetValue = sliderTempValue,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "slider_value_animation"
        )

        // Get feature explanation based on title
        val featureExplanation = getFeatureExplanation(title)

        BasicAlertDialog(
            onDismissRequest = onDismissRequest,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            ),
            content = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Header with icon and title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getFeatureIcon(title),
                                        contentDescription = "Settings",
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Column {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "RAM & Performance Control",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            // Feature explanation card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Info",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "About This Feature",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = featureExplanation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            if (additionalInfo != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Text(
                                        text = additionalInfo,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }

                            // Enhanced value display
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Current Value",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${animatedValue.roundToInt()}$valueSuffix",
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Enhanced slider
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Slider(
                                    value = sliderTempValue,
                                    onValueChange = { sliderTempValue = it },
                                    valueRange = valueRange,
                                    steps = steps,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        activeTickColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        inactiveTickColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                )

                                // Range indicators
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${valueRange.start.roundToInt()}$valueSuffix",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "${valueRange.endInclusive.roundToInt()}$valueSuffix",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismissRequest,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cancel", fontWeight = FontWeight.Medium)
                                }

                                FilledTonalButton(
                                    onClick = {
                                        onApplyClicked(sliderTempValue.roundToInt())
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Apply",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Apply", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            })
    }
}

// Helper function to get feature explanations
private fun getFeatureExplanation(title: String): String {
    return when {
        title.contains("Swappiness", ignoreCase = true) ->
            "Controls how aggressively the kernel moves data from RAM to storage. Low values (0-10) make the system focus more on using RAM, high values (60-100) use swap more frequently to save RAM."

        title.contains("ZRAM", ignoreCase = true) ->
            "Sets the size of compressed RAM to increase virtual memory capacity. ZRAM compresses data in RAM to store more applications without using slower storage."

        title.contains("Dirty Ratio", ignoreCase = true) ->
            "Sets the percentage of RAM used for write cache before forcing writes to storage. Higher values = better performance but risk of data loss during crashes."

        title.contains("Dirty Background", ignoreCase = true) ->
            "Sets when the kernel starts gradually writing cache to storage in the background. Helps maintain stable performance by reducing lag during large writes."

        title.contains("Min Free Memory", ignoreCase = true) ->
            "Sets the minimum amount of RAM that must always remain free. Higher values make the system more responsive but reduce RAM available for applications."

        title.contains("Dirty Expire", ignoreCase = true) ->
            "Sets how long cache data can persist before being forced to write to storage. Lower values = safer data but reduced performance."

        title.contains("Dirty Writeback", ignoreCase = true) ->
            "Sets the time interval for the kernel to check and write expired cache. Affects how often the system performs automatic cache cleanup."

        else ->
            "This setting affects how the system manages memory and performance. Adjust according to your device usage needs for optimal results."
    }
}

// Helper function to get appropriate icons
fun getFeatureIcon(title: String): ImageVector {
    return when {
        title.contains("Swappiness", ignoreCase = true) -> Icons.Default.SwapVert
        title.contains("ZRAM", ignoreCase = true) -> Icons.Default.Compress
        title.contains("Dirty", ignoreCase = true) -> Icons.Default.Storage
        title.contains("Memory", ignoreCase = true) -> Icons.Default.Memory
        else -> Icons.Default.Tune
    }
}

// Placeholder dialog composables - these need to be implemented based on your existing dialogs
@Composable
fun ZramSizeDialog(
    currentSize: Long,
    maxSize: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    SliderSettingDialog(
        showDialog = true,
        title = "Set ZRAM Size",
        currentValue = (currentSize / (1024 * 1024)).toInt(),
        valueSuffix = " MB",
        valueRange = 128f..(maxSize / (1024 * 1024)).toFloat(),
        steps = ((maxSize / (1024 * 1024)) - 128).toInt() / 128,
        onDismissRequest = onDismiss,
        onApplyClicked = { newValue ->
            onConfirm(newValue * 1024L * 1024L)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressionAlgorithmDialog(
    compressionAlgorithms: List<String>,
    currentCompression: String,
    onDismiss: () -> Unit,
    onAlgorithmSelected: (String) -> Unit
) {
    // Add animation for dialog appearance
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        ),
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = visible
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .heightIn(min = 300.dp, max = 600.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Enhanced Header with better styling
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Compress,
                                        contentDescription = "Compress",
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Choose Compression Algorithm",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "ZRAM Performance Settings",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Enhanced explanation card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                shape = RoundedCornerShape(24.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Info",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "About This Feature",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "The compression algorithm used by ZRAM to compress data in memory. LZ4 prioritizes speed, ZSTD high efficiency, LZO optimal balance between speed and compression ratio.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            // Enhanced algorithm selection list
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 280.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(compressionAlgorithms) { algorithm ->
                                    val isSelected = algorithm == currentCompression

                                    AnimatedVisibility(
                                        visible = true
                                    ) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { onAlgorithmSelected(algorithm) },
                                                    colors = RadioButtonDefaults.colors(
                                                        selectedColor = MaterialTheme.colorScheme.primary,
                                                        unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                    )
                                                )

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = algorithm.uppercase(),
                                                        style = MaterialTheme.typography.bodyLarge.copy(
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                                        ),
                                                        color = if (isSelected)
                                                            MaterialTheme.colorScheme.primary
                                                        else
                                                            MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = when (algorithm.lowercase()) { // ENGLISH_TRANSLATION
                                                            "lz4" -> "Maximum speed • Low latency"
                                                            "zstd" -> "High efficiency • Best compression ratio"
                                                            "lzo" -> "Optimal balance • Stable"
                                                            "lz4hc" -> "High compression • Moderate speed"
                                                            "deflate" -> "Standard compression • Moderate speed"
                                                            "lzma" -> "Maximum compression • Slower"
                                                            "bzip2" -> "Better compression • Slower"
                                                            "zlib" -> "Balanced compression • Moderate speed"
                                                            "lzo-rle" -> "Fast compression • Low CPU usage"
                                                            else -> "Compression algorithm • Good performance"
                                                        },
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }

                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Selected",
                                                        modifier = Modifier.size(20.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Enhanced action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("CANCEL", fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        })
}

@Composable
fun SwapSizeDialog(
    currentSize: Long,
    maxSize: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    SliderSettingDialog(
        showDialog = true,
        title = "Set Swap Size",
        currentValue = if (currentSize == 0L) 0 else (currentSize / (1024 * 1024)).toInt(),
        valueSuffix = " MB",
        valueRange = 0f..(maxSize / (1024 * 1024)).toFloat(),
        steps = (maxSize / (1024 * 1024)).toInt() / 128,
        onDismissRequest = onDismiss,
        onApplyClicked = { newValue ->
            onConfirm(if (newValue == 0) 0L else newValue * 1024L * 1024L)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapLoadingDialog(
    logs: List<String>,
    onDismissRequest: () -> Unit
) {
    // Add animation for dialog appearance
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    BasicAlertDialog(
        onDismissRequest = { }, // Cannot dismiss while loading
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        ),
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = visible
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .heightIn(min = 300.dp, max = 600.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .heightIn(max = 550.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Enhanced header with animated progress indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                                    Color.Transparent
                                                ),
                                                radius = 60f
                                            )
                                        )
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            RoundedCornerShape(24.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 3.dp
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Processing Swap File...",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Configuring Virtual Memory",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Enhanced explanation card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                shape = RoundedCornerShape(24.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Info",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Process in Progress",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "The system is configuring the swap file to increase virtual memory capacity. This process may take a while depending on the size of the file being configured.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            // Enhanced logs section with better styling - Always visible to prevent flickering
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, false),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                shape = RoundedCornerShape(24.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Terminal,
                                            contentDescription = "Logs",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Real-time Process Log",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // Scrollable logs with better formatting
                                    val listState = rememberLazyListState()

                                    // Auto-scroll to bottom when new logs are added
                                    LaunchedEffect(logs.size) {
                                        if (logs.isNotEmpty()) {
                                            listState.animateScrollToItem(logs.size - 1)
                                        }
                                    }

                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier.heightIn(min = 80.dp, max = 250.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (logs.isEmpty()) {
                                            item {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(8.dp),
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Initializing process...",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.Normal
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                        lineHeight = 16.sp
                                                    )
                                                }
                                            }
                                        } else {
                                            items(logs.takeLast(50)) { log -> // Limit to last 50 logs for performance
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primary)
                                                            .offset(y = 6.dp)
                                                    )

                                                    Text(
                                                        text = log,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.Normal
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        lineHeight = 16.sp,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Progress indicator section
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                shape = RoundedCornerShape(24.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "Please wait, do not close the application...",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
