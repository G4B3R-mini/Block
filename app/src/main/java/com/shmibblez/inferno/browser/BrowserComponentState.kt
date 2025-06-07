package com.shmibblez.inferno.browser

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import com.shmibblez.inferno.components.Components
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.lastOpenedNormalTab
import com.shmibblez.inferno.ext.newTab
import com.shmibblez.inferno.tabs.tabstray.InfernoTabsTraySelectedTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.selector.privateTabs
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.CustomTabSessionState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.recover.TabState
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature

enum class BrowserComponentMode {
    TOOLBAR, TOOLBAR_SEARCH, TOOLBAR_EXTERNAL, FIND_IN_PAGE, READER_VIEW, FULLSCREEN,
}

@Composable
fun rememberBrowserComponentState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    lifecycleOwner: LifecycleOwner? = null,
    components: Components = LocalContext.current.components,
    store: BrowserStore = LocalContext.current.components.core.store,
    tabsUseCases: TabsUseCases = LocalContext.current.components.useCases.tabsUseCases,
): MutableState<BrowserComponentState> {

    val state = remember {
        mutableStateOf(
            BrowserComponentState(
                coroutineScope = coroutineScope,
                lifecycleOwner = lifecycleOwner,
                components = components,
                store = store,
                tabsUseCases = tabsUseCases,
            )
        )
    }

    DisposableEffect(null) {
        state.value.start()
        onDispose { state.value.stop() }
    }

    return state
}

// 3 secs max
private const val INIT_JOB_MILLIS = 3000L

class BrowserComponentState(
    val coroutineScope: CoroutineScope,
    val lifecycleOwner: LifecycleOwner? = null,
    val components: Components,
    val store: BrowserStore,
    val tabsUseCases: TabsUseCases,
) : LifecycleAwareFeature {

    private var browserStateObserver: CoroutineScope? = null

    private var initialized = false
    private var preinitialized = false

    // todo: test, before run also make changes to tab bar (tab layout)
    private var awaitingNewTab = false

    // browser display mode, also sets bottomBarHeightDp
    var browserMode by mutableStateOf(BrowserComponentMode.TOOLBAR)
        private set
    private val isExternal
        get() = currentCustomTab != null

    var tabList: List<TabSessionState> by mutableStateOf(emptyList())
        private set
    var normalTabs: List<TabSessionState> by mutableStateOf(emptyList())
        private set
    var privateTabs: List<TabSessionState> by mutableStateOf(emptyList())
        private set
    var customTabs: List<CustomTabSessionState> by mutableStateOf(emptyList())
        private set
    var closedTabs: List<TabState> by mutableStateOf(emptyList())
        private set
    var currentTab: TabSessionState? by mutableStateOf(null)
        private set
    var currentCustomTab: CustomTabSessionState? by mutableStateOf(null)
        private set
    val customTabSessionId: String?
        get() = currentCustomTab?.id
    var isPendingTab by mutableStateOf(true)
        private set
    var isPrivateSession: Boolean by mutableStateOf(false)
        private set
    var searchEngine: SearchEngine? by mutableStateOf(null)
        private set
    var pageType: BrowserComponentPageType by mutableStateOf(BrowserComponentPageType.ENGINE)
        private set
    var selectedTabsTrayTab: InfernoTabsTraySelectedTab by mutableStateOf(InfernoTabsTraySelectedTab.NormalTabs)

    // helper for compose migration, might be a lil sloppy
    var onActivityResultHandler: ((OnActivityResultModel) -> Boolean)? = null
        private set
    val setOnActivityResultHandler: ((OnActivityResultModel) -> Boolean) -> Unit =
        { f -> onActivityResultHandler = f }

    override fun start() {
        browserStateObserver = store.flowScoped(lifecycleOwner) { flow ->
            flow.map { it }.collect {
                currentTab = it.selectedTab
                currentCustomTab = it.customTabs.firstOrNull()
                isPendingTab = currentTab == null && currentCustomTab == null

                Log.d(
                    "BrowserComponentState",
                    "browserStateObserver, isPendingTab: $isPendingTab, currentTab id: ${currentTab?.id}, customTabId: ${currentCustomTab?.id}"
                )

                // select external tab if not selected
                if (isExternal && it.selectedTabId != currentCustomTab?.id) {
                    currentCustomTab?.id?.let {tabId -> components.useCases.tabsUseCases.selectTab(tabId) }
                }

                // update browser mode based on if external
                if (isExternal && browserMode != BrowserComponentMode.TOOLBAR_EXTERNAL) {
                    browserMode = BrowserComponentMode.TOOLBAR_EXTERNAL
                } else if (!isExternal && browserMode == BrowserComponentMode.TOOLBAR_EXTERNAL) {
                    browserMode = BrowserComponentMode.TOOLBAR
                }

                if (!initialized && !preinitialized) {
                    // if first run, preinit and delay for a bit
                    preinitialized = true
                    delay(INIT_JOB_MILLIS)
                    initialized = true
                    return@collect
                } else if (!initialized && currentTab != null) {
                    // if not init, delaying, and current tab isn't null, warm up complete, set
                    // init to true and continue
                    initialized = true
                } else if (!initialized) {
                    // if not init and delaying, return
                    return@collect
                } else {
                    // if init complete continue
                }

//                Log.d("BrowserWrapperFrag", "content update")
                // if no tab selected, false
                isPrivateSession = currentTab?.content?.private ?: false
//                val mode = BrowsingMode.fromBoolean(isPrivateSession)
//                (context.getActivity()!! as HomeActivity).browsingModeManager.mode = mode
//                context.components.appStore.dispatch(AppAction.ModeChange(mode))
                selectedTabsTrayTab = when (isPrivateSession) {
                    true -> InfernoTabsTraySelectedTab.PrivateTabs
                    false -> InfernoTabsTraySelectedTab.NormalTabs
                }
                tabList = when (isPrivateSession) {
                    true -> it.privateTabs
                    false -> it.normalTabs
                }
                normalTabs = it.normalTabs
                privateTabs = it.privateTabs
                customTabs = it.customTabs
                closedTabs = it.closedTabs
                // if no tab selected, select one
                if (isPendingTab) {
                    if (customTabSessionId != null) {
                        // if custom tab select
                        tabsUseCases.selectTab(customTabSessionId!!)
                    } else if (tabList.isNotEmpty()) {
                        // if tabs exist select
                        val lastNormalTabId = store.state.lastOpenedNormalTab?.id
                        if (tabList.any { tab -> tab.id == lastNormalTabId }) {
                            tabsUseCases.selectTab(
                                lastNormalTabId!!
                            )
                        } else {
                            tabsUseCases.selectTab(tabList.last().id)
                        }
                    } else if (!awaitingNewTab) {
                        // if no tabs available add new tab
                        components.newTab(private = false)
                        awaitingNewTab = true
                    }
                } else {
                    awaitingNewTab = false
                }

                Log.d("BrowserComponentState", "browserStateObserver, selectedTab id: ${it.selectedTabId}")

                searchEngine = it.search.selectedOrDefaultSearchEngine!!
                pageType = resolvePageType(currentTab)
            }
        }
    }

    override fun stop() {
        browserStateObserver?.cancel()
    }

    fun toggleDesktopMode() {
        when (currentTab?.content?.desktopMode) {
            false -> components.useCases.sessionUseCases.requestDesktopSite(true)
            true -> components.useCases.sessionUseCases.requestDesktopSite(false)
            null -> {} // no-op
        }
    }

    /**
     * external helpers
     */
    fun migrateExternalToNormal() {
        currentCustomTab?.id?.let {
            components.useCases.customTabsUseCases.migrate(it, select = true)
            browserMode = BrowserComponentMode.TOOLBAR
        }
    }

    /**
     * browser mode
     */

    fun setBrowserModeToolbar() {
        browserMode = when (isExternal) {
            true -> BrowserComponentMode.TOOLBAR_EXTERNAL
            false -> BrowserComponentMode.TOOLBAR
        }
    }

    fun setBrowserModeSearch() {
        browserMode = BrowserComponentMode.TOOLBAR_SEARCH
    }

    fun setBrowserModeReaderView() {
        browserMode = BrowserComponentMode.READER_VIEW
    }

    fun setBrowserModeFindInPage() {
        browserMode = BrowserComponentMode.FIND_IN_PAGE
    }

    fun setBrowserModeFullscreen() {
        browserMode = BrowserComponentMode.FULLSCREEN
    }

    /**
     * page type
     */
    private fun resolvePageType(tabSessionState: TabSessionState?): BrowserComponentPageType {
        val url = tabSessionState?.content?.url
        return if (tabSessionState?.engineState?.crashed == true) BrowserComponentPageType.CRASH
        else if (url == "inferno:home" || url == "about:blank") // TODO: create const class and set base to inferno:home
            BrowserComponentPageType.HOME
        else if (url == "inferno:privatebrowsing" || url == "about:privatebrowsing")  // TODO: add to const class and set base to inferno:private
            BrowserComponentPageType.HOME_PRIVATE
        else BrowserComponentPageType.ENGINE

        // TODO: if home, show home page and load engineView in compose tree as hidden,
        //  if page then show engineView
    }

}