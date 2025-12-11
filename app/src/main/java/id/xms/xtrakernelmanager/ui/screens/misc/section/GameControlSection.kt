package id.xms.xtrakernelmanager.ui.screens.misc.section

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.LottieSwitchControlled
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel

@Composable
fun GameControlSection(
    viewModel: MiscViewModel
) {
    val context = LocalContext.current
    val enableGameOverlay by viewModel.enableGameOverlay.collectAsState()
    val needsOverlayPermission by viewModel.needsOverlayPermission.collectAsState()

    // Check overlay permission
    var hasOverlayPermission by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }

    // Overlay permission launcher
    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Check permission again after returning from settings
        hasOverlayPermission = Settings.canDrawOverlays(context)
        viewModel.clearOverlayPermissionRequest()
        
        // If permission was granted, try to enable overlay again
        if (hasOverlayPermission && !enableGameOverlay) {
            viewModel.setEnableGameOverlay(true)
        }
    }

    // Launch permission settings when needed
    LaunchedEffect(needsOverlayPermission) {
        if (needsOverlayPermission) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:${context.packageName}".toUri()
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.game_control),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.game_control_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.game_control_tap_fps),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))

                LottieSwitchControlled(
                    checked = enableGameOverlay,
                    onCheckedChange = { enabled ->
                        viewModel.setEnableGameOverlay(enabled)
                    },
                    width = 80.dp,
                    height = 40.dp,
                    scale = 2.2f
                )
            }

            // Show permission button only if not granted
            if (!hasOverlayPermission) {
                Button(
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            "package:${context.packageName}".toUri()
                        )
                        overlayPermissionLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.grant_overlay_permission))
                }
            }
        }
    }
}
