package id.xms.xtrakernelmanager.ui.screens.tuning

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalControlSection(viewModel: TuningViewModel) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = null,
                    modifier = Modifier.size(27.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.thermal_control),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Tambahkan deskripsi/help tip
            Text(
                text = "Set the thermal mode to optimize device temperature and performance. Choose a preset according to your usage needs!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                modifier = Modifier.padding(top = 1.dp, bottom = 6.dp)
            )

            val prefsThermal by viewModel.preferencesManager.getThermalPreset().collectAsState(initial = "Not Set")
            val prefsOnBoot by viewModel.preferencesManager.getThermalSetOnBoot().collectAsState(initial = false)
            var showDialog by remember { mutableStateOf(false) }

            val presetMap = mapOf(
                "Not Set" to R.string.thermal_not_set,
                "Dynamic" to R.string.thermal_dynamic,
                "Incalls" to R.string.thermal_incalls,
                "Thermal 20" to R.string.thermal_20
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
                            shape = RoundedCornerShape(13.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Thermal Preset",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                        Text(
                            text = stringResource(presetMap[prefsThermal] ?: R.string.thermal_not_set),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if (showDialog) {
                AlertDialog(
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(17.dp),
                    onDismissRequest = { showDialog = false },
                    confirmButton = {},
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Pilih Thermal Preset",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            presetMap.forEach { (preset, stringRes) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (preset == prefsThermal)
                                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.18f)
                                            else
                                                Color.Transparent,
                                            shape = RoundedCornerShape(11.dp)
                                        )
                                        .clickable {
                                            viewModel.setThermalPreset(preset, prefsOnBoot)
                                            showDialog = false
                                        }
                                        .padding(vertical = 8.dp, horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(stringRes),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (preset == prefsThermal)
                                                FontWeight.Bold else FontWeight.Normal,
                                            color = if (preset == prefsThermal)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    if (preset == prefsThermal)
                                        Icon(
                                            imageVector = Icons.Default.Done,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                }
                            }
                        }
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Set on Boot",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = prefsOnBoot,
                    onCheckedChange = {
                        viewModel.setThermalPreset(prefsThermal, it)
                    }
                )
            }
        }
    }
}
