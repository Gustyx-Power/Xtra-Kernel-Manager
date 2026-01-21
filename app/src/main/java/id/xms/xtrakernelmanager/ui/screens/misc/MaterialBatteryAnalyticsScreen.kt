package id.xms.xtrakernelmanager.ui.screens.misc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Battery Analytics Screen
 * Accessed from: Battery Monitor > Electric Current Card
 * Shows: Current/Voltage/Power stats, Current Flow Chart, Min/Max/Avg
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialBatteryAnalyticsScreen(
    viewModel: MiscViewModel,
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Battery Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Battery Status Card (Big Current, Voltage, Power)
            item { 
                Spacer(modifier = Modifier.height(8.dp))
                BatteryAnalyticsStatusCard(viewModel) 
            }

            // 2. Current Flow Chart
            item { CurrentFlowCard() }

            // 3. Stats Row (Min, Avg, Max)
            item { BatteryStatsRow(viewModel) }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
