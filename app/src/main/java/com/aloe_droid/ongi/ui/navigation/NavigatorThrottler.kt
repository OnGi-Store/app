package com.aloe_droid.ongi.ui.navigation

import androidx.compose.runtime.Stable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

@Stable
class NavigatorThrottler(private val duration: Duration = NAVIGATION_TIME.milliseconds) {
    private var lastMark: TimeSource.Monotonic.ValueTimeMark =
        TimeSource.Monotonic.markNow().minus(duration = duration)

    fun execute(action: () -> Unit) {
        val now = TimeSource.Monotonic.markNow()
        if (now - lastMark >= duration) {
            lastMark = now
            action()
        }
    }

    companion object {
        private const val NAVIGATION_TIME = 500
    }
}
