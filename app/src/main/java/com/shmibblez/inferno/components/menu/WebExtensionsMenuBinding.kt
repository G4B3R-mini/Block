/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.components.menu

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.WebExtensionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.webextension.Action
import mozilla.components.lib.state.Store
import mozilla.components.lib.state.helpers.AbstractBinding
import com.shmibblez.inferno.components.menu.compose.WebExtensionMenuItem
import com.shmibblez.inferno.components.menu.store.MenuAction
import com.shmibblez.inferno.components.menu.store.MenuState
import com.shmibblez.inferno.components.menu.store.MenuStore
import com.shmibblez.inferno.components.menu.store.WebExtensionMenuItem
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Helper for observing Extension state from both [BrowserState.extensions]
 * and [TabSessionState.extensionState].
 *
 * @param browserStore Used to listen for changes to [WebExtensionState].
 * @param menuStore The [Store] for holding the [MenuState] and applying [MenuAction]s.
 * @param iconSize for [WebExtensionMenuItem].
 * @param onDismiss Callback invoked to dismiss the menu dialog.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WebExtensionsMenuBinding(
    browserStore: BrowserStore,
    private val menuStore: MenuStore,
    private val iconSize: Int,
    private val onDismiss: () -> Unit,
) : AbstractBinding<BrowserState>(browserStore) {

    override suspend fun onState(flow: Flow<BrowserState>) {
        // InfernoBrowser level flows
        val browserFlow = flow.mapNotNull { state -> state }
            .distinctUntilChangedBy {
                it.extensions
            }

        // Session level flows
        val sessionFlow = flow.mapNotNull { state -> state.selectedTab }
            .distinctUntilChangedBy {
                it.extensionState
            }

        // Applying the flows together
        sessionFlow
            .combine(browserFlow) { sessionState, browserState ->
                WebExtensionsFlowState(
                    sessionState,
                    browserState,
                )
            }
            .collect { webExtensionsFlowState ->
                val eligibleExtensions = webExtensionsFlowState.browserState.extensions.values
                    .filterNot {
                        !it.allowedInPrivateBrowsing &&
                            webExtensionsFlowState.sessionState.content.private
                    }
                    .sortedBy { it.name }

                val browserWebExtensionMenuItems = eligibleExtensions.mapNotNull { extension ->
                    extension.browserAction?.let { browserAction ->
                        getWebExtensionMenuItem(
                            extension = extension,
                            webExtensionsFlowState = webExtensionsFlowState,
                            globalAction = browserAction,
                        )
                    }
                }

                if (browserWebExtensionMenuItems.isEmpty() && eligibleExtensions.filter { !it.isBuiltIn }
                        .all { !it.enabled }
                ) {
                    menuStore.dispatch(MenuAction.UpdateShowDisabledExtensionsOnboarding(true))
                } else {
                    menuStore.dispatch(MenuAction.UpdateShowDisabledExtensionsOnboarding(false))
                }

                menuStore.dispatch(
                    MenuAction.UpdateWebExtensionBrowserMenuItems(browserWebExtensionMenuItems),
                )
            }
    }

    @Suppress("ReturnCount")
    private suspend fun getWebExtensionMenuItem(
        extension: WebExtensionState,
        webExtensionsFlowState: WebExtensionsFlowState,
        globalAction: Action,
    ): WebExtensionMenuItem? {
        if (!extension.enabled) {
            return null
        }

        val tabAction = webExtensionsFlowState.sessionState.extensionState[extension.id]?.browserAction

        // Apply tab-specific override of browser/page action
        val action = tabAction?.let {
            globalAction.copyWithOverride(it)
        } ?: globalAction

        val title = action.title ?: return null

        val loadIcon = action.loadIcon?.invoke(iconSize)

        return WebExtensionMenuItem(
            label = title,
            enabled = action.enabled,
            icon = loadIcon,
            badgeText = action.badgeText,
            badgeTextColor = action.badgeTextColor,
            badgeBackgroundColor = action.badgeBackgroundColor,
            onClick = {
                onDismiss()
                action.onClick()
            },
        )
    }
}

/**
 * Convenience method to create a named pair for the web extensions flow.
 *
 * @property sessionState The session or tab state.
 * @property browserState The browser or global state.
 */
private data class WebExtensionsFlowState(
    val sessionState: TabSessionState,
    val browserState: BrowserState,
)
