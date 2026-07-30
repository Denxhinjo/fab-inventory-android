package com.denxhinjo.fabinventory.data.remote

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Wraps a suspending Retrofit call in a [Result], translating common failure
 * modes into user-facing messages. Deliberately rethrows
 * [CancellationException] instead of swallowing it, since catching it would
 * break structured concurrency (e.g. cancelling a search job on new input).
 */
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        Result.failure(Exception(httpErrorMessage(e)))
    } catch (e: IOException) {
        Result.failure(Exception("Can't reach the server. Check your connection and the server address."))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun httpErrorMessage(e: HttpException): String = when (e.code()) {
    401 -> "Session expired. Please log in again."
    403 -> "You don't have permission to do that."
    404 -> "Not found."
    429 -> "Too many attempts. Please wait a moment and try again."
    in 500..599 -> "Server error. Please try again shortly."
    else -> e.message() ?: "Request failed (${e.code()})."
}
