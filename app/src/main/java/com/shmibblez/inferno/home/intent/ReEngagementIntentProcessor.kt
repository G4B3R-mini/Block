/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.home.intent

import android.content.Intent
import androidx.navigation.NavController
import androidx.navigation.navOptions
import mozilla.components.concept.engine.EngineSession
import mozilla.telemetry.glean.private.NoExtras
import com.shmibblez.inferno.BrowserDirection
import com.shmibblez.inferno.GleanMetrics.Events
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.NavGraphDirections
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.browsingmode.BrowsingMode
import com.shmibblez.inferno.ext.nav
import com.shmibblez.inferno.onboarding.ReEngagementNotificationWorker
import com.shmibblez.inferno.onboarding.ReEngagementNotificationWorker.Companion.isReEngagementNotificationIntent
import com.shmibblez.inferno.utils.Settings

/**
 * Handle when the re-engagement notification is tapped
 *
 * This should only happens once in a user's lifetime notification,
 * [settings.shouldShowReEngagementNotification] will return false if the user already seen the
 * notification.
 */
class ReEngagementIntentProcessor(
    private val activity: HomeActivity,
    private val settings: Settings,
) : HomeIntentProcessor {

    override fun process(intent: Intent, navController: NavController, out: Intent): Boolean {
        return when {
            isReEngagementNotificationIntent(intent) -> {
                Events.reEngagementNotifTapped.record(NoExtras())

                when (settings.reEngagementNotificationType) {
                    ReEngagementNotificationWorker.NOTIFICATION_TYPE_B -> {
                        val directions = NavGraphDirections.actionGlobalSearchDialog(sessionId = null)
                        val options = navOptions {
                            popUpTo(R.id.homeFragment)
                        }
                        navController.nav(null, directions, options)
                    }
                    else -> {
                        activity.browsingModeManager.mode = BrowsingMode.Private
                        activity.openToBrowserAndLoad(
                            ReEngagementNotificationWorker.NOTIFICATION_TARGET_URL,
                            newTab = true,
                            from = BrowserDirection.FromGlobal,
                            flags = EngineSession.LoadUrlFlags.external(),
                        )
                    }
                }

                true
            }
            else -> false
        }
    }
}
