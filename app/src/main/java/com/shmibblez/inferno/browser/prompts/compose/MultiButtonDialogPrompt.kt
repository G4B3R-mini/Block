package com.shmibblez.inferno.browser.prompts.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.browser.prompts.onDismiss
import com.shmibblez.inferno.browser.prompts.onNegativeAction
import com.shmibblez.inferno.browser.prompts.onNeutralAction
import com.shmibblez.inferno.browser.prompts.onPositiveAction
import com.shmibblez.inferno.compose.base.InfernoCheckbox
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest

@Composable
fun MultiButtonDialogPrompt(
    multiButtonData: PromptRequest.Confirm,
    sessionId: String,
    negativeButtonText: String,
    neutralButtonText: String,
    positiveButtonText: String
) {
    val store = LocalContext.current.components.core.store
    var noMoreDialogs by remember { mutableStateOf(false) }
    PromptBottomSheetTemplate(
        onDismissRequest = {
            onDismiss(multiButtonData)
            store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, multiButtonData))
        },
        negativeAction = PromptBottomSheetTemplateAction(
            text = negativeButtonText,
            action = {
                onNegativeAction(multiButtonData, noMoreDialogs)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, multiButtonData))
            },
        ),
        neutralAction = PromptBottomSheetTemplateAction(
            text = neutralButtonText,
            action = {
                onNeutralAction(multiButtonData, noMoreDialogs)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, multiButtonData))
            },
        ),
        positiveAction = PromptBottomSheetTemplateAction(
            text = positiveButtonText,
            action = {
                onPositiveAction(multiButtonData, noMoreDialogs)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, multiButtonData))
            },
        ),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.BOTTOM,
    ) {
        InfernoText(
            text = multiButtonData.title,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        if (multiButtonData.hasShownManyDialogs) {
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
                    text = multiButtonData.message,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1F),
                )
            }
        }
    }
}