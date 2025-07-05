package com.shmibblez.inferno.browser.state

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.shmibblez.inferno.browser.BrowserComponentPageType
import com.shmibblez.inferno.browser.OnActivityResultModel
import com.shmibblez.inferno.components.Components
import com.shmibblez.inferno.customtabs.PoweredByNotification
import com.shmibblez.inferno.customtabs.WebAppSiteControlsBuilder
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.determineCustomHomeUrl
import com.shmibblez.inferno.ext.newTab
import com.shmibblez.inferno.ext.selectLastNormalTab
import com.shmibblez.inferno.ext.settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.selector.findCustomTab
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.selector.privateTabs
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.CustomTabSessionState
import mozilla.components.browser.state.state.ExternalAppType
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.manifest.WebAppManifest
import mozilla.components.feature.customtabs.CustomTabWindowFeature
import mozilla.components.feature.pwa.ManifestStorage
import mozilla.components.feature.pwa.WebAppShortcutManager
import mozilla.components.feature.pwa.feature.ManifestUpdateFeature
import mozilla.components.feature.pwa.feature.WebAppActivityFeature
import mozilla.components.feature.pwa.feature.WebAppContentFeature
import mozilla.components.feature.pwa.feature.WebAppHideToolbarFeature
import mozilla.components.feature.pwa.feature.WebAppSiteControlsFeature
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.android.NotificationsDelegate
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.ktx.android.arch.lifecycle.addObservers

enum class BrowserComponentMode {
    TOOLBAR, TOOLBAR_SEARCH, TOOLBAR_EXTERNAL, FIND_IN_PAGE, READER_VIEW, FULLSCREEN,
}

private class CustomTabManager(
    private val customTabSessionState: CustomTabSessionState,
    private val activity: AppCompatActivity,

    private val icons: BrowserIcons,
    private val store: BrowserStore,
    private val shortcutManager: WebAppShortcutManager,
    private val storage: ManifestStorage,
//    private val controlsBuilder: SiteControlsBuilder = SiteControlsBuilder.Default(),
    private val notificationsDelegate: NotificationsDelegate,
    private val components: Components,
    private val setShowExternalToolbar: (Boolean) -> Unit,
) : LifecycleAwareFeature {

    private var manifest: WebAppManifest? = customTabSessionState.content.webAppManifest
    private val scope = MainScope()

    val testObserver = LifecycleEventObserver { source, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> Log.d(
                "CustomTabManager", "test observer, event: ON_CREATE"
            )

            Lifecycle.Event.ON_START -> Log.d("CustomTabManager", "test observer, event: ON_START")
            Lifecycle.Event.ON_RESUME -> Log.d(
                "CustomTabManager", "test observer, event: ON_RESUME"
            )

            Lifecycle.Event.ON_PAUSE -> Log.d("CustomTabManager", "test observer, event: ON_PAUSE")
            Lifecycle.Event.ON_STOP -> Log.d("CustomTabManager", "test observer, event: ON_STOP")
            Lifecycle.Event.ON_DESTROY -> Log.d(
                "CustomTabManager", "test observer, event: ON_DESTROY"
            )

            Lifecycle.Event.ON_ANY -> Log.d("CustomTabManager", "test observer, event: ON_ANY")
        }
    }

    /** observers */
    // features
    var hideToolbarFeature: WebAppHideToolbarFeature? = null
    var windowFeature: CustomTabWindowFeature? = null

    // manifest != null
    var webAppActivityObserver: WebAppActivityFeature? = null
    var webAppContentObserver: WebAppContentFeature? = null
    var manifestUpdateObserver: ManifestUpdateFeature? = null
    var webAppSiteControlsObserver: WebAppSiteControlsFeature? = null

    // manifest == null
    var poweredByObserver: PoweredByNotification? = null


    override fun start() {
        Log.d("CustomTabManager", "start()")

        val isPwaTabOrTwaTab =
            customTabSessionState.config.externalAppType == ExternalAppType.PROGRESSIVE_WEB_APP || customTabSessionState.config.externalAppType == ExternalAppType.TRUSTED_WEB_ACTIVITY

        // setup features

        // Only set hideToolbarFeature if isPwaTabOrTwaTab
        if (isPwaTabOrTwaTab) {
            hideToolbarFeature = WebAppHideToolbarFeature(
                store = store,
                customTabsStore = components.core.customTabsStore,
                tabId = customTabSessionState.id,
                manifest = manifest,
                setToolbarVisibility = setShowExternalToolbar,
            )
        }


        windowFeature = CustomTabWindowFeature(
            activity = activity,
            store = store,
            sessionId = customTabSessionState.id,
        )

        hideToolbarFeature?.start()
        windowFeature?.start()

        // setup observers
        if (manifest != null) {
            Log.d("CustomTabManager", "start(), initializing observers with manifest != null")
            webAppActivityObserver = WebAppActivityFeature(
                activity,
                icons,
                manifest!!,
            )
            webAppContentObserver = WebAppContentFeature(
                store = store,
                tabId = customTabSessionState.id,
                manifest!!,
            )
            manifestUpdateObserver = ManifestUpdateFeature(
                activity.applicationContext,
                store,
                shortcutManager,
                storage,
                customTabSessionState.id,
                manifest!!,
            )
            webAppSiteControlsObserver = WebAppSiteControlsFeature(
                activity.applicationContext,
                store,
                components.useCases.sessionUseCases.reload,
                customTabSessionState.id,
                manifest,
                WebAppSiteControlsBuilder(
                    store,
                    components.useCases.sessionUseCases.reload,
                    customTabSessionState.id,
                    manifest!!,
                ),
                notificationsDelegate = notificationsDelegate,
            )
            activity.lifecycle.addObservers(
                webAppActivityObserver!!,
                webAppContentObserver!!,
                manifestUpdateObserver!!,
            )
            // todo: bind to view lifecycle instead
//            viewLifecycleOwner.lifecycle.addObserver(
            activity.lifecycle.addObserver(webAppSiteControlsObserver!!)
        } else {
            Log.d("CustomTabManager", "start(), initializing observers with manifest == null")
            // todo: bind to view lifecycle instead
//            viewLifecycleOwner.lifecycle.addObserver(
            poweredByObserver = PoweredByNotification(
                activity.applicationContext,
                store,
                customTabSessionState.id,
                notificationsDelegate,
            )
            activity.lifecycle.addObserver(poweredByObserver!!)
        }
        activity.lifecycle.addObserver(testObserver)
    }

    override fun stop() {
        Log.d("CustomTabManager", "stop()")

        // stop features
        hideToolbarFeature?.stop()
        windowFeature?.stop()

        // stop observers
        scope.cancel()
        webAppActivityObserver?.let {
            activity.lifecycle.removeObserver(it)
        }
        webAppContentObserver?.let {
            activity.lifecycle.removeObserver(it)
        }
        manifestUpdateObserver?.let {
            activity.lifecycle.removeObserver(it)
        }
        poweredByObserver?.let {
            activity.lifecycle.removeObserver(it)
        }
        activity.lifecycle.removeObserver(testObserver)
    }
}

@Composable
fun rememberBrowserComponentState(
    isAuth: Boolean,
    customTabSessionId: String?,
    activity: AppCompatActivity,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    lifecycleOwner: LifecycleOwner? = null,
    components: Components = LocalContext.current.components,
    store: BrowserStore = LocalContext.current.components.core.store,
    tabsUseCases: TabsUseCases = LocalContext.current.components.useCases.tabsUseCases,
): MutableState<BrowserComponentState> {

    val state = rememberSaveable(customTabSessionId, stateSaver = Saver(
        save = {
            customTabSessionId
        },
        restore = {
            BrowserComponentState(
                isAuth = isAuth,
                customTabSessionId = customTabSessionId,
                activity = activity,
                coroutineScope = coroutineScope,
                lifecycleOwner = lifecycleOwner,
                components = components,
                store = store,
                tabsUseCases = tabsUseCases,
            )
        },
    ), key = null, init = {
        mutableStateOf(
            BrowserComponentState(
                isAuth = isAuth,
                customTabSessionId = customTabSessionId,
                activity = activity,
                coroutineScope = coroutineScope,
                lifecycleOwner = lifecycleOwner,
                components = components,
                store = store,
                tabsUseCases = tabsUseCases,
            )
        )
    })

//    val state = remember {
//        mutableStateOf(
//            BrowserComponentState(
//                customTabSessionId = customTabSessionId,
//                activity = activity,
//                coroutineScope = coroutineScope,
//                lifecycleOwner = lifecycleOwner,
//                components = components,
//                store = store,
//                tabsUseCases = tabsUseCases,
//            )
//        )
//    }

    DisposableEffect(null) {
        state.value.start()
        onDispose { state.value.stop() }
    }

    return state
}

// 3 secs max
private const val INIT_JOB_MILLIS = 3000L

class BrowserComponentState(
    val isAuth: Boolean = false,
    val customTabSessionId: String?,
    val activity: AppCompatActivity,
    val coroutineScope: CoroutineScope,
    val lifecycleOwner: LifecycleOwner? = null,
    val components: Components,
    val store: BrowserStore,
    val tabsUseCases: TabsUseCases,
) : LifecycleAwareFeature {

    private var browserStateObserver: CoroutineScope? = null

    private var initialized = false
    private var preinitialized = false

    private var customTabManager: CustomTabManager? = null

    // todo: test, before run also make changes to tab bar (tab layout)
    private var awaitingNewTab = false

    // browser display mode, also sets bottomBarHeightDp
    var browserMode by mutableStateOf(BrowserComponentMode.TOOLBAR)
        private set
    private val isExternal
        get() = customTabSessionId != null

    var tabList: List<TabSessionState> by mutableStateOf(emptyList())
        private set
    var currentTab: TabSessionState? by mutableStateOf(null)
        private set
    var currentCustomTab: CustomTabSessionState? by mutableStateOf(null)
        private set
    var isPendingTab by mutableStateOf(true)
        private set
    var isPrivateSession: Boolean by mutableStateOf(false)
        private set
    var searchEngine: SearchEngine? by mutableStateOf(null)
        private set
    var pageType: BrowserComponentPageType by mutableStateOf(BrowserComponentPageType.ENGINE)
        private set
    var showExternalToolbar by mutableStateOf(true)
        private set

    var lastSavedGeneratedPassword: String? = null

    // helper for compose migration, might be a lil sloppy
    var onActivityResultHandler: ((OnActivityResultModel) -> Boolean)? = null
        private set
    val setOnActivityResultHandler: ((OnActivityResultModel) -> Boolean) -> Unit =
        { f -> onActivityResultHandler = f }


    override fun start() {
        Log.d(
            "BrowserComponentState",
            "----------------------------\n\nstart()\n\n----------------------------"
        )
        browserStateObserver = store.flowScoped(lifecycleOwner) { flow ->
            flow.map { it }.collect {
                currentTab = it.selectedTab
                // if custom tab id not null and custom tab null, find tab and set
                customTabSessionId?.let { id -> currentCustomTab = it.findCustomTab(id) }
                isPendingTab = currentTab == null && currentCustomTab == null

//                currentCustomTab?.let { ct ->
//                    Log.d(
//                        "BrowserComponentState",
//                        "windowRequest: ${ct.content.windowRequest}\nconfig: ${ct.config}\nmanifest: ${ct.content.webAppManifest}\ncontent: ${ct.content}"
//                    )
//                }


                // set and manage customTabManager
                if (currentCustomTab != null && customTabManager == null) {
                    // if custom tab exists and manager not setup, setup
                    customTabManager = CustomTabManager(
                        customTabSessionState = currentCustomTab!!,
                        activity = activity,
                        icons = components.core.icons,
                        store = store,
                        shortcutManager = components.core.webAppShortcutManager,
                        storage = components.core.webAppManifestStorage,
//                        controlsBuilder = SiteControlsBuilder.CopyAndRefresh(components.useCases.sessionUseCases.reload),
                        notificationsDelegate = components.notificationsDelegate,
                        components = components,
                        setShowExternalToolbar = { show -> showExternalToolbar = show },
                    )
                    customTabManager!!.start()
                } else if (currentCustomTab == null && customTabManager != null) {
                    // if custom tab nonexistent and manager exists, unexist manager and stop
                    customTabManager!!.stop()
                    customTabManager = null
                }

                // select external tab if not selected
                if (isExternal && it.selectedTabId != currentCustomTab?.id) {
                    currentCustomTab?.id?.let { tabId ->
                        components.useCases.tabsUseCases.selectTab(
                            tabId
                        )
                    }
                }

                val logStr = StringBuilder().apply {
                    append("state changed:")
                    append("\n  - currentCustomTab:")
                    when (currentCustomTab) {
                        null -> append(" null")
                        else -> {
                            append("\n    - id: $customTabSessionId")
                            append("\n    - loading: ${currentCustomTab?.content?.loading}")
                            append("\n    - engineState crash: ${currentCustomTab?.engineState?.crashed}")
                        }
                    }
                    append("\n  - currentTab:")
                    when (currentTab) {
                        null -> append(" null")
                        else -> {
                            append("\n    - id: ${currentTab?.id}")
                            append("\n    - loading: ${currentTab?.content?.loading}")
                            append("\n    - engineState crash: ${currentTab?.engineState?.crashed}")
                        }
                    }
                    append("\n  - webExtensionPromptRequest: ${it.webExtensionPromptRequest}")
                    append("\n\n")
                }.toString()

                Log.d("BrowserComponentState", logStr)

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
                tabList = when (isPrivateSession) {
                    true -> it.privateTabs
                    false -> it.normalTabs
                }
                // if no tab selected, select one
                if (isPendingTab) {
                    if (customTabSessionId != null) {
                        // if custom tab select
                        tabsUseCases.selectTab(customTabSessionId)
                    } else if (tabList.isNotEmpty()) {
                        // if tabs exist select
                        components.selectLastNormalTab()
                    } else if (!awaitingNewTab) {
                        // if no tabs available add new tab
                        components.newTab(
                            customHomeUrl = activity.settings().latestSettings?.determineCustomHomeUrl(),
                            private = false,
                        )
                        awaitingNewTab = true
                    }
                } else {
                    awaitingNewTab = false
                }

                searchEngine = it.search.selectedOrDefaultSearchEngine!!
                pageType = resolvePageType(currentTab)
            }
        }
    }

    override fun stop() {
        Log.d(
            "BrowserComponentState",
            "----------------------------\n\nstop()()\n\n----------------------------"
        )
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