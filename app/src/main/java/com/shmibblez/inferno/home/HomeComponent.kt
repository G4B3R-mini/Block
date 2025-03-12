package com.shmibblez.inferno.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.browsingmode.BrowsingMode
import com.shmibblez.inferno.browser.browsingmode.BrowsingModeManager
import com.shmibblez.inferno.browser.getActivity
import com.shmibblez.inferno.browser.tabstrip.isTabStripEnabled
import com.shmibblez.inferno.components.Components
import com.shmibblez.inferno.components.TabCollectionStorage
import com.shmibblez.inferno.components.appstate.AppAction
import com.shmibblez.inferno.components.appstate.AppAction.MessagingAction
import com.shmibblez.inferno.components.toolbar.navbar.shouldAddNavigationBar
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.consumeFrom
import com.shmibblez.inferno.ext.containsQueryParameters
import com.shmibblez.inferno.ext.isToolbarAtBottom
import com.shmibblez.inferno.ext.nav
import com.shmibblez.inferno.ext.openSetDefaultBrowserOption
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.ext.tabClosedUndoMessage
import com.shmibblez.inferno.home.bookmarks.BookmarksFeature
import com.shmibblez.inferno.home.bookmarks.controller.DefaultBookmarksController
import com.shmibblez.inferno.home.privatebrowsing.controller.DefaultPrivateBrowsingController
import com.shmibblez.inferno.home.recentsyncedtabs.RecentSyncedTabFeature
import com.shmibblez.inferno.home.recentsyncedtabs.controller.DefaultRecentSyncedTabController
import com.shmibblez.inferno.home.recenttabs.RecentTabsListFeature
import com.shmibblez.inferno.home.recenttabs.controller.DefaultRecentTabsController
import com.shmibblez.inferno.home.recentvisits.RecentVisitsFeature
import com.shmibblez.inferno.home.recentvisits.controller.DefaultRecentVisitsController
import com.shmibblez.inferno.home.sessioncontrol.DefaultSessionControlController
import com.shmibblez.inferno.home.sessioncontrol.SessionControlInteractor
import com.shmibblez.inferno.home.sessioncontrol.SessionControlView
import com.shmibblez.inferno.home.toolbar.DefaultToolbarController
import com.shmibblez.inferno.home.toolbar.SearchSelectorBinding
import com.shmibblez.inferno.home.toolbar.SearchSelectorMenuBinding
import com.shmibblez.inferno.home.topsites.DefaultTopSitesView
import com.shmibblez.inferno.messaging.DefaultMessageController
import com.shmibblez.inferno.messaging.FenixMessageSurfaceId
import com.shmibblez.inferno.messaging.MessagingFeature
import com.shmibblez.inferno.microsurvey.ui.ext.MicrosurveyUIData
import com.shmibblez.inferno.nimbus.FxNimbus
import com.shmibblez.inferno.onboarding.HomeScreenPopupManager
import com.shmibblez.inferno.perf.MarkersFragmentLifecycleCallbacks
import com.shmibblez.inferno.search.SearchDialogFragment
import com.shmibblez.inferno.search.toolbar.DefaultSearchSelectorController
import com.shmibblez.inferno.search.toolbar.SearchSelectorMenu
import com.shmibblez.inferno.tabstray.Page
import com.shmibblez.inferno.tabstray.TabsTrayAccessPoint
import com.shmibblez.inferno.utils.Settings.Companion.TOP_SITES_PROVIDER_MAX_THRESHOLD
import com.shmibblez.inferno.wallpapers.Wallpaper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mozilla.components.browser.menu.view.MenuButton
import mozilla.components.browser.state.selector.findTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.compose.cfr.CFRPopup
import mozilla.components.concept.storage.FrecencyThresholdOption
import mozilla.components.concept.sync.AccountObserver
import mozilla.components.concept.sync.AuthType
import mozilla.components.concept.sync.OAuthAccount
import mozilla.components.feature.tab.collections.TabCollection
import mozilla.components.feature.top.sites.TopSite
import mozilla.components.feature.top.sites.TopSitesConfig
import mozilla.components.feature.top.sites.TopSitesFeature
import mozilla.components.feature.top.sites.TopSitesFrecencyConfig
import mozilla.components.feature.top.sites.TopSitesProviderConfig
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.utils.BrowsersCache


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
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun HomeComponent(private: Boolean) {
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val store = context.components.core.store
    val view = LocalView.current
    val localConfiguration = LocalConfiguration.current
    val parentFragmentManager = context.getActivity()!!.supportFragmentManager

    val navController = rememberNavController()
//    private val args by navArgs<HomeFragmentArgs>()

    @androidx.annotation.VisibleForTesting
    // TODO: bundleArgs
//    lateinit var bundleArgs: Bundle

//    @androidx.annotation.VisibleForTesting
//    @Suppress("VariableNaming")
//    var _binding: FragmentHomeBinding? = null
//    val binding get() = _binding!!
//    val snackbarBinding = ViewBoundFeatureWrapper<SnackbarBinding>()

//    val homeViewModel: HomeScreenViewModel by activityViewModels()

//    var _bottomToolbarContainerView: BottomToolbarContainerView? = null
//    val bottomToolbarContainerView: BottomToolbarContainerView
//    get() = _bottomToolbarContainerView!!

    val collectionStorageObserver = remember {
        object : TabCollectionStorage.Observer {
            @SuppressLint("NotifyDataSetChanged")
            override fun onCollectionRenamed(tabCollection: TabCollection, title: String) {
//            lifecycleScope.launch(Main) {
                coroutineScope.launch(Main) {
                    // TODO
//                    binding.sessionControlRecyclerView.adapter?.notifyDataSetChanged()
                }
            }
            // TODO:
//            showRenamedSnackbar()
        }
    }

    val browsingModeManager = (context.getActivity() as HomeActivity).browsingModeManager

//    val topSites = (context.getActivity() as HomeActivity).topSites

//    var _sessionControlInteractor: SessionControlInteractor? = null
//    val sessionControlInteractor: SessionControlInteractor
//        get() = _sessionControlInteractor!!
    var sessionControlInteractor by remember { mutableStateOf<SessionControlInteractor?>(null) }

    val searchSelectorMenu by lazy {
        SearchSelectorMenu(
            context = context,
            interactor = sessionControlInteractor!!,
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onTabsAdded(tabCollection: TabCollection, sessions: List<TabSessionState>) {
        view?.let {
            val message = if (sessions.size == 1) {
                R.string.create_collection_tab_saved
            } else {
                R.string.create_collection_tabs_saved
            }

//                lifecycleScope.launch(Main) {
            coroutineScope.launch(Main) {
                // TODO
//                    binding.sessionControlRecyclerView.adapter?.notifyDataSetChanged()
            }

            // TODO
//                Snackbar.make(
//                    snackBarParentView = binding.dynamicSnackbarContainer,
//                    snackbarState = SnackbarState(
//                        message = it.context.getString(message),
//                        duration = SnackbarDuration.Long.toSnackbarStateDuration(),
//                    ),
//                ).show()
        }
    }


    /* new vals */

//    private var sessionControlView: SessionControlView? = null

//    @androidx.annotation.VisibleForTesting(otherwise = androidx.annotation.VisibleForTesting.PRIVATE)
//    var toolbarView: ToolbarView? = null

    val (lastAppliedWallpaperName, setLastAppliedWallpaperName) = remember {
        mutableStateOf<String>(
            Wallpaper.defaultName
        )
    }
    val (recommendPrivateBrowsingCFR, setRecommendPrivateBrowsingCFR) = remember {
        mutableStateOf<CFRPopup?>(
            null
        )
    }

    val topSitesFeature = remember { ViewBoundFeatureWrapper<TopSitesFeature>() }

    @androidx.annotation.VisibleForTesting val messagingFeatureHomescreen =
        remember { ViewBoundFeatureWrapper<MessagingFeature>() }

    @androidx.annotation.VisibleForTesting val messagingFeatureMicrosurvey =
        remember { ViewBoundFeatureWrapper<MessagingFeature>() }

    val recentTabsListFeature = remember { ViewBoundFeatureWrapper<RecentTabsListFeature>() }
    val recentSyncedTabFeature = remember { ViewBoundFeatureWrapper<RecentSyncedTabFeature>() }
    val bookmarksFeature = remember { ViewBoundFeatureWrapper<BookmarksFeature>() }
    val historyMetadataFeature = remember { ViewBoundFeatureWrapper<RecentVisitsFeature>() }
    val searchSelectorBinding = remember { ViewBoundFeatureWrapper<SearchSelectorBinding>() }
    val searchSelectorMenuBinding =
        remember { ViewBoundFeatureWrapper<SearchSelectorMenuBinding>() }
    val homeScreenPopupManager = remember { ViewBoundFeatureWrapper<HomeScreenPopupManager>() }
    /* new vals */

    LaunchedEffect(Unit) {
        /* HomeFragment onCreate */
        /* HomeFragment onCreate */

        /* onCreateView */
        // DO NOT ADD ANYTHING ABOVE THIS getProfilerTime CALL!
        var profilerStartTime = context.components.core.engine.profiler?.getProfilerTime()

//        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val activity = context.getActivity() as HomeActivity
        val components = context.components

        var currentWallpaperName = context.settings().currentWallpaperName
        applyWallpaper(
            wallpaperName = currentWallpaperName,
            orientationChange = false,
            orientation = context.resources.configuration.orientation,
            context = context,
            coroutineScope = coroutineScope,
            lastAppliedWallpaperName = lastAppliedWallpaperName,
            setLastAppliedWallpaperName = setLastAppliedWallpaperName
        )

        components.appStore.dispatch(AppAction.ModeChange(browsingModeManager.mode))

//        lifecycleScope.launch(IO) {
//            // Show Merino content recommendations.
//            val showContentRecommendations = context.settings().showContentRecommendations
//            // Show Pocket recommended stories.
//            val showPocketRecommendationsFeature =
//                context.settings().showPocketRecommendationsFeature
//            // Show sponsored stories if recommended stories are enabled.
//            val showSponsoredStories = context.settings().showPocketSponsoredStories &&
//                (showContentRecommendations || showPocketRecommendationsFeature)

//            if (showContentRecommendations) {
//                components.appStore.dispatch(
//                    ContentRecommendationsAction.ContentRecommendationsFetched(
//                        recommendations = components.core.pocketStoriesService.getContentRecommendations(),
//                    ),
//                )
//            } else if (showPocketRecommendationsFeature) {
//                val categories = components.core.pocketStoriesService.getStories()
//                    .groupBy { story -> story.category }
//                    .map { (category, stories) -> PocketRecommendedStoriesCategory(category, stories) }
//
//                components.appStore.dispatch(ContentRecommendationsAction.PocketStoriesCategoriesChange(categories))
//            } else {
//                components.appStore.dispatch(ContentRecommendationsAction.PocketStoriesClean)
//            }

//            if (showSponsoredStories) {
//                components.appStore.dispatch(
//                    ContentRecommendationsAction.PocketSponsoredStoriesChange(
//                        sponsoredStories = components.core.pocketStoriesService.getSponsoredStories(),
//                        showContentRecommendations = showContentRecommendations,
//                    ),
//                )
//            }
//        }

        if (context.settings().isExperimentationEnabled) {
            messagingFeatureHomescreen.set(
                feature = MessagingFeature(
                    appStore = context.components.appStore,
                    surface = FenixMessageSurfaceId.HOMESCREEN,
                ),
                owner = lifecycleOwner, // viewLifecycleOwner,
                view = view, // binding.root,
            )

            initializeMicrosurveyFeature(context.settings().microsurveyFeatureEnabled)
        }

        if (context.settings().showTopSitesFeature) {
            topSitesFeature.set(
                feature = TopSitesFeature(
                    view = DefaultTopSitesView(
                        appStore = components.appStore,
                        settings = components.settings,
                    ),
                    storage = components.core.topSitesStorage,
                    config = { getTopSitesConfig(context) },
                ), owner = lifecycleOwner, // viewLifecycleOwner,
                view = view // binding.root,
            )
        }

        if (context.settings().showRecentTabsFeature) {
            recentTabsListFeature.set(
                feature = RecentTabsListFeature(
                    browserStore = components.core.store,
                    appStore = components.appStore,
                ), owner = lifecycleOwner, // viewLifecycleOwner,
                view = view // binding.root,
            )

            recentSyncedTabFeature.set(
                feature = RecentSyncedTabFeature(
                    context = context,
                    appStore = context.components.appStore,
                    syncStore = context.components.backgroundServices.syncStore,
                    storage = context.components.backgroundServices.syncedTabsStorage,
                    accountManager = context.components.backgroundServices.accountManager,
                    historyStorage = context.components.core.historyStorage,
                    coroutineScope = coroutineScope, // viewLifecycleOwner.lifecycleScope,
                ), owner = lifecycleOwner, // viewLifecycleOwner,
                view = view // binding.root,
            )
        }

        if (context.settings().showBookmarksHomeFeature) {
            bookmarksFeature.set(
                feature = BookmarksFeature(
                    appStore = components.appStore,
                    bookmarksUseCase = run {
                        context.components.useCases.bookmarksUseCases
                    },
                    scope = coroutineScope, // viewLifecycleOwner.lifecycleScope,
                ), owner = lifecycleOwner, // viewLifecycleOwner,
                view = view // binding.root,
            )
        }

        if (context.settings().historyMetadataUIFeature) {
            historyMetadataFeature.set(
                feature = RecentVisitsFeature(
                    appStore = components.appStore,
                    historyMetadataStorage = components.core.historyStorage,
                    historyHighlightsStorage = components.core.lazyHistoryStorage,
                    scope = coroutineScope, // viewLifecycleOwner.lifecycleScope,
                ), owner = lifecycleOwner, // viewLifecycleOwner,
                view = view // binding.root,
            )
        }

        // todo: snackbar
//        snackbarBinding.set(
//            feature = SnackbarBinding(
//                context = context,
//                browserStore = context.components.core.store,
//                appStore = context.components.appStore,
//                snackbarDelegate = FenixSnackbarDelegate(binding.dynamicSnackbarContainer),
//                navController = navController,
//                sendTabUseCases = SendTabUseCases(context.components.backgroundServices.accountManager),
//                customTabSessionId = null,
//            ),
//            owner = this,
//            view =view // binding.root,
//        )

        sessionControlInteractor = SessionControlInteractor(
            controller = DefaultSessionControlController(
                activity = activity,
                settings = components.settings,
                engine = components.core.engine,
                messageController = DefaultMessageController(
                    appStore = components.appStore,
                    messagingController = components.nimbus.messaging,
                    homeActivity = activity,
                ),
                store = store,
                tabCollectionStorage = components.core.tabCollectionStorage,
                addTabUseCase = components.useCases.tabsUseCases.addTab,
                restoreUseCase = components.useCases.tabsUseCases.restore,
                selectTabUseCase = components.useCases.tabsUseCases.selectTab,
                reloadUrlUseCase = components.useCases.sessionUseCases.reload,
                topSitesUseCases = components.useCases.topSitesUseCase,
                appStore = components.appStore,
                navController = navController,
                viewLifecycleScope = coroutineScope, // viewLifecycleOwner.lifecycleScope,
                registerCollectionStorageObserver = {
                    registerCollectionStorageObserver(
                        context, collectionStorageObserver
                    )
                },
                removeCollectionWithUndo = { tabCollection ->
                    removeCollectionWithUndo(
                        tabCollection, context, coroutineScope
                    )
                },
                showUndoSnackbarForTopSite = { topSite ->
                    showUndoSnackbarForTopSite(
                        topSite, context, coroutineScope
                    )
                },
                showTabTray = { openTabsTray(navController, browsingModeManager) },
            ),
            recentTabController = DefaultRecentTabsController(
                selectTabUseCase = components.useCases.tabsUseCases.selectTab,
                navController = navController,
                appStore = components.appStore,
            ),
            recentSyncedTabController = DefaultRecentSyncedTabController(
                tabsUseCase = context.components.useCases.tabsUseCases,
                navController = navController,
                accessPoint = TabsTrayAccessPoint.HomeRecentSyncedTab,
                appStore = components.appStore,
            ),
            bookmarksController = DefaultBookmarksController(
                activity = activity,
                navController = navController,
                appStore = components.appStore,
                browserStore = components.core.store,
                selectTabUseCase = components.useCases.tabsUseCases.selectTab,
            ),
            recentVisitsController = DefaultRecentVisitsController(
                navController = navController,
                appStore = components.appStore,
                selectOrAddTabUseCase = components.useCases.tabsUseCases.selectOrAddTab,
                storage = components.core.historyStorage,
                scope = coroutineScope, // viewLifecycleOwner.lifecycleScope,
                store = components.core.store,
            ),
//            pocketStoriesController = DefaultPocketStoriesController(
//                homeActivity context.getActivity()!!,
//                appStore = components.appStore,
//                settings = components.settings,
//            ),
            privateBrowsingController = DefaultPrivateBrowsingController(
                activity = activity,
                appStore = components.appStore,
                navController = navController,
            ),
            searchSelectorController = DefaultSearchSelectorController(
                activity = activity,
                navController = navController,
            ),
            toolbarController = DefaultToolbarController(
                activity = activity,
                store = components.core.store,
                navController = navController,
            ),
        )

        // todo: toolbar
//        toolbarView = ToolbarView(
//            binding = binding,
//            interactor = sessionControlInteractor,
//            homeFragment = this,
//            homeActivity = activity,
//        )

        if (context.settings().microsurveyFeatureEnabled) {
            listenForMicrosurveyMessage(context)
        }

        if (context.settings().enableComposeHomepage) {
            initHomepage()
        } else {
            // todo: sessionControlView
//            sessionControlView = SessionControlView(
//                containerView = binding.sessionControlRecyclerView,
//                viewLifecycleOwner = viewLifecycleOwner,
//                interactor = sessionControlInteractor,
//                fragmentManager = parentFragmentManager,
//            )

            updateSessionControlView(browsingModeManager)
        }

        disableAppBarDragging()

        FxNimbus.features.homescreen.recordExposure()

        // DO NOT MOVE ANYTHING BELOW THIS addMarker CALL!
        context.components.core.engine.profiler?.addMarker(
            MarkersFragmentLifecycleCallbacks.MARKER_NAME,
            profilerStartTime,
            "HomeFragment.onCreateView",
        )
//        return binding.root
        /* onCreateView */

        /* HomeFragment onConfigurationChanged todo: move to individual components */
        // todo: toolbar
//        toolbarView?.dismissMenu()

        // If the navbar feature could be visible, we should update it's state.
        val shouldUpdateNavBarState = context.settings().navigationToolbarEnabled
        if (shouldUpdateNavBarState) {
            // todo: navbar
//            updateNavBarForConfigurationChange(
//                context = context,
//                parent = binding.homeLayout,
//                toolbarView = binding.toolbarLayout,
//                bottomToolbarContainerView = _bottomToolbarContainerView?.toolbarContainerView,
//                reinitializeNavBar = { reinitializeNavBar(context, coroutineScope) },
//                reinitializeMicrosurveyPrompt = { initializeMicrosurveyPrompt() },
//            )
            context.shouldAddNavigationBar().let {
                // todo: toolbar
//                toolbarView?.updateButtonVisibility(
//                    context.components.core.store.state,
//                    it,
//                )
            }
        }

        // If the microsurvey feature is visible, we should update it's state.
        if (shouldShowMicrosurveyPrompt(context) && !shouldUpdateNavBarState) {
            // todo: microsurvey
//            updateMicrosurveyPromptForConfigurationChange(
//                parent = binding.homeLayout,
//                bottomToolbarContainerView = _bottomToolbarContainerView?.toolbarContainerView,
//                reinitializeMicrosurveyPrompt = { initializeMicrosurveyPrompt() },
//            )
        }

        currentWallpaperName = context.settings().currentWallpaperName
        applyWallpaper(
            wallpaperName = currentWallpaperName,
            orientationChange = true,
            orientation = localConfiguration.orientation,
            context = context,
            coroutineScope = coroutineScope,
            lastAppliedWallpaperName = lastAppliedWallpaperName,
            setLastAppliedWallpaperName = setLastAppliedWallpaperName,
        )/* HomeFragment onConfigurationChanged */

        /* HomeFragment onViewCreated */
        // DO NOT ADD ANYTHING ABOVE THIS getProfilerTime CALL!
        profilerStartTime = context.components.core.engine.profiler?.getProfilerTime()

//        super.onViewCreated(view, savedInstanceState)
//        HomeScreen.homeScreenDisplayed.record(NoExtras())

//        with(context) {
//            if (settings().isExperimentationEnabled) {
//                recordEventInNimbus("home_screen_displayed")
//            }
//        }

//        HomeScreen.homeScreenViewCount.add()
//        if (!browsingModeManager.mode.isPrivate) {
//            HomeScreen.standardHomepageViewCount.add()
//        }

        observeSearchEngineNameChanges()
        observeWallpaperUpdates(context, coroutineScope)

        homeScreenPopupManager.set(
            feature = HomeScreenPopupManager(
                appStore = context.components.appStore,
                settings = context.settings(),
            ),
            owner = lifecycleOwner, // viewLifecycleOwner,
            view = view, // binding.root,
        )

        val shouldAddNavigationBar = context.shouldAddNavigationBar()
        if (shouldAddNavigationBar) {
            initializeNavBar(context.getActivity()!! as HomeActivity, context, coroutineScope)
        }

        // todo: toolbar
//        toolbarView?.build(context.components.core.store.state)
        if (context.isTabStripEnabled()) {
            initTabStrip()
        }

        // todo: private browsing
//        PrivateBrowsingButtonView(binding.privateBrowsingButton, browsingModeManager) { newMode ->
//            sessionControlInteractor.onPrivateModeButtonClicked(newMode)
////            Homepage.privateModeIconTapped.record(mozilla.telemetry.glean.private.NoExtras())
//        }


        consumeFrom(
            store = context.components.core.store,
            context = context,
            lifecycleOwner = lifecycleOwner,
        ) {
            // todo: toolbarView
//            toolbarView?.updateTabCounter(it)
            showCollectionsPlaceholder(it)
        }

        // TODO: homeViewModel
//        homeViewModel.sessionToDelete?.also {
//            if (it == ALL_NORMAL_TABS || it == ALL_PRIVATE_TABS) {
//                removeAllTabsAndShowSnackbar(it)
//            } else {
//                removeTabAndShowSnackbar(it)
//            }
//        }
//
//        homeViewModel.sessionToDelete = null

        // Determine if we should show the "Set as Default Browser" prompt
        if (context.settings().shouldShowSetAsDefaultPrompt && !BrowsersCache.all(context.applicationContext).isDefaultBrowser && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // This is to avoid disk read violations on some devices such as samsung and pixel for android 9/10
            context.components.strictMode.resetAfter(StrictMode.allowThreadDiskReads()) {
                showSetAsDefaultBrowserPrompt(context)
            }
        }

        context.components.appStore.state.wasLastTabClosedPrivate?.also {
            showUndoSnackbar(
                context.tabClosedUndoMessage(it), context, coroutineScope, navController
            )
            context.components.appStore.dispatch(AppAction.TabStripAction.UpdateLastTabClosed(null))
        }

        // TODO: toolbar
//        toolbarView?.updateTabCounter(context.components.core.store.state)

        val focusOnAddressBar = FxNimbus.features.oneClickSearch.value().enabled
//          todo:  bundleArgs.getBoolean(FOCUS_ON_ADDRESS_BAR) || FxNimbus.features.oneClickSearch.value().enabled

        if (focusOnAddressBar) {
            // If the fragment gets recreated by the activity, the search fragment might get recreated as well. Changing
            // between browsing modes triggers activity recreation, so when changing modes goes together with navigating
            // home, we should avoid navigating to search twice.
            val searchFragmentAlreadyAdded =
                parentFragmentManager.fragments.any { it is SearchDialogFragment }
            if (!searchFragmentAlreadyAdded) {
                sessionControlInteractor!!.onNavigateSearch()
            }
        }
        // todo:
//        else if (bundleArgs.getBoolean(SCROLL_TO_COLLECTION)) {
//            // todo: sessionControlView
////            MainScope().launch {
////                delay(ANIM_SCROLL_DELAY)
////                val smoothScroller: SmoothScroller =
////                    object : LinearSmoothScroller(sessionControlView!!.view.context) {
////                        override fun getVerticalSnapPreference(): Int {
////                            return SNAP_TO_START
////                        }
////                    }
////                val recyclerView = sessionControlView!!.view
////                val adapter = recyclerView.adapter!!
////                val collectionPosition = IntRange(0, adapter.itemCount - 1).firstOrNull {
////                    adapter.getItemViewType(it) == CollectionHeaderViewHolder.LAYOUT_ID
////                }
////                collectionPosition?.run {
////                    val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
////                    smoothScroller.targetPosition = this
////                    linearLayoutManager.startSmoothScroll(smoothScroller)
////                }
////            }
//        }

        // todo: searchSelector
//        searchSelectorBinding.set(
//            feature = SearchSelectorBinding(
//                context = view.context,
//                binding = binding,
//                browserStore = context.components.core.store,
//                searchSelectorMenu = searchSelectorMenu,
//            ),
//            owner = viewLifecycleOwner,
//            view =view // binding.root,
//        )

        searchSelectorMenuBinding.set(
            feature = SearchSelectorMenuBinding(
                context = view.context,
                interactor = sessionControlInteractor!!,
                searchSelectorMenu = searchSelectorMenu,
                browserStore = context.components.core.store,
            ),
            owner = lifecycleOwner, // viewLifecycleOwner,
            view = view,
        )

        // DO NOT MOVE ANYTHING BELOW THIS addMarker CALL!
        context.components.core.engine.profiler?.addMarker(
            MarkersFragmentLifecycleCallbacks.MARKER_NAME,
            profilerStartTime,
            "HomeFragment.onViewCreated",
        )/* HomeFragment onViewCreated */
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
//                    super.onStart()

                    subscribeToTabCollections(context, lifecycleOwner)

//                    val context = context

                    context.components.backgroundServices.accountManagerAvailableQueue.runIfReadyOrQueue {
//                        // By the time this code runs, we may not be attached to a context or have a view lifecycle owner.
////                        if ((this@HomeFragment).view?.context == null) {
//                        if (view == null) {
//                            return@runIfReadyOrQueue
//                        }

                        context.components.backgroundServices.accountManager.register(
                            object : AccountObserver {
                                override fun onAuthenticated(
                                    account: OAuthAccount, authType: AuthType
                                ) {
                                    if (authType != AuthType.Existing) {
                                        view?.let {
                                            // todo: snackbar
//                                            Snackbar.make(
//                                                snackBarParentView = binding.dynamicSnackbarContainer,
//                                                snackbarState = SnackbarState(
//                                                    message = it.context.getString(R.string.onboarding_firefox_account_sync_is_on),
//                                                ),
//                                            ).show()
                                        }
                                    }
                                }
                            },
                            owner = lifecycleOwner, // this@HomeFragment.viewLifecycleOwner,
                        )
                    }

                    if (browsingModeManager.mode.isPrivate // &&
                    // We will be showing the search dialog and don't want to show the CFR while the dialog shows
//                     todo:   !bundleArgs.getBoolean(HomeFragment.FOCUS_ON_ADDRESS_BAR) && context.settings().shouldShowPrivateModeCfr
                    ) {
                        recommendPrivateBrowsingShortcut(
                            context, recommendPrivateBrowsingCFR, setRecommendPrivateBrowsingCFR
                        )
                    }

                    // We only want this observer live just before we navigate away to the collection creation screen
                    context.components.core.tabCollectionStorage.unregister(
                        collectionStorageObserver
                    )

                    // TODO: review prompt system
//        lifecycleScope.launch(IO) {
//            context.components.reviewPromptController.promptReview(requireActivity())
//        }
                }

                Lifecycle.Event.ON_STOP -> {
                    dismissRecommendPrivateBrowsingShortcut(
                        recommendPrivateBrowsingCFR, setRecommendPrivateBrowsingCFR
                    )
//                    super.onStop()
                }

                Lifecycle.Event.ON_PAUSE -> {
//                    super.onPause()
                    if (browsingModeManager.mode == BrowsingMode.Private) {
                        context.getActivity()?.window?.setBackgroundDrawable(
                            ColorDrawable(
                                getColor(
                                    context, R.color.fx_mobile_private_layer_color_1
                                )
                            ),
                        )
                    }

                    // Counterpart to the update in onResume to keep the last access timestamp of the selected
                    // tab up-to-date.
                    context.components.useCases.sessionUseCases.updateLastAccess()
                }

                Lifecycle.Event.ON_RESUME -> {
//                    super.onResume()
                    if (browsingModeManager.mode == BrowsingMode.Private) {
                        context.getActivity()?.window?.setBackgroundDrawableResource(R.drawable.private_home_background_gradient)
                    }

                    // todo: toolbar
//                    hideToolbar()

                    val components = context.components
                    // Whenever a tab is selected its last access timestamp is automatically updated by A-C.
                    // However, in the case of resuming the app to the home fragment, we already have an
                    // existing selected tab, but its last access timestamp is outdated. No action is
                    // triggered to cause an automatic update on warm start (no tab selection occurs). So we
                    // update it manually here.
                    components.useCases.sessionUseCases.updateLastAccess()

                    evaluateMessagesForMicrosurvey(components)
                }

                Lifecycle.Event.ON_CREATE -> {
                    // DO NOT ADD ANYTHING ABOVE THIS getProfilerTime CALL!
                    var profilerStartTime =
                        context.components.core.engine.profiler?.getProfilerTime()

//        super.onCreate(savedInstanceState)

                    // todo: bundleArgs
//        bundleArgs = args.toBundle()
//        if (savedInstanceState != null) {
//            bundleArgs.putBoolean(FOCUS_ON_ADDRESS_BAR, false)
//        }

                    // todo: fragmentResultListener
//        setFragmentResultListener(SearchDialogFragment.SEARCH_VISIBILITY_RESPONSE_KEY) { _, _ ->
//            resetNavbar()
//        }

                    // DO NOT MOVE ANYTHING BELOW THIS addMarker CALL!
                    context.components.core.engine.profiler?.addMarker(
                        MarkersFragmentLifecycleCallbacks.MARKER_NAME,
                        profilerStartTime,
                        "HomeFragment.onCreate",
                    )
                }

                Lifecycle.Event.ON_DESTROY -> {
                    // TODO: onDestroy
//                    super.onDestroyView()
                    sessionControlInteractor = null
//                    sessionControlView = null
//                    toolbarView = null
//                    _bottomToolbarContainerView = null
//                    _binding = null

//                 todo:   bundleArgs.clear()
                    setLastAppliedWallpaperName(Wallpaper.defaultName)
                }

                else -> {

                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = Color.Black,
        content = {
            // TODO: layout
            it.toString()
            InfernoText(
                text = "Home page under construction, is private page: ${if (private) "true" else "false"}",
                modifier = Modifier
                    .fillMaxWidth(),
                fontColor = Color.Black,
            )
        }
    )
}


@Suppress("LongMethod", "ComplexMethod")
private fun initializeNavBar(
    activity: HomeActivity,
    context: Context,
    coroutineScope: CoroutineScope,
    isConfigChange: Boolean = false,
) {
//        NavigationBar.homeInitializeTimespan.start()

//    val context = context
    val isToolbarAtBottom = context.isToolbarAtBottom()

    // The toolbar view has already been added directly to the container.
    // We should remove it and add the view to the navigation bar container.
    // Should refactor this so there is no added view to remove to begin with:
    // https://bugzilla.mozilla.org/show_bug.cgi?id=1870976
    // TODO: toolbar
//    if (isToolbarAtBottom) {
//        binding.root.removeView(binding.toolbarLayout)
//    }

    val menuButton = MenuButton(context)
    menuButton.recordClickEvent = {} // { NavigationBar.homeMenuTapped.record(NoExtras()) }
    // TODO: home menu
//    HomeMenuView(
//        context = context,
//        lifecycleOwner = viewLifecycleOwner,
//        homeActivity = activity,
//        navController = navController,
//        homeFragment = this,
//        menuButton = WeakReference(menuButton),
//    ).also { it.build() }

    // TODO: toolbar
//    _bottomToolbarContainerView = BottomToolbarContainerView(
//        context = context,
//        parent = binding.homeLayout,
//        hideOnScroll = false,
//        content = {
//            val searchFragmentAlreadyAdded = parentFragmentManager.fragments.any { it is SearchDialogFragment }
//            val searchFragmentShouldBeAdded = !isConfigChange && bundleArgs.getBoolean(
//                FOCUS_ON_ADDRESS_BAR
//            )
//            val isSearchActive = searchFragmentAlreadyAdded || searchFragmentShouldBeAdded
//
//            FirefoxTheme {
//                Column {
//                    val shouldShowNavBarCFR =
//                        context.shouldAddNavigationBar() && context.settings().shouldShowNavigationBarCFR
//                    val shouldShowMicrosurveyPrompt = !activity.isMicrosurveyPromptDismissed.value
//                    var isMicrosurveyShown = false
//
//                    if (!isSearchActive && shouldShowMicrosurveyPrompt && !shouldShowNavBarCFR) {
//                        currentMicrosurvey
//                            ?.let {
//                                isMicrosurveyShown = true
//                                if (isToolbarAtBottom) {
//                                    updateToolbarViewUIForMicrosurveyPrompt()
//                                }
//
//                                Divider()
//
//                                MicrosurveyRequestPrompt(
//                                    microsurvey = it,
//                                    activity = activity,
//                                    onStartSurveyClicked = {
//                                        context.components.appStore.dispatch(
//                                            MicrosurveyAction.Started(it.id),
//                                        )
//                                        navController.nav(
//                                            R.id.homeFragment,
//                                            HomeFragmentDirections.actionGlobalMicrosurveyDialog(it.id),
//                                        )
//                                    },
//                                    onCloseButtonClicked = {
//                                        context.components.appStore.dispatch(
//                                            MicrosurveyAction.Dismissed(it.id),
//                                        )
//                                        context.settings().shouldShowMicrosurveyPrompt = false
//                                        activity.isMicrosurveyPromptDismissed.value = true
//
//                                        resetToolbarViewUI()
//                                        reinitializeNavBar()
//                                    },
//                                )
//                            }
//                    } else {
//                        binding.bottomBarShadow.visibility = View.VISIBLE
//                    }
//
//                    if (isToolbarAtBottom) {
//                        AndroidView(factory = { _ -> binding.toolbarLayout })
//                    }
//
//                    val showCFR = !isSearchActive &&
//                            homeScreenPopupManager.get()?.navBarCFRVisibility?.collectAsState()?.value ?: false
//
//                    CFRPopupLayout(
//                        showCFR = showCFR,
//                        properties = CFRPopupProperties(
//                            popupBodyColors = listOf(
//                                FirefoxTheme.colors.layerGradientEnd.toArgb(),
//                                FirefoxTheme.colors.layerGradientStart.toArgb(),
//                            ),
//                            dismissButtonColor = FirefoxTheme.colors.iconOnColor.toArgb(),
//                            indicatorDirection = CFRPopup.IndicatorDirection.DOWN,
//                            popupVerticalOffset = 10.dp,
//                            indicatorArrowStartOffset = 130.dp,
//                        ),
//                        onCFRShown = {}, // { NavigationBar.navigationBarCfrShown.record(NoExtras()) },
//                        onDismiss = {
////                                NavigationBar.navigationBarCfrDismissed.record(NoExtras())
//                            homeScreenPopupManager.get()?.setNavbarCFRShown(true)
//                        },
//                        title = {
//                            FirefoxTheme {
//                                Text(
//                                    text = stringResource(R.string.navbar_cfr_title),
//                                    color = FirefoxTheme.colors.textOnColorPrimary,
//                                    style = FirefoxTheme.typography.subtitle2,
//                                )
//                            }
//                        },
//                        text = {
//                            FirefoxTheme {
//                                Text(
//                                    text = stringResource(R.string.navbar_cfr_message_2),
//                                    color = FirefoxTheme.colors.textOnColorPrimary,
//                                    style = FirefoxTheme.typography.body2,
//                                )
//                            }
//                        },
//                    ) {
//                        val tabCounterMenu = lazy {
//                            FenixTabCounterMenu(
//                                context = context,
//                                onItemTapped = { item ->
//                                    if (item is TabCounterMenu.Item.NewTab) {
//                                        browsingModeManager.mode = BrowsingMode.Normal
//                                        val directions =
//                                            NavGraphDirections.actionGlobalSearchDialog(
//                                                sessionId = null,
//                                            )
//
//                                        navController.nav(
//                                            navController.currentDestination?.id,
//                                            directions,
//                                            BrowserAnimator.getToolbarNavOptions(activity),
//                                        )
//                                    } else if (item is TabCounterMenu.Item.NewPrivateTab) {
//                                        browsingModeManager.mode = BrowsingMode.Private
//                                        val directions =
//                                            NavGraphDirections.actionGlobalSearchDialog(
//                                                sessionId = null,
//                                            )
//
//                                        navController.nav(
//                                            navController.currentDestination?.id,
//                                            directions,
//                                            BrowserAnimator.getToolbarNavOptions(activity),
//                                        )
//                                    }
//                                },
//                                iconColor = when (activity.browsingModeManager.mode.isPrivate) {
//                                    true -> getColor(context, R.color.fx_mobile_private_icon_color_primary)
//
//                                    else -> null
//                                },
//                            ).also {
//                                it.updateMenu()
//                            }
//                        }
//
//                        if (!isSearchActive) {
//                            HomeNavBar(
//                                isPrivateMode = activity.browsingModeManager.mode.isPrivate,
//                                showDivider = !isMicrosurveyShown && !isToolbarAtBottom,
//                                browserStore = context.components.core.store,
//                                appStore = context.components.appStore,
//                                menuButton = menuButton,
//                                tabsCounterMenu = tabCounterMenu,
//                                onSearchButtonClick = {
////                                        NavigationBar.homeSearchTapped.record(NoExtras())
//                                    val directions =
//                                        NavGraphDirections.actionGlobalSearchDialog(
//                                            sessionId = null,
//                                        )
//
//                                    navController.nav(
//                                        navController.currentDestination?.id,
//                                        directions,
//                                        BrowserAnimator.getToolbarNavOptions(activity),
//                                    )
//                                },
//                                onTabsButtonClick = {
////                                        NavigationBar.homeTabTrayTapped.record(NoExtras())
//                                    navController.nav(
//                                        navController.currentDestination?.id,
//                                        NavGraphDirections.actionGlobalTabsTrayFragment(
//                                            page = when (browsingModeManager.mode) {
//                                                BrowsingMode.Normal -> Page.NormalTabs
//                                                BrowsingMode.Private -> Page.PrivateTabs
//                                            },
//                                        ),
//                                    )
//                                },
//                                onTabsButtonLongPress = {
////                                        NavigationBar.homeTabTrayLongTapped.record(NoExtras())
//                                },
//                                onMenuButtonClick = {
////                                        NavigationBar.homeMenuTapped.record(NoExtras())
//                                    navController.nav(
//                                        navController.currentDestination?.id,
//                                        HomeFragmentDirections.actionGlobalMenuDialogFragment(
//                                            accesspoint = MenuAccessPoint.Home,
//                                        ),
//                                    )
//                                },
//                            )
//                        }
//                    }
//                }
//            }
//        },
//    )

//        NavigationBar.homeInitializeTimespan.stop()
}

private fun reinitializeNavBar(context: Context, coroutineScope: CoroutineScope) {
    initializeNavBar(
        activity = context.getActivity()!! as HomeActivity,
        context = context,
        coroutineScope = coroutineScope,
        isConfigChange = true,
    )
}

@androidx.annotation.VisibleForTesting
internal fun initializeMicrosurveyFeature(isMicrosurveyEnabled: Boolean) {
    if (isMicrosurveyEnabled) {
        // todo: microsurvey
//        messagingFeatureMicrosurvey.set(
//            feature = MessagingFeature(
//                appStore = context.components.appStore,
//                surface = FenixMessageSurfaceId.MICROSURVEY,
//            ),
//            owner = viewLifecycleOwner,
//            view =view // binding.root,
//        )
    }
}

private fun initializeMicrosurveyPrompt(context: Context) {
//    val context = context

    val isToolbarAtTheBottom = context.isToolbarAtBottom()
    // The toolbar view has already been added directly to the container.
    // See initializeNavBar for more details on improving this.
    // todo: toolbar
//    if (isToolbarAtTheBottom) {
//        binding.root.removeView(binding.toolbarLayout)
//    }

    // todo: toolbar
//    _bottomToolbarContainerView = BottomToolbarContainerView(
//        context = context,
//        parent = binding.homeLayout,
//        content = {
//            FirefoxTheme {
//                Column {
//                    val activity = requireActivity() as HomeActivity
//                    val shouldShowNavBarCFR =
//                        context.shouldAddNavigationBar() && context.settings().shouldShowNavigationBarCFR
//                    val shouldShowMicrosurveyPrompt = !activity.isMicrosurveyPromptDismissed.value
//
//                    if (shouldShowMicrosurveyPrompt && !shouldShowNavBarCFR) {
//                        currentMicrosurvey
//                            ?.let {
//                                if (isToolbarAtTheBottom) {
//                                    updateToolbarViewUIForMicrosurveyPrompt()
//                                }
//
//                                Divider()
//
//                                MicrosurveyRequestPrompt(
//                                    microsurvey = it,
//                                    activity = activity,
//                                    onStartSurveyClicked = {
//                                        context.components.appStore.dispatch(MicrosurveyAction.Started(it.id))
//                                        navController.nav(
//                                            R.id.homeFragment,
//                                            HomeFragmentDirections.actionGlobalMicrosurveyDialog(it.id),
//                                        )
//                                    },
//                                    onCloseButtonClicked = {
//                                        context.components.appStore.dispatch(
//                                            MicrosurveyAction.Dismissed(it.id),
//                                        )
//                                        context.settings().shouldShowMicrosurveyPrompt = false
//                                        activity.isMicrosurveyPromptDismissed.value = true
//
//                                        resetToolbarViewUI()
//                                        initializeMicrosurveyPrompt()
//                                    },
//                                )
//                            }
//                    } else {
//                        binding.bottomBarShadow.visibility = View.VISIBLE
//                    }
//
//                    if (isToolbarAtTheBottom) {
//                        AndroidView(factory = { _ -> binding.toolbarLayout })
//                    }
//                }
//            }
//        },
//    )
}

private fun updateToolbarViewUIForMicrosurveyPrompt() {
    updateToolbarViewUI(R.drawable.home_bottom_bar_background_no_divider, View.GONE, 0.0f)
}

private fun resetToolbarViewUI(context: Context) {
    val elevation = if (context?.settings()?.navigationToolbarEnabled == true) {
        0f
    } else {
        context.resources.getDimension(R.dimen.browser_fragment_toolbar_elevation)
    }
    // todo: toolbar
//    _binding?.homeLayout?.removeView(bottomToolbarContainerView.toolbarContainerView)
    updateToolbarViewUI(
        R.drawable.home_bottom_bar_background,
        View.VISIBLE,
        elevation,
    )
}

/**
 * Build and show a new navbar.
 * Useful when needed to force an update of it's layout.
 */
private fun resetNavbar(context: Context) {
//    val safeContext = context ?: return
    // todo: navbar
//    if (!safeContext.shouldAddNavigationBar()) return

    // Prevent showing two navigation bars at the same time.
    // todo: navbar
//    binding.root.removeView(bottomToolbarContainerView.toolbarContainerView)
//    reinitializeNavBar()
}

private fun updateToolbarViewUI(@DrawableRes id: Int, visibility: Int, elevation: Float) {
    // todo: toolbar
//    _binding?.bottomBar?.background = compatDrawableFor(id)
//    _binding?.bottomBarShadow?.visibility = visibility
//    _binding?.toolbarLayout?.elevation = elevation
}

private fun compatDrawableFor(@DrawableRes id: Int, context: Context) =
//    ResourcesCompat.getDrawable(resources, id, null)
    ResourcesCompat.getDrawable(context.resources, id, null)

private var currentMicrosurvey: MicrosurveyUIData? = null

/**
 * Listens for the microsurvey message and initializes the microsurvey prompt if one is available.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun listenForMicrosurveyMessage(context: Context) {
    // todo: toolbar microsurvey
//    binding.root.consumeFrom(context.components.appStore, viewLifecycleOwner) { state ->
//        state.messaging.messageToShow[FenixMessageSurfaceId.MICROSURVEY]?.let { message ->
//            if (message.id != currentMicrosurvey?.id) {
//                message.toMicrosurveyUIData()?.let { microsurvey ->
//                    context.components.settings.shouldShowMicrosurveyPrompt = true
//                    currentMicrosurvey = microsurvey
//
//                    if (context.shouldAddNavigationBar()) {
//                        _bottomToolbarContainerView?.toolbarContainerView.let {
//                            binding.homeLayout.removeView(it)
//                        }
//                        reinitializeNavBar()
//                    } else {
//                        initializeMicrosurveyPrompt()
//                    }
//                }
//            }
//        }
//    }
}

private fun shouldShowMicrosurveyPrompt(context: Context) =
    context.components.settings.shouldShowMicrosurveyPrompt

/**
 * Returns a [TopSitesConfig] which specifies how many top sites to display and whether or
 * not frequently visited sites should be displayed.
 */
@androidx.annotation.VisibleForTesting
fun getTopSitesConfig(context: Context): TopSitesConfig {
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
                    AMAZON_SEARCH_ENGINE_NAME -> topSite.title != AMAZON_SPONSORED_TITLE
                    EBAY_SPONSORED_TITLE -> topSite.title != EBAY_SPONSORED_TITLE
                    else -> true
                }
            },
        ),
    )
}

@androidx.annotation.VisibleForTesting
internal fun showUndoSnackbarForTopSite(
    topSite: TopSite, context: Context, coroutineScope: CoroutineScope
) {
//    lifecycleScope.allowUndo(
    // todo: snackbar
//    coroutineScope.allowUndo(
//        view = binding.dynamicSnackbarContainer,
//        message = getString(R.string.snackbar_top_site_removed),
//        undoActionTitle = getString(R.string.snackbar_deleted_undo),
//        onCancel = {
//            context.components.useCases.topSitesUseCase.addPinnedSites(
//                topSite.title.toString(),
//                topSite.url,
//            )
//        },
//        operation = { },
//        elevation = TOAST_ELEVATION,
//    )
}

/**
 * The [SessionControlView] is forced to update with our current state when we call
 * [HomeFragment.onCreateView] in order to be able to draw everything at once with the current
 * data in our store. The [View.consumeFrom] coroutine dispatch
 * doesn't get run right away which means that we won't draw on the first layout pass.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun updateSessionControlView(browsingModeManager: BrowsingModeManager) {
    if (browsingModeManager.mode == BrowsingMode.Private) {
        // todo: session control view
//        binding.root.consumeFrom(context.components.appStore, viewLifecycleOwner) {
//            sessionControlView?.update(it)
//        }
    } else {
        // todo: session control view
//        sessionControlView?.update(context.components.appStore.state)
//
//        binding.root.consumeFrom(context.components.appStore, viewLifecycleOwner) {
//            sessionControlView?.update(it, shouldReportMetrics = true)
//        }
    }
}

private fun disableAppBarDragging() {
    // todo: home app bar
//    if (binding.homeAppBar.layoutParams != null) {
//        val appBarLayoutParams = binding.homeAppBar.layoutParams as CoordinatorLayout.LayoutParams
//        val appBarBehavior = AppBarLayout.Behavior()
//        appBarBehavior.setDragCallback(
//            object : AppBarLayout.Behavior.DragCallback() {
//                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
//                    return false
//                }
//            },
//        )
//        appBarLayoutParams.behavior = appBarBehavior
//    }
//    binding.homeAppBar.setExpanded(true)
}

private fun initHomepage() {
    // todo: homepage
//    binding.homepageView.isVisible = true
//
//    binding.homepageView.apply {
//        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
//
//        setContent {
//            FirefoxTheme {
//                val settings = LocalContext.current.settings()
//                val appState by components.appStore.observeAsState(
//                    initialValue = components.appStore.state,
//                ) { it }
//
//                Homepage(
//                    state = HomepageState.build(
//                        appState = appState,
//                        settings = settings,
//                        browsingModeManager = browsingModeManager,
//                    ),
//                    interactor = sessionControlInteractor,
//                    onTopSitesItemBound = {
//                        StartupTimeline.onTopSitesItemBound(activity = (requireActivity() as HomeActivity))
//                    },
//                )
//            }
//        }
//    }
}

private fun initTabStrip() {
    // todo: tabstrip
//    binding.tabStripView.isVisible = true
//    binding.tabStripView.apply {
//        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
//        setContent {
//            FirefoxTheme {
//                TabStrip(
//                    onHome = true,
//                    onAddTabClick = {
//                        sessionControlInteractor.onNavigateSearch()
////                            TabStripMetrics.newTabTapped.record()
//                    },
//                    onSelectedTabClick = {
//                        (requireActivity() as HomeActivity).openToBrowser(BrowserDirection.FromHome)
////                            TabStripMetrics.selectTab.record()
//                    },
//                    onLastTabClose = {},
//                    onCloseTabClick = { isPrivate ->
//                        showUndoSnackbar(context.tabClosedUndoMessage(isPrivate))
////                            TabStripMetrics.closeTab.record()
//                    },
//                    onPrivateModeToggleClick = { mode ->
//                        browsingModeManager.mode = mode
//                    },
//                    onTabCounterClick = { openTabsTray() },
//                )
//            }
//        }
//    }
}

/**
 * Method used to listen to search engine name changes and trigger a top sites update accordingly
 */
private fun observeSearchEngineNameChanges() {
    // todo: search engine name
//    consumeFlow(store) { flow ->
//        flow.map { state ->
//            when (state.search.selectedOrDefaultSearchEngine?.name) {
//                AMAZON_SEARCH_ENGINE_NAME -> AMAZON_SPONSORED_TITLE
//                EBAY_SPONSORED_TITLE -> EBAY_SPONSORED_TITLE
//                else -> null
//            }
//        }.distinctUntilChanged().collect {
//                topSitesFeature.withFeature {
//                    it.storage.notifyObservers { onStorageUpdated() }
//                }
//            }
//    }
}

private fun removeAllTabsAndShowSnackbar(
    sessionCode: String, context: Context, coroutineScope: CoroutineScope
) {
    if (sessionCode == ALL_PRIVATE_TABS) {
        context.components.useCases.tabsUseCases.removePrivateTabs()
    } else {
        context.components.useCases.tabsUseCases.removeNormalTabs()
    }

    val snackbarMessage = if (sessionCode == ALL_PRIVATE_TABS) {
        if (context.settings().feltPrivateBrowsingEnabled) {
            context.getString(R.string.snackbar_private_data_deleted)
        } else {
            context.getString(R.string.snackbar_private_tabs_closed)
        }
    } else {
        context.getString(R.string.snackbar_tabs_closed)
    }

//    viewLifecycleOwner.lifecycleScope.allowUndo(
    // todo: snackbar tab action
//    coroutineScope.allowUndo(
//        binding.dynamicSnackbarContainer,
//        snackbarMessage,
//        context.getString(R.string.snackbar_deleted_undo),
//        {
//            context.components.useCases.tabsUseCases.undo.invoke()
//        },
//        operation = { },
//    )
}

private fun removeTabAndShowSnackbar(
    sessionId: String,
    context: Context,
    coroutineScope: CoroutineScope,
    navController: NavController
) {
    val tab = context.components.core.store.state.findTab(sessionId) ?: return
    context.components.useCases.tabsUseCases.removeTab(sessionId)
    showUndoSnackbar(
        context.tabClosedUndoMessage(tab.content.private), context, coroutineScope, navController
    )
}

private fun showUndoSnackbar(
    message: String, context: Context, coroutineScope: CoroutineScope, navController: NavController
) {
//    viewLifecycleOwner.lifecycleScope.allowUndo(
    // todo: snackbar undo
//    coroutineScope.allowUndo(
//        binding.dynamicSnackbarContainer,
//        message,
//        context.getString(R.string.snackbar_deleted_undo),
//        {
//            context.components.useCases.tabsUseCases.undo.invoke()
//            navController.navigate(
//                HomeFragmentDirections.actionGlobalBrowser(null),
//            )
//        },
//        operation = { },
//    )
}

@androidx.annotation.VisibleForTesting
internal fun removeCollectionWithUndo(
    tabCollection: TabCollection, context: Context, coroutineScope: CoroutineScope
) {
    val snackbarMessage = context.getString(R.string.snackbar_collection_deleted)

//    lifecycleScope.allowUndo(
    // todo: snackbar
//    coroutineScope.allowUndo(
//        binding.dynamicSnackbarContainer,
//        snackbarMessage,
//        context.getString(R.string.snackbar_deleted_undo),
//        {
//            context.components.core.tabCollectionStorage.createCollection(tabCollection)
//        },
//        operation = { },
//        elevation = TOAST_ELEVATION,
//    )

//    lifecycleScope.launch(IO) {
    coroutineScope.launch(IO) {
        context.components.core.tabCollectionStorage.removeCollection(tabCollection)
    }
}

private fun evaluateMessagesForMicrosurvey(components: Components) =
    components.appStore.dispatch(MessagingAction.Evaluate(FenixMessageSurfaceId.MICROSURVEY))

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("LongMethod")
private fun recommendPrivateBrowsingShortcut(
    context: Context,
    recommendPrivateBrowsingCFR: CFRPopup?,
    setRecommendPrivateBrowsingCFR: (CFRPopup?) -> Unit
) {
    context?.let { context ->
        // todo: cfr popup
//        CFRPopup(
//            anchor = binding.privateBrowsingButton,
//            properties = CFRPopupProperties(
//                popupWidth = 256.dp,
//                popupAlignment = CFRPopup.PopupAlignment.INDICATOR_CENTERED_IN_ANCHOR,
//                popupBodyColors = listOf(
//                    getColor(context, R.color.fx_mobile_layer_color_gradient_end),
//                    getColor(context, R.color.fx_mobile_layer_color_gradient_start),
//                ),
//                showDismissButton = false,
//                dismissButtonColor = getColor(context, R.color.fx_mobile_icon_color_oncolor),
//                indicatorDirection = CFRPopup.IndicatorDirection.UP,
//            ),
//            onDismiss = {
////                    PrivateBrowsingShortcutCfr.cancel.record()
//                context.settings().showedPrivateModeContextualFeatureRecommender = true
//                context.settings().lastCfrShownTimeInMillis = System.currentTimeMillis()
//                dismissRecommendPrivateBrowsingShortcut(
//                    recommendPrivateBrowsingCFR, setRecommendPrivateBrowsingCFR
//                )
//            },
//            text = {
//                FirefoxTheme {
//                    Text(
//                        text = context.getString(R.string.private_mode_cfr_message_2),
//                        color = FirefoxTheme.colors.textOnColorPrimary,
//                        style = FirefoxTheme.typography.headline7,
//                        modifier = Modifier.semantics {
//                            testTagsAsResourceId = true
//                            testTag = "private.message"
//                        },
//                    )
//                }
//            },
//            action = {
//                FirefoxTheme {
//                    TextButton(
//                        onClick = {
////                                PrivateBrowsingShortcutCfr.addShortcut.record(NoExtras())
//                            PrivateShortcutCreateManager.createPrivateShortcut(context)
//                            context.settings().showedPrivateModeContextualFeatureRecommender = true
//                            context.settings().lastCfrShownTimeInMillis = System.currentTimeMillis()
//                            dismissRecommendPrivateBrowsingShortcut(
//                                recommendPrivateBrowsingCFR, setRecommendPrivateBrowsingCFR
//                            )
//                        },
//                        colors = ButtonDefaults.buttonColors(containerColor = PhotonColors.LightGrey30),
//                        shape = RoundedCornerShape(8.dp),
//                        modifier = Modifier
//                            .padding(top = 16.dp)
//                            .heightIn(36.dp)
//                            .fillMaxWidth()
//                            .semantics {
//                                testTagsAsResourceId = true
//                                testTag = "private.add"
//                            },
//                    ) {
//                        Text(
//                            text = context.getString(R.string.private_mode_cfr_pos_button_text),
//                            color = PhotonColors.DarkGrey50,
//                            style = FirefoxTheme.typography.headline7,
//                            textAlign = TextAlign.Center,
//                        )
//                    }
//                    TextButton(
//                        onClick = {
////                                PrivateBrowsingShortcutCfr.cancel.record()
//                            context.settings().showedPrivateModeContextualFeatureRecommender = true
//                            context.settings().lastCfrShownTimeInMillis = System.currentTimeMillis()
//                            dismissRecommendPrivateBrowsingShortcut(
//                                recommendPrivateBrowsingCFR, setRecommendPrivateBrowsingCFR
//                            )
//                        },
//                        modifier = Modifier
//                            .heightIn(36.dp)
//                            .fillMaxWidth()
//                            .semantics {
//                                testTagsAsResourceId = true
//                                testTag = "private.cancel"
//                            },
//                    ) {
//                        Text(
//                            text = context.getString(R.string.cfr_neg_button_text),
//                            textAlign = TextAlign.Center,
//                            color = FirefoxTheme.colors.textOnColorPrimary,
//                            style = FirefoxTheme.typography.headline7,
//                        )
//                    }
//                }
//            },
//        ).run {
//            setRecommendPrivateBrowsingCFR(this)
//            show()
//        }
    }
}

private fun dismissRecommendPrivateBrowsingShortcut(
    recommendPrivateBrowsingCFR: CFRPopup?, setRecommendPrivateBrowsingCFR: (CFRPopup?) -> Unit
) {
    recommendPrivateBrowsingCFR?.dismiss()
    setRecommendPrivateBrowsingCFR(null)
}

private fun subscribeToTabCollections(
    context: Context, lifecycleOwner: LifecycleOwner
): Observer<List<TabCollection>> {
    return Observer<List<TabCollection>> {
        context.components.core.tabCollectionStorage.cachedTabCollections = it
        context.components.appStore.dispatch(AppAction.CollectionsChange(it))
    }.also { observer ->
        context.components.core.tabCollectionStorage.getCollections()
            .observe(lifecycleOwner, observer) // this, observer)
    }
}

private fun registerCollectionStorageObserver(
    context: Context, collectionStorageObserver: TabCollectionStorage.Observer
) {
    context.components.core.tabCollectionStorage.register(collectionStorageObserver) // , this)
}

private fun showRenamedSnackbar() {
    // TODO: snackbar
//    view?.let { view ->
//        Snackbar.make(
//            snackBarParentView = binding.dynamicSnackbarContainer,
//            snackbarState = SnackbarState(
//                message = view.context.getString(R.string.snackbar_collection_renamed),
//                duration = SnackbarDuration.Long.toSnackbarStateDuration(),
//            ),
//        ).show()
//    }
}

private fun openTabsTray(navController: NavController, browsingModeManager: BrowsingModeManager) {
    navController.nav(
        R.id.homeFragment,
        HomeFragmentDirections.actionGlobalTabsTrayFragment(
            page = when (browsingModeManager.mode) {
                BrowsingMode.Normal -> Page.NormalTabs
                BrowsingMode.Private -> Page.PrivateTabs
            },
        ),
    )
}

private fun showCollectionsPlaceholder(browserState: BrowserState) {
    // todo: collections
//    val tabCount = if (browsingModeManager.mode.isPrivate) {
//        browserState.privateTabs.size
//    } else {
//        browserState.normalTabs.size
//    }
//
//    // The add_tabs_to_collections_button is added at runtime. We need to search for it in the same way.
//    sessionControlView?.view?.findViewById<MaterialButton>(R.id.add_tabs_to_collections_button)
//        ?.isVisible = tabCount > 0
}

@androidx.annotation.VisibleForTesting
internal fun shouldEnableWallpaper(context: Context) =
    (context.getActivity() as? HomeActivity)?.themeManager?.currentTheme?.isPrivate?.not() ?: false

private fun applyWallpaper(
    wallpaperName: String,
    orientationChange: Boolean,
    orientation: Int,
    context: Context,
    coroutineScope: CoroutineScope,
    lastAppliedWallpaperName: String,
    setLastAppliedWallpaperName: (String) -> Unit
) {
    when {
        !shouldEnableWallpaper(context) || (wallpaperName == lastAppliedWallpaperName && !orientationChange) -> return

        Wallpaper.nameIsDefault(wallpaperName) -> {
            // TODO: wallpaper
//            binding.wallpaperImageView.isVisible = false
            setLastAppliedWallpaperName(wallpaperName)
        }

        else -> {
//            viewLifecycleOwner.lifecycleScope.launch {
            coroutineScope.launch {
                // loadBitmap does file lookups based on name, so we don't need a fully
                // qualified type to load the image
                val wallpaper = Wallpaper.Default.copy(name = wallpaperName)
                val wallpaperImage =
                    context.components.useCases.wallpaperUseCases.loadBitmap(wallpaper, orientation)
                wallpaperImage?.let {
                    // TODO: wallpaper
//                    it.scaleToBottomOfView(binding.wallpaperImageView)
//                    binding.wallpaperImageView.isVisible = true
//                    lastAppliedWallpaperName = wallpaperName
                } ?: run {
                    if (!isActive) return@run
                    // TODO: snackbar
//                    with(binding.wallpaperImageView) {
//                        isVisible = false
//                        showSnackBar(
//                            view = binding.dynamicSnackbarContainer,
//                            text = resources.getString(com.shmibblez.inferno.R.string.wallpaper_select_error_snackbar_message),
//                        )
//                    }
                    // If setting a wallpaper failed reset also the contrasting text color.
                    context.settings().currentWallpaperTextColor = 0L
                    setLastAppliedWallpaperName(Wallpaper.defaultName)
                }
            }
        }
    }
    // Logo color should be updated in all cases.
    applyWallpaperTextColor(context)
}

/**
 * Apply a color better contrasting with the current wallpaper to the Fenix logo and private mode switcher.
 */
@androidx.annotation.VisibleForTesting
internal fun applyWallpaperTextColor(context: Context) {
    val tintColor = when (val color = context.settings().currentWallpaperTextColor.toInt()) {
        0 -> null // a null ColorStateList will clear the current tint
        else -> ColorStateList.valueOf(color)
    }

    // TODO: wallpaper
//    binding.wordmarkText.imageTintList = tintColor
//    binding.privateBrowsingButton.buttonTintList = tintColor
}

//@Composable
private fun observeWallpaperUpdates(context: Context, coroutineScope: CoroutineScope) {
    // TODO: wallpaper
//    consumeFlow(context.components.appStore, coroutineScope /*viewLifecycleOwner*/) { flow ->
//        flow.filter { it.mode == BrowsingMode.Normal }
//            .map { it.wallpaperState.currentWallpaper }
//            .distinctUntilChanged()
//            .collect {
//                if (it.name != lastAppliedWallpaperName) {
//                    applyWallpaper(
//                        wallpaperName = it.name,
//                        orientationChange = false,
//                        orientation = context.resources.configuration.orientation,
//                    )
//                }
//            }
//    }
}

@androidx.annotation.VisibleForTesting
internal fun showSetAsDefaultBrowserPrompt(context: Context) {
    context.components.appStore.dispatch(AppAction.UpdateWasNativeDefaultBrowserPromptShown(true))
    context.getActivity()?.openSetDefaultBrowserOption().also {
//            Metrics.setAsDefaultBrowserNativePromptShown.record()
        context.settings().setAsDefaultPromptCalled()
    }
}