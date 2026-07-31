package com.denxhinjo.fabinventory.ui.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denxhinjo.fabinventory.data.remote.dto.LocationResponse
import com.denxhinjo.fabinventory.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocationsListUiState(
    val isLoading: Boolean = true,
    val locations: List<LocationResponse> = emptyList(),
    val error: String? = null,
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
)

@HiltViewModel
class LocationsListViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationsListUiState())
    val uiState: StateFlow<LocationsListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch { fetchLocations() }
    }

    private suspend fun fetchLocations() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        locationRepository.getAllLocations().fold(
            onSuccess = { locations -> _uiState.update { it.copy(isLoading = false, locations = locations) } },
            onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load warehouses") } },
        )
    }

    fun deleteLocation(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteError = null) }
            locationRepository.deleteLocation(id).fold(
                onSuccess = { fetchLocations() },
                onFailure = { e -> _uiState.update { it.copy(deleteError = e.message ?: "Failed to delete warehouse") } },
            )
            _uiState.update { it.copy(isDeleting = false) }
        }
    }

    fun dismissDeleteError() = _uiState.update { it.copy(deleteError = null) }
}
