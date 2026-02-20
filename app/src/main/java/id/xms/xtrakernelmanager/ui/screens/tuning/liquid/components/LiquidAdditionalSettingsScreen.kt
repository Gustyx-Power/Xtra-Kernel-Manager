package id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.WavyBlobOrnament
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.screens.tuning.liquid.LiquidIOControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidAdditionalSettingsScreen(
    viewModel: TuningViewModel, 
    preferencesManager: PreferencesManager,
    onNavigateBack: () -> Unit,
    onNavigateToPerAppProfile: () -> Unit = {}
) {
    val cpuClusters by viewModel.cpuClusters.collectAsState()
    val availableGovernors = cpuClusters.firstOrNull()?.availableGovernors ?: emptyList()
    
    // Box container with WavyBlobOrnament background
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Layer
        WavyBlobOrnament(
            modifier = Modifier.fillMaxSize()
        )
        
        // Foreground Layer
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                            )
                        }
                        Text(
                            text = stringResource(R.string.liquid_additional_settings),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor()
                        )
                        // Spacer for balance
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
            ) {
                // Per App Profile Section
                item {
                    LiquidPerAppProfileCard(
                        preferencesManager = preferencesManager,
                        availableGovernors = availableGovernors,
                        onNavigateToFullScreen = onNavigateToPerAppProfile
                    )
                }
                
                // System Settings Group
                item {
                    Text(
                        text = "System Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = id.xms.xtrakernelmanager.ui.screens.home.components.liquid.adaptiveTextColor(),
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                    )
                }
                
                // Network Settings Section
                item {
                    LiquidNetworkSettings(viewModel = viewModel)
                }
                
                // I/O Control Section
                item {
                    LiquidIOControl(viewModel = viewModel)
                }
            }
        }
    }
}
