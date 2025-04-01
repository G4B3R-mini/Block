package com.shmibblez.inferno.browser.prompts.webPrompts.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.browser.prompts.onDismiss
import com.shmibblez.inferno.browser.prompts.onNegativeAction
import com.shmibblez.inferno.browser.prompts.onPositiveAction
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import mozilla.components.support.ktx.util.PromptAbuserDetector

@Composable
fun TextPromptDialogPrompt(
    textData: PromptRequest.TextPrompt,
    sessionId: String,
    hasShownManyDialogs: Boolean,
    promptAbuserDetector: PromptAbuserDetector
) {
    val store = LocalContext.current.components.core.store
    var noMoreDialogs by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(textData.inputValue) }

    PromptBottomSheetTemplate(
        onDismissRequest = {
            onDismiss(textData)
            store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, textData))
        },
        positiveAction = PromptBottomSheetTemplateAction(
            text = stringResource(android.R.string.ok),
            action = {
                onPositiveAction(textData, noMoreDialogs, text)
                promptAbuserDetector.userWantsMoreDialogs(!noMoreDialogs)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, textData))
            },
        ),
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(android.R.string.cancel),
            action = {
                onNegativeAction(textData)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, textData))
            },
        ),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.BOTTOM,
    ) {
        // label
        InfernoText(
            textData.title,
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 16.sp,
        )
        InfernoOutlinedTextField(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            value = text,
            onValueChange = {
                text = it
            },
            label = { InfernoText(text = textData.title) },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 16.sp,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
            singleLine = true,
        )
        if (hasShownManyDialogs) {
            Row(modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            ) {
                Checkbox(
                    checked = noMoreDialogs,
                    onCheckedChange = { noMoreDialogs = !noMoreDialogs },
                )
                InfernoText(
                    text = stringResource(R.string.mozac_feature_prompts_no_more_dialogs),
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .weight(1F)
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}