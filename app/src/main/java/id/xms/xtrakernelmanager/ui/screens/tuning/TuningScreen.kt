package id.xms.xtrakernelmanager.ui.screens.tuning

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.PillCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TuningScreen(
    preferencesManager: PreferencesManager
) {
    val factory = TuningViewModel.Factory(preferencesManager)
    val viewModel: TuningViewModel = viewModel(factory = factory)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isRootAvailable by viewModel.isRootAvailable.collectAsState()
    val cpuClusters by viewModel.cpuClusters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    var resumeKey by remember { mutableStateOf(0) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showSOCWarning by remember { mutableStateOf(false) }
    var socWarningMessage by remember { mutableStateOf("") }
    var pendingImportConfig by remember { mutableStateOf<id.xms.xtrakernelmanager.data.model.TuningConfig?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var detectionTimeoutReached by remember { mutableStateOf(false) }

    // Export launcher - Create file
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/toml")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    // AUTO FILE NAME!
                    val fileName = viewModel.getExportFileName()
                    val success = viewModel.exportConfigToUri(context, it)
                    Toast.makeText(
                        context,
                        if (success) "Configuration exported successfully"
                        else "Failed to export configuration",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Import launcher - Open file
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            isImporting = true
            scope.launch {
                val result = viewModel.importConfigFromUri(context, it)
                isImporting = false
                when (result) {
                    is ImportResult.Success -> {
                        Toast.makeText(
                            context,
                            "Configuration imported and applied successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is ImportResult.Warning -> {
                        pendingImportConfig = result.config
                        socWarningMessage = result.warning
                        showSOCWarning = true
                    }
                    is ImportResult.Error -> {
                        Toast.makeText(
                            context,
                            "Error: ${result.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                resumeKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(resumeKey) {
        viewModel.applyAllConfigurations()
    }
    LaunchedEffect(isLoading) {
        if (isLoading) {
            detectionTimeoutReached = false
            delay(3_000)
            detectionTimeoutReached = true
        } else {
            detectionTimeoutReached = false
        }
    }

    Box {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PillCard(text = stringResource(R.string.tuning_title))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Export Button
                        FilledIconButton(
                            onClick = { showExportDialog = true },
                            enabled = isRootAvailable && !isLoading,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = stringResource(R.string.tuning_export)
                            )
                        }

                        // Import Button
                        FilledIconButton(
                            onClick = { showImportDialog = true },
                            enabled = isRootAvailable && !isLoading,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = stringResource(R.string.tuning_import)
                            )
                        }
                    }
                }
            }

            if (!isRootAvailable) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.Start,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.error,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.padding(8.dp),
                                        tint = MaterialTheme.colorScheme.onError,
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.tuning_requires_root),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                            Text(
                                text = "Please grant root access to use kernel tuning features. Make sure your device is rooted with Magisk.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }
            } else if (isLoading && !detectionTimeoutReached) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = stringResource(R.string.loading),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "Detecting hardware configuration...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            } else {
                if (cpuClusters.isNotEmpty()) {
                    item { CPUControlSection(viewModel = viewModel) }
                }
                item { GPUControlSection(viewModel = viewModel) }
                item { ThermalControlSection(viewModel = viewModel) }
                item { RAMControlSection(viewModel = viewModel) }
                item { AdditionalControlSection(viewModel = viewModel) }
            }
        }

        // Export Confirmation Dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        text = "Export Configuration",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Export current tuning configuration to a file?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "This will save all CPU, GPU, RAM, thermal settings, and device information.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    FilledTonalButton(
                        onClick = {
                            showExportDialog = false
                            scope.launch {
                                val fileName = viewModel.getExportFileName()
                                exportLauncher.launch(fileName)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Export")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Import Confirmation Dialog
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
                title = {
                    Text(
                        text = "Import Configuration",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Import and apply tuning configuration from a file?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "⚠️ This will override all current settings and apply the imported configuration immediately.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                confirmButton = {
                    FilledTonalButton(
                        onClick = {
                            showImportDialog = false
                            importLauncher.launch(arrayOf("application/toml", "text/plain", "*/*"))
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Import")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // LOADING POPUP saat import
        if (isImporting) {
            AlertDialog(
                onDismissRequest = {},
                icon = {
                    CircularProgressIndicator()
                },
                title = { Text("Importing...") },
                text = {
                    Text("Applying configuration. Please wait...")
                },
                confirmButton = {},
                dismissButton = {}
            )
        }

        // SOC Compatibility Warning Dialog
        if (showSOCWarning) {
            AlertDialog(
                onDismissRequest = {
                    showSOCWarning = false
                    pendingImportConfig = null
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = {
                    Text(
                        text = "Device Compatibility Warning",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = socWarningMessage,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Divider()
                        Text(
                            text = "Do you want to proceed anyway?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Applying incompatible configurations may cause system instability, boot loops, or device malfunction.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                pendingImportConfig?.let { config ->
                                    viewModel.applyPreset(config)
                                    Toast.makeText(
                                        context,
                                        "Configuration applied (with warnings)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            showSOCWarning = false
                            pendingImportConfig = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Apply Anyway")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showSOCWarning = false
                        pendingImportConfig = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
