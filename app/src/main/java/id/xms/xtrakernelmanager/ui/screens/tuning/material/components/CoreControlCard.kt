package id.xms.xtrakernelmanager.ui.screens.tuning.material.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PowerOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.model.CoreInfo
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@Composable
fun CoreControlCard(
    cores: List<CoreInfo>,
    viewModel: TuningViewModel,
    modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }
  val onlineCores = cores.count { it.isOnline }
  val totalCores = cores.size

  Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      modifier = modifier.fillMaxWidth().animateContentSize(),
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      // Header
      Row(
          modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
              modifier =
                  Modifier.size(48.dp)
                      .clip(RoundedCornerShape(14.dp))
                      .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
              contentAlignment = Alignment.Center,
          ) {
            Icon(
                Icons.Rounded.Memory,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(16.dp))
          Column {
            Text(
                text = "Core Management",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = "$onlineCores/$totalCores cores online",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (onlineCores == totalCores) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.tertiaryContainer,
        ) {
          Text(
              text = if (expanded) "COLLAPSE" else "EXPAND",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              color = if (onlineCores == totalCores) 
                  MaterialTheme.colorScheme.onPrimaryContainer 
              else 
                  MaterialTheme.colorScheme.onTertiaryContainer,
          )
        }
      }

      // Expanded Content
      AnimatedVisibility(visible = expanded) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
          HorizontalDivider(
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
              modifier = Modifier.padding(bottom = 16.dp)
          )
          
          // Group cores by cluster
          val coresByCluster = cores.groupBy { it.cluster }
          
          coresByCluster.forEach { (clusterNum, clusterCores) ->
            ClusterCoreSection(
                clusterNumber = clusterNum,
                cores = clusterCores,
                viewModel = viewModel
            )
            
            if (clusterNum != coresByCluster.keys.last()) {
              Spacer(modifier = Modifier.height(16.dp))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ClusterCoreSection(
    clusterNumber: Int,
    cores: List<CoreInfo>,
    viewModel: TuningViewModel
) {
  Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Cluster Header
      Text(
          text = "Cluster $clusterNumber",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(bottom = 12.dp)
      )
      
      // Core Items
      cores.forEach { core ->
        CoreItem(core = core, viewModel = viewModel)
        
        if (core != cores.last()) {
          HorizontalDivider(
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
              modifier = Modifier.padding(vertical = 8.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun CoreItem(
    core: CoreInfo,
    viewModel: TuningViewModel
) {
  var isOnline by remember(core.isOnline) { mutableStateOf(core.isOnline) }
  val isCore0 = core.coreNumber == 0

  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f)
    ) {
      // Core Icon
      Box(
          modifier =
              Modifier.size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(
                      if (isOnline) 
                          MaterialTheme.colorScheme.primaryContainer 
                      else 
                          MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                  ),
          contentAlignment = Alignment.Center,
      ) {
        if (isOnline) {
          Icon(
              Icons.Rounded.Memory,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(18.dp)
          )
        } else {
          Icon(
              Icons.Rounded.PowerOff,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.size(18.dp)
          )
        }
      }
      
      Column {
        Text(
            text = "Core ${core.coreNumber}",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = if (isOnline) 
                MaterialTheme.colorScheme.onSurface 
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        
        if (isOnline && core.currentFreq > 0) {
          Text(
              text = "${core.currentFreq} MHz",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
          )
        } else if (!isOnline) {
          Text(
              text = "Offline",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error,
          )
        }
      }
    }
    
    // Switch (disabled for Core 0)
    Switch(
        checked = isOnline,
        onCheckedChange = { newState ->
          if (!isCore0) {
            isOnline = newState
            viewModel.setCpuCoreOnline(core.coreNumber, newState)
          }
        },
        enabled = !isCore0,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primary,
            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
            disabledCheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            disabledCheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        )
    )
  }
  
  // Core 0 warning
  if (isCore0) {
    Text(
        text = "Core 0 cannot be disabled",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.padding(start = 48.dp, top = 4.dp)
    )
  }
}
