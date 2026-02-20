package com.aloe_droid.presentation.filtered_store.contract

import com.aloe_droid.presentation.base.view.UiContract
import com.aloe_droid.presentation.filtered_store.data.StoreFilter
import kotlinx.parcelize.Parcelize

@Parcelize
data class FilteredStoreKey(
    val storeFilter: StoreFilter,
) : UiContract.RouteKey
