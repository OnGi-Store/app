package com.aloe_droid.presentation.filtered_store

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.aloe_droid.domain.entity.Store
import com.aloe_droid.domain.usecase.GetFilteredStoreUseCase
import com.aloe_droid.presentation.base.view.BaseViewModel
import com.aloe_droid.presentation.filtered_store.contract.FilteredStoreEffect
import com.aloe_droid.presentation.filtered_store.contract.FilteredStoreEvent
import com.aloe_droid.presentation.filtered_store.contract.FilteredStoreKey
import com.aloe_droid.presentation.filtered_store.contract.FilteredStoreUiState
import com.aloe_droid.presentation.filtered_store.data.StoreDistanceRange
import com.aloe_droid.presentation.filtered_store.data.StoreSortType
import com.aloe_droid.presentation.home.data.StoreData
import com.aloe_droid.presentation.home.data.StoreData.Companion.toStoreData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@HiltViewModel(assistedFactory = FilteredStoreViewModel.Factory::class)
@OptIn(ExperimentalCoroutinesApi::class)
class FilteredStoreViewModel @AssistedInject constructor(
    @Assisted private val navKey: FilteredStoreKey,
    private val getFilteredStoreUseCase: GetFilteredStoreUseCase
) : BaseViewModel<FilteredStoreKey, FilteredStoreUiState, FilteredStoreEvent, FilteredStoreEffect>(
    key = navKey
) {

    val pagingDataFlow: Flow<PagingData<StoreData>> by lazy {
        uiState.distinctUntilChangedBy { it.storeFilter }
            .flatMapLatest { filterState: FilteredStoreUiState ->
                getFilteredStoreUseCase(
                    category = filterState.storeFilter.category.toStoreQueryCategory(),
                    sortType = filterState.storeFilter.sortType.toStoreQuerySortType(),
                    distanceRange = filterState.storeFilter.distanceRange.toStoreQueryDistance(),
                    searchQuery = filterState.storeFilter.searchQuery,
                    onlyFavorites = filterState.storeFilter.onlyFavorites
                )
            }.map { pagingData: PagingData<Store> ->
                pagingData.map { store: Store ->
                    store.toStoreData()
                }
            }.onEach {
                updateState { uiState: FilteredStoreUiState ->
                    uiState.copy(isRefresh = false)
                }
            }.cachedIn(viewModelScope)
    }

    override fun initState(routeKey: FilteredStoreKey): FilteredStoreUiState {
        return FilteredStoreUiState(storeFilter = routeKey.storeFilter)
    }

    override fun handleEvent(event: FilteredStoreEvent) {
        when (event) {
            FilteredStoreEvent.RefreshEvent -> handleRefresh()
            is FilteredStoreEvent.SelectStore -> handleNavigateToStore(event.storeData)
            FilteredStoreEvent.ShowOrderBottomSheet -> handleShowOrderBottomSheet()
            FilteredStoreEvent.ShowDistanceBottomSheet -> handleShowDistanceBottomSheet()
            FilteredStoreEvent.CloseBottomSheet -> handleCloseBottomSheet()
            is FilteredStoreEvent.SelectStoreSortType -> handleSelectStoreSortType(event.sortType)
            is FilteredStoreEvent.SelectDistanceRange -> handleSelectStoreDistanceRange(event.distanceRange)
        }
    }

    override fun handleError(throwable: Throwable) {
        super.handleError(throwable)

        throwable.message?.let { message: String ->
            showErrorMessage(message = message)
        }
    }

    private fun handleRefresh() = viewModelScope.safeLaunch {
        updateState { uiState: FilteredStoreUiState ->
            uiState.copy(isRefresh = true)
        }
    }

    private fun handleNavigateToStore(storeData: StoreData) {
        val effect = FilteredStoreEffect.NavigateStore(id = storeData.id)
        sendSideEffect(uiEffect = effect)
    }

    private fun handleShowOrderBottomSheet() {
        updateState { uiState: FilteredStoreUiState ->
            uiState.copy(isShowOrderBottomSheet = true, isShowDistanceBottomSheet = false)
        }
    }

    private fun handleShowDistanceBottomSheet() {
        updateState { uiState: FilteredStoreUiState ->
            uiState.copy(isShowOrderBottomSheet = false, isShowDistanceBottomSheet = true)
        }
    }

    private fun handleCloseBottomSheet() {
        updateState { uiState: FilteredStoreUiState ->
            uiState.copy(isShowOrderBottomSheet = false, isShowDistanceBottomSheet = false)
        }
    }

    private fun handleSelectStoreSortType(sortType: StoreSortType) {
        if (sortType == currentState.storeFilter.sortType) {
            updateState { uiState: FilteredStoreUiState ->
                uiState.copy(isShowOrderBottomSheet = false)
            }
            return
        }

        updateState { uiState: FilteredStoreUiState ->
            val storeFilter = uiState.storeFilter.copy(sortType = sortType)
            uiState.copy(
                isRefresh = true,
                isShowOrderBottomSheet = false,
                storeFilter = storeFilter
            )
        }

        val effect = FilteredStoreEffect.ScrollToFirstPosition
        sendSideEffect(uiEffect = effect)
    }

    private fun handleSelectStoreDistanceRange(distanceRange: StoreDistanceRange) {
        if (distanceRange == currentState.storeFilter.distanceRange) {
            updateState { uiState: FilteredStoreUiState ->
                uiState.copy(isShowDistanceBottomSheet = false)
            }
            return
        }

        updateState { uiState: FilteredStoreUiState ->
            val storeFilter = uiState.storeFilter.copy(distanceRange = distanceRange)
            uiState.copy(
                isRefresh = true,
                isShowDistanceBottomSheet = false,
                storeFilter = storeFilter
            )
        }

        val effect = FilteredStoreEffect.ScrollToFirstPosition
        sendSideEffect(uiEffect = effect)
    }

    private fun showErrorMessage(message: String) {
        val effect: FilteredStoreEffect =
            FilteredStoreEffect.ShowErrorMessage(message = message)
        sendSideEffect(uiEffect = effect)
    }

    @AssistedFactory
    interface Factory {
        fun create(key: FilteredStoreKey): FilteredStoreViewModel
    }
}
