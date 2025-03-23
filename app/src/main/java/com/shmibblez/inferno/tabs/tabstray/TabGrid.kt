package com.shmibblez.inferno.tabs.tabstray

import androidx.compose.runtime.Composable
import mozilla.components.browser.state.state.TabSessionState

@Composable
fun TabGrid(
    activeTabId: String?,
    activeTabIndex: Int,
    tabs: List<TabSessionState>,
    mode: InfernoTabsTrayMode,
    header: (@Composable () -> Unit)? = null,
    onTabClick: (tab: TabSessionState) -> Unit,
    onTabClose: (tab: TabSessionState) -> Unit,
    onTabMediaClick: (tab: TabSessionState) -> Unit,
    onTabMove: (String, String?, Boolean) -> Unit,
    onTabDragStart: () -> Unit,
    onTabLongClick: (TabSessionState) -> Unit,
) {
    TODO()
}