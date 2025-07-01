/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.settings.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.browser.nav.InitialBrowserTask
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.settings
import mozilla.components.feature.intent.ext.getSessionId

/**
 * Processes incoming intents and sends them to the corresponding activity.
 */
class AuthIntentReceiverActivity : Activity() {

    @VisibleForTesting
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MainScope().launch {
            // The intent property is nullable, but the rest of the code below
            // assumes it is not. If it's null, then we make a new one and open
            // the HomeActivity.
            val intent = intent?.let { Intent(intent) } ?: Intent()
            val initialBrowserTask: InitialBrowserTask

            if (settings().lastKnownMode.isPrivate) {
                val matches = components.intentProcessors.privateCustomTabIntentProcessor.process(intent)
                initialBrowserTask = when (matches) {
                    true -> {
                        InitialBrowserTask.ExternalApp(
                            tabId = intent.getSessionId()!!,
                            private = true,
                        )
                    }

                    false -> {
                        InitialBrowserTask.OpenToBrowser(private = true)
                    }
                }
            } else {
                val matches = components.intentProcessors.customTabIntentProcessor.process(intent)
                initialBrowserTask = when (matches) {
                    true -> {
                        InitialBrowserTask.ExternalApp(
                            tabId = intent.getSessionId()!!,
                            private = false,
                        )
                    }

                    false -> {
                        InitialBrowserTask.OpenToBrowser(private = false)
                    }
                }
            }

            // todo: AuthCustomTabActivity, closes when auth complete, extend HomeActivity
            intent.setClassName(applicationContext, HomeActivity::class.java.name)
//            intent.setClassName(applicationContext, AuthCustomTabActivity::class.java.name)
            intent.putExtra(HomeActivity.OPEN_TO_BROWSER, true)
            intent.putExtra(HomeActivity.INITIAL_BROWSER_TASK, initialBrowserTask)

            startActivity(intent)

            finish()
        }
    }
}
