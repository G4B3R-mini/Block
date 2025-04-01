package com.shmibblez.inferno.browser.prompts.webPrompts.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.browser.prompts.onDismiss
import com.shmibblez.inferno.browser.prompts.onPositiveAction
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest

@Composable
fun AuthenticationPrompt(authData: PromptRequest.Authentication, sessionId: String) {
    val store = LocalContext.current.components.core.store
    var username by remember { mutableStateOf(authData.userName) }
    var password by remember { mutableStateOf(authData.password) }
    PromptBottomSheetTemplate(
        onDismissRequest = {
            onDismiss(authData)
            store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, authData))
        },
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(R.string.mozac_feature_prompts_cancel),
            action = {
                onDismiss(authData)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, authData))
            },
        ),
        positiveAction = PromptBottomSheetTemplateAction(text = stringResource(android.R.string.ok),
            action = {
                onPositiveAction(authData, username, password)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, authData))
            }),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.BOTTOM
    ) {
        // title
        InfernoText(
            text = authData.title,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        // message
        InfernoText(
            text = authData.message,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        )
        if (!authData.onlyShowPassword)
            InfernoOutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                label = {
                    InfernoText(stringResource(R.string.mozac_feature_prompt_username_hint))
                },
            )
        InfernoOutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            label = {
                InfernoText(stringResource(R.string.mozac_feature_prompt_password_hint))
            },
        )
    }
}