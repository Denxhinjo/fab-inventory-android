package com.denxhinjo.fabinventory.data.repository

import com.denxhinjo.fabinventory.data.remote.ApiService
import com.denxhinjo.fabinventory.data.remote.dto.LocationIdsRequest
import com.denxhinjo.fabinventory.data.remote.dto.LocationResponse
import com.denxhinjo.fabinventory.data.remote.dto.UserResponse
import com.denxhinjo.fabinventory.data.remote.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getUsers(): Result<List<UserResponse>> = safeApiCall { apiService.getUsers() }

    suspend fun getUserPermittedLocations(userId: Int): Result<List<LocationResponse>> =
        safeApiCall { apiService.getUserPermittedLocations(userId) }

    suspend fun setUserPermittedLocations(userId: Int, locationIds: List<Int>): Result<List<LocationResponse>> =
        safeApiCall { apiService.setUserPermittedLocations(userId, LocationIdsRequest(locationIds)) }
}
