package id.xms.xtrakernelmanager.ui.screens.tuning.liquid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.*
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
import id.xms.xtrakernelmanager.data.model.ThermalPolicyPresets
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.LottieSwitchControlled
import id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialog
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialogButton
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidThermalControl(viewModel: TuningViewModel) {
  val prefsThermal by
      viewModel.preferencesManager.getThermalPreset().collectAsState(initial = "Not Set")
  val prefsOnBoot by
      viewModel.preferencesManager.getThermalSetOnBoot().collectAsState(initial = false)
  val selectedThermalPolicy by viewModel.getCpuLockThermalPolicy().collectAsState(initial = "Policy B (Balanced)")

  var expanded by remember { mutableStateOf(false) }
  var showDialog by remember { mutableStateOf(false) }
  var showThermalPolicySection by remember { mutableStateOf(false) }
  var showThermalPolicyDialog by remember { mutableStateOf(false) }

  // Box container with WavyBlobOrnament background
  Box(modifier = Modifier.fillMaxSize()) {
    // Background Layer
    WavyBlobOrnament(
        modifier = Modifier.fillMaxSize()
    )
    
    // Foreground Layer
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
          GlassmorphicCard(
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
              IconButton(onClick = { /* Handle back navigation if needed */ }) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                )
              }
              Text(
                  text = stringResource(R.string.thermal_control),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
              )
              // Spacer to balance the layout
              Spacer(modifier = Modifier.width(48.dp))
            }
          }
        }
    ) { paddingValues ->
      LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
      ) {
        // System Thermal Index Section
        item {
          GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
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
                                              MaterialTheme.colorScheme.error,
                                              MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                          )
                                  )
                              ),
                      contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(28.dp),
                    )
                  }

                  Column {
                    Text(
                        text = stringResource(R.string.thermal_control),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.thermal_control_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }

                // Dropdown button
                Box(
                    modifier =
                        Modifier.size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                            .clickable { expanded = !expanded },
                    contentAlignment = Alignment.Center,
                ) {
                  Icon(
                      imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                      contentDescription = if (expanded) "Collapse" else "Expand",
                      modifier = Modifier.size(32.dp),
                      tint = MaterialTheme.colorScheme.error,
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
                  // Thermal Index System Card
                  SystemThermalIndexCard(
                    prefsThermal = prefsThermal,
                    prefsOnBoot = prefsOnBoot,
                    onShowDialog = { showDialog = true },
                    viewModel = viewModel
                  )
                }
              }
            }
          }
        }
        
        // Smart Thermal Policy Section
        item {
          GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
              // Header
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
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp),
                    )
                  }

                  Column {
                    Text(
                        text = stringResource(R.string.smart_thermal_policy),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.smart_thermal_policy_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }

                Box(
                    modifier =
                        Modifier.size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            .clickable { showThermalPolicySection = !showThermalPolicySection },
                    contentAlignment = Alignment.Center,
                ) {
                  Icon(
                      imageVector = if (showThermalPolicySection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                      contentDescription = if (showThermalPolicySection) "Collapse" else "Expand",
                      modifier = Modifier.size(32.dp),
                      tint = MaterialTheme.colorScheme.primary,
                  )
                }
              }

              // Collapsible Thermal Policy Content
              AnimatedVisibility(
                  visible = showThermalPolicySection,
                  enter = fadeIn() + expandVertically(),
                  exit = fadeOut() + shrinkVertically(),
              ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                  // Current Policy Display
                  Card(
                      modifier = Modifier.fillMaxWidth(),
                      colors = CardDefaults.cardColors(
                          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                      )
                  ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.current_policy),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedThermalPolicy,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                      }
                      
                      val currentPolicy = remember(selectedThermalPolicy) { 
                          ThermalPolicyPresets.getPolicyByName(selectedThermalPolicy) 
                      }
                      currentPolicy?.let { policy ->
                        Column(horizontalAlignment = Alignment.End) {
                          Text(
                              text = "${policy.emergencyThreshold}°C",
                              style = MaterialTheme.typography.bodySmall,
                              color = MaterialTheme.colorScheme.error,
                              fontWeight = FontWeight.Medium
                          )
                          Text(
                              text = stringResource(R.string.emergency_temp),
                              style = MaterialTheme.typography.labelSmall,
                              color = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        }
                      }
                    }
                  }
                  
                  // Change Policy Button
                  Button(
                      onClick = { 
                          showThermalPolicyDialog = true
                      },
                      modifier = Modifier.fillMaxWidth(),
                      shape = RoundedCornerShape(12.dp)
                  ) {
                      Icon(
                          imageVector = Icons.Default.Tune,
                          contentDescription = null,
                          modifier = Modifier.size(20.dp)
                      )
                      Spacer(modifier = Modifier.width(8.dp))
                      Text("Change Thermal Policy")
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // ===== THERMAL INDEX DIALOG =====
  if (showDialog) {
    SystemThermalIndexDialog(
      prefsThermal = prefsThermal,
      prefsOnBoot = prefsOnBoot,
      onDismiss = { showDialog = false },
      viewModel = viewModel
    )
  }
  
  // ===== THERMAL POLICY DIALOG =====
  if (showThermalPolicyDialog) {
    ThermalPolicySelectionDialog(
      selectedPolicy = selectedThermalPolicy,
      onPolicySelected = { policyName ->
        viewModel.setCpuLockThermalPolicy(policyName)
        showThermalPolicyDialog = false
      },
      onDismiss = { showThermalPolicyDialog = false }
    )
  }
}

@Composable
private fun SystemThermalIndexCard(
    prefsThermal: String,
    prefsOnBoot: Boolean,
    onShowDialog: () -> Unit,
    viewModel: TuningViewModel
) {
  val presetMap =
      mapOf(
          "Not Set" to R.string.thermal_not_set,
          "Class 0" to R.string.thermal_class_0,
          "Extreme" to R.string.thermal_extreme,
          "Dynamic" to R.string.thermal_dynamic,
          "Incalls" to R.string.thermal_incalls,
          "Thermal 20" to R.string.thermal_20,
      )

  Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
  ) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Header dengan badge di bawah
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(20.dp),
          )
          Text(
              text = "Thermal Index System",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.error,
          )
        }

        // Badge current preset di bawah title
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.errorContainer,
        ) {
          Text(
              text = stringResource(presetMap[prefsThermal] ?: R.string.thermal_not_set),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      // Card untuk ganti preset
      Card(
          modifier = Modifier.fillMaxWidth().clickable { onShowDialog() },
          colors =
              CardDefaults.cardColors(
                  containerColor =
                      MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
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
                        .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center,
            ) {
              Icon(
                  imageVector =
                      when (prefsThermal) {
                        "Class 0" -> Icons.Default.Speed
                        "Extreme" -> Icons.Default.Whatshot
                        "Dynamic" -> Icons.Default.AutoMode
                        "Incalls" -> Icons.Default.Call
                        "Thermal 20" -> Icons.Default.LocalFireDepartment
                        else -> Icons.Default.Block
                      },
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onError,
                  modifier = Modifier.size(20.dp),
              )
            }
            Column {
              Text(
                  text =
                      stringResource(presetMap[prefsThermal] ?: R.string.thermal_not_set),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
              )
              Text(
                  text = stringResource(R.string.thermal_tap_to_change),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(24.dp),
          )
        }
      }

      // Set on Boot
      Card(
          colors =
              CardDefaults.cardColors(
                  containerColor =
                      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Column {
              Text(
                  text = stringResource(R.string.set_on_boot),
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Medium,
              )
              Text(
                  text = "Apply on device startup",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          LottieSwitchControlled(
              checked = prefsOnBoot,
              onCheckedChange = { viewModel.setThermalPreset(prefsThermal, it) },
              width = 80.dp,
              height = 40.dp,
              scale = 2.2f,
          )
        }
      }
      
      // Change Thermal Index Button
      Button(
          onClick = onShowDialog,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
      ) {
          Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = null,
              modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Change Thermal Index")
      }
    }
  }
}

@Composable
private fun SystemThermalIndexDialog(
    prefsThermal: String,
    prefsOnBoot: Boolean,
    onDismiss: () -> Unit,
    viewModel: TuningViewModel
) {
  val presetMap =
      mapOf(
          "Not Set" to R.string.thermal_not_set,
          "Class 0" to R.string.thermal_class_0,
          "Extreme" to R.string.thermal_extreme,
          "Dynamic" to R.string.thermal_dynamic,
          "Incalls" to R.string.thermal_incalls,
          "Thermal 20" to R.string.thermal_20,
      )

  @Composable
  fun getPresetDescription(preset: String): String {
    return when (preset) {
      "Not Set" -> stringResource(R.string.thermal_not_set_desc)
      "Class 0" -> stringResource(R.string.thermal_class_0_desc)
      "Extreme" -> stringResource(R.string.thermal_extreme_desc)
      "Dynamic" -> stringResource(R.string.thermal_dynamic_desc)
      "Incalls" -> stringResource(R.string.thermal_incalls_desc)
      "Thermal 20" -> stringResource(R.string.thermal_20_desc)
      else -> stringResource(R.string.thermal_unknown_desc)
    }
  }

  LiquidDialog(
      onDismissRequest = onDismiss,
      title = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
              modifier =
                  Modifier.size(56.dp)
                      .clip(CircleShape)
                      .background(
                          Brush.linearGradient(
                              colors =
                                  listOf(
                                      MaterialTheme.colorScheme.error,
                                      MaterialTheme.colorScheme.errorContainer,
                                  )
                          )
                      ),
              contentAlignment = Alignment.Center,
          ) {
            Icon(
                imageVector = Icons.Default.Thermostat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(32.dp),
            )
          }
          
          Text(
              text = "Select Thermal Index",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
          )
          Text(
              text = "Choose thermal management mode",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      },
      content = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          items(presetMap.size) { index ->
            val (preset, stringRes) = presetMap.entries.elementAt(index)
            val isSelected = preset == prefsThermal

            Card(
                modifier =
                    Modifier.fillMaxWidth().clickable {
                      viewModel.setThermalPreset(preset, prefsOnBoot)
                      onDismiss()
                    },
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            if (isSelected) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                  Box(
                      modifier =
                          Modifier.size(40.dp)
                              .clip(CircleShape)
                              .background(
                                  if (isSelected) MaterialTheme.colorScheme.error
                                  else MaterialTheme.colorScheme.surfaceVariant
                              ),
                      contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                        imageVector =
                            when (preset) {
                              "Class 0" -> Icons.Default.Speed
                              "Extreme" -> Icons.Default.Whatshot
                              "Dynamic" -> Icons.Default.AutoMode
                              "Incalls" -> Icons.Default.Call
                              "Thermal 20" -> Icons.Default.LocalFireDepartment
                              else -> Icons.Default.Block
                            },
                        contentDescription = null,
                        tint =
                            if (isSelected) MaterialTheme.colorScheme.onError
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                  }

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(stringRes),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                    Text(
                        text = getPresetDescription(preset),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }

                if (isSelected) {
                  Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.error,
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
        LiquidDialogButton(
            text = stringResource(R.string.cancel),
            onClick = onDismiss,
            isPrimary = false
        )
      },
  )
}

@Composable
private fun ThermalPolicySelectionDialog(
    selectedPolicy: String,
    onPolicySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val allPolicies = remember { ThermalPolicyPresets.getAllPolicies() }
    
    LiquidDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Text(
                    text = stringResource(R.string.select_thermal_policy),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.choose_thermal_behavior),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        content = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allPolicies.size) { index ->
                    val policy = allPolicies[index]
                    val isSelected = policy.name == selectedPolicy
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPolicySelected(policy.name) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isSelected) 3.dp else 1.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = policy.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "Emergency: ${policy.emergencyThreshold}°C, Warning: ${policy.warningThreshold}°C",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
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