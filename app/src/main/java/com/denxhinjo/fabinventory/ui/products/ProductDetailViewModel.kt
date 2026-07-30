package com.denxhinjo.fabinventory.ui.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denxhinjo.fabinventory.data.remote.dto.ProductResponse
import com.denxhinjo.fabinventory.data.repository.AuthRepository
import com.denxhinjo.fabinventory.data.repository.ProductRepository
import com.denxhinjo.fabinventory.ui.common.UiState
import com.denxhinjo.fabinventory.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    private val productId: Int = checkNotNull(savedStateHandle[Routes.ARG_PRODUCT_ID])

    private val _uiState = MutableStateFlow<UiState<ProductResponse>>(UiState.Loading)
    val uiState: StateFlow<UiState<ProductResponse>> = _uiState.asStateFlow()

    val isAdmin: StateFlow<Boolean> = authRepository.sessionFlow
        .map { it?.role == "admin" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = productRepository.getProduct(productId)
            _uiState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load product") },
            )
        }
    }

    fun deleteProduct() {
        viewModelScope.launch {
            _isDeleting.update { true }
            _deleteError.update { null }
            productRepository.deleteProduct(productId).fold(
                onSuccess = { _deleted.update { true } },
                onFailure = { e -> _deleteError.update { e.message ?: "Failed to delete product" } },
            )
            _isDeleting.update { false }
        }
    }
}
