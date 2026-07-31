package com.denxhinjo.fabinventory.ui.products

import com.denxhinjo.fabinventory.MainDispatcherRule
import com.denxhinjo.fabinventory.data.local.UserSession
import com.denxhinjo.fabinventory.data.remote.dto.ProductListResponse
import com.denxhinjo.fabinventory.data.remote.dto.ProductResponse
import com.denxhinjo.fabinventory.data.repository.AuthRepository
import com.denxhinjo.fabinventory.data.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val productRepository: ProductRepository = mockk()
    private val authRepository: AuthRepository = mockk {
        every { sessionFlow } returns flowOf(UserSession(token = "t", role = "user", fullName = "Test", email = "t@example.com"))
    }

    private fun product(id: Int, name: String) = ProductResponse(
        id = id,
        name = name,
        sku = null,
        categoryId = null,
        description = null,
        quantity = 1.0,
        unit = "pcs",
        minStockLevel = 0.0,
        unitPrice = null,
        locationId = null,
        supplierId = null,
        status = "active",
        notes = null,
        imageUrl = null,
        isLowStock = false,
        category = null,
        location = null,
        supplier = null,
        createdAt = "2026-01-01T00:00:00",
        updatedAt = "2026-01-01T00:00:00",
    )

    @Test
    fun `initial load populates products and pagination state`() = runTest {
        coEvery { productRepository.getProducts(page = 1, search = "") } returns Result.success(
            ProductListResponse(
                items = listOf(product(1, "Cement"), product(2, "Nails")),
                total = 2,
                page = 1,
                pageSize = 20,
                totalPages = 1,
            ),
        )

        val viewModel = ProductsViewModel(productRepository, authRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.products.size)
        assertFalse(state.isLoading)
        assertFalse(state.hasMore)
    }

    @Test
    fun `search query is debounced before triggering a reload`() = runTest {
        coEvery { productRepository.getProducts(page = 1, search = "") } returns Result.success(
            ProductListResponse(emptyList(), total = 0, page = 1, pageSize = 20, totalPages = 1),
        )
        coEvery { productRepository.getProducts(page = 1, search = "cement") } returns Result.success(
            ProductListResponse(items = listOf(product(1, "Cement")), total = 1, page = 1, pageSize = 20, totalPages = 1),
        )

        val viewModel = ProductsViewModel(productRepository, authRepository)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("cement")
        advanceTimeBy(399)
        assertTrue(viewModel.uiState.value.products.isEmpty())

        advanceTimeBy(50)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.products.size)
        assertEquals("Cement", viewModel.uiState.value.products.first().name)
    }

    @Test
    fun `a failed load surfaces the error message`() = runTest {
        coEvery { productRepository.getProducts(page = 1, search = "") } returns
            Result.failure(Exception("Can't reach the server. Check your connection and the server address."))

        val viewModel = ProductsViewModel(productRepository, authRepository)
        advanceUntilIdle()

        assertEquals(
            "Can't reach the server. Check your connection and the server address.",
            viewModel.uiState.value.error,
        )
        assertTrue(viewModel.uiState.value.products.isEmpty())
    }
}
