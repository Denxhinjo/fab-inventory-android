package com.denxhinjo.fabinventory.ui.movements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denxhinjo.fabinventory.R
import com.denxhinjo.fabinventory.data.remote.dto.MovementType
import com.denxhinjo.fabinventory.data.remote.dto.ProductResponse
import com.denxhinjo.fabinventory.ui.common.AppCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMovementScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateMovementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.submitted) {
        if (uiState.submitted) onDone()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_movement_title)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_cancel))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            val selected = uiState.selectedProduct
            if (selected == null) {
                ProductPicker(
                    query = uiState.productQuery,
                    onQueryChange = viewModel::onProductQueryChange,
                    results = uiState.productResults,
                    isSearching = uiState.isSearchingProducts,
                    onSelect = viewModel::selectProduct,
                )
            } else {
                SelectedProductCard(product = selected, onClear = viewModel::clearSelectedProduct)

                MovementTypeDropdown(
                    selected = uiState.movementType,
                    onSelect = viewModel::onMovementTypeChange,
                )

                OutlinedTextField(
                    value = uiState.quantity,
                    onValueChange = viewModel::onQuantityChange,
                    label = { Text(stringResource(R.string.create_movement_qty_label, selected.unit)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                )

                OutlinedTextField(
                    value = uiState.reason,
                    onValueChange = viewModel::onReasonChange,
                    label = { Text(stringResource(R.string.create_movement_reason_label)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                )

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = { Text(stringResource(R.string.common_notes_optional)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                )

                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }

                Button(
                    onClick = viewModel::submit,
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                    } else {
                        Text(stringResource(R.string.create_movement_save))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductPicker(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<ProductResponse>,
    isSearching: Boolean,
    onSelect: (ProductResponse) -> Unit,
) {
    Text(stringResource(R.string.create_movement_select_product), style = MaterialTheme.typography.titleMedium)
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(stringResource(R.string.products_search_label)) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
    )
    if (isSearching) {
        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items(results, key = { it.id }) { product ->
            AppCard(
                onClick = { onSelect(product) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(product.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.create_movement_in_stock, product.quantity.trimmed(), product.unit),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedProductCard(product: ProductResponse, onClear: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.create_movement_currently_in_stock, product.quantity.trimmed(), product.unit),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            IconButton(onClick = onClear) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.create_movement_change_product_cd))
            }
        }
    }
}

@Composable
private fun MovementTypeDropdown(selected: String, onSelect: (String) -> Unit) {
    Text(
        text = stringResource(R.string.create_movement_type_label),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
    Row {
        MovementType.ALL.forEach { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelect(type) },
                label = { Text(movementTypeLabel(type)) },
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}

private fun Double.trimmed(): String =
    if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()
