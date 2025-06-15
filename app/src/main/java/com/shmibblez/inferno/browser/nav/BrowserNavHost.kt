package com.shmibblez.inferno.browser.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shmibblez.inferno.biometric.BiometricPromptCallbackManager
import com.shmibblez.inferno.browser.BrowserComponent
import com.shmibblez.inferno.browser.nav.InitialBrowserTask.AppIcon.asStartDestination
import com.shmibblez.inferno.browser.state.BrowserComponentState
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.extension.WebExtensionPromptFeature
import com.shmibblez.inferno.history.InfernoHistoryPage
import com.shmibblez.inferno.settings.accessibility.AccessibilitySettingsPage
import com.shmibblez.inferno.settings.account.AccountProblemSettingsPage
import com.shmibblez.inferno.settings.account.AccountSettingsPage
import com.shmibblez.inferno.settings.account.TurnOnSyncSettingsPage
import com.shmibblez.inferno.settings.autofill.AutofillSettingsPage
import com.shmibblez.inferno.settings.extensions.ExtensionPage
import com.shmibblez.inferno.settings.extensions.ExtensionsPage
import com.shmibblez.inferno.settings.gesture.GestureSettingsPage
import com.shmibblez.inferno.settings.home.HomePageSettingsPage
import com.shmibblez.inferno.settings.locale.LocaleSettingsPage
import com.shmibblez.inferno.settings.onQuit.OnQuitSettingsPage
import com.shmibblez.inferno.settings.passwords.PasswordExceptionSettingsPage
import com.shmibblez.inferno.settings.passwords.PasswordSettingsPage
import com.shmibblez.inferno.settings.privacyAndSecurity.PrivacyAndSecuritySettingsPage
import com.shmibblez.inferno.settings.search.SearchSettingsPage
import com.shmibblez.inferno.settings.settings.SettingsPage
import com.shmibblez.inferno.settings.sitepermissions.SitePermissionsExceptionsSettingsPage
import com.shmibblez.inferno.settings.sitepermissions.SitePermissionsSettingsPage
import com.shmibblez.inferno.settings.tabs.TabSettingsPage
import com.shmibblez.inferno.settings.theme.ThemeSettingsPage
import com.shmibblez.inferno.settings.toolbar.ToolbarSettingsPage
import com.shmibblez.inferno.settings.translation.AutomaticTranslationSettingsPage
import com.shmibblez.inferno.settings.translation.DownloadTranslationLanguagesSettingsPage
import com.shmibblez.inferno.settings.translation.TranslationExceptionsSettingsPage
import com.shmibblez.inferno.settings.translation.TranslationSettingsPage
import mozilla.components.feature.addons.Addon

@Composable
fun BrowserNavHost(
    browserComponentState: BrowserComponentState,
    biometricPromptCallbackManager: BiometricPromptCallbackManager,
//    customTabSessionId: String?,
    initialAction: InitialBrowserTask? = null,
    startDestination: BrowserRoute = initialAction.asStartDestination(),
) {
    val nav = rememberNavController()

//    val browserComponentState by rememberBrowserComponentState(
//        customTabSessionId = customTabSessionId,
//        activity = LocalContext.current.getActivity()!!,
//    )
    /**
     * todo: implement [WebExtensionPromptFeature] and init here
     */


    val context = LocalContext.current

    LaunchedEffect(initialAction) {
        // set settings host for request interceptor
        context.components.core.requestInterceptor.setNavigationController(nav)

        when (initialAction) {
            InitialBrowserTask.AppIcon -> {}
            is InitialBrowserTask.ExternalApp -> {}
            InitialBrowserTask.OpenPasswordManager -> {
                nav.navigate(route = BrowserRoute.Settings.PasswordSettingsPage)
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
        modifier = Modifier
            .fillMaxSize()
            .background(context.infernoTheme().value.primaryBackgroundColor),
//        contentAlignment = ,
        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    400,
                    easing = EaseInOut,
                )
            ) + slideIntoContainer(
                animationSpec = tween(
                    300,
                    easing = EaseInOut,
                ),
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
            )
        },
        exitTransition = {
            slideOutOfContainer(
                animationSpec = tween(
                    300,
                    easing = EaseInOut,
                ),
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                animationSpec = tween(
                    300,
                    easing = EaseInOut,
                ),
                towards = AnimatedContentTransitionScope.SlideDirection.End,
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                animationSpec = tween(
                    300,
                    easing = EaseInOut,
                ),
                towards = AnimatedContentTransitionScope.SlideDirection.End,
            )
        },
//        sizeTransform = ,
    ) {

        /**
         * Browser Pages
         */

        composable<BrowserRoute.InfernoBrowser> {
            BrowserComponent(
                biometricPromptCallbackManager = biometricPromptCallbackManager,
                state = browserComponentState,
                onNavToHistory = { nav.navigate(route = BrowserRoute.History) },
                onNavToSettings = { nav.navigate(route = BrowserRoute.Settings) },
                onNavToExtensions = { nav.navigate(route = BrowserRoute.Settings.ExtensionsSettingsPage) },
                onNavToPasswords = { nav.navigate(route = BrowserRoute.Settings.PasswordSettingsPage) },
                onNavToAutofillSettings = { nav.navigate(route = BrowserRoute.Settings.AutofillSettingsPage) },
            )
        }

        composable<BrowserRoute.History> {
            InfernoHistoryPage(goBack = { nav.popBackStack() })
        }

        /**
         * Settings Pages
         */

        composable<BrowserRoute.Settings> {
            SettingsPage(
                goBack = { nav.popBackStack() },
                onNavigateToAccountSettingsPage = { nav.navigate(route = BrowserRoute.Settings.AccountSettingsPage) },
                onNavigateToAccountProblemSettings = { nav.navigate(route = BrowserRoute.Settings.AccountProblemSettingsPage) },
                onNavigateToTurnOnSyncSettings = { nav.navigate(route = BrowserRoute.Settings.TurnOnSyncSettingsPage) },
                onNavigateToToolbarSettings = { nav.navigate(route = BrowserRoute.Settings.ToolbarSettingsPage) },
                onNavigateToTabBarSettings = { nav.navigate(route = BrowserRoute.Settings.TabSettingsPage) },
                onNavigateToSearchSettings = { nav.navigate(route = BrowserRoute.Settings.SearchSettingsPage) },
                onNavigateToThemeSettings = { nav.navigate(route = BrowserRoute.Settings.ThemeSettingsPage) },
                onNavigateToExtensionSettings = { nav.navigate(route = BrowserRoute.Settings.ExtensionsSettingsPage) },
                onNavigateToGestureSettings = { nav.navigate(route = BrowserRoute.Settings.GestureSettingsPage) },
                onNavigateToHomePageSettings = { nav.navigate(route = BrowserRoute.Settings.HomePageSettingsPage) },
                onNavigateToOnQuitSettings = { nav.navigate(route = BrowserRoute.Settings.OnQuitSettingsPage) },
                onNavigateToPasswordSettings = { nav.navigate(route = BrowserRoute.Settings.PasswordSettingsPage) },
                onNavigateToAutofillSettings = { nav.navigate(route = BrowserRoute.Settings.AutofillSettingsPage) },
                onNavigateToSitePermissionsSettings = { nav.navigate(route = BrowserRoute.Settings.SitePermissionsSettingsPage) },
                onNavigateToAccessibilitySettings = { nav.navigate(route = BrowserRoute.Settings.AccessibilitySettingsPage) },
                onNavigateToLocaleSettings = { nav.navigate(route = BrowserRoute.Settings.LocaleSettingsPage) },
                onNavigateToTranslationSettings = { nav.navigate(route = BrowserRoute.Settings.TranslationSettingsPage) },
                onNavigateToPrivacyAndSecuritySettings = { nav.navigate(route = BrowserRoute.Settings.PrivacyAndSecuritySettingsPage) },
            )
        }
        // todo
        composable<BrowserRoute.Settings.AccountSettingsPage> {
            AccountSettingsPage(goBack = { nav.popBackStack() })
        }
        // todo
        composable<BrowserRoute.Settings.AccountProblemSettingsPage> {
            AccountProblemSettingsPage(goBack = { nav.popBackStack() })
        }
        // todo
        composable<BrowserRoute.Settings.TurnOnSyncSettingsPage> {
            TurnOnSyncSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<BrowserRoute.Settings.ToolbarSettingsPage> {
            ToolbarSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<BrowserRoute.Settings.TabSettingsPage> {
            TabSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<BrowserRoute.Settings.SearchSettingsPage> {
            SearchSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<BrowserRoute.Settings.ThemeSettingsPage> {
            ThemeSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<BrowserRoute.Settings.ExtensionsSettingsPage> {
            ExtensionsPage(
                goBack = { nav.popBackStack() },
                onNavToAddon = {
                    val node =
                        nav.graph.findNode(route = BrowserRoute.Settings.ExtensionsSettingsPage.ExtensionSettingsPage)
                    if (node != null) {
                        nav.navigate(
                            resId = node.id,
                            args = bundleOf(
                                BrowserRoute.Settings.ExtensionsSettingsPage.ExtensionSettingsPage.ADDON_KEY to it,
                            ),
                        )
                    }
                },
                onNavToBrowser = {
                    nav.popBackStack(
                        route = BrowserRoute.InfernoBrowser,
                        inclusive = false,
                    )
                },
            )
        }
        composable<BrowserRoute.Settings.ExtensionsSettingsPage.ExtensionSettingsPage> {
            @Suppress("DEPRECATION") val addon =
                it.arguments?.getParcelable<Addon>(BrowserRoute.Settings.ExtensionsSettingsPage.ExtensionSettingsPage.ADDON_KEY)
            ExtensionPage(
                initialAddon = addon!!,
                goBack = {
                    nav.popBackStack()
                },
                onNavToBrowser = {
                    nav.popBackStack(
                        route = BrowserRoute.InfernoBrowser,
                        inclusive = false,
                    )
                },
            )
        }
        composable<BrowserRoute.Settings.GestureSettingsPage> {
            GestureSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<BrowserRoute.Settings.HomePageSettingsPage> {
            HomePageSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<BrowserRoute.Settings.OnQuitSettingsPage> {
            OnQuitSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<BrowserRoute.Settings.PasswordSettingsPage> {
            PasswordSettingsPage(
                goBack = { nav.popBackStack() },
                biometricPromptCallbackManager = biometricPromptCallbackManager,
                onNavToPasswordExceptionSettingsPage = { nav.navigate(route = BrowserRoute.Settings.PasswordSettingsPage.PasswordExceptionSettingsPage) },
            )
        }
        composable<BrowserRoute.Settings.PasswordSettingsPage.PasswordExceptionSettingsPage> {
            PasswordExceptionSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<BrowserRoute.Settings.AutofillSettingsPage> {
            AutofillSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<BrowserRoute.Settings.SitePermissionsSettingsPage> {
            SitePermissionsSettingsPage(
                goBack = { nav.popBackStack() },
                onNavToSitePermissionsExceptionsSettings = {
                    nav.navigate(route = BrowserRoute.Settings.SitePermissionsSettingsPage.SitePermissionsExceptionsSettingsPage)
                },
            )
        }
        composable<BrowserRoute.Settings.SitePermissionsSettingsPage.SitePermissionsExceptionsSettingsPage> {
            SitePermissionsExceptionsSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<BrowserRoute.Settings.AccessibilitySettingsPage> {
            AccessibilitySettingsPage(goBack = { nav.popBackStack() })
        }
        composable<BrowserRoute.Settings.LocaleSettingsPage> {
            LocaleSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<BrowserRoute.Settings.TranslationSettingsPage> {
            TranslationSettingsPage(
                goBack = { nav.popBackStack() },
                onNavigateToAutomaticTranslationSettings = { nav.navigate(route = BrowserRoute.Settings.TranslationSettingsPage.AutomaticTranslationSettingsPage) },
                onNavigateToDownloadTranslationLanguagesSettings = { nav.navigate(route = BrowserRoute.Settings.TranslationSettingsPage.DownloadTranslationLanguagesSettingsPage) },
                onNavigateToTranslationExceptionsSettings = { nav.navigate(route = BrowserRoute.Settings.TranslationSettingsPage.TranslationExceptionsSettingsPage) },
            )
        }
        // todo: possibly revise ui if too crowded (make items expandable instead, more room
        //  for descriptions)
        composable<BrowserRoute.Settings.TranslationSettingsPage.AutomaticTranslationSettingsPage> {
            AutomaticTranslationSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<BrowserRoute.Settings.TranslationSettingsPage.DownloadTranslationLanguagesSettingsPage> {
            DownloadTranslationLanguagesSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<BrowserRoute.Settings.TranslationSettingsPage.TranslationExceptionsSettingsPage> {
            TranslationExceptionsSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<BrowserRoute.Settings.PrivacyAndSecuritySettingsPage> {
            PrivacyAndSecuritySettingsPage(goBack = { nav.popBackStack() })
        }
    }
}
