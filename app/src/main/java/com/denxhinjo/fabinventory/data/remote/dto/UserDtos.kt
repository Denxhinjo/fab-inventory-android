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

/** Mirrors backend/app/schemas/user.py::UserCreate. Role must be "admin" or "user". */
@Serializable
data class UserCreateRequest(
    val email: String,
    val username: String,
    @SerialName("full_name") val fullName: String,
    val role: String,
    val phone: String? = null,
    val password: String,
)

/**
 * Mirrors UserUpdate -- every field optional, only supplied ones change.
 * A blank/null password leaves the existing password untouched.
 */
@Serializable
data class UserUpdateRequest(
    val email: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    val role: String? = null,
    val phone: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
    val password: String? = null,
)
