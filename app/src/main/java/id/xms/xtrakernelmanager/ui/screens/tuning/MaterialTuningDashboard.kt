package id.xms.xtrakernelmanager.ui.screens.tuning

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MaterialTuningDashboard(
    viewModel: TuningViewModel,
    preferencesManager: PreferencesManager? = null,
    onNavigate: (String) -> Unit = {},
) {

  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
  var memoryCardExpanded by remember { mutableStateOf(false) }
  var networkCardExpanded by remember { mutableStateOf(false) }
  var cpuCardExpanded by remember { mutableStateOf(false) }
  var thermalCardExpanded by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "Tuning",
                  style =
                      MaterialTheme.typography.headlineMedium.copy(
                          fontWeight = FontWeight.SemiBold
                      ),
              )
            },
            actions = {
              var showMenu by remember { mutableStateOf(false) }

              Box {
                IconButton(onClick = { showMenu = true }) {
                  Icon(Icons.Rounded.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    shape = RoundedCornerShape(12.dp),
                ) {
                  DropdownMenuItem(
                      text = { Text("Import Profile") },
                      onClick = {
                        showMenu = false
                        /* Import Action */
                      },
                      leadingIcon = { Icon(Icons.Rounded.FolderOpen, contentDescription = null) },
                  )
                  DropdownMenuItem(
                      text = { Text("Export Profile") },
                      onClick = {
                        showMenu = false
                        /* Export Action */
                      },
                      leadingIcon = { Icon(Icons.Rounded.Save, contentDescription = null) },
                  )
                }
              }
            },
            scrollBehavior = scrollBehavior,
        )
      },
  ) { paddingValues ->
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
        verticalItemSpacing = 16.dp,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
      // 1. Hero Card
      item(span = StaggeredGridItemSpan.FullLine) { HeroDeviceCard() }

      // 2 & 3. CPU & Thermal (Dynamic Bento Layout)
      if (thermalCardExpanded) {
        item(span = StaggeredGridItemSpan.FullLine, key = "thermal_card") {
          ExpandableThermalCard(
              viewModel = viewModel,
              expanded = true,
              onExpandChange = {
                thermalCardExpanded = it
              },
              onClickNav = { onNavigate("thermal_tuning") },
              topLeftContent = {
                ExpandableCPUCard(
                    viewModel = viewModel,
                    onClickNav = { onNavigate("cpu_tuning") },
                )
              },
          )
        }
      } else {
        item(key = "cpu_card") {
          ExpandableCPUCard(
              viewModel = viewModel,
              onClickNav = { onNavigate("cpu_tuning") },
          )
        }
        item(key = "thermal_card") {
          ExpandableThermalCard(
              viewModel = viewModel,
              expanded = false,
              onExpandChange = {
                thermalCardExpanded = it
              },
              onClickNav = { onNavigate("thermal_tuning") },
          )
        }
      }

      // 4. Profile Card
      item(span = StaggeredGridItemSpan.FullLine) { DashboardProfileCard() }

      // 5. GPU Card (Expandable)
      item(span = StaggeredGridItemSpan.FullLine) { ExpandableGPUCard(viewModel = viewModel) }

      // 6 & 7. Memory & Network (Dynamic Bento Layout)
      // 6 & 7. Memory & Network (Dynamic Bento Layout)
      if (memoryCardExpanded) {
        item(span = StaggeredGridItemSpan.FullLine) {
          ExpandableMemoryCard(
              expanded = true,
              onExpandChange = {
                memoryCardExpanded = it // Toggle
                if (it) networkCardExpanded = false // Ensure other is collapsed
              },
              onClickNav = { onNavigate("memory_tuning") },
              topRightContent = {
                ExpandableNetworkCard(
                    expanded = false,
                    onExpandChange = {
                      networkCardExpanded = it
                      if (it) memoryCardExpanded = false
                    },
                    onClickNav = { onNavigate("network_tuning") },
                )
              },
          )
        }
      } else if (networkCardExpanded) {
        item(span = StaggeredGridItemSpan.FullLine) {
          ExpandableNetworkCard(
              expanded = true,
              onExpandChange = {
                networkCardExpanded = it
                if (it) memoryCardExpanded = false
              },
              onClickNav = { onNavigate("network_tuning") },
              topLeftContent = {
                ExpandableMemoryCard(
                    expanded = false,
                    onExpandChange = {
                      memoryCardExpanded = it
                      if (it) networkCardExpanded = false
                    },
                    onClickNav = { onNavigate("memory_tuning") },
                )
              },
          )
        }
      } else {
        item(span = StaggeredGridItemSpan.SingleLane) {
          ExpandableMemoryCard(
              expanded = false,
              onExpandChange = {
                memoryCardExpanded = it
                if (it) networkCardExpanded = false
              },
              onClickNav = { onNavigate("memory_tuning") },
          )
        }
        item(span = StaggeredGridItemSpan.SingleLane) {
          ExpandableNetworkCard(
              expanded = false,
              onExpandChange = {
                networkCardExpanded = it
                if (it) memoryCardExpanded = false
              },
              onClickNav = { onNavigate("network_tuning") },
          )
        }
      }
    }
  }
}

@Composable
fun ExpandableMemoryCard(
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onClickNav: () -> Unit,
    topRightContent: @Composable () -> Unit = {},
) {
  // Animation
  val height by animateDpAsState(targetValue = if (expanded) 380.dp else 120.dp, label = "height")

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
      // COLLAPSED STATE (Simple Card)
      Card(
          modifier = Modifier.fillMaxSize(),
          shape = RoundedCornerShape(24.dp),
          colors =
              CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
          Icon(
              Icons.Rounded.SdCard,
              null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(28.dp),
          )
          Column {
            Text(
                "Memory",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "ZRAM 50% • LMK",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    } else {
      // EXPANDED STATE (L-SHAPE LAYOUT)

      val cornerRadius = 24.dp
      val gap = 24.dp
      val cutoutHeight = 120.dp // Match DashboardNavCard height
      val themeColor = MaterialTheme.colorScheme.secondaryContainer

      // 1. TOP RIGHT CARD (Slot for external content, e.g. Network Card)

      Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
          Box(modifier = Modifier.weight(1f).fillMaxHeight()) // Placeholder for Left L-Arm

          // TOP RIGHT SLOT
          Box(modifier = Modifier.weight(1f).height(cutoutHeight)) { topRightContent() }
        }
      }

      Box(modifier = Modifier.fillMaxSize()) {
        // The L-Shape Background
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val colW = w / 2 - gap.toPx() / 2 // Width of left column
          val ch = cutoutHeight.toPx() + gap.toPx()
          val cr = cornerRadius.toPx()
          val ir = cornerRadius.toPx() // Match inverted radius with card corner radius

          val path =
              Path().apply {
                // Start Top-Left
                moveTo(0f, cr)
                quadraticTo(0f, 0f, cr, 0f)

                // Top Edge to Top-Right Corner of Left Leg
                lineTo(colW - cr, 0f)
                quadraticTo(colW, 0f, colW, cr)

                // Going Down the inner vertical edge
                lineTo(colW, ch - ir)

                arcTo(
                    rect =
                        androidx.compose.ui.geometry.Rect(
                            left = colW,
                            top = ch - 2 * ir,
                            right = colW + 2 * ir,
                            bottom = ch,
                        ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false,
                )

                // Top Edge of Bottom-Right Section
                lineTo(w - cr, ch)
                quadraticTo(w, ch, w, ch + cr)

                // Right Edge
                lineTo(w, h - cr)
                quadraticTo(w, h, w - cr, h)

                // Bottom Edge
                lineTo(cr, h)
                quadraticTo(0f, h, 0f, h - cr)

                close()
              }

          drawPath(path, color = themeColor)
        }

        // CONTENT LAYOUT
        // We overlay content on the L-shape
        Row(modifier = Modifier.fillMaxSize()) {
          // Left Column Content (ZRAM)
          Column(
              modifier = Modifier.weight(1f).fillMaxHeight().padding(20.dp),
              verticalArrangement = Arrangement.SpaceBetween,
          ) {
            // Header info
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                  modifier =
                      Modifier.size(40.dp)
                          .clip(RoundedCornerShape(12.dp))
                          .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center,
              ) {
                Icon(
                    Icons.Rounded.Memory,
                    null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
              }
            }
          }

          // Right Column (Empty Top + Content Bottom)
          Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Spacer(modifier = Modifier.height(cutoutHeight + gap)) // Skip Cutout + Gap

            // Bottom Right Content (Placeholder / Empty)
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.Center,
            ) {
              // Content temporarily removed as requested
            }
          }
        }
      }
    }
  }
}

@Composable
fun HeroDeviceCard() {
  Card(
      modifier = Modifier.fillMaxWidth().height(140.dp),
      shape = RoundedCornerShape(24.dp),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
  ) {
    Column(
        modifier = Modifier.padding(20.dp).fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start,
    ) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
      ) {
        Text(
            text = "XIAOMI",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shape = RoundedCornerShape(100),
            modifier = Modifier.padding(top = 4.dp),
        ) {
          Text(
              text = "TARO",
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
          )
        }
      }

      Column {
        Text(
            text = "23049PCD8G",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "marble - 18% Load",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
fun ExpandableNetworkCard(
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onClickNav: () -> Unit,
    topLeftContent: @Composable () -> Unit = {},
) {
  // Animation
  val height by
      animateDpAsState(targetValue = if (expanded) 380.dp else 120.dp, label = "net_height")

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
      // COLLAPSED STATE
      DashboardNavCard(
          title = "Network",
          subtitle = "Westwood • DNS",
          icon = Icons.Rounded.Router,
          onClick = { onExpandChange(true) },
      )
    } else {
      // EXPANDED STATE (MIRRORED L-SHAPE)

      val cornerRadius = 24.dp
      val gap = 24.dp
      val cutoutHeight = 120.dp
      val themeColor = MaterialTheme.colorScheme.secondaryContainer

      // 1. TOP LEFT SLOT (e.g. Memory Card)
      Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {

          // TOP LEFT SLOT
          Box(modifier = Modifier.weight(1f).height(cutoutHeight)) { topLeftContent() }

          Box(modifier = Modifier.weight(1f).fillMaxHeight()) // Placeholder for Right Body
        }
      }

      // 2. THE MIRRORED L-SHAPE
      Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val bodyX = w / 2 + gap.toPx() / 2 // Start of Right Body
          val ch = cutoutHeight.toPx() + gap.toPx()
          val cr = cornerRadius.toPx()
          val ir = cornerRadius.toPx()

          val path =
              Path().apply {
                // Start Top-Left of Right Body
                moveTo(bodyX + cr, 0f)
                quadraticTo(bodyX, 0f, bodyX, cr)

                // Going Down the inner vertical edge
                lineTo(bodyX, ch - ir)

                // Inverted Corner (Turning Left)
                arcTo(
                    rect =
                        androidx.compose.ui.geometry.Rect(
                            left = bodyX - 2 * ir,
                            top = ch - 2 * ir,
                            right = bodyX,
                            bottom = ch,
                        ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )

                // Top Edge of Left Leg
                lineTo(cr, ch)
                quadraticTo(0f, ch, 0f, ch + cr)

                // Left Edge
                lineTo(0f, h - cr)
                quadraticTo(0f, h, cr, h)

                // Bottom Edge
                lineTo(w - cr, h)
                quadraticTo(w, h, w, h - cr)

                // Right Edge
                lineTo(w, cr)
                quadraticTo(w, 0f, w - cr, 0f)

                // Top Edge of Right Body
                lineTo(bodyX + cr, 0f)

                close()
              }

          drawPath(path, color = themeColor)
        }

        // Content Layout
        Row(modifier = Modifier.fillMaxSize()) {
          // Left Column (Empty Top + Content Bottom)
          Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Spacer(modifier = Modifier.height(cutoutHeight + gap))
          }

          // Right Column Content (Header)
          Column(
              modifier = Modifier.weight(1f).fillMaxHeight().padding(20.dp),
              verticalArrangement = Arrangement.SpaceBetween,
          ) {
            // Header info
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                  modifier =
                      Modifier.size(40.dp)
                          .clip(RoundedCornerShape(12.dp))
                          .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center,
              ) {
                Icon(
                    Icons.Rounded.Router,
                    null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ExpandableCPUCard(
    viewModel: TuningViewModel,
    onClickNav: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cpuClusters by viewModel.cpuClusters.collectAsState()
    val currentGovernor = cpuClusters.firstOrNull()?.governor?.replaceFirstChar { 
        it.uppercase() 
    } ?: "—"
    
    DashboardNavCard(
        title = "CPU",
        subtitle = "Clock & Governor",
        icon = Icons.Rounded.Memory,
        badgeText = currentGovernor, // Now shows real governor
        onClick = onClickNav,
    )
}


@Composable
fun ExpandableThermalCard(
    viewModel: TuningViewModel,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onClickNav: () -> Unit,
    topLeftContent: @Composable () -> Unit = {},
) {
  // Get real temperature from ViewModel
  val temperature by viewModel.cpuTemperature.collectAsState()
  val tempDisplay = if (temperature > 0) "${temperature.toInt()}°C" else "—"
  
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
          subtitle = "Normal",
          icon = Icons.Rounded.Thermostat,
          badgeText = tempDisplay, // Now shows real temperature
          onClick = { onExpandChange(true) },
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

        Row(modifier = Modifier.fillMaxSize()) {
          Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Spacer(modifier = Modifier.height(cutoutHeight + gap))
          }
          Column(
              modifier = Modifier.weight(1f).fillMaxHeight().padding(20.dp),
              verticalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
            }
          }
        }
      }
    }
  }
}

@Composable
fun DashboardNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String? = null,
    onClick: () -> Unit,
) {
  Card(
      modifier = Modifier.fillMaxWidth().height(120.dp).clickable(onClick = onClick),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Column(
        modifier = Modifier.padding(20.dp).fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
      ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
        )

        if (badgeText != null) {
          Surface(
              color = MaterialTheme.colorScheme.secondaryContainer,
              shape = RoundedCornerShape(100),
          ) {
            Text(
                text = badgeText,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }
        }
      }

      Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
fun DashboardProfileCard() {
  var expanded by remember { mutableStateOf(false) }
  var selectedProfile by remember { mutableStateOf("Performance") }
  val profiles = listOf("Performance", "Balance", "Powersave", "Battery")

  Card(
      modifier = Modifier.fillMaxWidth().animateContentSize().clickable { expanded = !expanded },
      shape = RoundedCornerShape(32.dp),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
  ) {
    Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(20.dp),
      ) {
        // Icon Bubble
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f),
        ) {
          Box(contentAlignment = Alignment.Center) {
            val icon =
                when (selectedProfile) {
                  "Performance" -> Icons.Rounded.Bolt
                  "Powersave" -> Icons.Rounded.Eco
                  "Battery" -> Icons.Rounded.BatteryFull
                  else -> Icons.Rounded.Speed // Balance
                }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
          }
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = selectedProfile,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
          )
          Text(
              text = "Active Profile",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
          )
        }
      }

      // Expanded List
      AnimatedVisibility(visible = expanded) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.08f),
        ) {
          Column(modifier = Modifier.padding(8.dp)) {
            profiles.forEach { profile ->
              val isSelected = profile == selectedProfile
              Row(
                  modifier =
                      Modifier.fillMaxWidth()
                          .clip(RoundedCornerShape(14.dp))
                          .clickable {
                            selectedProfile = profile
                            expanded = false
                          }
                          .background(
                              if (isSelected)
                                  MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f)
                              else Color.Transparent
                          )
                          .padding(vertical = 12.dp, horizontal = 16.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                    text = profile,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color =
                        MaterialTheme.colorScheme.onTertiaryContainer.copy(
                            alpha = if (isSelected) 1f else 0.8f
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
  val gpuModel = gpuInfo.renderer
      .replace("(TM)", "")
      .replace("(R)", "")
      .trim()
      .ifEmpty { "Unknown GPU" }

  Card(
      modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.animateContentSize(),
      shape = RoundedCornerShape(24.dp),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  MaterialTheme.colorScheme.secondaryContainer
          ),
  ) {
    Column(modifier = Modifier.padding(24.dp)) {
      // Header
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = "GPU",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
        )
        Surface(
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp),
        ) {
          // Show vendor from gpuInfo (e.g., "Qualcomm" -> "ADRENO")
          val badgeText = when {
            gpuInfo.vendor.contains("Qualcomm", ignoreCase = true) -> "ADRENO"
            gpuInfo.vendor.contains("ARM", ignoreCase = true) -> "MALI"
            gpuInfo.vendor.contains("PowerVR", ignoreCase = true) -> "POWERVR"
            gpuInfo.renderer.contains("Adreno", ignoreCase = true) -> "ADRENO"
            gpuInfo.renderer.contains("Mali", ignoreCase = true) -> "MALI"
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
            text = "Frequency",
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
                text = "Load",
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
                text = "GPU",
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
        
        // Build freq options from availableFreqs
        val freqOptions = gpuInfo.availableFreqs.map { "${it} MHz" }
        
        // Track selected values
        var selectedMinFreq by remember(gpuInfo.minFreq) { 
          mutableStateOf("${gpuInfo.minFreq} MHz") 
        }
        var selectedMaxFreq by remember(gpuInfo.maxFreq) { 
          mutableStateOf("${gpuInfo.maxFreq} MHz") 
        }
        var powerSliderValue by remember(gpuInfo.powerLevel, gpuInfo.numPwrLevels) {
          mutableFloatStateOf(
            if (gpuInfo.numPwrLevels > 0) 1f - (gpuInfo.powerLevel.toFloat() / gpuInfo.numPwrLevels.toFloat())
            else 0.5f
          )
        }
        
        Column(
            modifier = Modifier.padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

          // Governor (display only, not changeable for GPU)
          GpuControlRow(
              label = "Governor",
              value = "msm-adreno-tz",
              icon = Icons.Rounded.Speed,
              options = listOf("msm-adreno-tz"),
              onValueChange = { /* GPU governor not changeable */ },
          )

          // Min/Max Tiles with real freq options - auto-lock on change
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GpuTile(
                modifier = Modifier.weight(1f),
                label = "Min Frequency",
                value = selectedMinFreq,
                options = freqOptions.ifEmpty { listOf("${gpuInfo.minFreq} MHz") },
                onValueChange = { newValue ->
                  selectedMinFreq = newValue
                  // Auto-lock when changed
                  val minFreq = newValue.replace(" MHz", "").toIntOrNull() ?: gpuInfo.minFreq
                  val maxFreq = selectedMaxFreq.replace(" MHz", "").toIntOrNull() ?: gpuInfo.maxFreq
                  viewModel.lockGPUFrequency(minFreq, maxFreq)
                },
            )
            GpuTile(
                modifier = Modifier.weight(1f),
                label = "Max Frequency",
                value = selectedMaxFreq,
                options = freqOptions.ifEmpty { listOf("${gpuInfo.maxFreq} MHz") },
                onValueChange = { newValue ->
                  selectedMaxFreq = newValue
                  // Auto-lock when changed
                  val minFreq = selectedMinFreq.replace(" MHz", "").toIntOrNull() ?: gpuInfo.minFreq
                  val maxFreq = newValue.replace(" MHz", "").toIntOrNull() ?: gpuInfo.maxFreq
                  viewModel.lockGPUFrequency(minFreq, maxFreq)
                },
            )
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
                    "Power Level",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Level ${(powerSliderValue * 10).toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
              }
              Spacer(Modifier.height(16.dp))
              WavySlider(
                  value = powerSliderValue, 
                  onValueChange = { newValue ->
                    powerSliderValue = newValue
                    // Calculate power level 0-10, then map to actual GPU power levels
                    val level = ((1f - newValue) * gpuInfo.numPwrLevels.coerceAtLeast(1)).toInt()
                    viewModel.setGPUPowerLevel(level)
                  }
              )
            }
          }

          // Renderer
          GpuControlRow(
              label = "Renderer",
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
                  val newValue = (change.position.x / size.width).coerceIn(0f, 1f)
                  onValueChange(newValue)
                }
              }
  ) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
      val width = size.width
      val height = size.height
      val centerY = height / 2

      // Build the full wave path
      val path = Path()
      path.moveTo(0f, centerY)

      val step = 5f
      var x = 0f
      while (x <= width) {
        val y = centerY + sin((x / width) * (PI * waveFrequency)) * waveAmplitude
        path.lineTo(x, y.toFloat())
        x += step
      }

      // Draw inactive track (full wave)
      drawPath(
          path = path,
          color = inactiveColor,
          style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
      )

      // Draw active track (clipped wave)
      drawContext.canvas.save()
      drawContext.canvas.clipRect(0f, 0f, width * value, height)
      drawPath(
          path = path,
          color = primaryColor,
          style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
      )
      drawContext.canvas.restore()

      // Draw thumb
      val thumbX = width * value
      val thumbY = centerY + sin((thumbX / width) * (PI * waveFrequency)) * waveAmplitude
      drawCircle(
          color = primaryColor,
          radius = strokeWidth,
          center = Offset(thumbX, thumbY.toFloat()),
      )
    }
  }
}
