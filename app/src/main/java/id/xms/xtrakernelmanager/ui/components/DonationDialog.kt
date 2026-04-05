package id.xms.xtrakernelmanager.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.donation.ClassicDonationDialog
import id.xms.xtrakernelmanager.ui.components.donation.FrostedDonationDialog
import id.xms.xtrakernelmanager.ui.components.donation.MaterialDonationDialog

/**
 * Wrapper for donation dialog that selects the appropriate style
 * based on the current layout preference
 */
@Composable
fun DonationDialog(
    onDismiss: () -> Unit,
    onSupportClick: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = PreferencesManager(context)
    val layoutStyle by preferencesManager.getLayoutStyle().collectAsState(initial = "material")

    when (layoutStyle) {
        "classic" -> ClassicDonationDialog(
            onDismiss = onDismiss,
            onSupportClick = onSupportClick
        )
        "liquid" -> FrostedDonationDialog(
            onDismiss = onDismiss,
            onSupportClick = onSupportClick
        )
        else -> MaterialDonationDialog(
            onDismiss = onDismiss,
            onSupportClick = onSupportClick
        )
    }
}
