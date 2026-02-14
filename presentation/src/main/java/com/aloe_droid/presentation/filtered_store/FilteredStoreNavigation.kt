package com.aloe_droid.presentation.filtered_store

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.aloe_droid.presentation.R
import com.aloe_droid.presentation.base.component.LoadingScreen
import com.aloe_droid.presentation.base.view.BaseSnackBarVisuals
import com.aloe_droid.presentation.base.view.CollectSideEffects
import com.aloe_droid.presentation.base.view.ScreenTransition
import com.aloe_droid.presentation.filtered_store.contract.FilteredStore
import com.aloe_droid.presentation.filtered_store.contract.FilteredStoreEffect
import com.aloe_droid.presentation.filtered_store.contract.FilteredStoreEvent
import com.aloe_droid.presentation.filtered_store.contract.FilteredStoreUiState
import com.aloe_droid.presentation.filtered_store.data.StoreDistanceRange
import com.aloe_droid.presentation.filtered_store.data.StoreFilterNavTypes.StoreFilterTypeMap
import com.aloe_droid.presentation.filtered_store.data.StoreSortType
import com.aloe_droid.presentation.home.data.StoreData
import com.aloe_droid.presentation.search.contract.Search
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.filteredStoreScreen(
    showSnackMessage: (SnackbarVisuals) -> Unit,
    navigateUp: () -> Unit,
    navigateToStore: (UUID) -> Unit,
    navigateToSearch: () -> Unit
) = composable<FilteredStore>(
    typeMap = StoreFilterTypeMap,
    enterTransition = {
        ScreenTransition.slideInFromRight()
    },
    popEnterTransition = {
        if (initialState.destination.hasRoute<Search>()) ScreenTransition.fadeInAnim()
        else ScreenTransition.slideInFromLeft()
    },
    exitTransition = {
        if (targetState.destination.hasRoute<Search>()) ScreenTransition.fadeOutAnim()
        else ScreenTransition.slideOutToLeft()
    },
    popExitTransition = {
        ScreenTransition.slideOutToRight()
    }
) {
    val viewModel: FilteredStoreViewModel = hiltViewModel()
    val uiState: FilteredStoreUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val storeItems: LazyPagingItems<StoreData> = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val lazyListState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    CollectSideEffects(effectFlow = viewModel.uiEffect) { sideEffect: FilteredStoreEffect ->
        when (sideEffect) {
            is FilteredStoreEffect.ShowErrorMessage -> {
                val snackBarVisuals = BaseSnackBarVisuals(message = sideEffect.message)
                showSnackMessage(snackBarVisuals)
            }

            is FilteredStoreEffect.NavigateStore -> {
                navigateToStore(sideEffect.id)
            }

            FilteredStoreEffect.ScrollToFirstPosition -> {
                lazyListState.animateScrollToItem(0)
            }

        }
    }

    if (storeItems.loadState == LoadState.Loading) {
        LoadingScreen(content = stringResource(id = R.string.loading))
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(connection = scrollBehavior.nestedScrollConnection),
            topBar = {
                FilteredStoreTopBar(
                    storeFilter = uiState.storeFilter,
                    scrollBehavior = scrollBehavior,
                    navigateUp = navigateUp,
                    navigateToSearch = navigateToSearch
                )
            }
        ) { paddingValues: PaddingValues ->
            FilteredStoreScreen(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                storeItems = storeItems,
                isRefresh = uiState.isRefresh,
                selectedSortType = uiState.storeFilter.sortType,
                selectedDistanceRange = uiState.storeFilter.distanceRange,
                lazyListState = lazyListState,
                isOnlyFavorites = uiState.storeFilter.onlyFavorites,
                isShowOrderBottomSheet = uiState.isShowOrderBottomSheet,
                isShowDistanceBottomSheet = uiState.isShowDistanceBottomSheet,
                selectStore = { storeData: StoreData ->
                    val event = FilteredStoreEvent.SelectStore(storeData = storeData)
                    viewModel.sendEvent(event = event)
                },
                onRefresh = {
                    val event = FilteredStoreEvent.RefreshEvent
                    viewModel.sendEvent(event = event)
                    storeItems.refresh()
                },
                setShowOrderBottomSheet = {
                    val event = FilteredStoreEvent.ShowOrderBottomSheet
                    viewModel.sendEvent(event = event)
                },
                setShowDistanceBottomSheet = {
                    val event = FilteredStoreEvent.ShowDistanceBottomSheet
                    viewModel.sendEvent(event = event)
                },
                onDismissBottomSheet = {
                    val event = FilteredStoreEvent.CloseBottomSheet
                    viewModel.sendEvent(event = event)
                },
                onSelectSortType = { sortType: StoreSortType ->
                    val event = FilteredStoreEvent.SelectStoreSortType(sortType = sortType)
                    viewModel.sendEvent(event = event)
                },
                onSelectDistanceRange = { distanceRange: StoreDistanceRange ->
                    val event =
                        FilteredStoreEvent.SelectDistanceRange(distanceRange = distanceRange)
                    viewModel.sendEvent(event = event)
                },
            )
        }
    }

}
