package id.xms.xtrakernelmanager.ui.screens.tuning.material.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SdCard
import androidx.compose.runtime.Composable

@Composable
fun DashboardMemoryCard(
    onClickNav: () -> Unit,
) {
  DashboardNavCard(
      title = "Memory",
      subtitle = "ZRAM & Swap",
      icon = Icons.Rounded.SdCard,
      onClick = onClickNav,
  )
}
