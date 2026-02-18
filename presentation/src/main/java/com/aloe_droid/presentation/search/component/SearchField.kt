package com.aloe_droid.presentation.search.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults.InputField
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aloe_droid.presentation.R
import com.aloe_droid.presentation.base.theme.MinIconSize
import com.aloe_droid.presentation.base.theme.SmallCornerRadius

private const val WIDTH = 2
private const val ALPHA = 0.3f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchField(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    expanded: Boolean,
    setExpanded: (Boolean) -> Unit,
    onSearch: (String) -> Unit,
    navigateUp: () -> Unit
) {
    InputField(
        modifier = modifier.border(
            width = WIDTH.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = ALPHA),
            shape = RoundedCornerShape(SmallCornerRadius)
        ),
        colors = inputFieldColors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        expanded = expanded,
        onExpandedChange = setExpanded,
        placeholder = { Text(stringResource(id = R.string.search_plz)) },
        trailingIcon = {
            IconButton(onClick = { onSearch(query) }) {
                Icon(
                    modifier = Modifier.size(MinIconSize),
                    imageVector = Icons.Outlined.Search,
                    contentDescription = query
                )
            }
        },
        leadingIcon = {
            IconButton(onClick = navigateUp) {
                Icon(
                    modifier = Modifier.size(MinIconSize),
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "뒤로가기"
                )
            }
        }
    )
}
