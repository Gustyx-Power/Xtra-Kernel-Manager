package id.xms.xtrakernelmanager.ui.screens.home.components.material

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import id.xms.xtrakernelmanager.data.model.BatteryInfo
import java.util.Locale

@Composable
fun MaterialBatteryCard(batteryInfo: BatteryInfo) {
  Card(
      modifier = Modifier.fillMaxWidth().animateContentSize(),
      shape = MaterialTheme.shapes.extraLarge,
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer,
          ),
  ) {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
      // Header
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Box(
              modifier =
                  Modifier.size(36.dp)
                      .background(
                          MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                          MaterialTheme.shapes.medium,
                      ),
              contentAlignment = Alignment.Center,
          ) {
            Icon(
                imageVector = Icons.Rounded.BatteryChargingFull,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
          }
          Text(
              text = "Battery",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
          )
        }

        // Smart Badge: Technology
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = CircleShape,
        ) {
          Text(
              text = batteryInfo.technology.takeIf { it != "Unknown" } ?: "Li-ion",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          )
        }
      }

      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        BatterySilhouette(
            level = batteryInfo.level / 100f,
            isCharging = batteryInfo.status.contains("Charging", ignoreCase = true),
            color = MaterialTheme.colorScheme.primary,
        )

        Column {
          Text(
              text = "${batteryInfo.level}%",
              style = MaterialTheme.typography.displayMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              lineHeight = 1.em,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BatteryStatusChip(text = batteryInfo.status)
            BatteryStatusChip(
                text = "Health ${String.format(Locale.US, "%.0f", batteryInfo.healthPercent)}%"
            )
          }
        }
      }

      // Stats Grid (2x2)
      Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          val currentText =
              if (batteryInfo.currentNow >= 0) "+${batteryInfo.currentNow} mA"
              else "${batteryInfo.currentNow} mA"
          BatteryStatBox(label = "Current", value = currentText, modifier = Modifier.weight(1f))
          BatteryStatBox(
              label = "Voltage",
              value = "${batteryInfo.voltage} mV",
              modifier = Modifier.weight(1f),
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          BatteryStatBox(
              label = "Temperature",
              value = "${batteryInfo.temperature}°C",
              modifier = Modifier.weight(1f),
          )
          BatteryStatBox(
              label = "Cycle Count",
              value = "${batteryInfo.cycleCount}",
              modifier = Modifier.weight(1f),
          )
        }
      }
    }
  }
}

@Composable
private fun BatterySilhouette(level: Float, isCharging: Boolean, color: Color) {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    // Battery Cap
    Box(
        modifier =
            Modifier.size(20.dp, 4.dp)
                .background(
                    MaterialTheme.colorScheme.outlineVariant,
                    MaterialTheme.shapes.extraSmall,
                )
    )

    // Main Body
    Box(
        modifier =
            Modifier.size(50.dp, 80.dp)
                .border(4.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                .padding(4.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
      Box(
          modifier =
              Modifier.fillMaxWidth()
                  .fillMaxHeight(level)
                  .background(color, MaterialTheme.shapes.small)
      )
    }
  }
}
