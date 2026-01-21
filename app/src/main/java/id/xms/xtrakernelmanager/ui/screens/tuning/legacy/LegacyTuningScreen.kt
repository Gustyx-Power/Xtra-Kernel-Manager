package id.xms.xtrakernelmanager.ui.screens.tuning.legacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.PillCard
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@Composable
fun LegacyTuningScreen(
    viewModel: TuningViewModel,
    preferencesManager: PreferencesManager,
    isRootAvailable: Boolean,
    isLoading: Boolean,
    detectionTimeoutReached: Boolean,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onNavigate: (String) -> Unit,
) {
  val cpuClusters by viewModel.cpuClusters.collectAsState()

  LazyColumn(
      modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
      contentPadding = PaddingValues(vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        PillCard(text = stringResource(R.string.tuning_title))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          // Export Button
          FilledIconButton(
              onClick = onExportClick,
              enabled = isRootAvailable && !isLoading,
              colors =
                  IconButtonDefaults.filledIconButtonColors(
                      containerColor = MaterialTheme.colorScheme.primaryContainer,
                      contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                      disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                      disabledContentColor =
                          MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                  ),
          ) {
            Icon(
                imageVector = Icons.Default.Upload,
                contentDescription = stringResource(R.string.tuning_export),
            )
          }

          // Import Button
          FilledIconButton(
              onClick = onImportClick,
              enabled = isRootAvailable && !isLoading,
              colors =
                  IconButtonDefaults.filledIconButtonColors(
                      containerColor = MaterialTheme.colorScheme.secondaryContainer,
                      contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                      disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                      disabledContentColor =
                          MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                  ),
          ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = stringResource(R.string.tuning_import),
            )
          }
        }
      }
    }

    if (!isRootAvailable) {
      item {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
        ) {
          Column(
              modifier = Modifier.padding(20.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
              horizontalAlignment = Alignment.Start,
          ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
              Surface(
                  shape = MaterialTheme.shapes.medium,
                  color = MaterialTheme.colorScheme.error,
              ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.onError,
                )
              }
              Text(
                  text = stringResource(R.string.tuning_requires_root),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onErrorContainer,
              )
            }
            Text(
                text = stringResource(R.string.tuning_requires_root_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
          }
        }
      }
    } else if (isLoading && !detectionTimeoutReached) {
      item {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
          Box(
              modifier = Modifier.fillMaxWidth().height(200.dp),
              contentAlignment = Alignment.Center,
          ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
              CircularProgressIndicator()
              Text(
                  text = stringResource(R.string.loading),
                  style = MaterialTheme.typography.bodyLarge,
                  color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                  text = stringResource(R.string.tuning_detecting_hardware),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    } else {
      if (cpuClusters.isNotEmpty()) {
        item {
          LegacyCPUControl(viewModel = viewModel, onClick = { onNavigate("legacy_cpu_settings") })
        }
      }
      item { LegacyGPUControl(viewModel = viewModel) }
      item { LegacyThermalControl(viewModel = viewModel) }
      item { LegacyRAMControl(viewModel = viewModel) }
      item { LegacyAdditionalControl(viewModel = viewModel) }
      item {
        val availableGovernors = cpuClusters.firstOrNull()?.availableGovernors ?: emptyList()
        LegacyPerAppProfile(
            preferencesManager = preferencesManager,
            availableGovernors = availableGovernors,
        )
      }
    }
  }
}
