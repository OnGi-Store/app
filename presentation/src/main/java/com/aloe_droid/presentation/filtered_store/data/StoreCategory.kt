package com.aloe_droid.presentation.filtered_store.data

import com.aloe_droid.domain.entity.StoreQueryCategory
import com.aloe_droid.presentation.R
import kotlinx.serialization.Serializable

@Serializable
enum class StoreCategory {
    KOREAN_FOOD,
    WESTERN_FOOD,
    JAPANESE_FOOD,
    CHINESE_FOOD,
    BAKERY,
    RESTAURANT,
    BATH_HOUSE,
    LAUNDRY,
    HOTEL,
    HAIR_SALON,
    ETC,
    NONE;

    fun getNameRes(): Int = when (this) {
        KOREAN_FOOD -> R.string.category_korean
        WESTERN_FOOD -> R.string.category_western
        JAPANESE_FOOD -> R.string.category_japanese
        CHINESE_FOOD -> R.string.category_chinese
        BAKERY -> R.string.category_bakery
        RESTAURANT -> R.string.category_restaurant
        BATH_HOUSE -> R.string.category_bath
        LAUNDRY -> R.string.category_laundry
        HOTEL -> R.string.category_hotel
        HAIR_SALON -> R.string.category_hair
        ETC -> R.string.category_etc
        NONE -> R.string.none
    }

    fun toStoreQueryCategory(): StoreQueryCategory = when (this) {
        KOREAN_FOOD -> StoreQueryCategory.KOREAN_FOOD
        WESTERN_FOOD -> StoreQueryCategory.WESTERN_FOOD
        JAPANESE_FOOD -> StoreQueryCategory.JAPANESE_FOOD
        CHINESE_FOOD -> StoreQueryCategory.CHINESE_FOOD
        BAKERY -> StoreQueryCategory.BAKERY
        RESTAURANT -> StoreQueryCategory.RESTAURANT
        BATH_HOUSE -> StoreQueryCategory.BATH_HOUSE
        LAUNDRY -> StoreQueryCategory.LAUNDRY
        HOTEL -> StoreQueryCategory.HOTEL
        HAIR_SALON -> StoreQueryCategory.HAIR_SALON
        ETC -> StoreQueryCategory.ETC
        NONE -> StoreQueryCategory.NONE
    }
}
