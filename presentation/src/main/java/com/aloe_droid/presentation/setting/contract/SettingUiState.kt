package com.aloe_droid.presentation.setting.contract

import androidx.compose.runtime.Stable
import com.aloe_droid.domain.entity.StoreSyncEntity
import com.aloe_droid.presentation.base.view.UiContract
import kotlin.time.Instant

@Stable
data class SettingUiState(
    val isInitialState: Boolean = true
) : UiContract.State

@Stable
data class SettingUiData(
    val storeCount: Long = 0L,
    val syncTime: Instant = Instant.fromEpochMilliseconds(0L)
) {
    companion object {
        fun StoreSyncEntity.toSettingData() = SettingUiData(
            storeCount = storeCount,
            syncTime = syncTime
        )
    }
}
