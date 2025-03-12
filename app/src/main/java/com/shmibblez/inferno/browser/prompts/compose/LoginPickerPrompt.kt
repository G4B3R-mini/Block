package com.shmibblez.inferno.browser.prompts.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.onDismiss
import com.shmibblez.inferno.browser.prompts.onPositiveAction
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.storage.Login
import mozilla.components.feature.prompts.concept.SelectablePromptView
import mozilla.components.feature.prompts.login.LoginPicker
import mozilla.components.feature.prompts.login.LoginPickerColors

// todo: not tested, probably broken
@Composable
fun LoginPickerPrompt(loginData: PromptRequest.SelectLoginPrompt, sessionId: String) {
    val context = LocalContext.current
    val store = LocalContext.current.components.core.store
//    var logins by remember{mutableStateOf(listOf<Login>())}
    var isExpanded by remember { mutableStateOf(false) }
    val loginPickerColors = LoginPickerColors(context)

    var listener: SelectablePromptView.Listener<Login>? = null


    PromptBottomSheetTemplate(onDismissRequest = {
        onDismiss(loginData)
        store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, loginData))
    }) {
        LoginPicker(
            logins = loginData.logins,
            isExpanded = isExpanded,
            onExpandToggleClick = { isExpanded = it },
            onLoginSelected = {
                listener?.onOptionSelect(it)
                onPositiveAction(loginData, it)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, loginData))
            },
            onManagePasswordClicked = { listener?.onManageOptions() },
            loginPickerColors = loginPickerColors,
        )
    }
}