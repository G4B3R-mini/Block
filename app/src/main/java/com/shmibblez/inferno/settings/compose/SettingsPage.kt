package com.shmibblez.inferno.settings.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavController
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.nimbus.FxNimbus
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import com.shmibblez.inferno.settings.compose.mainSettings.AccountView
import com.shmibblez.inferno.settings.compose.mainSettings.rememberAccountState
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.accounts.FenixFxAEntryPoint
import com.shmibblez.inferno.ext.nav
import com.shmibblez.inferno.settings.SettingsFragmentDirections
import com.shmibblez.inferno.settings.compose.components.PreferenceAction

private const val SCROLL_INDICATOR_DELAY = 10L
private const val FXA_SYNC_OVERRIDE_EXIT_DELAY = 2000L
private const val AMO_COLLECTION_OVERRIDE_EXIT_DELAY = 3000L

// todo: settings page hierarchy:
//  - toolbar item
//    - address bar settings item
//      - address bar settings page
//    - toolbar icons list
//  - tabs item
//    - general title
//      - close tabs automatically after item
//        - never
//        - one day
//        - one week
//        - one month
//        - custom [whole number input] [day/week/month selector] max is 12 months
//    - tab tray title
//      - grid or list selector
//    - tab bar title
//      - show x to close
//        - all tabs
//        - active tab
@Composable
fun SettingsPage(
    nav: NavController,
) {
    val context = LocalContext.current
    val lifecycleScope = LocalLifecycleOwner.current.lifecycle.coroutineScope
    val translationSupported =
        FxNimbus.features.translations.value().globalSettingsEnabled && context.components.core.store.state.translationEngine.isEngineSupported == true
    val settings by context.infernoSettingsDataStore.data.collectAsState(
        initial = InfernoSettings.getDefaultInstance(),
    )

    val accountState = rememberAccountState(
        profile = context.components.backgroundServices.accountManager.accountProfile(),
        scope = lifecycleScope,
        accountManager = context.components.backgroundServices.accountManager,
        httpClient = context.components.core.client,
        updateFxAAllowDomesticChinaServerMenu = {},
    )

    AccountView(
        state = accountState,
        onNavigateSignedIn = {
            nav.nav(
                R.id.settingsFragment,
                SettingsFragmentDirections.actionSettingsFragmentToAccountSettingsFragment(),
            )
        },
        onNavigateRequiresReauth = {
            nav.nav(
                R.id.settingsFragment,
                SettingsFragmentDirections.actionSettingsFragmentToAccountProblemFragment(entrypoint = FenixFxAEntryPoint.SettingsMenu),
            )
        },
        onNavigateSignedOut = {
            nav.nav(
                R.id.settingsFragment,
                SettingsFragmentDirections.actionSettingsFragmentToTurnOnSyncFragment(entrypoint = FenixFxAEntryPoint.SettingsMenu),
            )
        },
    )

    PreferenceTitle(text = stringResource(R.string.preferences_category_general))


    PreferenceTitle(text = "Browser Components") // todo: string res
    PreferenceAction(
        title = "",
        action = {  },
    )



}
