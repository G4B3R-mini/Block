/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.shmibblez.inferno.mozillaAndroidComponents.browser.state.action.BrowserAction
import com.shmibblez.inferno.mozillaAndroidComponents.browser.state.action.ContentAction
import com.shmibblez.inferno.mozillaAndroidComponents.browser.state.selector.findTab
import com.shmibblez.inferno.mozillaAndroidComponents.browser.state.state.BrowserState
import com.shmibblez.inferno.mozillaAndroidComponents.concept.engine.prompt.PromptRequest
import com.shmibblez.inferno.mozillaAndroidComponents.lib.state.Middleware
import com.shmibblez.inferno.mozillaAndroidComponents.lib.state.MiddlewareContext

/**
 * [Middleware] implementation for managing [PromptRequest]s.
 */
class PromptMiddleware : Middleware<BrowserState, BrowserAction> {

    private val scope = MainScope()

    override fun invoke(
        context: MiddlewareContext<BrowserState, BrowserAction>,
        next: (BrowserAction) -> Unit,
        action: BrowserAction,
    ) {
        when (action) {
            is ContentAction.UpdatePromptRequestAction -> {
                if (shouldBlockPrompt(action, context)) {
                    return
                }
            }
            else -> {
                // no-op
            }
        }

        next(action)
    }

    private fun shouldBlockPrompt(
        action: ContentAction.UpdatePromptRequestAction,
        context: MiddlewareContext<BrowserState, BrowserAction>,
    ): Boolean {
        if (action.promptRequest is PromptRequest.Popup) {
            context.state.findTab(action.sessionId)?.let {
                if (it.content.promptRequests.lastOrNull { prompt -> prompt is PromptRequest.Popup } != null) {
                    scope.launch {
                        (action.promptRequest as PromptRequest.Popup).onDeny()
                    }
                    return true
                }
            }
        }
        return false
    }
}
