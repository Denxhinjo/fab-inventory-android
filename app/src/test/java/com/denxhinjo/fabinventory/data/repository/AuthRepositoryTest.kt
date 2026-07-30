package com.denxhinjo.fabinventory.data.repository

import com.denxhinjo.fabinventory.data.local.TokenManager
import com.denxhinjo.fabinventory.data.remote.ApiService
import com.denxhinjo.fabinventory.data.remote.dto.TokenResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AuthRepositoryTest {

    private val apiService: ApiService = mockk()
    private val tokenManager: TokenManager = mockk(relaxUnitFun = true) {
        every { sessionFlow } returns flowOf(null)
    }
    private val repository = AuthRepository(apiService, tokenManager)

    @Test
    fun `successful login saves the session from the token response`() = runTest {
        val token = TokenResponse(
            accessToken = "abc123",
            tokenType = "bearer",
            userId = 7,
            role = "admin",
            fullName = "Denis Xhabrahimi",
            email = "denis@example.com",
        )
        coEvery { apiService.login(username = "denis", password = "secret") } returns token

        val result = repository.login("denis", "secret")

        assertTrue(result.isSuccess)
        coVerify {
            tokenManager.saveSession(
                token = "abc123",
                role = "admin",
                fullName = "Denis Xhabrahimi",
                email = "denis@example.com",
            )
        }
    }

    @Test
    fun `401 from the login endpoint means bad credentials, not session expiry`() = runTest {
        val emptyBody = "".toResponseBody(null)
        val httpException = HttpException(Response.error<TokenResponse>(401, emptyBody))
        coEvery { apiService.login(username = "denis", password = "wrong") } throws httpException

        val result = repository.login("denis", "wrong")

        assertTrue(result.isFailure)
        assertEquals("Incorrect email/username or password", result.exceptionOrNull()?.message)
    }

    @Test
    fun `logout clears the stored session`() = runTest {
        repository.logout()

        coVerify { tokenManager.clearSession() }
    }
}
