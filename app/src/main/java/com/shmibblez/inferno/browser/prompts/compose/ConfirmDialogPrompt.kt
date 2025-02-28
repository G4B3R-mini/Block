package com.shmibblez.inferno.browser.prompts.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest

@Composable
fun ConfirmDialogPrompt(
    confirmData: PromptRequest,
    sessionId: String,
    title: String,
    body: String,
    leaveLabel: String,
    stayLabel: String,
) {
    if (confirmData !is PromptRequest.BeforeUnload && confirmData !is PromptRequest.Popup && confirmData !is PromptRequest.Repost) {
        throw IllegalArgumentException("unrecognized prompt type")
    }
    val store = LocalContext.current.components.core.store
    PromptBottomSheetTemplate(onDismissRequest = {
        onDismiss(confirmData)
        store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, confirmData))
    }, negativeAction = PromptBottomSheetTemplateAction(text = leaveLabel, action = {
        onNegativeAction(confirmData) // onLeave
        store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, confirmData))
    }), positiveAction = PromptBottomSheetTemplateAction(text = stayLabel, action = {
        onPositiveAction(confirmData)
        store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, confirmData))
    })) {
        // title
        InfernoText(
            text = title,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        // message
        InfernoText(
            text = body, textAlign = TextAlign.Start, modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}