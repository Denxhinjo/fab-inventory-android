package com.denxhinjo.fabinventory.ui.login

import app.cash.turbine.test
import com.denxhinjo.fabinventory.MainDispatcherRule
import com.denxhinjo.fabinventory.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk {
        every { sessionFlow } returns flowOf(null)
    }

    @Test
    fun `blank username shows validation error without calling repository`() = runTest {
        val viewModel = LoginViewModel(authRepository)

        viewModel.login(username = "", password = "secret")

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun `successful login emits Success state`() = runTest {
        coEvery { authRepository.login("denis", "hunter2") } returns Result.success(Unit)
        val viewModel = LoginViewModel(authRepository)

        viewModel.uiState.test {
            assertEquals(LoginUiState.Idle, awaitItem())
            viewModel.login("denis", "hunter2")
            assertEquals(LoginUiState.Loading, awaitItem())
            assertEquals(LoginUiState.Success, awaitItem())
        }
    }

    @Test
    fun `failed login emits Error state with the repository's message`() = runTest {
        coEvery { authRepository.login("denis", "wrong") } returns
            Result.failure(Exception("Incorrect email/username or password"))
        val viewModel = LoginViewModel(authRepository)

        viewModel.uiState.test {
            assertEquals(LoginUiState.Idle, awaitItem())
            viewModel.login("denis", "wrong")
            assertEquals(LoginUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is LoginUiState.Error)
            assertEquals("Incorrect email/username or password", (error as LoginUiState.Error).message)
        }
    }

    @Test
    fun `consumeError resets an Error state back to Idle`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(Exception("nope"))
        val viewModel = LoginViewModel(authRepository)

        viewModel.login("a", "b")
        advanceUntilIdle()

        viewModel.uiState.test {
            assertTrue(awaitItem() is LoginUiState.Error)
            viewModel.consumeError()
            assertEquals(LoginUiState.Idle, awaitItem())
        }
    }
}
