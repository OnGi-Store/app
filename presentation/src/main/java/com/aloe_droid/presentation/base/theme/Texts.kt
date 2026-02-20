package com.aloe_droid.presentation.base.theme

import android.icu.text.SimpleDateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aloe_droid.presentation.R
import com.aloe_droid.presentation.filtered_store.data.StoreDistanceRange
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Date
import java.util.Locale
import kotlin.time.Instant

fun Double.toDistance(): String = String.format(Locale.getDefault(), "%.1fkm", this)

fun Int.toFavorite(): String = when {
    this < 10 -> this.toString()
    this < 100 -> "10+"
    this < 1_000 -> "100+"
    else -> "1000+"
}

fun Long.toTimeStamp(): String {
    val date = Date(this)
    val formatter = SimpleDateFormat("MM.dd", Locale.getDefault())
    return formatter.format(date)
}

fun String.toWon(): String {
    val regex = Regex("\\d+")
    val price: String = regex.replace(this) { matchResult ->
        val number = matchResult.value.toLongOrNull() ?: matchResult.value
        "%,d".format(number)
    }

    val currencySuffix = if (Locale.getDefault().language == "ko") "원" else "won"
    return "${price}${currencySuffix}"
}

fun Long.toCount(): String {
    val formatted = "%,d".format(this)
    val suffix = if (Locale.getDefault().language == "ko") "개" else "items"
    return "$formatted$suffix"
}

fun Instant.toTime(): String {
    val localTimeZone: TimeZone = TimeZone.currentSystemDefault()
    val localTime: LocalDateTime = toLocalDateTime(localTimeZone)
    return "${localTime.date}"
}

@Composable
fun StoreDistanceRange.toDistanceString(): String {
    return if (this == StoreDistanceRange.NONE) {
        stringResource(id = R.string.max_range)
    } else {
        "${this.getKm()}km"
    }
}

@Composable
fun StoreDistanceRange.toSelectDistanceString(): String {
    return if (this == StoreDistanceRange.NONE) {
        this.toDistanceString()
    } else {
        "${stringResource(id = R.string.select_range)} ${this.toDistanceString()}"
    }
}

