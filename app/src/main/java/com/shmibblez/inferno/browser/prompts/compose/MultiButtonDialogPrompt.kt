package com.shmibblez.inferno.browser.prompts.compose

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
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest

@Composable
fun MultiButtonDialogPrompt(multiButtonData: PromptRequest.Confirm, sessionId: String, negativeButtonText: String, neutralButtonText: String,positiveButtonText: String) {
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
            }
        ),
        neutralAction = PromptBottomSheetTemplateAction(
            text = neutralButtonText,
            action = {
                onNeutralAction(multiButtonData, noMoreDialogs)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, multiButtonData))
            }
        ),
        positiveAction = PromptBottomSheetTemplateAction(
            text = negativeButtonText,
            action = {
                onPositiveAction(multiButtonData, noMoreDialogs)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, multiButtonData))
            }
        ),
    ) {
        InfernoText(
            text = multiButtonData.title,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(horizontal = 4.dp)
        )
        Row(modifier = Modifier.padding(horizontal = 4.dp)) {
            Checkbox(
                checked = noMoreDialogs,
                onCheckedChange = { noMoreDialogs = !noMoreDialogs },
            )
            InfernoText(
                text = multiButtonData.message,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(1F)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}