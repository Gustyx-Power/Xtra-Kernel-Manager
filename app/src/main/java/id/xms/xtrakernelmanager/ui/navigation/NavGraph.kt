package id.xms.xtrakernelmanager.ui.navigation

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.xms.xtrakernelmanager.data.repository.BatteryRepository
import id.xms.xtrakernelmanager.data.repository.KernelRepository
import id.xms.xtrakernelmanager.data.repository.SystemInfoRepository
import id.xms.xtrakernelmanager.ui.screens.home.HomeScreen
import id.xms.xtrakernelmanager.ui.screens.home.HomeViewModel
import id.xms.xtrakernelmanager.ui.screens.info.InfoScreen
import id.xms.xtrakernelmanager.ui.screens.info.InfoViewModel
import id.xms.xtrakernelmanager.ui.screens.misc.MiscScreen
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import id.xms.xtrakernelmanager.ui.screens.settings.SettingsScreen
import id.xms.xtrakernelmanager.ui.screens.settings.SettingsViewModel
import id.xms.xtrakernelmanager.ui.screens.splash.SplashScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningScreen
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Tuning : Screen("tuning")
    object Misc : Screen("misc")
    object Info : Screen("info")
    object Settings : Screen("settings")
}

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem(Screen.Home.route, Icons.Default.Home, "Home")
    object Tuning : BottomNavItem(Screen.Tuning.route, Icons.Default.Tune, "Tuning")
    object Misc : BottomNavItem(Screen.Misc.route, Icons.Default.Extension, "Misc")
    object Info : BottomNavItem(Screen.Info.route, Icons.Default.Info, "Info")
}

@Composable
fun AppNavigation(
    context: Context,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(
            onTimeout = { showSplash = false }
        )
    } else {
        Scaffold(
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                if (currentDestination?.route != Screen.Settings.route) {
                    NavigationBar {
                        val items = listOf(
                            BottomNavItem.Home,
                            BottomNavItem.Tuning,
                            BottomNavItem.Misc,
                            BottomNavItem.Info
                        )

                        items.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = modifier.padding(innerPadding),
                enterTransition = {
                    fadeIn(animationSpec = tween(300)) +
                            slideInHorizontally(animationSpec = tween(300)) { it }
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300)) +
                            slideOutHorizontally(animationSpec = tween(300)) { -it }
                }
            ) {
                composable(Screen.Home.route) {
                    val viewModel = remember {
                        HomeViewModel(
                            context = context,
                            kernelRepository = KernelRepository(),
                            batteryRepository = BatteryRepository(context),
                            systemInfoRepository = SystemInfoRepository(context)
                        )
                    }
                    HomeScreen(
                        viewModel = viewModel,
                        onSettingsClick = { navController.navigate(Screen.Settings.route) }
                    )
                }

                composable(Screen.Tuning.route) {
                    val viewModel = remember {
                        TuningViewModel(
                            application = context.applicationContext as android.app.Application, // ✅ FIXED
                            kernelRepository = KernelRepository()
                        )
                    }
                    TuningScreen(viewModel = viewModel)
                }

                composable(Screen.Misc.route) {
                    val viewModel = remember {
                        MiscViewModel(
                            application = context.applicationContext as android.app.Application,
                            batteryRepository = BatteryRepository(context)
                        )
                    }
                    MiscScreen(viewModel = viewModel)
                }

                composable(Screen.Info.route) {
                    val viewModel = remember {
                        InfoViewModel(
                            application = context.applicationContext as android.app.Application,
                            systemInfoRepository = SystemInfoRepository(context)
                        )
                    }
                    InfoScreen(viewModel = viewModel)
                }

                composable(Screen.Settings.route) {
                    val viewModel = remember {
                        SettingsViewModel(
                            application = context.applicationContext as android.app.Application
                        )
                    }
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
