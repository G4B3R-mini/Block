/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.ext

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import androidx.annotation.StringRes
import mozilla.components.support.locale.LocaleManager
import com.shmibblez.inferno.BrowserApplication
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.Components
import com.shmibblez.inferno.components.components
//import com.shmibblez.inferno.components.metrics.MetricController
import com.shmibblez.inferno.components.toolbar.ToolbarPosition
import com.shmibblez.inferno.mozillaAndroidComponents.compose.base.theme.AcornWindowSize
import com.shmibblez.inferno.settings.advanced.getSelectedLocale
import com.shmibblez.inferno.utils.isLargeScreenSize
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.action.TabListAction
import mozilla.components.browser.state.selector.findTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.SessionState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.createTab
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineSession.LoadUrlFlags
import mozilla.components.concept.storage.HistoryMetadataKey
import mozilla.components.feature.tabs.TabsUseCases
import java.lang.String.format
import java.util.Locale

/**
 * Get the BrowserApplication object from a context.
 */
val Context.application: BrowserApplication
    get() = applicationContext as BrowserApplication

/**
 * Get the requireComponents of this application.
 */
val Context.components: Components
    get() = application.components

/**
 * Add new tab convenience fun
 */
// todo: next to current, go to tab action add new tab and copy
fun Components.newTab(
    isPrivateSession: Boolean = false,
    nextTo: String? = null,
    url: String = if (!isPrivateSession) "inferno:home" else "inferno:private",
    selectTab: Boolean = true,
    startLoading: Boolean = true,
    parentId: String? = null,
    flags: LoadUrlFlags = LoadUrlFlags.none(),
    contextId: String? = null,
    engineSession: EngineSession? = null,
    source: SessionState.Source = SessionState.Source.Internal.NewTab,
    searchTerms: String = "",
    private: Boolean = false,
    historyMetadata: HistoryMetadataKey? = null,
    isSearch: Boolean = false,
    searchEngineName: String? = null,
    additionalHeaders: Map<String, String>? = null,
): String {
    val tabId = this.useCases.tabsUseCases.addTab(
        url = if (isPrivateSession) "inferno:privatebrowsing" else "inferno:home",
        selectTab = true,
        private = isPrivateSession
    )

    if (nextTo != null)
        // move new tab next to current
        this.useCases.tabsUseCases.moveTabs(listOf(tabId), nextTo, true)

    return tabId

//    /**<from [TabsUseCases]>*/
//    val store = this.core.store
//    val state = store.state
//
//    val tab = createTab(
//        url = url,
//        private = private,
//        source = source,
//        contextId = contextId,
//        parent = parentId?.let { store.state.findTab(it) },
//        engineSession = engineSession,
//        searchTerms = searchTerms,
//        initialLoadFlags = flags,
//        initialAdditionalHeaders = additionalHeaders,
//        historyMetadata = historyMetadata,
//        desktopMode = store.state.desktopMode,
//    )
//
//    /*</from [TabsUseCases]*/
//
//    /**<from store.dispatch(TabListAction.AddTabAction(tab, select = selectTab))>*/
//
//    /**
//     * Checks that the provided tab doesn't already exist and throws an
//     * [IllegalArgumentException] otherwise.
//     *
//     * @param state the current [BrowserState] (including all existing tabs).
//     * @param tab the [TabSessionState] to check.
//     */
////    private fun requireUniqueTab(state: BrowserState, tab: TabSessionState) {
//    require(state.tabs.find { it.id == tab.id } == null) {
//        "Tab with same ID already exists"
//    }
////    }
//
//    val updatedTabList = if (tab.parentId != null) {
//        val parentIndex = state.tabs.indexOfFirst { it.id == tab.parentId }
//        require(parentIndex != -1) {
//            "The parent does not exist"
//        }
//
//        // Add the child tab next to its parent
//        val childIndex = parentIndex + 1
//        state.tabs.subList(0, childIndex) + tab + state.tabs.subList(childIndex, state.tabs.size)
//    } else if (nextTo != null) {
//        // add the tab next to requested tab
//        // index of nextTo
//        val nextToIndex = state.tabs.indexOfFirst { it.id == nextTo } + 1
//        state.tabs.take(nextToIndex) + tab + state.tabs.drop(nextToIndex)
//    } else {
//        state.tabs + tab
//    }
//
//    state.copy(
//        tabs = updatedTabList,
//        selectedTabId = if (selectTab || state.selectedTabId == null) {
//            tab.id
//        } else {
//            state.selectedTabId
//        },
//    )
//
//    /*</from store.dispatch(TabListAction.AddTabAction(tab, select = selectTab))>*/
//
//    /**<from [TabsUseCases]>*/
//
//    store.dispatch(ContentAction.UpdateIsSearchAction(tab.id, isSearch, searchEngineName))
//
//    // If an engine session is specified then loading will have already started when linking
//    // the tab to its engine session. Otherwise we ask to load the URL here.
//    if (startLoading && engineSession == null) {
//        store.dispatch(
//            EngineAction.LoadUrlAction(
//                tabId = tab.id,
//                url = url,
//                flags = flags,
//                additionalHeaders = additionalHeaders,
//                includeParent = true,
//            ),
//        )
//    }
//
//    return tab.id
    /*</from [TabsUseCases]>*/
}


/**
 * Helper function to get the MetricController off of context.
 */
//val Context.metrics: MetricController
//    get() = this.components.analytics.metrics

fun Context.asActivity() =
    (this as? ContextThemeWrapper)?.baseContext as? Activity ?: this as? Activity

fun Context.getPreferenceKey(@StringRes resourceId: Int): String =
    resources.getString(resourceId)

/**
 * Gets the Root View with an activity context
 *
 * @return ViewGroup? if it is able to get a root view from the context
 */
fun Context.getRootView(): View? =
    asActivity()?.window?.decorView?.findViewById<View>(android.R.id.content) as? ViewGroup

fun Context.settings() = components.settings

/**
 * Used to catch IllegalArgumentException that is thrown when
 * a string's placeholder is incorrectly formatted in a translation
 *
 * @return the formatted string in locale language or English as a fallback
 */
fun Context.getStringWithArgSafe(@StringRes resId: Int, formatArg: String): String {
    return try {
        format(getString(resId), formatArg)
    } catch (e: IllegalArgumentException) {
        // fallback to <en> string
        logDebug(
            "L10n",
            "String: " + resources.getResourceEntryName(resId) + " not properly formatted in: " + LocaleManager.getSelectedLocale(
                this
            ).language,
        )
        val config = resources.configuration
        config.setLocale(Locale("en"))
        val localizedContext: Context = this.createConfigurationContext(config)
        return format(localizedContext.getString(resId), formatArg)
    }
}

/**
 * Used to obtain a reference to an AccessibilityManager
 * @return accessibilityManager
 */
val Context.accessibilityManager: AccessibilityManager
    get() = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

/**
 * Used to navigate to system notifications settings for app.
 *
 * @param onError Invoked when the activity described by the intent is not present on the device.
 */
fun Context.navigateToNotificationsSettings(
    onError: () -> Unit,
) {
    val intent = Intent()
    intent.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            it.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            it.putExtra(Settings.EXTRA_APP_PACKAGE, this.packageName)
        } else {
            it.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            it.putExtra("app_package", this.packageName)
            it.putExtra("app_uid", this.applicationInfo.uid)
        }
    }
    startExternalActivitySafe(intent, onError)
}

/**
 * Checks for the presence of an activity before starting it. In case it's not present,
 * [onActivityNotPresent] is invoked, preventing ActivityNotFoundException from being thrown.
 * This is useful when navigating to external activities like device permission settings,
 * notification settings, default app settings, etc.
 *
 * @param intent The Intent of the activity to resolve and start.
 * @param onActivityNotPresent Invoked when the activity to handle the intent is not present.
 */
inline fun Context.startExternalActivitySafe(intent: Intent, onActivityNotPresent: () -> Unit) {
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        onActivityNotPresent()
    }
}

/**
 * Helper function used to determine if the user's device is set to dark mode.
 *
 * @return true if the system is considered to be in dark theme.
 */
fun Context.isSystemInDarkTheme(): Boolean =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

/**
 * Returns the message to be shown when a tab is closed based on whether the tab was private or not.
 * @param private true if the tab was private, false otherwise.
 */
fun Context.tabClosedUndoMessage(private: Boolean): String = if (private) {
    getString(R.string.snackbar_private_tab_closed)
} else {
    getString(R.string.snackbar_tab_closed)
}

/**
 * Helper function used to determine whether the app's total *window* size is at least that of a tablet.
 * This relies on the window size check from [AcornWindowSize]. To determine whether the device's
 * *physical* size is at least the size of a tablet, use [Context.isLargeScreenSize] instead.
 *
 * @return true if the app has a large window size akin to a tablet.
 */
fun Context.isLargeWindow(): Boolean = AcornWindowSize.isLargeWindow(this)

/**
 * Returns true if the toolbar is position at the bottom.
 */
fun Context.isToolbarAtBottom() = components.settings.toolbarPosition == ToolbarPosition.BOTTOM
