package com.shmibblez.inferno.browser.prompts.creditcard

import androidx.compose.runtime.Composable
import com.shmibblez.inferno.browser.prompts.InfernoPromptFeatureState
import mozilla.components.feature.prompts.creditcard.CreditCardPicker

/**
 * Delegate for credit card picker and related callbacks
 */
interface InfernoCreditCardDelegate {
    /**
     * The [Composable] used for [CreditCardPicker] to display a
     * selectable prompt list of credit cards.
     */
    val creditCardPickerComposable: (@Composable (InfernoPromptFeatureState) -> Unit)?
        get() = null // todo: override in BrowserComponent, call in InfernoWebPrompter, if null use default

    /**
     * Callback invoked when a user selects "Manage credit cards"
     * from the select credit card prompt.
     */
    val onManageCreditCards: () -> Unit
        get() = {}

    /**
     * Callback invoked when a user selects a credit card option
     * from the select credit card prompt
     */
    val onSelectCreditCard: () -> Unit
        get() = {}
}