package com.shmibblez.inferno.browser.prompts.address

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shmibblez.inferno.browser.prompts.concept.SelectablePromptState
import com.shmibblez.inferno.browser.prompts.consumePromptFrom
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.storage.Address
import mozilla.components.support.base.log.logger.Logger


/**
 * Interactor that implements [SelectablePromptState.Listener] and notifies the feature about actions
 * the user performed in the address picker.
 *
 * @property store The [BrowserStore] this feature should subscribe to.
 * @property onManageAddresses Callback invoked when user clicks on "Manage adresses" button from
 * select address prompt.
 * @property sessionId The session ID which requested the prompt.
 */
class InfernoAddressPicker(
    private val store: BrowserStore,
    private val onManageAddresses: () -> Unit = {},
    private var sessionId: String? = null,
) : SelectablePromptState.Listener<Address> {
    var dismissedSessionId by mutableStateOf<String?>(null)

//    init {
//        addressSelectBar.listener = this
//    }

//    /**
//     * Shows the select address prompt in response to the [PromptRequest] event.
//     *
//     * @param request The [PromptRequest] containing the the address request data to be shown.
//     */
//    internal fun handleSelectAddressRequest(request: PromptRequest.SelectAddress) {
////        emitAddressAutofillShownFact()
//        // todo:
////        addressSelectBar.showPrompt(request.addresses)
//    }

    /**
     * Dismisses the active [PromptRequest.SelectAddress] request.
     *
     * @param promptRequest The current active [PromptRequest.SelectAddress] or null
     * otherwise.
     */
    @Suppress("TooGenericExceptionCaught")
    fun dismissSelectAddressRequest(promptRequest: PromptRequest.SelectAddress? = null) {
//        emitAddressAutofillDismissedFact()

        try {
            if (promptRequest != null) {
                promptRequest.onDismiss()
                sessionId?.let {
                    store.dispatch(ContentAction.ConsumePromptRequestAction(it, promptRequest))
                }
                return
            }

            store.consumePromptFrom<PromptRequest.SelectAddress>(sessionId) {
                it.onDismiss()
            }
        } catch (e: RuntimeException) {
            Logger.error("Can't dismiss select address prompt", e)
        }
    }

    override fun onOptionSelect(option: Address) {
        store.consumePromptFrom<PromptRequest.SelectAddress>(sessionId) {
            it.onConfirm(option)
        }
    }

    override fun onManageOptions() {
        onManageAddresses.invoke()
        dismissSelectAddressRequest()
    }
}