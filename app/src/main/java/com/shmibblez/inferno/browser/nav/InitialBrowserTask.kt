package com.shmibblez.inferno.browser.nav

/**
 * actions from intent handlers
 */
sealed class InitialBrowserTask : java.io.Serializable {

    data class ExternalApp(val tabId: String, val private: Boolean = false) : InitialBrowserTask(),
        java.io.Serializable

    data class OpenToBrowser(val private: Boolean = false) : InitialBrowserTask(),
        java.io.Serializable

    data class OpenToBrowserAndLoad(val url: String, val private: Boolean = false) :
        InitialBrowserTask(), java.io.Serializable

    data class OpenToSearch(val private: Boolean = false) : InitialBrowserTask(),
        java.io.Serializable

    data object PrivateBrowsingMode : InitialBrowserTask(), java.io.Serializable {
        private fun readResolve(): Any = PrivateBrowsingMode
    }

    data object StartInRecentsScreen : InitialBrowserTask(), java.io.Serializable {
        private fun readResolve(): Any = StartInRecentsScreen
    }

    data object OpenPasswordManager : InitialBrowserTask(), java.io.Serializable {
        private fun readResolve(): Any = OpenPasswordManager
    }

    data object AppIcon : InitialBrowserTask(), java.io.Serializable {
        private fun readResolve(): Any = AppIcon
    }

    fun InitialBrowserTask?.asStartDestination(): BrowserRoute {
        return when (this) {
            AppIcon -> BrowserRoute.InfernoBrowser
            is ExternalApp -> BrowserRoute.InfernoBrowser // BrowserRoute.ExternalBrowser
            OpenPasswordManager -> BrowserRoute.InfernoBrowser
            is OpenToBrowser -> BrowserRoute.InfernoBrowser
            is OpenToBrowserAndLoad -> BrowserRoute.InfernoBrowser
            is OpenToSearch -> BrowserRoute.InfernoBrowser
            PrivateBrowsingMode -> BrowserRoute.InfernoBrowser
            StartInRecentsScreen -> BrowserRoute.InfernoBrowser
            null -> BrowserRoute.InfernoBrowser
        }
    }

//    const val OPEN_TO_BROWSER = "open_to_browser"
//    const val OPEN_TO_BROWSER_AND_LOAD = "open_to_browser_and_load"
//    const val OPEN_TO_SEARCH = "open_to_search"
//    const val PRIVATE_BROWSING_MODE = "private_browsing_mode"
//    const val START_IN_RECENTS_SCREEN = "start_in_recents_screen"
//    const val OPEN_PASSWORD_MANAGER = "open_password_manager"
//    const val APP_ICON = "APP_ICON"

}