package com.aloe_droid.presentation.map.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import com.aloe_droid.presentation.R
import com.aloe_droid.presentation.base.component.PulsingCircularIndicator
import com.aloe_droid.presentation.base.theme.DefaultImageRatio
import com.aloe_droid.presentation.base.theme.DefaultTextSize
import com.aloe_droid.presentation.base.theme.LargePadding
import com.aloe_droid.presentation.base.theme.LargeTextSize
import com.aloe_droid.presentation.base.theme.SemiLargePadding
import com.aloe_droid.presentation.base.theme.SmallPadding
import com.aloe_droid.presentation.base.theme.TitleTextHeight
import com.aloe_droid.presentation.base.theme.toFavorite
import com.aloe_droid.presentation.map.data.StoreMapData

@Composable
fun SelectedStoreContent(
    modifier: Modifier = Modifier,
    selectedStore: StoreMapData,
) {
    Column(modifier = modifier) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratio = DefaultImageRatio),
            model = selectedStore.imageUrl,
            contentDescription = selectedStore.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.store_image_placeholder),
            error = painterResource(id = R.drawable.store_image_placeholder),
        )

        Spacer(modifier = Modifier.height(height = SmallPadding))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SemiLargePadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(9f),
                text = selectedStore.name,
                fontSize = LargeTextSize,
                fontWeight = FontWeight.SemiBold,
                lineHeight = TitleTextHeight
            )

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.star_24px),
                    contentDescription = stringResource(id = R.string.favorite),
                    tint = Color.Yellow
                )

                Text(
                    modifier = Modifier.padding(start = SmallPadding),
                    text = selectedStore.favoriteCount.toFavorite(),
                    fontSize = DefaultTextSize
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = LargePadding),
            horizontalArrangement = Arrangement.Center
        ) {
            PulsingCircularIndicator(
                primaryColor = MaterialTheme.colorScheme.primary,
                secondaryColor = MaterialTheme.colorScheme.secondary,
                backgroundColor = MaterialTheme.colorScheme.surface,
            )
        }
    }
}
