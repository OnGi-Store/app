package com.aloe_droid.presentation.base.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseViewModel<UiKey : UiContract.RouteKey, UiState : UiContract.State, UiEvent : UiContract.Event, UiEffect : UiContract.SideEffect>(
    val key: UiKey
) : ViewModel() {

    private val _uiState by lazy { MutableStateFlow(value = initState(routeKey = key)) }
    val uiState by lazy { _uiState.asStateFlow() }
    val currentState: UiState
        get() = uiState.value

    private val _uiEffect: Channel<UiEffect> = Channel(Channel.BUFFERED)
    val uiEffect: Flow<UiEffect> = _uiEffect.receiveAsFlow()

    fun sendEvent(event: UiEvent) = runCatching { handleEvent(event) }
        .onFailure { throwable: Throwable -> handleError(throwable) }

    protected fun sendSideEffect(uiEffect: UiEffect) {
        val result: ChannelResult<Unit> = _uiEffect.trySend(uiEffect)
        if (result.isFailure) Timber.e(result.exceptionOrNull())
    }

    protected fun updateState(function: (UiState) -> UiState) {
        _uiState.update(function = function)
    }

    protected open fun <Result> CoroutineScope.safeLaunch(
        onSuccess: (Result) -> Unit = {},
        block: suspend () -> Result
    ): Job = viewModelScope.launch {
        runCatching { block() }
            .onSuccess { result: Result -> onSuccess(result) }
            .onFailure { throwable: Throwable -> handleError(throwable = throwable) }
    }

    protected open fun <T> Flow<T>.handleError(): Flow<T> = catch { throwable: Throwable ->
        handleError(throwable = throwable)
    }

    protected open fun handleError(throwable: Throwable) {
        Timber.e(t = throwable)
    }

    protected fun <T> Flow<T>.toViewModelState(initValue: T) = stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = TIME_OUT),
        initialValue = initValue
    )

    abstract fun initState(routeKey: UiKey): UiState

    abstract fun handleEvent(event: UiEvent)

    companion object {

        protected const val DEFAULT_DEBOUNCE: Long = 300L
        protected const val TIME_OUT: Long = 5_000L
    }
}
