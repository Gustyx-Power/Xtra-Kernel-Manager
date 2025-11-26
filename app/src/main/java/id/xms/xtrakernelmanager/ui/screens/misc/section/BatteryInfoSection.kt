package id.xms.xtrakernelmanager.ui.screens.misc.section

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.service.BatteryInfoService
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import kotlinx.coroutines.launch

@Composable
fun BatteryInfoSection(
    viewModel: MiscViewModel
) {
    val context = LocalContext.current.applicationContext
    val showBatteryNotif by viewModel.showBatteryNotif.collectAsState()
    val scope = rememberCoroutineScope()

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryStd,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = stringResource(R.string.battery_guru),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.battery_guru_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Switch(
                checked = showBatteryNotif,
                onCheckedChange = { checked ->
                    viewModel.setShowBatteryNotification(checked)
                    scope.launch {
                        if (checked) {
                            context.startService(Intent(context, BatteryInfoService::class.java))
                        } else {
                            context.stopService(Intent(context, BatteryInfoService::class.java))
                        }
                    }
                }
            )
        }
    }
}
