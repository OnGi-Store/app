package com.aloe_droid.presentation.store

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aloe_droid.presentation.base.theme.DefaultIconSize
import com.aloe_droid.presentation.base.theme.StarColor
import com.aloe_droid.presentation.store.data.StoreData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreTopBar(
    store: StoreData,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateUp: () -> Unit,
    sendToggleFavoriteEvent: () -> Unit
) {
    val (isFavorite, setFavorite) = remember(key1 = store.isLikeStore) {
        mutableStateOf(store.isLikeStore)
    }

    TopAppBar(
        windowInsets = WindowInsets(top = 0.dp),
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        navigationIcon = {
            NavigateUpIcon(navigateUp = navigateUp)
        },
        actions = {
            FavoriteIcon(isFavorite = isFavorite) {
                setFavorite(!isFavorite)
                sendToggleFavoriteEvent()
            }
        },
        title = {
            Text(
                text = store.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    )
}

@Composable
private fun NavigateUpIcon(navigateUp: () -> Unit) {
    IconButton(onClick = navigateUp) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "back"
        )
    }
}

@Composable
private fun FavoriteIcon(isFavorite: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            modifier = Modifier.size(DefaultIconSize),
            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = "like",
            tint = StarColor
        )
    }
}
