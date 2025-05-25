package com.shmibblez.inferno.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.coroutineScope
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.nimbus.FxNimbus
import com.shmibblez.inferno.settings.compose.components.PreferenceAction
import com.shmibblez.inferno.settings.compose.components.PreferenceSpacer
import com.shmibblez.inferno.settings.account.AccountView
import com.shmibblez.inferno.settings.account.rememberAccountState

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


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    onNavigateToAccountSettingsPage: () -> Unit,
    onNavigateToAccountProblemSettings: () -> Unit,
    onNavigateToTurnOnSyncSettings: () -> Unit,
    onNavigateToToolbarSettings: () -> Unit,
    onNavigateToTabBarSettings: () -> Unit,
    onNavigateToSearchSettings: () -> Unit,
    onNavigateToThemeSettings: () -> Unit,
    onNavigateToGestureSettings: () -> Unit,
    onNavigateToHomePageSettings: () -> Unit,
    onNavigateToOnQuitSettings: () -> Unit,
    onNavigateToPasswordSettings: () -> Unit,
    onNavigateToAutofillSettings: () -> Unit,
    onNavigateToSitePermissionsSettings: () -> Unit,
    onNavigateToAccessibilitySettings: () -> Unit,
    onNavigateToLocaleSettings: () -> Unit,
    onNavigateToTranslationSettings: () -> Unit,
    onNavigateToPrivateModeSettings: () -> Unit,
    onNavigateToTrackingProtectionSettings: () -> Unit,
    onNavigateToHttpsOnlySettings: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleScope = LocalLifecycleOwner.current.lifecycle.coroutineScope
    val translationSupported =
        FxNimbus.features.translations.value().globalSettingsEnabled && context.components.core.store.state.translationEngine.isEngineSupported == true

    val accountState = rememberAccountState(
        profile = context.components.backgroundServices.accountManager.accountProfile(),
        scope = lifecycleScope,
        accountManager = context.components.backgroundServices.accountManager,
        httpClient = context.components.core.client,
        updateFxAAllowDomesticChinaServerMenu = {},
    )

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_back_button),
                        contentDescription = stringResource(R.string.browser_menu_back),
                        modifier = Modifier.clickable(onClick = {/* todo */ }),
                    )
                },
                title = { InfernoText(stringResource(R.string.settings_title)) },
            )
        },
    ) {
        // account auth component
        AccountView(
            state = accountState,
            onNavigateSignedIn = onNavigateToAccountSettingsPage,
            onNavigateRequiresReauth = onNavigateToAccountProblemSettings,
            onNavigateSignedOut = onNavigateToTurnOnSyncSettings,
        )

        PreferenceSpacer()

        // toolbar settings
        PreferenceAction(
            title = "Toolbar Settings", // todo: string res for title
            action = onNavigateToToolbarSettings,
        )
        // tab settings
        PreferenceAction(
            title = stringResource(R.string.preferences_tabs),
            action = onNavigateToTabBarSettings,
        )
        // search settings
        PreferenceAction(
            title = stringResource(R.string.preferences_search),
            action = onNavigateToSearchSettings,
        )
        // theme settings
        PreferenceAction(
            title = stringResource(R.string.preferences_theme),
            action = onNavigateToThemeSettings,
        )

        PreferenceSpacer()

        // gesture settings
        PreferenceAction(
            title = stringResource(R.string.preferences_gestures),
            action = onNavigateToGestureSettings,
        )
        // home page settings
        PreferenceAction(
            title = stringResource(R.string.preferences_home_2),
            action = onNavigateToHomePageSettings,
        )
        // on quit settings
        PreferenceAction(
            title = "On Quit", // todo: string res for title
            action = onNavigateToOnQuitSettings,
        )
        // password settings
        PreferenceAction(
            title = stringResource(R.string.preferences_passwords_logins_and_passwords_2),
            action = onNavigateToPasswordSettings,
        )
        PreferenceAction(
            title = stringResource(R.string.preferences_autofill),
            action = onNavigateToAutofillSettings,
        )

        PreferenceSpacer()

        // site permission settings
        PreferenceAction(
            title = stringResource(R.string.preferences_site_permissions),
            action = onNavigateToSitePermissionsSettings,
        )
        // accessibility settings
        PreferenceAction(
            title = stringResource(R.string.preferences_accessibility),
            action = onNavigateToAccessibilitySettings,
        )
        // locale settings
        PreferenceAction(
            title = stringResource(R.string.preferences_language),
            action = onNavigateToLocaleSettings,
        )
        // translation settings
        PreferenceAction(
            title = stringResource(R.string.preferences_translations),
            action = onNavigateToTranslationSettings,
        )

        PreferenceSpacer()

        // private mode settings
        PreferenceAction(
            title = stringResource(R.string.preferences_private_browsing_options),
            action = onNavigateToPrivateModeSettings,
        )
        // tracking protection settings
        PreferenceAction(
            title = stringResource(R.string.preference_enhanced_tracking_protection),
            action = onNavigateToTrackingProtectionSettings,
        )
        // https only settings
        PreferenceAction(
            title = stringResource(R.string.preferences_https_only_title),
            action = onNavigateToHttpsOnlySettings,
        )

        PreferenceSpacer()
    }
}
