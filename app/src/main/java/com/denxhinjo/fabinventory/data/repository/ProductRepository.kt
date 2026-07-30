package com.denxhinjo.fabinventory.data.repository

import com.denxhinjo.fabinventory.data.remote.ApiService
import com.denxhinjo.fabinventory.data.remote.dto.CategorySummary
import com.denxhinjo.fabinventory.data.remote.dto.ProductListResponse
import com.denxhinjo.fabinventory.data.remote.dto.ProductResponse
import com.denxhinjo.fabinventory.data.remote.dto.SupplierSummary
import com.denxhinjo.fabinventory.data.remote.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

data class ProductFormInput(
    val name: String,
    val sku: String?,
    val categoryId: Int?,
    val description: String?,
    val quantity: Double?,
    val unit: String?,
    val minStockLevel: Double?,
    val unitPrice: Double?,
    val locationId: Int?,
    val supplierId: Int?,
    val status: String?,
    val notes: String?,
    val imageUrl: String? = null,
)

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

    suspend fun createProduct(input: ProductFormInput): Result<ProductResponse> = safeApiCall {
        apiService.createProduct(
            name = input.name,
            sku = input.sku,
            categoryId = input.categoryId,
            description = input.description,
            quantity = input.quantity,
            unit = input.unit,
            minStockLevel = input.minStockLevel,
            unitPrice = input.unitPrice,
            locationId = input.locationId,
            supplierId = input.supplierId,
            productStatus = input.status,
            notes = input.notes,
            imageUrl = input.imageUrl,
        )
    }

    suspend fun updateProduct(id: Int, input: ProductFormInput): Result<ProductResponse> = safeApiCall {
        apiService.updateProduct(
            id = id,
            name = input.name,
            sku = input.sku,
            categoryId = input.categoryId,
            description = input.description,
            quantity = input.quantity,
            unit = input.unit,
            minStockLevel = input.minStockLevel,
            unitPrice = input.unitPrice,
            locationId = input.locationId,
            supplierId = input.supplierId,
            productStatus = input.status,
            notes = input.notes,
            imageUrl = input.imageUrl,
        )
    }

    suspend fun deleteProduct(id: Int): Result<Unit> = safeApiCall {
        val response = apiService.deleteProduct(id)
        if (!response.isSuccessful) {
            error("Failed to delete product (${response.code()})")
        }
    }

    suspend fun getCategories(): Result<List<CategorySummary>> = safeApiCall { apiService.getCategories() }

    suspend fun getSuppliers(): Result<List<SupplierSummary>> = safeApiCall { apiService.getSuppliers() }
}
