package id.xms.xtrakernelmanager.ui.screens.tuning.liquid

import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard

@Composable
fun LiquidAdditionalControl(viewModel: TuningViewModel) {
  val ioSchedulers by viewModel.availableIOSchedulers.collectAsState()
  val tcpCongestion by viewModel.availableTCPCongestion.collectAsState()

  val currentIO by viewModel.currentIOScheduler.collectAsState()
  val currentTCP by viewModel.currentTCPCongestion.collectAsState()
  val currentPerfMode by viewModel.currentPerfMode.collectAsState() // FIXED: dari ViewModel

  var expanded by remember { mutableStateOf(false) }
  var showIODialog by remember { mutableStateOf(false) }
  var showTCPDialog by remember { mutableStateOf(false) }

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
                                      MaterialTheme.colorScheme.secondary,
                                      MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                                  )
                          )
                      ),
              contentAlignment = Alignment.Center,
          ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(28.dp),
            )
          }

          Column {
            Text(
                text = stringResource(R.string.additional_control),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.additional_control_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Box(
            modifier =
                Modifier.size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                    .clickable { expanded = !expanded },
            contentAlignment = Alignment.Center,
        ) {
          Icon(
              imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
              contentDescription = if (expanded) "Collapse" else "Expand",
              modifier = Modifier.size(32.dp),
              tint = MaterialTheme.colorScheme.secondary,
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
          // I/O Scheduler Card
          if (ioSchedulers.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors =
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.io_scheduler),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                  }

                  Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = MaterialTheme.colorScheme.secondaryContainer,
                  ) {
                    Text(
                        text = currentIO.ifEmpty { stringResource(R.string.not_set) },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                  }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showIODialog = true },
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
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
                                  .background(MaterialTheme.colorScheme.secondary),
                          contentAlignment = Alignment.Center,
                      ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                      }
                      Column {
                        Text(
                            text =
                                currentIO.ifEmpty { stringResource(R.string.io_scheduler_select) },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.tap_to_change),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                      }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp),
                    )
                  }
                }
              }
            }
          }

          // TCP Congestion Card
          if (tcpCongestion.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors =
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.tcp_congestion),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                  }

                  Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = MaterialTheme.colorScheme.secondaryContainer,
                  ) {
                    Text(
                        text = currentTCP.ifEmpty { stringResource(R.string.not_set) },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                  }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showTCPDialog = true },
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
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
                                  .background(MaterialTheme.colorScheme.secondary),
                          contentAlignment = Alignment.Center,
                      ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                      }
                      Column {
                        Text(
                            text =
                                currentTCP.ifEmpty {
                                  stringResource(R.string.tcp_algorithm_select)
                                },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.tap_to_change),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                      }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp),
                    )
                  }
                }
              }
            }
          }
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
                      imageVector = Icons.Default.Speed,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.secondary,
                      modifier = Modifier.size(20.dp),
                  )
                  Text(
                      text = stringResource(R.string.perf_controller),
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.secondary,
                  )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                  Text(
                      text =
                          when (currentPerfMode) { // FIXED: Gunakan state dari ViewModel
                            "battery" -> stringResource(R.string.perf_battery)
                            "balance" -> stringResource(R.string.perf_balance)
                            "performance" -> stringResource(R.string.perf_performance)
                            else -> stringResource(R.string.perf_balance)
                          },
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSecondaryContainer,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  )
                }
              }

              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

              // Vertical Layout untuk Button
              Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                // Battery Button
                Card(
                    modifier =
                        Modifier.fillMaxWidth().clickable { viewModel.setPerfMode("battery") },
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                if (currentPerfMode == "battery") // FIXED
                                 MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                  Row(
                      modifier = Modifier.fillMaxWidth().padding(16.dp),
                      horizontalArrangement = Arrangement.spacedBy(12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Box(
                        modifier =
                            Modifier.size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (currentPerfMode == "battery") // FIXED
                                     MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                      Icon(
                          imageVector = Icons.Default.BatteryFull,
                          contentDescription = null,
                          tint =
                              if (currentPerfMode == "battery") // FIXED
                               MaterialTheme.colorScheme.onSecondary
                              else MaterialTheme.colorScheme.onSurfaceVariant,
                          modifier = Modifier.size(20.dp),
                      )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                          text = stringResource(R.string.perf_battery),
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight =
                              if (currentPerfMode == "battery") FontWeight.Bold
                              else FontWeight.Normal, // FIXED
                      )
                      Text(
                          text = "Power saving mode",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                    if (currentPerfMode == "battery") { // FIXED
                      Icon(
                          imageVector = Icons.Default.CheckCircle,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.secondary,
                          modifier = Modifier.size(24.dp),
                      )
                    }
                  }
                }

                // Balance Button
                Card(
                    modifier =
                        Modifier.fillMaxWidth().clickable { viewModel.setPerfMode("balance") },
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                if (currentPerfMode == "balance") // FIXED
                                 MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                  Row(
                      modifier = Modifier.fillMaxWidth().padding(16.dp),
                      horizontalArrangement = Arrangement.spacedBy(12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Box(
                        modifier =
                            Modifier.size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (currentPerfMode == "balance") // FIXED
                                     MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                      Icon(
                          imageVector = Icons.Default.Balance,
                          contentDescription = null,
                          tint =
                              if (currentPerfMode == "balance") // FIXED
                               MaterialTheme.colorScheme.onSecondary
                              else MaterialTheme.colorScheme.onSurfaceVariant,
                          modifier = Modifier.size(20.dp),
                      )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                          text = stringResource(R.string.perf_balance),
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight =
                              if (currentPerfMode == "balance") FontWeight.Bold
                              else FontWeight.Normal, // FIXED
                      )
                      Text(
                          text = "Balanced performance",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                    if (currentPerfMode == "balance") { // FIXED
                      Icon(
                          imageVector = Icons.Default.CheckCircle,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.secondary,
                          modifier = Modifier.size(24.dp),
                      )
                    }
                  }
                }

                // Performance Button
                Card(
                    modifier =
                        Modifier.fillMaxWidth().clickable { viewModel.setPerfMode("performance") },
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                if (currentPerfMode == "performance") // FIXED
                                 MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                  Row(
                      modifier = Modifier.fillMaxWidth().padding(16.dp),
                      horizontalArrangement = Arrangement.spacedBy(12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Box(
                        modifier =
                            Modifier.size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (currentPerfMode == "performance") // FIXED
                                     MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                      Icon(
                          imageVector = Icons.Default.Speed,
                          contentDescription = null,
                          tint =
                              if (currentPerfMode == "performance") // FIXED
                               MaterialTheme.colorScheme.onSecondary
                              else MaterialTheme.colorScheme.onSurfaceVariant,
                          modifier = Modifier.size(20.dp),
                      )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                          text = stringResource(R.string.perf_performance),
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight =
                              if (currentPerfMode == "performance") FontWeight.Bold
                              else FontWeight.Normal, // FIXED
                      )
                      Text(
                          text = "Maximum performance",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                    if (currentPerfMode == "performance") { // FIXED
                      Icon(
                          imageVector = Icons.Default.CheckCircle,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.secondary,
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
  }

  // ===== I/O SCHEDULER DIALOG =====
  if (showIODialog) {
    AlertDialog(
        onDismissRequest = { showIODialog = false },
        icon = {
          Box(
              modifier =
                  Modifier.size(56.dp)
                      .clip(CircleShape)
                      .background(
                          Brush.linearGradient(
                              colors =
                                  listOf(
                                      MaterialTheme.colorScheme.secondary,
                                      MaterialTheme.colorScheme.secondaryContainer,
                                  )
                          )
                      ),
              contentAlignment = Alignment.Center,
          ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(32.dp),
            )
          }
        },
        title = {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Select I/O Scheduler",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Choose disk scheduling algorithm",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ioSchedulers.forEach { scheduler ->
              val isSelected = scheduler == currentIO

              Card(
                  modifier =
                      Modifier.fillMaxWidth().clickable {
                        viewModel.setIOScheduler(scheduler)
                        showIODialog = false
                      },
                  colors =
                      CardDefaults.cardColors(
                          containerColor =
                              if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                              else MaterialTheme.colorScheme.surface
                      ),
                  elevation =
                      CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
                  shape = RoundedCornerShape(12.dp),
              ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                      text = scheduler,
                      style = MaterialTheme.typography.bodyLarge,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color =
                          if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                          else MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.weight(1f),
                  )

                  if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp),
                    )
                  }
                }
              }
            }
          }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = { showIODialog = false }) { Text("Close") } },
    )
  }

  // ===== TCP CONGESTION DIALOG =====
  if (showTCPDialog) {
    AlertDialog(
        onDismissRequest = { showTCPDialog = false },
        icon = {
          Box(
              modifier =
                  Modifier.size(56.dp)
                      .clip(CircleShape)
                      .background(
                          Brush.linearGradient(
                              colors =
                                  listOf(
                                      MaterialTheme.colorScheme.secondary,
                                      MaterialTheme.colorScheme.secondaryContainer,
                                  )
                          )
                      ),
              contentAlignment = Alignment.Center,
          ) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(32.dp),
            )
          }
        },
        title = {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Select TCP Algorithm",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Choose network congestion control",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tcpCongestion.forEach { algorithm ->
              val isSelected = algorithm == currentTCP

              Card(
                  modifier =
                      Modifier.fillMaxWidth().clickable {
                        viewModel.setTCPCongestion(algorithm)
                        showTCPDialog = false
                      },
                  colors =
                      CardDefaults.cardColors(
                          containerColor =
                              if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                              else MaterialTheme.colorScheme.surface
                      ),
                  elevation =
                      CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
                  shape = RoundedCornerShape(12.dp),
              ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                      text = algorithm,
                      style = MaterialTheme.typography.bodyLarge,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color =
                          if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                          else MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.weight(1f),
                  )

                  if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp),
                    )
                  }
                }
              }
            }
          }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = { showTCPDialog = false }) { Text("Close") } },
    )
  }
}
