package com.shmibblez.inferno.tabs.tabstray

import androidx.compose.runtime.Composable
import mozilla.components.browser.state.state.recover.TabState

@Composable
internal fun RecentlyClosedTabsPage(
    recentlyClosedTabs: List<TabState>,
    mode: InfernoTabsTrayMode,
    header: (@Composable () -> Unit)? = null,
    onHistoryClick: () -> Unit,
    onTabClick: (tab: TabState) -> Unit,
    onTabClose: (tab: TabState) -> Unit,
    onTabLongClick: (TabState) -> Unit,
) {
    ClosedTabList(
        tabs = recentlyClosedTabs,
        mode = mode,
        header = header,
        onHistoryClick = onHistoryClick,
        onTabClick = onTabClick,
        onTabClose =onTabClose ,
        onTabLongClick =onTabLongClick,
    )
}