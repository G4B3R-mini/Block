package com.shmibblez.inferno.browser.prompts

import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.storage.CreditCardValidationDelegate
import mozilla.components.concept.storage.LoginValidationDelegate
import mozilla.components.feature.prompts.login.LoginExceptions

internal interface InfernoPrompter {

    /**
     * Validates whether or not a given [CreditCard] may be stored.
     */
    val creditCardValidationDelegate: CreditCardValidationDelegate?

    /**
     * Validates whether or not a given Login may be stored.
     *
     * Logging in will not prompt a save dialog if this is left null.
     */
    val loginValidationDelegate: LoginValidationDelegate?

    /**
     * Stores whether a site should never be prompted for logins saving.
     */
    val loginExceptionStorage: LoginExceptions?

    /**
     * Invoked when a dialog is dismissed. This consumes the [PromptRequest] indicated by [promptRequestUID]
     * from the session indicated by [sessionId].
     *
     * @param sessionId this is the id of the session which requested the prompt.
     * @param promptRequestUID id of the [PromptRequest] for which this dialog was shown.
     * @param value an optional value provided by the dialog as a result of cancelling the action.
     */
    fun onCancel(sessionId: String, promptRequestUID: String, value: Any? = null)

    /**
     * Invoked when the user confirms the action on the dialog. This consumes the [PromptRequest] indicated
     * by [promptRequestUID] from the session indicated by [sessionId].
     *
     * @param sessionId that requested to show the dialog.
     * @param promptRequestUID id of the [PromptRequest] for which this dialog was shown.
     * @param value an optional value provided by the dialog as a result of confirming the action.
     */
    fun onConfirm(sessionId: String, promptRequestUID: String, value: Any? = null)

    /**
     * Invoked when the user is requesting to clear the selected value from the dialog.
     * This consumes the [PromptFeature] value from the session indicated by [sessionId].
     *
     * @param sessionId that requested to show the dialog.
     * @param promptRequestUID id of the [PromptRequest] for which this dialog was shown.
     */
    fun onClear(sessionId: String, promptRequestUID: String)

    /**
     * Invoked when the user is requesting to open a website from the dialog.
     *
     * @param url The url to be opened.
     */
    fun onOpenLink(url: String)
}