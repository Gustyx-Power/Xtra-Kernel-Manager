package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialog
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialogButton
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidGPUSettingsScreen(viewModel: TuningViewModel, onNavigateBack: () -> Unit) {
    val isMediatek by viewModel.isMediatek.collectAsState()
    val gpuInfo by viewModel.gpuInfo.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Dialog States
    var showRebootDialog by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var showRomInfoDialog by remember { mutableStateOf(false) }
    var showRendererDialog by remember { mutableStateOf(false) }
    
    // Logic States
    var pendingRenderer by remember { mutableStateOf("") }
    var verificationSuccess by remember { mutableStateOf(false) }
    var verificationMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var selectedRenderer by remember { mutableStateOf(gpuInfo.rendererType) }

    LaunchedEffect(gpuInfo.rendererType) { selectedRenderer = gpuInfo.rendererType }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament(modifier = Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
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
                                tint = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                            )
                        }
                        Text(
                            text = stringResource(R.string.gpu_control),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                        )
                        IconButton(onClick = { showRomInfoDialog = true }) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = "Info",
                                tint = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            // Main Content
            if (isMediatek) {
                 Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                     LiquidWarningCard(
                         title = stringResource(R.string.mediatek_device_detected),
                         message = stringResource(R.string.mediatek_gpu_unavailable),
                     )
                 }
            } else {
                 Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Info Card
                    LiquidGPUInfoCard(
                        icon = Icons.Rounded.Gamepad,
                        title = "GPU Management",
                        description = "Configure GPU frequency, power level, and rendering settings"
                    )

                    // GPU Frequency Card
                    if (gpuInfo.availableFreqs.isNotEmpty()) {
                        LiquidGPUFrequencyCard(viewModel = viewModel, gpuInfo = gpuInfo)
                    }

                    // GPU Power Level Card
                    LiquidGPUPowerLevelCard(viewModel = viewModel, gpuInfo = gpuInfo)

                    // GPU Renderer Card
                    LiquidGPURendererCard(
                        selectedRenderer = selectedRenderer,
                        onRendererClick = { showRendererDialog = true },
                    )

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
        // --- DIALOGS ---

        if (showRomInfoDialog) {
            RomInfoDialog(onDismiss = { showRomInfoDialog = false })
        }

        if (showVerificationDialog) {
            VerificationDialog(
                isProcessing = isProcessing,
                verificationSuccess = verificationSuccess,
                verificationMessage = verificationMessage,
                pendingRenderer = pendingRenderer,
                onDismiss = { if (!isProcessing) showVerificationDialog = false },
                onReboot = {
                    coroutineScope.launch { viewModel.performReboot() }
                    showVerificationDialog = false
                },
            )
        }

        if (showRebootDialog) {
            RebootConfirmationDialog(
                gpuInfo = gpuInfo,
                pendingRenderer = pendingRenderer,
                onDismiss = { showRebootDialog = false },
                onCheckCompatibility = {
                    showRebootDialog = false
                    showRomInfoDialog = true
                },
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
                },
            )
        }

        if (showRendererDialog) {
            RendererSelectionDialog(
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
                },
            )
        }
    }
}

// --- MODERN LIQUID COMPONENTS ---

@Composable
private fun LiquidGPUInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
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
                                Color(0xFFEC4899).copy(alpha = 0.3f),
                                Color(0xFF8B5CF6).copy(alpha = 0.3f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFEC4899),
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.7f)
                )
            }
        }
    }
}

@Composable
private fun LiquidWarningCard(title: String, message: String) {
    id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.7f)
                )
            }
        }
    }
}

@Composable
private fun LiquidGPUFrequencyCard(viewModel: TuningViewModel, gpuInfo: GPUInfo) {
  val isFrequencyLocked by viewModel.isGpuFrequencyLocked.collectAsState()
  val lockedMinFreq by viewModel.lockedGpuMinFreq.collectAsState()
  val lockedMaxFreq by viewModel.lockedGpuMaxFreq.collectAsState()

  var minFreqSlider by
      remember(gpuInfo.minFreq, isFrequencyLocked, lockedMinFreq) {
        mutableFloatStateOf(
            if (isFrequencyLocked && lockedMinFreq > 0) lockedMinFreq.toFloat()
            else gpuInfo.minFreq.toFloat()
        )
      }

  var maxFreqSlider by
      remember(gpuInfo.maxFreq, isFrequencyLocked, lockedMaxFreq) {
        mutableFloatStateOf(
            if (isFrequencyLocked && lockedMaxFreq > 0) lockedMaxFreq.toFloat()
            else gpuInfo.maxFreq.toFloat()
        )
      }

  val backdrop = com.kyant.backdrop.backdrops.rememberLayerBackdrop()

  id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(20.dp)
  ) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header with lock badge
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
                  .background(Color(0xFFEC4899).copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
          ) {
            Icon(
                imageVector = Icons.Outlined.Speed,
                contentDescription = null,
                tint = Color(0xFFEC4899),
                modifier = Modifier.size(20.dp)
            )
          }
          Text(
              text = stringResource(R.string.gpu_frequency),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
          )
        }

        AnimatedVisibility(visible = isFrequencyLocked) {
           Surface(
               shape = RoundedCornerShape(8.dp),
               color = Color(0xFFEF4444).copy(alpha = 0.15f)
           ) {
              Text(
                  "Locked",
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFEF4444)
              )
           }
        }
      }

      // Min Frequency Slider
      LiquidGPUSliderCard(
          label = stringResource(R.string.gpu_min_freq),
          value = minFreqSlider,
          valueRange = gpuInfo.availableFreqs.minOrNull()!!.toFloat()..gpuInfo.availableFreqs.maxOrNull()!!.toFloat(),
          valueDisplay = "${minFreqSlider.toInt()} MHz",
          onValueChange = { minFreqSlider = it },
          icon = Icons.Rounded.South,
          color = Color(0xFF10B981)
      )

      // Max Frequency Slider
      LiquidGPUSliderCard(
          label = stringResource(R.string.gpu_max_freq),
          value = maxFreqSlider,
          valueRange = gpuInfo.availableFreqs.minOrNull()!!.toFloat()..gpuInfo.availableFreqs.maxOrNull()!!.toFloat(),
          valueDisplay = "${maxFreqSlider.toInt()} MHz",
          onValueChange = { maxFreqSlider = it },
          icon = Icons.Rounded.North,
          color = Color(0xFF3B82F6)
      )

      HorizontalDivider(color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveSurfaceColor(0.2f))

      // Action Buttons
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Surface(
            onClick = {
              viewModel.setGPUFrequency(minFreqSlider.toInt(), maxFreqSlider.toInt())
              if (isFrequencyLocked) viewModel.unlockGPUFrequency()
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF10B981).copy(alpha = 0.15f)
        ) {
          Row(
              modifier = Modifier.padding(16.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.apply),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
          }
        }

        Surface(
            onClick = {
              if (isFrequencyLocked) viewModel.unlockGPUFrequency()
              else viewModel.lockGPUFrequency(minFreqSlider.toInt(), maxFreqSlider.toInt())
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = if (isFrequencyLocked) Color(0xFFEF4444).copy(alpha = 0.15f)
                   else Color(0xFF3B82F6).copy(alpha = 0.15f)
        ) {
          Row(
              modifier = Modifier.padding(16.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
                imageVector = if (isFrequencyLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isFrequencyLocked) Color(0xFFEF4444) else Color(0xFF3B82F6),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isFrequencyLocked) stringResource(R.string.gpu_unlock) else stringResource(R.string.gpu_lock),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isFrequencyLocked) Color(0xFFEF4444) else Color(0xFF3B82F6)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun LiquidGPUSliderCard(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueDisplay: String,
    onValueChange: (Float) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    val backdrop = com.kyant.backdrop.backdrops.rememberLayerBackdrop()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveSurfaceColor(0.05f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
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
                    color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                )
            }
            Surface(
                color = color.copy(alpha = 0.15f),
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

        // Liquid Slider
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
private fun LiquidGPUPowerLevelCard(viewModel: TuningViewModel, gpuInfo: GPUInfo) {
  var powerLevel by remember(gpuInfo.powerLevel) { mutableFloatStateOf(gpuInfo.powerLevel.toFloat()) }
  var lastAppliedLevel by remember { mutableStateOf(gpuInfo.powerLevel) }
  val backdrop = com.kyant.backdrop.backdrops.rememberLayerBackdrop()

  // Apply changes when user stops dragging
  LaunchedEffect(powerLevel) {
    if (powerLevel.toInt() != lastAppliedLevel) {
      kotlinx.coroutines.delay(500) // Debounce
      if (powerLevel.toInt() != lastAppliedLevel) {
        viewModel.setGPUPowerLevel(powerLevel.toInt())
        lastAppliedLevel = powerLevel.toInt()
      }
    }
  }

  id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(20.dp)
  ) {
    Column(
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
                .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
          Icon(
              imageVector = Icons.Rounded.BatteryFull,
              contentDescription = null,
              tint = Color(0xFFF59E0B),
              modifier = Modifier.size(20.dp)
          )
        }
        Text(
            text = stringResource(R.string.gpu_power_level_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
        )
      }

      // Power Level Slider
      Column(
          modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveSurfaceColor(0.05f))
              .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
              text = "Power Level",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
          )
          Surface(
              color = Color(0xFFF59E0B).copy(alpha = 0.15f),
              shape = RoundedCornerShape(8.dp)
          ) {
            Text(
                text = "Level ${powerLevel.toInt()}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF59E0B),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
          }
        }

        id.xms.xtrakernelmanager.ui.components.liquid.LiquidSlider(
            value = { powerLevel },
            onValueChange = { powerLevel = it },
            valueRange = 0f..(gpuInfo.numPwrLevels - 1).coerceAtLeast(1).toFloat(),
            visibilityThreshold = 1f,
            backdrop = backdrop,
            modifier = Modifier.padding(vertical = 8.dp)
        )
      }
    }
  }
}

@Composable
private fun LiquidGPURendererCard(
    selectedRenderer: String,
    onRendererClick: () -> Unit
) {
  id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(20.dp)
  ) {
    Column(
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
                .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
          Icon(
              imageVector = Icons.Outlined.Palette,
              contentDescription = null,
              tint = Color(0xFF8B5CF6),
              modifier = Modifier.size(20.dp)
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = stringResource(R.string.gpu_renderer),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
          )
          Text(
              text = selectedRenderer,
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFF8B5CF6),
              fontWeight = FontWeight.SemiBold
          )
        }
      }

      HorizontalDivider(color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveSurfaceColor(0.2f))

      // Info Banner
      Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF3B82F6).copy(alpha = 0.1f)
      ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
          Icon(
              imageVector = Icons.Outlined.Info,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
              tint = Color(0xFF3B82F6)
          )
          Text(
              text = stringResource(R.string.gpu_renderer_rom_info),
              style = MaterialTheme.typography.bodySmall,
              color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.8f)
          )
        }
      }

      // Renderer Selection Button
      Surface(
          onClick = onRendererClick,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFF8B5CF6).copy(alpha = 0.15f)
      ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
          ) {
            Icon(
                imageVector = Icons.Outlined.Palette,
                contentDescription = null,
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.gpu_renderer_tap_to_change),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5CF6)
            )
          }
          Icon(
              imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
              contentDescription = null,
              tint = Color(0xFF8B5CF6),
              modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

// --- DIALOGS (Preserved from original) ---


@Composable
private fun RomInfoDialog(onDismiss: () -> Unit) {
  LiquidDialog(
      onDismissRequest = onDismiss,
      title = stringResource(R.string.gpu_renderer_compatibility_title),
      content = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          Text(
              text = stringResource(R.string.gpu_renderer_compatibility_intro),
              style = MaterialTheme.typography.bodyMedium,
              color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.8f)
          )

          // Supported ROMs Card
          id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
              modifier = Modifier.fillMaxWidth(),
              contentPadding = PaddingValues(16.dp),
              shape = RoundedCornerShape(16.dp)
          ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.gpu_fully_supported),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
              }
              Text(
                  text = stringResource(R.string.gpu_supported_roms),
                  style = MaterialTheme.typography.bodySmall,
                  color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.7f)
              )
            }
          }

          // Limited Support ROMs Card
          id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
              modifier = Modifier.fillMaxWidth(),
              contentPadding = PaddingValues(16.dp),
              shape = RoundedCornerShape(16.dp)
          ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.gpu_limited_support),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF59E0B)
                )
              }
              Text(
                  text = stringResource(R.string.gpu_unsupported_roms),
                  style = MaterialTheme.typography.bodySmall,
                  color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.7f)
              )
            }
          }

          HorizontalDivider(color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveSurfaceColor(0.2f))

          // Why It Doesn't Work Section
          Text(
              text = stringResource(R.string.gpu_why_not_work),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
          )

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                    stringResource(R.string.gpu_reason_1),
                    stringResource(R.string.gpu_reason_2),
                    stringResource(R.string.gpu_reason_3),
                    stringResource(R.string.gpu_reason_4),
                    stringResource(R.string.gpu_reason_5),
                )
                .forEach { reason ->
                  Row(
                      horizontalArrangement = Arrangement.spacedBy(10.dp),
                      verticalAlignment = Alignment.Top,
                  ) {
                    Box(
                        modifier =
                            Modifier.size(6.dp)
                                .offset(y = 8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6))
                    )
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.7f),
                        modifier = Modifier.weight(1f),
                    )
                  }
                }
          }

          // Tip Card
          Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFF3B82F6).copy(alpha = 0.1f),
          ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                  imageVector = Icons.Outlined.Lightbulb,
                  contentDescription = null,
                  tint = Color(0xFF3B82F6),
                  modifier = Modifier.size(20.dp),
              )
              Text(
                  text = stringResource(R.string.gpu_verify_tip),
                  style = MaterialTheme.typography.bodySmall,
                  color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.8f)
              )
            }
          }
        }
      },
      confirmButton = {
        LiquidDialogButton(
            text = stringResource(R.string.gpu_got_it),
            onClick = onDismiss,
            isPrimary = true
        )
      }
  )
}

@Composable
private fun VerificationDialog(
    isProcessing: Boolean,
    verificationSuccess: Boolean,
    verificationMessage: String,
    pendingRenderer: String,
    onDismiss: () -> Unit,
    onReboot: () -> Unit,
) {
  LiquidDialog(
      onDismissRequest = { if (!isProcessing) onDismiss() },
      title = when {
          isProcessing -> stringResource(R.string.gpu_applying_changes)
          verificationSuccess -> stringResource(R.string.gpu_changes_applied)
          else -> stringResource(R.string.gpu_warning)
      },
      content = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          // Icon/Loading indicator
          Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
            if (isProcessing) {
              CircularProgressIndicator(
                  modifier = Modifier.size(56.dp),
                  color = Color(0xFF3B82F6),
                  strokeWidth = 4.dp,
              )
            } else {
              Box(
                  modifier = Modifier
                      .size(64.dp)
                      .clip(CircleShape)
                      .background(
                          if (verificationSuccess) Color(0xFF10B981).copy(alpha = 0.2f)
                          else Color(0xFFEF4444).copy(alpha = 0.2f)
                      ),
                  contentAlignment = Alignment.Center,
              ) {
                Icon(
                    imageVector = if (verificationSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = if (verificationSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
                )
              }
            }
          }
          
          when {
            isProcessing -> {
              Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Text(
                    text = stringResource(R.string.gpu_writing_system_files),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF3B82F6).copy(alpha = 0.15f)
                ) {
                  Text(
                      text = stringResource(R.string.gpu_renderer_label, pendingRenderer),
                      style = MaterialTheme.typography.bodySmall,
                      color = Color(0xFF3B82F6),
                      fontWeight = FontWeight.SemiBold,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  )
                }
              }
            }
            verificationSuccess -> {
              id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
                  modifier = Modifier.fillMaxWidth(),
                  contentPadding = PaddingValues(16.dp),
                  shape = RoundedCornerShape(16.dp)
              ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                  Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(10.dp),
                  ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = stringResource(R.string.gpu_runtime_property_updated),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                    )
                  }
                  Text(
                      text = stringResource(R.string.gpu_renderer_label, pendingRenderer),
                      style = MaterialTheme.typography.bodySmall,
                      color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.7f)
                  )
                }
              }

              id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
                  modifier = Modifier.fillMaxWidth(),
                  contentPadding = PaddingValues(16.dp),
                  shape = RoundedCornerShape(16.dp)
              ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(10.dp),
                  ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = stringResource(R.string.gpu_reboot_required),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                    )
                  }
                  Text(
                      text = stringResource(R.string.gpu_reboot_required_desc),
                      style = MaterialTheme.typography.bodySmall,
                      color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.7f)
                  )
                }
              }
            }
            else -> {
              id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
                  modifier = Modifier.fillMaxWidth(),
                  contentPadding = PaddingValues(16.dp),
                  shape = RoundedCornerShape(16.dp)
              ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Text(
                      text = stringResource(R.string.gpu_failed_apply),
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFFEF4444)
                  )
                  if (verificationMessage.isNotBlank()) {
                    Text(
                        text = verificationMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.7f)
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
            LiquidDialogButton(
                text = stringResource(R.string.gpu_reboot_now),
                onClick = onReboot,
                isPrimary = true
            )
          } else {
            LiquidDialogButton(
                text = stringResource(R.string.gpu_close),
                onClick = onDismiss,
                isPrimary = true
            )
          }
        }
      },
      dismissButton = if (!isProcessing && verificationSuccess) {
        {
          LiquidDialogButton(
              text = stringResource(R.string.gpu_reboot_later),
              onClick = onDismiss,
              isPrimary = false
          )
        }
      } else null
  )
}

@Composable
private fun RebootConfirmationDialog(gpuInfo: GPUInfo, pendingRenderer: String, onDismiss: () -> Unit, onCheckCompatibility: () -> Unit, onConfirm: () -> Unit) {
    LiquidDialog(
      onDismissRequest = onDismiss,
      title = stringResource(R.string.gpu_change_renderer_title),
      content = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          Text(
              text = stringResource(R.string.gpu_change_renderer_intro),
              style = MaterialTheme.typography.bodyMedium,
              color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.8f),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
          )

          // Change Preview Card
          id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
              modifier = Modifier.fillMaxWidth(),
              contentPadding = PaddingValues(16.dp),
              shape = RoundedCornerShape(16.dp)
          ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              // Current Renderer
              Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                    text = stringResource(R.string.gpu_current),
                    style = MaterialTheme.typography.labelSmall,
                    color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.6f)
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveSurfaceColor(0.1f)
                ) {
                  Text(
                      text = gpuInfo.rendererType,
                      style = MaterialTheme.typography.bodyLarge,
                      fontWeight = FontWeight.Medium,
                      color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(),
                      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                  )
                }
              }

              // Arrow Down
              Icon(
                  imageVector = Icons.Filled.ArrowDownward,
                  contentDescription = null,
                  modifier = Modifier.size(28.dp),
                  tint = Color(0xFF3B82F6)
              )

              // New Renderer
              Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                    text = stringResource(R.string.gpu_new),
                    style = MaterialTheme.typography.labelSmall,
                    color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.6f)
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF3B82F6).copy(alpha = 0.15f)
                ) {
                  Text(
                      text = pendingRenderer,
                      style = MaterialTheme.typography.bodyLarge,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF3B82F6),
                      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                  )
                }
              }
            }
          }

          HorizontalDivider(color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveSurfaceColor(0.2f))

          // Important Warning
          Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFFF59E0B).copy(alpha = 0.1f)
          ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.gpu_important),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                )
              }
              Text(
                  text = stringResource(R.string.gpu_change_warnings),
                  style = MaterialTheme.typography.bodySmall,
                  color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.8f)
              )
            }
          }

          // Check Compatibility Button
          Surface(
              onClick = onCheckCompatibility,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFF3B82F6).copy(alpha = 0.1f),
              border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f))
          ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                  imageVector = Icons.Outlined.Info,
                  contentDescription = null,
                  tint = Color(0xFF3B82F6),
                  modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                  text = stringResource(R.string.gpu_check_rom_compatibility),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF3B82F6)
              )
            }
          }
        }
      },
      confirmButton = {
        LiquidDialogButton(
            text = stringResource(R.string.gpu_apply_changes),
            onClick = onConfirm,
            isPrimary = true
        )
      },
      dismissButton = {
        LiquidDialogButton(
            text = stringResource(R.string.gpu_cancel),
            onClick = onDismiss,
            isPrimary = false
        )
      }
  )
}

@Composable
private fun RendererSelectionDialog(selectedRenderer: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
  LiquidDialog(
      onDismissRequest = onDismiss,
      title = stringResource(R.string.gpu_renderer_select_title),
      content = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text(
              text = stringResource(R.string.gpu_renderer_select_desc),
              style = MaterialTheme.typography.bodySmall,
              color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(0.7f),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
          )
          
          Spacer(modifier = Modifier.height(4.dp))

          val renderers =
              listOf(
                  stringResource(R.string.gpu_renderer_opengl) to
                      stringResource(R.string.gpu_renderer_opengl_desc),
                  stringResource(R.string.gpu_renderer_vulkan) to
                      stringResource(R.string.gpu_renderer_vulkan_desc),
                  stringResource(R.string.gpu_renderer_angle) to
                      stringResource(R.string.gpu_renderer_angle_desc),
                  stringResource(R.string.gpu_renderer_skiagl) to
                      stringResource(R.string.gpu_renderer_skiagl_desc),
                  stringResource(R.string.gpu_renderer_skiavulkan) to
                      stringResource(R.string.gpu_renderer_skiavulkan_desc),
              )

          renderers.forEach { (renderer, description) ->
            val isSelected = renderer == selectedRenderer

            Surface(
                onClick = { onSelect(renderer) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.15f)
                       else id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveSurfaceColor(0.05f),
                border = if (isSelected) BorderStroke(2.dp, Color(0xFF8B5CF6))
                        else BorderStroke(1.dp, id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveSurfaceColor(0.2f))
            ) {
              Row(
                  modifier = Modifier.fillMaxWidth().padding(16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Text(
                      text = renderer,
                      style = MaterialTheme.typography.bodyLarge,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) Color(0xFF8B5CF6)
                             else id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                  )
                  Text(
                      text = description,
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.Medium,
                      color = Color(0xFFF59E0B) // Kuning untuk deskripsi
                  )
                }

                if (isSelected) {
                  Box(
                      modifier =
                          Modifier.size(28.dp)
                              .clip(CircleShape)
                              .background(Color(0xFF8B5CF6)),
                      contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        LiquidDialogButton(
            text = stringResource(R.string.cancel),
            onClick = onDismiss,
            isPrimary = false
        )
      }
  )
}
