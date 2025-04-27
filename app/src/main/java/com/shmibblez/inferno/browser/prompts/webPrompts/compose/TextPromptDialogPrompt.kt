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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoText
import mozilla.components.concept.engine.prompt.PromptRequest

@Composable
fun TextPromptDialogPrompt(
    promptRequest: PromptRequest.TextPrompt,
    hasShownManyDialogs: Boolean,
    onCancel: () -> Unit,
    onConfirm: (Pair<Boolean, String>) -> Unit,
) {
    var noMoreDialogs by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(promptRequest.inputValue) }

    PromptBottomSheetTemplate(
        onDismissRequest = onCancel,
        positiveAction = PromptBottomSheetTemplateAction(
            text = stringResource(android.R.string.ok),
            action = {
                onConfirm(noMoreDialogs to text)
            },
        ),
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(android.R.string.cancel),
            action = onCancel,
        ),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.BOTTOM,
    ) {
        // label
        InfernoText(
            promptRequest.title,
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
            label = { InfernoText(text = promptRequest.title) },
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