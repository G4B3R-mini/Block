package com.shmibblez.inferno.browser.prompts.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.browser.prompts.compose.sub.IdentityCredentialItem
import com.shmibblez.inferno.browser.prompts.onPositiveAction
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.identitycredential.Account
import mozilla.components.feature.prompts.identitycredential.DialogColors
import mozilla.components.support.ktx.kotlin.base64ToBitmap

@Composable
fun SelectAccountDialogPrompt(
    selectAccountData: PromptRequest.IdentityCredential.SelectAccount,
    sessionId: String,
) {
    val store = LocalContext.current.components.core.store
//    var selectedAccount by remember { mutableStateOf(selectAccountData.accounts[0]) }
    val colors = DialogColors.defaultProvider().provideColors()
    PromptBottomSheetTemplate(
        onDismissRequest = {
            selectAccountData.onDismiss.invoke()
            store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, selectAccountData))
        },
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(android.R.string.cancel),
            action = {
                selectAccountData.onDismiss.invoke()
                store.dispatch(
                    ContentAction.ConsumePromptRequestAction(
                        sessionId, selectAccountData
                    )
                )
            },
        ),
//        positiveAction = PromptBottomSheetTemplateAction(
//            text = stringResource(android.R.string.ok),
//            action = {
//                selectAccountData.onConfirm.invoke(selectedAccount)
//                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, selectAccountData))
//            },
//        ),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.TOP,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
        ) {
            selectAccountData.provider.icon?.base64ToBitmap()?.asImageBitmap()?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.size(16.dp),
                )
            }
            // title
            InfernoText(
                text = stringResource(
                    id = R.string.mozac_feature_prompts_identity_credentials_choose_account_for_provider,
                    selectAccountData.provider.name,
                ),
            )
        }
        // account list
        LazyColumn(
            modifier = Modifier.padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            items(selectAccountData.accounts) { account ->
                AccountItem(account = account, colors = colors, onClick = {
                    onPositiveAction(selectAccountData, it)
                    store.dispatch(
                        ContentAction.ConsumePromptRequestAction(
                            sessionId, selectAccountData
                        )
                    )
                })
            }
        }
    }
}

@Composable
private fun AccountItem(
    account: Account,
    modifier: Modifier = Modifier,
    colors: DialogColors = DialogColors.default(),
    onClick: (Account) -> Unit,
) {
    IdentityCredentialItem(
        title = account.name,
        description = account.email,
        colors = colors,
        modifier = modifier,
        onClick = { onClick(account) },
    ) {
        account.icon?.base64ToBitmap()?.asImageBitmap()?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(32.dp),
            )
        } ?: Spacer(
            Modifier
                .padding(horizontal = 16.dp)
                .size(32.dp),
        )
    }
}