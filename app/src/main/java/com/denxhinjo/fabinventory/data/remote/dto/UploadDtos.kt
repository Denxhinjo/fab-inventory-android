package com.denxhinjo.fabinventory.data.remote.dto

import kotlinx.serialization.Serializable

/** Mirrors backend/app/routers/uploads.py -- the single upload path for both clients. */
@Serializable
data class UploadImageResponse(
    val url: String,
)
