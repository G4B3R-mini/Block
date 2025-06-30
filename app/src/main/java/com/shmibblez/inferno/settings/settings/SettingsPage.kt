package com.shmibblez.inferno.settings.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.coroutineScope
import com.shmibblez.inferno.R
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.nimbus.FxNimbus
import com.shmibblez.inferno.settings.account.AccountView
import com.shmibblez.inferno.settings.account.rememberAccountState
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import com.shmibblez.inferno.settings.compose.components.PreferenceAction
import com.shmibblez.inferno.settings.compose.components.PreferenceSpacer

//private const val SCROLL_INDICATOR_DELAY = 10L
//private const val FXA_SYNC_OVERRIDE_EXIT_DELAY = 2000L
//private const val AMO_COLLECTION_OVERRIDE_EXIT_DELAY = 3000L

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsPage(
    goBack: () -> Unit,
    onNavToAccountSettings: () -> Unit,
    onNavigateToToolbarSettings: () -> Unit,
    onNavigateToTabBarSettings: () -> Unit,
    onNavigateToSearchSettings: () -> Unit,
    onNavigateToThemeSettings: () -> Unit,
    onNavigateToExtensionSettings: () -> Unit,
    onNavigateToGestureSettings: () -> Unit,
    onNavigateToHomePageSettings: () -> Unit,
    onNavigateToOnQuitSettings: () -> Unit,
    onNavigateToPasswordSettings: () -> Unit,
    onNavigateToAutofillSettings: () -> Unit,
    onNavigateToSitePermissionsSettings: () -> Unit,
    onNavigateToAccessibilitySettings: () -> Unit,
    onNavigateToLocaleSettings: () -> Unit,
    onNavigateToTranslationSettings: () -> Unit,
    onNavigateToPrivacyAndSecuritySettings: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleScope = LocalLifecycleOwner.current.lifecycle.coroutineScope
    val translationSupported =
        FxNimbus.features.translations.value().globalSettingsEnabled && context.components.core.store.state.translationEngine.isEngineSupported == true

    val accountState by rememberAccountState(
        scope = lifecycleScope,
        accountManager = context.components.backgroundServices.accountManager,
        httpClient = context.components.core.client,
//        updateFxAAllowDomesticChinaServerMenu = {},
    )

    InfernoSettingsPage(
        title = stringResource(R.string.settings_title),
        goBack = goBack,
    ) { edgeInsets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(edgeInsets),
        ) {
            // account auth component
            item {
                AccountView(
                    state = accountState,
                    onNavToAccountSettings = onNavToAccountSettings,
                )
            }

            item { PreferenceSpacer() }

            // toolbar settings
            item {
                PreferenceAction(
                    title = "Toolbar Settings", // todo: string res for title
                    action = onNavigateToToolbarSettings,
                )
            }
            // tab settings
            item {
                PreferenceAction(
                    title = stringResource(R.string.preferences_tabs),
                    action = onNavigateToTabBarSettings,
                )
            }
            // search settings
            item {
                PreferenceAction(
                    title = stringResource(R.string.preferences_search),
                    action = onNavigateToSearchSettings,
                )
            }
            // theme settings
            item {
                PreferenceAction(
                    title = stringResource(R.string.preferences_theme),
                    action = onNavigateToThemeSettings,
                )
            }
            // extensions settings
            item {
                PreferenceAction(
                    title = stringResource(R.string.preferences_extensions),
                    action = onNavigateToExtensionSettings,
                )
            }

            item { PreferenceSpacer() }

            // gesture settings
            item {
                PreferenceAction(
                    title = stringResource(R.string.preferences_gestures),
                    action = onNavigateToGestureSettings,
                )
            }
            // home page settings
            item {
                PreferenceAction(
                    title = stringResource(R.string.preferences_home_2),
                    action = onNavigateToHomePageSettings,
                )
            }
            // on quit settings
            item {
                PreferenceAction(
                    title = "On Quit", // todo: string res for title
                    action = onNavigateToOnQuitSettings,
                )
            }
            // password settings
            item {
                PreferenceAction(
                    title = stringResource(R.string.preferences_passwords_logins_and_passwords_2),
                    action = onNavigateToPasswordSettings,
                )
            }
            item {
                PreferenceAction(
                    title = stringResource(R.string.preferences_autofill),
                    action = onNavigateToAutofillSettings,
                )
            }

            item { PreferenceSpacer() }

            // site permission settings
            item {
                PreferenceAction(
                    title = stringResource(R.string.preferences_site_permissions),
                    action = onNavigateToSitePermissionsSettings,
                )
            }
            // accessibility settings
            item {
                PreferenceAction(
                    title = stringResource(R.string.preferences_accessibility),
                    action = onNavigateToAccessibilitySettings,
                )
            }
            // locale settings
            item {
                PreferenceAction(
                    title = stringResource(R.string.preferences_language),
                    action = onNavigateToLocaleSettings,
                )
            }
            // translation settings
            item {
                if (translationSupported) {
                    PreferenceAction(
                        title = stringResource(R.string.preferences_translations),
                        action = onNavigateToTranslationSettings,
                    )
                }
            }

            item { PreferenceSpacer() }

            // privacy and security settings
            item {
                PreferenceAction(
                    title = stringResource(R.string.preferences_category_privacy_security),
                    action = onNavigateToPrivacyAndSecuritySettings,
                )
            }

            item { PreferenceSpacer() }
        }
    }
}
