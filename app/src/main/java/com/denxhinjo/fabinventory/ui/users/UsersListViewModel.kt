package com.denxhinjo.fabinventory.ui.users

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

data class UsersListUiState(
    val isLoading: Boolean = true,
    val users: List<UserResponse> = emptyList(),
    val error: String? = null,
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
)

@HiltViewModel
class UsersListViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsersListUiState())
    val uiState: StateFlow<UsersListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch { fetchUsers() }
    }

    private suspend fun fetchUsers() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        userRepository.getUsers().fold(
            onSuccess = { users -> _uiState.update { it.copy(isLoading = false, users = users) } },
            onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load users") } },
        )
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteError = null) }
            userRepository.deleteUser(id).fold(
                onSuccess = { fetchUsers() },
                onFailure = { e -> _uiState.update { it.copy(deleteError = e.message ?: "Failed to delete user") } },
            )
            _uiState.update { it.copy(isDeleting = false) }
        }
    }
}
