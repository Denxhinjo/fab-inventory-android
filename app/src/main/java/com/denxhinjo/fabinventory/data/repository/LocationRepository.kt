package com.denxhinjo.fabinventory.data.repository

import com.denxhinjo.fabinventory.data.remote.ApiService
import com.denxhinjo.fabinventory.data.remote.dto.LocationRequest
import com.denxhinjo.fabinventory.data.remote.dto.LocationResponse
import com.denxhinjo.fabinventory.data.remote.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

data class LocationFormInput(
    val name: String,
    val address: String?,
    val city: String?,
    val managerName: String?,
    val contactEmail: String?,
    val contactPhone: String?,
    val notes: String?,
    val isActive: Boolean? = null,
)

private fun LocationFormInput.toRequest() = LocationRequest(
    name = name,
    address = address,
    city = city,
    managerName = managerName,
    contactEmail = contactEmail,
    contactPhone = contactPhone,
    notes = notes,
    isActive = isActive,
)

@Singleton
class LocationRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getAllLocations(): Result<List<LocationResponse>> =
        safeApiCall { apiService.getLocations() }

    /** The warehouses the current (possibly non-admin) user may create/edit products in. */
    suspend fun getMyPermittedLocations(): Result<List<LocationResponse>> =
        safeApiCall { apiService.getMyPermittedLocations() }

    suspend fun createLocation(input: LocationFormInput): Result<LocationResponse> =
        safeApiCall { apiService.createLocation(input.toRequest()) }

    suspend fun updateLocation(id: Int, input: LocationFormInput): Result<LocationResponse> =
        safeApiCall { apiService.updateLocation(id, input.toRequest()) }

    suspend fun deleteLocation(id: Int): Result<Unit> = safeApiCall {
        val response = apiService.deleteLocation(id)
        if (!response.isSuccessful) {
            error(
                if (response.code() == 400) {
                    "Can't delete a warehouse that still has products in it"
                } else {
                    "Failed to delete warehouse (${response.code()})"
                },
            )
        }
    }
}
