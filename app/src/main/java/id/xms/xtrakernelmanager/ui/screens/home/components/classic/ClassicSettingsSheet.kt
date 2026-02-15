package id.xms.xtrakernelmanager.ui.screens.home.components.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import kotlinx.coroutines.launch

@Composable
fun ClassicSettingsSheet(preferencesManager: PreferencesManager, currentLayout: String, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    // Removed internal collection, using passed currentLayout
    val isLayoutSwitching by preferencesManager.isLayoutSwitching().collectAsState(initial = false)
    var targetLayoutSelection by remember { mutableStateOf(currentLayout) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ClassicColors.Background) // Ensure background matches classic theme
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(ClassicColors.Surface, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    tint = ClassicColors.Primary,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Quick Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface,
                )
                Text(
                    text = "Classic Mode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.Secondary,
                )
            }
        }

        Divider(color = ClassicColors.SurfaceVariant)

        // Layout Selection
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Layout Style",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = ClassicColors.OnSurface,
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
               ClassicLayoutOption(
                   title = "Material",
                   isSelected = currentLayout == "material",
                   modifier = Modifier.weight(1f),
                   enabled = !isLayoutSwitching,
                   onClick = {
                       haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                       targetLayoutSelection = "material"
                       scope.launch { preferencesManager.setLayoutStyle("material") }
                   }
               )
               ClassicLayoutOption(
                   title = "Liquid",
                   isSelected = currentLayout == "liquid",
                   modifier = Modifier.weight(1f),
                   enabled = !isLayoutSwitching,
                   onClick = {
                       haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                       targetLayoutSelection = "liquid"
                       scope.launch { preferencesManager.setLayoutStyle("liquid") }
                   }
               )
               ClassicLayoutOption(
                   title = "Classic",
                   isSelected = currentLayout == "classic",
                   modifier = Modifier.weight(1f),
                   enabled = !isLayoutSwitching,
                   onClick = {
                       haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                       targetLayoutSelection = "classic"
                       scope.launch { preferencesManager.setLayoutStyle("classic") }
                   }
               )
            }
            
            if (isLayoutSwitching) {
                 val displayLayout = targetLayoutSelection
                 val targetLayoutName = when (displayLayout) {
                    "liquid" -> "Liquid Glass"
                    "material" -> "Material"
                    else -> "Classic"
                }
                 LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = ClassicColors.Primary,
                    trackColor = ClassicColors.SurfaceVariant
                )
                Text("Switching to $targetLayoutName...", color = ClassicColors.Secondary, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ClassicLayoutOption(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) ClassicColors.Primary else ClassicColors.SurfaceVariant
    val textColor = if (isSelected) ClassicColors.Primary else ClassicColors.OnSurfaceVariant
    
    Box(
        modifier = modifier
            .height(60.dp)
            .border(2.dp, borderColor, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(ClassicColors.Surface)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
         Text(
             text = title,
             style = MaterialTheme.typography.titleMedium,
             fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
             color = textColor
         )
    }
}
