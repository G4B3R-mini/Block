package com.shmibblez.inferno.browser.prompts.compose

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.browser.prompts.onDismiss
import com.shmibblez.inferno.browser.prompts.onNegativeAction
import com.shmibblez.inferno.browser.prompts.onPositiveAction
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.facts.emitCreditCardAutofillCreatedFact
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.facts.emitCreditCardAutofillUpdatedFact
import kotlinx.coroutines.launch
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.storage.CreditCardValidationDelegate.Result
import mozilla.components.service.sync.autofill.DefaultCreditCardValidationDelegate
import mozilla.components.support.ktx.android.content.appName
import mozilla.components.support.utils.creditCardIssuerNetwork

@Composable
fun CreditCardSaveDialogPrompt(saveData: PromptRequest.SaveCreditCard, sessionId: String) {
    val store = LocalContext.current.components.core.store
    val context = LocalContext.current
    var confirmResult by remember { mutableStateOf<Result?>(null) }
    LaunchedEffect(null) {
        this.launch {
            val validationDelegate = DefaultCreditCardValidationDelegate(
                context.components.core.lazyAutofillStorage,
            )
            confirmResult = validationDelegate.shouldCreateOrUpdate(saveData.creditCard)
        }
    }
    PromptBottomSheetTemplate(
        onDismissRequest = {
            onDismiss(saveData)
            store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, saveData))
        },
        negativeAction = PromptBottomSheetTemplateAction(
            text = when (confirmResult) {
                is Result.CanBeCreated -> stringResource(R.string.mozac_feature_prompt_not_now)
                is Result.CanBeUpdated -> stringResource(R.string.mozac_feature_prompts_cancel)
                else -> stringResource(R.string.mozac_feature_prompt_not_now)
            },
            action = {
                onNegativeAction(saveData)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, saveData))
            },
        ),
        positiveAction = PromptBottomSheetTemplateAction(
            text = when (confirmResult) {
                is Result.CanBeCreated -> stringResource(R.string.mozac_feature_prompt_save_confirmation)
                is Result.CanBeUpdated -> stringResource(R.string.mozac_feature_prompt_update_confirmation)
                else -> stringResource(R.string.mozac_feature_prompt_save_confirmation)
            },
            action = {
                onPositiveAction(saveData, saveData.creditCard)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, saveData))
                emitSaveUpdateFact(confirmResult)
            },
        ),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.BOTTOM,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // todo: tint should be text color primary
            // lock icon
            Icon(
                painter = painterResource(R.drawable.mozac_ic_lock_24),
                tint = Color.White,
                contentDescription = "secure lock",
                modifier = Modifier.size(24.dp)
            )
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1F),
                horizontalAlignment = Alignment.Start
            ) {
                // save credit card header
                InfernoText(
                    text = when (confirmResult) {
                        is Result.CanBeCreated -> stringResource(R.string.mozac_feature_prompts_save_credit_card_prompt_title)
                        is Result.CanBeUpdated -> stringResource(R.string.mozac_feature_prompts_update_credit_card_prompt_title)
                        else -> stringResource(R.string.mozac_feature_prompts_save_credit_card_prompt_title)
                    },
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                // save credit card message
                InfernoText(
                    text = when (confirmResult) {
                        is Result.CanBeCreated -> stringResource(R.string.mozac_feature_prompts_save_credit_card_prompt_body_2, context.appName)
                        is Result.CanBeUpdated -> ""
                        else -> ""
                    },
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                ) {
                    // card type
                    Icon(
                        painter = painterResource(saveData.creditCard.cardType.creditCardIssuerNetwork().icon),
                        tint = Color.White,
                        contentDescription = "card type",
                        modifier = Modifier.size(40.dp),
                    )
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                    ) {
                        // credit card number
                        InfernoText(
                            text = saveData.creditCard.obfuscatedCardNumber,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 16.dp, end = 48.dp),
                        )
                        // card expiration date
                        InfernoText(
                            text = saveData.creditCard.expiryDate,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 16.dp, end = 48.dp),
                        )
                    }
                }
            }
        }
    }
}


/**
 * Emit the save or update fact based on the confirm action for the credit card.
 */
@VisibleForTesting
internal fun emitSaveUpdateFact(confirmResult: Result?) {
    when (confirmResult) {
        is Result.CanBeCreated -> {
            emitCreditCardAutofillCreatedFact()
        }

        is Result.CanBeUpdated -> {
            emitCreditCardAutofillUpdatedFact()
        }

        null -> {}
    }
}