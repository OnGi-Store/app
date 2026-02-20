package com.aloe_droid.presentation.setting

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.ui.NavDisplay
import com.aloe_droid.presentation.BuildConfig
import com.aloe_droid.presentation.R
import com.aloe_droid.presentation.base.component.LoadingScreen
import com.aloe_droid.presentation.base.view.BaseSnackBarVisuals
import com.aloe_droid.presentation.base.view.CollectSideEffects
import com.aloe_droid.presentation.base.view.ScreenTransition
import com.aloe_droid.presentation.base.view.UiContract
import com.aloe_droid.presentation.filtered_store.contract.FilteredStoreKey
import com.aloe_droid.presentation.setting.contract.SettingEffect
import com.aloe_droid.presentation.setting.contract.SettingEvent
import com.aloe_droid.presentation.setting.contract.SettingKey
import com.aloe_droid.presentation.setting.contract.SettingUiData
import com.aloe_droid.presentation.setting.contract.SettingUiState

fun EntryProviderScope<UiContract.RouteKey>.settingScreen(
    showSnackMessage: (SnackbarVisuals) -> Unit,
    navigateToFilteredStoreWithFavorite: () -> Unit,
) = entry<SettingKey>(
    clazzContentKey = { it },
    metadata = NavDisplay.transitionSpec {
        ScreenTransition.slideInFromRight() togetherWith ScreenTransition.slideOutToLeft()
    } + NavDisplay.popTransitionSpec {
        val isFromFilteredStore: Boolean = initialState.key is FilteredStoreKey
        if (isFromFilteredStore) {
            ScreenTransition.slideInFromRight() togetherWith ExitTransition.None
        } else {
            EnterTransition.None togetherWith ExitTransition.None
        }
    }
) { key: SettingKey ->
    val context: Context = LocalContext.current
    val viewModel: SettingViewModel =
        hiltViewModel { factory: SettingViewModel.Factory -> factory.create(key = key) }
    val uiState: SettingUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiData: SettingUiData by viewModel.uiData.collectAsStateWithLifecycle()

    CollectSideEffects(effectFlow = viewModel.uiEffect) { sideEffect: SettingEffect ->
        when (sideEffect) {
            is SettingEffect.ShowErrorMessage -> {
                val snackBarVisuals = BaseSnackBarVisuals(message = sideEffect.message)
                showSnackMessage(snackBarVisuals)
            }

            SettingEffect.NavigateToFilteredStore -> {
                navigateToFilteredStoreWithFavorite()
            }

            SettingEffect.MoveToPrivacyPolicy -> {
                CustomTabsIntent.Builder()
                    .build()
                    .launchUrl(context, BuildConfig.PRIAVACY_SECURITY.toUri())
            }

            SettingEffect.MoveToInQueryToDeveloper -> {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:${BuildConfig.EMAIL}".toUri()
                }
                context.startActivity(intent)
            }

            SettingEffect.MoveToLocationAuth -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${context.packageName}".toUri()
                }
                context.startActivity(intent)
            }
        }
    }

    if (uiState.isInitialState) {
        LoadingScreen(content = stringResource(id = R.string.loading))
    } else {
        SettingScreen(
            storeCount = uiData.storeCount,
            syncTime = uiData.syncTime,
            onClickFavoriteStore = {
                val event = SettingEvent.ClickFavoriteStore
                viewModel.sendEvent(event = event)
            },
            onClickPrivacyPolicy = {
                val event = SettingEvent.ClickPrivacyPolicy
                viewModel.sendEvent(event = event)
            },
            onClickInQueryToDeveloper = {
                val event = SettingEvent.ClickInquiryToDeveloper
                viewModel.sendEvent(event = event)
            },
            onClickLocationAuth = {
                val event = SettingEvent.ClickLocationAuth
                viewModel.sendEvent(event = event)
            }
        )
    }
}
