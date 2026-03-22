package id.xms.xtrakernelmanager.ui.screens.tuning.frosted.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.frosted.FrostedDialog
import id.xms.xtrakernelmanager.ui.components.frosted.FrostedDialogButton
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrostedGPUSettingsScreen(viewModel: TuningViewModel, onNavigateBack: () -> Unit) {
    val isMediatek by viewModel.isMediatek.collectAsState()
    val gpuInfo by viewModel.gpuInfo.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showRebootDialog by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var showRomInfoDialog by remember { mutableStateOf(false) }
    var showRendererDialog by remember { mutableStateOf(false) }
    
    var pendingRenderer by remember { mutableStateOf("") }
    var verificationSuccess by remember { mutableStateOf(false) }
    var verificationMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var selectedRenderer by remember { mutableStateOf(gpuInfo.rendererType) }

    LaunchedEffect(gpuInfo.rendererType) { selectedRenderer = gpuInfo.rendererType }

    Box(modifier = Modifier.fillMaxSize()) {
        id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament(modifier = Modifier.fillMaxSize())

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
                                contentDescription = "Back"
                            )
                        }
                        Text(
                            text = stringResource(R.string.gpu_control),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showRomInfoDialog = true }) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = "Info"
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            if (isMediatek) {
                 Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                     FrostedWarningCard(
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
                    FrostedGPUInfoCard(
                        icon = Icons.Rounded.Gamepad,
                        title = "GPU Management",
                        description = "Configure GPU frequency, power level, and rendering settings"
                    )

                    if (gpuInfo.availableFreqs.isNotEmpty()) {
                        FrostedGPUFrequencyCard(viewModel = viewModel, gpuInfo = gpuInfo)
                    }

                    FrostedGPUPowerLevelCard(viewModel = viewModel, gpuInfo = gpuInfo)

                    FrostedGPURendererCard(
                        selectedRenderer = selectedRenderer,
                        onRendererClick = { showRendererDialog = true },
                    )

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
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

@Composable
private fun FrostedGPUInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FrostedWarningCard(title: String, message: String) {
    id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FrostedGPUFrequencyCard(viewModel: TuningViewModel, gpuInfo: GPUInfo) {
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
      shape = RoundedCornerShape(24.dp),
      contentPadding = PaddingValues(24.dp)
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
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
              imageVector = Icons.Outlined.Speed,
              contentDescription = null,
              modifier = Modifier.size(28.dp)
          )
          Column {
            Text(
                text = stringResource(R.string.gpu_frequency),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Control GPU clock speed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        AnimatedVisibility(visible = isFrequencyLocked) {
          Text(
              "Locked",
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.error
          )
        }
      }

      FrostedGPUSliderCard(
          label = stringResource(R.string.gpu_min_freq),
          value = minFreqSlider,
          valueRange = gpuInfo.availableFreqs.minOrNull()!!.toFloat()..gpuInfo.availableFreqs.maxOrNull()!!.toFloat(),
          valueDisplay = "${minFreqSlider.toInt()} MHz",
          onValueChange = { minFreqSlider = it },
          icon = Icons.Rounded.South,
          color = Color(0xFF10B981)
      )

      FrostedGPUSliderCard(
          label = stringResource(R.string.gpu_max_freq),
          value = maxFreqSlider,
          valueRange = gpuInfo.availableFreqs.minOrNull()!!.toFloat()..gpuInfo.availableFreqs.maxOrNull()!!.toFloat(),
          valueDisplay = "${maxFreqSlider.toInt()} MHz",
          onValueChange = { maxFreqSlider = it },
          icon = Icons.Rounded.North,
          color = Color(0xFF3B82F6)
      )

      HorizontalDivider()

      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
            onClick = {
              viewModel.setGPUFrequency(minFreqSlider.toInt(), maxFreqSlider.toInt())
              if (isFrequencyLocked) viewModel.unlockGPUFrequency()
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.apply),
                fontWeight = FontWeight.Bold
            )
          }
        }

        id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
            onClick = {
              if (isFrequencyLocked) viewModel.unlockGPUFrequency()
              else viewModel.lockGPUFrequency(minFreqSlider.toInt(), maxFreqSlider.toInt())
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
                imageVector = if (isFrequencyLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isFrequencyLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isFrequencyLocked) stringResource(R.string.gpu_unlock) else stringResource(R.string.gpu_lock),
                fontWeight = FontWeight.Bold,
                color = if (isFrequencyLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }
  }
}

@Composable
private fun FrostedGPUSliderCard(
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
        modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = valueDisplay,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
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

@Composable
private fun FrostedGPUPowerLevelCard(viewModel: TuningViewModel, gpuInfo: GPUInfo) {
  id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(24.dp),
      contentPadding = PaddingValues(24.dp)
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
            imageVector = Icons.Rounded.BatteryChargingFull,
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Column {
          Text(
              text = stringResource(R.string.gpu_power_level_title),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
          )
          Text(
              text = "Select performance level",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      HorizontalDivider()

      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
              text = "Current Level",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold
          )
          Text(
              text = stringResource(R.string.frosted_gpu_power_level),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Text(
            text = "${gpuInfo.powerLevel}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
      }

      val maxLevel = (gpuInfo.numPwrLevels - 1).coerceAtLeast(0)
      
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Available Levels (0-$maxLevel)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        val levels = (0..maxLevel).toList()

        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          levels.forEach { level ->
            val isSelected = level == gpuInfo.powerLevel
            id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
                onClick = { viewModel.setGPUPowerLevel(level) },
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
              Box(
                  modifier = Modifier.fillMaxSize(),
                  contentAlignment = Alignment.Center
              ) {
                Text(
                    text = level.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
              imageVector = Icons.Outlined.Info,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
          )
          Text(
              text = "Level 0 = Highest performance • Level $maxLevel = Lowest performance",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
private fun FrostedGPURendererCard(
    selectedRenderer: String,
    onRendererClick: () -> Unit
) {
  id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(24.dp),
      contentPadding = PaddingValues(24.dp)
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
            imageVector = Icons.Outlined.Palette,
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = stringResource(R.string.gpu_renderer),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
          )
          Text(
              text = selectedRenderer,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      HorizontalDivider()

      Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.Top
      ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = stringResource(R.string.gpu_renderer_rom_info),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
          onClick = onRendererClick,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          contentPadding = PaddingValues(16.dp)
      ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.gpu_renderer_tap_to_change),
                fontWeight = FontWeight.Bold
            )
          }
          Icon(
              imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
              contentDescription = null,
              modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun RomInfoDialog(onDismiss: () -> Unit) {
  FrostedDialog(
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
              color = MaterialTheme.colorScheme.onSurfaceVariant
          )

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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.gpu_fully_supported),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
              }
              Text(
                  text = stringResource(R.string.gpu_supported_roms),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
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
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.gpu_limited_support),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
              }
              Text(
                  text = stringResource(R.string.gpu_unsupported_roms),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          HorizontalDivider()

          Text(
              text = stringResource(R.string.gpu_why_not_work),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
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
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                      verticalAlignment = Alignment.Top,
                  ) {
                    Text(text = "•", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                  }
                }
          }

          Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.gpu_verify_tip),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      },
      confirmButton = {
        FrostedDialogButton(
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
  FrostedDialog(
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
          Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
            if (isProcessing) {
              CircularProgressIndicator(
                  modifier = Modifier.size(56.dp),
                  strokeWidth = 4.dp,
              )
            } else {
              Icon(
                  imageVector = if (verificationSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                  contentDescription = null,
                  modifier = Modifier.size(48.dp),
                  tint = if (verificationSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
              )
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.gpu_renderer_label, pendingRenderer),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = stringResource(R.string.gpu_runtime_property_updated),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                  }
                  Text(
                      text = stringResource(R.string.gpu_renderer_label, pendingRenderer),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = stringResource(R.string.gpu_reboot_required),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                  }
                  Text(
                      text = stringResource(R.string.gpu_reboot_required_desc),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
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
                      color = MaterialTheme.colorScheme.error
                  )
                  if (verificationMessage.isNotBlank()) {
                    Text(
                        text = verificationMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            FrostedDialogButton(
                text = stringResource(R.string.gpu_reboot_now),
                onClick = onReboot,
                isPrimary = true
            )
          } else {
            FrostedDialogButton(
                text = stringResource(R.string.gpu_close),
                onClick = onDismiss,
                isPrimary = true
            )
          }
        }
      },
      dismissButton = if (!isProcessing && verificationSuccess) {
        {
          FrostedDialogButton(
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
    FrostedDialog(
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
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
          )

          id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
              modifier = Modifier.fillMaxWidth(),
              contentPadding = PaddingValues(16.dp),
              shape = RoundedCornerShape(16.dp)
          ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                    text = stringResource(R.string.gpu_current),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = gpuInfo.rendererType,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
              }

              Icon(
                  imageVector = Icons.Filled.ArrowDownward,
                  contentDescription = null,
                  modifier = Modifier.size(28.dp)
              )

              Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                    text = stringResource(R.string.gpu_new),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = pendingRenderer,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }

          HorizontalDivider()

          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Column {
              Text(
                  text = stringResource(R.string.gpu_important),
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
              )
              Text(
                  text = stringResource(R.string.gpu_change_warnings),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Surface(
              onClick = onCheckCompatibility,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp)
          ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                  imageVector = Icons.Outlined.Info,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                  text = stringResource(R.string.gpu_check_rom_compatibility),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      },
      confirmButton = {
        FrostedDialogButton(
            text = stringResource(R.string.gpu_apply_changes),
            onClick = onConfirm,
            isPrimary = true
        )
      },
      dismissButton = {
        FrostedDialogButton(
            text = stringResource(R.string.gpu_cancel),
            onClick = onDismiss,
            isPrimary = false
        )
      }
  )
}

@Composable
private fun RendererSelectionDialog(selectedRenderer: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
  FrostedDialog(
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
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
          )

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

            id.xms.xtrakernelmanager.ui.components.GlassmorphicCard(
                onClick = { onSelect(renderer) },
                modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
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
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  )
                  Text(
                      text = description,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                if (isSelected) {
                  Icon(
                      imageVector = Icons.Filled.Check,
                      contentDescription = null,
                      modifier = Modifier.size(24.dp)
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {
        FrostedDialogButton(
            text = stringResource(R.string.cancel),
            onClick = onDismiss,
            isPrimary = false
        )
      },
      dismissButton = null
  )
}
