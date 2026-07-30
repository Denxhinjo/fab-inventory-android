package com.denxhinjo.fabinventory.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors backend/app/schemas/user.py::UserOut.
 */
@Serializable
data class UserResponse(
    val id: Int,
    val email: String,
    val username: String,
    @SerialName("full_name") val fullName: String,
    val role: String,
    val phone: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)
