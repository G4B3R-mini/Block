package com.shmibblez.inferno.browser.prompts.webPrompts.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.compose.base.InfernoCheckbox
import com.shmibblez.inferno.compose.base.InfernoText
import mozilla.components.concept.engine.prompt.PromptRequest

enum class MultiButtonDialogButtonType {
    POSITIVE, NEUTRAL, NEGATIVE
}

@Composable
fun MultiButtonDialogPrompt(
    promptRequest: PromptRequest.Confirm,
    negativeButtonText: String,
    neutralButtonText: String,
    positiveButtonText: String,
    onCancel: () -> Unit,
    onConfirm: (Pair<Boolean, MultiButtonDialogButtonType>) -> Unit,
) {
    var noMoreDialogs by remember { mutableStateOf(false) }
    PromptBottomSheetTemplate(
        onDismissRequest = {
            onCancel.invoke()
        },
        negativeAction = PromptBottomSheetTemplateAction(
            text = negativeButtonText,
            action = {
                onConfirm(noMoreDialogs to MultiButtonDialogButtonType.NEGATIVE)
            },
        ),
        neutralAction = PromptBottomSheetTemplateAction(
            text = neutralButtonText,
            action = {
                onConfirm(noMoreDialogs to MultiButtonDialogButtonType.NEUTRAL)
            },
        ),
        positiveAction = PromptBottomSheetTemplateAction(
            text = positiveButtonText,
            action = {
                onConfirm(noMoreDialogs to MultiButtonDialogButtonType.POSITIVE)
            },
        ),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.BOTTOM,
    ) {
        InfernoText(
            text = promptRequest.title,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        if (promptRequest.hasShownManyDialogs) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InfernoCheckbox(
                    checked = noMoreDialogs,
                    onCheckedChange = { noMoreDialogs = !noMoreDialogs },
                )
                InfernoText(
                    text = promptRequest.message,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1F),
                )
            }
        }
    }
}