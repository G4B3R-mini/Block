/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.ext

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_SUBJECT
import android.content.Intent.EXTRA_TEXT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.provider.Settings
import android.service.chooser.ChooserAction
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import androidx.annotation.StringRes
import mozilla.components.support.locale.LocaleManager
import com.shmibblez.inferno.BrowserApplication
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.Components
//import com.shmibblez.inferno.components.metrics.MetricController
import com.shmibblez.inferno.components.toolbar.ToolbarPosition
import com.shmibblez.inferno.mozillaAndroidComponents.compose.base.theme.AcornWindowSize
import com.shmibblez.inferno.settings.advanced.getSelectedLocale
import com.shmibblez.inferno.utils.isLargeScreenSize
import mozilla.components.browser.state.state.SessionState
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineSession.LoadUrlFlags
import mozilla.components.concept.storage.HistoryMetadataKey
import mozilla.components.support.base.log.Log
import mozilla.components.support.ktx.android.content.createChooserExcludingCurrentApp
import java.lang.String.format
import java.util.ArrayList
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
 * Shares content via [ACTION_SEND] intent.
 *
 * @param text the data to be shared [EXTRA_TEXT]
 * @param subject of the intent [EXTRA_TEXT]
 * @return true it is able to share false otherwise.
 */
fun Context.shareTextList(
    textList: ArrayList<String>,
    subject: String = getString(R.string.mozac_support_ktx_share_dialog_title),
): Boolean {
    return try {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putStringArrayListExtra(Intent.ACTION_SEND_MULTIPLE, textList)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, subject))

        true
    } catch (e: ActivityNotFoundException) {
        Log.log(
            Log.Priority.WARN,
            message = "No activity to share to found",
            throwable = e,
            tag = "share"
        )

        false
    }
}

/**
 * Add new tab convenience fun
 */
// todo: next to current, go to tab action add new tab and copy
fun Components.newTab(
    nextTo: String? = null,
    private: Boolean = false,
    url: String = if (!private) "inferno:home" else "inferno:privatebrowsing",
    selectTab: Boolean = true,
    startLoading: Boolean = true,
    parentId: String? = null,
    flags: LoadUrlFlags = LoadUrlFlags.none(),
    contextId: String? = null,
    engineSession: EngineSession? = null,
    source: SessionState.Source = SessionState.Source.Internal.NewTab,
    searchTerms: String = "",
    historyMetadata: HistoryMetadataKey? = null,
    isSearch: Boolean = false,
    searchEngineName: String? = null,
    additionalHeaders: Map<String, String>? = null,
): String {
    val tabId = this.useCases.tabsUseCases.addTab(
        url = url,
        selectTab = selectTab,
        startLoading = startLoading,
        parentId = parentId,
        flags = flags,
        contextId = contextId,
        engineSession = engineSession,
        source = source,
        searchTerms = searchTerms,
        private = private,
        historyMetadata = historyMetadata,
        isSearch = isSearch,
        searchEngineName = searchEngineName,
        additionalHeaders = additionalHeaders,
    )

    if (nextTo != null)
    // move new tab next to current
        this.useCases.tabsUseCases.moveTabs(listOf(tabId), nextTo, true)

    return tabId
}


/**
 * Helper function to get the MetricController off of context.
 */
//val Context.metrics: MetricController
//    get() = this.components.analytics.metrics

fun Context.asActivity() =
    (this as? ContextThemeWrapper)?.baseContext as? Activity ?: this as? Activity

fun Context.getPreferenceKey(@StringRes resourceId: Int): String = resources.getString(resourceId)

/**
 * Gets the Root View with an activity context
 *
 * @return ViewGroup? if it is able to get a root view from the context
 */
fun Context.getRootView(): View? =
    asActivity()?.window?.decorView?.findViewById<View>(android.R.id.content) as? ViewGroup

fun Context.settings() = components.settings

fun Context.infernoTheme() = components.infernoTheme

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
