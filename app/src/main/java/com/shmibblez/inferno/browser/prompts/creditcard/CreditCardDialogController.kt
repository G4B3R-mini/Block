package com.shmibblez.inferno.browser.prompts.creditcard

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shmibblez.inferno.browser.prompts.consumePromptFrom
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.storage.CreditCardEntry
import mozilla.components.feature.prompts.concept.SelectablePromptView
import mozilla.components.support.base.log.logger.Logger


/**
 * Interactor that implements [SelectablePromptView.Listener] and notifies the feature about actions
 * the user performed in the credit card picker.
 *
 * @property store The [BrowserStore] this feature should subscribe to.
 * @property manageCreditCardsCallback A callback invoked when a user selects "Manage credit cards"
 * from the select credit card prompt.
 * @property selectCreditCardCallback A callback invoked when a user selects a credit card option
 * from the select credit card prompt
 * @property sessionId The session ID which requested the prompt.
 */
class CreditCardDialogController(
    private val store: BrowserStore,
    private val manageCreditCardsCallback: () -> Unit = {},
    private val selectCreditCardCallback: () -> Unit = {},
    private var sessionId: String? = null,
) {

//    init {
//        creditCardSelectBar.listener = this
//    }

    // The selected credit card option to confirm.
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var selectedCreditCard by mutableStateOf<CreditCardEntry?>(null)

    fun onManageOptions() {
        manageCreditCardsCallback.invoke()
        dismissSelectCreditCardRequest()
    }

    fun onOptionSelect(option: CreditCardEntry) {
        selectedCreditCard = option
//        creditCardSelectBar.hidePrompt()
        selectCreditCardCallback.invoke()
    }

    /**
     * Called on a successful authentication to confirm the selected credit card option.
     */
    fun onAuthSuccess() {
        store.consumePromptFrom<PromptRequest.SelectCreditCard>(sessionId) {
            selectedCreditCard?.let { creditCard ->
                it.onConfirm(creditCard)
            }

            selectedCreditCard = null
        }
    }

    /**
     * Called on a failed authentication to dismiss the current select credit card prompt request.
     */
    fun onAuthFailure() {
        selectedCreditCard = null

        store.consumePromptFrom<PromptRequest.SelectCreditCard>(sessionId) {
            it.onDismiss()
        }
    }

    /**
     * Dismisses the active select credit card request.
     *
     * @param promptRequest The current active [PromptRequest.SelectCreditCard] or null
     * otherwise.
     */
    @Suppress("TooGenericExceptionCaught")
    fun dismissSelectCreditCardRequest(promptRequest: PromptRequest.SelectCreditCard? = null) {
//        emitCreditCardAutofillDismissedFact()
//        creditCardSelectBar.hidePrompt()

        try {
            if (promptRequest != null) {
                promptRequest.onDismiss()
                sessionId?.let {
                    store.dispatch(ContentAction.ConsumePromptRequestAction(it, promptRequest))
                }
                return
            }

            store.consumePromptFrom<PromptRequest.SelectCreditCard>(sessionId) {
                it.onDismiss()
            }
        } catch (e: RuntimeException) {
            Logger.error("Can't dismiss this select credit card prompt", e)
        }
    }

//    /**
//     * Shows the select credit card prompt in response to the [PromptRequest] event.
//     *
//     * @param request The [PromptRequest] containing the the credit card request data to be shown.
//     */
//    internal fun handleSelectCreditCardRequest(request: PromptRequest.SelectCreditCard) {
////        emitCreditCardAutofillShownFact()
//    // todo:
////        creditCardSelectBar.showPrompt(request.creditCards)
//    }
}
