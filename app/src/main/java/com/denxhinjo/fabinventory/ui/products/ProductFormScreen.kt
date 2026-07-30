package com.denxhinjo.fabinventory.ui.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.denxhinjo.fabinventory.data.remote.dto.LocationResponse
import com.denxhinjo.fabinventory.data.remote.resolveMediaUrl
import com.denxhinjo.fabinventory.ui.common.FullScreenLoading
import com.denxhinjo.fabinventory.ui.common.createImageCaptureUri

private val PRODUCT_STATUSES = listOf("active", "inactive", "discontinued")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductFormScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingCameraUri?.let(viewModel::onImagePicked)
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::onImagePicked)
    }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onDone()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit product" else "New product") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            FullScreenLoading(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            if (!uiState.isAdmin && uiState.availableLocations.isEmpty()) {
                Text(
                    "You haven't been granted access to any warehouse yet. Ask an admin to grant you access before adding products.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                return@Scaffold
            }

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = uiState.sku,
                onValueChange = viewModel::onSkuChange,
                label = { Text("SKU (optional)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )

            Text("Photo", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            ProductImagePicker(
                localUri = uiState.localImageUri,
                remoteUrl = uiState.imageUrl,
                isUploading = uiState.isUploadingImage,
                onCameraClick = {
                    val uri = createImageCaptureUri(context)
                    pendingCameraUri = uri
                    cameraLauncher.launch(uri)
                },
                onGalleryClick = {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            )
            uiState.imageError?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Text("Location", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            LocationPicker(
                locations = uiState.availableLocations,
                selected = uiState.selectedLocation,
                onSelect = viewModel::onLocationSelected,
            )

            Row2(
                left = {
                    OutlinedTextField(
                        value = uiState.quantity,
                        onValueChange = viewModel::onQuantityChange,
                        label = { Text("Quantity") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                right = {
                    OutlinedTextField(
                        value = uiState.unit,
                        onValueChange = viewModel::onUnitChange,
                        label = { Text("Unit") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            )

            Row2(
                left = {
                    OutlinedTextField(
                        value = uiState.minStockLevel,
                        onValueChange = viewModel::onMinStockLevelChange,
                        label = { Text("Min stock level") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                right = {
                    OutlinedTextField(
                        value = uiState.unitPrice,
                        onValueChange = viewModel::onUnitPriceChange,
                        label = { Text("Unit price (optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            )

            Text("Status", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PRODUCT_STATUSES.forEach { status ->
                    FilterChip(
                        selected = uiState.status == status,
                        onClick = { viewModel.onStatusChange(status) },
                        label = { Text(status.replaceFirstChar { it.uppercase() }) },
                    )
                }
            }

            if (uiState.availableCategories.isNotEmpty()) {
                Text("Category (optional)", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { viewModel.onCategorySelected(null) },
                        label = { Text("None") },
                    )
                    uiState.availableCategories.forEach { category ->
                        FilterChip(
                            selected = uiState.selectedCategory?.id == category.id,
                            onClick = { viewModel.onCategorySelected(category) },
                            label = { Text(category.name) },
                        )
                    }
                }
            }

            if (uiState.availableSuppliers.isNotEmpty()) {
                Text("Supplier (optional)", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.selectedSupplier == null,
                        onClick = { viewModel.onSupplierSelected(null) },
                        label = { Text("None") },
                    )
                    uiState.availableSuppliers.forEach { supplier ->
                        FilterChip(
                            selected = uiState.selectedSupplier?.id == supplier.id,
                            onClick = { viewModel.onSupplierSelected(supplier) },
                            label = { Text(supplier.name) },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notes (optional)") },
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
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                } else {
                    Text(if (uiState.isEditMode) "Save changes" else "Create product")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LocationPicker(
    locations: List<LocationResponse>,
    selected: LocationResponse?,
    onSelect: (LocationResponse) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        locations.forEach { location ->
            FilterChip(
                selected = selected?.id == location.id,
                onClick = { onSelect(location) },
                label = { Text(location.name) },
            )
        }
    }
}

@Composable
private fun Row2(left: @Composable () -> Unit, right: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) { left() }
        Column(modifier = Modifier.weight(1f)) { right() }
    }
}

@Composable
private fun ProductImagePicker(
    localUri: Uri?,
    remoteUrl: String?,
    isUploading: Boolean,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
) {
    Column {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            val model = localUri ?: resolveMediaUrl(remoteUrl)
            if (model != null) {
                AsyncImage(
                    model = model,
                    contentDescription = "Product photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    Icons.Filled.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isUploading) {
                CircularProgressIndicator()
            }
        }
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onCameraClick) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(" Camera")
            }
            OutlinedButton(onClick = onGalleryClick) {
                Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(" Gallery")
            }
        }
    }
}
