package id.xms.xtrakernelmanager.ui.screens.home.components.material

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.CompactMorphingSwitcher
import id.xms.xtrakernelmanager.ui.screens.home.components.LayoutOptionCard
import kotlinx.coroutines.launch

@Composable
fun MaterialSettingsSheet(preferencesManager: PreferencesManager, currentLayout: String, onDismiss: () -> Unit) {
  val scope = rememberCoroutineScope()
  val haptic = LocalHapticFeedback.current
  // Removed internal collection, using passed currentLayout
  val isLayoutSwitching by preferencesManager.isLayoutSwitching().collectAsState(initial = false)
  var targetLayoutSelection by remember { mutableStateOf(currentLayout) }

  Column(
      modifier = Modifier.fillMaxWidth().padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    // Header
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      Box(
          modifier =
              Modifier.size(48.dp)
                  .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
          contentAlignment = Alignment.Center,
      ) {
        Icon(
            imageVector = Icons.Rounded.Settings,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
      }
      Spacer(modifier = Modifier.width(16.dp))
      Column {
        Text(
            text = stringResource(id.xms.xtrakernelmanager.R.string.settings_quick_settings),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(id.xms.xtrakernelmanager.R.string.settings_customize_experience),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    // Layout Selection
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Text(
          text = stringResource(id.xms.xtrakernelmanager.R.string.settings_layout_style),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
      )

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LayoutOptionCard(
            title = "Material",
            description = "Modern & Clean",
            isSelected = currentLayout == "material",
            modifier = Modifier.weight(1f),
            onClick = { 
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              targetLayoutSelection = "material"
              scope.launch { preferencesManager.setLayoutStyle("material") } 
            },
            enabled = !isLayoutSwitching
        )

        LayoutOptionCard(
            title = "Liquid",
            description = "Glass Theme",
            isSelected = currentLayout == "liquid",
            modifier = Modifier.weight(1f),
            onClick = { 
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              targetLayoutSelection = "liquid"
              scope.launch { preferencesManager.setLayoutStyle("liquid") } 
            },
            enabled = !isLayoutSwitching
        )

        LayoutOptionCard(
            title = "Classic",
            description = "Solid & Simple",
            isSelected = currentLayout == "classic",
            modifier = Modifier.weight(1f),
            onClick = { 
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              targetLayoutSelection = "classic"
              scope.launch { preferencesManager.setLayoutStyle("classic") } 
            },
            enabled = !isLayoutSwitching
        )
      }
      
      val displayLayout = targetLayoutSelection
      val targetLayoutName = when (displayLayout) {
          "liquid" -> "Liquid Glass"
          "material" -> "Material"
          else -> "Classic"
      }
      CompactMorphingSwitcher(
          isLoading = isLayoutSwitching,
          targetLayout = targetLayoutName,
          modifier = Modifier.fillMaxWidth()
      )
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
