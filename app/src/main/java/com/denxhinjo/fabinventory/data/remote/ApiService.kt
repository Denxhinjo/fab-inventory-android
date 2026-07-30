package com.denxhinjo.fabinventory.data.remote

import com.denxhinjo.fabinventory.data.remote.dto.DashboardResponse
import com.denxhinjo.fabinventory.data.remote.dto.ProductListResponse
import com.denxhinjo.fabinventory.data.remote.dto.ProductResponse
import com.denxhinjo.fabinventory.data.remote.dto.StockMovementCreateRequest
import com.denxhinjo.fabinventory.data.remote.dto.StockMovementListResponse
import com.denxhinjo.fabinventory.data.remote.dto.StockMovementResponse
import com.denxhinjo.fabinventory.data.remote.dto.TokenResponse
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Matches the FastAPI routes exposed by the FAB Construction IMS backend
 * (see the routers under backend/app/routers in the fab-construction-ims repo).
 */
interface ApiService {

    @FormUrlEncoded
    @POST("api/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password",
    ): TokenResponse

    @GET("api/dashboard/stats")
    suspend fun getDashboardStats(): DashboardResponse

    @GET("api/products")
    suspend fun getProducts(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int = 20,
        @Query("search") search: String? = null,
        @Query("low_stock") lowStock: Boolean? = null,
    ): ProductListResponse

    @GET("api/products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductResponse

    @GET("api/stock-movements")
    suspend fun getStockMovements(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int = 20,
        @Query("product_id") productId: Int? = null,
        @Query("movement_type") movementType: String? = null,
    ): StockMovementListResponse

    @POST("api/stock-movements")
    suspend fun createStockMovement(@Body request: StockMovementCreateRequest): StockMovementResponse
}
