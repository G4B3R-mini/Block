package com.shmibblez.inferno.home

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.getActivity
import com.shmibblez.inferno.components.TabCollectionStorage
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.containsQueryParameters
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.home.bookmarks.BookmarksFeature
import com.shmibblez.inferno.home.controllers.InfernoBookmarksController
import com.shmibblez.inferno.home.controllers.InfernoPrivateBrowsingController
import com.shmibblez.inferno.home.controllers.InfernoRecentSyncedTabController
import com.shmibblez.inferno.home.controllers.InfernoRecentTabController
import com.shmibblez.inferno.home.controllers.InfernoRecentVisitsController
import com.shmibblez.inferno.home.controllers.InfernoSearchSelectorController
import com.shmibblez.inferno.home.controllers.InfernoSessionControlController
import com.shmibblez.inferno.home.controllers.InfernoToolbarController
import com.shmibblez.inferno.home.recentsyncedtabs.RecentSyncedTabFeature
import com.shmibblez.inferno.home.recenttabs.RecentTabsListFeature
import com.shmibblez.inferno.home.recentvisits.RecentVisitsFeature
import com.shmibblez.inferno.home.sessioncontrol.SessionControlInteractor
import com.shmibblez.inferno.home.store.HomepageState
import com.shmibblez.inferno.home.topsites.DefaultTopSitesView
import com.shmibblez.inferno.home.ui.Homepage
import com.shmibblez.inferno.messaging.DefaultMessageController
import com.shmibblez.inferno.tabs.tabstray.InfernoTabsTraySelectedTab
import com.shmibblez.inferno.utils.Settings.Companion.TOP_SITES_PROVIDER_MAX_THRESHOLD
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.concept.storage.FrecencyThresholdOption
import mozilla.components.feature.tab.collections.TabCollection
import mozilla.components.feature.top.sites.TopSitesConfig
import mozilla.components.feature.top.sites.TopSitesFeature
import mozilla.components.feature.top.sites.TopSitesFrecencyConfig
import mozilla.components.feature.top.sites.TopSitesProviderConfig
import mozilla.components.lib.state.ext.observeAsState
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper


//object BrowserComponentConstants {
// Used to set homeViewModel.sessionToDelete when all tabs of a browsing mode are closed
const val ALL_NORMAL_TABS = "all_normal"
const val ALL_PRIVATE_TABS = "all_private"

// Navigation arguments passed to HomeFragment
const val FOCUS_ON_ADDRESS_BAR = "focusOnAddressBar"
private const val SCROLL_TO_COLLECTION = "scrollToCollection"

// Delay for scrolling to the collection header
private const val ANIM_SCROLL_DELAY = 100L

// Sponsored top sites titles and search engine names used for filtering
const val AMAZON_SPONSORED_TITLE = "Amazon"
const val AMAZON_SEARCH_ENGINE_NAME = "Amazon.com"
const val EBAY_SPONSORED_TITLE = "eBay"

// Elevation for undo toasts
internal const val TOAST_ELEVATION = 80f
//}

// todo: implement layout, look at binding / layout [fragment_home.xml]

/**
 * Home page in new tabs, replaces [HomeFragment]
 */
@Composable
fun InfernoHomeComponent(
    isPrivate: Boolean,
    modifier: Modifier = Modifier,
    onShowTabsTray: (InfernoTabsTraySelectedTab?) -> Unit,
    onNavToHistory: () -> Unit,
    onNavToBookmarks: () -> Unit,
    onNavToSearchSettings: () -> Unit,
) {
    val settings = LocalContext.current.settings()
    val context = LocalContext.current
    val components = context.components
    val appState by components.appStore.observeAsState(
        initialValue = components.appStore.state,
    ) { it }
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleScope = lifecycleOwner.lifecycleScope
    val view = LocalView.current

//    if (context.settings().isExperimentationEnabled) {
//        messagingFeatureHomescreen.set(
//            feature = MessagingFeature(
//                appStore = components.appStore,
//                surface = FenixMessageSurfaceId.HOMESCREEN,
//            ),
//            owner = lifecycleOwner,
//            view = view,
//        )
//
////        initializeMicrosurveyFeature(context.settings().microsurveyFeatureEnabled)
//    }

    val topSitesFeature = remember { ViewBoundFeatureWrapper<TopSitesFeature>() }
    val recentTabsListFeature = remember { ViewBoundFeatureWrapper<RecentTabsListFeature>() }
    val recentSyncedTabFeature = remember { ViewBoundFeatureWrapper<RecentSyncedTabFeature>() }
    val bookmarksFeature = remember { ViewBoundFeatureWrapper<BookmarksFeature>() }
    val historyMetadataFeature = remember { ViewBoundFeatureWrapper<RecentVisitsFeature>() }

    // todo: use settings flow, collect as state or rebuild based on specific settings, custom collector?
    val shouldShowTopSites = context.settings().showTopSitesFeature

    if (shouldShowTopSites) {
        topSitesFeature.set(
            feature = TopSitesFeature(
                view = DefaultTopSitesView(
                    appStore = components.appStore,
                    settings = components.settings,
                ),
                storage = components.core.topSitesStorage,
                config = { getTopSitesConfig(context) },
            ),
            owner = lifecycleOwner,
            view = view,
        )
    }

    if (context.settings().showRecentTabsFeature) {
        recentTabsListFeature.set(
            feature = RecentTabsListFeature(
                browserStore = components.core.store,
                appStore = components.appStore,
            ),
            owner = lifecycleOwner,
            view = view,
        )

        recentSyncedTabFeature.set(
            feature = RecentSyncedTabFeature(
                context = context,
                appStore = components.appStore,
                syncStore = components.backgroundServices.syncStore,
                storage = components.backgroundServices.syncedTabsStorage,
                accountManager = components.backgroundServices.accountManager,
                historyStorage = components.core.historyStorage,
                coroutineScope = lifecycleOwner.lifecycleScope,
            ),
            owner = lifecycleOwner,
            view = view,
        )
    }

    if (context.settings().showBookmarksHomeFeature) {
        bookmarksFeature.set(
            feature = BookmarksFeature(
                appStore = components.appStore,
                bookmarksUseCase = run {
                    context.components.useCases.bookmarksUseCases
                },
                scope = lifecycleOwner.lifecycleScope,
            ),
            owner = lifecycleOwner,
            view = view,
        )
    }

    if (context.settings().shouldShowHistory) {
        historyMetadataFeature.set(
            feature = RecentVisitsFeature(
                appStore = components.appStore,
                historyMetadataStorage = components.core.historyStorage,
                historyHighlightsStorage = components.core.lazyHistoryStorage,
                scope = lifecycleOwner.lifecycleScope,
            ),
            owner = lifecycleOwner,
            view = view,
        )
    }
    val collectionStorageObserver = object : TabCollectionStorage.Observer {
        @SuppressLint("NotifyDataSetChanged")
        override fun onCollectionRenamed(tabCollection: TabCollection, title: String) {
            // todo: session control
//            lifecycleScope.launch(Main) {
//                binding.sessionControlRecyclerView.adapter?.notifyDataSetChanged()
//            }
            // todo: snackbar: showRenamedSnackbar()
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onTabsAdded(tabCollection: TabCollection, sessions: List<TabSessionState>) {
            // todo: tab message
//            view?.let {
//                val message = if (sessions.size == 1) {
//                    R.string.create_collection_tab_saved
//                } else {
//                    R.string.create_collection_tabs_saved
//                }
//
//                // todo: session control
////                lifecycleScope.launch(Main) {
////                    binding.sessionControlRecyclerView.adapter?.notifyDataSetChanged()
////                }
//
//                // todo: snackbar
////                Snackbar.make(
////                    snackBarParentView = binding.dynamicSnackbarContainer,
////                    snackbarState = SnackbarState(
////                        message = it.context.getString(message),
////                        duration = SnackbarDuration.Long.toSnackbarStateDuration(),
////                    ),
////                ).show()
//            }
        }
    }
    // todo: in controllers use loadUrl instead of newTab since already in browser
    //  check if loadUrl creates new tab or loads in current
    val sessionControlInteractor = SessionControlInteractor(
        controller = InfernoSessionControlController(
            activity = context.getActivity()!! as HomeActivity,
            settings = components.settings,
            engine = components.core.engine,
            messageController = DefaultMessageController(
                appStore = components.appStore,
                messagingController = components.nimbus.messaging,
                homeActivity = context.getActivity()!! as HomeActivity,
            ),
            store = components.core.store,
            tabCollectionStorage = components.core.tabCollectionStorage,
            addTabUseCase = components.useCases.tabsUseCases.addTab,
            restoreUseCase = components.useCases.tabsUseCases.restore,
            selectTabUseCase = components.useCases.tabsUseCases.selectTab,
            reloadUrlUseCase = components.useCases.sessionUseCases.reload,
            topSitesUseCases = components.useCases.topSitesUseCase,
            appStore = components.appStore,
            viewLifecycleScope = lifecycleOwner.lifecycleScope,
            registerCollectionStorageObserver = {
                components.core.tabCollectionStorage.register(
                    collectionStorageObserver, lifecycleOwner, true
                )
            },
            removeCollectionWithUndo = { tabCollection ->
                val snackbarMessage = context.getString(R.string.snackbar_collection_deleted)

                // todo: undo snackbar
//                lifecycleScope.allowUndo(
//                    binding.dynamicSnackbarContainer,
//                    snackbarMessage,
//                    getString(R.string.snackbar_deleted_undo),
//                    {
//                        components.core.tabCollectionStorage.createCollection(tabCollection)
//                    },
//                    operation = { },
//                    elevation = HomeFragment.TOAST_ELEVATION,
//                )

                lifecycleScope.launch(IO) {
                    components.core.tabCollectionStorage.removeCollection(tabCollection)
                }
            },
            showUndoSnackbarForTopSite = { topSite ->
                // todo: undo snackbar
//                lifecycleScope.allowUndo(
//                    view = binding.dynamicSnackbarContainer,
//                    message = context.getString(R.string.snackbar_top_site_removed),
//                    undoActionTitle = context.getString(R.string.snackbar_deleted_undo),
//                    onCancel = {
//                        components.useCases.topSitesUseCase.addPinnedSites(
//                            topSite.title.toString(),
//                            topSite.url,
//                        )
//                    },
//                    operation = { },
//                    elevation = Companion.TOAST_ELEVATION,
//                )
            },
            showTabTray = onShowTabsTray,
            onNavToHistory = onNavToHistory,
            onNavToBookmarks = onNavToBookmarks,
        ),
        recentTabController = InfernoRecentTabController(
            selectTabUseCase = components.useCases.tabsUseCases.selectTab,
            appStore = components.appStore,
            onShowTabsTray = onShowTabsTray,
        ),
        recentSyncedTabController = InfernoRecentSyncedTabController(
            tabsUseCase = components.useCases.tabsUseCases,
            appStore = components.appStore,
            onShowTabsTray = onShowTabsTray,
        ),
        bookmarksController = InfernoBookmarksController(
            appStore = components.appStore,
            browserStore = components.core.store,
            selectOrAddTabUseCase = components.useCases.tabsUseCases.selectOrAddTab,
            onNavToBookmarks = onNavToBookmarks,
        ),
        recentVisitsController = InfernoRecentVisitsController(
            appStore = components.appStore,
            selectOrAddTabUseCase = components.useCases.tabsUseCases.selectOrAddTab,
            storage = components.core.historyStorage,
            scope = lifecycleOwner.lifecycleScope,
            store = components.core.store,
            onNavToHistory = onNavToHistory,
        ),
//            pocketStoriesController = DefaultPocketStoriesController(
//                homeActivity = context.getActivity()!! as HomeActivity,
//                appStore = components.appStore,
//                settings = components.settings,
//            ),
        privateBrowsingController = InfernoPrivateBrowsingController(
            activity = context.getActivity()!! as HomeActivity,
            appStore = components.appStore,
        ),
        searchSelectorController = InfernoSearchSelectorController(
            onNavToSearchSettings = onNavToSearchSettings,
        ),
        toolbarController = InfernoToolbarController(
            store = components.core.store,
            defaultSearchUseCase = components.useCases.searchUseCases.defaultSearch,
        ),
    )

    Homepage(
        modifier = modifier.focusable(),
        state = HomepageState.build(
            appState = appState,
            settings = settings,
            isPrivate = isPrivate,
        ),
        isPrivate = isPrivate,
        interactor = sessionControlInteractor,
        onTopSitesItemBound = { },
    )
}

/**
 * Returns a [TopSitesConfig] which specifies how many top sites to display and whether or
 * not frequently visited sites should be displayed.
 */
@VisibleForTesting
internal fun getTopSitesConfig(context: Context): TopSitesConfig {
    val settings = context.settings()
    return TopSitesConfig(
        totalSites = settings.topSitesMaxLimit,
        frecencyConfig = TopSitesFrecencyConfig(
            FrecencyThresholdOption.SKIP_ONE_TIME_PAGES,
        ) { !Uri.parse(it.url).containsQueryParameters(settings.frecencyFilterQuery) },
        providerConfig = TopSitesProviderConfig(
            showProviderTopSites = settings.showContileFeature,
            maxThreshold = TOP_SITES_PROVIDER_MAX_THRESHOLD,
            providerFilter = { topSite ->
                when (context.components.core.store.state.search.selectedOrDefaultSearchEngine?.name) {
                    HomeFragment.AMAZON_SEARCH_ENGINE_NAME -> topSite.title != HomeFragment.AMAZON_SPONSORED_TITLE
                    HomeFragment.EBAY_SPONSORED_TITLE -> topSite.title != HomeFragment.EBAY_SPONSORED_TITLE
                    else -> true
                }
            },
        ),
    )
}