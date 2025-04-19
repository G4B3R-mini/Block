package com.shmibblez.inferno.browser.prompts.login

import mozilla.components.concept.storage.Login
import mozilla.components.feature.prompts.concept.SelectablePromptView
import mozilla.components.feature.prompts.login.LoginPicker

/**
 * Delegate to display the login select prompt and related callbacks
 */
interface InfernoLoginDelegate {
//    /**
//     * The [SelectablePromptView] used for [LoginPicker] to display a
//     * selectable prompt list of logins.
//     */
//    val loginPickerView: SelectablePromptView<Login>?
//        get() = null

    /**
     * Callback invoked when a user selects "Manage logins"
     * from the select login prompt.
     */
    val onManageLogins: () -> Unit
        get() = {}
}