package com.denxhinjo.fabinventory.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denxhinjo.fabinventory.data.remote.dto.UserResponse
import com.denxhinjo.fabinventory.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageAccessUiState(
    val isLoading: Boolean = true,
    val users: List<UserResponse> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class ManageAccessViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageAccessUiState())
    val uiState: StateFlow<ManageAccessUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            userRepository.getUsers().fold(
                onSuccess = { users ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            // Assigning warehouse access to admins is meaningless -- they're
                            // already unrestricted -- so only non-admin users are manageable here.
                            users = users.filter { u -> u.role != "admin" },
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load users") }
                },
            )
        }
    }
}
