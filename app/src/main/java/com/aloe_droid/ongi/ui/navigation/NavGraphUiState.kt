package com.aloe_droid.ongi.ui.navigation

import androidx.compose.runtime.Stable
import com.aloe_droid.presentation.base.view.UiContract

@Stable
data class NavGraphUiState(
    val currentKey: UiContract.RouteKey,
    val currentBackStack: List<UiContract.RouteKey>,
    val isRoot: Boolean
)
