package com.aloe_droid.presentation.store

import android.content.Context
import android.content.Intent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.ui.NavDisplay
import com.aloe_droid.presentation.R
import com.aloe_droid.presentation.base.component.LoadingScreen
import com.aloe_droid.presentation.base.view.BaseSnackBarVisuals
import com.aloe_droid.presentation.base.view.CollectSideEffects
import com.aloe_droid.presentation.base.view.ScreenTransition
import com.aloe_droid.presentation.base.view.UiContract
import com.aloe_droid.presentation.filtered_store.contract.FilteredStoreKey
import com.aloe_droid.presentation.map.contract.MapKey
import com.aloe_droid.presentation.store.contract.StoreEffect
import com.aloe_droid.presentation.store.contract.StoreEvent
import com.aloe_droid.presentation.store.contract.StoreKey
import com.aloe_droid.presentation.store.contract.StoreUiData
import com.aloe_droid.presentation.store.contract.StoreUiState
import com.aloe_droid.presentation.store.data.AddressData

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<UiContract.RouteKey>.storeScreen(
    showSnackMessage: (SnackbarVisuals) -> Unit,
    navigateUp: () -> Unit,
) = entry<StoreKey>(
    clazzContentKey = { it },
    metadata = NavDisplay.transitionSpec {
        val isFromMap: Boolean = initialState.key is MapKey
        if (isFromMap) {
            ScreenTransition.fadeInAnim() togetherWith ScreenTransition.fadeOutAnim()
        } else {
            ScreenTransition.slideInFromRight() togetherWith ScreenTransition.slideOutToLeft()
        }
    } + NavDisplay.popTransitionSpec {
        val isToFilteredStore: Boolean = targetState.key is FilteredStoreKey
        if (isToFilteredStore) {
            ScreenTransition.slideInFromLeft() togetherWith ScreenTransition.slideOutToRight()
        } else {
            EnterTransition.None togetherWith ExitTransition.None
        }
    }
) { key: StoreKey ->
    val context: Context = LocalContext.current
    val viewModel: StoreViewModel =
        hiltViewModel { factory: StoreViewModel.Factory -> factory.create(key = key) }
    val uiState: StoreUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiData: StoreUiData by viewModel.uiData.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    CollectSideEffects(effectFlow = viewModel.uiEffect) { sideEffect: StoreEffect ->
        when (sideEffect) {
            is StoreEffect.ShowErrorMessage -> {
                val snackBarVisuals = BaseSnackBarVisuals(message = sideEffect.message)
                showSnackMessage(snackBarVisuals)
            }

            is StoreEffect.PopUpWithMessage -> {
                val snackBarVisuals = BaseSnackBarVisuals(message = sideEffect.message)
                showSnackMessage(snackBarVisuals)
                navigateUp()
            }

            is StoreEffect.MoveToCall -> {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = "tel:${sideEffect.phone}".toUri()
                }
                context.startActivity(intent)
            }

            is StoreEffect.MoveToMap -> {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = sideEffect.address.toUri()
                }
                context.startActivity(intent)
            }

        }
    }

    Screen(
        uiState = uiState,
        uiData = uiData,
        viewModel = viewModel,
        scrollBehavior = scrollBehavior,
        navigateUp = navigateUp,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Screen(
    uiState: StoreUiState,
    uiData: StoreUiData,
    viewModel: StoreViewModel,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateUp: () -> Unit,
) {
    if (uiState.isInitialState) {
        LoadingScreen(content = stringResource(id = R.string.loading))
    } else if (uiData.store != null) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(connection = scrollBehavior.nestedScrollConnection),
            topBar = {
                StoreTopBar(
                    store = uiData.store,
                    navigateUp = navigateUp,
                    scrollBehavior = scrollBehavior,
                    sendToggleFavoriteEvent = {
                        val event: StoreEvent = StoreEvent.ToggleFavorite
                        viewModel.sendEvent(event = event)
                    }
                )
            }
        ) { paddingValues: PaddingValues ->
            StoreScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                storeData = uiData.store,
                onCallClick = { phone: String ->
                    val event = StoreEvent.CallEvent(phone = phone)
                    viewModel.sendEvent(event = event)
                },
                onAddressClick = { address: AddressData ->
                    val event = StoreEvent.MapEvent(address = address)
                    viewModel.sendEvent(event = event)
                }
            )
        }

    } else {
        val message: String = stringResource(id = R.string.cant_find_store)
        val event: StoreEvent = StoreEvent.CantFindStoreEvent(message = message)
        viewModel.sendEvent(event = event)
    }
}
