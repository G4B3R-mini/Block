package com.shmibblez.inferno.browser.prompts.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
//        negativeAction = PromptBottomSheetTemplateAction(
//            text = stringResource(android.R.string.cancel),
//            action = {
//                selectAccountData.onDismiss.invoke()
//                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, selectAccountData))
//            },
//        ),
//        positiveAction = PromptBottomSheetTemplateAction(
//            text = stringResource(android.R.string.ok),
//            action = {
//                selectAccountData.onConfirm.invoke(selectedAccount)
//                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, selectAccountData))
//            },
//        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            selectAccountData.provider.icon?.base64ToBitmap()?.asImageBitmap()?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.size(16.dp),
                )

                Spacer(Modifier.width(4.dp))
            }

            InfernoText(
                text = stringResource(
                    id = R.string.mozac_feature_prompts_identity_credentials_choose_account_for_provider,
                    selectAccountData.provider.name,
                ),
            )
        }

        selectAccountData.accounts.forEach { account ->
            AccountItem(account = account, colors = colors, onClick = {
                onPositiveAction(selectAccountData, account)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, selectAccountData))
            })
        }

        Spacer(modifier = Modifier.height(16.dp))
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
                .width(32.dp),
        )
    }
}