package com.shmibblez.inferno.browser.prompts.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shmibblez.inferno.browser.prompts.concept.PasswordPromptState
import com.shmibblez.inferno.browser.prompts.consumePromptFrom
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.support.base.log.logger.Logger


/**
 * Displays a [PasswordPromptView] for a site after receiving a [PromptRequest.SelectLoginPrompt]
 * when a user clicks into a login field and we don't have any matching logins. The user can receive
 * a suggestion for a strong password that can be used for filling in the password field.
 *
 * @property browserStore The [BrowserStore] this feature should subscribe to.
 * @property suggestStrongPasswordBar The view where the suggest strong password "prompt" will be inflated.
 * @property sessionId This is the id of the session which requested the prompt.
 */
class InfernoStrongPasswordPromptStateListener(
    private val browserStore: BrowserStore,
    private var sessionId: String? = null,
) : PasswordPromptState.Listener {

    var onGeneratedPasswordPromptClick: () -> Unit = { }

    var showStrongPasswordBar by mutableStateOf(false)
        private set

    /**
     * notifies dependent composable to not show strong password bar
     */
    internal fun showStrongPasswordBar() {
        showStrongPasswordBar = true
    }

    /**
     * notifies dependent composable to show strong password bar
     */
    internal fun hideStrongPasswordBar() {
        showStrongPasswordBar = false
    }

    @Suppress("TooGenericExceptionCaught")
    fun dismissCurrentSuggestStrongPassword(promptRequest: PromptRequest.SelectLoginPrompt? = null) {
        try {
            if (promptRequest != null) {
                promptRequest.onDismiss()
                sessionId?.let {
                    browserStore.dispatch(
                        ContentAction.ConsumePromptRequestAction(
                            it,
                            promptRequest,
                        ),
                    )
                }
                showStrongPasswordBar = false
                return
            }

            browserStore.consumePromptFrom<PromptRequest.SelectLoginPrompt>(sessionId) {
                it.onDismiss()
            }
        } catch (e: RuntimeException) {
            Logger.error("Can't dismiss this prompt", e)
        }

        showStrongPasswordBar = false
    }

    override fun onGeneratedPasswordPromptClick() {
        onGeneratedPasswordPromptClick.invoke()
    }
}
