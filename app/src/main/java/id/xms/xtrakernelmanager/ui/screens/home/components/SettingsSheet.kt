package id.xms.xtrakernelmanager.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import kotlinx.coroutines.launch

@Composable
fun SettingsSheet(preferencesManager: PreferencesManager, onDismiss: () -> Unit) {
  val scope = rememberCoroutineScope()
  val currentLayout by preferencesManager.getLayoutStyle().collectAsState(initial = "material")

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
            text = "Quick Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Customize your experience",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    // Layout Selection
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Text(
          text = "Layout Style",
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
            onClick = { scope.launch { preferencesManager.setLayoutStyle("material") } },
        )

        LayoutOptionCard(
            title = "Liquid Glass",
            description = "Glass inspired by iOS 26",
            isSelected = currentLayout == "liquid",
            modifier = Modifier.weight(1f),
            onClick = { scope.launch { preferencesManager.setLayoutStyle("liquid") } },
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
fun LayoutOptionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
  val containerColor =
      if (isSelected) MaterialTheme.colorScheme.primaryContainer
      else MaterialTheme.colorScheme.surfaceContainer
  val contentColor =
      if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
      else MaterialTheme.colorScheme.onSurface

  Surface(
      onClick = onClick,
      shape = RoundedCornerShape(16.dp),
      color = containerColor,
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
            color = contentColor,
        )
        if (isSelected) {
          Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              tint = contentColor,
              modifier = Modifier.size(20.dp),
          )
        }
      }
      Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = contentColor.copy(alpha = 0.7f),
      )
    }
  }
}
