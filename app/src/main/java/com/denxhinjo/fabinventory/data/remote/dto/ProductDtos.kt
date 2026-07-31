package com.denxhinjo.fabinventory.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors backend/app/schemas/product.py.
 */
@Serializable
data class CategorySummary(
    val id: Int,
    val name: String,
    val color: String? = null,
)

@Serializable
data class LocationSummary(
    val id: Int,
    val name: String,
    val city: String? = null,
)

@Serializable
data class SupplierSummary(
    val id: Int,
    val name: String,
)

/** Mirrors backend/app/schemas/category.py::CategoryOut -- includes the product_count the plain CategorySummary omits. */
@Serializable
data class CategoryResponse(
    val id: Int,
    val name: String,
    val color: String? = null,
    @SerialName("product_count") val productCount: Int = 0,
)

@Serializable
data class ProductResponse(
    val id: Int,
    val name: String,
    val sku: String? = null,
    @SerialName("category_id") val categoryId: Int? = null,
    val description: String? = null,
    val quantity: Double,
    val unit: String,
    @SerialName("min_stock_level") val minStockLevel: Double,
    @SerialName("unit_price") val unitPrice: Double? = null,
    @SerialName("location_id") val locationId: Int? = null,
    @SerialName("supplier_id") val supplierId: Int? = null,
    val status: String,
    val notes: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("is_low_stock") val isLowStock: Boolean = false,
    val category: CategorySummary? = null,
    val location: LocationSummary? = null,
    val supplier: SupplierSummary? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class ProductListResponse(
    val items: List<ProductResponse>,
    val total: Int,
    val page: Int,
    @SerialName("page_size") val pageSize: Int,
    @SerialName("total_pages") val totalPages: Int,
)
