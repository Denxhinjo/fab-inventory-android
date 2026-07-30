package com.denxhinjo.fabinventory.ui.locations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denxhinjo.fabinventory.data.repository.LocationFormInput
import com.denxhinjo.fabinventory.data.repository.LocationRepository
import com.denxhinjo.fabinventory.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocationFormUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val managerName: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val notes: String = "",
    val error: String? = null,
    val saved: Boolean = false,
)

@HiltViewModel
class LocationFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val editLocationId: Int? = savedStateHandle.get<String>(Routes.ARG_EDIT_LOCATION_ID)?.toIntOrNull()

    private val _uiState = MutableStateFlow(LocationFormUiState(isEditMode = editLocationId != null))
    val uiState: StateFlow<LocationFormUiState> = _uiState.asStateFlow()

    init {
        val id = editLocationId
        if (id != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                locationRepository.getAllLocations().fold(
                    onSuccess = { locations ->
                        val location = locations.find { it.id == id }
                        if (location != null) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    name = location.name,
                                    address = location.address.orEmpty(),
                                    city = location.city.orEmpty(),
                                    managerName = location.managerName.orEmpty(),
                                    contactEmail = location.contactEmail.orEmpty(),
                                    contactPhone = location.contactPhone.orEmpty(),
                                    notes = location.notes.orEmpty(),
                                )
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false, error = "Warehouse not found") }
                        }
                    },
                    onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load warehouse") } },
                )
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun onAddressChange(value: String) = _uiState.update { it.copy(address = value) }
    fun onCityChange(value: String) = _uiState.update { it.copy(city = value) }
    fun onManagerNameChange(value: String) = _uiState.update { it.copy(managerName = value) }
    fun onContactEmailChange(value: String) = _uiState.update { it.copy(contactEmail = value) }
    fun onContactPhoneChange(value: String) = _uiState.update { it.copy(contactPhone = value) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }

    fun submit() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }

        val input = LocationFormInput(
            name = state.name.trim(),
            address = state.address.trim().takeIf { it.isNotBlank() },
            city = state.city.trim().takeIf { it.isNotBlank() },
            managerName = state.managerName.trim().takeIf { it.isNotBlank() },
            contactEmail = state.contactEmail.trim().takeIf { it.isNotBlank() },
            contactPhone = state.contactPhone.trim().takeIf { it.isNotBlank() },
            notes = state.notes.trim().takeIf { it.isNotBlank() },
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val id = editLocationId
            val result = if (id != null) {
                locationRepository.updateLocation(id, input)
            } else {
                locationRepository.createLocation(input)
            }
            result.fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
                onFailure = { e -> _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save warehouse") } },
            )
        }
    }
}
