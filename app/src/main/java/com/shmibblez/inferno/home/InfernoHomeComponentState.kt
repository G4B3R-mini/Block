package com.shmibblez.inferno.home

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.components.Components
import com.shmibblez.inferno.components.appstate.AppState
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.containsQueryParameters
import com.shmibblez.inferno.ext.shouldShowRecentSyncedTabs
import com.shmibblez.inferno.ext.shouldShowRecentTabs
import com.shmibblez.inferno.home.bookmarks.BookmarksFeature
import com.shmibblez.inferno.home.recentsyncedtabs.RecentSyncedTabFeature
import com.shmibblez.inferno.home.recenttabs.RecentTabsListFeature
import com.shmibblez.inferno.home.recentvisits.RecentVisitsFeature
import com.shmibblez.inferno.home.topsites.DefaultTopSitesView
import com.shmibblez.inferno.tabs.tabstray.InfernoTabsTraySelectedTab
import com.shmibblez.inferno.utils.Settings.Companion.TOP_SITES_MAX_COUNT
import com.shmibblez.inferno.utils.Settings.Companion.TOP_SITES_PROVIDER_MAX_THRESHOLD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.storage.FrecencyThresholdOption
import mozilla.components.feature.top.sites.TopSitesConfig
import mozilla.components.feature.top.sites.TopSitesFeature
import mozilla.components.feature.top.sites.TopSitesFrecencyConfig
import mozilla.components.feature.top.sites.TopSitesProviderConfig
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature

// todo: settings is a hot mess
@Composable
fun rememberInfernoHomeComponentState(
    initialAppState: AppState = LocalContext.current.components.appStore.state,
    initialShouldShowTopSites: Boolean = false,
    initialShouldShowRecentTabs: Boolean = false,
    initialShouldShowBookmarks: Boolean = false,
    initialShouldShowHistory: Boolean = false,
    context: Context = LocalContext.current,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    components: Components = LocalContext.current.components,
    store: BrowserStore = LocalContext.current.components.core.store,
    appStore: AppStore = LocalContext.current.components.appStore,
    onShowTabsTray: (InfernoTabsTraySelectedTab?) -> Unit,
    onNavToHistory: () -> Unit,
    onNavToBookmarks: () -> Unit,
    onNavToSearchSettings: () -> Unit,
    onNavToHomeSettings: () -> Unit,
): MutableState<InfernoHomeComponentState> {
    val state = remember {
        mutableStateOf(
            InfernoHomeComponentState(
                initialAppState = initialAppState,
                initialShouldShowTopSites = initialShouldShowTopSites,
                initialShouldShowRecentTabs = initialShouldShowRecentTabs,
                initialShouldShowBookmarks = initialShouldShowBookmarks,
                initialShouldShowHistory = initialShouldShowHistory,
                context = context,
                lifecycleOwner = lifecycleOwner,
                components = components,
                store = store,
                appStore = appStore,
                onShowTabsTray = onShowTabsTray,
                onNavToHistory = onNavToHistory,
                onNavToBookmarks = onNavToBookmarks,
                onNavToSearchSettings = onNavToSearchSettings,
                onNavToHomeSettings = onNavToHomeSettings,
            )
        )
    }

    DisposableEffect(null) {
        state.value.start()
        onDispose { state.value.stop() }
    }

    return state
}

// todo: this is a hot mess
class InfernoHomeComponentState(
    initialAppState: AppState,
    initialShouldShowTopSites: Boolean,
    initialShouldShowRecentTabs: Boolean,
    initialShouldShowBookmarks: Boolean,
    initialShouldShowHistory: Boolean,
    val context: Context,
    val lifecycleOwner: LifecycleOwner,
    val components: Components,
    val store: BrowserStore,
    val appStore: AppStore,
    val onShowTabsTray: (InfernoTabsTraySelectedTab?) -> Unit,
    val onNavToHistory: () -> Unit,
    val onNavToBookmarks: () -> Unit,
    val onNavToSearchSettings: () -> Unit,
    val onNavToHomeSettings: () -> Unit,
) : LifecycleAwareFeature {

    // throttles updates, when invisible does not update state vars
    var isVisible by mutableStateOf(false)

    private var storeScope: CoroutineScope? = null
    private var appStateScope: CoroutineScope? = null

    var isPrivate by mutableStateOf(false)

    var appState by mutableStateOf(initialAppState)

    // features
    private var topSitesFeature by mutableStateOf<TopSitesFeature?>(null)
    private var recentTabsListFeature by mutableStateOf<RecentTabsListFeature?>(null)
    private var recentSyncedTabFeature by mutableStateOf<RecentSyncedTabFeature?>(null)
    private var bookmarksFeature by mutableStateOf<BookmarksFeature?>(null)
    private var historyMetadataFeature by mutableStateOf<RecentVisitsFeature?>(null)

    private var shouldShowTopSites by mutableStateOf(initialShouldShowTopSites)
    private var shouldShowRecentTabs by mutableStateOf(initialShouldShowRecentTabs)
    private var shouldShowBookmarks by mutableStateOf(initialShouldShowBookmarks)
    private var shouldShowHistory by mutableStateOf(initialShouldShowHistory)

    var topSites by mutableStateOf(appState.topSites)
        private set
    var showTopSites by mutableStateOf(shouldShowTopSites && topSites.isNotEmpty())
        private set
    var recentTabs by mutableStateOf(appState.recentTabs)
        private set
    var showRecentTabs by mutableStateOf(appState.shouldShowRecentTabs(shouldShowRecentTabs))
        private set
    var bookmarks by mutableStateOf( appState.bookmarks)
        private set
    var showBookmarks by mutableStateOf( shouldShowBookmarks && bookmarks.isNotEmpty())
        private set
    var showRecentSyncedTab by mutableStateOf( appState.shouldShowRecentSyncedTabs())
        private set
    var recentlyVisited by mutableStateOf(appState.recentHistory)
        private set
    var showRecentlyVisited by mutableStateOf(shouldShowHistory && recentlyVisited.isNotEmpty())
        private set

    private fun refreshAppStateVars() {
        if (!isVisible) return
        Log.d("HomeState", "refreshAppStateVars called")
        topSites = appState.topSites
        showTopSites = shouldShowTopSites && topSites.isNotEmpty()
        recentTabs = appState.recentTabs
        showRecentTabs = appState.shouldShowRecentTabs(shouldShowRecentTabs)
        bookmarks = appState.bookmarks
        showBookmarks = shouldShowBookmarks && bookmarks.isNotEmpty()
        showRecentSyncedTab = appState.shouldShowRecentSyncedTabs()
        recentlyVisited = appState.recentHistory
        showRecentlyVisited = shouldShowHistory && recentlyVisited.isNotEmpty()
    }

    fun updateSettings(
        shouldShowTopSites: Boolean,
        shouldShowRecentTabs: Boolean,
        shouldShowBookmarks: Boolean,
        shouldShowHistory: Boolean,
    ) {
        if (!isVisible) return
        Log.d("HomeState", "updateSettings called")
        this.shouldShowTopSites = shouldShowTopSites
        this.shouldShowRecentTabs = shouldShowRecentTabs
        this.shouldShowBookmarks = shouldShowBookmarks
        this.shouldShowHistory = shouldShowHistory

        if (shouldShowTopSites) {
            topSitesFeature = TopSitesFeature(
                view = DefaultTopSitesView(
                    appStore = appStore,
//                    settings = components.settings,
                ),
                storage = components.core.topSitesStorage,
                config = { getTopSitesConfig(store, TOP_SITES_MAX_COUNT) },
            ).apply { this.start() }
        }

        if (shouldShowRecentTabs) {
            recentTabsListFeature = RecentTabsListFeature(
                browserStore = store,
                appStore = appStore,
            ).apply { this.start() }

            recentSyncedTabFeature = RecentSyncedTabFeature(
                context = context,
                appStore = appStore,
                syncStore = components.backgroundServices.syncStore,
                storage = components.backgroundServices.syncedTabsStorage,
                accountManager = components.backgroundServices.accountManager,
                historyStorage = components.core.historyStorage,
                coroutineScope = lifecycleOwner.lifecycleScope,
            ).apply { this.start() }
        }

        if (shouldShowBookmarks) {
            bookmarksFeature = BookmarksFeature(
                appStore = appStore,
                bookmarksUseCase = run {
                    context.components.useCases.bookmarksUseCases
                },
                scope = lifecycleOwner.lifecycleScope,
            ).apply { this.start() }
        }

        if (shouldShowHistory) {
            historyMetadataFeature = RecentVisitsFeature(
                appStore = appStore,
                historyMetadataStorage = components.core.historyStorage,
                historyHighlightsStorage = components.core.lazyHistoryStorage,
                scope = lifecycleOwner.lifecycleScope,
            ).apply { this.start() }
        }

        refreshAppStateVars()
    }

    override fun start() {
        Log.d("HomeState", "start called")

        storeScope = store.flowScoped { flow ->
            flow.map { it.selectedTab?.content?.private }.collect { private ->
                isPrivate = private ?: false
            }
        }
        appStateScope = appStore.flowScoped { flow ->
            flow.map { it }.collect { state ->
                appState = state
                refreshAppStateVars()
            }
        }
    }

    override fun stop() {
        Log.d("HomeState", "stop called")

        storeScope?.cancel()
        appStateScope?.cancel()
    }
}

/**
 * Returns a [TopSitesConfig] which specifies how many top sites to display and whether or
 * not frequently visited sites should be displayed.
 */
private fun getTopSitesConfig(
    store: BrowserStore,
    topSitesMaxCount: Int = TOP_SITES_MAX_COUNT,
    frecencyFilterQuery: String = "mfadid=adm",
): TopSitesConfig {
    return TopSitesConfig(
        totalSites = topSitesMaxCount,
        frecencyConfig = TopSitesFrecencyConfig(
            FrecencyThresholdOption.SKIP_ONE_TIME_PAGES,
        ) { !Uri.parse(it.url).containsQueryParameters(frecencyFilterQuery) },
        providerConfig = TopSitesProviderConfig(
            showProviderTopSites = false, // settings.showContileFeature, (show sponsored top sites, NO)
            maxThreshold = TOP_SITES_PROVIDER_MAX_THRESHOLD,
            providerFilter = { topSite ->
                when (store.state.search.selectedOrDefaultSearchEngine?.name) {
                    HomeFragment.AMAZON_SEARCH_ENGINE_NAME -> topSite.title != HomeFragment.AMAZON_SPONSORED_TITLE
                    HomeFragment.EBAY_SPONSORED_TITLE -> topSite.title != HomeFragment.EBAY_SPONSORED_TITLE
                    else -> true
                }
            },
        ),
    )
}