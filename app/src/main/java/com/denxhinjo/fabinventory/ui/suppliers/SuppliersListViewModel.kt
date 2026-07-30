package com.denxhinjo.fabinventory.ui.suppliers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denxhinjo.fabinventory.data.remote.dto.SupplierResponse
import com.denxhinjo.fabinventory.data.repository.SupplierRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SuppliersListUiState(
    val isLoading: Boolean = true,
    val suppliers: List<SupplierResponse> = emptyList(),
    val error: String? = null,
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
)

@HiltViewModel
class SuppliersListViewModel @Inject constructor(
    private val supplierRepository: SupplierRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SuppliersListUiState())
    val uiState: StateFlow<SuppliersListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch { fetchSuppliers() }
    }

    private suspend fun fetchSuppliers() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        supplierRepository.getAllSuppliers().fold(
            onSuccess = { suppliers -> _uiState.update { it.copy(isLoading = false, suppliers = suppliers) } },
            onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load suppliers") } },
        )
    }

    fun deleteSupplier(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteError = null) }
            supplierRepository.deleteSupplier(id).fold(
                onSuccess = { fetchSuppliers() },
                onFailure = { e -> _uiState.update { it.copy(deleteError = e.message ?: "Failed to delete supplier") } },
            )
            _uiState.update { it.copy(isDeleting = false) }
        }
    }
}
