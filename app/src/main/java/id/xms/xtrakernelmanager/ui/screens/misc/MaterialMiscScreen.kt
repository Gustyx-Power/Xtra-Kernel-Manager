package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import id.xms.xtrakernelmanager.ui.components.WavySlider
import id.xms.xtrakernelmanager.ui.screens.home.components.SettingsSheet
import java.util.Locale
import kotlinx.coroutines.delay
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialMiscScreen(viewModel: MiscViewModel = viewModel(), onNavigate: (String) -> Unit = {}) {
  val context = LocalContext.current
  val batteryInfo by viewModel.batteryInfo.collectAsState()
  val isRooted by viewModel.isRootAvailable.collectAsState()

  // State for Sheets
  var showSettingsBottomSheet by remember { mutableStateOf(false) }
  @OptIn(ExperimentalMaterial3Api::class) val settingsSheetState = rememberModalBottomSheetState()

  // State for Card Expansion (Mutually Exclusive ideally, but enforced by visibility)
  var isGameSpaceExpanded by remember { mutableStateOf(false) }
  var isDisplayExpanded by remember { mutableStateOf(false) }

  // Load initial data
  LaunchedEffect(Unit) {
    viewModel.loadBatteryInfo(context)
    // Root check is already in ViewModel init
  }

  Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      topBar = { MaterialMiscHeader(onSettingsClick = { showSettingsBottomSheet = true }) },
  ) { paddingValues ->
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
        verticalItemSpacing = 16.dp,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
      // 1. Battery Hero Card
      item(span = StaggeredGridItemSpan.FullLine) {
        StaggeredEntry(delayMillis = 0) { MiscBatteryCard(batteryInfo) }
      }

      // 2. Display & Color (Left) - Hide if Game Space is Expanded
      if (!isGameSpaceExpanded) {
        item(
            span =
                if (isDisplayExpanded) StaggeredGridItemSpan.FullLine
                else StaggeredGridItemSpan.SingleLane
        ) {
          StaggeredEntry(delayMillis = 100) {
            DisplayColorCard(
                viewModel = viewModel,
                isRooted = isRooted,
                expanded = isDisplayExpanded,
                onExpandedChange = {
                  isDisplayExpanded = it
                  if (it) isGameSpaceExpanded = false // Safety
                },
            )
          }
        }
      }

      // 3. Game Space (Right) - Hide if Display is Expanded
      if (!isDisplayExpanded) {
        item(
            span =
                if (isGameSpaceExpanded) StaggeredGridItemSpan.FullLine
                else StaggeredGridItemSpan.SingleLane
        ) {
          StaggeredEntry(delayMillis = 200) {
            GameSpaceCard(
                viewModel = viewModel,
                expanded = isGameSpaceExpanded,
                onExpandedChange = {
                  isGameSpaceExpanded = it
                  if (it) isDisplayExpanded = false // Safety
                },
            )
          }
        }
      }

      // 4. Connectivity & Hostname (Full Width Group)
      item(span = StaggeredGridItemSpan.FullLine) {
        StaggeredEntry(delayMillis = 300) { ConnectivityGroup(viewModel) }
      }

      // 5. Functional ROM (Conditional)
      item(span = StaggeredGridItemSpan.FullLine) {
        StaggeredEntry(delayMillis = 400) { FunctionalRomCard(viewModel) }
      }
    }
  }

  // Settings Sheet
  if (showSettingsBottomSheet) {
    ModalBottomSheet(
        onDismissRequest = { showSettingsBottomSheet = false },
        sheetState = settingsSheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
      SettingsSheet(
          preferencesManager = viewModel.preferencesManager,
          onDismiss = { showSettingsBottomSheet = false },
      )
    }
  }
}

@Composable
fun MaterialMiscHeader(onSettingsClick: () -> Unit) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Column {
      Text(
          text = "Miscellaneous",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground,
      )
      Text(
          text = "Tools & Tweaks",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
      )
    }

    IconButton(
        onClick = onSettingsClick,
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
    ) {
      Icon(
          imageVector = Icons.Rounded.Settings,
          contentDescription = "Settings",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
fun MiscBatteryCard(batteryInfo: BatteryInfo) {
  Card(
      modifier =
          Modifier
              .fillMaxWidth(), // Removed fixed height, removed height(140.dp) to allow flexibility
      shape = RoundedCornerShape(32.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Background Watermark (Optional: Charging Bolt)
      if (batteryInfo.status.contains("Charging", ignoreCase = true)) {
        Icon(
            Icons.Rounded.Bolt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.05f),
            modifier = Modifier.size(120.dp).align(Alignment.CenterEnd).offset(x = 24.dp),
        )
      }

      Row(
          modifier = Modifier.fillMaxWidth().padding(24.dp), // Increased padding
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.BatteryStd,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Battery",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
          }

          Column {
            Text(
                text = "${batteryInfo.level}%",
                style = MaterialTheme.typography.displayMedium, // kept large
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = batteryInfo.status,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
          }
        }

        // Right Side Stats - Vertical Badge Stack
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End,
        ) {
          BatteryStatBadge(
              value = "${batteryInfo.temperature}°C",
              label = "Temp",
              icon = Icons.Rounded.Thermostat,
          )
          BatteryStatBadge(
              value = "${batteryInfo.currentNow}mA",
              label = "Current",
              icon = Icons.Rounded.ElectricBolt,
          )
        }
      }
    }
  }
}

@Composable
fun BatteryStatBadge(value: String, label: String, icon: ImageVector) {
  Surface(
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), // increased alpha
      shape = RoundedCornerShape(16.dp),
  ) {
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Column(horizontalAlignment = Alignment.End) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
      }
    }
  }
}

@Composable
fun DisplayColorCard(
    viewModel: MiscViewModel,
    isRooted: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
  // Local state for slider to prevent stutter, synced on expanded
  var sliderValue by remember { mutableFloatStateOf(1.0f) }

  Card(
      onClick = { onExpandedChange(!expanded) },
      modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp).animateContentSize(),
      shape = RoundedCornerShape(24.dp),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Background Watermark
      Icon(
          Icons.Rounded.Palette,
          null,
          tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.05f),
          modifier =
              Modifier.size(if (expanded) 240.dp else 100.dp)
                  .align(Alignment.BottomEnd)
                  .offset(x = 20.dp, y = 20.dp),
      )

      Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.SpaceBetween,
          horizontalAlignment = Alignment.Start,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
                Icons.Rounded.Palette,
                null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(28.dp),
            )

            if (expanded && isRooted) {
              // Reset Button or similar could go here if needed
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          Text(
              text = "Display",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
          )
          Text(
              text = "Colors & Saturation",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
          )
        }

        if (!expanded) {
          Spacer(modifier = Modifier.height(16.dp))
          // Previously gradient box, now nothing or just spacing
        } else {
          // Expanded Content
          if (!isRooted) {
            Text(
                text = "Root access required.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp),
            )
          } else {
            Column(modifier = Modifier.padding(top = 16.dp)) {
              HorizontalDivider(
                  modifier = Modifier.padding(bottom = 16.dp),
                  color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
              )

              Text(
                  "Saturation: ${String.format(Locale.US, "%.1f", sliderValue)}",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
              )

              WavySlider(
                  value = sliderValue,
                  onValueChange = { sliderValue = it },
                  onValueChangeFinished = { viewModel.setDisplaySaturation(sliderValue) },
                  valueRange = 0f..2.0f,
                  steps = 19,
              )

              // Presets Chips
              Row(
                  modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                val presets = listOf(0.0f to "Gray", 1.0f to "Std", 1.2f to "Vivid")
                presets.forEach { (valFloat, name) ->
                  SuggestionChip(
                      onClick = {
                        sliderValue = valFloat
                        viewModel.setDisplaySaturation(valFloat)
                      },
                      label = { Text(name) },
                      colors =
                          SuggestionChipDefaults.suggestionChipColors(
                              containerColor =
                                  if (sliderValue == valFloat)
                                      MaterialTheme.colorScheme.onSecondaryContainer
                                  else Color.Transparent,
                              labelColor =
                                  if (sliderValue == valFloat)
                                      MaterialTheme.colorScheme.secondaryContainer
                                  else MaterialTheme.colorScheme.onSecondaryContainer,
                          ),
                      border =
                          BorderStroke(
                              1.dp,
                              MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                          ),
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

@Composable
fun GameSpaceCard(
    viewModel: MiscViewModel,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
  val gameApps by viewModel.gameApps.collectAsState()
  val isServiceRunning by viewModel.enableGameOverlay.collectAsState()

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .heightIn(min = 140.dp) // Dynamic height
              .animateContentSize(),
      shape = RoundedCornerShape(24.dp),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
      onClick = { onExpandedChange(!expanded) },
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Background Watermark
      Icon(
          Icons.Rounded.Gamepad,
          null,
          tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.05f),
          modifier =
              Modifier.size(if (expanded) 240.dp else 120.dp)
                  .align(Alignment.CenterEnd)
                  .offset(x = 30.dp, y = 10.dp),
      )

      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
              Icons.Rounded.Gamepad,
              null,
              tint = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.size(28.dp),
          )

          // Toggle
          Switch(
              checked = isServiceRunning,
              onCheckedChange = { viewModel.setEnableGameOverlay(it) },
              colors =
                  SwitchDefaults.colors(
                      checkedThumbColor = MaterialTheme.colorScheme.tertiary,
                      checkedTrackColor =
                          MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f),
                      uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                      uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                  ),
              modifier = Modifier.scale(0.7f), // Smaller
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Game Space",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )

        if (!expanded) {
          Text(
              text =
                  "${try { JSONArray(gameApps).length() } catch(e: Exception) { 0 }} Apps active",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
          )
        } else {
          Column(modifier = Modifier.padding(top = 8.dp)) {
            Text(
                text = "Manage your games library for automatic optimization.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Button(
                onClick = { /* TODO: Open App Picker */ },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                shape = RoundedCornerShape(12.dp),
            ) {
              Text("Manage Library")
            }
          }
        }
      }
    }
  }
}

@Composable
fun ConnectivityGroup(viewModel: MiscViewModel) {
  val hostname by viewModel.currentHostname.collectAsState()
  val networkStatus by viewModel.networkStatus.collectAsState()
  var showHostnameDialog by remember { mutableStateOf(false) }

  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    // Hostname Strip
    Surface(
        onClick = { showHostnameDialog = true },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth().height(60.dp),
    ) {
      Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
            Icons.Rounded.Dns,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
              "Hostname",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
              hostname.ifEmpty { "android-xxxxxxxx" },
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
          )
        }
        Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(16.dp))
      }
    }

    // TCP & Network
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      Card(
          modifier = Modifier.weight(1f).height(100.dp),
          shape = RoundedCornerShape(20.dp),
          colors =
              CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      ) {
        Column(
            Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
          Icon(Icons.Rounded.Router, null, tint = MaterialTheme.colorScheme.secondary)
          Text(
              networkStatus,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
          )
        }
      }
      Card(
          modifier = Modifier.weight(1f).height(100.dp),
          shape = RoundedCornerShape(20.dp),
          colors =
              CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      ) {
        Column(
            Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
          Icon(Icons.Rounded.Security, null, tint = MaterialTheme.colorScheme.tertiary)
          Text(
              "Private DNS", // Value placeholder
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }

  // Hostname Dialog
  if (showHostnameDialog) {
    var input by remember { mutableStateOf(hostname) }
    AlertDialog(
        onDismissRequest = { showHostnameDialog = false },
        title = { Text("Set Hostname") },
        text = {
          OutlinedTextField(
              value = input,
              onValueChange = { input = it },
              label = { Text("Hostname") },
              singleLine = true,
          )
        },
        confirmButton = {
          Button(
              onClick = {
                viewModel.setHostname(input)
                showHostnameDialog = false
              }
          ) {
            Text("Save")
          }
        },
        dismissButton = { TextButton(onClick = { showHostnameDialog = false }) { Text("Cancel") } },
    )
  }
}

@Composable
fun FunctionalRomCard(viewModel: MiscViewModel) {
  Card(
      modifier = Modifier.fillMaxWidth().height(80.dp),
      shape = RoundedCornerShape(20.dp),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) // Damped red
          ),
  ) {
    Row(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Rounded.Extension, // Use Extension icon instead of missing drawable
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
          Text(
              "Functional ROM",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onErrorContainer,
          )
          Text(
              "VIP Feature",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
          )
        }
      }
      Icon(Icons.Rounded.Lock, null, tint = MaterialTheme.colorScheme.error)
    }
  }
}

@Composable
fun StaggeredEntry(delayMillis: Int, content: @Composable () -> Unit) {
  var visible by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(delayMillis.toLong())
    visible = true
  }

  AnimatedVisibility(
      visible = visible,
      enter =
          fadeIn(animationSpec = tween(500)) +
              slideInVertically(
                  animationSpec = tween(500, easing = FastOutSlowInEasing),
                  initialOffsetY = { 100 },
              ),
      exit = fadeOut(),
  ) {
    content()
  }
}
