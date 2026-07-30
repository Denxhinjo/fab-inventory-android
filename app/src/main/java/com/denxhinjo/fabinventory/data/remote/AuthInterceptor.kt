package com.denxhinjo.fabinventory.data.remote

import com.denxhinjo.fabinventory.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Attaches the stored bearer token to every request. Runs on OkHttp's own
 * dispatcher thread, so reading the (suspending) DataStore value via
 * [runBlocking] here is the standard pattern rather than a hack.
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenManager.currentToken() }
        val original = chain.request()
        val request = if (token != null) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
