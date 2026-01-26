package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.screens.tuning.liquid.LiquidAdditionalControl
import id.xms.xtrakernelmanager.ui.screens.tuning.liquid.LiquidPerAppProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidAdditionalSettingsScreen(
    viewModel: TuningViewModel, 
    preferencesManager: PreferencesManager,
    onNavigateBack: () -> Unit
) {
    val cpuClusters by viewModel.cpuClusters.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Additional Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LiquidAdditionalControl(viewModel = viewModel)
            
            val availableGovernors = cpuClusters.firstOrNull()?.availableGovernors ?: emptyList()
            LiquidPerAppProfile(
                preferencesManager = preferencesManager,
                availableGovernors = availableGovernors,
            )
        }
    }
}
