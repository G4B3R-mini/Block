package com.shmibblez.inferno.tabs.tabstray

import androidx.compose.runtime.Composable
import mozilla.components.browser.state.state.TabSessionState

@Composable
internal fun PrivateTabsPage(
    activeTabId: String?,
    privateTabs: List<TabSessionState>,
    tabDisplayType: InfernoTabsTrayDisplayType,
    mode: InfernoTabsTrayMode,
) {

}