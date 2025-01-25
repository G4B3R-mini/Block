/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.debugsettings.ui

import android.os.StrictMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.createTab
import mozilla.components.browser.state.store.BrowserStore
import com.shmibblez.inferno.mozillaAndroidComponents.compose.base.annotation.LightDarkPreview
import mozilla.components.concept.storage.LoginsStorage
import mozilla.components.lib.state.ext.observeAsState
import com.shmibblez.inferno.debugsettings.addresses.AddressesDebugLocalesRepository
import com.shmibblez.inferno.debugsettings.addresses.AddressesTools
import com.shmibblez.inferno.debugsettings.addresses.FakeAddressesDebugLocalesRepository
import com.shmibblez.inferno.debugsettings.addresses.SharedPrefsAddressesDebugLocalesRepository
import com.shmibblez.inferno.debugsettings.cfrs.CfrToolsPreferencesMiddleware
import com.shmibblez.inferno.debugsettings.cfrs.CfrToolsState
import com.shmibblez.inferno.debugsettings.cfrs.CfrToolsStore
import com.shmibblez.inferno.debugsettings.cfrs.DefaultCfrPreferencesRepository
//import com.shmibblez.inferno.debugsettings.gleandebugtools.DefaultGleanDebugToolsStorage
//import com.shmibblez.inferno.debugsettings.gleandebugtools.GleanDebugToolsMiddleware
//import com.shmibblez.inferno.debugsettings.gleandebugtools.GleanDebugToolsState
//import com.shmibblez.inferno.debugsettings.gleandebugtools.GleanDebugToolsStore
import com.shmibblez.inferno.debugsettings.logins.FakeLoginsStorage
import com.shmibblez.inferno.debugsettings.logins.LoginsTools
import com.shmibblez.inferno.debugsettings.navigation.DebugDrawerRoute
import com.shmibblez.inferno.debugsettings.store.DebugDrawerAction
import com.shmibblez.inferno.debugsettings.store.DebugDrawerNavigationMiddleware
import com.shmibblez.inferno.debugsettings.store.DebugDrawerStore
import com.shmibblez.inferno.debugsettings.store.DrawerStatus
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.theme.FirefoxTheme
import com.shmibblez.inferno.theme.Theme

/**
 * Overlay for presenting Fenix-wide debugging content.
 *
 * @param browserStore [BrowserStore] used to access [BrowserState].
 * @param loginsStorage [LoginsStorage] used to access logins for [LoginsTools].
 * @param inactiveTabsEnabled Whether the inactive tabs feature is enabled.
 */
@Composable
fun FenixOverlay(
    browserStore: BrowserStore,
    loginsStorage: LoginsStorage,
    inactiveTabsEnabled: Boolean,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    FenixOverlay(
        browserStore = browserStore,
        cfrToolsStore = CfrToolsStore(
            middlewares = listOf(
                CfrToolsPreferencesMiddleware(
                    cfrPreferencesRepository = DefaultCfrPreferencesRepository(
                        context = LocalContext.current,
                        lifecycleOwner = lifecycleOwner,
                        coroutineScope = lifecycleOwner.lifecycleScope,
                    ),
                    coroutineScope = lifecycleOwner.lifecycleScope,
                ),
            ),
        ),
//        gleanDebugToolsStore = GleanDebugToolsStore(
//            middlewares = listOf(
//                GleanDebugToolsMiddleware(
//                    gleanDebugToolsStorage = DefaultGleanDebugToolsStorage(),
//                    clipboardHandler = context.components.clipboardHandler,
//                    openDebugView = { debugViewLink ->
//                        val intent = Intent(Intent.ACTION_VIEW)
//                        intent.data = Uri.parse(debugViewLink)
//                        context.startActivity(intent)
//                    },
//                    showToast = { resId ->
//                        val toast = Toast.makeText(
//                            context,
//                            context.getString(resId),
//                            Toast.LENGTH_LONG,
//                        )
//                        toast.show()
//                    },
//                ),
//            ),
//        ),
        loginsStorage = loginsStorage,
        addressesDebugLocalesRepository = context.components.strictMode.resetAfter(StrictMode.allowThreadDiskReads()) {
            SharedPrefsAddressesDebugLocalesRepository(
                context,
            )
        },
        inactiveTabsEnabled = inactiveTabsEnabled,
    )
}

/**
 * Overlay for presenting Fenix-wide debugging content.
 *
 * @param browserStore [BrowserStore] used to access [BrowserState].
 * @param cfrToolsStore [CfrToolsStore] used to access [CfrToolsState].
 * @param gleanDebugToolsStore [GleanDebugToolsStore] used to access [GleanDebugToolsState].
 * @param loginsStorage [LoginsStorage] used to access logins for [LoginsTools].
 * @param addressesDebugLocalesRepository used to control storage for [AddressesTools].
 * @param inactiveTabsEnabled Whether the inactive tabs feature is enabled.
 */
@Composable
private fun FenixOverlay(
    browserStore: BrowserStore,
    cfrToolsStore: CfrToolsStore,
//    gleanDebugToolsStore: GleanDebugToolsStore,
    loginsStorage: LoginsStorage,
    addressesDebugLocalesRepository: AddressesDebugLocalesRepository,
    inactiveTabsEnabled: Boolean,
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    val debugDrawerStore = remember {
        DebugDrawerStore(
            middlewares = listOf(
                DebugDrawerNavigationMiddleware(
                    navController = navController,
                    scope = coroutineScope,
                ),
            ),
        )
    }

    val debugDrawerDestinations = remember {
        DebugDrawerRoute.generateDebugDrawerDestinations(
            debugDrawerStore = debugDrawerStore,
            browserStore = browserStore,
            cfrToolsStore = cfrToolsStore,
//            gleanDebugToolsStore = gleanDebugToolsStore,
            inactiveTabsEnabled = inactiveTabsEnabled,
            loginsStorage = loginsStorage,
            addressesDebugLocalesRepository = addressesDebugLocalesRepository,
        )
    }
    val drawerStatus by debugDrawerStore.observeAsState(initialValue = DrawerStatus.Closed) { state ->
        state.drawerStatus
    }

    FirefoxTheme(theme = Theme.getTheme(allowPrivateTheme = false)) {
        DebugOverlay(
            navController = navController,
            drawerStatus = drawerStatus,
            debugDrawerDestinations = debugDrawerDestinations,
            onDrawerOpen = {
                debugDrawerStore.dispatch(DebugDrawerAction.DrawerOpened)
            },
            onDrawerClose = {
                debugDrawerStore.dispatch(DebugDrawerAction.DrawerClosed)
            },
            onDrawerBackButtonClick = {
                debugDrawerStore.dispatch(DebugDrawerAction.OnBackPressed)
            },
        )
    }
}

@LightDarkPreview
@Composable
private fun FenixOverlayPreview() {
    val selectedTab = createTab("https://mozilla.org")
    FenixOverlay(
        browserStore = BrowserStore(
            BrowserState(selectedTabId = selectedTab.id, tabs = listOf(selectedTab)),
        ),
        cfrToolsStore = CfrToolsStore(),
//        gleanDebugToolsStore = GleanDebugToolsStore(),
        inactiveTabsEnabled = true,
        loginsStorage = FakeLoginsStorage(),
        addressesDebugLocalesRepository = FakeAddressesDebugLocalesRepository(),
    )
}
