package com.denxhinjo.fabinventory.data.repository

import com.denxhinjo.fabinventory.data.remote.ApiService
import com.denxhinjo.fabinventory.data.remote.dto.LocationIdsRequest
import com.denxhinjo.fabinventory.data.remote.dto.LocationResponse
import com.denxhinjo.fabinventory.data.remote.dto.UserCreateRequest
import com.denxhinjo.fabinventory.data.remote.dto.UserResponse
import com.denxhinjo.fabinventory.data.remote.dto.UserUpdateRequest
import com.denxhinjo.fabinventory.data.remote.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

data class UserCreateInput(
    val email: String,
    val username: String,
    val fullName: String,
    val role: String,
    val phone: String?,
    val password: String,
)

data class UserUpdateInput(
    val email: String?,
    val fullName: String?,
    val role: String?,
    val phone: String?,
    val isActive: Boolean?,
    val password: String?,
)

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getUsers(): Result<List<UserResponse>> = safeApiCall { apiService.getUsers() }

    suspend fun createUser(input: UserCreateInput): Result<UserResponse> = safeApiCall {
        apiService.createUser(
            UserCreateRequest(
                email = input.email,
                username = input.username,
                fullName = input.fullName,
                role = input.role,
                phone = input.phone,
                password = input.password,
            ),
        )
    }

    suspend fun updateUser(id: Int, input: UserUpdateInput): Result<UserResponse> = safeApiCall {
        apiService.updateUser(
            id,
            UserUpdateRequest(
                email = input.email,
                fullName = input.fullName,
                role = input.role,
                phone = input.phone,
                isActive = input.isActive,
                password = input.password,
            ),
        )
    }

    suspend fun deleteUser(id: Int): Result<Unit> = safeApiCall {
        val response = apiService.deleteUser(id)
        if (!response.isSuccessful) {
            error(
                if (response.code() == 400) {
                    "You can't delete your own account"
                } else {
                    "Failed to delete user (${response.code()})"
                },
            )
        }
    }

    suspend fun getUserPermittedLocations(userId: Int): Result<List<LocationResponse>> =
        safeApiCall { apiService.getUserPermittedLocations(userId) }

    suspend fun setUserPermittedLocations(userId: Int, locationIds: List<Int>): Result<List<LocationResponse>> =
        safeApiCall { apiService.setUserPermittedLocations(userId, LocationIdsRequest(locationIds)) }
}
