package id.xms.xtrakernelmanager.ui.screens.home.components.material

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MaterialTempTile(
    modifier: Modifier = Modifier,
    cpuTemp: Int,
    gpuTemp: Int,
    pmicTemp: Int,
    thermalTemp: Int,
    color: Color,
) {
  Card(
      modifier = modifier.animateContentSize(),
      shape = MaterialTheme.shapes.extraLarge,
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
          ),
  ) {
    Column(
        modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Header
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
      ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(36.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.Thermostat,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp),
            )
          }
        }

        Surface(
            color = color.copy(alpha = 0.1f),
            shape = CircleShape,
        ) {
          Text(
              text = "TEMP",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = color,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Temperature List
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        TempRow(label = "CPU", value = "${cpuTemp}°C")
        TempRow(label = "GPU", value = "${gpuTemp}°C")
        TempRow(label = "PMIC", value = "${pmicTemp}°C")
        TempRow(label = "Thermal", value = "${thermalTemp}°C")
      }
    }
  }
}

@Composable
private fun TempRow(label: String, value: String) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
        fontWeight = FontWeight.SemiBold,
    )
    Text(
        text = value,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        fontWeight = FontWeight.Bold,
    )
  }
}
