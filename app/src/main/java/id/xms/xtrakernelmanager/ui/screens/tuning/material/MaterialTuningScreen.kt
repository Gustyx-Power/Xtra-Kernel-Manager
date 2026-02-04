package id.xms.xtrakernelmanager.ui.screens.tuning.material

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.screens.tuning.material.components.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MaterialTuningScreen(
    viewModel: TuningViewModel,
    preferencesManager: PreferencesManager? = null,
    onNavigate: (String) -> Unit = {},
    onExportConfig: () -> Unit = {},
    onImportConfig: () -> Unit = {},
) {

  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
  var networkCardExpanded by remember { mutableStateOf(false) }
  var thermalCardExpanded by remember { mutableStateOf(false) }
  // cpuCardExpanded was unused

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        TopAppBar(
            modifier = Modifier.offset(y = (-24).dp),
            title = { Text(text = stringResource(R.string.material_tuning_title), fontWeight = FontWeight.SemiBold, fontSize = 24.sp) },
            actions = {
              var showMenu by remember { mutableStateOf(false) }

              Box {
                IconButton(onClick = { showMenu = true }) {
                  Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(id.xms.xtrakernelmanager.R.string.options_menu))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    shape = RoundedCornerShape(12.dp),
                ) {
                  DropdownMenuItem(
                      text = { Text(stringResource(id.xms.xtrakernelmanager.R.string.import_profile)) },
                      onClick = {
                        showMenu = false
                        onImportConfig()
                      },
                      leadingIcon = { Icon(Icons.Rounded.FolderOpen, contentDescription = null) },
                  )
                  DropdownMenuItem(
                      text = { Text(stringResource(id.xms.xtrakernelmanager.R.string.export_profile)) },
                      onClick = {
                        showMenu = false
                        onExportConfig()
                      },
                      leadingIcon = { Icon(Icons.Rounded.Save, contentDescription = null) },
                  )
                }
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
        )
      },
  ) { paddingValues ->
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
        verticalItemSpacing = 16.dp,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
      // 1. Hero Card
      item(span = StaggeredGridItemSpan.FullLine) { HeroDeviceCard(viewModel) }

      if (thermalCardExpanded) {
        item(span = StaggeredGridItemSpan.FullLine, key = "thermal_card") {
          ExpandableThermalCard(
              viewModel = viewModel,
              expanded = true,
              onExpandChange = { thermalCardExpanded = it },
              onClickNav = { onNavigate("thermal_tuning") },
              topLeftContent = {
                ExpandableCPUCard(
                    viewModel = viewModel,
                    onClickNav = { onNavigate("cpu_tuning") },
                )
              },
          )
        }
      } else {
        item(key = "cpu_card") {
          ExpandableCPUCard(
              viewModel = viewModel,
              onClickNav = { onNavigate("cpu_tuning") },
          )
        }
        item(key = "thermal_card") {
          ExpandableThermalCard(
              viewModel = viewModel,
              expanded = false,
              onExpandChange = { thermalCardExpanded = it },
              onClickNav = { onNavigate("thermal_tuning") },
          )
        }
      }

      item(span = StaggeredGridItemSpan.FullLine) { DashboardProfileCard(viewModel) }
      item(span = StaggeredGridItemSpan.FullLine) { ExpandableGPUCard(viewModel = viewModel) }

      if (networkCardExpanded) {
        item(span = StaggeredGridItemSpan.FullLine) {
          ExpandableNetworkCard(
              viewModel = viewModel,
              expanded = true,
              onExpandChange = {
                networkCardExpanded = it // Toggle
              },
              onClickNav = { onNavigate("network_tuning") },
              topRightContent = {
                DashboardMemoryCard(
                    onClickNav = { onNavigate("memory_tuning") },
                )
              },
          )
        }
      } else {
        item(span = StaggeredGridItemSpan.SingleLane) {
          ExpandableNetworkCard(
              viewModel = viewModel,
              expanded = false,
              onExpandChange = { networkCardExpanded = it },
              onClickNav = { onNavigate("network_tuning") },
          )
        }
        item(span = StaggeredGridItemSpan.SingleLane) {
          DashboardMemoryCard(
              onClickNav = { onNavigate("memory_tuning") },
          )
        }
      }

      // Add Additional Control Card at the bottom
      item(span = StaggeredGridItemSpan.FullLine) { AdditionalControlCard(viewModel = viewModel) }
    }
  }
}
