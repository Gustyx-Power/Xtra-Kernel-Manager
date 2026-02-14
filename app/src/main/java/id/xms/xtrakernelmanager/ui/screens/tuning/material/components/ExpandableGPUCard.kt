package id.xms.xtrakernelmanager.ui.screens.tuning.material.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import java.lang.Math.PI
import java.lang.Math.sin

@Composable
fun ExpandableGPUCard(viewModel: TuningViewModel) {
  // Get real GPU data from ViewModel
  val gpuInfo by viewModel.gpuInfo.collectAsState()

  var expanded by remember { mutableStateOf(false) }
  var sliderValue by remember { mutableFloatStateOf(0.7f) }
  var governorValue by remember { mutableStateOf("msm-adreno-tz") }
  var minFreq by remember { mutableStateOf("305 MHz") }
  var maxFreq by remember { mutableStateOf("680 MHz") }
  var rendererValue by remember { mutableStateOf("SkiaGL (Vulkan)") }

  // Extract GPU model from renderer (e.g., "Adreno (TM) 725" -> "Adreno 725")
  val gpuModel =
      gpuInfo.renderer.replace("(TM)", "").replace("(R)", "").trim().ifEmpty { "Unknown GPU" }

  Card(
      modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.animateContentSize(),
      shape = RoundedCornerShape(24.dp),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
  ) {
    Column(modifier = Modifier.padding(24.dp)) {
      // Header
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = stringResource(R.string.material_gpu_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
        )
        Surface(
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp),
        ) {
          // Show vendor from gpuInfo (e.g., "Qualcomm" -> "ADRENO")
          val badgeText =
              when {
                gpuInfo.vendor.contains("Qualcomm", ignoreCase = true) -> stringResource(R.string.material_gpu_adreno)
                gpuInfo.vendor.contains("ARM", ignoreCase = true) -> stringResource(R.string.material_gpu_mali)
                gpuInfo.vendor.contains("PowerVR", ignoreCase = true) -> stringResource(R.string.material_gpu_powervr)
                gpuInfo.renderer.contains("Adreno", ignoreCase = true) -> stringResource(R.string.material_gpu_adreno)
                gpuInfo.renderer.contains("Mali", ignoreCase = true) -> stringResource(R.string.material_gpu_mali)
                else -> gpuInfo.vendor.uppercase().take(8)
              }
          Text(
              text = badgeText,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              style = MaterialTheme.typography.labelSmall,
              letterSpacing = 1.sp,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
          )
        }
      }

      Spacer(Modifier.height(16.dp))

      // Main Stats
      Column {
        Row(verticalAlignment = Alignment.Bottom) {
          Text(
              text = "${gpuInfo.currentFreq}",
              style = MaterialTheme.typography.displayMedium,
              fontWeight = FontWeight.Medium,
              lineHeight = 40.sp,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
          )
          Text(
              text = " MHz",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
              modifier = Modifier.padding(bottom = 6.dp),
          )
        }
        Text(
            text = stringResource(R.string.material_gpu_frequency),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
        )
      }

      Spacer(Modifier.height(24.dp))

      // Grid Stats (Load & Model)
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Inner Card 1: Load
        Surface(
            modifier = Modifier.weight(1f).height(90.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), // Inner Surface
            shape = RoundedCornerShape(16.dp),
        ) {
          Column(
              modifier = Modifier.padding(16.dp).fillMaxSize(),
              verticalArrangement = Arrangement.Center, // Centered vertically like Home
          ) {
            Text(
                text = "${gpuInfo.gpuLoad}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.material_gpu_load),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
          }
        }

        // Inner Card 2: GPU Name
        Surface(
            modifier = Modifier.weight(1f).height(90.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), // Inner Surface
            shape = RoundedCornerShape(16.dp),
        ) {
          Column(
              modifier = Modifier.padding(16.dp).fillMaxSize(),
              verticalArrangement = Arrangement.Center,
          ) {
            Text(
                text = gpuModel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
            )
            Text(
                text = stringResource(R.string.material_gpu_name),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
          }
        }
      }

      // Expanded Controls
      AnimatedVisibility(visible = expanded) {
        // Get ViewModel states
        val isFrequencyLocked by viewModel.isGpuFrequencyLocked.collectAsState()
        val lockedMinFreq by viewModel.lockedGpuMinFreq.collectAsState()
        val lockedMaxFreq by viewModel.lockedGpuMaxFreq.collectAsState()

        // Build freq options from availableFreqs
        val freqOptions = gpuInfo.availableFreqs.map { "${it} MHz" }

        // Track selected values - use locked values if available
        var selectedMinFreq by
            remember(gpuInfo.minFreq, isFrequencyLocked, lockedMinFreq) { 
              mutableStateOf(
                if (isFrequencyLocked && lockedMinFreq > 0) "${lockedMinFreq} MHz"
                else "${gpuInfo.minFreq} MHz"
              ) 
            }
        var selectedMaxFreq by
            remember(gpuInfo.maxFreq, isFrequencyLocked, lockedMaxFreq) { 
              mutableStateOf(
                if (isFrequencyLocked && lockedMaxFreq > 0) "${lockedMaxFreq} MHz"
                else "${gpuInfo.maxFreq} MHz"
              ) 
            }
        val maxPowerLevel = gpuInfo.numPwrLevels.coerceIn(1, 10) // Cap at 10
        var powerSliderValue by
            remember(gpuInfo.powerLevel, maxPowerLevel) {
              mutableFloatStateOf(
                  // Map current GPU power level to slider (0.0 - 1.0)
                  if (maxPowerLevel > 0)
                      gpuInfo.powerLevel.coerceAtMost(maxPowerLevel).toFloat() /
                          maxPowerLevel.toFloat()
                  else 0f
              )
            }

        Column(
            modifier = Modifier.padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

          // Governor (display only, not changeable for GPU)
          GpuControlRow(
              label = stringResource(R.string.material_gpu_governor),
              value = "msm-adreno-tz",
              icon = Icons.Rounded.Speed,
              options = listOf("msm-adreno-tz"),
              onValueChange = { /* GPU governor not changeable */ },
          )

          // GPU Frequency Lock Section with Lock Status Badge
          Surface(
              modifier = Modifier.fillMaxWidth(),
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
              shape = RoundedCornerShape(16.dp),
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              // Header with Lock Status
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                    stringResource(R.string.material_gpu_frequency_lock),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                
                // Lock Status Badge
                AnimatedVisibility(
                    visible = isFrequencyLocked,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                ) {
                  Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = MaterialTheme.colorScheme.primaryContainer,
                  ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Icon(
                          imageVector = Icons.Filled.Lock,
                          contentDescription = null,
                          modifier = Modifier.size(12.dp),
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

              Spacer(Modifier.height(12.dp))

              // Min/Max Tiles with real freq options
              Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GpuTile(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.material_gpu_min_frequency),
                    value = selectedMinFreq,
                    options = freqOptions.ifEmpty { listOf("${gpuInfo.minFreq} MHz") },
                    onValueChange = { newValue ->
                      selectedMinFreq = newValue
                      val minFreq = newValue.replace(" MHz", "").toIntOrNull() ?: gpuInfo.minFreq
                      val maxFreq = selectedMaxFreq.replace(" MHz", "").toIntOrNull() ?: gpuInfo.maxFreq
                      viewModel.lockGPUFrequency(minFreq, maxFreq)
                    },
                )
                GpuTile(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.material_gpu_max_frequency),
                    value = selectedMaxFreq,
                    options = freqOptions.ifEmpty { listOf("${gpuInfo.maxFreq} MHz") },
                    onValueChange = { newValue ->
                      selectedMaxFreq = newValue
                      val minFreq = selectedMinFreq.replace(" MHz", "").toIntOrNull() ?: gpuInfo.minFreq
                      val maxFreq = newValue.replace(" MHz", "").toIntOrNull() ?: gpuInfo.maxFreq
                      viewModel.lockGPUFrequency(minFreq, maxFreq)
                    },
                )
              }

              // Lock/Unlock Button
              Spacer(Modifier.height(12.dp))
              Button(
                  onClick = {
                    if (isFrequencyLocked) {
                      viewModel.unlockGPUFrequency()
                    } else {
                      val minFreq = selectedMinFreq.replace(" MHz", "").toIntOrNull() ?: gpuInfo.minFreq
                      val maxFreq = selectedMaxFreq.replace(" MHz", "").toIntOrNull() ?: gpuInfo.maxFreq
                      viewModel.lockGPUFrequency(minFreq, maxFreq)
                    }
                  },
                  modifier = Modifier.fillMaxWidth(),
                  colors = ButtonDefaults.buttonColors(
                      containerColor = if (isFrequencyLocked) 
                          MaterialTheme.colorScheme.error
                      else 
                          MaterialTheme.colorScheme.primary
                  ),
              ) {
                Icon(
                    imageVector = if (isFrequencyLocked) Icons.Filled.LockOpen else Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFrequencyLocked) 
                        stringResource(R.string.material_gpu_unlock_frequency)
                    else 
                        stringResource(R.string.material_gpu_lock_frequency)
                )
              }
            }
          }

          // Power Slider with WavySlider style - 0 to 10
          Surface(
              modifier = Modifier.fillMaxWidth(),
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
              shape = RoundedCornerShape(16.dp),
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                    stringResource(R.string.material_gpu_power_level),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    stringResource(R.string.material_gpu_level_format, (powerSliderValue * maxPowerLevel).toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
              }
              Spacer(Modifier.height(16.dp))
              WavySlider(
                  value = powerSliderValue,
                  onValueChange = { newValue ->
                    powerSliderValue = newValue
                    val level = (newValue * maxPowerLevel).toInt()
                    viewModel.setGPUPowerLevel(level)
                  },
              )
            }
          }

          // Renderer
          GpuControlRow(
              label = stringResource(R.string.material_gpu_renderer),
              value = gpuInfo.rendererType,
              options = listOf("skiavk", "skiagl", "opengl"),
              onValueChange = { viewModel.setGPURenderer(it) },
          )
        }
      }
    }
  }
}

@Composable
fun GpuControlRow(
    label: String,
    value: String,
    icon: ImageVector? = null,
    options: List<String> = emptyList(),
    onValueChange: (String) -> Unit = {},
) {
  var expanded by remember { mutableStateOf(false) }

  Surface(
      modifier = Modifier.fillMaxWidth().clickable { expanded = true },
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), // Inner Surface
      shape = RoundedCornerShape(12.dp),
  ) {
    Column {
      Row(
          modifier = Modifier.padding(12.dp).fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Text(
              label,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
              value,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold,
          )
          Icon(
              Icons.Rounded.ArrowDropDown,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      DropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false },
          modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
          shape = RoundedCornerShape(12.dp),
      ) {
        options.forEach { option ->
          val isSelected = option == value
          DropdownMenuItem(
              text = {
                Text(
                    option,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
              },
              colors =
                  MenuDefaults.itemColors(
                      textColor =
                          if (isSelected) MaterialTheme.colorScheme.primary
                          else MaterialTheme.colorScheme.onSurface,
                  ),
              onClick = {
                onValueChange(option)
                expanded = false
              },
          )
        }
      }
    }
  }
}

@Composable
fun GpuTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    options: List<String> = emptyList(),
    onValueChange: (String) -> Unit = {},
) {
  var expanded by remember { mutableStateOf(false) }

  Surface(
      modifier = modifier.clickable { expanded = true },
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), // Inner Surface
      shape = RoundedCornerShape(12.dp),
  ) {
    Column {
      Column(modifier = Modifier.padding(12.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
              value,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.primary,
          )
          Icon(
              Icons.Rounded.ArrowDropDown,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      DropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false },
          modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
          shape = RoundedCornerShape(12.dp),
      ) {
        options.forEach { option ->
          val isSelected = option == value
          DropdownMenuItem(
              text = {
                Text(
                    option,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
              },
              colors =
                  MenuDefaults.itemColors(
                      textColor =
                          if (isSelected) MaterialTheme.colorScheme.primary
                          else MaterialTheme.colorScheme.onSurface,
                  ),
              onClick = {
                onValueChange(option)
                expanded = false
              },
          )
        }
      }
    }
  }
}

@Composable
fun WavySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    waveAmplitude: Float = 10f,
    waveFrequency: Float = 20f,
    strokeWidth: Float = 12f,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
  val piFreq = remember(waveFrequency) { PI * waveFrequency }
  val step = 8f
  Box(
      modifier =
          modifier
              .fillMaxWidth()
              .height(waveAmplitude.dp * 3)
              .pointerInput(Unit) {
                detectTapGestures { offset ->
                  val newValue = (offset.x / size.width).coerceIn(0f, 1f)
                  onValueChange(newValue)
                }
              }
              .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                  change.consume()
                  val newValue = (change.position.x / size.width).coerceIn(0f, 1f)
                  onValueChange(newValue)
                }
              }
  ) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
      val width = size.width
      val height = size.height
      val centerY = height / 2

      // Build the full wave path with optimized step
      val path = Path()
      path.moveTo(0f, centerY)

      var x = step
      while (x <= width) {
        val normalizedX = x / width
        val y = centerY + sin(normalizedX * piFreq) * waveAmplitude
        path.lineTo(x, y.toFloat())
        x += step
      }
      // Ensure we reach the end
      path.lineTo(width, centerY + (sin(piFreq).toFloat() * waveAmplitude))

      // Draw inactive track (full wave)
      drawPath(
          path = path,
          color = inactiveColor,
          style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
      )

      // Draw active track (clipped wave)
      val activeEndX = width * value
      if (activeEndX > 0f) {
        drawContext.canvas.save()
        drawContext.canvas.clipRect(0f, 0f, activeEndX, height)
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        drawContext.canvas.restore()
      }

      // Draw thumb - use cached sin value
      val thumbX = activeEndX
      val thumbY = centerY + sin((thumbX / width) * piFreq) * waveAmplitude
      drawCircle(
          color = primaryColor,
          radius = strokeWidth,
          center = Offset(thumbX, thumbY.toFloat()),
      )
    }
  }
}
