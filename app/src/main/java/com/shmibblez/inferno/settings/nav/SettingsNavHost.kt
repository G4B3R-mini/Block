package com.shmibblez.inferno.settings.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shmibblez.inferno.settings.passwords.PasswordSettingsPage
import com.shmibblez.inferno.settings.SettingsFragmentDirections
import com.shmibblez.inferno.settings.accessibility.AccessibilitySettingsPage
import com.shmibblez.inferno.settings.account.AccountProblemSettingsPage
import com.shmibblez.inferno.settings.account.AccountSettingsPage
import com.shmibblez.inferno.settings.account.TurnOnSyncSettingsPage
import com.shmibblez.inferno.settings.autofill.AutofillSettingsPage
import com.shmibblez.inferno.settings.SettingsPage
import com.shmibblez.inferno.settings.gesture.GestureSettingsPage
import com.shmibblez.inferno.settings.home.HomePageSettingsPage
import com.shmibblez.inferno.settings.locale.LocaleSettingsPage
import com.shmibblez.inferno.settings.onQuit.OnQuitSettingsPage
import com.shmibblez.inferno.settings.passwords.PasswordExceptionSettingsPage
import com.shmibblez.inferno.settings.privacyAndSecurity.PrivacyAndSecuritySettingsPage
import com.shmibblez.inferno.settings.search.SearchSettingsPage
import com.shmibblez.inferno.settings.sitepermissions.SitePermissionsExceptionsSettingsPage
import com.shmibblez.inferno.settings.sitepermissions.SitePermissionsSettingsPage
import com.shmibblez.inferno.settings.tabs.TabSettingsPage
import com.shmibblez.inferno.settings.theme.ThemeSettingsPage
import com.shmibblez.inferno.settings.toolbar.ToolbarSettingsPage
import com.shmibblez.inferno.settings.translation.AutomaticTranslationSettingsPage
import com.shmibblez.inferno.settings.translation.DownloadTranslationLanguagesSettingsPage
import com.shmibblez.inferno.settings.translation.TranslationExceptionsSettingsPage
import com.shmibblez.inferno.settings.translation.TranslationSettingsPage
import kotlinx.serialization.Serializable


private interface SettingsRoute {

    /**
     * todo: based on [SettingsFragmentDirections.actionSettingsFragmentToAccountSettingsFragment]
     */
    @Serializable
    object AccountSettingsPage: SettingsRoute

    /**
     * todo: based on [SettingsFragmentDirections.actionSettingsFragmentToAccountProblemFragment]
     */
    @Serializable
    object AccountProblemSettingsPage: SettingsRoute

    /**
     * todo: based on [SettingsFragmentDirections.actionSettingsFragmentToTurnOnSyncFragment]
     */
    @Serializable
    object TurnOnSyncSettingsPage: SettingsRoute

    @Serializable
    object SettingsPage: SettingsRoute

    @Serializable
    object ToolbarSettingsPage: SettingsRoute

    @Serializable
    object TabSettingsPage: SettingsRoute

    @Serializable
    object SearchSettingsPage: SettingsRoute

    @Serializable
    object ThemeSettingsPage: SettingsRoute

    @Serializable
    object GestureSettingsPage: SettingsRoute

    @Serializable
    object HomePageSettingsPage: SettingsRoute

    @Serializable
    object OnQuitSettingsPage: SettingsRoute

    @Serializable
    object PasswordSettingsPage: SettingsRoute {
        @Serializable
        object PasswordExceptionSettingsPage: SettingsRoute
    }

    @Serializable
    object AutofillSettingsPage: SettingsRoute

    @Serializable
    object SitePermissionsSettingsPage: SettingsRoute {
        @Serializable
        object SitePermissionsExceptionsSettingsPage: SettingsRoute
    }

    @Serializable
    object AccessibilitySettingsPage: SettingsRoute

    @Serializable
    object LocaleSettingsPage: SettingsRoute

    @Serializable
    object TranslationSettingsPage: SettingsRoute {
        @Serializable
        object AutomaticTranslationSettingsPage: SettingsRoute

        @Serializable
        object DownloadTranslationLanguagesSettingsPage: SettingsRoute

        @Serializable
        object TranslationExceptionsSettingsPage: SettingsRoute
    }

    @Serializable
    object PrivacyAndSecuritySettingsPage: SettingsRoute

}

// todo: make main view of settings fragment

@Composable
fun SettingsNavHost(
    goBackLegacy: () -> Unit,
) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = SettingsRoute.SettingsPage,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable<SettingsRoute.SettingsPage> {
            SettingsPage(
                goBackLegacy = goBackLegacy,
                onNavigateToAccountSettingsPage = { nav.navigate(route = SettingsRoute.AccountSettingsPage) },
                onNavigateToAccountProblemSettings = { nav.navigate(route = SettingsRoute.AccountProblemSettingsPage) },
                onNavigateToTurnOnSyncSettings = { nav.navigate(route = SettingsRoute.TurnOnSyncSettingsPage) },
                onNavigateToToolbarSettings = { nav.navigate(route = SettingsRoute.ToolbarSettingsPage) },
                onNavigateToTabBarSettings = { nav.navigate(route = SettingsRoute.TabSettingsPage) },
                onNavigateToSearchSettings = { nav.navigate(route = SettingsRoute.SearchSettingsPage) },
                onNavigateToThemeSettings = { nav.navigate(route = SettingsRoute.ThemeSettingsPage) },
                onNavigateToGestureSettings = { nav.navigate(route = SettingsRoute.GestureSettingsPage) },
                onNavigateToHomePageSettings = { nav.navigate(route = SettingsRoute.HomePageSettingsPage) },
                onNavigateToOnQuitSettings = { nav.navigate(route = SettingsRoute.OnQuitSettingsPage) },
                onNavigateToPasswordSettings = { nav.navigate(route = SettingsRoute.PasswordSettingsPage) },
                onNavigateToAutofillSettings = { nav.navigate(route = SettingsRoute.AutofillSettingsPage) },
                onNavigateToSitePermissionsSettings = { nav.navigate(route = SettingsRoute.SitePermissionsSettingsPage) },
                onNavigateToAccessibilitySettings = { nav.navigate(route = SettingsRoute.AccessibilitySettingsPage) },
                onNavigateToLocaleSettings = { nav.navigate(route = SettingsRoute.LocaleSettingsPage) },
                onNavigateToTranslationSettings = { nav.navigate(route = SettingsRoute.TranslationSettingsPage) },
                onNavigateToPrivacyAndSecuritySettings = { nav.navigate(route = SettingsRoute.PrivacyAndSecuritySettingsPage) },
            )
        }
        // todo
        composable<SettingsRoute.AccountSettingsPage> {
            AccountSettingsPage(goBack = { nav.popBackStack() })
        }
        // todo
        composable<SettingsRoute.AccountProblemSettingsPage> {
            AccountProblemSettingsPage(goBack = { nav.popBackStack() })
        }
        // todo
        composable<SettingsRoute.TurnOnSyncSettingsPage> {
            TurnOnSyncSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoute.ToolbarSettingsPage> {
            ToolbarSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoute.TabSettingsPage> {
            TabSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoute.SearchSettingsPage> {
            SearchSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoute.ThemeSettingsPage> {
            ThemeSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoute.GestureSettingsPage> {
            GestureSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoute.HomePageSettingsPage> {
            HomePageSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoute.OnQuitSettingsPage> {
            OnQuitSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoute.AutofillSettingsPage> {
            PasswordSettingsPage(
                goBack = { nav.popBackStack() },
                onNavToPasswordExceptionSettingsPage = { nav.navigate(route = SettingsRoute.PasswordSettingsPage.PasswordExceptionSettingsPage) },
            )
        }
        composable<SettingsRoute.PasswordSettingsPage.PasswordExceptionSettingsPage> {
            PasswordExceptionSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoute.AutofillSettingsPage> {
            AutofillSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoute.SitePermissionsSettingsPage> {
            SitePermissionsSettingsPage(
                goBack = { nav.popBackStack() },
                onNavToSitePermissionsExceptionsSettings = {
                    nav.navigate(route = SettingsRoute.SitePermissionsSettingsPage.SitePermissionsExceptionsSettingsPage)
                },
            )
        }
        composable<SettingsRoute.SitePermissionsSettingsPage.SitePermissionsExceptionsSettingsPage> {
            SitePermissionsExceptionsSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoute.AccessibilitySettingsPage> {
            AccessibilitySettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoute.LocaleSettingsPage> {
            LocaleSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoute.TranslationSettingsPage> {
            TranslationSettingsPage(
                goBack = { nav.popBackStack() },
                onNavigateToAutomaticTranslationSettings = { nav.navigate(route = SettingsRoute.TranslationSettingsPage.AutomaticTranslationSettingsPage) },
                onNavigateToDownloadTranslationLanguagesSettings = { nav.navigate(route = SettingsRoute.TranslationSettingsPage.DownloadTranslationLanguagesSettingsPage) },
                onNavigateToTranslationExceptionsSettings = { nav.navigate(route = SettingsRoute.TranslationSettingsPage.TranslationExceptionsSettingsPage) },
            )
        }
        // todo: possibly revise ui if too crowded (make items expandable instead, more room
        //  for descriptions)
        composable<SettingsRoute.TranslationSettingsPage.AutomaticTranslationSettingsPage> {
            AutomaticTranslationSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoute.TranslationSettingsPage.DownloadTranslationLanguagesSettingsPage> {
            DownloadTranslationLanguagesSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoute.TranslationSettingsPage.TranslationExceptionsSettingsPage> {
            TranslationExceptionsSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoute.PrivacyAndSecuritySettingsPage> {
            PrivacyAndSecuritySettingsPage(goBack = { nav.popBackStack() })
        }
    }
}