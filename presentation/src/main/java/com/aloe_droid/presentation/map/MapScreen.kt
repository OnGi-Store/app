package com.aloe_droid.presentation.map

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.Dp
import com.aloe_droid.presentation.base.theme.DefaultShadowElevation
import com.aloe_droid.presentation.base.theme.DetailStoreSheetHeight
import com.aloe_droid.presentation.base.theme.StoreListSheetHeight
import com.aloe_droid.presentation.map.component.OnGiNaverMap
import com.aloe_droid.presentation.map.component.OnGiStoreSheetContent
import com.aloe_droid.presentation.map.data.MapData
import com.aloe_droid.presentation.map.data.StoreMapData
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DEFAULT_DELAY: Long = 300L

@OptIn(ExperimentalNaverMapApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    location: LatLng,
    storeListState: LazyListState,
    storeItems: List<StoreMapData>,
    selectedMarkerStore: StoreMapData?,
    onLocationCheck: () -> Unit,
    onChangeMapData: (MapData) -> Unit,
    onMarkerClick: (StoreMapData) -> Unit,
    onSearch: () -> Unit,
    selectStore: (StoreMapData) -> Unit
) {
    val scope: CoroutineScope = rememberCoroutineScope()
    val sheetState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            skipHiddenState = false
        )
    )

    val peekHeight: Dp = if (selectedMarkerStore != null) {
        DetailStoreSheetHeight
    } else {
        StoreListSheetHeight
    }

    LaunchedEffect(key1 = selectedMarkerStore) {
        if (selectedMarkerStore == null) return@LaunchedEffect
        snapshotFlow { sheetState.bottomSheetState.currentValue }
            .distinctUntilChanged()
            .collect { value: SheetValue ->
                if (value != SheetValue.Expanded) return@collect
                selectStore(selectedMarkerStore)

                withContext(context = NonCancellable) {
                    delay(timeMillis = DEFAULT_DELAY)
                    sheetState.bottomSheetState.partialExpand()
                }
            }
    }

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetPeekHeight = peekHeight,
        sheetShadowElevation = DefaultShadowElevation,
        sheetContent = {
            OnGiStoreSheetContent(
                selectedStore = selectedMarkerStore,
                state = storeListState,
                storeItems = storeItems,
                selectStore = selectStore
            )
        },
        content = {
            OnGiNaverMap(
                location = location,
                storeItems = storeItems,
                onLocationCheck = onLocationCheck,
                onChangeMapData = onChangeMapData,
                onSearch = {
                    scope.launch {
                        sheetState.bottomSheetState.partialExpand()
                    }.invokeOnCompletion {
                        onSearch()
                    }
                },
                onMarkerClick = { store: StoreMapData ->
                    scope.launch {
                        sheetState.bottomSheetState.partialExpand()
                    }.invokeOnCompletion {
                        onMarkerClick(store)
                    }
                }
            )
        }
    )
}
