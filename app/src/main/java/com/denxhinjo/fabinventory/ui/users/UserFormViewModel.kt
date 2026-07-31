package com.denxhinjo.fabinventory.ui.users

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denxhinjo.fabinventory.data.repository.UserCreateInput
import com.denxhinjo.fabinventory.data.repository.UserRepository
import com.denxhinjo.fabinventory.data.repository.UserUpdateInput
import com.denxhinjo.fabinventory.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserFormUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val email: String = "",
    val username: String = "",
    val fullName: String = "",
    val role: String = "user",
    val phone: String = "",
    val password: String = "",
    val isActive: Boolean = true,
    val error: String? = null,
    val saved: Boolean = false,
)

@HiltViewModel
class UserFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val editUserId: Int? = savedStateHandle.get<String>(Routes.ARG_EDIT_USER_ID)?.toIntOrNull()

    private val _uiState = MutableStateFlow(UserFormUiState(isEditMode = editUserId != null))
    val uiState: StateFlow<UserFormUiState> = _uiState.asStateFlow()

    init {
        val id = editUserId
        if (id != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                userRepository.getUsers().fold(
                    onSuccess = { users ->
                        val user = users.find { it.id == id }
                        if (user != null) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    email = user.email,
                                    username = user.username,
                                    fullName = user.fullName,
                                    role = user.role,
                                    phone = user.phone.orEmpty(),
                                    isActive = user.isActive,
                                )
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false, error = "User not found") }
                        }
                    },
                    onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load user") } },
                )
            }
        }
    }

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onUsernameChange(value: String) = _uiState.update { it.copy(username = value, error = null) }
    fun onFullNameChange(value: String) = _uiState.update { it.copy(fullName = value, error = null) }
    fun onRoleChange(value: String) = _uiState.update { it.copy(role = value) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun onActiveChange(value: Boolean) = _uiState.update { it.copy(isActive = value) }

    fun submit() {
        val state = _uiState.value
        if (state.fullName.isBlank() || state.email.isBlank()) {
            _uiState.update { it.copy(error = "Full name and email are required") }
            return
        }
        if (!state.isEditMode && (state.username.isBlank() || state.password.length < 8)) {
            _uiState.update { it.copy(error = "Username is required and password must be at least 8 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val id = editUserId
            val result = if (id != null) {
                userRepository.updateUser(
                    id,
                    UserUpdateInput(
                        email = state.email.trim(),
                        fullName = state.fullName.trim(),
                        role = state.role,
                        phone = state.phone.trim().takeIf { it.isNotBlank() },
                        isActive = state.isActive,
                        password = state.password.takeIf { it.isNotBlank() },
                    ),
                )
            } else {
                userRepository.createUser(
                    UserCreateInput(
                        email = state.email.trim(),
                        username = state.username.trim(),
                        fullName = state.fullName.trim(),
                        role = state.role,
                        phone = state.phone.trim().takeIf { it.isNotBlank() },
                        password = state.password,
                    ),
                )
            }
            result.fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
                onFailure = { e -> _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save user") } },
            )
        }
    }
}
