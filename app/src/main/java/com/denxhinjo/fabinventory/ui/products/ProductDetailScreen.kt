package com.denxhinjo.fabinventory.ui.products

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denxhinjo.fabinventory.data.remote.dto.ProductResponse
import com.denxhinjo.fabinventory.ui.common.AppCard
import com.denxhinjo.fabinventory.ui.common.FullScreenError
import com.denxhinjo.fabinventory.ui.common.FullScreenLoading
import com.denxhinjo.fabinventory.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onRecordMovement: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Product") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
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
            is UiState.Success -> ProductDetailContent(
                product = state.data,
                onRecordMovement = { onRecordMovement(state.data.id) },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun ProductDetailContent(
    product: ProductResponse,
    onRecordMovement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        product.sku?.let {
            Text("SKU $it", style = MaterialTheme.typography.bodyMedium)
        }

        AppCard(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow("Quantity", "${product.quantity.trimmed()} ${product.unit}")
                DetailRow("Minimum stock level", "${product.minStockLevel.trimmed()} ${product.unit}")
                product.unitPrice?.let { DetailRow("Unit price", it.trimmed()) }
                DetailRow("Status", product.status.replaceFirstChar { it.uppercase() })
                product.category?.let { DetailRow("Category", it.name) }
                product.location?.let { DetailRow("Location", it.name) }
                product.supplier?.let { DetailRow("Supplier", it.name) }
            }
        }

        product.description?.takeIf { it.isNotBlank() }?.let { description ->
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp, bottom = 4.dp),
            )
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }

        product.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
            Text(notes, style = MaterialTheme.typography.bodyMedium)
        }

        if (product.isLowStock) {
            Text(
                text = "This item is at or below its minimum stock level.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        Button(
            onClick = onRecordMovement,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        ) {
            Text("Record stock movement")
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
    HorizontalDivider(modifier = Modifier.padding(top = 6.dp))
}

private fun Double.trimmed(): String =
    if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()
