package com.shmibblez.inferno.browser.prompts.webPrompts.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.compose.base.InfernoCheckbox
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest

@Composable
fun AlertDialogPrompt(
    promptRequest: PromptRequest.Alert,
    onCancel: () -> Unit,
    onConfirm: (Boolean) -> Unit,
) {
    var noMoreDialogs by remember { mutableStateOf(false) }
    PromptBottomSheetTemplate(
        onDismissRequest = onCancel,
        positiveAction = PromptBottomSheetTemplateAction(
            text = stringResource(android.R.string.ok),
            action = {
                onConfirm.invoke(noMoreDialogs)
            },
        ),
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(android.R.string.cancel),
            action = onCancel,
        ),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.BOTTOM
    ) {
        InfernoText(
            text = promptRequest.title,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(horizontal = 16.dp),
            fontWeight = FontWeight.Bold,
        )
        if (promptRequest.hasShownManyDialogs) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
            ) {
                InfernoCheckbox(
                    modifier = Modifier.padding(0.dp),
                    checked = noMoreDialogs,
                    onCheckedChange = { noMoreDialogs = !noMoreDialogs },
                )
                InfernoText(
                    text = promptRequest.message,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .weight(1F)
                        .padding(horizontal = 4.dp),
                )
            }
        }
    }
}