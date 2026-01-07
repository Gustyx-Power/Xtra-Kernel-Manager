package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MaterialBatteryScreen(onBack: () -> Unit) {
  Scaffold(
      topBar = {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(onClick = onBack) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Monitoring", style = MaterialTheme.typography.titleLarge)
          Spacer(modifier = Modifier.weight(1f))
          IconButton(onClick = { /* TODO: Notification Settings */ }) {
            Icon(Icons.Rounded.Settings, contentDescription = "Settings")
          }
        }
      }
  ) { paddingValues ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // 1. History Chart Card
      item { HistoryChartCard() }

      // 2. Current & Session Cards (Row)
      item {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          Box(modifier = Modifier.weight(1f)) { ElectricCurrentCard() }
          Box(modifier = Modifier.weight(1f)) { CurrentSessionCard() }
        }
      }
    }
  }
}

@Composable
fun HistoryChartCard() {
  Card(
      shape = RoundedCornerShape(24.dp),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f)
          ), // Lighter background
      modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(24.dp)) {
      Text(
          text = "History",
          style = MaterialTheme.typography.titleLarge, // Larger title
          fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(16.dp))

      // Legend Row - Badges
      Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        LegendBadge(color = Color(0xFF00796B), label = "Charging")
        LegendBadge(color = Color(0xFFC62828), label = "Discharging")
        LegendBadge(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            label = "No Data",
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Divider
      Box(
          modifier =
              Modifier.fillMaxWidth()
                  .height(1.dp)
                  .background(MaterialTheme.colorScheme.outlineVariant)
      )

      Spacer(modifier = Modifier.height(32.dp))

      // Bar Chart - Fully Rounded Bars
      Row(
          modifier = Modifier.fillMaxWidth().height(140.dp),
          horizontalArrangement = Arrangement.SpaceBetween, // Even spacing
          verticalAlignment = Alignment.Bottom,
      ) {
        val days = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su", "Mo", "Tu", "We")
        days.forEach { day ->
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier =
                    Modifier.width(22.dp) // Slightly wider
                        .height((40..100).random().dp)
                        .clip(CircleShape) // Fully rounded
                        .background(
                            MaterialTheme.colorScheme.primaryContainer
                        ) // Monet pink/purple usually
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = day,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      // Summary Stats - Card Style
      Card(
          colors =
              CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
              ), // Subtle background
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          SummaryStat("Charging", "0 %")
          VerticalDivider(
              modifier = Modifier.height(32.dp).width(1.dp),
              color = MaterialTheme.colorScheme.outlineVariant,
          )
          SummaryStat("Discharging", "-- %")
          VerticalDivider(
              modifier = Modifier.height(32.dp).width(1.dp),
              color = MaterialTheme.colorScheme.outlineVariant,
          )
          SummaryStat("Sessions", "1")
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
      Text(
          "Last 10 days",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
fun ElectricCurrentCard() {
  Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      modifier = Modifier.fillMaxWidth().height(240.dp), // Taller to match session card/needs
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      Text(
          text = "Electric Current",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
          text = "457 mA",
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Medium,
      )

      Spacer(modifier = Modifier.weight(1f))

      // Graph Placeholder - Solid rounded block as per image
      Box(
          modifier =
              Modifier.fillMaxWidth()
                  .height(60.dp) // Reduced height slightly to be neater
                  .clip(RoundedCornerShape(12.dp))
                  .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
      )

      Spacer(modifier = Modifier.height(16.dp)) // Increased spacing

      Text(
          text = "1.9 W • 4233 mV",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
fun CurrentSessionCard() {
  Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      modifier = Modifier.fillMaxWidth().height(240.dp), // Match height
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = "Current Session",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Icon(
            Icons.Rounded.ArrowBack,
            contentDescription = null,
            modifier = Modifier.size(16.dp).alpha(0f),
        ) // Invisible spacer if needed
      }
      Spacer(modifier = Modifier.height(24.dp))

      StatRowCompact(icon = Icons.Rounded.WbSunny, label = "Screen On", value = "6m 30d")
      Spacer(modifier = Modifier.height(20.dp))
      StatRowCompact(icon = Icons.Rounded.NightsStay, label = "Screen Off", value = "2m 23d")
      Spacer(modifier = Modifier.height(20.dp))
      StatRowCompact(icon = Icons.Rounded.BatteryStd, label = "Charged", value = "0% • 41 mAh")
    }
  }
}

@Composable
fun StatRowCompact(icon: ImageVector, label: String, value: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
      Text(text = value, style = MaterialTheme.typography.bodySmall)
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
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
      Spacer(modifier = Modifier.width(8.dp))
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
    Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    Text(text = value, style = MaterialTheme.typography.bodyMedium)
  }
}
