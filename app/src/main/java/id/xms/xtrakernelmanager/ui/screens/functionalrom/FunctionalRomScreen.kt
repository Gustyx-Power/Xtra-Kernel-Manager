package id.xms.xtrakernelmanager.ui.screens.functionalrom

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FunctionalRomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShimokuRom: () -> Unit = {},
    onNavigateToHideAccessibility: () -> Unit = {},
    onNavigateToDisplaySize: () -> Unit,
    viewModel: FunctionalRomViewModel,
) {
    val layoutStyle by viewModel.layoutStyle.collectAsStateWithLifecycle()

    if (layoutStyle == "material") {
        MaterialFunctionalRomScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToShimokuRom = onNavigateToShimokuRom,
            onNavigateToHideAccessibility = onNavigateToHideAccessibility,
            onNavigateToDisplaySize = onNavigateToDisplaySize,
            viewModel = viewModel
        )
    } else {
        LiquidFunctionalRomScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToShimokuRom = onNavigateToShimokuRom,
            onNavigateToHideAccessibility = onNavigateToHideAccessibility,
            onNavigateToDisplaySize = onNavigateToDisplaySize,
            viewModel = viewModel
        )
    }
}