package com.aloe_droid.ongi.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.aloe_droid.ongi.ui.navigation.bottom.BottomRoute
import com.aloe_droid.presentation.base.view.UiContract

@Stable
class NavGraphState(
    bottomRouteList: List<BottomRoute>,
    private val initialKey: UiContract.RouteKey,
    private val navigatorThrottler: NavigatorThrottler = NavigatorThrottler()
) {

    init {
        if (bottomRouteList.isEmpty()) error(message = EMPTY_LIST)
        if (bottomRouteList.none { it.routeKey == initialKey }) error(message = KEY_NOT_FOUND)
    }

    private val backStacks: Map<UiContract.RouteKey, SnapshotStateList<UiContract.RouteKey>> =
        bottomRouteList.associate { route ->
            route.routeKey to mutableStateListOf(route.routeKey)
        }

    var currentKey: UiContract.RouteKey by mutableStateOf(value = initialKey)
        private set

    val currentBackStack: SnapshotStateList<UiContract.RouteKey>
        get() = backStacks[currentKey] ?: error(message = BACK_STACK_NOT_FOUND)

    val isRoot: Boolean
        get() = currentBackStack.size == 1 && currentKey == initialKey

    fun select(routeKey: UiContract.RouteKey) = tryNavigate {
        if (currentKey == routeKey) return@tryNavigate
        currentKey = routeKey
    }

    fun navigate(routeKey: UiContract.RouteKey) = tryNavigate {
        if (currentBackStack.last() == routeKey) return@tryNavigate
        currentBackStack.add(routeKey)
    }

    fun popBackStack() = tryNavigate {
        if (currentBackStack.size > 1) {
            currentBackStack.removeLastOrNull()
        }
    }

    fun onBack() = tryNavigate {
        when {
            currentBackStack.size > 1 -> {
                currentBackStack.removeLastOrNull()
            }

            currentKey != initialKey -> {
                currentKey = initialKey
            }
        }
    }

    private inline fun tryNavigate(crossinline action: () -> Unit) = navigatorThrottler.execute {
        action()
    }

    companion object {
        private const val EMPTY_LIST = "bottomRouteList가 비어있습니다"
        private const val KEY_NOT_FOUND = "initialKey가 bottomRouteList에 존재하지 않습니다"
        private const val BACK_STACK_NOT_FOUND = "currentKey에 해당하는 백스택을 찾을 수 없습니다"
    }
}

// TODO: Configuration 전환 시에도 유지 되려면 ViewModel 관리가 필요
@Composable
fun rememberNavGraphState(
    bottomRouteList: List<BottomRoute> = BottomRoute.DefaultBottomList,
    initialKey: UiContract.RouteKey = BottomRoute.HomeRoute.routeKey,
): NavGraphState =
    remember { NavGraphState(bottomRouteList = bottomRouteList, initialKey = initialKey) }
