package id.xms.xtrakernelmanager.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.screens.home.components.classic.ClassicSettingsSheet
import id.xms.xtrakernelmanager.ui.screens.home.components.liquid.LiquidSettingsSheet
import id.xms.xtrakernelmanager.ui.screens.home.components.material.MaterialSettingsSheet

@Composable
fun SettingsSheet(preferencesManager: PreferencesManager, currentLayout: String, onDismiss: () -> Unit) {
    when (currentLayout) {
        "classic" -> ClassicSettingsSheet(preferencesManager, currentLayout, onDismiss)
        "liquid" -> LiquidSettingsSheet(preferencesManager, currentLayout, onDismiss)
        else -> MaterialSettingsSheet(preferencesManager, currentLayout, onDismiss)
    }
}

// Kept for compatibility if used elsewhere, although mostly used by MaterialSettingsSheet now
@Composable
fun LayoutOptionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
  val containerColor =
      if (isSelected) MaterialTheme.colorScheme.primaryContainer
      else MaterialTheme.colorScheme.surfaceContainer
  val contentColor =
      if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
      else MaterialTheme.colorScheme.onSurface

  Surface(
      onClick = if (enabled) onClick else { {} },
      shape = RoundedCornerShape(16.dp),
      color = containerColor.copy(alpha = if (enabled) 1f else 0.6f),
      modifier = modifier,
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor.copy(alpha = if (enabled) 1f else 0.6f),
        )
        if (isSelected) {
          Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              tint = contentColor.copy(alpha = if (enabled) 1f else 0.6f),
              modifier = Modifier.size(20.dp),
          )
        }
      }
      Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = contentColor.copy(alpha = if (enabled) 0.7f else 0.4f),
      )
    }
  }
}
