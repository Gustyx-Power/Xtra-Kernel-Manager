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
import java.util.Locale
import kotlinx.coroutines.delay
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialMiscScreen(viewModel: MiscViewModel = viewModel(), onNavigate: (String) -> Unit = {}) {
  var showBatteryDetail by remember { mutableStateOf(false) }
  var showProcessManager by remember { mutableStateOf(false) }
  var showGameSpace by remember { mutableStateOf(false) }
  var showPerAppProfile by remember { mutableStateOf(false) }

  when {
    showBatteryDetail -> MaterialBatteryScreen(onBack = { showBatteryDetail = false })
    showProcessManager -> MaterialProcessManagerScreen(viewModel = viewModel, onBack = { showProcessManager = false })
    showGameSpace -> MaterialGameSpaceScreen(viewModel = viewModel, onBack = { showGameSpace = false })
    showPerAppProfile -> MaterialPerAppProfileScreen(viewModel = viewModel, onBack = { showPerAppProfile = false })
    else -> MaterialMiscScreenContent(
        viewModel,
        onNavigate,
        onBatteryDetailClick = { showBatteryDetail = true },
        onProcessManagerClick = { showProcessManager = true },
        onGameSpaceClick = { showGameSpace = true },
        onPerAppProfileClick = { showPerAppProfile = true },
    )
  }
}

@Composable
fun MaterialMiscScreenContent(
    viewModel: MiscViewModel,
    onNavigate: (String) -> Unit,
    onBatteryDetailClick: () -> Unit,
    onProcessManagerClick: () -> Unit,
    onGameSpaceClick: () -> Unit,
    onPerAppProfileClick: () -> Unit,
) {
  val context = LocalContext.current
  val batteryInfo by viewModel.batteryInfo.collectAsState()
  val isRooted by viewModel.isRootAvailable.collectAsState()

  // State for Card Expansion
  var isDisplayExpanded by remember { mutableStateOf(false) }
  var isSELinuxExpanded by remember { mutableStateOf(false) }

  // Load initial data
  LaunchedEffect(Unit) {
    viewModel.loadBatteryInfo(context)
    // Root check is already in ViewModel init
  }

  Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      topBar = { MaterialMiscHeader() },
  ) { paddingValues ->
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
        verticalItemSpacing = 16.dp,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
      // 1. Power Insight Card
      item(span = StaggeredGridItemSpan.FullLine) {
        StaggeredEntry(delayMillis = 0) {
          PowerInsightCard(viewModel, batteryInfo, onClick = onBatteryDetailClick)
        }
      }

      // 2. Game Space (Left) - Navigate to screen, hide when Display expanded
      if (!isDisplayExpanded) {
        item(span = StaggeredGridItemSpan.SingleLane) {
          StaggeredEntry(delayMillis = 100) {
            GameSpaceCard(
                viewModel = viewModel,
                onClick = onGameSpaceClick,
            )
          }
        }
      }

      // 3. Display & Color (Right) - Expandable
      item(
          span =
              if (isDisplayExpanded) StaggeredGridItemSpan.FullLine
              else StaggeredGridItemSpan.SingleLane
      ) {
        StaggeredEntry(delayMillis = 200) {
          DisplayColorCard(
              viewModel = viewModel,
              isRooted = isRooted,
              expanded = isDisplayExpanded,
              onExpandedChange = { isDisplayExpanded = it },
          )
        }
      }

      // 4. Per App Profile Card (NEW - After Display & Game Space row)
      item(span = StaggeredGridItemSpan.FullLine) {
        StaggeredEntry(delayMillis = 250) {
          PerAppProfileCard(onClick = onPerAppProfileClick)
        }
      }

      // 5. SELinux Card (Left)
      item(
          span =
              if (isSELinuxExpanded) StaggeredGridItemSpan.FullLine
              else StaggeredGridItemSpan.SingleLane
      ) {
        StaggeredEntry(delayMillis = 300) {
          SELinuxCard(
              viewModel = viewModel,
              isRooted = isRooted,
              expanded = isSELinuxExpanded,
              onExpandedChange = { isSELinuxExpanded = it },
          )
        }
      }

      // 6. Process Manager Card (Right) - Next to SELinux
      if (!isSELinuxExpanded) {
        item(span = StaggeredGridItemSpan.SingleLane) {
          StaggeredEntry(delayMillis = 350) {
            ProcessManagerCard(onClick = onProcessManagerClick)
          }
        }
      }

      // 7. Functional ROM (VIP Feature - Moved to bottom)
      item(span = StaggeredGridItemSpan.FullLine) {
        StaggeredEntry(delayMillis = 400) { FunctionalRomCard(viewModel) }
      }
    }
  }
}

@Composable
fun MaterialMiscHeader() {
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
  }
}

@Composable
fun PowerInsightCard(viewModel: MiscViewModel, batteryInfo: BatteryInfo, onClick: () -> Unit) {
  val screenOnTime by viewModel.screenOnTime.collectAsState()
  val screenOffTime by viewModel.screenOffTime.collectAsState()
  val deepSleepTime by viewModel.deepSleepTime.collectAsState()
  val drainRate by viewModel.drainRate.collectAsState()

  Card(
      modifier = Modifier.fillMaxWidth().height(220.dp),
      shape = RoundedCornerShape(32.dp),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainer
          ), // Dynamic background
      onClick = onClick,
  ) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
      // Header
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
              Icons.Rounded.Schedule, // Clock icon
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
              modifier =
                  Modifier.size(24.dp)
                      .background(
                          MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                          RoundedCornerShape(8.dp),
                      )
                      .padding(4.dp),
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
              text = "Power Insight",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
          )
        }

        // Charging Status Badge
        val isPluggedIn =
            batteryInfo.status.contains("Charging", ignoreCase = true) ||
                batteryInfo.status.contains("Full", ignoreCase = true)
        Surface(
            color =
                if (isPluggedIn) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(50),
        ) {
          Text(
              text = if (isPluggedIn) "Plugged In" else "Unplugged",
              style = MaterialTheme.typography.labelMedium,
              color =
                  if (isPluggedIn) MaterialTheme.colorScheme.onPrimaryContainer
                  else MaterialTheme.colorScheme.onSecondaryContainer,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          )
        }
      }

      // Content (Progress + Stats)
      Row(
          modifier = Modifier.fillMaxSize().padding(top = 40.dp), // Space for header
          horizontalArrangement = Arrangement.spacedBy(24.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        // Left: Wavy Progress
        Box(contentAlignment = Alignment.Center) {
          id.xms.xtrakernelmanager.ui.components.WavyCircularProgressIndicator(
              progress = 0.75f, // Placeholder progress
              modifier = Modifier.size(120.dp),
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
              strokeWidth = 14.dp,
              amplitude = 4.dp,
              frequency = 10,
          )
          Text(
              text = screenOnTime,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
          )
        }

        // Right: Stats List
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start,
        ) {
          PowerStatItem(
              icon = Icons.Rounded.WbSunny,
              value = screenOnTime,
              label = "Screen On",
              iconTint = MaterialTheme.colorScheme.primary,
          )
          PowerStatItem(
              icon = Icons.Rounded.Smartphone,
              value = screenOffTime,
              label = "Screen Off",
              iconTint = MaterialTheme.colorScheme.primary,
          )
          PowerStatItem(
              icon = Icons.Rounded.NightsStay,
              value = deepSleepTime,
              label = "Deep Sleep",
              iconTint = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }
  }
}

@Composable
fun PowerStatItem(icon: ImageVector, value: String, label: String, iconTint: Color) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint, // Override with specific color if needed, or use argument
        modifier =
            Modifier.size(20.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
                .padding(4.dp),
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(
          text = value,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
      )
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
    onClick: () -> Unit,
) {
  val gameApps by viewModel.gameApps.collectAsState()

  val appCount = try { JSONArray(gameApps).length() } catch (e: Exception) { 0 }

  Card(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth().height(140.dp),
      shape = RoundedCornerShape(24.dp),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Background Watermark
      Icon(
          Icons.Rounded.Gamepad,
          null,
          tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.05f),
          modifier =
              Modifier.size(100.dp)
                  .align(Alignment.BottomEnd)
                  .offset(x = 20.dp, y = 20.dp),
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


        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Game Space",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        Text(
            text = "$appCount Apps active",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
        )
      }
    }
  }
}

@Composable
fun SELinuxCard(
    viewModel: MiscViewModel,
    isRooted: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
  val selinuxStatus by viewModel.selinuxStatus.collectAsState()
  val isLoading by viewModel.selinuxLoading.collectAsState()
  val isEnforcing = selinuxStatus.equals("Enforcing", ignoreCase = true)

  Card(
      onClick = { onExpandedChange(!expanded) },
      modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp).animateContentSize(),
      shape = RoundedCornerShape(24.dp),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (isEnforcing) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
          ),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Background Watermark
      Icon(
          Icons.Rounded.Shield,
          null,
          tint =
              if (isEnforcing)
                  MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.05f)
              else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.05f),
          modifier =
              Modifier.size(if (expanded) 200.dp else 100.dp)
                  .align(Alignment.BottomEnd)
                  .offset(x = 20.dp, y = 20.dp),
      )

      Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.SpaceBetween,
          horizontalAlignment = Alignment.Start,
      ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
              Icons.Rounded.Shield,
              null,
              tint =
                  if (isEnforcing) MaterialTheme.colorScheme.onPrimaryContainer
                  else MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.size(28.dp),
          )

          // Status Badge
          Surface(
              color =
                  if (isEnforcing) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                  else MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
              shape = RoundedCornerShape(50),
          ) {
            Text(
                text = selinuxStatus,
                style = MaterialTheme.typography.labelSmall,
                color =
                    if (isEnforcing) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "SELinux",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color =
                if (isEnforcing) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onErrorContainer,
        )
        Text(
            text = "Security Policy",
            style = MaterialTheme.typography.bodySmall,
            color =
                if (isEnforcing)
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
        )

        // Expanded Content
        if (expanded) {
          Column(modifier = Modifier.padding(top = 16.dp)) {
            HorizontalDivider(
                modifier = Modifier.padding(bottom = 16.dp),
                color =
                    if (isEnforcing)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.1f),
            )

            if (!isRooted) {
              Text(
                  text = "Root access required.",
                  color = MaterialTheme.colorScheme.error,
                  style = MaterialTheme.typography.bodyMedium,
              )
            } else {
              // Toggle Row
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                Column {
                  Text(
                      text = if (isEnforcing) "Enforcing" else "Permissive",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color =
                          if (isEnforcing) MaterialTheme.colorScheme.onPrimaryContainer
                          else MaterialTheme.colorScheme.onErrorContainer,
                  )
                  Text(
                      text =
                          if (isEnforcing) "Security policies active"
                          else "Policies not enforced",
                      style = MaterialTheme.typography.bodySmall,
                      color =
                          if (isEnforcing)
                              MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                          else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                  )
                }

                if (isLoading) {
                  CircularProgressIndicator(
                      modifier = Modifier.size(24.dp),
                      strokeWidth = 2.dp,
                      color =
                          if (isEnforcing) MaterialTheme.colorScheme.primary
                          else MaterialTheme.colorScheme.error,
                  )
                } else {
                  Switch(
                      checked = isEnforcing,
                      onCheckedChange = { viewModel.setSELinuxMode(it) },
                      colors =
                          SwitchDefaults.colors(
                              checkedThumbColor = MaterialTheme.colorScheme.primary,
                              checkedTrackColor =
                                  MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                              uncheckedThumbColor = MaterialTheme.colorScheme.error,
                              uncheckedTrackColor =
                                  MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f),
                          ),
                      modifier = Modifier.scale(0.8f),
                  )
                }
              }

              // Warning Text
              Spacer(modifier = Modifier.height(12.dp))
              Surface(
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  shape = RoundedCornerShape(8.dp),
              ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                  Icon(
                      Icons.Rounded.Info,
                      null,
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(16.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                      text = "Changes reset after reboot. Permissive mode may break some apps.",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
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
fun ProcessManagerCard(onClick: () -> Unit) {
  Card(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth().height(140.dp),
      shape = RoundedCornerShape(24.dp),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
          ),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Background Watermark
      Icon(
          Icons.Rounded.Memory,
          null,
          tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
          modifier =
              Modifier.size(100.dp)
                  .align(Alignment.BottomEnd)
                  .offset(x = 20.dp, y = 20.dp),
      )

      Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
              Icons.Rounded.Memory,
              null,
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(28.dp),
          )

          Icon(
              Icons.Rounded.ChevronRight,
              null,
              tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
              modifier = Modifier.size(20.dp),
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Processes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "View & Kill Apps",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
      }
    }
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
fun PerAppProfileCard(onClick: () -> Unit) {
  Card(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth().height(80.dp),
      shape = MaterialTheme.shapes.large,
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
          ),
  ) {
    Row(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Rounded.AppSettingsAlt,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
          Text(
              "Per App Profile",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
          Text(
              "Custom settings for each app",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
          )
        }
      }
      Icon(
          Icons.Rounded.ChevronRight,
          null,
          tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
      )
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
