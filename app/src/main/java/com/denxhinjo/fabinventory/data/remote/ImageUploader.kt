package com.denxhinjo.fabinventory.data.remote

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uploads images through the backend's own `/api/uploads/image` endpoint
 * (backend/app/routers/uploads.py), which holds the Cloudinary credentials
 * and forwards them server-side. Neither this app nor the web frontend talks
 * to Cloudinary directly or embeds an unsigned upload preset anymore.
 */
@Singleton
class ImageUploader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
) {
    suspend fun uploadImage(uri: Uri, folder: String = "products"): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(Exception("Couldn't read the selected image"))

            val filePart = MultipartBody.Part.createFormData(
                "file",
                "upload.jpg",
                bytes.toRequestBody("image/*".toMediaTypeOrNull()),
            )
            val folderPart = folder.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.uploadImage(filePart, folderPart)
            Result.success(response.url)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Couldn't upload image. Check your connection and try again."))
        }
    }
}
