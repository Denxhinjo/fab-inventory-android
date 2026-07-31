package com.denxhinjo.fabinventory.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denxhinjo.fabinventory.data.remote.dto.DashboardResponse
import com.denxhinjo.fabinventory.data.remote.dto.LowStockItem
import com.denxhinjo.fabinventory.data.remote.dto.RecentActivityItem
import com.denxhinjo.fabinventory.ui.common.AppCard
import com.denxhinjo.fabinventory.ui.common.FullScreenError
import com.denxhinjo.fabinventory.ui.common.FullScreenLoading
import com.denxhinjo.fabinventory.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onManageAccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(session?.fullName?.takeIf { it.isNotBlank() }?.let { "Hi, $it" } ?: "Dashboard") },
                actions = {
                    if (session?.role == "admin") {
                        IconButton(onClick = onManageAccess) {
                            Icon(Icons.Filled.AdminPanelSettings, contentDescription = "Manage warehouse access")
                        }
                    }
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> FullScreenLoading(modifier = Modifier.padding(padding))
            is UiState.Error -> FullScreenError(
                message = state.message,
                onRetry = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            is UiState.Success -> DashboardContent(
                data = state.data,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun DashboardContent(data: DashboardResponse, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    icon = Icons.Filled.Inventory2,
                    label = "Products",
                    value = data.stats.totalProducts.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    icon = Icons.Filled.WarningAmber,
                    label = "Low stock",
                    value = data.stats.lowStockProducts.toString(),
                    emphasize = data.stats.lowStockProducts > 0,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    icon = Icons.Filled.Warehouse,
                    label = "Locations",
                    value = data.stats.totalLocations.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    icon = Icons.Filled.Engineering,
                    label = "Active work",
                    value = data.stats.activeWorkProcesses.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconBadge(icon = Icons.Filled.SwapVert)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text("Last 30 days", style = MaterialTheme.typography.titleMedium)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Stock in: ${data.stockSummary.stockIn30d.formatQty()}")
                            Text("Stock out: ${data.stockSummary.stockOut30d.formatQty()}")
                        }
                    }
                }
            }
        }

        if (data.lowStockItems.isNotEmpty()) {
            item { SectionHeader("Low stock items") }
            // Prefixed keys: low-stock items and recent-activity entries come from
            // different tables (products vs. stock movements) and can share the
            // same numeric id, which crashes LazyColumn if used as-is for both.
            items(data.lowStockItems, key = { "low_stock_${it.id}" }) { item ->
                LowStockRow(item, modifier = Modifier.animateItem())
            }
        }

        if (data.recentActivity.isNotEmpty()) {
            item { SectionHeader("Recent activity") }
            items(data.recentActivity, key = { "activity_${it.id}" }) { item ->
                RecentActivityRow(item, modifier = Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun IconBadge(icon: ImageVector, tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasize: Boolean = false,
) {
    AppCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            IconBadge(
                icon = icon,
                tint = if (emphasize) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = if (emphasize) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun LowStockRow(item: LowStockItem, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${item.quantity.formatQty()} ${item.unit} left (min ${item.minStockLevel.formatQty()})" +
                    (item.location?.let { " · $it" } ?: ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
    HorizontalDivider()
}

@Composable
private fun RecentActivityRow(item: RecentActivityItem, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${item.type} · ${item.productName}", style = MaterialTheme.typography.titleMedium)
            Text(
                "${item.quantity.formatQty()} ${item.unit} · ${item.userName} · ${item.date}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
    HorizontalDivider()
}

private fun Double.formatQty(): String =
    if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()
