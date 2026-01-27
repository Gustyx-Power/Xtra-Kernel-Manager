package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PowerOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.model.CoreInfo
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidToggle
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@Composable
fun LiquidCoreControl(
    cores: List<CoreInfo>,
    viewModel: TuningViewModel,
    modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }
  val onlineCores = cores.count { it.isOnline }
  val totalCores = cores.size
  val rotationAngle by animateFloatAsState(
      targetValue = if (expanded) 180f else 0f,
      animationSpec = tween(300)
  )

  GlassmorphicCard(
      modifier = modifier.fillMaxWidth().animateContentSize(),
      onClick = { expanded = !expanded }
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      // Header
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          // Icon Container
          Surface(
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
              modifier = Modifier.size(56.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                  imageVector = Icons.Rounded.Memory,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(28.dp),
              )
            }
          }

          Column {
            Text(
                text = "Core Management",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
              Surface(
                  shape = CircleShape,
                  color = if (onlineCores == totalCores) 
                      MaterialTheme.colorScheme.primaryContainer 
                  else 
                      MaterialTheme.colorScheme.tertiaryContainer,
              ) {
                Text(
                    text = "$onlineCores/$totalCores",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = if (onlineCores == totalCores) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onTertiaryContainer,
                )
              }
              Text(
                  text = "cores online",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        // Expand Icon
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(40.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp).rotate(rotationAngle),
            )
          }
        }
      }

      // Expanded Content
      AnimatedVisibility(visible = expanded) {
        Column(modifier = Modifier.padding(top = 20.dp)) {
          HorizontalDivider(
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
              modifier = Modifier.padding(bottom = 16.dp)
          )

          // Group cores by cluster
          val coresByCluster = cores.groupBy { it.cluster }

          coresByCluster.forEach { (clusterNum, clusterCores) ->
            LiquidClusterCoreSection(
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
private fun LiquidClusterCoreSection(
    clusterNumber: Int,
    cores: List<CoreInfo>,
    viewModel: TuningViewModel
) {
  Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Cluster Header
      Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = "Cluster $clusterNumber",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        ) {
          Text(
              text = "${cores.size} cores",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
      }

      // Core Items
      cores.forEachIndexed { index, core ->
        LiquidCoreItem(core = core, viewModel = viewModel)

        if (index != cores.lastIndex) {
          HorizontalDivider(
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
              modifier = Modifier.padding(vertical = 12.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun LiquidCoreItem(
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
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.weight(1f)
    ) {
      // Core Icon with Status
      Box(
          modifier =
              Modifier.size(44.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(
                      if (isOnline)
                          MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
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
              modifier = Modifier.size(22.dp)
          )
        } else {
          Icon(
              Icons.Rounded.PowerOff,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.size(22.dp)
          )
        }
      }

      Column(modifier = Modifier.weight(1f)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              text = "Core ${core.coreNumber}",
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
              color = if (isOnline)
                  MaterialTheme.colorScheme.onSurface
              else
                  MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
          )

          // Core 0 Badge
          if (isCore0) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
            ) {
              Text(
                  text = "PRIMARY",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  color = MaterialTheme.colorScheme.onTertiaryContainer,
              )
            }
          }
        }

        // Frequency Info
        if (isOnline && core.currentFreq > 0) {
          Row(
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(6.dp)
            ) {}
            Text(
                text = "${core.currentFreq} MHz",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
          }
        } else if (!isOnline) {
          Text(
              text = "Offline",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error,
              fontWeight = FontWeight.Medium,
          )
        }
      }
    }

    // Liquid Toggle
    LiquidToggle(
        checked = isOnline,
        onCheckedChange = { newState ->
          if (!isCore0) {
            isOnline = newState
            viewModel.setCpuCoreOnline(core.coreNumber, newState)
          }
        },
        enabled = !isCore0,
        modifier = Modifier.padding(start = 8.dp)
    )
  }

  // Core 0 Info Message
  if (isCore0) {
    Text(
        text = "Primary core cannot be disabled",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 58.dp, top = 6.dp)
    )
  }
}
