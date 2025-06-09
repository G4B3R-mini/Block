package com.shmibblez.inferno.browser.nav

import kotlinx.serialization.Serializable

interface BrowserRoute {

//    @Serializable
//    object ExternalBrowser : BrowserRoute

    @Serializable
    object InfernoBrowser : BrowserRoute

    @Serializable
    object PasswordManager : BrowserRoute {

        @Serializable
        object Exceptions : BrowserRoute

    }

    @Serializable
    object History : BrowserRoute

    @Serializable
    object InfernoSettings : BrowserRoute

    @Serializable
    object MozExtensions : BrowserRoute

}