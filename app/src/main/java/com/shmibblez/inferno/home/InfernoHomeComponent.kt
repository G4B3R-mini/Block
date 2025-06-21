package com.shmibblez.inferno.home

import android.annotation.SuppressLint
import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.browser.getActivity
import com.shmibblez.inferno.components.TabCollectionStorage
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.home.controllers.InfernoBookmarksController
import com.shmibblez.inferno.home.controllers.InfernoPrivateBrowsingController
import com.shmibblez.inferno.home.controllers.InfernoRecentSyncedTabController
import com.shmibblez.inferno.home.controllers.InfernoRecentTabController
import com.shmibblez.inferno.home.controllers.InfernoRecentVisitsController
import com.shmibblez.inferno.home.controllers.InfernoSearchSelectorController
import com.shmibblez.inferno.home.controllers.InfernoSessionControlController
import com.shmibblez.inferno.home.controllers.InfernoToolbarController
import com.shmibblez.inferno.home.sessioncontrol.SessionControlInteractor
import com.shmibblez.inferno.home.ui.Homepage
import com.shmibblez.inferno.messaging.DefaultMessageController
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.feature.tab.collections.TabCollection


////object BrowserComponentConstants {
//// Used to set homeViewModel.sessionToDelete when all tabs of a browsing mode are closed
//const val ALL_NORMAL_TABS = "all_normal"
//const val ALL_PRIVATE_TABS = "all_private"
//
//// Navigation arguments passed to HomeFragment
//const val FOCUS_ON_ADDRESS_BAR = "focusOnAddressBar"
//private const val SCROLL_TO_COLLECTION = "scrollToCollection"
//
//// Delay for scrolling to the collection header
//private const val ANIM_SCROLL_DELAY = 100L
//
//// Sponsored top sites titles and search engine names used for filtering
//const val AMAZON_SPONSORED_TITLE = "Amazon"
//const val AMAZON_SEARCH_ENGINE_NAME = "Amazon.com"
//const val EBAY_SPONSORED_TITLE = "eBay"
//
//// Elevation for undo toasts
//internal const val TOAST_ELEVATION = 80f
//}

// todo: implement layout, look at binding / layout [fragment_home.xml]

/**
 * Home page in new tabs, replaces [HomeFragment]
 */
@Composable
fun InfernoHomeComponent(
    state: InfernoHomeComponentState,
    modifier: Modifier = Modifier,
) {
    val settings by LocalContext.current.infernoSettingsDataStore.data.collectAsState(
        initial = InfernoSettings.getDefaultInstance(),
    )
    val context = LocalContext.current
    val components = context.components
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleScope = lifecycleOwner.lifecycleScope

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

    DisposableEffect(null) {
        state.isVisible = true
        onDispose { state.isVisible = false }
    }

    LaunchedEffect(
        settings.shouldShowTopSites,
        settings.shouldShowRecentTabs,
        settings.shouldShowBookmarks,
        settings.shouldShowHistory,
    ) {
        state.updateSettings(
            shouldShowTopSites = settings.shouldShowTopSites,
            shouldShowRecentTabs = settings.shouldShowRecentTabs,
            shouldShowBookmarks = settings.shouldShowBookmarks,
            shouldShowHistory = settings.shouldShowHistory,
        )
    }

    val collectionStorageObserver = object : TabCollectionStorage.Observer {
        @SuppressLint("NotifyDataSetChanged")
        override fun onCollectionRenamed(tabCollection: TabCollection, title: String) {
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
    // todo: in some controllers a new tab is created, so far:
    //  - InfernoToolbarController (defaultSearch creates new tab)
    //  in these cases delete home tab and then proceed with action
    //  disc: there may be other controllers that do this, if using anything other than
    //  loadUrl or selectTab use cases, check to make sure not opening new tab, if so close first
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
            loadUrlUseCase = components.useCases.sessionUseCases.loadUrl,
            topSitesUseCases = components.useCases.topSitesUseCase,
            appStore = components.appStore,
            viewLifecycleScope = lifecycleOwner.lifecycleScope,
            registerCollectionStorageObserver = {
                components.core.tabCollectionStorage.register(
                    collectionStorageObserver, lifecycleOwner, true
                )
            },
            removeCollectionWithUndo = { tabCollection ->
//                val snackbarMessage = context.getString(R.string.snackbar_collection_deleted)

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
            showUndoSnackbarForTopSite = { // topSite ->
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
            showTabTray = state.onShowTabsTray,
            onNavToHistory = state.onNavToHistory,
            onNavToBookmarks = state.onNavToBookmarks,
            onNavToHomeSettings = state.onNavToHomeSettings,
        ),
        recentTabController = InfernoRecentTabController(
            selectTabUseCase = components.useCases.tabsUseCases.selectTab,
            appStore = components.appStore,
            onShowTabsTray = state.onShowTabsTray,
        ),
        recentSyncedTabController = InfernoRecentSyncedTabController(
            appStore = components.appStore,
            browserStore = components.core.store,
            loadUrlUseCase = components.useCases.sessionUseCases.loadUrl,
            selectTabUseCase = components.useCases.tabsUseCases.selectTab,
            onShowTabsTray = state.onShowTabsTray,
        ),
        bookmarksController = InfernoBookmarksController(
            appStore = components.appStore,
            browserStore = components.core.store,
            loadUrlUseCase = components.useCases.sessionUseCases.loadUrl,
            selectTabUseCase = components.useCases.tabsUseCases.selectTab,
            onNavToBookmarks = state.onNavToBookmarks,
        ),
        recentVisitsController = InfernoRecentVisitsController(
            appStore = components.appStore,
            storage = components.core.historyStorage,
            scope = lifecycleOwner.lifecycleScope,
            store = components.core.store,
            selectTabUseCase = components.useCases.tabsUseCases.selectTab,
            loadUrlUseCase = components.useCases.sessionUseCases.loadUrl,
            onNavToHistory = state.onNavToHistory,
        ),
//            pocketStoriesController = DefaultPocketStoriesController(
//                homeActivity = context.getActivity()!! as HomeActivity,
//                appStore = components.appStore,
//                settings = components.settings,
//            ),
        privateBrowsingController = InfernoPrivateBrowsingController(
            activity = context.getActivity()!! as HomeActivity,
            appStore = components.appStore,
            loadUrlUseCase = components.useCases.sessionUseCases.loadUrl,
        ),
        searchSelectorController = InfernoSearchSelectorController(
            onNavToSearchSettings = state.onNavToSearchSettings,
        ),
        toolbarController = InfernoToolbarController(
            store = components.core.store,
            defaultSearchUseCase = components.useCases.searchUseCases.defaultSearch,
        ),
    )

    Homepage(
        state = state,
        modifier = modifier.focusable(),
//        state = HomepageState.build(
//            appState = state.appState,
////            settings = settings,
//            isPrivate = state.isPrivate,
//        ),
        interactor = sessionControlInteractor,
        onTopSitesItemBound = { },
    )
}

///**
// * Returns a [TopSitesConfig] which specifies how many top sites to display and whether or
// * not frequently visited sites should be displayed.
// */
//@VisibleForTesting
//internal fun getTopSitesConfig(context: Context): TopSitesConfig {
//    val settings = context.settings()
//    return TopSitesConfig(
//        totalSites = settings.topSitesMaxLimit,
//        frecencyConfig = TopSitesFrecencyConfig(
//            FrecencyThresholdOption.SKIP_ONE_TIME_PAGES,
//        ) { !Uri.parse(it.url).containsQueryParameters(settings.frecencyFilterQuery) },
//        providerConfig = TopSitesProviderConfig(
//            showProviderTopSites = false, // settings.showContileFeature, (show sponsored top sites, NO)
//            maxThreshold = TOP_SITES_PROVIDER_MAX_THRESHOLD,
//            providerFilter = { topSite ->
//                when (context.components.core.store.state.search.selectedOrDefaultSearchEngine?.name) {
//                    HomeFragment.AMAZON_SEARCH_ENGINE_NAME -> topSite.title != HomeFragment.AMAZON_SPONSORED_TITLE
//                    HomeFragment.EBAY_SPONSORED_TITLE -> topSite.title != HomeFragment.EBAY_SPONSORED_TITLE
//                    else -> true
//                }
//            },
//        ),
//    )
//}