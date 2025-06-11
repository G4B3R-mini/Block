package com.shmibblez.inferno.browser.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.shmibblez.inferno.browser.BrowserComponent
import com.shmibblez.inferno.browser.getActivity
import com.shmibblez.inferno.browser.state.rememberBrowserComponentState
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.history.InfernoHistoryPage
import com.shmibblez.inferno.settings.extensions.ExtensionPage
import com.shmibblez.inferno.settings.extensions.ExtensionsPage
import com.shmibblez.inferno.settings.nav.SettingsNavHost
import com.shmibblez.inferno.settings.passwords.PasswordExceptionSettingsPage
import com.shmibblez.inferno.settings.passwords.PasswordSettingsPage
import mozilla.components.feature.addons.Addon

@Composable
fun BrowserNavHost(
    customTabSessionId: String?,
    initialAction: InitialBrowserTask? = null,
    startDestination: BrowserRoute = initialAction?.asStartDestination()
        ?: BrowserRoute.InfernoBrowser,
) {
    val nav = rememberNavController()

    val browserComponentState by rememberBrowserComponentState(
        customTabSessionId = customTabSessionId,
        activity = LocalContext.current.getActivity()!!,
    )

    val context = LocalContext.current

    LaunchedEffect(initialAction) {
        // set nav host for request interceptor
        context.components.core.requestInterceptor.setNavigationController(nav)

        when (initialAction) {
            InitialBrowserTask.AppIcon -> {}
            is InitialBrowserTask.ExternalApp -> {}
            InitialBrowserTask.OpenPasswordManager -> {
                nav.navigate(route = BrowserRoute.PasswordManager)
            }

            is InitialBrowserTask.OpenToBrowser -> {
//                if (initialAction.private) {
//                    // todo:
////                    browserComponentState.switchToPrivate()
//                } else {
//                    // todo:
////                    browserComponentState.switchToNormal()
//                }
            }

            is InitialBrowserTask.OpenToBrowserAndLoad -> {
                // todo: tab should already be selected as currentTab
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

            null -> {}
        }
    }

    NavHost(
        navController = nav,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize(),
//        contentAlignment = ,
        enterTransition = {
            slideIntoContainer(
                animationSpec = tween(
                    300,
                    easing = EaseOut,
                ),
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
            )
        },
        exitTransition = {
            slideOutOfContainer(
                animationSpec = tween(
                    300,
                    easing = EaseOut,
                ),
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                animationSpec = tween(
                    300,
                    easing = EaseIn,
                ),
                towards = AnimatedContentTransitionScope.SlideDirection.End,
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                animationSpec = tween(
                    300,
                    easing = EaseIn,
                ),
                towards = AnimatedContentTransitionScope.SlideDirection.End,
            )
        },
//        sizeTransform = ,
    ) {

        composable<BrowserRoute.InfernoBrowser> {
            BrowserComponent(
                navController = nav,
                state = browserComponentState,
                onNavToHistory = { nav.navigate(route = BrowserRoute.History) },
                onNavToSettings = { nav.navigate(route = BrowserRoute.InfernoSettings) },
                onNavToExtensions = { nav.navigate(route = BrowserRoute.MozExtensions) },
            )
        }

        composable<BrowserRoute.PasswordManager> {
            PasswordSettingsPage(
                goBack = { nav.popBackStack() },
                onNavToPasswordExceptionSettingsPage = {
                    nav.navigate(route = BrowserRoute.PasswordManager.Exceptions)
                },
            )
        }

        composable<BrowserRoute.PasswordManager.Exceptions> {
            PasswordExceptionSettingsPage(goBack = { nav.popBackStack() })
        }

        composable<BrowserRoute.History> {
            InfernoHistoryPage(goBack = { nav.popBackStack() })
        }

        composable<BrowserRoute.InfernoSettings> {
            SettingsNavHost(goBackLegacy = { nav.popBackStack() })
        }

        composable<BrowserRoute.MozExtensions> {
            ExtensionsPage(
                goBack = { nav.popBackStack() },
                onNavToAddon = { nav.navigate(route = BrowserRoute.MozExtensions.MozExtension(addon = it)) },
                onNavToBrowser = { nav.popBackStack() },
            )
        }

        composable<BrowserRoute.MozExtensions.MozExtension> { backStackEntry ->
            val addon: Addon = backStackEntry.toRoute()
            ExtensionPage(addon = addon, goBack = { nav.popBackStack() })
        }
    }
}
