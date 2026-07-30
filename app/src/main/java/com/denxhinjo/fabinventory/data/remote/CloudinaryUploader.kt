package com.denxhinjo.fabinventory.data.remote

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

private const val CLOUDINARY_CLOUD_NAME = "dgtqh3jr"
private const val CLOUDINARY_UPLOAD_PRESET = "fab-ims-products"

/**
 * Uploads product photos directly to Cloudinary via its unsigned-upload API,
 * matching the frontend's approach (frontend/src/pages/Inventory/AddEditProduct.tsx)
 * rather than routing image bytes through our own backend, which has no
 * upload endpoint of its own -- it only stores whatever image_url string it's
 * given.
 *
 * Deliberately uses a plain OkHttpClient rather than the app's shared one:
 * that client carries our backend's bearer-token AuthInterceptor, which has
 * no business being sent to a third-party host.
 */
@Singleton
class CloudinaryUploader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val client = OkHttpClient()

    suspend fun uploadImage(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(Exception("Couldn't read the selected image"))

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                .addFormDataPart("file", "upload.jpg", bytes.toRequestBody("image/*".toMediaTypeOrNull()))
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Image upload failed (${response.code})"))
                }
                val secureUrl = extractSecureUrl(bodyString)
                    ?: return@withContext Result.failure(Exception("Upload succeeded but no image URL was returned"))
                Result.success(secureUrl)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Couldn't upload image. Check your connection and try again."))
        }
    }

    private fun extractSecureUrl(rawJson: String): String? = try {
        Json.parseToJsonElement(rawJson).jsonObject["secure_url"]?.jsonPrimitive?.content
    } catch (e: Exception) {
        null
    }
}
