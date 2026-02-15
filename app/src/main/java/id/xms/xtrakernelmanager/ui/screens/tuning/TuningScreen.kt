package id.xms.xtrakernelmanager.ui.screens.tuning

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.TuningConfig
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.screens.tuning.liquid.LiquidTuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components.LiquidCPUSettingsScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components.LiquidGPUSettingsScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components.LiquidRAMSettingsScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components.LiquidThermalSettingsScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components.ThermalIndexSelectionScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components.ThermalPolicySelectionScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components.LiquidThermalSettingsScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components.LiquidAdditionalSettingsScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.classic.ClassicTuningScreen

// ... (existing imports)

import id.xms.xtrakernelmanager.ui.screens.tuning.classic.ClassicTuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.MaterialTuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.components.MaterialThermalSettingsScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.components.MaterialThermalIndexSelectionScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.components.MaterialThermalPolicySelectionScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.screens.tuning.ImportResult
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
  val layoutStyle by preferencesManager.getLayoutStyle().collectAsState(initial = "liquid")
  var resumeKey by remember { mutableStateOf(0) }
  var showExportDialog by remember { mutableStateOf(false) }
  var showImportDialog by remember { mutableStateOf(false) }
  var showSOCWarning by remember { mutableStateOf(false) }
  var socWarningMessage by remember { mutableStateOf("") }
  var pendingImportConfig by remember { mutableStateOf<TuningConfig?>(null) }
  var isImporting by remember { mutableStateOf(false) }
  var detectionTimeoutReached by remember { mutableStateOf(false) }
  
  // Internal Navigation State
  var currentRoute by remember { mutableStateOf("main") }

  // Handle Back Press
  BackHandler(enabled = currentRoute != "main") {
      currentRoute = "main"
  }

  val exportLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.CreateDocument("application/toml")
      ) { uri ->
        uri?.let {
          scope.launch {
            try {
              viewModel.getExportFileName() 
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
    if (layoutStyle == "classic") {
       ClassicTuningScreen(
           viewModel = viewModel,
           preferencesManager = preferencesManager,
           onNavigate = onNavigate,
           onExportConfig = { showExportDialog = true },
           onImportConfig = { showImportDialog = true }
       )
    } else if (layoutStyle != "liquid") {
      MaterialTuningScreen(
          viewModel = viewModel,
          preferencesManager = preferencesManager,
          onNavigate = onNavigate,
          onExportConfig = { showExportDialog = true },
          onImportConfig = { showImportDialog = true },
      )
    } else {
      // Internal Navigation Handling
      AnimatedContent(
          targetState = currentRoute,
          transitionSpec = {
              if (targetState != "main") {
                  slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
              } else {
                  slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
              }
          }
      ) { route ->
          when (route) {
              "main" -> LiquidTuningScreen(
                  viewModel = viewModel,
                  preferencesManager = preferencesManager,
                  isRootAvailable = isRootAvailable,
                  isLoading = isLoading,
                  detectionTimeoutReached = detectionTimeoutReached,
                  onExportClick = { showExportDialog = true },
                  onImportClick = { showImportDialog = true },
                  onNavigate = { dest ->
                      // Check if it's one of our internal routes
                      if (dest.startsWith("liquid_")) {
                          currentRoute = dest
                      } else {
                          onNavigate(dest) // Pass up if unknown (e.g. legacy)
                      }
                  },
              )
              // Detail Screens
              "liquid_cpu_settings" -> LiquidCPUSettingsScreen(
                  viewModel = viewModel,
                  onNavigateBack = { currentRoute = "main" },
                  onNavigateToSmartLock = { onNavigate("smart_frequency_lock") }
              )
              "liquid_gpu_settings" -> LiquidGPUSettingsScreen(viewModel) { currentRoute = "main" }
              "liquid_ram_settings" -> LiquidRAMSettingsScreen(viewModel) { currentRoute = "main" }
              "liquid_thermal_settings" -> LiquidThermalSettingsScreen(
                  viewModel = viewModel,
                  onNavigateBack = { currentRoute = "main" },
                  onNavigateToIndexSelection = { currentRoute = "thermal_index_selection" },
                  onNavigateToPolicySelection = { currentRoute = "thermal_policy_selection" }
              )
              "thermal_index_selection" -> {
                  val currentIndex by viewModel.preferencesManager.getThermalPreset().collectAsState(initial = "Not Set")
                  val currentOnBoot by viewModel.preferencesManager.getThermalSetOnBoot().collectAsState(initial = false)
                  ThermalIndexSelectionScreen(
                      viewModel = viewModel,
                      currentIndex = currentIndex,
                      onNavigateBack = { currentRoute = "liquid_thermal_settings" },
                      onIndexSelected = { index ->
                          viewModel.setThermalPreset(index, currentOnBoot)
                          currentRoute = "liquid_thermal_settings"
                      }
                  )
              }
              "thermal_policy_selection" -> {
                  val currentPolicy by viewModel.getCpuLockThermalPolicy().collectAsState(initial = "Policy B (Balanced)")
                  ThermalPolicySelectionScreen(
                      viewModel = viewModel,
                      currentPolicy = currentPolicy,
                      onNavigateBack = { currentRoute = "liquid_thermal_settings" },
                      onPolicySelected = { policy ->
                          viewModel.setCpuLockThermalPolicy(policy)
                          currentRoute = "liquid_thermal_settings"
                      }
                  )
              }
              "liquid_additional_settings" -> LiquidAdditionalSettingsScreen(viewModel, preferencesManager) { currentRoute = "main" }
              else -> Text("Unknown route: $route")
          }
      }
    }

    // Export Confirmation Dialog
    if (showExportDialog) {
      id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialog(
          onDismissRequest = { showExportDialog = false },
          title = stringResource(R.string.tuning_export_title),
          content = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                  text = stringResource(R.string.tuning_export_message),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
              )
              Text(
                  text = stringResource(R.string.tuning_export_description),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          },
          confirmButton = {
            id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialogButton(
                text = stringResource(R.string.tuning_export_button),
                onClick = {
                  showExportDialog = false
                  scope.launch {
                    val fileName = viewModel.getExportFileName()
                    exportLauncher.launch(fileName)
                  }
                },
                isPrimary = true
            )
          },
          dismissButton = {
            id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialogButton(
                text = stringResource(R.string.cancel),
                onClick = { showExportDialog = false },
                isPrimary = false
            )
          }
      )
    }

    // Import Confirmation Dialog
    if (showImportDialog) {
      id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialog(
          onDismissRequest = { showImportDialog = false },
          title = stringResource(R.string.tuning_import_title),
          content = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                  text = stringResource(R.string.tuning_import_message),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
              )
              Text(
                  text = stringResource(R.string.tuning_import_warning),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.error,
              )
            }
          },
          confirmButton = {
            id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialogButton(
                text = stringResource(R.string.tuning_import_button),
                onClick = {
                  showImportDialog = false
                  importLauncher.launch(arrayOf("application/toml", "text/plain", "*/*"))
                },
                isPrimary = true
            )
          },
          dismissButton = {
            id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialogButton(
                text = stringResource(R.string.cancel),
                onClick = { showImportDialog = false },
                isPrimary = false
            )
          }
      )
    }

    // LOADING POPUP saat import
    if (isImporting) {
      id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialog(
          onDismissRequest = {},
          title = stringResource(R.string.tuning_importing),
          content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              CircularProgressIndicator()
              Text(
                  text = stringResource(R.string.tuning_applying_config),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
              )
            }
          },
          confirmButton = {},
          dismissButton = null,
          properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
      )
    }

    // SOC Compatibility Warning Dialog
    if (showSOCWarning) {
      id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialog(
          onDismissRequest = {
            showSOCWarning = false
            pendingImportConfig = null
          },
          title = stringResource(R.string.tuning_soc_warning_title),
          content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Text(
                  text = socWarningMessage, 
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
              )
              HorizontalDivider()
              Text(
                  text = stringResource(R.string.tuning_soc_warning_question),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                  text = stringResource(R.string.tuning_soc_warning_desc),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.error,
              )
            }
          },
          confirmButton = {
            id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialogButton(
                text = stringResource(R.string.tuning_apply_anyway),
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
                isPrimary = true
            )
          },
          dismissButton = {
            id.xms.xtrakernelmanager.ui.components.liquid.LiquidDialogButton(
                text = stringResource(R.string.cancel),
                onClick = {
                  showSOCWarning = false
                  pendingImportConfig = null
                },
                isPrimary = false
            )
          }
      )
    }
  }
}
