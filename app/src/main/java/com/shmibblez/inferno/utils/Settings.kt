/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.utils

import android.accessibilityservice.AccessibilityServiceInfo.CAPABILITY_CAN_PERFORM_GESTURES
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.ShortcutManager
import android.os.Build
import android.view.accessibility.AccessibilityManager
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.shmibblez.inferno.Config
import com.shmibblez.inferno.FeatureFlags
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.BrowserFragment
import com.shmibblez.inferno.browser.browsingmode.BrowsingMode
import com.shmibblez.inferno.browser.tabstrip.isTabStripEnabled
import com.shmibblez.inferno.components.settings.counterPreference
import com.shmibblez.inferno.components.settings.featureFlagPreference
import com.shmibblez.inferno.components.settings.lazyFeatureFlagPreference
import com.shmibblez.inferno.components.toolbar.ToolbarPosition
import com.shmibblez.inferno.components.toolbar.navbar.shouldAddNavigationBar
import com.shmibblez.inferno.debugsettings.addresses.SharedPrefsAddressesDebugLocalesRepository
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.getPreferenceKey
import com.shmibblez.inferno.home.HomeFragment
import com.shmibblez.inferno.nimbus.CookieBannersSection
import com.shmibblez.inferno.nimbus.FxNimbus
import com.shmibblez.inferno.nimbus.HomeScreenSection
import com.shmibblez.inferno.nimbus.Mr2022Section
import com.shmibblez.inferno.nimbus.QueryParameterStrippingSection
import com.shmibblez.inferno.nimbus.QueryParameterStrippingSection.QUERY_PARAMETER_STRIPPING
import com.shmibblez.inferno.nimbus.QueryParameterStrippingSection.QUERY_PARAMETER_STRIPPING_ALLOW_LIST
import com.shmibblez.inferno.nimbus.QueryParameterStrippingSection.QUERY_PARAMETER_STRIPPING_PMB
import com.shmibblez.inferno.nimbus.QueryParameterStrippingSection.QUERY_PARAMETER_STRIPPING_STRIP_LIST
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.PhoneFeature
import com.shmibblez.inferno.settings.deletebrowsingdata.DeleteBrowsingDataOnQuitType
import com.shmibblez.inferno.settings.logins.SavedLoginsSortingStrategyMenu
import com.shmibblez.inferno.settings.logins.SortingStrategy
import com.shmibblez.inferno.settings.sitepermissions.AUTOPLAY_ALLOW_ALL
import com.shmibblez.inferno.settings.sitepermissions.AUTOPLAY_ALLOW_ON_WIFI
import com.shmibblez.inferno.settings.sitepermissions.AUTOPLAY_BLOCK_ALL
import com.shmibblez.inferno.settings.sitepermissions.AUTOPLAY_BLOCK_AUDIBLE
import com.shmibblez.inferno.toolbar.defaultToolbarItems
import com.shmibblez.inferno.wallpapers.Wallpaper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mozilla.components.concept.engine.Engine.HttpsOnlyMode
import mozilla.components.concept.engine.EngineSession.CookieBannerHandlingMode
import mozilla.components.feature.sitepermissions.SitePermissionsRules
import mozilla.components.feature.sitepermissions.SitePermissionsRules.Action
import mozilla.components.feature.sitepermissions.SitePermissionsRules.AutoplayAction
import mozilla.components.support.ktx.android.content.PreferencesHolder
import mozilla.components.support.ktx.android.content.booleanPreference
import mozilla.components.support.ktx.android.content.intPreference
import mozilla.components.support.ktx.android.content.longPreference
import mozilla.components.support.ktx.android.content.stringPreference
import mozilla.components.support.ktx.android.content.stringSetPreference
import mozilla.components.support.locale.LocaleManager
import mozilla.components.support.utils.BrowsersCache
import java.security.InvalidParameterException

//private const val AUTOPLAY_USER_SETTING = "AUTOPLAY_USER_SETTING"

/**
 * A simple wrapper for SharedPreferences that makes reading preference a little bit easier.
 *
 * @param appContext Reference to application context.
 */
@Suppress("LargeClass", "TooManyFunctions")
class Settings(private val appContext: Context) : PreferencesHolder {

    companion object {
        const val FENIX_PREFERENCES = "fenix_preferences"

        private const val BLOCKED_INT = 0
        private const val ASK_TO_ALLOW_INT = 1
        private const val ALLOWED_INT = 2

        //        private const val CFR_COUNT_CONDITION_FOCUS_INSTALLED = 1
//        private const val CFR_COUNT_CONDITION_FOCUS_NOT_INSTALLED = 3
        private const val INACTIVE_TAB_MINIMUM_TO_SHOW_AUTO_CLOSE_DIALOG = 20

        const val ONE_MINUTE_MS = 60 * 1000L
        private const val ONE_HOUR_MS = 60 * ONE_MINUTE_MS
        const val ONE_DAY_MS = 24 * ONE_HOUR_MS
        const val TWO_DAYS_MS = 2 * ONE_DAY_MS
        const val THREE_DAYS_MS = 3 * ONE_DAY_MS
        const val ONE_WEEK_MS = 60 * 60 * 24 * 7 * 1000L
        const val ONE_MONTH_MS = (60 * 60 * 24 * 365 * 1000L) / 12
        const val FOUR_HOURS_MS = 4 * ONE_HOUR_MS

        /**
         * The minimum number a search groups should contain.
         */
        @VisibleForTesting
        internal var SEARCH_GROUP_MINIMUM_SITES: Int = 2

        // The maximum number of top sites to display.
        const val TOP_SITES_MAX_COUNT = 16

        /**
         * Only fetch top sites from the [ContileTopSitesProvider] when the number of default and
         * pinned sites are below this maximum threshold.
         */
        const val TOP_SITES_PROVIDER_MAX_THRESHOLD = 8

        private fun Action.toInt() = when (this) {
            Action.BLOCKED -> BLOCKED_INT
            Action.ASK_TO_ALLOW -> ASK_TO_ALLOW_INT
            Action.ALLOWED -> ALLOWED_INT
        }

        private fun AutoplayAction.toInt() = when (this) {
            AutoplayAction.BLOCKED -> BLOCKED_INT
            AutoplayAction.ALLOWED -> ALLOWED_INT
        }

        private fun Int.toAction() = when (this) {
            BLOCKED_INT -> Action.BLOCKED
            ASK_TO_ALLOW_INT -> Action.ASK_TO_ALLOW
            ALLOWED_INT -> Action.ALLOWED
            else -> throw InvalidParameterException("$this is not a valid SitePermissionsRules.Action")
        }

        private fun Int.toAutoplayAction() = when (this) {
            BLOCKED_INT -> AutoplayAction.BLOCKED
            ALLOWED_INT -> AutoplayAction.ALLOWED
            // Users from older versions may have saved invalid values. Migrate them to BLOCKED
            ASK_TO_ALLOW_INT -> AutoplayAction.BLOCKED
            else -> throw InvalidParameterException("$this is not a valid SitePermissionsRules.AutoplayAction")
        }
    }

//    @VisibleForTesting
//    internal val isCrashReportEnabledInBuild: Boolean =
//        BuildConfig.CRASH_REPORTING && Config.channel.isReleased

    private fun <T> getPref(default: () -> T, getter: (InfernoSettings?) -> T?): T {
        return getter.invoke(runBlocking { appContext.infernoSettingsDataStore.data.lastOrNull() })
            ?: default.invoke()
    }

    private fun <T> getPref(default: T, getter: (InfernoSettings?) -> T?): T {
        return getter.invoke(runBlocking { appContext.infernoSettingsDataStore.data.lastOrNull() })
            ?: default
    }

    private fun setPref(setter: (InfernoSettings.Builder) -> Unit) {
        MainScope().launch {
            appContext.infernoSettingsDataStore.updateData {
                val builder = it.toBuilder()
                setter.invoke(builder)
                builder.build()
            }
        }
    }

    override val preferences: SharedPreferences =
        appContext.getSharedPreferences(FENIX_PREFERENCES, MODE_PRIVATE)


    /*** vars ***/


    var numberOfAppLaunches: Long
        get() = getPref(default = 0L) { it?.numberOfAppLaunches }
        set(value) {
            setPref { it.setNumberOfAppLaunches(value) }
        }
//    var numberOfAppLaunches by intPreference(
//        appContext.getPreferenceKey(R.string.pref_key_times_app_opened),
//        default = 0,
//    )
    /**
     * Indicates the last time when the user was interacting with the [BrowserFragment],
     * This is useful to determine if the user has to start on the [HomeFragment]
     * or it should go directly to the [BrowserFragment].
     *
     * This value defaults to 0L because we want to know if the user never had any interaction
     * with the [BrowserFragment]
     */
    var lastBrowseActivity: Long
        get() = getPref(default = 0L) { it?.lastBrowseActivityMs }
        set(value) {
            setPref { it.setLastBrowseActivityMs(value) }
        }
//    var lastBrowseActivity by longPreference(
//        appContext.getPreferenceKey(R.string.pref_key_last_browse_activity_time),
//        default = 0L,
//    )


    /*** toolbar settings ***/


    var shouldUseBottomToolbar: Boolean
        get() = getPref(default = true) { it?.toolbarVerticalPosition == InfernoSettings.VerticalToolbarPosition.TOOLBAR_BOTTOM }
        set(value) {
            when (value) {
                true -> setPref { it.setToolbarVerticalPosition(InfernoSettings.VerticalToolbarPosition.TOOLBAR_BOTTOM) }
                false -> setPref { it.setToolbarVerticalPosition(InfernoSettings.VerticalToolbarPosition.TOOLBAR_TOP) }
            }
        }

    //    var shouldUseBottomToolbar by booleanPreference(
//        key = appContext.getPreferenceKey(R.string.pref_key_toolbar_bottom),
//        default = false,
//        persistDefaultIfNotExists = true,
//    )
    var inAppToolbarVerticalPosition: InfernoSettings.VerticalToolbarPosition
        get() = getPref(default = InfernoSettings.VerticalToolbarPosition.TOOLBAR_BOTTOM) { it?.inAppToolbarVerticalPosition }
        set(value) {
            setPref { it.setToolbarVerticalPosition(value) }
        }
    var toolbarItems: List<InfernoSettings.ToolbarItem>
        get() = getPref(default = (null as InfernoSettings.ToolbarItem?).defaultToolbarItems) { it?.toolbarItemsList }
        set(value) {
            setPref {
                it.toolbarItemsList.apply {
                    this.clear()
                    this.addAll(value)
                }
            }
        }
    var defaultSearchEngineName: String
        get() = getPref(default = "") { it?.defaultSearchEngine }
        set(value) {
            setPref { it.setDefaultSearchEngine(value) }
        }

    //    var defaultSearchEngineName by stringPreference(
//        appContext.getPreferenceKey(R.string.pref_key_search_engine),
//        default = "",
//    )
    var shouldAutocompleteInAwesomebar: Boolean
        get() = getPref(default = true) { it?.shouldAutocompleteUrls }
        set(value) {
            setPref { it.setShouldAutocompleteUrls(value) }
        }

    //    val shouldAutocompleteInAwesomebar by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_enable_autocomplete_urls),
//        default = true,
//    )
    var shouldAutocompleteInAwesomebarPrivate: Boolean
        get() = getPref(default = true) { it?.shouldAutocompleteUrlsInPrivate }
        set(value) {
            setPref { it.setShouldAutocompleteUrlsInPrivate(value) }
        }
    var shouldShowSearchSuggestions: Boolean
        get() = getPref(default = true) { it?.shouldShowSearchSuggestions }
        set(value) {
            setPref { it.setShouldShowSearchSuggestions(value) }
        }

    //    val shouldShowSearchSuggestions by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_show_search_suggestions),
//        default = true,
//    )
    var shouldShowSearchSuggestionsInPrivate: Boolean
        get() = getPref(default = true) { it?.shouldShowSearchSuggestionsInPrivate }
        set(value) {
            setPref { it.setShouldShowSearchSuggestionsInPrivate(value) }
        }

    //    var shouldShowSearchSuggestionsInPrivate by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_show_search_suggestions_in_private),
//        default = false,
//    )
    var shouldShowHistorySuggestions: Boolean
        get() = getPref(default = true) { it?.shouldShowHistorySuggestions }
        set(value) {
            setPref { it.setShouldShowHistorySuggestions(value) }
        }
//    val shouldShowHistorySuggestions by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_search_browsing_history),
//        default = true,
//    )

    var shouldShowBookmarkSuggestions: Boolean
        get() = getPref(default = true) { it?.shouldShowBookmarkSuggestions }
        set(value) {
            setPref { it.setShouldShowBookmarkSuggestions(value) }
        }
//    val shouldShowBookmarkSuggestions by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_search_bookmarks),
//        default = true,
//    )

    var shouldShowSyncedTabsSuggestions: Boolean
        get() = getPref(default = true) { it?.shouldShowSyncedTabsSuggestions }
        set(value) {
            setPref { it.setShouldShowSyncedTabsSuggestions(value) }
        }
//    val shouldShowSyncedTabsSuggestions by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_search_synced_tabs),
//        default = true,
//    )

    var shouldShowClipboardSuggestions: Boolean
        get() = getPref(default = true) { it?.shouldShowClipboardSuggestions }
        set(value) {
            setPref { it.setShouldShowClipboardSuggestions(value) }
        }

    //    val shouldShowClipboardSuggestions by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_show_clipboard_suggestions),
//        default = true,
//    )
    var shouldShowVoiceSearch: Boolean
        get() = getPref(default = true) { it?.shouldShowVoiceSearch }
        set(value) {
            setPref { it.setShouldShowVoiceSearch(value) }
        }

    //    var shouldShowVoiceSearch by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_show_voice_search),
//        default = true,
//    )

    // not used
//    val shouldShowSearchShortcuts by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_show_search_engine_shortcuts),
//        default = false,
//    )


    /*** tab settings ***/


    // general

    var manuallyCloseTabs: Boolean
        get() = getPref(default = true) { it?.closeTabsMethod == InfernoSettings.CloseTabsMethod.CLOSE_TABS_MANUALLY }
        set(value) {
            when (value) {
                true -> setPref { it.setCloseTabsMethod(InfernoSettings.CloseTabsMethod.CLOSE_TABS_MANUALLY) }
                false -> setPref { it.setCloseTabsMethod(InfernoSettings.CloseTabsMethod.CLOSE_TABS_AFTER_ONE_MONTH) }
            }
        }

    //    var manuallyCloseTabs by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_close_tabs_manually),
//        default = true,
//    )
    var closeTabsAfterOneDay: Boolean
        get() = getPref(default = true) { it?.closeTabsMethod == InfernoSettings.CloseTabsMethod.CLOSE_TABS_AFTER_ONE_DAY }
        set(value) {
            when (value) {
                true -> setPref { it.setCloseTabsMethod(InfernoSettings.CloseTabsMethod.CLOSE_TABS_AFTER_ONE_DAY) }
                false -> setPref { it.setCloseTabsMethod(InfernoSettings.CloseTabsMethod.CLOSE_TABS_AFTER_ONE_MONTH) }
            }
        }

    var closeTabsAfterOneWeek: Boolean
        get() = getPref(default = true) { it?.closeTabsMethod == InfernoSettings.CloseTabsMethod.CLOSE_TABS_AFTER_ONE_WEEK }
        set(value) {
            when (value) {
                true -> setPref { it.setCloseTabsMethod(InfernoSettings.CloseTabsMethod.CLOSE_TABS_AFTER_ONE_WEEK) }
                false -> setPref { it.setCloseTabsMethod(InfernoSettings.CloseTabsMethod.CLOSE_TABS_AFTER_ONE_MONTH) }
            }
        }
//    var closeTabsAfterOneWeek by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_close_tabs_after_one_week),
//        default = false,
//    )

    private var closeTabsAfterOneMonth: Boolean
        get() = getPref(default = true) { it?.closeTabsMethod == InfernoSettings.CloseTabsMethod.CLOSE_TABS_AFTER_ONE_MONTH }
        set(value) {
            when (value) {
                true -> setPref { it.setCloseTabsMethod(InfernoSettings.CloseTabsMethod.CLOSE_TABS_AFTER_ONE_MONTH) }
                false -> setPref { it.setCloseTabsMethod(InfernoSettings.CloseTabsMethod.CLOSE_TABS_AFTER_ONE_MONTH) }
            }
        }
//    var closeTabsAfterOneMonth by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_close_tabs_after_one_month),
//        default = false,
//    )
    /**
     * Indicates if the user has enabled the inactive tabs feature.
     */
    var inactiveTabsAreEnabled: Boolean
        get() = getPref(default = true) { it?.shouldSeparateInactiveTabs }
        set(value) {
            setPref { it.setShouldSeparateInactiveTabs(value) }
        }

    //    var inactiveTabsAreEnabled by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_inactive_tabs),
//        default = true,
//    )
    var isTabBarEnabled: Boolean
        get() = getPref(default = true) { it?.isTabBarEnabled }
        set(value) {
            setPref { it.setIsTabBarEnabled(value) }
        }
    var tabBarVerticalPosition: InfernoSettings.VerticalTabBarPosition
        get() = getPref(default = InfernoSettings.VerticalTabBarPosition.TAB_BAR_BOTTOM) { it?.tabBarVerticalPosition }
        set(value) {
            setPref { it.setTabBarVerticalPosition(value) }
        }
    var tabBarPosition: InfernoSettings.TabBarPosition
        get() = getPref(default = InfernoSettings.TabBarPosition.TAB_BAR_ABOVE_TOOLBAR) { it?.tabBarPosition }
        set(value) {
            setPref { it.setTabBarPosition(value) }
        }
    var tabTrayStyle: InfernoSettings.TabTrayStyle
        get() = getPref(default = InfernoSettings.TabTrayStyle.TAB_TRAY_LIST) { it?.tabTrayStyle }
        set(value) {
            setPref { it.setTabTrayStyle(value) }
        }
    var isPullToRefreshEnabledInBrowser: Boolean
        get() = getPref(default = true) { it?.isPullToRefreshEnabled }
        set(value) {
            setPref { it.setIsPullToRefreshEnabled(value) }
        }
    var isDynamicToolbarEnabled: Boolean
        get() = getPref(default = true) { it?.isDynamicToolbarEnabled }
        set(value) {
            setPref { it.setIsDynamicToolbarEnabled(value) }
        }
    var isSwipeToolbarToSwitchTabsEnabled: Boolean
        get() = false // getPref(default = true) { it?.isSwipeHorizontalToSwitchTabsEnabled }
        set(value) {
//            setPref { it.setIsSwipeHorizontalToSwitchTabsEnabled(value) }
        }
    var isSwipeUpToCloseTabEnabled: Boolean
        get() = false // getPref(default = true) { it?.isSwipeUpToCloseTabEnabled }
        set(value) {
//            setPref { it.setIsSwipeUpToCloseTabEnabled(value) }
        }
    var selectedDefaultTheme: InfernoSettings.DefaultTheme
        get() = getPref(default = InfernoSettings.DefaultTheme.INFERNO_DARK) { it?.selectedDefaultTheme }
        set(value) {
            setPref { it.setSelectedDefaultTheme(value) }
        }
    var selectedCustomTheme: String
        get() = getPref(default = "") { it?.selectedCustomTheme }
        set(value) {
            setPref { it.setSelectedCustomTheme(value) }
        }
    var customThemes: Map<String, InfernoSettings.InfernoTheme>
        get() = getPref(default = emptyMap()) { it?.customThemesMap }
        set(value) {
            setPref {
                it.customThemesMap.apply {
                    this.clear()
                    this += value
                }
            }
        }


    /*** theme settings ***/


    // general

    var defaultTopSitesAdded: Boolean
        get() = getPref(default = false) { it?.defaultTopSitesAdded } // todo: set to true and add sponsors if ever get to that point
        set(value) {
            setPref { it.setDefaultTopSitesAdded(value) }
        }

    /**
     * Indicates whether or not top sites should be shown on the home screen.
     */
    var showTopSitesFeature: Boolean
        get() = getPref(default = { homescreenSections[HomeScreenSection.TOP_SITES] == true }) { it?.shouldShowTopSites }
        set(value) {
            setPref { it.setShouldShowTopSites(value) }
        }
//    var showTopSitesFeature by lazyFeatureFlagPreference(
//        appContext.getPreferenceKey(R.string.pref_key_show_top_sites),
//        featureFlag = true,
//        default = { homescreenSections[HomeScreenSection.TOP_SITES] == true },
//    )
    /**
     * Indicates if the recent tabs functionality should be visible.
     */
    var showRecentTabsFeature: Boolean
        get() = getPref(default = true) { it?.shouldShowRecentTabs }
        set(value) {
            setPref { it.setShouldShowRecentTabs(value) }
        }
//    var showRecentTabsFeature by lazyFeatureFlagPreference(
//        appContext.getPreferenceKey(R.string.pref_key_recent_tabs),
//        featureFlag = true,
//        default = { homescreenSections[HomeScreenSection.JUMP_BACK_IN] == true },
//    )

    /**
     * Indicates if the recent saved bookmarks functionality should be visible.
     */
    var showBookmarksHomeFeature: Boolean
        get() = getPref(default = true) { it?.shouldShowBookmarks }
        set(value) {
            setPref { it.setShouldShowBookmarks(value) }
        }

    //    var showBookmarksHomeFeature by lazyFeatureFlagPreference(
//        appContext.getPreferenceKey(R.string.pref_key_customization_bookmarks),
//        default = { homescreenSections[HomeScreenSection.BOOKMARKS] == true },
//        featureFlag = true,
//    )
    var shouldShowHistory: Boolean
        get() = getPref(default = true) { it?.shouldShowHistory }
        set(value) {
            setPref { it.setShouldShowHistory(value) }
        }

    //    var historyMetadataUIFeature by lazyFeatureFlagPreference(
//        appContext.getPreferenceKey(R.string.pref_key_history_metadata_feature),
//        default = { homescreenSections[HomeScreenSection.RECENT_EXPLORATIONS] == true },
//        featureFlag = true,
//    )
    var shouldShowSearchWidget: Boolean
        get() = getPref(default = true) { it?.shouldShowSearchWidget }
        set(value) {
            setPref { it.setShouldShowSearchWidget(value) }
        }
//    val searchWidgetInstalled by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_search_widget_installed_2),
//        default = false,
//    )


    // navigation

    var pageWhenBrowserReopened: InfernoSettings.PageWhenBrowserReopened
        get() = getPref(default = InfernoSettings.PageWhenBrowserReopened.OPEN_ON_HOME_AFTER_FOUR_HOURS) { it?.pageWhenBrowserReopened }
        set(value) {
            setPref { it.setPageWhenBrowserReopened(value) }
        }

    /**
     * Indicates if the user should start on the home screen, based on the user's preferences.
     */
    fun shouldStartOnHome(): Boolean {
        return when {
            openHomepageAfterFourHoursOfInactivity -> timeNowInMillis() - lastBrowseActivity >= FOUR_HOURS_MS
            alwaysOpenTheHomepageWhenOpeningTheApp -> true
            alwaysOpenTheLastTabWhenOpeningTheApp -> false
            else -> false
        }
    }

    /**
     * Indicates if the user has selected the option to start on the home screen after
     * four hours of inactivity.
     */
    var openHomepageAfterFourHoursOfInactivity: Boolean
        get() = getPref(default = true) { it?.pageWhenBrowserReopened == InfernoSettings.PageWhenBrowserReopened.OPEN_ON_HOME_AFTER_FOUR_HOURS }
        set(value) {
            if (value) setPref { it.setPageWhenBrowserReopened(InfernoSettings.PageWhenBrowserReopened.OPEN_ON_HOME_AFTER_FOUR_HOURS) }
        }

    /**
     * Indicates if the user has selected the option to always start on the home screen.
     */
    var alwaysOpenTheHomepageWhenOpeningTheApp: Boolean
        get() = getPref(default = false) { it?.pageWhenBrowserReopened == InfernoSettings.PageWhenBrowserReopened.OPEN_ON_HOME_ALWAYS }
        set(value) {
            if (value) setPref { it.setPageWhenBrowserReopened(InfernoSettings.PageWhenBrowserReopened.OPEN_ON_HOME_ALWAYS) }
        }

    /**
     * Indicates if the user has selected the option to never start on the home screen and have
     * their last tab opened.
     */
    var alwaysOpenTheLastTabWhenOpeningTheApp: Boolean
        get() = getPref(default = false) { it?.pageWhenBrowserReopened == InfernoSettings.PageWhenBrowserReopened.OPEN_ON_LAST_TAB }
        set(value) {
            if (value) setPref { it.setPageWhenBrowserReopened(InfernoSettings.PageWhenBrowserReopened.OPEN_ON_LAST_TAB) }
        }
    var useInfernoHome: Boolean
        get() = getPref(default = true) { it?.shouldUseInfernoHome }
        set(value) {
            setPref { it.setShouldUseInfernoHome(value) }
        }
    var homeUrl: String
        get() = getPref(default = "") { it?.homeUrl }
        set(value) {
            setPref { it.setHomeUrl(value) }
        }


    /*** general ***/


    // on quit

    var shouldDeleteBrowsingDataOnQuit: Boolean
        get() = getPref(default = false) { it?.deleteBrowsingDataOnQuit }
        set(value) {
            setPref { it.setDeleteBrowsingDataOnQuit(value) }
        }
    var deleteOpenTabs: Boolean
        get() = getPref(default = true) { it?.deleteOpenTabsOnQuit }
        set(value) {
            setPref { it.setDeleteOpenTabsOnQuit(value) }
        }
    var deleteBrowsingHistory: Boolean
        get() = getPref(default = true) { it?.deleteBrowsingHistoryOnQuit }
        set(value) {
            setPref { it.setDeleteBrowsingHistoryOnQuit(value) }
        }
    var deleteCookies: Boolean
        get() = getPref(default = true) { it?.deleteCookiesAndSiteDataOnQuit }
        set(value) {
            setPref { it.setDeleteCookiesAndSiteDataOnQuit(value) }
        }
    var deleteCache: Boolean
        get() = getPref(default = true) { it?.deleteCachesOnQuit }
        set(value) {
            setPref { it.setDeleteCachesOnQuit(value) }
        }
    var deleteSitePermissions: Boolean
        get() = getPref(default = true) { it?.deletePermissionsOnQuit }
        set(value) {
            setPref { it.setDeletePermissionsOnQuit(value) }
        }
    var deleteDownloads: Boolean
        get() = getPref(default = true) { it?.deleteDownloadsOnQuit }
        set(value) {
            setPref { it.setDeleteDownloadsOnQuit(value) }
        }

    // other

    var shouldUseExternalDownloadManager: Boolean
        get() = getPref(default = false) { it?.shouldUseExternalDownloadManager }
        set(value) {
            setPref { it.setShouldUseExternalDownloadManager(value) }
        }

    var isRemoteDebuggingEnabled: Boolean
        get() = getPref(default = false) { it?.remoteDebuggingOverUsb }
        set(value) {
            setPref { it.setRemoteDebuggingOverUsb(value) }
        }
//    val isRemoteDebuggingEnabled by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_remote_debugging),
//        default = false,
//    )


    /*** autofill data ***/


    // login storage

    var shouldPromptToSaveLogins: Boolean
        get() = getPref(default = true) { it?.saveLoginsSettings == InfernoSettings.LoginsStorage.ASK_TO_SAVE }
        set(value) {
            when (value) {
                true -> setPref { it.setSaveLoginsSettings(InfernoSettings.LoginsStorage.ASK_TO_SAVE) }
                false -> setPref { it.setSaveLoginsSettings(InfernoSettings.LoginsStorage.DONT_SAVE) }
            }
        }
    var shouldSyncLogins: Boolean
        get() = getPref(default = true) { it?.shouldSyncLogins }
        set(value) {
            setPref { it.setShouldSyncLogins(value) }
        }
    var shouldAutofillLogins: Boolean
        get() = getPref(default = true) { it?.shouldAutofillLogins }
        set(value) {
            setPref { it.setShouldAutofillLogins(value) }
        }
    var isAndroidAutofillEnabled: Boolean
        get() = getPref(default = true) { it?.isAndroidAutofillEnabled }
        set(value) {
            setPref { it.setIsAndroidAutofillEnabled(value) }
        }

    /**
     * Stores the user choice from the "Autofill Addresses" settings for whether
     * save and autofill addresses should be enabled or not.
     * If set to `true` when the user focuses on address fields in a webpage an Android prompt is shown,
     * allowing the selection of an address details to be automatically filled in the webpage fields.
     */
    var shouldAutofillAddressDetails: Boolean
        get() = getPref(default = true) { it?.isAddressSaveAndAutofillEnabled }
        set(value) {
            setPref { it.setIsAddressSaveAndAutofillEnabled(value) }
        }

    /**
     * Storing the user choice from the "Payment methods" settings for whether save and autofill cards
     * should be enabled or not.
     * If set to `true` when the user focuses on credit card fields in the webpage an Android prompt letting her
     * select the card details to be automatically filled will appear.
     */
    var shouldAutofillCreditCardDetails: Boolean
        get() = getPref(default = true) { it?.isCardSaveAndAutofillEnabled }
        set(value) {
            setPref { it.setIsCardSaveAndAutofillEnabled(value) }
        }
    var shouldSyncCards: Boolean
        get() = getPref(default = true) { it?.shouldSyncCards }
        set(value) {
            setPref { it.setShouldSyncCards(value) }
        }


    /*** site settings ***/


    // site settings

    private var appLinksSetting: InfernoSettings.AppLinks
        get() = getPref(default = InfernoSettings.AppLinks.APP_LINKS_ASK_TO_OPEN) { it?.appLinksSetting }
        set(value) {
            setPref { it.setAppLinksSetting(value) }
        }
    private var autoPlaySetting: InfernoSettings.AutoPlay
        get() = getPref(default = InfernoSettings.AutoPlay.BLOCK_AUDIO_ONLY) { it?.autoplaySetting }
        set(value) {
            setPref { it.setAutoplaySetting(value) }
        }
    private var cameraSetting: InfernoSettings.Camera
        get() = getPref(default = InfernoSettings.Camera.CAMERA_ASK_TO_ALLOW) { it?.cameraSetting }
        set(value) {
            setPref { it.setCameraSetting(value) }
        }
    private var locationSetting: InfernoSettings.Location
        get() = getPref(default = InfernoSettings.Location.LOCATION_ASK_TO_ALLOW) { it?.locationSetting }
        set(value) {
            setPref { it.setLocationSetting(value) }
        }
    private var microphoneSetting: InfernoSettings.Microphone
        get() = getPref(default = InfernoSettings.Microphone.MICROPHONE_ASK_TO_ALLOW) { it?.microphoneSetting }
        set(value) {
            setPref { it.setMicrophoneSetting(value) }
        }
    private var notificationsSetting: InfernoSettings.Notifications
        get() = getPref(default = InfernoSettings.Notifications.NOTIFICATIONS_ASK_TO_ALLOW) { it?.notificationsSetting }
        set(value) {
            setPref { it.setNotificationsSetting(value) }
        }
    private var persistentStorageSetting: InfernoSettings.PersistentStorage
        get() = getPref(default = InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_ASK_TO_ALLOW) { it?.persistentStorageSetting }
        set(value) {
            setPref { it.setPersistentStorageSetting(value) }
        }
    private var crossSiteCookiesSetting: InfernoSettings.CrossSiteCookies
        get() = getPref(default = InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_ASK_TO_ALLOW) { it?.crossSiteCookiesSetting }
        set(value) {
            setPref { it.setCrossSiteCookiesSetting(value) }
        }
    private var drmControlledContentSetting: InfernoSettings.DrmControlledContent
        get() = getPref(default = InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_ASK_TO_ALLOW) { it?.drmControlledContentSetting }
        set(value) {
            setPref { it.setDrmControlledContentSetting(value) }
        }

    /**
     *  Returns a sitePermissions action for the provided [feature].
     */
    fun getSitePermissionsPhoneFeatureAction(
        feature: PhoneFeature,
        default: Action = Action.ASK_TO_ALLOW,
    ) = preferences.getInt(feature.getPreferenceKey(appContext), default.toInt()).toAction()

    /**
     * Gets the user selected autoplay setting.
     *
     * Under the hood, autoplay is represented by two settings, [PhoneFeature.AUTOPLAY_AUDIBLE] and
     * [PhoneFeature.AUTOPLAY_INAUDIBLE]. The user selection cannot be inferred from the combination of these
     * settings because, while on [AUTOPLAY_ALLOW_ON_WIFI], they will be indistinguishable from
     * either [AUTOPLAY_ALLOW_ALL] or [AUTOPLAY_BLOCK_ALL]. Because of this, we are forced to save
     * the user selected setting as well.
     */
    fun getAutoplayUserSetting() = when (autoPlaySetting) {
        InfernoSettings.AutoPlay.BLOCK_AUDIO_ONLY -> AUTOPLAY_BLOCK_AUDIBLE
        InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO -> AUTOPLAY_BLOCK_ALL
        InfernoSettings.AutoPlay.ALLOW_AUDIO_AND_VIDEO -> AUTOPLAY_ALLOW_ALL
        InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO_ON_CELLULAR_DATA_ONLY -> AUTOPLAY_ALLOW_ON_WIFI
        InfernoSettings.AutoPlay.UNRECOGNIZED -> AUTOPLAY_BLOCK_AUDIBLE
    }
//    fun getAutoplayUserSetting() = preferences.getInt(AUTOPLAY_USER_SETTING, AUTOPLAY_BLOCK_AUDIBLE)

    /**
     * Saves the user selected autoplay setting.
     *
     * Under the hood, autoplay is represented by two settings, [PhoneFeature.AUTOPLAY_AUDIBLE] and
     * [PhoneFeature.AUTOPLAY_INAUDIBLE]. The user selection cannot be inferred from the combination of these
     * settings because, while on [AUTOPLAY_ALLOW_ON_WIFI], they will be indistinguishable from
     * either [AUTOPLAY_ALLOW_ALL] or [AUTOPLAY_BLOCK_ALL]. Because of this, we are forced to save
     * the user selected setting as well.
     */
    fun setAutoplayUserSetting(
        autoplaySetting: Int,
    ) {
        this.autoPlaySetting = when (autoplaySetting.toInt()) {
            AUTOPLAY_ALLOW_ALL -> InfernoSettings.AutoPlay.ALLOW_AUDIO_AND_VIDEO
            AUTOPLAY_ALLOW_ON_WIFI -> InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO_ON_CELLULAR_DATA_ONLY
            AUTOPLAY_BLOCK_AUDIBLE -> InfernoSettings.AutoPlay.BLOCK_AUDIO_ONLY
            AUTOPLAY_BLOCK_ALL -> InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO
            else -> InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO
        }
//        preferences.edit().putInt(AUTOPLAY_USER_SETTING, autoplaySetting).apply()
    }

    private fun getSitePermissionsPhoneFeatureAutoplayAction(
        feature: PhoneFeature,
        default: AutoplayAction = AutoplayAction.BLOCKED,
    ) = when (autoPlaySetting) {
        InfernoSettings.AutoPlay.ALLOW_AUDIO_AND_VIDEO -> AutoplayAction.ALLOWED
        InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO -> AutoplayAction.BLOCKED
        else -> default
    }
//    private fun getSitePermissionsPhoneFeatureAutoplayAction(
//        feature: PhoneFeature,
//        default: AutoplayAction = AutoplayAction.BLOCKED,
//    ) = preferences.getInt(feature.getPreferenceKey(appContext), default.toInt()).toAutoplayAction()


    /**
     *  Sets a sitePermissions action for the provided [feature].
     */
    fun setSitePermissionsPhoneFeatureAction(
        feature: PhoneFeature,
        value: Action,
    ) {
        when (feature) {
            PhoneFeature.CAMERA -> {
                cameraSetting = when (value) {
                    Action.ALLOWED -> InfernoSettings.Camera.CAMERA_ALLOWED
                    Action.BLOCKED -> InfernoSettings.Camera.CAMERA_BLOCKED
                    Action.ASK_TO_ALLOW -> InfernoSettings.Camera.CAMERA_ASK_TO_ALLOW
                }
            }

            PhoneFeature.LOCATION -> {
                locationSetting = when (value) {
                    Action.ALLOWED -> InfernoSettings.Location.LOCATION_ALLOWED
                    Action.BLOCKED -> InfernoSettings.Location.LOCATION_BLOCKED
                    Action.ASK_TO_ALLOW -> InfernoSettings.Location.LOCATION_ASK_TO_ALLOW
                }
            }

            PhoneFeature.MICROPHONE -> {
                microphoneSetting = when (value) {
                    Action.ALLOWED -> InfernoSettings.Microphone.MICROPHONE_ALLOWED
                    Action.BLOCKED -> InfernoSettings.Microphone.MICROPHONE_BLOCKED
                    Action.ASK_TO_ALLOW -> InfernoSettings.Microphone.MICROPHONE_ASK_TO_ALLOW
                }
            }

            PhoneFeature.NOTIFICATION -> {
                notificationsSetting = when (value) {
                    Action.ALLOWED -> InfernoSettings.Notifications.NOTIFICATIONS_ALLOWED
                    Action.BLOCKED -> InfernoSettings.Notifications.NOTIFICATIONS_BLOCKED
                    Action.ASK_TO_ALLOW -> InfernoSettings.Notifications.NOTIFICATIONS_ASK_TO_ALLOW
                }
            }

            PhoneFeature.AUTOPLAY -> {
                autoPlaySetting = when (value) {
                    Action.ALLOWED -> InfernoSettings.AutoPlay.ALLOW_AUDIO_AND_VIDEO
                    Action.BLOCKED -> InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO
                    Action.ASK_TO_ALLOW -> InfernoSettings.AutoPlay.BLOCK_AUDIO_ONLY
                }
            }

            PhoneFeature.AUTOPLAY_AUDIBLE -> {
                autoPlaySetting = when (value.toInt()) {
                    AUTOPLAY_ALLOW_ALL -> InfernoSettings.AutoPlay.ALLOW_AUDIO_AND_VIDEO
                    AUTOPLAY_ALLOW_ON_WIFI -> InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO_ON_CELLULAR_DATA_ONLY
                    AUTOPLAY_BLOCK_AUDIBLE -> InfernoSettings.AutoPlay.BLOCK_AUDIO_ONLY
                    AUTOPLAY_BLOCK_ALL -> InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO
                    else -> InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO
                }
            }

            PhoneFeature.AUTOPLAY_INAUDIBLE -> {
                autoPlaySetting = when (value) {
                    Action.ALLOWED -> InfernoSettings.AutoPlay.ALLOW_AUDIO_AND_VIDEO
                    Action.BLOCKED -> InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO
                    Action.ASK_TO_ALLOW -> InfernoSettings.AutoPlay.BLOCK_AUDIO_ONLY
                }
            }

            PhoneFeature.PERSISTENT_STORAGE -> {
                persistentStorageSetting = when (value) {
                    Action.ALLOWED -> InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_ALLOWED
                    Action.BLOCKED -> InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_BLOCKED
                    Action.ASK_TO_ALLOW -> InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_ASK_TO_ALLOW
                }
            }

            PhoneFeature.MEDIA_KEY_SYSTEM_ACCESS -> {
                drmControlledContentSetting = when (value) {
                    Action.ALLOWED -> InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_ALLOWED
                    Action.BLOCKED -> InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_BLOCKED
                    Action.ASK_TO_ALLOW -> InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_ASK_TO_ALLOW
                }
            }

            PhoneFeature.CROSS_ORIGIN_STORAGE_ACCESS -> {
                crossSiteCookiesSetting = when (value) {
                    Action.ALLOWED -> InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_ALLOWED
                    Action.BLOCKED -> InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_BLOCKED
                    Action.ASK_TO_ALLOW -> InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_ASK_TO_ALLOW
                }
            }
        }
//        preferences.edit().putInt(feature.getPreferenceKey(appContext), value.toInt()).apply()
    }

    fun getSitePermissionsCustomSettingsRules(): SitePermissionsRules {
        return SitePermissionsRules(
            notification = getSitePermissionsPhoneFeatureAction(PhoneFeature.NOTIFICATION),
            microphone = getSitePermissionsPhoneFeatureAction(PhoneFeature.MICROPHONE),
            location = getSitePermissionsPhoneFeatureAction(PhoneFeature.LOCATION),
            camera = getSitePermissionsPhoneFeatureAction(PhoneFeature.CAMERA),
            autoplayAudible = getSitePermissionsPhoneFeatureAutoplayAction(
                feature = PhoneFeature.AUTOPLAY_AUDIBLE,
                default = AutoplayAction.BLOCKED,
            ),
            autoplayInaudible = getSitePermissionsPhoneFeatureAutoplayAction(
                feature = PhoneFeature.AUTOPLAY_INAUDIBLE,
                default = AutoplayAction.ALLOWED,
            ),
            persistentStorage = getSitePermissionsPhoneFeatureAction(PhoneFeature.PERSISTENT_STORAGE),
            crossOriginStorageAccess = getSitePermissionsPhoneFeatureAction(PhoneFeature.CROSS_ORIGIN_STORAGE_ACCESS),
            mediaKeySystemAccess = getSitePermissionsPhoneFeatureAction(PhoneFeature.MEDIA_KEY_SYSTEM_ACCESS),
        )
    }

    fun setSitePermissionSettingListener(lifecycleOwner: LifecycleOwner, listener: () -> Unit) {
        // todo: check if works
//        MainScope().launch {
        lifecycleOwner.lifecycleScope.launch {
            appContext.infernoSettingsDataStore.data.distinctUntilChanged(areEquivalent = { old, new ->
                old.appLinksSetting == new.appLinksSetting && old.autoplaySetting == new.autoplaySetting && old.cameraSetting == new.cameraSetting && old.locationSetting == new.locationSetting && old.microphoneSetting == new.microphoneSetting && old.notificationsSetting == new.notificationsSetting && old.persistentStorageSetting == new.persistentStorageSetting && old.crossSiteCookiesSetting == new.crossSiteCookiesSetting && old.drmControlledContentSetting == new.drmControlledContentSetting
            }).collect {
                listener.invoke()
            }
        }

//        val sitePermissionKeys = listOf(
//            PhoneFeature.NOTIFICATION,
//            PhoneFeature.MICROPHONE,
//            PhoneFeature.LOCATION,
//            PhoneFeature.CAMERA,
//            PhoneFeature.AUTOPLAY_AUDIBLE,
//            PhoneFeature.AUTOPLAY_INAUDIBLE,
//            PhoneFeature.PERSISTENT_STORAGE,
//            PhoneFeature.CROSS_ORIGIN_STORAGE_ACCESS,
//            PhoneFeature.MEDIA_KEY_SYSTEM_ACCESS,
//        ).map { it.getPreferenceKey(appContext) }
//
//        preferences.registerOnSharedPreferenceChangeListener(lifecycleOwner) { _, key ->
//            if (key in sitePermissionKeys) listener.invoke()
//        }
    }


    /*** accessibility ***/


    var shouldUseAutoSize: Boolean
        get() = getPref(default = true) { it?.shouldSizeFontAutomatically }
        set(value) {
            setPref { it.setShouldSizeFontAutomatically(value) }
        }
    var fontSizeFactor: Float
        get() = getPref(default = 1f) { it?.fontSizeFactor }
        set(value) {
            setPref { it.setFontSizeFactor(value) }
        }
    var forceEnableZoom: Boolean
        get() = getPref(default = false) { it?.shouldForceEnableZoomInWebsites }
        set(value) {
            setPref { it.setShouldForceEnableZoomInWebsites(value) }
        }
    var alwaysRequestDesktopSite: Boolean
        get() = getPref(default = false) { it?.alwaysRequestDesktopSite }
        set(value) {
            setPref { it.setAlwaysRequestDesktopSite(value) }
        }


    /*** locale/language is set through Storage with LocaleManager ***/


    /*** translation is managed through browserStore with TranslationsAction ***/


    /*** private mode settings ***/


    var openLinksInAPrivateTab: Boolean
        get() = getPref(default = false) { it?.openLinksInPrivateTab }
        set(value) {
            setPref { it.setOpenLinksInPrivateTab(value) }
        }
    var allowScreenshotsInPrivateMode: Boolean
        get() = getPref(default = false) { it?.allowScreenshotsInPrivateMode }
        set(value) {
            setPref { it.setAllowScreenshotsInPrivateMode(value) }
        }


    /*** enhanced tracking protection settings ***/


    var shouldUseTrackingProtection: Boolean
        get() = getPref(default = true) { it?.isEnhancedTrackingProtectionEnabled }
        set(value) {
            setPref { it.setIsEnhancedTrackingProtectionEnabled(value) }
        }
    var shouldEnableGlobalPrivacyControl: Boolean
        get() = getPref(default = false) { it?.isGlobalPrivacyControlEnabled }
        set(value) {
            setPref { it.setIsGlobalPrivacyControlEnabled(value) }
        }
    var selectedTrackingProtection: InfernoSettings.TrackingProtectionDefault
        get() = getPref(default = InfernoSettings.TrackingProtectionDefault.STANDARD) { it?.selectedTrackingProtection }
        set(value) {
            setPref {
                it.setSelectedTrackingProtection(value)
                appContext.components.let { components ->
                    val policy =
                        components.core.trackingProtectionPolicyFactory.createTrackingProtectionPolicy()
                    components.useCases.settingsUseCases.updateTrackingProtection.invoke(policy)
                    components.useCases.sessionUseCases.reload.invoke()
                }
            }
        }

    // todo: what is this & why not used, check moz search
//    @VisibleForTesting(otherwise = PRIVATE)
//    fun setStrictETP() {
//        preferences.edit().putBoolean(
//            appContext.getPreferenceKey(R.string.pref_key_tracking_protection_strict_default),
//            true,
//        ).apply()
//        preferences.edit().putBoolean(
//            appContext.getPreferenceKey(R.string.pref_key_tracking_protection_standard_option),
//            false,
//        ).apply()
//        appContext.components.let {
//            val policy = it.core.trackingProtectionPolicyFactory.createTrackingProtectionPolicy()
//            it.useCases.settingsUseCases.updateTrackingProtection.invoke(policy)
//            it.useCases.sessionUseCases.reload.invoke()
//        }
//    }
    val enabledTotalCookieProtection: Boolean
        get() = mr2022Sections[Mr2022Section.TCP_FEATURE] == true
    var blockCookiesInCustomTrackingProtection: Boolean
        get() = getPref(default = true) { it?.customTrackingProtection?.blockCustomCookies != InfernoSettings.CustomTrackingProtection.CookiePolicy.NONE }
        set(value) {
            // todo: does this work?
            if (!value) setPref {
                it.setCustomTrackingProtection(
                    it.customTrackingProtection.toBuilder()
                        .setBlockCustomCookies(InfernoSettings.CustomTrackingProtection.CookiePolicy.NONE)
                        .build()
                )
            }
        }
    var blockCookiesSelectionInCustomTrackingProtection: InfernoSettings.CustomTrackingProtection.CookiePolicy
        get() = getPref(
            default = when (enabledTotalCookieProtection) {
                true -> InfernoSettings.CustomTrackingProtection.CookiePolicy.ALL_COOKIES
                false -> InfernoSettings.CustomTrackingProtection.CookiePolicy.CROSS_SITE_AND_SOCIAL_MEDIA_TRACKERS
            }
        ) { it?.customTrackingProtection?.blockCustomCookies }
        set(value) {
            setPref {
                it.setCustomTrackingProtection(
                    it.customTrackingProtection.toBuilder().setBlockCustomCookies(value)
                )
            }
        }
    val blockTrackingContentInCustomTrackingProtection: Boolean
        get() {
            return blockTrackingContentInCustomTrackingProtectionInNormalTabs || blockTrackingContentInCustomTrackingProtectionInPrivateTabs
        }
    var blockTrackingContentInCustomTrackingProtectionInNormalTabs: Boolean
        get() = getPref(default = true) { it?.customTrackingProtection?.trackingContentBlockedInNormalMode }
        set(value) {
            setPref {
                it.setCustomTrackingProtection(
                    it.customTrackingProtection.toBuilder()
                        .setTrackingContentBlockedInNormalMode(value)
                )
            }
        }
    var blockTrackingContentInCustomTrackingProtectionInPrivateTabs: Boolean
        get() = getPref(default = true) { it?.customTrackingProtection?.trackingContentBlockedInPrivateMode }
        set(value) {
            setPref {
                it.setCustomTrackingProtection(
                    it.customTrackingProtection.toBuilder()
                        .setTrackingContentBlockedInPrivateMode(value)
                )
            }
        }
    var blockCryptominersInCustomTrackingProtection: Boolean
        get() = getPref(default = true) { it?.customTrackingProtection?.blockCryptominers }
        set(value) {
            setPref {
                it.setCustomTrackingProtection(
                    it.customTrackingProtection.toBuilder().setBlockCryptominers(value)
                )
            }
        }
    var blockFingerprintersInCustomTrackingProtection: Boolean
        get() = getPref(default = true) { it?.customTrackingProtection?.blockKnownFingerprinters }
        set(value) {
            setPref {
                it.setCustomTrackingProtection(
                    it.customTrackingProtection.toBuilder().setBlockKnownFingerprinters(value)
                )
            }
        }
    var blockRedirectTrackersInCustomTrackingProtection: Boolean
        get() = getPref(default = true) { it?.customTrackingProtection?.blockRedirectTrackers }
        set(value) {
            setPref {
                it.setCustomTrackingProtection(
                    it.customTrackingProtection.toBuilder().setBlockRedirectTrackers(value)
                )
            }
        }
    val blockSuspectedFingerprinters: Boolean
        get() {
            return blockSuspectedFingerprintersInCustomTrackingProtectionInNormalTabs || blockSuspectedFingerprintersInCustomTrackingProtectionInPrivateTabs
        }
    var blockSuspectedFingerprintersInCustomTrackingProtectionInNormalTabs: Boolean
        get() = getPref(default = true) { it?.customTrackingProtection?.blockSuspectedFingerPrintersInNormalMode }
        set(value) {
            setPref {
                it.setCustomTrackingProtection(
                    it.customTrackingProtection.toBuilder()
                        .setBlockSuspectedFingerPrintersInNormalMode(value)
                )
            }
        }
    var blockSuspectedFingerprintersInCustomTrackingProtectionInPrivateTabs: Boolean
        get() = getPref(default = true) { it?.customTrackingProtection?.blockSuspectedFingerPrintersInPrivateMode }
        set(value) {
            setPref {
                it.setCustomTrackingProtection(
                    it.customTrackingProtection.toBuilder()
                        .setBlockSuspectedFingerPrintersInPrivateMode(value)
                )
            }
        }


    /*** https-only mode settings ***/


    var httpsOnlyMode: InfernoSettings.HttpsOnlyMode
        get() = getPref(default = InfernoSettings.HttpsOnlyMode.HTTPS_ONLY_DISABLED) { it?.httpsOnlyMode }
        set(value) {
            setPref { it.setHttpsOnlyMode(value) }
        }


    // todo: left off here, reorganizing settings

    // not used
//    var lastReviewPromptTimeInMillis by longPreference(
//        appContext.getPreferenceKey(R.string.pref_key_last_review_prompt_shown_time),
//        default = 0L,
//    )

    // todo: check usages to see if need to remove
    var lastCfrShownTimeInMillis by longPreference(
        appContext.getPreferenceKey(R.string.pref_key_last_cfr_shown_time),
        default = 0L,
    )

    val canShowCfr: Boolean
        get() = (System.currentTimeMillis() - lastCfrShownTimeInMillis) > THREE_DAYS_MS


    // todo: check usages to see if need to remove
    var adjustCampaignId by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_adjust_campaign),
        default = "",
    )

    // todo: check usages to see if need to remove
    var adjustNetwork by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_adjust_network),
        default = "",
    )

    // todo: check usages to see if need to remove
    var adjustAdGroup by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_adjust_adgroup),
        default = "",
    )

    // todo: check usages to see if need to remove
    var adjustCreative by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_adjust_creative),
        default = "",
    )

    // todo: check usages to see if need to remove
    var nimbusExperimentsFetched by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_nimbus_experiments_fetched),
        default = false,
    )

    // not used
//    var utmParamsKnown by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_utm_params_known),
//        default = false,
//    )

    // todo: check usages to see if need to remove
    var utmSource by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_utm_source),
        default = "",
    )

    // todo: check usages to see if need to remove
    var utmMedium by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_utm_medium),
        default = "",
    )

    // todo: check usages to see if need to remove
    var utmCampaign by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_utm_campaign),
        default = "",
    )

    // todo: check usages to see if need to remove
    var utmTerm by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_utm_term),
        default = "",
    )

    // todo: check usages to see if need to remove
    var utmContent by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_utm_content),
        default = "",
    )

    // todo: check usages to see if need to remove
    var contileContextId by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_contile_context_id),
        default = "",
    )

    // todo: check usages to see if need to remove
    var currentWallpaperName by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_current_wallpaper),
        default = Wallpaper.Default.name,
    )

    /**
     * A cache of the text color to use on text overlaying the current wallpaper.
     * The value will be `0` if the color is unavailable.
     */
    // todo: check usages to see if need to remove
    var currentWallpaperTextColor by longPreference(
        appContext.getPreferenceKey(R.string.pref_key_current_wallpaper_text_color),
        default = 0,
    )

    /**
     * A cache of the background color to use on cards overlaying the current wallpaper when the user's
     * theme is set to Light.
     */
    // todo: check usages to see if need to remove
    var currentWallpaperCardColorLight by longPreference(
        appContext.getPreferenceKey(R.string.pref_key_current_wallpaper_card_color_light),
        default = 0,
    )

    /**
     * A cache of the background color to use on cards overlaying the current wallpaper when the user's
     * theme is set to Dark.
     */
    // todo: check usages to see if need to remove
    var currentWallpaperCardColorDark by longPreference(
        appContext.getPreferenceKey(R.string.pref_key_current_wallpaper_card_color_dark),
        default = 0,
    )

    /**
     * Indicates if the current legacy wallpaper should be migrated.
     */
    // todo: check usages to see if need to remove
    var shouldMigrateLegacyWallpaper by booleanPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_should_migrate_wallpaper),
        default = true,
    )

    /**
     * Indicates if the current legacy wallpaper card colors should be migrated.
     */
    // todo: check usages to see if need to remove
    var shouldMigrateLegacyWallpaperCardColors by booleanPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_should_migrate_wallpaper_card_colors),
        default = true,
    )

    /**
     * Indicates if the wallpaper onboarding dialog should be shown.
     */
    // todo: check usages to see if need to remove
    var showWallpaperOnboarding by lazyFeatureFlagPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_wallpapers_onboarding),
        featureFlag = true,
        default = { mr2022Sections[Mr2022Section.WALLPAPERS_SELECTION_TOOL] == true },
    )

    // todo: check usages to see if need to remove
    var shouldReturnToBrowser by booleanPreference(
        appContext.getString(R.string.pref_key_return_to_browser),
        false,
    )

    // todo: check usages to see if need to remove
    var openInAppOpened by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_open_in_app_opened),
        default = false,
    )

    // todo: check usages to see if need to remove
    var installPwaOpened by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_install_pwa_opened),
        default = false,
    )

    // todo: add
    var showCollectionsPlaceholderOnHome by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_show_collections_placeholder_home),
        default = true,
    )

    // not used
//    val isCrashReportingEnabled: Boolean
//        get() = isCrashReportEnabledInBuild &&
//            preferences.getBoolean(
//                appContext.getPreferenceKey(R.string.pref_key_crash_reporter),
//                true,
//            )

    // todo: check usages to see if need to remove
    var isTelemetryEnabled by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_telemetry),
        default = true,
    )

    // not used
//    var isMarketingTelemetryEnabled by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_marketing_telemetry),
//        default = false,
//    )

    // todo: check usages to see if need to remove
    var isExperimentationEnabled by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_experimentation_v2),
        default = isTelemetryEnabled,
    )

    // todo: check usages to see if need to remove
    var isOverrideTPPopupsForPerformanceTest = false

    // We do not use `booleanPreference` because we only want the "read" part of this setting to be
    // controlled by a shared pref (if any). In the secret settings, there is a toggle switch to enable
    // and disable this pref. Other than that, the `SecretDebugMenuTrigger` should be able to change
    // this setting for the duration of the session only, i.e. `SecretDebugMenuTrigger` should never
    // be able to (indirectly) change the value of the shared pref.
    // todo: check usages to see if need to remove
    var showSecretDebugMenuThisSession: Boolean = false
        get() = field || preferences.getBoolean(
            appContext.getPreferenceKey(R.string.pref_key_persistent_debug_menu),
            false,
        )
    val shouldShowSecurityPinWarningSync: Boolean
        get() = loginsSecureWarningSyncCount.underMaxCount()

    val shouldShowSecurityPinWarning: Boolean
        get() = secureWarningCount.underMaxCount()

    // not used
//    var shouldShowPrivacyPopWindow by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_privacy_pop_window),
//        default = true,
//    )

    // todo: check usages to see if need to remove
    var shouldUseLightTheme by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_light_theme),
        default = false,
    )

    // todo: check usages to see if need to remove
    var allowThirdPartyRootCerts by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_allow_third_party_root_certs),
        default = false,
    )

    // todo: check usages to see if need to remove
    var nimbusUsePreview by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_nimbus_use_preview),
        default = false,
    )

    // todo: check usages to see if need to remove
    var isFirstNimbusRun: Boolean by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_is_first_run),
        default = true,
    )

    // todo: check usages to see if need to remove
    var isFirstSplashScreenShown: Boolean by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_is_first_splash_screen_shown),
        default = false,
    )

    // todo: check usages to see if need to remove
    var nimbusLastFetchTime: Long by longPreference(
        appContext.getPreferenceKey(R.string.pref_key_nimbus_last_fetch),
        default = 0L,
    )


    /**
     * Indicates if the user has completed successfully first translation.
     */
    // todo: check usages to see if need to remove
    var showFirstTimeTranslation: Boolean by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_show_first_time_translation),
        default = true,
    )

    /**
     * Indicates if the user wants translations to automatically be offered as a popup of the dialog.
     */
    // todo: check usages to see if need to remove
    var offerTranslation: Boolean by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_translations_offer),
        default = true,
    )

    @VisibleForTesting
    internal fun timeNowInMillis(): Long = System.currentTimeMillis()

    fun getTabTimeout(): Long = when {
        closeTabsAfterOneDay -> ONE_DAY_MS
        closeTabsAfterOneWeek -> ONE_WEEK_MS
        closeTabsAfterOneMonth -> ONE_MONTH_MS
        else -> Long.MAX_VALUE
    }

    enum class TabView {
        GRID, LIST
    }

    fun getTabViewPingString() =
        if (tabTrayStyle == InfernoSettings.TabTrayStyle.TAB_TRAY_GRID) TabView.GRID.name else TabView.LIST.name

    enum class TabTimout {
        ONE_DAY, ONE_WEEK, ONE_MONTH, MANUAL
    }

    fun getTabTimeoutPingString(): String = when {
        closeTabsAfterOneDay -> {
            TabTimout.ONE_DAY.name
        }

        closeTabsAfterOneWeek -> {
            TabTimout.ONE_WEEK.name
        }

        closeTabsAfterOneMonth -> {
            TabTimout.ONE_MONTH.name
        }

        else -> {
            TabTimout.MANUAL.name
        }
    }

    fun getTabTimeoutString(): String = when {
        closeTabsAfterOneDay -> {
            appContext.getString(R.string.close_tabs_after_one_day_summary)
        }

        closeTabsAfterOneWeek -> {
            appContext.getString(R.string.close_tabs_after_one_week_summary)
        }

        closeTabsAfterOneMonth -> {
            appContext.getString(R.string.close_tabs_after_one_month_summary)
        }

        else -> {
            appContext.getString(R.string.close_tabs_manually_summary)
        }
    }

    /**
     * Get the display string for the current open links in apps setting
     */
    fun getOpenLinksInAppsString(): String = when (openLinksInExternalApp) {
        appContext.getString(R.string.pref_key_open_links_in_apps_always) -> {
            if (lastKnownMode == BrowsingMode.Normal) {
                appContext.getString(R.string.preferences_open_links_in_apps_always)
            } else {
                appContext.getString(R.string.preferences_open_links_in_apps_ask)
            }
        }

        appContext.getString(R.string.pref_key_open_links_in_apps_ask) -> {
            appContext.getString(R.string.preferences_open_links_in_apps_ask)
        }

        else -> {
            appContext.getString(R.string.preferences_open_links_in_apps_never)
        }
    }

    // todo: check usages to see if need to remove
    var shouldUseDarkTheme by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_dark_theme),
        default = false,
    )

    // todo: check usages to see if need to remove
    var shouldFollowDeviceTheme by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_follow_device_theme),
        default = false,
    )

    // todo: check usages to see if need to remove
    var shouldUseCookieBannerPrivateMode by lazyFeatureFlagPreference(
        appContext.getPreferenceKey(R.string.pref_key_cookie_banner_private_mode),
        featureFlag = true,
        default = { shouldUseCookieBannerPrivateModeDefaultValue },
    )

    // todo: check usages to see if need to remove
    val shouldUseCookieBannerPrivateModeDefaultValue: Boolean
        get() = cookieBannersSection[CookieBannersSection.FEATURE_SETTING_VALUE_PBM] == 1

    // todo: check usages to see if need to remove
    val shouldUseCookieBanner: Boolean
        get() = cookieBannersSection[CookieBannersSection.FEATURE_SETTING_VALUE] == 1

    // todo: check usages to see if need to remove
    val shouldShowCookieBannerUI: Boolean
        get() = cookieBannersSection[CookieBannersSection.FEATURE_UI] == 1

    // todo: check usages to see if need to remove
    val shouldEnableCookieBannerDetectOnly: Boolean
        get() = cookieBannersSection[CookieBannersSection.FEATURE_SETTING_DETECT_ONLY] == 1

    // todo: check usages to see if need to remove
    val shouldEnableCookieBannerGlobalRules: Boolean
        get() = cookieBannersSection[CookieBannersSection.FEATURE_SETTING_GLOBAL_RULES] == 1

    // todo: check usages to see if need to remove
    val shouldEnableCookieBannerGlobalRulesSubFrame: Boolean
        get() = cookieBannersSection[CookieBannersSection.FEATURE_SETTING_GLOBAL_RULES_SUB_FRAMES] == 1

    // todo: check usages to see if need to remove
    val shouldEnableQueryParameterStripping: Boolean
        get() = queryParameterStrippingSection[QUERY_PARAMETER_STRIPPING] == "1"

    // todo: check usages to see if need to remove
    val shouldEnableQueryParameterStrippingPrivateBrowsing: Boolean
        get() = queryParameterStrippingSection[QUERY_PARAMETER_STRIPPING_PMB] == "1"

    // todo: check usages to see if need to remove
    val queryParameterStrippingAllowList: String
        get() = queryParameterStrippingSection[QUERY_PARAMETER_STRIPPING_ALLOW_LIST].orEmpty()

    // todo: check usages to see if need to remove
    val queryParameterStrippingStripList: String
        get() = queryParameterStrippingSection[QUERY_PARAMETER_STRIPPING_STRIP_LIST].orEmpty()

    // todo: not used
//    /**
//     * Declared as a function for performance purposes. This could be declared as a variable using
//     * booleanPreference like other members of this class. However, doing so will make it so it will
//     * be initialized once Settings.kt is first called, which in turn will call `isDefaultBrowserBlocking()`.
//     * This will lead to a performance regression since that function can be expensive to call.
//     */
//    fun checkIfFenixIsDefaultBrowserOnAppResume(): Boolean {
//        val prefKey = appContext.getPreferenceKey(R.string.pref_key_default_browser)
//        val isDefaultBrowserNow = isDefaultBrowserBlocking()
//        val wasDefaultBrowserOnLastResume =
//            this.preferences.getBoolean(prefKey, isDefaultBrowserNow)
//        this.preferences.edit().putBoolean(prefKey, isDefaultBrowserNow).apply()
//        return isDefaultBrowserNow && !wasDefaultBrowserOnLastResume
//    }

    /**
     * This function is "blocking" since calling this can take approx. 30-40ms (timing taken on a
     * G5+).
     */
    fun isDefaultBrowserBlocking(): Boolean {
        val browsers = BrowsersCache.all(appContext)
        return browsers.isDefaultBrowser
    }

    // todo: check usages to see if need to remove
    var reEngagementNotificationShown by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_re_engagement_notification_shown),
        default = false,
    )

    /**
     * Check if we should set the re-engagement notification.
     */
    // todo: check usages to see if need to remove
    fun shouldSetReEngagementNotification(): Boolean {
        return numberOfAppLaunches <= 1 && !reEngagementNotificationShown
    }

    /**
     * Check if we should show the re-engagement notification.
     */
    // todo: check usages to see if need to remove
    fun shouldShowReEngagementNotification(): Boolean {
        return !reEngagementNotificationShown && !isDefaultBrowserBlocking()
    }

    /**
     * Indicates if the re-engagement notification feature is enabled
     */
    // todo: check usages to see if need to remove
    var reEngagementNotificationEnabled by lazyFeatureFlagPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_re_engagement_notification_enabled),
        default = { FxNimbus.features.reEngagementNotification.value().enabled },
        featureFlag = true,
    )

    /**
     * Indicates if the re-engagement notification feature is enabled
     */
    // todo: check usages to see if need to remove
    val reEngagementNotificationType: Int
        get() = FxNimbus.features.reEngagementNotification.value().type

    // todo: check usages to see if need to remove
    val shouldUseAutoBatteryTheme by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_auto_battery_theme),
        default = false,
    )


    // todo: check usages to see if need to remove
    val useProductionRemoteSettingsServer by booleanPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_remote_server_prod),
        default = true,
    )

    /**
     * Indicates if the total cookie protection CRF should be shown.
     */
    // todo: check usages to see if need to remove
    var shouldShowEraseActionCFR by lazyFeatureFlagPreference(
        appContext.getPreferenceKey(R.string.pref_key_should_show_erase_action_popup),
        featureFlag = true,
        default = { feltPrivateBrowsingEnabled },
    )

    /**
     * Indicates if the cookie banners CRF should be shown.
     */
    // todo: check usages to see if need to remove
    var shouldShowCookieBannersCFR by lazyFeatureFlagPreference(
        appContext.getPreferenceKey(R.string.pref_key_should_show_cookie_banners_action_popup),
        featureFlag = true,
        default = { shouldShowCookieBannerUI },
    )

    // todo: check usages to see if need to remove
    var shouldShowTabSwipeCFR by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_toolbar_tab_swipe_cfr),
        default = false,
    )

    // todo: check usages to see if need to remove
    var hasShownTabSwipeCFR by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_toolbar_has_shown_tab_swipe_cfr),
        default = false,
    )


    /**
     * Prefer to use a fixed top toolbar when:
     * - a talkback service is enabled or
     * - switch access is enabled.
     *
     * This is automatically inferred based on the current system status. Not a setting in our app.
     */
    // todo: fix based on toolbar vars
    val shouldUseFixedTopToolbar: Boolean
        get() {
            return touchExplorationIsEnabled || switchServiceIsEnabled
        }

    // todo: check usages to see if need to remove
    var lastKnownMode: BrowsingMode = BrowsingMode.Normal
        get() {
            val lastKnownModeWasPrivate = preferences.getBoolean(
                appContext.getPreferenceKey(R.string.pref_key_last_known_mode_private),
                false,
            )

            return if (lastKnownModeWasPrivate) {
                BrowsingMode.Private
            } else {
                BrowsingMode.Normal
            }
        }
        set(value) {
            val lastKnownModeWasPrivate = (value == BrowsingMode.Private)

            preferences.edit().putBoolean(
                appContext.getPreferenceKey(R.string.pref_key_last_known_mode_private),
                lastKnownModeWasPrivate,
            ).apply()

            field = value
        }

    val toolbarPosition: ToolbarPosition
        get() = if (appContext.isTabStripEnabled()) {
            ToolbarPosition.BOTTOM
        } else if (shouldUseBottomToolbar) {
            ToolbarPosition.BOTTOM
        } else {
            ToolbarPosition.TOP
        }

    /**
     * Check each active accessibility service to see if it can perform gestures, if any can,
     * then it is *likely* a switch service is enabled. We are assuming this to be the case based on #7486
     */
    // todo: check usages to see if need to remove
    val switchServiceIsEnabled: Boolean
        get() {
            val accessibilityManager =
                appContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager

            accessibilityManager?.getEnabledAccessibilityServiceList(0)?.let { activeServices ->
                for (service in activeServices) {
                    if (service.capabilities.and(CAPABILITY_CAN_PERFORM_GESTURES) == 1) {
                        return true
                    }
                }
            }

            return false
        }

    // todo: check usages to see if need to remove
    val touchExplorationIsEnabled: Boolean
        get() {
            val accessibilityManager =
                appContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            return accessibilityManager?.isTouchExplorationEnabled ?: false
        }

    // todo: check usages to see if need to remove
    val accessibilityServicesEnabled: Boolean
        get() {
            return touchExplorationIsEnabled || switchServiceIsEnabled
        }

    fun getDeleteDataOnQuit(type: DeleteBrowsingDataOnQuitType): Boolean = when (type) {
        DeleteBrowsingDataOnQuitType.TABS -> deleteOpenTabs
        DeleteBrowsingDataOnQuitType.HISTORY -> deleteBrowsingHistory
        DeleteBrowsingDataOnQuitType.COOKIES -> deleteCookies
        DeleteBrowsingDataOnQuitType.CACHE -> deleteCache
        DeleteBrowsingDataOnQuitType.PERMISSIONS -> deleteSitePermissions
        DeleteBrowsingDataOnQuitType.DOWNLOADS -> deleteDownloads
    }
//        preferences.getBoolean(type.getPreferenceKey(appContext), false)

//    fun setDeleteDataOnQuit(type: DeleteBrowsingDataOnQuitType, value: Boolean) {
//        preferences.edit().putBoolean(type.getPreferenceKey(appContext), value).apply()
//    }

    fun shouldDeleteAnyDataOnQuit() =
        DeleteBrowsingDataOnQuitType.entries.any { getDeleteDataOnQuit(it) }

    // not used
//    val passwordsEncryptionKeyGenerated by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_encryption_key_generated),
//        false,
//    )

    // not used
//    fun recordPasswordsEncryptionKeyGenerated() = preferences.edit().putBoolean(
//        appContext.getPreferenceKey(R.string.pref_key_encryption_key_generated),
//        true,
//    ).apply()

    @VisibleForTesting(otherwise = PRIVATE)
    // todo: check usages to see if need to remove
    internal val loginsSecureWarningSyncCount = counterPreference(
        appContext.getPreferenceKey(R.string.pref_key_logins_secure_warning_sync),
        maxCount = 1,
    )

    @VisibleForTesting(otherwise = PRIVATE)
    // todo: check usages to see if need to remove
    internal val secureWarningCount = counterPreference(
        appContext.getPreferenceKey(R.string.pref_key_secure_warning),
        maxCount = 1,
    )

    // todo: check usages to see if need to remove
    fun incrementSecureWarningCount() = secureWarningCount.increment()

    // todo: check usages to see if need to remove
    fun incrementShowLoginsSecureWarningSyncCount() = loginsSecureWarningSyncCount.increment()


    // todo: check usages to see if need to remove
    var showSearchSuggestionsInPrivateOnboardingFinished by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_show_search_suggestions_in_private_onboarding),
        default = false,
    )

    // todo: check usages to see if need to remove
    fun incrementVisitedInstallableCount() = pwaInstallableVisitCount.increment()

    @VisibleForTesting(otherwise = PRIVATE)
    internal val pwaInstallableVisitCount = counterPreference(
        appContext.getPreferenceKey(R.string.pref_key_install_pwa_visits),
        maxCount = 3,
    )

    // todo: check usages to see if need to remove
    private val userNeedsToVisitInstallableSites: Boolean
        get() = pwaInstallableVisitCount.underMaxCount()

    // todo: check usages to see if need to remove
    val shouldShowPwaCfr: Boolean
        get() {
            if (!canShowCfr) return false
            // We only want to show this on the 3rd time a user visits a site
            if (userNeedsToVisitInstallableSites) return false

            // ShortcutManager::pinnedShortcuts is only available on Oreo+
            if (!userKnowsAboutPwas && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = appContext.getSystemService(ShortcutManager::class.java)
                val alreadyHavePwaInstalled = manager != null && manager.pinnedShortcuts.size > 0

                // Users know about PWAs onboarding if they already have PWAs installed.
                userKnowsAboutPwas = alreadyHavePwaInstalled
            }
            // Show dialog only if user does not know abut PWAs
            return !userKnowsAboutPwas
        }

    // todo: check usages to see if need to remove
    var userKnowsAboutPwas by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_user_knows_about_pwa),
        default = false,
    )

    // todo: check usages to see if need to remove
    var shouldShowOpenInAppBanner by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_should_show_open_in_app_banner),
        default = true,
    )

//    val shouldShowOpenInAppCfr: Boolean
//        get() = canShowCfr && shouldShowOpenInAppBanner

    // todo: check usages to see if need to remove
    var shouldShowAutoCloseTabsBanner by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_should_show_auto_close_tabs_banner),
        default = true,
    )

    // todo: check usages to see if need to remove
    var shouldShowInactiveTabsOnboardingPopup by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_should_show_inactive_tabs_popup),
        default = true,
    )

    /**
     * Indicates if the auto-close dialog for inactive tabs has been dismissed before.
     */
    // todo: check usages to see if need to remove
    var hasInactiveTabsAutoCloseDialogBeenDismissed by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_has_inactive_tabs_auto_close_dialog_dismissed),
        default = false,
    )

    /**
     * Indicates if the auto-close dialog should be visible based on
     * if the user has dismissed it before [hasInactiveTabsAutoCloseDialogBeenDismissed],
     * if the minimum number of tabs has been accumulated [numbersOfTabs]
     * and if the auto-close setting is already set to [closeTabsAfterOneMonth].
     */
    // todo: check usages to see if need to remove
    fun shouldShowInactiveTabsAutoCloseDialog(numbersOfTabs: Int): Boolean {
        return !hasInactiveTabsAutoCloseDialogBeenDismissed && numbersOfTabs >= INACTIVE_TAB_MINIMUM_TO_SHOW_AUTO_CLOSE_DIALOG && !closeTabsAfterOneMonth
    }


    /**
     * Used in [SearchDialogFragment.kt], [SearchFragment.kt] (deprecated), and [PairFragment.kt]
     * to see if we need to check for camera permissions before using the QR code scanner.
     */
    // todo: check usages to see if need to remove
    var shouldShowCameraPermissionPrompt by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_camera_permissions_needed),
        default = true,
    )

    /**
     * Sets the state of permissions that have been checked, where [false] denotes already checked
     * and [true] denotes needing to check. See [shouldShowCameraPermissionPrompt].
     */
    // todo: check usages to see if need to remove
    var setCameraPermissionNeededState by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_camera_permissions_needed),
        default = true,
    )

    /**
     * Used in [SearchWidgetProvider] to update when the search widget
     * exists on home screen or if it has been removed completely.
     */
    fun setSearchWidgetInstalled(installed: Boolean) {
        shouldShowSearchWidget = installed
//        val key = appContext.getPreferenceKey(R.string.pref_key_search_widget_installed_2)
//        preferences.edit().putBoolean(key, installed).apply()
    }


    // todo: check usages to see if need to remove
    fun incrementNumTimesPrivateModeOpened() = numTimesPrivateModeOpened.increment()

    // todo: check usages to see if need to remove
    var showedPrivateModeContextualFeatureRecommender by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_showed_private_mode_cfr),
        default = false,
    )

    // todo: check usages to see if need to remove
    private val numTimesPrivateModeOpened = counterPreference(
        appContext.getPreferenceKey(R.string.pref_key_private_mode_opened),
    )

    // todo: check usages to see if need to remove
    val shouldShowPrivateModeCfr: Boolean
        get() {
            if (!canShowCfr) return false
//            val focusInstalled = MozillaProductDetector
//                .getInstalledMozillaProducts(appContext as Application)
//                .contains(MozillaProductDetector.MozillaProducts.FOCUS.productName)

//            val showCondition = if (focusInstalled) {
//                numTimesPrivateModeOpened.value >= CFR_COUNT_CONDITION_FOCUS_INSTALLED
//            } else {
//                numTimesPrivateModeOpened.value >= CFR_COUNT_CONDITION_FOCUS_NOT_INSTALLED
//            }
//
//            if (showCondition && !showedPrivateModeContextualFeatureRecommender) {
//                return true
//            }

            return false
        }

    /**
     * Check to see if we should open the link in an external app
     */
    fun shouldOpenLinksInApp(isCustomTab: Boolean = false): Boolean {
        return when (openLinksInExternalApp) {
            appContext.getString(R.string.pref_key_open_links_in_apps_always) -> true
            appContext.getString(R.string.pref_key_open_links_in_apps_ask) -> true/* Some applications will not work if custom tab never open links in apps, return true if it's custom tab */
            appContext.getString(R.string.pref_key_open_links_in_apps_never) -> isCustomTab
            else -> false
        }
    }

    /**
     * Check to see if we need to prompt the user if the link can be opened in an external app
     */
    // todo: check usages to see if need to remove
    fun shouldPromptOpenLinksInApp(): Boolean {
        return when (openLinksInExternalApp) {
            appContext.getString(R.string.pref_key_open_links_in_apps_always) -> false
            appContext.getString(R.string.pref_key_open_links_in_apps_ask) -> true
            appContext.getString(R.string.pref_key_open_links_in_apps_never) -> true
            else -> true
        }
    }

    // todo: check usages to see if need to remove
    var openLinksInExternalApp by stringPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_open_links_in_apps),
        default = appContext.getString(R.string.pref_key_open_links_in_apps_ask),
    )

    // todo: check usages to see if need to remove
    var allowDomesticChinaFxaServer by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_allow_domestic_china_fxa_server),
        default = true,
    )

    // todo: check usages to see if need to remove
    var overrideFxAServer by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_override_fxa_server),
        default = "",
    )

    // todo: check usages to see if need to remove
    var useReactFxAServer by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_use_react_fxa),
        default = false,
    )

    // todo: check usages to see if need to remove
    var overrideSyncTokenServer by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_override_sync_tokenserver),
        default = "",
    )

    // todo: check usages to see if need to remove
    var overridePushServer by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_override_push_server),
        default = "",
    )

    // todo: check usages to see if need to remove
    var overrideAmoUser by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_override_amo_user),
        default = "",
    )

    // todo: check usages to see if need to remove
    var overrideAmoCollection by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_override_amo_collection),
        default = "",
    )

    // todo: check usages to see if need to remove
    var enableGeckoLogs by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_enable_gecko_logs),
        default = Config.channel.isDebug,
    )

    // todo: check usages to see if need to remove
    fun amoCollectionOverrideConfigured(): Boolean {
        return overrideAmoUser.isNotEmpty() || overrideAmoCollection.isNotEmpty()
    }

//    var topSitesSize by intPreference(
//        appContext.getPreferenceKey(R.string.pref_key_top_sites_size),
//        default = 0,
//    )

    // todo: check usages to see if need to remove
    val topSitesMaxLimit by intPreference(
        appContext.getPreferenceKey(R.string.pref_key_top_sites_max_limit),
        default = TOP_SITES_MAX_COUNT,
    )

//    var openTabsCount by intPreference(
//        appContext.getPreferenceKey(R.string.pref_key_open_tabs_count),
//        0,
//    )

    // todo: check usages to see if need to remove
    var openPrivateTabsCount by intPreference(
        appContext.getPreferenceKey(R.string.pref_key_open_private_tabs_count),
        0,
    )

    // todo: check usages to see if need to remove
    var mobileBookmarksSize by intPreference(
        appContext.getPreferenceKey(R.string.pref_key_mobile_bookmarks_size),
        0,
    )

    // todo: check usages to see if need to remove
    var desktopBookmarksSize by intPreference(
        appContext.getPreferenceKey(R.string.pref_key_desktop_bookmarks_size),
        0,
    )

    /**
     *  URLs from the user's history that contain this search param will be hidden.
     *  The value is a string with one of the following forms:
     * - "" (empty) - Disable this feature
     * - "key" - Search param named "key" with any or no value
     * - "key=" - Search param named "key" with no value
     * - "key=value" - Search param named "key" with value "value"
     */
    // todo: check usages to see if need to remove
    val frecencyFilterQuery by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_frecency_filter_query),
        default = "mfadid=adm", // Parameter provided by adM
    )

    // todo: check usages to see if need to remove
    private var savedLoginsSortingStrategyString by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_saved_logins_sorting_strategy),
        default = SavedLoginsSortingStrategyMenu.Item.AlphabeticallySort.strategyString,
    )

    // todo: check usages to see if need to remove
    val savedLoginsMenuHighlightedItem: SavedLoginsSortingStrategyMenu.Item
        get() = SavedLoginsSortingStrategyMenu.Item.fromString(savedLoginsSortingStrategyString)

    // todo: check usages to see if need to remove
    var savedLoginsSortingStrategy: SortingStrategy
        get() {
            return when (savedLoginsMenuHighlightedItem) {
                SavedLoginsSortingStrategyMenu.Item.AlphabeticallySort -> SortingStrategy.Alphabetically
                SavedLoginsSortingStrategyMenu.Item.LastUsedSort -> SortingStrategy.LastUsed
            }
        }
        set(value) {
            savedLoginsSortingStrategyString = when (value) {
                is SortingStrategy.Alphabetically -> SavedLoginsSortingStrategyMenu.Item.AlphabeticallySort.strategyString

                is SortingStrategy.LastUsed -> SavedLoginsSortingStrategyMenu.Item.LastUsedSort.strategyString
            }
        }

    // todo: check usages to see if need to remove
    var addressFeature by featureFlagPreference(
        appContext.getPreferenceKey(R.string.pref_key_show_address_feature),
        default = true,
        featureFlag = isAddressFeatureEnabled(appContext),
    )

    /**
     * Show the Addresses autofill feature.
     */
    // todo: check usages to see if need to remove
    private fun isAddressFeatureEnabled(context: Context): Boolean {
        val releaseEnabledLanguages = listOf(
            "en-US",
            "en-CA",
            "fr-CA",
        )
        val currentlyEnabledLanguages = if (Config.channel.isNightlyOrDebug) {
            releaseEnabledLanguages + SharedPrefsAddressesDebugLocalesRepository(context).getAllEnabledLocales()
                .map { it.langTag }
        } else {
            releaseEnabledLanguages
        }

        val userLangTag = LocaleManager.getCurrentLocale(context)?.toLanguageTag()
            ?: LocaleManager.getSystemDefault().toLanguageTag()
        return currentlyEnabledLanguages.contains(userLangTag)
    }

    // todo: check usages to see if need to remove
    private val mr2022Sections: Map<Mr2022Section, Boolean>
        get() = FxNimbus.features.mr2022.value().sectionsEnabled

    // todo: check usages to see if need to remove
    private val cookieBannersSection: Map<CookieBannersSection, Int>
        get() = FxNimbus.features.cookieBanners.value().sectionsEnabled

    // todo: check usages to see if need to remove
    private val queryParameterStrippingSection: Map<QueryParameterStrippingSection, String>
        get() = FxNimbus.features.queryParameterStripping.value().sectionsEnabled

    // todo: check usages to see if need to remove
    private val homescreenSections: Map<HomeScreenSection, Boolean>
        get() = FxNimbus.features.homescreen.value().sectionsEnabled

    /**
     * Indicates if sync onboarding CFR should be shown.
     */
    // todo: check usages to see if need to remove
    var showSyncCFR by lazyFeatureFlagPreference(
        appContext.getPreferenceKey(R.string.pref_key_should_show_sync_cfr),
        featureFlag = true,
        default = { mr2022Sections[Mr2022Section.SYNC_CFR] == true },
    )

    /**
     * Indicates if home onboarding dialog should be shown.
     */
    // todo: check usages to see if need to remove
    var showHomeOnboardingDialog by lazyFeatureFlagPreference(
        appContext.getPreferenceKey(R.string.pref_key_should_show_home_onboarding_dialog),
        featureFlag = true,
        default = { mr2022Sections[Mr2022Section.HOME_ONBOARDING_DIALOG_EXISTING_USERS] == true },
    )

    // todo: check usages to see if need to remove
    var signedInFxaAccount by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_fxa_signed_in),
        default = false,
    )


    /**
     * Indicates if the Pocket recommended stories homescreen section should be shown.
     */
    // todo: check usages to see if need to remove
    var showPocketRecommendationsFeature by lazyFeatureFlagPreference(
        appContext.getPreferenceKey(R.string.pref_key_pocket_homescreen_recommendations),
        featureFlag = FeatureFlags.isPocketRecommendationsFeatureEnabled(appContext),
        default = { homescreenSections[HomeScreenSection.POCKET] == true },
    )

    /**
     * Indicates if the Pocket recommendations homescreen section should also show sponsored stories.
     */
    // todo: check usages to see if need to remove
    val showPocketSponsoredStories by lazyFeatureFlagPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_pocket_sponsored_stories),
        default = { homescreenSections[HomeScreenSection.POCKET_SPONSORED_STORIES] == true },
        featureFlag = FeatureFlags.isPocketSponsoredStoriesFeatureEnabled(appContext),
    )

//    /**
//     * Indicates if Merino content recommendations should be shown.
//     */
//    var showContentRecommendations by booleanPreference(
//        key = appContext.getPreferenceKey(R.string.pref_key_pocket_content_recommendations),
//        default = FeatureFlags.merinoContentRecommendations,
//    )

    /**
     *  Whether or not to display the Pocket sponsored stories parameter secret settings.
     */
    // todo: check usages to see if need to remove
    var useCustomConfigurationForSponsoredStories by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_custom_sponsored_stories_parameters_enabled),
        default = false,
    )

    /**
     * Site parameter used to set the spoc content.
     */
    // todo: check usages to see if need to remove
    var pocketSponsoredStoriesSiteId by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_custom_sponsored_stories_site_id),
        default = "",
    )

    /**
     * Country parameter used to set the spoc content.
     */
    // todo: check usages to see if need to remove
    var pocketSponsoredStoriesCountry by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_custom_sponsored_stories_country),
        default = "",
    )

    /**
     * City parameter used to set the spoc content.
     */
    // todo: check usages to see if need to remove
    var pocketSponsoredStoriesCity by stringPreference(
        appContext.getPreferenceKey(R.string.pref_key_custom_sponsored_stories_city),
        default = "",
    )

    /**
     * Indicates if the Contile functionality should be visible.
     */
    // todo: check usages to see if need to remove
    var showContileFeature by booleanPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_enable_contile),
        default = true,
    )

    /**
     * Indicates if the Unified Search feature should be visible.
     */
    // todo: check usages to see if need to remove
    val showUnifiedSearchFeature = true

    /**
     * Blocklist used to filter items from the home screen that have previously been removed.
     */
    // todo: check usages to see if need to remove
    var homescreenBlocklist by stringSetPreference(
        appContext.getPreferenceKey(R.string.pref_key_home_blocklist),
        default = setOf(),
    )

    // not used, onboarding
//    /**
//     * Returns whether onboarding should be shown to the user.
//     *
//     * @param hasUserBeenOnboarded Boolean to indicate whether the user has been onboarded.
//     * @param isLauncherIntent Boolean to indicate whether the app was launched on tapping on the
//     * app icon.
//     */
//    fun shouldShowOnboarding(hasUserBeenOnboarded: Boolean, isLauncherIntent: Boolean): Boolean {
//        return if (!hasUserBeenOnboarded && isLauncherIntent) {
//            FxNimbus.features.junoOnboarding.recordExposure()
//            true
//        } else {
//            false
//        }
//    }

    // todo: check usages to see if need to remove
    val feltPrivateBrowsingEnabled by lazyFeatureFlagPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_should_enable_felt_privacy),
        featureFlag = true,
        default = {
            FxNimbus.features.privateBrowsing.value().feltPrivacyEnabled
        },
    )

    /**
     * Indicates if the review quality check feature is enabled by the user.
     */
    // todo: check usages to see if need to remove
    var isReviewQualityCheckEnabled by booleanPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_is_review_quality_check_enabled),
        default = false,
    )

    /**
     * Indicates if the review quality check product recommendations option is enabled by the user.
     */
    // todo: check usages to see if need to remove
    var isReviewQualityCheckProductRecommendationsEnabled by booleanPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_is_review_quality_check_product_recommendations_enabled),
        default = false,
    )

    /**
     * Indicates if the navigation bar CFR should be displayed to the user.
     */
    // todo: check usages to see if need to remove
    var shouldShowNavigationBarCFR by booleanPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_should_navbar_cfr),
        default = true,
    )

    /**
     * Indicates Navigation Bar's Navigation buttons CFR should be displayed to the user.
     */
    // todo: check usages to see if need to remove
    var shouldShowNavigationButtonsCFR by booleanPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_toolbar_navigation_cfr),
        default = true,
    )

    /**
     * Indicates if the menu CFR should be displayed to the user.
     */
    // todo: check usages to see if need to remove
    var shouldShowMenuCFR by booleanPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_menu_cfr),
        default = true,
    )

//    /**
//     * Time in milliseconds since the user first opted in the review quality check feature.
//     */
//    var reviewQualityCheckOptInTimeInMillis by longPreference(
//        appContext.getPreferenceKey(R.string.pref_key_should_show_review_quality_opt_in_time),
//        default = 0L,
//    )

    /**
     * Get the current mode for how https-only is enabled.
     */
    fun getHttpsOnlyMode(): HttpsOnlyMode {
        return if (httpsOnlyMode == InfernoSettings.HttpsOnlyMode.HTTPS_ONLY_DISABLED) {
            HttpsOnlyMode.DISABLED
        } else if (httpsOnlyMode == InfernoSettings.HttpsOnlyMode.HTTPS_ONLY_ENABLED_PRIVATE_ONLY) {
            HttpsOnlyMode.ENABLED_PRIVATE_ONLY
        } else {
            HttpsOnlyMode.ENABLED
        }
    }

    /**
     * Get the current mode for cookie banner handling
     */
    fun getCookieBannerHandling(): CookieBannerHandlingMode {
        return when (shouldUseCookieBanner) {
            true -> CookieBannerHandlingMode.REJECT_ALL
            false -> {
                CookieBannerHandlingMode.DISABLED
            }
        }
    }

    /**
     * Get the current mode for cookie banner handling
     */
    fun getCookieBannerHandlingPrivateMode(): CookieBannerHandlingMode {
        return when (shouldUseCookieBannerPrivateMode) {
            true -> CookieBannerHandlingMode.REJECT_ALL
            false -> {
                CookieBannerHandlingMode.DISABLED
            }
        }
    }

    /**
     * Indicates if the Tabs Tray to Compose changes are enabled.
     */
    // todo: check usages to see if need to remove
    var enableTabsTrayToCompose by booleanPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_enable_tabs_tray_to_compose),
        default = true // FeatureFlags.composeTabsTray,
    )

    /**
     * Indicates if the Compose Top Sites are enabled.
     */
    // todo: check usages to see if need to remove
    var enableComposeTopSites by booleanPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_enable_compose_top_sites),
        default = true // FeatureFlags.composeTopSites,
    )

    /**
     * Indicates if the Compose Homepage is enabled.
     */
    // todo: check usages to see if need to remove
    var enableComposeHomepage by booleanPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_enable_compose_homepage),
        default = true // FeatureFlags.composeHomepage,
    )

    /**
     * Indicates if the menu redesign is enabled.
     */
    // todo: check usages to see if need to remove
    var enableMenuRedesign by lazyFeatureFlagPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_enable_menu_redesign),
        default = { FxNimbus.features.menuRedesign.value().enabled },
        featureFlag = true,
    )

    /**
     * Indicates if the Homepage as a New Tab is enabled.
     */
    // todo: check usages to see if need to remove
    var enableHomepageAsNewTab by booleanPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_enable_homepage_as_new_tab),
        default = true // FeatureFlags.homepageAsNewTab,
    )

    /**
     * Indicates if the Unified Trust Panel is enabled.
     */
    // todo: check usages to see if need to remove
    var enableUnifiedTrustPanel by booleanPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_enable_unified_trust_panel),
        default = true // FeatureFlags.unifiedTrustPanel,
    )

    // not used
//    /**
//     * Adjust Activated User sent
//     */
//    var growthUserActivatedSent by booleanPreference(
//        key = appContext.getPreferenceKey(R.string.pref_key_growth_user_activated_sent),
//        default = false,
//    )

    /**
     * Indicates if hidden engines were restored due to migration to unified search settings UI.
     * Should be removed once we expect the majority of the users to migrate.
     * Tracking: https://bugzilla.mozilla.org/show_bug.cgi?id=1850767
     */
    // todo: check usages to see if need to remove
    var hiddenEnginesRestored: Boolean by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_hidden_engines_restored),
        default = false,
    )

    /**
     * Indicates if Firefox Suggest is enabled.
     */
    // todo: check usages to see if need to remove
    var enableFxSuggest by lazyFeatureFlagPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_enable_fxsuggest),
        default = { FxNimbus.features.fxSuggest.value().enabled },
        featureFlag = true // FeatureFlags.fxSuggest,
    )

    /**
     * Indicates if boosting AMP/wiki suggestions is enabled.
     */
    // todo: check usages to see if need to remove
    val boostAmpWikiSuggestions: Boolean
        get() = FxNimbus.features.fxSuggest.value().boostAmpWiki

    /**
     * Indicates first time engaging with signup
     */
    // todo: check usages to see if need to remove
    var isFirstTimeEngagingWithSignup: Boolean by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_first_time_engage_with_signup),
        default = true,
    )

    /**
     * Indicates if the user has chosen to show sponsored search suggestions in the awesomebar.
     * The default value is computed lazily, and based on whether Firefox Suggest is enabled.
     */
    // todo: check usages to see if need to remove
    var showSponsoredSuggestions by lazyFeatureFlagPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_show_sponsored_suggestions),
        default = { enableFxSuggest },
        featureFlag = false // FeatureFlags.fxSuggest,
    )

    /**
     * Indicates if the user has chosen to show search suggestions for web content in the
     * awesomebar. The default value is computed lazily, and based on whether Firefox Suggest
     * is enabled.
     */
    // todo: check usages to see if need to remove
    var showNonSponsoredSuggestions by lazyFeatureFlagPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_show_nonsponsored_suggestions),
        default = { enableFxSuggest },
        featureFlag = true // FeatureFlags.fxSuggest,
    )

    /**
     * Indicates that the user does not want warned of a translations
     * model download while in data saver mode and using mobile data.
     */
    // todo: check usages to see if need to remove
    var ignoreTranslationsDataSaverWarning by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_ignore_translations_data_saver_warning),
        default = false,
    )

    /**
     * Indicates if the feature to close synced tabs is enabled.
     */
    // todo: check usages to see if need to remove
    val enableCloseSyncedTabs: Boolean
        get() = FxNimbus.features.remoteTabManagement.value().closeTabsEnabled

    /**
     * Returns the height of the bottom toolbar.
     *
     * The bottom toolbar can consist of:
     *  - a navigation bar.
     *  - a combination of a navigation and address bar.
     *  - a combination of a navigation and address bar & a microsurvey.
     *  - a combination of address bar & a microsurvey.
     *  - be absent.
     *
     *  @param context to be used for [shouldAddNavigationBar] function
     */
    // todo: eventually remove
    fun getBottomToolbarHeight(context: Context): Int {
        val isNavbarVisible = context.shouldAddNavigationBar()
        val isMicrosurveyEnabled = shouldShowMicrosurveyPrompt
        val isToolbarAtBottom = toolbarPosition == ToolbarPosition.BOTTOM

        val navbarHeight = appContext.resources.getDimensionPixelSize(R.dimen.browser_navbar_height)
        val microsurveyHeight =
            appContext.resources.getDimensionPixelSize(R.dimen.browser_microsurvey_height)
        val toolbarHeight =
            appContext.resources.getDimensionPixelSize(R.dimen.browser_toolbar_height)

        return when {
            isNavbarVisible && isMicrosurveyEnabled && isToolbarAtBottom -> navbarHeight + microsurveyHeight + toolbarHeight

            isNavbarVisible && isMicrosurveyEnabled -> navbarHeight + microsurveyHeight
            isNavbarVisible && isToolbarAtBottom -> navbarHeight + toolbarHeight
            isMicrosurveyEnabled && isToolbarAtBottom -> microsurveyHeight + toolbarHeight

            isNavbarVisible -> navbarHeight
            isMicrosurveyEnabled -> microsurveyHeight
            isToolbarAtBottom -> toolbarHeight

            else -> 0
        }
    }

    /**
     * Returns the height of the top toolbar.
     *
     * @param includeTabStrip If true, the height of the tab strip is included in the calculation.
     */
    // todo: eventually remove
    fun getTopToolbarHeight(includeTabStrip: Boolean): Int {
        val isToolbarAtTop = toolbarPosition == ToolbarPosition.TOP
        val toolbarHeight =
            appContext.resources.getDimensionPixelSize(R.dimen.browser_toolbar_height)

        return if (isToolbarAtTop && includeTabStrip) {
            toolbarHeight + appContext.resources.getDimensionPixelSize(R.dimen.tab_strip_height)
        } else if (isToolbarAtTop) {
            toolbarHeight
        } else {
            0
        }
    }

    /**
     * Returns the height of the bottom toolbar container.
     *
     * The bottom toolbar container can consist of a navigation bar, the microsurvey prompt
     * a combination of a navigation and microsurvey prompt, or be absent.
     */
    // todo: eventually remove
    fun getBottomToolbarContainerHeight(): Int {
        val isNavBarEnabled = navigationToolbarEnabled
        val isMicrosurveyEnabled = shouldShowMicrosurveyPrompt
        val navbarHeight = appContext.resources.getDimensionPixelSize(R.dimen.browser_navbar_height)
        val microsurveyHeight =
            appContext.resources.getDimensionPixelSize(R.dimen.browser_microsurvey_height)

        return when {
            isNavBarEnabled && isMicrosurveyEnabled -> navbarHeight + microsurveyHeight
            isNavBarEnabled -> navbarHeight
            isMicrosurveyEnabled -> microsurveyHeight
            else -> 0
        }
    }

    /**
     * Indicates if the user is shown the new navigation toolbar.
     */
    // todo: eventually remove
    var navigationToolbarEnabled by lazyFeatureFlagPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_toolbar_show_navigation_toolbar),
        default = { FxNimbus.features.navigationToolbar.value().enabled },
        featureFlag = true,
    )

    /**
     * Indicates if the microsurvey feature is enabled.
     */
    // todo: check usages to see if need to remove
    var microsurveyFeatureEnabled by lazyFeatureFlagPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_microsurvey_feature_enabled),
        default = { FxNimbus.features.microsurveys.value().enabled },
        featureFlag = true,
    )

    /**
     * Indicates if a microsurvey should be shown to the user.
     */
    // todo: check usages to see if need to remove
    var shouldShowMicrosurveyPrompt by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_should_show_microsurvey_prompt),
        default = false,
    )

    /**
     * Indicates if the Set as default browser prompt for existing users feature is enabled.
     */
    // todo: check usages to see if need to remove
    var setAsDefaultBrowserPromptForExistingUsersEnabled by lazyFeatureFlagPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_set_as_default_browser_prompt_enabled),
        default = { FxNimbus.features.setAsDefaultPrompt.value().enabled },
        featureFlag = true,
    )

    /**
     * Last time the Set as default Browser prompt has been displayed to the user.
     */
    // todo: check usages to see if need to remove
    var lastSetAsDefaultPromptShownTimeInMillis by longPreference(
        appContext.getPreferenceKey(R.string.pref_key_last_set_as_default_prompt_shown_time),
        default = 0L,
    )

    /**
     * Number of times the Set as default Browser prompt has been displayed to the user.
     */
    // todo: check usages to see if need to remove
    var numberOfSetAsDefaultPromptShownTimes by intPreference(
        appContext.getPreferenceKey(R.string.pref_key_number_of_set_as_default_prompt_shown_times),
        default = 0,
    )

    /**
     * Number of app cold starts between Set as default Browser prompts.
     */
    // todo: check usages to see if need to remove
    var coldStartsBetweenSetAsDefaultPrompts by intPreference(
        appContext.getPreferenceKey(R.string.pref_key_app_cold_start_count),
        default = 0,
    )

    /**
     * Number of days between Set as default Browser prompts.
     */
    // todo: check usages to see if need to remove
    private val daysBetweenDefaultBrowserPrompts: Int
        get() = FxNimbus.features.setAsDefaultPrompt.value().daysBetweenPrompts

    /**
     * Maximum number of times the Set as default Browser prompt can be displayed to the user.
     */
    // todo: check usages to see if need to remove
    private val maxNumberOfDefaultBrowserPrompts: Int
        get() = FxNimbus.features.setAsDefaultPrompt.value().maxNumberOfTimesToDisplay

    /**
     * Number of app cold starts before displaying the Set as default Browser prompt.
     */
    // todo: check usages to see if need to remove
    private val appColdStartsToShowDefaultPrompt: Int
        get() = FxNimbus.features.setAsDefaultPrompt.value().appColdStartsBetweenPrompts

    /**
     * Indicates if the Set as default Browser prompt should be displayed to the user.
     */
    // todo: check usages to see if need to remove
    val shouldShowSetAsDefaultPrompt: Boolean
        get() = setAsDefaultBrowserPromptForExistingUsersEnabled && (System.currentTimeMillis() - lastSetAsDefaultPromptShownTimeInMillis) > daysBetweenDefaultBrowserPrompts * ONE_DAY_MS && numberOfSetAsDefaultPromptShownTimes < maxNumberOfDefaultBrowserPrompts && coldStartsBetweenSetAsDefaultPrompts >= appColdStartsToShowDefaultPrompt

    /**
     * Updates the relevant settings when the "Set as Default Browser" prompt is shown.
     *
     * This method increments the count of how many times the prompt has been shown,
     * records the current time as the last time the prompt was shown, and resets
     * the counter for the number of cold starts between prompts.
     */
    // todo: check usages to see if need to remove
    fun setAsDefaultPromptCalled() {
        numberOfSetAsDefaultPromptShownTimes += 1
        lastSetAsDefaultPromptShownTimeInMillis = System.currentTimeMillis()
        coldStartsBetweenSetAsDefaultPrompts = 0
    }

//    /**
//     * A timestamp indicating the end of a deferral period, initiated when users deny submitted a crash,
//     * during which we avoid showing the unsubmitted crash dialog.
//     */
//    var crashReportDeferredUntil by longPreference(
//        appContext.getPreferenceKey(R.string.pref_key_crash_reporting_deferred_until),
//        default = 0,
//    )

//    /**
//     * A timestamp (in milliseconds) representing the earliest cutoff date for fetching crashes
//     * from the database. Crashes that occurred before this timestamp are ignored, ensuring the
//     * unsubmitted crash dialog is not displayed for older crashes.
//     */
//    var crashReportCutoffDate by longPreference(
//        appContext.getPreferenceKey(R.string.pref_key_crash_reporting_cutoff_date),
//        default = 0,
//    )

    /**
     * A user preference indicating that crash reports should always be automatically sent. This can be updated
     * through the unsubmitted crash dialog or through data choice preferences.
     */
    // todo: check usages to see if need to remove
    var crashReportAlwaysSend by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_crash_reporting_always_report),
        default = false,
    )

//    /**
//     * Indicates whether or not we should use the new crash reporter dialog.
//     */
//    // todo: crash reporting
//    var useNewCrashReporterDialog by booleanPreference(
//        appContext.getPreferenceKey(R.string.pref_key_use_new_crash_reporter),
//        default = false,
//    )

    /**
     * Indicates whether or not we should use the new bookmarks UI.
     */
    // todo: check usages to see if need to remove
    var useNewBookmarks by lazyFeatureFlagPreference(
        key = appContext.getPreferenceKey(R.string.pref_key_use_new_bookmarks_ui),
        default = { FxNimbus.features.bookmarks.value().newComposeUi },
        featureFlag = true,
    )
}