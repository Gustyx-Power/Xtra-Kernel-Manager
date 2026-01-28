package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.domain.usecase.FunctionalRomUseCase
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.PillCard
import id.xms.xtrakernelmanager.ui.screens.misc.section.BatteryInfoSection
import id.xms.xtrakernelmanager.ui.screens.misc.section.DisplaySection
import id.xms.xtrakernelmanager.ui.screens.misc.section.GameControlSection
import id.xms.xtrakernelmanager.ui.screens.misc.material.MaterialMiscScreen
import id.xms.xtrakernelmanager.ui.screens.misc.liquid.LiquidMiscScreen

@Composable
fun MiscScreen(
    viewModel: MiscViewModel,
    onNavigateToFunctionalRom: () -> Unit = {},
    onNavigateToAppPicker: () -> Unit = {},
) {
  // Collect Layout Style Preference
  val layoutStyle by viewModel.layoutStyle.collectAsState()

  // Switch between designs
  if (layoutStyle == "material") {
    MaterialMiscScreen(
        viewModel = viewModel,
        onNavigate = { route ->
          when (route) {
            "functional_rom" -> onNavigateToFunctionalRom()
            "app_picker" -> onNavigateToAppPicker()
          }
        },
    )
  } else {
    LiquidMiscScreen(viewModel, onNavigateToFunctionalRom, onNavigateToAppPicker)
  }
}

// Legacy implementation kept for reference - now using LiquidMiscScreen
