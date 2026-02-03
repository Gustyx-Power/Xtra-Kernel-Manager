package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialog
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialogButton
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

@Composable
fun LiquidNetworkSettings(viewModel: TuningViewModel) {
    val currentTCP by viewModel.currentTCPCongestion.collectAsState()
    val availableTCP by viewModel.availableTCPCongestion.collectAsState()
    val currentDNS by viewModel.currentDNS.collectAsState()
    val availableDNS = viewModel.availableDNS
    val currentHostname by viewModel.currentHostname.collectAsState()

    var showTCPDialog by remember { mutableStateOf(false) }
    var showDNSDialog by remember { mutableStateOf(false) }
    var showHostnameDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Hostname Card
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showHostnameDialog = true }
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Smartphone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.liquid_network_hostname),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentHostname.ifEmpty { "android" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // TCP Congestion Card
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showTCPDialog = true }
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Wifi,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.liquid_network_tcp_congestion),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentTCP.ifEmpty { "Not Set" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Private DNS Card
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showDNSDialog = true }
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.liquid_network_private_dns),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentDNS.ifEmpty { "Automatic" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Hostname Dialog
    if (showHostnameDialog) {
        var hostnameInput by remember { mutableStateOf(currentHostname) }
        LiquidDialog(
            onDismissRequest = { showHostnameDialog = false },
            title = "Set Hostname",
            content = {
                OutlinedTextField(
                    value = hostnameInput,
                    onValueChange = { hostnameInput = it },
                    label = { Text("Hostname") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                LiquidDialogButton(
                    text = stringResource(R.string.save),
                    onClick = {
                        viewModel.setHostname(hostnameInput)
                        showHostnameDialog = false
                    },
                    isPrimary = true
                )
            },
            dismissButton = {
                LiquidDialogButton(
                    text = stringResource(R.string.cancel),
                    onClick = { showHostnameDialog = false }
                )
            }
        )
    }

    // TCP Dialog
    if (showTCPDialog) {
        LiquidSelectionDialog(
            title = "TCP Congestion Control",
            items = availableTCP,
            currentValue = currentTCP,
            onDismiss = { showTCPDialog = false },
            onSelect = { tcp ->
                viewModel.setTCPCongestion(tcp)
                showTCPDialog = false
            }
        )
    }

    // DNS Dialog
    if (showDNSDialog) {
        LiquidSelectionDialog(
            title = "Private DNS Provider",
            items = availableDNS.map { it.first },
            currentValue = currentDNS,
            onDismiss = { showDNSDialog = false },
            onSelect = { dnsName ->
                val dnsEntry = availableDNS.find { it.first == dnsName }
                if (dnsEntry != null) {
                    viewModel.setPrivateDNS(dnsEntry.first, dnsEntry.second)
                }
                showDNSDialog = false
            }
        )
    }
}

@Composable
private fun LiquidSelectionDialog(
    title: String,
    items: List<String>,
    currentValue: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    LiquidDialog(
        onDismissRequest = onDismiss,
        title = title,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    val isSelected = item == currentValue

                    Surface(
                        onClick = { onSelect(item) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            LiquidDialogButton(
                text = stringResource(R.string.liquid_dialog_close),
                onClick = onDismiss,
                isPrimary = true
            )
        }
    )
}
