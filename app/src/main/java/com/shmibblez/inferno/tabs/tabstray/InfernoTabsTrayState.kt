package com.shmibblez.inferno.tabs.tabstray

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.selector.privateTabs
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.recover.TabState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.storage.sync.Tab
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature

@Composable
fun rememberInfernoTabsTrayState(
    store: BrowserStore = LocalContext.current.components.core.store,
    initiallyVisible: Boolean = false,
    initialMode: InfernoTabsTrayMode = InfernoTabsTrayMode.Normal,
    initiallySelectedTab: InfernoTabsTraySelectedTab = InfernoTabsTraySelectedTab.NormalTabs,
    onRequestScreenshot: () -> Unit,

    onBookmarkSelectedTabsClick: (selectedTabs: Set<TabSessionState>, dismiss: () -> Unit) -> Unit,
    onDeleteSelectedTabsClick: (selectedTabs: Set<TabSessionState>, dismiss: () -> Unit) -> Unit,
    onForceSelectedTabsAsInactiveClick: (selectedTabs: Set<TabSessionState>, dismiss: () -> Unit) -> Unit,
    onTabSettingsClick: (selectedTabs: Set<TabSessionState>, dismiss: () -> Unit) -> Unit,
    onHistoryClick: () -> Unit,
    onShareAllTabsClick: () -> Unit,
    onDeleteAllTabsClick: (private: Boolean) -> Unit,
    onAccountSettingsClick: () -> Unit,

    onTabClick: (tab: TabSessionState, currentMode: InfernoTabsTrayMode, enableSelect: (Set<TabSessionState>) -> Unit, dismiss: () -> Unit) -> Unit,
    onTabClose: (tab: TabSessionState, dismiss: () -> Unit, setSelectedTabTrayTab: (InfernoTabsTraySelectedTab) -> Unit) -> Unit,
    onTabMediaClick: (tab: TabSessionState) -> Unit,
    onTabMove: (String, String?, Boolean) -> Unit,
    onTabLongClick: (tab: TabSessionState, currentMode: InfernoTabsTrayMode, private: Boolean, enableSelect: (Set<TabSessionState>) -> Unit) -> Unit,

    onSyncedTabClick: (tab: Tab, dismiss: () -> Unit) -> Unit,
    onSyncedTabClose: (deviceId: String, tab: Tab) -> Unit,

    onClosedTabClick: (TabState, currentMode: InfernoTabsTrayMode, enableSelectClosed: (Set<TabState>) -> Unit, dismiss: () -> Unit) -> Unit,
    onClosedTabClose: (TabState) -> Unit,
    onClosedTabLongClick: (TabState, mode: InfernoTabsTrayMode, enableSelectClosed: (Set<TabState>) -> Unit) -> Unit,
    onDeleteSelectedCloseTabsClick: (mode: InfernoTabsTrayMode, dismiss: () -> Unit) -> Unit,
): MutableState<InfernoTabsTrayState> {
    val settings by LocalContext.current.infernoSettingsDataStore.data.collectAsState(
        initial = InfernoSettings.getDefaultInstance(),
    )

    val state = remember {
        mutableStateOf(
            InfernoTabsTrayState(
                store = store,
                initiallyVisible = initiallyVisible,
                initialMode = initialMode,
                initialTabDisplayType = settings.tabTrayStyle,
                initiallySelectedTab = initiallySelectedTab,
                onRequestScreenshot = onRequestScreenshot,
                onBookmarkSelectedTabsClick = onBookmarkSelectedTabsClick,
                onDeleteSelectedTabsClick = onDeleteSelectedTabsClick,
                onForceSelectedTabsAsInactiveClick = onForceSelectedTabsAsInactiveClick,
                onTabSettingsClick = onTabSettingsClick,
                onHistoryClick = onHistoryClick,
                onShareAllTabsClick = onShareAllTabsClick,
                onDeleteAllTabsClick = onDeleteAllTabsClick,
                onAccountSettingsClick = onAccountSettingsClick,
                onTabClick = onTabClick,
                onTabClose = onTabClose,
                onTabMediaClick = onTabMediaClick,
                onTabMove = onTabMove,
                onTabLongClick = onTabLongClick,
                onSyncedTabClick = onSyncedTabClick,
                onSyncedTabClose = onSyncedTabClose,
                onClosedTabClick = onClosedTabClick,
                onClosedTabClose = onClosedTabClose,
                onClosedTabLongClick = onClosedTabLongClick,
                onDeleteSelectedCloseTabsClick = onDeleteSelectedCloseTabsClick,
            )
        )
    }

    LaunchedEffect(settings.tabTrayStyle) {
        state.value.setTabTrayStyle(settings.tabTrayStyle)
    }

    DisposableEffect(null) {
        state.value.start()
        onDispose { state.value.stop() }
    }

    return state
}

class InfernoTabsTrayState(
    private val store: BrowserStore,
    initiallyVisible: Boolean = false,
    initialMode: InfernoTabsTrayMode = InfernoTabsTrayMode.Normal,
    initialTabDisplayType: InfernoSettings.TabTrayStyle = InfernoSettings.TabTrayStyle.TAB_TRAY_LIST,
    initiallySelectedTab: InfernoTabsTraySelectedTab = InfernoTabsTraySelectedTab.NormalTabs,
    val onRequestScreenshot: () -> Unit,

    private val onBookmarkSelectedTabsClick: (selectedTabs: Set<TabSessionState>, dismiss: () -> Unit) -> Unit,
    private val onDeleteSelectedTabsClick: (selectedTabs: Set<TabSessionState>, dismiss: () -> Unit) -> Unit,
    private val onForceSelectedTabsAsInactiveClick: (selectedTabs: Set<TabSessionState>, dismiss: () -> Unit) -> Unit,
    private val onTabSettingsClick: (selectedTabs: Set<TabSessionState>, dismiss: () -> Unit) -> Unit,
    val onHistoryClick: () -> Unit,
    val onShareAllTabsClick: () -> Unit,
    private val onDeleteAllTabsClick: (private: Boolean) -> Unit,
    val onAccountSettingsClick: () -> Unit,

    private val onTabClick: (tab: TabSessionState, currentMode: InfernoTabsTrayMode, enableSelect: (Set<TabSessionState>) -> Unit, dismiss: () -> Unit) -> Unit,
    private val onTabClose: (tab: TabSessionState, dismiss: () -> Unit, setSelectedTabTrayTab: (InfernoTabsTraySelectedTab) -> Unit) -> Unit,
    val onTabMediaClick: (tab: TabSessionState) -> Unit,
    val onTabMove: (String, String?, Boolean) -> Unit,
    private val onTabLongClick: (tab: TabSessionState, currentMode: InfernoTabsTrayMode, private: Boolean, enableSelect: (Set<TabSessionState>) -> Unit) -> Unit,

    private val onSyncedTabClick: (tab: Tab, dismiss: () -> Unit) -> Unit,
    val onSyncedTabClose: (deviceId: String, tab: Tab) -> Unit,

    val onClosedTabClick: (tab: TabState, currentMode: InfernoTabsTrayMode, enableSelect: (Set<TabState>) -> Unit, dismiss: () -> Unit) -> Unit,
    val onClosedTabClose: (tab: TabState) -> Unit,
    private val onClosedTabLongClick: (tab: TabState, mode: InfernoTabsTrayMode, enableSelectClosed: (Set<TabState>) -> Unit) -> Unit,
    private val onDeleteSelectedCloseTabsClick: (mode: InfernoTabsTrayMode, dismiss: () -> Unit) -> Unit,
) : LifecycleAwareFeature {
    private var scope: CoroutineScope? = null

    var visible by run {
        val state = mutableStateOf(initiallyVisible)
        object : MutableState<Boolean> by state {
            override var value: Boolean
                get() = state.value
                set(value) {
                    if (value) {
                        onRequestScreenshot.invoke()
                    }
                    state.value = value
                }
        }
    }
        private set

    var mode by mutableStateOf(initialMode)
        private set

    var selectedTabId by mutableStateOf<String?>(null)
        private set

    var normalTabs by mutableStateOf<List<TabSessionState>>(emptyList())
        private set
    var privateTabs by mutableStateOf<List<TabSessionState>>(emptyList())
        private set
    var recentlyClosedTabs by mutableStateOf<List<TabState>>(emptyList())
        private set

    var tabDisplayType by mutableStateOf(initialTabDisplayType)
        private set
    var selectedTrayTab by mutableStateOf(initiallySelectedTab)
        private set

    fun setTabTrayStyle(type: InfernoSettings.TabTrayStyle) {
        tabDisplayType = type
    }

    fun setSelectedTabsTrayTab(newTab: InfernoTabsTraySelectedTab) {
        selectedTrayTab = newTab
    }

    fun setTabsTrayMode(newMode: InfernoTabsTrayMode) {
        mode = newMode
    }

    fun show(selectedTab: InfernoTabsTraySelectedTab? = null) {
        selectedTab?.let { selectedTrayTab = it }
        visible = true
    }

    fun show(private: Boolean) {
        selectedTrayTab = when (private) {
            true -> InfernoTabsTraySelectedTab.PrivateTabs
            false -> InfernoTabsTraySelectedTab.NormalTabs
        }
        visible = true
    }

    fun dismiss() {
        visible = false
    }

    fun onBookmarkSelectedTabsClick() {
        onBookmarkSelectedTabsClick.invoke(mode.selectedTabs) { mode = InfernoTabsTrayMode.Normal }
    }

    fun onDeleteSelectedTabsClick() {
        onDeleteSelectedTabsClick.invoke(mode.selectedTabs) { mode = InfernoTabsTrayMode.Normal }
    }

    fun onForceSelectedTabsAsInactiveClick() {
        onForceSelectedTabsAsInactiveClick.invoke(mode.selectedTabs) {
            mode = InfernoTabsTrayMode.Normal
        }
    }

    fun onTabSettingsClick() {
        onTabSettingsClick.invoke(mode.selectedTabs) { mode = InfernoTabsTrayMode.Normal }
    }

    fun onDeleteAllTabsClick() {
        when (selectedTrayTab) {
            InfernoTabsTraySelectedTab.NormalTabs -> onDeleteAllTabsClick.invoke(false)
            InfernoTabsTraySelectedTab.PrivateTabs -> onDeleteAllTabsClick.invoke(true)
            else -> {}
        }
    }

    fun onTabClick(clickedTab: TabSessionState) {
        onTabClick(
            clickedTab,
            mode,
            { mode = InfernoTabsTrayMode.Select(it) },
            { mode = InfernoTabsTrayMode.Normal },
        )
    }

    fun onTabClose(closedTab: TabSessionState) {
        onTabClose.invoke(
            closedTab,
            { mode = InfernoTabsTrayMode.Normal },
            { selectedTrayTab = it },
        )
    }

    fun onTabLongClick(tab: TabSessionState) {
        val private = tab.content.private
        onTabLongClick.invoke(tab, mode, private) {
            mode = InfernoTabsTrayMode.Select(it)
        }
    }

    fun onSyncedTabClick(tab: Tab) {
        onSyncedTabClick.invoke(tab) {
            mode = InfernoTabsTrayMode.Normal
        }
    }

    fun onClosedTabClick(tab: TabState) {
        onClosedTabClick.invoke(
            tab,
            mode,
            { mode = InfernoTabsTrayMode.SelectClosed(it) },
            { mode = InfernoTabsTrayMode.Normal },
        )
    }

    fun onClosedTabLongClick(tab: TabState) {
        onClosedTabLongClick.invoke(tab, mode) {
            mode = InfernoTabsTrayMode.SelectClosed(it)
        }
    }

    fun onDeleteSelectedCloseTabsClick() {
        onDeleteSelectedCloseTabsClick.invoke(mode) {
            mode = InfernoTabsTrayMode.Normal
        }
    }

    override fun start() {
        scope = store.flowScoped { flow ->
            flow.map { it }.collect {
                selectedTabId = it.selectedTabId
                normalTabs = it.normalTabs
                privateTabs = it.privateTabs
                recentlyClosedTabs = it.closedTabs
                // update selected tab tray tab based on if normal or private
                it.selectedTab?.content?.private?.let { private ->
                    selectedTrayTab = when (private) {
                        true -> InfernoTabsTraySelectedTab.PrivateTabs
                        false -> InfernoTabsTraySelectedTab.NormalTabs
                    }
                }
            }
        }
    }

    override fun stop() {
        scope?.cancel()
    }
}