package com.shmibblez.inferno.browser

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomAppBar
import androidx.compose.material.FabPosition
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldDefaults
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.view.children
import androidx.fragment.app.findFragment
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.preference.PreferenceManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.shmibblez.inferno.R
import com.shmibblez.inferno.addons.WebExtensionPromptFeature
import com.shmibblez.inferno.downloads.DownloadService
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.getPreferenceKey
import com.shmibblez.inferno.pip.PictureInPictureIntegration
import com.shmibblez.inferno.search.AwesomeBarWrapper
import com.shmibblez.inferno.tabbar.BrowserTabBar
import com.shmibblez.inferno.tabbar.toTabList
import com.shmibblez.inferno.tabs.LastTabFeature
import com.shmibblez.inferno.tabs.TabsTrayFragment
import com.shmibblez.inferno.toolbar.BrowserToolbar
import mozilla.components.browser.engine.gecko.GeckoEngineView
import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.thumbnails.BrowserThumbnails
import mozilla.components.browser.toolbar.BrowserToolbar as BrowserToolbarCompat
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.app.links.AppLinksFeature
import mozilla.components.feature.awesomebar.AwesomeBarFeature
import mozilla.components.feature.awesomebar.provider.SearchSuggestionProvider
import mozilla.components.feature.downloads.DownloadsFeature
import mozilla.components.feature.downloads.manager.FetchDownloadManager
import mozilla.components.feature.downloads.temporary.ShareDownloadFeature
import mozilla.components.feature.findinpage.view.FindInPageBar
import mozilla.components.feature.findinpage.view.FindInPageView
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
import mozilla.components.lib.state.Store
import mozilla.components.lib.state.ext.observe
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.android.view.enterImmersiveMode
import mozilla.components.support.ktx.android.view.exitImmersiveMode
import mozilla.components.ui.widgets.VerticalSwipeRefreshLayout
import kotlin.math.roundToInt

// TODO: check if works:
//  - vertical swipe refresh
//  - permissions handling
//  -

fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}


// TODO: pass data from parent fragment onActivityResult to handlers here
//  with SharedFlow or LiveData (bound to view lifecycle), put this in event receiver:
//  ``activityResultHandler.any { it.onActivityResult(requestCode, data, resultCode) }``

/**
 * @param sessionId session id, from Moz BaseBrowserFragment
 */
@Composable
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
fun BrowserComponent(
    sessionId: String?, setOnActivityResultHandler: ((OnActivityResultModel) -> Boolean) -> Unit
) {
    Log.i("BrowserComponent", "BrowserComponent composed")
    val scaffoldState = rememberScaffoldState()
//    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val view = LocalView.current
    val parentFragmentManager = context.getActivity()!!.supportFragmentManager
    // browser state observer setup
    val localLifecycleOwner = LocalLifecycleOwner.current
    var browserStateObserver: Store.Subscription<BrowserState, BrowserAction>? by remember {
        mutableStateOf(
            null
        )
    }
    var tabList by remember { mutableStateOf(context.components.core.store.state.toTabList().first) }
    var tabSessionState by remember { mutableStateOf(context.components.core.store.state.selectedTab) }
    //
    val (showMenuBottomSheet, setShowMenuBottomSheet) = remember { mutableStateOf(false) }
    
    if (showMenuBottomSheet){
        // todo: show ModalBottomSheet
    }

    // setup tab observer
    DisposableEffect(true) {
        browserStateObserver = context.components.core.store.observe(localLifecycleOwner) {
            tabList = it.toTabList().first
            tabSessionState = it.selectedTab
        }

        onDispose {
            browserStateObserver!!.unsubscribe()
        }
    }
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


    /// views
    // from Moz BaseBrowserFragment
    var engineView: EngineView? by remember { mutableStateOf(null) }
    var toolbar: BrowserToolbarCompat? by remember { mutableStateOf(null) }
    var findInPageBar: FindInPageBar? by remember { mutableStateOf(null) }
    var swipeRefresh: SwipeRefreshLayout? by remember { mutableStateOf(null) }
    // from Moz BrowserFragment
    var awesomeBar: AwesomeBarWrapper? by remember { mutableStateOf(null) }
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

    // from Moz BaseBrowserFragment
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
    BackHandler {
        // combines Moz BrowserFragment and Moz BaseBrowserFragment implementations
        onBackPressed(readerViewFeature, backButtonHandler)
    }

    // TODO: implement downloadsFeature, appLinksFeature, contextMenuIntegration,
    //  promptsFeature, webExtensionPromptsFeature, and fullScreenFeature in compose
    //  ONLY IF NECESSARY, MIGHT NOT BE


    LaunchedEffect(engineView) {
        if (engineView == null) return@LaunchedEffect
        // from Moz BaseBrowserFragment
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

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
                useCases = context.components.useCases.downloadsUseCases,
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
                        it, context, toolbar!!, engineView!!
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

        // from Moz BrowserHandler
        engineView!!.setDynamicToolbarMaxHeight(context.resources.getDimensionPixelSize(R.dimen.browser_toolbar_height))
    }

    val bottomBarHeight = 90.dp
    val bottomBarHeightPx = with(LocalDensity.current) { bottomBarHeight.roundToPx().toFloat() }
    val bottomBarOffsetHeightPx = remember { mutableFloatStateOf(0f) }
    // connection to the nested scroll system and listen to the scroll
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset, source: NestedScrollSource
            ): Offset {
                val delta = available.y
                val newOffset = bottomBarOffsetHeightPx.floatValue + delta
                bottomBarOffsetHeightPx.floatValue = newOffset.coerceIn(-bottomBarHeightPx, 0f)

                return Offset.Zero
            }
        }
    }

    Scaffold(modifier = Modifier.nestedScroll(nestedScrollConnection),
        scaffoldState = scaffoldState,
//        topBar = {
//            TopAppBar(
//                content = {
//
//                }
//            )
//        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        content = { _ ->
            MozAwesomeBar(setView = { ab -> awesomeBar = ab })
            MozEngineView(
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
                    .height(bottomBarHeight)
                    .offset {
                        IntOffset(
                            x = 0, y = -bottomBarOffsetHeightPx.floatValue.roundToInt()
                        )
                    }) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    BrowserTabBar(tabList)
                    BrowserToolbar(tabSessionState = tabSessionState,
                        setShowMenu = setShowMenuBottomSheet)
                    MozBrowserToolbar { bt -> toolbar = bt }
                    MozFindInPageBar { fip -> findInPageBar = fip }
                    MozReaderViewControlsBar { cb -> readerViewBar = cb }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(Color.Magenta)
                    )
                }
            }
        })
}

private fun viewportFitChanged(viewportFit: Int, context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        context.getActivity()!!.window.attributes.layoutInDisplayCutoutMode = viewportFit
    }
}

private fun fullScreenChanged(
    enabled: Boolean, context: Context, toolbar: BrowserToolbarCompat, engineView: EngineView
) {
    if (enabled) {
        context.getActivity()?.enterImmersiveMode()
        toolbar.visibility = View.GONE
        engineView.setDynamicToolbarMaxHeight(0)
    } else {
        context.getActivity()?.exitImmersiveMode()
        toolbar.visibility = View.VISIBLE
        engineView.setDynamicToolbarMaxHeight(context.resources.getDimensionPixelSize(R.dimen.browser_toolbar_height))
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
    setSwipeView: (VerticalSwipeRefreshLayout) -> Unit, setEngineView: (GeckoEngineView) -> Unit
) {
    AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
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
        gv!!.setDynamicToolbarMaxHeight(R.dimen.browser_toolbar_height)
        with(sv.layoutParams) {
            this.width = LayoutParams.MATCH_PARENT
            this.height = LayoutParams.MATCH_PARENT
        }
        with(gv.layoutParams) {
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