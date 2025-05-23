package com.shmibblez.inferno.settings.nav

import androidx.compose.runtime.Composable
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
import com.shmibblez.inferno.settings.compose.SettingsPage
import com.shmibblez.inferno.settings.gesture.GestureSettingsPage
import com.shmibblez.inferno.settings.home.HomePageSettingsPage
import com.shmibblez.inferno.settings.httpsonly.HttpsOnlySettingsPage
import com.shmibblez.inferno.settings.locale.LocaleSettingsPage
import com.shmibblez.inferno.settings.onQuit.OnQuitSettingsPage
import com.shmibblez.inferno.settings.passwords.PasswordExceptionSettingsPage
import com.shmibblez.inferno.settings.privatemode.PrivateModeSettingsPage
import com.shmibblez.inferno.settings.search.SearchSettingsPage
import com.shmibblez.inferno.settings.sitepermissions.SitePermissionsSettingsPage
import com.shmibblez.inferno.settings.tabs.TabSettingsPage
import com.shmibblez.inferno.settings.theme.ThemeSettingsPage
import com.shmibblez.inferno.settings.toolbar.ToolbarSettingsPage
import com.shmibblez.inferno.settings.trackingprotection.TrackingProtectionSettingsPage
import com.shmibblez.inferno.settings.translation.TranslationSettingsPage
import kotlinx.serialization.Serializable


private object SettingsRoutes {

    /**
     * todo: based on [SettingsFragmentDirections.actionSettingsFragmentToAccountSettingsFragment]
     */
    @Serializable
    object AccountSettingsPage

    /**
     * todo: based on [SettingsFragmentDirections.actionSettingsFragmentToAccountProblemFragment]
     */
    @Serializable
    object AccountProblemSettingsPage

    /**
     * todo: based on [SettingsFragmentDirections.actionSettingsFragmentToTurnOnSyncFragment]
     */
    @Serializable
    object TurnOnSyncSettingsPage

    @Serializable
    object SettingsPage

    @Serializable
    object ToolbarSettingsPage

    @Serializable
    object TabSettingsPage

    @Serializable
    object SearchSettingsPage

    @Serializable
    object ThemeSettingsPage

    @Serializable
    object GestureSettingsPage

    @Serializable
    object HomePageSettingsPage

    @Serializable
    object OnQuitSettingsPage

    @Serializable
    object PasswordSettingsPage {
        @Serializable
        object PasswordExceptionSettingsPage
    }

    @Serializable
    object AutofillSettingsPage

    @Serializable
    object SitePermissionsSettingsPage

    @Serializable
    object AccessibilitySettingsPage

    @Serializable
    object LocaleSettingsPage

    @Serializable
    object TranslationSettingsPage

    @Serializable
    object PrivateModeSettingsPage

    @Serializable
    object TrackingProtectionSettingsPage

    @Serializable
    object HttpsOnlySettingsPage

}

// todo: make main view of settings fragment
@Composable
fun SettingsNavHost() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = SettingsRoutes.SettingsPage,
    ) {
        composable<SettingsRoutes.SettingsPage> {
            SettingsPage(
                onNavigateToAccountSettingsPage = { nav.navigate(route = SettingsRoutes.AccountSettingsPage) },
                onNavigateToAccountProblemSettingsPage = { nav.navigate(route = SettingsRoutes.AccountProblemSettingsPage) },
                onNavigateToTurnOnSyncSettingsPage = { nav.navigate(route = SettingsRoutes.TurnOnSyncSettingsPage) },
                onNavigateToToolbarSettings = { nav.navigate(route = SettingsRoutes.ToolbarSettingsPage) },
                onNavigateToTabBarSettings = { nav.navigate(route = SettingsRoutes.TabSettingsPage) },
                onNavigateToSearchSettings = { nav.navigate(route = SettingsRoutes.SearchSettingsPage) },
                onNavigateToThemeSettings = { nav.navigate(route = SettingsRoutes.ThemeSettingsPage) },
                onNavigateToGestureSettings = { nav.navigate(route = SettingsRoutes.GestureSettingsPage) },
                onNavigateToHomePageSettings = { nav.navigate(route = SettingsRoutes.HomePageSettingsPage) },
                onNavigateToOnQuitSettings = { nav.navigate(route = SettingsRoutes.OnQuitSettingsPage) },
                onNavigateToPasswordSettings = { nav.navigate(route = SettingsRoutes.PasswordSettingsPage) },
                onNavigateToAutofillSettings = { nav.navigate(route = SettingsRoutes.AutofillSettingsPage) },
                onNavigateToSitePermissionsSettings = { nav.navigate(route = SettingsRoutes.SitePermissionsSettingsPage) },
                onNavigateToAccessibilitySettings = { nav.navigate(route = SettingsRoutes.AccessibilitySettingsPage) },
                onNavigateToLocaleSettings = { nav.navigate(route = SettingsRoutes.LocaleSettingsPage) },
                onNavigateToTranslationSettings = { nav.navigate(route = SettingsRoutes.TranslationSettingsPage) },
                onNavigateToPrivateModeSettings = { nav.navigate(route = SettingsRoutes.PrivateModeSettingsPage) },
                onNavigateToTrackingProtectionSettings = { nav.navigate(route = SettingsRoutes.TrackingProtectionSettingsPage) },
                onNavigateToHttpsOnlySettings = { nav.navigate(route = SettingsRoutes.HttpsOnlySettingsPage) },
            )
        }
        composable<SettingsRoutes.AccountSettingsPage> {
            AccountSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.AccountProblemSettingsPage> {
            AccountProblemSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.TurnOnSyncSettingsPage> {
            TurnOnSyncSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.ToolbarSettingsPage> {
            ToolbarSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.TabSettingsPage> {
            TabSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.SearchSettingsPage> {
            SearchSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.ThemeSettingsPage> {
            ThemeSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.GestureSettingsPage> {
            GestureSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.HomePageSettingsPage> {
            HomePageSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.OnQuitSettingsPage> {
            OnQuitSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.AutofillSettingsPage> {
            PasswordSettingsPage(
                goBack = { nav.popBackStack() },
                onNavToPasswordExceptionSettingsPage = { nav.navigate(route = SettingsRoutes.PasswordSettingsPage.PasswordExceptionSettingsPage) },
            )
        }
        composable<SettingsRoutes.PasswordSettingsPage.PasswordExceptionSettingsPage> {
            PasswordExceptionSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.AutofillSettingsPage> {
            AutofillSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.SitePermissionsSettingsPage> {
            SitePermissionsSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.AccessibilitySettingsPage> {
            AccessibilitySettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.LocaleSettingsPage> {
            LocaleSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.TranslationSettingsPage> {
            TranslationSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.PrivateModeSettingsPage> {
            PrivateModeSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.TrackingProtectionSettingsPage> {
            TrackingProtectionSettingsPage(goBack = { nav.popBackStack() })
        }
        composable<SettingsRoutes.HttpsOnlySettingsPage> {
            HttpsOnlySettingsPage(goBack = { nav.popBackStack() })
        }
    }
}