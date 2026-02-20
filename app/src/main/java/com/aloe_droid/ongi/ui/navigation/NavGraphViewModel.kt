package com.aloe_droid.ongi.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aloe_droid.ongi.ui.navigation.bottom.BottomRoute
import com.aloe_droid.ongi.ui.navigation.util.NavigatorThrottler
import com.aloe_droid.presentation.base.view.UiContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class NavGraphViewModel @Inject constructor() : ViewModel() {
    private val bottomRouteList: List<BottomRoute> = BottomRoute.DefaultBottomList
    private val initialKey: UiContract.RouteKey = BottomRoute.HomeRoute.routeKey
    private val navigatorThrottler: NavigatorThrottler = NavigatorThrottler()

    init {
        if (bottomRouteList.isEmpty()) error(message = EMPTY_LIST)
        if (bottomRouteList.none { it.routeKey == initialKey }) error(message = KEY_NOT_FOUND)
    }

    private val _state: MutableStateFlow<NavGraphInternalState> = MutableStateFlow(
        value = NavGraphInternalState(
            currentKey = initialKey,
            backStacks = bottomRouteList.associate { it.routeKey to listOf(it.routeKey) }
        )
    )

    val navState: StateFlow<NavGraphUiState> = _state.map { state: NavGraphInternalState ->
        val backStack: List<UiContract.RouteKey> =
            state.backStacks[state.currentKey] ?: error(message = BACK_STACK_NOT_FOUND)
        NavGraphUiState(
            currentKey = state.currentKey,
            currentBackStack = backStack,
            isRoot = state.currentKey == initialKey && backStack.size == 1
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = TIME_OUT),
        initialValue = NavGraphUiState(
            currentKey = initialKey,
            currentBackStack = listOf(initialKey),
            isRoot = true
        )
    )

    fun select(routeKey: UiContract.RouteKey) = tryNavigate {
        _state.update { state: NavGraphInternalState ->
            if (state.currentKey == routeKey) return@update state
            state.copy(currentKey = routeKey)
        }
    }

    fun navigate(routeKey: UiContract.RouteKey) = tryNavigate {
        _state.update { state: NavGraphInternalState ->
            val current = state.backStacks[state.currentKey] ?: return@update state
            if (current.last() == routeKey) return@update state
            state.copy(
                backStacks = state.backStacks.toMutableMap().apply {
                    this[state.currentKey] = current + routeKey
                }
            )
        }
    }

    fun popBackStack() = tryNavigate {
        _state.update { state: NavGraphInternalState -> state.popCurrentBackStack() }
    }

    fun onBack() = tryNavigate {
        _state.update { state: NavGraphInternalState ->
            val current = state.backStacks[state.currentKey] ?: return@update state
            when {
                current.size > 1 -> state.popCurrentBackStack()
                state.currentKey != initialKey -> state.copy(currentKey = initialKey)
                else -> state
            }
        }
    }

    private fun NavGraphInternalState.popCurrentBackStack(): NavGraphInternalState {
        val current = backStacks[currentKey] ?: return this
        if (current.size <= 1) return this
        return copy(
            backStacks = backStacks.toMutableMap().apply {
                this[currentKey] = current.dropLast(n = 1)
            }
        )
    }

    private inline fun tryNavigate(crossinline action: () -> Unit) =
        navigatorThrottler.execute { action() }

    companion object {
        private const val EMPTY_LIST = "bottomRouteList가 비어있습니다"
        private const val KEY_NOT_FOUND = "initialKey가 bottomRouteList에 존재하지 않습니다"
        private const val BACK_STACK_NOT_FOUND = "currentKey에 해당하는 백스택을 찾을 수 없습니다"
        private const val TIME_OUT: Long = 5_000L
    }
}

private data class NavGraphInternalState(
    val currentKey: UiContract.RouteKey,
    val backStacks: Map<UiContract.RouteKey, List<UiContract.RouteKey>>
)
