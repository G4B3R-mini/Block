package com.shmibblez.inferno.tabs.tabstray

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.menu.DropdownMenu
import com.shmibblez.inferno.compose.menu.MenuItem
import com.shmibblez.inferno.compose.text.Text
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.newTab
import com.shmibblez.inferno.tabstray.TabsTrayState.Mode
import com.shmibblez.inferno.tabstray.TabsTrayTestTag
import com.shmibblez.inferno.tabstray.syncedtabs.SyncedTabsListItem
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.recover.TabState
import mozilla.components.browser.storage.sync.Tab

enum class InfernoTabsTraySelectedTab {
    NormalTabs, PrivateTabs, SyncedTabs, RecentlyClosedTabs,
}

enum class InfernoTabsTrayDisplayType {
    List, Grid,
}

open class InfernoTabsTrayMode {
    open val selectedTabs = emptySet<TabSessionState>()

    data object Normal : InfernoTabsTrayMode()

    data class Select(override val selectedTabs: Set<TabSessionState>) : InfernoTabsTrayMode()

    /**
     * A helper to check if we're in [Mode.Select] mode.
     */
    fun isSelect() = this is Select

    /**
     * A helper to check if we're in [Mode.Normal] mode.
     */
    fun isNormal() = this is Normal

    /**
     * Returns the list of menu items corresponding to the selected mode
     *
    //     * @param shouldShowInactiveButton Whether or not to show the inactive tabs menu item.
     * @param selectedPage The currently selected page.
     * @param normalTabCount The normal tabs number.
     * @param privateTabCount The private tabs number.
     * @param onBookmarkSelectedTabsClick Invoked when user interacts with the bookmark menu item.
     * @param onCloseSelectedTabsClick Invoked when user interacts with the close menu item.
     * @param onMakeSelectedTabsInactive Invoked when user interacts with the make inactive menu item.
     * @param onTabSettingsClick Invoked when user interacts with the tab settings menu.
    //     * @param onRecentlyClosedClick Invoked when user interacts with the recently closed menu item.
     * @param onEnterMultiselectModeClick Invoked when user enters the multiselect mode.
     * @param onShareAllTabsClick Invoked when user interacts with the share all menu item.
     * @param onDeleteAllTabsClick Invoked when user interacts with the delete all menu item.
     * @param onAccountSettingsClick Invoked when user interacts with the account settings.
     */
    fun generateMenuItems(
//        shouldShowInactiveButton: Boolean,
        selectedPage: InfernoTabsTraySelectedTab,
        normalTabCount: Int,
        privateTabCount: Int,
        onBookmarkSelectedTabsClick: () -> Unit,
        onCloseSelectedTabsClick: () -> Unit,
        onMakeSelectedTabsInactive: () -> Unit,
        onTabSettingsClick: () -> Unit,
//        onRecentlyClosedClick: () -> Unit,
        onEnterMultiselectModeClick: () -> Unit,
        onShareAllTabsClick: () -> Unit,
        onDeleteAllTabsClick: () -> Unit,
        onAccountSettingsClick: () -> Unit,
    ): List<MenuItem> {
        return if (this.isSelect()) {
            generateMultiSelectBannerMenuItems(
//                shouldShowInactiveButton = shouldShowInactiveButton,
                onBookmarkSelectedTabsClick = onBookmarkSelectedTabsClick,
                onCloseSelectedTabsClick = onCloseSelectedTabsClick,
                onMakeSelectedTabsInactive = onMakeSelectedTabsInactive,
            )
        } else {
            generateTabPageBannerMenuItems(
                selectedPage = selectedPage,
                normalTabCount = normalTabCount,
                privateTabCount = privateTabCount,
                onTabSettingsClick = onTabSettingsClick,
//                onRecentlyClosedClick = onRecentlyClosedClick,
                onEnterMultiselectModeClick = onEnterMultiselectModeClick,
                onShareAllTabsClick = onShareAllTabsClick,
                onDeleteAllTabsClick = onDeleteAllTabsClick,
                onAccountSettingsClick = onAccountSettingsClick,
            )
        }
    }

    /**
     *  Builds the menu items list when in multiselect mode
     */
    private fun generateMultiSelectBannerMenuItems(
//        shouldShowInactiveButton: Boolean,
        onBookmarkSelectedTabsClick: () -> Unit,
        onCloseSelectedTabsClick: () -> Unit,
        onMakeSelectedTabsInactive: () -> Unit,
    ): List<MenuItem> {
        val menuItems = mutableListOf(
            MenuItem.TextItem(
                text = Text.Resource(R.string.tab_tray_multiselect_menu_item_bookmark),
                onClick = onBookmarkSelectedTabsClick,
            ),
            MenuItem.TextItem(
                text = Text.Resource(R.string.tab_tray_multiselect_menu_item_close),
                onClick = onCloseSelectedTabsClick,
            ),
        )
//        if (shouldShowInactiveButton) {
        menuItems.add(
            MenuItem.TextItem(
                text = Text.Resource(R.string.inactive_tabs_menu_item),
                onClick = onMakeSelectedTabsInactive,
            ),
        )
//        }
        return menuItems
    }

    /**
     *  Builds the menu items list when in normal mode
     */
    @Suppress("LongParameterList")
    private fun generateTabPageBannerMenuItems(
        selectedPage: InfernoTabsTraySelectedTab,
        normalTabCount: Int,
        privateTabCount: Int,
        onTabSettingsClick: () -> Unit,
//        onRecentlyClosedClick: () -> Unit,
        onEnterMultiselectModeClick: () -> Unit,
        onShareAllTabsClick: () -> Unit,
        onDeleteAllTabsClick: () -> Unit,
        onAccountSettingsClick: () -> Unit,
    ): List<MenuItem> {
        val tabSettingsItem = MenuItem.TextItem(
            text = Text.Resource(R.string.tab_tray_menu_tab_settings),
            testTag = TabsTrayTestTag.tabSettings,
            onClick = onTabSettingsClick,
        )
//        val recentlyClosedTabsItem = MenuItem.TextItem(
//            text = Text.Resource(R.string.tab_tray_menu_recently_closed),
//            testTag = TabsTrayTestTag.recentlyClosedTabs,
//            onClick = onRecentlyClosedClick,
//        )
        val enterSelectModeItem = MenuItem.TextItem(
            text = Text.Resource(R.string.tabs_tray_select_tabs),
            testTag = TabsTrayTestTag.selectTabs,
            onClick = onEnterMultiselectModeClick,
        )
        val shareAllTabsItem = MenuItem.TextItem(
            text = Text.Resource(R.string.tab_tray_menu_item_share),
            testTag = TabsTrayTestTag.shareAllTabs,
            onClick = onShareAllTabsClick,
        )
        val deleteAllTabsItem = MenuItem.TextItem(
            text = Text.Resource(R.string.tab_tray_menu_item_close),
            testTag = TabsTrayTestTag.closeAllTabs,
            onClick = onDeleteAllTabsClick,
        )
        val accountSettingsItem = MenuItem.TextItem(
            text = Text.Resource(R.string.tab_tray_menu_account_settings),
            testTag = TabsTrayTestTag.accountSettings,
            onClick = onAccountSettingsClick,
        )
        return when {
            selectedPage == InfernoTabsTraySelectedTab.NormalTabs && (normalTabCount == 0 || privateTabCount == 0) -> listOf(
                tabSettingsItem,
//                recentlyClosedTabsItem,
            )

            selectedPage == InfernoTabsTraySelectedTab.NormalTabs -> listOf(
                enterSelectModeItem,
                shareAllTabsItem,
                tabSettingsItem,
//                recentlyClosedTabsItem,
                deleteAllTabsItem,
            )

            selectedPage == InfernoTabsTraySelectedTab.PrivateTabs -> listOf(
                tabSettingsItem,
//                recentlyClosedTabsItem,
                deleteAllTabsItem,
            )

            selectedPage == InfernoTabsTraySelectedTab.SyncedTabs -> listOf(
                accountSettingsItem,
//                recentlyClosedTabsItem,
            )

            else -> emptyList()
        }
    }

}

private val ICON_PADDING = 16.dp
private val ICON_SIZE = 20.dp
private val MENU_ICON_SIZE = 16.dp

// todo: when implementing copy [TabsTrayFragment]
//   - bug: tab not removed after swipe left, or when click on x, must be some problem with state
//   - top insets not working (overflowing into top status bar)
//   - add drag handle (small one)
//   - dont close tab tray after swipe delete tab, just select prev one and stay

// todo:
//   - synced tabs
//   - recently closed tabs.

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfernoTabsTray(
    dismiss: () -> Unit,
    mode: InfernoTabsTrayMode,
    setMode: (InfernoTabsTrayMode) -> Unit,
    activeTabId: String?,
    normalTabs: List<TabSessionState>,
    privateTabs: List<TabSessionState>,
    syncedTabs: List<SyncedTabsListItem>,
    recentlyClosedTabs: List<TabState>,
    tabDisplayType: InfernoTabsTrayDisplayType = InfernoTabsTrayDisplayType.List,
    initiallySelectedTab: InfernoTabsTraySelectedTab = InfernoTabsTraySelectedTab.NormalTabs,

    onBookmarkSelectedTabsClick: () -> Unit,
    onDeleteSelectedTabsClick: () -> Unit,
    onForceSelectedTabsAsInactiveClick: () -> Unit,
    onTabSettingsClick: () -> Unit,
    onShareAllTabsClick: () -> Unit,
    onDeleteAllTabsClick: () -> Unit,
    onAccountSettingsClick: () -> Unit,

    onTabClick: (tab: TabSessionState) -> Unit,
    onTabClose: (tab: TabSessionState) -> Unit,
    onTabMediaClick: (tab: TabSessionState) -> Unit,
    onTabMove: (String, String?, Boolean) -> Unit,
    onTabLongClick: (TabSessionState) -> Unit,

    onSyncedTabClick: (tab: Tab) -> Unit,
    onSyncedTabClose: (deviceId: String, tab: Tab) -> Unit,

    ) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(initiallySelectedTab) }
    val normalTabCount by remember { derivedStateOf { privateTabs.size } }
    val privateTabCount by remember { derivedStateOf { privateTabs.size } }
    val sheetState = rememberModalBottomSheetState()
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(null) {
        initialized = true
    }

    ModalBottomSheet(
        onDismissRequest = dismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(WindowInsets.statusBars.asPaddingValues(LocalDensity.current)),
        sheetState = sheetState,
        shape = RectangleShape,
        containerColor = Color.Black, // todo: set color from theme
        scrimColor = Color.Black.copy(alpha = 0.25F),
        dragHandle = {
            // todo: drag handle as small as possible, box with centered rounded horizontal bar
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PrimaryTabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        modifier = Modifier
                            .weight(1F)
                            .wrapContentHeight(),
                        divider = {},
                        containerColor = Color.Black,
                        contentColor = Color(
                            143, 0, 255
                        ), // todo: purple color, add to FirefoxTheme as iconActive
                        tabs = {
                            NormalTabsIcon(
                                selected = selectedTab == InfernoTabsTraySelectedTab.NormalTabs,
                                onSelected = {
                                    selectedTab = InfernoTabsTraySelectedTab.NormalTabs
                                },
                                count = normalTabs.size,
                            )
                            PrivateTabsIcon(
                                selected = selectedTab == InfernoTabsTraySelectedTab.PrivateTabs,
                                onSelected = {
                                    selectedTab = InfernoTabsTraySelectedTab.PrivateTabs
                                },
                            )
                            SyncedTabsIcon(
                                selected = selectedTab == InfernoTabsTraySelectedTab.SyncedTabs,
                                onSelected = {
                                    selectedTab = InfernoTabsTraySelectedTab.SyncedTabs
                                },
                            )
                            RecentlyClosedTabsIcon(
                                selected = selectedTab == InfernoTabsTraySelectedTab.RecentlyClosedTabs,
                                onSelected = {
                                    selectedTab = InfernoTabsTraySelectedTab.RecentlyClosedTabs
                                },
                            )
                        },
                    )
                    VerticalDivider(
                        thickness = 1.dp,
                        color = Color.White,
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
//                    onRecentlyClosedClick = onRecentlyClosedClick,
                                onEnterMultiselectModeClick = {
                                    setMode(InfernoTabsTrayMode.Select(emptySet()))
                                },
                                onShareAllTabsClick = onShareAllTabsClick,
                                onDeleteAllTabsClick = onDeleteAllTabsClick,
                                onAccountSettingsClick = onAccountSettingsClick,
                            ),
                            expanded = showMenu,
                            offset = DpOffset(x = 0.dp, y = -ICON_SIZE),
                            onDismissRequest = {
                                showMenu = false
                            },
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = stringResource(id = R.string.open_tabs_menu),
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.CenterVertically),
                            tint = Color.White,
                        )
                    }
                }

                // corresponding tab page
                when (selectedTab) {
                    InfernoTabsTraySelectedTab.NormalTabs -> NormalTabsPage(
                        activeTabId = activeTabId,
                        normalTabs = normalTabs,
                        tabDisplayType = tabDisplayType,
                        mode = mode,
                        header = null, // todo
                        onTabClick = onTabClick,
                        onTabClose = onTabClose,
                        onTabMediaClick = onTabMediaClick,
                        onTabMove = onTabMove,
                        onTabDragStart = {
                            setMode(InfernoTabsTrayMode.Normal)
                        },
                        onTabLongClick = onTabLongClick,
                    )

                    InfernoTabsTraySelectedTab.PrivateTabs -> PrivateTabsPage(
                        activeTabId = activeTabId,
                        privateTabs = privateTabs,
                        tabDisplayType = tabDisplayType,
                        mode = mode,
                        header = null, // todo
                        onTabClick = onTabClick,
                        onTabClose = onTabClose,
                        onTabMediaClick = onTabMediaClick,
                        onTabMove = onTabMove,
                        onTabDragStart = {
                            setMode(InfernoTabsTrayMode.Normal)
                        },
                        onTabLongClick = onTabLongClick,
                    )

                    InfernoTabsTraySelectedTab.SyncedTabs -> SyncedTabsPage(
                        activeTabId = activeTabId,
                        syncedTabsStorage = context.components.backgroundServices.syncedTabsStorage,
                        accountManager = context.components.backgroundServices.accountManager,
                        commands = context.components.backgroundServices.syncedTabsCommands,
                        onTabClick = onSyncedTabClick,
                        onTabClose = onSyncedTabClose,
                        tabDisplayType = tabDisplayType,
                        mode = mode,
                    )

                    InfernoTabsTraySelectedTab.RecentlyClosedTabs -> RecentlyClosedTabsPage(
//            activeTabId = activeTabId,
                        recentlyClosedTabs = recentlyClosedTabs,
                        tabDisplayType = tabDisplayType,
                        mode = mode,
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
                selectedTab,
                activeTabId = activeTabId,
                offset = if (initialized) -sheetState.requireOffset().toInt() else 0,
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
            { context.components.newTab(isPrivateSession = false, nextTo = activeTabId) }
        }

        InfernoTabsTraySelectedTab.PrivateTabs -> {
            { context.components.newTab(isPrivateSession = true) }
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
                    Icon(
                        painter = painterResource(R.drawable.ic_new),
                        contentDescription = "add tab",
                        tint = Color.White,
                        modifier = Modifier
//                            .padding(ICON_PADDING)
                            .size(20.dp),
                    )
                },
                text = { InfernoText(text = text) },
                containerColor = Color(
                    143, 0, 255
                ), // todo: purple color, add to FirefoxTheme as iconActive
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
                Icon(
                    painter = painterResource(R.drawable.ic_tabcounter_box_24),
                    contentDescription = "normal tabs",
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.White,
                )
                InfernoText(
                    text = "$count",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontColor = Color.White,
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
            Icon(
                painter = painterResource(R.drawable.ic_private_browsing),
                contentDescription = "private tabs",
                modifier = Modifier.fillMaxSize(),
                tint = Color.White,
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
            Icon(
                painter = painterResource(R.drawable.ic_synced_tabs),
                contentDescription = "private tabs",
                modifier = Modifier.fillMaxSize(),
                tint = Color.White,
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
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = "private tabs",
                modifier = Modifier.fillMaxSize(),
                tint = Color.White,
            )
        },
    )
}