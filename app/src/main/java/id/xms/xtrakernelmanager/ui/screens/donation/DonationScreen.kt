package id.xms.xtrakernelmanager.ui.screens.donation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager

/**
 * Wrapper for donation screen that selects the appropriate style
 * based on the current layout preference
 */
@Composable
fun DonationScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val preferencesManager = PreferencesManager(context)
    val layoutStyle by preferencesManager.getLayoutStyle().collectAsState(initial = "material")

    when (layoutStyle) {
        "classic" -> ClassicDonationScreen(onNavigateBack = onNavigateBack)
        "liquid" -> FrostedDonationScreen(onNavigateBack = onNavigateBack)
        else -> MaterialDonationScreen(onNavigateBack = onNavigateBack)
    }
}
