package com.aloe_droid.presentation.filtered_store

import androidx.compose.animation.togetherWith
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.ui.NavDisplay
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.aloe_droid.presentation.R
import com.aloe_droid.presentation.base.component.LoadingScreen
import com.aloe_droid.presentation.base.view.BaseSnackBarVisuals
import com.aloe_droid.presentation.base.view.CollectSideEffects
import com.aloe_droid.presentation.base.view.ScreenTransition
import com.aloe_droid.presentation.base.view.UiContract
import com.aloe_droid.presentation.filtered_store.contract.FilteredStoreEffect
import com.aloe_droid.presentation.filtered_store.contract.FilteredStoreEvent
import com.aloe_droid.presentation.filtered_store.contract.FilteredStoreKey
import com.aloe_droid.presentation.filtered_store.contract.FilteredStoreUiState
import com.aloe_droid.presentation.filtered_store.data.StoreDistanceRange
import com.aloe_droid.presentation.filtered_store.data.StoreSortType
import com.aloe_droid.presentation.home.data.StoreData
import com.aloe_droid.presentation.search.contract.SearchKey
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<UiContract.RouteKey>.filteredStoreScreen(
    showSnackMessage: (SnackbarVisuals) -> Unit,
    navigateUp: () -> Unit,
    navigateToStore: (UUID) -> Unit,
    navigateToSearch: () -> Unit
) = entry<FilteredStoreKey>(
    clazzContentKey = { it },
    metadata = NavDisplay.transitionSpec {
        val isFromSearch: Boolean = initialState.key is SearchKey
        if (isFromSearch) {
            ScreenTransition.fadeInAnim() togetherWith ScreenTransition.fadeOutAnim()
        } else {
            ScreenTransition.slideInFromRight() togetherWith ScreenTransition.slideOutToLeft()
        }
    } + NavDisplay.popTransitionSpec {
        val isToSearch: Boolean = targetState.key is SearchKey
        if (isToSearch) {
            ScreenTransition.fadeInAnim() togetherWith ScreenTransition.fadeOutAnim()
        } else {
            ScreenTransition.slideInFromLeft() togetherWith ScreenTransition.slideOutToRight()
        }
    }
) { key: FilteredStoreKey ->
    val viewModel: FilteredStoreViewModel =
        hiltViewModel { factory: FilteredStoreViewModel.Factory -> factory.create(key = key) }
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
