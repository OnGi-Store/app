package com.aloe_droid.presentation.filtered_store.data

import com.aloe_droid.domain.entity.StoreQueryDistance
import kotlinx.serialization.Serializable

@Serializable
enum class StoreDistanceRange {
    M_5,
    K_1,
    K_3,
    K_5,
    K_10,
    K_15,
    NONE;

    fun getKm(): Double = when (this) {
        M_5 -> 0.5
        K_1 -> 1.0
        K_3 -> 3.0
        K_5 -> 5.0
        K_10 -> 10.0
        K_15 -> 15.0
        NONE -> -1.0
    }

    fun toStoreQueryDistance(): StoreQueryDistance = when (this) {
        M_5 -> StoreQueryDistance.M_5
        K_1 -> StoreQueryDistance.K_1
        K_3 -> StoreQueryDistance.K_3
        K_5 -> StoreQueryDistance.K_5
        K_10 -> StoreQueryDistance.K_10
        K_15 -> StoreQueryDistance.K_15
        NONE -> StoreQueryDistance.NONE
    }
}
