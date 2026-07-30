package com.denxhinjo.fabinventory.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors backend/app/schemas/supplier.py::SupplierOut. A separate, fuller
 * type from [SupplierSummary] (used only for product-form pickers) since the
 * supplier management screens need every field.
 */
@Serializable
data class SupplierResponse(
    val id: Int,
    val name: String,
    @SerialName("contact_name") val contactName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val notes: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("product_count") val productCount: Int? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

/** Mirrors SupplierCreate / SupplierUpdate -- see LocationRequest's docs for the shared update semantics. */
@Serializable
data class SupplierRequest(
    val name: String,
    @SerialName("contact_name") val contactName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val notes: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
)
