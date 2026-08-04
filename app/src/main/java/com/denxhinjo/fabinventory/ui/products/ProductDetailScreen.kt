package com.denxhinjo.fabinventory.ui.products

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.denxhinjo.fabinventory.R
import com.denxhinjo.fabinventory.data.remote.dto.ProductResponse
import com.denxhinjo.fabinventory.data.remote.resolveMediaUrl
import com.denxhinjo.fabinventory.ui.common.AppCard
import com.denxhinjo.fabinventory.ui.common.FullScreenError
import com.denxhinjo.fabinventory.ui.common.FullScreenLoading
import com.denxhinjo.fabinventory.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onRecordMovement: (Int) -> Unit,
    onEdit: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val isDeleting by viewModel.isDeleting.collectAsStateWithLifecycle()
    val deleted by viewModel.deleted.collectAsStateWithLifecycle()
    val deleteError by viewModel.deleteError.collectAsStateWithLifecycle()

    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(deleted) {
        if (deleted) onBack()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.product_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    val state = uiState
                    if (state is UiState.Success) {
                        IconButton(onClick = { onEdit(state.data.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.product_detail_edit_cd))
                        }
                        if (isAdmin) {
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.product_detail_delete_cd))
                            }
                        }
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
            is UiState.Success -> Column {
                deleteError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(padding).padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                ProductDetailContent(
                    product = state.data,
                    onRecordMovement = { onRecordMovement(state.data.id) },
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.product_detail_delete_title)) },
            text = { Text(stringResource(R.string.common_cannot_be_undone)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteProduct()
                    },
                    enabled = !isDeleting,
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.padding(2.dp))
                    } else {
                        Text(stringResource(R.string.common_delete))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.common_cancel)) }
            },
        )
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
        resolveMediaUrl(product.imageUrl)?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = stringResource(R.string.product_detail_photo_cd, product.name),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(12.dp))
        }

        Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        product.sku?.let {
            Text(stringResource(R.string.product_detail_sku, it), style = MaterialTheme.typography.bodyMedium)
        }

        AppCard(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow(stringResource(R.string.product_detail_quantity_label), "${product.quantity.trimmed()} ${product.unit}")
                DetailRow(
                    stringResource(R.string.product_detail_min_stock_label),
                    "${product.minStockLevel.trimmed()} ${product.unit}",
                )
                product.unitPrice?.let { DetailRow(stringResource(R.string.product_detail_unit_price_label), it.trimmed()) }
                DetailRow(stringResource(R.string.product_detail_status_label), productStatusLabel(product.status))
                product.category?.let { DetailRow(stringResource(R.string.product_detail_category_label), it.name) }
                product.location?.let { DetailRow(stringResource(R.string.product_detail_warehouse_label), it.name) }
                product.supplier?.let { DetailRow(stringResource(R.string.product_detail_supplier_label), it.name) }
            }
        }

        product.description?.takeIf { it.isNotBlank() }?.let { description ->
            Text(
                text = stringResource(R.string.product_detail_description_label),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp, bottom = 4.dp),
            )
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }

        product.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            Text(
                text = stringResource(R.string.product_detail_notes_label),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
            Text(notes, style = MaterialTheme.typography.bodyMedium)
        }

        if (product.isLowStock) {
            Text(
                text = stringResource(R.string.product_detail_low_stock_warning),
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
            Text(stringResource(R.string.product_detail_record_movement))
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
