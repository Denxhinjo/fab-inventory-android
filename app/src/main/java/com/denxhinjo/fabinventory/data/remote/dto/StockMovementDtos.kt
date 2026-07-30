package com.denxhinjo.fabinventory.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors backend/app/schemas/stock_movement.py. movement_type must be one of
 * "Stock In", "Stock Out", "Adjustment" -- validated again on the server.
 */
@Serializable
data class ProductRef(
    val id: Int,
    val name: String,
    val sku: String? = null,
    val quantity: Double,
    val unit: String,
)

@Serializable
data class UserRef(
    val id: Int,
    @SerialName("full_name") val fullName: String,
    val email: String,
    val role: String,
)

@Serializable
data class StockMovementResponse(
    val id: Int,
    @SerialName("product_id") val productId: Int,
    @SerialName("movement_type") val movementType: String,
    val quantity: Double,
    val reason: String? = null,
    @SerialName("movement_date") val movementDate: String,
    val notes: String? = null,
    @SerialName("reference_number") val referenceNumber: String? = null,
    @SerialName("previous_quantity") val previousQuantity: Double? = null,
    @SerialName("new_quantity") val newQuantity: Double? = null,
    @SerialName("user_id") val userId: Int,
    val product: ProductRef? = null,
    val user: UserRef? = null,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class StockMovementListResponse(
    val items: List<StockMovementResponse>,
    val total: Int,
    val page: Int,
    @SerialName("page_size") val pageSize: Int,
    @SerialName("total_pages") val totalPages: Int,
)

@Serializable
data class StockMovementCreateRequest(
    @SerialName("product_id") val productId: Int,
    @SerialName("movement_type") val movementType: String,
    val quantity: Double,
    val reason: String? = null,
    @SerialName("movement_date") val movementDate: String,
    val notes: String? = null,
    @SerialName("reference_number") val referenceNumber: String? = null,
)

object MovementType {
    const val STOCK_IN = "Stock In"
    const val STOCK_OUT = "Stock Out"
    const val ADJUSTMENT = "Adjustment"

    val ALL = listOf(STOCK_IN, STOCK_OUT, ADJUSTMENT)
}
