package id.xms.xtrakernelmanager.ui.screens.tuning.material.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SdCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import id.xms.xtrakernelmanager.R

@Composable
fun DashboardMemoryCard(
    onClickNav: () -> Unit,
) {
  DashboardNavCard(
      title = stringResource(R.string.material_memory_title),
      subtitle = stringResource(R.string.material_memory_subtitle),
      icon = Icons.Rounded.SdCard,
      onClick = onClickNav,
  )
}
