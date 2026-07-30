package com.denxhinjo.fabinventory.ui.suppliers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denxhinjo.fabinventory.data.repository.SupplierFormInput
import com.denxhinjo.fabinventory.data.repository.SupplierRepository
import com.denxhinjo.fabinventory.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SupplierFormUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val contactName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val city: String = "",
    val notes: String = "",
    val error: String? = null,
    val saved: Boolean = false,
)

@HiltViewModel
class SupplierFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val supplierRepository: SupplierRepository,
) : ViewModel() {

    private val editSupplierId: Int? = savedStateHandle.get<String>(Routes.ARG_EDIT_SUPPLIER_ID)?.toIntOrNull()

    private val _uiState = MutableStateFlow(SupplierFormUiState(isEditMode = editSupplierId != null))
    val uiState: StateFlow<SupplierFormUiState> = _uiState.asStateFlow()

    init {
        val id = editSupplierId
        if (id != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                supplierRepository.getAllSuppliers().fold(
                    onSuccess = { suppliers ->
                        val supplier = suppliers.find { it.id == id }
                        if (supplier != null) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    name = supplier.name,
                                    contactName = supplier.contactName.orEmpty(),
                                    email = supplier.email.orEmpty(),
                                    phone = supplier.phone.orEmpty(),
                                    address = supplier.address.orEmpty(),
                                    city = supplier.city.orEmpty(),
                                    notes = supplier.notes.orEmpty(),
                                )
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false, error = "Supplier not found") }
                        }
                    },
                    onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load supplier") } },
                )
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun onContactNameChange(value: String) = _uiState.update { it.copy(contactName = value) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value) }
    fun onAddressChange(value: String) = _uiState.update { it.copy(address = value) }
    fun onCityChange(value: String) = _uiState.update { it.copy(city = value) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }

    fun submit() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }

        val input = SupplierFormInput(
            name = state.name.trim(),
            contactName = state.contactName.trim().takeIf { it.isNotBlank() },
            email = state.email.trim().takeIf { it.isNotBlank() },
            phone = state.phone.trim().takeIf { it.isNotBlank() },
            address = state.address.trim().takeIf { it.isNotBlank() },
            city = state.city.trim().takeIf { it.isNotBlank() },
            notes = state.notes.trim().takeIf { it.isNotBlank() },
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val id = editSupplierId
            val result = if (id != null) {
                supplierRepository.updateSupplier(id, input)
            } else {
                supplierRepository.createSupplier(input)
            }
            result.fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
                onFailure = { e -> _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save supplier") } },
            )
        }
    }
}
