package com.denxhinjo.fabinventory.ui.products

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denxhinjo.fabinventory.data.remote.CloudinaryUploader
import com.denxhinjo.fabinventory.data.remote.dto.CategorySummary
import com.denxhinjo.fabinventory.data.remote.dto.LocationResponse
import com.denxhinjo.fabinventory.data.remote.dto.SupplierSummary
import com.denxhinjo.fabinventory.data.repository.AuthRepository
import com.denxhinjo.fabinventory.data.repository.LocationRepository
import com.denxhinjo.fabinventory.data.repository.ProductFormInput
import com.denxhinjo.fabinventory.data.repository.ProductRepository
import com.denxhinjo.fabinventory.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductFormUiState(
    val isEditMode: Boolean = false,
    val isAdmin: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val name: String = "",
    val sku: String = "",
    val description: String = "",
    val quantity: String = "",
    val unit: String = "pcs",
    val minStockLevel: String = "",
    val unitPrice: String = "",
    val notes: String = "",
    val status: String = "active",
    val availableLocations: List<LocationResponse> = emptyList(),
    val selectedLocation: LocationResponse? = null,
    val availableCategories: List<CategorySummary> = emptyList(),
    val selectedCategory: CategorySummary? = null,
    val availableSuppliers: List<SupplierSummary> = emptyList(),
    val selectedSupplier: SupplierSummary? = null,
    // Existing image URL (edit mode) or the Cloudinary URL from a freshly uploaded photo.
    val imageUrl: String? = null,
    // The just-picked local image, shown immediately while the upload is in flight.
    val localImageUri: Uri? = null,
    val isUploadingImage: Boolean = false,
    val imageError: String? = null,
    val error: String? = null,
    val saved: Boolean = false,
)

private fun Double.trimmedString(): String =
    if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()

@HiltViewModel
class ProductFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val locationRepository: LocationRepository,
    private val authRepository: AuthRepository,
    private val cloudinaryUploader: CloudinaryUploader,
) : ViewModel() {

    private val editProductId: Int? = savedStateHandle.get<String>(Routes.ARG_EDIT_PRODUCT_ID)?.toIntOrNull()

    private val _uiState = MutableStateFlow(ProductFormUiState(isEditMode = editProductId != null))
    val uiState: StateFlow<ProductFormUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val session = authRepository.sessionFlow.first()
            val isAdmin = session?.role == "admin"

            val locations = (if (isAdmin) locationRepository.getAllLocations() else locationRepository.getMyPermittedLocations())
                .getOrElse { emptyList() }
            val categories = productRepository.getCategories().getOrElse { emptyList() }
            val suppliers = productRepository.getSuppliers().getOrElse { emptyList() }

            var next = _uiState.value.copy(
                isAdmin = isAdmin,
                availableLocations = locations,
                availableCategories = categories,
                availableSuppliers = suppliers,
                selectedLocation = if (locations.size == 1) locations.first() else null,
            )

            val id = editProductId
            if (id != null) {
                productRepository.getProduct(id).fold(
                    onSuccess = { product ->
                        next = next.copy(
                            name = product.name,
                            sku = product.sku.orEmpty(),
                            description = product.description.orEmpty(),
                            quantity = product.quantity.trimmedString(),
                            unit = product.unit,
                            minStockLevel = product.minStockLevel.trimmedString(),
                            unitPrice = product.unitPrice?.trimmedString().orEmpty(),
                            notes = product.notes.orEmpty(),
                            status = product.status,
                            selectedLocation = locations.find { it.id == product.locationId } ?: next.selectedLocation,
                            selectedCategory = categories.find { it.id == product.categoryId },
                            selectedSupplier = suppliers.find { it.id == product.supplierId },
                            imageUrl = product.imageUrl,
                        )
                    },
                    onFailure = { e -> next = next.copy(error = e.message ?: "Failed to load product") },
                )
            }

            _uiState.value = next.copy(isLoading = false)
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun onSkuChange(value: String) = _uiState.update { it.copy(sku = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onQuantityChange(value: String) = _uiState.update { it.copy(quantity = value) }
    fun onUnitChange(value: String) = _uiState.update { it.copy(unit = value) }
    fun onMinStockLevelChange(value: String) = _uiState.update { it.copy(minStockLevel = value) }
    fun onUnitPriceChange(value: String) = _uiState.update { it.copy(unitPrice = value) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }
    fun onStatusChange(value: String) = _uiState.update { it.copy(status = value) }
    fun onLocationSelected(location: LocationResponse) = _uiState.update { it.copy(selectedLocation = location, error = null) }
    fun onCategorySelected(category: CategorySummary?) = _uiState.update { it.copy(selectedCategory = category) }
    fun onSupplierSelected(supplier: SupplierSummary?) = _uiState.update { it.copy(selectedSupplier = supplier) }

    fun onImagePicked(uri: Uri) {
        _uiState.update { it.copy(localImageUri = uri, imageError = null, isUploadingImage = true) }
        viewModelScope.launch {
            cloudinaryUploader.uploadImage(uri).fold(
                onSuccess = { url -> _uiState.update { it.copy(isUploadingImage = false, imageUrl = url) } },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isUploadingImage = false, imageError = e.message ?: "Couldn't upload image")
                    }
                },
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }
        if (state.selectedLocation == null) {
            _uiState.update {
                it.copy(error = if (it.availableLocations.isEmpty()) "You don't have access to any warehouse yet" else "Select a location")
            }
            return
        }
        if (state.isUploadingImage) {
            _uiState.update { it.copy(error = "Wait for the photo to finish uploading") }
            return
        }

        val input = ProductFormInput(
            name = state.name.trim(),
            sku = state.sku.trim().takeIf { it.isNotBlank() },
            categoryId = state.selectedCategory?.id,
            description = state.description.trim().takeIf { it.isNotBlank() },
            quantity = state.quantity.toDoubleOrNull(),
            unit = state.unit.trim().takeIf { it.isNotBlank() },
            minStockLevel = state.minStockLevel.toDoubleOrNull(),
            unitPrice = state.unitPrice.trim().takeIf { it.isNotBlank() }?.toDoubleOrNull(),
            locationId = state.selectedLocation.id,
            supplierId = state.selectedSupplier?.id,
            status = state.status,
            notes = state.notes.trim().takeIf { it.isNotBlank() },
            imageUrl = state.imageUrl,
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val id = editProductId
            val result = if (id != null) {
                productRepository.updateProduct(id, input)
            } else {
                productRepository.createProduct(input)
            }
            result.fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
                onFailure = { e -> _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save product") } },
            )
        }
    }
}
