package id.xms.xtrakernelmanager.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.BottomNavItem
import id.xms.xtrakernelmanager.ui.components.HolidayCelebrationDialog
import id.xms.xtrakernelmanager.ui.components.material.MaterialFloatingBottomBar
import id.xms.xtrakernelmanager.ui.components.frosted.FrostedBottomTabs
import id.xms.xtrakernelmanager.ui.components.frosted.FrostedBottomTab
import id.xms.xtrakernelmanager.ui.components.classic.ClassicBottomBar
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import id.xms.xtrakernelmanager.ui.screens.functionalrom.FunctionalRomScreen
import id.xms.xtrakernelmanager.ui.screens.functionalrom.FunctionalRomViewModel
import id.xms.xtrakernelmanager.ui.screens.functionalrom.ShimokuRomScreen
import id.xms.xtrakernelmanager.ui.screens.functionalrom.PlayIntegritySettingsScreen
import id.xms.xtrakernelmanager.ui.screens.functionalrom.XiaomiTouchSettingsScreen
import id.xms.xtrakernelmanager.ui.screens.functionalrom.HideAccessibilitySettingsScreen
import id.xms.xtrakernelmanager.ui.screens.functionalrom.DisplaySizeScreen
import id.xms.xtrakernelmanager.ui.screens.functionalrom.MaterialDisplaySizeScreen
import id.xms.xtrakernelmanager.ui.screens.functionalrom.ClassicDisplaySizeScreen
import id.xms.xtrakernelmanager.ui.screens.functionalrom.MaterialGlobalRefreshRateScreen
import id.xms.xtrakernelmanager.ui.screens.functionalrom.ClassicGlobalRefreshRateScreen
import id.xms.xtrakernelmanager.ui.screens.functionalrom.FrostedGlobalRefreshRateScreen
import id.xms.xtrakernelmanager.data.model.HideAccessibilityConfig
import id.xms.xtrakernelmanager.ui.screens.home.HomeScreen
import id.xms.xtrakernelmanager.ui.screens.donation.DonationScreen
import id.xms.xtrakernelmanager.ui.screens.home.components.material.PowerMenuContent
import id.xms.xtrakernelmanager.ui.screens.info.InfoScreen
import id.xms.xtrakernelmanager.ui.screens.info.SystemInfoScreen
import id.xms.xtrakernelmanager.ui.screens.info.UpdateViewModel
import id.xms.xtrakernelmanager.ui.screens.webview.MaterialWebViewScreen
import id.xms.xtrakernelmanager.ui.screens.webview.FrostedWebViewScreen
import id.xms.xtrakernelmanager.ui.screens.settings.SettingsScreen
import id.xms.xtrakernelmanager.utils.RootShell
import id.xms.xtrakernelmanager.ui.screens.misc.material.MaterialGameAppSelectorScreen
import id.xms.xtrakernelmanager.ui.screens.misc.classic.ClassicGameAppSelectorScreen
import id.xms.xtrakernelmanager.ui.screens.misc.MiscScreen
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import id.xms.xtrakernelmanager.ui.screens.setup.SetupScreen
import id.xms.xtrakernelmanager.ui.screens.root.RootAccessScreen
import id.xms.xtrakernelmanager.ui.splash.checkRootAccess
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.screens.tuning.classic.ClassicCPUTuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.classic.ClassicGPUTuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.classic.ClassicMemoryTuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.classic.ClassicSmartFrequencyLockScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.classic.ClassicThermalSettingsScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.classic.components.ClassicThermalIndexSelectionScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.frosted.components.FrostedCPUSettingsScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.frosted.components.SmartFrequencyLockScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.CPUTuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.MemoryTuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.components.MaterialSmartFrequencyLockScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.components.MaterialThermalSettingsScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.components.MaterialThermalIndexSelectionScreen
import id.xms.xtrakernelmanager.utils.Holiday
import id.xms.xtrakernelmanager.utils.HolidayChecker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.CompositionLocalProvider
import id.xms.xtrakernelmanager.ui.components.LocalBackdrop
import id.xms.xtrakernelmanager.ui.components.utils.layerBackdrop
import id.xms.xtrakernelmanager.ui.components.utils.rememberLayerBackdrop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(
    preferencesManager: PreferencesManager,
    shouldShowDonationDialog: Boolean = false,
    updateViewModel: UpdateViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  
  // Collect update state
  val updateState by updateViewModel.updateState.collectAsState()
  
  // Check for updates when app starts
  LaunchedEffect(Unit) {
    updateViewModel.checkForUpdates(context)
  }

  // Power menu state for Material theme
  var showPowerBottomSheet by remember { mutableStateOf(false) }

  // Holiday celebration state
  var showHolidayDialog by remember { mutableStateOf(false) }
  var currentHoliday by remember { mutableStateOf<Holiday?>(null) }
  var hasCheckedHoliday by remember { mutableStateOf(false) }
  val currentYear = HolidayChecker.getCurrentYear()
  val currentHijriYear = HolidayChecker.getCurrentHijriYear()

  // Collect holiday shown years from preferences
  val christmasShownYear by preferencesManager.getChristmasShownYear().collectAsState(initial = 0)
  val newYearShownYear by preferencesManager.getNewYearShownYear().collectAsState(initial = 0)
  val ramadanShownYear by preferencesManager.getRamadanShownYear().collectAsState(initial = 0)
  val eidFitrShownYear by preferencesManager.getEidFitrShownYear().collectAsState(initial = 0)
  
  // Handle donation dialog from notification
  LaunchedEffect(shouldShowDonationDialog) {
    if (shouldShowDonationDialog) {
      // Wait for NavHost to be composed and ready
      delay(200)
      try {
        // Navigate to home if not already there
        if (navController.currentDestination?.route != "home") {
          navController.navigate("home") {
            popUpTo(navController.graph.startDestinationId) { inclusive = false }
          }
        }
      } catch (e: Exception) {
        android.util.Log.e("Navigation", "Error navigating to home from notification", e)
      }
    }
  }

  // Check for holidays on launch - only once
  LaunchedEffect(Unit) {
    if (!hasCheckedHoliday) {
      hasCheckedHoliday = true
      val holiday = HolidayChecker.getCurrentHoliday()
      if (holiday != null) {
        val lastShownYear =
            when (holiday) {
              Holiday.CHRISTMAS -> christmasShownYear
              Holiday.NEW_YEAR -> newYearShownYear
              Holiday.RAMADAN -> ramadanShownYear
              Holiday.EID_FITR -> eidFitrShownYear
            }
        if (HolidayChecker.shouldShowHolidayDialog(holiday, lastShownYear)) {
          currentHoliday = holiday
          showHolidayDialog = true
        }
      }
    }
  }

  // Show holiday celebration dialog
  if (showHolidayDialog && currentHoliday != null) {
    HolidayCelebrationDialog(
        holiday = currentHoliday!!,
        year =
            if (currentHoliday == Holiday.RAMADAN || currentHoliday == Holiday.EID_FITR)
                currentHijriYear
            else currentYear,
        onDismiss = {
          showHolidayDialog = false
          // Mark this holiday as shown for current year (after dialog closed)
          scope.launch {
            val yearToSave =
                if (currentHoliday == Holiday.RAMADAN || currentHoliday == Holiday.EID_FITR)
                    currentHijriYear
                else currentYear
            when (currentHoliday) {
              Holiday.CHRISTMAS -> preferencesManager.setChristmasShownYear(yearToSave)
              Holiday.NEW_YEAR -> preferencesManager.setNewYearShownYear(yearToSave)
              Holiday.RAMADAN -> preferencesManager.setRamadanShownYear(yearToSave)
              Holiday.EID_FITR -> preferencesManager.setEidFitrShownYear(yearToSave)
              null -> {}
            }
            currentHoliday = null
          }
        },
    )
  }

  val bottomNavItems =
      listOf(
          BottomNavItem(route = "home", icon = Icons.Default.Home, label = R.string.nav_home),
          BottomNavItem(
              route = "tuning",
              icon = Icons.Default.Settings,
              label = R.string.nav_tuning,
          ),
          BottomNavItem(route = "profiles", icon = Icons.Default.Speed, label = R.string.nav_misc),
          BottomNavItem(route = "info", icon = Icons.Default.Info, label = R.string.nav_info),
      )

  // Collect Setup State
  val isSetupCompleteState by preferencesManager.isSetupComplete.collectAsState(initial = null)
  
  // Check root access
  var hasRootAccess by remember { mutableStateOf<Boolean?>(null) }
  
  LaunchedEffect(isSetupCompleteState) {
    if (isSetupCompleteState == true) {
      // Check root access after setup is complete
      hasRootAccess = checkRootAccess()
    }
  }

  // Show nothing while checking setup state to confirm start destination
  if (isSetupCompleteState == null) return
  
  // Determine start destination based on setup and root status
  val startDest = when {
    isSetupCompleteState == false -> "setup"
    hasRootAccess == null -> "home" // Still checking root, show home temporarily
    hasRootAccess == false -> "root_access"
    else -> "home"
  }

  // Redirect to Home if setup is completed while on setup screen
  LaunchedEffect(isSetupCompleteState, currentRoute) {
    if (isSetupCompleteState == true && currentRoute == "setup") {
      // Check root first before going to home
      val rootCheck = checkRootAccess()
      
      if (rootCheck) {
        navController.navigate("home") { popUpTo("setup") { inclusive = true } }
      } else {
        navController.navigate("root_access") { popUpTo("setup") { inclusive = true } }
      }
    }
  }
  val layoutStyle by preferencesManager.getLayoutStyle().collectAsState(initial = "frosted")

  // Shared FunctionalRomViewModel - created once and reused across all related screens
  // to prevent state flickering when navigating between functionalrom, shimokurom, and hideaccessibilitysettings
  val functionalRomFactory = remember { FunctionalRomViewModel.Companion.Factory(preferencesManager, context.applicationContext) }
  val sharedFunctionalRomViewModel: FunctionalRomViewModel = viewModel(factory = functionalRomFactory)

  // Setup backdrop for frosted glass effect (like temanmu's code)
  val backgroundColor = MaterialTheme.colorScheme.background
  val backdrop = rememberLayerBackdrop {
    drawRect(backgroundColor)
    drawContent()
  }

  Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
    CompositionLocalProvider(LocalBackdrop provides backdrop) {
      Scaffold(
          containerColor = Color.Transparent
      ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier
                .layerBackdrop(backdrop)
                .padding(paddingValues)
                .padding(bottom = if (layoutStyle == "frosted") 0.dp else 0.dp),
        ) {
        composable("setup") {
          SetupScreen(
              onSetupComplete = { layoutStyle ->
                scope.launch {
                  // Save layout style and setup complete flag
                  preferencesManager.setLayoutStyle(layoutStyle)
                  preferencesManager.setSetupComplete(true)
                  
                  // Wait a bit to ensure preferences are saved
                  kotlinx.coroutines.delay(100)
                  
                  // Check root access before navigating
                  val rootCheck = checkRootAccess()
                  
                  // Navigate based on root status
                  if (rootCheck) {
                    navController.navigate("home") { popUpTo("setup") { inclusive = true } }
                  } else {
                    navController.navigate("root_access") { popUpTo("setup") { inclusive = true } }
                  }
                }
              }
          )
        }
        
        composable("root_access") {
          RootAccessScreen(
              onGrantPermissions = {
                scope.launch {
                  // Try to request root access
                  val rootGranted = checkRootAccess()
                  if (rootGranted) {
                    // Root granted, navigate to home
                    navController.navigate("home") { 
                      popUpTo("root_access") { inclusive = true } 
                    }
                  }
                }
              },
              onTryAgain = {
                scope.launch {
                  // Check root again
                  val rootGranted = checkRootAccess()
                  if (rootGranted) {
                    navController.navigate("home") { 
                      popUpTo("root_access") { inclusive = true } 
                    }
                  }
                }
              }
          )
        }
        
        composable("home") { 
          HomeScreen(
              preferencesManager = preferencesManager,
              onNavigateToSettings = { navController.navigate("settings") },
              onNavigateToDonation = { navController.navigate("donation") },
              forceShowDonationDialog = shouldShowDonationDialog
          ) 
        }
        composable("tuning") {
          TuningScreen(
              preferencesManager = preferencesManager,
              onNavigate = { route -> navController.navigate(route) },
          )
        }
        composable("legacy_cpu_settings") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          FrostedCPUSettingsScreen(
              viewModel = tuningViewModel,
              onNavigateBack = { navController.popBackStack() },
              onNavigateToSmartLock = { navController.navigate("smart_frequency_lock") }
          )
        }
        composable("smart_frequency_lock") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          SmartFrequencyLockScreen(
              viewModel = tuningViewModel,
              onNavigateBack = { navController.popBackStack() }
          )
        }
        composable("cpu_tuning") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          
          // Use Classic CPU Tuning Screen for classic layout
          if (layoutStyle == "classic") {
            ClassicCPUTuningScreen(
                viewModel = tuningViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSmartLock = { navController.navigate("material_smart_frequency_lock") }
            )
          } else {
            CPUTuningScreen(
                viewModel = tuningViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSmartLock = { navController.navigate("material_smart_frequency_lock") }
            )
          }
        }
        composable("material_smart_frequency_lock") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          
          // Use Classic Smart Frequency Lock for classic layout
          if (layoutStyle == "classic") {
            ClassicSmartFrequencyLockScreen(
                viewModel = tuningViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
          } else {
            MaterialSmartFrequencyLockScreen(
                viewModel = tuningViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
          }
        }
        composable("memory_tuning") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          
          // Use Classic Memory Tuning Screen for classic layout
          if (layoutStyle == "classic") {
            ClassicMemoryTuningScreen(
                viewModel = tuningViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
          } else {
            // Use Material Memory Tuning Screen for other layouts
            MemoryTuningScreen(viewModel = tuningViewModel, navController = navController)
          }
        }
        composable("gpu_tuning") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          
          // Use Classic GPU Tuning Screen for classic layout
          if (layoutStyle == "classic") {
            ClassicGPUTuningScreen(
                viewModel = tuningViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
          } else {
            // For other layouts, navigate back for now (can add material/frosted GPU screens later)
            navController.popBackStack()
          }
        }
        composable("classic_thermal_settings") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          ClassicThermalSettingsScreen(
              viewModel = tuningViewModel,
              onNavigateBack = { navController.popBackStack() },
              onNavigateToIndexSelection = { navController.navigate("classic_thermal_index_selection") }
          )
        }
        composable("classic_thermal_index_selection") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          val currentIndex by tuningViewModel.preferencesManager.getThermalPreset().collectAsState(initial = "Not Set")
          val currentOnBoot by tuningViewModel.preferencesManager.getThermalSetOnBoot().collectAsState(initial = false)
          val scope = rememberCoroutineScope()
          ClassicThermalIndexSelectionScreen(
              viewModel = tuningViewModel,
              currentIndex = currentIndex,
              onNavigateBack = { navController.popBackStack() },
              onIndexSelected = { index ->
                  tuningViewModel.setThermalPreset(index, currentOnBoot)
                  scope.launch {
                      delay(100)
                      navController.popBackStack()
                  }
              }
          )
        }
        composable("material_thermal_settings") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          MaterialThermalSettingsScreen(
              viewModel = tuningViewModel,
              onNavigateBack = { navController.popBackStack() },
              onNavigateToIndexSelection = { navController.navigate("material_thermal_index_selection") }
          )
        }
        composable("material_thermal_index_selection") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          val currentIndex by tuningViewModel.preferencesManager.getThermalPreset().collectAsState(initial = "Not Set")
          val currentOnBoot by tuningViewModel.preferencesManager.getThermalSetOnBoot().collectAsState(initial = false)
          val scope = rememberCoroutineScope()
          MaterialThermalIndexSelectionScreen(
              viewModel = tuningViewModel,
              currentIndex = currentIndex,
              onNavigateBack = { navController.popBackStack() },
              onIndexSelected = { index ->
                  tuningViewModel.setThermalPreset(index, currentOnBoot)
                  scope.launch {
                      delay(100)
                      navController.popBackStack()
                  }
              }
          )
        }
        composable("app_picker") {
          val context = LocalContext.current
          val miscViewModel = remember {
            MiscViewModel(
                preferencesManager = preferencesManager,
                context = context.applicationContext,
            )
          }
          when (layoutStyle) {
            "classic" -> {
              ClassicGameAppSelectorScreen(
                  viewModel = miscViewModel,
                  onBack = { navController.popBackStack() },
              )
            }
            else -> {
              // Material and Frosted use the same material design
              MaterialGameAppSelectorScreen(
                  viewModel = miscViewModel,
                  onBack = { navController.popBackStack() },
              )
            }
          }
        }
        composable("profiles") {
          val context = LocalContext.current
          val miscViewModel = remember {
            MiscViewModel(
                preferencesManager = preferencesManager,
                context = context.applicationContext,
            )
          }
          MiscScreen(
              viewModel = miscViewModel,
              onNavigateToFunctionalRom = { navController.navigate("functionalrom") },
              onNavigateToAppPicker = { navController.navigate("app_picker") },
          )
        }

        composable("functionalrom") {
          FunctionalRomScreen(
              onNavigateBack = { navController.popBackStack() },
              onNavigateToShimokuRom = { navController.navigate("shimokurom") },
              onNavigateToHideAccessibility = { navController.navigate("hideaccessibilitysettings") },
              onNavigateToDisplaySize = { navController.navigate("display_size") },
              onNavigateToGlobalRefreshRate = { navController.navigate("global_refresh_rate") },
              viewModel = sharedFunctionalRomViewModel,
          )
        }

        composable("global_refresh_rate") {
          val layoutStyle by sharedFunctionalRomViewModel.layoutStyle.collectAsState()
          when (layoutStyle) {
            "material" -> MaterialGlobalRefreshRateScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = sharedFunctionalRomViewModel
            )
            "classic" -> ClassicGlobalRefreshRateScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = sharedFunctionalRomViewModel
            )
            else -> FrostedGlobalRefreshRateScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = sharedFunctionalRomViewModel
            )
          }
        }

        composable("shimokurom") {
          ShimokuRomScreen(
              onNavigateBack = { navController.popBackStack() },
              onNavigateToPlayIntegrity = { navController.navigate("playintegritysettings") },
              onNavigateToXiaomiTouch = { navController.navigate("xiaomitouchsettings") },
              viewModel = sharedFunctionalRomViewModel,
          )
        }

        composable("playintegritysettings") {
          PlayIntegritySettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("xiaomitouchsettings") {
          XiaomiTouchSettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("display_size") {
          when (layoutStyle) {
            "material" -> {
              MaterialDisplaySizeScreen(onNavigateBack = { navController.popBackStack() })
            }
            "classic" -> {
              ClassicDisplaySizeScreen(onNavigateBack = { navController.popBackStack() })
            }
            else -> {
              // Frosted uses frosted design
              DisplaySizeScreen(onNavigateBack = { navController.popBackStack() })
            }
          }
        }

        composable("hideaccessibilitysettings") {
          val uiState by sharedFunctionalRomViewModel.uiState.collectAsState()
          
          HideAccessibilitySettingsScreen(
              config = uiState.hideAccessibilityConfig,
              onNavigateBack = { navController.popBackStack() },
              onConfigChange = { newConfig: HideAccessibilityConfig ->
                // Update individual config properties
                if (newConfig.isEnabled != uiState.hideAccessibilityConfig.isEnabled) {
                  sharedFunctionalRomViewModel.setHideAccessibilityEnabled(newConfig.isEnabled)
                }
                if (newConfig.currentTab != uiState.hideAccessibilityConfig.currentTab) {
                  sharedFunctionalRomViewModel.setHideAccessibilityTab(newConfig.currentTab)
                }
                if (newConfig.appsToHide != uiState.hideAccessibilityConfig.appsToHide) {
                  sharedFunctionalRomViewModel.setHideAccessibilityAppsToHide(newConfig.appsToHide)
                }
                if (newConfig.detectorApps != uiState.hideAccessibilityConfig.detectorApps) {
                  sharedFunctionalRomViewModel.setHideAccessibilityDetectorApps(newConfig.detectorApps)
                }
              },
              onRefreshLSPosedStatus = {
                sharedFunctionalRomViewModel.refreshLSPosedStatus()
              }
          )
        }

        composable("info") { 
          InfoScreen(
              preferencesManager = preferencesManager,
              onNavigateToWebView = { navController.navigate("webview") },
              onNavigateToLicense = { navController.navigate("license_webview") },
              onNavigateToSystemInfo = { navController.navigate("system_info") }
          )
        }
        
        composable("system_info") {
          SystemInfoScreen(
              layoutStyle = layoutStyle,
              onNavigateBack = { navController.popBackStack() }
          )
        }
        
        composable("webview") {
          if (layoutStyle == "frosted") {
            FrostedWebViewScreen(
                url = "https://xtramanagersoftwares.tech/",
                title = "XMS Website",
                onNavigateBack = { navController.popBackStack() }
            )
          } else {
            MaterialWebViewScreen(
                url = "https://xtramanagersoftwares.tech/",
                title = "XMS Website",
                onNavigateBack = { navController.popBackStack() }
            )
          }
        }
        
        composable("license_webview") {
          if (layoutStyle == "frosted") {
            FrostedWebViewScreen(
                url = "https://raw.githubusercontent.com/Xtra-Computing/Xtra-Kernel-Manager/main/LICENSE",
                title = "MIT License",
                onNavigateBack = { navController.popBackStack() }
            )
          } else {
            MaterialWebViewScreen(
                url = "https://raw.githubusercontent.com/Xtra-Manager-Software/Xtra-Kernel-Manager/staging-version-3.1/LICENSE",
                title = "MIT License",
                onNavigateBack = { navController.popBackStack() }
            )
          }
        }
        
        composable("settings") {
          SettingsScreen(
              preferencesManager = preferencesManager,
              onNavigateBack = { navController.popBackStack() },
              onNavigateToDonation = { navController.navigate("donation") }
          )
        }
        
        composable("donation") {
          DonationScreen(
              onNavigateBack = { navController.popBackStack() }
          )
        }
      }
    }
    } // Close CompositionLocalProvider

    // Floating Bottom Dock
    if (currentRoute != "setup" && currentRoute != "root_access") {
      val selectedIndex = bottomNavItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
      val navigateToRoute: (String) -> Unit = { route ->
        if (currentRoute != route) {
          navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
          }
        }
      }

      if (layoutStyle == "frosted") {
        val isDark = isSystemInDarkTheme()
        val contentColor = if (isDark) androidx.compose.ui.graphics.Color.White 
                           else androidx.compose.ui.graphics.Color.Black
        
        FrostedBottomTabs(
            selectedTabIndex = { selectedIndex },
            onTabSelected = { index -> navigateToRoute(bottomNavItems[index].route) },
            tabsCount = bottomNavItems.size,
            backdrop = backdrop,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
          bottomNavItems.forEachIndexed { index, item ->
            FrostedBottomTab(
                onClick = { navigateToRoute(item.route) },
                onPress = { press() },
                onRelease = { release() }
            ) {
              Box {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                      imageVector = item.icon,
                      contentDescription = stringResource(item.label),
                      tint = contentColor,
                      modifier = Modifier.size(26.dp)
                  )
                  Text(
                      text = stringResource(item.label),
                      style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                      color = contentColor
                  )
                }
                // Badge for info tab when update is available
                if (item.route == "info" && updateState.hasUpdate) {
                  Box(
                      modifier = Modifier
                          .align(Alignment.TopEnd)
                          .offset(x = 8.dp, y = (-4).dp)
                          .size(8.dp)
                          .background(androidx.compose.ui.graphics.Color(0xFFFF6B6B), CircleShape)
                  )
                }
              }
            }
          }
        }
      } else if (layoutStyle == "classic") {
        ClassicBottomBar(
            currentRoute = currentRoute,
            onNavigate = navigateToRoute,
            items = bottomNavItems,
            hasUpdate = updateState.hasUpdate,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
      } else {
        MaterialFloatingBottomBar(
            currentRoute = currentRoute,
            onNavigate = navigateToRoute,
            items = bottomNavItems,
            hasUpdate = updateState.hasUpdate,
            onPowerMenuClick = { showPowerBottomSheet = true },
            modifier = Modifier
                .align(Alignment.BottomCenter),
        )
      }
    }

    // Power Menu Bottom Sheet for Material theme
    if (layoutStyle != "frosted" && layoutStyle != "classic" && showPowerBottomSheet) {
      ModalBottomSheet(
          onDismissRequest = { showPowerBottomSheet = false },
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
      ) {
        PowerMenuContent(
            onAction = { action ->
              showPowerBottomSheet = false
              scope.launch {
                RootShell.execute(action.command)
              }
            }
        )
      }
    }
  }
}

