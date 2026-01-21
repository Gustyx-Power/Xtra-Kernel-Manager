package id.xms.xtrakernelmanager.ui.screens.tuning.material.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@Composable
fun HeroDeviceCard(viewModel: TuningViewModel) {
  val cpuLoad by viewModel.cpuLoad.collectAsState()

  // Real device info from Android Build API
  val manufacturer = android.os.Build.MANUFACTURER.uppercase()
  val model = android.os.Build.MODEL
  val codename = android.os.Build.DEVICE.uppercase()
  val board = android.os.Build.BOARD.lowercase()

  // Format CPU load as percentage
  val loadDisplay = "${cpuLoad.toInt()}% Load"

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
            text = manufacturer,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shape = RoundedCornerShape(100),
            modifier = Modifier.padding(top = 4.dp),
        ) {
          Text(
              text = codename,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
          )
        }
      }

      Column {
        Text(
            text = model,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "$board • $loadDisplay",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
