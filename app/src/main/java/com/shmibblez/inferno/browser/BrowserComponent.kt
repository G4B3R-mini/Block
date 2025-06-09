package com.shmibblez.inferno.browser

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.getSystemService
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.shmibblez.inferno.BrowserDirection
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.IntentReceiverActivity
import com.shmibblez.inferno.NavGraphDirections
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.browsingmode.BrowsingMode
import com.shmibblez.inferno.browser.prompts.DownloadComponent
import com.shmibblez.inferno.browser.prompts.InfernoPromptFeatureState
import com.shmibblez.inferno.browser.prompts.InfernoWebPrompter
import com.shmibblez.inferno.browser.prompts.creditcard.InfernoCreditCardDelegate
import com.shmibblez.inferno.browser.prompts.login.InfernoLoginDelegate
import com.shmibblez.inferno.browser.prompts.rememberInfernoPromptFeatureState
import com.shmibblez.inferno.browser.readermode.InfernoReaderViewControls
import com.shmibblez.inferno.browser.readermode.rememberInfernoReaderViewFeatureState
import com.shmibblez.inferno.browser.state.BrowserComponentMode
import com.shmibblez.inferno.browser.state.BrowserComponentState
import com.shmibblez.inferno.browser.tabstrip.isTabStripEnabled
import com.shmibblez.inferno.components.Components
import com.shmibblez.inferno.components.TabCollectionStorage
import com.shmibblez.inferno.components.accounts.FenixFxAEntryPoint
import com.shmibblez.inferno.components.accounts.FxaWebChannelIntegration
import com.shmibblez.inferno.components.appstate.AppAction.MessagingAction
import com.shmibblez.inferno.components.appstate.AppAction.ShoppingAction
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.snackbar.AcornSnackbarHostState
import com.shmibblez.inferno.compose.snackbar.Snackbar
import com.shmibblez.inferno.compose.snackbar.SnackbarHost
import com.shmibblez.inferno.customtabs.ExternalAppBrowserActivity
import com.shmibblez.inferno.downloads.DownloadService
import com.shmibblez.inferno.downloads.dialog.DynamicDownloadDialog
import com.shmibblez.inferno.ext.DEFAULT_ACTIVE_DAYS
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.consumeFlow
import com.shmibblez.inferno.ext.dpToPx
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.ext.isLargeWindow
import com.shmibblez.inferno.ext.nav
import com.shmibblez.inferno.ext.navigateWithBreadcrumb
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.findInPageBar.BrowserFindInPageBar
import com.shmibblez.inferno.home.CrashComponent
import com.shmibblez.inferno.home.HomeFragment
import com.shmibblez.inferno.home.InfernoHomeComponent
import com.shmibblez.inferno.library.bookmarks.friendlyRootTitle
import com.shmibblez.inferno.loading.InfernoLoadingComponent
import com.shmibblez.inferno.messaging.FenixMessageSurfaceId
import com.shmibblez.inferno.messaging.MessagingFeature
import com.shmibblez.inferno.microsurvey.ui.ext.MicrosurveyUIData
import com.shmibblez.inferno.perf.MarkersFragmentLifecycleCallbacks
import com.shmibblez.inferno.settings.SupportUtils
import com.shmibblez.inferno.settings.biometric.BiometricPromptFeature
import com.shmibblez.inferno.shopping.DefaultShoppingExperienceFeature
import com.shmibblez.inferno.shopping.ReviewQualityCheckFeature
import com.shmibblez.inferno.shortcut.PwaOnboardingObserver
import com.shmibblez.inferno.tabbar.InfernoTabBar
import com.shmibblez.inferno.tabs.tabstray.InfernoTabsTray
import com.shmibblez.inferno.tabs.tabstray.InfernoTabsTrayDisplayType
import com.shmibblez.inferno.tabs.tabstray.InfernoTabsTrayMode
import com.shmibblez.inferno.tabs.tabstray.InfernoTabsTraySelectedTab
import com.shmibblez.inferno.tabstray.TabsTrayFragmentDirections
import com.shmibblez.inferno.tabstray.ext.isActiveDownload
import com.shmibblez.inferno.tabstray.ext.isNormalTab
import com.shmibblez.inferno.theme.ThemeManager
import com.shmibblez.inferno.toolbar.InfernoExternalToolbar
import com.shmibblez.inferno.toolbar.InfernoToolbar
import com.shmibblez.inferno.toolbar.ToolbarMenuBottomSheet
import com.shmibblez.inferno.wifi.SitePermissionsWifiIntegration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.appservices.places.BookmarkRoot
import mozilla.appservices.places.uniffi.PlacesApiException
import mozilla.components.browser.engine.gecko.GeckoEngineView
import mozilla.components.browser.state.action.DebugAction
import mozilla.components.browser.state.action.LastAccessAction
import mozilla.components.browser.state.action.RecentlyClosedAction
import mozilla.components.browser.state.selector.findCustomTabOrSelectedTab
import mozilla.components.browser.state.selector.findTab
import mozilla.components.browser.state.selector.findTabOrCustomTab
import mozilla.components.browser.state.selector.findTabOrCustomTabOrSelectedTab
import mozilla.components.browser.state.selector.getNormalOrPrivateTabs
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.CustomTabSessionState
import mozilla.components.browser.state.state.SessionState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.content.DownloadState
import mozilla.components.browser.state.state.content.DownloadState.Status
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.thumbnails.BrowserThumbnails
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.engine.mediasession.MediaSession
import mozilla.components.concept.engine.permission.SitePermissions
import mozilla.components.concept.engine.prompt.ShareData
import mozilla.components.concept.storage.LoginEntry
import mozilla.components.feature.accounts.push.CloseTabsUseCases
import mozilla.components.feature.downloads.manager.FetchDownloadManager
import mozilla.components.feature.downloads.temporary.CopyDownloadFeature
import mozilla.components.feature.downloads.temporary.ShareDownloadFeature
import mozilla.components.feature.downloads.ui.DownloadCancelDialogFragment
import mozilla.components.feature.media.fullscreen.MediaSessionFullscreenFeature
import mozilla.components.feature.privatemode.feature.SecureWindowFeature
import mozilla.components.feature.prompts.address.AddressDelegate
import mozilla.components.feature.prompts.dialog.FullScreenNotificationToast
import mozilla.components.feature.prompts.dialog.GestureNavUtils
import mozilla.components.feature.prompts.share.ShareDelegate
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
import mozilla.components.lib.state.DelicateAction
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.service.sync.autofill.DefaultCreditCardValidationDelegate
import mozilla.components.service.sync.logins.DefaultLoginValidationDelegate
import mozilla.components.service.sync.logins.LoginsApiException
import mozilla.components.service.sync.logins.SyncableLoginsStorage
import mozilla.components.support.base.feature.ActivityResultHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.android.content.share
import mozilla.components.support.ktx.android.view.enterImmersiveMode
import mozilla.components.support.ktx.android.view.exitImmersiveMode
import mozilla.components.support.ktx.android.view.hideKeyboard
import mozilla.components.support.ktx.kotlin.getOrigin
import mozilla.components.support.utils.ext.isLandscape
import mozilla.components.ui.widgets.VerticalSwipeRefreshLayout
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt

// todo:
//   - make new tab next to current based on config, default is true
//     - so far called from [BrowserTabBar] and from [TabTrayComponent]

// todo:
//  - change app name to ember, package too
//  - splash screen, customize: https://medium.com/geekculture/implementing-the-perfect-splash-screen-in-android-295de045a8dc
//    - add huge drawable with diagonal red and black in center (for ember will be gradient, bottom black top red)
//    - center inferno logo drawable (for ember will be fire icon with white rounded letters below spelling "ember")
// todo: from fragment_browser.xml, for below views first wrap views with AndroidView, then
//  progressively implement in compose
//

// todo: test
//   4. crash_reporter_view CrashContentView (com.shmibblez.inferno/crashes/CrashContentIntegration.kt)
//   6. dynamicSnackbarContainer
//   7. loginSelectBar, implemented in PromptComponent
//   9. addressSelectBar AddressSelectBar, implemented in PromptComponent
//   10. creditCardSelectBar CreditCardSelectBar, implemented in PromptComponent
//     - test biometric authentication in SelectableListPrompt for credit cards (urgent)
//   11. tabPreview (TabPreview moved to toolbar (swipe to switch tabs))


// todo: test
//   - savedLoginsLauncher

// todo (long term):
//  - add param callbacks in components (ex, DownloadComponent) for custom composable components
//    (download dialogs, web dialogs, etc) and invoke where necessary, allows more customizability
//    -> callbacks provide composable params, and composable fun is returned and called in component
// todo:
//  - fix external app browser implementation
//  - animated splash screen not animating
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
//    - all components should have colors baked in with theme
//    - add constructor for InfernoText with just text, modifier, and text type (theme/color, font size,
//      font weight, etc. all auto based on type (title, normal, subtitle, description, etc))
//      - abstracts text/base components for next material update, need to only update base components, not all instances

//companion object {
//private const val KEY_CUSTOM_TAB_SESSION_ID = "custom_tab_session_id"
//private const val REQUEST_CODE_DOWNLOAD_PERMISSIONS = 1
//private const val REQUEST_CODE_PROMPT_PERMISSIONS = 2
//private const val REQUEST_CODE_APP_PERMISSIONS = 3
//private const val METRIC_SOURCE = "page_action_menu"
//private const val TOAST_METRIC_SOURCE = "add_bookmark_toast"
//private const val LAST_SAVED_GENERATED_PASSWORD = "last_saved_generated_password"

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
//const val READER_MODE_WEIGHT = 1
//const val TRANSLATIONS_WEIGHT = 2
const val REVIEW_QUALITY_CHECK_WEIGHT = 3
const val SHARE_WEIGHT = 4

//const val RELOAD_WEIGHT = 5
const val OPEN_IN_ACTION_WEIGHT = 6
//}

//private const val NAVIGATION_CFR_VERTICAL_OFFSET = 10
//private const val NAVIGATION_CFR_ARROW_OFFSET = 24
//private const val NAVIGATION_CFR_MAX_MS_BETWEEN_CLICKS = 5000

fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

fun nav(
    navController: NavController,
    @IdRes id: Int?,
    directions: NavDirections,
    options: NavOptions? = null,
) {
    navController.nav(id, directions, options)
}

enum class BrowserComponentPageType {
    CRASH, ENGINE, HOME, HOME_PRIVATE
}

object UiConst {
    const val BAR_BG_ALPHA = 0.74F
    val TOP_BAR_INTERNAL_PADDING = 16.dp

    val TOOLBAR_HEIGHT = 44.dp
    val TAB_BAR_HEIGHT = 36.dp
    val AWESOME_BAR_HEIGHT = 200.dp
    val TAB_WIDTH = 112.dp
    val EXTERNAL_TOOLBAR_HEIGHT = 62.dp // TOOLBAR_HEIGHT + TAB_BAR_HEIGHT

    //    val TAB_CORNER_RADIUS = 8.dp
    val FIND_IN_PAGE_BAR_HEIGHT = 50.dp
    val READER_VIEW_HEIGHT = 50.dp
    val PROGRESS_BAR_HEIGHT = 2.dp
    private val FULLSCREEN_BOTTOM_BAR_HEIGHT = 0.dp
    fun calcBottomBarHeight(browserComponentMode: BrowserComponentMode): Dp {
        return when (browserComponentMode) {
            BrowserComponentMode.TOOLBAR -> TOOLBAR_HEIGHT + TAB_BAR_HEIGHT
            BrowserComponentMode.TOOLBAR_SEARCH -> TOOLBAR_HEIGHT + AWESOME_BAR_HEIGHT
            BrowserComponentMode.TOOLBAR_EXTERNAL -> EXTERNAL_TOOLBAR_HEIGHT
            BrowserComponentMode.FIND_IN_PAGE -> FIND_IN_PAGE_BAR_HEIGHT
            BrowserComponentMode.READER_VIEW -> READER_VIEW_HEIGHT
            BrowserComponentMode.FULLSCREEN -> FULLSCREEN_BOTTOM_BAR_HEIGHT
        }
    }
}


@OptIn(ExperimentalCoroutinesApi::class, DelicateAction::class)
@Composable
@SuppressLint(
    "UnusedMaterialScaffoldPaddingParameter",
    "VisibleForTests",
    "UnusedMaterial3ScaffoldPaddingParameter"
)

// todo: home nav args, do stuff like new tab, new tab specific url, deeplink url, etc
fun BrowserComponent(
    navController: NavController,
    state: BrowserComponentState,
    onNavToSettings: () -> Unit,
    onNavToHistory: () -> Unit,
) {
    Log.d("BrowserComponent", "rebuilt")
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val store = context.components.core.store
    val view = LocalView.current
//    val localConfiguration = LocalConfiguration.current
    val parentFragmentManager = context.getActivity()!!.supportFragmentManager
    val snackbarHostState = remember { AcornSnackbarHostState() }
    var activeAlertDialog by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    // tabs tray vars
    var showTabsTray by remember { mutableStateOf(false) }
    var tabsTrayMode by remember { mutableStateOf<InfernoTabsTrayMode>(InfernoTabsTrayMode.Normal) }
    fun dismissTabsTray() {
        showTabsTray = false
        tabsTrayMode = InfernoTabsTrayMode.Normal
    }

    /* BrowserFragment vars */
    val windowFeature = ViewBoundFeatureWrapper<WindowFeature>()
//    val openInAppOnboardingObserver = ViewBoundFeatureWrapper<OpenInAppOnboardingObserver>()
    val reviewQualityCheckFeature = ViewBoundFeatureWrapper<ReviewQualityCheckFeature>()
//    val translationsBinding = ViewBoundFeatureWrapper<TranslationsBinding>()

    var reviewQualityCheckAvailable by remember {
        mutableStateOf(
            false
        )
    }
    var pwaOnboardingObserver: PwaOnboardingObserver? = null

    /* BrowserFragment  vars */

    /* BaseBrowserFragment vars */
    lateinit var browserAnimator: BrowserAnimator

    val thumbnailsFeature = remember { ViewBoundFeatureWrapper<BrowserThumbnails>() }

    @VisibleForTesting val messagingFeatureMicrosurvey = ViewBoundFeatureWrapper<MessagingFeature>()

    val sessionFeature = remember { ViewBoundFeatureWrapper<SessionFeature>() }
    val shareDownloadsFeature = remember { ViewBoundFeatureWrapper<ShareDownloadFeature>() }
    val copyDownloadsFeature = remember { ViewBoundFeatureWrapper<CopyDownloadFeature>() }

    val sitePermissionsFeature = ViewBoundFeatureWrapper<SitePermissionsFeature>()
    val fullScreenFeature = ViewBoundFeatureWrapper<FullScreenFeature>()
    val swipeRefreshFeature = ViewBoundFeatureWrapper<SwipeRefreshFeature>()
    val webchannelIntegration = ViewBoundFeatureWrapper<FxaWebChannelIntegration>()
    val sitePermissionWifiIntegration = ViewBoundFeatureWrapper<SitePermissionsWifiIntegration>()
    val secureWindowFeature = ViewBoundFeatureWrapper<SecureWindowFeature>()
    val fullScreenMediaSessionFeature = ViewBoundFeatureWrapper<MediaSessionFullscreenFeature>()
    val searchFeature = ViewBoundFeatureWrapper<SearchFeature>()
    val webAuthnFeature = ViewBoundFeatureWrapper<WebAuthnFeature>()
    val screenOrientationFeature = ViewBoundFeatureWrapper<ScreenOrientationFeature>()
    val biometricPromptFeature = ViewBoundFeatureWrapper<BiometricPromptFeature>()
//    val crashContentIntegration = ViewBoundFeatureWrapper<CrashContentIntegration>()
//    val openInFirefoxBinding = ViewBoundFeatureWrapper<OpenInFirefoxBinding>()

    var pipFeature by remember { mutableStateOf<PictureInPictureFeature?>(null) }

    val browserInitialized by remember { mutableStateOf(false) }
    val initUIJob by remember { mutableStateOf<Job?>(null) }

    // todo:
//    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED) var webAppToolbarShouldBeVisible =
//        true

    var lastSavedGeneratedPassword by remember {
        mutableStateOf<String?>(
            null
        )
    }

    /// old component features
//    val webExtensionPromptFeature =
//        remember { ViewBoundFeatureWrapper<WebExtensionPromptFeature>() }

    var requestDownloadPermissionsCallback by remember {
        mutableStateOf<((permissions: Array<String>, grantResults: IntArray) -> Unit)?>(
            null
        )
    }
    // permission launchers
    val requestDownloadPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val permissions = results.keys.toTypedArray()
            val grantResults = results.values.map {
                if (it) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
            }.toIntArray()
            requestDownloadPermissionsCallback?.invoke(permissions, grantResults)
        }
    val requestSitePermissionsLauncher: ActivityResultLauncher<Array<String>> =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val permissions = results.keys.toTypedArray()
            val grantResults = results.values.map {
                if (it) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
            }.toIntArray()
            sitePermissionsFeature.withFeature {
                it.onPermissionsResult(permissions, grantResults)
            }
        }
    var tempWebPrompterState by remember { mutableStateOf<InfernoPromptFeatureState?>(null) }
    val requestPromptsPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val permissions = results.keys.toTypedArray()
            val grantResults = results.values.map {
                if (it) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
            }.toIntArray()
            tempWebPrompterState?.onPermissionsResult(permissions, grantResults)
        }

    // connection to the nested scroll system and listen to the scroll
    var bottomBarHeightDp by remember {
        mutableStateOf(
            UiConst.calcBottomBarHeight(
                BrowserComponentMode.TOOLBAR
            )
        )
    }

    LaunchedEffect(state.browserMode) {
        bottomBarHeightDp = UiConst.calcBottomBarHeight(state.browserMode)
    }

    val bottomBarOffsetPx = remember { Animatable(0F) }


    val webPrompterState = rememberInfernoPromptFeatureState(
        activity = context.getActivity()!!,
        store = store,
        customTabId = state.customTabSessionId,
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
//            requestPermissions(permissions, BaseBrowserFragment.REQUEST_CODE_PROMPT_PERMISSIONS)
            requestPromptsPermissionsLauncher.launch(permissions)
        },
        loginDelegate = object : InfernoLoginDelegate {
            override val onManageLogins = {
                browserAnimator.captureEngineViewAndDrawStatically {
                    val directions = NavGraphDirections.actionGlobalSavedLoginsAuthFragment()
                    navController.navigate(directions)
                }
            }
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
                setLastSavedGeneratedPassword = { lastSavedGeneratedPassword = it },
            )
        },
        onSaveLogin = { isUpdate ->
            showSnackbarAfterLoginChange(
                isUpdate = isUpdate,
                context = context,
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState,
            )
        },
        hideUpdateFragmentAfterSavingGeneratedPassword = { username, password ->
            hideUpdateFragmentAfterSavingGeneratedPassword(
                username = username,
                password = password,
                lastSavedGeneratedPassword = lastSavedGeneratedPassword
            )
        },
        removeLastSavedGeneratedPassword = {
            removeLastSavedGeneratedPassword(
                setLastSavedGeneratedPassword = {
                    lastSavedGeneratedPassword = it
                },
            )
        },
        creditCardDelegate = { prompterState, activityResultLauncher ->
            object : InfernoCreditCardDelegate {
                override val onManageCreditCards = {
                    val directions = NavGraphDirections.actionGlobalAutofillSettingFragment()
                    navController.navigate(directions)
                }
                override val onSelectCreditCard = {
                    showBiometricPrompt(
                        context = context,
                        biometricPromptFeature = biometricPromptFeature,
                        webPrompterState = prompterState,
                        startForResult = activityResultLauncher,
                        setAlertDialog = { activeAlertDialog = it },
                    )
                }
            }
        },
        addressDelegate = object : AddressDelegate {
            override val addressPickerView
                get() = null
            override val onManageAddresses = {
                val directions = NavGraphDirections.actionGlobalAutofillSettingFragment()
                navController.navigate(directions)
            }
        },
    )
    tempWebPrompterState = webPrompterState

    val infernoReaderViewState = rememberInfernoReaderViewFeatureState(
        context = context,
        engine = context.components.core.engine,
        store = store,
        onReaderViewStatusChange = { _, active ->
            when (active) {
                true -> state.setBrowserModeReaderView()
                false -> state.setBrowserModeToolbar()
            }
        },
    )

    val backHandler = OnBackPressedHandler(
        context = context,
        toolbarBackPressedHandler = {
            if (state.browserMode != BrowserComponentMode.TOOLBAR) {
                state.setBrowserModeToolbar()
                true
            } else {
                false
            }
        },
        readerViewBackPressedHandler = {
            if (infernoReaderViewState.active) {
                infernoReaderViewState.hideReaderView()
                true
            } else {
                false
            }
        },
        fullScreenFeature = fullScreenFeature,
//        promptsFeature = promptsFeature,
        sessionFeature = sessionFeature,
    )

    // show alert dialog if not null
    if (activeAlertDialog != null) {
        activeAlertDialog!!.invoke()
    }
    // bottom sheet menu setup
    var showToolbarMenuBottomSheet by remember { mutableStateOf(false) }
    if (showToolbarMenuBottomSheet) {
        ToolbarMenuBottomSheet(
            tabSessionState = state.currentTab,
            loading = state.currentTab?.content?.loading ?: false,
            tabCount = state.tabList.size,
            onDismissMenuBottomSheet = { showToolbarMenuBottomSheet = false },
            onActivateFindInPage = { state.setBrowserModeFindInPage() },
            onActivateReaderView = {
                val successful = infernoReaderViewState.showReaderView()
                if (successful) {
                    state.setBrowserModeReaderView()
                }
            },
            onRequestSearchBar = { /* todo */ },
            onNavToSettings = onNavToSettings,
            onNavToTabsTray = { showTabsTray = true },
            onNavToHistory = onNavToHistory,
        )
    }

    if (showTabsTray) {
        // request screenshot before showing
        thumbnailsFeature.get()?.requestScreenshot()
        // todo: missing functionality from [TabsTrayFragment], [DefaultTabsTrayInteractor], and [DefaultTabsTrayController]
        InfernoTabsTray(
            dismiss = ::dismissTabsTray,
            mode = tabsTrayMode,
            setMode = { mode -> tabsTrayMode = mode },
            activeTabId = state.currentTab?.id,
            normalTabs = state.normalTabs,
            privateTabs = state.privateTabs,
            recentlyClosedTabs = state.closedTabs,
            tabDisplayType = InfernoTabsTrayDisplayType.List,
            selectedTabsTrayTab = state.selectedTabsTrayTab,
            onBookmarkSelectedTabsClick = {
                CoroutineScope(IO).launch {
                    Result.runCatching {
                        val bookmarksStorage = context.components.core.bookmarksStorage
                        val parentGuid =
                            bookmarksStorage.getRecentBookmarks(1).firstOrNull()?.parentGuid
                                ?: BookmarkRoot.Mobile.id

                        val parentNode = bookmarksStorage.getBookmark(parentGuid)

                        tabsTrayMode.selectedTabs.forEach { tab ->
                            bookmarksStorage.addItem(
                                parentGuid = parentNode!!.guid,
                                url = tab.content.url,
                                title = tab.content.title,
                                position = null,
                            )
                        }
                        withContext(Main) {
                            showBookmarkSnackbar(
                                tabsTrayMode.selectedTabs.size,
                                parentNode?.title,
                                context = context,
                                coroutineScope = coroutineScope,
                                snackbarHostState = snackbarHostState,
                                navController = navController,
                                dismissTabsTray = ::dismissTabsTray
                            )
                        }
                    }.getOrElse {
                        // silently fail
                    }
                }
                tabsTrayMode = InfernoTabsTrayMode.Normal
            },
            onDeleteSelectedTabsClick = {
                /**
                 * Helper function to delete multiple tabs and offer an undo option.
                 */
                fun deleteMultipleTabs(tabs: Collection<TabSessionState>) {
                    val isPrivate = tabs.any { it.content.private }

                    // If user closes all the tabs from selected tabs page dismiss tray and navigate home.
                    if (tabs.size == context.components.core.store.state.getNormalOrPrivateTabs(
                            isPrivate
                        ).size
                    ) {
                        tabs.map { it.id }.let {
                            context.components.useCases.tabsUseCases.removeTabs(it)
                        }
                        dismissTabsTray()
                        // todo: select last private or normal tab depending on type deleted
//                        dismissTabsTrayAndNavigateHome(
//                            if (isPrivate) HomeFragment.ALL_PRIVATE_TABS else HomeFragment.ALL_NORMAL_TABS,
//                        )
                    } else {
                        tabs.map { it.id }.let {
                            context.components.useCases.tabsUseCases.removeTabs(it)
                        }
                    }
                    showUndoSnackbarForTab(isPrivate, context, coroutineScope, snackbarHostState)
                }

                val tabs = tabsTrayMode.selectedTabs
                deleteMultipleTabs(tabs)
//                TabsTray.closeSelectedTabs.record(TabsTray.CloseSelectedTabsExtra(tabCount = tabs.size))
                tabsTrayMode = InfernoTabsTrayMode.Normal
            },
            onForceSelectedTabsAsInactiveClick = {
                val numDays: Long = DEFAULT_ACTIVE_DAYS + 1
                val tabs = tabsTrayMode.selectedTabs
                val currentTabId = context.components.core.store.state.selectedTabId
                tabs.filterNot { it.id == currentTabId }.forEach { tab ->
                    val daysSince = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(numDays)
                    context.components.core.store.apply {
                        dispatch(LastAccessAction.UpdateLastAccessAction(tab.id, daysSince))
                        dispatch(DebugAction.UpdateCreatedAtAction(tab.id, daysSince))
                    }
                }

                tabsTrayMode = InfernoTabsTrayMode.Normal
            },
            onTabSettingsClick = {
                navController.navigate(
                    TabsTrayFragmentDirections.actionGlobalTabSettingsFragment(),
                )
            },
            onHistoryClick = {
                navController.navigate(
                    TabsTrayFragmentDirections.actionGlobalHistoryFragment()
                )
            },
            onShareAllTabsClick = {
                // todo:
//                if (tabsTrayStore.state.selectedPage == Page.NormalTabs) {
//                    tabsTrayStore.dispatch(TabsTrayAction.ShareAllNormalTabs)
//                } else if (tabsTrayStore.state.selectedPage == Page.PrivateTabs) {
//                    tabsTrayStore.dispatch(TabsTrayAction.ShareAllPrivateTabs)
//                }
//
//                navigationInteractor.onShareTabsOfTypeClicked(
//                    private = tabsTrayStore.state.selectedPage == Page.PrivateTabs,
//                )
            },
            onDeleteAllTabsClick = {

                when (state.selectedTabsTrayTab) {
                    InfernoTabsTraySelectedTab.NormalTabs -> {
                        context.components.useCases.tabsUseCases.removeNormalTabs.invoke()
                        showUndoSnackbarForTab(
                            isPrivate = false,
                            context = context,
                            coroutineScope = coroutineScope,
                            snackbarHostState = snackbarHostState,
                        )
                    }

                    InfernoTabsTraySelectedTab.PrivateTabs -> {
                        context.components.useCases.tabsUseCases.removePrivateTabs.invoke()
                        showUndoSnackbarForTab(
                            isPrivate = true,
                            context = context,
                            coroutineScope = coroutineScope,
                            snackbarHostState = snackbarHostState,
                        )
                    }

                    else -> {}
                }

//                if (tabsTrayStore.state.selectedPage == Page.NormalTabs) {
//                    tabsTrayStore.dispatch(TabsTrayAction.CloseAllNormalTabs)
//                } else if (tabsTrayStore.state.selectedPage == Page.PrivateTabs) {
//                    tabsTrayStore.dispatch(TabsTrayAction.CloseAllPrivateTabs)
//                }
//
//                navigationInteractor.onCloseAllTabsClicked(
//                    private = tabsTrayStore.state.selectedPage == Page.PrivateTabs,
//                )
            },
            onAccountSettingsClick = {
                val isSignedIn =
                    context.components.backgroundServices.accountManager.authenticatedAccount() != null

                val direction = if (isSignedIn) {
                    TabsTrayFragmentDirections.actionGlobalAccountSettingsFragment()
                } else {
                    TabsTrayFragmentDirections.actionGlobalTurnOnSync(entrypoint = FenixFxAEntryPoint.NavigationInteraction)
                }
                navController.navigate(direction)
            },
            onTabClick = { tab ->
                fun getTabPositionFromId(tabsList: List<TabSessionState>, tabId: String): Int {
                    tabsList.forEachIndexed { index, tab -> if (tab.id == tabId) return index }
                    return -1
                }
                run outer@{
                    if (!context.settings().hasShownTabSwipeCFR && !context.isTabStripEnabled() && context.settings().isSwipeToolbarToSwitchTabsEnabled) {
//                        val normalTabs = context.components.core.store.state.normalTabs
                        val currentTabId = state.currentTab?.id

                        if (state.normalTabs.size >= 2) {
                            val currentTabPosition =
                                currentTabId?.let { getTabPositionFromId(state.normalTabs, it) }
                                    ?: return@outer
                            val newTabPosition = getTabPositionFromId(state.normalTabs, tab.id)

                            if (abs(currentTabPosition - newTabPosition) == 1) {
                                context.settings().shouldShowTabSwipeCFR = true
                            }
                        }
                    }
                }

                val selected = tabsTrayMode.selectedTabs
                when {
                    selected.isEmpty() && tabsTrayMode.isNormal() -> {
//                TabsTray.openedExistingTab.record(TabsTray.OpenedExistingTabExtra(source ?: "unknown"))
                        context.components.useCases.tabsUseCases.selectTab(tab.id)
                        dismissTabsTray()
                    }

                    selected.contains(tab) -> {
                        tabsTrayMode = InfernoTabsTrayMode.Select(selected - tab)
                    }

                    else -> {
                        tabsTrayMode = InfernoTabsTrayMode.Select(selected + tab)
                    }
                }

//                tabsTrayInteractor.onTabSelected(tab, TABS_TRAY_FEATURE_NAME)
            },
            onTabClose = { closedTab ->
                fun deleteTab(tabId: String, source: String?, isConfirmed: Boolean) {
                    val browserStore = context.components.core.store
                    val tab = browserStore.state.findTab(tabId)
                    tab?.let {
                        val isLastTab =
                            browserStore.state.getNormalOrPrivateTabs(it.content.private).size == 1
                        val isCurrentTab = browserStore.state.selectedTabId.equals(tabId)
                        if (!isLastTab || !isCurrentTab) {
                            context.components.useCases.tabsUseCases.removeTab(tabId)
                            showUndoSnackbarForTab(
                                isPrivate = it.content.private,
                                context = context,
                                coroutineScope = coroutineScope,
                                snackbarHostState = snackbarHostState,
                                setInitiallySelectedTabTray = { selectedTab ->
                                    state.selectedTabsTrayTab = selectedTab
                                },
                                tab = tab,
                            )
                        } else {
                            val privateDownloads = browserStore.state.downloads.filter { map ->
                                map.value.private && map.value.isActiveDownload()
                            }
                            if (!isConfirmed && privateDownloads.isNotEmpty()) {
                                showCancelledDownloadWarning(downloadCount = privateDownloads.size,
                                    tabId = tabId,
                                    source = source,
                                    context = context,
                                    coroutineScope = coroutineScope,
                                    setAlertDialog = { activeAlertDialog = it },
                                    snackbarHostState = snackbarHostState,
                                    setInitiallySelectedTabTray = { selectedTab: InfernoTabsTraySelectedTab ->
                                        state.selectedTabsTrayTab = selectedTab
                                    },
                                    dismissTabsTray = { dismissTabsTray() })
                                return
                            } else {
                                dismissTabsTray()
                                context.components.useCases.tabsUseCases.removeTab(tabId)
                            }
                        }
//            TabsTray.closedExistingTab.record(TabsTray.ClosedExistingTabExtra(source ?: "unknown"))
                    }
                }

                deleteTab(closedTab.id, null, isConfirmed = false)
            },
            onTabMediaClick = { tab ->
                when (tab.mediaSessionState?.playbackState) {
                    MediaSession.PlaybackState.PLAYING -> {
//                GleanTab.mediaPause.record(NoExtras())
                        tab.mediaSessionState?.controller?.pause()
                    }

                    MediaSession.PlaybackState.PAUSED -> {
//                GleanTab.mediaPlay.record(NoExtras())
                        tab.mediaSessionState?.controller?.play()
                    }

                    else -> throw AssertionError(
                        "Play/Pause button clicked without play/pause state.",
                    )
                }
            },
            onTabMove = { tabId, targetId, placeAfter ->
                if (targetId != null && tabId != targetId) {
                    context.components.useCases.tabsUseCases.moveTabs(
                        listOf(tabId), targetId, placeAfter
                    )
                }
            },
            onTabLongClick = { tab ->
                // set mode select and add pressed tab
                if (tabsTrayMode.isNormal()) {
                    when (state.selectedTabsTrayTab) {
                        InfernoTabsTraySelectedTab.NormalTabs -> {
                            tabsTrayMode = InfernoTabsTrayMode.Select(setOf(tab))
                        }

                        InfernoTabsTraySelectedTab.PrivateTabs -> {
                            tabsTrayMode = InfernoTabsTrayMode.Select(setOf(tab))
                        }

                        else -> {}
                    }
                }
            },
            onSyncedTabClick = { tab ->
                dismissTabsTray()
                (context.getActivity()!! as HomeActivity).openToBrowserAndLoad(
                    searchTermOrURL = tab.active().url,
                    newTab = true,
                    from = BrowserDirection.FromTabsTray,
                )
            },
            onSyncedTabClose = { deviceId, tab ->
                val closeSyncedTabsUseCases = context.components.useCases.closeSyncedTabsUseCases
                CoroutineScope(IO).launch {
                    val operation = closeSyncedTabsUseCases.close(deviceId, tab.active().url)
                    withContext(Main) {
                        showUndoSnackbarForSyncedTab(
                            closeOperation = operation,
                            context = context,
                            coroutineScope = coroutineScope,
                            snackbarHostState = snackbarHostState,
                        )
                    }
                }
            },
            onClosedTabClick = { tabState ->
                val selected = tabsTrayMode.selectedClosedTabs
                when {
                    // restore tab
                    tabsTrayMode.isNormal() -> {
                        val recentlyClosedTabsStorage =
                            context.components.core.recentlyClosedTabsStorage.value
                        val tabsUseCases = context.components.useCases.tabsUseCases
                        val browserStore = context.components.core.store
//            RecentlyClosedTabs.openTab.record(NoExtras())
                        // restore tab
                        coroutineScope.launch {
                            tabsUseCases.restore(
                                tabState,
                                recentlyClosedTabsStorage.engineStateStorage(),
                            )
                            // remove restored tab from recently closed storage
                            browserStore.dispatch(
                                RecentlyClosedAction.RemoveClosedTabAction(
                                    tabState
                                )
                            )
                        }
                        dismissTabsTray()
//                TabsTray.openedExistingTab.record(TabsTray.OpenedExistingTabExtra(source ?: "unknown"))
                    }
                    // if in selection mode and selected, deselect
                    selected.contains(tabState) -> {
                        tabsTrayMode = InfernoTabsTrayMode.SelectClosed(selected - tabState)
                    }
                    // if in selection mode and not selected, select
                    else -> {
                        tabsTrayMode = InfernoTabsTrayMode.SelectClosed(selected + tabState)
                    }
                }
            },
            onClosedTabClose = { tabState ->
                val browserStore = context.components.core.store
                browserStore.dispatch(RecentlyClosedAction.RemoveClosedTabAction(tabState))
            },
            onClosedTabLongClick = { tabState ->
                if (tabsTrayMode.isNormal()) {
                    tabsTrayMode = InfernoTabsTrayMode.SelectClosed(setOf(tabState))
                }
            },
            onDeleteSelectedCloseTabsClick = {
                val browserStore = context.components.core.store
                for (tabState in tabsTrayMode.selectedClosedTabs) {
                    browserStore.dispatch(RecentlyClosedAction.RemoveClosedTabAction(tabState))
                }
                tabsTrayMode = InfernoTabsTrayMode.Normal
            },
        )
    }

    val downloadManager = remember {
        FetchDownloadManager(
            context.applicationContext,
            store,
            DownloadService::class,
            notificationsDelegate = context.components.notificationsDelegate,
        )
    }

    DownloadComponent(
        applicationContext = context.applicationContext,
        store = store,
        useCases = context.components.useCases.downloadUseCases,
        customTabSessionId = state.customTabSessionId,
        downloadManager = downloadManager,
        shouldForwardToThirdParties = {
            context.settings().shouldUseExternalDownloadManager
//            PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
//                context.getPreferenceKey(R.string.pref_key_external_download_manager),
//                false, // todo: test if true
//            )
        },
        onNeedToRequestPermissions = { permissions ->
            // todo: test
            requestDownloadPermissionsLauncher.launch(permissions)
//            requestPermissions(permissions, REQUEST_CODE_DOWNLOAD_PERMISSIONS)
        },
        onCannotOpenFile = {
            showCannotOpenFileError(
                context = context,
                downloadState = it,
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState,
            )
        },
        useCustomFirstPartyDownloadPrompt = true,
        useCustomThirdPartyDownloadDialog = true,
        setOnPermissionsResultCallback = {
            requestDownloadPermissionsCallback = it
        },
    )

    InfernoWebPrompter(
        state = webPrompterState,
        currentTab = state.currentTab,
    )

    /// views
    var engineView by remember { mutableStateOf<EngineView?>(null) }
    var swipeRefresh by remember { mutableStateOf<SwipeRefreshLayout?>(null) }

    /// event handlers
    val activityResultHandler: List<ActivityResultHandler?> = listOf(
        webAuthnFeature.get(), webPrompterState,
//        promptsFeature,
    )
    // sets parent fragment handler for onActivityResult
    state.setOnActivityResultHandler { result: OnActivityResultModel ->
        Logger.info(
            "Fragment onActivityResult received with " + "requestCode: ${result.requestCode}, resultCode: ${result.resultCode}, data: ${result.data}",
        )

        // feature activity result handler
        activityResultHandler.any {
            it?.onActivityResult(
                result.requestCode, result.data, result.resultCode
            ) ?: false
        }

    }

    // currently set to 0 always, todo: only do this if dynamic toolbar enabled
    fun setEngineDynamicToolbarMaxHeight(h: Int = 0) {
//        engineView?.setDynamicToolbarMaxHeight(h)
        engineView?.setDynamicToolbarMaxHeight(h)
        engineView?.setVerticalClipping(h)
    }

    // on back pressed handlers
    BackHandler {
        onBackPressed(backHandler)
    }

    // moz components setup and shared preferences
    LaunchedEffect(engineView == null) {
        if (engineView == null) return@LaunchedEffect
//        setEngineDynamicToolbarMaxHeight(bottomBarHeightDp.dpToPx(context) - bottomBarOffsetPx.value.toInt())
        setEngineDynamicToolbarMaxHeight(0)

        /* BaseBrowserFragment onViewCreated */
        // DO NOT ADD ANYTHING ABOVE THIS getProfilerTime CALL!
        val profilerStartTime = context.components.core.engine.profiler?.getProfilerTime()

        fun initializeUI() {

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

            if (context.settings().microsurveyFeatureEnabled) {
                listenForMicrosurveyMessage(context, lifecycleOwner)
            }


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

            // todo: nav to quick settings when site security icons clicked
//    browserToolbarView.view.display.setOnSiteSecurityClickedListener {
//        showQuickSettingsDialog()
//    }

            val allowScreenshotsInPrivateMode = context.settings().allowScreenshotsInPrivateMode
            secureWindowFeature.set(
                feature = SecureWindowFeature(
                    window = context.getActivity()!!.window,
                    store = store,
                    customTabId = state.customTabSessionId,
                    isSecure = { !allowScreenshotsInPrivateMode && it.content.private },
                    clearFlagOnStop = false,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            fullScreenMediaSessionFeature.set(
                feature = MediaSessionFullscreenFeature(
                    context.getActivity()!!,
                    context.components.core.store,
                    state.customTabSessionId,
                ),
                owner = lifecycleOwner,
                view = view,
            )

            val shareDownloadFeature = ShareDownloadFeature(
                context = context.applicationContext,
                httpClient = context.components.core.client,
                store = store,
                tabId = state.customTabSessionId,
            )

            val copyDownloadFeature = CopyDownloadFeature(
                context = context.applicationContext,
                httpClient = context.components.core.client,
                store = store,
                tabId = state.customTabSessionId,
                onCopyConfirmation = {
                    showSnackbarForClipboardCopy(context, coroutineScope, snackbarHostState)
                },
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

            pipFeature = PictureInPictureFeature(
                store = store,
                activity = context.getActivity()!!,
                crashReporting = context.components.crashReporter, // context.components.analytics.crashReporter,
                tabId = state.customTabSessionId,
            )

            biometricPromptFeature.set(
                feature = BiometricPromptFeature(
                    context = context,
                    fragment = null, // view.findFragment(),
                    onAuthFailure = {
                        // todo: test biometrics
                        webPrompterState.onBiometricResult(isAuthenticated = false)
                    },
                    onAuthSuccess = {
                        // todo: test biometrics
                        webPrompterState.onBiometricResult(isAuthenticated = true)
                    },
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
                    state.customTabSessionId,
                ),
                owner = lifecycleOwner,
                view = view,
            )

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
                feature = SearchFeature(store, state.customTabSessionId) { request, tabId ->
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
                    sessionId = state.customTabSessionId,
                    onNeedToRequestPermissions = { permissions ->
                        // todo: test
                        requestSitePermissionsLauncher.launch(permissions)
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
            // sike
//            if (BuildConfig.MOZILLA_OFFICIAL) {
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
//            }

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
//                    assignSitePermissionsRules(context, sitePermissionsFeature)
//                }
//            }

            fullScreenFeature.set(
                feature = FullScreenFeature(
                    context.components.core.store,
                    context.components.useCases.sessionUseCases,
                    state.customTabSessionId,
                    { viewportFitChange(it, context) },
                    { inFullScreen ->
                        fullScreenChanged(
                            inFullScreen = inFullScreen,
                            context = context,
                            activity = context.getActivity()!!,
                            engineView = engineView!!,
                            swipeRefresh = swipeRefresh!!,
                            enableFullscreen = { state.setBrowserModeFullscreen() },
                            disableFullscreen = { state.setBrowserModeToolbar() },
                        )
                    },
                ),
                owner = lifecycleOwner,
                view = view,
            )

            store.flowScoped(lifecycleOwner) { flow ->
                flow.mapNotNull { browserState ->
                    browserState.findTabOrCustomTabOrSelectedTab(
                        state.customTabSessionId
                    )
                }.distinctUntilChangedBy { tab -> tab.content.pictureInPictureEnabled }
                    .collect { tab ->
                        pipModeChanged(
                            session = tab,
                            backPressedHandler = backHandler,
                            context = context,
                            activity = context.getActivity()!!,
                            engineView = engineView!!,
                            swipeRefresh = swipeRefresh!!,
                            enableFullscreen = { state.setBrowserModeFullscreen() },
                            disableFullscreen = { state.setBrowserModeToolbar() },
                        )
                    }
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
            swipeRefreshFeature.set(
                feature = SwipeRefreshFeature(
                    context.components.core.store,
                    context.components.useCases.sessionUseCases.reload,
                    swipeRefresh!!,
                    { },
                    state.customTabSessionId,
                ),
                owner = lifecycleOwner,
                view = view,
            )
//            }

            webchannelIntegration.set(
                feature = FxaWebChannelIntegration(
                    customTabSessionId = state.customTabSessionId,
                    runtime = context.components.core.engine,
                    store = context.components.core.store,
                    accountManager = context.components.backgroundServices.accountManager,
                    serverConfig = context.components.backgroundServices.serverConfig,
                    activityRef = WeakReference(context.getActivity()),
                ),
                owner = lifecycleOwner,
                view = view,
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

            updateBrowserToolbarLeadingAndNavigationActions(
                context = context,
                redesignEnabled = context.settings().navigationToolbarEnabled,
                isLandscape = context.isLandscape(),
                isTablet = isLargeWindow(context),
                isPrivate = (context.getActivity()!! as HomeActivity).browsingModeManager.mode.isPrivate,
                feltPrivateBrowsingEnabled = context.settings().feltPrivateBrowsingEnabled,
                isWindowSizeSmall = true, // AcornWindowSize.getWindowSize(context) == AcornWindowSize.Small,
            )

            updateBrowserToolbarMenuVisibility()

            // todo: translation, browser toolbar interactor
//            initTranslationsAction(
//                context, view, browserToolbarInteractor!!, translationsAvailable.value
//            )
            initReviewQualityCheck(
                context,
                lifecycleOwner,
                view,
                navController,
                { reviewQualityCheckAvailable = it },
                reviewQualityCheckAvailable,
                reviewQualityCheckFeature
            )
            // todo: init share page action, browser toolbar interactor
//            initSharePageAction(context, browserToolbarInteractor)
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
//                    lifecycleOwner = lifecycleOwner,
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

        observeTabSelection(
            context.components.core.store,
            context,
            lifecycleOwner,
            coroutineScope,
            browserInitialized,
        )

        if (!context.components.fenixOnboarding.userHasBeenOnboarded()) {
            observeTabSource(
                context.components.core.store, context, lifecycleOwner, coroutineScope
            )
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

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
//                    super.onCreate(savedInstanceState)
//                    savedLoginsLauncher = registerForActivityResult { navigateToSavedLoginsFragment(navController) }
                }

                Lifecycle.Event.ON_DESTROY -> {
//                    super.onDestroyView()
                    engineView!!.setActivityContext(null)
                    // todo: accessibility
//                    context.accessibilityManager.removeAccessibilityStateChangeListener(view.findFragment<BrowserComponentWrapperFragment>())

//                    _bottomToolbarContainerView = null
//                    _browserToolbarView = null
//                    _browserToolbarInteractor = null
//                    _binding = null


//                    leadingAction = null
//                    forwardAction = null
//                    backAction = null
//                    refreshAction = null
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
//                    currentStartDownloadDialog?.dismiss()

                    context.components.core.store.state.findTabOrCustomTabOrSelectedTab(
                        state.customTabSessionId
                    )?.let { session ->
                        // If we didn't enter PiP, exit full screen on stop
                        if (!session.content.pictureInPictureEnabled && fullScreenFeature.onBackPressed()) {
                            fullScreenChanged(
                                inFullScreen = false,
                                context = context,
                                activity = context.getActivity()!!,
                                engineView = engineView!!,
                                swipeRefresh = swipeRefresh!!,
                                enableFullscreen = { state.setBrowserModeFullscreen() },
                                disableFullscreen = { state.setBrowserModeToolbar() },
                            )
                        }
                    }

                    updateLastBrowseActivity(context)
                    updateHistoryMetadata(context)
                    pwaOnboardingObserver?.stop()
                }

                Lifecycle.Event.ON_PAUSE -> {
//                    super.onPause()
                    if (navController.currentDestination?.id != R.id.searchDialogFragment) {
                        view.hideKeyboard()
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
                    context.settings().shouldOpenLinksInApp(state.customTabSessionId != null)
                        .let { openLinksInExternalApp ->
                            components.services.appLinksInterceptor.updateLaunchInApp {
                                openLinksInExternalApp
                            }
                        }

                    evaluateMessagesForMicrosurvey(components)

                    context.components.core.tabCollectionStorage.register(
                        collectionStorageObserver(
                            context, navController, view, coroutineScope, snackbarHostState
                        ),
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

    // todo: fix all feature init
    LaunchedEffect(engineView, state.customTabSessionId) {
        sessionFeature.set(
            feature = SessionFeature(
                context.components.core.store,
                context.components.useCases.sessionUseCases.goBack,
                context.components.useCases.sessionUseCases.goForward,
                engineView!!, // binding.engineView,
                state.customTabSessionId,
            ),
            owner = lifecycleOwner,
            view = view,
        )
    }

    ContextMenuComponent(
        store = store,
        engineView = engineView,
        useCases = context.components.useCases.contextMenuUseCases,
        tabId = state.customTabSessionId,
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState = snackbarHostState) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        containerColor = LocalContext.current.infernoTheme().value.primaryBackgroundColor,
        content = {
            //    var startX by remember {mutableFloatStateOf(0F)}
            var startY by remember { mutableFloatStateOf(0F) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
//                    .pointerInput(state.currentTab) {
//                        awaitEachGesture {
//                            Log.d("BrowserComponent", "new gesture: ${this.currentEvent}")
//                            val down = awaitFirstDown()
//                            Log.d("BrowserComponent", "before touch slop, startX: $startX, startY: $startY")
//                            var change =
//                                awaitTouchSlopOrCancellation(down.id) { change, over ->
//                                    Log.d("BrowserComponent", "touch slop surpassed")
//                                    val original = Offset(startX, startY)
//                                    val summed = original + over
//                                    val newValue =
//                                        Offset(
//                                            x = summed.x.coerceIn(
//                                                0f,
//                                                size.width - 50.dp.toPx()
//                                            ),
//                                            y = summed.y.coerceIn(
//                                                0f,
//                                                size.height - 50.dp.toPx()
//                                            )
//                                        )
//                                        change.consume()
//                                    startX = newValue.x
//                                    startY = newValue.y
//                                }
//                            Log.d("BrowserComponent", "after touch slop, will now begin scrolling")
//
//                            // while scrolling
//                            while (change != null && change.pressed) {
//                                change = awaitVerticalDragOrCancellation(change.id)
//                                Log.d("BrowserComponent", "drag or cancellation change: $change")
//                                // if not scrolling or null then exit
//                                if (change == null || !change.pressed) break
//
//                                // get scroll offset
//                                val original = Offset(startX, startY)
//                                val summed =
//                                    original + change.positionChangeIgnoreConsumed()
//                                val newValue =
//                                    Offset(
//                                        x = summed.x.coerceIn(
//                                            0f,
//                                            size.width - 50.dp.toPx()
//                                        ),
//                                        y = summed.y.coerceIn(
//                                            0f,
//                                            size.height - 50.dp.toPx()
//                                        )
//                                    )
//                                        change.consume()
//
//                                // apply scroll to bottom bar
//                                val dy = newValue.y - startY
//                                Log.d("BrowserComponent", "Scroll dy: $dy")
//                                // if not loading scroll
////                                if (currentTab?.content?.loading == false) {
//                                val newOffset = (bottomBarOffsetPx.value - dy).coerceIn(
//                                    0F,
//                                    bottomBarHeightDp
//                                        .toPx()
//                                )
//                                coroutineScope.launch {
//                                    bottomBarOffsetPx.snapTo(newOffset)
//                                    engineView!!.setDynamicToolbarMaxHeight(
//                                        bottomBarHeightDp
//                                            .toPx()
//                                            .roundToInt() - newOffset.roundToInt()
//                                    )
//                                }
//                                // update start positions
//                                startX = newValue.x
//                                startY = newValue.y
////                                    }
//                            }
//
//                            // after scrolling done snap bottom bar
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
//                                engineView!!.setDynamicToolbarMaxHeight(
//                                    bottomBarHeightDp
//                                        .toPx()
//                                        .roundToInt() - bottomBarOffsetPx.value.roundToInt()
//                                )
//                            }
//                        }
//                    }
                    .motionEventSpy {
                        if (it.action == MotionEvent.ACTION_DOWN) {
                            startY = it.y
                        }
                        if (it.action == MotionEvent.ACTION_MOVE) {
                            val dy = it.y - startY
                            startY = it.y
                            // if not loading scroll
//                                if (currentTab?.content?.loading == false) {
                            val newOffset = (bottomBarOffsetPx.value - dy).coerceIn(
                                0F,
                                bottomBarHeightDp
                                    .dpToPx(context)
                                    .toFloat()
                            )
                            coroutineScope.launch {
                                bottomBarOffsetPx.snapTo(newOffset)
//                                setEngineDynamicToolbarMaxHeight(
//                                    bottomBarHeightDp.dpToPx(context) - newOffset.toInt()
//                                )
                            }
//                                }
                        }
                        if (it.action == MotionEvent.ACTION_UP || it.action == MotionEvent.ACTION_CANCEL) {
                            // set bottom bar position
                            coroutineScope.launch {
                                if (bottomBarOffsetPx.value <= (bottomBarHeightDp.dpToPx(context) / 2)) {
                                    // if more than halfway up, go up
                                    bottomBarOffsetPx.animateTo(0F)
                                } else {
                                    // if more than halfway down, go down
                                    bottomBarOffsetPx.animateTo(
                                        bottomBarHeightDp
                                            .dpToPx(context)
                                            .toFloat()
                                    )
                                }
//                                setEngineDynamicToolbarMaxHeight(
//                                    bottomBarHeightDp.dpToPx(context) - bottomBarOffsetPx.value.toInt()
//                                )
                            }
                        }
//                        else if (it.action == MotionEvent.ACTION_SCROLL) {
//                            // TODO: move nested scroll connection logic here
//                        }
                    },
            ) {
//                MozAwesomeBar(setView = { ab -> awesomeBar = ab })
                MozEngineView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp
                        ),
                    setEngineView = { ev -> engineView = ev },
                    setSwipeView = { sr -> swipeRefresh = sr },
                )

                if (state.isPendingTab) {
                    InfernoLoadingComponent()
                } else {
                    when (state.pageType) {
                        BrowserComponentPageType.HOME_PRIVATE -> {
                            InfernoHomeComponent(isPrivate = true, navController)
                        }

                        BrowserComponentPageType.HOME -> {
                            InfernoHomeComponent(isPrivate = false, navController)
                        }

                        BrowserComponentPageType.CRASH -> {
                            CrashComponent()
                        }

                        else -> {
                            // engine view already shown, kept there so engine view doesn't reset
                        }
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
//            MozFloatingActionButton { fab -> readerViewAppearanceButton = fab }
        },
        bottomBar = {
            // hide and show when scrolling
            BottomAppBar(
//                scrollBehavior = BottomAppBarScrollBehavior, // todo: scroll behavior, remove offset
                contentPadding = PaddingValues(0.dp),
                containerColor = Color.Transparent,
                modifier = Modifier
                    .height(bottomBarHeightDp)
                    .offset {
                        IntOffset(
                            x = 0, y = bottomBarOffsetPx.value.roundToInt()
                        )
                    },
            ) {
                Column(
                    Modifier.fillMaxSize()
                ) {
                    if (state.browserMode == BrowserComponentMode.TOOLBAR_EXTERNAL) {
                        InfernoExternalToolbar(
                            session = state.currentCustomTab,
                            onNavToBrowser = {
                                state.migrateExternalToNormal()
                                context.getActivity()?.let {
                                    it.finish()
                                    // todo: intent launcher, create funs for adding flags to intent for different task types (browser, custom tab, pwa)
                                    val intent = Intent(context, HomeActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    it.startActivity(intent)
                                }
                            },
                            onToggleDesktopMode = { state.toggleDesktopMode() },
                            onGoBack = {
                                if (state.currentCustomTab?.content?.canGoBack != false) {
                                    // if can go back (or sesh null), invoke go back
                                    context.components.useCases.sessionUseCases.goBack.invoke(it)
                                } else {
                                    // if cant go back, end activity / return to app that launcehd
                                    context.getActivity()!!.finish()
                                }
                            },
                            onGoForward = {
                                context.components.useCases.sessionUseCases.goForward.invoke(it)
                            },
                            onReload = {
                                context.components.useCases.sessionUseCases.reload.invoke(it)
                            },
                            onShare = { context.share(it) },
                        )
                    }
                    if (state.browserMode == BrowserComponentMode.TOOLBAR || state.browserMode == BrowserComponentMode.TOOLBAR_SEARCH) {
                        if (state.browserMode == BrowserComponentMode.TOOLBAR) {
                            InfernoTabBar(state.tabList, state.currentTab)
                        }
                        // todo: package in variables above, invoke here or in top app bar based on
                        //  settings
                        InfernoToolbar(
                            tabSessionState = state.currentTab,
                            tabCount = state.tabList.size,
                            onShowMenuBottomSheet = { showToolbarMenuBottomSheet = true },
                            onDismissMenuBottomSheet = { showToolbarMenuBottomSheet = false },
                            onRequestSearchBar = { /* todo: search bar */ },
                            onActivateFindInPage = { state.setBrowserModeFindInPage() },
                            onActivateReaderView = {
                                val successful = infernoReaderViewState.showReaderView()
                                if (successful) {
                                    state.setBrowserModeReaderView()
                                }
                            },
                            onNavToSettings = onNavToSettings,
                            onNavToHistory = onNavToHistory,
                            onNavToTabsTray = { showTabsTray = true },
                            searchEngine = state.searchEngine,
                            editMode = state.browserMode == BrowserComponentMode.TOOLBAR_SEARCH,
                            onStartSearch = { state.setBrowserModeSearch() },
                            onStopSearch = { state.setBrowserModeToolbar() },
                        )
                    }
                    if (state.browserMode == BrowserComponentMode.FIND_IN_PAGE) {
                        BrowserFindInPageBar(
                            onDismiss = { state.setBrowserModeToolbar() },
                            engineSession = state.currentTab?.engineState?.engineSession,
                            engineView = engineView,
                            session = state.currentTab,
                        )
                    }
                    if (state.browserMode == BrowserComponentMode.READER_VIEW) {
                        InfernoReaderViewControls(
                            state = infernoReaderViewState,
                        )
                    }
                }
            }
        },
    )
}

fun hideToolbar(context: Context) {
    context.getActivity()?.supportActionBar?.hide()
}

//private fun viewportFitChanged(viewportFit: Int, context: Context) {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//        context.getActivity()!!.window.attributes.layoutInDisplayCutoutMode = viewportFit
//    }
//}

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

private data class OnBackPressedHandler(
    val context: Context,
    val toolbarBackPressedHandler: () -> Boolean,
    val readerViewBackPressedHandler: () -> Boolean,
    val fullScreenFeature: ViewBoundFeatureWrapper<FullScreenFeature>,
//    val promptsFeature: ViewBoundFeatureWrapper<PromptFeature>,
    val sessionFeature: ViewBoundFeatureWrapper<SessionFeature>,
)

// combines Moz BrowserFragment and Moz BaseBrowserFragment implementations
private fun onBackPressed(
    onBackPressedHandler: OnBackPressedHandler,
): Boolean {
    with(onBackPressedHandler) {
        // todo: findInPageIntegration.onBackPressed() ||
        return readerViewBackPressedHandler.invoke() || fullScreenFeature.onBackPressed() || toolbarBackPressedHandler.invoke()
//                || promptsFeature.onBackPressed()
                || sessionFeature.onBackPressed() || removeSessionIfNeeded(context)
    }
}

@Composable
fun MozEngineView(
    modifier: Modifier = Modifier,
    setSwipeView: (VerticalSwipeRefreshLayout) -> Unit,
    setEngineView: (GeckoEngineView) -> Unit,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val vl = VerticalSwipeRefreshLayout(context)
            val gv = GeckoEngineView(context)
            setSwipeView(vl)
            setEngineView(gv)
            vl.layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
            )
            // todo: user prefs if swipe refresh enabled or not
            vl.visibility = View.VISIBLE
            vl.isEnabled = true
            vl.isActivated = true
            vl.isVisible = true
            vl.addView(gv)
            gv.layoutParams.width = LayoutParams.MATCH_PARENT
            gv.layoutParams.height = LayoutParams.MATCH_PARENT
            gv.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            gv.visibility = View.VISIBLE
            gv.setBackgroundColor(android.graphics.Color.DKGRAY)
            gv.isEnabled = true
            gv.isActivated = true
            gv.isVisible = true
            vl
        },
        update = { sv ->
            var gv: GeckoEngineView? = null
            for (v in sv.children) {
                if (v is GeckoEngineView) {
                    gv = v
                    break
                }
            }
//        // setup views
//        with(sv.layoutParams) {
//            this.width = LayoutParams.MATCH_PARENT
//            this.height = LayoutParams.MATCH_PARENT
//        }
//        with(gv!!.layoutParams) {
//            this.width = LayoutParams.MATCH_PARENT
//            this.height = LayoutParams.MATCH_PARENT
//        }
            gv!!.isEnabled = true
            gv.isActivated = true
        },
    )
}

// todo: reader view button, what this for?
//@Composable
//fun MozFloatingActionButton(
//    setView: (FloatingActionButton) -> Unit,
//) {
//    AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
//        val v = FloatingActionButton(context)
//        setView(v)
//        v
//    }, update = { it.visibility = View.GONE })
//}

/* BaseBrowserFragment funs */

// todo: show download cancelled dialog as bottom prompt
@VisibleForTesting
internal fun showCancelledDownloadWarning(
    downloadCount: Int,
    tabId: String?,
    source: String?,
    context: Context,
    coroutineScope: CoroutineScope,
    setAlertDialog: ((@Composable () -> Unit)?) -> Unit,
    snackbarHostState: AcornSnackbarHostState,
    setInitiallySelectedTabTray: ((InfernoTabsTraySelectedTab) -> Unit)? = null,
    dismissTabsTray: () -> Unit,
) {
//        context?.components?.analytics?.crashReporter?.recordCrashBreadcrumb(
//            Breadcrumb("DownloadCancelDialogFragment show"),
//        )

    // TODO: set text according to content in [DownloadCancelDialogFragment]
    DownloadCancelDialogFragment
    setAlertDialog {
        AlertDialog(
            onDismissRequest = { setAlertDialog(null) },
            title = {
                InfernoText(
                    text = String.format(
                        stringResource(R.string.mozac_feature_downloads_cancel_active_downloads_warning_content_title),
                        downloadCount
                    ),
                    fontColor = LocalContext.current.infernoTheme().value.primaryTextColor,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                InfernoText(
                    text = stringResource(R.string.mozac_feature_downloads_cancel_active_private_downloads_warning_content_body),
                )
            },
            confirmButton = {
                InfernoText(
                    text = stringResource(R.string.mozac_feature_downloads_cancel_active_downloads_accept),
                    modifier = Modifier.clickable {
                        setAlertDialog(null)
                        onCancelDownloadWarningAccepted(
                            tabId = tabId,
                            source = source,
                            context = context,
                            coroutineScope = coroutineScope,
                            setAlertDialog = setAlertDialog,
                            snackbarHostState = snackbarHostState,
                            setInitiallySelectedTabTray = setInitiallySelectedTabTray,
                            dismissTabsTray = dismissTabsTray,
                        )
                    },
                )
            },
            dismissButton = {
                InfernoText(
                    text = stringResource(R.string.mozac_feature_downloads_cancel_active_private_downloads_deny),
                    modifier = Modifier.clickable {
                        // dismiss
                        setAlertDialog(null)
                    },
                )
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = true,
            ),
        )
    }
//    val dialog = DownloadCancelDialogFragment.newInstance(downloadCount = downloadCount,
//        tabId = tabId,
//        source = source,
//        promptStyling = DownloadCancelDialogFragment.PromptStyling(
//            gravity = Gravity.BOTTOM,
//            shouldWidthMatchParent = true,
//            positiveButtonBackgroundColor = ThemeManager.resolveAttribute(
//                R.attr.accent,
//                context,
//            ),
//            positiveButtonTextColor = ThemeManager.resolveAttribute(
//                R.attr.textOnColorPrimary,
//                context,
//            ),
//            positiveButtonRadius = (context.resources.getDimensionPixelSize(R.dimen.tab_corner_radius)).toFloat(),
//        ),
//
//        onPositiveButtonClicked = { tabId, source ->
//            onCancelDownloadWarningAccepted(
//                tabId = tabId,
//                source = source,
//                context = context,
//                coroutineScope = coroutineScope,
//                setAlertDialog = setAlertDialog,
//                snackbarHostState = snackbarHostState,
//                setInitiallySelectedTabTray = setInitiallySelectedTabTray,
//                dismissTabsTray = dismissTabsTray
//            )
//        })
//    dialog.show(parentFragmentManager, DOWNLOAD_CANCEL_DIALOG_FRAGMENT_TAG)
}

@VisibleForTesting
internal fun onCancelDownloadWarningAccepted(
    tabId: String?,
    source: String?,
    context: Context,
    coroutineScope: CoroutineScope,
    setAlertDialog: ((@Composable () -> Unit)?) -> Unit,
    snackbarHostState: AcornSnackbarHostState,
    setInitiallySelectedTabTray: ((InfernoTabsTraySelectedTab) -> Unit)? = null,
    dismissTabsTray: () -> Unit,
) {
    if (tabId != null) {
        deleteTab(
            tabId = tabId,
            source = source,
            isConfirmed = true,
            context = context,
            coroutineScope = coroutineScope,
            setAlertDialog = setAlertDialog,
            snackbarHostState = snackbarHostState,
            setInitiallySelectedTabTray = setInitiallySelectedTabTray,
            dismissTabsTray = dismissTabsTray,
        )
//        tabsTrayInteractor.onDeletePrivateTabWarningAccepted(tabId, source)
    } else {
        closeAllTabs(
            private = true,
            isConfirmed = true,
            context = context,
            coroutineScope = coroutineScope,
            setAlertDialog = setAlertDialog,
            snackbarHostState = snackbarHostState,
            setInitiallySelectedTabTray = setInitiallySelectedTabTray,
            dismissTabsTray = dismissTabsTray,
        )
//        navigationInteractor.onCloseAllPrivateTabsWarningConfirmed(private = true)
    }
}

private fun deleteTab(
    tabId: String,
    source: String?,
    isConfirmed: Boolean,
    context: Context,
    coroutineScope: CoroutineScope,
    setAlertDialog: ((@Composable () -> Unit)?) -> Unit,
    snackbarHostState: AcornSnackbarHostState,
    setInitiallySelectedTabTray: ((InfernoTabsTraySelectedTab) -> Unit)? = null,
    dismissTabsTray: () -> Unit,
) {
    val browserStore = context.components.core.store
    val tabsUseCases = context.components.useCases.tabsUseCases
    val tab = browserStore.state.findTab(tabId)

    tab?.let {
        val isLastTab = browserStore.state.getNormalOrPrivateTabs(it.content.private).size == 1
        val isCurrentTab = browserStore.state.selectedTabId.equals(tabId)
        if (!isLastTab || !isCurrentTab) {
            tabsUseCases.removeTab(tabId)
            showUndoSnackbarForTab(
                it.content.private,
                context,
                coroutineScope,
                snackbarHostState,
                setInitiallySelectedTabTray,
                it
            )
        } else {
            val privateDownloads = browserStore.state.downloads.filter { map ->
                map.value.private && map.value.isActiveDownload()
            }
            if (!isConfirmed && privateDownloads.isNotEmpty()) {
                showCancelledDownloadWarning(
                    privateDownloads.size,
                    tabId,
                    source,
                    context,
                    coroutineScope = coroutineScope,
                    setAlertDialog = setAlertDialog,
                    snackbarHostState = snackbarHostState,
                    setInitiallySelectedTabTray = setInitiallySelectedTabTray,
                    dismissTabsTray = dismissTabsTray,
                )
                return
            } else {
                tabsUseCases.removeTab(tabId)
                dismissTabsTray.invoke()
            }
        }
//            TabsTray.closedExistingTab.record(TabsTray.ClosedExistingTabExtra(source ?: "unknown"))
    }

//    todo: tabsTrayStore.dispatch(TabsTrayAction.ExitSelectMode)
}

private fun closeAllTabs(
    private: Boolean,
    isConfirmed: Boolean,
    context: Context,
    coroutineScope: CoroutineScope,
    setAlertDialog: ((@Composable () -> Unit)?) -> Unit,
    snackbarHostState: AcornSnackbarHostState,
    setInitiallySelectedTabTray: ((InfernoTabsTraySelectedTab) -> Unit)? = null,
    dismissTabsTray: () -> Unit,
) {
    val browserStore = context.components.core.store
//    val sessionsToClose = if (private) {
//        HomeFragment.ALL_PRIVATE_TABS
//    } else {
//        HomeFragment.ALL_NORMAL_TABS
//    }

    if (private && !isConfirmed) {
        val privateDownloads = browserStore.state.downloads.filter {
            it.value.private && it.value.isActiveDownload()
        }
        if (privateDownloads.isNotEmpty()) {
            showCancelledDownloadWarning(
                downloadCount = privateDownloads.size,
                tabId = null,
                source = null,
                context = context,
                coroutineScope = coroutineScope,
                setAlertDialog = setAlertDialog,
                snackbarHostState = snackbarHostState,
                setInitiallySelectedTabTray = setInitiallySelectedTabTray,
                dismissTabsTray = dismissTabsTray,
            )
            return
        }
    }
    dismissTabsTray.invoke()
//    dismissTabTrayAndNavigateHome(sessionsToClose)
}

private fun showUndoSnackbar(
    message: String,
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: AcornSnackbarHostState,
) {
    coroutineScope.launch {
        val result = snackbarHostState.defaultSnackbarHostState.showSnackbar(
            message, actionLabel = context.getString(R.string.snackbar_deleted_undo), //"undo",
            duration = SnackbarDuration.Long
        )
        when (result) {
            SnackbarResult.ActionPerformed -> {
                context.components.useCases.tabsUseCases.undo.invoke()
            }

            SnackbarResult.Dismissed -> {
                // nothing
            }
        }
    }
}

internal fun showUndoSnackbarForSyncedTab(
    closeOperation: CloseTabsUseCases.UndoableOperation,
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: AcornSnackbarHostState,
) {
    coroutineScope.launch {
        val result = snackbarHostState.defaultSnackbarHostState.showSnackbar(
            message = context.getString(R.string.snackbar_tab_closed),
            actionLabel = context.getString(R.string.snackbar_deleted_undo),
            duration = SnackbarDuration.Long,
        )

        when (result) {
            SnackbarResult.ActionPerformed -> {
                closeOperation.undo()
            }

            SnackbarResult.Dismissed -> {}
        }
    }
}

private fun showBookmarkSnackbar(
    tabSize: Int,
    parentFolderTitle: String?,
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: AcornSnackbarHostState,
    navController: NavController,
    dismissTabsTray: () -> Unit,
) {
    val displayFolderTitle = parentFolderTitle ?: context.getString(R.string.library_bookmarks)
    val displayResId = when {
        tabSize > 1 -> {
            R.string.snackbar_message_bookmarks_saved_in
        }

        else -> {
            R.string.bookmark_saved_in_folder_snackbar
        }
    }

    coroutineScope.launch {
        val result = snackbarHostState.defaultSnackbarHostState.showSnackbar(
            message = context.getString(displayResId, displayFolderTitle),
            actionLabel = context.getString(R.string.create_collection_view),
            duration = SnackbarDuration.Long
        )

        when (result) {
            SnackbarResult.ActionPerformed -> {
                navController.navigate(
                    TabsTrayFragmentDirections.actionGlobalBookmarkFragment(BookmarkRoot.Mobile.id),
                )
                dismissTabsTray.invoke()
            }

            SnackbarResult.Dismissed -> {}
        }
    }
}

@VisibleForTesting
internal fun showUndoSnackbarForTab(
    isPrivate: Boolean,
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: AcornSnackbarHostState,
    setInitiallySelectedTabTray: ((InfernoTabsTraySelectedTab) -> Unit)? = null,
    tab: TabSessionState? = null,
) {
    val snackbarMessage = when (isPrivate) {
        true -> context.getString(R.string.snackbar_private_tab_closed)
        false -> context.getString(R.string.snackbar_tab_closed)
    }
    coroutineScope.launch {
        val result = snackbarHostState.defaultSnackbarHostState.showSnackbar(
            message = snackbarMessage,
            actionLabel = context.getString(R.string.snackbar_deleted_undo),
            duration = SnackbarDuration.Long,
        )

        when (result) {
            SnackbarResult.ActionPerformed -> {
                context.components.useCases.tabsUseCases.undo.invoke()
                // select recovered tab
                if (tab != null) {
                    context.components.useCases.tabsUseCases.selectTab.invoke(tab.id)
                    if (tab.isNormalTab()) {
                        setInitiallySelectedTabTray?.invoke(InfernoTabsTraySelectedTab.NormalTabs)
                    } else if (tab.content.private) {
                        setInitiallySelectedTabTray?.invoke(InfernoTabsTraySelectedTab.PrivateTabs)
                    }
                }
            }

            SnackbarResult.Dismissed -> {}
        }
    }
}

/**
 * Show a [Snackbar] when data is set to the device clipboard. To avoid duplicate displays of
 * information only show a [Snackbar] for Android 12 and lower.
 *
 * [See details](https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#duplicate-notifications).
 */
private fun showSnackbarForClipboardCopy(
    context: Context, coroutineScope: CoroutineScope, snackbarHostState: AcornSnackbarHostState,
) {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
        coroutineScope.launch {
            snackbarHostState.defaultSnackbarHostState.showSnackbar(
                message = context.getString(R.string.snackbar_copy_image_to_clipboard_confirmation),
                duration = SnackbarDuration.Long,
            )
        }
    }
}

/**
 * Show a [Snackbar] when credentials are saved or updated.
 */
private fun showSnackbarAfterLoginChange(
    isUpdate: Boolean,
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: AcornSnackbarHostState,
) {
    val snackbarText = if (isUpdate) {
        R.string.mozac_feature_prompt_login_snackbar_username_updated
    } else {
        R.string.mozac_feature_prompts_suggest_strong_password_saved_snackbar_title
    }
    coroutineScope.launch {
        snackbarHostState.defaultSnackbarHostState.showSnackbar(
            message = context.getString(snackbarText),
            duration = SnackbarDuration.Long,
        )
    }
}

/**
 * Shows a biometric prompt and fallback to prompting for the password.
 */
private fun showBiometricPrompt(
    context: Context,
    biometricPromptFeature: ViewBoundFeatureWrapper<BiometricPromptFeature>,
    webPrompterState: InfernoPromptFeatureState?,
    startForResult: ManagedActivityResultLauncher<Intent, ActivityResult>,
    setAlertDialog: ((@Composable () -> Unit)?) -> Unit,
) {
    if (BiometricPromptFeature.canUseFeature(context)) {
        biometricPromptFeature.get()
            ?.requestAuthentication(context.getString(R.string.credit_cards_biometric_prompt_unlock_message_2))
        return
    }

    // Fallback to prompting for password with the KeyguardManager
    val manager = context.getSystemService<KeyguardManager>()
    if (manager?.isKeyguardSecure == true) {
        showPinVerification(context, manager, startForResult)
    } else {
        // Warn that the device has not been secured
        if (context.settings().shouldShowSecurityPinWarning) {
            showPinDialogWarning(context, setAlertDialog, webPrompterState)
        } else {
//                promptsFeature.get()?.onBiometricResult(isAuthenticated = true)
        }
    }
}

/**
 * Shows a pin request prompt. This is only used when BiometricPrompt is unavailable.
 */
@Suppress("DEPRECATION")
private fun showPinVerification(
    context: Context,
    manager: KeyguardManager,
    startForResult: ManagedActivityResultLauncher<Intent, ActivityResult>,
) {
    val intent = manager.createConfirmDeviceCredentialIntent(
        context.getString(R.string.credit_cards_biometric_prompt_message_pin),
        context.getString(R.string.credit_cards_biometric_prompt_unlock_message_2),
    )

    startForResult.launch(intent)
}

/**
 * Shows a dialog warning about setting up a device lock PIN.
 */
private fun showPinDialogWarning(
    context: Context,
    setAlertDialog: ((@Composable () -> Unit)?) -> Unit,
    webPrompterState: InfernoPromptFeatureState?,
) {
//    AlertDialog.Builder(context).apply {
//        setNegativeButton(context.getString(R.string.credit_cards_warning_dialog_later)) { _: DialogInterface, _ ->
////                promptsFeature.get()?.onBiometricResult(isAuthenticated = false)
//        }
//
//        setPositiveButton(context.getString(R.string.credit_cards_warning_dialog_set_up_now)) { it: DialogInterface, _ ->
//            it.dismiss()
////                promptsFeature.get()?.onBiometricResult(isAuthenticated = false)
//            context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
//        }
//
//        create()
//    }.show().withCenterAlignedButtons().secure(activity)

    setAlertDialog {
        AlertDialog(
            onDismissRequest = { setAlertDialog(null) },
            title = {
                InfernoText(
                    text = stringResource(R.string.credit_cards_warning_dialog_title_2),
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                InfernoText(
                    text = stringResource(R.string.credit_cards_warning_dialog_message_3),
                )
            },
            confirmButton = {
                InfernoText(
                    text = stringResource(R.string.credit_cards_warning_dialog_set_up_now),
                    modifier = Modifier.clickable {
                        setAlertDialog(null)
                        webPrompterState?.onBiometricResult(isAuthenticated = false)
                        context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                    },
                )
            },
            dismissButton = {
                InfernoText(
                    text = stringResource(R.string.credit_cards_warning_dialog_later),
                    modifier = Modifier.clickable {
                        setAlertDialog(null)
                        webPrompterState?.onBiometricResult(isAuthenticated = false)
                    },
                )
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = true,
            ),
        )
    }

    context.settings().incrementSecureWarningCount()
}


@VisibleForTesting
internal fun shouldPullToRefreshBeEnabled(inFullScreen: Boolean, context: Context): Boolean {
    return /* FeatureFlags.pullToRefreshEnabled && */ context.settings().isPullToRefreshEnabledInBrowser && !inFullScreen
}

//@Suppress("LongMethod")
//private fun initializeNavBar(
//    browserToolbar: BrowserToolbar,
//    view: View,
//    context: Context,
//    activity: HomeActivity,
//) {
////        NavigationBar.browserInitializeTimespan.start()
//
//    val isToolbarAtBottom = context.isToolbarAtBottom()
//
//    // The toolbar view has already been added directly to the container.
//    // We should remove it and add the view to the navigation bar container.
//    // Should refactor this so there is no added view to remove to begin with:
//    // https://bugzilla.mozilla.org/show_bug.cgi?id=1870976
//    // todo: toolbar
////    if (isToolbarAtBottom) {
////        binding.browserLayout.removeView(browserToolbar)
////    }

//@Suppress("LongMethod")
//@Composable
//internal fun NavigationButtonsCFR(
//    context: Context,
//    navController: NavController,
//    activity: HomeActivity,
//    showDivider: Boolean,
//    browserToolbarInteractor: BrowserToolbarInteractor,
//) {
//    var showCFR by remember { mutableStateOf(false) }
//    val lastTimeNavigationButtonsClicked = remember { mutableLongStateOf(0L) }
//
//    // todo: menu button
//    // We need a second menu button, but we could reuse the existing builder.
////    val menuButton = MenuButton(context).apply {
////        menuBuilder = browserToolbarView.menuToolbar.menuBuilder
////        // We have to set colorFilter manually as the button isn't being managed by a [BrowserToolbarView].
////        setColorFilter(
////            getColor(
////                context,
////                ThemeManager.resolveAttribute(R.attr.textPrimary, context),
////            ),
////        )
////        recordClickEvent = { }
////    }
////    menuButton.setHighlightStatus()
////    _menuButtonView = menuButton
//
//    CFRPopupLayout(
//        showCFR = showCFR && context.settings().shouldShowNavigationButtonsCFR,
//        properties = CFRPopupProperties(
//            popupBodyColors = listOf(
//                FirefoxTheme.colors.layerGradientEnd.toArgb(),
//                FirefoxTheme.colors.layerGradientStart.toArgb(),
//            ),
//            dismissButtonColor = FirefoxTheme.colors.iconOnColor.toArgb(),
//            indicatorDirection = CFRPopup.IndicatorDirection.DOWN,
//            popupVerticalOffset = NAVIGATION_CFR_VERTICAL_OFFSET.dp,
//            indicatorArrowStartOffset = NAVIGATION_CFR_ARROW_OFFSET.dp,
//            popupAlignment = CFRPopup.PopupAlignment.BODY_TO_ANCHOR_START_WITH_OFFSET,
//        ),
//        onCFRShown = {
//            context.settings().shouldShowNavigationButtonsCFR = false
//            context.settings().lastCfrShownTimeInMillis = System.currentTimeMillis()
//        },
//        onDismiss = {},
//        text = {
//            FirefoxTheme {
//                Text(
//                    text = stringResource(R.string.navbar_navigation_buttons_cfr_message),
//                    color = FirefoxTheme.colors.textOnColorPrimary,
//                    style = FirefoxTheme.typography.body2,
//                )
//            }
//        },
//    ) {
//        val tabCounterMenu = lazy {
//            // todo: tab counter
//            FenixTabCounterMenu(
//                context = context,
//                onItemTapped = { item ->
//                    browserToolbarInteractor.onTabCounterMenuItemTapped(item)
//                },
//                iconColor = when (activity.browsingModeManager.mode.isPrivate) {
//                    true -> getColor(context, R.color.fx_mobile_private_icon_color_primary)
//                    else -> null
//                },
//            ).also {
//                it.updateMenu(
//                    toolbarPosition = context.settings().toolbarPosition,
//                )
//            }
//        }
//
//        // todo: navbar
////        BrowserNavBar(
////            isPrivateMode = activity.browsingModeManager.mode.isPrivate,
////            showDivider = showDivider,
////            browserStore = context.components.core.store,
////            menuButton = menuButton,
////            newTabMenu = NewTabMenu(
////                context = context,
////                onItemTapped = { item ->
////                    browserToolbarInteractor.onTabCounterMenuItemTapped(item)
////                },
////                iconColor = when (activity.browsingModeManager.mode.isPrivate) {
////                    true -> getColor(context, R.color.fx_mobile_private_icon_color_primary)
////                    else -> null
////                },
////            ),
////            tabsCounterMenu = tabCounterMenu,
////            onBackButtonClick = {
////                if (context.settings().shouldShowNavigationButtonsCFR) {
////                    val currentTime = System.currentTimeMillis()
////                    if (currentTime - lastTimeNavigationButtonsClicked.longValue <= NAVIGATION_CFR_MAX_MS_BETWEEN_CLICKS) {
////                        showCFR = true
////                    }
////                    lastTimeNavigationButtonsClicked.longValue = currentTime
////                }
////                browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
////                    ToolbarMenu.Item.Back(viewHistory = false),
////                )
////            },
////            onBackButtonLongPress = {
////                browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
////                    ToolbarMenu.Item.Back(viewHistory = true),
////                )
////            },
////            onForwardButtonClick = {
////                if (context.settings().shouldShowNavigationButtonsCFR) {
////                    val currentTime = System.currentTimeMillis()
////                    if (currentTime - lastTimeNavigationButtonsClicked.longValue <= NAVIGATION_CFR_MAX_MS_BETWEEN_CLICKS) {
////                        showCFR = true
////                    }
////                    lastTimeNavigationButtonsClicked.longValue = currentTime
////                }
////                browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
////                    ToolbarMenu.Item.Forward(viewHistory = false),
////                )
////            },
////            onForwardButtonLongPress = {
////                browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
////                    ToolbarMenu.Item.Forward(viewHistory = true),
////                )
////            },
////            onNewTabButtonClick = {
////                browserToolbarInteractor.onNewTabButtonClicked()
////            },
////            onNewTabButtonLongPress = {
////                browserToolbarInteractor.onNewTabButtonLongClicked()
////            },
////            onTabsButtonClick = {
////                onTabCounterClicked(activity.browsingModeManager.mode, navController, thumbnailsFeature)
////            },
////            onTabsButtonLongPress = {},
////            onMenuButtonClick = {
////                navController.nav(
////                    R.id.browserFragment,
////                    BrowserComponentWrapperFragmentDirections.actionGlobalMenuDialogFragment(
////                        accesspoint = MenuAccessPoint.Browser,
////                    ),
////                )
////            },
////            onVisibilityUpdated = {
////                configureEngineViewWithDynamicToolbarsMaxHeight(
////                    context, state.customTabSessionId, findInPageIntegration
////                )
////            },
////        )
//    }
//}


@VisibleForTesting
internal fun initializeMicrosurveyFeature(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    messagingFeatureMicrosurvey: ViewBoundFeatureWrapper<MessagingFeature>,
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

//@Suppress("LongMethod")
//private fun initializeMicrosurveyPrompt(
//    context: Context, view: View, fullScreenFeature: ViewBoundFeatureWrapper<FullScreenFeature>
//) {
////    val context = context
////    val view = requireView()
//
//    // todo: toolbar
////    val isToolbarAtBottom = context.isToolbarAtBottom()
////    val browserToolbar = browserToolbarView.view
////    // The toolbar view has already been added directly to the container.
////    // See initializeNavBar for more details on improving this.
////    if (isToolbarAtBottom) {
////        binding.browserLayout.removeView(browserToolbar)
////    }
//
////    _bottomToolbarContainerView = BottomToolbarContainerView(
////        context = context,
////        parent = binding.browserLayout,
////        hideOnScroll = isToolbarDynamic(context),
////        content = {
////            FirefoxTheme {
////                Column {
////                    val activity = context.getActivity()!! as HomeActivity
////
////                    if (!activity.isMicrosurveyPromptDismissed.value) {
////                        currentMicrosurvey?.let {
////                            if (isToolbarAtBottom) {
////                                removeBottomToolbarDivider(browserToolbar)
////                            }
////
////                            Divider()
////
////                            MicrosurveyRequestPrompt(
////                                microsurvey = it,
////                                activity = activity,
////                                onStartSurveyClicked = {
////                                    context.components.appStore.dispatch(
////                                        MicrosurveyAction.Started(
////                                            it.id
////                                        )
////                                    )
////                                    navController.nav(
////                                        R.id.browserFragment,
////                                        BrowserComponentWrapperFragmentDirections.actionGlobalMicrosurveyDialog(
////                                            it.id
////                                        ),
////                                    )
////                                },
////                                onCloseButtonClicked = {
////                                    context.components.appStore.dispatch(
////                                        MicrosurveyAction.Dismissed(it.id),
////                                    )
////
////                                    context.settings().shouldShowMicrosurveyPrompt = false
////                                    activity.isMicrosurveyPromptDismissed.value = true
////
////                                    resumeDownloadDialogState(
////                                        getCurrentTab()?.id,
////                                        context.components.core.store,
////                                        context,
////                                    )
////                                },
////                            )
////                        }
////                    } else {
////                        restoreBottomToolbarDivider(browserToolbar)
////                    }
////
////                    if (isToolbarAtBottom) {
////                        AndroidView(factory = { _ -> browserToolbar })
////                    }
////                }
////            }
////        },
////    ).apply {
////        // This covers the usecase when the app goes into fullscreen mode from portrait orientation.
////        // Transition to fullscreen happens first, and orientation change follows. Microsurvey container is getting
////        // reinitialized when going into landscape mode, but it shouldn't be visible if the app is already in the
////        // fullscreen mode. It still has to be initialized to be shown after the user exits the fullscreen.
////        val isFullscreen = fullScreenFeature.get()?.isFullScreen == true
////        toolbarContainerView.isVisible = !isFullscreen
////    }
////
////    bottomToolbarContainerIntegration.set(
////        feature = BottomToolbarContainerIntegration(
////            toolbar = bottomToolbarContainerView.toolbarContainerView,
////            store = context.components.core.store,
////            sessionId = state.customTabSessionId,
////        ),
////        owner = lifecycleOwner,
////        view = view,
////    )
//
//    reinitializeEngineView(context, fullScreenFeature)
//}

//private fun restoreBottomToolbarDivider(browserToolbar: BrowserToolbar, context: Context) {
//    val safeContext = context ?: return
//    if (safeContext.isToolbarAtBottom()) {
//        val defaultBackground = ResourcesCompat.getDrawable(
//            context.resources,
//            R.drawable.toolbar_background,
//            context?.theme,
//        )
//        browserToolbar.background = defaultBackground
//    }
//}

private var currentMicrosurvey: MicrosurveyUIData? = null

/**
 * Listens for the microsurvey message and initializes the microsurvey prompt if one is available.
 */
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

//private fun shouldShowMicrosurveyPrompt(context: Context) =
//    context.components.settings.shouldShowMicrosurveyPrompt

//private fun isToolbarDynamic(context: Context) =
//    !context.settings().shouldUseFixedTopToolbar && context.settings().isDynamicToolbarEnabled

///**
// * Returns a list of context menu items [ContextMenuCandidate] for the context menu
// */
//abstract fun getContextMenuCandidates(
//    context: Context,
//    view: View,
//): List<ContextMenuCandidate>


@VisibleForTesting
internal fun observeTabSelection(
    store: BrowserStore,
    context: Context,
    lifecycleOwner: LifecycleOwner,
    coroutineScope: CoroutineScope,
    browserInitialized: Boolean,
) {
    consumeFlow(store, lifecycleOwner, context, coroutineScope) { flow ->
        flow.distinctUntilChangedBy {
            it.selectedTabId
        }.mapNotNull {
            it.selectedTab
        }.collect {
//            currentStartDownloadDialog?.dismiss()
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
    coroutineScope: CoroutineScope,
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
 *//* override */ fun onActivityResult(
    requestCode: Int, data: Intent?, resultCode: Int,
): Boolean {
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
    navController: NavController, customTabSessionId: String?,
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

///**
// * Configure the engine view to know where to place website's dynamic elements
// * depending on the space taken by any dynamic toolbar.
// */
//@VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
//internal fun configureEngineViewWithDynamicToolbarsMaxHeight(
//    context: Context,
//    customTabSessionId: String?,
//) {
//    val currentTab =
//        context.components.core.store.state.findCustomTabOrSelectedTab(customTabSessionId)
//    if (currentTab?.content?.isPdf == true) return
////    if (findInPageIntegration.get()?.isFeatureActive == true) return
////    val toolbarHeights = view?.let { probeToolbarHeights(it) } ?: return
//
//    context.also {
//        if (isToolbarDynamic(it)) {
//            // todo: toolbar
////            if (!context.components.core.geckoRuntime.isInteractiveWidgetDefaultResizesVisual) {
////                getEngineView().setDynamicToolbarMaxHeight(toolbarHeights.first + toolbarHeights.second)
////            }
//        } else {
//            // todo: toolbar
////            (getSwipeRefreshLayout().layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
////                bottomMargin = toolbarHeights.second
////            }
//        }
//    }
//}

///**
// * Get an instant reading of the top toolbar height and the bottom toolbar height.
// */
//private fun probeToolbarHeights(
//    rootView: View,
//    customTabSessionId: String?,
//    fullScreenFeature: ViewBoundFeatureWrapper<FullScreenFeature>,
//): Pair<Int, Int> {
//    val context = rootView.context
//    // Avoid any change for scenarios where the toolbar is not shown
//    if (fullScreenFeature.get()?.isFullScreen == true) return 0 to 0
//
//    val topToolbarHeight = context.settings().getTopToolbarHeight(
//        includeTabStrip = customTabSessionId == null && context.isTabStripEnabled(),
//    )
//    val navbarHeight = context.resources.getDimensionPixelSize(R.dimen.browser_navbar_height)
//    val isKeyboardShown = rootView.isKeyboardVisible()
//    val bottomToolbarHeight = context.settings().getBottomToolbarHeight(context).minus(
//        when (isKeyboardShown) {
//            true -> navbarHeight // When keyboard is shown the navbar is expected to be hidden. Ignore it's height.
//            false -> 0
//        },
//    )
//
//    return topToolbarHeight to bottomToolbarHeight
//}

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
    sessionUrl: String,
    sessionTitle: String,
    context: Context,
    navController: NavController,
    coroutineScope: CoroutineScope,
    snackbarHostState: AcornSnackbarHostState,
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
                BrowserComponentWrapperFragmentDirections.actionGlobalBookmarkEditFragment(
                    existing.guid, true
                ),
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
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState,
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
                coroutineScope.launch {
                    snackbarHostState.warningSnackbarHostState.showSnackbar(
                        message = context.getString(R.string.bookmark_invalid_url_error),
                        duration = SnackbarDuration.Long,
                    )
                }
            }
        }
    }
}

private fun showBookmarkSavedSnackbar(
    message: String,
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: AcornSnackbarHostState,
    onClick: () -> Unit,
) {
    coroutineScope.launch {
        val result = snackbarHostState.defaultSnackbarHostState.showSnackbar(
            message = message,
            actionLabel = context.getString(R.string.edit_bookmark_snackbar_action),
            duration = SnackbarDuration.Long,
        )
        when (result) {
            SnackbarResult.ActionPerformed -> {
                onClick.invoke()
            }

            SnackbarResult.Dismissed -> {}
        }
    }
}

fun onHomePressed(pipFeature: PictureInPictureFeature?) = pipFeature?.onHomePressed() ?: false

/**
 * Exit fullscreen mode when exiting PIP mode
 */
private fun pipModeChanged(
    session: SessionState,
    backPressedHandler: OnBackPressedHandler,
    context: Context,
    activity: AppCompatActivity,
    engineView: EngineView,
    swipeRefresh: SwipeRefreshLayout,
    enableFullscreen: () -> Unit,
    disableFullscreen: () -> Unit,
) {
    if (!session.content.pictureInPictureEnabled && session.content.fullScreen) {
        onBackPressed(backPressedHandler)
        fullScreenChanged(
            inFullScreen = false,
            context = context,
            activity = activity,
            engineView = engineView,
            swipeRefresh = swipeRefresh,
            enableFullscreen = enableFullscreen,
            disableFullscreen = disableFullscreen,
        )
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
    activity: AppCompatActivity,
    engineView: EngineView,
    swipeRefresh: SwipeRefreshLayout,
    enableFullscreen: () -> Unit,
    disableFullscreen: () -> Unit,
) {
    Log.d("BrowserComponent", "full screen changed, inFullScreen: $inFullScreen")
//    val activity = context.getActivity() ?: return
    if (inFullScreen) {
        // Close find in page bar if opened
//        findInPageIntegration.onBackPressed()
        // todo: hide bottom bar -> set offset to max and disable scroll listener (dont change offset on scroll if in fullscreen)
        enableFullscreen.invoke()

        FullScreenNotificationToast(
            activity = activity,
            gestureNavString = activity.getString(R.string.exit_fullscreen_with_gesture_short),
            backButtonString = context.getString(R.string.exit_fullscreen_with_back_button_short),
            GestureNavUtils,
        ).show()

        activity.enterImmersiveMode(
            setOnApplyWindowInsetsListener = { key: String, listener: OnApplyWindowInsetsListener ->
                engineView.addWindowInsetsListener(key, listener)
            },
        )
//        (view as? SwipeGestureLayout)?.isSwipeEnabled = false
        // todo:
        //  expandBrowserView()

    } else {
        disableFullscreen.invoke()

        activity.exitImmersiveMode(
            unregisterOnApplyWindowInsetsListener = engineView::removeWindowInsetsListener,
        )

//        (view as? SwipeGestureLayout)?.isSwipeEnabled = true
        (activity as? HomeActivity)?.let { homeActivity ->
            // ExternalAppBrowserActivity exclusively handles it's own theming unless in private mode.
            if (homeActivity !is ExternalAppBrowserActivity || homeActivity.browsingModeManager.mode.isPrivate) {
                homeActivity.themeManager.applyStatusBarTheme(
                    homeActivity, homeActivity.isTabStripEnabled()
                )
            }
        }
    }

    swipeRefresh.isEnabled = shouldPullToRefreshBeEnabled(inFullScreen, context)
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

//@CallSuper
//fun onUpdateToolbarForConfigurationChange(
//    toolbar: BrowserToolbarView,
//    context: Context,
//    fullScreenFeature: ViewBoundFeatureWrapper<FullScreenFeature>,
//) {
//    toolbar.dismissMenu()
//
//    // If the navbar feature could be visible, we should update it's state.
//    val shouldUpdateNavBarState =
//        // todo: webAppToolbarShouldBeVisible
//        context.settings().navigationToolbarEnabled // && webAppToolbarShouldBeVisible
//    if (shouldUpdateNavBarState) {
//        // todo: navbar
////        updateNavBarForConfigurationChange(
////            context = context,
////            parent = binding.browserLayout,
////            toolbarView = browserToolbarView.view,
////            bottomToolbarContainerView = _bottomToolbarContainerView?.toolbarContainerView,
////            reinitializeNavBar = ::reinitializeNavBar,
////            reinitializeMicrosurveyPrompt = ::initializeMicrosurveyPrompt,
////        )
//    }
//
//    reinitializeEngineView(context, fullScreenFeature)
//
//    // If the microsurvey feature is visible, we should update it's state.
//    if (shouldShowMicrosurveyPrompt(context) && !shouldUpdateNavBarState) {
//        // todo: microsurvey
////        updateMicrosurveyPromptForConfigurationChange(
////            parent = binding.browserLayout,
////            bottomToolbarContainerView = _bottomToolbarContainerView?.toolbarContainerView,
////            reinitializeMicrosurveyPrompt = ::initializeMicrosurveyPrompt,
////        )
//    }
//}

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
    context: Context,
    downloadState: DownloadState,
    coroutineScope: CoroutineScope,
    snackbarHostState: AcornSnackbarHostState,
) {
    coroutineScope.launch {
        snackbarHostState.warningSnackbarHostState.showSnackbar(
            message = DynamicDownloadDialog.getCannotOpenFileErrorMessage(
                context, downloadState
            ), duration = SnackbarDuration.Long
        )
    }
}

//fun onAccessibilityStateChanged(enabled: Boolean) {
//    // todo: toolbar
////    if (_browserToolbarView != null) {
////        browserToolbarView.setToolbarBehavior(enabled)
////    }
//}

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
    status: Status,
    context: Context,
): Boolean {
    val isValidStatus = status in listOf(Status.COMPLETED, Status.FAILED)
    val isSameTab = downloadState.sessionId == (getCurrentTab(context)?.id ?: false)

    return isValidStatus && isSameTab
}

private fun handleOnSaveLoginWithGeneratedStrongPassword(
    passwordsStorage: SyncableLoginsStorage,
    url: String,
    password: String,
    lifecycleScope: CoroutineScope,
    setLastSavedGeneratedPassword: (String) -> Unit,
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

// todo: share page
//private fun initSharePageAction(
//    context: Context, browserToolbarInteractor: BrowserToolbarInteractor
//) {
//    if (!context.settings().navigationToolbarEnabled || context.isTabStripEnabled()) {
//        return
//    }
//
//    val sharePageAction = BrowserToolbar.createShareBrowserAction(
//        context = context,
//    ) {
////            AddressToolbar.shareTapped.record((NoExtras()))
//        browserToolbarInteractor.onShareActionClicked()
//    }
//
//    // todo: add share action to toolbar
////    browserToolbarView.view.addPageAction(sharePageAction)
//}

// todo: web content translation
//private fun initTranslationsAction(
//    context: Context,
//    view: View,
//    browserToolbarInteractor: BrowserToolbarInteractor,
//    translationsAvailable: Boolean
//) {
//    if (!FxNimbus.features.translations.value().mainFlowToolbarEnabled) {
//        return
//    }
//
//    val translationsAction = Toolbar.ActionButton(
//        AppCompatResources.getDrawable(
//            context,
//            R.drawable.mozac_ic_translate_24,
//        ),
//        contentDescription = context.getString(R.string.browser_toolbar_translate),
//        iconTintColorResource = ThemeManager.resolveAttribute(R.attr.textPrimary, context),
//        visible = { translationsAvailable },
//        weight = { TRANSLATIONS_WEIGHT },
//        listener = {
////            browserToolbarInteractor.onTranslationsButtonClicked()
//        },
//    )
//    // todo: add translation action to toolbar
////    browserToolbarView.view.addPageAction(translationsAction)
//
//    // todo: translations
////    translationsBinding.set(
////        feature = TranslationsBinding(browserStore = context.components.core.store,
////            onTranslationsActionUpdated = {
////                translationsAvailable = it.isVisible
////
////                translationsAction.updateView(
////                    tintColorResource = if (it.isTranslated) {
////                        R.color.fx_mobile_icon_color_accent_violet
////                    } else {
////                        ThemeManager.resolveAttribute(R.attr.textPrimary, context)
////                    },
////                    contentDescription = if (it.isTranslated) {
////                        context.getString(
////                            R.string.browser_toolbar_translated_successfully,
////                            it.fromSelectedLanguage?.localizedDisplayName,
////                            it.toSelectedLanguage?.localizedDisplayName,
////                        )
////                    } else {
////                        context.getString(R.string.browser_toolbar_translate)
////                    },
////                )
////
////                safeInvalidateBrowserToolbarView()
////
////                if (!it.isTranslateProcessing) {
////                    context.components.appStore.dispatch(SnackbarAction.SnackbarDismissed)
////                }
////            },
////            onShowTranslationsDialog = {} //FIXME: browserToolbarInteractor.onTranslationsButtonClicked(),
////        ),
////        owner = lifecycleOwner,
////        view = view,
////    )
//}

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
        visible = { reviewQualityCheckAvailable },
        weight = { REVIEW_QUALITY_CHECK_WEIGHT },
        listener = { _ ->
            context.components.appStore.dispatch(
                ShoppingAction.ShoppingSheetStateUpdated(expanded = true),
            )
            navController.navigate(
                BrowserComponentWrapperFragmentDirections.actionGlobalReviewQualityCheckDialogFragment(),
            )
        },
    )

    // todo: qualityCheck???, also page actions not implemented
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
 * This code takes care of the [InfernoToolbar] leading and navigation actions.
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

//@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
//internal fun addTabletActions(context: Context) {
//    addNavigationActions(context)
//
//    val enableTint = ThemeManager.resolveAttribute(R.attr.textPrimary, context)
//    // todo: refresh action
////    if (refreshAction == null) {
////        refreshAction = BrowserToolbar.TwoStateButton(
////            primaryImage = AppCompatResources.getDrawable(
////                context,
////                R.drawable.mozac_ic_arrow_clockwise_24,
////            )!!,
////            primaryContentDescription = context.getString(R.string.browser_menu_refresh),
////            primaryImageTintResource = enableTint,
////            isInPrimaryState = {
////                getSafeCurrentTab()?.content?.loading == false
////            },
////            secondaryImage = AppCompatResources.getDrawable(
////                context,
////                R.drawable.mozac_ic_stop,
////            )!!,
////            secondaryContentDescription = context.getString(R.string.browser_menu_stop),
////            disableInSecondaryState = false,
////            longClickListener = {
////                browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
////                    ToolbarMenu.Item.Reload(bypassCache = true),
////                )
////            },
////            listener = {
////                if (getCurrentTab()?.content?.loading == true) {
////                    browserToolbarInteractor.onBrowserToolbarMenuItemTapped(ToolbarMenu.Item.Stop)
////                } else {
////                    browserToolbarInteractor.onBrowserToolbarMenuItemTapped(
////                        ToolbarMenu.Item.Reload(bypassCache = false),
////                    )
////                }
////            },
////        ).also {
////            browserToolbarView.view.addNavigationAction(it)
////        }
////    }
//}

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

//@VisibleForTesting
//internal fun removeTabletActions() {
//    removeNavigationActions()
//
//    // todo: refresh action
////    refreshAction?.let {
////        browserToolbarView.view.removeNavigationAction(it)
////    }
//}

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
    useCase.containsException(tab.id) { _ -> // hasTrackingProtectionException ->
//        lifecycleScope.launch {
        coroutineScope.launch {
//            val cookieBannersStorage = context.components.core.cookieBannersStorage
//            val cookieBannerUIMode = cookieBannersStorage.getCookieBannerUIMode(
//                context,
//                tab,
//            )
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
    coroutineScope: CoroutineScope,
    snackbarHostState: AcornSnackbarHostState,
): TabCollectionStorage.Observer {
    return object : TabCollectionStorage.Observer {
        override fun onCollectionCreated(
            title: String,
            sessions: List<TabSessionState>,
            id: Long?,
        ) {
            showTabSavedToCollectionSnackbar(
                sessions.size, context, navController, coroutineScope, snackbarHostState, true
            )
        }

        override fun onTabsAdded(
            tabCollection: TabCollection, sessions: List<TabSessionState>,
        ) {
            showTabSavedToCollectionSnackbar(
                sessions.size, context, navController, coroutineScope, snackbarHostState
            )
        }

        fun showTabSavedToCollectionSnackbar(
            tabSize: Int,
            context: Context,
            navController: NavController,
            coroutineScope: CoroutineScope,
            snackbarHostState: AcornSnackbarHostState,
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

                // show snackbar
                coroutineScope.launch {
                    val result = snackbarHostState.defaultSnackbarHostState.showSnackbar(
                        message = context.getString(messageStringRes),
                        duration = SnackbarDuration.Long,
                        actionLabel = context.getString(R.string.create_collection_view),
                    )
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            navController.navigate(
                                BrowserComponentWrapperFragmentDirections.actionGlobalHome(
                                    focusOnAddressBar = false,
                                    scrollToCollection = true,
                                ),
                            )
                        }

                        SnackbarResult.Dismissed -> {}
                    }
                }

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

//fun getContextMenuCandidates(
//    context: Context,
//    view: View,
//): List<ContextMenuCandidate> {
//    val contextMenuCandidateAppLinksUseCases = AppLinksUseCases(
//        context,
//        { true },
//    )
//
//    return ContextMenuCandidate.defaultCandidates(
//        context,
//        context.components.useCases.tabsUseCases,
//        context.components.useCases.contextMenuUseCases,
//        view,
//        ContextMenuSnackbarDelegate(),
//    ) + ContextMenuCandidate.createOpenInExternalAppCandidate(
//        context,
//        contextMenuCandidateAppLinksUseCases,
//    )
//}

/**
 * Updates the last time the user was active on the [BrowserFragment].
 * This is useful to determine if the user has to start on the [HomeFragment]
 * or it should go directly to the [BrowserFragment].
 */
@VisibleForTesting
fun updateLastBrowseActivity(context: Context) {
    context.settings().lastBrowseActivity = System.currentTimeMillis()
}