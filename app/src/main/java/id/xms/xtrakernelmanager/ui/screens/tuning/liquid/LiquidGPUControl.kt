package id.xms.xtrakernelmanager.ui.screens.tuning.liquid

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.ChangeCircle
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.GPUInfo
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidGPUControl(viewModel: TuningViewModel) {
  val isMediatek by viewModel.isMediatek.collectAsState()
  val gpuInfo by viewModel.gpuInfo.collectAsState()
  val coroutineScope = rememberCoroutineScope()
  var expanded by remember { mutableStateOf(false) }
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

  // Mediatek warning card
  if (isMediatek) {
    ModernWarningCard(
        title = stringResource(R.string.mediatek_device_detected),
        message = stringResource(R.string.mediatek_gpu_unavailable),
    )
    return
  }

  // Dialogs
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

  // Main Content
  GlassmorphicCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Modern Header
      ModernHeader(
          expanded = expanded,
          onExpandClick = { expanded = !expanded },
          onInfoClick = { showRomInfoDialog = true },
      )

      // Collapsible Content
      AnimatedVisibility(
          visible = expanded,
          enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
          exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300)),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          // GPU Frequency Card
          if (gpuInfo.availableFreqs.isNotEmpty()) {
            GPUFrequencyCard(viewModel = viewModel, gpuInfo = gpuInfo)
          }

          // GPU Power Level Card
          GPUPowerLevelCard(viewModel = viewModel, gpuInfo = gpuInfo)

          // GPU Renderer Card
          GPURendererCard(
              selectedRenderer = selectedRenderer,
              onRendererClick = { showRendererDialog = true },
          )
        }
      }
    }
  }
}

// ============ MODERN COMPONENTS ============

@Composable
private fun ModernHeader(expanded: Boolean, onExpandClick: () -> Unit, onInfoClick: () -> Unit) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.weight(1f),
    ) {
      // Modern Icon Container
      Box(
          modifier =
              Modifier.size(56.dp)
                  .shadow(4.dp, RoundedCornerShape(16.dp))
                  .clip(RoundedCornerShape(16.dp))
                  .background(
                      Brush.linearGradient(
                          colors =
                              listOf(
                                  MaterialTheme.colorScheme.tertiary,
                                  MaterialTheme.colorScheme.tertiaryContainer,
                              )
                      )
                  ),
          contentAlignment = Alignment.Center,
      ) {
        Icon(
            imageVector = Icons.Filled.Memory,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.size(32.dp),
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
              text = stringResource(R.string.gpu_control),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
          )

          // Modern Info Badge
          FilledIconButton(
              onClick = onInfoClick,
              modifier = Modifier.size(24.dp),
              colors =
                  IconButtonDefaults.filledIconButtonColors(
                      containerColor = MaterialTheme.colorScheme.tertiaryContainer
                  ),
          ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
          }
        }

        Text(
            text = stringResource(R.string.gpu_control_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    // Animated Expand Button
    val rotationState by
        animateFloatAsState(targetValue = if (expanded) 180f else 0f, animationSpec = tween(300))

    FilledIconButton(
        onClick = onExpandClick,
        modifier = Modifier.size(56.dp),
        colors =
            IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            ),
    ) {
      Icon(
          imageVector = Icons.Filled.ExpandMore,
          contentDescription = if (expanded) "Collapse" else "Expand",
          modifier = Modifier.size(32.dp).graphicsLayer { rotationZ = rotationState },
          tint = MaterialTheme.colorScheme.tertiary,
      )
    }
  }
}

@Composable
private fun GPUFrequencyCard(viewModel: TuningViewModel, gpuInfo: GPUInfo) {
  val isFrequencyLocked by viewModel.isGpuFrequencyLocked.collectAsState()
  val lockedMinFreq by viewModel.lockedGpuMinFreq.collectAsState()
  val lockedMaxFreq by viewModel.lockedGpuMaxFreq.collectAsState()

  // Use rememberSaveable to persist slider state across recompositions and tab changes
  var minFreqSlider by rememberSaveable { 
    mutableFloatStateOf(
      if (isFrequencyLocked && lockedMinFreq > 0) lockedMinFreq.toFloat()
      else gpuInfo.minFreq.toFloat()
    )
  }

  var maxFreqSlider by rememberSaveable { 
    mutableFloatStateOf(
      if (isFrequencyLocked && lockedMaxFreq > 0) lockedMaxFreq.toFloat()
      else gpuInfo.maxFreq.toFloat()
    )
  }

  var isUserInteracting by remember { mutableStateOf(false) }

  // Update slider values when GPU info changes but user is not interacting
  LaunchedEffect(gpuInfo.minFreq, isFrequencyLocked, lockedMinFreq) {
    if (!isUserInteracting) {
      val newMinFreq = if (isFrequencyLocked && lockedMinFreq > 0) lockedMinFreq.toFloat()
                      else gpuInfo.minFreq.toFloat()
      if (kotlin.math.abs(minFreqSlider - newMinFreq) > 1f) {
        minFreqSlider = newMinFreq
      }
    }
  }

  LaunchedEffect(gpuInfo.maxFreq, isFrequencyLocked, lockedMaxFreq) {
    if (!isUserInteracting) {
      val newMaxFreq = if (isFrequencyLocked && lockedMaxFreq > 0) lockedMaxFreq.toFloat()
                      else gpuInfo.maxFreq.toFloat()
      if (kotlin.math.abs(maxFreqSlider - newMaxFreq) > 1f) {
        maxFreqSlider = newMaxFreq
      }
    }
  }

  ElevatedCard(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
      colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
      ),
  ) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Card Header with Lock Status
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Box(
              modifier = Modifier
                  .size(40.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
          ) {
            Icon(
                imageVector = Icons.Outlined.Speed,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(22.dp),
            )
          }
          Text(
              text = stringResource(R.string.gpu_frequency),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }

        // Lock Status Badge
        AnimatedVisibility(
            visible = isFrequencyLocked,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
        ) {
          Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.primaryContainer,
          ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                  imageVector = Icons.Filled.Lock,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
              )
              Text(
                  text = stringResource(R.string.gpu_locked),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

      // Min Frequency Slider
      FrequencySliderItem(
          label = stringResource(R.string.gpu_min_freq),
          value = minFreqSlider.toInt(),
          onValueChange = { 
            isUserInteracting = true
            minFreqSlider = it 
          },
          onValueChangeFinished = { isUserInteracting = false },
          availableFreqs = gpuInfo.availableFreqs,
          color = MaterialTheme.colorScheme.tertiary,
      )

      // Max Frequency Slider
      FrequencySliderItem(
          label = stringResource(R.string.gpu_max_freq),
          value = maxFreqSlider.toInt(),
          onValueChange = { 
            isUserInteracting = true
            maxFreqSlider = it 
          },
          onValueChangeFinished = { isUserInteracting = false },
          availableFreqs = gpuInfo.availableFreqs,
          color = MaterialTheme.colorScheme.secondary,
      )

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

      // Action Buttons
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FilledTonalButton(
            onClick = {
              viewModel.setGPUFrequency(minFreqSlider.toInt(), maxFreqSlider.toInt())
              if (isFrequencyLocked) {
                viewModel.unlockGPUFrequency()
              }
            },
            modifier = Modifier.weight(1f),
        ) {
          Icon(
              imageVector = Icons.Filled.Check,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(stringResource(R.string.apply))
        }

        Button(
            onClick = {
              if (isFrequencyLocked) {
                viewModel.unlockGPUFrequency()
              } else {
                viewModel.lockGPUFrequency(minFreqSlider.toInt(), maxFreqSlider.toInt())
              }
            },
            modifier = Modifier.weight(1f),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        if (isFrequencyLocked) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                ),
        ) {
          Icon(
              imageVector = if (isFrequencyLocked) Icons.Filled.LockOpen else Icons.Filled.Lock,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
              if (isFrequencyLocked) stringResource(R.string.gpu_unlock)
              else stringResource(R.string.gpu_lock)
          )
        }
      }

      // Info Text
      Text(
          text =
              if (isFrequencyLocked) stringResource(R.string.gpu_locked_warning)
              else stringResource(R.string.gpu_lock_info),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun FrequencySliderItem(
    label: String,
    value: Int,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    availableFreqs: List<Int>,
    color: Color,
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          text = label,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
      )
      Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f)) {
        Text(
            text = "$value MHz",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
      }
    }

    Slider(
        value = value.toFloat(),
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = availableFreqs.minOrNull()!!.toFloat()..availableFreqs.maxOrNull()!!.toFloat(),
        colors =
            SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.2f),
            ),
    )
  }
}

@Composable
private fun GPUPowerLevelCard(viewModel: TuningViewModel, gpuInfo: GPUInfo) {
  ElevatedCard(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
      colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
      ),
  ) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      // Header with icon and title
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
        ) {
          Icon(
              imageVector = Icons.Outlined.BatteryChargingFull,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondary,
              modifier = Modifier.size(22.dp),
          )
        }
        Column {
          Text(
              text = stringResource(R.string.gpu_power_level_title),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
          )
          Text(
              text = "Select performance level",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))

      // Current level display with better styling
      Row(
          modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
              .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
              text = "Current Level",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
          )
          Text(
              text = stringResource(R.string.gpu_power_level),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
          )
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.secondary,
        ) {
          Text(
              text = "${gpuInfo.powerLevel}",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondary,
              modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
          )
        }
      }

      // Power level selection grid with improved design
      val maxLevel = (gpuInfo.numPwrLevels - 1).coerceAtLeast(0)
      val levels = (0..maxLevel).toList()
      
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Available Levels (0-$maxLevel)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        
        // Use FlowRow for better wrapping behavior
        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          levels.forEach { level ->
            val isSelected = level == gpuInfo.powerLevel
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .clickable { viewModel.setGPUPowerLevel(level) }
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
              Text(
                  text = level.toString(),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) MaterialTheme.colorScheme.onSecondary
                          else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }

      // Info text
      Row(
          modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
              .padding(12.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "Level 0 = Highest performance, Level $maxLevel = Lowest performance",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun GPURendererCard(selectedRenderer: String, onRendererClick: () -> Unit) {
  ElevatedCard(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
      colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
      ),
  ) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.tertiary),
            contentAlignment = Alignment.Center
        ) {
          Icon(
              imageVector = Icons.Outlined.Settings,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onTertiary,
              modifier = Modifier.size(22.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = stringResource(R.string.gpu_renderer),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
          )
          Text(
              text = selectedRenderer,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.tertiary,
              fontWeight = FontWeight.SemiBold,
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

      // Info Banner
      Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
      ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
          Icon(
              imageVector = Icons.Outlined.Info,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
              tint = MaterialTheme.colorScheme.onTertiaryContainer,
          )
          Text(
              text = stringResource(R.string.gpu_renderer_rom_info),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
          )
        }
      }

      // Renderer Selection Button
      FilledTonalButton(
          onClick = onRendererClick,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
      ) {
        Icon(
            imageVector = Icons.Outlined.Palette,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.gpu_renderer_tap_to_change),
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

@Composable
private fun ModernWarningCard(title: String, message: String) {
  ElevatedCard(
      modifier = Modifier.fillMaxWidth(),
      colors =
          CardDefaults.elevatedCardColors(
              containerColor = MaterialTheme.colorScheme.errorContainer
          ),
      elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
  ) {
    Row(
        modifier = Modifier.padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
          modifier =
              Modifier.size(48.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center,
      ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(28.dp),
        )
      }

      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
      }
    }
  }
}

// ============ DIALOG COMPONENTS ============

@Composable
private fun RomInfoDialog(onDismiss: () -> Unit) {
  AlertDialog(
      onDismissRequest = onDismiss,
      icon = {
        Box(
            modifier =
                Modifier.size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer,
                                )
                        )
                    ),
            contentAlignment = Alignment.Center,
        ) {
          Icon(
              imageVector = Icons.Outlined.Info,
              contentDescription = null,
              modifier = Modifier.size(36.dp),
              tint = MaterialTheme.colorScheme.onPrimary,
          )
        }
      },
      title = {
        Text(
            text = stringResource(R.string.gpu_renderer_compatibility_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
      },
      text = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          Text(
              text = stringResource(R.string.gpu_renderer_compatibility_intro),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          // Supported ROMs Card
          ElevatedCard(
              colors =
                  CardDefaults.elevatedCardColors(
                      containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                  )
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
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.gpu_fully_supported),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
              }
              Text(
                  text = stringResource(R.string.gpu_supported_roms),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
          }

          // Limited Support ROMs Card
          ElevatedCard(
              colors =
                  CardDefaults.elevatedCardColors(
                      containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                  )
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
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.gpu_limited_support),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )
              }
              Text(
                  text = stringResource(R.string.gpu_unsupported_roms),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onErrorContainer,
              )
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

          // Why It Doesn't Work Section
          Text(
              text = stringResource(R.string.gpu_why_not_work),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
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
                                .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                  }
                }
          }

          // Tip Card
          Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
          ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                  imageVector = Icons.Outlined.Lightbulb,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.tertiary,
                  modifier = Modifier.size(20.dp),
              )
              Text(
                  text = stringResource(R.string.gpu_verify_tip),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onTertiaryContainer,
              )
            }
          }
        }
      },
      confirmButton = {
        FilledTonalButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
          Text(stringResource(R.string.gpu_got_it))
        }
      },
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
  AlertDialog(
      onDismissRequest = { if (!isProcessing) onDismiss() },
      icon = {
        Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
          if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
            )
          } else {
            Box(
                modifier =
                    Modifier.size(64.dp)
                        .clip(CircleShape)
                        .background(
                            if (verificationSuccess) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.errorContainer
                        ),
                contentAlignment = Alignment.Center,
            ) {
              Icon(
                  imageVector =
                      if (verificationSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                  contentDescription = null,
                  modifier = Modifier.size(36.dp),
                  tint =
                      if (verificationSuccess) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.error,
              )
            }
          }
        }
      },
      title = {
        Text(
            text =
                when {
                  isProcessing -> stringResource(R.string.gpu_applying_changes)
                  verificationSuccess -> stringResource(R.string.gpu_changes_applied)
                  else -> stringResource(R.string.gpu_warning)
                },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
      },
      text = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                  Text(
                      text = stringResource(R.string.gpu_renderer_label, pendingRenderer),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  )
                }
              }
            }
            verificationSuccess -> {
              ElevatedCard(
                  colors =
                      CardDefaults.elevatedCardColors(
                          containerColor =
                              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                      )
              ) {
                Column(
                    modifier = Modifier.padding(16.dp),
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
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                  }
                  Text(
                      text = stringResource(R.string.gpu_renderer_label, pendingRenderer),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                  )
                }
              }

              ElevatedCard(
                  colors =
                      CardDefaults.elevatedCardColors(
                          containerColor =
                              MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                      )
              ) {
                Column(
                    modifier = Modifier.padding(16.dp),
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
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                  }
                  Text(
                      text = stringResource(R.string.gpu_reboot_required_desc),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                  )
                }
              }
            }
            else -> {
              ElevatedCard(
                  colors =
                      CardDefaults.elevatedCardColors(
                          containerColor =
                              MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                      )
              ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Text(
                      text = stringResource(R.string.gpu_failed_apply),
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onErrorContainer,
                  )
                  if (verificationMessage.isNotBlank()) {
                    Text(
                        text = verificationMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
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
            Button(
                onClick = onReboot,
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) {
              Icon(
                  imageVector = Icons.Filled.Refresh,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp),
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(stringResource(R.string.gpu_reboot_now))
            }
          } else {
            FilledTonalButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
              Text(stringResource(R.string.gpu_close))
            }
          }
        }
      },
      dismissButton = {
        if (!isProcessing && verificationSuccess) {
          TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.gpu_reboot_later))
          }
        }
      },
  )
}

@Composable
private fun RebootConfirmationDialog(
    gpuInfo: GPUInfo,
    pendingRenderer: String,
    onDismiss: () -> Unit,
    onCheckCompatibility: () -> Unit,
    onConfirm: () -> Unit,
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      icon = {
        Box(
            modifier =
                Modifier.size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.colorScheme.tertiaryContainer,
                                )
                        )
                    ),
            contentAlignment = Alignment.Center,
        ) {
          Icon(
              imageVector = Icons.Outlined.ChangeCircle,
              contentDescription = null,
              modifier = Modifier.size(36.dp),
              tint = MaterialTheme.colorScheme.onTertiary,
          )
        }
      },
      title = {
        Text(
            text = stringResource(R.string.gpu_change_renderer_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
      },
      text = {
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

          // Change Preview Card
          ElevatedCard(
              colors =
                  CardDefaults.elevatedCardColors(
                      containerColor = MaterialTheme.colorScheme.surfaceVariant
                  )
          ) {
            Column(
                modifier = Modifier.padding(16.dp),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                  Text(
                      text = gpuInfo.rendererType,
                      style = MaterialTheme.typography.bodyLarge,
                      fontWeight = FontWeight.Medium,
                      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                  )
                }
              }

              // Arrow Down with Animation
              Icon(
                  imageVector = Icons.Filled.ArrowDownward,
                  contentDescription = null,
                  modifier = Modifier.size(28.dp),
                  tint = MaterialTheme.colorScheme.primary,
              )

              // New Renderer
              Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                    text = stringResource(R.string.gpu_new),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                  Text(
                      text = pendingRenderer,
                      style = MaterialTheme.typography.bodyLarge,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onPrimaryContainer,
                      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                  )
                }
              }
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

          // Important Warning
          Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
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
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.gpu_important),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
              }
              Text(
                  text = stringResource(R.string.gpu_change_warnings),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
              )
            }
          }

          // Check Compatibility Button
          OutlinedButton(
              onClick = onCheckCompatibility,
              modifier = Modifier.fillMaxWidth(),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(stringResource(R.string.gpu_check_rom_compatibility))
          }
        }
      },
      confirmButton = {
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
          Icon(
              imageVector = Icons.Filled.Check,
              contentDescription = null,
              modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(stringResource(R.string.gpu_apply_changes))
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
          Text(stringResource(R.string.gpu_cancel))
        }
      },
  )
}

@Composable
private fun RendererSelectionDialog(
    selectedRenderer: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      icon = {
        Box(
            modifier =
                Modifier.size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.colorScheme.tertiaryContainer,
                                )
                        )
                    ),
            contentAlignment = Alignment.Center,
        ) {
          Icon(
              imageVector = Icons.Outlined.Palette,
              contentDescription = null,
              modifier = Modifier.size(36.dp),
              tint = MaterialTheme.colorScheme.onTertiary,
          )
        }
      },
      title = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
              text = stringResource(R.string.gpu_renderer_select_title),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
          )
          Text(
              text = stringResource(R.string.gpu_renderer_select_desc),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
          )
        }
      },
      text = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
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

            ElevatedCard(
                onClick = { onSelect(renderer) },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.elevatedCardColors(
                        containerColor =
                            if (isSelected) MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.surface
                    ),
                elevation =
                    CardDefaults.elevatedCardElevation(
                        defaultElevation = if (isSelected) 4.dp else 1.dp
                    ),
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
                      color =
                          if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer
                          else MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                      text = description,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }

                if (isSelected) {
                  Box(
                      modifier =
                          Modifier.size(28.dp)
                              .clip(CircleShape)
                              .background(MaterialTheme.colorScheme.tertiary),
                      contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiary,
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
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
          Text(stringResource(R.string.cancel))
        }
      },
  )
}
