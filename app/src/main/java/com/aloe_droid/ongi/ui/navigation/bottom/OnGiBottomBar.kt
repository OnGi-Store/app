package com.aloe_droid.ongi.ui.navigation.bottom

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.aloe_droid.presentation.base.theme.BottomBarHeight
import com.aloe_droid.presentation.base.theme.ZeroDp
import com.aloe_droid.presentation.base.view.UiContract.RouteKey
import com.aloe_droid.presentation.search.contract.SearchKey

@Composable
fun OnGiBottomBar(
    bottomRouteList: List<BottomRoute>,
    currentStack: RouteKey,
    selectRoute: (RouteKey) -> Unit
) {
    val isBottomRoute: Boolean = bottomRouteList.any { it.routeKey == currentStack }
    val isSearchFromBottom: Boolean =
        currentStack is SearchKey && currentStack.isFromBottomNavigate

    if (isBottomRoute || isSearchFromBottom) {
        BottomBar(
            bottomRouteList = bottomRouteList,
            currentStack = currentStack,
            selectRoute = selectRoute
        )
    }
}

@Composable
private fun BottomBar(
    bottomRouteList: List<BottomRoute>,
    currentStack: RouteKey,
    selectRoute: (RouteKey) -> Unit,
    selectedColor: Color = MaterialTheme.colorScheme.onSurface,
    unSelectedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
) {
    NavigationBar(
        modifier = Modifier.height(BottomBarHeight),
        windowInsets = WindowInsets(bottom = ZeroDp)
    ) {
        bottomRouteList.forEach { bottomRoute: BottomRoute ->
            with(receiver = bottomRoute) {
                val isSelected: Boolean = bottomRoute.routeKey == currentStack
                val res: Int = if (isSelected) selectedIconRes else unSelectedIconRes
                val color: Color = if (isSelected) selectedColor else unSelectedColor

                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = res),
                            contentDescription = name,
                            tint = color
                        )
                    },
                    label = { Text(text = name, color = color) },
                    selected = isSelected,
                    onClick = { selectRoute(routeKey) }
                )
            }
        }
    }
}
