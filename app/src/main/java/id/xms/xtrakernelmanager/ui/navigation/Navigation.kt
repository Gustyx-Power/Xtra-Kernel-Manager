package id.xms.xtrakernelmanager.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.preferences.PreferencesManager
import id.xms.xtrakernelmanager.ui.components.BottomNavItem
import id.xms.xtrakernelmanager.ui.components.ModernBottomBar
import id.xms.xtrakernelmanager.ui.screens.home.HomeScreen
import id.xms.xtrakernelmanager.ui.screens.info.InfoScreen
import id.xms.xtrakernelmanager.ui.screens.misc.MiscScreen
import id.xms.xtrakernelmanager.ui.screens.misc.MiscViewModel
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningScreen

@Composable
fun Navigation(preferencesManager: PreferencesManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
            label = R.string.nav_misc // Pastikan string resource ini ada (Profil/Misc)
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
                    MiscViewModel(preferencesManager)
                }
                MiscScreen(viewModel = miscViewModel)
            }

            composable("info") {
                InfoScreen()
            }
        }
    }
}