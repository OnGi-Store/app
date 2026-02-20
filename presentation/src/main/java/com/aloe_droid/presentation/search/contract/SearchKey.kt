package com.aloe_droid.presentation.search.contract

import com.aloe_droid.presentation.base.view.UiContract
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchKey(val isFromBottomNavigate: Boolean = false) : UiContract.RouteKey
