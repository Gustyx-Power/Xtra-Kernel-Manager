package id.xms.xtrakernelmanager.ui.screens.home.components.material

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.ui.components.DeviceSilhouette

@Composable
fun MaterialDeviceCard(systemInfo: SystemInfo) {
  Card(
      modifier = Modifier.fillMaxWidth().animateContentSize(),
      shape = MaterialTheme.shapes.extraLarge,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              text = android.os.Build.MANUFACTURER.uppercase(),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              letterSpacing = 2.sp,
              fontWeight = FontWeight.Bold,
          )

          // Smart Badge: Board/SoC
          Surface(
              color =
                  MaterialTheme.colorScheme.onPrimaryContainer.copy(
                      alpha = 0.1f
                  ),
              shape = CircleShape,
          ) {
            Text(
                text = android.os.Build.BOARD.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text =
                systemInfo.deviceModel
                    .replace(android.os.Build.MANUFACTURER, "", ignoreCase = true)
                    .trim()
                    .ifBlank { stringResource(id.xms.xtrakernelmanager.R.string.material_device_unknown_model) },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        Text(
            text = android.os.Build.DEVICE.ifBlank { stringResource(id.xms.xtrakernelmanager.R.string.material_device_unknown) },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.05f),
            shape = MaterialTheme.shapes.large,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(end = 100.dp),
        ) {
          Text(
              text = systemInfo.kernelVersion.ifBlank { stringResource(id.xms.xtrakernelmanager.R.string.material_device_unknown) },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
              fontFamily = FontFamily.Monospace,
              maxLines = 1,
              overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          )
        }
      }

      // Device Silhouette with Wallpaper
      Box(
          modifier =
              Modifier.align(Alignment.BottomEnd)
                  .padding(end = 24.dp)
                  .offset(y = 12.dp)
      ) {
        DeviceSilhouette(
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f),
            showWallpaper = true,
        )
      }
    }
  }
}
