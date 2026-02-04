package id.xms.xtrakernelmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import kotlinx.coroutines.launch

enum class DPIMode {
    SYSTEM,      // Always use system DPI (responsive to user changes)
    SMART,       // Smart detection (current behavior)
    FORCE_410    // Always force 410 DPI (old behavior)
}

@Composable
fun DPISettingsDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    preferencesManager: PreferencesManager
) {
    if (!isVisible) return
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedMode by remember { mutableStateOf(DPIMode.SMART) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load current preference
    LaunchedEffect(Unit) {
        // You'll need to add this to PreferencesManager
        // selectedMode = preferencesManager.getDPIMode().first()
        isLoading = false
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "DPI Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Choose how the app handles screen density:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(
                        modifier = Modifier.selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DPIModeOption(
                            mode = DPIMode.SMART,
                            title = "Smart (Recommended)",
                            description = "Tablets and high-res phones use system DPI, older phones use fixed DPI",
                            selected = selectedMode == DPIMode.SMART,
                            onSelect = { selectedMode = DPIMode.SMART }
                        )
                        
                        DPIModeOption(
                            mode = DPIMode.SYSTEM,
                            title = "Always Use System DPI",
                            description = "Fully responsive to DPI changes in Developer Options",
                            selected = selectedMode == DPIMode.SYSTEM,
                            onSelect = { selectedMode = DPIMode.SYSTEM }
                        )
                        
                        DPIModeOption(
                            mode = DPIMode.FORCE_410,
                            title = "Force 410 DPI",
                            description = "Fixed DPI for all devices (old behavior)",
                            selected = selectedMode == DPIMode.FORCE_410,
                            onSelect = { selectedMode = DPIMode.FORCE_410 }
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            scope.launch {
                                // Save preference and restart app
                                // preferencesManager.setDPIMode(selectedMode)
                                
                                // Show restart dialog
                                // You might want to show a dialog asking user to restart app
                                onDismiss()
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text("Apply & Restart")
                    }
                }
                
                if (selectedMode == DPIMode.SYSTEM) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "💡 Tip",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "You can change DPI in Developer Options > Smallest width to make UI larger or smaller",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DPIModeOption(
    mode: DPIMode,
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}