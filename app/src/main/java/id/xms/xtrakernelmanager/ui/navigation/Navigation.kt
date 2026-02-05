package id.xms.xtrakernelmanager.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import id.xms.xtrakernelmanager.ui.components.ModernBottomBar
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidBottomTabs
import id.xms.xtrakernelmanager.ui.components.liquid.LiquidBottomTab
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import id.xms.xtrakernelmanager.ui.screens.functionalrom.FunctionalRomScreen
import id.xms.xtrakernelmanager.ui.screens.functionalrom.FunctionalRomViewModel
import id.xms.xtrakernelmanager.ui.screens.functionalrom.PlayIntegritySettingsScreen
import id.xms.xtrakernelmanager.ui.screens.functionalrom.XiaomiTouchSettingsScreen
import id.xms.xtrakernelmanager.ui.screens.home.HomeScreen
import id.xms.xtrakernelmanager.ui.screens.info.InfoScreen
import id.xms.xtrakernelmanager.ui.screens.misc.material.MaterialGameAppSelectorScreen
import id.xms.xtrakernelmanager.ui.screens.misc.MiscScreen
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import id.xms.xtrakernelmanager.ui.screens.setup.SetupScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components.LiquidCPUSettingsScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.liquid.components.SmartFrequencyLockScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.CPUTuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.MemoryTuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.components.MaterialSmartFrequencyLockScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.components.MaterialThermalSettingsScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.components.MaterialThermalIndexSelectionScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.material.components.MaterialThermalPolicySelectionScreen
import id.xms.xtrakernelmanager.utils.Holiday
import id.xms.xtrakernelmanager.utils.HolidayChecker
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun Navigation(preferencesManager: PreferencesManager) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val scope = rememberCoroutineScope()

  // Holiday celebration state
  var showHolidayDialog by remember { mutableStateOf(false) }
  var currentHoliday by remember { mutableStateOf<Holiday?>(null) }
  var hasCheckedHoliday by remember { mutableStateOf(false) }
  val currentYear = HolidayChecker.getCurrentYear()
  val currentHijriYear = HolidayChecker.getCurrentHijriYear()
  
  // Layout switching state
  var showLayoutSwitchToast by remember { mutableStateOf(false) }
  var layoutSwitchMessage by remember { mutableStateOf("") }
  
  // Timeout mechanism for layout switching
  var layoutSwitchingStartTime by remember { mutableStateOf(0L) }

  // Collect holiday shown years from preferences
  val christmasShownYear by preferencesManager.getChristmasShownYear().collectAsState(initial = 0)
  val newYearShownYear by preferencesManager.getNewYearShownYear().collectAsState(initial = 0)
  val ramadanShownYear by preferencesManager.getRamadanShownYear().collectAsState(initial = 0)
  val eidFitrShownYear by preferencesManager.getEidFitrShownYear().collectAsState(initial = 0)

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

  // Show nothing while checking setup state to confirm start destination
  if (isSetupCompleteState == null) return

  val startDest = if (isSetupCompleteState == true) "home" else "setup"

  // Redirect to Home if setup is completed while on setup screen
  LaunchedEffect(isSetupCompleteState, currentRoute) {
    if (isSetupCompleteState == true && currentRoute == "setup") {
      navController.navigate("home") { popUpTo("setup") { inclusive = true } }
    }
  }
  val layoutStyle by preferencesManager.getLayoutStyle().collectAsState(initial = "liquid")
  val isLayoutSwitching by preferencesManager.isLayoutSwitching().collectAsState(initial = false)
  
  // Track layout switching completion
  var previousLayoutSwitching by remember { mutableStateOf(false) }
  
  LaunchedEffect(isLayoutSwitching) {
    if (isLayoutSwitching && !previousLayoutSwitching) {
      // Layout switching just started - record start time
      layoutSwitchingStartTime = System.currentTimeMillis()
    } else if (previousLayoutSwitching && !isLayoutSwitching) {
      // Layout switching just completed
      val layoutName = if (layoutStyle == "liquid") "Liquid Glass" else "Material"
      layoutSwitchMessage = "Successfully switched to $layoutName layout!"
      showLayoutSwitchToast = true
      layoutSwitchingStartTime = 0L
    }
    previousLayoutSwitching = isLayoutSwitching
  }
  
  // Timeout mechanism - reset loading state if stuck for more than 5 seconds
  LaunchedEffect(isLayoutSwitching, layoutSwitchingStartTime) {
    if (isLayoutSwitching && layoutSwitchingStartTime > 0L) {
      kotlinx.coroutines.delay(5000) // Wait 5 seconds
      if (System.currentTimeMillis() - layoutSwitchingStartTime > 5000) {
        // Force reset if still loading after 5 seconds
        android.util.Log.w("Navigation", "Layout switching timeout - forcing reset")
        preferencesManager.resetLayoutSwitching()
        layoutSwitchMessage = "Layout switching completed"
        showLayoutSwitchToast = true
        layoutSwitchingStartTime = 0L
      }
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
      NavHost(
          navController = navController,
          startDestination = startDest,
          modifier = Modifier
              .padding(paddingValues)
              .padding(bottom = if (layoutStyle == "liquid") 0.dp else 120.dp),
      ) {
        composable("setup") {
          SetupScreen(
              onSetupComplete = { layoutStyle ->
                scope.launch {
                  preferencesManager.setLayoutStyle(layoutStyle)
                  preferencesManager.setSetupComplete(true)
                }
                navController.navigate("home") { popUpTo("setup") { inclusive = true } }
              }
          )
        }
        composable("home") { 
          HomeScreen(preferencesManager = preferencesManager) 
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
          LiquidCPUSettingsScreen(
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
          CPUTuningScreen(
              viewModel = tuningViewModel,
              onNavigateBack = { navController.popBackStack() },
              onNavigateToSmartLock = { navController.navigate("material_smart_frequency_lock") }
          )
        }
        composable("material_smart_frequency_lock") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          MaterialSmartFrequencyLockScreen(
              viewModel = tuningViewModel,
              onNavigateBack = { navController.popBackStack() }
          )
        }
        composable("memory_tuning") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          MemoryTuningScreen(viewModel = tuningViewModel, navController = navController)
        }
        composable("material_thermal_settings") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          MaterialThermalSettingsScreen(
              viewModel = tuningViewModel,
              onNavigateBack = { navController.popBackStack() },
              onNavigateToIndexSelection = { navController.navigate("material_thermal_index_selection") },
              onNavigateToPolicySelection = { navController.navigate("material_thermal_policy_selection") }
          )
        }
        composable("material_thermal_index_selection") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          val currentIndex by tuningViewModel.preferencesManager.getThermalPreset().collectAsState(initial = "Not Set")
          val currentOnBoot by tuningViewModel.preferencesManager.getThermalSetOnBoot().collectAsState(initial = false)
          MaterialThermalIndexSelectionScreen(
              viewModel = tuningViewModel,
              currentIndex = currentIndex,
              onNavigateBack = { navController.popBackStack() },
              onIndexSelected = { index ->
                  tuningViewModel.setThermalPreset(index, currentOnBoot)
                  navController.popBackStack()
              }
          )
        }
        composable("material_thermal_policy_selection") {
          val factory = TuningViewModel.Factory(preferencesManager)
          val tuningViewModel: TuningViewModel = viewModel(factory = factory)
          val currentPolicy by tuningViewModel.getCpuLockThermalPolicy().collectAsState(initial = "Policy B (Balanced)")
          MaterialThermalPolicySelectionScreen(
              viewModel = tuningViewModel,
              currentPolicy = currentPolicy,
              onNavigateBack = { navController.popBackStack() },
              onPolicySelected = { policy ->
                  tuningViewModel.setCpuLockThermalPolicy(policy)
                  navController.popBackStack()
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
          MaterialGameAppSelectorScreen(
              viewModel = miscViewModel,
              onBack = { navController.popBackStack() },
          )
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
          val context = LocalContext.current
          val functionalRomViewModel = remember {
            FunctionalRomViewModel(
                preferencesManager = preferencesManager,
                context = context.applicationContext,
            )
          }
          FunctionalRomScreen(
              onNavigateBack = { navController.popBackStack() },
              onNavigateToPlayIntegrity = { navController.navigate("playintegritysettings") },
              onNavigateToXiaomiTouch = { navController.navigate("xiaomitouchsettings") },
              viewModel = functionalRomViewModel,
          )
        }

        composable("playintegritysettings") {
          PlayIntegritySettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("xiaomitouchsettings") {
          XiaomiTouchSettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("info") { InfoScreen(preferencesManager) }
      }
    }

    // Morphing layout switcher
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(horizontal = 16.dp)
            .padding(bottom = if (layoutStyle == "liquid") 120.dp else 180.dp)
    ) {
      // Simple target layout name
      val targetLayoutName = if (layoutStyle == "liquid") "Liquid Glass" else "Material"
      id.xms.xtrakernelmanager.ui.components.MorphingLayoutSwitcher(
          isLoading = isLayoutSwitching,
          targetLayout = targetLayoutName,
          onComplete = {
            layoutSwitchMessage = "Successfully switched to $targetLayoutName layout!"
            showLayoutSwitchToast = true
          }
      )
    }
    
    // Layout switch success toast
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
    ) {
      id.xms.xtrakernelmanager.ui.components.LayoutSwitchToast(
          message = layoutSwitchMessage,
          isVisible = showLayoutSwitchToast,
          onDismiss = { 
            showLayoutSwitchToast = false
            layoutSwitchMessage = ""
          }
      )
    }

    // Floating Bottom Dock
    if (currentRoute != "setup") {
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

      if (layoutStyle == "liquid") {
        val isDark = isSystemInDarkTheme()
        val contentColor = if (isDark) androidx.compose.ui.graphics.Color.White 
                           else androidx.compose.ui.graphics.Color.Black
        
        LiquidBottomTabs(
            selectedTabIndex = { selectedIndex },
            onTabSelected = { index -> navigateToRoute(bottomNavItems[index].route) },
            tabsCount = bottomNavItems.size,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
          bottomNavItems.forEachIndexed { index, item ->
            LiquidBottomTab(
                onClick = { navigateToRoute(item.route) },
                onPress = { press() },
                onRelease = { release() }
            ) {
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
          }
        }
      } else {
        ModernBottomBar(
            currentRoute = currentRoute,
            onNavigate = navigateToRoute,
            items = bottomNavItems,
            modifier = Modifier
                .align(Alignment.BottomCenter),
        )
      }
    }
  }
}

