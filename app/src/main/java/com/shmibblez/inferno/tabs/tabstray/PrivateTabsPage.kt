package com.shmibblez.inferno.tabs.tabstray

import androidx.compose.runtime.Composable
import mozilla.components.browser.state.state.TabSessionState

@Composable
internal fun PrivateTabsPage(
    activeTabId: String?,
    privateTabs: List<TabSessionState>,
    tabDisplayType: InfernoTabsTrayDisplayType,
    mode: InfernoTabsTrayMode,
    header: (@Composable () -> Unit)? = null,
    onTabClick: (tab: TabSessionState) -> Unit,
    onTabClose: (tab: TabSessionState) -> Unit,
    onTabMediaClick: (tab: TabSessionState) -> Unit,
    onTabMove: (String, String?, Boolean) -> Unit,
    onTabDragStart: () -> Unit,
    onTabLongClick: (TabSessionState) -> Unit,
) {
    var activeTabIndex = 0
    activeTabId.let {
        privateTabs.forEachIndexed { index, tab ->
            if (tab.id == activeTabId) {
                activeTabIndex = index
                return@forEachIndexed
            }
        }
    }
    when (tabDisplayType) {
        InfernoTabsTrayDisplayType.List -> {
            TabList(
                activeTabId = activeTabId,
                activeTabIndex = activeTabIndex,
                tabs = privateTabs,
                mode = mode,
                header = header,
                onTabClick = onTabClick,
                onTabClose = onTabClose,
                onTabMediaClick = onTabMediaClick,
                onTabMove = onTabMove,
                onTabDragStart = onTabDragStart,
                onTabLongClick = onTabLongClick,
            )
        }

        InfernoTabsTrayDisplayType.Grid -> {
            TabGrid(
                activeTabId = activeTabId,
                activeTabIndex = activeTabIndex,
                tabs = privateTabs,
                mode = mode,
                header = header,
                onTabClick = onTabClick,
                onTabClose = onTabClose,
                onTabMediaClick = onTabMediaClick,
                onTabMove = onTabMove,
                onTabDragStart = onTabDragStart,
                onTabLongClick = onTabLongClick,
            )
        }
    }
}