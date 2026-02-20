package com.aloe_droid.ongi.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.aloe_droid.presentation.filtered_store.contract.FilteredStoreKey
import com.aloe_droid.presentation.filtered_store.data.StoreDistanceRange
import com.aloe_droid.presentation.filtered_store.data.StoreFilter
import com.aloe_droid.presentation.filtered_store.filteredStoreScreen
import com.aloe_droid.presentation.home.homeScreen
import com.aloe_droid.presentation.map.mapScreen
import com.aloe_droid.presentation.search.contract.SearchKey
import com.aloe_droid.presentation.search.searchScreen
import com.aloe_droid.presentation.setting.settingScreen
import com.aloe_droid.presentation.store.contract.StoreKey
import com.aloe_droid.presentation.store.storeScreen
import java.util.UUID

@Composable
fun OnGiNavDisplay(
    modifier: Modifier = Modifier,
    navGraphState: NavGraphState,
    showSnackMessage: (SnackbarVisuals) -> Unit
) {
    BackHandler(enabled = !navGraphState.isRoot, onBack = navGraphState::onBack)

    NavDisplay(
        modifier = modifier,
        backStack = navGraphState.currentBackStack,
        onBack = navGraphState::onBack,
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            homeScreen(
                showSnackMessage = showSnackMessage,
                navigateToFilteredStore = { filter: StoreFilter ->
                    navGraphState.navigate(routeKey = FilteredStoreKey(storeFilter = filter))
                },
                navigateToStore = { id: UUID ->
                    navGraphState.navigate(routeKey = StoreKey(id = id.toString()))
                }
            )

            searchScreen(
                showSnackMessage = showSnackMessage,
                navigateUp = navGraphState::popBackStack,
                navigateToStore = { id: UUID ->
                    navGraphState.navigate(routeKey = StoreKey(id = id.toString()))
                },
                navigateToFilteredStore = { query: String ->
                    navGraphState.navigate(
                        routeKey = FilteredStoreKey(
                            storeFilter = StoreFilter(
                                searchQuery = query,
                                distanceRange = StoreDistanceRange.NONE
                            )
                        )
                    )
                }
            )

            mapScreen(
                showSnackMessage = showSnackMessage,
                navigateToStore = { id: UUID ->
                    navGraphState.navigate(routeKey = StoreKey(id = id.toString()))
                }
            )

            settingScreen(
                showSnackMessage = showSnackMessage,
                navigateToFilteredStoreWithFavorite = {
                    navGraphState.navigate(
                        routeKey = FilteredStoreKey(
                            storeFilter = StoreFilter(
                                distanceRange = StoreDistanceRange.NONE,
                                onlyFavorites = true
                            )
                        )
                    )
                }
            )

            filteredStoreScreen(
                showSnackMessage = showSnackMessage,
                navigateUp = navGraphState::popBackStack,
                navigateToSearch = {
                    navGraphState.navigate(routeKey = SearchKey())
                },
                navigateToStore = { id: UUID ->
                    navGraphState.navigate(routeKey = StoreKey(id = id.toString()))
                }
            )

            storeScreen(
                showSnackMessage = showSnackMessage,
                navigateUp = navGraphState::popBackStack,
            )
        }
    )
}
