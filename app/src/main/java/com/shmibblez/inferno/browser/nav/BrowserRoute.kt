package com.shmibblez.inferno.browser.nav

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import mozilla.components.feature.addons.Addon

@SuppressLint("ParcelCreator")
@Parcelize
open class BrowserRoute() : Parcelable {

//    @Serializable
//    object ExternalBrowser : BrowserRoute

//    @SuppressLint("ParcelCreator")
    @Parcelize
    object InfernoBrowser : BrowserRoute()

    @Parcelize
    object PasswordManager : BrowserRoute() {

        @Parcelize
        object Exceptions : BrowserRoute()

    }

    @Parcelize
    object History : BrowserRoute()

    @Parcelize
    object InfernoSettings : BrowserRoute()

    @Parcelize
    object MozExtensions : BrowserRoute() {

        @Parcelize
        data class MozExtension( val addon: Addon): BrowserRoute()

    }
}