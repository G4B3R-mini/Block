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
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest

@Composable
fun AuthenticationPrompt(
    promptRequest: PromptRequest.Authentication,
    onCancel: () -> Unit,
    onConfirm: (Pair<String, String>) -> Unit,
) {
    var username by remember { mutableStateOf(promptRequest.userName) }
    var password by remember { mutableStateOf(promptRequest.password) }
    PromptBottomSheetTemplate(
        onDismissRequest = onCancel,
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(R.string.mozac_feature_prompts_cancel),
            action = onCancel,
        ),
        positiveAction = PromptBottomSheetTemplateAction(text = stringResource(android.R.string.ok),
            action = {
                onConfirm(username to password)
            }),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.BOTTOM
    ) {
        // title
        InfernoText(
            text = promptRequest.title,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        // message
        InfernoText(
            text = promptRequest.message,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        )
        if (!promptRequest.onlyShowPassword)
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