package com.denxhinjo.fabinventory.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

private data class BottomTab(val route: String, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    BottomTab(Routes.DASHBOARD, "Dashboard", Icons.Filled.Dashboard),
    BottomTab(Routes.PRODUCTS, "Products", Icons.Filled.Inventory2),
    BottomTab(Routes.MOVEMENTS, "Movements", Icons.Filled.SwapVert),
)

@Composable
fun MainScaffold(
    navController: NavHostController,
    currentRoute: String,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            if (currentRoute != tab.route) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        // The wrapped screen has its own nested Scaffold (its own top bar, and
        // sometimes its own FAB). Without applying this padding, that nested
        // Scaffold doesn't know to leave room for this bottom nav bar, so its
        // FAB ends up positioned underneath it instead of above it.
        Box(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            content(padding)
        }
    }
}
