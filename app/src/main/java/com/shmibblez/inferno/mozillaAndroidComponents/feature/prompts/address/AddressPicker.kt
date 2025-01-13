/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.address

import com.shmibblez.inferno.mozillaAndroidComponents.browser.state.action.ContentAction
import com.shmibblez.inferno.mozillaAndroidComponents.browser.state.store.BrowserStore
import com.shmibblez.inferno.mozillaAndroidComponents.concept.engine.prompt.PromptRequest
import com.shmibblez.inferno.mozillaAndroidComponents.concept.storage.Address
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.concept.AutocompletePrompt
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.concept.SelectablePromptView
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.consumePromptFrom
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.facts.emitAddressAutofillDismissedFact
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.facts.emitAddressAutofillShownFact
import com.shmibblez.inferno.mozillaAndroidComponents.support.base.log.logger.Logger

/**
 * Interactor that implements [SelectablePromptView.Listener] and notifies the feature about actions
 * the user performed in the address picker.
 *
 * @property store The [BrowserStore] this feature should subscribe to.
 * @property addressSelectBar The [AutocompletePrompt] view into which the select address
 * prompt will be inflated.
 * @property onManageAddresses Callback invoked when user clicks on "Manage adresses" button from
 * select address prompt.
 * @property sessionId The session ID which requested the prompt.
 */
class AddressPicker(
    private val store: BrowserStore,
    private val addressSelectBar: AutocompletePrompt<Address>,
    private val onManageAddresses: () -> Unit = {},
    private var sessionId: String? = null,
) : SelectablePromptView.Listener<Address> {

    init {
        addressSelectBar.selectablePromptListener = this
    }

    /**
     * Shows the select address prompt in response to the [PromptRequest] event.
     *
     * @param request The [PromptRequest] containing the the address request data to be shown.
     */
    internal fun handleSelectAddressRequest(request: PromptRequest.SelectAddress) {
        emitAddressAutofillShownFact()
        addressSelectBar.showPrompt()
        addressSelectBar.populate(request.addresses)
    }

    /**
     * Dismisses the active [PromptRequest.SelectAddress] request.
     *
     * @param promptRequest The current active [PromptRequest.SelectAddress] or null
     * otherwise.
     */
    @Suppress("TooGenericExceptionCaught")
    fun dismissSelectAddressRequest(promptRequest: PromptRequest.SelectAddress? = null) {
        emitAddressAutofillDismissedFact()
        addressSelectBar.hidePrompt()

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

        addressSelectBar.hidePrompt()
    }

    override fun onManageOptions() {
        onManageAddresses.invoke()
        dismissSelectAddressRequest()
    }
}
