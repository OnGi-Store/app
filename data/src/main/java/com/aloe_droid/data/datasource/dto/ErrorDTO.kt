package com.aloe_droid.data.datasource.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ErrorDTO(
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val timeStamp: Instant
)
