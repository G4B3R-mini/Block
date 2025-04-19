package com.shmibblez.inferno.browser.prompts.login

import com.shmibblez.inferno.browser.prompts.consumePromptFrom
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.storage.Login
import mozilla.components.support.base.log.logger.Logger

open class SelectLoginPromptController {

    fun isLoginPickerDialog() = this is LoginPickerDialog
    fun isPasswordGeneratorDialog() = this is PasswordGeneratorDialog
    fun isStrongPasswordBarDialog() = this is StrongPasswordBarDialog

    data class LoginPickerDialog(
        private val store: BrowserStore,
        private val manageLoginsCallback: () -> Unit = {},
        private var sessionId: String? = null,
    ): SelectLoginPromptController() {
        fun onOptionSelect(option: Login) {
            store.consumePromptFrom<PromptRequest.SelectLoginPrompt>(sessionId) {
                it.onConfirm(option)
            }
//        emitLoginAutofillPerformedFact()
            // todo:
//        loginSelectBar.hidePrompt()
        }

        fun onManageOptions() {
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
        }
    }

    data object PasswordGeneratorDialog : SelectLoginPromptController()

    data class StrongPasswordBarDialog(
        private val browserStore: BrowserStore,
        private var sessionId: String? = null,
        private var onGeneratedPasswordPromptClick: () -> Unit = { },
    ) : SelectLoginPromptController() {

        fun onGeneratedPasswordPromptClick() {
            onGeneratedPasswordPromptClick.invoke()
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
                    return
                }

                browserStore.consumePromptFrom<PromptRequest.SelectLoginPrompt>(sessionId) {
                    it.onDismiss()
                }
            } catch (e: RuntimeException) {
                Logger.error("Can't dismiss this prompt", e)
            }
        }
    }
}