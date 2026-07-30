package com.denxhinjo.fabinventory.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors backend/app/schemas/location.py::LocationOut.
 */
@Serializable
data class LocationResponse(
    val id: Int,
    val name: String,
    val address: String? = null,
    val city: String? = null,
    @SerialName("manager_name") val managerName: String? = null,
    @SerialName("contact_email") val contactEmail: String? = null,
    @SerialName("contact_phone") val contactPhone: String? = null,
    val notes: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("product_count") val productCount: Int? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class LocationIdsRequest(
    @SerialName("location_ids") val locationIds: List<Int>,
)

/**
 * Mirrors backend/app/schemas/location.py::LocationCreate / LocationUpdate.
 * Used for both create and update -- on update, omit (leave null) any field
 * that shouldn't change; the backend only applies fields it actually receives.
 */
@Serializable
data class LocationRequest(
    val name: String,
    val address: String? = null,
    val city: String? = null,
    @SerialName("manager_name") val managerName: String? = null,
    @SerialName("contact_email") val contactEmail: String? = null,
    @SerialName("contact_phone") val contactPhone: String? = null,
    val notes: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
)
