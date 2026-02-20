package com.aloe_droid.presentation.map

import com.aloe_droid.domain.entity.Store
import com.aloe_droid.domain.entity.StoreMapEntity
import com.aloe_droid.domain.exception.LocationPermissionException
import com.aloe_droid.domain.usecase.GetMapInfoUseCase
import com.aloe_droid.domain.usecase.GetStoreInfoUseCase
import com.aloe_droid.presentation.base.view.BaseViewModel
import com.aloe_droid.presentation.home.data.LocationData.Companion.toLocationData
import com.aloe_droid.presentation.map.contract.MapEffect
import com.aloe_droid.presentation.map.contract.MapEvent
import com.aloe_droid.presentation.map.contract.MapKey
import com.aloe_droid.presentation.map.contract.MapUiData
import com.aloe_droid.presentation.map.contract.MapUiState
import com.aloe_droid.presentation.map.data.MapData
import com.aloe_droid.presentation.map.data.StoreMapData
import com.aloe_droid.presentation.map.data.StoreMapData.Companion.toStoreMapData
import com.aloe_droid.presentation.map.data.StoreMapData.Companion.toStoreMapDataList
import com.google.android.gms.common.api.ResolvableApiException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.UUID

@HiltViewModel(assistedFactory = MapViewModel.Factory::class)
class MapViewModel @AssistedInject constructor(
    @Assisted private val navKey: MapKey,
    private val getMapInfoUseCase: GetMapInfoUseCase,
    private val getStoreInfoUseCase: GetStoreInfoUseCase,
) : BaseViewModel<MapKey, MapUiState, MapEvent, MapEffect>(key = navKey) {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val searchedStores: Flow<List<StoreMapData>> by lazy {
        uiState.distinctUntilChanged(MapUiState.changeComparator)
            .flatMapLatest { mapUiState ->
                buildMapInfoFlow(mapUiState)
            }.onEach { mapEntity: StoreMapEntity ->
                checkEntity(mapEntity = mapEntity)
            }.map { mapEntity: StoreMapEntity ->
                val location = currentState.locationData
                val myLat: Double = location.latitude
                val myLon: Double = location.longitude
                mapEntity.stores.toStoreMapDataList(myLat = myLat, myLon = myLon)
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val markerStore: Flow<StoreMapData?> by lazy {
        uiState.distinctUntilChangedBy { it.selectedMarkerId }
            .flatMapLatest { mapUiState: MapUiState ->
                val id: UUID? = mapUiState.selectedMarkerId
                if (id != null) getStoreInfoUseCase.getLocalStore(storeId = id)
                else flowOf(null)
            }.map { store: Store? ->
                val location = currentState.locationData
                val myLat: Double = location.latitude
                val myLon: Double = location.longitude
                store?.toStoreMapData(myLat = myLat, myLon = myLon)
            }
    }

    val uiData: StateFlow<MapUiData> by lazy {
        combine(searchedStores, markerStore) { stores, selectStore ->
            MapUiData(selectedMarkerStore = selectStore, searchedStoreList = stores)
        }.toViewModelState(initValue = MapUiData())
    }

    override fun initState(routeKey: MapKey): MapUiState = MapUiState()

    override fun handleEvent(event: MapEvent) {
        when (event) {
            MapEvent.LocationRetry -> handleRetry()
            is MapEvent.LocationSkip -> handlePermissionSkip(event.skipMessage)
            MapEvent.CheckLocation -> handleCheckLocation()
            is MapEvent.ChangeMapInfo -> handleChangeMapInfo(event.mapData)
            is MapEvent.SelectStoreMarker -> handleSelectStoreMaker(event.storeData)
            MapEvent.SearchNearbyStores -> handleSearchStores()
            is MapEvent.SelectStore -> handleSelectStore(event.storeData)
        }
    }

    private fun checkEntity(mapEntity: StoreMapEntity) {
        updateState { uiState ->
            val mapData: MapData = if (uiState.isInitialState) {
                MapData(mapCenter = mapEntity.location.toLocationData())
            } else {
                uiState.mapData
            }

            uiState.copy(
                isInitialState = false,
                checkLocation = false,
                findStores = false,
                locationData = mapEntity.location.toLocationData(),
                mapData = mapData
            )
        }

        if (mapEntity.location.isDefault) handleLocationError(mapEntity.location.error)
    }

    private fun handleChangeMapInfo(mapData: MapData) {
        updateState { uiState: MapUiState ->
            uiState.copy(mapData = mapData)
        }
    }

    private fun handleSearchStores() {
        updateState { uiState: MapUiState ->
            uiState.copy(findStores = true, selectedMarkerId = null)
        }
        val effect = MapEffect.ScrollToFirstPosition
        sendSideEffect(uiEffect = effect)
    }

    private fun handleSelectStoreMaker(storeData: StoreMapData) {
        updateState { uiState: MapUiState ->
            uiState.copy(selectedMarkerId = storeData.id)
        }
    }

    private fun handleSelectStore(storeData: StoreMapData) {
        val effect: MapEffect = MapEffect.NavigateStore(id = storeData.id)
        sendSideEffect(uiEffect = effect)
    }

    private fun handleCheckLocation() {
        updateState { uiState: MapUiState ->
            uiState.copy(checkLocation = true)
        }
    }

    private fun handleLocationError(throwable: Throwable?) = when (throwable) {
        is ResolvableApiException -> handleNeedGPS(throwable = throwable)
        is LocationPermissionException -> handleNeedPermission(throwable = throwable)
        else -> Timber.e(throwable)
    }

    private fun handleNeedPermission(throwable: Throwable) {
        Timber.e(throwable)

        updateState { state: MapUiState ->
            state.copy(isNeedPermission = true)
        }
    }

    private fun handleNeedGPS(throwable: ResolvableApiException) {
        updateState { state: MapUiState ->
            state.copy(gpsError = throwable)
        }
    }

    private fun handleRetry() {
        updateState { state: MapUiState ->
            state.copy(isNeedPermission = false, gpsError = null, isInitialState = true)
        }
    }

    private fun handlePermissionSkip(skipMessage: String) {
        showErrorMessage(skipMessage)

        updateState { state: MapUiState ->
            state.copy(isNeedPermission = false, gpsError = null)
        }
    }

    private fun buildMapInfoFlow(mapUiState: MapUiState): Flow<StoreMapEntity> {
        val flow: Flow<StoreMapEntity> = if (mapUiState.isInitialState) {
            getMapInfoUseCase(isLocalLocation = !mapUiState.checkLocation)
        } else {
            val mapData = currentState.mapData
            getMapInfoUseCase(
                isLocalLocation = !mapUiState.checkLocation,
                distance = mapData.distance,
                latitude = mapData.mapCenter.latitude,
                longitude = mapData.mapCenter.longitude
            )
        }
        return flow.handleError()
    }

    override fun handleError(throwable: Throwable) {
        super.handleError(throwable)
        updateState { state: MapUiState ->
            state.copy(
                isInitialState = false,
                checkLocation = false,
                findStores = false
            )
        }

        throwable.message?.let { message: String ->
            showErrorMessage(message = message)
        }
    }

    private fun showErrorMessage(message: String) {
        val effect: MapEffect = MapEffect.ShowErrorMessage(message = message)
        sendSideEffect(uiEffect = effect)
    }

    @AssistedFactory
    interface Factory {
        fun create(key: MapKey): MapViewModel
    }
}
