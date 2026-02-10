package id.xms.xtrakernelmanager.ui.screens.functionalrom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.LottieSwitchControlled
import id.xms.xtrakernelmanager.ui.components.PillCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import id.xms.xtrakernelmanager.data.model.HideAccessibilityTab
import id.xms.xtrakernelmanager.data.model.HideAccessibilityConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunctionalRomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShimokuRom: () -> Unit = {},
    onNavigateToHideAccessibility: () -> Unit = {},
    viewModel: FunctionalRomViewModel,
) {
  val uiState by viewModel.uiState.collectAsState()

  // Loading state
  if (uiState.isLoading) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(R.string.loading_features),
            style = MaterialTheme.typography.bodyMedium,
        )
      }
    }
    return
  }

  LazyColumn(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Header with PillCard
    item {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          FilledIconButton(
              onClick = onNavigateBack,
              colors =
                  IconButtonDefaults.filledIconButtonColors(
                      containerColor = MaterialTheme.colorScheme.surfaceVariant,
                      contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  ),
              modifier = Modifier.size(40.dp),
          ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp),
            )
          }
          PillCard(text = stringResource(R.string.functional_rom_title))
        }
      }
    }

    // ROM Information Card
    item {
      uiState.romInfo?.let { romInfo ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(
            containerColor = if (romInfo.isShimokuRom) 
              MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surfaceVariant
          )
        ) {
          Column(
            modifier = Modifier.padding(16.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = if (romInfo.isShimokuRom) Icons.Default.Verified else Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = if (romInfo.isShimokuRom) 
                  MaterialTheme.colorScheme.onPrimaryContainer 
                else MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = romInfo.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Android ${romInfo.androidVersion} • ${romInfo.systemBrand}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Universal Features
    item { 
      Spacer(modifier = Modifier.height(8.dp))
      CategoryHeader(title = "Universal Features") 
    }
    item {
      DeveloperOptionsCard(viewModel = viewModel)
    }
    item {
      DPIChangerCard()
    }
    item {
      ClickableFeatureCard(
          title = "Hide Accessibility Service",
          description = "Sistem tab untuk menyembunyikan aplikasi dari deteksi aksesibilitas (Universal - bekerja di semua ROM)",
          icon = Icons.Default.VisibilityOff,
          onClick = onNavigateToHideAccessibility,
          enabled = true, // Always enabled - universal feature
          statusText = if (uiState.hideAccessibilityConfig.isEnabled) {
            "${uiState.hideAccessibilityConfig.currentTab.displayName} • ${getTotalSelectedApps(uiState.hideAccessibilityConfig)} apps"
          } else "Disabled"
      )
    }

    // ROM-Specific Features
    item { 
      Spacer(modifier = Modifier.height(8.dp))
      CategoryHeader(title = "ROM-Specific Features") 
    }
    item {
      ClickableFeatureCard(
          title = "Shimoku ROM Features",
          description = "Fitur khusus untuk Shimoku ROM termasuk Play Integrity, Touch Settings, dan Native Features",
          icon = Icons.Default.Verified,
          onClick = onNavigateToShimokuRom,
          enabled = true, // Always accessible to show lock message for non-Shimoku ROMs
          statusText = if (uiState.isShimokuRom) "Available" else "Locked"
      )
    }
  }
}

@Composable
private fun CategoryHeader(title: String) {
  Text(
      text = title,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(horizontal = 4.dp)
  )
}

@Composable
private fun ClickableFeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    statusText: String = ""
) {
  Card(
      modifier = Modifier
          .fillMaxWidth()
          .alpha(if (enabled) 1f else 0.6f),
      onClick = if (enabled) onClick else { {} }
  ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (statusText.isNotEmpty()) {
          Text(
              text = statusText,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary
          )
        }
      }
      Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

private fun getTotalSelectedApps(config: HideAccessibilityConfig): Int {
  return config.appsToHide.size + config.detectorApps.size
}

@Composable
private fun DPIChangerCard() {
  val context = androidx.compose.ui.platform.LocalContext.current
  val scope = rememberCoroutineScope()
  var showDPIDialog by remember { mutableStateOf(false) }
  
  // Get current DPI and smallest width
  val currentDPI = remember {
    context.resources.displayMetrics.densityDpi
  }
  
  val currentSmallestWidth = remember {
    context.resources.configuration.smallestScreenWidthDp
  }
  
  Card(
      modifier = Modifier.fillMaxWidth(),
      onClick = { showDPIDialog = true }
  ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Icon(
          imageVector = Icons.Default.PhoneAndroid,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = "Display Size Changer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Adjust UI size (same as Developer Options > Smallest width)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Current: $currentSmallestWidth dp",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
      }
      Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
  
  if (showDPIDialog) {
    DPIChangerDialog(
        currentDPI = currentDPI,
        onDismiss = { showDPIDialog = false }
    )
  }
}

@Composable
private fun DPIChangerDialog(
    currentDPI: Int,
    onDismiss: () -> Unit
) {
  val context = androidx.compose.ui.platform.LocalContext.current
  val scope = rememberCoroutineScope()
  
  // Get current smallest width in dp
  val currentSmallestWidth = remember {
    val config = context.resources.configuration
    config.smallestScreenWidthDp
  }
  
  var selectedWidth by remember { mutableStateOf(currentSmallestWidth) }
  var customWidth by remember { mutableStateOf(currentSmallestWidth.toString()) }
  var isApplying by remember { mutableStateOf(false) }
  var useCustom by remember { mutableStateOf(false) }
  
  // Preset smallest width values (in dp)
  val presetWidths = listOf(
      320 to "320 dp (Large UI)",
      360 to "360 dp (Default)",
      411 to "411 dp (Compact)",
      480 to "480 dp (More Compact)",
      540 to "540 dp (Very Compact)",
      600 to "600 dp (Tablet-like)"
  )
  
  AlertDialog(
      onDismissRequest = onDismiss,
      title = {
        Column {
          Text(
              text = "Display Size Changer",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold
          )
          Text(
              text = "Current: $currentSmallestWidth dp (DPI: $currentDPI)",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      text = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
              text = "Select smallest width (dp):",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium
          )
          
          // Info card
          Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.secondaryContainer
              )
          ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                  text = "💡 How it works:",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSecondaryContainer
              )
              Text(
                  text = "• Lower value = Larger UI (easier to tap)",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer
              )
              Text(
                  text = "• Higher value = Smaller UI (more content)",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer
              )
              Text(
                  text = "Same as Developer Options > Smallest width",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                  fontWeight = FontWeight.Medium
              )
            }
          }
          
          // Preset width options
          Column(
              modifier = Modifier.selectableGroup(),
              verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            presetWidths.forEach { (width, label) ->
              Row(
                  modifier = Modifier
                      .fillMaxWidth()
                      .selectable(
                          selected = !useCustom && selectedWidth == width,
                          onClick = {
                            useCustom = false
                            selectedWidth = width
                          },
                          role = Role.RadioButton
                      )
                      .padding(vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                    selected = !useCustom && selectedWidth == width,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium
                )
              }
            }
            
            // Custom width option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = useCustom,
                        onClick = { useCustom = true },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                  selected = useCustom,
                  onClick = null
              )
              Spacer(modifier = Modifier.width(8.dp))
              OutlinedTextField(
                  value = customWidth,
                  onValueChange = { 
                    customWidth = it.filter { char -> char.isDigit() }
                    useCustom = true
                  },
                  label = { Text("Custom (dp)") },
                  modifier = Modifier.weight(1f),
                  singleLine = true,
                  keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                      keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                  )
              )
            }
          }
          
          // Warning card
          Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer
              )
          ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                  text = "Changes require root and apply immediately",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
            onClick = {
              scope.launch {
                isApplying = true
                try {
                  val targetWidth = if (useCustom) {
                    customWidth.toIntOrNull() ?: selectedWidth
                  } else {
                    selectedWidth
                  }
                  
                  if (targetWidth < 200 || targetWidth > 800) {
                    android.widget.Toast.makeText(
                        context,
                        "Width must be between 200-800 dp",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    isApplying = false
                    return@launch
                  }
                  
                  // Apply smallest width by calculating and setting density
                  // Formula: density = (smallest_physical_pixels / target_dp) * 160
                  withContext(Dispatchers.IO) {
                    // Get screen dimensions
                    val displayMetrics = context.resources.displayMetrics
                    val widthPixels = displayMetrics.widthPixels
                    val heightPixels = displayMetrics.heightPixels
                    val smallestPixels = minOf(widthPixels, heightPixels)
                    
                    // Calculate new density for target smallest width
                    val newDensity = (smallestPixels.toFloat() / targetWidth.toFloat() * 160).toInt()
                    
                    // Apply the new density
                    id.xms.xtrakernelmanager.utils.RootShell.execute(
                        "wm density $newDensity"
                    )
                  }
                  
                  android.widget.Toast.makeText(
                      context,
                      "Display size changed to $targetWidth dp",
                      android.widget.Toast.LENGTH_SHORT
                  ).show()
                  onDismiss()
                } catch (e: Exception) {
                  android.widget.Toast.makeText(
                      context,
                      "Error: ${e.message}",
                      android.widget.Toast.LENGTH_SHORT
                  ).show()
                } finally {
                  isApplying = false
                }
              }
            },
            enabled = !isApplying
        ) {
          if (isApplying) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
          }
          Text(if (isApplying) "Applying..." else "Apply")
        }
      },
      dismissButton = {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          TextButton(
              onClick = {
                scope.launch {
                  try {
                    // Reset to default
                    withContext(Dispatchers.IO) {
                      id.xms.xtrakernelmanager.utils.RootShell.execute(
                          "wm density reset"
                      )
                    }
                    android.widget.Toast.makeText(
                        context,
                        "Display size reset to default",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    onDismiss()
                  } catch (e: Exception) {
                    android.widget.Toast.makeText(
                        context,
                        "Error: ${e.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                  }
                }
              },
              enabled = !isApplying
          ) {
            Text("Reset")
          }
          
          TextButton(
              onClick = onDismiss,
              enabled = !isApplying
          ) {
            Text("Cancel")
          }
        }
      }
  )
}

@Composable
private fun DeveloperOptionsCard(viewModel: FunctionalRomViewModel) {
  val context = androidx.compose.ui.platform.LocalContext.current
  val scope = rememberCoroutineScope()
  var isDeveloperEnabled by remember { mutableStateOf(
    android.provider.Settings.Global.getInt(
      context.contentResolver,
      android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
      0
    ) == 1
  ) }
  
  Card(
      modifier = Modifier.fillMaxWidth(),
      onClick = {
        if (isDeveloperEnabled) {
          try {
            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            context.startActivity(intent)
          } catch (e: Exception) {
            android.widget.Toast.makeText(
              context,
              "Unable to open Developer Options",
              android.widget.Toast.LENGTH_SHORT
            ).show()
          }
        } else {
          scope.launch {
            try {
              withContext(Dispatchers.IO) {
                id.xms.xtrakernelmanager.utils.RootShell.execute(
                  "settings put global development_settings_enabled 1"
                )
              }
                isDeveloperEnabled = android.provider.Settings.Global.getInt(
                context.contentResolver,
                android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
              ) == 1
              
              if (isDeveloperEnabled) {
                android.widget.Toast.makeText(
                  context,
                  "Developer Options Enabled!",
                  android.widget.Toast.LENGTH_SHORT
                ).show()
                  try {
                  val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                  context.startActivity(intent)
                } catch (e: Exception) {
                  android.widget.Toast.makeText(
                    context,
                    "Developer Options enabled. Please find it in Settings.",
                    android.widget.Toast.LENGTH_SHORT
                  ).show()
                }
              } else {
                android.widget.Toast.makeText(
                  context,
                  "Failed to enable Developer Options. Root access required.",
                  android.widget.Toast.LENGTH_SHORT
                ).show()
              }
            } catch (e: Exception) {
              android.widget.Toast.makeText(
                context,
                "Error: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
              ).show()
            }
          }
        }
      }
  ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Icon(
          imageVector = Icons.Default.DeveloperMode,
          contentDescription = null,
          tint = if (isDeveloperEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = "Developer Options",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = if (isDeveloperEnabled) {
              "Developer options are enabled. Click to open settings."
            } else {
              "Click to enable developer options (requires root)"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (isDeveloperEnabled) "Enabled" else "Disabled",
            style = MaterialTheme.typography.bodySmall,
            color = if (isDeveloperEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
      }
      Icon(
          imageVector = if (isDeveloperEnabled) Icons.Default.ChevronRight else Icons.Default.Lock,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}