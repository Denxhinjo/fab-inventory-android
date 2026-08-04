package com.denxhinjo.fabinventory.ui.movements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denxhinjo.fabinventory.R
import com.denxhinjo.fabinventory.data.remote.dto.MovementType
import com.denxhinjo.fabinventory.data.remote.dto.StockMovementResponse
import com.denxhinjo.fabinventory.ui.common.AppCard
import com.denxhinjo.fabinventory.ui.common.EmptyState
import com.denxhinjo.fabinventory.ui.common.FullScreenError
import com.denxhinjo.fabinventory.ui.common.FullScreenLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementsScreen(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovementsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItems = layoutInfo.totalItemsCount
                if (totalItems > 0 && lastVisible >= totalItems - 3) {
                    viewModel.loadMore()
                }
            }
    }

    Scaffold(
        modifier = modifier,
        topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(R.string.movements_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.movements_record_cd))
            }
        },
    ) { padding ->
        when {
            uiState.isLoading && uiState.movements.isEmpty() -> FullScreenLoading(modifier = Modifier.padding(padding))
            uiState.error != null && uiState.movements.isEmpty() -> FullScreenError(
                message = uiState.error ?: stringResource(R.string.common_something_wrong),
                onRetry = viewModel::refresh,
                modifier = Modifier.padding(padding),
            )
            uiState.movements.isEmpty() -> EmptyState(
                icon = Icons.Filled.SwapVert,
                title = stringResource(R.string.movements_empty_title),
                subtitle = stringResource(R.string.movements_empty_subtitle),
                modifier = Modifier.padding(padding),
            )
            else -> PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = viewModel::refresh,
                modifier = Modifier.padding(padding).fillMaxSize(),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    items(uiState.movements, key = { it.id }) { movement ->
                        MovementRow(movement, modifier = Modifier.animateItem())
                    }
                    if (uiState.isLoadingMore) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovementRow(movement: StockMovementResponse, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = movement.product?.name ?: stringResource(R.string.movements_unknown_product, movement.productId),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "${movementTypeLabel(movement.movementType)} · ${movement.quantity.trimmed()} ${movement.product?.unit ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = movement.movementType.colorFor(),
            )
            Text(
                text = "${movement.movementDate} · ${movement.user?.fullName ?: stringResource(R.string.movements_unknown_user)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            movement.reason?.takeIf { it.isNotBlank() }?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
internal fun movementTypeLabel(type: String): String = when (type) {
    MovementType.STOCK_IN -> stringResource(R.string.movement_type_stock_in)
    MovementType.STOCK_OUT -> stringResource(R.string.movement_type_stock_out)
    MovementType.ADJUSTMENT -> stringResource(R.string.movement_type_adjustment)
    else -> type
}

@Composable
private fun String.colorFor(): Color = when (this) {
    MovementType.STOCK_IN -> Color(0xFF2E7D32)
    MovementType.STOCK_OUT -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun Double.trimmed(): String =
    if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()
