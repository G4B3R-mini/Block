package com.shmibblez.inferno.tabs.tabstray

import androidx.compose.runtime.Composable
import com.shmibblez.inferno.proto.InfernoSettings
import mozilla.components.browser.state.state.TabSessionState

// todo: inactive tabs
@Composable
internal fun NormalTabsPage(
    activeTabId: String?,
    normalTabs: List<TabSessionState>,
    tabDisplayType: InfernoSettings.TabTrayStyle,
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
        normalTabs.forEachIndexed { index, tab ->
            if (tab.id == activeTabId) {
                activeTabIndex = index
                return@forEachIndexed
            }
        }
    }
    when (tabDisplayType) {
        InfernoSettings.TabTrayStyle.TAB_TRAY_LIST -> {
            TabList(
                activeTabId = activeTabId,
                activeTabIndex = activeTabIndex,
                tabs = normalTabs,
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

//        InfernoSettings.TabTrayStyle.TAB_TRAY_GRID -> {
//            TabGrid(
//                activeTabId = activeTabId,
//                activeTabIndex = activeTabIndex,
//                tabs = normalTabs,
//                mode = mode,
//                header = header,
//                onTabClick = onTabClick,
//                onTabClose = onTabClose,
//                onTabMediaClick = onTabMediaClick,
//                onTabMove = onTabMove,
//                onTabDragStart = onTabDragStart,
//                onTabLongClick = onTabLongClick,
//            )
//        }

        // fixme: for now while TabGrid is implemented
        else -> {
            TabList(
                activeTabId = activeTabId,
                activeTabIndex = activeTabIndex,
                tabs = normalTabs,
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