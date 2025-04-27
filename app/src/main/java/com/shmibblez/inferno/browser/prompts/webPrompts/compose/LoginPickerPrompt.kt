package com.shmibblez.inferno.browser.prompts.webPrompts.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.login.SelectLoginPromptController
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.storage.Login
import mozilla.components.feature.prompts.concept.SelectablePromptView
import mozilla.components.feature.prompts.login.LoginPicker
import mozilla.components.feature.prompts.login.LoginPickerColors

// todo: not tested, probably broken
@Composable
fun LoginPickerPrompt(
    promptRequest: PromptRequest.SelectLoginPrompt,
    controller: SelectLoginPromptController.LoginPickerDialog,
    onCancel: () -> Unit,
    onConfirm: (Login) -> Unit,
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    val loginPickerColors = LoginPickerColors(context)

    PromptBottomSheetTemplate(onDismissRequest = onCancel) {
        LoginPicker(
            logins = promptRequest.logins,
            isExpanded = isExpanded,
            onExpandToggleClick = { isExpanded = it },
            onLoginSelected = onConfirm,
            onManagePasswordClicked = { controller.onManageOptions() },
            loginPickerColors = loginPickerColors,
        )
    }
}