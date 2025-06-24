package com.shmibblez.inferno.tabs.tabstray

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.IconButton
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.menu.DropdownMenu
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.ext.newTab

private val ICON_PADDING = 16.dp
private val ICON_SIZE = 20.dp
private val MENU_ICON_SIZE = 16.dp

// todo: when implementing copy [TabsTrayFragment]
//   - top insets not working (overflowing into top status bar)
//   - dont close tab tray after swipe delete tab, just select prev one and stay

// todo:
//   - synced tabs
//   - recently closed tabs.

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfernoTabsTray(state: InfernoTabsTrayState) {
    val context = LocalContext.current
    val normalTabCount = state.normalTabs.size
    val privateTabCount = state.privateTabs.size
    val sheetState = rememberModalBottomSheetState()
    var initialized by remember { mutableStateOf(false) }

    if (!state.visible) return
    // request screenshot of active page before showing
    state.onRequestScreenshot()

    LaunchedEffect(null) {
        initialized = true
    }



    ModalBottomSheet(
        onDismissRequest = state::dismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(WindowInsets.statusBars.asPaddingValues(LocalDensity.current)),
        sheetState = sheetState,
        shape = RectangleShape,
        containerColor = context.infernoTheme().value.primaryBackgroundColor,
        scrimColor = context.infernoTheme().value.primaryBackgroundColor.copy(alpha = 0.5F),
        dragHandle = {
            // todo: drag handle as small as possible, box with centered rounded horizontal bar
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column {

                if (state.mode == InfernoTabsTrayMode.Normal) {
                    NormalBanner(
                        mode = state.mode,
                        setMode = state::setTabsTrayMode,
                        setSelectedTab = state::setSelectedTabsTrayTab,
                        selectedTab = state.selectedTrayTab,
                        normalTabCount = normalTabCount,
                        privateTabCount = privateTabCount,
                        onBookmarkSelectedTabsClick = state::onBookmarkSelectedTabsClick,
                        onDeleteSelectedTabsClick = state::onDeleteSelectedTabsClick,
                        onForceSelectedTabsAsInactiveClick = state::onForceSelectedTabsAsInactiveClick,
                        onTabSettingsClick = state::onTabSettingsClick,
                        onHistoryClick = state.onHistoryClick,
                        onShareAllTabsClick = state.onShareAllTabsClick,
                        onDeleteAllTabsClick = state::onDeleteAllTabsClick,
                        onAccountSettingsClick = state.onAccountSettingsClick,
                        onDeleteSelectedCloseTabsClick = state::onDeleteSelectedCloseTabsClick,
                    )
                } else {
                    SelectBanner(
                        mode = state.mode,
                        setMode = state::setTabsTrayMode,
                        selectedTab = state.selectedTrayTab,
                        normalTabCount = normalTabCount,
                        privateTabCount = privateTabCount,
                        onBookmarkSelectedTabsClick = state::onBookmarkSelectedTabsClick,
                        onDeleteSelectedTabsClick = state::onDeleteSelectedTabsClick,
                        onForceSelectedTabsAsInactiveClick = state::onForceSelectedTabsAsInactiveClick,
                        onTabSettingsClick = state::onTabSettingsClick,
                        onHistoryClick = state.onHistoryClick,
                        onShareAllTabsClick = state.onShareAllTabsClick,
                        onDeleteAllTabsClick = state::onDeleteAllTabsClick,
                        onAccountSettingsClick = state.onAccountSettingsClick,
                        onDeleteSelectedCloseTabsClick = state::onDeleteSelectedCloseTabsClick,
                    )
                }

                // corresponding tab page
                when (state.selectedTrayTab) {
                    InfernoTabsTraySelectedTab.NormalTabs -> NormalTabsPage(
                        activeTabId = state.selectedTabId,
                        normalTabs = state.normalTabs,
                        tabDisplayType = state.tabDisplayType,
                        mode = state.mode,
                        header = null, // todo
                        onTabClick = state::onTabClick,
                        onTabClose = state::onTabClose,
                        onTabMediaClick = state.onTabMediaClick,
                        onTabMove = state.onTabMove,
                        onTabDragStart = {
                            state.setTabsTrayMode(InfernoTabsTrayMode.Normal)
                        },
                        onTabLongClick = state::onTabLongClick,
                    )

                    InfernoTabsTraySelectedTab.PrivateTabs -> PrivateTabsPage(
                        activeTabId = state.selectedTabId,
                        privateTabs = state.privateTabs,
                        tabDisplayType = state.tabDisplayType,
                        mode = state.mode,
                        header = null, // todo
                        onTabClick = state::onTabClick,
                        onTabClose = state::onTabClose,
                        onTabMediaClick = state.onTabMediaClick,
                        onTabMove = state.onTabMove,
                        onTabDragStart = {
                            state.setTabsTrayMode(InfernoTabsTrayMode.Normal)
                        },
                        onTabLongClick = state::onTabLongClick,
                    )

                    InfernoTabsTraySelectedTab.SyncedTabs -> SyncedTabsPage(
                        activeTabId = state.selectedTabId,
                        syncedTabsStorage = context.components.backgroundServices.syncedTabsStorage,
                        accountManager = context.components.backgroundServices.accountManager,
                        commands = context.components.backgroundServices.syncedTabsCommands,
                        onTabClick = state::onSyncedTabClick,
                        onTabClose = state.onSyncedTabClose,
                        tabDisplayType = state.tabDisplayType,
                        mode = state.mode,
                    )

                    InfernoTabsTraySelectedTab.RecentlyClosedTabs -> RecentlyClosedTabsPage(
//            activeTabId = activeTabId,
                        recentlyClosedTabs = state.recentlyClosedTabs,
                        mode = state.mode,
                        header = null, // todo
                        onHistoryClick = state.onHistoryClick,
                        onTabClick = state::onClosedTabClick,
                        onTabClose = state.onClosedTabClose,
                        onTabLongClick = state::onClosedTabLongClick,
                    )
                }
                // extra item for button
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                )
            }
            NewTabButton(
                state.selectedTrayTab,
                activeTabId = state.selectedTabId,
                offset = if (initialized) -sheetState.requireOffset().toInt() else 0,
            )
        }
    }
}

private val BANNER_HEIGHT = 56.dp

@Composable
private fun SelectBanner(
    mode: InfernoTabsTrayMode,
    setMode: (InfernoTabsTrayMode) -> Unit,
    selectedTab: InfernoTabsTraySelectedTab,
    normalTabCount: Int,
    privateTabCount: Int,

    onBookmarkSelectedTabsClick: () -> Unit,
    onDeleteSelectedTabsClick: () -> Unit,
    onForceSelectedTabsAsInactiveClick: () -> Unit,
    onTabSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onShareAllTabsClick: () -> Unit,
    onDeleteAllTabsClick: () -> Unit,
    onAccountSettingsClick: () -> Unit,
    onDeleteSelectedCloseTabsClick: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    val selectedTabCount = when (mode) {
        is InfernoTabsTrayMode.Select -> {
            mode.selectedTabs.size
        }

        is InfernoTabsTrayMode.SelectClosed -> {
            mode.selectedClosedTabs.size
        }

        else -> {
            0
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalContext.current.infernoTheme().value.primaryActionColor)
            .height(BANNER_HEIGHT),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfernoIcon(
            painter = painterResource(R.drawable.ic_close_24),
            contentDescription = stringResource(android.R.string.cancel),
            modifier = Modifier
                .clickable { setMode.invoke(InfernoTabsTrayMode.Normal) }
                .padding(ICON_PADDING)
                .size(MENU_ICON_SIZE),
        )

        InfernoText(
            text = when (selectedTabCount > 0) {
                true -> "$selectedTabCount " + stringResource(R.string.tab_tray_multiselect_selected_content_description)
                false -> stringResource(R.string.tabs_tray_select_tabs)
            },
            modifier = Modifier.weight(1F),
        )

        IconButton(
            onClick = { showMenu = true },
            modifier = Modifier
                .padding(ICON_PADDING)
                .size(MENU_ICON_SIZE),
        ) {
            DropdownMenu(
                menuItems = mode.generateMenuItems(
//                    shouldShowInactiveButton = isInDebugMode,
                    onBookmarkSelectedTabsClick = onBookmarkSelectedTabsClick,
                    onCloseSelectedTabsClick = onDeleteSelectedTabsClick,
                    onMakeSelectedTabsInactive = onForceSelectedTabsAsInactiveClick,

                    selectedPage = selectedTab,
                    normalTabCount = normalTabCount,
                    privateTabCount = privateTabCount,
                    onTabSettingsClick = onTabSettingsClick,
                    onHistoryClick = onHistoryClick,
//                    onRecentlyClosedClick = onRecentlyClosedClick,
                    onEnterMultiselectModeClick = {
                        setMode(InfernoTabsTrayMode.Select(emptySet()))
                    },
                    onShareAllTabsClick = onShareAllTabsClick,
                    onDeleteAllTabsClick = onDeleteAllTabsClick,
                    onAccountSettingsClick = onAccountSettingsClick,
                    onDeleteSelectedCloseTabsClick = onDeleteSelectedCloseTabsClick,
                ),
                expanded = showMenu,
                offset = DpOffset(x = 0.dp, y = -ICON_SIZE),
                onDismissRequest = {
                    showMenu = false
                },
            )
            InfernoIcon(
                painter = painterResource(R.drawable.ic_menu_24),
                contentDescription = stringResource(id = R.string.open_tabs_menu),
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterVertically),
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalBanner(
    mode: InfernoTabsTrayMode,
    setMode: (InfernoTabsTrayMode) -> Unit,
    setSelectedTab: (InfernoTabsTraySelectedTab) -> Unit,
    selectedTab: InfernoTabsTraySelectedTab,
    normalTabCount: Int,
    privateTabCount: Int,

    onBookmarkSelectedTabsClick: () -> Unit,
    onDeleteSelectedTabsClick: () -> Unit,
    onForceSelectedTabsAsInactiveClick: () -> Unit,
    onTabSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onShareAllTabsClick: () -> Unit,
    onDeleteAllTabsClick: () -> Unit,
    onAccountSettingsClick: () -> Unit,
    onDeleteSelectedCloseTabsClick: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(BANNER_HEIGHT)
            .background(LocalContext.current.infernoTheme().value.primaryBackgroundColor),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier
                .weight(1F)
                .wrapContentHeight(),
            divider = {},
            containerColor = Color.Transparent,
            contentColor = LocalContext.current.infernoTheme().value.primaryActionColor,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTab.ordinal, matchContentSize = true),
                    width = 32.dp,
                )
            },
            tabs = {
                NormalTabsIcon(
                    selected = selectedTab == InfernoTabsTraySelectedTab.NormalTabs,
                    onSelected = {
                        setSelectedTab.invoke(InfernoTabsTraySelectedTab.NormalTabs)
                    },
                    count = normalTabCount,
                )
                PrivateTabsIcon(
                    selected = selectedTab == InfernoTabsTraySelectedTab.PrivateTabs,
                    onSelected = {
                        setSelectedTab.invoke(InfernoTabsTraySelectedTab.PrivateTabs)
                    },
                )
                SyncedTabsIcon(
                    selected = selectedTab == InfernoTabsTraySelectedTab.SyncedTabs,
                    onSelected = {
                        setSelectedTab.invoke(InfernoTabsTraySelectedTab.SyncedTabs)
                    },
                )
                RecentlyClosedTabsIcon(
                    selected = selectedTab == InfernoTabsTraySelectedTab.RecentlyClosedTabs,
                    onSelected = {
                        setSelectedTab.invoke(InfernoTabsTraySelectedTab.RecentlyClosedTabs)
                    },
                )
            },
        )
        VerticalDivider(
            thickness = 1.dp,
            color = LocalContext.current.infernoTheme().value.primaryIconColor,
            modifier = Modifier.height(24.dp),
        )
        IconButton(
            onClick = { showMenu = true },
            modifier = Modifier
                .padding(ICON_PADDING)
                .size(MENU_ICON_SIZE),
        ) {
            DropdownMenu(
                menuItems = mode.generateMenuItems(
//                    shouldShowInactiveButton = isInDebugMode,
                    onBookmarkSelectedTabsClick = onBookmarkSelectedTabsClick,
                    onCloseSelectedTabsClick = onDeleteSelectedTabsClick,
                    onMakeSelectedTabsInactive = onForceSelectedTabsAsInactiveClick,

                    selectedPage = selectedTab,
                    normalTabCount = normalTabCount,
                    privateTabCount = privateTabCount,
                    onTabSettingsClick = onTabSettingsClick,
                    onHistoryClick = onHistoryClick,
//                    onRecentlyClosedClick = onRecentlyClosedClick,
                    onEnterMultiselectModeClick = {
                        setMode(InfernoTabsTrayMode.Select(emptySet()))
                    },
                    onShareAllTabsClick = onShareAllTabsClick,
                    onDeleteAllTabsClick = onDeleteAllTabsClick,
                    onAccountSettingsClick = onAccountSettingsClick,
                    onDeleteSelectedCloseTabsClick = onDeleteSelectedCloseTabsClick
                ),
                expanded = showMenu,
                offset = DpOffset(x = 0.dp, y = -ICON_SIZE),
                onDismissRequest = {
                    showMenu = false
                },
            )
            InfernoIcon(
                painter = painterResource(R.drawable.ic_menu_24),
                contentDescription = stringResource(id = R.string.open_tabs_menu),
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterVertically),
            )
        }
    }
}

@Composable
private fun BoxScope.NewTabButton(
    selectedTab: InfernoTabsTraySelectedTab,
    activeTabId: String?,
    offset: Int,
) {
    val context = LocalContext.current
    val onClick: () -> Unit = when (selectedTab) {
        InfernoTabsTraySelectedTab.NormalTabs -> {
            { context.components.newTab(private = false, nextTo = activeTabId) }
        }

        InfernoTabsTraySelectedTab.PrivateTabs -> {
            { context.components.newTab(private = true) }
        }

        InfernoTabsTraySelectedTab.SyncedTabs -> {
            // todo
            {}
        }

        InfernoTabsTraySelectedTab.RecentlyClosedTabs -> {
            // todo
            {}
        }
    }
    val text = when (selectedTab) {
        InfernoTabsTraySelectedTab.NormalTabs -> {
            stringResource(R.string.mozac_browser_menu_new_tab)
        }

        InfernoTabsTraySelectedTab.PrivateTabs -> {
            stringResource(R.string.mozac_browser_menu_new_private_tab)
        }

        InfernoTabsTraySelectedTab.SyncedTabs -> {
            // todo
            ""
        }

        InfernoTabsTraySelectedTab.RecentlyClosedTabs -> {
            // todo
            ""
        }
    }
    when (selectedTab) {
        InfernoTabsTraySelectedTab.NormalTabs, InfernoTabsTraySelectedTab.PrivateTabs -> {
            ExtendedFloatingActionButton(
                onClick = onClick,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd)
                    .offset {
                        Log.d("NewTabButton", "offset: $offset")
                        IntOffset(x = 0, y = offset)
                    },
                icon = {
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_new_24),
                        contentDescription = "add tab",
                        modifier = Modifier
//                            .padding(ICON_PADDING)
                            .size(20.dp),
                    )
                },
                text = { InfernoText(text = text) },
                containerColor = LocalContext.current.infernoTheme().value.primaryActionColor,
            )
        }

        InfernoTabsTraySelectedTab.SyncedTabs -> {/* todo */
        }

        InfernoTabsTraySelectedTab.RecentlyClosedTabs -> {/* todo */
        }
    }
}

@Composable
private fun NormalTabsIcon(selected: Boolean, onSelected: () -> Unit, count: Int) {
    Tab(
        selected = selected,
        onClick = onSelected,
        modifier = Modifier
            .padding(ICON_PADDING)
            .size(ICON_SIZE),
        icon = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight(unbounded = true),
                contentAlignment = Alignment.Center,
            ) {
                InfernoIcon(
                    painter = painterResource(R.drawable.ic_tabcounter_box_24),
                    contentDescription = "normal tabs",
                    modifier = Modifier.fillMaxSize(),
                )
                InfernoText(
                    text = "$count",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontColor = LocalContext.current.infernoTheme().value.primaryIconColor,
                )
            }
        },
    )
}

@Composable
private fun PrivateTabsIcon(selected: Boolean, onSelected: () -> Unit) {
    Tab(
        selected = selected,
        onClick = onSelected,
        modifier = Modifier
            .padding(ICON_PADDING)
            .size(ICON_SIZE),
        icon = {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_private_browsing_24),
                contentDescription = "private tabs",
                modifier = Modifier.fillMaxSize(),
            )
        },
    )
}

@Composable
private fun SyncedTabsIcon(selected: Boolean, onSelected: () -> Unit) {
    Tab(
        selected = selected,
        onClick = onSelected,
        modifier = Modifier
            .padding(ICON_PADDING)
            .size(ICON_SIZE),
        icon = {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_synced_tabs),
                contentDescription = "private tabs", // todo: string res
                modifier = Modifier.fillMaxSize(),
            )
        },
    )
}

@Composable
private fun RecentlyClosedTabsIcon(selected: Boolean, onSelected: () -> Unit) {
    Tab(
        selected = selected,
        onClick = onSelected,
        modifier = Modifier
            .padding(ICON_PADDING)
            .size(ICON_SIZE),
        icon = {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_delete_24),
                contentDescription = "private tabs", // todo: string res
                modifier = Modifier.fillMaxSize(),
            )
        },
    )
}