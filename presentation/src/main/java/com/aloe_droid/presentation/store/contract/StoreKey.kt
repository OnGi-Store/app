package com.aloe_droid.presentation.store.contract

import com.aloe_droid.presentation.base.view.UiContract
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoreKey(val id: String) : UiContract.RouteKey
