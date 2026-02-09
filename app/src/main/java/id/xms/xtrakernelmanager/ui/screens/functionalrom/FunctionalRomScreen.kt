package id.xms.xtrakernelmanager.ui.screens.functionalrom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.LottieSwitchControlled
import id.xms.xtrakernelmanager.ui.components.PillCard
import id.xms.xtrakernelmanager.data.model.HideAccessibilityTab
import id.xms.xtrakernelmanager.data.model.HideAccessibilityConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunctionalRomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShimokuRom: () -> Unit = {},
    onNavigateToHideAccessibility: () -> Unit = {},
    viewModel: FunctionalRomViewModel,
) {
  val uiState by viewModel.uiState.collectAsState()

  // Loading state
  if (uiState.isLoading) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(R.string.loading_features),
            style = MaterialTheme.typography.bodyMedium,
        )
      }
    }
    return
  }

  LazyColumn(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Header with PillCard
    item {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          FilledIconButton(
              onClick = onNavigateBack,
              colors =
                  IconButtonDefaults.filledIconButtonColors(
                      containerColor = MaterialTheme.colorScheme.surfaceVariant,
                      contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  ),
              modifier = Modifier.size(40.dp),
          ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp),
            )
          }
          PillCard(text = stringResource(R.string.functional_rom_title))
        }
      }
    }

    // ROM Information Card
    item {
      uiState.romInfo?.let { romInfo ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(
            containerColor = if (romInfo.isShimokuRom) 
              MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surfaceVariant
          )
        ) {
          Column(
            modifier = Modifier.padding(16.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = if (romInfo.isShimokuRom) Icons.Default.Verified else Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = if (romInfo.isShimokuRom) 
                  MaterialTheme.colorScheme.onPrimaryContainer 
                else MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = romInfo.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Android ${romInfo.androidVersion} • ${romInfo.systemBrand}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Universal Features
    item { 
      Spacer(modifier = Modifier.height(8.dp))
      CategoryHeader(title = "Universal Features") 
    }
    item {
      ClickableFeatureCard(
          title = "Hide Accessibility Service",
          description = "Sistem tab untuk menyembunyikan aplikasi dari deteksi aksesibilitas (Universal - bekerja di semua ROM)",
          icon = Icons.Default.VisibilityOff,
          onClick = onNavigateToHideAccessibility,
          enabled = true, // Always enabled - universal feature
          statusText = if (uiState.hideAccessibilityConfig.isEnabled) {
            "${uiState.hideAccessibilityConfig.currentTab.displayName} • ${getTotalSelectedApps(uiState.hideAccessibilityConfig)} apps"
          } else "Disabled"
      )
    }

    // ROM-Specific Features
    item { 
      Spacer(modifier = Modifier.height(8.dp))
      CategoryHeader(title = "ROM-Specific Features") 
    }
    item {
      ClickableFeatureCard(
          title = "Shimoku ROM Features",
          description = "Fitur khusus untuk Shimoku ROM termasuk Play Integrity, Touch Settings, dan Native Features",
          icon = Icons.Default.Verified,
          onClick = onNavigateToShimokuRom,
          enabled = true, // Always accessible to show lock message for non-Shimoku ROMs
          statusText = if (uiState.isShimokuRom) "Available" else "Locked"
      )
    }
  }
}

@Composable
private fun CategoryHeader(title: String) {
  Text(
      text = title,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(horizontal = 4.dp)
  )
}

@Composable
private fun ClickableFeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    statusText: String = ""
) {
  Card(
      modifier = Modifier
          .fillMaxWidth()
          .alpha(if (enabled) 1f else 0.6f),
      onClick = if (enabled) onClick else { {} }
  ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (statusText.isNotEmpty()) {
          Text(
              text = statusText,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary
          )
        }
      }
      Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

private fun getTotalSelectedApps(config: HideAccessibilityConfig): Int {
  return config.appsToHide.size + config.detectorApps.size
}