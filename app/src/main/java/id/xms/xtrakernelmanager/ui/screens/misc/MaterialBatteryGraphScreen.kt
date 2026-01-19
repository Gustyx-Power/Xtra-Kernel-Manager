package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ElectricMeter
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialBatteryGraphScreen(viewModel: MiscViewModel, onBack: () -> Unit) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0 = Charging, 1 = Discharging
  var selectedTimeRange by remember { mutableStateOf("10m") }
  var isTipExpanded by remember { mutableStateOf(true) }

  Scaffold(
      containerColor = MaterialTheme.colorScheme.surface, // Standard surface for M3
      topBar = {
        TopAppBar(
            title = {
              Text("Battery Analytics", fontWeight = FontWeight.SemiBold, fontSize = 24.sp)
            },
            navigationIcon = {
              IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
        )
      },
  ) { paddingValues ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // 1. Hero Status Card
      item { HeroStatusCard(selectedTab = selectedTab, onTabSelected = { selectedTab = it }) }

      // 2. Chart Section
      item {
        ChartSection(
            selectedTimeRange = selectedTimeRange,
            onTimeRangeSelected = { selectedTimeRange = it },
        )
      }

      // 3. Stats Row
      item { StatsRow() }

      // 4. Tip/Insight Section
      item { TipCard(isExpanded = isTipExpanded, onToggle = { isTipExpanded = !isTipExpanded }) }

      item { Spacer(modifier = Modifier.height(32.dp)) }
    }
  }
}

@Composable
fun HeroStatusCard(selectedTab: Int, onTabSelected: (Int) -> Unit) {
  Card(
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer,
              contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          ),
      shape = RoundedCornerShape(28.dp),
      modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(24.dp)) {
      // Header with Segmentation
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        // Tab Switcher Pilled
        Surface(
            color =
                MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), // Glassy look on container
            shape = CircleShape,
            modifier = Modifier.height(36.dp),
        ) {
          Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            TabPill(text = "Charging", selected = selectedTab == 0, onClick = { onTabSelected(0) })
            Spacer(modifier = Modifier.width(4.dp))
            TabPill(
                text = "Discharging",
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
            )
          }
        }

        // Icon Badge
        Surface(
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(40.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (selectedTab == 0) Icons.Rounded.Bolt else Icons.Rounded.Power,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Big Value
      Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = if (selectedTab == 0) "1,240" else "-450", // Mock values
            style =
                MaterialTheme.typography.displayLarge.copy(
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-2).sp,
                ),
            lineHeight = 56.sp,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "mA",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp),
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Sub-metrics
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        SubMetricPill(icon = Icons.Rounded.ElectricMeter, value = "4.2 V")
        SubMetricPill(icon = Icons.Rounded.Speed, value = "5.2 W")
      }
    }
  }
}

@Composable
fun TabPill(text: String, selected: Boolean, onClick: () -> Unit) {
  Box(
      modifier =
          Modifier.clip(CircleShape)
              .background(
                  if (selected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Transparent
              )
              .clickable(onClick = onClick)
              .padding(horizontal = 12.dp, vertical = 6.dp),
      contentAlignment = Alignment.Center,
  ) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color =
            if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.onPrimaryContainer,
    )
  }
}

@Composable
fun SubMetricPill(icon: ImageVector, value: String) {
  Surface(
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
      shape = RoundedCornerShape(12.dp),
  ) {
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
          text = value,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
      )
    }
  }
}

@Composable
fun ChartSection(selectedTimeRange: String, onTimeRangeSelected: (String) -> Unit) {
  Card(
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
      shape = RoundedCornerShape(24.dp),
      modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = "Current Flow",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        // Time Range Selector
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("10m", "1h", "6h").forEach { range ->
            TimeChip(
                text = range,
                selected = selectedTimeRange == range,
                onClick = { onTimeRangeSelected(range) },
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Placeholder Chart
      Box(
          modifier =
              Modifier.fillMaxWidth()
                  .height(200.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .background(MaterialTheme.colorScheme.surface),
          contentAlignment = Alignment.Center,
      ) {
        Text(
            text = "Chart Placeholder",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
fun TimeChip(text: String, selected: Boolean, onClick: () -> Unit) {
  Surface(
      color =
          if (selected) MaterialTheme.colorScheme.secondaryContainer
          else MaterialTheme.colorScheme.surface,
      shape = CircleShape,
      onClick = onClick,
      border =
          if (!selected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
  ) {
    Box(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
      Text(
          text = text,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color =
              if (selected) MaterialTheme.colorScheme.onSecondaryContainer
              else MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

@Composable
fun StatsRow() {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    StatCard(
        label = "Avg",
        value = "850 mA",
        modifier = Modifier.weight(1f),
        color = MaterialTheme.colorScheme.surfaceContainer,
    )
    StatCard(
        label = "Min",
        value = "120 mA",
        modifier = Modifier.weight(1f),
        color = MaterialTheme.colorScheme.surfaceContainer,
    )
    StatCard(
        label = "Max",
        value = "1500 mA",
        modifier = Modifier.weight(1f),
        color = MaterialTheme.colorScheme.surfaceContainer,
    )
  }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier, color: Color) {
  Card(
      colors = CardDefaults.cardColors(containerColor = color),
      shape = RoundedCornerShape(20.dp),
      modifier = modifier,
  ) {
    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
      Text(
          text = label,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
          text = value,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

@Composable
fun TipCard(isExpanded: Boolean, onToggle: () -> Unit) {
  Card(
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.tertiaryContainer,
              contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
          ),
      shape = RoundedCornerShape(24.dp),
      modifier = Modifier.fillMaxWidth().clickable { onToggle() },
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(20.dp))
          Text(
              text = "Charging Insight",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
          )
        }

        Icon(
            imageVector =
                if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
            contentDescription = "Toggle",
        )
      }

      AnimatedVisibility(
          visible = isExpanded,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut(),
      ) {
        Column {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
              text =
                  "Why is the charging current higher than what is stated on my charger? Fast charging protocols negotiate higher voltage to deliver more power, which triggers higher current readings at lower battery levels.",
              style = MaterialTheme.typography.bodyMedium,
              lineHeight = 20.sp,
          )
        }
      }
    }
  }
}
