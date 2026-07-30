package com.denxhinjo.fabinventory.ui.products

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.denxhinjo.fabinventory.data.remote.dto.ProductResponse
import com.denxhinjo.fabinventory.data.remote.resolveMediaUrl
import com.denxhinjo.fabinventory.ui.common.AppCard
import com.denxhinjo.fabinventory.ui.common.FullScreenError
import com.denxhinjo.fabinventory.ui.common.FullScreenLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    onProductClick: (Int) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductsViewModel = hiltViewModel(),
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
        topBar = { CenterAlignedTopAppBar(title = { Text("Products") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add product")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text("Search products") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            when {
                uiState.isLoading && uiState.products.isEmpty() -> {
                    FullScreenLoading()
                }
                uiState.error != null && uiState.products.isEmpty() -> {
                    FullScreenError(message = uiState.error ?: "Something went wrong", onRetry = viewModel::refresh)
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.products, key = { it.id }) { product ->
                            ProductRow(product = product, onClick = { onProductClick(product.id) })
                        }
                        if (uiState.isLoadingMore) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductRow(product: ProductResponse, onClick: () -> Unit) {
    AppCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                val imageUrl = resolveMediaUrl(product.imageUrl)
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        Icons.Filled.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    buildString {
                        product.sku?.let { append("SKU $it · ") }
                        append("${product.quantity.trimmedString()} ${product.unit}")
                        product.location?.name?.let { append(" · $it") }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (product.isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun Double.trimmedString(): String =
    if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()
