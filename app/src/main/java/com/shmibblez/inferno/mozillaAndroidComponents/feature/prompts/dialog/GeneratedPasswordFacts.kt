/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.dialog

import com.shmibblez.inferno.mozillaAndroidComponents.support.base.Component
import com.shmibblez.inferno.mozillaAndroidComponents.support.base.facts.Action
import com.shmibblez.inferno.mozillaAndroidComponents.support.base.facts.Fact
import com.shmibblez.inferno.mozillaAndroidComponents.support.base.facts.collect

/**
 * Facts emitted for telemetry related to [com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.login.PasswordGeneratorDialogFragment]
 */
class GeneratedPasswordFacts {
    /**
     * Items that specify how the [com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.login.PasswordGeneratorDialogFragment]
     * was interacted with
     */
    object Items {
        const val SHOWN = "shown"
        const val FILLED = "filled"
    }
}

private fun emitPasswordGeneratorDialogFacts(
    action: Action,
    item: String,
    value: String? = null,
    metadata: Map<String, Any>? = null,
) {
    Fact(
        Component.FEATURE_PROMPTS,
        action,
        item,
        value,
        metadata,
    ).collect()
}

internal fun emitGeneratedPasswordShownFact() = emitPasswordGeneratorDialogFacts(
    action = Action.CLICK,
    item = GeneratedPasswordFacts.Items.SHOWN,
)

internal fun emitGeneratedPasswordFilledFact() = emitPasswordGeneratorDialogFacts(
    action = Action.CLICK,
    item = GeneratedPasswordFacts.Items.FILLED,
)
