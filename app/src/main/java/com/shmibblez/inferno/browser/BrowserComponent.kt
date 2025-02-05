package com.shmibblez.inferno.browser

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
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
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.shmibblez.inferno.BuildConfig
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.IntentReceiverActivity
import com.shmibblez.inferno.NavGraphDirections
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.browsingmode.BrowsingMode
import com.shmibblez.inferno.browser.tabstrip.isTabStripEnabled
import com.shmibblez.inferno.components.Components
import com.shmibblez.inferno.components.FindInPageIntegration
import com.shmibblez.inferno.components.TabCollectionStorage
import com.shmibblez.inferno.components.accounts.FxaWebChannelIntegration
import com.shmibblez.inferno.components.appstate.AppAction.MessagingAction
import com.shmibblez.inferno.components.appstate.AppAction.ShoppingAction
import com.shmibblez.inferno.components.toolbar.BottomToolbarContainerIntegration
import com.shmibblez.inferno.components.toolbar.BrowserFragmentStore
import com.shmibblez.inferno.components.toolbar.BrowserToolbarView
import com.shmibblez.inferno.components.toolbar.FenixTabCounterMenu
import com.shmibblez.inferno.components.toolbar.ToolbarContainerView
import com.shmibblez.inferno.components.toolbar.ToolbarIntegration
import com.shmibblez.inferno.components.toolbar.ToolbarPosition
import com.shmibblez.inferno.components.toolbar.interactor.BrowserToolbarInteractor
import com.shmibblez.inferno.components.toolbar.navbar.shouldAddNavigationBar
import com.shmibblez.inferno.components.toolbar.ui.createShareBrowserAction
import com.shmibblez.inferno.compose.snackbar.Snackbar
import com.shmibblez.inferno.compose.snackbar.SnackbarState
import com.shmibblez.inferno.customtabs.ExternalAppBrowserActivity
import com.shmibblez.inferno.downloads.DownloadService
import com.shmibblez.inferno.downloads.dialog.DynamicDownloadDialog
import com.shmibblez.inferno.downloads.dialog.StartDownloadDialog
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.consumeFlow
import com.shmibblez.inferno.ext.getPreferenceKey
import com.shmibblez.inferno.ext.isKeyboardVisible
import com.shmibblez.inferno.ext.isToolbarAtBottom
import com.shmibblez.inferno.ext.nav
import com.shmibblez.inferno.ext.navigateWithBreadcrumb
import com.shmibblez.inferno.ext.secure
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.findInPageBar.BrowserFindInPageBar
import com.shmibblez.inferno.home.HomeComponent
import com.shmibblez.inferno.home.HomeFragment
import com.shmibblez.inferno.library.bookmarks.friendlyRootTitle
import com.shmibblez.inferno.messaging.FenixMessageSurfaceId
import com.shmibblez.inferno.messaging.MessagingFeature
import com.shmibblez.inferno.microsurvey.ui.ext.MicrosurveyUIData
import com.shmibblez.inferno.nimbus.FxNimbus
import com.shmibblez.inferno.perf.MarkersFragmentLifecycleCallbacks
import com.shmibblez.inferno.pip.PictureInPictureIntegration
import com.shmibblez.inferno.search.AwesomeBarWrapper
import com.shmibblez.inferno.settings.SupportUtils
import com.shmibblez.inferno.settings.biometric.BiometricPromptFeature
import com.shmibblez.inferno.settings.quicksettings.protections.cookiebanners.getCookieBannerUIMode
import com.shmibblez.inferno.shopping.DefaultShoppingExperienceFeature
import com.shmibblez.inferno.shopping.ReviewQualityCheckFeature
import com.shmibblez.inferno.shortcut.PwaOnboardingObserver
import com.shmibblez.inferno.tabbar.BrowserTabBar
import com.shmibblez.inferno.tabbar.toTabList
import com.shmibblez.inferno.tabs.LastTabFeature
import com.shmibblez.inferno.tabs.TabsTrayFragment
import com.shmibblez.inferno.tabstray.Page
import com.shmibblez.inferno.theme.FirefoxTheme
import com.shmibblez.inferno.theme.ThemeManager
import com.shmibblez.inferno.toolbar.BrowserToolbar
import com.shmibblez.inferno.toolbar.ToolbarBottomMenuSheet
import com.shmibblez.inferno.wifi.SitePermissionsWifiIntegration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.appservices.places.BookmarkRoot
import mozilla.appservices.places.uniffi.PlacesApiException
import mozilla.components.browser.engine.gecko.GeckoEngineView
import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.selector.findCustomTabOrSelectedTab
import mozilla.components.browser.state.selector.findTabOrCustomTab
import mozilla.components.browser.state.selector.findTabOrCustomTabOrSelectedTab
import mozilla.components.browser.state.selector.getNormalOrPrivateTabs
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.CustomTabSessionState
import mozilla.components.browser.state.state.SessionState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.content.DownloadState
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.thumbnails.BrowserThumbnails
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.compose.cfr.CFRPopup
import mozilla.components.compose.cfr.CFRPopupLayout
import mozilla.components.compose.cfr.CFRPopupProperties
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.engine.permission.SitePermissions
import mozilla.components.concept.engine.prompt.ShareData
import mozilla.components.concept.storage.LoginEntry
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.feature.app.links.AppLinksUseCases
import mozilla.components.feature.contextmenu.ContextMenuCandidate
import mozilla.components.feature.contextmenu.ContextMenuFeature
import mozilla.components.feature.downloads.DownloadsFeature
import mozilla.components.feature.downloads.manager.FetchDownloadManager
import mozilla.components.feature.downloads.temporary.CopyDownloadFeature
import mozilla.components.feature.downloads.temporary.ShareDownloadFeature
import mozilla.components.feature.findinpage.view.FindInPageBar
import mozilla.components.feature.media.fullscreen.MediaSessionFullscreenFeature
import mozilla.components.feature.privatemode.feature.SecureWindowFeature
import mozilla.components.feature.prompts.PromptFeature
import mozilla.components.feature.prompts.address.AddressDelegate
import mozilla.components.feature.prompts.address.AddressSelectBar
import mozilla.components.feature.prompts.creditcard.CreditCardDelegate
import mozilla.components.feature.prompts.dialog.FullScreenNotificationToast
import mozilla.components.feature.prompts.dialog.GestureNavUtils
import mozilla.components.feature.prompts.file.AndroidPhotoPicker
import mozilla.components.feature.prompts.identitycredential.DialogColors
import mozilla.components.feature.prompts.identitycredential.DialogColorsProvider
import mozilla.components.feature.prompts.login.LoginDelegate
import mozilla.components.feature.prompts.login.PasswordGeneratorDialogColors
import mozilla.components.feature.prompts.login.PasswordGeneratorDialogColorsProvider
import mozilla.components.feature.prompts.login.SuggestStrongPasswordDelegate
import mozilla.components.feature.prompts.share.ShareDelegate
import mozilla.components.feature.readerview.ReaderViewFeature
import mozilla.components.feature.readerview.view.ReaderViewControlsBar
import mozilla.components.feature.search.SearchFeature
import mozilla.components.feature.session.FullScreenFeature
import mozilla.components.feature.session.PictureInPictureFeature
import mozilla.components.feature.session.ScreenOrientationFeature
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.session.SwipeRefreshFeature
import mozilla.components.feature.sitepermissions.SitePermissionsFeature
import mozilla.components.feature.tab.collections.TabCollection
import mozilla.components.feature.tabs.WindowFeature
import mozilla.components.feature.webauthn.WebAuthnFeature
import mozilla.components.lib.state.Store
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.lib.state.ext.observe
import mozilla.components.service.sync.autofill.DefaultCreditCardValidationDelegate
import mozilla.components.service.sync.logins.DefaultLoginValidationDelegate
import mozilla.components.service.sync.logins.LoginsApiException
import mozilla.components.service.sync.logins.SyncableLoginsStorage
import mozilla.components.support.base.feature.ActivityResultHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.android.view.enterImmersiveMode
import mozilla.components.support.ktx.android.view.exitImmersiveMode
import mozilla.components.support.ktx.android.view.hideKeyboard
import mozilla.components.support.ktx.kotlin.getOrigin
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifAnyChanged
import mozilla.components.support.utils.ext.isLandscape
import mozilla.components.ui.widgets.VerticalSwipeRefreshLayout
import mozilla.components.ui.widgets.withCenterAlignedButtons
import java.lang.ref.WeakReference
import kotlin.math.roundToInt
import mozilla.components.browser.toolbar.BrowserToolbar as BrowserToolbarCompat

// todo: implement [BrowserFragment], [BaseBrowserFragment]
// todo: implement layout from fragment_browser.xml

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

//companion object {
private const val KEY_CUSTOM_TAB_SESSION_ID = "custom_tab_session_id"
private const val REQUEST_CODE_DOWNLOAD_PERMISSIONS = 1
private const val REQUEST_CODE_PROMPT_PERMISSIONS = 2
private const val REQUEST_CODE_APP_PERMISSIONS = 3
private const val METRIC_SOURCE = "page_action_menu"
private const val TOAST_METRIC_SOURCE = "add_bookmark_toast"
private const val LAST_SAVED_GENERATED_PASSWORD = "last_saved_generated_password"

val onboardingLinksList: List<String> = listOf(
    SupportUtils.getMozillaPageUrl(SupportUtils.MozillaPage.PRIVATE_NOTICE),
    SupportUtils.FXACCOUNT_SUMO_URL,
)
//}

//companion object {
/**
 * Indicates weight of a page action. The lesser the weight, the closer it is to the URL.
 *
 * A weight of -1 indicates the position is not cared for and the action will be appended at the end.
 */
const val READER_MODE_WEIGHT = 1
const val TRANSLATIONS_WEIGHT = 2
const val REVIEW_QUALITY_CHECK_WEIGHT = 3
const val SHARE_WEIGHT = 4
const val RELOAD_WEIGHT = 5
const val OPEN_IN_ACTION_WEIGHT = 6
//}

private const val NAVIGATION_CFR_VERTICAL_OFFSET = 10
private const val NAVIGATION_CFR_ARROW_OFFSET = 24
private const val NAVIGATION_CFR_MAX_MS_BETWEEN_CLICKS = 5000

fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

fun nav(
    navController: NavController,
    @IdRes id: Int?,
    directions: NavDirections,
    options: NavOptions? = null
) {
    navController.nav(id, directions, options)
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
    val TAB_CORNER_RADIUS = 8.dp
    val FIND_IN_PAGE_BAR_HEIGHT = 50.dp
    fun BOTTOM_BAR_HEIGHT(browserComponentMode: BrowserComponentMode): Dp {
        return when (browserComponentMode) {
            BrowserComponentMode.TOOLBAR -> TOOLBAR_HEIGHT + TAB_BAR_HEIGHT
            BrowserComponentMode.FIND_IN_PAGE -> FIND_IN_PAGE_BAR_HEIGHT
        }
    }
}


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
//    args: HomeFragmentArgs
) {
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val navController = rememberNavController()
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

    /* BrowserFragment vars */
    val windowFeature = ViewBoundFeatureWrapper<WindowFeature>()
//    val openInAppOnboardingObserver = ViewBoundFeatureWrapper<OpenInAppOnboardingObserver>()
    val reviewQualityCheckFeature = ViewBoundFeatureWrapper<ReviewQualityCheckFeature>()
    val translationsBinding = ViewBoundFeatureWrapper<TranslationsBinding>()

    var readerModeAvailable = remember{mutableStateOf(false)}
    val (reviewQualityCheckAvailable, setReviewQualityCheckAvailable) = remember{mutableStateOf(false)}
    var translationsAvailable = remember{mutableStateOf(false)}

    var pwaOnboardingObserver: PwaOnboardingObserver? = null

    @VisibleForTesting var leadingAction: BrowserToolbar.Button? = null
    var forwardAction: BrowserToolbar.TwoStateButton? = null
    var backAction: BrowserToolbar.TwoStateButton? = null
    var refreshAction: BrowserToolbar.TwoStateButton? = null
    var isTablet: Boolean = false/* BrowserFragment  vars */

    /* BaseBrowserFragment vars */
//    var _binding: FragmentBrowserBinding? = null
//    val binding get() = _binding!!

    lateinit var browserFragmentStore: BrowserFragmentStore
    lateinit var browserAnimator: BrowserAnimator
    lateinit var startForResult: ActivityResultLauncher<Intent>

//    var _browserToolbarInteractor: BrowserToolbarInteractor? = null

    var (browserToolbarInteractor, setBrowserToolbarInteractor) = remember {
        mutableStateOf<BrowserToolbarInteractor?>(
            null
        )
    }

//    @VisibleForTesting
//    @Suppress("VariableNaming")
//    var _browserToolbarView: BrowserToolbarView? = null

//    @VisibleForTesting
//    val browserToolbarView: BrowserToolbarView
//    get() = _browserToolbarView!!

//    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
//    @Suppress("VariableNaming")
//    var _bottomToolbarContainerView: BottomToolbarContainerView? = null
//    val bottomToolbarContainerView: BottomToolbarContainerView
//    get() = _bottomToolbarContainerView!!

//    @Suppress("VariableNaming")
//    @VisibleForTesting
//    var _menuButtonView: MenuButton? = null

    val readerViewFeature = remember{ ViewBoundFeatureWrapper<ReaderViewFeature>() }
    val thumbnailsFeature = remember{ ViewBoundFeatureWrapper<BrowserThumbnails>() }

    @VisibleForTesting val messagingFeatureMicrosurvey = ViewBoundFeatureWrapper<MessagingFeature>()

    val sessionFeature = remember{ ViewBoundFeatureWrapper<SessionFeature>() }
    val contextMenuFeature = remember{ ViewBoundFeatureWrapper<ContextMenuFeature>() }
    val downloadsFeature = remember{ ViewBoundFeatureWrapper<DownloadsFeature>() }
    val shareDownloadsFeature = remember{ ViewBoundFeatureWrapper<ShareDownloadFeature>() }
    val copyDownloadsFeature = remember{ ViewBoundFeatureWrapper<CopyDownloadFeature>() }
    val promptsFeature = remember{ ViewBoundFeatureWrapper<PromptFeature>() }
//    lateinit var loginBarsIntegration: LoginBarsIntegration

//    @VisibleForTesting
    val findInPageIntegration = ViewBoundFeatureWrapper<com.shmibblez.inferno.components.FindInPageIntegration>()
    val toolbarIntegration = ViewBoundFeatureWrapper<ToolbarIntegration>()
    val bottomToolbarContainerIntegration =
        ViewBoundFeatureWrapper<BottomToolbarContainerIntegration>()
    val sitePermissionsFeature = ViewBoundFeatureWrapper<SitePermissionsFeature>()
    val fullScreenFeature = ViewBoundFeatureWrapper<FullScreenFeature>()
    val swipeRefreshFeature = ViewBoundFeatureWrapper<SwipeRefreshFeature>()
    val webchannelIntegration = ViewBoundFeatureWrapper<FxaWebChannelIntegration>()
    val sitePermissionWifiIntegration = ViewBoundFeatureWrapper<SitePermissionsWifiIntegration>()
    val secureWindowFeature = ViewBoundFeatureWrapper<SecureWindowFeature>()
    var fullScreenMediaSessionFeature = ViewBoundFeatureWrapper<MediaSessionFullscreenFeature>()
    val searchFeature = ViewBoundFeatureWrapper<SearchFeature>()
    val webAuthnFeature = ViewBoundFeatureWrapper<WebAuthnFeature>()
    val screenOrientationFeature = ViewBoundFeatureWrapper<ScreenOrientationFeature>()
    val biometricPromptFeature = ViewBoundFeatureWrapper<BiometricPromptFeature>()
//    val crashContentIntegration = ViewBoundFeatureWrapper<CrashContentIntegration>()
//    val readerViewBinding = ViewBoundFeatureWrapper<ReaderViewBinding>()
//    val openInFirefoxBinding = ViewBoundFeatureWrapper<OpenInFirefoxBinding>()
//    val findInPageBinding = ViewBoundFeatureWrapper<FindInPageBinding>()
//    val snackbarBinding = ViewBoundFeatureWrapper<SnackbarBinding>()
//    val standardSnackbarErrorBinding =
//        ViewBoundFeatureWrapper<StandardSnackbarErrorBinding>()

    var pipFeature by remember { mutableStateOf<PictureInPictureFeature?>(null) }

    val (customTabSessionId, setCustomTabSessionId) = remember { mutableStateOf<String?>(null) }

    val (browserInitialized, setBrowserInitialized) = remember { mutableStateOf(false) }
    var initUIJob by remember { mutableStateOf<Job?>(null) }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED) var webAppToolbarShouldBeVisible =
        true

//    val sharedViewModel: SharedViewModel by activityViewModels()
//    val homeViewModel: HomeScreenViewModel by activityViewModels()

    val currentStartDownloadDialog by remember { mutableStateOf<StartDownloadDialog?>(null) }

    lateinit var savedLoginsLauncher: ActivityResultLauncher<Intent>

    val (lastSavedGeneratedPassword, setLastSavedGeneratedPassword) = remember {
        mutableStateOf<String?>(
            null
        )
    }

    // Registers a photo picker activity launcher in single-select mode.
    val singleMediaPicker = AndroidPhotoPicker.singleMediaPicker(
        { getFragment(view) },
        { promptsFeature.get() },
    )

    // Registers a photo picker activity launcher in multi-select mode.
    val multipleMediaPicker = AndroidPhotoPicker.multipleMediaPicker(
        { getFragment(view) },
        { promptsFeature.get() },
    )/* BaseBrowserFragment vars */

    val backHandler = onBackPressedHandler(
        context = context,
        readerViewFeature = readerViewFeature,
        findInPageIntegration = findInPageIntegration,
        fullScreenFeature = fullScreenFeature,
        promptsFeature = promptsFeature,
        currentStartDownloadDialog = currentStartDownloadDialog!!,
        sessionFeature = sessionFeature,
    )

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

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
//                    super.onCreate(savedInstanceState)
                    // todo: registerForActivityResult
//                    savedLoginsLauncher = registerForActivityResult { navigateToSavedLoginsFragment() }

                }

                Lifecycle.Event.ON_DESTROY -> {
//                    super.onDestroyView()
//                    binding.engineView.setActivityContext(null)
                    // todo: accessibility
//                    context.accessibilityManager.removeAccessibilityStateChangeListener(this)

//                    _bottomToolbarContainerView = null
//                    _browserToolbarView = null
//                    _browserToolbarInteractor = null
//                    _binding = null


                    isTablet = false
                    leadingAction = null
                    forwardAction = null
                    backAction = null
                    refreshAction = null
                }

                Lifecycle.Event.ON_START -> {
//                    super.onStart()
//                    val context = context
                    val settings = context.settings()

                    if (!settings.userKnowsAboutPwas) {
                        pwaOnboardingObserver = PwaOnboardingObserver(
                            store = context.components.core.store,
                            lifecycleOwner = lifecycleOwner,
                            navController = navController,
                            settings = settings,
                            webAppUseCases = context.components.useCases.webAppUseCases,
                        ).also {
                            it.start()
                        }
                    }

                    subscribeToTabCollections(context, lifecycleOwner)
                    updateLastBrowseActivity(context)
                }

                Lifecycle.Event.ON_STOP -> {
//                    super.onStop()
                    initUIJob?.cancel()
                    currentStartDownloadDialog?.dismiss()

                    context.components.core.store.state.findTabOrCustomTabOrSelectedTab(
                        customTabSessionId
                    )?.let { session ->
                        // If we didn't enter PiP, exit full screen on stop
                        if (!session.content.pictureInPictureEnabled && fullScreenFeature.onBackPressed()) {
                            fullScreenChanged(false, context)
                        }
                    }

                    updateLastBrowseActivity(context)
                    updateHistoryMetadata(context)
                    pwaOnboardingObserver?.stop()
                }

                Lifecycle.Event.ON_PAUSE -> {
//                    super.onPause()
                    if (navController.currentDestination?.id != R.id.searchDialogFragment) {
                        view?.hideKeyboard()
                    }

                    context.components.services.appLinksInterceptor.updateFragmentManger(
                        fragmentManager = null,
                    )
                }

                Lifecycle.Event.ON_RESUME -> {
                    val components = context.components

                    val preferredColorScheme = components.core.getPreferredColorScheme()
                    if (components.core.engine.settings.preferredColorScheme != preferredColorScheme) {
                        components.core.engine.settings.preferredColorScheme = preferredColorScheme
                        components.useCases.sessionUseCases.reload()
                    }
                    hideToolbar(context)

                    components.services.appLinksInterceptor.updateFragmentManger(
                        fragmentManager = parentFragmentManager,
                    )
                    context?.settings()?.shouldOpenLinksInApp(customTabSessionId != null)
                        ?.let { openLinksInExternalApp ->
                            components.services.appLinksInterceptor.updateLaunchInApp {
                                openLinksInExternalApp
                            }
                        }

                    evaluateMessagesForMicrosurvey(components)

                    context.components.core.tabCollectionStorage.register(
                        collectionStorageObserver(context, navController, view),
                        lifecycleOwner,
                    )
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
    val sitePermissionFeature = remember { ViewBoundFeatureWrapper<SitePermissionsFeature>() }
    val pictureInPictureIntegration =
        remember { ViewBoundFeatureWrapper<PictureInPictureIntegration>() }
//    val swipeRefreshFeature = remember { ViewBoundFeatureWrapper<SwipeRefreshFeature>() }
//    val windowFeature = remember { ViewBoundFeatureWrapper<WindowFeature>() }
//    val webAuthnFeature = remember { ViewBoundFeatureWrapper<WebAuthnFeature>() }
//    val fullScreenMediaSessionFeature = remember {
//        ViewBoundFeatureWrapper<MediaSessionFullscreenFeature>()
//    }
    val lastTabFeature = remember { ViewBoundFeatureWrapper<LastTabFeature>() }
//    val screenOrientationFeature = remember { ViewBoundFeatureWrapper<ScreenOrientationFeature>() }
//    val thumbnailsFeature = remember { ViewBoundFeatureWrapper<BrowserThumbnails>() }
//    val readerViewFeature = remember { ViewBoundFeatureWrapper<ReaderViewIntegration>() }
//    val webExtToolbarFeature = remember { ViewBoundFeatureWrapper<WebExtensionToolbarFeature>() }

    /// views
    var engineView: EngineView? by remember { mutableStateOf(null) }
    var toolbar: BrowserToolbarCompat? by remember { mutableStateOf(null) }
    var findInPageBar: FindInPageBar? by remember { mutableStateOf(null) }
    var swipeRefresh: SwipeRefreshLayout? by remember { mutableStateOf(null) }
    var awesomeBar: AwesomeBarWrapper? by remember { mutableStateOf(null) }
    var readerViewBar: ReaderViewControlsBar? by remember { mutableStateOf(null) }
    var readerViewAppearanceButton: FloatingActionButton? by remember { mutableStateOf(null) }

    /// event handlers
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

    // on back pressed handlers
    BackHandler {
        onBackPressed(backHandler)
    }

    // moz components setup and shared preferences
    LaunchedEffect(engineView == null) {

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

    LaunchedEffect(Unit) {
        /* BaseBrowserFragment onViewCreated */
        // DO NOT ADD ANYTHING ABOVE THIS getProfilerTime CALL!
        val profilerStartTime = context.components.core.engine.profiler?.getProfilerTime()

        fun initializeUI() {
            val store = context.components.core.store
            val activity = context.getActivity()!! as HomeActivity

            // browser animations
//            browserAnimator = BrowserAnimator(
//                fragment = WeakReference(this),
//                engineView = WeakReference(engineView!!),
//                swipeRefresh = WeakReference(swipeRefresh!!),
//                viewLifecycleScope = WeakReference(coroutineScope),// viewLifecycleOwner.lifecycleScope),
//            ).apply {
//                beginAnimateInIfNecessary()
//            }

            val openInFenixIntent = Intent(context, IntentReceiverActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra(HomeActivity.OPEN_TO_BROWSER, true)
            }

            // todo: readerView
//            val readerMenuController = DefaultReaderModeController(
//                readerViewFeature,
//                binding.readerViewControlsBar,
//                isPrivate = activity.browsingModeManager.mode.isPrivate,
//                onReaderModeChanged = { activity.finishActionMode() },
//            )
            // todo: toolbar
//    val browserToolbarController = DefaultBrowserToolbarController(
//        store = store,
//        appStore = context.components.appStore,
//        tabsUseCases = context.components.useCases.tabsUseCases,
//        activity = activity,
//        settings = context.settings(),
//        navController = navController,
//        readerModeController = readerMenuController,
//        engineView = binding.engineView,
//        homeViewModel = homeViewModel,
//        customTabSessionId = customTabSessionId,
//        browserAnimator = browserAnimator,
//        onTabCounterClicked = {
//            onTabCounterClicked(activity.browsingModeManager.mode)
//        },
//        onCloseTab = { closedSession ->
//            val closedTab =
//                store.state.findTab(closedSession.id) ?: return@DefaultBrowserToolbarController
//            showUndoSnackbar(context.tabClosedUndoMessage(closedTab.content.private))
//        },
//    )
//            val browserToolbarMenuController = DefaultBrowserToolbarMenuController(
//                fragment = this,
//                store = store,
//                appStore = context.components.appStore,
//                activity = activity,
//                navController = navController,
//                settings = context.settings(),
//                readerModeController = readerMenuController,
//                sessionFeature = sessionFeature,
//                findInPageLauncher = { findInPageIntegration.withFeature { it.launch() } },
//                browserAnimator = browserAnimator,
//                customTabSessionId = customTabSessionId,
//                openInFenixIntent = openInFenixIntent,
//                bookmarkTapped = { url: String, title: String ->
//                    lifecycleOwner.lifecycleScope.launch {
//                        bookmarkTapped(url, title)
//                    }
//                },
//                scope = lifecycleOwner.lifecycleScope,
//                tabCollectionStorage = context.components.core.tabCollectionStorage,
//                topSitesStorage = context.components.core.topSitesStorage,
//                pinnedSiteStorage = context.components.core.pinnedSiteStorage,
//            )
////
//            setBrowserToolbarInteractor(
//                DefaultBrowserToolbarInteractor(
//                    browserToolbarController,
//                    browserToolbarMenuController,
//                )
//            )
//
//    _browserToolbarView = BrowserToolbarView(
//        context = context,
//        container = binding.browserLayout,
//        snackbarParent = binding.dynamicSnackbarContainer,
//        settings = context.settings(),
//        interactor = browserToolbarInteractor,
//        customTabSession = customTabSessionId?.let { store.state.findCustomTab(it) },
//        lifecycleOwner = viewLifecycleOwner,
//        tabStripContent = {
//            FirefoxTheme {
//                TabStrip(
//                    onAddTabClick = {
//                        navController.navigate(
//                            NavGraphDirections.actionGlobalHome(
//                                focusOnAddressBar = true,
//                            ),
//                        )
//                    },
//                    onLastTabClose = { isPrivate ->
//                        context.components.appStore.dispatch(
//                            AppAction.TabStripAction.UpdateLastTabClosed(isPrivate),
//                        )
//                        navController.navigate(
//                            BrowserComponentWrapperFragmentDirections.actionGlobalHome(),
//                        )
//                    },
//                    onSelectedTabClick = {},
//                    onCloseTabClick = { isPrivate ->
//                        showUndoSnackbar(context.tabClosedUndoMessage(isPrivate))
//                    },
//                    onPrivateModeToggleClick = { mode ->
//                        activity.browsingModeManager.mode = mode
//                        navController.navigate(
//                            BrowserComponentWrapperFragmentDirections.actionGlobalHome(),
//                        )
//                    },
//                    onTabCounterClick = {
////                            onTabCounterClicked(activity.browsingModeManager.mode)
//                    },
//                )
//            }
//        },
//    )

            // todo: login bars
//    loginBarsIntegration = LoginBarsIntegration(
//        loginsBar = binding.loginSelectBar,
//        passwordBar = binding.suggestStrongPasswordBar,
//        settings = context.settings(),
//        onLoginsBarShown = {
//            removeBottomToolbarDivider(browserToolbarView.view)
//            updateNavbarDivider()
//        },
//        onLoginsBarHidden = {
//            restoreBottomToolbarDivider(browserToolbarView.view)
//            updateNavbarDivider()
//        },
//    )

            // todo: toolbar
//    val shouldAddNavigationBar = context.shouldAddNavigationBar() // && webAppToolbarShouldBeVisible
//    if (shouldAddNavigationBar) {
//        initializeNavBar(
//            browserToolbar = browserToolbarView.view,
//            view = view,
//            context = context,
//            activity = activity,
//        )
//    }

            if (context.settings().microsurveyFeatureEnabled) {
                listenForMicrosurveyMessage(context, lifecycleOwner)
            }

            // todo: toolbar
//    toolbarIntegration.set(
//        feature = browserToolbarView.toolbarIntegration,
//        owner = lifecycleOwner,
//        view = view,
//    )
            // todo: findInPage
//    findInPageIntegration.set(
//        feature = com.shmibblez.inferno.components.FindInPageIntegration(
//            store = store,
//            appStore = context.components.appStore,
//            sessionId = customTabSessionId,
//            view = binding.findInPageView,
//            engineView = binding.engineView,
//            toolbarsHideCallback = {
//                expandBrowserView()
//            },
//            toolbarsResetCallback = {
//                onUpdateToolbarForConfigurationChange(browserToolbarView)
//                collapseBrowserView()
//            },
//        ),
//        owner = lifecycleOwner,
//        view = view,
//    )
//
//    findInPageBinding.set(
//        feature = FindInPageBinding(
//            appStore = context.components.appStore,
//            onFindInPageLaunch = { findInPageIntegration.withFeature { it.launch() } },
//        ),
//        owner = lifecycleOwner,
//        view = view,
//    )

            // todo: readerView
//    readerViewBinding.set(
//        feature = ReaderViewBinding(
//            appStore = context.components.appStore,
//            readerMenuController = readerMenuController,
//        ),
//        owner = lifecycleOwner,
//        view = view,
//    )

            // todo: open in firefox
//    openInFirefoxBinding.set(
//        feature = OpenInFirefoxBinding(
//            activity = activity,
//            appStore = context.components.appStore,
//            customTabSessionId = customTabSessionId,
//            customTabsUseCases = context.components.useCases.customTabsUseCases,
//            openInFenixIntent = openInFenixIntent,
//            sessionFeature = sessionFeature,
//        ),
//        owner = lifecycleOwner,
//        view = view,
//    )

            // todo: toolbar
//    browserToolbarView.view.display.setOnSiteSecurityClickedListener {
//        showQuickSettingsDialog()
//    }

            // todo: context menu
//    contextMenuFeature.set(
//        feature = ContextMenuFeature(
//            fragmentManager = parentFragmentManager,
//            store = store,
//            candidates = getContextMenuCandidates(context, binding.dynamicSnackbarContainer),
//            engineView = binding.engineView,
//            useCases = context.components.useCases.contextMenuUseCases,
//            tabId = customTabSessionId,
//        ),
//        owner = lifecycleOwner,
//        view = view,
//    )

            // todo: snackbar
//    snackbarBinding.set(
//        feature = SnackbarBinding(
//            context = context,
//            browserStore = context.components.core.store,
//            appStore = context.components.appStore,
//            snackbarDelegate = FenixSnackbarDelegate(binding.dynamicSnackbarContainer),
//            navController = navController,
//            sendTabUseCases = SendTabUseCases(context.components.backgroundServices.accountManager),
//            customTabSessionId = customTabSessionId,
//        ),
//        owner = lifecycleOwner,
//        view = view,
//    )
//
//    standardSnackbarErrorBinding.set(
//        feature = StandardSnackbarErrorBinding(
//            context.getActivity()!!,
//            binding.dynamicSnackbarContainer,
//            context.getActivity()!!.components.appStore,
//        ),
//        owner = viewLifecycleOwner,
//        view = binding.root,
//    )

            val allowScreenshotsInPrivateMode = context.settings().allowScreenshotsInPrivateMode
            // todo: secure window
            secureWindowFeature.set(
                feature = SecureWindowFeature(
                    window = context.getActivity()!!.window,
                    store = store,
                    customTabId = customTabSessionId,
                    isSecure = { !allowScreenshotsInPrivateMode && it.content.private },
                    clearFlagOnStop = false,
                ),
                owner = lifecycleOwner, // this,
                view = view,
            )

            fullScreenMediaSessionFeature.set(
                feature = MediaSessionFullscreenFeature(
                    context.getActivity()!!,
                    context.components.core.store,
                    customTabSessionId,
                ),
                owner = lifecycleOwner, // this,
                view = view,
            )

            val shareDownloadFeature = ShareDownloadFeature(
                context = context.applicationContext,
                httpClient = context.components.core.client,
                store = store,
                tabId = customTabSessionId,
            )

            val copyDownloadFeature = CopyDownloadFeature(
                context = context.applicationContext,
                httpClient = context.components.core.client,
                store = store,
                tabId = customTabSessionId,
                onCopyConfirmation = {
                    showSnackbarForClipboardCopy()
                },
            )

            val downloadFeature = DownloadsFeature(
                context.applicationContext,
                store = store,
                useCases = context.components.useCases.downloadUseCases,
                // todo: test since using parent frag manager
                fragmentManager = parentFragmentManager, // childFragmentManager,
                tabId = customTabSessionId,
                downloadManager = FetchDownloadManager(
                    context.applicationContext,
                    store,
                    DownloadService::class,
                    notificationsDelegate = context.components.notificationsDelegate,
                ),
                shouldForwardToThirdParties = {
                    PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                        context.getPreferenceKey(R.string.pref_key_external_download_manager),
                        false,
                    )
                },
                promptsStyling = DownloadsFeature.PromptsStyling(
                    gravity = Gravity.BOTTOM,
                    shouldWidthMatchParent = true,
                    positiveButtonBackgroundColor = ThemeManager.resolveAttribute(
                        R.attr.accent,
                        context,
                    ),
                    positiveButtonTextColor = ThemeManager.resolveAttribute(
                        R.attr.textOnColorPrimary,
                        context,
                    ),
                    positiveButtonRadius = ComponentDimens.TAB_CORNER_RADIUS.value,
                ),
                onNeedToRequestPermissions = { permissions ->
                    // todo: request permissions
//            requestPermissions(permissions, REQUEST_CODE_DOWNLOAD_PERMISSIONS)
                },
                customFirstPartyDownloadDialog = { filename, contentSize, positiveAction, negativeAction ->
                    run {
                        if (currentStartDownloadDialog == null) {
                            // todo: download dialog
//                            FirstPartyDownloadDialog(
//                                activity = context.getActivity()!!,
//                                filename = filename.value,
//                                contentSize = contentSize.value,
//                                positiveButtonAction = positiveAction.value,
//                                negativeButtonAction = negativeAction.value,
//                            ).onDismiss {
//                                currentStartDownloadDialog = null
//                            }.show(binding.startDownloadDialogContainer).also {
//                                currentStartDownloadDialog = it
//                            }
                        }
                    }
                },
                customThirdPartyDownloadDialog = { downloaderApps, onAppSelected, negativeActionCallback ->
                    run {
                        if (currentStartDownloadDialog == null) {
                            // todo: download dialog
//                            ThirdPartyDownloadDialog(
//                                activity = context.getActivity()!!,
//                                downloaderApps = downloaderApps.value,
//                                onAppSelected = onAppSelected.value,
//                                negativeButtonAction = negativeActionCallback.value,
//                            ).onDismiss {
//                                currentStartDownloadDialog = null
//                            }.show(binding.startDownloadDialogContainer).also {
//                                currentStartDownloadDialog = it
//                            }
                        }
                    }
                },
            )

            val passwordGeneratorColorsProvider = PasswordGeneratorDialogColorsProvider {
                PasswordGeneratorDialogColors(
                    title = ThemeManager.resolveAttributeColor(attribute = R.attr.textPrimary),
                    description = ThemeManager.resolveAttributeColor(attribute = R.attr.textSecondary),
                    background = ThemeManager.resolveAttributeColor(attribute = R.attr.layer1),
                    cancelText = ThemeManager.resolveAttributeColor(attribute = R.attr.textAccent),
                    confirmButton = ThemeManager.resolveAttributeColor(attribute = R.attr.actionPrimary),
                    passwordBox = ThemeManager.resolveAttributeColor(attribute = R.attr.layer2),
                    boxBorder = ThemeManager.resolveAttributeColor(attribute = R.attr.textDisabled),
                )
            }

            val bottomToolbarHeight = context.settings().getBottomToolbarHeight(context)

            downloadFeature.onDownloadStopped = { downloadState, _, downloadJobStatus ->
                // todo: dialogs (in below function)
                // todo: toolbar
//                handleOnDownloadFinished(
//                    context = context,
//                    downloadState = downloadState,
//                    downloadJobStatus = downloadJobStatus,
//                    tryAgain = downloadFeature::tryAgain,
//                    browserToolbars = listOfNotNull(
//                        browserToolbarView,
//                        _bottomToolbarContainerView?.toolbarContainerView,
//                    ),
//                )
            }

            resumeDownloadDialogState(
                getCurrentTab(context)?.id,
                store,
                context,
            )

            shareDownloadsFeature.set(
                shareDownloadFeature,
                owner = lifecycleOwner,
                view = view,
            )

            copyDownloadsFeature.set(
                copyDownloadFeature,
                owner = lifecycleOwner,
                view = view,
            )

            downloadsFeature.set(
                downloadFeature,
                owner = lifecycleOwner,
                view = view,
            )

            pipFeature = PictureInPictureFeature(
                store = store,
                activity = context.getActivity()!!,
                crashReporting = context.components.crashReporter, // context.components.analytics.crashReporter,
                tabId = customTabSessionId,
            )

            biometricPromptFeature.set(
                feature = BiometricPromptFeature(
                    context = context,
                    fragment = view.findFragment(),
                    onAuthFailure = {
                        promptsFeature.get()?.onBiometricResult(isAuthenticated = false)
                    },
                    onAuthSuccess = {
                        promptsFeature.get()?.onBiometricResult(isAuthenticated = true)
                    },
                ),
                owner = lifecycleOwner,
                view = view,
            )

            val colorsProvider = DialogColorsProvider {
                DialogColors(
                    title = ThemeManager.resolveAttributeColor(attribute = R.attr.textPrimary),
                    description = ThemeManager.resolveAttributeColor(attribute = R.attr.textSecondary),
                )
            }

            promptsFeature.set(
                feature = PromptFeature(
                    activity = activity,
                    store = store,
                    customTabId = customTabSessionId,
                    fragmentManager = parentFragmentManager,
                    identityCredentialColorsProvider = colorsProvider,
                    tabsUseCases = context.components.useCases.tabsUseCases,
                    fileUploadsDirCleaner = context.components.core.fileUploadsDirCleaner,
                    creditCardValidationDelegate = DefaultCreditCardValidationDelegate(
                        context.components.core.lazyAutofillStorage,
                    ),
                    loginValidationDelegate = DefaultLoginValidationDelegate(
                        context.components.core.lazyPasswordsStorage,
                    ),
                    isLoginAutofillEnabled = {
                        context.settings().shouldAutofillLogins
                    },
                    isSaveLoginEnabled = {
                        context.settings().shouldPromptToSaveLogins
                    },
                    isCreditCardAutofillEnabled = {
                        context.settings().shouldAutofillCreditCardDetails
                    },
                    isAddressAutofillEnabled = {
                        context.settings().addressFeature && context.settings().shouldAutofillAddressDetails
                    },
                    loginExceptionStorage = context.components.core.loginExceptionStorage,
                    shareDelegate = object : ShareDelegate {
                        override fun showShareSheet(
                            context: Context,
                            shareData: ShareData,
                            onDismiss: () -> Unit,
                            onSuccess: () -> Unit,
                        ) {
                            val directions = NavGraphDirections.actionGlobalShareFragment(
                                data = arrayOf(shareData),
                                showPage = true,
                                sessionId = getCurrentTab(context)?.id,
                            )
                            navController.navigate(directions)
                        }
                    },
                    onNeedToRequestPermissions = { permissions ->
                        // todo: permissions
//                        requestPermissions(permissions, REQUEST_CODE_PROMPT_PERMISSIONS)
                    },
                    loginDelegate = object : LoginDelegate {
                        // todo: login delegate
//                        override val loginPickerView
//                            get() = binding.loginSelectBar
//                        override val onManageLogins = {
//                            browserAnimator.captureEngineViewAndDrawStatically {
//                                val directions = NavGraphDirections.actionGlobalSavedLoginsAuthFragment()
//                                navController.navigate(directions)
//                            }
//                        }
                    },
                    suggestStrongPasswordDelegate = object : SuggestStrongPasswordDelegate {
                        // todo: password delegate
//                        override val strongPasswordPromptViewListenerView
//                            get() = binding.suggestStrongPasswordBar
                    },
                    shouldAutomaticallyShowSuggestedPassword = { context.settings().isFirstTimeEngagingWithSignup },
                    onFirstTimeEngagedWithSignup = {
                        context.settings().isFirstTimeEngagingWithSignup = false
                    },
                    onSaveLoginWithStrongPassword = { url, password ->
                        handleOnSaveLoginWithGeneratedStrongPassword(
                            passwordsStorage = context.components.core.passwordsStorage,
                            url = url,
                            password = password,
                            lifecycleScope = coroutineScope,
                            setLastSavedGeneratedPassword,
                        )
                    },
                    onSaveLogin = { isUpdate ->
                        showSnackbarAfterLoginChange(
                            isUpdate,
                        )
                    },
                    passwordGeneratorColorsProvider = passwordGeneratorColorsProvider,
                    hideUpdateFragmentAfterSavingGeneratedPassword = { username, password ->
                        hideUpdateFragmentAfterSavingGeneratedPassword(
                            username,
                            password,
                            lastSavedGeneratedPassword,
                        )
                    },
                    removeLastSavedGeneratedPassword = { removeLastSavedGeneratedPassword(setLastSavedGeneratedPassword) },
                    creditCardDelegate = object : CreditCardDelegate {
                        // todo: credit card delegate
//                        override val creditCardPickerView
//                            get() = binding.creditCardSelectBar
                        override val onManageCreditCards = {
                            val directions =
                                NavGraphDirections.actionGlobalAutofillSettingFragment()
                            navController.navigate(directions)
                        }
                        override val onSelectCreditCard = {
                            showBiometricPrompt(context, biometricPromptFeature, promptsFeature)
                        }
                    },
                    addressDelegate = object : AddressDelegate {
                        // todo: address delegate
                        override val addressPickerView
                            // todo: address select bar
                            get() = AddressSelectBar(context) // binding.addressSelectBar
                        override val onManageAddresses = {
                            val directions =
                                NavGraphDirections.actionGlobalAutofillSettingFragment()
                            navController.navigate(directions)
                        }
                    },
                    androidPhotoPicker = AndroidPhotoPicker(
                        context,
                        singleMediaPicker,
                        multipleMediaPicker,
                    ),
                ),
                owner = lifecycleOwner,
                view = view,
            )

            sessionFeature.set(
                feature = SessionFeature(
                    context.components.core.store,
                    context.components.useCases.sessionUseCases.goBack,
                    context.components.useCases.sessionUseCases.goForward,
                    engineView!!, // binding.engineView,
                    customTabSessionId,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            // todo: crash content integration
//            crashContentIntegration.set(
//                feature = CrashContentIntegration(
//                    context = context,
//                    browserStore = context.components.core.store,
//                    appStore = context.components.appStore,
//                    toolbar = browserToolbarView.view,
//                    crashReporterView = binding.crashReporterView,
//                    components = context.components,
//                    settings = context.settings(),
//                    navController = navController,
//                    sessionId = customTabSessionId,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )

            searchFeature.set(
                feature = SearchFeature(store, customTabSessionId) { request, tabId ->
                    val parentSession = store.state.findTabOrCustomTab(tabId)
                    val useCase = if (request.isPrivate) {
                        context.components.useCases.searchUseCases.newPrivateTabSearch
                    } else {
                        context.components.useCases.searchUseCases.newTabSearch
                    }

                    if (parentSession is CustomTabSessionState) {
                        useCase.invoke(request.query)
                        context.getActivity()!!.startActivity(openInFenixIntent)
                    } else {
                        useCase.invoke(request.query, parentSessionId = parentSession?.id)
                    }
                },
                owner = lifecycleOwner,
                view = view,
            )

            val accentHighContrastColor =
                ThemeManager.resolveAttribute(R.attr.actionPrimary, context)

            sitePermissionsFeature.set(
                feature = SitePermissionsFeature(
                    context = context,
                    storage = context.components.core.geckoSitePermissionsStorage,
                    fragmentManager = parentFragmentManager,
                    promptsStyling = SitePermissionsFeature.PromptsStyling(
                        gravity = getAppropriateLayoutGravity(context),
                        shouldWidthMatchParent = true,
                        positiveButtonBackgroundColor = accentHighContrastColor,
                        positiveButtonTextColor = R.color.fx_mobile_text_color_action_primary,
                    ),
                    sessionId = customTabSessionId,
                    onNeedToRequestPermissions = { permissions ->
                        // todo: request permissions
//                        requestPermissions(permissions, REQUEST_CODE_APP_PERMISSIONS)
                    },
                    onShouldShowRequestPermissionRationale = {
                        // todo: permissions
                        false
//                        shouldShowRequestPermissionRationale(
//                            it,
//                        )
                    },
                    store = store,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            sitePermissionWifiIntegration.set(
                feature = SitePermissionsWifiIntegration(
                    settings = context.settings(),
                    wifiConnectionMonitor = context.components.wifiConnectionMonitor,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            // This component feature only works on Fenix when built on Mozilla infrastructure.
            if (BuildConfig.MOZILLA_OFFICIAL) {
                webAuthnFeature.set(
                    feature = WebAuthnFeature(
                        engine = context.components.core.engine,
                        activity = context.getActivity()!!,
                        exitFullScreen = context.components.useCases.sessionUseCases.exitFullscreen::invoke,
                        currentTab = { store.state.selectedTabId },
                    ),
                    owner = lifecycleOwner,
                    view = view,
                )
            }

            screenOrientationFeature.set(
                feature = ScreenOrientationFeature(
                    engine = context.components.core.engine,
                    activity = context.getActivity()!!,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            // todo: site permissions
//            context.settings().setSitePermissionSettingListener(viewLifecycleOwner) {
//                // If the user connects to WIFI while on the BrowserFragment, this will update the
//                // SitePermissionsRules (specifically autoplay) accordingly
//                runIfFragmentIsAttached {
//                    assignSitePermissionsRules()
//                }
//            }
            assignSitePermissionsRules(context, sitePermissionFeature)

            fullScreenFeature.set(
                feature = FullScreenFeature(
                    context.components.core.store,
                    context.components.useCases.sessionUseCases,
                    customTabSessionId,
                    { viewportFitChange(it, context) },
                        { fullScreenChanged(false, context) },
                ),
                owner = lifecycleOwner,
                view = view,
            )

            closeFindInPageBarOnNavigation(store, lifecycleOwner, context, coroutineScope, findInPageIntegration)

            store.flowScoped(lifecycleOwner) { flow ->
                flow.mapNotNull { state -> state.findTabOrCustomTabOrSelectedTab(customTabSessionId) }
                    .distinctUntilChangedBy { tab -> tab.content.pictureInPictureEnabled }
                    .collect { tab -> pipModeChanged(tab, context, backHandler) }
            }

            // todo: swipe refresh
//            binding.swipeRefresh.isEnabled = shouldPullToRefreshBeEnabled(false)
//
//            if (binding.swipeRefresh.isEnabled) {
//                val primaryTextColor = ThemeManager.resolveAttribute(R.attr.textPrimary, context)
//                val primaryBackgroundColor = ThemeManager.resolveAttribute(R.attr.layer2, context)
//                binding.swipeRefresh.apply {
//                    setColorSchemeResources(primaryTextColor)
//                    setProgressBackgroundColorSchemeResource(primaryBackgroundColor)
//                }
//                swipeRefreshFeature.set(
//                    feature = SwipeRefreshFeature(
//                        context.components.core.store,
//                        context.components.useCases.sessionUseCases.reload,
//                        binding.swipeRefresh,
//                        { },
//                        customTabSessionId,
//                    ),
//                    owner = lifecycleOwner,
//                    view = view,
//                )
//            }

            webchannelIntegration.set(
                feature = FxaWebChannelIntegration(
                    customTabSessionId = customTabSessionId,
                    runtime = context.components.core.engine,
                    store = context.components.core.store,
                    accountManager = context.components.backgroundServices.accountManager,
                    serverConfig = context.components.backgroundServices.serverConfig,
                    activityRef = WeakReference(context.getActivity()),
                ),
                owner = lifecycleOwner,
                view = view,
            )

            initializeEngineView(
                topToolbarHeight = context.settings().getTopToolbarHeight(
                    includeTabStrip = customTabSessionId == null && context.isTabStripEnabled(),
                ),
                bottomToolbarHeight = bottomToolbarHeight,
                context = context,
            )

            initializeMicrosurveyFeature(context, lifecycleOwner, messagingFeatureMicrosurvey)

            // TODO: super
//    super.initializeUI(view, tab)

            /* super */
            // TODO
//    val tab = getCurrentTab()
//    browserInitialized = if (tab != null) {
//        initializeUI(view, tab)
//        true
//    } else {
//        false
//    }
            /* super */

            val components = context.components

            if (!context.isTabStripEnabled() && context.settings().isSwipeToolbarToSwitchTabsEnabled) {
                // todo: tab gestures
//        binding.gestureLayout.addGestureListener(
//            ToolbarGestureHandler(
//                activity = context.getActivity()!!,
//                contentLayout = binding.browserLayout,
//                tabPreview = binding.tabPreview,
//                toolbarLayout = browserToolbarView.view,
//                store = components.core.store,
//                selectTabUseCase = components.useCases.tabsUseCases.selectTab,
//                onSwipeStarted = {
//                    thumbnailsFeature.get()?.requestScreenshot()
//                },
//            ),
//        )
            }

            updateBrowserToolbarLeadingAndNavigationActions(
                context = context,
                redesignEnabled = context.settings().navigationToolbarEnabled,
                isLandscape = context.isLandscape(),
                isTablet = com.shmibblez.inferno.ext.isLargeWindow(context),
                isPrivate = (context.getActivity()!! as HomeActivity).browsingModeManager.mode.isPrivate,
                feltPrivateBrowsingEnabled = context.settings().feltPrivateBrowsingEnabled,
                isWindowSizeSmall = true, // AcornWindowSize.getWindowSize(context) == AcornWindowSize.Small,
            )

            updateBrowserToolbarMenuVisibility()

            initReaderMode(context, view)
            initTranslationsAction(context, view, browserToolbarInteractor!!, translationsAvailable.value)
            initReviewQualityCheck(context, lifecycleOwner, view, navController, setReviewQualityCheckAvailable, reviewQualityCheckAvailable, reviewQualityCheckFeature )
            initSharePageAction(context, browserToolbarInteractor)
            initReloadAction(context)

            thumbnailsFeature.set(
                feature = BrowserThumbnails(context, engineView!!, components.core.store),
                owner = lifecycleOwner,
                view = view,
            )

            windowFeature.set(
                feature = WindowFeature(
                    store = components.core.store,
                    tabsUseCases = components.useCases.tabsUseCases,
                ),
                owner = lifecycleOwner,
                view = view,
            )

//        if (context.settings().shouldShowOpenInAppCfr) {
//            openInAppOnboardingObserver.set(
//                feature = OpenInAppOnboardingObserver(
//                    context = context,
//                    store = context.components.core.store,
//                    lifecycleowner = lifecycleOwner,
//                    navController = navController,
//                    settings = context.settings(),
//                    appLinksUseCases = context.components.useCases.appLinksUseCases,
//                    container = binding.browserLayout as ViewGroup,
//                    shouldScrollWithTopToolbar = !context.settings().shouldUseBottomToolbar,
//                ),
//                owner = lifecycleOwner,
//                view = view,
//            )
//        }
        }
        initializeUI()

        if (customTabSessionId == null) {
            // We currently only need this observer to navigate to home
            // in case all tabs have been removed on startup. No need to
            // this if we have a known session to display.
            observeRestoreComplete(context.components.core.store, context, lifecycleOwner, coroutineScope, navController)
        }

        observeTabSelection(context.components.core.store, context, lifecycleOwner, coroutineScope, currentStartDownloadDialog, browserInitialized)

        if (!context.components.fenixOnboarding.userHasBeenOnboarded()) {
            observeTabSource(context.components.core.store, context, lifecycleOwner, coroutineScope)
        }

        // todo: accessibility
//        context.accessibilityManager.addAccessibilityStateChangeListener(view) // this)

        context.components.backgroundServices.closeSyncedTabsCommandReceiver.register(
            observer = CloseLastSyncedTabObserver(
                scope = coroutineScope, // viewLifecycleOwner.lifecycleScope,
                navController = navController,
            ),
            view = view,
        )

        // DO NOT MOVE ANYTHING BELOW THIS addMarker CALL!
        context.components.core.engine.profiler?.addMarker(
            MarkersFragmentLifecycleCallbacks.MARKER_NAME,
            profilerStartTime,
            "BaseBrowserFragment.onViewCreated",
        )/* BaseBrowserFragment onViewCreated */
    }

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        content = { paddingValues ->
            MozAwesomeBar(setView = { ab -> awesomeBar = ab })
            if (pageType == BrowserComponentPageType.ENGINE) {
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
            }
            if (pageType == BrowserComponentPageType.HOME_PRIVATE) {
                HomeComponent(private = true)
            }
            if (pageType == BrowserComponentPageType.HOME) {
                HomeComponent(private = false)
            }
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

fun hideToolbar(context: Context) {
    (context.getActivity()!! as AppCompatActivity).supportActionBar?.hide()
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

data class onBackPressedHandler(
    val context: Context,
    val readerViewFeature: ViewBoundFeatureWrapper<ReaderViewFeature>,
    val findInPageIntegration: ViewBoundFeatureWrapper<com.shmibblez.inferno.components.FindInPageIntegration>,
    val fullScreenFeature: ViewBoundFeatureWrapper<FullScreenFeature>,
    val promptsFeature: ViewBoundFeatureWrapper<PromptFeature>,
    val currentStartDownloadDialog: StartDownloadDialog,
    val sessionFeature: ViewBoundFeatureWrapper<SessionFeature>,
)

// combines Moz BrowserFragment and Moz BaseBrowserFragment implementations
private fun onBackPressed(
    onBackPressedHandler: onBackPressedHandler
): Boolean {
    with(onBackPressedHandler) {
        return readerViewFeature.onBackPressed() || findInPageIntegration.onBackPressed() || fullScreenFeature.onBackPressed() || promptsFeature.onBackPressed() || currentStartDownloadDialog?.let {
            it.dismiss()
            true
        } ?: false || sessionFeature.onBackPressed() || removeSessionIfNeeded(context)
    }
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

/* new functions *//* BaseBrowserFragment funs */
private fun getFragment(view: View): Fragment {
//    return this
    return view.findFragment()
}

private fun getPromptsFeature(promptsFeature: ViewBoundFeatureWrapper<PromptFeature>): PromptFeature? {
    return promptsFeature.get()
}


private fun initializeUI(view: View, context: Context, setBrowserInitialized: (Boolean)-> Unit) {
    val tab = getCurrentTab(context)
    setBrowserInitialized(
        if (tab != null) {
            initializeUI(view, context, setBrowserInitialized)// tab, context)
            true
        } else {
            false
        }
    )
}

@Suppress("ComplexMethod", "LongMethod", "DEPRECATION")
// https://github.com/mozilla-mobile/fenix/issues/19920
@CallSuper
internal fun initializeUI(
    view: View,
    tab: SessionState,
    context: Context,
    navController: NavController,
    lifecycleOwner: LifecycleOwner,
    readerViewFeature: ViewBoundFeatureWrapper<ReaderViewIntegration>,
) {

}

private fun showUndoSnackbar(message: String, context: Context, lifecycleOwner: LifecycleOwner) {
    // todo: snackbar
//    lifecycleOwner.lifecycleScope.allowUndo(
//        binding.dynamicSnackbarContainer,
//        message,
//        context.getString(R.string.snackbar_deleted_undo),
//        {
//            context.components.useCases.tabsUseCases.undo.invoke()
//        },
//        operation = { },
//    )
}

/**
 * Show a [Snackbar] when data is set to the device clipboard. To avoid duplicate displays of
 * information only show a [Snackbar] for Android 12 and lower.
 *
 * [See details](https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#duplicate-notifications).
 */
private fun showSnackbarForClipboardCopy() {
    // todo: snackbar
//    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
//        ContextMenuSnackbarDelegate().show(
//            snackBarParentView = binding.dynamicSnackbarContainer,
//            text = R.string.snackbar_copy_image_to_clipboard_confirmation,
//            duration = LENGTH_LONG,
//        )
//    }
}

/**
 * Show a [Snackbar] when credentials are saved or updated.
 */
private fun showSnackbarAfterLoginChange(isUpdate: Boolean) {
    val snackbarText = if (isUpdate) {
        R.string.mozac_feature_prompt_login_snackbar_username_updated
    } else {
        R.string.mozac_feature_prompts_suggest_strong_password_saved_snackbar_title
    }
    // todo: snackbar
//    ContextMenuSnackbarDelegate().show(
//        snackBarParentView = binding.dynamicSnackbarContainer,
//        text = snackbarText,
//        duration = LENGTH_LONG,
//    )
}

/**
 * Shows a biometric prompt and fallback to prompting for the password.
 */
private fun showBiometricPrompt(
    context: Context, biometricPromptFeature: ViewBoundFeatureWrapper<BiometricPromptFeature>,
    promptFeature: ViewBoundFeatureWrapper<PromptFeature>
) {
    if (BiometricPromptFeature.canUseFeature(context)) {
        biometricPromptFeature.get()
            ?.requestAuthentication(context.getString(R.string.credit_cards_biometric_prompt_unlock_message_2))
        return
    }

    // Fallback to prompting for password with the KeyguardManager
    val manager = context.getSystemService<KeyguardManager>()
    if (manager?.isKeyguardSecure == true) {
        showPinVerification(manager, context)
    } else {
        // Warn that the device has not been secured
        if (context.settings().shouldShowSecurityPinWarning) {
            showPinDialogWarning(context, promptFeature)
        } else {
            promptFeature.get()?.onBiometricResult(isAuthenticated = true)
        }
    }
}

/**
 * Shows a pin request prompt. This is only used when BiometricPrompt is unavailable.
 */
@Suppress("DEPRECATION")
private fun showPinVerification(manager: KeyguardManager, context: Context) {
    val intent = manager.createConfirmDeviceCredentialIntent(
        context.getString(R.string.credit_cards_biometric_prompt_message_pin),
        context.getString(R.string.credit_cards_biometric_prompt_unlock_message_2),
    )

    // todo: start for result
//    startForResult.launch(intent)
}

/**
 * Shows a dialog warning about setting up a device lock PIN.
 */
private fun showPinDialogWarning(
    context: Context, promptsFeature: ViewBoundFeatureWrapper<PromptFeature>
) {
    AlertDialog.Builder(context).apply {
        setTitle(context.getString(R.string.credit_cards_warning_dialog_title_2))
        setMessage(context.getString(R.string.credit_cards_warning_dialog_message_3))

        setNegativeButton(context.getString(R.string.credit_cards_warning_dialog_later)) { _: DialogInterface, _ ->
            promptsFeature.get()?.onBiometricResult(isAuthenticated = false)
        }

        setPositiveButton(context.getString(R.string.credit_cards_warning_dialog_set_up_now)) { it: DialogInterface, _ ->
            it.dismiss()
            promptsFeature.get()?.onBiometricResult(isAuthenticated = false)
            context.getActivity()?.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
        }

        create()
    }.show().withCenterAlignedButtons().secure(context.getActivity())

    context.settings().incrementSecureWarningCount()
}

private fun closeFindInPageBarOnNavigation(store: BrowserStore, lifecycleOwner:LifecycleOwner, context: Context, coroutineScope: CoroutineScope, findInPageIntegration: ViewBoundFeatureWrapper<com.shmibblez.inferno.components.FindInPageIntegration>,customTabSessionId: String? = null,) {
    consumeFlow(store, lifecycleOwner, context, coroutineScope) { flow ->
        flow.mapNotNull { state ->
            state.findCustomTabOrSelectedTab(customTabSessionId)
        }.ifAnyChanged { tab ->
            arrayOf(tab.content.url, tab.content.loadRequest)
        }.collect {
            findInPageIntegration.onBackPressed()
        }
    }
}

/**
 * Preserves current state of the [DynamicDownloadDialog] to persist through tab changes and
 * other fragments navigation.
 * */
internal fun saveDownloadDialogState(
    sessionId: String?,
    downloadState: DownloadState,
    downloadJobStatus: DownloadState.Status,
) {
    sessionId?.let { id ->
        // todo: download
//        sharedViewModel.downloadDialogState[id] = Pair(
//            downloadState,
//            downloadJobStatus == DownloadState.Status.FAILED,
//        )
    }
}

/**
 * Re-initializes [DynamicDownloadDialog] if the user hasn't dismissed the dialog
 * before navigating away from it's original tab.
 * onTryAgain it will use [ContentAction.UpdateDownloadAction] to re-enqueue the former failed
 * download, because [DownloadsFeature] clears any queued downloads onStop.
 * */
@VisibleForTesting
internal fun resumeDownloadDialogState(
    sessionId: String?,
    store: BrowserStore,
    context: Context,
) {
    // todo: download
//    val savedDownloadState = sharedViewModel.downloadDialogState[sessionId]
//
//    if (savedDownloadState == null || sessionId == null) {
//        binding.viewDynamicDownloadDialog.root.visibility = View.GONE
//        return
//    }
//
//    val onTryAgain: (String) -> Unit = {
//        savedDownloadState.first?.let { dlState ->
//            store.dispatch(
//                ContentAction.UpdateDownloadAction(
//                    sessionId,
//                    dlState.copy(skipConfirmation = true),
//                ),
//            )
//        }
//    }

    // todo: dismiss
//    val onDismiss: () -> Unit = { sharedViewModel.downloadDialogState.remove(sessionId) }

//    DynamicDownloadDialog(
//        context = context,
//        downloadState = savedDownloadState.first,
//        didFail = savedDownloadState.second,
//        tryAgain = onTryAgain,
//        onCannotOpenFile = {
//            showCannotOpenFileError(binding.dynamicSnackbarContainer, context, it)
//        },
//        binding = binding.viewDynamicDownloadDialog,
//        onDismiss = onDismiss,
//    ).show()

//    browserToolbarView.expand()
}

@VisibleForTesting
internal fun shouldPullToRefreshBeEnabled(inFullScreen: Boolean, context: Context): Boolean {
    return /* FeatureFlags.pullToRefreshEnabled && */ context.settings().isPullToRefreshEnabledInBrowser && !inFullScreen
}

/**
 * Sets up the necessary layout configurations for the engine view. If the toolbar is dynamic, this method sets a
 * [CoordinatorLayout.Behavior] that will adjust the top/bottom paddings when the tab content is being scrolled.
 * If the toolbar is not dynamic, it simply sets the top and bottom margins to ensure that content is always
 * displayed above or below the respective toolbars.
 *
 * @param topToolbarHeight The height of the top toolbar, which could be zero if the toolbar is positioned at the
 * bottom, or it could be equal to the height of [BrowserToolbar].
 * @param bottomToolbarHeight The height of the bottom toolbar, which could be equal to the height of
 * [BrowserToolbar] or [ToolbarContainerView], or zero if the toolbar is positioned at the top without a navigation
 * bar.
 */
@VisibleForTesting
internal fun initializeEngineView(
    topToolbarHeight: Int,
    bottomToolbarHeight: Int,
    context: Context,
) {
    if (isToolbarDynamic(context)) { // && webAppToolbarShouldBeVisible) {
        // todo: engine view
//        getEngineView().setDynamicToolbarMaxHeight(topToolbarHeight + bottomToolbarHeight)

        if (context.settings().navigationToolbarEnabled || shouldShowMicrosurveyPrompt(context)) {
            // todo: swipe refresh
//            (getSwipeRefreshLayout().layoutParams as CoordinatorLayout.LayoutParams).behavior =
//                EngineViewClippingBehavior(
//                    context = context,
//                    attrs = null,
//                    engineViewParent = getSwipeRefreshLayout(),
//                    topToolbarHeight = topToolbarHeight,
//                )
        } else {
            val toolbarPosition = when (context.settings().toolbarPosition) {
                ToolbarPosition.BOTTOM -> mozilla.components.ui.widgets.behavior.ToolbarPosition.BOTTOM
                ToolbarPosition.TOP -> mozilla.components.ui.widgets.behavior.ToolbarPosition.TOP
            }

            val toolbarHeight = when (toolbarPosition) {
                mozilla.components.ui.widgets.behavior.ToolbarPosition.BOTTOM -> bottomToolbarHeight
                mozilla.components.ui.widgets.behavior.ToolbarPosition.TOP -> topToolbarHeight
            }
            // todo: swipe refresh
//            (getSwipeRefreshLayout().layoutParams as CoordinatorLayout.LayoutParams).behavior =
//                mozilla.components.ui.widgets.behavior.EngineViewClippingBehavior(
//                    context,
//                    null,
//                    getSwipeRefreshLayout(),
//                    toolbarHeight,
//                    toolbarPosition,
//                )
        }
    } else {
        // todo: engine view
//        // Ensure webpage's bottom elements are aligned to the very bottom of the engineView.
//        getEngineView().setDynamicToolbarMaxHeight(0)
//
//        // Effectively place the engineView on top/below of the toolbars if that is not dynamic.
//        val swipeRefreshParams =
//            getSwipeRefreshLayout().layoutParams as CoordinatorLayout.LayoutParams
//        swipeRefreshParams.topMargin = topToolbarHeight
//        swipeRefreshParams.bottomMargin = bottomToolbarHeight
    }
}

@Suppress("LongMethod")
private fun initializeNavBar(
    browserToolbar: BrowserToolbar,
    view: View,
    context: Context,
    activity: HomeActivity,
) {
//        NavigationBar.browserInitializeTimespan.start()

    val isToolbarAtBottom = context.isToolbarAtBottom()

    // The toolbar view has already been added directly to the container.
    // We should remove it and add the view to the navigation bar container.
    // Should refactor this so there is no added view to remove to begin with:
    // https://bugzilla.mozilla.org/show_bug.cgi?id=1870976
    // todo: toolbar
//    if (isToolbarAtBottom) {
//        binding.browserLayout.removeView(browserToolbar)
//    }

    // todo: toolbar
//    _bottomToolbarContainerView = BottomToolbarContainerView(
//        context = context,
//        parent = binding.browserLayout,
//        hideOnScroll = isToolbarDynamic(context),
//        content = {
//            val areLoginBarsShown by remember { mutableStateOf(loginBarsIntegration.isVisible) }
//
//            FirefoxTheme {
//                Column(
//                    modifier = Modifier.background(FirefoxTheme.colors.layer1),
//                ) {
//                    if (!activity.isMicrosurveyPromptDismissed.value) {
//                        currentMicrosurvey?.let {
//                            if (isToolbarAtBottom) {
//                                removeBottomToolbarDivider(browserToolbar)
//                            }
//
//                            HorizontalDivider()
//
//                            MicrosurveyRequestPrompt(
//                                microsurvey = it,
//                                activity = activity,
//                                onStartSurveyClicked = {
//                                    context.components.appStore.dispatch(
//                                        MicrosurveyAction.Started(
//                                            it.id
//                                        )
//                                    )
//                                    navController.nav(
//                                        R.id.browserFragment,
//                                        BrowserComponentWrapperFragmentDirections.actionGlobalMicrosurveyDialog(
//                                            it.id
//                                        ),
//                                    )
//                                },
//                                onCloseButtonClicked = {
//                                    context.components.appStore.dispatch(
//                                        MicrosurveyAction.Dismissed(it.id),
//                                    )
//
//                                    context.settings().shouldShowMicrosurveyPrompt = false
//                                    activity.isMicrosurveyPromptDismissed.value = true
//
//                                    resumeDownloadDialogState(
//                                        getCurrentTab()?.id,
//                                        context.components.core.store,
//                                        context,
//                                    )
//                                },
//                            )
//                        }
//                    } else {
//                        restoreBottomToolbarDivider(browserToolbar)
//                    }
//
//                    if (isToolbarAtBottom) {
//                        AndroidView(factory = { _ -> browserToolbar })
//                    }
//
//                    NavigationButtonsCFR(
//                        context = context,
//                        activity = activity,
//                        showDivider = !isToolbarAtBottom && !areLoginBarsShown && (currentMicrosurvey == null || activity.isMicrosurveyPromptDismissed.value),
//                    )
//                }
//            }
//        },
//    )
//
//    bottomToolbarContainerIntegration.set(
//        feature = BottomToolbarContainerIntegration(
//            toolbar = bottomToolbarContainerView.toolbarContainerView,
//            store = context.components.core.store,
//            sessionId = customTabSessionId,
//        ),
//        owner = lifecycleOwner,
//        view = view,
//    )

//        NavigationBar.browserInitializeTimespan.stop()
}

@Suppress("LongMethod")
@Composable
internal fun NavigationButtonsCFR(
    context: Context,
    navController: NavController,
    activity: HomeActivity,
    showDivider: Boolean,
    browserToolbarInteractor: BrowserToolbarInteractor
) {
    var showCFR by remember { mutableStateOf(false) }
    val lastTimeNavigationButtonsClicked = remember { mutableLongStateOf(0L) }

    // todo: menu button
    // We need a second menu button, but we could reuse the existing builder.
//    val menuButton = MenuButton(context).apply {
//        menuBuilder = browserToolbarView.menuToolbar.menuBuilder
//        // We have to set colorFilter manually as the button isn't being managed by a [BrowserToolbarView].
//        setColorFilter(
//            getColor(
//                context,
//                ThemeManager.resolveAttribute(R.attr.textPrimary, context),
//            ),
//        )
//        recordClickEvent = { }
//    }
//    menuButton.setHighlightStatus()
//    _menuButtonView = menuButton

    CFRPopupLayout(
        showCFR = showCFR && context.settings().shouldShowNavigationButtonsCFR,
        properties = CFRPopupProperties(
            popupBodyColors = listOf(
                FirefoxTheme.colors.layerGradientEnd.toArgb(),
                FirefoxTheme.colors.layerGradientStart.toArgb(),
            ),
            dismissButtonColor = FirefoxTheme.colors.iconOnColor.toArgb(),
            indicatorDirection = CFRPopup.IndicatorDirection.DOWN,
            popupVerticalOffset = NAVIGATION_CFR_VERTICAL_OFFSET.dp,
            indicatorArrowStartOffset = NAVIGATION_CFR_ARROW_OFFSET.dp,
            popupAlignment = CFRPopup.PopupAlignment.BODY_TO_ANCHOR_START_WITH_OFFSET,
        ),
        onCFRShown = {
            context.settings().shouldShowNavigationButtonsCFR = false
            context.settings().lastCfrShownTimeInMillis = System.currentTimeMillis()
        },
        onDismiss = {},
        text = {
            FirefoxTheme {
                Text(
                    text = stringResource(R.string.navbar_navigation_buttons_cfr_message),
                    color = FirefoxTheme.colors.textOnColorPrimary,
                    style = FirefoxTheme.typography.body2,
                )
            }
        },
    ) {
        val tabCounterMenu = lazy {
            // todo: tab counter
            FenixTabCounterMenu(
                context = context,
                onItemTapped = { item ->
                    browserToolbarInteractor.onTabCounterMenuItemTapped(item)
                },
                iconColor = when (activity.browsingModeManager.mode.isPrivate) {
                    true -> getColor(context, R.color.fx_mobile_private_icon_color_primary)
                    else -> null
                },
            ).also {
                it.updateMenu(
                    toolbarPosition = context.settings().toolbarPosition,
                )
            }
        }

        // todo: navbar
//        BrowserNavBar(
//            isPrivateMode = activity.browsingModeManager.mode.isPrivate,
//            showDivider = showDivider,
//            browserStore = context.components.core.store,
//            menuButton = menuButton,
//            newTabMenu = NewTabMenu(
//                context = context,
//                onItemTapped = { item ->
//                    browserToolbarInteractor.onTabCounterMenuItemTapped(item)
//                },
//                iconColor = when (activity.browsingModeManager.mode.isPrivate) {
//                    true -> getColor(context, R.color.fx_mobile_private_icon_color_primary)
//                    else -> null
//                },
//            ),
//            tabsCounterMenu = tabCounterMenu,
//            onBackButtonClick = {
//                if (context.settings().shouldShowNavigationButtonsCFR) {
//                    val currentTime = System.currentTimeMillis()
//                    if (currentTime - lastTimeNavigationButtonsClicked.longValue <= NAVIGATION_CFR_MAX_MS_BETWEEN_CLICKS) {
//                        showCFR = true
//                    }
//                    lastTimeNavigationButtonsClicked.longValue = currentTime
//                }
//                browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
//                    ToolbarMenu.Item.Back(viewHistory = false),
//                )
//            },
//            onBackButtonLongPress = {
//                browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
//                    ToolbarMenu.Item.Back(viewHistory = true),
//                )
//            },
//            onForwardButtonClick = {
//                if (context.settings().shouldShowNavigationButtonsCFR) {
//                    val currentTime = System.currentTimeMillis()
//                    if (currentTime - lastTimeNavigationButtonsClicked.longValue <= NAVIGATION_CFR_MAX_MS_BETWEEN_CLICKS) {
//                        showCFR = true
//                    }
//                    lastTimeNavigationButtonsClicked.longValue = currentTime
//                }
//                browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
//                    ToolbarMenu.Item.Forward(viewHistory = false),
//                )
//            },
//            onForwardButtonLongPress = {
//                browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
//                    ToolbarMenu.Item.Forward(viewHistory = true),
//                )
//            },
//            onNewTabButtonClick = {
//                browserToolbarInteractor.onNewTabButtonClicked()
//            },
//            onNewTabButtonLongPress = {
//                browserToolbarInteractor.onNewTabButtonLongClicked()
//            },
//            onTabsButtonClick = {
//                onTabCounterClicked(activity.browsingModeManager.mode, navController, thumbnailsFeature)
//            },
//            onTabsButtonLongPress = {},
//            onMenuButtonClick = {
//                navController.nav(
//                    R.id.browserFragment,
//                    BrowserComponentWrapperFragmentDirections.actionGlobalMenuDialogFragment(
//                        accesspoint = MenuAccessPoint.Browser,
//                    ),
//                )
//            },
//            onVisibilityUpdated = {
//                configureEngineViewWithDynamicToolbarsMaxHeight(
//                    context, customTabSessionId, findInPageIntegration
//                )
//            },
//        )
    }
}

private fun onTabCounterClicked(
    browsingMode: BrowsingMode,
    navController: NavController,
    thumbnailsFeature: ViewBoundFeatureWrapper<BrowserThumbnails>
) {
    thumbnailsFeature.get()?.requestScreenshot()
    navController.nav(
        R.id.browserComponentWrapperFragment,
        BrowserComponentWrapperFragmentDirections.actionGlobalTabsTrayFragment(
            page = when (browsingMode) {
                BrowsingMode.Normal -> Page.NormalTabs
                BrowsingMode.Private -> Page.PrivateTabs
            },
        ),
    )
}

@VisibleForTesting
internal fun initializeMicrosurveyFeature(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    messagingFeatureMicrosurvey: ViewBoundFeatureWrapper<MessagingFeature>
) {
    if (context.settings().isExperimentationEnabled && context.settings().microsurveyFeatureEnabled) {
        // todo: microsurvey
//        messagingFeatureMicrosurvey.set(
//            feature = MessagingFeature(
//                appStore = context.components.appStore,
//                surface = FenixMessageSurfaceId.MICROSURVEY,
//            ),
//            owner = lifecycleOwner,
//            view = binding.root,
//        )
    }
}

@Suppress("LongMethod")
private fun initializeMicrosurveyPrompt(context: Context, view: View, fullScreenFeature: ViewBoundFeatureWrapper<FullScreenFeature>) {
//    val context = context
//    val view = requireView()

    // todo: toolbar
//    val isToolbarAtBottom = context.isToolbarAtBottom()
//    val browserToolbar = browserToolbarView.view
//    // The toolbar view has already been added directly to the container.
//    // See initializeNavBar for more details on improving this.
//    if (isToolbarAtBottom) {
//        binding.browserLayout.removeView(browserToolbar)
//    }

//    _bottomToolbarContainerView = BottomToolbarContainerView(
//        context = context,
//        parent = binding.browserLayout,
//        hideOnScroll = isToolbarDynamic(context),
//        content = {
//            FirefoxTheme {
//                Column {
//                    val activity = context.getActivity()!! as HomeActivity
//
//                    if (!activity.isMicrosurveyPromptDismissed.value) {
//                        currentMicrosurvey?.let {
//                            if (isToolbarAtBottom) {
//                                removeBottomToolbarDivider(browserToolbar)
//                            }
//
//                            Divider()
//
//                            MicrosurveyRequestPrompt(
//                                microsurvey = it,
//                                activity = activity,
//                                onStartSurveyClicked = {
//                                    context.components.appStore.dispatch(
//                                        MicrosurveyAction.Started(
//                                            it.id
//                                        )
//                                    )
//                                    navController.nav(
//                                        R.id.browserFragment,
//                                        BrowserComponentWrapperFragmentDirections.actionGlobalMicrosurveyDialog(
//                                            it.id
//                                        ),
//                                    )
//                                },
//                                onCloseButtonClicked = {
//                                    context.components.appStore.dispatch(
//                                        MicrosurveyAction.Dismissed(it.id),
//                                    )
//
//                                    context.settings().shouldShowMicrosurveyPrompt = false
//                                    activity.isMicrosurveyPromptDismissed.value = true
//
//                                    resumeDownloadDialogState(
//                                        getCurrentTab()?.id,
//                                        context.components.core.store,
//                                        context,
//                                    )
//                                },
//                            )
//                        }
//                    } else {
//                        restoreBottomToolbarDivider(browserToolbar)
//                    }
//
//                    if (isToolbarAtBottom) {
//                        AndroidView(factory = { _ -> browserToolbar })
//                    }
//                }
//            }
//        },
//    ).apply {
//        // This covers the usecase when the app goes into fullscreen mode from portrait orientation.
//        // Transition to fullscreen happens first, and orientation change follows. Microsurvey container is getting
//        // reinitialized when going into landscape mode, but it shouldn't be visible if the app is already in the
//        // fullscreen mode. It still has to be initialized to be shown after the user exits the fullscreen.
//        val isFullscreen = fullScreenFeature.get()?.isFullScreen == true
//        toolbarContainerView.isVisible = !isFullscreen
//    }
//
//    bottomToolbarContainerIntegration.set(
//        feature = BottomToolbarContainerIntegration(
//            toolbar = bottomToolbarContainerView.toolbarContainerView,
//            store = context.components.core.store,
//            sessionId = customTabSessionId,
//        ),
//        owner = lifecycleOwner,
//        view = view,
//    )

    reinitializeEngineView(context, fullScreenFeature)
}

private fun removeBottomToolbarDivider(browserToolbar: BrowserToolbar, context: Context) {
    val safeContext = context ?: return
    if (safeContext.isToolbarAtBottom()) {
        val drawable = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.toolbar_background_no_divider,
            null,
        )
        browserToolbar.background = drawable
        browserToolbar.elevation = 0.0f
    }
}

private fun restoreBottomToolbarDivider(browserToolbar: BrowserToolbar, context: Context) {
    val safeContext = context ?: return
    if (safeContext.isToolbarAtBottom()) {
        val defaultBackground = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.toolbar_background,
            context?.theme,
        )
        browserToolbar.background = defaultBackground
    }
}

private fun updateNavbarDivider(context: Context) {
    val safeContext = context ?: return

    // Evaluate showing the navbar divider only if addressbar is shown at the top
    // and the toolbar chrome should be is visible.
    if (!safeContext.isToolbarAtBottom()) { // && webAppToolbarShouldBeVisible) {
        resetNavbar(context)
    }
}

/**
 * Build and show a new navbar.
 * Useful when needed to force an update of it's layout.
 */
private fun resetNavbar(context: Context) {
    if (context?.shouldAddNavigationBar() != true) return // || !webAppToolbarShouldBeVisible) return

    // todo: toolbar
    // Prevent showing two navigation bars at the same time.
//    _bottomToolbarContainerView?.toolbarContainerView?.let {
//        binding.browserLayout.removeView(it)
//    }
    reinitializeNavBar()
}

private var currentMicrosurvey: MicrosurveyUIData? = null

/**
 * Listens for the microsurvey message and initializes the microsurvey prompt if one is available.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun listenForMicrosurveyMessage(context: Context, lifecycleOwner: LifecycleOwner) {
    // todo: microsurvey
//    binding.root.consumeFrom(context.components.appStore, lifecycleOwner) { state ->
//        state.messaging.messageToShow[FenixMessageSurfaceId.MICROSURVEY]?.let { message ->
//            if (message.id != currentMicrosurvey?.id) {
//                message.toMicrosurveyUIData()?.let { microsurvey ->
//                    context.components.settings.shouldShowMicrosurveyPrompt = true
//                    currentMicrosurvey = microsurvey
//
//                    _bottomToolbarContainerView?.toolbarContainerView.let {
//                        binding.browserLayout.removeView(it)
//                    }
//
//                    if (context.shouldAddNavigationBar()) {
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

private fun isToolbarDynamic(context: Context) =
    !context.settings().shouldUseFixedTopToolbar && context.settings().isDynamicToolbarEnabled

///**
// * Returns a list of context menu items [ContextMenuCandidate] for the context menu
// */
//abstract fun getContextMenuCandidates(
//    context: Context,
//    view: View,
//): List<ContextMenuCandidate>

@VisibleForTesting
internal fun observeRestoreComplete(
    store: BrowserStore,
    context: Context,
    lifecycleOwner: LifecycleOwner,
    coroutineScope: CoroutineScope,
    navController: NavController,
) {
    val activity = context.getActivity()!! as HomeActivity
    consumeFlow(store, lifecycleOwner, context, coroutineScope) { flow ->
        flow.map { state -> state.restoreComplete }.distinctUntilChanged().collect { restored ->
            if (restored) {
                // Once tab restoration is complete, if there are no tabs to show in the browser, go home
                val tabs = store.state.getNormalOrPrivateTabs(
                    activity.browsingModeManager.mode.isPrivate,
                )
                if (tabs.isEmpty() || store.state.selectedTabId == null) {
                    navController.popBackStack(R.id.homeFragment, false)
                }
            }
        }
    }
}

@VisibleForTesting
internal fun observeTabSelection(
    store: BrowserStore,
    context: Context,
    lifecycleOwner: LifecycleOwner,
    coroutineScope: CoroutineScope,
    currentStartDownloadDialog: StartDownloadDialog?,
    browserInitialized: Boolean,
) {
    consumeFlow(store, lifecycleOwner, context, coroutineScope) { flow ->
        flow.distinctUntilChangedBy {
            it.selectedTabId
        }.mapNotNull {
            it.selectedTab
        }.collect {
            currentStartDownloadDialog?.dismiss()
            handleTabSelected(it, browserInitialized)
        }
    }
}

@VisibleForTesting
@Suppress("ComplexCondition")
internal fun observeTabSource(
    store: BrowserStore,
    context: Context,
    lifecycleOwner: LifecycleOwner,
    coroutineScope: CoroutineScope
) {
    consumeFlow(store, lifecycleOwner, context, coroutineScope) { flow ->
        flow.mapNotNull { state ->
            state.selectedTab
        }.collect {
            if (!context.components.fenixOnboarding.userHasBeenOnboarded() && it.content.loadRequest?.triggeredByRedirect != true && it.source !is SessionState.Source.External && it.content.url !in onboardingLinksList) {
                context.components.fenixOnboarding.finish()
            }
        }
    }
}

private fun handleTabSelected(selectedTab: TabSessionState, browserInitialized: Boolean) {
    // todo: theme
//    if (!this.isRemoving) {
//        updateThemeForSession(selectedTab)
//    }

    // todo: toolbar
//    if (browserInitialized) {
//        view?.let {
//            fullScreenChanged(false)
////            browserToolbarView.expand()
//
//            val context = context
//            resumeDownloadDialogState(selectedTab.id, context.components.core.store, context)
//            it.announceForAccessibility(selectedTab.toDisplayTitle())
//        }
//    } else {
//        view?.let { view -> initializeUI(view) }
//    }
}


private fun evaluateMessagesForMicrosurvey(components: Components) =
    components.appStore.dispatch(MessagingAction.Evaluate(FenixMessageSurfaceId.MICROSURVEY))


@CallSuper
fun onForwardPressed(sessionFeature: ViewBoundFeatureWrapper<SessionFeature>): Boolean {
    return sessionFeature.onForwardPressed()
}

/**
 * Forwards activity results to the [ActivityResultHandler] features.
 *//* override */ fun onActivityResult(requestCode: Int, data: Intent?, resultCode: Int): Boolean {
    // todo: onActivityResult
//    return listOf(
//        promptsFeature,
//        webAuthnFeature,
//    ).any { it.onActivityResult(requestCode, data, resultCode) }
     return true
}

/**
 * Navigate to GlobalTabHistoryDialogFragment.
 */
private fun navigateToGlobalTabHistoryDialogFragment(
    navController: NavController, customTabSessionId: String?
) {
    navController.navigate(
        NavGraphDirections.actionGlobalTabHistoryDialogFragment(
            activeSessionId = customTabSessionId,
        ),
    )
}

/* override */ fun onBackLongPressed(): Boolean {
    // todo:
//    navigateToGlobalTabHistoryDialogFragment()
    return true
}

/* override */ fun onForwardLongPressed(): Boolean {
    // todo:
//    navigateToGlobalTabHistoryDialogFragment()
    return true
}

/**
 * Saves the external app session ID to be restored later in [onViewStateRestored].
 *//* override */ fun onSaveInstanceState(outState: Bundle) {
    // todo:
//    super.onSaveInstanceState(outState)
//    outState.putString(KEY_CUSTOM_TAB_SESSION_ID, customTabSessionId)
//    outState.putString(LAST_SAVED_GENERATED_PASSWORD, lastSavedGeneratedPassword)
}

/**
 * Retrieves the external app session ID saved by [onSaveInstanceState].
 *//* override */ fun onViewStateRestored(savedInstanceState: Bundle?) {
    // todo:
//    super.onViewStateRestored(savedInstanceState)
//    savedInstanceState?.getString(KEY_CUSTOM_TAB_SESSION_ID)?.let {
//        if (context.components.core.store.state.findCustomTab(it) != null) {
//            customTabSessionId = it
//        }
//    }
//    lastSavedGeneratedPassword = savedInstanceState?.getString(LAST_SAVED_GENERATED_PASSWORD)
}

/**
 * Forwards permission grant results to one of the features.
 */
@Deprecated("Deprecated in Java")/* override */ fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray,
) {
    // todo: permissions
//    val feature: PermissionsFeature? = when (requestCode) {
//        REQUEST_CODE_DOWNLOAD_PERMISSIONS -> downloadsFeature.get()
//        REQUEST_CODE_PROMPT_PERMISSIONS -> promptsFeature.get()
//        REQUEST_CODE_APP_PERMISSIONS -> sitePermissionsFeature.get()
//        else -> null
//    }
//    feature?.onPermissionsResult(permissions, grantResults)
}

/**
 * Removes the session if it was opened by an ACTION_VIEW intent
 * or if it has a parent session and no more history
 */
fun removeSessionIfNeeded(context: Context): Boolean {
    getCurrentTab(context)?.let { session ->
        return if (session.source is SessionState.Source.External && !session.restored) {
            context.getActivity()?.finish()
            context.components.useCases.tabsUseCases.removeTab(session.id)
            true
        } else {
            val hasParentSession = session is TabSessionState && session.parentId != null
            if (hasParentSession) {
                context.components.useCases.tabsUseCases.removeTab(
                    session.id, selectParentIfExists = true
                )
            }
            // We want to return to home if this session didn't have a parent session to select.
            val goToOverview = !hasParentSession
            !goToOverview
        }
    }
    return false
}

/**
 * Returns the layout [android.view.Gravity] for the quick settings and ETP dialog.
 */
fun getAppropriateLayoutGravity(context: Context): Int =
    context.components.settings.toolbarPosition.androidGravity

/**
 * Configure the engine view to know where to place website's dynamic elements
 * depending on the space taken by any dynamic toolbar.
 */
@VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
internal fun configureEngineViewWithDynamicToolbarsMaxHeight(
    context: Context,
    customTabSessionId: String?,
    findInPageIntegration: ViewBoundFeatureWrapper<FindInPageIntegration>
) {
    val currentTab =
        context.components.core.store.state.findCustomTabOrSelectedTab(customTabSessionId)
    if (currentTab?.content?.isPdf == true) return
    if (findInPageIntegration.get()?.isFeatureActive == true) return
//    val toolbarHeights = view?.let { probeToolbarHeights(it) } ?: return

    context?.also {
        if (isToolbarDynamic(it)) {
            // todo: toolbar
//            if (!context.components.core.geckoRuntime.isInteractiveWidgetDefaultResizesVisual) {
//                getEngineView().setDynamicToolbarMaxHeight(toolbarHeights.first + toolbarHeights.second)
//            }
        } else {
            // todo: toolbar
//            (getSwipeRefreshLayout().layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
//                bottomMargin = toolbarHeights.second
//            }
        }
    }
}

/**
 * Get an instant reading of the top toolbar height and the bottom toolbar height.
 */
private fun probeToolbarHeights(
    rootView: View,
    customTabSessionId: String?,
    fullScreenFeature: ViewBoundFeatureWrapper<FullScreenFeature>,
): Pair<Int, Int> {
    val context = rootView.context
    // Avoid any change for scenarios where the toolbar is not shown
    if (fullScreenFeature.get()?.isFullScreen == true) return 0 to 0

    val topToolbarHeight = context.settings().getTopToolbarHeight(
        includeTabStrip = customTabSessionId == null && context.isTabStripEnabled(),
    )
    val navbarHeight = context.resources.getDimensionPixelSize(R.dimen.browser_navbar_height)
    val isKeyboardShown = rootView.isKeyboardVisible()
    val bottomToolbarHeight = context.settings().getBottomToolbarHeight(context).minus(
        when (isKeyboardShown) {
            true -> navbarHeight // When keyboard is shown the navbar is expected to be hidden. Ignore it's height.
            false -> 0
        },
    )

    return topToolbarHeight to bottomToolbarHeight
}

/**
 * Updates the site permissions rules based on user settings.
 */
private fun assignSitePermissionsRules(
    context: Context,
    sitePermissionsFeature: ViewBoundFeatureWrapper<SitePermissionsFeature>,
) {
    // todo:
//    val rules = context.components.settings.getSitePermissionsCustomSettingsRules()
//
//    sitePermissionsFeature.withFeature {
//        it.sitePermissionsRules = rules
//    }
}

/**
 * Displays the quick settings dialog,
 * which lets the user control tracking protection and site settings.
 */
private fun showQuickSettingsDialog(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    coroutineScope: CoroutineScope,
    navController: NavController,
    view: View,
    customTabSessionId: String?,
) {
    val tab = getCurrentTab(context, customTabSessionId) ?: return
    lifecycleOwner.lifecycleScope.launch(Main) {
        val sitePermissions: SitePermissions? = tab.content.url.getOrigin()?.let { origin ->
            val storage = context.components.core.permissionStorage
            storage.findSitePermissionsBy(origin, tab.content.private)
        }

        view?.let {
            navToQuickSettingsSheet(
                tab,
                sitePermissions,
                context = context,
                coroutineScope = coroutineScope,
                navController = navController
            )
        }
    }
}

/**
 * Set the activity normal/private theme to match the current session.
 */
@VisibleForTesting
internal fun updateThemeForSession(session: SessionState, context: Context) {
    val sessionMode = BrowsingMode.fromBoolean(session.content.private)
    (context.getActivity()!! as HomeActivity).browsingModeManager.mode = sessionMode
}

/**
 * A safe version of [getCurrentTab] that safely checks for context nullability.
 */
fun getSafeCurrentTab(context: Context, customTabSessionId: String?): SessionState? {
    return context.components.core.store.state.findCustomTabOrSelectedTab(
        customTabSessionId
    )
}

@VisibleForTesting
fun getCurrentTab(context: Context, customTabSessionId: String? = null): SessionState? {
    return context.components.core.store.state.findCustomTabOrSelectedTab(customTabSessionId)
}

private suspend fun bookmarkTapped(
    sessionUrl: String, sessionTitle: String, context: Context, navController: NavController,
) = withContext(IO) {
    val bookmarksStorage = context.components.core.bookmarksStorage
    val existing =
        bookmarksStorage.getBookmarksWithUrl(sessionUrl).firstOrNull { it.url == sessionUrl }
    if (existing != null) {
        // Bookmark exists, go to edit fragment
        withContext(Main) {
            nav(
                navController,
                R.id.browserComponentWrapperFragment,
                BrowserComponentWrapperFragmentDirections.actionGlobalBookmarkEditFragment(existing.guid, true),
            )
        }
    } else {
        // Save bookmark, then go to edit fragment
        try {
            val parentNode = Result.runCatching {
                val parentGuid = bookmarksStorage.getRecentBookmarks(1).firstOrNull()?.parentGuid
                    ?: BookmarkRoot.Mobile.id

                bookmarksStorage.getBookmark(parentGuid)!!
            }.getOrElse {
                // this should be a temporary hack until the menu redesign is completed
                // see MenuDialogMiddleware for the updated version
                throw PlacesApiException.UrlParseFailed(reason = "no parent node")
            }

            val guid = bookmarksStorage.addItem(
                parentNode.guid,
                url = sessionUrl,
                title = sessionTitle,
                position = null,
            )

//                MetricsUtils.recordBookmarkMetrics(MetricsUtils.BookmarkAction.ADD, METRIC_SOURCE)
            showBookmarkSavedSnackbar(
                message = context.getString(
                    R.string.bookmark_saved_in_folder_snackbar,
                    friendlyRootTitle(context, parentNode),
                ),
                context = context,
                onClick = {
//                        MetricsUtils.recordBookmarkMetrics(
//                            MetricsUtils.BookmarkAction.EDIT,
//                            TOAST_METRIC_SOURCE,
//                        )
                    navController.navigateWithBreadcrumb(
                        directions = BrowserComponentWrapperFragmentDirections.actionGlobalBookmarkEditFragment(
                            guid,
                            true,
                        ),
                        navigateFrom = "BrowserFragment",
                        navigateTo = "ActionGlobalBookmarkEditFragment",
                    )
                },
            )
        } catch (e: PlacesApiException.UrlParseFailed) {
            withContext(Main) {
                // todo: bookmark tapped
//                view?.let {
//                    Snackbar.make(
//                        snackBarParentView = binding.dynamicSnackbarContainer,
//                        snackbarState = SnackbarState(
//                            message = getString(R.string.bookmark_invalid_url_error),
//                            duration = SnackbarState.Duration.Preset.Long,
//                        ),
//                    ).show()
//                }
            }
        }
    }
}

private fun showBookmarkSavedSnackbar(message: String, context: Context, onClick: () -> Unit) {
    // todo: snackbar
//    Snackbar.make(
//        snackBarParentView = binding.dynamicSnackbarContainer,
//        snackbarState = SnackbarState(
//            message = message,
//            duration = SnackbarState.Duration.Preset.Long,
//            action = Action(
//                label = context.getString(R.string.edit_bookmark_snackbar_action),
//                onClick = onClick,
//            ),
//        ),
//    ).show()
}

fun onHomePressed(pipFeature: PictureInPictureFeature) = pipFeature?.onHomePressed() ?: false

/**
 * Exit fullscreen mode when exiting PIP mode
 */
private fun pipModeChanged(session: SessionState, context: Context, backPressedHandler: onBackPressedHandler) {
    // todo: isAdded
    if (!session.content.pictureInPictureEnabled && session.content.fullScreen) { // && isAdded) {
        onBackPressed(backPressedHandler)
        fullScreenChanged(false, context)
    }
}

fun onPictureInPictureModeChanged(enabled: Boolean, pipFeature: PictureInPictureFeature?) {
    pipFeature?.onPictureInPictureModeChanged(enabled)
}

private fun viewportFitChange(layoutInDisplayCutoutMode: Int, context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val layoutParams = context.getActivity()?.window?.attributes
        layoutParams?.layoutInDisplayCutoutMode = layoutInDisplayCutoutMode
        context.getActivity()?.window?.attributes = layoutParams
    }
}

@VisibleForTesting
internal fun fullScreenChanged(
    inFullScreen: Boolean,
    context: Context,
) {
    val activity = context.getActivity() ?: return
    if (inFullScreen) {
        // Close find in page bar if opened
        // todo: find in page
//        findInPageIntegration.onBackPressed()

        FullScreenNotificationToast(
            activity = activity,
            gestureNavString = context.getString(R.string.exit_fullscreen_with_gesture_short),
            backButtonString = context.getString(R.string.exit_fullscreen_with_back_button_short),
            GestureNavUtils,
        ).show()

        // todo: engine view
//        activity.enterImmersiveMode(
//            setOnApplyWindowInsetsListener = { key: String, listener: OnApplyWindowInsetsListener ->
//                binding.engineView.addWindowInsetsListener(key, listener)
//            },
//        )
//        (view as? SwipeGestureLayout)?.isSwipeEnabled = false
        expandBrowserView()

    } else {
        // todo: engine view
//        activity.exitImmersiveMode(
//            unregisterOnApplyWindowInsetsListener = binding.engineView::removeWindowInsetsListener,
//        )

//        (view as? SwipeGestureLayout)?.isSwipeEnabled = true
        (context.getActivity() as? HomeActivity)?.let { homeActivity ->
            // ExternalAppBrowserActivity exclusively handles it's own theming unless in private mode.
            if (homeActivity !is ExternalAppBrowserActivity || homeActivity.browsingModeManager.mode.isPrivate) {
                homeActivity.themeManager.applyStatusBarTheme(
                    homeActivity, homeActivity.isTabStripEnabled()
                )
            }
        }
        collapseBrowserView()
    }

    // todo: swipe refresh
//    binding.swipeRefresh.isEnabled = shouldPullToRefreshBeEnabled(inFullScreen)
}

@VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
internal fun expandBrowserView() {
    // todo: toolbar
//    browserToolbarView.apply {
//        collapse()
//        gone()
//    }
//    _bottomToolbarContainerView?.toolbarContainerView?.apply {
//        collapse()
//        isVisible = false
//    }
    // todo: engine
//    val browserEngine = getSwipeRefreshLayout().layoutParams as CoordinatorLayout.LayoutParams
//    browserEngine.behavior = null
//    browserEngine.bottomMargin = 0
//    browserEngine.topMargin = 0
    // todo:
//    getSwipeRefreshLayout().translationY = 0f

//    getEngineView().apply {
    // todo: engine view
//        setDynamicToolbarMaxHeight(0)
//        setVerticalClipping(0)
//    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
internal fun collapseBrowserView() {
    // todo:
//    if (webAppToolbarShouldBeVisible) {
//        browserToolbarView.visible()
//        _bottomToolbarContainerView?.toolbarContainerView?.isVisible = true
//        reinitializeEngineView()
//        browserToolbarView.expand()
//        _bottomToolbarContainerView?.toolbarContainerView?.expand()
//    }
}

@CallSuper
fun onUpdateToolbarForConfigurationChange(
    toolbar: BrowserToolbarView,
    context: Context,
    fullScreenFeature: ViewBoundFeatureWrapper<FullScreenFeature>,
) {
    toolbar.dismissMenu()

    // If the navbar feature could be visible, we should update it's state.
    val shouldUpdateNavBarState =
        // todo: webAppToolbarShouldBeVisible
        context.settings().navigationToolbarEnabled // && webAppToolbarShouldBeVisible
    if (shouldUpdateNavBarState) {
        // todo: navbar
//        updateNavBarForConfigurationChange(
//            context = context,
//            parent = binding.browserLayout,
//            toolbarView = browserToolbarView.view,
//            bottomToolbarContainerView = _bottomToolbarContainerView?.toolbarContainerView,
//            reinitializeNavBar = ::reinitializeNavBar,
//            reinitializeMicrosurveyPrompt = ::initializeMicrosurveyPrompt,
//        )
    }

    reinitializeEngineView(context, fullScreenFeature)

    // If the microsurvey feature is visible, we should update it's state.
    if (shouldShowMicrosurveyPrompt(context) && !shouldUpdateNavBarState) {
        // todo: microsurvey
//        updateMicrosurveyPromptForConfigurationChange(
//            parent = binding.browserLayout,
//            bottomToolbarContainerView = _bottomToolbarContainerView?.toolbarContainerView,
//            reinitializeMicrosurveyPrompt = ::initializeMicrosurveyPrompt,
//        )
    }
}

private fun reinitializeNavBar() {
    // todo: navbar
//    initializeNavBar(
//        browserToolbar = browserToolbarView.view,
//        view = requireView(),
//        context = context,
//        activity = context.getActivity()!! as HomeActivity,
//    )
}

@VisibleForTesting
internal fun reinitializeEngineView(
    context: Context,
    fullScreenFeature: ViewBoundFeatureWrapper<FullScreenFeature>,
    customTabSessionId: String? = null,
) {
    val isFullscreen = fullScreenFeature.get()?.isFullScreen == true
    val shouldToolbarsBeHidden = isFullscreen // || !webAppToolbarShouldBeVisible
    val topToolbarHeight = context.settings().getTopToolbarHeight(
        includeTabStrip = customTabSessionId == null && context.isTabStripEnabled(),
    )
    val bottomToolbarHeight = context.settings().getBottomToolbarHeight(context)

    initializeEngineView(
        topToolbarHeight = if (shouldToolbarsBeHidden) 0 else topToolbarHeight,
        bottomToolbarHeight = if (shouldToolbarsBeHidden) 0 else bottomToolbarHeight,
        context = context,
    )
}

/*
 * Dereference these views when the fragment view is destroyed to prevent memory leaks
 */

//override fun onAttach(context: Context) {
//    super.onAttach(context)
//}
//
//override fun onDetach() {
//    super.onDetach()
//}

internal fun showCannotOpenFileError(
    container: ViewGroup,
    context: Context,
    downloadState: DownloadState,
) {
    Snackbar.make(
        snackBarParentView = container,
        snackbarState = SnackbarState(
            message = DynamicDownloadDialog.getCannotOpenFileErrorMessage(
                context, downloadState
            ),
        ),
    ).show()
}

fun onAccessibilityStateChanged(enabled: Boolean) {
    // todo: toolbar
//    if (_browserToolbarView != null) {
//        browserToolbarView.setToolbarBehavior(enabled)
//    }
}

fun onConfigurationChanged(newConfig: Configuration) {
//    super.onConfigurationChanged(newConfig)
//
    // todo: find in page
//    if (findInPageIntegration.get()?.isFeatureActive != true && fullScreenFeature.get()?.isFullScreen != true) {
//        _browserToolbarView?.let {
//            onUpdateToolbarForConfigurationChange(it)
//        }
//    }
}

// This method is called in response to native web extension messages from
// content scripts (e.g the reader view extension). By the time these
// messages are processed the fragment/view may no longer be attached.
internal fun safeInvalidateBrowserToolbarView() {
    // todo: toolbar
//    runIfFragmentIsAttached {
//        val toolbarView = _browserToolbarView
//        if (toolbarView != null) {
//            toolbarView.view.invalidateActions()
//            toolbarView.toolbarIntegration.invalidateMenu()
//        }
//        _menuButtonView?.setHighlightStatus()
//    }
}

///**
// * Convenience method for replacing EngineView (id/engineView) in unit tests.
// */
//@VisibleForTesting
//internal fun getEngineView() = engineView!!// binding.engineView

///**
// * Convenience method for replacing SwipeRefreshLayout (id/swipeRefresh) in unit tests.
// */
//@VisibleForTesting
//internal fun getSwipeRefreshLayout() = binding.swipeRefresh

internal fun shouldShowCompletedDownloadDialog(
    downloadState: DownloadState,
    status: DownloadState.Status,
    context: Context,
): Boolean {
    val isValidStatus =
        status in listOf(DownloadState.Status.COMPLETED, DownloadState.Status.FAILED)
    val isSameTab = downloadState.sessionId == (getCurrentTab(context)?.id ?: false)

    return isValidStatus && isSameTab
}

private fun handleOnSaveLoginWithGeneratedStrongPassword(
    passwordsStorage: SyncableLoginsStorage,
    url: String,
    password: String,
    lifecycleScope: CoroutineScope,
    setLastSavedGeneratedPassword: (String) -> Unit
) {
    setLastSavedGeneratedPassword(password)
    val loginToSave = LoginEntry(
        origin = url,
        httpRealm = url,
        username = "",
        password = password,
    )
    var saveLoginJob: Deferred<Unit>? = null
    lifecycleScope.launch(IO) {
        saveLoginJob = async {
            try {
                passwordsStorage.add(loginToSave)
            } catch (loginException: LoginsApiException) {
                loginException.printStackTrace()
                Log.e(
                    "Add new login",
                    "Failed to add new login with generated password.",
                    loginException,
                )
            }
            saveLoginJob?.await()
        }
        saveLoginJob?.invokeOnCompletion {
            if (it is CancellationException) {
                saveLoginJob?.cancel()
            }
        }
    }
}

private fun hideUpdateFragmentAfterSavingGeneratedPassword(
    username: String,
    password: String,
    lastSavedGeneratedPassword: String?,
): Boolean {
    return username.isEmpty() && password == lastSavedGeneratedPassword
}

private fun removeLastSavedGeneratedPassword(setLastSavedGeneratedPassword: (String?) -> Unit) {
    setLastSavedGeneratedPassword(null)
}

private fun navigateToSavedLoginsFragment(navController: NavController) {
    if (navController.currentDestination?.id == R.id.browserComponentWrapperFragment) {
        val directions = BrowserComponentWrapperFragmentDirections.actionLoginsListFragment()
        navController.navigate(directions)
    }
}

/* BaseBrowserFragment funs */

/* BrowserFragment funs */

private fun initSharePageAction(
    context: Context, browserToolbarInteractor: BrowserToolbarInteractor
) {
    if (!context.settings().navigationToolbarEnabled || context.isTabStripEnabled()) {
        return
    }

    val sharePageAction = BrowserToolbar.createShareBrowserAction(
        context = context,
    ) {
//            AddressToolbar.shareTapped.record((NoExtras()))
        browserToolbarInteractor.onShareActionClicked()
    }

    // todo: toolbar
//    browserToolbarView.view.addPageAction(sharePageAction)
}

private fun initTranslationsAction(
    context: Context,
    view: View,
    browserToolbarInteractor: BrowserToolbarInteractor,
    translationsAvailable: Boolean
) {
    if (!FxNimbus.features.translations.value().mainFlowToolbarEnabled) {
        return
    }

    val translationsAction = Toolbar.ActionButton(
        AppCompatResources.getDrawable(
            context,
            R.drawable.mozac_ic_translate_24,
        ),
        contentDescription = context.getString(R.string.browser_toolbar_translate),
        iconTintColorResource = ThemeManager.resolveAttribute(R.attr.textPrimary, context),
        visible = { translationsAvailable },
        weight = { TRANSLATIONS_WEIGHT },
        listener = {
//            browserToolbarInteractor.onTranslationsButtonClicked()
        },
    )
    // todo: toolbar
//    browserToolbarView.view.addPageAction(translationsAction)

    // todo: translations
//    translationsBinding.set(
//        feature = TranslationsBinding(browserStore = context.components.core.store,
//            onTranslationsActionUpdated = {
//                translationsAvailable = it.isVisible
//
//                translationsAction.updateView(
//                    tintColorResource = if (it.isTranslated) {
//                        R.color.fx_mobile_icon_color_accent_violet
//                    } else {
//                        ThemeManager.resolveAttribute(R.attr.textPrimary, context)
//                    },
//                    contentDescription = if (it.isTranslated) {
//                        context.getString(
//                            R.string.browser_toolbar_translated_successfully,
//                            it.fromSelectedLanguage?.localizedDisplayName,
//                            it.toSelectedLanguage?.localizedDisplayName,
//                        )
//                    } else {
//                        context.getString(R.string.browser_toolbar_translate)
//                    },
//                )
//
//                safeInvalidateBrowserToolbarView()
//
//                if (!it.isTranslateProcessing) {
//                    context.components.appStore.dispatch(SnackbarAction.SnackbarDismissed)
//                }
//            },
//            onShowTranslationsDialog = {} //FIXME: browserToolbarInteractor.onTranslationsButtonClicked(),
//        ),
//        owner = lifecycleOwner,
//        view = view,
//    )
}

@SuppressLint("VisibleForTests")
private fun initReloadAction(context: Context) {
    if (!context.settings().navigationToolbarEnabled) {
        return
    }

    // todo: refresh action
//    refreshAction = BrowserToolbar.TwoStateButton(
//        primaryImage = AppCompatResources.getDrawable(
//            context,
//            R.drawable.mozac_ic_arrow_clockwise_24,
//        )!!,
//        primaryContentDescription = context.getString(R.string.browser_menu_refresh),
//        primaryImageTintResource = ThemeManager.resolveAttribute(R.attr.textPrimary, context),
//        isInPrimaryState = {
//            getSafeCurrentTab()?.content?.loading == false
//        },
//        secondaryImage = AppCompatResources.getDrawable(
//            context,
//            R.drawable.mozac_ic_stop,
//        )!!,
//        secondaryContentDescription = context.getString(R.string.browser_menu_stop),
//        disableInSecondaryState = false,
//        weight = { RELOAD_WEIGHT },
//        longClickListener = {
//            browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
//                ToolbarMenu.Item.Reload(bypassCache = true),
//            )
//        },
//        listener = {
//            if (getCurrentTab()?.content?.loading == true) {
//                browserToolbarInteractor.onBrowserToolbarMenuItemTapped(ToolbarMenu.Item.Stop)
//            } else {
//                browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
//                    ToolbarMenu.Item.Reload(bypassCache = false),
//                )
//            }
//        },
//    )
//
//    refreshAction?.let {
//        browserToolbarView.view.addPageAction(it)
//    }
}

private fun initReaderMode(context: Context, view: View) {
    // todo: reader mode
//    val readerModeAction = BrowserToolbar.ToggleButton(
//        image = AppCompatResources.getDrawable(
//            context,
//            R.drawable.ic_readermode,
//        )!!,
//        imageSelected = AppCompatResources.getDrawable(
//            context,
//            R.drawable.ic_readermode_selected,
//        )!!,
//        contentDescription = context.getString(R.string.browser_menu_read),
//        contentDescriptionSelected = context.getString(R.string.browser_menu_read_close),
//        visible = {
//            readerModeAvailable && !reviewQualityCheckAvailable
//        },
//        weight = { READER_MODE_WEIGHT },
//        selected = getSafeCurrentTab()?.let {
//            activity?.components?.core?.store?.state?.findTab(it.id)?.readerState?.active
//        } ?: false,
//        listener = browserToolbarInteractor::onReaderModePressed,
//    )

    // todo: toolbar
//    browserToolbarView.view.addPageAction(readerModeAction)
//
//    readerViewFeature.set(
//        feature = context.components.strictMode.resetAfter(StrictMode.allowThreadDiskReads()) {
//            ReaderViewFeature(
//                context = context,
//                engine = context.components.core.engine,
//                store = context.components.core.store,
//                controlsView = binding.readerViewControlsBar,
//            ) { available, active ->
//                readerModeAvailable = available
//                readerModeAction.setSelected(active)
//                safeInvalidateBrowserToolbarView()
//            }
//        },
//        owner = lifecycleOwner,
//        view = view,
//    )
}

private fun initReviewQualityCheck(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    view: View,
    navController: NavController,
    setReviewQualityCheckAvailable: (Boolean) -> Unit,
    reviewQualityCheckAvailable: Boolean,
    reviewQualityCheckFeature: ViewBoundFeatureWrapper<ReviewQualityCheckFeature>,
) {
    val reviewQualityCheck = BrowserToolbar.ToggleButton(
        image = AppCompatResources.getDrawable(
            context,
            R.drawable.mozac_ic_shopping_24,
        )!!.apply {
            setTint(getColor(context, R.color.fx_mobile_text_color_primary))
        },
        imageSelected = AppCompatResources.getDrawable(
            context,
            R.drawable.ic_shopping_selected,
        )!!,
        contentDescription = context.getString(R.string.review_quality_check_open_handle_content_description),
        contentDescriptionSelected = context.getString(R.string.review_quality_check_close_handle_content_description),
        visible = {reviewQualityCheckAvailable},
        weight = { REVIEW_QUALITY_CHECK_WEIGHT },
        listener = { _ ->
            context.components.appStore.dispatch(
                ShoppingAction.ShoppingSheetStateUpdated(expanded = true),
            )
            navController.navigate(
                BrowserComponentWrapperFragmentDirections.actionBrowserFragmentToReviewQualityCheckDialogFragment(),
            )
        },
    )

    // todo: toolbar
//    browserToolbarView.view.addPageAction(reviewQualityCheck)

    reviewQualityCheckFeature.set(
        feature = ReviewQualityCheckFeature(
            appStore = context.components.appStore,
            browserStore = context.components.core.store,
            shoppingExperienceFeature = DefaultShoppingExperienceFeature(),
            onIconVisibilityChange = {
                setReviewQualityCheckAvailable(it)
                safeInvalidateBrowserToolbarView()
            },
            onBottomSheetStateChange = {
                reviewQualityCheck.setSelected(selected = it, notifyListener = false)
            },
            onProductPageDetected = {
                // Shopping.productPageVisits.add()
            },
        ),
        owner = lifecycleOwner,
        view = view,
    )
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun addLeadingAction(
    context: Context,
    showHomeButton: Boolean,
    showEraseButton: Boolean,
) {
    // todo: leading action
//    if (leadingAction != null) return
//
//    leadingAction = if (showEraseButton) {
//        BrowserToolbar.Button(
//            imageDrawable = AppCompatResources.getDrawable(
//                context,
//                R.drawable.mozac_ic_data_clearance_24,
//            )!!,
//            contentDescription = context.getString(R.string.browser_toolbar_erase),
//            iconTintColorResource = ThemeManager.resolveAttribute(R.attr.textPrimary, context),
//            listener = browserToolbarInteractor::onEraseButtonClicked,
//        )
//    } else if (showHomeButton) {
//        BrowserToolbar.Button(
//            imageDrawable = AppCompatResources.getDrawable(
//                context,
//                R.drawable.mozac_ic_home_24,
//            )!!,
//            contentDescription = context.getString(R.string.browser_toolbar_home),
//            iconTintColorResource = ThemeManager.resolveAttribute(R.attr.textPrimary, context),
//            listener = browserToolbarInteractor::onHomeButtonClicked,
//        )
//    } else {
//        null
//    }
//
//    leadingAction?.let {
//        browserToolbarView.view.addNavigationAction(it)
//    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun removeLeadingAction() {
    // todo: leading action
//    leadingAction?.let {
//        browserToolbarView.view.removeNavigationAction(it)
//    }
//    leadingAction = null
}

/**
 * This code takes care of the [BrowserToolbar] leading and navigation actions.
 * The older design requires a HomeButton followed by navigation buttons for tablets.
 * The newer design expects NavigationButtons and a HomeButton in landscape mode for phones and in both modes
 * for tablets.
 */
@VisibleForTesting
internal fun updateBrowserToolbarLeadingAndNavigationActions(
    context: Context,
    redesignEnabled: Boolean,
    isLandscape: Boolean,
    isTablet: Boolean,
    isPrivate: Boolean,
    feltPrivateBrowsingEnabled: Boolean,
    isWindowSizeSmall: Boolean,
) {
    if (redesignEnabled) {
        updateAddressBarNavigationActions(
            context = context,
            isWindowSizeSmall = isWindowSizeSmall,
        )
        updateAddressBarLeadingAction(
            redesignEnabled = true,
            isLandscape = isLandscape,
            isTablet = isTablet,
            isPrivate = isPrivate,
            feltPrivateBrowsingEnabled = feltPrivateBrowsingEnabled,
            context = context,
        )
    } else {
        updateAddressBarLeadingAction(
            redesignEnabled = false,
            isLandscape = isLandscape,
            isPrivate = isPrivate,
            isTablet = isTablet,
            feltPrivateBrowsingEnabled = feltPrivateBrowsingEnabled,
            context = context,
        )
        updateTabletToolbarActions(isTablet = isTablet, context)
    }
    // todo: toolbar
//    browserToolbarView.view.invalidateActions()
}

private fun updateBrowserToolbarMenuVisibility() {
    // todo: toolbar
//    browserToolbarView.updateMenuVisibility(
//        isVisible = false // !context.shouldAddNavigationBar(),
//    )
}

@VisibleForTesting
internal fun updateAddressBarLeadingAction(
    redesignEnabled: Boolean,
    isLandscape: Boolean,
    isTablet: Boolean,
    isPrivate: Boolean,
    feltPrivateBrowsingEnabled: Boolean,
    context: Context,
) {
    val showHomeButton = !redesignEnabled
    val showEraseButton = feltPrivateBrowsingEnabled && isPrivate && (isLandscape || isTablet)

    if (showHomeButton || showEraseButton) {
        addLeadingAction(
            context = context,
            showHomeButton = showHomeButton,
            showEraseButton = showEraseButton,
        )
    } else {
        removeLeadingAction()
    }
}

@VisibleForTesting
internal fun updateAddressBarNavigationActions(
    context: Context,
    isWindowSizeSmall: Boolean,
) {
    if (!isWindowSizeSmall) {
        addNavigationActions(context)
    } else {
        removeNavigationActions()
    }
}

fun onUpdateToolbarForConfigurationChange(toolbar: BrowserToolbarView, context: Context) {
//    super.onUpdateToolbarForConfigurationChange(toolbar)

    updateBrowserToolbarLeadingAndNavigationActions(
        context = context,
        redesignEnabled = context.settings().navigationToolbarEnabled,
        isLandscape = context.isLandscape(),
        isTablet = com.shmibblez.inferno.ext.isLargeWindow(context),
        isPrivate = (context.getActivity()!! as HomeActivity).browsingModeManager.mode.isPrivate,
        feltPrivateBrowsingEnabled = context.settings().feltPrivateBrowsingEnabled,
        isWindowSizeSmall = false, // AcornWindowSize.getWindowSize(context) == AcornWindowSize.Small,
    )

    updateBrowserToolbarMenuVisibility()
}

@VisibleForTesting
internal fun updateTabletToolbarActions(isTablet: Boolean, context: Context) {
    // todo: isTablet
//    if (isTablet == this.isTablet) return
//
//    if (isTablet) {
//        addTabletActions(context)
//    } else {
//        removeTabletActions()
//    }
//
//    this.isTablet = isTablet
}

@VisibleForTesting
internal fun addNavigationActions(context: Context) {
    val enableTint = ThemeManager.resolveAttribute(R.attr.textPrimary, context)
    val disableTint = ThemeManager.resolveAttribute(R.attr.textDisabled, context)

    // todo: back action
//    if (backAction == null) {
//        backAction = BrowserToolbar.TwoStateButton(
//            primaryImage = AppCompatResources.getDrawable(
//                context,
//                R.drawable.mozac_ic_back_24,
//            )!!,
//            primaryContentDescription = context.getString(R.string.browser_menu_back),
//            primaryImageTintResource = enableTint,
//            isInPrimaryState = { getSafeCurrentTab()?.content?.canGoBack ?: false },
//            secondaryImageTintResource = disableTint,
//            disableInSecondaryState = true,
//            longClickListener = {
////                    if (!this.isTablet) {
////                        NavigationBar.browserBackLongTapped.record(NoExtras())
////                    }
//                browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
//                    ToolbarMenu.Item.Back(viewHistory = true),
//                )
//            },
//            listener = {
////                    if (!this.isTablet) {
////                        NavigationBar.browserBackTapped.record(NoExtras())
////                    }
//                browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
//                    ToolbarMenu.Item.Back(viewHistory = false),
//                )
//            },
//        ).also {
//            browserToolbarView.view.addNavigationAction(it)
//        }
//    }

    // todo: forward action
//    if (forwardAction == null) {
//        forwardAction = BrowserToolbar.TwoStateButton(
//            primaryImage = AppCompatResources.getDrawable(
//                context,
//                R.drawable.mozac_ic_forward_24,
//            )!!,
//            primaryContentDescription = context.getString(R.string.browser_menu_forward),
//            primaryImageTintResource = enableTint,
//            isInPrimaryState = { getSafeCurrentTab()?.content?.canGoForward ?: false },
//            secondaryImageTintResource = disableTint,
//            disableInSecondaryState = true,
//            longClickListener = {
////                    if (!this.isTablet) {
////                        NavigationBar.browserForwardLongTapped.record(NoExtras())
////                    }
//                browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
//                    ToolbarMenu.Item.Forward(viewHistory = true),
//                )
//            },
//            listener = {
////                    if (!this.isTablet) {
////                        NavigationBar.browserForwardTapped.record(NoExtras())
////                    }
//                browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
//                    ToolbarMenu.Item.Forward(viewHistory = false),
//                )
//            },
//        ).also {
//            browserToolbarView.view.addNavigationAction(it)
//        }
//    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun addTabletActions(context: Context) {
    addNavigationActions(context)

    val enableTint = ThemeManager.resolveAttribute(R.attr.textPrimary, context)
    // todo: refresh action
//    if (refreshAction == null) {
//        refreshAction = BrowserToolbar.TwoStateButton(
//            primaryImage = AppCompatResources.getDrawable(
//                context,
//                R.drawable.mozac_ic_arrow_clockwise_24,
//            )!!,
//            primaryContentDescription = context.getString(R.string.browser_menu_refresh),
//            primaryImageTintResource = enableTint,
//            isInPrimaryState = {
//                getSafeCurrentTab()?.content?.loading == false
//            },
//            secondaryImage = AppCompatResources.getDrawable(
//                context,
//                R.drawable.mozac_ic_stop,
//            )!!,
//            secondaryContentDescription = context.getString(R.string.browser_menu_stop),
//            disableInSecondaryState = false,
//            longClickListener = {
//                browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
//                    ToolbarMenu.Item.Reload(bypassCache = true),
//                )
//            },
//            listener = {
//                if (getCurrentTab()?.content?.loading == true) {
//                    browserToolbarInteractor.onBrowserToolbarMenuItemTapped(ToolbarMenu.Item.Stop)
//                } else {
//                    browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
//                        ToolbarMenu.Item.Reload(bypassCache = false),
//                    )
//                }
//            },
//        ).also {
//            browserToolbarView.view.addNavigationAction(it)
//        }
//    }
}

@VisibleForTesting
internal fun removeNavigationActions() {
    // todo: forward action
//    forwardAction?.let {
//        browserToolbarView.view.removeNavigationAction(it)
//    }
//    forwardAction = null
    // todo: back action
//    backAction?.let {
//        browserToolbarView.view.removeNavigationAction(it)
//    }
//    backAction = null
}

@VisibleForTesting
internal fun removeTabletActions() {
    removeNavigationActions()

    // todo: refresh action
//    refreshAction?.let {
//        browserToolbarView.view.removeNavigationAction(it)
//    }
}

@SuppressLint("VisibleForTests")
private fun updateHistoryMetadata(context: Context) {
    getCurrentTab(context)?.let { tab ->
        (tab as? TabSessionState)?.historyMetadata?.let {
            context.components.core.historyMetadataService.updateMetadata(it, tab)
        }
    }
}

private fun subscribeToTabCollections(context: Context, lifecycleOwner: LifecycleOwner) {
    Observer<List<TabCollection>> {
        context.components.core.tabCollectionStorage.cachedTabCollections = it
    }.also { observer ->
        context.components.core.tabCollectionStorage.getCollections()
            .observe(lifecycleOwner, observer)
    }
}


fun navToQuickSettingsSheet(
    tab: SessionState,
    sitePermissions: SitePermissions?,
    context: Context,
    coroutineScope: CoroutineScope,
    navController: NavController,
) {
    val useCase = context.components.useCases.trackingProtectionUseCases
//        FxNimbus.features.cookieBanners.recordExposure()
    useCase.containsException(tab.id) { hasTrackingProtectionException ->
//        lifecycleScope.launch {
        coroutineScope.launch {
            val cookieBannersStorage = context.components.core.cookieBannersStorage
            val cookieBannerUIMode = cookieBannersStorage.getCookieBannerUIMode(
                context,
                tab,
            )
            withContext(Main) {
                // todo: check if fragment attached
//                runIfFragmentIsAttached {
//                    val isTrackingProtectionEnabled =
//                        tab.trackingProtection.enabled && !hasTrackingProtectionException
//                    val directions = if (context.settings().enableUnifiedTrustPanel) {
//                        BrowserComponentWrapperFragmentDirections.actionBrowserFragmentToTrustPanelFragment(
//                            sessionId = tab.id,
//                            url = tab.content.url,
//                            title = tab.content.title,
//                            isSecured = tab.content.securityInfo.secure,
//                            sitePermissions = sitePermissions,
//                            certificateName = tab.content.securityInfo.issuer,
//                            permissionHighlights = tab.content.permissionHighlights,
//                            isTrackingProtectionEnabled = isTrackingProtectionEnabled,
//                            cookieBannerUIMode = cookieBannerUIMode,
//                        )
//                    } else {
//                        BrowserComponentWrapperFragmentDirections.actionBrowserFragmentToQuickSettingsSheetDialogFragment(
//                            sessionId = tab.id,
//                            url = tab.content.url,
//                            title = tab.content.title,
//                            isSecured = tab.content.securityInfo.secure,
//                            sitePermissions = sitePermissions,
//                            gravity = getAppropriateLayoutGravity(),
//                            certificateName = tab.content.securityInfo.issuer,
//                            permissionHighlights = tab.content.permissionHighlights,
//                            isTrackingProtectionEnabled = isTrackingProtectionEnabled,
//                            cookieBannerUIMode = cookieBannerUIMode,
//                        )
//                    }
//                    nav(navController, R.id.browserFragment, directions)
//                }
            }
        }
    }
}

private fun collectionStorageObserver(
    context: Context,
    navController: NavController,
    view: View,
): TabCollectionStorage.Observer {
    return object : TabCollectionStorage.Observer {
        override fun onCollectionCreated(
            title: String,
            sessions: List<TabSessionState>,
            id: Long?,
        ) {
            showTabSavedToCollectionSnackbar(sessions.size, context, navController, true)
        }

        override fun onTabsAdded(tabCollection: TabCollection, sessions: List<TabSessionState>) {
            showTabSavedToCollectionSnackbar(sessions.size, context, navController)
        }

        fun showTabSavedToCollectionSnackbar(
            tabSize: Int,
            context: Context,
            navController: NavController,
            isNewCollection: Boolean = false,
        ) {
            view?.let {
                val messageStringRes = when {
                    isNewCollection -> {
                        R.string.create_collection_tabs_saved_new_collection
                    }

                    tabSize > 1 -> {
                        R.string.create_collection_tabs_saved
                    }

                    else -> {
                        R.string.create_collection_tab_saved
                    }
                }
                // todo: snackbar
//                Snackbar.make(
//                    snackBarParentView = binding.dynamicSnackbarContainer,
//                    snackbarState = SnackbarState(
//                        message = context.getString(messageStringRes),
//                        action = Action(
//                            label = context.getString(R.string.create_collection_view),
//                            onClick = {
//                                navController.navigate(
//                                    BrowserComponentWrapperFragmentDirections.actionGlobalHome(
//                                        focusOnAddressBar = false,
//                                        scrollToCollection = true,
//                                    ),
//                                )
//                            },
//                        ),
//                    ),
//                ).show()
            }
        }
    }
}

fun getContextMenuCandidates(
    context: Context,
    view: View,
): List<ContextMenuCandidate> {
    val contextMenuCandidateAppLinksUseCases = AppLinksUseCases(
        context,
        { true },
    )

    return ContextMenuCandidate.defaultCandidates(
        context,
        context.components.useCases.tabsUseCases,
        context.components.useCases.contextMenuUseCases,
        view,
        ContextMenuSnackbarDelegate(),
    ) + ContextMenuCandidate.createOpenInExternalAppCandidate(
        context,
        contextMenuCandidateAppLinksUseCases,
    )
}

/**
 * Updates the last time the user was active on the [BrowserFragment].
 * This is useful to determine if the user has to start on the [HomeFragment]
 * or it should go directly to the [BrowserFragment].
 */
@VisibleForTesting
fun updateLastBrowseActivity(context: Context) {
    context.settings().lastBrowseActivity = System.currentTimeMillis()
}

/* new functions */

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