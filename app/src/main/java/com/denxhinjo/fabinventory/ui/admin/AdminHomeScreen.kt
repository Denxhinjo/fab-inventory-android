package com.denxhinjo.fabinventory.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.denxhinjo.fabinventory.ui.common.AppCard

private data class AdminAction(val title: String, val subtitle: String, val icon: ImageVector, val onClick: () -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    onBack: () -> Unit,
    onManageAccess: () -> Unit,
    onManageLocations: () -> Unit,
    onManageSuppliers: () -> Unit,
    onManageUsers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actions = listOf(
        AdminAction(
            "Users",
            "Create, edit, deactivate, or delete user accounts",
            Icons.Filled.People,
            onManageUsers,
        ),
        AdminAction(
            "Warehouse access",
            "Choose which warehouses each user can add or edit products in",
            Icons.Filled.AdminPanelSettings,
            onManageAccess,
        ),
        AdminAction(
            "Warehouses",
            "Add, edit, or remove warehouse locations",
            Icons.Filled.Warehouse,
            onManageLocations,
        ),
        AdminAction(
            "Suppliers",
            "Add, edit, or remove suppliers",
            Icons.Filled.LocalShipping,
            onManageSuppliers,
        ),
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Admin") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            actions.forEach { action ->
                AppCard(
                    onClick = action.onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(action.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(action.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                        Text(action.subtitle, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
