/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.components

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.drawable.toBitmap
import androidx.preference.PreferenceManager
import mozilla.components.browser.engine.gecko.permission.GeckoSitePermissionsStorage
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.session.storage.SessionStorage
import mozilla.components.browser.state.engine.EngineMiddleware
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.storage.sync.PlacesHistoryStorage
import mozilla.components.browser.storage.sync.RemoteTabsStorage
import mozilla.components.browser.thumbnails.ThumbnailsMiddleware
import mozilla.components.browser.thumbnails.storage.ThumbnailStorage
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineSession.TrackingProtectionPolicy
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.addons.AddonManager
import mozilla.components.feature.addons.amo.AMOAddonsProvider
import mozilla.components.feature.addons.migration.DefaultSupportedAddonsChecker
import mozilla.components.feature.addons.update.DefaultAddonUpdater
import mozilla.components.feature.customtabs.store.CustomTabsServiceStore
import mozilla.components.feature.downloads.DownloadMiddleware
import mozilla.components.feature.media.MediaSessionFeature
import mozilla.components.feature.media.middleware.RecordingDevicesMiddleware
import mozilla.components.feature.prompts.file.FileUploadsDirCleaner
import mozilla.components.feature.pwa.ManifestStorage
import mozilla.components.feature.pwa.WebAppShortcutManager
import mozilla.components.feature.readerview.ReaderViewMiddleware
import mozilla.components.feature.search.middleware.SearchMiddleware
import mozilla.components.feature.search.region.RegionMiddleware
import mozilla.components.feature.session.HistoryDelegate
import mozilla.components.feature.sitepermissions.OnDiskSitePermissionsStorage
import mozilla.components.feature.webnotifications.WebNotificationFeature
import mozilla.components.lib.crash.CrashReporter
import mozilla.components.lib.dataprotect.SecureAbove22Preferences
import mozilla.components.service.location.LocationService
import mozilla.components.service.sync.logins.SyncableLoginsStorage
import mozilla.components.support.base.worker.Frequency
import com.shmibblez.inferno.AppRequestInterceptor
import com.shmibblez.inferno.BrowserActivity
import com.shmibblez.inferno.EngineProvider
import com.shmibblez.inferno.R
import com.shmibblez.inferno.R.string.pref_key_remote_debugging
import com.shmibblez.inferno.R.string.pref_key_tracking_protection_normal
import com.shmibblez.inferno.R.string.pref_key_tracking_protection_private
import com.shmibblez.inferno.downloads.DownloadService
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.getPreferenceKey
import com.shmibblez.inferno.media.MediaSessionService
import com.shmibblez.inferno.settings.Settings
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.state.SearchState
import mozilla.components.feature.media.middleware.LastMediaAccessMiddleware
import mozilla.components.feature.search.ext.createApplicationSearchEngine
import mozilla.components.feature.session.middleware.LastAccessMiddleware
import java.util.UUID
import java.util.concurrent.TimeUnit

private const val DAY_IN_MINUTES = 24 * 60L

private object DefaultSearchEngines {
//    val Google = SearchEngine(
//        id="Google",
//        name = "google",
//        icon = ,
//        inputEncoding = "",
//        )
}

/**
 * Component group for all core browser functionality.
 */
class Core(private val context: Context, crashReporter: CrashReporter) {
    /**
     * The browser engine component initialized based on the build
     * configuration (see build variants).
     */
    val engine: Engine by lazy {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val defaultSettings = DefaultSettings(
            requestInterceptor = AppRequestInterceptor(context),
            remoteDebuggingEnabled = prefs.getBoolean(
                context.getPreferenceKey(
                    pref_key_remote_debugging
                ), false
            ),
            testingModeEnabled = prefs.getBoolean(
                context.getPreferenceKey(R.string.pref_key_testing_mode), false
            ),
            trackingProtectionPolicy = createTrackingProtectionPolicy(prefs),
            historyTrackingDelegate = HistoryDelegate(lazyHistoryStorage),
            globalPrivacyControlEnabled = prefs.getBoolean(
                context.getPreferenceKey(R.string.pref_key_global_privacy_control),
                false,
            ),
        )
        EngineProvider.createEngine(context, defaultSettings)
    }

    /**
     * The [Client] implementation (`concept-fetch`) used for HTTP requests.
     */
    val client: Client by lazy {
        EngineProvider.createClient(context)
    }

    val customSearchEngines: List<SearchEngine> = listOf(
        createApplicationSearchEngine(
            id = UUID.randomUUID().toString(),
            name = GOOGLE_SEARCH_ENGINE_ID,
            url = "www.google.com/",
            icon = getDrawable(context, R.drawable.search_engine_google_24).let {
                it?.colorFilter =
                    PorterDuffColorFilter(Color.White.toArgb(), PorterDuff.Mode.SRC_IN)
                it?.toBitmap()!!
            },
        ), createApplicationSearchEngine(
            id = UUID.randomUUID().toString(),
            name = DUCKDUCKGO_SEARCH_ENGINE_ID,
            url = "www.duckduckgo.com/",
            icon = getDrawable(context, R.drawable.search_engine_duckduckgo_24).let {
                it?.colorFilter =
                    PorterDuffColorFilter(Color.White.toArgb(), PorterDuff.Mode.SRC_IN)
                it?.toBitmap()!!
            },
        ), createApplicationSearchEngine(
            id = UUID.randomUUID().toString(),
            name = BRAVE_SEARCH_ENGINE_ID,
            url = "search.brave.com/",
            icon = getDrawable(context, R.drawable.search_engine_brave_24).let {
                it?.colorFilter =
                    PorterDuffColorFilter(Color.White.toArgb(), PorterDuff.Mode.SRC_IN)
                it?.toBitmap()!!
            },
        ), createApplicationSearchEngine(
            id = UUID.randomUUID().toString(),
            name = YAHOO_SEARCH_ENGINE_ID,
            url = "search.yahoo.com/",
            icon = getDrawable(context, R.drawable.search_engine_yahoo_24).let {
                it?.colorFilter =
                    PorterDuffColorFilter(Color.White.toArgb(), PorterDuff.Mode.SRC_IN)
                it?.toBitmap()!!
            },
        ), createApplicationSearchEngine(
            id = UUID.randomUUID().toString(),
            name = BING_SEARCH_ENGINE_ID,
            url = "www.bing.com/",
            icon = getDrawable(context, R.drawable.search_engine_bing_24).let {
                it?.colorFilter =
                    PorterDuffColorFilter(Color.White.toArgb(), PorterDuff.Mode.SRC_IN)
                it?.toBitmap()!!
            },
        )
    )

//    val applicationSearchEngines: List<SearchEngine> by lazyMonitored {
//        listOf(
//            createApplicationSearchEngine(
//                id = BOOKMARKS_SEARCH_ENGINE_ID,
//                name = context.getString(R.string.library_bookmarks),
//                url = "www.",
//                icon = getDrawable(context, R.drawable.ic_bookmarks_search)?.toBitmap()!!,
//            ),
//            createApplicationSearchEngine(
//                id = TABS_SEARCH_ENGINE_ID,
//                name = context.getString(R.string.preferences_tabs),
//                url = "",
//                icon = getDrawable(context, R.drawable.ic_tabs_search)?.toBitmap()!!,
//            ),
//            createApplicationSearchEngine(
//                id = HISTORY_SEARCH_ENGINE_ID,
//                name = context.getString(R.string.library_history),
//                url = "",
//                icon = getDrawable(context, R.drawable.ic_history_search)?.toBitmap()!!,
//            ),
//        )
//    }

    /**
     * The [BrowserStore] holds the global [BrowserState].
     */
    val store by lazy {
        val middlewareList = mutableListOf(
            LastAccessMiddleware(),
//            RecentlyClosedMiddleware(recentlyClosedTabsStorage, RECENTLY_CLOSED_MAX),
//            TelemetryMiddleware(context.settings(), metrics, crashReporter),
//            UndoMiddleware(context.getUndoDelay()),
//            PromptMiddleware(),
//            AdsTelemetryMiddleware(adsTelemetry),
            LastMediaAccessMiddleware(),
//            HistoryMetadataMiddleware(historyMetadataService),
//            SessionPrioritizationMiddleware(),
//            SaveToPDFMiddleware(context),
            DownloadMiddleware(context, DownloadService::class.java),
            ThumbnailsMiddleware(thumbnailStorage),
            ReaderViewMiddleware(),
            RegionMiddleware(
                context,
                LocationService.default(),
            ),
            SearchMiddleware(context),
            RecordingDevicesMiddleware(context, context.components.notificationsDelegate),
        )
        BrowserStore(
            initialState = BrowserState(
                search = SearchState(
                    customSearchEngines = customSearchEngines,
//                    additionalBundledSearchEngineIds = listOf("reddit", "youtube"),
//                    migration = SearchMigration(context),
                )
            ),
            middleware = middlewareList + EngineMiddleware.create(engine),
        ).apply {
            icons.install(engine, this)

            WebNotificationFeature(
                context,
                engine,
                icons,
                R.drawable.ic_notification,
                geckoSitePermissionsStorage,
                BrowserActivity::class.java,
                notificationsDelegate = context.components.notificationsDelegate,
            )

            MediaSessionFeature(context, MediaSessionService::class.java, this).start()
        }
    }

    /**
     * The [CustomTabsServiceStore] holds global custom tabs related data.
     */
    val customTabsStore by lazy { CustomTabsServiceStore() }

    /**
     * The storage component for persisting browser tab sessions.
     */
    val sessionStorage: SessionStorage by lazy {
        SessionStorage(context, engine)
    }

    /**
     * The storage component to persist browsing history (with the exception of
     * private sessions).
     */
    val lazyHistoryStorage = lazy { PlacesHistoryStorage(context) }

    /**
     * A convenience accessor to the [PlacesHistoryStorage].
     */
    val historyStorage by lazy { lazyHistoryStorage.value }

    /**
     * The storage component to persist logins data (username/password) for websites.
     */
    val lazyLoginsStorage = lazy { SyncableLoginsStorage(context, lazySecurePrefs) }

    /**
     * A convenience accessor to the [SyncableLoginsStorage].
     */
    val loginsStorage by lazy { lazyLoginsStorage.value }

    /**
     * The storage component to sync and persist tabs in a Firefox Sync account.
     */
    val lazyRemoteTabsStorage = lazy { RemoteTabsStorage(context, crashReporter) }

    /**
     * A storage component for persisting thumbnail images of tabs.
     */
    val thumbnailStorage by lazy { ThumbnailStorage(context) }

    /**
     * Component for managing shortcuts (both regular and PWA).
     */
    val shortcutManager by lazy { WebAppShortcutManager(context, client, ManifestStorage(context)) }

    /**
     * A storage component for site permissions.
     */
    val geckoSitePermissionsStorage by lazy {
        val geckoRuntime = EngineProvider.getOrCreateRuntime(context)
        GeckoSitePermissionsStorage(geckoRuntime, OnDiskSitePermissionsStorage(context))
    }

    /**
     * Icons component for loading, caching and processing website icons.
     */
    val icons by lazy { BrowserIcons(context, client) }

    // Addons
    val addonManager by lazy {
        AddonManager(store, engine, addonProvider, addonUpdater)
    }

    val addonUpdater by lazy {
        DefaultAddonUpdater(
            context,
            Frequency(1, TimeUnit.DAYS),
            notificationsDelegate = context.components.notificationsDelegate,
        )
    }

    val addonProvider by lazy {
        if (Settings.isAmoCollectionOverrideConfigured(context)) {
            provideCustomAddonProvider()
        } else {
            provideDefaultAddonProvider()
        }
    }

    val supportedAddonsChecker by lazy {
        DefaultSupportedAddonsChecker(
            context,
            Frequency(12, TimeUnit.HOURS),
        )
    }

    val fileUploadsDirCleaner: FileUploadsDirCleaner by lazy {
        FileUploadsDirCleaner { context.cacheDir }
    }

    private fun provideDefaultAddonProvider(): AMOAddonsProvider {
        return AMOAddonsProvider(
            context = context,
            client = client,
            collectionName = "7dfae8669acc4312a65e8ba5553036",
            maxCacheAgeInMinutes = DAY_IN_MINUTES,
        )
    }

    private fun provideCustomAddonProvider(): AMOAddonsProvider {
        return AMOAddonsProvider(
            context,
            client,
            collectionUser = Settings.getOverrideAmoUser(context),
            collectionName = Settings.getOverrideAmoCollection(context),
        )
    }

    /**
     * Constructs a [TrackingProtectionPolicy] based on current preferences.
     *
     * @param prefs the shared preferences to use when reading tracking
     * protection settings.
     * @param normalMode whether or not tracking protection should be enabled
     * in normal browsing mode, defaults to the current preference value.
     * @param privateMode whether or not tracking protection should be enabled
     * in private browsing mode, default to the current preference value.
     * @return the constructed tracking protection policy based on preferences.
     */
    fun createTrackingProtectionPolicy(
        prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context),
        normalMode: Boolean = prefs.getBoolean(
            context.getPreferenceKey(
                pref_key_tracking_protection_normal
            ), true
        ),
        privateMode: Boolean = prefs.getBoolean(
            context.getPreferenceKey(
                pref_key_tracking_protection_private
            ), true
        ),
    ): TrackingProtectionPolicy {
        val trackingPolicy = TrackingProtectionPolicy.recommended()
        return when {
            normalMode && privateMode -> trackingPolicy
            normalMode && !privateMode -> trackingPolicy.forRegularSessionsOnly()
            !normalMode && privateMode -> trackingPolicy.forPrivateSessionsOnly()
            else -> TrackingProtectionPolicy.none()
        }
    }

    private val lazySecurePrefs = lazy { SecureAbove22Preferences(context, KEY_STORAGE_NAME) }

    companion object {
        private const val KEY_STORAGE_NAME = "core_prefs"
        // search engine ids
        private const val GOOGLE_SEARCH_ENGINE_ID= "google"
        private const val DUCKDUCKGO_SEARCH_ENGINE_ID= "duckduckgo"
        private const val BRAVE_SEARCH_ENGINE_ID= "brave"
        private const val YAHOO_SEARCH_ENGINE_ID= "yahoo"
        private const val BING_SEARCH_ENGINE_ID= "bing"
    }
}
