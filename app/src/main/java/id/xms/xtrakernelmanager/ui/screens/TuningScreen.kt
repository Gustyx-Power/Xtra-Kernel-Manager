@file:Suppress("UNUSED_PARAMETER", "DEPRECATION")

package id.xms.xtrakernelmanager.ui.screens

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import id.xms.xtrakernelmanager.ui.components.*
import id.xms.xtrakernelmanager.viewmodel.TuningViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


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
                title = { Text("Tuning Control", style = TextStyle(fontSize = 27.sp)) },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Filled.Info, contentDescription = "Informasi Fitur")
                    }
                }
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
            PerformanceModeCard(viewModel = viewModel, blur = true)
            CpuGovernorCard(vm = viewModel, blur = true)
            GpuControlCard(tuningViewModel = viewModel, blur = true)
            ThermalCard(viewModel = viewModel, blur = true)
            SwappinessCard(vm = viewModel, blur = true)
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun generateFileContent(): String {
        val dialogTitleText = if (selectedLanguage == Language.ID) "Informasi Fitur Tuning" else "Tuning Feature Information"
        val contentBuilder = StringBuilder()
        contentBuilder.appendLine("== $dialogTitleText ==")
        contentBuilder.appendLine()

        features.forEach { feature ->
            val title = if (selectedLanguage == Language.ID) feature.titleId else feature.titleEn
            val description = if (selectedLanguage == Language.ID) feature.descriptionId else feature.descriptionEn
            contentBuilder.appendLine("Fitur: $title")
            contentBuilder.appendLine("Deskripsi: $description")
            contentBuilder.appendLine("------------------------------")
            contentBuilder.appendLine()
        }
        return contentBuilder.toString()
    }

    fun saveContentToFile(content: String) {
        coroutineScope.launch {
            val success = saveTextToFileHelper(context, content, selectedLanguage)
            withContext(Dispatchers.Main) {
                val message = if (success) {
                    if (selectedLanguage == Language.ID) "Informasi disimpan ke direktori Documents" else "Information saved to Documents directory"
                } else {
                    if (selectedLanguage == Language.ID) "Gagal menyimpan informasi" else "Failed to save information"
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

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
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    val fileContent = generateFileContent()
                    saveContentToFile(fileContent)
                }) {
                    Icon(Icons.Filled.Save, contentDescription = if (selectedLanguage == Language.ID) "Simpan" else "Save", modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(if (selectedLanguage == Language.ID) "SIMPAN" else "SAVE")
                }
                TextButton(onClick = onDismissRequest) {
                    Text(if (selectedLanguage == Language.ID) "TUTUP" else "CLOSE")
                }
            }
        },
        dismissButton = null,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .fillMaxWidth()
    )
}

// Fungsi helper untuk menyimpan teks ke file, dipisahkan agar lebih rapi
@SuppressLint("ObsoleteSdkInt")
suspend fun saveTextToFileHelper(context: Context, textContent: String, language: Language): Boolean {
    return withContext(Dispatchers.IO) {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = sdf.format(Date())
        val fileName = "TuningInfo_${timestamp}.txt"
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                }
                val uri = resolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues)
                if (uri != null) {
                    outputStream = resolver.openOutputStream(uri)
                } else {
                    android.util.Log.e("SaveToFile", "MediaStore URI was null.")
                    return@withContext false
                }
            } else {
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                if (!documentsDir.exists() && !documentsDir.mkdirs()) {
                    android.util.Log.e("SaveToFile", "Failed to create Documents directory.")
                    return@withContext false
                }
                val file = File(documentsDir, fileName)
                outputStream = FileOutputStream(file)
            }

            outputStream?.use { stream ->
                stream.write(textContent.toByteArray(Charsets.UTF_8))
                stream.flush()
            }
            outputStream != null
        } catch (e: Exception) {
            android.util.Log.e("SaveToFile", "Error saving file", e)
            e.printStackTrace()
            false
        } finally {
            try {
                outputStream?.close()
            } catch (e: java.io.IOException) {
                e.printStackTrace()
            }
        }
    }
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
    val batteryYellow = Color(0xFFFFA726) // Orange-yellow for battery saver
    val balancedGreen = Color(0xFF66BB6A) // Green for balanced
    val performanceRed = Color(0xFFEF5350) // Red for performance

    SuperGlassCard(
        modifier = Modifier.fillMaxWidth(),
        glassIntensity = if (blur) GlassIntensity.Light else GlassIntensity.Light
    ) {
        Column(
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
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
                    SuperGlassCard(
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
                        glassIntensity = GlassIntensity.Light // Keep consistent light intensity to avoid blur obstruction
                    ) {
                        // Add colored background for theming
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = when (mode) {
                                        "Battery Saver" -> if (performanceMode == mode) batteryYellow.copy(alpha = 0.15f) else batteryYellow.copy(alpha = 0.05f)
                                        "Balanced" -> if (performanceMode == mode) balancedGreen.copy(alpha = 0.15f) else balancedGreen.copy(alpha = 0.05f)
                                        "Performance" -> if (performanceMode == mode) performanceRed.copy(alpha = 0.15f) else performanceRed.copy(alpha = 0.05f)
                                        else -> Color.Transparent
                                    },
                                    shape = RoundedCornerShape(28.dp)
                                )
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
            }

            if (performanceMode != "Balanced") {
                SuperGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glassIntensity = GlassIntensity.Light
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = when (performanceMode) {
                                    "Battery Saver" -> batteryYellow.copy(alpha = 0.1f)
                                    "Performance" -> performanceRed.copy(alpha = 0.1f)
                                    else -> Color.Transparent
                                },
                                shape = RoundedCornerShape(28.dp)
                            )
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
}