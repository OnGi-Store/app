package com.aloe_droid.presentation.search

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.aloe_droid.domain.entity.SearchHistory
import com.aloe_droid.domain.entity.Store
import com.aloe_droid.domain.usecase.DeleteSearchHistoryUseCase
import com.aloe_droid.domain.usecase.GetFilteredStoreUseCase
import com.aloe_droid.domain.usecase.GetSearchHistoryUseCase
import com.aloe_droid.domain.usecase.InsertSearchHistoryUseCase
import com.aloe_droid.presentation.base.view.BaseViewModel
import com.aloe_droid.presentation.search.contract.SearchEffect
import com.aloe_droid.presentation.search.contract.SearchEvent
import com.aloe_droid.presentation.search.contract.SearchKey
import com.aloe_droid.presentation.search.contract.SearchUiState
import com.aloe_droid.presentation.search.data.SearchedStore
import com.aloe_droid.presentation.search.data.SearchedStore.Companion.toPagingSearchStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel(assistedFactory = SearchViewModel.Factory::class)
class SearchViewModel @AssistedInject constructor(
    @Assisted private val navKey: SearchKey,
    getFilteredStoreUseCase: GetFilteredStoreUseCase,
    getSearchHistoryUseCase: GetSearchHistoryUseCase,
    private val insertSearchHistoryUseCase: InsertSearchHistoryUseCase,
    private val deleteSearchHistoryUseCase: DeleteSearchHistoryUseCase
) : BaseViewModel<SearchKey, SearchUiState, SearchEvent, SearchEffect>(key = navKey) {

    val queryResult: StateFlow<PagingData<SearchedStore>> by lazy {
        uiState.debounce { DEFAULT_DEBOUNCE }
            .map { it.searchQuery }
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.isBlank()) flowOf(value = PagingData.empty())
                else getFilteredStoreUseCase(searchQuery = query).handleError()
            }.map { pagingData: PagingData<Store> -> pagingData.toPagingSearchStore() }
            .cachedIn(viewModelScope)
            .toViewModelState(initValue = PagingData.empty())
    }

    val searchHistory: StateFlow<PagingData<SearchHistory>> by lazy {
        uiState
            .distinctUntilChangedBy { it.isInitialState }
            .flatMapLatest { getSearchHistoryUseCase().handleError() }
            .cachedIn(viewModelScope)
            .toViewModelState(initValue = PagingData.empty())
    }

    override fun initState(routeKey: SearchKey): SearchUiState = SearchUiState()

    override fun handleEvent(event: SearchEvent) {
        when (event) {
            SearchEvent.LoadEvent -> handleLoad()
            SearchEvent.NavigateUpEvent -> handleNavigateUp()
            SearchEvent.DeleteAllQuery -> handleDeleteAllHistory()
            is SearchEvent.ChangeQuery -> handleChangeQuery(event.query)
            is SearchEvent.SearchQuery -> handleSearchQuery(event.query)
            is SearchEvent.DeleteQuery -> handleDeleteHistory(event.id)
            is SearchEvent.SelectStore -> handleSelectStore(event.store)
        }
    }

    override fun handleError(throwable: Throwable) {
        super.handleError(throwable)
        updateState { state: SearchUiState ->
            state.copy(isInitialState = false)
        }

        throwable.message?.let { message: String ->
            showErrorMessage(message = message)
        }
    }

    private fun handleLoad() = viewModelScope.safeLaunch {
        updateState { uiState: SearchUiState ->
            uiState.copy(isInitialState = false)
        }
    }

    private fun handleNavigateUp() {
        val effect: SearchEffect = SearchEffect.NavigateUp
        sendSideEffect(uiEffect = effect)
    }

    private fun handleDeleteAllHistory() = viewModelScope.safeLaunch {
        deleteSearchHistoryUseCase()
    }

    private fun handleChangeQuery(query: String) {
        updateState { uiState: SearchUiState ->
            uiState.copy(searchQuery = query)
        }
    }

    private fun handleSearchQuery(query: String) = viewModelScope.safeLaunch {
        insertSearchHistoryUseCase(keyword = query)
        updateState { uiState: SearchUiState ->
            uiState.copy(searchQuery = query)
        }

        val effect: SearchEffect = SearchEffect.NavigateToFilteredStoreList(query = query)
        sendSideEffect(uiEffect = effect)
    }

    private fun handleDeleteHistory(historyId: Long) = viewModelScope.safeLaunch {
        deleteSearchHistoryUseCase(historyId = historyId)
    }

    private fun handleSelectStore(store: SearchedStore) = viewModelScope.safeLaunch {
        insertSearchHistoryUseCase(keyword = store.name)

        val effect: SearchEffect = SearchEffect.SelectStore(storeId = store.id)
        sendSideEffect(uiEffect = effect)
    }

    private fun showErrorMessage(message: String) {
        val effect: SearchEffect = SearchEffect.ShowErrorMessage(message = message)
        sendSideEffect(uiEffect = effect)
    }

    @AssistedFactory
    interface Factory {
        fun create(key: SearchKey): SearchViewModel
    }
}
