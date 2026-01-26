package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import id.xms.xtrakernelmanager.data.model.AppBatteryStats
import id.xms.xtrakernelmanager.data.model.BatteryUsageType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialBatteryScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onGraphClick: () -> Unit = {},
    onCurrentSessionClick: () -> Unit = {},
) {
  val context = LocalContext.current
  Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      topBar = {
        TopAppBar(
            title = {
              Text(
                  "Battery Monitor",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
              )
            },
            navigationIcon = {
              IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
              }
            },
            actions = {
              IconButton(onClick = onSettingsClick) {
                Icon(Icons.Rounded.Settings, contentDescription = "Settings")
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
        )
      },
  ) { paddingValues ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // 1. History Chart Card
      item { HistoryChartCard(onCurrentSessionClick = onCurrentSessionClick) }

      // 2. Current & Session Cards (Row)
      item {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            BatteryCapacityCard()
            ElectricCurrentCard(onClick = onGraphClick)
          }

          // Right Column: Session Stats
          val screenOnTime by viewModel.screenOnTime.collectAsState()
          val screenOffTime by viewModel.screenOffTime.collectAsState()
          val deepSleepTime by viewModel.deepSleepTime.collectAsState()
          val batteryInfo by viewModel.batteryInfo.collectAsState()

          Box(modifier = Modifier.weight(1f)) {
            CurrentSessionCard(
                onClick = onCurrentSessionClick,
                screenOnTime = screenOnTime,
                screenOffTime = screenOffTime,
                deepSleepTime = deepSleepTime,
                chargedInfo =
                    "${batteryInfo.level}% • ${kotlin.math.abs(batteryInfo.currentNow)} mA",
            )
          }
        }
      }

      item { Spacer(modifier = Modifier.height(8.dp)) }

      // 3. App Battery Usage List
      item { AppBatteryUsageList(viewModel) }

      item { Spacer(modifier = Modifier.height(24.dp)) }
    }
  }
}

@Composable
fun HistoryChartCard(onCurrentSessionClick: () -> Unit = {}) {
  // Card background simulating the dark grey/blue from screenshot
  val cardColor = Color(0xFF1E1F24)
  val state by
      id.xms.xtrakernelmanager.data.repository.HistoryRepository.hourlyStats.collectAsState()

  // Filter State: true = Screen On (Time), false = Drain (%)
  var showScreenOn by remember { mutableStateOf(true) }

  Card(
      shape = RoundedCornerShape(32.dp),
      colors = CardDefaults.cardColors(containerColor = cardColor),
      modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(24.dp)) {
      // Header with Filter Toggle
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
              text = "History",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color.White,
          )
          Text(
              text = state.date,
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.5f),
          )
        }

        // Toggle Pill
        Surface(
            color = Color(0xFF2C2D35),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        ) {
          Row(modifier = Modifier.padding(4.dp)) {
            // Screen On Tab
            Box(
                modifier =
                    Modifier.clip(RoundedCornerShape(50))
                        .background(if (showScreenOn) Color(0xFF009688) else Color.Transparent)
                        .clickable { showScreenOn = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                  text = "Screen",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (showScreenOn) Color.White else Color.White.copy(alpha = 0.5f),
                  fontWeight = FontWeight.Bold,
              )
            }

            // Drain Tab
            Box(
                modifier =
                    Modifier.clip(RoundedCornerShape(50))
                        .background(if (!showScreenOn) Color(0xFFD32F2F) else Color.Transparent)
                        .clickable { showScreenOn = false }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                  text = "Drain",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (!showScreenOn) Color.White else Color.White.copy(alpha = 0.5f),
                  fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      // Bar Chart Area
      val buckets = state.buckets
      val maxVal =
          if (showScreenOn) {
            // Max minutes in an hour is 60. Cap it at 60 for 100% height, but allow dynamic if
            // needed
            60f
          } else {
            // Max drain percent. Dynamic based on data, min 10%
            buckets.maxOfOrNull { it.drainPercent }?.toFloat()?.coerceAtLeast(10f) ?: 10f
          }

      Row(
          modifier = Modifier.fillMaxWidth().height(140.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom,
      ) {
        // Show only even hours to save space: 0, 2, 4 ... 22
        // Or show all 24 as thin bars?
        // Design choice: Show buckets in groups or just every 4th hour label
        val primaryColor = if (showScreenOn) Color(0xFF009688) else Color(0xFFD32F2F)

        buckets.forEachIndexed { index, bucket ->
          // Calculate height ratio
          val value =
              if (showScreenOn) (bucket.screenOnMs / 60000f) else bucket.drainPercent.toFloat()
          val heightPercent = (value / maxVal).coerceIn(0.05f, 1f) // Min 5% height to be visible

          // Only show some bars if too crowded? No, 24 bars is fine on width
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.weight(1f),
          ) {
            // Tooltip/Value on top if selected? Too complex for now, just bars
            Box(
                modifier =
                    Modifier.width(6.dp)
                        .fillMaxHeight(heightPercent)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (value > 0) primaryColor else Color(0xFF3F455A))
            )
            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis Labels (Every 4 hours)
            if (index % 4 == 0) {
              Text(
                  text = "%02d".format(index),
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  color = Color.White.copy(alpha = 0.5f),
              )
            } else {
              Spacer(modifier = Modifier.height(12.dp)) // Placeholder for alignment
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Total Summary for View
      val totalStr =
          if (showScreenOn) {
            val totalMs = buckets.sumOf { it.screenOnMs }
            formatDuration(totalMs)
          } else {
            "${buckets.sumOf { it.drainPercent }}%"
          }

      Text(
          text = "Total Today: $totalStr",
          style = MaterialTheme.typography.bodyMedium,
          color = Color.White.copy(alpha = 0.7f),
          modifier = Modifier.align(Alignment.CenterHorizontally),
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Divider
      Box(
          modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f))
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Nested Stats Card (Session)
      val sessionState by
          id.xms.xtrakernelmanager.data.repository.BatteryRepository.batteryState.collectAsState()

      // Calculate display values
      val activeDrain = "%.2f".format(sessionState.activeDrainRate)
      val idleDrain = "%.2f".format(sessionState.idleDrainRate)
      val screenOnStr = formatDuration(sessionState.screenOnTime)

      // Nested Stats Card
      Card(
          onClick = onCurrentSessionClick,
          colors =
              CardDefaults.cardColors(
                  containerColor = Color(0xFF16171B) // Slightly darker than main card
              ),
          shape = RoundedCornerShape(24.dp),
          modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          SummaryStat("Active", "$activeDrain%/h")
          VerticalDivider(
              modifier = Modifier.height(32.dp),
              thickness = 1.dp,
              color = Color.White.copy(alpha = 0.1f),
          )
          SummaryStat("Idle", "$idleDrain%/h")
          VerticalDivider(
              modifier = Modifier.height(32.dp),
              thickness = 1.dp,
              color = Color.White.copy(alpha = 0.1f),
          )
          SummaryStat("Session", screenOnStr)
        }
      }
    }
  }
}

@Composable
fun ElectricCurrentCard(onClick: () -> Unit = {}) {
  val state by
      id.xms.xtrakernelmanager.data.repository.BatteryRepository.batteryState.collectAsState()

  // Calculate specific metrics
  val currentMa = state.currentNow
  val voltageMv = state.voltage
  val watts = (kotlin.math.abs(currentMa) / 1000f) * (voltageMv / 1000f)

  Card(
      shape = RoundedCornerShape(32.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F24)),
      modifier =
          Modifier.fillMaxWidth().height(160.dp).clickable(onClick = onClick), // Height reduced
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      Text(
          text = "Electric Current",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = Color.White,
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
          text = "$currentMa mA",
          style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
          fontWeight = FontWeight.Medium,
          color =
              if (currentMa > 0) Color(0xFF009688)
              else if (currentMa < 0) Color(0xFFD32F2F) else Color.White,
      )

      Spacer(modifier = Modifier.weight(1f))

      // Graph Removed as per request

      Spacer(modifier = Modifier.weight(1f))

      Text(
          text = "%.1f W • %d mV".format(watts, voltageMv),
          style = MaterialTheme.typography.bodyMedium,
          color = Color.White.copy(alpha = 0.6f),
      )
    }
  }
}

@Composable
fun BatteryCapacityCard() {
  val state by
      id.xms.xtrakernelmanager.data.repository.BatteryRepository.batteryState.collectAsState()
  // Mock capacity if 0 (native/root failure fallback)
  val design = if (state.totalCapacity > 0) state.totalCapacity else 5000
  val current = if (state.currentCapacity > 0) state.currentCapacity else 4500
  // Calculate Health
  val healthPercent = ((current.toFloat() / design.toFloat()) * 100).toInt().coerceIn(0, 100)

  Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F24)),
      modifier =
          Modifier.fillMaxWidth().height(84.dp), // Remaining height to match right card roughly
  ) {
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column {
        Text(
            text = "Battery Health",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "$healthPercent%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
      }


    }
  }
}

@Composable
fun CurrentSessionCard(
    onClick: () -> Unit = {},
    screenOnTime: String = "--",
    screenOffTime: String = "--",
    deepSleepTime: String = "--",
    chargedInfo: String = "0% • 0 mAh",
) {
  Card(
      onClick = onClick,
      shape = RoundedCornerShape(32.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F24)),
      modifier = Modifier.fillMaxWidth().height(260.dp),
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      Text(
          text = "Current Session",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = Color.White,
      )

      Spacer(modifier = Modifier.height(20.dp))

      StatRowCompact(icon = Icons.Rounded.WbSunny, label = "Screen On", value = screenOnTime)
      Spacer(modifier = Modifier.height(16.dp))
      StatRowCompact(icon = Icons.Rounded.NightsStay, label = "Screen Off", value = screenOffTime)
      Spacer(modifier = Modifier.height(16.dp))
      StatRowCompact(icon = Icons.Rounded.Bedtime, label = "Deep Sleep", value = deepSleepTime)
      Spacer(modifier = Modifier.height(16.dp))
      StatRowCompact(icon = Icons.Rounded.BatteryStd, label = "Charged", value = chargedInfo)
    }
  }
}

@Composable
fun StatRowCompact(icon: ImageVector, label: String, value: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
        tint = Color.White,
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = Color.White,
      )
      Text(
          text = value,
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.7f),
      )
    }
  }
}

@Composable
fun LegendBadge(color: Color, label: String) {
  Surface(
      color = color.copy(alpha = 0.1f),
      shape = RoundedCornerShape(50),
      border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
  ) {
    Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
      Spacer(modifier = Modifier.width(6.dp))
      Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = color,
          fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Composable
fun SummaryStat(label: String, value: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        color = Color.White.copy(alpha = 0.7f),
    )
  }
}

private fun formatDuration(millis: Long): String {
  val seconds = millis / 1000
  val minutes = seconds / 60
  val hours = minutes / 60
  return if (hours > 0) "%dh %02dm".format(hours, minutes % 60)
  else "%dm %02ds".format(minutes, seconds % 60)
}

@Composable
fun AppBatteryUsageList(viewModel: MiscViewModel) {
  val appUsageList by viewModel.appBatteryUsage.collectAsState()
  val isLoading by viewModel.isLoadingAppUsage.collectAsState()

  // false = Apps, true = System
  var showSystem by rememberSaveable { mutableStateOf(false) }

  val filteredList =
      remember(appUsageList, showSystem) {
        if (showSystem) {
          appUsageList.filter { it.usageType == BatteryUsageType.SYSTEM }
        } else {
          appUsageList.filter { it.usageType == BatteryUsageType.APP }
        }
      }

  Column(modifier = Modifier.fillMaxWidth()) {
    // Header
    Text(
        text = "Battery usage since last full charge",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Filter Dropdown
    FilterDropdown(isSystem = showSystem, onFilterChange = { showSystem = it })

    Spacer(modifier = Modifier.height(16.dp))

    if (isLoading && appUsageList.isEmpty()) {
      Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
      }
    } else if (filteredList.isEmpty()) {
      Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
        Text(
            text = "No usage data available",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.5f),
        )
      }
    } else {
      // List Items
      Card(
          shape = RoundedCornerShape(32.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F24)),
          modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          filteredList.forEachIndexed { index, app ->
            AppUsageItem(app)
            if (index < filteredList.lastIndex) {
              HorizontalDivider(
                  modifier = Modifier.padding(vertical = 12.dp, horizontal = 0.dp),
                  thickness = 1.dp,
                  color = Color.White.copy(alpha = 0.05f),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun FilterDropdown(isSystem: Boolean, onFilterChange: (Boolean) -> Unit) {
  var expanded by remember { mutableStateOf(false) }

  Box {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFF2C2D35),
        modifier = Modifier.clickable { expanded = true },
    ) {
      Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = if (isSystem) "View by systems" else "View by apps",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Rounded.ArrowDropDown,
            contentDescription = null,
            tint = Color.White,
        )
      }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        containerColor = Color(0xFF2C2D35),
    ) {
      DropdownMenuItem(
          text = { Text("View by apps", color = Color.White) },
          onClick = {
            onFilterChange(false)
            expanded = false
          },
      )
      DropdownMenuItem(
          text = { Text("View by systems", color = Color.White) },
          onClick = {
            onFilterChange(true)
            expanded = false
          },
      )
    }
  }
}

@Composable
fun AppUsageItem(app: AppBatteryStats) {
  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    // App Icon
    if (app.icon != null) {
      androidx.compose.foundation.Image(
          painter = rememberAsyncImagePainter(model = app.icon),
          contentDescription = null,
          modifier = Modifier.size(40.dp),
      )
    } else {
      // Placeholder Icon
      Box(
          modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF3F455A)),
          contentAlignment = Alignment.Center,
      ) {
        Icon(
            imageVector =
                if (app.usageType == BatteryUsageType.SYSTEM) Icons.Rounded.Android
                else Icons.Rounded.Apps,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp),
        )
      }
    }

    Spacer(modifier = Modifier.width(16.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
          text = app.appName,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = Color.White,
      )

      Text(
          text = "Battery usage", // Generic subtitle since we lack exact screen/bg times
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.5f),
      )
    }

    Text(
        text = "${app.percent.toInt()}%",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
    )
  }
}

