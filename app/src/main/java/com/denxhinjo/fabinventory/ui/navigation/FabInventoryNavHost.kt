package com.denxhinjo.fabinventory.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.denxhinjo.fabinventory.ui.admin.AdminHomeScreen
import com.denxhinjo.fabinventory.ui.admin.EditUserAccessScreen
import com.denxhinjo.fabinventory.ui.admin.ManageAccessScreen
import com.denxhinjo.fabinventory.ui.dashboard.DashboardScreen
import com.denxhinjo.fabinventory.ui.locations.LocationFormScreen
import com.denxhinjo.fabinventory.ui.locations.LocationsListScreen
import com.denxhinjo.fabinventory.ui.login.LoginScreen
import com.denxhinjo.fabinventory.ui.movements.CreateMovementScreen
import com.denxhinjo.fabinventory.ui.movements.MovementsScreen
import com.denxhinjo.fabinventory.ui.products.ProductDetailScreen
import com.denxhinjo.fabinventory.ui.products.ProductFormScreen
import com.denxhinjo.fabinventory.ui.products.ProductsScreen
import com.denxhinjo.fabinventory.ui.suppliers.SupplierFormScreen
import com.denxhinjo.fabinventory.ui.suppliers.SuppliersListScreen
import com.denxhinjo.fabinventory.ui.users.UserFormScreen
import com.denxhinjo.fabinventory.ui.users.UsersListScreen

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
                    onManageAccess = { navController.navigate(Routes.ADMIN_HOME) },
                )
            }
        }

        composable(Routes.PRODUCTS) {
            MainScaffold(navController = navController, currentRoute = Routes.PRODUCTS) {
                ProductsScreen(
                    onProductClick = { id -> navController.navigate(Routes.productDetail(id)) },
                    onAddClick = { navController.navigate(Routes.productForm()) },
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
                onEdit = { id -> navController.navigate(Routes.productForm(id)) },
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

        composable(
            route = Routes.PRODUCT_FORM,
            arguments = listOf(
                navArgument(Routes.ARG_EDIT_PRODUCT_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            ProductFormScreen(
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }

        composable(Routes.ADMIN_HOME) {
            AdminHomeScreen(
                onBack = { navController.popBackStack() },
                onManageAccess = { navController.navigate(Routes.MANAGE_ACCESS) },
                onManageLocations = { navController.navigate(Routes.LOCATIONS_LIST) },
                onManageSuppliers = { navController.navigate(Routes.SUPPLIERS_LIST) },
                onManageUsers = { navController.navigate(Routes.USERS_LIST) },
            )
        }

        composable(Routes.MANAGE_ACCESS) {
            ManageAccessScreen(
                onBack = { navController.popBackStack() },
                onUserClick = { userId -> navController.navigate(Routes.editUserAccess(userId)) },
            )
        }

        composable(
            route = Routes.EDIT_USER_ACCESS,
            arguments = listOf(navArgument(Routes.ARG_USER_ID) { type = NavType.IntType }),
        ) {
            EditUserAccessScreen(
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }

        composable(Routes.LOCATIONS_LIST) {
            LocationsListScreen(
                onBack = { navController.popBackStack() },
                onAddClick = { navController.navigate(Routes.locationForm()) },
                onLocationClick = { id -> navController.navigate(Routes.locationForm(id)) },
            )
        }

        composable(
            route = Routes.LOCATION_FORM,
            arguments = listOf(
                navArgument(Routes.ARG_EDIT_LOCATION_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            LocationFormScreen(
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }

        composable(Routes.SUPPLIERS_LIST) {
            SuppliersListScreen(
                onBack = { navController.popBackStack() },
                onAddClick = { navController.navigate(Routes.supplierForm()) },
                onSupplierClick = { id -> navController.navigate(Routes.supplierForm(id)) },
            )
        }

        composable(
            route = Routes.SUPPLIER_FORM,
            arguments = listOf(
                navArgument(Routes.ARG_EDIT_SUPPLIER_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            SupplierFormScreen(
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }

        composable(Routes.USERS_LIST) {
            UsersListScreen(
                onBack = { navController.popBackStack() },
                onAddClick = { navController.navigate(Routes.userForm()) },
                onUserClick = { id -> navController.navigate(Routes.userForm(id)) },
            )
        }

        composable(
            route = Routes.USER_FORM,
            arguments = listOf(
                navArgument(Routes.ARG_EDIT_USER_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            UserFormScreen(
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
    }
}
