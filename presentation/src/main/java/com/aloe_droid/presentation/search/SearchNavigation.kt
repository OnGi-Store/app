package com.aloe_droid.presentation.search

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.ui.NavDisplay
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.aloe_droid.domain.entity.SearchHistory
import com.aloe_droid.presentation.R
import com.aloe_droid.presentation.base.component.LoadingScreen
import com.aloe_droid.presentation.base.view.BaseSnackBarVisuals
import com.aloe_droid.presentation.base.view.CollectSideEffects
import com.aloe_droid.presentation.base.view.ScreenTransition
import com.aloe_droid.presentation.base.view.UiContract
import com.aloe_droid.presentation.filtered_store.contract.FilteredStoreKey
import com.aloe_droid.presentation.home.contract.HomeKey
import com.aloe_droid.presentation.search.contract.SearchEffect
import com.aloe_droid.presentation.search.contract.SearchEvent
import com.aloe_droid.presentation.search.contract.SearchKey
import com.aloe_droid.presentation.search.contract.SearchUiState
import com.aloe_droid.presentation.search.data.SearchedStore
import java.util.UUID

fun EntryProviderScope<UiContract.RouteKey>.searchScreen(
    showSnackMessage: (SnackbarVisuals) -> Unit,
    navigateToStore: (UUID) -> Unit,
    navigateToFilteredStore: (String) -> Unit,
    navigateUp: () -> Unit,
) = entry<SearchKey>(
    clazzContentKey = { it },
    metadata = NavDisplay.transitionSpec {
        val isFromLeftTab: Boolean = initialState.key is HomeKey
        val isFromFilteredStore: Boolean = initialState.key is FilteredStoreKey
        if (isFromLeftTab) {
            ScreenTransition.slideInFromRight() togetherWith ScreenTransition.slideOutToLeft()
        } else if (isFromFilteredStore) {
            ScreenTransition.fadeInAnim() togetherWith ScreenTransition.fadeOutAnim()
        } else {
            ScreenTransition.slideInFromLeft() togetherWith ScreenTransition.slideOutToRight()
        }
    } + NavDisplay.popTransitionSpec {
        val isFromFilteredStore: Boolean = initialState.key is FilteredStoreKey
        if (isFromFilteredStore) {
            ScreenTransition.fadeInAnim() togetherWith ScreenTransition.fadeOutAnim()
        } else {
            EnterTransition.None togetherWith ExitTransition.None
        }
    }
) { key: SearchKey ->
    val viewModel: SearchViewModel =
        hiltViewModel { factory: SearchViewModel.Factory -> factory.create(key = key) }
    val uiState: SearchUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val storeItems: LazyPagingItems<SearchedStore> = viewModel.queryResult
        .collectAsLazyPagingItems()
    val historyItems: LazyPagingItems<SearchHistory> = viewModel.searchHistory
        .collectAsLazyPagingItems()

    CollectSideEffects(effectFlow = viewModel.uiEffect) { sideEffect: SearchEffect ->
        when (sideEffect) {
            is SearchEffect.ShowErrorMessage -> {
                val snackBarVisuals = BaseSnackBarVisuals(message = sideEffect.message)
                showSnackMessage(snackBarVisuals)
            }

            is SearchEffect.SelectStore -> {
                navigateToStore(sideEffect.storeId)
            }

            is SearchEffect.NavigateToFilteredStoreList -> {
                navigateToFilteredStore(sideEffect.query)
            }

            is SearchEffect.NavigateUp -> {
                navigateUp()
            }
        }
    }

    if (uiState.isInitialState) {
        viewModel.sendEvent(event = SearchEvent.LoadEvent)
        LoadingScreen(content = stringResource(id = R.string.loading))
    } else {
        SearchScreen(
            canGoUp = !key.isFromBottomNavigate,
            storeItems = storeItems,
            historyItems = historyItems,
            query = uiState.searchQuery,
            navigateUp = {
                val event = SearchEvent.NavigateUpEvent
                viewModel.sendEvent(event = event)
            },
            onQueryChange = { query: String ->
                val event = SearchEvent.ChangeQuery(query = query)
                viewModel.sendEvent(event = event)
            },
            onSearch = { query: String ->
                if (query.isBlank()) return@SearchScreen
                val event = SearchEvent.SearchQuery(query = query)
                viewModel.sendEvent(event = event)
            },
            selectStore = { store: SearchedStore ->
                val event = SearchEvent.SelectStore(store = store)
                viewModel.sendEvent(event = event)
            },
            deleteHistory = { historyId: Long ->
                val event = SearchEvent.DeleteQuery(id = historyId)
                viewModel.sendEvent(event = event)
            },
            deleteAllHistory = {
                val event = SearchEvent.DeleteAllQuery
                viewModel.sendEvent(event = event)
            }
        )
    }
}
