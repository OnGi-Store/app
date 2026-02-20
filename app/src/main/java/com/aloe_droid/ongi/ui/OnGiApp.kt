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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.aloe_droid.ongi.ui.navigation.OnGiNavDisplay
import com.aloe_droid.ongi.ui.navigation.bottom.BottomRoute
import com.aloe_droid.ongi.ui.navigation.NavGraphState
import com.aloe_droid.ongi.ui.navigation.bottom.OnGiBottomBar
import com.aloe_droid.ongi.ui.navigation.rememberNavGraphState
import com.aloe_droid.presentation.base.view.UiContract
import kotlinx.coroutines.launch

@Composable
fun OnGiApp() {
    val navGraphState: NavGraphState = rememberNavGraphState()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

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
                currentStack = navGraphState.currentBackStack.last(),
                selectRoute = { routeKey: UiContract.RouteKey ->
                    navGraphState.select(routeKey = routeKey)
                }
            )
        }
    ) { innerPadding: PaddingValues ->
        OnGiNavDisplay(
            modifier = Modifier.padding(paddingValues = innerPadding),
            navGraphState = navGraphState,
            showSnackMessage = { snackBarVisuals: SnackbarVisuals ->
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(visuals = snackBarVisuals)
                }
            }
        )
    }
}
