package com.aloe_droid.data.repository.impl

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.map
import com.aloe_droid.data.common.Dispatcher
import com.aloe_droid.data.common.DispatcherType
import com.aloe_droid.data.datasource.dto.store.MenuDTO
import com.aloe_droid.data.datasource.dto.store.StoreDTO
import com.aloe_droid.data.datasource.dto.store.StoreDetailDTO
import com.aloe_droid.data.datasource.local.dao.StoreDao
import com.aloe_droid.data.datasource.local.dao.StoreQueryDao
import com.aloe_droid.data.datasource.local.data.StoresWithQuery
import com.aloe_droid.data.datasource.local.entity.StoreEntity
import com.aloe_droid.data.datasource.local.entity.StoreEntity.Companion.toStore
import com.aloe_droid.data.datasource.local.entity.StoreEntity.Companion.toStoreList
import com.aloe_droid.data.datasource.network.source.StoreDataSource
import com.aloe_droid.data.repository.mapper.StoreMapper.toMenuList
import com.aloe_droid.data.repository.mapper.StoreMapper.toStore
import com.aloe_droid.data.repository.mapper.StoreMapper.toStoreDetail
import com.aloe_droid.data.repository.page.StoreRemoteMediator
import com.aloe_droid.domain.entity.Menu
import com.aloe_droid.domain.entity.Store
import com.aloe_droid.domain.entity.StoreDetail
import com.aloe_droid.domain.entity.StoreQuery
import com.aloe_droid.domain.repository.StoreRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Instant

class StoreRepositoryImpl @Inject constructor(
    private val storeDataSource: StoreDataSource,
    private val storeRemoteMediatorFactory: StoreRemoteMediator.Factory,
    private val storeDao: StoreDao,
    private val queryDao: StoreQueryDao,
    @Dispatcher(DispatcherType.IO) private val ioDispatcher: CoroutineDispatcher
) : StoreRepository {

    @OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
    override fun getStoreList(storeQuery: StoreQuery): Flow<List<Store>> = flow {
        val queryId = queryDao.upsert(storeQuery = storeQuery).id
        val mediator: StoreRemoteMediator = storeRemoteMediatorFactory.create(storeQuery, queryId)
        val result: RemoteMediator.MediatorResult = mediator.load(
            loadType = LoadType.REFRESH,
            state = PagingState(
                pages = emptyList(),
                anchorPosition = null,
                config = PagingConfig(pageSize = storeQuery.size),
                leadingPlaceholderCount = 0
            )
        )
        if (result is RemoteMediator.MediatorResult.Error) throw result.throwable
        emit(queryId)
    }.flatMapLatest { queryId: String ->
        storeDao.getTopStoresWithQueryId(queryId, storeQuery.size).filter { it.queryId == queryId }
    }.map { storesWithQuery: StoresWithQuery ->
        storesWithQuery.storeList.toStoreList()
    }.flowOn(ioDispatcher)

    @OptIn(ExperimentalPagingApi::class)
    override fun getStoreStream(storeQuery: StoreQuery): Flow<PagingData<Store>> = flow {
        val queryId = queryDao.upsert(storeQuery = storeQuery).id
        val flow: Flow<PagingData<Store>> = Pager(
            config = PagingConfig(pageSize = storeQuery.size),
            remoteMediator = storeRemoteMediatorFactory.create(storeQuery, queryId),
            pagingSourceFactory = { storeDao.getStoresByQueryId(queryId = queryId) }
        ).flow.map { pagingData: PagingData<StoreEntity> ->
            pagingData.map { storeEntity: StoreEntity ->
                storeEntity.toStore()
            }
        }.flowOn(ioDispatcher)
        emitAll(flow = flow)
    }

    override fun getLocalStore(storeId: UUID): Flow<Store> = storeDao
        .getStoreFlowById(id = storeId.toString())
        .map { it.toStore() }

    override fun getStore(
        id: UUID,
        latitude: Double,
        longitude: Double
    ): Flow<Store> = storeDataSource.getStore(id = id, latitude = latitude, longitude = longitude)
        .map { storeDTO: StoreDTO -> storeDTO.toStore() }
        .flowOn(ioDispatcher)

    override fun getStoreDetail(id: UUID): Flow<StoreDetail> = storeDataSource.getStoreDetail(id)
        .map { storeDetailDTO: StoreDetailDTO -> storeDetailDTO.toStoreDetail() }
        .flowOn(ioDispatcher)

    override fun getStoreMenus(id: UUID): Flow<List<Menu>> = storeDataSource.getStoreMenus(id = id)
        .map { menuDTOList: List<MenuDTO> -> menuDTOList.toMenuList() }
        .flowOn(ioDispatcher)

    override suspend fun updateStoreFavoriteCount(id: UUID, isLike: Boolean) {
        val storeEntity: StoreEntity = storeDao.getStoreById(id.toString())
        val favoriteCount = storeEntity.favoriteCount
        val updatedStoreEntity: StoreEntity = if (isLike) {
            storeEntity.copy(favoriteCount = favoriteCount + 1)
        } else {
            storeEntity.copy(favoriteCount = favoriteCount - 1)
        }

        storeDao.updateStore(storeEntity = updatedStoreEntity)
    }

    override fun getStoreCount(): Flow<Long> = storeDataSource.getStoreCount()

    override fun getStoreSyncTime(): Flow<Instant> = storeDataSource.getStoreSyncTime()
}
