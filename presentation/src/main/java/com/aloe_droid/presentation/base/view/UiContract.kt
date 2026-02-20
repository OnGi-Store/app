package com.aloe_droid.presentation.base.view

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey

interface UiContract {
    interface RouteKey: NavKey, Parcelable
    interface State
    interface Event
    interface SideEffect
}
