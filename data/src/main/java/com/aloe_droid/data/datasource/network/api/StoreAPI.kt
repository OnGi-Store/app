package com.aloe_droid.data.datasource.network.api

import com.aloe_droid.data.datasource.dto.store.MenuDTO
import com.aloe_droid.data.datasource.dto.store.StoreDTO
import com.aloe_droid.data.datasource.dto.store.StoreDetailDTO
import com.aloe_droid.data.datasource.dto.store.StorePage
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import java.util.UUID
import kotlin.time.Instant

interface StoreAPI {

    @GET("api/v1/stores")
    suspend fun getStores(@QueryMap queryParams: Map<String, String>): Response<StorePage>

    @GET("api/v1/stores/{storeId}")
    suspend fun getStore(
        @Path(value = "storeId") id: UUID,
        @Query(value = "latitude") latitude: Double,
        @Query(value = "longitude") longitude: Double,
    ): Response<StoreDTO>

    @GET("api/v1/stores/{storeId}/detail")
    suspend fun getStoreDetail(@Path(value = "storeId") id: UUID): Response<StoreDetailDTO>

    @GET("api/v1/stores/{storeId}/menus")
    suspend fun getStoreMenus(@Path(value = "storeId") id: UUID): Response<List<MenuDTO>>

    @GET("api/v1/stores/count")
    suspend fun getStoreCount(): Response<Long>

    @GET("api/v1/time")
    suspend fun getStoreSyncTime(): Response<Instant>
}
