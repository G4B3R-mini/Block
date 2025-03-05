package com.shmibblez.inferno.browser.prompts.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.feature.prompts.concept.PasswordPromptView
import mozilla.components.feature.prompts.login.PasswordGeneratorPromptColors

// todo: not tested
@Composable
fun GeneratePasswordPrompt(
    loginData: PromptRequest.SelectLoginPrompt,
    sessionId: String,
    onGeneratePassword: @Composable () -> Unit
) {
    val store = LocalContext.current.components.core.store

    val listener: PasswordPromptView.Listener? = null
    val colors = PasswordGeneratorPromptColors(LocalContext.current)
    var showPasswordGenerator by remember { mutableStateOf(false) }

    PromptBottomSheetTemplate(
        onDismissRequest = {
            onDismiss(loginData)
            store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, loginData))
        },
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(android.R.string.cancel),
            action = {
                onNegativeAction(loginData)
            }
        )
    ) {
        if (showPasswordGenerator) {
            mozilla.components.feature.prompts.login.PasswordGeneratorPrompt(
                onGeneratedPasswordPromptClick = {
                    listener?.onGeneratedPasswordPromptClick()
                    showPasswordGenerator = true
                },
                colors = colors,
            )
        } else {
            onGeneratePassword.invoke()
        }
    }
}