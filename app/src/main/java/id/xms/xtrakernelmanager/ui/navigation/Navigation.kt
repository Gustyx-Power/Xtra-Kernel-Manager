package id.xms.xtrakernelmanager.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.BottomNavItem
import id.xms.xtrakernelmanager.ui.components.HolidayCelebrationDialog
import id.xms.xtrakernelmanager.ui.components.ModernBottomBar
import id.xms.xtrakernelmanager.ui.screens.home.HomeScreen
import id.xms.xtrakernelmanager.ui.screens.info.InfoScreen
import id.xms.xtrakernelmanager.ui.screens.misc.MiscScreen
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningScreen
import id.xms.xtrakernelmanager.ui.screens.functionalrom.FunctionalRomScreen
import id.xms.xtrakernelmanager.ui.screens.functionalrom.FunctionalRomViewModel
import id.xms.xtrakernelmanager.ui.screens.functionalrom.PlayIntegritySettingsScreen
import id.xms.xtrakernelmanager.ui.screens.functionalrom.XiaomiTouchSettingsScreen
import id.xms.xtrakernelmanager.utils.Holiday
import id.xms.xtrakernelmanager.utils.HolidayChecker
import kotlinx.coroutines.launch

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
                val lastShownYear = when (holiday) {
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
            year = if (currentHoliday == Holiday.RAMADAN || currentHoliday == Holiday.EID_FITR) currentHijriYear else currentYear,
            onDismiss = {
                showHolidayDialog = false
                // Mark this holiday as shown for current year (after dialog closed)
                scope.launch {
                    val yearToSave = if (currentHoliday == Holiday.RAMADAN || currentHoliday == Holiday.EID_FITR) currentHijriYear else currentYear
                    when (currentHoliday) {
                        Holiday.CHRISTMAS -> preferencesManager.setChristmasShownYear(yearToSave)
                        Holiday.NEW_YEAR -> preferencesManager.setNewYearShownYear(yearToSave)
                        Holiday.RAMADAN -> preferencesManager.setRamadanShownYear(yearToSave)
                        Holiday.EID_FITR -> preferencesManager.setEidFitrShownYear(yearToSave)
                        null -> {}
                    }
                    currentHoliday = null
                }
            }
        )
    }

    val bottomNavItems = listOf(
        BottomNavItem(
            route = "home",
            icon = Icons.Default.Home,
            label = R.string.nav_home
        ),
        BottomNavItem(
            route = "tuning",
            icon = Icons.Default.Settings,
            label = R.string.nav_tuning
        ),
        BottomNavItem(
            route = "profiles",
            icon = Icons.Default.Speed,
            label = R.string.nav_misc
        ),
        BottomNavItem(
            route = "info",
            icon = Icons.Default.Info,
            label = R.string.nav_info
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            ModernBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                items = bottomNavItems
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(preferencesManager = preferencesManager)
            }
            composable("tuning") {
                TuningScreen(preferencesManager = preferencesManager)
            }
            composable("profiles") {
                val context = LocalContext.current
                val miscViewModel = remember {
                    MiscViewModel(
                        preferencesManager = preferencesManager,
                        context = context.applicationContext
                    )
                }
                MiscScreen(
                    viewModel = miscViewModel,
                    onNavigateToFunctionalRom = {
                        navController.navigate("functionalrom")
                    }
                )
            }

            composable("functionalrom") {
                val context = LocalContext.current
                val functionalRomViewModel = remember {
                    FunctionalRomViewModel(
                        preferencesManager = preferencesManager,
                        context = context.applicationContext
                    )
                }
                FunctionalRomScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToPlayIntegrity = {
                        navController.navigate("playintegritysettings")
                    },
                    onNavigateToXiaomiTouch = {
                        navController.navigate("xiaomitouchsettings")
                    },
                    viewModel = functionalRomViewModel
                )
            }

            composable("playintegritysettings") {
                PlayIntegritySettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("xiaomitouchsettings") {
                XiaomiTouchSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("info") {
                InfoScreen()
            }
        }
    }
}
