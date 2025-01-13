/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.ext

import com.shmibblez.inferno.mozillaAndroidComponents.concept.engine.prompt.PromptRequest
import com.shmibblez.inferno.mozillaAndroidComponents.concept.engine.prompt.PromptRequest.Alert
import com.shmibblez.inferno.mozillaAndroidComponents.concept.engine.prompt.PromptRequest.Confirm
import com.shmibblez.inferno.mozillaAndroidComponents.concept.engine.prompt.PromptRequest.Popup
import com.shmibblez.inferno.mozillaAndroidComponents.concept.engine.prompt.PromptRequest.TextPrompt
import kotlin.reflect.KClass

/**
 * List of all prompts who are not to be shown in fullscreen.
 */
@PublishedApi
internal val PROMPTS_TO_EXIT_FULLSCREEN_FOR = listOf<KClass<out PromptRequest>>(
    Alert::class,
    TextPrompt::class,
    Confirm::class,
    Popup::class,
)

/**
 * Convenience method for executing code if the current [PromptRequest] is one that
 * should not be shown in fullscreen tabs.
 */
internal inline fun PromptRequest.executeIfWindowedPrompt(
    block: () -> Unit,
) {
    if (PROMPTS_TO_EXIT_FULLSCREEN_FOR.any { this::class == it }) {
        block.invoke()
    }
}
