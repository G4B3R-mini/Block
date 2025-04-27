package com.shmibblez.inferno.browser.prompts.webPrompts.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.compose.base.InfernoText
import mozilla.components.concept.engine.prompt.PromptRequest

@Composable
fun ConfirmDialogPrompt(
    promptRequest: PromptRequest,
    title: String,
    body: String?,
    negativeLabel: String,
    positiveLabel: String,
    hasShownManyDialogs: Boolean,
    onCancel: (Boolean) -> Unit,
    onConfirm: () -> Unit,
) {
    if (promptRequest !is PromptRequest.BeforeUnload && promptRequest !is PromptRequest.Popup && promptRequest !is PromptRequest.Repost) {
        throw IllegalArgumentException("unrecognized prompt type")
    }
    var noMoreDialogs by remember { mutableStateOf(false) }
    PromptBottomSheetTemplate(
        onDismissRequest = { onCancel.invoke(noMoreDialogs) },
        negativeAction = PromptBottomSheetTemplateAction(
            text = negativeLabel,
            action = { onCancel.invoke(noMoreDialogs) },
        ),
        positiveAction = PromptBottomSheetTemplateAction(text = positiveLabel, action = onConfirm),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.BOTTOM,
    ) {
        // title
        InfernoText(
            text = title,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        // message
        if (body != null) InfernoText(
            text = body,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
        )
        // no more dialogs
        if (hasShownManyDialogs) {
            Row(
                modifier = Modifier
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