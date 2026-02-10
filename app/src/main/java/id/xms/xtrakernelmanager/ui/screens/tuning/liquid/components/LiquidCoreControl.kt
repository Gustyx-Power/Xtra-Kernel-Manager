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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
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

  Surface(
      modifier = modifier.fillMaxWidth().animateContentSize(),
      onClick = { expanded = !expanded },
      shape = RoundedCornerShape(24.dp),
      color = Color(0xFF3B82F6).copy(alpha = 0.15f) // Blue glass for core control
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
              color = Color(0xFF3B82F6).copy(alpha = 0.3f), // Stronger blue glass for icon
              modifier = Modifier.size(56.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                  imageVector = Icons.Rounded.Memory,
                  contentDescription = null,
                  tint = Color(0xFF3B82F6),
                  modifier = Modifier.size(28.dp),
              )
            }
          }

          Column {
            Text(
                text = stringResource(R.string.liquid_cpu_core_management),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
              Surface(
                  shape = CircleShape,
                  color = if (onlineCores == totalCores) 
                      Color(0xFF10B981).copy(alpha = 0.3f) // Green glass for all online
                  else 
                      Color(0xFF8B5CF6).copy(alpha = 0.3f), // Purple glass for partial
              ) {
                Text(
                    text = "$onlineCores/$totalCores",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = if (onlineCores == totalCores) 
                        Color(0xFF10B981)
                    else 
                        Color(0xFF8B5CF6),
                )
              }
              Text(
                  text = stringResource(R.string.liquid_cpu_cores_online),
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color.White.copy(alpha = 0.7f),
              )
            }
          }
        }

        // Expand Icon
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.1f), // Subtle white glass
            modifier = Modifier.size(40.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp).rotate(rotationAngle),
            )
          }
        }
      }

      // Expanded Content
      AnimatedVisibility(visible = expanded) {
        Column(modifier = Modifier.padding(top = 20.dp)) {
          HorizontalDivider(
              color = Color.White.copy(alpha = 0.2f),
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
      color = Color(0xFF10B981).copy(alpha = 0.1f), // Green glass effect for cluster sections
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Cluster Header
      Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = stringResource(R.string.liquid_cpu_cluster_format, clusterNumber),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF10B981),
        )
        Surface(
            shape = CircleShape,
            color = Color(0xFF10B981).copy(alpha = 0.3f), // Green glass badge
        ) {
          Text(
              text = stringResource(R.string.liquid_cpu_cores_format, cores.size),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              color = Color(0xFF10B981),
          )
        }
      }

      // Core Items
      cores.forEachIndexed { index, core ->
        LiquidCoreItem(core = core, viewModel = viewModel)

        if (index != cores.lastIndex) {
          HorizontalDivider(
              color = Color.White.copy(alpha = 0.1f),
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
                          Color(0xFF10B981).copy(alpha = 0.2f) // Green glass for online
                      else
                          Color(0xFFEF4444).copy(alpha = 0.2f) // Red glass for offline
                  ),
          contentAlignment = Alignment.Center,
      ) {
        if (isOnline) {
          Icon(
              Icons.Rounded.Memory,
              contentDescription = null,
              tint = Color(0xFF10B981),
              modifier = Modifier.size(22.dp)
          )
        } else {
          Icon(
              Icons.Rounded.PowerOff,
              contentDescription = null,
              tint = Color(0xFFEF4444),
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
              text = stringResource(R.string.liquid_cpu_core_format, core.coreNumber),
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
              color = if (isOnline)
                  Color.White
              else
                  Color.White.copy(alpha = 0.5f)
          )

          // Core 0 Badge
          if (isCore0) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF8B5CF6).copy(alpha = 0.2f), // Purple glass for primary badge
            ) {
              Text(
                  text = stringResource(R.string.liquid_cpu_primary),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  color = Color(0xFF8B5CF6),
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
                color = Color(0xFF10B981),
                modifier = Modifier.size(6.dp)
            ) {}
            Text(
                text = "${core.currentFreq} MHz",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF10B981),
                fontWeight = FontWeight.Medium,
            )
          }
        } else if (!isOnline) {
          Text(
              text = stringResource(R.string.liquid_cpu_offline),
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFFEF4444),
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
        text = stringResource(R.string.liquid_cpu_primary_core_cannot_disabled),
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 58.dp, top = 6.dp)
    )
  }
}
