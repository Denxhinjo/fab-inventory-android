package com.denxhinjo.fabinventory.ui.movements

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denxhinjo.fabinventory.data.remote.dto.MovementType
import com.denxhinjo.fabinventory.data.remote.dto.ProductResponse
import com.denxhinjo.fabinventory.data.remote.dto.StockMovementCreateRequest
import com.denxhinjo.fabinventory.data.repository.ProductRepository
import com.denxhinjo.fabinventory.data.repository.StockMovementRepository
import com.denxhinjo.fabinventory.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class CreateMovementUiState(
    val selectedProduct: ProductResponse? = null,
    val productQuery: String = "",
    val productResults: List<ProductResponse> = emptyList(),
    val isSearchingProducts: Boolean = false,
    val movementType: String = MovementType.STOCK_IN,
    val quantity: String = "",
    val reason: String = "",
    val notes: String = "",
    val isSubmitting: Boolean = false,
    val submitted: Boolean = false,
    val error: String? = null,
)

private const val PRODUCT_SEARCH_DEBOUNCE_MS = 400L

@HiltViewModel
class CreateMovementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val stockMovementRepository: StockMovementRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val prefilledProductId: Int? = savedStateHandle.get<String>(Routes.ARG_PREFILLED_PRODUCT_ID)?.toIntOrNull()

    private val _uiState = MutableStateFlow(CreateMovementUiState())
    val uiState: StateFlow<CreateMovementUiState> = _uiState.asStateFlow()

    private var productSearchJob: Job? = null

    init {
        prefilledProductId?.let { id ->
            viewModelScope.launch {
                productRepository.getProduct(id).onSuccess { product ->
                    _uiState.update { it.copy(selectedProduct = product) }
                }
            }
        }
    }

    fun onProductQueryChange(query: String) {
        _uiState.update { it.copy(productQuery = query) }
        productSearchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(productResults = emptyList(), isSearchingProducts = false) }
            return
        }
        productSearchJob = viewModelScope.launch {
            delay(PRODUCT_SEARCH_DEBOUNCE_MS)
            _uiState.update { it.copy(isSearchingProducts = true) }
            productRepository.getProducts(page = 1, search = query).fold(
                onSuccess = { response ->
                    _uiState.update { it.copy(productResults = response.items, isSearchingProducts = false) }
                },
                onFailure = {
                    _uiState.update { it.copy(isSearchingProducts = false) }
                },
            )
        }
    }

    fun selectProduct(product: ProductResponse) {
        _uiState.update { it.copy(selectedProduct = product, productResults = emptyList(), productQuery = "") }
    }

    fun clearSelectedProduct() {
        _uiState.update { it.copy(selectedProduct = null) }
    }

    fun onMovementTypeChange(type: String) = _uiState.update { it.copy(movementType = type) }
    fun onQuantityChange(value: String) = _uiState.update { it.copy(quantity = value, error = null) }
    fun onReasonChange(value: String) = _uiState.update { it.copy(reason = value) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }

    fun submit() {
        val state = _uiState.value
        val product = state.selectedProduct
        val quantity = state.quantity.toDoubleOrNull()

        if (product == null) {
            _uiState.update { it.copy(error = "Select a product first") }
            return
        }
        if (quantity == null || quantity <= 0) {
            _uiState.update { it.copy(error = "Enter a quantity greater than 0") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val request = StockMovementCreateRequest(
                productId = product.id,
                movementType = state.movementType,
                quantity = quantity,
                reason = state.reason.takeIf { it.isNotBlank() },
                movementDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                notes = state.notes.takeIf { it.isNotBlank() },
                referenceNumber = null,
            )
            val result = stockMovementRepository.createMovement(request)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSubmitting = false, submitted = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message ?: "Failed to record movement") }
                },
            )
        }
    }
}
