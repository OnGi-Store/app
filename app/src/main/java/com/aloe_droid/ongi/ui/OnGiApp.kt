package com.aloe_droid.ongi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aloe_droid.ongi.ui.navigation.NavGraphUiState
import com.aloe_droid.ongi.ui.navigation.NavGraphViewModel
import com.aloe_droid.ongi.ui.navigation.OnGiNavDisplay
import com.aloe_droid.ongi.ui.navigation.bottom.BottomRoute
import com.aloe_droid.ongi.ui.navigation.bottom.OnGiBottomBar
import com.aloe_droid.presentation.base.view.UiContract
import kotlinx.coroutines.launch

@Composable
fun OnGiApp(navGraphViewModel: NavGraphViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val navState: NavGraphUiState by navGraphViewModel.navState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .navigationBarsPadding(),
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
        bottomBar = {
            OnGiBottomBar(
                bottomRouteList = BottomRoute.DefaultBottomList,
                currentStack = navState.currentBackStack.last(),
                selectRoute = { routeKey: UiContract.RouteKey ->
                    navGraphViewModel.select(routeKey = routeKey)
                }
            )
        }
    ) { innerPadding: PaddingValues ->
        OnGiNavDisplay(
            modifier = Modifier.padding(paddingValues = innerPadding),
            navGraphState = navState,
            navigate = navGraphViewModel::navigate,
            onBack = navGraphViewModel::onBack,
            popBackStack = navGraphViewModel::popBackStack,
            showSnackMessage = { snackBarVisuals: SnackbarVisuals ->
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(visuals = snackBarVisuals)
                }
            }
        )
    }
}
