package id.xms.xtrakernelmanager.ui.screens.tuning.material.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@Composable
fun ExpandableCPUCard(
    viewModel: TuningViewModel,
    onClickNav: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val cpuClusters by viewModel.cpuClusters.collectAsState()
  val currentGovernor =
      cpuClusters.firstOrNull()?.governor?.replaceFirstChar { it.uppercase() } ?: "—"

  DashboardNavCard(
      title = "CPU",
      subtitle = "Clock & Governor",
      icon = Icons.Rounded.Memory,
      badgeText = currentGovernor, // Now shows real governor
      onClick = onClickNav,
  )
}
