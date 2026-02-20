package com.aloe_droid.presentation.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.aloe_droid.presentation.base.theme.DefaultPadding
import com.aloe_droid.presentation.base.theme.LargePadding
import com.aloe_droid.presentation.base.theme.SemiLargePadding
import com.aloe_droid.presentation.setting.component.SettingButtons
import com.aloe_droid.presentation.setting.component.StoreSyncInfo
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun SettingScreen(
    storeCount: Long,
    syncTime: Instant,
    onClickFavoriteStore: () -> Unit,
    onClickPrivacyPolicy: () -> Unit,
    onClickInQueryToDeveloper: () -> Unit,
    onClickLocationAuth: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SemiLargePadding)
    ) {
        StoreSyncInfo(
            modifier = Modifier.padding(top = DefaultPadding, bottom = LargePadding),
            storeCount = storeCount,
            syncTime = syncTime
        )

        SettingButtons(
            onClickFavoriteStore = onClickFavoriteStore,
            onClickPrivacyPolicy = onClickPrivacyPolicy,
            onClickInQueryToDeveloper = onClickInQueryToDeveloper,
            onClickLocationAuth = onClickLocationAuth
        )
    }
}

@Composable
@Preview
fun SettingScreenPreview() {
    SettingScreen(
        storeCount = 25565,
        syncTime = Clock.System.now(),
        onClickFavoriteStore = {},
        onClickPrivacyPolicy = {},
        onClickInQueryToDeveloper = {},
        onClickLocationAuth = {}
    )
}
