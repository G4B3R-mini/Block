/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.shortcut

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import mozilla.components.feature.intent.processing.IntentProcessor
import mozilla.components.support.utils.SafeIntent
import com.shmibblez.inferno.HomeActivity

/**
 * Intent processor that will handle intents for the password manager shortcut
 */
class PasswordManagerIntentProcessor : IntentProcessor {

    /**
     * Processes the given [Intent].
     *
     * @param intent The intent to process.
     * @return True if the intent was processed, otherwise false.
     */
    override fun process(intent: Intent): Boolean {
        val safeIntent = SafeIntent(intent)

        if (!safeIntent.action.equals(ACTION_OPEN_PASSWORD_MANAGER)) {
            return false
        }

        intent.putExtra(HomeActivity.OPEN_PASSWORD_MANAGER, true)
        intent.flags = intent.flags or FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        return true
    }

    companion object {
        const val ACTION_OPEN_PASSWORD_MANAGER = "com.shmibblez.inferno.OPEN_PASSWORD_MANAGER"
    }
}
