package id.xms.xtrakernelmanager.ui.screens.tuning.frosted.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
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
import id.xms.xtrakernelmanager.ui.screens.home.components.frosted.adaptiveSurfaceColor
import id.xms.xtrakernelmanager.ui.screens.home.components.frosted.adaptiveTextColor
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.screens.tuning.frosted.FrostedIOControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrostedAdditionalSettingsScreen(
    viewModel: TuningViewModel, 
    preferencesManager: PreferencesManager,
    onNavigateBack: () -> Unit,
    onNavigateToPerAppProfile: () -> Unit = {}
) {
    val cpuClusters by viewModel.cpuClusters.collectAsState()
    val availableGovernors = cpuClusters.firstOrNull()?.availableGovernors ?: emptyList()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        WavyBlobOrnament(modifier = Modifier.fillMaxSize())
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = adaptiveTextColor()
                            )
                        }
                        Text(
                            text = stringResource(R.string.frosted_additional_settings),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = adaptiveTextColor()
                        )
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Card (without icon)
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Additional Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = adaptiveTextColor()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Configure advanced system settings, network, and I/O optimizations",
                            style = MaterialTheme.typography.bodySmall,
                            color = adaptiveTextColor(0.8f)
                        )
                    }
                }

                // Per App Profile Section
                Text(
                    text = "App Profiles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )

                FrostedPerAppProfileCard(
                    preferencesManager = preferencesManager,
                    availableGovernors = availableGovernors,
                    onNavigateToFullScreen = onNavigateToPerAppProfile
                )

                // System Settings Section
                Text(
                    text = "System Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Network Settings
                FrostedNetworkSettings(viewModel = viewModel)

                // I/O Control
                FrostedIOControl(viewModel = viewModel)

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
