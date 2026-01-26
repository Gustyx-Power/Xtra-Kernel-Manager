package id.xms.xtrakernelmanager.ui.components.liquid

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Example usage of LiquidDialog
 * 
 * Replace standard AlertDialog with LiquidDialog for liquid glass effect
 */

// BEFORE (Standard AlertDialog):
/*
AlertDialog(
    onDismissRequest = { showDialog = false },
    title = { Text("Dialog Title") },
    text = { Text("Dialog content goes here") },
    confirmButton = {
        TextButton(onClick = { 
            // Handle confirm
            showDialog = false
        }) {
            Text("Confirm")
        }
    },
    dismissButton = {
        TextButton(onClick = { showDialog = false }) {
            Text("Cancel")
        }
    }
)
*/

// AFTER (LiquidDialog):
/*
LiquidDialog(
    onDismissRequest = { showDialog = false },
    title = "Dialog Title",
    content = {
        Text(
            text = "Dialog content goes here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
        )
    },
    confirmButton = {
        LiquidDialogButton(
            text = "Confirm",
            onClick = {
                // Handle confirm
                showDialog = false
            },
            isPrimary = true
        )
    },
    dismissButton = {
        LiquidDialogButton(
            text = "Cancel",
            onClick = { showDialog = false },
            isPrimary = false
        )
    }
)
*/

/**
 * Example 1: Simple confirmation dialog
 */
@Composable
fun ExampleSimpleDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        LiquidDialog(
            onDismissRequest = onDismiss,
            title = "Confirm Action",
            content = {
                Text(
                    text = "Are you sure you want to proceed with this action?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                )
            },
            confirmButton = {
                LiquidDialogButton(
                    text = "Confirm",
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    isPrimary = true
                )
            },
            dismissButton = {
                LiquidDialogButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    isPrimary = false
                )
            }
        )
    }
}

/**
 * Example 2: Dialog with multiple content sections
 */
@Composable
fun ExampleComplexDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    if (showDialog) {
        LiquidDialog(
            onDismissRequest = onDismiss,
            title = "Settings",
            content = {
                Text(
                    text = "Section 1",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Some description text here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Section 2",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "More content here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                )
            },
            confirmButton = {
                LiquidDialogButton(
                    text = "Apply",
                    onClick = {
                        onApply()
                        onDismiss()
                    },
                    isPrimary = true
                )
            },
            dismissButton = {
                LiquidDialogButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    isPrimary = false
                )
            }
        )
    }
}

/**
 * Example 3: Dialog with only confirm button (no dismiss)
 */
@Composable
fun ExampleSingleButtonDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        LiquidDialog(
            onDismissRequest = onDismiss,
            title = "Information",
            content = {
                Text(
                    text = "This is an informational message.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                )
            },
            confirmButton = {
                LiquidDialogButton(
                    text = "OK",
                    onClick = onDismiss,
                    isPrimary = true
                )
            },
            dismissButton = null // No dismiss button
        )
    }
}
