package id.xms.xtrakernelmanager.ui.screens.tuning.liquid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.RAMConfig
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidRAMControl(viewModel: TuningViewModel) {
  val persistedConfig by
      viewModel.preferencesManager.getRamConfig().collectAsState(initial = RAMConfig())

  var expanded by remember { mutableStateOf(false) }
  var swappiness by remember { mutableFloatStateOf(persistedConfig.swappiness.toFloat()) }
  var zramSize by remember { mutableFloatStateOf(persistedConfig.zramSize.toFloat()) }
  var swapSize by remember { mutableFloatStateOf(persistedConfig.swapSize.toFloat()) }
  var dirtyRatio by remember { mutableFloatStateOf(persistedConfig.dirtyRatio.toFloat()) }
  var minFreeMem by remember { mutableFloatStateOf(persistedConfig.minFreeMem.toFloat()) }

  LaunchedEffect(persistedConfig) {
    swappiness = persistedConfig.swappiness.toFloat()
    zramSize = persistedConfig.zramSize.toFloat()
    swapSize = persistedConfig.swapSize.toFloat()
    dirtyRatio = persistedConfig.dirtyRatio.toFloat()
    minFreeMem = persistedConfig.minFreeMem.toFloat()
  }

  var showZramDialog by remember { mutableStateOf(false) }
  var showSwapDialog by remember { mutableStateOf(false) }
  var showZramApplyingDialog by remember { mutableStateOf(false) }
  var showSwapApplyingDialog by remember { mutableStateOf(false) }
  var showResultDialog by remember { mutableStateOf(false) }
  var resultSuccess by remember { mutableStateOf(false) }
  var resultMessage by remember { mutableStateOf("") }
  var isApplyingZram by remember { mutableStateOf(false) }
  var isApplyingSwap by remember { mutableStateOf(false) }

  var zramLogs by remember { mutableStateOf(listOf<String>()) }
  var swapLogs by remember { mutableStateOf(listOf<String>()) }

  val currentCompAlgo by viewModel.currentCompressionAlgorithm.collectAsState()

  fun pushConfig(
      sw: Int = swappiness.toInt(),
      zr: Int = zramSize.toInt(),
      sp: Int = swapSize.toInt(),
      dr: Int = dirtyRatio.toInt(),
      mf: Int = minFreeMem.toInt(),
      compAlgo: String = currentCompAlgo,
  ) {
    viewModel.setRAMParameters(
        RAMConfig(
            swappiness = sw,
            zramSize = zr,
            swapSize = sp,
            dirtyRatio = dr,
            minFreeMem = mf,
            compressionAlgorithm = compAlgo,
        )
    )
  }

  GlassmorphicCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
      // ===== HEADER =====
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
          Box(
              modifier =
                  Modifier.size(48.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(
                          Brush.linearGradient(
                              colors =
                                  listOf(
                                      MaterialTheme.colorScheme.primary,
                                      MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                  )
                          )
                      ),
              contentAlignment = Alignment.Center,
          ) {
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp),
            )
          }

          Column {
            Text(
                text = stringResource(R.string.ram_control),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.liquid_ram_virtual_memory_management),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        // Dropdown button LEBIH BESAR
        Box(
            modifier =
                Modifier.size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .clickable { expanded = !expanded },
            contentAlignment = Alignment.Center,
        ) {
          Icon(
              imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
              contentDescription = if (expanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
              modifier = Modifier.size(32.dp),
              tint = MaterialTheme.colorScheme.primary,
          )
        }
      }

      // ===== COLLAPSIBLE CONTENT =====
      AnimatedVisibility(
          visible = expanded,
          enter = fadeIn() + expandVertically(),
          exit = fadeOut() + shrinkVertically(),
      ) {
        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          // Swappiness Card
          Card(
              modifier = Modifier.fillMaxWidth(),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.swappiness),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
              }

              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Bottom,
              ) {
                Text(
                    text = stringResource(R.string.liquid_ram_value),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${swappiness.toInt()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
              }

              Slider(
                  value = swappiness,
                  onValueChange = { swappiness = it },
                  onValueChangeFinished = { pushConfig() },
                  valueRange = 0f..200f,
                  steps = 19,
                  colors =
                      SliderDefaults.colors(
                          thumbColor = MaterialTheme.colorScheme.primary,
                          activeTrackColor = MaterialTheme.colorScheme.primary,
                      ),
              )
            }
          }

          // Dirty Ratio Card
          Card(
              modifier = Modifier.fillMaxWidth(),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                    imageVector = Icons.Default.DataUsage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.dirty_ratio),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
              }

              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Bottom,
              ) {
                Text(
                    text = stringResource(R.string.liquid_ram_percentage),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${dirtyRatio.toInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
              }

              Slider(
                  value = dirtyRatio,
                  onValueChange = { dirtyRatio = it },
                  onValueChangeFinished = { pushConfig() },
                  valueRange = 1f..50f,
                  steps = 48,
                  colors =
                      SliderDefaults.colors(
                          thumbColor = MaterialTheme.colorScheme.primary,
                          activeTrackColor = MaterialTheme.colorScheme.primary,
                      ),
              )
            }
          }

          // Min Free Memory Card
          Card(
              modifier = Modifier.fillMaxWidth(),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.min_free_memory),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
              }

              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Bottom,
              ) {
                Text(
                    text = stringResource(R.string.liquid_ram_size_kb),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${minFreeMem.toInt()} KB",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
              }

              Slider(
                  value = minFreeMem,
                  onValueChange = { minFreeMem = it },
                  onValueChangeFinished = { pushConfig() },
                  valueRange = 0f..262144f,
                  steps = 14,
                  colors =
                      SliderDefaults.colors(
                          thumbColor = MaterialTheme.colorScheme.primary,
                          activeTrackColor = MaterialTheme.colorScheme.primary,
                      ),
              )
            }
          }

          // ZRAM Card
          Card(
              modifier = Modifier.fillMaxWidth(),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                      imageVector = Icons.Default.CloudCircle,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(20.dp),
                  )
                  Text(
                      text = stringResource(R.string.zram_size),
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary,
                  )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = MaterialTheme.colorScheme.primaryContainer,
                  ) {
                    Text(
                        text = if (zramSize.toInt() > 0) "${zramSize.toInt()} MB" else stringResource(R.string.disabled),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                  }

                  val currentCompAlgorithm by viewModel.currentCompressionAlgorithm.collectAsState()
                  if (zramSize.toInt() > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                    ) {
                      Row(
                          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                          horizontalArrangement = Arrangement.spacedBy(4.dp),
                          verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                        Text(
                            text = currentCompAlgorithm.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                      }
                    }
                  }
                }
              }

              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

              Card(
                  modifier = Modifier.fillMaxWidth().clickable { showZramDialog = true },
                  colors =
                      CardDefaults.cardColors(
                          containerColor =
                              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                      ),
                  shape = RoundedCornerShape(12.dp),
              ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                  Row(
                      horizontalArrangement = Arrangement.spacedBy(12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Box(
                        modifier =
                            Modifier.size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                      Icon(
                          imageVector = Icons.Default.Tune,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.onPrimary,
                          modifier = Modifier.size(20.dp),
                      )
                    }
                    Column {
                      Text(
                          text = stringResource(R.string.liquid_ram_configure_zram),
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                      )
                      Text(
                          text = stringResource(R.string.liquid_ram_tap_to_adjust_size),
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                  }
                  if (isApplyingZram) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                  } else {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                  }
                }
              }
            }
          }

          // Swap Card
          Card(
              modifier = Modifier.fillMaxWidth(),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                      imageVector = Icons.Default.SdCard,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(20.dp),
                  )
                  Text(
                      text = stringResource(R.string.swap_size),
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary,
                  )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                  val swapText =
                      if (swapSize.toInt() > 0) {
                        val gb = swapSize / 1024f
                        String.format(Locale.US, "%.1f GB", gb)
                      } else stringResource(R.string.disabled)
                  Text(
                      text = swapText,
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onPrimaryContainer,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  )
                }
              }

              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

              Card(
                  modifier = Modifier.fillMaxWidth().clickable { showSwapDialog = true },
                  colors =
                      CardDefaults.cardColors(
                          containerColor =
                              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                      ),
                  shape = RoundedCornerShape(12.dp),
              ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                  Row(
                      horizontalArrangement = Arrangement.spacedBy(12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Box(
                        modifier =
                            Modifier.size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                      Icon(
                          imageVector = Icons.Default.Tune,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.onPrimary,
                          modifier = Modifier.size(20.dp),
                      )
                    }
                    Column {
                      Text(
                          text = stringResource(R.string.swap_configure),
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                      )
                      Text(
                          text = stringResource(R.string.tap_to_adjust_size),
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                  }
                  if (isApplyingSwap) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                  } else {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // ===== ENHANCED ZRAM CONFIG DIALOG =====
  if (showZramDialog) {
    var tempZram by remember { mutableFloatStateOf(zramSize.coerceAtLeast(256f)) }
    val availableAlgorithms by viewModel.availableCompressionAlgorithms.collectAsState()
    val currentAlgorithm by viewModel.currentCompressionAlgorithm.collectAsState()
    var selectedAlgorithm by remember { mutableStateOf(currentAlgorithm) }
    var expandedAlgoDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(currentAlgorithm) { selectedAlgorithm = currentAlgorithm }

    AlertDialog(
        onDismissRequest = { if (!isApplyingZram) showZramDialog = false },
        icon = {
          Box(
              modifier =
                  Modifier.size(56.dp)
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
                imageVector = Icons.Default.CloudCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp),
            )
          }
        },
        title = {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.zram_configure),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.zram_compressed_allocation),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
        text = {
          Column(
              modifier = Modifier.verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
            ) {
              Row(
                  modifier = Modifier.padding(12.dp),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.zram_set_size_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
              }
            }

            // Compression Algorithm Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                    imageVector = Icons.Default.Archive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.liquid_ram_compression_algorithm),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
              }

              ExposedDropdownMenuBox(
                  expanded = expandedAlgoDropdown,
                  onExpandedChange = { expandedAlgoDropdown = !expandedAlgoDropdown },
              ) {
                OutlinedTextField(
                    value = selectedAlgorithm,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                      Icon(
                          imageVector =
                              if (expandedAlgoDropdown) Icons.Default.ExpandLess
                              else Icons.Default.ExpandMore,
                          contentDescription = null,
                      )
                    },
                    modifier =
                        Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                    shape = RoundedCornerShape(12.dp),
                )
                ExposedDropdownMenu(
                    expanded = expandedAlgoDropdown,
                    onDismissRequest = { expandedAlgoDropdown = false },
                ) {
                  availableAlgorithms.forEach { algo ->
                    DropdownMenuItem(
                        text = {
                          Row(
                              horizontalArrangement = Arrangement.spacedBy(8.dp),
                              verticalAlignment = Alignment.CenterVertically,
                          ) {
                            Text(algo.uppercase())
                            if (algo == selectedAlgorithm) {
                              Icon(
                                  imageVector = Icons.Default.Check,
                                  contentDescription = null,
                                  modifier = Modifier.size(16.dp),
                                  tint = MaterialTheme.colorScheme.primary,
                              )
                            }
                          }
                        },
                        onClick = {
                          selectedAlgorithm = algo
                          expandedAlgoDropdown = false
                        },
                    )
                  }
                }
              }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Bottom,
              ) {
                Text(
                    text = stringResource(R.string.zram_size),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                  Text(
                      text = if (tempZram.toInt() == 0) stringResource(R.string.disabled) else "${tempZram.toInt()} MB",
                      style = MaterialTheme.typography.labelLarge,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onPrimaryContainer,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                  )
                }
              }

              LinearProgressIndicator(
                  progress = { tempZram / 4096f },
                  modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                  color = MaterialTheme.colorScheme.primary,
                  trackColor = MaterialTheme.colorScheme.surfaceVariant,
              )

              Slider(
                  value = tempZram,
                  onValueChange = { tempZram = it },
                  valueRange = 0f..4096f,
                  steps = 15,
                  colors =
                      SliderDefaults.colors(
                          thumbColor = MaterialTheme.colorScheme.primary,
                          activeTrackColor = MaterialTheme.colorScheme.primary,
                      ),
              )
            }
          }
        },
        confirmButton = {
          Button(
              enabled = !isApplyingZram,
              onClick = {
                showZramDialog = false
                isApplyingZram = true
                showZramApplyingDialog = true
                zramLogs = listOf()

                viewModel.setZRAMWithLiveLog(
                    sizeBytes = tempZram.toLong() * 1024L * 1024L,
                    compressionAlgorithm = selectedAlgorithm,
                    onLog = { log -> zramLogs = zramLogs + log },
                    onComplete = { success ->
                      zramSize = tempZram
                      pushConfig(zr = tempZram.toInt(), compAlgo = selectedAlgorithm)
                      isApplyingZram = false
                      showZramApplyingDialog = false
                      resultSuccess = success
                      resultMessage =
                          if (success) "ZRAM applied: ${tempZram.toInt()} MB ($selectedAlgorithm)"
                          else "Failed to apply ZRAM"
                      showResultDialog = true
                    },
                )
              },
          ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Apply Changes")
          }
        },
        dismissButton = {
          TextButton(onClick = { if (!isApplyingZram) showZramDialog = false }) { Text("Cancel") }
        },
    )
  }

  // ===== ENHANCED SWAP CONFIG DIALOG =====
  if (showSwapDialog) {
    var tempSwap by remember { mutableFloatStateOf(swapSize.coerceAtLeast(1024f)) }

    AlertDialog(
        onDismissRequest = { if (!isApplyingSwap) showSwapDialog = false },
        icon = {
          Box(
              modifier =
                  Modifier.size(56.dp)
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
                imageVector = Icons.Default.SdCard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp),
            )
          }
        },
        title = {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.swap_configure),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.swap_virtual_memory),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
            ) {
              Row(
                  modifier = Modifier.padding(12.dp),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.swap_set_size_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
              }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Bottom,
              ) {
                Text(
                    text = stringResource(R.string.swap_size),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                  val gb = tempSwap / 1024f
                  Text(
                      text =
                          if (tempSwap.toInt() == 0) "Disabled"
                          else String.format(Locale.US, "%.1f GB", gb),
                      style = MaterialTheme.typography.labelLarge,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onPrimaryContainer,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                  )
                }
              }

              LinearProgressIndicator(
                  progress = { tempSwap / 16384f },
                  modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                  color = MaterialTheme.colorScheme.secondary,
                  trackColor = MaterialTheme.colorScheme.surfaceVariant,
              )

              Slider(
                  value = tempSwap,
                  onValueChange = { tempSwap = it },
                  valueRange = 0f..16384f,
                  steps = 31,
                  colors =
                      SliderDefaults.colors(
                          thumbColor = MaterialTheme.colorScheme.secondary,
                          activeTrackColor = MaterialTheme.colorScheme.secondary,
                      ),
              )
            }
          }
        },
        confirmButton = {
          Button(
              enabled = !isApplyingSwap,
              onClick = {
                showSwapDialog = false
                isApplyingSwap = true
                showSwapApplyingDialog = true
                swapLogs = listOf()

                viewModel.setSwapWithLiveLog(
                    sizeMb = tempSwap.toInt(),
                    onLog = { log -> swapLogs = swapLogs + log },
                    onComplete = { success ->
                      swapSize = tempSwap
                      pushConfig(sp = tempSwap.toInt())
                      isApplyingSwap = false
                      showSwapApplyingDialog = false
                      resultSuccess = success
                      val gb = tempSwap / 1024f
                      resultMessage =
                          if (success) "Swap applied: ${String.format(Locale.US, "%.1f GB", gb)}"
                          else "Failed to apply swap"
                      showResultDialog = true
                    },
                )
              },
          ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Apply Changes")
          }
        },
        dismissButton = {
          TextButton(onClick = { if (!isApplyingSwap) showSwapDialog = false }) { Text("Cancel") }
        },
    )
  }

  // ===== ENHANCED ZRAM LIVE LOG DIALOG =====
  if (showZramApplyingDialog) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        icon = {
          Box(
              modifier =
                  Modifier.size(56.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary,
            )
          }
        },
        title = {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.liquid_ram_applying_zram),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.liquid_ram_please_wait),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
        text = {
          Column(modifier = Modifier.height(300.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                shape = RoundedCornerShape(12.dp),
            ) {
              Column(modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                if (zramLogs.isEmpty()) {
                  Text(
                      text = stringResource(R.string.liquid_ram_starting_zram_config),
                      fontFamily = FontFamily.Monospace,
                      fontSize = 12.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                } else {
                  zramLogs.forEach { log ->
                    Text(
                        text = "$ $log",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                  }
                }
              }
            }
          }
        },
    )
  }

  // ===== ENHANCED SWAP LIVE LOG DIALOG =====
  if (showSwapApplyingDialog) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        icon = {
          Box(
              modifier =
                  Modifier.size(56.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary,
            )
          }
        },
        title = {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.liquid_ram_applying_swap),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.liquid_ram_please_wait),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
        text = {
          Column(modifier = Modifier.height(300.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                shape = RoundedCornerShape(12.dp),
            ) {
              Column(modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                if (swapLogs.isEmpty()) {
                  Text(
                      text = stringResource(R.string.liquid_ram_starting_swap_config),
                      fontFamily = FontFamily.Monospace,
                      fontSize = 12.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                } else {
                  swapLogs.forEach { log ->
                    Text(
                        text = "$ $log",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                  }
                }
              }
            }
          }
        },
    )
  }

  // ===== ENHANCED RESULT DIALOG =====
  if (showResultDialog) {
    AlertDialog(
        onDismissRequest = { showResultDialog = false },
        icon = {
          Box(
              modifier =
                  Modifier.size(56.dp)
                      .clip(CircleShape)
                      .background(
                          if (resultSuccess) MaterialTheme.colorScheme.primaryContainer
                          else MaterialTheme.colorScheme.errorContainer
                      ),
              contentAlignment = Alignment.Center,
          ) {
            Icon(
                imageVector = if (resultSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint =
                    if (resultSuccess) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp),
            )
          }
        },
        title = {
          Text(
              text = if (resultSuccess) "Success" else "Failed",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
          )
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            if (resultSuccess)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
            ) {
              Text(
                  text = resultMessage,
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(12.dp),
              )
            }
          }
        },
        confirmButton = { Button(onClick = { showResultDialog = false }) { Text("OK") } },
    )
  }
}
