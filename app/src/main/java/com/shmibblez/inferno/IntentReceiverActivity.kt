/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import androidx.annotation.VisibleForTesting
import mozilla.components.feature.intent.ext.sanitize
import mozilla.components.feature.intent.processing.IntentProcessor
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.utils.EXTRA_ACTIVITY_REFERRER_CATEGORY
import mozilla.components.support.utils.EXTRA_ACTIVITY_REFERRER_PACKAGE
import mozilla.components.support.utils.INTENT_TYPE_PDF
import mozilla.components.support.utils.ext.getApplicationInfoCompat
import mozilla.components.support.utils.toSafeIntent
import com.shmibblez.inferno.HomeActivity.Companion.PRIVATE_BROWSING_MODE
import com.shmibblez.inferno.browser.nav.BrowserNavHost
import com.shmibblez.inferno.browser.nav.InitialBrowserTask
import com.shmibblez.inferno.customtabs.FennecWebAppIntentProcessor
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.isIntentInternal
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.home.intent.FennecBookmarkShortcutsIntentProcessor
import com.shmibblez.inferno.intent.ExternalDeepLinkIntentProcessor
import com.shmibblez.inferno.perf.MarkersActivityLifecycleCallbacks
import com.shmibblez.inferno.perf.StartupTimeline
import com.shmibblez.inferno.shortcut.NewTabShortcutIntentProcessor
import com.shmibblez.inferno.shortcut.PasswordManagerIntentProcessor
import mozilla.components.feature.customtabs.CustomTabIntentProcessor
import mozilla.components.feature.intent.ext.getSessionId
import mozilla.components.feature.intent.processing.TabIntentProcessor
import mozilla.components.feature.pwa.intent.WebAppIntentProcessor
import mozilla.components.feature.webnotifications.WebNotificationIntentProcessor

/**
 * Processes incoming intents
 * Sends all possible actions as [InitialBrowserTask]s to [BrowserNavHost].
 */
class IntentReceiverActivity : Activity() {

    private val logger = Logger("IntentReceiverActivity")

    @VisibleForTesting
    override fun onCreate(savedInstanceState: Bundle?) {
        // DO NOT MOVE ANYTHING ABOVE THIS getProfilerTime CALL.
        val startTimeProfiler = components.core.engine.profiler?.getProfilerTime()

        // StrictMode violation on certain devices such as Samsung
        components.strictMode.resetAfter(StrictMode.allowThreadDiskReads()) {
            super.onCreate(savedInstanceState)
        }

        // The intent property is nullable, but the rest of the code below
        // assumes it is not. If it's null, then we make a new one and open
        // the HomeActivity.
        val intent = intent?.let { Intent(it) } ?: Intent()
        intent.sanitize().stripUnwantedFlags()
        processIntent(intent)

        components.core.engine.profiler?.addMarker(
            MarkersActivityLifecycleCallbacks.MARKER_NAME,
            startTimeProfiler,
            "IntentReceiverActivity.onCreate",
        )
        StartupTimeline.onActivityCreateEndIntentReceiver() // DO NOT MOVE ANYTHING BELOW HERE.
    }

    private fun processIntent(intent: Intent) {
        // Call process for side effects, short on the first that returns true

        var private = settings().openLinksInAPrivateTab
        if (!private) {
            // if PRIVATE_BROWSING_MODE is already set to true, honor that
            private = intent.getBooleanExtra(PRIVATE_BROWSING_MODE, false)
        }
        intent.putExtra(PRIVATE_BROWSING_MODE, private)

        addReferrerInformation(intent)

        if (intent.type == INTENT_TYPE_PDF) {
            val referrerIsFenix = this.isIntentInternal()
            if (!referrerIsFenix) {
                intent.toSafeIntent().data?.let(::persistUriReadPermission)
            }
        }

        val processor = getIntentProcessors(private).firstOrNull { it.process(intent) }
//        val intentProcessorType = components.intentProcessors.getType(processor)

        val initialTask = processor.toInitialBrowserTask(intent, private)
        launch(intent, initialTask)
    }

    private fun persistUriReadPermission(uri: Uri) {
        try {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (securityException: SecurityException) {
            logger.debug("UriPermission could not be persisted", securityException)
        }
    }


    @VisibleForTesting
    internal fun launch(intent: Intent, initialBrowserTask: InitialBrowserTask) {
        intent.setClassName(
            applicationContext, HomeActivity::class.java.name
        ) // intentProcessorType.activityClassName)
//        intent.putExtra("aaa", intentProcessorType.getBrowserAction(intent))
        intent.putExtra(HomeActivity.INITIAL_BROWSER_TASK, initialBrowserTask)

//        if (!intent.hasExtra(HomeActivity.OPEN_TO_BROWSER)) {
//            intent.putExtra(
//                HomeActivity.OPEN_TO_BROWSER,
//                intentProcessorType.shouldOpenToBrowser(intent),
//            )
//        }
        // StrictMode violation on certain devices such as Samsung
        components.strictMode.resetAfter(StrictMode.allowThreadDiskReads()) {
            startActivity(intent)
        }
        finish() // must finish() after starting the other activity
    }

    private fun getIntentProcessors(private: Boolean): List<IntentProcessor> {
        val modeDependentProcessors = if (private) {
            listOf(
                components.intentProcessors.privateCustomTabIntentProcessor,
                components.intentProcessors.privateIntentProcessor,
            )
        } else {
            listOf(
                components.intentProcessors.customTabIntentProcessor,
                components.intentProcessors.intentProcessor,
            )
        }

        return components.intentProcessors.externalAppIntentProcessors + components.intentProcessors.fennecPageShortcutIntentProcessor + components.intentProcessors.externalDeepLinkIntentProcessor + components.intentProcessors.webNotificationsIntentProcessor + components.intentProcessors.passwordManagerIntentProcessor + modeDependentProcessors + NewTabShortcutIntentProcessor()
    }

    private fun addReferrerInformation(intent: Intent) {
        // Pass along referrer information when possible.
        // Referrer is supported for API>=22.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            return
        }
        // unfortunately you can get a RuntimeException thrown from android here
        @Suppress("TooGenericExceptionCaught") val r = try {
            // NB: referrer can be spoofed by the calling application. Use with caution.
            referrer
        } catch (e: RuntimeException) {
            // this could happen if the referrer intent contains data we can't deserialize
            return
        } ?: return
        intent.putExtra(EXTRA_ACTIVITY_REFERRER_PACKAGE, r.host)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Category is supported for API>=26.
            r.host?.let { host ->
                try {
                    val category = packageManager.getApplicationInfoCompat(host, 0).category
                    intent.putExtra(EXTRA_ACTIVITY_REFERRER_CATEGORY, category)
                } catch (e: PackageManager.NameNotFoundException) {
                    // At least we tried.
                }
            }
        }
    }
}

private fun Intent.stripUnwantedFlags() {
    // Explicitly remove the new task and clear task flags (Our browser activity is a single
    // task activity and we never want to start a second task here).
    flags = flags and Intent.FLAG_ACTIVITY_NEW_TASK.inv()
    flags = flags and Intent.FLAG_ACTIVITY_CLEAR_TASK.inv()

    // IntentReceiverActivity is started with the "excludeFromRecents" flag (set in manifest). We
    // do not want to propagate this flag from the intent receiver activity to the browser.
    flags = flags and Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS.inv()
}

private fun IntentProcessor?.toInitialBrowserTask(
    intent: Intent,
    private: Boolean = false,
): InitialBrowserTask {
    return when (this) {
        /**
         * external app processors (web apps, custom tabs, etc)
         * todo: check if need to use custom tab or just initial currentTab
         */
        // IntentProcessors.externalAppIntentProcessors
        is WebAppIntentProcessor,
        is FennecWebAppIntentProcessor,
            -> {
            intent.getSessionId().let {
                if (!it.isNullOrEmpty()) {
                    // if url good, launch external app component
                    InitialBrowserTask.ExternalApp(url = it, private = false)
                } else {
                    // if url null or empty, launch browser normally
                    InitialBrowserTask.OpenToBrowser(private = false)
                }
            }
        }
        // IntentProcessors.customTabIntentProcessor
        // IntentProcessors.privateCustomTabIntentProcessor
        is CustomTabIntentProcessor,
            -> {
            intent.getSessionId().let {
                if (!it.isNullOrEmpty()) {
                    // if url good, launch external app component
                    InitialBrowserTask.ExternalApp(url = it, private = private)
                } else {
                    // if url null or empty, launch browser normally
                    InitialBrowserTask.OpenToBrowser(private = false)
                }
            }
        }

        /**
         * new tab processors (shortcut opened, web notification clicked, etc)
         */
        // IntentProcessors.intentProcessor
        // IntentProcessors.privateIntentProcessor
        is TabIntentProcessor -> {
            // in case of processViewIntent, processSendIntent, and processSearchIntent,
            // just open to browser in normal/private mode, tab should already be selected
            InitialBrowserTask.OpenToBrowser(private = private)
        }
        // IntentProcessors.fennecPageShortcutIntentProcessor
        is FennecBookmarkShortcutsIntentProcessor -> {
            // open to browser, tab should already be selected
            InitialBrowserTask.OpenToBrowser(private = false)
        }
        // IntentProcessors.webNotificationsIntentProcessor
        is WebNotificationIntentProcessor -> {
            // todo: test, 99% sure this is implemented by gecko engine already
            // open to browser, corresponding tab should already be selected/created
            InitialBrowserTask.OpenToBrowser(private = false)
        }

        /**
         * deeplink processor
         */
        // IntentProcessors.externalDeepLinkIntentProcessor
        is ExternalDeepLinkIntentProcessor -> {
            // no idea how this works
            // todo: check what is done with deep link intent flags, may need to integrate
            InitialBrowserTask.OpenToBrowser(private = false)
        }

        /**
         * password manager processor
         */
        // IntentProcessors.passwordManagerIntentProcessor
        is PasswordManagerIntentProcessor -> {
            InitialBrowserTask.OpenPasswordManager
        }

        /**
         * other, just open to browser
         */
        null -> {
            InitialBrowserTask.OpenToBrowser(private = private)
        }

        else -> {
            InitialBrowserTask.OpenToBrowser(private = private)
        }
    }
}