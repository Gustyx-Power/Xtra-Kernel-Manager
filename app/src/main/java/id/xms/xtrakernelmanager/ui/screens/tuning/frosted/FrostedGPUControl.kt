package id.xms.xtrakernelmanager.ui.screens.tuning.frosted

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun FrostedGPUControl(viewModel: TuningViewModel) {
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
    GlassmorphicCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(32.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = stringResource(R.string.mediatek_device_detected),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
          )
          Text(
              text = stringResource(R.string.mediatek_gpu_unavailable),
              style = MaterialTheme.typography.bodyMedium,
          )
        }
      }
    }
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
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Header
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
          Surface(
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.tertiaryContainer,
              modifier = Modifier.size(48.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                  imageVector = Icons.Filled.Memory,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onTertiaryContainer,
                  modifier = Modifier.size(24.dp),
              )
            }
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
              IconButton(
                  onClick = { showRomInfoDialog = true },
                  modifier = Modifier.size(24.dp),
              ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
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

        val rotationState by animateFloatAsState(
            targetValue = if (expanded) 180f else 0f,
            animationSpec = tween(300)
        )

        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.size(48.dp),
        ) {
          Icon(
              imageVector = Icons.Filled.ExpandMore,
              contentDescription = if (expanded) "Collapse" else "Expand",
              modifier = Modifier.size(28.dp).graphicsLayer { rotationZ = rotationState },
          )
        }
      }

      AnimatedVisibility(
          visible = expanded,
          enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
          exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300)),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          if (gpuInfo.availableFreqs.isNotEmpty()) {
            GPUFrequencyCard(viewModel = viewModel, gpuInfo = gpuInfo)
          }
          GPUPowerLevelCard(viewModel = viewModel, gpuInfo = gpuInfo)
          GPURendererCard(
              selectedRenderer = selectedRenderer,
              onRendererClick = { showRendererDialog = true },
          )
        }
      }
    }
  }
}

@Composable
private fun GPUFrequencyCard(viewModel: TuningViewModel, gpuInfo: GPUInfo) {
  val isFrequencyLocked by viewModel.isGpuFrequencyLocked.collectAsState()
  val lockedMinFreq by viewModel.lockedGpuMinFreq.collectAsState()
  val lockedMaxFreq by viewModel.lockedGpuMaxFreq.collectAsState()

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

  GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Icon(
              imageVector = Icons.Outlined.Speed,
              contentDescription = null,
              modifier = Modifier.size(24.dp),
          )
          Text(
              text = stringResource(R.string.gpu_frequency),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
          )
        }

        if (isFrequencyLocked) {
          Row(
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(R.string.gpu_locked),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
          }
        }
      }

      HorizontalDivider()

      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              text = stringResource(R.string.gpu_min_freq),
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
          )
          Text(
              text = "${minFreqSlider.toInt()} MHz",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
          )
        }

        Slider(
            value = minFreqSlider,
            onValueChange = { 
              isUserInteracting = true
              minFreqSlider = it 
            },
            onValueChangeFinished = { isUserInteracting = false },
            valueRange = gpuInfo.availableFreqs.minOrNull()!!.toFloat()..gpuInfo.availableFreqs.maxOrNull()!!.toFloat(),
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              text = stringResource(R.string.gpu_max_freq),
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
          )
          Text(
              text = "${maxFreqSlider.toInt()} MHz",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
          )
        }

        Slider(
            value = maxFreqSlider,
            onValueChange = { 
              isUserInteracting = true
              maxFreqSlider = it 
            },
            onValueChangeFinished = { isUserInteracting = false },
            valueRange = gpuInfo.availableFreqs.minOrNull()!!.toFloat()..gpuInfo.availableFreqs.maxOrNull()!!.toFloat(),
        )
      }

      HorizontalDivider()

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
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFrequencyLocked) MaterialTheme.colorScheme.error
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

      Text(
          text = if (isFrequencyLocked) stringResource(R.string.gpu_locked_warning)
                 else stringResource(R.string.gpu_lock_info),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun GPUPowerLevelCard(viewModel: TuningViewModel, gpuInfo: GPUInfo) {
  GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Icon(
            imageVector = Icons.Outlined.BatteryChargingFull,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Column {
          Text(
              text = stringResource(R.string.gpu_power_level_title),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
          )
          Text(
              text = "Select performance level",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      HorizontalDivider()

      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = "Current Level",
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = "${gpuInfo.powerLevel}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
      }

      val maxLevel = (gpuInfo.numPwrLevels - 1).coerceAtLeast(0)
      val levels = (0..maxLevel).toList()
      
      Text(
          text = "Available Levels (0-$maxLevel)",
          style = MaterialTheme.typography.labelMedium,
      )
      
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
                      if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.surfaceVariant
                  )
                  .clickable { viewModel.setGPUPowerLevel(level) },
              contentAlignment = Alignment.Center
          ) {
            Text(
                text = level.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
          }
        }
      }

      Text(
          text = "Level 0 = Highest performance, Level $maxLevel = Lowest performance",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun GPURendererCard(selectedRenderer: String, onRendererClick: () -> Unit) {
  GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = stringResource(R.string.gpu_renderer),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
          )
          Text(
              text = selectedRenderer,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
          )
        }
      }

      HorizontalDivider()

      Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.Top,
      ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(R.string.gpu_renderer_rom_info),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      FilledTonalButton(
          onClick = onRendererClick,
          modifier = Modifier.fillMaxWidth(),
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
private fun RomInfoDialog(onDismiss: () -> Unit) {
  AlertDialog(
      onDismissRequest = onDismiss,
      icon = {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
      },
      title = {
        Text(
            text = stringResource(R.string.gpu_renderer_compatibility_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
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
          )

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
              )
            }
            Text(
                text = stringResource(R.string.gpu_supported_roms),
                style = MaterialTheme.typography.bodySmall,
            )
          }

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
              )
            }
            Text(
                text = stringResource(R.string.gpu_unsupported_roms),
                style = MaterialTheme.typography.bodySmall,
            )
          }

          HorizontalDivider()

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
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                      verticalAlignment = Alignment.Top,
                  ) {
                    Text(text = "•", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodySmall,
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
            )
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
        if (isProcessing) {
          CircularProgressIndicator(modifier = Modifier.size(48.dp))
        } else {
          Icon(
              imageVector = if (verificationSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
              contentDescription = null,
              modifier = Modifier.size(48.dp),
              tint = if (verificationSuccess) MaterialTheme.colorScheme.primary
                     else MaterialTheme.colorScheme.error,
          )
        }
      },
      title = {
        Text(
            text = when {
              isProcessing -> stringResource(R.string.gpu_applying_changes)
              verificationSuccess -> stringResource(R.string.gpu_changes_applied)
              else -> stringResource(R.string.gpu_warning)
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
      },
      text = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          when {
            isProcessing -> {
              Text(
                  text = stringResource(R.string.gpu_writing_system_files),
                  style = MaterialTheme.typography.bodyMedium,
              )
              Text(
                  text = stringResource(R.string.gpu_renderer_label, pendingRenderer),
                  style = MaterialTheme.typography.bodySmall,
              )
            }
            verificationSuccess -> {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                      imageVector = Icons.Filled.Check,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(20.dp),
                  )
                  Text(
                      text = stringResource(R.string.gpu_runtime_property_updated),
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                  )
                }
                Text(
                    text = stringResource(R.string.gpu_renderer_label, pendingRenderer),
                    style = MaterialTheme.typography.bodySmall,
                )
              }

              HorizontalDivider()

              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                      imageVector = Icons.Outlined.Refresh,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(20.dp),
                  )
                  Text(
                      text = stringResource(R.string.gpu_reboot_required),
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                  )
                }
                Text(
                    text = stringResource(R.string.gpu_reboot_required_desc),
                    style = MaterialTheme.typography.bodySmall,
                )
              }
            }
            else -> {
              Text(
                  text = stringResource(R.string.gpu_failed_apply),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
              )
              if (verificationMessage.isNotBlank()) {
                Text(
                    text = verificationMessage,
                    style = MaterialTheme.typography.bodySmall,
                )
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
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
        Icon(
            imageVector = Icons.Outlined.ChangeCircle,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
      },
      title = {
        Text(
            text = stringResource(R.string.gpu_change_renderer_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
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
          )

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
              )
              Text(
                  text = gpuInfo.rendererType,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Medium,
              )
            }

            Icon(
                imageVector = Icons.Filled.ArrowDownward,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                  text = stringResource(R.string.gpu_new),
                  style = MaterialTheme.typography.labelSmall,
              )
              Text(
                  text = pendingRenderer,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Bold,
              )
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
                  fontWeight = FontWeight.Bold,
              )
              Text(
                  text = stringResource(R.string.gpu_change_warnings),
                  style = MaterialTheme.typography.bodySmall,
              )
            }
          }

          OutlinedButton(
              onClick = onCheckCompatibility,
              modifier = Modifier.fillMaxWidth(),
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
        Icon(
            imageVector = Icons.Outlined.Palette,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
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
          )
          Text(
              text = stringResource(R.string.gpu_renderer_select_desc),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      },
      text = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          val renderers = listOf(
              stringResource(R.string.gpu_renderer_opengl) to stringResource(R.string.gpu_renderer_opengl_desc),
              stringResource(R.string.gpu_renderer_vulkan) to stringResource(R.string.gpu_renderer_vulkan_desc),
              stringResource(R.string.gpu_renderer_angle) to stringResource(R.string.gpu_renderer_angle_desc),
              stringResource(R.string.gpu_renderer_skiagl) to stringResource(R.string.gpu_renderer_skiagl_desc),
              stringResource(R.string.gpu_renderer_skiavulkan) to stringResource(R.string.gpu_renderer_skiavulkan_desc),
          )

          renderers.forEach { (renderer, description) ->
            val isSelected = renderer == selectedRenderer

            GlassmorphicCard(
                onClick = { onSelect(renderer) },
                modifier = Modifier.fillMaxWidth(),
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
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  )
                  Text(
                      text = description,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }

                if (isSelected) {
                  Icon(
                      imageVector = Icons.Filled.Check,
                      contentDescription = null,
                      modifier = Modifier.size(24.dp),
                  )
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
