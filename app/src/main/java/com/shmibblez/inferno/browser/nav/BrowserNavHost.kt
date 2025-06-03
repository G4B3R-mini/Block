package com.shmibblez.inferno.browser.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shmibblez.inferno.browser.BrowserComponent
import com.shmibblez.inferno.browser.rememberBrowserComponentState
import com.shmibblez.inferno.library.history.HistoryFragment
import com.shmibblez.inferno.settings.nav.SettingsNavHost
import kotlinx.serialization.Serializable

/**
 * actions from intent handlers
 */
sealed interface InitialBrowserTask : java.io.Serializable {

    data class ExternalApp(val url: String, val private: Boolean = false) : InitialBrowserTask,
        java.io.Serializable

    data class OpenToBrowser(val private: Boolean = false) : InitialBrowserTask,
        java.io.Serializable

    data class OpenToBrowserAndLoad(val url: String, val private: Boolean = false) :
        InitialBrowserTask, java.io.Serializable

    data class OpenToSearch(val private: Boolean = false) : InitialBrowserTask, java.io.Serializable

    data object PrivateBrowsingMode : InitialBrowserTask, java.io.Serializable {
        private fun readResolve(): Any = PrivateBrowsingMode
    }

    data object StartInRecentsScreen : InitialBrowserTask, java.io.Serializable {
        private fun readResolve(): Any = StartInRecentsScreen
    }

    data object OpenPasswordManager : InitialBrowserTask, java.io.Serializable {
        private fun readResolve(): Any = OpenPasswordManager
    }

    data object AppIcon : InitialBrowserTask, java.io.Serializable {
        private fun readResolve(): Any = AppIcon
    }

    fun asStartDestination(): BrowserRoute {
        return when (this) {
            AppIcon -> BrowserRoute.InfernoBrowser
            is ExternalApp -> BrowserRoute.ExternalBrowser
            OpenPasswordManager -> BrowserRoute.InfernoBrowser
            is OpenToBrowser -> BrowserRoute.InfernoBrowser
            is OpenToBrowserAndLoad -> BrowserRoute.InfernoBrowser
            is OpenToSearch -> BrowserRoute.InfernoBrowser
            PrivateBrowsingMode -> BrowserRoute.InfernoBrowser
            StartInRecentsScreen -> BrowserRoute.InfernoBrowser
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

interface BrowserRoute {

    @Serializable
    object ExternalBrowser : BrowserRoute

    @Serializable
    object InfernoBrowser : BrowserRoute

    @Serializable
    object PasswordManager : BrowserRoute

    @Serializable
    object History : BrowserRoute

    @Serializable
    object InfernoSettings : BrowserRoute

}

@Composable
fun BrowserNavHost(
    initialAction: InitialBrowserTask? = null,
    startDestination: BrowserRoute = initialAction?.asStartDestination()
        ?: BrowserRoute.InfernoBrowser,
) {
    val nav = rememberNavController()
    val browserComponentState by rememberBrowserComponentState()

    LaunchedEffect(initialAction) {
        when (initialAction) {
            null -> {}
//            else -> {
//                throw NoWhenBranchMatchedException("unimplemented InitialBrowserTask: $initialAction")
//            }
            InitialBrowserTask.AppIcon -> {}
            is InitialBrowserTask.ExternalApp -> {}
            InitialBrowserTask.OpenPasswordManager -> {
                nav.navigate(route = BrowserRoute.PasswordManager)
            }

            is InitialBrowserTask.OpenToBrowser -> {
                if (initialAction.private) {
                    // todo:
//                    browserComponentState.switchToPrivate()
                } else {
                    // todo:
//                    browserComponentState.switchToNormal()
                }
            }

            is InitialBrowserTask.OpenToBrowserAndLoad -> {
                // todo: tab should already be selected as currentTab, maybe just make private if not private
//                browserComponentState.loadUrl(url = initialAction.url, private = initialAction.private)
            }

            is InitialBrowserTask.OpenToSearch -> {
                // todo: open new tab and focus toolbar address bar
//                browserComponentState.beginSearch(private = initialAction.private)
            }

            InitialBrowserTask.PrivateBrowsingMode -> {
                // todo:
//                browserComponentState.switchToPrivate()
            }

            InitialBrowserTask.StartInRecentsScreen -> {
                // todo:
//                browserComponentState.goToRecents()
            }
        }
    }

    NavHost(
        navController = nav,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize(),
//        contentAlignment = ,
//        enterTransition = ,
//        exitTransition = ,
//        popEnterTransition = ,
//        popExitTransition = ,
//        sizeTransform = ,
    ) {
        composable<BrowserRoute.InfernoBrowser> {
            BrowserComponent(
                navController = nav,
                state = browserComponentState,
                onNavToSettings = { nav.navigate(route = BrowserRoute.InfernoSettings) },
            )
        }

        composable<BrowserRoute.InfernoSettings> {
            SettingsNavHost(goBackLegacy = { nav.popBackStack() })
        }

        composable<BrowserRoute.History> {
            /**
             * todo: implement [HistoryFragment]
             */
        }
    }
}
