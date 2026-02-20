package com.aloe_droid.presentation.setting.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.aloe_droid.presentation.R
import com.aloe_droid.presentation.base.theme.LargeTextSize
import com.aloe_droid.presentation.base.theme.toCount
import com.aloe_droid.presentation.base.theme.toTime
import kotlin.time.Instant

@Composable
fun StoreSyncInfo(
    modifier: Modifier = Modifier,
    storeCount: Long,
    syncTime: Instant
) {
    Column(modifier = modifier) {
        val goodStore = stringResource(id = R.string.good_store)
        val updated = stringResource(id = R.string.update)
        val countText = "$goodStore: ${storeCount.toCount()}"
        val syncTime = "$updated: ${syncTime.toTime()}"

        Text(
            text = countText,
            fontSize = LargeTextSize,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = syncTime,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}
