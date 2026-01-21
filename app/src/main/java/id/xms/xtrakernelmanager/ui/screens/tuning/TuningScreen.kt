package id.xms.xtrakernelmanager.ui.screens.tuning

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import id.xms.xtrakernelmanager.data.model.TuningConfig
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.screens.tuning.legacy.LegacyTuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.MaterialTuningScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TuningScreen(preferencesManager: PreferencesManager, onNavigate: (String) -> Unit = {}) {
  val factory = TuningViewModel.Factory(preferencesManager)
  val viewModel: TuningViewModel = viewModel(factory = factory)

  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val isRootAvailable by viewModel.isRootAvailable.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  val lifecycleOwner = LocalLifecycleOwner.current
  val layoutStyle by preferencesManager.getLayoutStyle().collectAsState(initial = "legacy")
  var resumeKey by remember { mutableStateOf(0) }
  var showExportDialog by remember { mutableStateOf(false) }
  var showImportDialog by remember { mutableStateOf(false) }
  var showSOCWarning by remember { mutableStateOf(false) }
  var socWarningMessage by remember { mutableStateOf("") }
  var pendingImportConfig by remember {
    mutableStateOf<TuningConfig?>(null)
  }
  var isImporting by remember { mutableStateOf(false) }
  var detectionTimeoutReached by remember { mutableStateOf(false) }

  val exportLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.CreateDocument("application/toml")
      ) { uri ->
        uri?.let {
          scope.launch {
            try {
              viewModel.getExportFileName() // Assuming usage for file name generaton logic inside VM
              val success = viewModel.exportConfigToUri(context, it)
              Toast.makeText(
                      context,
                      if (success) context.getString(R.string.tuning_export_success)
                      else context.getString(R.string.tuning_export_failed),
                      Toast.LENGTH_SHORT,
                  )
                  .show()
            } catch (e: Exception) {
              Toast.makeText(
                      context,
                      context.getString(R.string.tuning_error_format, e.message),
                      Toast.LENGTH_SHORT,
                  )
                  .show()
            }
          }
        }
      }

  val importLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
          isImporting = true
          scope.launch {
            val result = viewModel.importConfigFromUri(context, it)
            isImporting = false
            when (result) {
              is ImportResult.Success -> {
                Toast.makeText(
                        context,
                        context.getString(R.string.tuning_import_success),
                        Toast.LENGTH_SHORT,
                    )
                    .show()
              }
              is ImportResult.Warning -> {
                pendingImportConfig = result.config
                socWarningMessage = result.warning
                showSOCWarning = true
              }
              is ImportResult.Error -> {
                Toast.makeText(
                        context,
                        context.getString(R.string.tuning_error_format, result.message),
                        Toast.LENGTH_LONG,
                    )
                    .show()
              }
            }
          }
        }
      }

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        resumeKey++
        viewModel.startRealTimeMonitoring()
      } else if (event == Lifecycle.Event.ON_PAUSE) {
        viewModel.stopRealTimeMonitoring()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  LaunchedEffect(resumeKey) { viewModel.applyAllConfigurations() }
  LaunchedEffect(isLoading) {
    if (isLoading) {
      detectionTimeoutReached = false
      delay(3_000)
      detectionTimeoutReached = true
    } else {
      detectionTimeoutReached = false
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    if (layoutStyle != "legacy") {
      MaterialTuningScreen(
          viewModel = viewModel,
          preferencesManager = preferencesManager,
          onNavigate = onNavigate,
          onExportConfig = { showExportDialog = true },
          onImportConfig = { showImportDialog = true },
      )
    } else {
      LegacyTuningScreen(
          viewModel = viewModel,
          preferencesManager = preferencesManager,
          isRootAvailable = isRootAvailable,
          isLoading = isLoading,
          detectionTimeoutReached = detectionTimeoutReached,
          onExportClick = { showExportDialog = true },
          onImportClick = { showImportDialog = true }
      )
    }

    // Export Confirmation Dialog
    if (showExportDialog) {
      AlertDialog(
          onDismissRequest = { showExportDialog = false },
          icon = {
            Icon(
                imageVector = Icons.Default.Upload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
          },
          title = {
            Text(
                text = stringResource(R.string.tuning_export_title),
                style = MaterialTheme.typography.headlineSmall,
            )
          },
          text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                  text = stringResource(R.string.tuning_export_message),
                  style = MaterialTheme.typography.bodyMedium,
              )
              Text(
                  text = stringResource(R.string.tuning_export_description),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                  modifier = Modifier.size(18.dp),
              )
              Spacer(Modifier.width(8.dp))
              Text(stringResource(R.string.tuning_export_button))
            }
          },
          dismissButton = {
            TextButton(onClick = { showExportDialog = false }) {
              Text(stringResource(R.string.cancel))
            }
          },
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
                tint = MaterialTheme.colorScheme.secondary,
            )
          },
          title = {
            Text(
                text = stringResource(R.string.tuning_import_title),
                style = MaterialTheme.typography.headlineSmall,
            )
          },
          text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                  text = stringResource(R.string.tuning_import_message),
                  style = MaterialTheme.typography.bodyMedium,
              )
              Text(
                  text = stringResource(R.string.tuning_import_warning),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.error,
              )
            }
          },
          confirmButton = {
            FilledTonalButton(
                onClick = {
                  showImportDialog = false
                  importLauncher.launch(arrayOf("application/toml", "text/plain", "*/*"))
                },
                colors =
                    ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
            ) {
              Icon(
                  imageVector = Icons.Default.Download,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
              )
              Spacer(Modifier.width(8.dp))
              Text(stringResource(R.string.tuning_import_button))
            }
          },
          dismissButton = {
            TextButton(onClick = { showImportDialog = false }) {
              Text(stringResource(R.string.cancel))
            }
          },
      )
    }

    // LOADING POPUP saat import
    if (isImporting) {
      AlertDialog(
          onDismissRequest = {},
          icon = { CircularProgressIndicator() },
          title = { Text(stringResource(R.string.tuning_importing)) },
          text = { Text(stringResource(R.string.tuning_applying_config)) },
          confirmButton = {},
          dismissButton = {},
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
                tint = MaterialTheme.colorScheme.error,
            )
          },
          title = {
            Text(
                text = stringResource(R.string.tuning_soc_warning_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error,
            )
          },
          text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Text(text = socWarningMessage, style = MaterialTheme.typography.bodyMedium)
              Divider()
              Text(
                  text = stringResource(R.string.tuning_soc_warning_question),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
              )
              Text(
                  text = stringResource(R.string.tuning_soc_warning_desc),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.error,
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
                              context.getString(R.string.tuning_apply_with_warning),
                              Toast.LENGTH_SHORT,
                          )
                          .show()
                    }
                  }
                  showSOCWarning = false
                  pendingImportConfig = null
                },
                colors =
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) {
              Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
              )
              Spacer(Modifier.width(8.dp))
              Text(stringResource(R.string.tuning_apply_anyway))
            }
          },
          dismissButton = {
            TextButton(
                onClick = {
                  showSOCWarning = false
                  pendingImportConfig = null
                }
            ) {
              Text(stringResource(R.string.cancel))
            }
          },
      )
    }
  }
}
