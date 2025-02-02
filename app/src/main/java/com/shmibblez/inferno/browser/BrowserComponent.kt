package com.shmibblez.inferno.browser

//import com.shmibblez.inferno.ext.components
import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.R
import com.shmibblez.inferno.addons.WebExtensionPromptFeature
import com.shmibblez.inferno.browser.browsingmode.BrowsingMode
import com.shmibblez.inferno.browser.browsingmode.BrowsingModeManager
import com.shmibblez.inferno.browser.tabstrip.isTabStripEnabled
import com.shmibblez.inferno.components.Components
import com.shmibblez.inferno.components.TabCollectionStorage
import com.shmibblez.inferno.components.appstate.AppAction
import com.shmibblez.inferno.components.appstate.AppAction.MessagingAction
import com.shmibblez.inferno.components.toolbar.ToolbarIntegration
import com.shmibblez.inferno.components.toolbar.navbar.shouldAddNavigationBar
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.consumeFrom
import com.shmibblez.inferno.ext.containsQueryParameters
import com.shmibblez.inferno.ext.isToolbarAtBottom
import com.shmibblez.inferno.ext.nav
import com.shmibblez.inferno.ext.openSetDefaultBrowserOption
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.ext.tabClosedUndoMessage
import com.shmibblez.inferno.findInPageBar.BrowserFindInPageBar
import com.shmibblez.inferno.home.HomeFragment
import com.shmibblez.inferno.home.HomeFragmentArgs
import com.shmibblez.inferno.home.HomeFragmentDirections
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
import com.shmibblez.inferno.pip.PictureInPictureIntegration
import com.shmibblez.inferno.search.AwesomeBarWrapper
import com.shmibblez.inferno.search.SearchDialogFragment
import com.shmibblez.inferno.search.toolbar.DefaultSearchSelectorController
import com.shmibblez.inferno.search.toolbar.SearchSelectorMenu
import com.shmibblez.inferno.tabbar.BrowserTabBar
import com.shmibblez.inferno.tabbar.toTabList
import com.shmibblez.inferno.tabs.LastTabFeature
import com.shmibblez.inferno.tabs.TabsTrayFragment
import com.shmibblez.inferno.tabstray.Page
import com.shmibblez.inferno.tabstray.TabsTrayAccessPoint
import com.shmibblez.inferno.toolbar.BrowserToolbar
import com.shmibblez.inferno.toolbar.ToolbarBottomMenuSheet
import com.shmibblez.inferno.utils.Settings.Companion.TOP_SITES_PROVIDER_MAX_THRESHOLD
import com.shmibblez.inferno.wallpapers.Wallpaper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mozilla.components.browser.engine.gecko.GeckoEngineView
import mozilla.components.browser.menu.view.MenuButton
import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.selector.findTab
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.browser.thumbnails.BrowserThumbnails
import mozilla.components.compose.cfr.CFRPopup
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.storage.FrecencyThresholdOption
import mozilla.components.concept.sync.AccountObserver
import mozilla.components.concept.sync.AuthType
import mozilla.components.concept.sync.OAuthAccount
import mozilla.components.feature.app.links.AppLinksFeature
import mozilla.components.feature.downloads.DownloadsFeature
import mozilla.components.feature.downloads.temporary.ShareDownloadFeature
import mozilla.components.feature.findinpage.view.FindInPageBar
import mozilla.components.feature.media.fullscreen.MediaSessionFullscreenFeature
import mozilla.components.feature.prompts.PromptFeature
import mozilla.components.feature.readerview.view.ReaderViewControlsBar
import mozilla.components.feature.session.FullScreenFeature
import mozilla.components.feature.session.ScreenOrientationFeature
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.session.SwipeRefreshFeature
import mozilla.components.feature.sitepermissions.SitePermissionsFeature
import mozilla.components.feature.tab.collections.TabCollection
import mozilla.components.feature.tabs.WindowFeature
import mozilla.components.feature.toolbar.WebExtensionToolbarFeature
import mozilla.components.feature.top.sites.TopSite
import mozilla.components.feature.top.sites.TopSitesConfig
import mozilla.components.feature.top.sites.TopSitesFeature
import mozilla.components.feature.top.sites.TopSitesFrecencyConfig
import mozilla.components.feature.top.sites.TopSitesProviderConfig
import mozilla.components.feature.webauthn.WebAuthnFeature
import mozilla.components.lib.state.Store
import mozilla.components.lib.state.ext.observe
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.android.view.enterImmersiveMode
import mozilla.components.support.ktx.android.view.exitImmersiveMode
import mozilla.components.support.utils.BrowsersCache
import mozilla.components.ui.widgets.VerticalSwipeRefreshLayout
import kotlin.math.roundToInt
import mozilla.components.browser.toolbar.BrowserToolbar as BrowserToolbarCompat

// fixme: replaces [HomeFragment]
// todo: implement layout, look at binding / layout

// TODO:
//  - implement composable FindInPageBar
//  - home page
//  - move to selected tab on start
//  - use nicer icons for toolbar options
//  - fix external app browser implementation
//  - improve splash screen
//  - add home page (look at firefox source code)
//  - add default search engines, select default
//    - bundle in app
//    - add search engine settings page
//      - add search engine editor which allows removing or adding search engines
//      - add way to select search engine
//  - toolbar
//    - revisit search engines, how to modify bundled?
//  - change from datastore preferences to datastore
//    - switch from: implementation "androidx.datastore:datastore-preferences:1.1.1"
//      to: implementation "androidx.datastore:datastore:1.1.1"
//  - create Mozilla Location Service (MLS) token and put in components/Core.kt
//  - BuildConfig.MLS_TOKEN
//  - color scheme, search for FirefoxTheme usages

fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

enum class BrowserComponentMode {
    TOOLBAR, FIND_IN_PAGE,
}

enum class BrowserComponentPageType {
    ENGINE, HOME, HOME_PRIVATE
}

object ComponentDimens {
    val TOOLBAR_HEIGHT = 40.dp
    val TAB_BAR_HEIGHT = 30.dp
    val TAB_WIDTH = 95.dp
    val FIND_IN_PAGE_BAR_HEIGHT = 50.dp
    fun BOTTOM_BAR_HEIGHT(browserComponentMode: BrowserComponentMode): Dp {
        return when (browserComponentMode) {
            BrowserComponentMode.TOOLBAR -> TOOLBAR_HEIGHT + TAB_BAR_HEIGHT
            BrowserComponentMode.FIND_IN_PAGE -> FIND_IN_PAGE_BAR_HEIGHT
        }
    }
}

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

/**
 * @param sessionId session id, from Moz BaseBrowserFragment
 */
@OptIn(
    ExperimentalComposeUiApi::class, ExperimentalCoroutinesApi::class
)
@Composable
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
fun BrowserComponent(
    sessionId: String?,
    setOnActivityResultHandler: ((OnActivityResultModel) -> Boolean) -> Unit,
    args: HomeFragmentArgs
) {
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val store = context.components.core.store
    val view = LocalView.current
    val localConfiguration = LocalConfiguration.current
    val parentFragmentManager = context.getActivity()!!.supportFragmentManager

    // browser state observer setup
    val localLifecycleOwner = LocalLifecycleOwner.current
    var browserStateObserver: Store.Subscription<BrowserState, BrowserAction>? by remember {
        mutableStateOf(
            null
        )
    }
    var tabList by remember { mutableStateOf(store.state.toTabList().first) }
    var tabSessionState by remember { mutableStateOf(store.state.selectedTab) }
    var searchEngine by remember { mutableStateOf(store.state.search.selectedOrDefaultSearchEngine!!) }
    val pageType by remember {
        mutableStateOf(with(tabSessionState?.content?.url) {
            if (this == "about:blank") // TODO: create const class and set base to inferno:home
                BrowserComponentPageType.HOME
            else if (this == "private page blank") // TODO: add to const class and set base to inferno:private
                BrowserComponentPageType.HOME_PRIVATE
            else {
                BrowserComponentPageType.ENGINE
            }
            // TODO: if home, show home page and load engineView in compose tree as hidden,
            //  if page then show engineView
        })
    }
    // setup tab observer
    DisposableEffect(true) {
        browserStateObserver = store.observe(localLifecycleOwner) {
            tabList = it.toTabList().first
            tabSessionState = it.selectedTab
            searchEngine = it.search.selectedOrDefaultSearchEngine!!
            Log.d("BrowserComponent", "search engine: ${searchEngine.name}")
        }

        onDispose {
            browserStateObserver!!.unsubscribe()
        }
    }

    // browser display mode
    val (browserMode, setBrowserMode) = remember {
        mutableStateOf(BrowserComponentMode.TOOLBAR)
    }

    // bottom sheet menu setup
    val (showMenuBottomSheet, setShowMenuBottomSheet) = remember { mutableStateOf(false) }
    if (showMenuBottomSheet) {
        ToolbarBottomMenuSheet(
            tabSessionState = tabSessionState,
            setShowBottomMenuSheet = setShowMenuBottomSheet,
            setBrowserComponentMode = setBrowserMode
        )
    }

    /// component features
    val sessionFeature = remember { ViewBoundFeatureWrapper<SessionFeature>() }
    val toolbarIntegration = remember { ViewBoundFeatureWrapper<ToolbarIntegration>() }
    val contextMenuIntegration = remember { ViewBoundFeatureWrapper<ContextMenuIntegration>() }
    val downloadsFeature = remember { ViewBoundFeatureWrapper<DownloadsFeature>() }
    val shareDownloadsFeature = remember { ViewBoundFeatureWrapper<ShareDownloadFeature>() }
    val appLinksFeature = remember { ViewBoundFeatureWrapper<AppLinksFeature>() }
    val promptsFeature = remember { ViewBoundFeatureWrapper<PromptFeature>() }
    val webExtensionPromptFeature =
        remember { ViewBoundFeatureWrapper<WebExtensionPromptFeature>() }
    val fullScreenFeature = remember { ViewBoundFeatureWrapper<FullScreenFeature>() }
    val findInPageIntegration = remember { ViewBoundFeatureWrapper<FindInPageIntegration>() }
    val sitePermissionFeature = remember { ViewBoundFeatureWrapper<SitePermissionsFeature>() }
    val pictureInPictureIntegration =
        remember { ViewBoundFeatureWrapper<PictureInPictureIntegration>() }
    val swipeRefreshFeature = remember { ViewBoundFeatureWrapper<SwipeRefreshFeature>() }
    val windowFeature = remember { ViewBoundFeatureWrapper<WindowFeature>() }
    val webAuthnFeature = remember { ViewBoundFeatureWrapper<WebAuthnFeature>() }
    val fullScreenMediaSessionFeature = remember {
        ViewBoundFeatureWrapper<MediaSessionFullscreenFeature>()
    }
    val lastTabFeature = remember { ViewBoundFeatureWrapper<LastTabFeature>() }
    val screenOrientationFeature = remember { ViewBoundFeatureWrapper<ScreenOrientationFeature>() }
    val thumbnailsFeature = remember { ViewBoundFeatureWrapper<BrowserThumbnails>() }
    val readerViewFeature = remember { ViewBoundFeatureWrapper<ReaderViewIntegration>() }
    val webExtToolbarFeature = remember { ViewBoundFeatureWrapper<WebExtensionToolbarFeature>() }

    /// views
    var engineView: EngineView? by remember { mutableStateOf(null) }
    var toolbar: BrowserToolbarCompat? by remember { mutableStateOf(null) }
    var findInPageBar: FindInPageBar? by remember { mutableStateOf(null) }
    var swipeRefresh: SwipeRefreshLayout? by remember { mutableStateOf(null) }
    var awesomeBar: AwesomeBarWrapper? by remember { mutableStateOf(null) }
    var readerViewBar: ReaderViewControlsBar? by remember { mutableStateOf(null) }
    var readerViewAppearanceButton: FloatingActionButton? by remember { mutableStateOf(null) }

    /// event handlers
    val backButtonHandler: List<ViewBoundFeatureWrapper<*>> = listOf(
        fullScreenFeature,
        findInPageIntegration,
        toolbarIntegration,
        sessionFeature,
        lastTabFeature,
    )
    val activityResultHandler: List<ViewBoundFeatureWrapper<*>> = listOf(
        webAuthnFeature,
        promptsFeature,
    )
    // sets parent fragment handler for onActivityResult
    setOnActivityResultHandler { result: OnActivityResultModel ->
        Logger.info(
            "Fragment onActivityResult received with " + "requestCode: ${result.requestCode}, resultCode: ${result.resultCode}, data: ${result.data}",
        )
        activityResultHandler.any {
            it.onActivityResult(
                result.requestCode, result.data, result.resultCode
            )
        }
    }
//    var webAppToolbarShouldBeVisible = true

    // permission launchers
    val requestDownloadPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val permissions = results.keys.toTypedArray()
            val grantResults = results.values.map {
                if (it) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
            }.toIntArray()
            downloadsFeature.withFeature {
                it.onPermissionsResult(permissions, grantResults)
            }
        }
    val requestSitePermissionsLauncher: ActivityResultLauncher<Array<String>> =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val permissions = results.keys.toTypedArray()
            val grantResults = results.values.map {
                if (it) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
            }.toIntArray()
            sitePermissionFeature.withFeature {
                it.onPermissionsResult(permissions, grantResults)
            }
        }
    val requestPromptsPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val permissions = results.keys.toTypedArray()
            val grantResults = results.values.map {
                if (it) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
            }.toIntArray()
            promptsFeature.withFeature {
                it.onPermissionsResult(permissions, grantResults)
            }
        }

    // connection to the nested scroll system and listen to the scroll
    val bottomBarHeightDp = ComponentDimens.BOTTOM_BAR_HEIGHT(browserMode)
    val bottomBarOffsetPx = remember { Animatable(0F) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset, source: NestedScrollSource
            ): Offset {
                if (tabSessionState?.content?.loading == false) {
                    val delta = available.y
                    val newOffset = (bottomBarOffsetPx.value - delta).coerceIn(
                        0F, bottomBarHeightDp.toPx().toFloat()
                    )
                    coroutineScope.launch {
                        bottomBarOffsetPx.snapTo(newOffset)
                        engineView!!.setDynamicToolbarMaxHeight(bottomBarHeightDp.toPx() - newOffset.toInt())
                    }
                }
                return Offset.Zero
            }
        }
    }


    /* new vars */
    val navController = rememberNavController()
//    private val args by navArgs<HomeFragmentArgs>()

    @androidx.annotation.VisibleForTesting
    // TODO: bundleArgs
    lateinit var bundleArgs: Bundle

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


    // on back pressed handlers
    BackHandler {
        onBackPressed(readerViewFeature, backButtonHandler)
    }

    // moz components setup and shared preferences
    LaunchedEffect(engineView == null) {
        /* HomeFragment onCreate */
        // DO NOT ADD ANYTHING ABOVE THIS getProfilerTime CALL!
        var profilerStartTime = context.components.core.engine.profiler?.getProfilerTime()

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
        )/* HomeFragment onCreate */

        /* onCreateView */
        // DO NOT ADD ANYTHING ABOVE THIS getProfilerTime CALL!
        profilerStartTime = context.components.core.engine.profiler?.getProfilerTime()

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

        val focusOnAddressBar =
            bundleArgs.getBoolean(FOCUS_ON_ADDRESS_BAR) || FxNimbus.features.oneClickSearch.value().enabled

        if (focusOnAddressBar) {
            // If the fragment gets recreated by the activity, the search fragment might get recreated as well. Changing
            // between browsing modes triggers activity recreation, so when changing modes goes together with navigating
            // home, we should avoid navigating to search twice.
            val searchFragmentAlreadyAdded =
                parentFragmentManager.fragments.any { it is SearchDialogFragment }
            if (!searchFragmentAlreadyAdded) {
                sessionControlInteractor!!.onNavigateSearch()
            }
        } else if (bundleArgs.getBoolean(SCROLL_TO_COLLECTION)) {
            // todo: sessionControlView
//            MainScope().launch {
//                delay(ANIM_SCROLL_DELAY)
//                val smoothScroller: SmoothScroller =
//                    object : LinearSmoothScroller(sessionControlView!!.view.context) {
//                        override fun getVerticalSnapPreference(): Int {
//                            return SNAP_TO_START
//                        }
//                    }
//                val recyclerView = sessionControlView!!.view
//                val adapter = recyclerView.adapter!!
//                val collectionPosition = IntRange(0, adapter.itemCount - 1).firstOrNull {
//                    adapter.getItemViewType(it) == CollectionHeaderViewHolder.LAYOUT_ID
//                }
//                collectionPosition?.run {
//                    val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
//                    smoothScroller.targetPosition = this
//                    linearLayoutManager.startSmoothScroll(smoothScroller)
//                }
//            }
        }

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


        if (engineView == null) return@LaunchedEffect
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        /**
         * mozilla integrations setup
         *//*
        fun mozSetup(): Unit {
            sessionFeature.set(
                feature = SessionFeature(
                    context.components.core.store,
                    context.components.useCases.sessionUseCases.goBack,
                    context.components.useCases.sessionUseCases.goForward,
                    engineView!!,
                    sessionId,
                ),
                owner = lifecycleOwner,
                view = view,
            )

//        (toolbar!!.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
//            behavior = EngineViewScrollingBehavior(
//                view.context,
//                null,
//                ViewPosition.BOTTOM,
//            )
//        }

            toolbarIntegration.set(
                feature = ToolbarIntegration(
                    context,
                    toolbar!!,
                    context.components.core.historyStorage,
                    context.components.core.store,
                    context.components.useCases.sessionUseCases,
                    context.components.useCases.tabsUseCases,
                    context.components.useCases.webAppUseCases,
                    sessionId,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            contextMenuIntegration.set(
                feature = ContextMenuIntegration(
                    context,
                    parentFragmentManager,
                    context.components.core.store,
                    context.components.useCases.tabsUseCases,
                    context.components.useCases.contextMenuUseCases,
                    engineView!!,
                    view,
                    sessionId,
                ),
                owner = lifecycleOwner,
                view = view,
            )
            shareDownloadsFeature.set(
                ShareDownloadFeature(
                    context = context.applicationContext,
                    httpClient = context.components.core.client,
                    store = context.components.core.store,
                    tabId = sessionId,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            downloadsFeature.set(
                feature = DownloadsFeature(
                    context,
                    store = context.components.core.store,
                    useCases = context.components.useCases.downloadUseCases,
                    fragmentManager = context.getActivity()?.supportFragmentManager,
                    downloadManager = FetchDownloadManager(
                        context.applicationContext,
                        context.components.core.store,
                        DownloadService::class,
                        notificationsDelegate = context.components.notificationsDelegate,
                    ),
                    onNeedToRequestPermissions = { permissions ->
                        requestDownloadPermissionsLauncher.launch(permissions)
                    },
                ),
                owner = lifecycleOwner,
                view = view,
            )

            appLinksFeature.set(
                feature = AppLinksFeature(
                    context,
                    store = context.components.core.store,
                    sessionId = sessionId,
                    fragmentManager = parentFragmentManager,
                    launchInApp = {
                        prefs.getBoolean(
                            context.getPreferenceKey(R.string.pref_key_launch_external_app), false
                        )
                    },
                ),
                owner = lifecycleOwner,
                view = view,
            )

            promptsFeature.set(
                feature = PromptFeature(
                    fragment = view.findFragment(),
                    store = context.components.core.store,
                    tabsUseCases = context.components.useCases.tabsUseCases,
                    customTabId = sessionId,
                    fileUploadsDirCleaner = context.components.core.fileUploadsDirCleaner,
                    fragmentManager = parentFragmentManager,
                    onNeedToRequestPermissions = { permissions ->
                        requestPromptsPermissionsLauncher.launch(permissions)
                    },
                ),
                owner = lifecycleOwner,
                view = view,
            )

            webExtensionPromptFeature.set(
                feature = WebExtensionPromptFeature(
                    store = context.components.core.store,
                    context = context,
                    fragmentManager = parentFragmentManager,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            windowFeature.set(
                feature = WindowFeature(
                    context.components.core.store, context.components.useCases.tabsUseCases
                ),
                owner = lifecycleOwner,
                view = view,
            )

            fullScreenFeature.set(
                feature = FullScreenFeature(
                    store = context.components.core.store,
                    sessionUseCases = context.components.useCases.sessionUseCases,
                    tabId = sessionId,
                    viewportFitChanged = {
                        viewportFitChanged(
                            viewportFit = it, context
                        )
                    },
                    fullScreenChanged = {
                        fullScreenChanged(
                            it,
                            context,
                            toolbar!!,
                            engineView!!,
                            bottomBarHeightDp.toPx() - bottomBarOffsetPx.value.toInt()

                        )
                    },
                ),
                owner = lifecycleOwner,
                view = view,
            )

            findInPageIntegration.set(
                feature = FindInPageIntegration(
                    context.components.core.store,
                    sessionId,
                    findInPageBar as FindInPageView,
                    engineView!!,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            sitePermissionFeature.set(
                feature = SitePermissionsFeature(
                    context = context,
                    fragmentManager = parentFragmentManager,
                    sessionId = sessionId,
                    storage = context.components.core.geckoSitePermissionsStorage,
                    onNeedToRequestPermissions = { permissions ->
                        requestSitePermissionsLauncher.launch(permissions)
                    },
                    onShouldShowRequestPermissionRationale = {
                        if (context.getActivity() == null) shouldShowRequestPermissionRationale(
                            context.getActivity()!!, it
                        )
                        else false
                    },
                    store = context.components.core.store,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            pictureInPictureIntegration.set(
                feature = PictureInPictureIntegration(
                    context.components.core.store,
                    context.getActivity()!!,
                    sessionId,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            fullScreenMediaSessionFeature.set(
                feature = MediaSessionFullscreenFeature(
                    context.getActivity()!!,
                    context.components.core.store,
                    sessionId,
                ),
                owner = lifecycleOwner,
                view = view,
            )

//        (swipeRefresh!!.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
//            behavior = EngineViewClippingBehavior(
//                context,
//                null,
//                swipeRefresh!!,
//                toolbar!!.height,
//                ToolbarPosition.BOTTOM,
//            )
//        }
            swipeRefreshFeature.set(
                feature = SwipeRefreshFeature(
                    context.components.core.store,
                    context.components.useCases.sessionUseCases.reload,
                    swipeRefresh!!,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            lastTabFeature.set(
                feature = LastTabFeature(
                    context.components.core.store,
                    sessionId,
                    context.components.useCases.tabsUseCases.removeTab,
                    context.getActivity()!!,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            screenOrientationFeature.set(
                feature = ScreenOrientationFeature(
                    context.components.core.engine,
                    context.getActivity()!!,
                ),
                owner = lifecycleOwner,
                view = view,
            )

//        if (BuildConfig.MOZILLA_OFFICIAL) {
            webAuthnFeature.set(
                feature = WebAuthnFeature(
                    context.components.core.engine,
                    context.getActivity()!!,
                    context.components.useCases.sessionUseCases.exitFullscreen::invoke,
                ) { context.components.core.store.state.selectedTabId },
                owner = lifecycleOwner,
                view = view,
            )
//        }

            // from Moz BrowserFragment
            AwesomeBarFeature(awesomeBar!!, toolbar!!, engineView).addSearchProvider(
                context,
                context.components.core.store,
                context.components.useCases.searchUseCases.defaultSearch,
                fetchClient = context.components.core.client,
                mode = SearchSuggestionProvider.Mode.MULTIPLE_SUGGESTIONS,
                engine = context.components.core.engine,
                limit = 5,
                filterExactMatch = true,
            ).addSessionProvider(
                context.resources,
                context.components.core.store,
                context.components.useCases.tabsUseCases.selectTab,
            ).addHistoryProvider(
                context.components.core.historyStorage,
                context.components.useCases.sessionUseCases.loadUrl,
            ).addClipboardProvider(
                context, context.components.useCases.sessionUseCases.loadUrl
            )

            // from Moz BrowserHandler
            // We cannot really add a `addSyncedTabsProvider` to `AwesomeBarFeature` coz that would create
            // a dependency on feature-syncedtabs (which depends on Sync).
            awesomeBar!!.addProviders(
                SyncedTabsStorageSuggestionProvider(
                    context.components.backgroundServices.syncedTabsStorage,
                    context.components.useCases.tabsUseCases.addTab,
                    context.components.core.icons,
                ),
            )

            // from Moz BrowserHandler
            TabsToolbarFeature(
                toolbar = toolbar!!,
                sessionId = sessionId,
                store = context.components.core.store,
                showTabs = { showTabs(context) },
                lifecycleOwner = lifecycleOwner,
            )

            // from Moz BrowserHandler
            thumbnailsFeature.set(
                feature = BrowserThumbnails(
                    context,
                    engineView!!,
                    context.components.core.store,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            // from Moz BrowserHandler
            readerViewFeature.set(
                feature = ReaderViewIntegration(
                    context,
                    context.components.core.engine,
                    context.components.core.store,
                    toolbar!!,
                    readerViewBar!!,
                    readerViewAppearanceButton!!,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            // from Moz BrowserHandler
            webExtToolbarFeature.set(
                feature = WebExtensionToolbarFeature(
                    toolbar!!,
                    context.components.core.store,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            // from Moz BrowserHandler
            windowFeature.set(
                feature = WindowFeature(
                    store = context.components.core.store,
                    tabsUseCases = context.components.useCases.tabsUseCases,
                ),
                owner = lifecycleOwner,
                view = view,
            )

//            if (context.settings().showTopSitesFeature) {
//            topSitesFeature.set(
//                feature = TopSitesFeature(
//                    view = DefaultTopSitesView(
//                        appStore = context.components.appStore,
//                        settings = context.components.settings,
//                    ), storage = context.components.core.topSitesStorage,
//                    config = {getTopSitesConfig(context)},
//                ),
//                owner = viewLifecycleOwner,
//                view =view // binding.root,
//            )
//            }
        }
        mozSetup() */
        engineView!!.setDynamicToolbarMaxHeight(bottomBarHeightDp.toPx() - bottomBarOffsetPx.value.toInt())
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
//                    super.onStart()

                    subscribeToTabCollections(context, lifecycleOwner)

//                    val context = context

                    context.components.backgroundServices.accountManagerAvailableQueue.runIfReadyOrQueue {
                        // By the time this code runs, we may not be attached to a context or have a view lifecycle owner.
//                        if ((this@HomeFragment).view?.context == null) {
                        if (view == null) {
                            return@runIfReadyOrQueue
                        }

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

                    if (browsingModeManager.mode.isPrivate &&
                        // We will be showing the search dialog and don't want to show the CFR while the dialog shows
                        !bundleArgs.getBoolean(HomeFragment.FOCUS_ON_ADDRESS_BAR) && context.settings().shouldShowPrivateModeCfr
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

                Lifecycle.Event.ON_DESTROY -> {
                    // TODO: onDestroy
//                    super.onDestroyView()
                    sessionControlInteractor = null
//                    sessionControlView = null
//                    toolbarView = null
//                    _bottomToolbarContainerView = null
//                    _binding = null

                    bundleArgs.clear()
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
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        content = { paddingValues ->
            MozAwesomeBar(setView = { ab -> awesomeBar = ab })
            MozEngineView(
                modifier = Modifier
                    .padding(
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        top = 0.dp,
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = 0.dp
                    )
                    .nestedScroll(nestedScrollConnection)
                    .motionEventSpy {
                        if (it.action == MotionEvent.ACTION_UP || it.action == MotionEvent.ACTION_CANCEL) {
                            // set bottom bar position
                            coroutineScope.launch {
                                if (bottomBarOffsetPx.value <= (bottomBarHeightDp.toPx() / 2)) {
                                    // if more than halfway up, go up
                                    bottomBarOffsetPx.animateTo(0F)
                                } else {
                                    // if more than halfway down, go down
                                    bottomBarOffsetPx.animateTo(
                                        bottomBarHeightDp
                                            .toPx()
                                            .toFloat()
                                    )
                                }
                                engineView!!.setDynamicToolbarMaxHeight(0)
                            }
                        }
//                        else if (it.action == MotionEvent.ACTION_SCROLL) {
//                            // TODO: move nested scroll connection logic here
//                        }
                    },
                setEngineView = { ev -> engineView = ev },
                setSwipeView = { sr -> swipeRefresh = sr },
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            MozFloatingActionButton { fab -> readerViewAppearanceButton = fab }
        },
        bottomBar = {
            // hide and show when scrolling
            BottomAppBar(contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .height(bottomBarHeightDp)
                    .offset {
                        IntOffset(
                            x = 0, y = bottomBarOffsetPx.value.roundToInt()
                        )
                    }) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    if (browserMode == BrowserComponentMode.TOOLBAR) {
                        BrowserTabBar(tabList)
                        BrowserToolbar(
                            tabSessionState = tabSessionState,
                            searchEngine = searchEngine,
                            setShowMenu = setShowMenuBottomSheet
                        )
                    }
                    if (browserMode == BrowserComponentMode.FIND_IN_PAGE) {
                        BrowserFindInPageBar()
                    }
                    MozFindInPageBar { fip -> findInPageBar = fip }
                    MozBrowserToolbar { bt -> toolbar = bt }
                    MozReaderViewControlsBar { cb -> readerViewBar = cb }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(Color.Magenta)
                    )
                }
            }
        },
    )
}

private fun viewportFitChanged(viewportFit: Int, context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        context.getActivity()!!.window.attributes.layoutInDisplayCutoutMode = viewportFit
    }
}

private fun fullScreenChanged(
    enabled: Boolean,
    context: Context,
    toolbar: BrowserToolbarCompat,
    engineView: EngineView,
    bottomBarTotalHeight: Int
) {
    if (enabled) {
        context.getActivity()?.enterImmersiveMode()
        toolbar.visibility = View.GONE
        engineView.setDynamicToolbarMaxHeight(0)
    } else {
        context.getActivity()?.exitImmersiveMode()
        toolbar.visibility = View.VISIBLE
        engineView.setDynamicToolbarMaxHeight(bottomBarTotalHeight)
    }
}

// from Moz BrowserHandler
private fun showTabs(context: Context) {
    // For now we are performing manual fragment transactions here. Once we can use the new
    // navigation support library we may want to pass navigation graphs around.
    // TODO: use navigation instead of fragment transactions
    context.getActivity()?.supportFragmentManager?.beginTransaction()?.apply {
        replace(R.id.container, TabsTrayFragment())
        commit()
    }
}

// combines Moz BrowserFragment and Moz BaseBrowserFragment implementations
private fun onBackPressed(
    readerViewFeature: ViewBoundFeatureWrapper<ReaderViewIntegration>,
    backButtonHandler: List<ViewBoundFeatureWrapper<*>>
): Boolean {
    return readerViewFeature.onBackPressed() || backButtonHandler.any { it.onBackPressed() }
}

fun Dp.toPx(): Int {
    return (this.value * Resources.getSystem().displayMetrics.density).toInt()
}

@Composable
fun MozAwesomeBar(setView: (AwesomeBarWrapper) -> Unit) {
    AndroidView(modifier = Modifier
        .height(0.dp)
        .width(0.dp),
        factory = { context -> AwesomeBarWrapper(context) },
        update = {
            it.visibility = View.GONE
            it.layoutParams.width = LayoutParams.MATCH_PARENT
            it.layoutParams.height = LayoutParams.MATCH_PARENT
            it.setPadding(4.dp.toPx(), 4.dp.toPx(), 4.dp.toPx(), 4.dp.toPx())
            setView(it)
        })
}

@Composable
fun MozEngineView(
    modifier: Modifier,
    setSwipeView: (VerticalSwipeRefreshLayout) -> Unit,
    setEngineView: (GeckoEngineView) -> Unit
) {
    AndroidView(modifier = modifier.fillMaxSize(), factory = { context ->
        val vl = VerticalSwipeRefreshLayout(context)
        val gv = GeckoEngineView(context)
        vl.addView(gv)
        vl
    }, update = { sv ->
        var gv: GeckoEngineView? = null
        // find GeckoEngineView child in scroll view
        for (v in sv.children) {
            if (v is GeckoEngineView) {
                gv = v
                break
            }
        }
        // setup views
        with(sv.layoutParams) {
            this.width = LayoutParams.MATCH_PARENT
            this.height = LayoutParams.MATCH_PARENT
        }
        with(gv!!.layoutParams) {
            this.width = LayoutParams.MATCH_PARENT
            this.height = LayoutParams.MATCH_PARENT
        }
        // set view references
        setSwipeView(sv)
        setEngineView(gv)
    })
}

@Composable
fun MozBrowserToolbar(setView: (BrowserToolbarCompat) -> Unit) {
    AndroidView(modifier = Modifier
        .fillMaxWidth()
        .height(dimensionResource(id = R.dimen.browser_toolbar_height))
        .background(Color.Black)
        .padding(horizontal = 8.dp, vertical = 0.dp),
        factory = { context -> BrowserToolbarCompat(context) },
        update = { bt ->
            bt.layoutParams.height = R.dimen.browser_toolbar_height
            bt.layoutParams.width = LayoutParams.MATCH_PARENT
            bt.visibility = View.VISIBLE
            bt.setBackgroundColor(0xFF0000)
            bt.displayMode()
            setView(bt)
        })
}

/**
 * @param setView function to set view variable in parent
 */
@Composable
fun MozFindInPageBar(setView: (FindInPageBar) -> Unit) {
    AndroidView(modifier = Modifier
        .fillMaxSize()
        .height(0.dp)
        .width(0.dp),
        factory = { context -> FindInPageBar(context) },
        update = {
            setView(it)
            it.visibility = View.GONE
        })
}

@Composable
fun MozReaderViewControlsBar(
    setView: (ReaderViewControlsBar) -> Unit
) {
    AndroidView(modifier = Modifier
        .fillMaxSize()
        .background(colorResource(id = R.color.toolbarBackgroundColor))
        .height(0.dp)
        .width(0.dp),
        factory = { context -> ReaderViewControlsBar(context) },
        update = {
            setView(it)
            it.visibility = View.GONE
        })
}

// reader view button, what this for?
@Composable
fun MozFloatingActionButton(
    setView: (FloatingActionButton) -> Unit
) {
    AndroidView(modifier = Modifier.fillMaxSize(),
        factory = { context -> FloatingActionButton(context) },
        update = {
            setView(it)
            it.visibility = View.GONE
        })
}

/* new functions */

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

// old code jic
//fun Context.getActivity(): AppCompatActivity? = when (this) {
//    is AppCompatActivity -> this
//    is ContextWrapper -> baseContext.getActivity()
//    else -> null
//}
//
//enum class BrowserComponentMode {
//    TOOLBAR, FIND_IN_PAGE,
//}
//
//enum class BrowserComponentPageType {
//    ENGINE, HOME, HOME_PRIVATE
//}
//
//object ComponentDimens {
//    val TOOLBAR_HEIGHT = 40.dp
//    val TAB_BAR_HEIGHT = 30.dp
//    val TAB_WIDTH = 95.dp
//    val FIND_IN_PAGE_BAR_HEIGHT = 50.dp
//    fun BOTTOM_BAR_HEIGHT(browserComponentMode: BrowserComponentMode): Dp {
//        return when (browserComponentMode) {
//            BrowserComponentMode.TOOLBAR -> TOOLBAR_HEIGHT + TAB_BAR_HEIGHT
//            BrowserComponentMode.FIND_IN_PAGE -> FIND_IN_PAGE_BAR_HEIGHT
//        }
//    }
//}
//
///**
// * @param sessionId session id, from Moz BaseBrowserFragment
// */
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
//@Composable
//@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
//fun BrowserComponent(
//    sessionId: String?, setOnActivityResultHandler: ((OnActivityResultModel) -> Boolean) -> Unit
//) {
//    val coroutineScope = rememberCoroutineScope()
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val context = LocalContext.current
//    val view = LocalView.current
//    val parentFragmentManager = context.getActivity()!!.supportFragmentManager
//
//    // browser state observer setup
//    val localLifecycleOwner = LocalLifecycleOwner.current
//    var browserStateObserver: Store.Subscription<BrowserState, BrowserAction>? by remember {
//        mutableStateOf(
//            null
//        )
//    }
//    var tabList by remember { mutableStateOf(context.components.core.store.state.toTabList().first) }
//    var tabSessionState by remember { mutableStateOf(context.components.core.store.state.selectedTab) }
//    var searchEngine by remember { mutableStateOf(context.components.core.store.state.search.selectedOrDefaultSearchEngine!!) }
//    val pageType by remember {
//        mutableStateOf(with(tabSessionState?.content?.url) {
//            if (this == "about:blank") // TODO: create const class and set base to inferno:home
//                BrowserComponentPageType.HOME
//            else if (this == "private page blank") // TODO: add to const class and set base to inferno:private
//                BrowserComponentPageType.HOME_PRIVATE
//            else {
//                BrowserComponentPageType.ENGINE
//            }
//            // TODO: if home, show home page and load engineView in compose tree as hidden,
//            //  if page then show engineView
//        })
//    }
//    // setup tab observer
//    DisposableEffect(true) {
//        browserStateObserver = context.components.core.store.observe(localLifecycleOwner) {
//            tabList = it.toTabList().first
//            tabSessionState = it.selectedTab
//            searchEngine = it.search.selectedOrDefaultSearchEngine!!
//            Log.d("BrowserComponent", "search engine: ${searchEngine.name}")
//        }
//
//        onDispose {
//            browserStateObserver!!.unsubscribe()
//        }
//    }
//
//    // browser display mode
//    val (browserMode, setBrowserMode) = remember {
//        mutableStateOf(BrowserComponentMode.TOOLBAR)
//    }
//
//    // bottom sheet menu setup
//    val (showMenuBottomSheet, setShowMenuBottomSheet) = remember { mutableStateOf(false) }
//    if (showMenuBottomSheet) {
//        ToolbarBottomMenuSheet(
//            tabSessionState = tabSessionState,
//            setShowBottomMenuSheet = setShowMenuBottomSheet,
//            setBrowserComponentMode = setBrowserMode
//        )
//    }
//
//    /// component features
//    val sessionFeature = remember { ViewBoundFeatureWrapper<SessionFeature>() }
//    val toolbarIntegration = remember { ViewBoundFeatureWrapper<ToolbarIntegration>() }
//    val contextMenuIntegration = remember { ViewBoundFeatureWrapper<ContextMenuIntegration>() }
//    val downloadsFeature = remember { ViewBoundFeatureWrapper<DownloadsFeature>() }
//    val shareDownloadsFeature = remember { ViewBoundFeatureWrapper<ShareDownloadFeature>() }
//    val appLinksFeature = remember { ViewBoundFeatureWrapper<AppLinksFeature>() }
//    val promptsFeature = remember { ViewBoundFeatureWrapper<PromptFeature>() }
//    val webExtensionPromptFeature =
//        remember { ViewBoundFeatureWrapper<WebExtensionPromptFeature>() }
//    val fullScreenFeature = remember { ViewBoundFeatureWrapper<FullScreenFeature>() }
//    val findInPageIntegration = remember { ViewBoundFeatureWrapper<FindInPageIntegration>() }
//    val sitePermissionFeature = remember { ViewBoundFeatureWrapper<SitePermissionsFeature>() }
//    val pictureInPictureIntegration =
//        remember { ViewBoundFeatureWrapper<PictureInPictureIntegration>() }
//    val swipeRefreshFeature = remember { ViewBoundFeatureWrapper<SwipeRefreshFeature>() }
//    val windowFeature = remember { ViewBoundFeatureWrapper<WindowFeature>() }
//    val webAuthnFeature = remember { ViewBoundFeatureWrapper<WebAuthnFeature>() }
//    val fullScreenMediaSessionFeature = remember {
//        ViewBoundFeatureWrapper<MediaSessionFullscreenFeature>()
//    }
//    val lastTabFeature = remember { ViewBoundFeatureWrapper<LastTabFeature>() }
//    val screenOrientationFeature = remember { ViewBoundFeatureWrapper<ScreenOrientationFeature>() }
//    val thumbnailsFeature = remember { ViewBoundFeatureWrapper<BrowserThumbnails>() }
//    val readerViewFeature = remember { ViewBoundFeatureWrapper<ReaderViewIntegration>() }
//    val webExtToolbarFeature = remember { ViewBoundFeatureWrapper<WebExtensionToolbarFeature>() }
//    val topSitesFeature = remember { ViewBoundFeatureWrapper<TopSitesFeature>() }
//
//    /// views
//    var engineView: EngineView? by remember { mutableStateOf(null) }
//    var toolbar: BrowserToolbarCompat? by remember { mutableStateOf(null) }
//    var findInPageBar: FindInPageBar? by remember { mutableStateOf(null) }
//    var swipeRefresh: SwipeRefreshLayout? by remember { mutableStateOf(null) }
//    var awesomeBar: AwesomeBarWrapper? by remember { mutableStateOf(null) }
//    var readerViewBar: ReaderViewControlsBar? by remember { mutableStateOf(null) }
//    var readerViewAppearanceButton: FloatingActionButton? by remember { mutableStateOf(null) }
//
//    /// event handlers
//    val backButtonHandler: List<ViewBoundFeatureWrapper<*>> = listOf(
//        fullScreenFeature,
//        findInPageIntegration,
//        toolbarIntegration,
//        sessionFeature,
//        lastTabFeature,
//    )
//    val activityResultHandler: List<ViewBoundFeatureWrapper<*>> = listOf(
//        webAuthnFeature,
//        promptsFeature,
//    )
//    // sets parent fragment handler for onActivityResult
//    setOnActivityResultHandler { result: OnActivityResultModel ->
//        Logger.info(
//            "Fragment onActivityResult received with " + "requestCode: ${result.requestCode}, resultCode: ${result.resultCode}, data: ${result.data}",
//        )
//        activityResultHandler.any {
//            it.onActivityResult(
//                result.requestCode, result.data, result.resultCode
//            )
//        }
//    }
////    var webAppToolbarShouldBeVisible = true
//
//    // permission launchers
//    val requestDownloadPermissionsLauncher: ActivityResultLauncher<Array<String>> =
//        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) { results ->
//            val permissions = results.keys.toTypedArray()
//            val grantResults = results.values.map {
//                if (it) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
//            }.toIntArray()
//            downloadsFeature.withFeature {
//                it.onPermissionsResult(permissions, grantResults)
//            }
//        }
//    val requestSitePermissionsLauncher: ActivityResultLauncher<Array<String>> =
//        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
//            val permissions = results.keys.toTypedArray()
//            val grantResults = results.values.map {
//                if (it) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
//            }.toIntArray()
//            sitePermissionFeature.withFeature {
//                it.onPermissionsResult(permissions, grantResults)
//            }
//        }
//    val requestPromptsPermissionsLauncher: ActivityResultLauncher<Array<String>> =
//        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
//            val permissions = results.keys.toTypedArray()
//            val grantResults = results.values.map {
//                if (it) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
//            }.toIntArray()
//            promptsFeature.withFeature {
//                it.onPermissionsResult(permissions, grantResults)
//            }
//        }
//
//    // connection to the nested scroll system and listen to the scroll
//    val bottomBarHeightDp = ComponentDimens.BOTTOM_BAR_HEIGHT(browserMode)
//    val bottomBarOffsetPx = remember { Animatable(0F) }
//    val nestedScrollConnection = remember {
//        object : NestedScrollConnection {
//            override fun onPreScroll(
//                available: Offset, source: NestedScrollSource
//            ): Offset {
//                if (tabSessionState?.content?.loading == false) {
//                    val delta = available.y
//                    val newOffset = (bottomBarOffsetPx.value - delta).coerceIn(
//                        0F, bottomBarHeightDp.toPx().toFloat()
//                    )
//                    coroutineScope.launch {
//                        bottomBarOffsetPx.snapTo(newOffset)
//                        engineView!!.setDynamicToolbarMaxHeight(bottomBarHeightDp.toPx() - newOffset.toInt())
//                    }
//                }
//                return Offset.Zero
//            }
//        }
//    }
//
//    // on back pressed handlers
//    BackHandler {
//        onBackPressed(readerViewFeature, backButtonHandler)
//    }
//
//    // moz components setup and shared preferences
//    LaunchedEffect(engineView == null) {
//        if (engineView == null) return@LaunchedEffect
//        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
//
//        /**
//         * mozilla integrations setup
//         */
//        /*
//        fun mozSetup(): Unit {
//            sessionFeature.set(
//                feature = SessionFeature(
//                    context.components.core.store,
//                    context.components.useCases.sessionUseCases.goBack,
//                    context.components.useCases.sessionUseCases.goForward,
//                    engineView!!,
//                    sessionId,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
////        (toolbar!!.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
////            behavior = EngineViewScrollingBehavior(
////                view.context,
////                null,
////                ViewPosition.BOTTOM,
////            )
////        }
//
//            toolbarIntegration.set(
//                feature = ToolbarIntegration(
//                    context,
//                    toolbar!!,
//                    context.components.core.historyStorage,
//                    context.components.core.store,
//                    context.components.useCases.sessionUseCases,
//                    context.components.useCases.tabsUseCases,
//                    context.components.useCases.webAppUseCases,
//                    sessionId,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
//            contextMenuIntegration.set(
//                feature = ContextMenuIntegration(
//                    context,
//                    parentFragmentManager,
//                    context.components.core.store,
//                    context.components.useCases.tabsUseCases,
//                    context.components.useCases.contextMenuUseCases,
//                    engineView!!,
//                    view,
//                    sessionId,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//            shareDownloadsFeature.set(
//                ShareDownloadFeature(
//                    context = context.applicationContext,
//                    httpClient = context.components.core.client,
//                    store = context.components.core.store,
//                    tabId = sessionId,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
//            downloadsFeature.set(
//                feature = DownloadsFeature(
//                    context,
//                    store = context.components.core.store,
//                    useCases = context.components.useCases.downloadUseCases,
//                    fragmentManager = context.getActivity()?.supportFragmentManager,
//                    downloadManager = FetchDownloadManager(
//                        context.applicationContext,
//                        context.components.core.store,
//                        DownloadService::class,
//                        notificationsDelegate = context.components.notificationsDelegate,
//                    ),
//                    onNeedToRequestPermissions = { permissions ->
//                        requestDownloadPermissionsLauncher.launch(permissions)
//                    },
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
//            appLinksFeature.set(
//                feature = AppLinksFeature(
//                    context,
//                    store = context.components.core.store,
//                    sessionId = sessionId,
//                    fragmentManager = parentFragmentManager,
//                    launchInApp = {
//                        prefs.getBoolean(
//                            context.getPreferenceKey(R.string.pref_key_launch_external_app), false
//                        )
//                    },
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
//            promptsFeature.set(
//                feature = PromptFeature(
//                    fragment = view.findFragment(),
//                    store = context.components.core.store,
//                    tabsUseCases = context.components.useCases.tabsUseCases,
//                    customTabId = sessionId,
//                    fileUploadsDirCleaner = context.components.core.fileUploadsDirCleaner,
//                    fragmentManager = parentFragmentManager,
//                    onNeedToRequestPermissions = { permissions ->
//                        requestPromptsPermissionsLauncher.launch(permissions)
//                    },
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
//            webExtensionPromptFeature.set(
//                feature = WebExtensionPromptFeature(
//                    store = context.components.core.store,
//                    context = context,
//                    fragmentManager = parentFragmentManager,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
//            windowFeature.set(
//                feature = WindowFeature(
//                    context.components.core.store, context.components.useCases.tabsUseCases
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
//            fullScreenFeature.set(
//                feature = FullScreenFeature(
//                    store = context.components.core.store,
//                    sessionUseCases = context.components.useCases.sessionUseCases,
//                    tabId = sessionId,
//                    viewportFitChanged = {
//                        viewportFitChanged(
//                            viewportFit = it, context
//                        )
//                    },
//                    fullScreenChanged = {
//                        fullScreenChanged(
//                            it,
//                            context,
//                            toolbar!!,
//                            engineView!!,
//                            bottomBarHeightDp.toPx() - bottomBarOffsetPx.value.toInt()
//
//                        )
//                    },
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
//            findInPageIntegration.set(
//                feature = FindInPageIntegration(
//                    context.components.core.store,
//                    sessionId,
//                    findInPageBar as FindInPageView,
//                    engineView!!,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
//            sitePermissionFeature.set(
//                feature = SitePermissionsFeature(
//                    context = context,
//                    fragmentManager = parentFragmentManager,
//                    sessionId = sessionId,
//                    storage = context.components.core.geckoSitePermissionsStorage,
//                    onNeedToRequestPermissions = { permissions ->
//                        requestSitePermissionsLauncher.launch(permissions)
//                    },
//                    onShouldShowRequestPermissionRationale = {
//                        if (context.getActivity() == null) shouldShowRequestPermissionRationale(
//                            context.getActivity()!!, it
//                        )
//                        else false
//                    },
//                    store = context.components.core.store,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
//            pictureInPictureIntegration.set(
//                feature = PictureInPictureIntegration(
//                    context.components.core.store,
//                    context.getActivity()!!,
//                    sessionId,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
//            fullScreenMediaSessionFeature.set(
//                feature = MediaSessionFullscreenFeature(
//                    context.getActivity()!!,
//                    context.components.core.store,
//                    sessionId,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
////        (swipeRefresh!!.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
////            behavior = EngineViewClippingBehavior(
////                context,
////                null,
////                swipeRefresh!!,
////                toolbar!!.height,
////                ToolbarPosition.BOTTOM,
////            )
////        }
//            swipeRefreshFeature.set(
//                feature = SwipeRefreshFeature(
//                    context.components.core.store,
//                    context.components.useCases.sessionUseCases.reload,
//                    swipeRefresh!!,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
//            lastTabFeature.set(
//                feature = LastTabFeature(
//                    context.components.core.store,
//                    sessionId,
//                    context.components.useCases.tabsUseCases.removeTab,
//                    context.getActivity()!!,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
//            screenOrientationFeature.set(
//                feature = ScreenOrientationFeature(
//                    context.components.core.engine,
//                    context.getActivity()!!,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
////        if (BuildConfig.MOZILLA_OFFICIAL) {
//            webAuthnFeature.set(
//                feature = WebAuthnFeature(
//                    context.components.core.engine,
//                    context.getActivity()!!,
//                    context.components.useCases.sessionUseCases.exitFullscreen::invoke,
//                ) { context.components.core.store.state.selectedTabId },
//                owner = lifecycleOwner,
//                view = view,
//            )
////        }
//
//            // from Moz BrowserFragment
//            AwesomeBarFeature(awesomeBar!!, toolbar!!, engineView).addSearchProvider(
//                context,
//                context.components.core.store,
//                context.components.useCases.searchUseCases.defaultSearch,
//                fetchClient = context.components.core.client,
//                mode = SearchSuggestionProvider.Mode.MULTIPLE_SUGGESTIONS,
//                engine = context.components.core.engine,
//                limit = 5,
//                filterExactMatch = true,
//            ).addSessionProvider(
//                context.resources,
//                context.components.core.store,
//                context.components.useCases.tabsUseCases.selectTab,
//            ).addHistoryProvider(
//                context.components.core.historyStorage,
//                context.components.useCases.sessionUseCases.loadUrl,
//            ).addClipboardProvider(
//                context, context.components.useCases.sessionUseCases.loadUrl
//            )
//
//            // from Moz BrowserHandler
//            // We cannot really add a `addSyncedTabsProvider` to `AwesomeBarFeature` coz that would create
//            // a dependency on feature-syncedtabs (which depends on Sync).
//            awesomeBar!!.addProviders(
//                SyncedTabsStorageSuggestionProvider(
//                    context.components.backgroundServices.syncedTabsStorage,
//                    context.components.useCases.tabsUseCases.addTab,
//                    context.components.core.icons,
//                ),
//            )
//
//            // from Moz BrowserHandler
//            TabsToolbarFeature(
//                toolbar = toolbar!!,
//                sessionId = sessionId,
//                store = context.components.core.store,
//                showTabs = { showTabs(context) },
//                lifecycleOwner = lifecycleOwner,
//            )
//
//            // from Moz BrowserHandler
//            thumbnailsFeature.set(
//                feature = BrowserThumbnails(
//                    context,
//                    engineView!!,
//                    context.components.core.store,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
//            // from Moz BrowserHandler
//            readerViewFeature.set(
//                feature = ReaderViewIntegration(
//                    context,
//                    context.components.core.engine,
//                    context.components.core.store,
//                    toolbar!!,
//                    readerViewBar!!,
//                    readerViewAppearanceButton!!,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
//            // from Moz BrowserHandler
//            webExtToolbarFeature.set(
//                feature = WebExtensionToolbarFeature(
//                    toolbar!!,
//                    context.components.core.store,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
//            // from Moz BrowserHandler
//            windowFeature.set(
//                feature = WindowFeature(
//                    store = context.components.core.store,
//                    tabsUseCases = context.components.useCases.tabsUseCases,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//
////            if (context.settings().showTopSitesFeature) {
////            topSitesFeature.set(
////                feature = TopSitesFeature(
////                    view = DefaultTopSitesView(
////                        appStore = context.components.appStore,
////                        settings = context.components.settings,
////                    ), storage = context.components.core.topSitesStorage,
////                    config = {getTopSitesConfig(context)},
////                ),
////                owner = viewLifecycleOwner,
////                view =view // binding.root,
////            )
////            }
//        }
//        mozSetup() */
//        engineView!!.setDynamicToolbarMaxHeight(bottomBarHeightDp.toPx() - bottomBarOffsetPx.value.toInt())
//    }
//
//    Scaffold(contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
//        content = { paddingValues ->
//            MozAwesomeBar(setView = { ab -> awesomeBar = ab })
//            MozEngineView(
//                modifier = Modifier
//                    .padding(
//                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
//                        top = 0.dp,
//                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
//                        bottom = 0.dp
//                    )
//                    .nestedScroll(nestedScrollConnection)
//                    .motionEventSpy {
//                        if (it.action == MotionEvent.ACTION_UP || it.action == MotionEvent.ACTION_CANCEL) {
//                            // set bottom bar position
//                            coroutineScope.launch {
//                                if (bottomBarOffsetPx.value <= (bottomBarHeightDp.toPx() / 2)) {
//                                    // if more than halfway up, go up
//                                    bottomBarOffsetPx.animateTo(0F)
//                                } else {
//                                    // if more than halfway down, go down
//                                    bottomBarOffsetPx.animateTo(
//                                        bottomBarHeightDp
//                                            .toPx()
//                                            .toFloat()
//                                    )
//                                }
//                                engineView!!.setDynamicToolbarMaxHeight(0)
//                            }
//                        }
////                        else if (it.action == MotionEvent.ACTION_SCROLL) {
////                            // TODO: move nested scroll connection logic here
////                        }
//                    },
//                setEngineView = { ev -> engineView = ev },
//                setSwipeView = { sr -> swipeRefresh = sr },
//            )
//        },
//        floatingActionButtonPosition = FabPosition.End,
//        floatingActionButton = {
//            MozFloatingActionButton { fab -> readerViewAppearanceButton = fab }
//        },
//        bottomBar = {
//            // hide and show when scrolling
//            BottomAppBar(contentPadding = PaddingValues(0.dp),
//                modifier = Modifier
//                    .height(bottomBarHeightDp)
//                    .offset {
//                        IntOffset(
//                            x = 0, y = bottomBarOffsetPx.value.roundToInt()
//                        )
//                    }) {
//                Column(
//                    Modifier
//                        .fillMaxSize()
//                        .background(Color.Black)
//                ) {
//                    if (browserMode == BrowserComponentMode.TOOLBAR) {
//                        BrowserTabBar(tabList)
//                        BrowserToolbar(
//                            tabSessionState = tabSessionState,
//                            searchEngine = searchEngine,
//                            setShowMenu = setShowMenuBottomSheet
//                        )
//                    }
//                    if (browserMode == BrowserComponentMode.FIND_IN_PAGE) {
//                        BrowserFindInPageBar()
//                    }
//                    MozFindInPageBar { fip -> findInPageBar = fip }
//                    MozBrowserToolbar { bt -> toolbar = bt }
//                    MozReaderViewControlsBar { cb -> readerViewBar = cb }
//                    Box(
//                        Modifier
//                            .fillMaxWidth()
//                            .height(10.dp)
//                            .background(Color.Magenta)
//                    )
//                }
//            }
//        })
//}
//
//private fun viewportFitChanged(viewportFit: Int, context: Context) {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//        context.getActivity()!!.window.attributes.layoutInDisplayCutoutMode = viewportFit
//    }
//}
//
//private fun fullScreenChanged(
//    enabled: Boolean,
//    context: Context,
//    toolbar: BrowserToolbarCompat,
//    engineView: EngineView,
//    bottomBarTotalHeight: Int
//) {
//    if (enabled) {
//        context.getActivity()?.enterImmersiveMode()
//        toolbar.visibility = View.GONE
//        engineView.setDynamicToolbarMaxHeight(0)
//    } else {
//        context.getActivity()?.exitImmersiveMode()
//        toolbar.visibility = View.VISIBLE
//        engineView.setDynamicToolbarMaxHeight(bottomBarTotalHeight)
//    }
//}
//
//// from Moz BrowserHandler
//private fun showTabs(context: Context) {
//    // For now we are performing manual fragment transactions here. Once we can use the new
//    // navigation support library we may want to pass navigation graphs around.
//    // TODO: use navigation instead of fragment transactions
//    context.getActivity()?.supportFragmentManager?.beginTransaction()?.apply {
//        replace(R.id.container, TabsTrayFragment())
//        commit()
//    }
//}
//
//// combines Moz BrowserFragment and Moz BaseBrowserFragment implementations
//private fun onBackPressed(
//    readerViewFeature: ViewBoundFeatureWrapper<ReaderViewIntegration>,
//    backButtonHandler: List<ViewBoundFeatureWrapper<*>>
//): Boolean {
//    return readerViewFeature.onBackPressed() || backButtonHandler.any { it.onBackPressed() }
//}
//
//fun Dp.toPx(): Int {
//    return (this.value * Resources.getSystem().displayMetrics.density).toInt()
//}
//
//
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
//            showProviderTopSites = settings.showContileFeature,
//            maxThreshold = TOP_SITES_PROVIDER_MAX_THRESHOLD,
//            providerFilter = { topSite ->
//                when (context.components.core.store.state.search.selectedOrDefaultSearchEngine?.name) {
//                    AMAZON_SEARCH_ENGINE_NAME -> topSite.title != AMAZON_SPONSORED_TITLE
//                    EBAY_SPONSORED_TITLE -> topSite.title != EBAY_SPONSORED_TITLE
//                    else -> true
//                }
//            },
//        ),
//    )
//}
//
//@Composable
//fun MozAwesomeBar(setView: (AwesomeBarWrapper) -> Unit) {
//    AndroidView(modifier = Modifier
//        .height(0.dp)
//        .width(0.dp),
//        factory = { context -> AwesomeBarWrapper(context) },
//        update = {
//            it.visibility = View.GONE
//            it.layoutParams.width = LayoutParams.MATCH_PARENT
//            it.layoutParams.height = LayoutParams.MATCH_PARENT
//            it.setPadding(4.dp.toPx(), 4.dp.toPx(), 4.dp.toPx(), 4.dp.toPx())
//            setView(it)
//        })
//}
//
//@Composable
//fun MozEngineView(
//    modifier: Modifier,
//    setSwipeView: (VerticalSwipeRefreshLayout) -> Unit,
//    setEngineView: (GeckoEngineView) -> Unit
//) {
//    AndroidView(modifier = modifier.fillMaxSize(), factory = { context ->
//        val vl = VerticalSwipeRefreshLayout(context)
//        val gv = GeckoEngineView(context)
//        vl.addView(gv)
//        vl
//    }, update = { sv ->
//        var gv: GeckoEngineView? = null
//        // find GeckoEngineView child in scroll view
//        for (v in sv.children) {
//            if (v is GeckoEngineView) {
//                gv = v
//                break
//            }
//        }
//        // setup views
//        with(sv.layoutParams) {
//            this.width = LayoutParams.MATCH_PARENT
//            this.height = LayoutParams.MATCH_PARENT
//        }
//        with(gv!!.layoutParams) {
//            this.width = LayoutParams.MATCH_PARENT
//            this.height = LayoutParams.MATCH_PARENT
//        }
//        // set view references
//        setSwipeView(sv)
//        setEngineView(gv)
//    })
//}
//
//@Composable
//fun MozBrowserToolbar(setView: (BrowserToolbarCompat) -> Unit) {
//    AndroidView(modifier = Modifier
//        .fillMaxWidth()
//        .height(dimensionResource(id = R.dimen.browser_toolbar_height))
//        .background(Color.Black)
//        .padding(horizontal = 8.dp, vertical = 0.dp),
//        factory = { context -> BrowserToolbarCompat(context) },
//        update = { bt ->
//            bt.layoutParams.height = R.dimen.browser_toolbar_height
//            bt.layoutParams.width = LayoutParams.MATCH_PARENT
//            bt.visibility = View.VISIBLE
//            bt.setBackgroundColor(0xFF0000)
//            bt.displayMode()
//            setView(bt)
//        })
//}
//
///**
// * @param setView function to set view variable in parent
// */
//@Composable
//fun MozFindInPageBar(setView: (FindInPageBar) -> Unit) {
//    AndroidView(modifier = Modifier
//        .fillMaxSize()
//        .height(0.dp)
//        .width(0.dp),
//        factory = { context -> FindInPageBar(context) },
//        update = {
//            setView(it)
//            it.visibility = View.GONE
//        })
//}
//
//@Composable
//fun MozReaderViewControlsBar(
//    setView: (ReaderViewControlsBar) -> Unit
//) {
//    AndroidView(modifier = Modifier
//        .fillMaxSize()
//        .background(colorResource(id = R.color.toolbarBackgroundColor))
//        .height(0.dp)
//        .width(0.dp),
//        factory = { context -> ReaderViewControlsBar(context) },
//        update = {
//            setView(it)
//            it.visibility = View.GONE
//        })
//}
//
//// reader view button, what this for?
//@Composable
//fun MozFloatingActionButton(
//    setView: (FloatingActionButton) -> Unit
//) {
//    AndroidView(modifier = Modifier.fillMaxSize(),
//        factory = { context -> FloatingActionButton(context) },
//        update = {
//            setView(it)
//            it.visibility = View.GONE
//        })
//}