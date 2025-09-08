import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.hilt.navigation.compose.hiltViewModel
import id.xms.xtrakernelmanager.ui.components.*
import id.xms.xtrakernelmanager.viewmodel.TuningViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


data class FeatureText(
    val titleId: String,
    val titleEn: String,
    val descriptionId: String,
    val descriptionEn: String
)

// Daftar fitur dengan terjemahannya
val tuningFeatures = listOf(
    FeatureText(
        titleId = "Mode Performa",
        titleEn = "Performance Mode",
        descriptionId = "Menyediakan preset konfigurasi untuk mengoptimalkan sistem berdasarkan kebutuhan: Battery Saver (powersave governor), Balanced (schedutil governor), dan Performance (performance governor).",
        descriptionEn = "Provides configuration presets to optimize the system based on needs: Battery Saver (powersave governor), Balanced (schedutil governor), and Performance (performance governor)."
    ),
    FeatureText(
        titleId = "CPU Governor",
        titleEn = "CPU Governor",
        descriptionId = "Mengatur bagaimana frekuensi CPU naik atau turun berdasarkan beban kerja. Pilihan governor yang berbeda dapat mempengaruhi performa dan konsumsi daya.",
        descriptionEn = "Controls how CPU frequency scales up or down based on workload. Different governors can affect performance and power consumption."
    ),
    FeatureText(
        titleId = "Kontrol GPU",
        titleEn = "GPU Control",
        descriptionId = "Menyesuaikan berbagai parameter terkait GPU seperti frekuensi maksimum, governor GPU, dan lainnya untuk mengoptimalkan performa grafis atau efisiensi daya.",
        descriptionEn = "Adjusts various GPU-related parameters like maximum frequency, GPU governor, and others to optimize graphics performance or power efficiency."
    ),
    FeatureText(
        titleId = "Thermal",
        titleEn = "Thermal",
        descriptionId = "Mengelola batas suhu perangkat. Penyesuaian di sini dapat membantu mencegah throttling (penurunan performa akibat panas berlebih) atau sebaliknya, memungkinkan performa lebih tinggi dengan risiko suhu lebih tinggi.",
        descriptionEn = "Manages device temperature limits. Adjustments here can help prevent throttling (performance reduction due to overheating) or, conversely, allow higher performance at the risk of higher temperatures."
    ),
    FeatureText(
        titleId = "Swappiness",
        titleEn = "Swappiness",
        descriptionId = "Mengontrol seberapa agresif kernel memindahkan data dari RAM ke zRAM/swap. Nilai yang lebih tinggi berarti lebih agresif memindahkan ke swap (bisa menghemat RAM aktif tapi lebih lambat), nilai lebih rendah mempertahankan data di RAM lebih lama.",
        descriptionEn = "Controls how aggressively the kernel moves data from RAM to zRAM/swap. A higher value means more aggressive swapping (can save active RAM but is slower), a lower value keeps data in RAM longer."
    )
    // Tambahkan fitur lainnya di sini jika ada
)

enum class Language {
    ID, EN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuningScreen(viewModel: TuningViewModel = hiltViewModel()) {
    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tuning Control",
                    style = MaterialTheme.typography.headlineSmall) },
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Header
            HeroHeader(
                onClick = { showInfoDialog = true }
            )
            
            PerformanceModeCard(viewModel = viewModel)
            CpuGovernorCard(vm = viewModel)
            GpuControlCard(tuningViewModel = viewModel)
            ThermalCard(viewModel = viewModel)
            SwappinessCard(vm = viewModel)
        }
    }

    if (showInfoDialog) {
        FeatureInfoDialog(
            onDismissRequest = { showInfoDialog = false },
            features = tuningFeatures
        )
    }
}

@Composable
fun FeatureInfoDialog(
    onDismissRequest: () -> Unit,
    features: List<FeatureText>
) {
    var selectedLanguage by remember { mutableStateOf(Language.EN) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = {
            Text(
                text = if (selectedLanguage == Language.ID) "Informasi Fitur Tuning" else "Tuning Feature Information",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) { // Baris pertama untuk TabRow
                    TabRow(
                        selectedTabIndex = selectedLanguage.ordinal,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(tabPositions[selectedLanguage.ordinal]))
                        }
                    ) {
                        Tab(
                            selected = selectedLanguage == Language.ID,
                            onClick = { selectedLanguage = Language.ID },
                            text = { Text("ID") },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Tab(
                            selected = selectedLanguage == Language.EN,
                            onClick = { selectedLanguage = Language.EN },
                            text = { Text("EN") },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp)) // Spacer antara TabRow dan konten fitur

                Column(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Biarkan kolom ini mengambil sisa ruang vertikal
                    .verticalScroll(rememberScrollState())
                ) {
                    features.forEachIndexed { index, feature ->
                        FeatureDescription(
                            title = if (selectedLanguage == Language.ID) feature.titleId else feature.titleEn,
                            description = if (selectedLanguage == Language.ID) feature.descriptionId else feature.descriptionEn
                        )
                        if (index < features.lastIndex) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = if (selectedLanguage == Language.ID) "Tutup" else "Close",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = null,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .fillMaxWidth()
    )
}

@Composable
fun FeatureDescription(title: String, description: String) {
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PerformanceModeCard(
    viewModel: TuningViewModel,
    blur: Boolean = true
) {
    var performanceMode by remember { mutableStateOf("Balanced") }

    // Governor mappings as specified
    val governorMappings = mapOf(
        "Battery Saver" to "powersave",
        "Balanced" to "schedutil",
        "Performance" to "performance"
    )

    // Custom color themes for each mode
    val batteryYellow = MaterialTheme.colorScheme.tertiary // Orange-yellow for battery saver
    val balancedGreen = MaterialTheme.colorScheme.primary // Green for balanced
    val performanceRed = MaterialTheme.colorScheme.error // Red for performance

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Performance Mode",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }

            Text(
                text = "Quick presets to optimize system performance and power consumption",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Battery Saver", "Balanced", "Performance").forEach { mode ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                performanceMode = mode
                                // Apply the corresponding governor to all CPU clusters
                                val governor = governorMappings[mode] ?: "schedutil"
                                viewModel.cpuClusters.forEach { cluster ->
                                    viewModel.setCpuGov(cluster, governor)
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = when (mode) {
                                "Battery Saver" -> if (performanceMode == mode) batteryYellow.copy(alpha = 0.15f) else batteryYellow.copy(alpha = 0.05f)
                                "Balanced" -> if (performanceMode == mode) balancedGreen.copy(alpha = 0.15f) else balancedGreen.copy(alpha = 0.05f)
                                "Performance" -> if (performanceMode == mode) performanceRed.copy(alpha = 0.15f) else performanceRed.copy(alpha = 0.05f)
                                else -> MaterialTheme.colorScheme.surface
                            }
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = when (mode) {
                                    "Battery Saver" -> Icons.Default.BatteryStd
                                    "Balanced" -> Icons.Default.Balance
                                    "Performance" -> Icons.Default.FlashOn
                                    else -> Icons.Default.Speed
                                },
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = when (mode) {
                                    "Battery Saver" -> if (performanceMode == mode) batteryYellow else MaterialTheme.colorScheme.onSurfaceVariant
                                    "Balanced" -> if (performanceMode == mode) balancedGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    "Performance" -> if (performanceMode == mode) performanceRed else MaterialTheme.colorScheme.onSurfaceVariant
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = mode,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (performanceMode == mode) FontWeight.Bold else FontWeight.Medium,
                                    color = when (mode) {
                                        "Battery Saver" -> if (performanceMode == mode) batteryYellow else MaterialTheme.colorScheme.onSurface
                                        "Balanced" -> if (performanceMode == mode) balancedGreen else MaterialTheme.colorScheme.onSurface
                                        "Performance" -> if (performanceMode == mode) performanceRed else MaterialTheme.colorScheme.onSurface
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = when (mode) {
                                        "Battery Saver" -> "Powersave governor for maximum battery life"
                                        "Balanced" -> "Schedutil governor for balanced performance"
                                        "Performance" -> "Performance governor for maximum speed"
                                        else -> "Default governor"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Checkmark for selected mode
                            if (performanceMode == mode) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    modifier = Modifier.size(20.dp),
                                    tint = when (mode) {
                                        "Battery Saver" -> batteryYellow
                                        "Balanced" -> balancedGreen
                                        "Performance" -> performanceRed
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = performanceMode != "Balanced",
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (performanceMode) {
                            "Battery Saver" -> batteryYellow.copy(alpha = 0.1f)
                            "Performance" -> performanceRed.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.surfaceContainerLow
                        }
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = when (performanceMode) {
                                "Battery Saver" -> "🔋 Battery Saver Active"
                                "Performance" -> "⚡ Performance Mode Active"
                                else -> "Mode Active"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = when (performanceMode) {
                                "Battery Saver" -> batteryYellow
                                "Performance" -> performanceRed
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                        Text(
                            text = when (performanceMode) {
                                "Battery Saver" -> "Using powersave governor for maximum battery life"
                                "Performance" -> "Using performance governor for maximum speed"
                                else -> "Using default governor"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroHeader(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Title
            Text(
                text = "System Tuning",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Description
            Text(
                text = "Optimize your device performance and power efficiency with advanced kernel controls",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            // Info text
            Text(
                text = "Tap for more information",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}