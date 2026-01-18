package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialBatteryScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit
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
      

      
      item { Spacer(modifier = Modifier.height(24.dp)) }
    }
  }
}



@Composable
fun HistoryChartCard() {
  // Card background simulating the dark grey/blue from screenshot
  val cardColor = Color(0xFF1E1F24) 
  
  Card(
      shape = RoundedCornerShape(32.dp),
      colors = CardDefaults.cardColors(containerColor = cardColor),
      modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(24.dp)) {
      // Header with Status Pill
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
      ) {
          Text(
              text = "History",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color.White
          )
          
          // Status Pill (Mock data: Charging)
          Surface(
              color = Color(0xFF009688).copy(alpha = 0.1f),
              shape = RoundedCornerShape(50),
              border = BorderStroke(1.dp, Color(0xFF009688).copy(alpha = 0.2f)),
          ) {
              Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                  Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF009688)))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                      text = "Charging",
                      style = MaterialTheme.typography.labelMedium,
                      color = Color(0xFF009688),
                      fontWeight = FontWeight.Bold,
                  )
              }
          }
      }

      Spacer(modifier = Modifier.height(32.dp))

      // Bar Chart Area
      // Mock Data 
      val bars = remember { List(10) { (20..90).random() } }
      
      Row(
          modifier = Modifier.fillMaxWidth().height(140.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom,
      ) {
        val days = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su", "Mo", "Tu", "We")
        bars.forEachIndexed { index, heightPercent ->
             val isSelected = index == bars.lastIndex - 3 // Just to simulate selection
             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight(heightPercent / 100f)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color(0xFF7986CB) else Color(0xFF3F455A)
                        )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = days[index],
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        }
      }
      
      Spacer(modifier = Modifier.height(8.dp))
      
      // Play Icon Divider
      Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
           Icon(
               Icons.Rounded.PlayArrow, 
               null, 
               tint = Color.White.copy(alpha = 0.6f),
               modifier = Modifier.size(20.dp).graphicsLayer(rotationZ = 90f) // Standard rotation logic
           )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Nested Stats Card
      Card(
          colors = CardDefaults.cardColors(
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
          SummaryStat("Charging", "0 %")
          VerticalDivider(
              modifier = Modifier.height(32.dp),
              thickness = 1.dp,
              color = Color.White.copy(alpha = 0.1f)
          )
          SummaryStat("Discharging", "-- %")
           VerticalDivider(
              modifier = Modifier.height(32.dp),
              thickness = 1.dp,
              color = Color.White.copy(alpha = 0.1f)
          )
          SummaryStat("Sessions", "1")
        }
      }
      
      Spacer(modifier = Modifier.height(16.dp))
      Text(
          "Last 10 days",
          style = MaterialTheme.typography.bodyMedium,
          color = Color.White.copy(alpha = 0.5f),
      )
    }
  }
}

@Composable
fun ElectricCurrentCard() {
  Card(
      shape = RoundedCornerShape(32.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F24)),
      modifier = Modifier.fillMaxWidth().height(260.dp),
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      Text(
          text = "Electric Current",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = Color.White
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
          text = "457 mA",
          style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
          fontWeight = FontWeight.Medium,
          color = Color.White
      )

      Spacer(modifier = Modifier.weight(1f))

      // Graph Placeholder - Solid block
      Box(
          modifier = Modifier
              .fillMaxWidth()
              .height(60.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFF2D2E36))
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
          text = "1.9 W • 4233 mV",
          style = MaterialTheme.typography.bodyMedium,
          color = Color.White.copy(alpha = 0.6f),
      )
    }
  }
}

@Composable
fun CurrentSessionCard() {
  Card(
      shape = RoundedCornerShape(32.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F24)),
      modifier = Modifier.fillMaxWidth().height(260.dp),
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      Text(
            text = "Current Session",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
      )
      
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
    Icon(
        imageVector = icon, 
        contentDescription = null, 
        modifier = Modifier.size(20.dp),
        tint = Color.White
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(
          text = label, 
          style = MaterialTheme.typography.labelSmall, 
          fontWeight = FontWeight.Bold,
          color = Color.White
      )
      Text(
          text = value, 
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.7f)
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
        color = Color.White
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = value, 
        style = MaterialTheme.typography.titleMedium,
        color = Color.White.copy(alpha = 0.7f)
    )
  }
}
