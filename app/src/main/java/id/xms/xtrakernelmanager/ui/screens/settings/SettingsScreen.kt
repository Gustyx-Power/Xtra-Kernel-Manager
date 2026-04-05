package id.xms.xtrakernelmanager.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.screens.settings.components.ClassicSettingsContent
import id.xms.xtrakernelmanager.ui.screens.settings.components.FrostedSettingsContent
import id.xms.xtrakernelmanager.ui.screens.settings.components.MaterialSettingsContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    onNavigateBack: () -> Unit,
    onNavigateToDonation: () -> Unit = {}
) {
    val layoutStyle by preferencesManager.getLayoutStyle().collectAsState(initial = "material")
    
    when (layoutStyle) {
        "classic" -> ClassicSettingsContent(
            preferencesManager = preferencesManager,
            currentLayout = layoutStyle,
            onNavigateBack = onNavigateBack,
            onNavigateToDonation = onNavigateToDonation
        )
        "liquid" -> FrostedSettingsContent(
            preferencesManager = preferencesManager,
            currentLayout = layoutStyle,
            onNavigateBack = onNavigateBack,
            onNavigateToDonation = onNavigateToDonation
        )
        else -> MaterialSettingsContent(
            preferencesManager = preferencesManager,
            currentLayout = layoutStyle,
            onNavigateBack = onNavigateBack,
            onNavigateToDonation = onNavigateToDonation
        )
    }
}
