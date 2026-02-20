package com.aloe_droid.data.datasource.network.source

import com.aloe_droid.data.datasource.dto.store.MenuDTO
import com.aloe_droid.data.datasource.dto.store.StoreDTO
import com.aloe_droid.data.datasource.dto.store.StoreDetailDTO
import com.aloe_droid.data.datasource.dto.store.StorePage
import com.aloe_droid.domain.entity.StoreQuery
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.time.Instant

interface StoreDataSource {

    fun getStoreList(storeQuery: StoreQuery): Flow<StorePage>

    fun getStore(id: UUID, latitude: Double, longitude: Double): Flow<StoreDTO>

    fun getStoreDetail(id: UUID): Flow<StoreDetailDTO>

    fun getStoreMenus(id: UUID): Flow<List<MenuDTO>>

    fun getStoreCount(): Flow<Long>

    fun getStoreSyncTime(): Flow<Instant>
}
