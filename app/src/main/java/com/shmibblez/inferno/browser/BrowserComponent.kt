package com.shmibblez.inferno.browser

import android.util.TypedValue
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomAppBar
import androidx.compose.material.FabPosition
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldDefaults
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.shmibblez.inferno.R
import com.shmibblez.inferno.addons.WebExtensionPromptFeature
import com.shmibblez.inferno.browser.BaseBrowserFragment.Companion.SESSION_ID
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.pip.PictureInPictureIntegration
import com.shmibblez.inferno.search.AwesomeBarWrapper
import com.shmibblez.inferno.tabbar.BrowserTabBar
import com.shmibblez.inferno.tabbar.ToolbarWrapper
import com.shmibblez.inferno.tabs.LastTabFeature
import com.shmibblez.inferno.tabs.TabsTrayFragment
import mozilla.components.browser.engine.gecko.GeckoEngineView
import mozilla.components.browser.thumbnails.BrowserThumbnails
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.app.links.AppLinksFeature
import mozilla.components.feature.awesomebar.AwesomeBarFeature
import mozilla.components.feature.awesomebar.provider.SearchSuggestionProvider
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
import mozilla.components.feature.syncedtabs.SyncedTabsStorageSuggestionProvider
import mozilla.components.feature.tabs.WindowFeature
import mozilla.components.feature.tabs.toolbar.TabsToolbarFeature
import mozilla.components.feature.toolbar.WebExtensionToolbarFeature
import mozilla.components.feature.webauthn.WebAuthnFeature
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import kotlin.math.roundToInt

// TODO: vertical swipe refresh
// TODO: permissions handling

/**
 * @param sessionId session id, from Moz BaseBrowserFragment
 */
@Composable
fun BrowserComponent(sessionId: String?) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val view = LocalView.current

    /// features
    // from Moz BaseBrowserFragment
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
    // from Moz BrowserFragment
    val thumbnailsFeature = remember { ViewBoundFeatureWrapper<BrowserThumbnails>() }
    val readerViewFeature = remember { ViewBoundFeatureWrapper<ReaderViewIntegration>() }
    val webExtToolbarFeature = remember { ViewBoundFeatureWrapper<WebExtensionToolbarFeature>() }
    // also in Moz BrowserHandler
    // val windowFeature = remember { ViewBoundFeatureWrapper<WindowFeature>() }

    /// views
    // from Moz BaseBrowserFragment
    var engineView: EngineView? by remember { mutableStateOf(null) }
    val toolbar: BrowserToolbar? by remember { mutableStateOf(null) }
    val tabBar: ComposeView? by remember { mutableStateOf(null) }
    val bottomLayout: ToolbarWrapper? by remember { mutableStateOf(null) }
    val findInPageBar: FindInPageBar? by remember { mutableStateOf(null) }
    val swipeRefresh: SwipeRefreshLayout? by remember { mutableStateOf(null) }
    // from Moz BrowserFragment
    // TODO: where does awesomeBar go in layout?
    var awesomeBar: AwesomeBarWrapper? by remember { mutableStateOf(null) }
    // also in Moz BaseBrowserHandler
    // var toolbar: BrowserToolbar? by remember { mutableStateOf(null) }
    // also in Moz BaseBrowserHandler
    // var engineView: EngineView? by remember { mutableStateOf(null) }
    var readerViewBar: ReaderViewControlsBar? by remember { mutableStateOf(null) }
    var readerViewAppearanceButton: FloatingActionButton? by remember { mutableStateOf(null) }

    /// handlers & misc
    // from Moz BaseBrowserFragment
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
    var webAppToolbarShouldBeVisible = true
    // TODO: request permissions with accompanyist or experimental api
    lateinit var requestDownloadPermissionsLauncher: ActivityResultLauncher<Array<String>>
    lateinit var requestSitePermissionsLauncher: ActivityResultLauncher<Array<String>>
    lateinit var requestPromptsPermissionsLauncher: ActivityResultLauncher<Array<String>>

    BackHandler {
        // from Moz BrowserHandler
        readerViewFeature.onBackPressed()
    }

    // TODO: setup below in startup
    LaunchedEffect(true) {
        // from Moz BrowserHandler
        AwesomeBarFeature(awesomeBar!!, toolbar!!, engineView)
            .addSearchProvider(
                context,
                context.components.core.store,
                context.components.useCases.searchUseCases.defaultSearch,
                fetchClient = context.components.core.client,
                mode = SearchSuggestionProvider.Mode.MULTIPLE_SUGGESTIONS,
                engine = context.components.core.engine,
                limit = 5,
                filterExactMatch = true,
            )
            .addSessionProvider(
                context.resources,
                context.components.core.store,
                context.components.useCases.tabsUseCases.selectTab,
            )
            .addHistoryProvider(
                context.components.core.historyStorage,
                context.components.useCases.sessionUseCases.loadUrl,
            )
            .addClipboardProvider(context, context.components.useCases.sessionUseCases.loadUrl)

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
            showTabs = ::showTabs,
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

        // from Moz BrowserHandler
        engineView!!.setDynamicToolbarMaxHeight(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                96F,
                context.resources.displayMetrics
            ).toInt()
        )
    }

    val bottomBarHeight = 48.dp
    val bottomBarHeightPx = with(LocalDensity.current) { bottomBarHeight.roundToPx().toFloat() }
    val bottomBarOffsetHeightPx = remember { mutableFloatStateOf(0f) }
    // connection to the nested scroll system and listen to the scroll
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = bottomBarOffsetHeightPx.floatValue + delta
                bottomBarOffsetHeightPx.floatValue = newOffset.coerceIn(-bottomBarHeightPx, 0f)

                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        scaffoldState = scaffoldState,
        topBar = {},
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        content = { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                MozEngineView(setView = { ev ->
                    engineView = ev
                })
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            MozFloatingActionButton { fab -> readerViewAppearanceButton = fab }
        },
        bottomBar = {
            // hide and show when scrolling
            BottomAppBar(
                modifier = Modifier
                    .height(40.dp)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = -bottomBarOffsetHeightPx.floatValue.roundToInt()
                        )
                    }
            ) {
                Column {
                    MozFindInPageBar { fip -> findInPageBar = fip }
                    MozReaderViewControlsBar { cb -> readerViewBar = cb }
                    BrowserTabBar()
                    MozBrowserToolbar { bt -> toolbar = bt }
                }
            }
        }
    )
}

// from Moz BrowserHandler
private fun showTabs() {
    // For now we are performing manual fragment transactions here. Once we can use the new
    // navigation support library we may want to pass navigation graphs around.
    // TODO: use navigation instead of fragment transactions
    activity?.supportFragmentManager?.beginTransaction()?.apply {
        replace(R.id.container, TabsTrayFragment())
        commit()
    }
}

@Composable
fun MozEngineView(setView: (GeckoEngineView) -> Unit) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context -> GeckoEngineView(context) },
        update = { setView(it) }
    )
}

@Composable
fun MozBrowserToolbar(setView: (BrowserToolbar) -> Unit) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context -> BrowserToolbar(context) },
        update = { setView(it) }
    )
}

/**
 * @param setView function to set view variable in parent
 */
@Composable
fun MozFindInPageBar(visible: Boolean = false, setView: (FindInPageBar) -> Unit) {
    if (visible)
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context -> FindInPageBar(context) },
            update = { setView(it) }
        )
}

@Composable
fun MozReaderViewControlsBar(visible: Boolean = false, setView: (ReaderViewControlsBar) -> Unit) {
    if (visible)
        AndroidView(
            factory = { context -> ReaderViewControlsBar(context) },
            update = { setView(it) }
        )
}

// reader view button, what this for?
@Composable
fun MozFloatingActionButton(visible: Boolean = false, setView: (FloatingActionButton) -> Unit) {
    if (visible)
        AndroidView(
            factory = { context -> FloatingActionButton(context) },
            update = { setView(it) }
        )
}