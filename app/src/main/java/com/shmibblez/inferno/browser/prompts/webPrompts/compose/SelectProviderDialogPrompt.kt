package com.shmibblez.inferno.browser.prompts.webPrompts.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.compose.base.InfernoText
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.identitycredential.Provider
import mozilla.components.feature.prompts.identitycredential.DialogColors
import mozilla.components.support.ktx.kotlin.base64ToBitmap
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.browser.prompts.onDismiss
import com.shmibblez.inferno.browser.prompts.onPositiveAction
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.sub.IdentityCredentialItem
import com.shmibblez.inferno.ext.components

@Composable
fun SelectProviderDialogPrompt(
    selectData: PromptRequest.IdentityCredential.SelectProvider,
    sessionId: String,
) {
    val colors = DialogColors.defaultProvider().provideColors()
    val store = LocalContext.current.components.core.store
    PromptBottomSheetTemplate(
        onDismissRequest = {
            onDismiss(selectData)
            store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, selectData))
        },
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(android.R.string.cancel),
            action = {
                onDismiss(selectData)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, selectData))
            }
        ),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.TOP
    ) {
        InfernoText(
            text = stringResource(id = R.string.mozac_feature_prompts_identity_credentials_choose_provider),
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = colors.title,
                letterSpacing = 0.15.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyColumn(
            modifier = Modifier.padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            items(selectData.providers) { provider ->
                ProviderItem(provider = provider, onClick = {
                    onPositiveAction(selectData, it)
                    store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, selectData))
                }, colors = colors)
            }
        }
    }
}

@Composable
private fun ProviderItem(
    provider: Provider,
    modifier: Modifier = Modifier,
    colors: DialogColors = DialogColors.default(),
    onClick: (Provider) -> Unit,
) {
    IdentityCredentialItem(
        title = provider.name,
        description = provider.domain,
        modifier = modifier,
        colors = colors,
        onClick = { onClick(provider) },
    ) {
        provider.icon?.base64ToBitmap()?.asImageBitmap()?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(24.dp),
            )
        } ?: Spacer(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .width(24.dp),
        )
    }
}