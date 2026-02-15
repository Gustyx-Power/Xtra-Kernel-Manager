package id.xms.xtrakernelmanager.ui.screens.home.components.liquid

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.CompactMorphingSwitcher
import kotlinx.coroutines.launch

@Composable
fun LiquidSettingsSheet(preferencesManager: PreferencesManager, currentLayout: String, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    // Removed internal collection, using passed currentLayout
    val isLayoutSwitching by preferencesManager.isLayoutSwitching().collectAsState(initial = false)

    // State to track the target layout name for the switcher
    var targetLayoutSelection by remember { mutableStateOf(currentLayout) }

    // Wrap the entire sheet content in a GlassmorphicCard to provide the glass background
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                GlassmorphicCard(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.settings_quick_settings),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.settings_customize_experience),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Layout Selection
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(R.string.settings_layout_style),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LiquidLayoutOption(
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
                    LiquidLayoutOption(
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
                    LiquidLayoutOption(
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
        }
    }
}

@Composable
fun LiquidLayoutOption(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(modifier = modifier.height(80.dp).clickable(enabled = enabled, onClick = onClick)) {
        GlassmorphicCard(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp)
        ) {
             Column(
                 modifier = Modifier.fillMaxSize(),
                 horizontalAlignment = Alignment.CenterHorizontally,
                 verticalArrangement = Arrangement.Center
             ) {
                 if (isSelected) {
                     Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                 }
                 Text(
                     text = title,
                     style = MaterialTheme.typography.bodyMedium,
                     fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                     color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                 )
             }
        }
    }
}
