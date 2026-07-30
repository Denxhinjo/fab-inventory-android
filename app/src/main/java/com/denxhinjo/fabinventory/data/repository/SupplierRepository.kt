package com.denxhinjo.fabinventory.data.repository

import com.denxhinjo.fabinventory.data.remote.ApiService
import com.denxhinjo.fabinventory.data.remote.dto.SupplierRequest
import com.denxhinjo.fabinventory.data.remote.dto.SupplierResponse
import com.denxhinjo.fabinventory.data.remote.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

data class SupplierFormInput(
    val name: String,
    val contactName: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val city: String?,
    val notes: String?,
    val isActive: Boolean? = null,
)

private fun SupplierFormInput.toRequest() = SupplierRequest(
    name = name,
    contactName = contactName,
    email = email,
    phone = phone,
    address = address,
    city = city,
    notes = notes,
    isActive = isActive,
)

@Singleton
class SupplierRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getAllSuppliers(): Result<List<SupplierResponse>> =
        safeApiCall { apiService.getSuppliersFull() }

    suspend fun createSupplier(input: SupplierFormInput): Result<SupplierResponse> =
        safeApiCall { apiService.createSupplier(input.toRequest()) }

    suspend fun updateSupplier(id: Int, input: SupplierFormInput): Result<SupplierResponse> =
        safeApiCall { apiService.updateSupplier(id, input.toRequest()) }

    suspend fun deleteSupplier(id: Int): Result<Unit> = safeApiCall {
        val response = apiService.deleteSupplier(id)
        if (!response.isSuccessful) {
            error("Failed to delete supplier (${response.code()})")
        }
    }
}
