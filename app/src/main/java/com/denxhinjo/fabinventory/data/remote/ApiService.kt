package com.denxhinjo.fabinventory.data.remote

import com.denxhinjo.fabinventory.data.remote.dto.CategoryResponse
import com.denxhinjo.fabinventory.data.remote.dto.CategorySummary
import com.denxhinjo.fabinventory.data.remote.dto.DashboardResponse
import com.denxhinjo.fabinventory.data.remote.dto.LocationIdsRequest
import com.denxhinjo.fabinventory.data.remote.dto.LocationRequest
import com.denxhinjo.fabinventory.data.remote.dto.LocationResponse
import com.denxhinjo.fabinventory.data.remote.dto.ProductListResponse
import com.denxhinjo.fabinventory.data.remote.dto.ProductResponse
import com.denxhinjo.fabinventory.data.remote.dto.StockMovementCreateRequest
import com.denxhinjo.fabinventory.data.remote.dto.StockMovementListResponse
import com.denxhinjo.fabinventory.data.remote.dto.StockMovementResponse
import com.denxhinjo.fabinventory.data.remote.dto.SupplierRequest
import com.denxhinjo.fabinventory.data.remote.dto.SupplierResponse
import com.denxhinjo.fabinventory.data.remote.dto.SupplierSummary
import com.denxhinjo.fabinventory.data.remote.dto.TokenResponse
import com.denxhinjo.fabinventory.data.remote.dto.UserCreateRequest
import com.denxhinjo.fabinventory.data.remote.dto.UserResponse
import com.denxhinjo.fabinventory.data.remote.dto.UploadImageResponse
import com.denxhinjo.fabinventory.data.remote.dto.UserUpdateRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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

    @FormUrlEncoded
    @POST("api/products")
    suspend fun createProduct(
        @Field("name") name: String,
        @Field("sku") sku: String? = null,
        @Field("category_id") categoryId: Int? = null,
        @Field("description") description: String? = null,
        @Field("quantity") quantity: Double? = null,
        @Field("unit") unit: String? = null,
        @Field("min_stock_level") minStockLevel: Double? = null,
        @Field("unit_price") unitPrice: Double? = null,
        @Field("location_id") locationId: Int? = null,
        @Field("supplier_id") supplierId: Int? = null,
        @Field("product_status") productStatus: String? = null,
        @Field("notes") notes: String? = null,
        @Field("image_url") imageUrl: String? = null,
    ): ProductResponse

    @FormUrlEncoded
    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Field("name") name: String? = null,
        @Field("sku") sku: String? = null,
        @Field("category_id") categoryId: Int? = null,
        @Field("description") description: String? = null,
        @Field("quantity") quantity: Double? = null,
        @Field("unit") unit: String? = null,
        @Field("min_stock_level") minStockLevel: Double? = null,
        @Field("unit_price") unitPrice: Double? = null,
        @Field("location_id") locationId: Int? = null,
        @Field("supplier_id") supplierId: Int? = null,
        @Field("product_status") productStatus: String? = null,
        @Field("notes") notes: String? = null,
        @Field("image_url") imageUrl: String? = null,
    ): ProductResponse

    // Returned as a raw Response<ResponseBody> rather than a JSON type: the
    // backend replies 204 No Content, and running the kotlinx.serialization
    // converter over an empty body would fail -- ResponseBody is one of
    // Retrofit's built-in pass-through types, so no converter runs at all.
    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<ResponseBody>

    @GET("api/categories")
    suspend fun getCategories(): List<CategorySummary>

    @GET("api/categories")
    suspend fun getCategoriesFull(): List<CategoryResponse>

    @GET("api/suppliers")
    suspend fun getSuppliers(): List<SupplierSummary>

    @GET("api/suppliers")
    suspend fun getSuppliersFull(): List<SupplierResponse>

    @POST("api/suppliers")
    suspend fun createSupplier(@Body request: SupplierRequest): SupplierResponse

    @PUT("api/suppliers/{id}")
    suspend fun updateSupplier(@Path("id") id: Int, @Body request: SupplierRequest): SupplierResponse

    @DELETE("api/suppliers/{id}")
    suspend fun deleteSupplier(@Path("id") id: Int): Response<ResponseBody>

    @GET("api/locations")
    suspend fun getLocations(): List<LocationResponse>

    @POST("api/locations")
    suspend fun createLocation(@Body request: LocationRequest): LocationResponse

    @PUT("api/locations/{id}")
    suspend fun updateLocation(@Path("id") id: Int, @Body request: LocationRequest): LocationResponse

    @DELETE("api/locations/{id}")
    suspend fun deleteLocation(@Path("id") id: Int): Response<ResponseBody>

    @GET("api/users")
    suspend fun getUsers(): List<UserResponse>

    @POST("api/users")
    suspend fun createUser(@Body request: UserCreateRequest): UserResponse

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body request: UserUpdateRequest): UserResponse

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<ResponseBody>

    @GET("api/users/me/locations")
    suspend fun getMyPermittedLocations(): List<LocationResponse>

    @GET("api/users/{id}/locations")
    suspend fun getUserPermittedLocations(@Path("id") userId: Int): List<LocationResponse>

    @PUT("api/users/{id}/locations")
    suspend fun setUserPermittedLocations(
        @Path("id") userId: Int,
        @Body request: LocationIdsRequest,
    ): List<LocationResponse>

    @Multipart
    @POST("api/uploads/image")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("folder") folder: RequestBody,
    ): UploadImageResponse

    @GET("api/stock-movements")
    suspend fun getStockMovements(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int = 20,
        @Query("product_id") productId: Int? = null,
        @Query("movement_type") movementType: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
    ): StockMovementListResponse

    @POST("api/stock-movements")
    suspend fun createStockMovement(@Body request: StockMovementCreateRequest): StockMovementResponse
}
