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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShimokuRomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayIntegrity: () -> Unit = {},
    onNavigateToXiaomiTouch: () -> Unit = {},
    viewModel: FunctionalRomViewModel,
) {
  val uiState by viewModel.uiState.collectAsState()

  // Force refresh rate value selector state
  var showRefreshRateDialog by remember { mutableStateOf(false) }

  // Charging limit value selector state
  var showChargingLimitDialog by remember { mutableStateOf(false) }

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
          PillCard(text = "Shimoku ROM Features")
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
            else MaterialTheme.colorScheme.errorContainer
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
                imageVector = if (romInfo.isShimokuRom) Icons.Default.Verified else Icons.Default.Warning,
                contentDescription = null,
                tint = if (romInfo.isShimokuRom) 
                  MaterialTheme.colorScheme.onPrimaryContainer 
                else MaterialTheme.colorScheme.onErrorContainer
              )
              Text(
                text = if (romInfo.isShimokuRom) "Shimoku ROM Detected" else "Non-Shimoku ROM",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (romInfo.isShimokuRom) 
                  MaterialTheme.colorScheme.onPrimaryContainer 
                else MaterialTheme.colorScheme.onErrorContainer
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "${romInfo.displayName} • Android ${romInfo.androidVersion}",
              style = MaterialTheme.typography.bodyMedium,
              color = if (romInfo.isShimokuRom) 
                MaterialTheme.colorScheme.onPrimaryContainer 
              else MaterialTheme.colorScheme.onErrorContainer
            )
            
            if (!romInfo.isShimokuRom) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Features on this screen are only available on Shimoku ROM",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
            }
          }
        }
      }
    }

    // PlayIntegrity Category
    item { 
      Spacer(modifier = Modifier.height(8.dp))
      CategoryHeader(title = stringResource(R.string.category_play_integrity)) 
    }
    item {
      ClickableFeatureCard(
          title = stringResource(R.string.play_integrity_fix),
          description = "Play Integrity Fix untuk bypass deteksi root dan bootloader unlock",
          icon = Icons.Default.Security,
          onClick = onNavigateToPlayIntegrity,
          enabled = uiState.isShimokuRom && uiState.isVipCommunity,
          statusText = if (uiState.playIntegrityFixEnabled) "Enabled" else "Disabled"
      )
    }

    // Touch & Kernel Category
    item {
      Spacer(modifier = Modifier.height(8.dp))
      CategoryHeader(title = stringResource(R.string.category_touch_kernel))
    }
    item {
      ClickableFeatureCard(
          title = stringResource(R.string.xiaomi_touch_settings),
          description = "Pengaturan touch sensitivity dan game mode untuk perangkat Xiaomi",
          icon = Icons.Default.TouchApp,
          onClick = onNavigateToXiaomiTouch,
          enabled = uiState.isShimokuRom && uiState.isVipCommunity,
          statusText = if (uiState.touchGameModeEnabled || uiState.touchActiveModeEnabled) "Configured" else "Default"
      )
    }

    // Native Features Category (Shimoku ROM specific)
    if (uiState.isShimokuRom) {
      item {
        Spacer(modifier = Modifier.height(8.dp))
        CategoryHeader(title = "Native Features")
      }

      // Bypass Charging
      if (uiState.bypassChargingAvailable) {
        item {
          FeatureCard(
              title = stringResource(R.string.bypass_charging),
              description = "Bypass charging untuk mencegah overcharge saat gaming",
              icon = Icons.Default.BatteryChargingFull,
              isEnabled = uiState.bypassChargingEnabled,
              onToggle = { viewModel.setBypassCharging(it) },
              enabled = uiState.isVipCommunity,
          )
        }
      }

      // Charging Limit
      if (uiState.chargingLimitAvailable) {
        item {
          FeatureCard(
              title = stringResource(R.string.charging_limit),
              description = "Batasi charging hingga ${uiState.chargingLimitValue}% untuk kesehatan baterai",
              icon = Icons.Default.BatteryAlert,
              isEnabled = uiState.chargingLimitEnabled,
              onToggle = { viewModel.setChargingLimit(it) },
              enabled = uiState.isVipCommunity,
              onClick = { showChargingLimitDialog = true }
          )
        }
      }

      // Double Tap to Wake
      if (uiState.dt2wAvailable) {
        item {
          FeatureCard(
              title = stringResource(R.string.double_tap_wake),
              description = "Double tap untuk membangunkan layar",
              icon = Icons.Default.TouchApp,
              isEnabled = uiState.doubleTapWakeEnabled,
              onToggle = { viewModel.setDoubleTapToWake(it) },
              enabled = uiState.isVipCommunity,
          )
        }
      }

      // Force Refresh Rate
      item {
        FeatureCard(
            title = stringResource(R.string.force_refresh_rate),
            description = "Paksa refresh rate ke ${uiState.forceRefreshRateValue}Hz",
            icon = Icons.Default.Speed,
            isEnabled = uiState.forceRefreshRateEnabled,
            onToggle = { viewModel.setForceRefreshRate(it) },
            enabled = uiState.isVipCommunity,
            onClick = { showRefreshRateDialog = true }
        )
      }

      // Property-based Features (Shimoku's area)
      item {
        Spacer(modifier = Modifier.height(8.dp))
        CategoryHeader(title = "Property Features")
      }

      // Touch Boost
      item {
        FeatureCard(
            title = stringResource(R.string.touch_boost),
            description = "Boost performa touch untuk gaming",
            icon = Icons.Default.Speed,
            isEnabled = uiState.touchBoostEnabled,
            onToggle = { viewModel.setTouchBoost(it) },
            enabled = uiState.isVipCommunity,
        )
      }

      // Display Features
      item {
        Spacer(modifier = Modifier.height(8.dp))
        CategoryHeader(title = "Display Features")
      }

      // Unlock Nits
      item {
        FeatureCard(
            title = "Unlock Additional Nits",
            description = "Buka kunci brightness tambahan untuk layar lebih terang",
            icon = Icons.Default.Brightness7,
            isEnabled = uiState.unlockNitsEnabled,
            onToggle = { viewModel.setUnlockNits(it) },
            enabled = uiState.isVipCommunity,
        )
      }

      // Dynamic Refresh Rate
      item {
        FeatureCard(
            title = stringResource(R.string.dynamic_refresh_rate),
            description = "Refresh rate dinamis untuk menghemat baterai",
            icon = Icons.Default.DisplaySettings,
            isEnabled = uiState.dynamicRefreshRateEnabled,
            onToggle = { viewModel.setDynamicRefreshRate(it) },
            enabled = uiState.isVipCommunity,
        )
      }

      // DC Dimming
      item {
        FeatureCard(
            title = stringResource(R.string.dc_dimming),
            description = "DC Dimming untuk mengurangi flicker pada brightness rendah",
            icon = Icons.Default.Brightness4,
            isEnabled = uiState.dcDimmingEnabled,
            onToggle = { viewModel.setDcDimming(it) },
            enabled = uiState.isVipCommunity,
        )
      }

      // System Features
      item {
        Spacer(modifier = Modifier.height(8.dp))
        CategoryHeader(title = "System Features")
      }

      // Smart Charging
      item {
        FeatureCard(
            title = stringResource(R.string.smart_charging),
            description = "Smart charging untuk optimasi kesehatan baterai",
            icon = Icons.Default.Psychology,
            isEnabled = uiState.smartChargingEnabled,
            onToggle = { viewModel.setSmartCharging(it) },
            enabled = uiState.isVipCommunity,
        )
      }

      // Fix DT2W
      item {
        FeatureCard(
            title = stringResource(R.string.fix_dt2w),
            description = "Fix Double Tap to Wake untuk perangkat yang tidak support",
            icon = Icons.Default.Build,
            isEnabled = uiState.fixDt2wEnabled,
            onToggle = { viewModel.setFixDt2w(it) },
            enabled = uiState.isVipCommunity,
        )
      }
    }
  }

  // Dialogs
  if (showRefreshRateDialog) {
    RefreshRateDialog(
        currentValue = uiState.forceRefreshRateValue,
        onValueChange = { viewModel.setForceRefreshRateValue(it) },
        onDismiss = { showRefreshRateDialog = false }
    )
  }

  if (showChargingLimitDialog) {
    ChargingLimitDialog(
        currentValue = uiState.chargingLimitValue,
        onValueChange = { viewModel.setChargingLimitValue(it) },
        onDismiss = { showChargingLimitDialog = false }
    )
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

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    enabled: Boolean,
    onClick: (() -> Unit)? = null
) {
  Card(
      modifier = Modifier
          .fillMaxWidth()
          .alpha(if (enabled) 1f else 0.6f),
      onClick = onClick ?: {}
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
      }
      LottieSwitchControlled(
          checked = isEnabled,
          onCheckedChange = onToggle,
          enabled = enabled
      )
    }
  }
}

@Composable
private fun RefreshRateDialog(
    currentValue: Int,
    onValueChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
  val refreshRates = listOf(60, 90, 120, 144, 165, 240)
  
  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Select Refresh Rate") },
      text = {
        LazyColumn {
          items(refreshRates.size) { index ->
            val rate = refreshRates[index]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                  selected = currentValue == rate,
                  onClick = { 
                    onValueChange(rate)
                    onDismiss()
                  }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("${rate}Hz")
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
  )
}

@Composable
private fun ChargingLimitDialog(
    currentValue: Int,
    onValueChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
  val limits = listOf(80, 85, 90, 95)
  
  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Select Charging Limit") },
      text = {
        LazyColumn {
          items(limits.size) { index ->
            val limit = limits[index]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                  selected = currentValue == limit,
                  onClick = { 
                    onValueChange(limit)
                    onDismiss()
                  }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("${limit}%")
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
  )
}