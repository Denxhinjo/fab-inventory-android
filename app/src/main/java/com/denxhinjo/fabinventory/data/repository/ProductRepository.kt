package com.denxhinjo.fabinventory.data.repository

import com.denxhinjo.fabinventory.data.remote.ApiService
import com.denxhinjo.fabinventory.data.remote.dto.ProductListResponse
import com.denxhinjo.fabinventory.data.remote.dto.ProductResponse
import com.denxhinjo.fabinventory.data.remote.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getProducts(
        page: Int,
        pageSize: Int = 20,
        search: String? = null,
    ): Result<ProductListResponse> = safeApiCall {
        apiService.getProducts(page = page, pageSize = pageSize, search = search?.takeIf { it.isNotBlank() })
    }

    suspend fun getProduct(id: Int): Result<ProductResponse> =
        safeApiCall { apiService.getProduct(id) }
}
