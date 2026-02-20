package com.aloe_droid.ongi.ui.navigation.bottom

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavBackStack
import com.aloe_droid.ongi.R
import com.aloe_droid.presentation.base.view.UiContract
import com.aloe_droid.presentation.home.contract.HomeKey
import com.aloe_droid.presentation.map.contract.MapKey
import com.aloe_droid.presentation.search.contract.SearchKey
import com.aloe_droid.presentation.setting.contract.SettingKey

@Stable
data class BottomRoute(
    val name: String,
    val routeKey: UiContract.RouteKey,
    @param:DrawableRes val selectedIconRes: Int,
    @param:DrawableRes val unSelectedIconRes: Int,
    val backStack: NavBackStack<UiContract.RouteKey> = NavBackStack(routeKey)
) {
    companion object {
        val HomeRoute = BottomRoute(
            name = "홈",
            routeKey = HomeKey,
            selectedIconRes = R.drawable.grid_view_fill_24px,
            unSelectedIconRes = R.drawable.grid_view_24px,
        )

        private val SearchRoute = BottomRoute(
            name = "검색",
            routeKey = SearchKey(isFromBottomNavigate = true),
            selectedIconRes = R.drawable.search_fill_24px,
            unSelectedIconRes = R.drawable.search_24px
        )

        private val MapRoute = BottomRoute(
            name = "지도",
            routeKey = MapKey,
            selectedIconRes = R.drawable.map_fill_24px,
            unSelectedIconRes = R.drawable.map_24px
        )

        private val SettingRoute = BottomRoute(
            name = "설정",
            routeKey = SettingKey,
            selectedIconRes = R.drawable.settings_fill_24px,
            unSelectedIconRes = R.drawable.settings_24px
        )

        val DefaultBottomList: List<BottomRoute> = listOf(
            HomeRoute,
            SearchRoute,
            MapRoute,
            SettingRoute
        )
    }
}
