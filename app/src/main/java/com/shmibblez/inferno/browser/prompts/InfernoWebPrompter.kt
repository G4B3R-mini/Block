package com.shmibblez.inferno.browser.prompts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.login.SelectLoginPromptController
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.AlertDialogPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.AuthenticationPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.ChoiceDialogPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.ColorPickerDialogPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.ConfirmDialogPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.CreditCardSaveDialogPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.LoginPickerPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.MENU_CHOICE_DIALOG_TYPE
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.MULTIPLE_CHOICE_DIALOG_TYPE
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.MultiButtonDialogPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.PasswordGeneratorDialogPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.PrivacyPolicyDialog
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.SELECTION_TYPE_DATE
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.SELECTION_TYPE_DATE_AND_TIME
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.SELECTION_TYPE_MONTH
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.SELECTION_TYPE_TIME
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.SINGLE_CHOICE_DIALOG_TYPE
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.SaveLoginDialogPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.SelectAccountDialogPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.SelectProviderDialogPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.SelectableListPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.TextPromptDialogPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.TimeSelectionPrompt
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.selector.findTabOrCustomTabOrSelectedTab
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.prompt.Choice
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.engine.prompt.PromptRequest.Alert
import mozilla.components.concept.engine.prompt.PromptRequest.Authentication
import mozilla.components.concept.engine.prompt.PromptRequest.BeforeUnload
import mozilla.components.concept.engine.prompt.PromptRequest.Confirm
import mozilla.components.concept.engine.prompt.PromptRequest.File
import mozilla.components.concept.engine.prompt.PromptRequest.IdentityCredential
import mozilla.components.concept.engine.prompt.PromptRequest.MenuChoice
import mozilla.components.concept.engine.prompt.PromptRequest.MultipleChoice
import mozilla.components.concept.engine.prompt.PromptRequest.Popup
import mozilla.components.concept.engine.prompt.PromptRequest.Repost
import mozilla.components.concept.engine.prompt.PromptRequest.SaveCreditCard
import mozilla.components.concept.engine.prompt.PromptRequest.SaveLoginPrompt
import mozilla.components.concept.engine.prompt.PromptRequest.SelectAddress
import mozilla.components.concept.engine.prompt.PromptRequest.SelectCreditCard
import mozilla.components.concept.engine.prompt.PromptRequest.SelectLoginPrompt
import mozilla.components.concept.engine.prompt.PromptRequest.Share
import mozilla.components.concept.engine.prompt.PromptRequest.SingleChoice
import mozilla.components.concept.engine.prompt.PromptRequest.TextPrompt
import mozilla.components.concept.engine.prompt.PromptRequest.TimeSelection
import mozilla.components.concept.identitycredential.Account
import mozilla.components.concept.identitycredential.Provider
import mozilla.components.concept.storage.Address
import mozilla.components.concept.storage.CreditCardEntry
import mozilla.components.concept.storage.Login
import mozilla.components.concept.storage.LoginEntry
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.kotlin.ifNullOrEmpty
import mozilla.components.support.ktx.util.PromptAbuserDetector
import java.security.InvalidParameterException
import java.util.Date

// todo:
//   - colors
//   - some prompts not working nicely, maybe not dismissing properly
//   - move file picker implementation here, make clean with callbacks and such
//   - implement share dialog in compose
//   - some minor problems in some components, go to ones with todos in their files
//   - check usage of parameters that are not in use for [PromptComponent] (commented out)
//
// todo:
//   - change name of InfernoStrongPasswordPromptStateListener, not very fitting, name should
//      include something like controller

/**
 * replaces prompt integration
 * shows modal bottom sheet with requested prompt inside
 */
@Composable
fun InfernoWebPrompter(
    state: InfernoPromptFeatureState,
    currentTab: TabSessionState?,
) {
    if (currentTab == null) return
    val context = LocalContext.current
    val logger = remember { Logger("Prompt Component") }
//    var promptRequest by remember { mutableStateOf<PromptRequest?>(null) }
    val sessionId = currentTab.id
    val promptAbuserDetector = remember { PromptAbuserDetector() }
    val store = LocalContext.current.components.core.store

    val prompt = state.visiblePrompt

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // display prompt depending on type
        when (prompt) {
            is Alert -> AlertDialogPrompt(prompt, sessionId, promptAbuserDetector)

            is Authentication -> AuthenticationPrompt(prompt, sessionId)

            is BeforeUnload -> {
                val title = stringResource(R.string.mozac_feature_prompt_before_unload_dialog_title)
                val body = stringResource(R.string.mozac_feature_prompt_before_unload_dialog_body)
                val leaveLabel = stringResource(R.string.mozac_feature_prompts_before_unload_leave)
                val stayLabel = stringResource(R.string.mozac_feature_prompts_before_unload_stay)
                ConfirmDialogPrompt(
                    prompt, sessionId, title, body, stayLabel, leaveLabel, promptAbuserDetector
                )
            }

            is PromptRequest.Color -> ColorPickerDialogPrompt(prompt, sessionId)

            is Confirm -> {
                val positiveButton =
                    prompt.positiveButtonTitle.ifEmpty { stringResource(R.string.mozac_feature_prompts_ok) }
                val negativeButton =
                    prompt.negativeButtonTitle.ifEmpty { stringResource(R.string.mozac_feature_prompts_cancel) }
                val neutralButton =
                    prompt.neutralButtonTitle.ifEmpty { stringResource(R.string.mozac_feature_prompts_suggest_strong_password_dismiss) }
                MultiButtonDialogPrompt(
                    prompt, sessionId, negativeButton, neutralButton, positiveButton
                )
            }

            is IdentityCredential.PrivacyPolicy -> {
                val title = context.getString(
                    R.string.mozac_feature_prompts_identity_credentials_privacy_policy_title,
                    prompt.providerDomain,
                )
                val message = context.getString(
                    R.string.mozac_feature_prompts_identity_credentials_privacy_policy_description,
                    prompt.host,
                    prompt.providerDomain,
                    prompt.privacyPolicyUrl,
                    prompt.termsOfServiceUrl,
                )
                PrivacyPolicyDialog(prompt, sessionId, title, message)
            }

            is IdentityCredential.SelectAccount -> SelectAccountDialogPrompt(prompt, sessionId)

            is IdentityCredential.SelectProvider -> SelectProviderDialogPrompt(prompt, sessionId)

            is MenuChoice -> ChoiceDialogPrompt(prompt, sessionId, MENU_CHOICE_DIALOG_TYPE)

            is MultipleChoice -> ChoiceDialogPrompt(prompt, sessionId, MULTIPLE_CHOICE_DIALOG_TYPE)

            is Popup -> {
                val title = context.getString(R.string.mozac_feature_prompts_popup_dialog_title)
                val positiveLabel = context.getString(R.string.mozac_feature_prompts_allow)
                val negativeLabel = context.getString(R.string.mozac_feature_prompts_deny)

                ConfirmDialogPrompt(
                    prompt,
                    sessionId,
                    title,
                    null,
                    negativeLabel,
                    positiveLabel,
                    promptAbuserDetector
                )
            }

            is Repost -> {
                val title = stringResource(R.string.mozac_feature_prompt_repost_title)
                val message = stringResource(R.string.mozac_feature_prompt_repost_message)
                val positiveAction =
                    stringResource(R.string.mozac_feature_prompt_repost_positive_button_text)
                val negativeAction =
                    stringResource(R.string.mozac_feature_prompt_repost_negative_button_text)

                ConfirmDialogPrompt(
                    prompt,
                    sessionId,
                    title,
                    message,
                    negativeAction,
                    positiveAction,
                    promptAbuserDetector
                )
            }

            is SaveCreditCard -> CreditCardSaveDialogPrompt(prompt, sessionId)

            is SaveLoginPrompt -> SaveLoginDialogPrompt(
                loginData = prompt,
                sessionId = sessionId,
                icon = currentTab.content.icon,
                onShowSnackbarAfterLoginChange = state.onSaveLogin,
                loginValidationDelegate = state.loginValidationDelegate,
            )

            is SelectAddress -> SelectableListPrompt(
                prompt,
                sessionId,
                header = stringResource(R.string.mozac_feature_prompts_select_address_2),
                manageText = stringResource(R.string.mozac_feature_prompts_manage_address),
            )


            is SelectCreditCard -> SelectableListPrompt(
                prompt,
                sessionId,
                header = stringResource(R.string.mozac_feature_prompts_select_credit_card_2),
                manageText = stringResource(R.string.mozac_feature_prompts_manage_credit_cards_2),
            )

            is SelectLoginPrompt -> {
//                val generatedPassword = prompt.generatedPassword
//                if (generatedPassword == null || currentUrl == null) {

                when (state.selectLoginPromptController) {
                    is SelectLoginPromptController.LoginPickerDialog -> LoginPickerPrompt(
                        state.strongPasswordPromptStateListener.showStrongPasswordBar,
                        prompt,
                        sessionId,
                        currentUrl = state.currentUrl ?: "<empty>",
                        onSavedGeneratedPassword = state.onSaveLogin,
                    )

                    is SelectLoginPromptController.StrongPasswordBarDialog -> {
                        // todo
                    }

                    is SelectLoginPromptController.PasswordGeneratorDialog -> {
                        // todo
                    }
                }

                // todo: when to show?
//                val currentUrl =
//                    store.state.findTabOrCustomTabOrSelectedTab(customTabId)?.content?.url
//                           // val generatedPassword = prompt.generatedPassword
//                           // if (generatedPassword == null || currentUrl == null) {
//                if (currentUrl == null) {
//                    logger.debug(
//                        "Ignoring received SelectLogin.onGeneratedPasswordPromptClick" + " when either the generated password or the currentUrl is null.",
//                    )
//                    dismissDialogRequest(prompt, sessionId, store)
//                    return
//                }
//                           // emitGeneratedPasswordShownFact()
//                GeneratePasswordPrompt(prompt, sessionId, onGeneratePassword = {
//                    PasswordGeneratorDialogPrompt(
//                        prompt,
//                        sessionId,
//                        currentUrl = currentUrl,
//                        onSavedGeneratedPassword = onSaveLogin
//                    )
//                })


                // todo: integrate with:
//                loginPicker?.handleSelectLoginRequest(promptRequest)
//
            }

            is Share -> {
////                    emitPromptDisplayedFact(promptName = "ShareSheet")
//                // todo: customize this and make bottom prompt sheet
//                shareDelegate.showShareSheet(
//                    context = context,
//                    shareData = prompt.data,
//                    onDismiss = {
////                            emitPromptDismissedFact(promptName = "ShareSheet")
//                        onDismiss(prompt)
//                        onNegativeAction(prompt)
//                        store.dispatch(
//                            ContentAction.ConsumePromptRequestAction(
//                                sessionId, prompt
//                            )
//                        )
//                    },
//                    onSuccess = {
//                        onPositiveAction(prompt)
//                        store.dispatch(
//                            ContentAction.ConsumePromptRequestAction(
//                                sessionId, prompt
//                            )
//                        )
//                    },
//                )
            }

            is SingleChoice -> ChoiceDialogPrompt(prompt, sessionId, SINGLE_CHOICE_DIALOG_TYPE)

            is TextPrompt -> TextPromptDialogPrompt(
                prompt,
                sessionId,
                promptAbuserDetector.areDialogsBeingAbused(),
                promptAbuserDetector,
            )

            is TimeSelection -> {
                val selectionType = when (prompt.type) {
                    TimeSelection.Type.DATE -> SELECTION_TYPE_DATE
                    TimeSelection.Type.DATE_AND_TIME -> SELECTION_TYPE_DATE_AND_TIME
                    TimeSelection.Type.TIME -> SELECTION_TYPE_TIME
                    TimeSelection.Type.MONTH -> SELECTION_TYPE_MONTH
                }
                TimeSelectionPrompt(
                    prompt,
                    sessionId,
                    type = selectionType,
                )
            }

            else -> throw InvalidParameterException("Not valid prompt request type $prompt")
        }
    }
}

fun onDismiss(promptRequest: PromptRequest) {
    when (promptRequest) {
        is Alert -> promptRequest.onDismiss.invoke()
        is Authentication -> promptRequest.onDismiss.invoke()
        is BeforeUnload -> promptRequest.onDismiss.invoke()
        is PromptRequest.Color -> promptRequest.onDismiss.invoke()
        is Confirm -> promptRequest.onDismiss.invoke()
        is File -> promptRequest.onDismiss.invoke()
        is IdentityCredential.PrivacyPolicy -> promptRequest.onDismiss.invoke()
        is IdentityCredential.SelectAccount -> promptRequest.onDismiss.invoke()
        is IdentityCredential.SelectProvider -> promptRequest.onDismiss.invoke()
        is MenuChoice -> promptRequest.onDismiss.invoke()
        is MultipleChoice -> promptRequest.onDismiss.invoke()
        is Popup -> promptRequest.onDismiss.invoke()
        is Repost -> promptRequest.onDismiss.invoke()
        is SaveCreditCard -> promptRequest.onDismiss.invoke()
        is SaveLoginPrompt -> promptRequest.onDismiss.invoke()
        is SelectAddress -> promptRequest.onDismiss.invoke()
        is SelectCreditCard -> promptRequest.onDismiss.invoke()
        is SelectLoginPrompt -> promptRequest.onDismiss.invoke()
        is Share -> promptRequest.onDismiss.invoke()
        is SingleChoice -> promptRequest.onDismiss.invoke()
        is TextPrompt -> promptRequest.onDismiss.invoke()
        is TimeSelection -> promptRequest.onDismiss.invoke()
    }
}


fun onPositiveAction(promptRequest: PromptRequest, value: Any? = null, value2: Any? = null) {
    when (promptRequest) {
        is MultipleChoice -> promptRequest.onConfirm.invoke((value as HashMap<*, *>).keys.map { it as Choice }
            .toTypedArray())

        is MenuChoice -> promptRequest.onConfirm.invoke(value as Choice)
        is BeforeUnload -> promptRequest.onLeave.invoke()
        is Alert -> promptRequest.onConfirm.invoke(value as Boolean)
        is Authentication -> promptRequest.onConfirm.invoke(
            value as String, value2 as String
        )

        is PromptRequest.Color -> promptRequest.onConfirm.invoke(value as String)
        is Confirm -> promptRequest.onConfirmPositiveButton.invoke(value as Boolean)
        is File -> {}
        is IdentityCredential.PrivacyPolicy -> promptRequest.onConfirm.invoke(value as Boolean)
        is IdentityCredential.SelectAccount -> promptRequest.onConfirm.invoke(value as Account)
        is IdentityCredential.SelectProvider -> promptRequest.onConfirm.invoke(value as Provider)
        is Popup -> promptRequest.onAllow.invoke()
        is Repost -> promptRequest.onConfirm.invoke()
        is SaveCreditCard -> promptRequest.onConfirm.invoke(value as CreditCardEntry)
        is SaveLoginPrompt -> promptRequest.onConfirm.invoke(value as LoginEntry)
        is SelectAddress -> promptRequest.onConfirm.invoke(value as Address)
        is SelectCreditCard -> promptRequest.onConfirm.invoke(value as CreditCardEntry)
        is SelectLoginPrompt -> promptRequest.onConfirm.invoke(value as Login)
        is Share -> promptRequest.onSuccess.invoke()
        is SingleChoice -> promptRequest.onConfirm.invoke(value as Choice)
        is TextPrompt -> promptRequest.onConfirm.invoke(
            value as Boolean, value2 as String
        )

        is TimeSelection -> promptRequest.onConfirm.invoke(value as Date)
    }
}

fun onNeutralAction(promptRequest: PromptRequest, value: Any? = null) {
    when (promptRequest) {
        is Alert -> promptRequest.onDismiss.invoke()
        is Authentication -> promptRequest.onDismiss.invoke()
        is BeforeUnload -> promptRequest.onDismiss.invoke()
        is PromptRequest.Color -> promptRequest.onDismiss.invoke()
        is Confirm -> promptRequest.onConfirmNeutralButton.invoke(value as Boolean)
        is File -> promptRequest.onDismiss.invoke()
        is IdentityCredential.PrivacyPolicy -> promptRequest.onDismiss.invoke()
        is IdentityCredential.SelectAccount -> promptRequest.onDismiss.invoke()
        is IdentityCredential.SelectProvider -> promptRequest.onDismiss.invoke()
        is MenuChoice -> promptRequest.onDismiss.invoke()
        is MultipleChoice -> promptRequest.onDismiss.invoke()
        is Popup -> promptRequest.onDismiss.invoke()
        is Repost -> promptRequest.onDismiss.invoke()
        is SaveCreditCard -> promptRequest.onDismiss.invoke()
        is SaveLoginPrompt -> promptRequest.onDismiss.invoke()
        is SelectAddress -> promptRequest.onDismiss.invoke()
        is SelectCreditCard -> promptRequest.onDismiss.invoke()
        is SelectLoginPrompt -> promptRequest.onDismiss.invoke()
        is Share -> promptRequest.onDismiss.invoke()
        is SingleChoice -> promptRequest.onDismiss.invoke()
        is TextPrompt -> promptRequest.onDismiss.invoke()
        is TimeSelection -> promptRequest.onClear.invoke()
    }
}

fun onNegativeAction(promptRequest: PromptRequest, value: Any? = null) {
    when (promptRequest) {
        is Alert -> promptRequest.onDismiss.invoke()
        is Authentication -> promptRequest.onDismiss.invoke()
        is BeforeUnload -> promptRequest.onStay.invoke()
        is PromptRequest.Color -> promptRequest.onDismiss.invoke()
        is Confirm -> promptRequest.onConfirmNegativeButton.invoke(value as Boolean)
        is File -> { /* not necessary */
        }

        is IdentityCredential.PrivacyPolicy -> promptRequest.onDismiss.invoke()
        is IdentityCredential.SelectAccount -> promptRequest.onDismiss.invoke()
        is IdentityCredential.SelectProvider -> promptRequest.onDismiss.invoke()
        is MenuChoice -> promptRequest.onDismiss.invoke()
        is MultipleChoice -> promptRequest.onDismiss.invoke()
        is Popup -> promptRequest.onDismiss.invoke()
        is Repost -> promptRequest.onDismiss.invoke()
        is SaveCreditCard -> promptRequest.onDismiss.invoke()
        is SaveLoginPrompt -> promptRequest.onDismiss.invoke()
        is SelectAddress -> promptRequest.onDismiss.invoke()
        is SelectCreditCard -> promptRequest.onDismiss.invoke()
        is SelectLoginPrompt -> promptRequest.onDismiss.invoke()
        is Share -> promptRequest.onFailure.invoke()
        is SingleChoice -> promptRequest.onDismiss.invoke()
        is TextPrompt -> promptRequest.onDismiss.invoke()
        is TimeSelection -> promptRequest.onDismiss.invoke()
    }
}

/**
 * Dismiss and consume the given prompt request for the session.
 */
//@VisibleForTesting
internal fun dismissDialogRequest(
    promptRequest: PromptRequest, sessionId: String, store: BrowserStore,
) {
    onDismiss(promptRequest)
    store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, promptRequest))
    emitPromptDismissedFact(promptName = promptRequest::class.simpleName.ifNullOrEmpty { "" })
}

private fun canShowThisPrompt(
    promptRequest: PromptRequest, promptAbuserDetector: PromptAbuserDetector,
): Boolean {
    return when (promptRequest) {
        is SingleChoice,
        is MultipleChoice,
        is MenuChoice,
        is TimeSelection,
        is File,
        is PromptRequest.Color,
        is Authentication,
        is BeforeUnload,
        is SaveLoginPrompt,
        is SelectLoginPrompt,
        is SelectCreditCard,
        is SaveCreditCard,
        is SelectAddress,
        is Share,
        is IdentityCredential.SelectProvider,
        is IdentityCredential.SelectAccount,
        is IdentityCredential.PrivacyPolicy,
            -> true

        is Alert, is TextPrompt, is Confirm, is Repost, is Popup -> promptAbuserDetector.shouldShowMoreDialogs
    }
}
