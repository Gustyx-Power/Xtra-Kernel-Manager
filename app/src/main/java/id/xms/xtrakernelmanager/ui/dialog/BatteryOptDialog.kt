package id.xms.xtrakernelmanager.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun BatteryOptDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onExit: () -> Unit,
    showExitButton: Boolean = false
) {
    AlertDialog(
        onDismissRequest = { /* Prevent dismissal by tapping outside */ },
        title = { Text(if (showExitButton) "Permissions Required" else "Battery Optimization") },
        text = {
            Text(
                if (showExitButton) {
                    "XKM requires battery optimization exclusion to function properly. Without this permission, " +
                    "the app cannot maintain your thermal settings in the background. Please grant the permission " +
                    "or exit the app."
                } else {
                    "XKM needs to be excluded from battery optimization to maintain your thermal settings " +
                    "in the background. Please allow this permission for the app to work properly."
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            if (showExitButton) {
                TextButton(onClick = onExit) {
                    Text("Exit App")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Later")
                }
            }
        }
    )
}
