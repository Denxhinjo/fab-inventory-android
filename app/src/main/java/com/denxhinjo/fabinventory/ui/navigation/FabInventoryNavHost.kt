package com.denxhinjo.fabinventory.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.denxhinjo.fabinventory.ui.dashboard.DashboardScreen
import com.denxhinjo.fabinventory.ui.login.LoginScreen
import com.denxhinjo.fabinventory.ui.movements.CreateMovementScreen
import com.denxhinjo.fabinventory.ui.movements.MovementsScreen
import com.denxhinjo.fabinventory.ui.products.ProductDetailScreen
import com.denxhinjo.fabinventory.ui.products.ProductsScreen

@Composable
fun FabInventoryNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.DASHBOARD) {
            MainScaffold(navController = navController, currentRoute = Routes.DASHBOARD) {
                DashboardScreen(
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
        }

        composable(Routes.PRODUCTS) {
            MainScaffold(navController = navController, currentRoute = Routes.PRODUCTS) {
                ProductsScreen(
                    onProductClick = { id -> navController.navigate(Routes.productDetail(id)) },
                )
            }
        }

        composable(Routes.MOVEMENTS) {
            MainScaffold(navController = navController, currentRoute = Routes.MOVEMENTS) {
                MovementsScreen(
                    onAddClick = { navController.navigate(Routes.createMovement()) },
                )
            }
        }

        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_PRODUCT_ID) { type = NavType.IntType }),
        ) {
            ProductDetailScreen(
                onBack = { navController.popBackStack() },
                onRecordMovement = { id -> navController.navigate(Routes.createMovement(id)) },
            )
        }

        composable(
            route = Routes.CREATE_MOVEMENT,
            arguments = listOf(
                navArgument(Routes.ARG_PREFILLED_PRODUCT_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            CreateMovementScreen(
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
    }
}
