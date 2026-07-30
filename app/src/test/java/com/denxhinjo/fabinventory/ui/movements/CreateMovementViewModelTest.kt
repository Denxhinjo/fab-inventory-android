package com.denxhinjo.fabinventory.ui.movements

import androidx.lifecycle.SavedStateHandle
import com.denxhinjo.fabinventory.MainDispatcherRule
import com.denxhinjo.fabinventory.data.remote.dto.MovementType
import com.denxhinjo.fabinventory.data.remote.dto.ProductResponse
import com.denxhinjo.fabinventory.data.remote.dto.StockMovementResponse
import com.denxhinjo.fabinventory.data.repository.ProductRepository
import com.denxhinjo.fabinventory.data.repository.StockMovementRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateMovementViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val stockMovementRepository: StockMovementRepository = mockk()
    private val productRepository: ProductRepository = mockk()

    private fun sampleProduct(id: Int = 1, quantity: Double = 10.0) = ProductResponse(
        id = id,
        name = "Cement bags",
        sku = "CEM-01",
        categoryId = null,
        description = null,
        quantity = quantity,
        unit = "bags",
        minStockLevel = 5.0,
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

    private fun viewModel(prefilledProductId: Int? = null): CreateMovementViewModel {
        val handle = SavedStateHandle(
            mapOf("prefilledProductId" to prefilledProductId?.toString()),
        )
        return CreateMovementViewModel(handle, stockMovementRepository, productRepository)
    }

    @Test
    fun `prefilled product id loads the product into state`() = runTest {
        coEvery { productRepository.getProduct(1) } returns Result.success(sampleProduct(id = 1))

        val viewModel = viewModel(prefilledProductId = 1)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.selectedProduct?.id)
    }

    @Test
    fun `submit without a selected product sets an error`() = runTest {
        val viewModel = viewModel(prefilledProductId = null)

        viewModel.submit()

        assertEquals("Select a product first", viewModel.uiState.value.error)
    }

    @Test
    fun `submit with a non-positive quantity sets an error and does not call the repository`() = runTest {
        coEvery { productRepository.getProduct(1) } returns Result.success(sampleProduct(id = 1))
        val viewModel = viewModel(prefilledProductId = 1)
        advanceUntilIdle()

        viewModel.onQuantityChange("0")
        viewModel.submit()

        assertEquals("Enter a quantity greater than 0", viewModel.uiState.value.error)
        coVerify(exactly = 0) { stockMovementRepository.createMovement(any()) }
    }

    @Test
    fun `successful submit marks state as submitted`() = runTest {
        val product = sampleProduct(id = 1)
        coEvery { productRepository.getProduct(1) } returns Result.success(product)
        coEvery { stockMovementRepository.createMovement(any()) } returns Result.success(
            StockMovementResponse(
                id = 42,
                productId = 1,
                movementType = MovementType.STOCK_IN,
                quantity = 5.0,
                reason = null,
                movementDate = "2026-01-01",
                notes = null,
                referenceNumber = null,
                previousQuantity = 10.0,
                newQuantity = 15.0,
                userId = 1,
                product = null,
                user = null,
                createdAt = "2026-01-01T00:00:00",
            ),
        )

        val viewModel = viewModel(prefilledProductId = 1)
        advanceUntilIdle()

        viewModel.onQuantityChange("5")
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.submitted)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertNull(viewModel.uiState.value.error)
    }
}
