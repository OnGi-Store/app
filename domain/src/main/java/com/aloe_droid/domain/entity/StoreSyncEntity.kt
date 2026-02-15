package com.aloe_droid.domain.entity

import kotlin.time.Instant

data class StoreSyncEntity(
    val storeCount: Long,
    val syncTime: Instant
)
