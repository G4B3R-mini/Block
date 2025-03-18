package com.shmibblez.inferno.browser.prompts.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.browser.prompts.onDismiss
import com.shmibblez.inferno.browser.prompts.onPositiveAction
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest

@Composable
fun PrivacyPolicyDialog(
    privacyPolicyData: PromptRequest.IdentityCredential.PrivacyPolicy,
    sessionId: String,
    title: String,
    message: String
) {
    val store = LocalContext.current.components.core.store
    PromptBottomSheetTemplate(
        onDismissRequest = {
            onDismiss(privacyPolicyData)
            store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, privacyPolicyData))
        },
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(R.string.mozac_feature_prompts_identity_credentials_cancel),
            action = {
                onPositiveAction(privacyPolicyData, false)
                store.dispatch(
                    ContentAction.ConsumePromptRequestAction(
                        sessionId,
                        privacyPolicyData
                    )
                )
            },
        ),
        positiveAction = PromptBottomSheetTemplateAction(
            text = stringResource(R.string.mozac_feature_prompts_identity_credentials_continue),
            action = {
                onPositiveAction(privacyPolicyData, true)
                store.dispatch(
                    ContentAction.ConsumePromptRequestAction(
                        sessionId,
                        privacyPolicyData
                    )
                )
            },
        ),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.BOTTOM,
    ) {
        InfernoText(
            text = title,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        InfernoText(
            text = message,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
        )
        InfernoText(
            text = linkifiedText(message),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
        )
    }
}

private fun linkifiedText(str: String): AnnotatedString {
    val urlRegex = Regex("\\b((https?://)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(:\\d+)?(/\\S*)?)\\b")
    val matches = urlRegex.findAll(str).map { it.range }
    var lastIntRangeIndex = 0
    return buildAnnotatedString {
        matches.forEach { intRange ->
            append(str.slice(IntRange(0, intRange.first - 1)))
            withLink(
                LinkAnnotation.Url(
                    url = if (str.slice(intRange)
                            .startsWith("www")
                    ) "https://" + str.slice(intRange) else str.slice(intRange),
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = Color.Blue, textDecoration = TextDecoration.Underline
                        )
                    )
                )
            ) {
                append(str.slice(intRange))
            }
            lastIntRangeIndex = intRange.last + 1
        }
        append(str.slice(IntRange(lastIntRangeIndex, str.length - 1)))
    }
}