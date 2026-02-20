package com.aloe_droid.presentation.home

import com.aloe_droid.domain.entity.HomeEntity
import com.aloe_droid.domain.exception.LocationPermissionException
import com.aloe_droid.domain.usecase.GetHomeInfoUseCase
import com.aloe_droid.presentation.base.view.BaseViewModel
import com.aloe_droid.presentation.filtered_store.data.StoreDistanceRange
import com.aloe_droid.presentation.filtered_store.data.StoreFilter
import com.aloe_droid.presentation.filtered_store.data.StoreSortType
import com.aloe_droid.presentation.home.contract.HomeEffect
import com.aloe_droid.presentation.home.contract.HomeEvent
import com.aloe_droid.presentation.home.contract.HomeKey
import com.aloe_droid.presentation.home.contract.HomeUiData
import com.aloe_droid.presentation.home.contract.HomeUiData.Companion.toHomeData
import com.aloe_droid.presentation.home.contract.HomeUiState
import com.aloe_droid.presentation.home.data.StoreData
import com.google.android.gms.common.api.ResolvableApiException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@HiltViewModel(assistedFactory = HomeViewModel.Factory::class)
class HomeViewModel @AssistedInject constructor(
    @Assisted private val navKey: HomeKey,
    private val getHomeInfoUseCase: GetHomeInfoUseCase
) : BaseViewModel<HomeKey, HomeUiState, HomeEvent, HomeEffect>(key = navKey) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiData: StateFlow<HomeUiData> by lazy {
        uiState.map { state: HomeUiState -> state.shouldFetchHomeData }
            .distinctUntilChanged()
            .filter { shouldFetchHomeData: Boolean -> shouldFetchHomeData }
            .flatMapLatest { getHomeInfoUseCase().handleError() }
            .onEach { homeEntity: HomeEntity -> checkEntity(homeEntity) }
            .map { homeEntity: HomeEntity -> homeEntity.toHomeData() }
            .toViewModelState(initValue = HomeUiData())
    }

    override fun initState(routeKey: HomeKey): HomeUiState = HomeUiState()

    override fun handleEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.RefreshEvent -> handleRefresh()
            is HomeEvent.SelectBannerEvent -> handleSelectBanner(event.bannerData.url)
            HomeEvent.LocationRetry -> handleRetry()
            is HomeEvent.SelectStore -> handleSelectStore(event.storeData)
            is HomeEvent.LocationSkip -> handlePermissionSkip(event.skipMessage)
            is HomeEvent.SelectCategoryEvent -> handleSelectStores {
                copy(category = event.categoryData.storeCategory)
            }

            HomeEvent.SelectNearbyStoreListEvent -> handleSelectStores {
                copy(sortType = StoreSortType.DISTANCE)
            }

            HomeEvent.SelectFavoriteStoreListEvent -> handleSelectStores {
                copy(
                    sortType = StoreSortType.FAVORITE,
                    distanceRange = StoreDistanceRange.NONE
                )
            }
        }
    }

    override fun handleError(throwable: Throwable) {
        super.handleError(throwable)
        updateState { state: HomeUiState ->
            state.copy(isInitialState = false, isRefreshing = false)
        }

        throwable.message?.let { message: String ->
            showErrorMessage(message = message)
        }
    }

    private fun handleRefresh() {
        updateState { state: HomeUiState ->
            state.copy(
                isRefreshing = true,
                isNeedPermission = false
            )
        }
    }

    private fun handleSelectStores(set: StoreFilter.() -> StoreFilter) {
        val storeFilter: StoreFilter = StoreFilter().set()
        val effect: HomeEffect = HomeEffect.NavigateStoreList(filter = storeFilter)
        sendSideEffect(uiEffect = effect)
    }

    private fun handleSelectBanner(url: String) {
        val effect: HomeEffect = HomeEffect.ShowBrowser(url = url)
        sendSideEffect(uiEffect = effect)
    }

    private fun handleSelectStore(storeData: StoreData) {
        val effect: HomeEffect = HomeEffect.NavigateStore(id = storeData.id)
        sendSideEffect(uiEffect = effect)
    }

    private fun checkEntity(homeEntity: HomeEntity) {
        updateState { uiState ->
            uiState.copy(isInitialState = false, isRefreshing = false)
        }

        if (homeEntity.location.isDefault) handleLocationError(homeEntity.location.error)
    }

    private fun handleLocationError(throwable: Throwable?) = when (throwable) {
        is ResolvableApiException -> handleNeedGPS(throwable = throwable)
        is LocationPermissionException -> handleNeedPermission(throwable = throwable)
        else -> Timber.e(throwable)
    }

    private fun handleNeedPermission(throwable: Throwable) {
        Timber.e(throwable)

        updateState { state: HomeUiState ->
            state.copy(isNeedPermission = true, isRefreshing = false)
        }
    }

    private fun handleNeedGPS(throwable: ResolvableApiException) {
        updateState { state: HomeUiState ->
            state.copy(gpsError = throwable, isRefreshing = false)
        }
    }

    private fun handleRetry() {
        updateState { state: HomeUiState ->
            state.copy(isNeedPermission = false, gpsError = null, isInitialState = true)
        }
    }

    private fun handlePermissionSkip(skipMessage: String) {
        showErrorMessage(skipMessage)

        updateState { state: HomeUiState ->
            state.copy(isNeedPermission = false, gpsError = null)
        }
    }

    private fun showErrorMessage(message: String) {
        val effect: HomeEffect = HomeEffect.ShowErrorMessage(message = message)
        sendSideEffect(uiEffect = effect)
    }

    @AssistedFactory
    interface Factory {
        fun create(key: HomeKey): HomeViewModel
    }
}
