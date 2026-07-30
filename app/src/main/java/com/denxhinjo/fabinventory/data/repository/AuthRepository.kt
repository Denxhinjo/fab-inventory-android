package com.denxhinjo.fabinventory.data.repository

import com.denxhinjo.fabinventory.data.local.TokenManager
import com.denxhinjo.fabinventory.data.local.UserSession
import com.denxhinjo.fabinventory.data.remote.ApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
) {
    val sessionFlow: Flow<UserSession?> = tokenManager.sessionFlow

    // Deliberately doesn't reuse safeApiCall: a 401 here means something different
    // (wrong credentials) than it does for every other, already-authenticated call
    // (session expired), so this endpoint needs its own error mapping.
    suspend fun login(username: String, password: String): Result<Unit> {
        return try {
            val response = apiService.login(username = username, password = password)
            tokenManager.saveSession(
                token = response.accessToken,
                role = response.role,
                fullName = response.fullName,
                email = response.email,
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            val message = when (e.code()) {
                401 -> "Incorrect email/username or password"
                429 -> "Too many login attempts. Please wait a moment and try again."
                else -> "Login failed (${e.code()}). Please try again."
            }
            Result.failure(Exception(message))
        } catch (e: IOException) {
            Result.failure(Exception("Can't reach the server. Check your connection and the server address."))
        }
    }

    suspend fun logout() {
        tokenManager.clearSession()
    }
}
