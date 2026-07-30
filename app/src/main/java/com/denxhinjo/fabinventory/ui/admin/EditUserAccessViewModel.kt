package com.denxhinjo.fabinventory.ui.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denxhinjo.fabinventory.data.remote.dto.LocationResponse
import com.denxhinjo.fabinventory.data.repository.LocationRepository
import com.denxhinjo.fabinventory.data.repository.UserRepository
import com.denxhinjo.fabinventory.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditUserAccessUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val userName: String = "",
    val allLocations: List<LocationResponse> = emptyList(),
    val selectedLocationIds: Set<Int> = emptySet(),
    val error: String? = null,
    val saved: Boolean = false,
)

@HiltViewModel
class EditUserAccessViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val userId: Int = checkNotNull(savedStateHandle[Routes.ARG_USER_ID])

    private val _uiState = MutableStateFlow(EditUserAccessUiState())
    val uiState: StateFlow<EditUserAccessUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val name = userRepository.getUsers().getOrNull()?.find { it.id == userId }?.fullName.orEmpty()
            val allLocationsResult = locationRepository.getAllLocations()
            val permittedResult = userRepository.getUserPermittedLocations(userId)

            allLocationsResult.fold(
                onSuccess = { allLocations ->
                    permittedResult.fold(
                        onSuccess = { permitted ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    userName = name,
                                    allLocations = allLocations,
                                    selectedLocationIds = permitted.map { loc -> loc.id }.toSet(),
                                )
                            }
                        },
                        onFailure = { e ->
                            _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load this user's access") }
                        },
                    )
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load locations") }
                },
            )
        }
    }

    fun toggleLocation(locationId: Int) {
        _uiState.update {
            val current = it.selectedLocationIds
            it.copy(selectedLocationIds = if (locationId in current) current - locationId else current + locationId)
        }
    }

    fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            userRepository.setUserPermittedLocations(userId, _uiState.value.selectedLocationIds.toList()).fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
                onFailure = { e -> _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save access") } },
            )
        }
    }
}
