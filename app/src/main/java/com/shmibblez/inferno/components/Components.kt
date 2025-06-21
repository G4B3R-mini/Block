/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.components

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
//import com.google.android.play.core.review.ReviewManagerFactory
import mozilla.components.feature.addons.AddonManager
import mozilla.components.feature.addons.amo.AMOAddonsProvider
import mozilla.components.feature.addons.migration.DefaultSupportedAddonsChecker
import mozilla.components.feature.addons.update.DefaultAddonUpdater
import mozilla.components.feature.autofill.AutofillConfiguration
import mozilla.components.lib.crash.store.CrashAction
import mozilla.components.lib.publicsuffixlist.PublicSuffixList
import mozilla.components.support.base.android.NotificationsDelegate
import mozilla.components.support.base.worker.Frequency
import com.shmibblez.inferno.BuildConfig
import com.shmibblez.inferno.Config
import com.shmibblez.inferno.FeatureFlags
import com.shmibblez.inferno.R
import com.shmibblez.inferno.autofill.AutofillConfirmActivity
import com.shmibblez.inferno.autofill.AutofillSearchActivity
import com.shmibblez.inferno.autofill.AutofillUnlockActivity
import com.shmibblez.inferno.components.appstate.AppAction
import com.shmibblez.inferno.components.appstate.AppState
//import com.shmibblez.inferno.components.metrics.MetricsMiddleware
//import com.shmibblez.inferno.crashes.CrashReportingAppMiddleware
//import com.shmibblez.inferno.crashes.SettingsCrashReportCache
//import com.shmibblez.inferno.datastore.pocketStoriesSelectedCategoriesDataStore
import com.shmibblez.inferno.ext.asRecentTabs
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.filterState
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.ext.sort
//import com.shmibblez.inferno.home.PocketUpdatesMiddleware
import com.shmibblez.inferno.home.blocklist.BlocklistHandler
import com.shmibblez.inferno.home.blocklist.BlocklistMiddleware
import com.shmibblez.inferno.messaging.state.MessagingMiddleware
import com.shmibblez.inferno.onboarding.FenixOnboarding
import com.shmibblez.inferno.perf.AppStartReasonProvider
import com.shmibblez.inferno.perf.StartupActivityLog
import com.shmibblez.inferno.perf.StartupStateProvider
import com.shmibblez.inferno.perf.StrictModeManager
import com.shmibblez.inferno.perf.lazyMonitored
import com.shmibblez.inferno.settings.theme.InfernoThemeProvider
import com.shmibblez.inferno.utils.ClipboardHandler
import com.shmibblez.inferno.utils.Settings
import com.shmibblez.inferno.wifi.WifiConnectionMonitor
import kotlinx.coroutines.Job
import mozilla.components.concept.base.crash.Breadcrumb
import mozilla.components.concept.base.crash.CrashReporting
import mozilla.components.lib.crash.CrashReporter
import mozilla.components.lib.crash.service.MozillaSocorroService
//import mozilla.components.lib.crash.CrashReporter
import java.util.concurrent.TimeUnit

private const val AMO_COLLECTION_MAX_CACHE_AGE = 2 * 24 * 60L // Two days in minutes

/**
 * Provides access to all components. This class is an implementation of the Service Locator
 * pattern, which helps us manage the dependencies in our app.
 *
 * Note: these aren't just "components" from "android-components": they're any "component" that
 * can be considered a building block of our app.
 */
class Components(private val context: Context) {
    private val notificationManagerCompat = NotificationManagerCompat.from(context)

    val notificationsDelegate: NotificationsDelegate by lazyMonitored {
        NotificationsDelegate(
            notificationManagerCompat,
        )
    }

    // todo: key to handling errors
//    Thread.setDefaultUncaughtExceptionHandler(handler)
    val crashReporter = object : CrashReporting {
        override fun recordCrashBreadcrumb(breadcrumb: Breadcrumb) {
            Firebase.crashlytics.setCustomKey(breadcrumb.type.name, breadcrumb.message)
        }
        override fun submitCaughtException(throwable: Throwable): Job {
            Firebase.crashlytics.recordException(throwable)
            return Job().also { it.complete() }
        }
    }
//    val crashReporter = CrashReporter(
//        enabled = false,
//        context = context,
//        notificationsDelegate = notificationsDelegate,
//        services = listOf(
//            MozillaSocorroService(
//                applicationContext = context, appName = "Inferno Browser"
//            ),
//        ),
//    )

    val backgroundServices by lazyMonitored {
        BackgroundServices(
            context,
//            push = Push(context, crashReporter),
            core.lazyHistoryStorage,
            core.lazyBookmarksStorage,
            core.lazyPasswordsStorage,
            core.lazyRemoteTabsStorage,
            core.lazyAutofillStorage,
            strictMode,
        )
    }
    val services by lazyMonitored {
        Services(context, core.store, backgroundServices.accountManager)
    }


    val core by lazyMonitored {
        Core(context, crashReporter, strictMode)
    }

    val useCases by lazyMonitored {
        UseCases(
            context,
            core.engine,
            core.store,
            core.webAppShortcutManager,
            core.topSitesStorage,
            core.bookmarksStorage,
            core.historyStorage,
            backgroundServices.syncedTabsCommands,
            appStore,
            core.client,
            strictMode,
        )
    }

    val intentProcessors by lazyMonitored {
        IntentProcessors(
            context,
            core.store,
            useCases.sessionUseCases,
            useCases.tabsUseCases,
            useCases.customTabsUseCases,
            useCases.searchUseCases,
            core.webAppManifestStorage,
            core.engine,
        )
    }

    val addonsProvider by lazyMonitored {
        // Check if we have a customized (overridden) AMO collection (supported in Nightly & Beta)
        if (FeatureFlags.customExtensionCollectionFeature && context.settings()
                .amoCollectionOverrideConfigured()
        ) {
            AMOAddonsProvider(
                context,
                core.client,
                collectionUser = context.settings().overrideAmoUser,
                collectionName = context.settings().overrideAmoCollection,
            )
        }
        // Use build config otherwise
        else if (!BuildConfig.AMO_COLLECTION_USER.isNullOrEmpty() && !BuildConfig.AMO_COLLECTION_NAME.isNullOrEmpty()) {
            AMOAddonsProvider(
                context,
                core.client,
                serverURL = BuildConfig.AMO_SERVER_URL,
                collectionUser = BuildConfig.AMO_COLLECTION_USER,
                collectionName = BuildConfig.AMO_COLLECTION_NAME,
                maxCacheAgeInMinutes = AMO_COLLECTION_MAX_CACHE_AGE,
            )
        }
        // Fall back to defaults
        else {
            AMOAddonsProvider(
                context, core.client, maxCacheAgeInMinutes = AMO_COLLECTION_MAX_CACHE_AGE
            )
        }
    }

    @Suppress("MagicNumber")
    val addonUpdater by lazyMonitored {
        DefaultAddonUpdater(context, Frequency(12, TimeUnit.HOURS), notificationsDelegate)
    }

    @Suppress("MagicNumber")
    val supportedAddonsChecker by lazyMonitored {
        DefaultSupportedAddonsChecker(
            context,
            Frequency(12, TimeUnit.HOURS),
        )
    }

    val addonManager by lazyMonitored {
        AddonManager(core.store, core.engine, addonsProvider, addonUpdater)
    }

    //    val analytics by lazyMonitored { Analytics(context, performance.visualCompletenessQueue.queue) }
    val nimbus by lazyMonitored { NimbusComponents(context) }
    val publicSuffixList by lazyMonitored { PublicSuffixList(context) }
    val clipboardHandler by lazyMonitored { ClipboardHandler(context) }
    val performance by lazyMonitored { PerformanceComponent() }

    //    val push by lazyMonitored { Push(context, analytics.crashReporter) }
    val wifiConnectionMonitor by lazyMonitored { WifiConnectionMonitor(context as Application) }
    val strictMode by lazyMonitored { StrictModeManager(Config, this) }

    val settings by lazyMonitored { Settings(context) }
    val infernoTheme by lazyMonitored { InfernoThemeProvider(context) }

    val fenixOnboarding by lazyMonitored { FenixOnboarding(context) }

//    val reviewPromptController by lazyMonitored {
//        ReviewPromptController(
//            manager = ReviewManagerFactory.create(context),
//            reviewSettings = FenixReviewSettings(settings),
//        )
//    }

    @delegate:SuppressLint("NewApi")
    val autofillConfiguration by lazyMonitored {
        AutofillConfiguration(
            storage = core.passwordsStorage,
            publicSuffixList = publicSuffixList,
            unlockActivity = AutofillUnlockActivity::class.java,
            confirmActivity = AutofillConfirmActivity::class.java,
            searchActivity = AutofillSearchActivity::class.java,
            applicationName = context.getString(R.string.app_name),
            httpClient = core.client,
        )
    }

    val appStartReasonProvider by lazyMonitored { AppStartReasonProvider() }
    val startupActivityLog by lazyMonitored { StartupActivityLog() }
    val startupStateProvider by lazyMonitored {
        StartupStateProvider(
            startupActivityLog, appStartReasonProvider
        )
    }

    val appStore by lazyMonitored {
        val blocklistHandler = BlocklistHandler(settings)

        AppStore(
            initialState = AppState(
                collections = core.tabCollectionStorage.cachedTabCollections,
                expandedCollections = emptySet(),
                topSites = core.topSitesStorage.cachedTopSites.sort(),
                bookmarks = emptyList(),
                showCollectionPlaceholder = settings.showCollectionsPlaceholderOnHome,
                // Provide an initial state for recent tabs to prevent re-rendering on the home screen.
                //  This will otherwise cause a visual jump as the section gets rendered from no state
                //  to some state.
                recentTabs = if (settings.showRecentTabsFeature) {
                    core.store.state.asRecentTabs()
                } else {
                    emptyList()
                },
                recentHistory = emptyList(),
            ).run { filterState(blocklistHandler) },
            middlewares = listOf(
                BlocklistMiddleware(blocklistHandler),
//                PocketUpdatesMiddleware(
//                    core.pocketStoriesService,
//                    context.pocketStoriesSelectedCategoriesDataStore,
//                ),
                MessagingMiddleware(
                    controller = nimbus.messaging,
                    settings = settings,
                ),
//                MetricsMiddleware(metrics = analytics.metrics),
//                CrashReportingAppMiddleware(
//                    CrashMiddleware(
//                        cache = SettingsCrashReportCache(settings),
//                        crashReporter = analytics.crashReporter,
//                        currentTimeInMillis = { System.currentTimeMillis() },
//                    ),
//                ),
            ),
        ).also {
            it.dispatch(AppAction.CrashActionWrapper(CrashAction.Initialize))
        }
    }

    val fxSuggest by lazyMonitored { FxSuggest(context) }
}

/**
 * Returns the [Components] object from within a [Composable].
 */
val components: Components
    @Composable get() = LocalContext.current.components
