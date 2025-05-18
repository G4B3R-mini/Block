package com.shmibblez.inferno.settings.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shmibblez.inferno.settings.SettingsFragmentDirections
import com.shmibblez.inferno.settings.account.AccountProblemSettingsPage
import com.shmibblez.inferno.settings.account.AccountSettingsPage
import com.shmibblez.inferno.settings.account.TurnOnSyncSettingsPage
import com.shmibblez.inferno.settings.compose.SettingsPage
import com.shmibblez.inferno.settings.gesture.GestureSettingsPage
import com.shmibblez.inferno.settings.search.SearchSettingsPage
import com.shmibblez.inferno.settings.tabs.TabSettingsPage
import com.shmibblez.inferno.settings.theme.ThemeSettingsPage
import com.shmibblez.inferno.settings.toolbar.ToolbarSettingsPage
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
    object GestureSettingsPage

    @Serializable
    object ThemeSettingsPage
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
            )
        }
        composable<SettingsRoutes.AccountSettingsPage> { AccountSettingsPage(goBack = { nav.popBackStack() }) }
        composable<SettingsRoutes.AccountProblemSettingsPage> { AccountProblemSettingsPage(goBack = { nav.popBackStack() }) }
        composable<SettingsRoutes.TurnOnSyncSettingsPage> { TurnOnSyncSettingsPage(goBack = { nav.popBackStack() }) }
        composable<SettingsRoutes.ToolbarSettingsPage> { ToolbarSettingsPage(goBack = { nav.popBackStack() }) }
        composable<SettingsRoutes.TabSettingsPage> { TabSettingsPage(goBack = { nav.popBackStack() }) }
        composable<SettingsRoutes.SearchSettingsPage> { SearchSettingsPage(goBack = { nav.popBackStack() }) }
        composable<SettingsRoutes.ThemeSettingsPage> { ThemeSettingsPage(goBack = { nav.popBackStack() }) }
        composable<SettingsRoutes.GestureSettingsPage> { GestureSettingsPage(goBack = { nav.popBackStack() }) }
    }
}