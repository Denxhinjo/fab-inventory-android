package com.denxhinjo.fabinventory.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denxhinjo.fabinventory.data.remote.dto.CategoryResponse
import com.denxhinjo.fabinventory.data.remote.dto.DashboardResponse
import com.denxhinjo.fabinventory.data.remote.dto.LowStockItem
import com.denxhinjo.fabinventory.data.remote.dto.RecentActivityItem
import com.denxhinjo.fabinventory.data.remote.dto.SupplierResponse
import com.denxhinjo.fabinventory.ui.common.AppCard
import com.denxhinjo.fabinventory.ui.common.FullScreenError
import com.denxhinjo.fabinventory.ui.common.FullScreenLoading
import com.denxhinjo.fabinventory.ui.common.UiState
import com.denxhinjo.fabinventory.ui.theme.ColorAccent
import com.denxhinjo.fabinventory.ui.theme.ColorAccent2
import com.denxhinjo.fabinventory.ui.theme.ColorSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onManageAccess: () -> Unit,
    onAddProduct: () -> Unit,
    onRecordMovement: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val trend by viewModel.trend.collectAsStateWithLifecycle()
    val topSuppliers by viewModel.topSuppliers.collectAsStateWithLifecycle()

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
                categories = categories,
                trend = trend,
                topSuppliers = topSuppliers,
                onAddProduct = onAddProduct,
                onRecordMovement = onRecordMovement,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun DashboardContent(
    data: DashboardResponse,
    categories: List<CategoryResponse>,
    trend: List<DayPoint>,
    topSuppliers: List<SupplierResponse>,
    onAddProduct: () -> Unit,
    onRecordMovement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { InventoryHeroCard(data = data) }

        item { WeeklyProgressCard(thisWeek = data.movementsThisWeek, lastWeek = data.movementsLastWeek) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FlowCard(
                    label = "Stock in",
                    value = data.stockSummary.stockIn30d,
                    color = ColorSuccess,
                    icon = Icons.Filled.ArrowDownward,
                    modifier = Modifier.weight(1f),
                )
                FlowCard(
                    label = "Stock out",
                    value = data.stockSummary.stockOut30d,
                    color = MaterialTheme.colorScheme.error,
                    icon = Icons.Filled.ArrowUpward,
                    modifier = Modifier.weight(1f),
                )
                FlowCard(
                    label = "Net change",
                    value = data.stockSummary.stockIn30d - data.stockSummary.stockOut30d,
                    color = ColorAccent2,
                    icon = Icons.Filled.SwapVert,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Health overview", style = MaterialTheme.typography.titleMedium)
                    Spacer12()
                    val total = data.stats.totalProducts.coerceAtLeast(1)
                    val healthy = data.stats.totalProducts - data.stats.lowStockProducts
                    val wpTotal = data.workProcessByStatus.values.sum().coerceAtLeast(1)
                    val wpDone = data.workProcessByStatus["Done"] ?: 0
                    GaugeRow(
                        entries = listOf(
                            GaugeEntry("Stock health", Math.round(100f * healthy / total), ColorSuccess),
                            GaugeEntry("Work done", Math.round(100f * wpDone / wpTotal), ColorAccent2),
                            GaugeEntry("Needs attention", Math.round(100f * data.stats.lowStockProducts / total), MaterialTheme.colorScheme.error),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Work process overview", style = MaterialTheme.typography.titleMedium)
                    Spacer12()
                    WorkProcessBreakdown(data.workProcessByStatus)
                }
            }
        }

        if (data.topMovedProducts.isNotEmpty()) {
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Top moved products (30 days)", style = MaterialTheme.typography.titleMedium)
                        Spacer12()
                        BarChart(
                            entries = data.topMovedProducts.map { BarEntry(it.name, it.quantity) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        item {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Stock movement (14 days)", style = MaterialTheme.typography.titleMedium)
                    Spacer12()
                    TrendChart(points = trend, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        if (categories.isNotEmpty()) {
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Products by category", style = MaterialTheme.typography.titleMedium)
                        Spacer12()
                        DonutChart(
                            slices = categories
                                .filter { it.productCount > 0 }
                                .mapIndexed { index, c -> DonutSlice(c.name, c.productCount, donutColor(index)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        if (topSuppliers.isNotEmpty()) {
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Top suppliers", style = MaterialTheme.typography.titleMedium)
                        Spacer12()
                        topSuppliers.forEach { supplier ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(supplier.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${supplier.productCount ?: 0} products",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        item { QuickActionsRow(onAddProduct = onAddProduct, onRecordMovement = onRecordMovement) }

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
private fun Spacer12() = androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))

@Composable
private fun InventoryHeroCard(data: DashboardResponse) {
    val healthy = data.stats.totalProducts - data.stats.lowStockProducts
    val healthPct = if (data.stats.totalProducts > 0) {
        (healthy.toFloat() / data.stats.totalProducts.toFloat()).coerceIn(0f, 1f)
    } else {
        1f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(ColorAccent2, ColorAccent)))
            .padding(20.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Color.White)
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text("Total Inventory Value", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "$${data.stats.totalInventoryValue.formatMoney()}",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }

            Spacer12()

            Text(
                "Stock health: ${Math.round(healthPct * 100)}% of products above minimum",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodySmall,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.25f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(healthPct)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White),
                )
            }
        }
    }
}

@Composable
private fun WeeklyProgressCard(thisWeek: Int, lastWeek: Int) {
    val progress = if (maxOf(thisWeek, lastWeek) > 0) {
        thisWeek.toFloat() / maxOf(thisWeek, lastWeek).toFloat()
    } else {
        0f
    }
    val growth = if (lastWeek > 0) Math.round(100f * (thisWeek - lastWeek) / lastWeek) else null

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Movements this week", style = MaterialTheme.typography.bodyMedium)
            Text("$thisWeek", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 2.dp))
            Spacer12()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0.03f, 1f))
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
            if (growth != null) {
                Text(
                    text = "${if (growth >= 0) "↑" else "↓"} ${kotlin.math.abs(growth)}% vs last week",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (growth >= 0) ColorSuccess else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun FlowCard(label: String, value: Double, color: Color, icon: ImageVector, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .background(color.copy(alpha = 0.12f))
                .padding(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Text(
                text = value.formatQty(),
                style = MaterialTheme.typography.titleMedium,
                color = color,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickActionsRow(onAddProduct: () -> Unit, onRecordMovement: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppCard(onClick = onAddProduct, modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconBadge(icon = Icons.Filled.Inventory2)
                Text("Add product", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            }
        }
        AppCard(onClick = onRecordMovement, modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconBadge(icon = Icons.AutoMirrored.Filled.PlaylistAddCheck)
                Text("Record movement", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

private val workProcessColors = mapOf(
    "Not Started" to Color(0xFF5C6B81),
    "Started" to ColorAccent2,
    "In Process" to Color(0xFFF59E0B),
    "Done" to ColorSuccess,
)

@Composable
private fun WorkProcessBreakdown(byStatus: Map<String, Int>) {
    val total = byStatus.values.sum().coerceAtLeast(1)
    val order = listOf("Not Started", "Started", "In Process", "Done")
    Column {
        order.forEach { status ->
            val count = byStatus[status] ?: 0
            val pct = Math.round(100f * count / total)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(workProcessColors[status] ?: MaterialTheme.colorScheme.onSurfaceVariant),
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp).weight(1f),
                )
                Text(text = "$count", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = " · $pct%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun donutColor(index: Int): Color {
    val palette = listOf(
        ColorAccent,
        ColorAccent2,
        Color(0xFFF59E0B),
        ColorSuccess,
        Color(0xFFEC4899),
        Color(0xFF8B5CF6),
    )
    return palette[index % palette.size]
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
private fun IconBadge(icon: ImageVector, tint: Color = MaterialTheme.colorScheme.primary) {
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

private fun Double.formatMoney(): String {
    val rounded = Math.round(this)
    val digits = rounded.toString()
    val sb = StringBuilder()
    digits.reversed().forEachIndexed { index, c ->
        if (index != 0 && index % 3 == 0) sb.append(',')
        sb.append(c)
    }
    return sb.reverse().toString()
}
