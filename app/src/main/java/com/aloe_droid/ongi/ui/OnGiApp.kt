package com.aloe_droid.ongi.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aloe_droid.ongi.ui.navigation.NavUtil.Companion.safeMove
import com.aloe_droid.ongi.ui.navigation.OnGiNavHost
import com.aloe_droid.ongi.ui.navigation.bottom.BottomRoute
import com.aloe_droid.ongi.ui.navigation.bottom.OnGiBottomBar
import com.aloe_droid.presentation.base.view.UiContract
import com.aloe_droid.presentation.home.contract.Home
import kotlinx.coroutines.launch

@Composable
fun OnGiApp(navController: NavHostController = rememberNavController()) {
    val backStackEntry: NavBackStackEntry? by navController.currentBackStackEntryAsState()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
        bottomBar = {
            OnGiBottomBar(
                bottomRouteList = BottomRoute.DefaultBottomList,
                backStackEntry = backStackEntry,
                selectRoute = { route: UiContract.Route ->
                    navController.safeMove {
                        navigate(route) {
                            popUpTo(graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding: PaddingValues ->
        OnGiNavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            startRoute = Home,
            showSnackMessage = { snackBarVisuals: SnackbarVisuals ->
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(visuals = snackBarVisuals)
                }
            }
        )
    }
}
