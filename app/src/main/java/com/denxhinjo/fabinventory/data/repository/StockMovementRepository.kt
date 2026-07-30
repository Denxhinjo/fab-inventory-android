package com.denxhinjo.fabinventory.data.repository

import com.denxhinjo.fabinventory.data.remote.ApiService
import com.denxhinjo.fabinventory.data.remote.dto.StockMovementCreateRequest
import com.denxhinjo.fabinventory.data.remote.dto.StockMovementListResponse
import com.denxhinjo.fabinventory.data.remote.dto.StockMovementResponse
import com.denxhinjo.fabinventory.data.remote.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockMovementRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getMovements(page: Int, pageSize: Int = 20): Result<StockMovementListResponse> =
        safeApiCall { apiService.getStockMovements(page = page, pageSize = pageSize) }

    suspend fun createMovement(request: StockMovementCreateRequest): Result<StockMovementResponse> =
        safeApiCall { apiService.createStockMovement(request) }
}
