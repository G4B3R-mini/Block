package com.shmibblez.inferno.settings.compose

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
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
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.PreferenceAction
import com.shmibblez.inferno.settings.compose.components.PreferenceSpacer
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import com.shmibblez.inferno.settings.compose.mainSettings.AccountView
import com.shmibblez.inferno.settings.compose.mainSettings.rememberAccountState

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
    onNavigateToAccountProblemSettingsPage: () -> Unit,
    onNavigateToTurnOnSyncSettingsPage: () -> Unit,
    onNavigateToToolbarSettings: () -> Unit,
    onNavigateToTabBarSettings: () -> Unit,
    onNavigateToSearchSettings: () -> Unit,
    onNavigateToThemeSettings: () -> Unit,
    onNavigateToGestureSettings: () -> Unit,
    onNavigateToHomePageSettings: () -> Unit,
    onNavigateToOnQuitSettings: () -> Unit,
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
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_back_button),
                        contentDescription = stringResource(R.string.browser_menu_back),
//                        modifier = Modifier.clickable(onClick = goBack),
                    )
                },
                title = { InfernoText("Toolbar Settings") }, // todo: string res
            )
        },
    ) {
        AccountView(
            state = accountState,
            onNavigateSignedIn = onNavigateToAccountSettingsPage,
            onNavigateRequiresReauth = onNavigateToAccountProblemSettingsPage,
            onNavigateSignedOut = onNavigateToTurnOnSyncSettingsPage,
        )

        PreferenceSpacer()

        PreferenceAction(
            title = "Toolbar Settings", // todo: string res for title
            action = onNavigateToToolbarSettings,
        )
        PreferenceAction(
            title = "Tab Settings", // todo: string res for title
            action = onNavigateToTabBarSettings,
        )
        PreferenceAction(
            title = "Search Settings", // todo: string res for title
            action = onNavigateToSearchSettings,
        )
        PreferenceAction(
            title = "Theme Settings", // todo: string res for title
            action = onNavigateToThemeSettings,
        )
        PreferenceAction(
            title = "Gesture Settings", // todo: string res for title
            action = onNavigateToGestureSettings,
        )
        PreferenceAction(
            title = "Home Page Settings", // todo: string res for title
            action = onNavigateToHomePageSettings,
        )
        PreferenceAction(
            title = "On Quit Settings", // todo: string res for title
            action = onNavigateToOnQuitSettings,
        )
        PreferenceAction(
            title = "Autofill Settings", // todo: string res for title
            action = onNavigateToAutofillSettings,
        )
        PreferenceAction(
            title = "On Quit Settings", // todo: string res for title
            action = onNavigateToSitePermissionsSettings,
        )
        PreferenceAction(
            title = "Accessibility Settings", // todo: string res for title
            action = onNavigateToAccessibilitySettings,
        )
        PreferenceAction(
            title = "Locale Settings", // todo: string res for title
            action = onNavigateToLocaleSettings,
        )
        PreferenceAction(
            title = "Translation Settings", // todo: string res for title
            action = onNavigateToTranslationSettings,
        )
        PreferenceAction(
            title = "Private Mode Settings", // todo: string res for title
            action = onNavigateToPrivateModeSettings,
        )
        PreferenceAction(
            title = "Tracking Protection Settings", // todo: string res for title
            action = onNavigateToTrackingProtectionSettings,
        )
        PreferenceAction(
            title = "Https Only Settings", // todo: string res for title
            action = onNavigateToHttpsOnlySettings,
        )

        PreferenceSpacer()

        PreferenceTitle(text = "Browser Components") // todo: string res
        PreferenceAction(
            title = "",
            action = { },
        )
    }
}
