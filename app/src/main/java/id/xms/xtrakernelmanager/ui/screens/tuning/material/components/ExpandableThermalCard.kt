package id.xms.xtrakernelmanager.ui.screens.tuning.material.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.screens.tuning.material.WavyCircularProgressIndicator
import androidx.compose.ui.res.stringResource

@Composable
fun ExpandableThermalCard(
    viewModel: TuningViewModel,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onClickNav: () -> Unit,
    topLeftContent: @Composable () -> Unit = {},
) {
  // Get real temperature from ViewModel
  val cpuTemp by viewModel.cpuTemperature.collectAsState()
  val thermalZones by viewModel.thermalZones.collectAsState()

  val tempDisplay = if (cpuTemp > 0) "${cpuTemp.toInt()}°C" else "—"

  val height by
      animateDpAsState(targetValue = if (expanded) 380.dp else 120.dp, label = "thermal_height")

  Box(
      modifier =
          Modifier.fillMaxWidth().height(height).clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
          ) {
            onExpandChange(!expanded)
          }
  ) {
    if (!expanded) {
      DashboardNavCard(
          title = "Thermal",
          subtitle = "Monitor & Profiles",
          icon = Icons.Rounded.Thermostat,
          badgeText = tempDisplay, // Now shows real temperature
          onClick = onClickNav, // Navigate to thermal settings instead of just expanding
      )
    } else {
      val cornerRadius = 24.dp
      val gap = 24.dp
      val cutoutHeight = 120.dp
      val themeColor = MaterialTheme.colorScheme.secondaryContainer

      Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
          Box(modifier = Modifier.weight(1f).height(cutoutHeight)) { topLeftContent() }
          Box(modifier = Modifier.weight(1f).fillMaxHeight())
        }
      }

      Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val bodyX = w / 2 + gap.toPx() / 2
          val ch = cutoutHeight.toPx() + gap.toPx()
          val cr = cornerRadius.toPx()
          val ir = cornerRadius.toPx()

          val path =
              Path().apply {
                moveTo(bodyX + cr, 0f)
                quadraticTo(bodyX, 0f, bodyX, cr)
                lineTo(bodyX, ch - ir)
                arcTo(
                    androidx.compose.ui.geometry.Rect(bodyX - 2 * ir, ch - 2 * ir, bodyX, ch),
                    0f,
                    90f,
                    false,
                )
                lineTo(cr, ch)
                quadraticTo(0f, ch, 0f, ch + cr)
                lineTo(0f, h - cr)
                quadraticTo(0f, h, cr, h)
                lineTo(w - cr, h)
                quadraticTo(w, h, w, h - cr)
                lineTo(w, cr)
                quadraticTo(w, 0f, w - cr, 0f)
                lineTo(bodyX + cr, 0f)
                close()
              }
          drawPath(path, color = themeColor)
        }

        Column(modifier = Modifier.fillMaxSize()) {
          Row(modifier = Modifier.fillMaxWidth()) {
            // Left: Spacer + Single Status Badge
            Column(modifier = Modifier.weight(1f).padding(start = 24.dp, end = 8.dp)) {
              Spacer(
                  modifier = Modifier.height(cutoutHeight + gap + 16.dp)
              ) // Lowered by extra 16dp for generous clearance
              // Single Health Badge
              val (healthIcon, healthText, healthColor) =
                  when {
                    cpuTemp < 60 ->
                        Triple(
                            Icons.Rounded.CheckCircle,
                            stringResource(id.xms.xtrakernelmanager.R.string.system_healthy),
                            MaterialTheme.colorScheme.primary,
                        )
                    cpuTemp < 80 ->
                        Triple(
                            Icons.Rounded.Warning,
                            stringResource(id.xms.xtrakernelmanager.R.string.system_warm),
                            MaterialTheme.colorScheme.tertiary,
                        )
                    else ->
                        Triple(
                            Icons.Rounded.Dangerous,
                            stringResource(id.xms.xtrakernelmanager.R.string.system_throttling),
                            MaterialTheme.colorScheme.error,
                        )
                  }

              Surface(
                  color = healthColor.copy(alpha = 0.1f),
                  contentColor = healthColor,
                  shape = RoundedCornerShape(12.dp),
                  border =
                      androidx.compose.foundation.BorderStroke(
                          1.dp,
                          healthColor.copy(alpha = 0.2f),
                      ),
                  modifier = Modifier.fillMaxWidth(),
              ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                  Icon(healthIcon, null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                      text = healthText,
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.SemiBold,
                  )
                }
              }
            }

            // Right: Header Icon
            Column(
                modifier = Modifier.weight(1f).padding(top = 20.dp, end = 20.dp, start = 32.dp)
            ) {
              val currentPreset by viewModel.currentThermalPreset.collectAsState()

              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                Box(
                    modifier =
                        Modifier.size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                  Icon(
                      Icons.Rounded.Thermostat,
                      null,
                      tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  )
                }

                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp),
                ) {
                  Text(
                      text = currentPreset,
                      style = MaterialTheme.typography.labelMedium,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                      color = MaterialTheme.colorScheme.onTertiaryContainer,
                  )
                }
              }

              Spacer(modifier = Modifier.height(24.dp))

              // Wavy Temperature Indicator
              Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                val progress = (cpuTemp / 100f).coerceIn(0f, 1f)
                val color =
                    when {
                      cpuTemp < 45 -> MaterialTheme.colorScheme.primary
                      cpuTemp < 60 -> MaterialTheme.colorScheme.tertiary
                      else -> MaterialTheme.colorScheme.error
                    }

                val statusText =
                    when {
                      cpuTemp < 45 -> stringResource(id.xms.xtrakernelmanager.R.string.temp_normal)
                      cpuTemp < 65 -> stringResource(id.xms.xtrakernelmanager.R.string.temp_warm)
                      else -> stringResource(id.xms.xtrakernelmanager.R.string.temp_overheat)
                    }

                WavyCircularProgressIndicator(
                    progress = progress,
                    text = "${cpuTemp.toInt()}°C",
                    subtext = statusText,
                    modifier = Modifier.size(140.dp),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    strokeWidth = 10.dp,
                    amplitude = 3.dp,
                    frequency = 8,
                )
              }
            }
          }

          Column(
              modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp),
              verticalArrangement = Arrangement.Center,
          ) {
            ThermalPresetDropdown(viewModel)
            Spacer(modifier = Modifier.height(16.dp))
            ThermalSetOnBootToggle(viewModel)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Navigation Button to Thermal Settings
            Button(
                onClick = onClickNav,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Thermal Settings",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalPresetDropdown(viewModel: TuningViewModel) {
  val currentPreset by viewModel.currentThermalPreset.collectAsState()
  val setOnBoot by viewModel.isThermalSetOnBoot.collectAsState()
  val presets = listOf("Class 0", "Extreme", "Dynamic", "Incalls", "Thermal 20")

  var expanded by remember { mutableStateOf(false) }

  Box {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { expanded = true },
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text(
            stringResource(id.xms.xtrakernelmanager.R.string.thermal_profile_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
              currentPreset,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.primary,
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
              Icons.Rounded.ArrowDropDown,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
        shape = RoundedCornerShape(12.dp),
    ) {
      presets.forEach { preset ->
        val isSelected = preset == currentPreset
        DropdownMenuItem(
            text = {
              Text(
                  preset,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color =
                      if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.onSurface,
              )
            },
            onClick = {
              viewModel.setThermalPreset(preset, setOnBoot)
              expanded = false
            },
        )
      }
    }
  }
}

@Composable
fun ThermalSetOnBootToggle(viewModel: TuningViewModel) {
  val currentPreset by viewModel.currentThermalPreset.collectAsState()
  val setOnBoot by viewModel.isThermalSetOnBoot.collectAsState()

  Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text("Set on Boot", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    Switch(
        checked = setOnBoot,
        onCheckedChange = { viewModel.setThermalPreset(currentPreset, it) },
        thumbContent =
            if (setOnBoot) {
              {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                )
              }
            } else {
              null
            },
        colors =
            SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
            ),
    )
  }
}

@Composable
fun ThermalZoneItem(zone: id.xms.xtrakernelmanager.domain.native.NativeLib.ThermalZone) {
  val color =
      when {
        zone.temp > 60 -> MaterialTheme.colorScheme.errorContainer
        zone.temp > 45 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
      }

  val contentColor =
      when {
        zone.temp > 60 -> MaterialTheme.colorScheme.onErrorContainer
        zone.temp > 45 -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
      }

  Column(
      modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(color).padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
        text = "${zone.temp.toInt()}°",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = contentColor,
    )
    Text(
        text = zone.name.take(10), // Truncate long names
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        color = contentColor.copy(alpha = 0.8f),
    )
  }
}
