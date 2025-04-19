package com.shmibblez.inferno.browser.prompts

import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.engine.prompt.PromptRequest.Alert
import mozilla.components.concept.engine.prompt.PromptRequest.Confirm
import mozilla.components.concept.engine.prompt.PromptRequest.Popup
import mozilla.components.concept.engine.prompt.PromptRequest.TextPrompt
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