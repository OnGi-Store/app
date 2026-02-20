package com.aloe_droid.presentation.filtered_store.data

import com.aloe_droid.domain.entity.StoreQuerySortType
import com.aloe_droid.presentation.R
import kotlinx.serialization.Serializable

@Serializable
enum class StoreSortType {
    NAME,
    FAVORITE,
    DISTANCE;

    fun getNameRes(): Int = when (this) {
        NAME -> R.string.sort_by_name
        FAVORITE -> R.string.sort_by_favorite
        DISTANCE -> R.string.sort_by_distance
    }

    fun toStoreQuerySortType(): StoreQuerySortType = when (this) {
        NAME -> StoreQuerySortType.NAME
        FAVORITE -> StoreQuerySortType.FAVORITE
        DISTANCE -> StoreQuerySortType.DISTANCE
    }
}
