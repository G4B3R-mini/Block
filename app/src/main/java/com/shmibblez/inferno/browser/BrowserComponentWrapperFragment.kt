package com.shmibblez.inferno.browser

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.webPrompts.AndroidPhotoPicker
import com.shmibblez.inferno.browser.prompts.webPrompts.FilePicker
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.lastOpenedNormalTab
import com.shmibblez.inferno.ext.newTab
import com.shmibblez.inferno.ext.requireComponents
import com.shmibblez.inferno.nimbus.FxNimbus
import com.shmibblez.inferno.tabbar.toTabList
import com.shmibblez.inferno.tabs.tabstray.InfernoTabsTraySelectedTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.selector.privateTabs
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.recover.TabState
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.ActivityResultHandler
import mozilla.components.support.base.feature.UserInteractionHandler

class OnActivityResultModel(
    val requestCode: Int, val data: Intent?, val resultCode: Int,
)

data class BrowserUiState(
    var tabList: List<TabSessionState> = emptyList(),
    var normalTabs: List<TabSessionState> = emptyList(),
    var privateTabs: List<TabSessionState> = emptyList(),
    var closedTabs: List<TabState> = emptyList(),
    var currentTab: TabSessionState? = null,
    var isPrivateSession: Boolean = false,
    var searchEngine: SearchEngine? = null,
    var pageType: BrowserComponentPageType = BrowserComponentPageType.ENGINE,
    var selectedTabsTrayTab: InfernoTabsTraySelectedTab = InfernoTabsTraySelectedTab.NormalTabs,
)

class BrowserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    fun update(
        tabList: List<TabSessionState>,
        normalTabs: List<TabSessionState>,
        privateTabs: List<TabSessionState>,
        closedTabs: List<TabState>,
        currentTab: TabSessionState?,
        isPrivateSession: Boolean,
        searchEngine: SearchEngine?,
        pageType: BrowserComponentPageType,
        selectedTabsTrayTab: InfernoTabsTraySelectedTab,
    ) {
        _uiState.update {
            it.copy(
                tabList = tabList,
                normalTabs = normalTabs,
                privateTabs = privateTabs,
                closedTabs = closedTabs,
                currentTab = currentTab,
                isPrivateSession = isPrivateSession,
                searchEngine = searchEngine,
                pageType = pageType,
                selectedTabsTrayTab = selectedTabsTrayTab,
            )
        }
    }

    fun setSelectedTabsTrayTab(selectedTabsTrayTab: InfernoTabsTraySelectedTab) {
        _uiState.update {
            it.copy(
                selectedTabsTrayTab = selectedTabsTrayTab,
            )
        }
    }
}

// 3 secs max
private const val INIT_JOB_MILLIS = 3000L;

class BrowserComponentWrapperFragment : Fragment(), UserInteractionHandler, ActivityResultHandler,
    AccessibilityManager.AccessibilityStateChangeListener {

//    private val args by navArgs<BrowserComponentWrapperFragmentArgs>()

//    @VisibleForTesting
//    internal lateinit var bundleArgs: Bundle

    private var browserStateObserver: CoroutineScope? = null

    private var initialized = false
    private var preinitialized = false
    private var awaitingNewTab = false

    private val baseComposeView: ComposeView
        get() = requireView().findViewById(R.id.baseComposeView)

    // helper for compose migration, might be a lil sloppy
    private var onActivityResultHandler: ((OnActivityResultModel) -> Boolean)? = null
    private val setOnActivityResultHandler: ((OnActivityResultModel) -> Boolean) -> Unit =
        { f -> onActivityResultHandler = f }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        bundleArgs = args.toBundle()
        if (savedInstanceState != null) {
            arguments?.putBoolean(FOCUS_ON_ADDRESS_BAR, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_browser_component_wrapper, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // todo: implement functionality, reference [HomeFragment]
        val focusOnAddressBar =
            arguments?.getBoolean(FOCUS_ON_ADDRESS_BAR) ?: false || FxNimbus.features.oneClickSearch.value().enabled
        val scrollToCollection = arguments?.getBoolean(SCROLL_TO_COLLECTION) ?: false

        val browserViewModel: BrowserViewModel by viewModels()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                browserViewModel.uiState.collect {
                    baseComposeView.setContent {
                        BrowserComponent(
                            navController = findNavController(),
                            setOnActivityResultHandler = setOnActivityResultHandler,
                            setSelectedTabsTrayTab = { browserViewModel.setSelectedTabsTrayTab(it) },
                            tabList = it.tabList,
                            normalTabs = it.normalTabs,
                            privateTabs = it.privateTabs,
                            closedTabs = it.closedTabs,
                            currentTab = it.currentTab,
                            searchEngine = it.searchEngine,
                            pageType = it.pageType,
                            selectedTabsTrayTab = it.selectedTabsTrayTab,
                        )
                    }
                }
            }
        }

        val store = requireComponents.core.store

        browserStateObserver = store.flowScoped(viewLifecycleOwner) { flow ->
            flow.map { it }.collect {
                    val currentTab = it.selectedTab

                    if (!initialized && !preinitialized) {
                        // if first run, preinit and delay for a bit
                        preinitialized = true
                        delay(INIT_JOB_MILLIS)
                        initialized = true
                        return@collect
                    } else if (!initialized && preinitialized && currentTab != null) {
                        // if not init, delaying, and current tab isn't null, warm up complete, set
                        // init to true and continue
                        initialized = true
                    } else if (!initialized && preinitialized) {
                        // if not init and delaying, return
                        return@collect
                    } else {
                        // if init complete continue
                    }

                    val tabList: List<TabSessionState>

                    Log.d("WrapperFrag", "content update")
                    // if no tab selected, false
                    val isPrivateSession: Boolean = currentTab?.content?.private ?: false
//                val mode = BrowsingMode.fromBoolean(isPrivateSession)
//                (context.getActivity()!! as HomeActivity).browsingModeManager.mode = mode
//                context.components.appStore.dispatch(AppAction.ModeChange(mode))
                    val selectedTabsTrayTab: InfernoTabsTraySelectedTab =
                        if (isPrivateSession) InfernoTabsTraySelectedTab.PrivateTabs else InfernoTabsTraySelectedTab.NormalTabs
                    tabList =
                        if (isPrivateSession) it.privateTabs else it.normalTabs // it.toTabList().first
                    val normalTabs: List<TabSessionState> = it.normalTabs
                    val privateTabs: List<TabSessionState> = it.privateTabs
                    val closedTabs: List<TabState> = it.closedTabs
                    // if no tab selected, select one
                    if (currentTab == null) {
                        if (tabList.isNotEmpty()) {
                            val lastNormalTabId = store.state.lastOpenedNormalTab?.id
                            if (tabList.any { tab -> tab.id == lastNormalTabId }) {
                                requireComponents.useCases.tabsUseCases.selectTab(
                                    lastNormalTabId!!
                                )
                            } else {
                                requireComponents.useCases.tabsUseCases.selectTab(tabList.last().id)
                            }
                        } else if (!awaitingNewTab) {
                            // if tab list empty add new tab
                            requireComponents.newTab(false)
                            awaitingNewTab = true
                        }
                    } else {
                        awaitingNewTab = false
                    }
                    val searchEngine: SearchEngine? = it.search.selectedOrDefaultSearchEngine!!
                    val pageType: BrowserComponentPageType = resolvePageType(currentTab)

                    browserViewModel.update(
                        tabList = tabList,
                        normalTabs = normalTabs,
                        privateTabs = privateTabs,
                        closedTabs = closedTabs,
                        currentTab = currentTab,
                        isPrivateSession = isPrivateSession,
                        searchEngine = searchEngine,
                        pageType = pageType,
                        selectedTabsTrayTab = selectedTabsTrayTab,
                    )
                }
        }

        requireContext().components.crashReporter.install(requireContext())
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        browserStateObserver?.cancel()
    }

    companion object {
        // todo: implement functionality, reference [HomeFragment]
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
        private const val SESSION_ID = "session_id"

//        @JvmStatic
//        private fun Bundle.putSessionId(sessionId: String?) {
//            putString(SESSION_ID, sessionId)
//        }

        fun create(sessionId: String? = null) = BrowserComponentWrapperFragment().apply {
            arguments = Bundle().apply {
//                putSessionId(sessionId)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, data: Intent?, resultCode: Int): Boolean {
        Log.d("BrowserComponentWFrag", "BrowserComponentWrapperFragment.onActivityResult called")
        Log.d(
            "BrowserComponentWFrag",
            "BrowserComponentWrapperFragment.onActivityResult, onActivityResultHandler: $onActivityResultHandler"
        )

        if (onActivityResultHandler != null) {
            Log.d(
                "BrowserComponentWFrag",
                "BrowserComponentWrapperFragment.onActivityResult, handled correctly"
            )
            return onActivityResultHandler!!.invoke(
                OnActivityResultModel(requestCode, data, resultCode)
            )
        } else return false
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
////        bundleArgs.clear()
//    }

    override fun onBackPressed(): Boolean {
//        TODO("Not yet implemented")
        return false
    }

    override fun onAccessibilityStateChanged(enabled: Boolean) {
        // todo: make toolbar unscrollable if true
    }
}

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
