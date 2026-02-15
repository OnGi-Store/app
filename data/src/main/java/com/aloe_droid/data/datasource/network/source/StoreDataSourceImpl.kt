package com.aloe_droid.data.datasource.network.source

import com.aloe_droid.data.datasource.dto.store.MenuDTO
import com.aloe_droid.data.datasource.dto.store.StoreDTO
import com.aloe_droid.data.datasource.dto.store.StoreDetailDTO
import com.aloe_droid.data.datasource.dto.store.StorePage
import com.aloe_droid.data.datasource.network.api.StoreAPI
import com.aloe_droid.data.datasource.network.util.ApiUtil.safeApiCallToFlow
import com.aloe_droid.data.datasource.network.util.FlowUtil.retryOnIOException
import com.aloe_droid.domain.entity.StoreQuery
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Instant

class StoreDataSourceImpl @Inject constructor(private val storeAPI: StoreAPI) : StoreDataSource {

    override fun getStoreList(storeQuery: StoreQuery): Flow<StorePage> = safeApiCallToFlow {
        storeAPI.getStores(storeQuery.toQueryMap().mapValues { it.value.toString() })
    }.retryOnIOException()

    override fun getStore(
        id: UUID,
        latitude: Double,
        longitude: Double
    ): Flow<StoreDTO> = safeApiCallToFlow {
        storeAPI.getStore(id = id, latitude = latitude, longitude = longitude)
    }.retryOnIOException()

    override fun getStoreDetail(id: UUID): Flow<StoreDetailDTO> = safeApiCallToFlow {
        storeAPI.getStoreDetail(id = id)
    }.retryOnIOException()

    override fun getStoreMenus(id: UUID): Flow<List<MenuDTO>> = safeApiCallToFlow {
        storeAPI.getStoreMenus(id = id)
    }.retryOnIOException()

    override fun getStoreCount(): Flow<Long> = safeApiCallToFlow {
        storeAPI.getStoreCount()
    }.retryOnIOException()

    override fun getStoreSyncTime(): Flow<Instant> = safeApiCallToFlow {
        storeAPI.getStoreSyncTime()
    }.retryOnIOException()
}
