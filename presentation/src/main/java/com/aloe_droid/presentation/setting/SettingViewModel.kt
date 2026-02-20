package com.aloe_droid.presentation.setting

import com.aloe_droid.domain.entity.StoreSyncEntity
import com.aloe_droid.domain.usecase.GetStoreSyncInfoUseCase
import com.aloe_droid.presentation.base.view.BaseViewModel
import com.aloe_droid.presentation.setting.contract.SettingEffect
import com.aloe_droid.presentation.setting.contract.SettingEvent
import com.aloe_droid.presentation.setting.contract.SettingKey
import com.aloe_droid.presentation.setting.contract.SettingUiData
import com.aloe_droid.presentation.setting.contract.SettingUiData.Companion.toSettingData
import com.aloe_droid.presentation.setting.contract.SettingUiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@HiltViewModel(assistedFactory = SettingViewModel.Factory::class)
class SettingViewModel @AssistedInject constructor(
    @Assisted private val navKey: SettingKey,
    private val getStoreSyncInfoUseCase: GetStoreSyncInfoUseCase,
) : BaseViewModel<SettingKey, SettingUiState, SettingEvent, SettingEffect>(key = navKey) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiData: StateFlow<SettingUiData> by lazy {
        uiState
            .flatMapLatest { getStoreSyncInfoUseCase().handleError() }
            .onEach { completeInitialState() }
            .map { storeSyncEntity: StoreSyncEntity -> storeSyncEntity.toSettingData() }
            .toViewModelState(initValue = SettingUiData())
    }

    override fun initState(routeKey: SettingKey): SettingUiState = SettingUiState()

    override fun handleEvent(event: SettingEvent) {
        when (event) {
            SettingEvent.ClickFavoriteStore -> handleNavigateToFilteredStore()
            SettingEvent.ClickPrivacyPolicy -> handleMoveToPrivacyPolicy()
            SettingEvent.ClickInquiryToDeveloper -> handleMoveToInQueryToDeveloper()
            SettingEvent.ClickLocationAuth -> handleMoveToLocationAuth()
        }
    }

    override fun handleError(throwable: Throwable) {
        super.handleError(throwable)
        updateState { state: SettingUiState ->
            state.copy(isInitialState = false)
        }

        throwable.message?.let { message: String ->
            showErrorMessage(message = message)
        }
    }

    private fun completeInitialState() {
        updateState { uiState: SettingUiState ->
            uiState.copy(isInitialState = false)
        }
    }

    private fun handleNavigateToFilteredStore() {
        val effect = SettingEffect.NavigateToFilteredStore
        sendSideEffect(uiEffect = effect)
    }

    private fun handleMoveToPrivacyPolicy() {
        val effect = SettingEffect.MoveToPrivacyPolicy
        sendSideEffect(uiEffect = effect)
    }

    private fun handleMoveToInQueryToDeveloper() {
        val effect = SettingEffect.MoveToInQueryToDeveloper
        sendSideEffect(uiEffect = effect)
    }

    private fun handleMoveToLocationAuth() {
        val effect = SettingEffect.MoveToLocationAuth
        sendSideEffect(uiEffect = effect)
    }

    private fun showErrorMessage(message: String) {
        val effect: SettingEffect = SettingEffect.ShowErrorMessage(message = message)
        sendSideEffect(uiEffect = effect)
    }

    @AssistedFactory
    interface Factory {
        fun create(key: SettingKey): SettingViewModel
    }
}
