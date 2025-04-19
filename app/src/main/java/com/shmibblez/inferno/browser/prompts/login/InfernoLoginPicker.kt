package com.shmibblez.inferno.browser.prompts.login

import com.shmibblez.inferno.browser.prompts.concept.SelectablePromptState
import com.shmibblez.inferno.browser.prompts.consumePromptFrom
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.storage.Login
import mozilla.components.feature.prompts.login.LoginPicker
import mozilla.components.support.base.log.logger.Logger


/**
 * The [LoginPicker] displays a list of possible logins in a [SelectablePromptState] for a site after
 * receiving a [PromptRequest.SelectLoginPrompt] when a user clicks into a login field and we have
 * matching logins. It allows the user to select which one of these logins they would like to fill,
 * or select an option to manage their logins.
 *
 * @property store The [BrowserStore] this feature should subscribe to.
 * @property manageLoginsCallback A callback invoked when a user selects "manage logins" from the
 * select login prompt.
 * @property sessionId This is the id of the session which requested the prompt.
 */
class InfernoLoginPicker(
    private val store: BrowserStore,
    private val manageLoginsCallback: () -> Unit = {},
    private var sessionId: String? = null,
) : SelectablePromptState.Listener<Login> {

//    init {
//        loginSelectBar.listener = this
//    }

    internal fun handleSelectLoginRequest(request: PromptRequest.SelectLoginPrompt) {
//        emitLoginAutofillShownFact()
        // todo:
//        loginSelectBar.showPrompt(request.logins)
    }

    override fun onOptionSelect(option: Login) {
        store.consumePromptFrom<PromptRequest.SelectLoginPrompt>(sessionId) {
            it.onConfirm(option)
        }
//        emitLoginAutofillPerformedFact()
    // todo:
//        loginSelectBar.hidePrompt()
    }

    override fun onManageOptions() {
        manageLoginsCallback.invoke()
        dismissCurrentLoginSelect()
    }

    @Suppress("TooGenericExceptionCaught")
    fun dismissCurrentLoginSelect(promptRequest: PromptRequest.SelectLoginPrompt? = null) {
        try {
            if (promptRequest != null) {
                promptRequest.onDismiss()
                sessionId?.let {
                    store.dispatch(ContentAction.ConsumePromptRequestAction(it, promptRequest))
                }
            // todo:
//                loginSelectBar.hidePrompt()
                return
            }

            store.consumePromptFrom<PromptRequest.SelectLoginPrompt>(sessionId) {
                it.onDismiss()
            }
        } catch (e: RuntimeException) {
            Logger.error("Can't dismiss this login select prompt", e)
        }
//        emitLoginAutofillDismissedFact()
    // todo:
//        loginSelectBar.hidePrompt()
    }
}
