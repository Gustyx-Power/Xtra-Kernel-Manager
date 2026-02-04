package id.xms.xtrakernelmanager.ui.screens.tuning.material.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@Composable
fun AdditionalControlCard(viewModel: TuningViewModel) {
  val ioSchedulers by viewModel.availableIOSchedulers.collectAsState()
  val currentIOScheduler by viewModel.currentIOScheduler.collectAsState()
  val ioSetOnBoot by viewModel.preferencesManager.getIOSetOnBoot().collectAsState(initial = false)
  val tcpSetOnBoot by viewModel.preferencesManager.getTCPSetOnBoot().collectAsState(initial = false)
  val additionalSetOnBoot by viewModel.preferencesManager.getAdditionalSetOnBoot().collectAsState(initial = false)

  var expanded by remember { mutableStateOf(false) }

  Card(
      modifier = Modifier.fillMaxWidth().animateContentSize().clickable { expanded = !expanded },
      shape = RoundedCornerShape(24.dp),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      // Header
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
              imageVector = Icons.Rounded.Build,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(24.dp),
          )
          Spacer(modifier = Modifier.width(16.dp))
          Column {
            Text(
                text = stringResource(R.string.material_additional_control),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.material_io_scheduler),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      AnimatedVisibility(visible = expanded) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          Spacer(modifier = Modifier.height(16.dp))

          // IO Scheduler Section
          Text(
              text = stringResource(R.string.material_io_scheduler),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(8.dp))

          var dropdownExpanded by remember { mutableStateOf(false) }

          Surface(
              modifier = Modifier.fillMaxWidth().clickable { dropdownExpanded = true },
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surface,
          ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Storage,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = currentIOScheduler.ifEmpty { stringResource(R.string.material_dialog_none) },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
              }
              Icon(
                  Icons.Rounded.ArrowDropDown,
                  null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest),
            ) {
              ioSchedulers.forEach { scheduler ->
                DropdownMenuItem(
                    text = { Text(scheduler) },
                    onClick = {
                      viewModel.setIOScheduler(scheduler)
                      dropdownExpanded = false
                    },
                    colors =
                        MenuDefaults.itemColors(
                            textColor =
                                if (scheduler == currentIOScheduler)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                        ),
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Set on Boot Section
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          Spacer(modifier = Modifier.height(16.dp))

          Text(
              text = stringResource(R.string.set_on_boot),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(8.dp))

          // I/O Set on Boot Toggle
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
                text = stringResource(R.string.material_io_scheduler),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Switch(
                checked = ioSetOnBoot,
                onCheckedChange = { viewModel.setIOSetOnBoot(it) },
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // TCP Set on Boot Toggle
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
                text = "TCP Congestion",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Switch(
                checked = tcpSetOnBoot,
                onCheckedChange = { viewModel.setTCPSetOnBoot(it) },
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Additional Set on Boot Toggle (Global)
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
                text = "Network Settings",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Switch(
                checked = additionalSetOnBoot,
                onCheckedChange = { viewModel.setAdditionalSetOnBoot(it) },
            )
          }
        }
      }
    }
  }
}
