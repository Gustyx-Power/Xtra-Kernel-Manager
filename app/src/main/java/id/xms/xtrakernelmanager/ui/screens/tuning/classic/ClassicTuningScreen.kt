package id.xms.xtrakernelmanager.ui.screens.tuning.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.screens.tuning.classic.components.*
import id.xms.xtrakernelmanager.ui.theme.ClassicColors

@Composable
fun ClassicTuningScreen(
    viewModel: TuningViewModel,
    preferencesManager: PreferencesManager,
    onNavigate: (String) -> Unit,
    onExportConfig: () -> Unit,
    onImportConfig: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClassicColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header (Custom for Tuning)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "System Tuning",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface
                )
                Text(
                    text = "Classic Mode",
                    style = MaterialTheme.typography.labelMedium,
                    color = ClassicColors.Secondary
                )
            }
            // Export/Import Buttons
            Row {
                IconButton(onClick = onImportConfig) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Import",
                        tint = ClassicColors.Primary
                    )
                }
                IconButton(onClick = onExportConfig) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export",
                        tint = ClassicColors.Primary
                    )
                }
            }
        }

        // CPU Tuning
        ClassicCPUTuningCard(viewModel)
        
        // GPU Tuning
        ClassicGPUTuningCard(viewModel)

        // Memory Tuning
        ClassicRAMTuningCard(viewModel)
        
        // Thermal Tuning
        ClassicThermalTuningCard(viewModel)
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
