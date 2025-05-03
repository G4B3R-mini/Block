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
        get() = null

    /**
     * The [Composable] used when biometric authentication not available to authenticate
     * [CreditCardPicker] selection
     */
    val pinDialogWarningComposable: (@Composable (InfernoPromptFeatureState) -> Unit)?
        get() = null

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