package com.denxhinjo.fabinventory.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denxhinjo.fabinventory.data.remote.dto.ProductResponse
import com.denxhinjo.fabinventory.data.repository.AuthRepository
import com.denxhinjo.fabinventory.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductsUiState(
    val products: List<ProductResponse> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val searchQuery: String = "",
    val error: String? = null,
    val deleteError: String? = null,
)

private const val SEARCH_DEBOUNCE_MS = 400L

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    val isAdmin: StateFlow<Boolean> = authRepository.sessionFlow
        .map { it?.role == "admin" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private var searchJob: Job? = null

    init {
        loadProducts(reset = true)
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(deleteError = null) }
            productRepository.deleteProduct(id).fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(products = state.products.filterNot { it.id == id }) }
                },
                onFailure = { e -> _uiState.update { it.copy(deleteError = e.message ?: "Failed to delete product") } },
            )
        }
    }

    fun dismissDeleteError() = _uiState.update { it.copy(deleteError = null) }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            loadProducts(reset = true)
        }
    }

    fun refresh() = loadProducts(reset = true)

    fun loadMore() {
        if (_uiState.value.hasMore && !_uiState.value.isLoadingMore) {
            loadProducts(reset = false)
        }
    }

    private fun loadProducts(reset: Boolean) {
        val current = _uiState.value
        val page = if (reset) 1 else current.page + 1

        viewModelScope.launch {
            _uiState.update {
                if (reset) it.copy(isLoading = true, error = null) else it.copy(isLoadingMore = true)
            }
            val result = productRepository.getProducts(page = page, search = current.searchQuery)
            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            products = if (reset) response.items else it.products + response.items,
                            page = response.page,
                            hasMore = response.page < response.totalPages,
                            error = null,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, isLoadingMore = false, error = e.message ?: "Failed to load products")
                    }
                },
            )
        }
    }
}
