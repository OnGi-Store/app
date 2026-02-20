package com.aloe_droid.presentation.map

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.ui.NavDisplay
import com.aloe_droid.presentation.R
import com.aloe_droid.presentation.base.component.LoadingScreen
import com.aloe_droid.presentation.base.view.ScreenTransition
import com.aloe_droid.presentation.base.view.UiContract
import com.aloe_droid.presentation.home.contract.HomeKey
import com.aloe_droid.presentation.map.component.CollectMapSideEffects
import com.aloe_droid.presentation.map.component.LocationHandler
import com.aloe_droid.presentation.map.contract.MapEvent
import com.aloe_droid.presentation.map.contract.MapKey
import com.aloe_droid.presentation.map.contract.MapUiData
import com.aloe_droid.presentation.map.contract.MapUiState
import com.aloe_droid.presentation.map.data.MapData
import com.aloe_droid.presentation.map.data.StoreMapData
import com.aloe_droid.presentation.search.contract.SearchKey
import com.aloe_droid.presentation.store.contract.StoreKey
import com.naver.maps.geometry.LatLng
import java.util.UUID

fun EntryProviderScope<UiContract.RouteKey>.mapScreen(
    showSnackMessage: (SnackbarVisuals) -> Unit,
    navigateToStore: (UUID) -> Unit,
) = entry<MapKey>(
    clazzContentKey = { it },
    metadata = NavDisplay.transitionSpec {
        val isFromLeftTab = initialState.key is HomeKey || initialState.key is SearchKey
        if (isFromLeftTab) {
            ScreenTransition.slideInFromRight() togetherWith ScreenTransition.slideOutToLeft()
        } else {
            ScreenTransition.slideInFromLeft() togetherWith ScreenTransition.slideOutToRight()
        }
    } + NavDisplay.popTransitionSpec {
        val isFromStore = initialState.key is StoreKey
        if (isFromStore) {
            ScreenTransition.fadeInAnim() togetherWith ScreenTransition.fadeOutAnim()
        } else {
            EnterTransition.None togetherWith ExitTransition.None
        }
    }
) { key: MapKey ->
    val viewModel: MapViewModel =
        hiltViewModel { factory: MapViewModel.Factory -> factory.create(key = key) }
    val uiState: MapUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiData: MapUiData by viewModel.uiData.collectAsStateWithLifecycle()
    val storeListState: LazyListState = rememberLazyListState()

    CollectMapSideEffects(
        sideEffect = viewModel.uiEffect,
        storeListState = storeListState,
        showSnackMessage = showSnackMessage,
        navigateToStore = navigateToStore
    )

    LocationHandler(uiState = uiState, viewModel = viewModel)

    if (uiState.isInitialState) {
        LoadingScreen(content = stringResource(id = R.string.loading))
    } else {
        MapScreen(
            location = LatLng(uiState.locationData.latitude, uiState.locationData.longitude),
            storeListState = storeListState,
            storeItems = uiData.searchedStoreList,
            selectedMarkerStore = uiData.selectedMarkerStore,
            onLocationCheck = {
                val event = MapEvent.CheckLocation
                viewModel.sendEvent(event = event)
            },
            onMarkerClick = { store: StoreMapData ->
                val event = MapEvent.SelectStoreMarker(storeData = store)
                viewModel.sendEvent(event = event)
            },
            selectStore = { store: StoreMapData ->
                val event = MapEvent.SelectStore(storeData = store)
                viewModel.sendEvent(event = event)
            },
            onChangeMapData = { map: MapData ->
                val event = MapEvent.ChangeMapInfo(mapData = map)
                viewModel.sendEvent(event = event)
            },
            onSearch = {
                val event = MapEvent.SearchNearbyStores
                viewModel.sendEvent(event = event)
            }
        )
    }
}
