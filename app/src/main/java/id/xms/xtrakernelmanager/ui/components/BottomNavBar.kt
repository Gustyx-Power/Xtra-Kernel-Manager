package id.xms.xtrakernelmanager.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
// Removed: import androidx.compose.foundation.layout.padding, not directly used now
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.* // Import outlined icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
// Removed: import androidx.compose.ui.graphics.Color // No longer needed by the helper for this approach
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight // Import FontWeight
// Removed: import androidx.compose.ui.unit.dp // Not directly used now
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavHostController, items: List<String>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEach { screen ->
            val selected = currentRoute == screen.lowercase()
            val (filledIcon, outlinedIcon) = getNavIcons(screen)
            val iconToDisplay = if (selected) filledIcon else outlinedIcon

            NavigationBarItem(
                selected = selected,
                onClick = {
                    val targetRoute = screen.lowercase()
                    // Jika pengguna berada di SettingsScreen (atau screen lain yang tidak ada di bottom nav),
                    // dan mengklik tombol home, arahkan ke home screen
                    if (currentRoute != "home" && targetRoute == "home") {
                        navController.navigate(targetRoute) {
                            // Pop semua screen sampai kembali ke home
                            popUpTo("home") {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    } else if (currentRoute != targetRoute) {
                        // Untuk screen lain dalam bottom nav, gunakan navigasi normal
                        navController.navigate(targetRoute) {
                            popUpTo("home") {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(imageVector = iconToDisplay, contentDescription = screen) },
                label = {
                    Text(
                        screen,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface, // Or onSecondaryContainer
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// Helper function to get filled and outlined icons for each screen.
private fun getNavIcons(screen: String): Pair<ImageVector, ImageVector> { // Pair(Filled, Outlined)
    return when (screen.lowercase()) {
        "home" -> Pair(Icons.Filled.Home, Icons.Outlined.Home)
        "tuning" -> Pair(Icons.Filled.Build, Icons.Outlined.Build) // Assuming Build has an Outlined version
        "misc" -> Pair(Icons.Filled.Tune, Icons.Outlined.Tune) // Changed icon
        "info" -> Pair(Icons.Filled.Info, Icons.Outlined.Info)
        // Fallback icons if a screen name doesn't match
        else -> Pair(Icons.Filled.Help, Icons.Outlined.HelpOutline) // Example fallback
    }
}