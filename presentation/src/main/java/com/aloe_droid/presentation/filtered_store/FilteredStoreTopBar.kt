package com.aloe_droid.presentation.filtered_store

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aloe_droid.presentation.R
import com.aloe_droid.presentation.filtered_store.data.StoreFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilteredStoreTopBar(
    storeFilter: StoreFilter,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateUp: () -> Unit,
    navigateToSearch: () -> Unit,
) {
    val title = with(storeFilter) {
        if (onlyFavorites) stringResource(id = R.string.favorite)
        else searchQuery.ifBlank { stringResource(id = category.getNameRes()) }
    }

    FilteredStoreTopBar(
        title = title,
        scrollBehavior = scrollBehavior,
        navigateUp = navigateUp,
        navigateToSearch = navigateToSearch
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FilteredStoreTopBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateUp: () -> Unit,
    navigateToSearch: () -> Unit
) {
    TopAppBar(
        windowInsets = WindowInsets(top = 0.dp),
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        navigationIcon = {
            IconButton(onClick = navigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기"
                )
            }
        },
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        actions = {
            IconButton(onClick = navigateToSearch) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "검색"
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun FilteredStoreTopBarPreview() {
    FilteredStoreTopBar(
        title = "일식",
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
        navigateUp = {},
        navigateToSearch = {}
    )
}
