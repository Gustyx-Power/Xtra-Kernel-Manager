package id.xms.xtrakernelmanager.ui.screens.tuning

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard

@OptIn(ExperimentalMaterial3Api::class)  // Tambahkan annotation ini
@Composable
fun AdditionalControlSection(viewModel: TuningViewModel) {
    val ioSchedulers by viewModel.availableIOSchedulers.collectAsState()
    val tcpCongestion by viewModel.availableTCPCongestion.collectAsState()

    GlassmorphicCard {
        Text(
            text = stringResource(R.string.additional_control),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        var selectedIO by remember { mutableStateOf("") }
        var ioExpanded by remember { mutableStateOf(false) }

        if (ioSchedulers.isNotEmpty()) {
            LaunchedEffect(ioSchedulers) {
                selectedIO = ioSchedulers.firstOrNull() ?: ""
            }

            Text(
                text = stringResource(R.string.io_scheduler),
                style = MaterialTheme.typography.bodyMedium
            )

            ExposedDropdownMenuBox(
                expanded = ioExpanded,
                onExpandedChange = { ioExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedIO,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ioExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = ioExpanded,
                    onDismissRequest = { ioExpanded = false }
                ) {
                    ioSchedulers.forEach { scheduler ->
                        DropdownMenuItem(
                            text = { Text(scheduler) },
                            onClick = {
                                selectedIO = scheduler
                                viewModel.setIOScheduler(scheduler)
                                ioExpanded = false
                            }
                        )
                    }
                }
            }
        }

        var selectedTCP by remember { mutableStateOf("") }
        var tcpExpanded by remember { mutableStateOf(false) }

        if (tcpCongestion.isNotEmpty()) {
            LaunchedEffect(tcpCongestion) {
                selectedTCP = tcpCongestion.firstOrNull() ?: ""
            }

            Text(
                text = stringResource(R.string.tcp_congestion),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = tcpExpanded,
                onExpandedChange = { tcpExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedTCP,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tcpExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = tcpExpanded,
                    onDismissRequest = { tcpExpanded = false }
                ) {
                    tcpCongestion.forEach { congestion ->
                        DropdownMenuItem(
                            text = { Text(congestion) },
                            onClick = {
                                selectedTCP = congestion
                                viewModel.setTCPCongestion(congestion)
                                tcpExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.perf_controller),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.setPerfMode("battery") },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.perf_battery))
            }
            Button(
                onClick = { viewModel.setPerfMode("balance") },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.perf_balance))
            }
            Button(
                onClick = { viewModel.setPerfMode("performance") },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.perf_performance))
            }
        }
    }
}
