package id.xms.xtrakernelmanager.ui.screens.tuning.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicGPUTuningScreen(
    viewModel: TuningViewModel,
    onNavigateBack: () -> Unit
) {
    val isMediatek by viewModel.isMediatek.collectAsState()
    val gpuInfo by viewModel.gpuInfo.collectAsState()
    val isFrequencyLocked by viewModel.isGpuFrequencyLocked.collectAsState()
    val lockedMinFreq by viewModel.lockedGpuMinFreq.collectAsState()
    val lockedMaxFreq by viewModel.lockedGpuMaxFreq.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    var showRendererDialog by remember { mutableStateOf(false) }
    var showRebootDialog by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var pendingRenderer by remember { mutableStateOf("") }
    var verificationSuccess by remember { mutableStateOf(false) }
    var verificationMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var selectedRenderer by remember { mutableStateOf(gpuInfo.rendererType) }

    LaunchedEffect(gpuInfo.rendererType) { 
        selectedRenderer = gpuInfo.rendererType 
    }

    var minFreqSlider by remember(gpuInfo.minFreq, isFrequencyLocked, lockedMinFreq) {
        mutableFloatStateOf(
            if (isFrequencyLocked && lockedMinFreq > 0) lockedMinFreq.toFloat()
            else gpuInfo.minFreq.toFloat()
        )
    }

    var maxFreqSlider by remember(gpuInfo.maxFreq, isFrequencyLocked, lockedMaxFreq) {
        mutableFloatStateOf(
            if (isFrequencyLocked && lockedMaxFreq > 0) lockedMaxFreq.toFloat()
            else gpuInfo.maxFreq.toFloat()
        )
    }

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
                        text = "GPU Tuning",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                }
            }
        }
    ) { paddingValues ->
        if (isMediatek) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = ClassicColors.Error,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "MediaTek Device Detected",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "GPU tuning is not available for MediaTek devices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // System Core Status Header
                ClassicGPUHeaderCard(gpuInfo = gpuInfo)

                // Frequency Control Card
                if (gpuInfo.availableFreqs.isNotEmpty()) {
                    ClassicFrequencyControlCard(
                        minFreq = minFreqSlider,
                        maxFreq = maxFreqSlider,
                        availableFreqs = gpuInfo.availableFreqs,
                        isLocked = isFrequencyLocked,
                        onMinFreqChange = { minFreqSlider = it },
                        onMaxFreqChange = { maxFreqSlider = it },
                        onApply = {
                            viewModel.setGPUFrequency(minFreqSlider.toInt(), maxFreqSlider.toInt())
                            if (isFrequencyLocked) viewModel.unlockGPUFrequency()
                        },
                        onToggleLock = {
                            if (isFrequencyLocked) viewModel.unlockGPUFrequency()
                            else viewModel.lockGPUFrequency(minFreqSlider.toInt(), maxFreqSlider.toInt())
                        }
                    )
                }

                // Power Level Card
                ClassicPowerLevelCard(
                    gpuInfo = gpuInfo,
                    viewModel = viewModel
                )

                // Renderer Selection Card
                ClassicRendererCard(
                    selectedRenderer = selectedRenderer,
                    onClick = { showRendererDialog = true }
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        
        // Renderer Selection Dialog
        if (showRendererDialog) {
            ClassicRendererSelectionDialog(
                selectedRenderer = selectedRenderer,
                onDismiss = { showRendererDialog = false },
                onSelect = { renderer ->
                    if (renderer != selectedRenderer) {
                        pendingRenderer = renderer
                        showRendererDialog = false
                        showRebootDialog = true
                    } else {
                        showRendererDialog = false
                    }
                }
            )
        }
        
        // Reboot Confirmation Dialog
        if (showRebootDialog) {
            ClassicRebootConfirmationDialog(
                gpuInfo = gpuInfo,
                pendingRenderer = pendingRenderer,
                onDismiss = { showRebootDialog = false },
                onConfirm = {
                    showRebootDialog = false
                    isProcessing = true
                    showVerificationDialog = true
                    coroutineScope.launch {
                        try {
                            viewModel.setGPURenderer(pendingRenderer)
                            kotlinx.coroutines.delay(2000)
                            val verified = viewModel.verifyRendererChange(pendingRenderer)
                            verificationSuccess = verified
                            if (!verified) {
                                verificationMessage = "Property set but verification uncertain."
                            }
                        } catch (e: Exception) {
                            verificationSuccess = false
                            verificationMessage = e.message ?: "Unknown error"
                        } finally {
                            isProcessing = false
                        }
                    }
                }
            )
        }
        
        // Verification Dialog
        if (showVerificationDialog) {
            ClassicVerificationDialog(
                isProcessing = isProcessing,
                verificationSuccess = verificationSuccess,
                verificationMessage = verificationMessage,
                pendingRenderer = pendingRenderer,
                onDismiss = { if (!isProcessing) showVerificationDialog = false },
                onReboot = {
                    coroutineScope.launch { viewModel.performReboot() }
                    showVerificationDialog = false
                }
            )
        }
    }
}

@Composable
private fun ClassicGPUHeaderCard(gpuInfo: GPUInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SYSTEM CORE STATUS",
                style = MaterialTheme.typography.labelSmall,
                color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                letterSpacing = 1.2.sp
            )
            Text(
                text = "GPU Tuner",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ClassicColors.Good)
                    )
                    Text(
                        text = "OPTIMIZED",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassicColors.OnSurface.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = gpuInfo.renderer.take(20),
                    style = MaterialTheme.typography.labelSmall,
                    color = ClassicColors.OnSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun ClassicFrequencyControlCard(
    minFreq: Float,
    maxFreq: Float,
    availableFreqs: List<Int>,
    isLocked: Boolean,
    onMinFreqChange: (Float) -> Unit,
    onMaxFreqChange: (Float) -> Unit,
    onApply: () -> Unit,
    onToggleLock: () -> Unit
) {
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
                            Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = ClassicColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Frequency Control",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        Text(
                            text = "Clock speed synchronization",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                if (isLocked) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ClassicColors.Moderate.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "LOCKED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.Moderate,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = ClassicColors.OnSurface.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            // Min Clock
            ClassicFrequencySlider(
                label = "MIN CLOCK",
                value = minFreq,
                valueRange = availableFreqs.minOrNull()!!.toFloat()..availableFreqs.maxOrNull()!!.toFloat(),
                onValueChange = onMinFreqChange,
                color = ClassicColors.Good
            )

            // Max Clock
            ClassicFrequencySlider(
                label = "MAX CLOCK",
                value = maxFreq,
                valueRange = availableFreqs.minOrNull()!!.toFloat()..availableFreqs.maxOrNull()!!.toFloat(),
                onValueChange = onMaxFreqChange,
                color = ClassicColors.Primary
            )

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable(onClick = onApply),
                    shape = RoundedCornerShape(10.dp),
                    color = ClassicColors.Background,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        ClassicColors.OnSurface.copy(alpha = 0.2f)
                    )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = ClassicColors.OnSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Apply",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ClassicColors.OnSurface
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable(onClick = onToggleLock),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isLocked) ClassicColors.Moderate.copy(alpha = 0.15f)
                    else ClassicColors.Primary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isLocked) ClassicColors.Moderate else ClassicColors.Primary
                    )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isLocked) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = if (isLocked) ClassicColors.Moderate else ClassicColors.Primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isLocked) "Unlock" else "Lock",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isLocked) ClassicColors.Moderate else ClassicColors.Primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassicFrequencySlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    color: Color
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
                style = MaterialTheme.typography.labelSmall,
                color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            Text(
                text = "${value.toInt()} MHz",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = ClassicColors.OnSurface.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
private fun ClassicPowerLevelCard(
    gpuInfo: GPUInfo,
    viewModel: TuningViewModel
) {
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
                        Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = ClassicColors.Accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Power Level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                    Text(
                        text = "TDP management and limiters",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                    )
                }
            }

            HorizontalDivider(
                color = ClassicColors.OnSurface.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            // Current Level Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${gpuInfo.powerLevel}%",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ClassicColors.Primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "TARGET: PERFORMANCE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.Primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        letterSpacing = 1.sp
                    )
                }
            }

            // Power Level Slider
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Eco",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassicColors.OnSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Standard",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassicColors.OnSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Extreme",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassicColors.OnSurface.copy(alpha = 0.5f)
                    )
                }
                
                val maxLevel = (gpuInfo.numPwrLevels - 1).coerceAtLeast(0)
                Slider(
                    value = gpuInfo.powerLevel.toFloat(),
                    onValueChange = { viewModel.setGPUPowerLevel(it.toInt()) },
                    valueRange = 0f..maxLevel.toFloat(),
                    steps = maxLevel - 1,
                    colors = SliderDefaults.colors(
                        thumbColor = ClassicColors.Accent,
                        activeTrackColor = ClassicColors.Accent,
                        inactiveTrackColor = ClassicColors.OnSurface.copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}

@Composable
private fun ClassicGPUStatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.Surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface
            )
        }
    }
}


@Composable
private fun ClassicRendererCard(
    selectedRenderer: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                        .background(ClassicColors.Secondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Palette,
                        contentDescription = null,
                        tint = ClassicColors.Secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GPU Renderer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.OnSurface
                    )
                    Text(
                        text = "Graphics rendering backend",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                    )
                }
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = ClassicColors.OnSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }

            HorizontalDivider(
                color = ClassicColors.OnSurface.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            // Current Renderer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURRENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ClassicColors.Primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = selectedRenderer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClassicColors.Primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

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
                    text = "Changing renderer requires a reboot to take effect",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassicColors.OnSurface.copy(alpha = 0.5f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ClassicRendererSelectionDialog(
    selectedRenderer: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val renderers = listOf(
        "OpenGL" to "Traditional graphics API",
        "Vulkan" to "Modern low-level API",
        "ANGLE" to "OpenGL ES over Vulkan/D3D",
        "SkiaGL" to "Skia with OpenGL backend",
        "SkiaVulkan" to "Skia with Vulkan backend"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ClassicColors.Surface,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Select Renderer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface
                )
                Text(
                    text = "Choose graphics rendering backend",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                renderers.forEach { (renderer, description) ->
                    val isSelected = renderer == selectedRenderer
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(renderer) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected)
                            ClassicColors.Primary.copy(alpha = 0.15f)
                        else
                            ClassicColors.Background,
                        border = if (isSelected)
                            androidx.compose.foundation.BorderStroke(2.dp, ClassicColors.Primary)
                        else
                            androidx.compose.foundation.BorderStroke(1.dp, ClassicColors.OnSurface.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = renderer,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ClassicColors.Primary else ClassicColors.OnSurface
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = ClassicColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ClassicColors.Primary
                )
            ) {
                Text(
                    text = "Close",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun ClassicRebootConfirmationDialog(
    gpuInfo: GPUInfo,
    pendingRenderer: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ClassicColors.Surface,
        title = {
            Text(
                text = "Change Renderer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "You are about to change the GPU renderer. This requires a reboot to take effect.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurface.copy(alpha = 0.7f)
                )
                
                // Current -> New Renderer Display
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ClassicColors.Background
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "CURRENT",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = gpuInfo.rendererType,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = ClassicColors.OnSurface
                            )
                        }
                        
                        Icon(
                            Icons.Rounded.ArrowDownward,
                            contentDescription = null,
                            tint = ClassicColors.OnSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "NEW",
                                style = MaterialTheme.typography.labelSmall,
                                color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = pendingRenderer,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = ClassicColors.Primary
                            )
                        }
                    }
                }
                
                HorizontalDivider(
                    color = ClassicColors.OnSurface.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
                
                // Warning
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = ClassicColors.Moderate,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Important",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ClassicColors.OnSurface
                        )
                        Text(
                            text = "• Reboot required for changes to take effect\n• Some renderers may not be compatible with your ROM\n• System may become unstable if incompatible",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClassicColors.OnSurface.copy(alpha = 0.6f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ClassicColors.Primary
                )
            ) {
                Text(
                    text = "Apply Changes",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ClassicColors.OnSurface.copy(alpha = 0.6f)
                )
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun ClassicVerificationDialog(
    isProcessing: Boolean,
    verificationSuccess: Boolean,
    verificationMessage: String,
    pendingRenderer: String,
    onDismiss: () -> Unit,
    onReboot: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        containerColor = ClassicColors.Surface,
        title = {
            Text(
                text = when {
                    isProcessing -> "Applying Changes"
                    verificationSuccess -> "Changes Applied"
                    else -> "Warning"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ClassicColors.OnSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Icon/Progress
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(56.dp),
                            strokeWidth = 4.dp,
                            color = ClassicColors.Primary
                        )
                    } else {
                        Icon(
                            imageVector = if (verificationSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = if (verificationSuccess) ClassicColors.Good else ClassicColors.Error
                        )
                    }
                }
                
                when {
                    isProcessing -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Writing system files...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ClassicColors.OnSurface
                            )
                            Text(
                                text = "Renderer: $pendingRenderer",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ClassicColors.OnSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    verificationSuccess -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Success Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = ClassicColors.Good.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    ClassicColors.Good.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = ClassicColors.Good,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Runtime Property Updated",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ClassicColors.OnSurface
                                        )
                                    }
                                    Text(
                                        text = "Renderer: $pendingRenderer",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            
                            // Reboot Required Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = ClassicColors.Error.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    ClassicColors.Error.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Rounded.Refresh,
                                            contentDescription = null,
                                            tint = ClassicColors.Error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Reboot Required",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ClassicColors.OnSurface
                                        )
                                    }
                                    Text(
                                        text = "Changes will take effect after reboot",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = ClassicColors.Error.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                ClassicColors.Error.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Failed to Apply Changes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ClassicColors.Error
                                )
                                if (verificationMessage.isNotBlank()) {
                                    Text(
                                        text = verificationMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ClassicColors.OnSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isProcessing) {
                if (verificationSuccess) {
                    TextButton(
                        onClick = onReboot,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = ClassicColors.Primary
                        )
                    ) {
                        Text(
                            text = "Reboot Now",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = ClassicColors.Primary
                        )
                    ) {
                        Text(
                            text = "Close",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        dismissButton = if (!isProcessing && verificationSuccess) {
            {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = ClassicColors.OnSurface.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = "Reboot Later",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else null,
        shape = RoundedCornerShape(20.dp)
    )
}
