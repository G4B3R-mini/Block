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
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.engine.prompt.PromptRequest.Alert
import mozilla.components.concept.engine.prompt.PromptRequest.Authentication
import mozilla.components.concept.engine.prompt.PromptRequest.BeforeUnload
import mozilla.components.concept.engine.prompt.PromptRequest.Confirm
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
import mozilla.components.feature.prompts.PromptFeature
import mozilla.components.support.ktx.util.PromptAbuserDetector
import java.security.InvalidParameterException

// todo:
//   - colors
//   - some prompts not working nicely, maybe not dismissing properly
//   - implement share dialog in compose
//   - some minor problems in some components, go to ones with todos in their files

/**
 * replaces prompt integration
 * shows modal bottom sheet with requested prompt inside
 */
@Composable
fun InfernoWebPrompter(
    state: InfernoPromptFeatureState,
    currentTab: TabSessionState?,
) {
    PromptFeature
    if (currentTab == null) return
    val context = LocalContext.current
//    val logger = remember { Logger("Prompt Component") }
//    var promptRequest by remember { mutableStateOf<PromptRequest?>(null) }
    val sessionId = currentTab.id
    val promptAbuserDetector = remember { PromptAbuserDetector() }
    val promptRequest = state.visiblePrompt

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // display prompt depending on type
        when (promptRequest) {
            is Alert -> AlertDialogPrompt(promptRequest = promptRequest,
                onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) })

            is Authentication -> AuthenticationPrompt(
                promptRequest = promptRequest,
                onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) },
            )

            is BeforeUnload -> {
                val title = stringResource(R.string.mozac_feature_prompt_before_unload_dialog_title)
                val body = stringResource(R.string.mozac_feature_prompt_before_unload_dialog_body)
                val leaveLabel = stringResource(R.string.mozac_feature_prompts_before_unload_leave)
                val stayLabel = stringResource(R.string.mozac_feature_prompts_before_unload_stay)
                ConfirmDialogPrompt(
                    promptRequest = promptRequest,
                    title = title,
                    body = body,
                    negativeLabel = stayLabel,
                    positiveLabel = leaveLabel,
                    hasShownManyDialogs = false,
                    onCancel = { state.onCancel(sessionId, promptRequest.uid, it) },
                    onConfirm = { state.onConfirm(sessionId, promptRequest.uid) },
                )
            }

            is PromptRequest.Color -> ColorPickerDialogPrompt(
                promptRequest = promptRequest,
                onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) },
            )

            is Confirm -> {
                val positiveButton =
                    promptRequest.positiveButtonTitle.ifEmpty { stringResource(R.string.mozac_feature_prompts_ok) }
                val negativeButton =
                    promptRequest.negativeButtonTitle.ifEmpty { stringResource(R.string.mozac_feature_prompts_cancel) }
                val neutralButton =
                    promptRequest.neutralButtonTitle.ifEmpty { stringResource(R.string.mozac_feature_prompts_suggest_strong_password_dismiss) }
                MultiButtonDialogPrompt(
                    promptRequest = promptRequest,
                    negativeButtonText = negativeButton,
                    neutralButtonText = neutralButton,
                    positiveButtonText = positiveButton,
                    onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                    onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) },
                )
            }

            is IdentityCredential.PrivacyPolicy -> {
                val title = context.getString(
                    R.string.mozac_feature_prompts_identity_credentials_privacy_policy_title,
                    promptRequest.providerDomain,
                )
                val message = context.getString(
                    R.string.mozac_feature_prompts_identity_credentials_privacy_policy_description,
                    promptRequest.host,
                    promptRequest.providerDomain,
                    promptRequest.privacyPolicyUrl,
                    promptRequest.termsOfServiceUrl,
                )
                PrivacyPolicyDialog(
                    promptRequest = promptRequest,
                    title = title,
                    message = message,
                    onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                    onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) },
                )
            }

            is IdentityCredential.SelectAccount -> SelectAccountDialogPrompt(
                promptRequest = promptRequest,
                onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) },
            )

            is IdentityCredential.SelectProvider -> SelectProviderDialogPrompt(
                promptRequest = promptRequest,
                onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) },
            )

            is MenuChoice -> ChoiceDialogPrompt(
                promptRequest = promptRequest,
                dialogType = MENU_CHOICE_DIALOG_TYPE,
                onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) },
            )

            is MultipleChoice -> ChoiceDialogPrompt(
                promptRequest = promptRequest,
                dialogType = MULTIPLE_CHOICE_DIALOG_TYPE,
                onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) },
            )

            is Popup -> {
                val title = context.getString(R.string.mozac_feature_prompts_popup_dialog_title)
                val positiveLabel = context.getString(R.string.mozac_feature_prompts_allow)
                val negativeLabel = context.getString(R.string.mozac_feature_prompts_deny)

                ConfirmDialogPrompt(
                    promptRequest = promptRequest,
                    title = title,
                    body = null,
                    negativeLabel = negativeLabel,
                    positiveLabel = positiveLabel,
                    hasShownManyDialogs = promptAbuserDetector.areDialogsBeingAbused(),
                    onCancel = { state.onCancel(sessionId, promptRequest.uid, it) },
                    onConfirm = { state.onConfirm(sessionId, promptRequest.uid) },
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
                    promptRequest = promptRequest,
                    title = title,
                    body = message,
                    negativeLabel = negativeAction,
                    positiveLabel = positiveAction,
                    hasShownManyDialogs = false,
                    onCancel = { state.onCancel(sessionId, promptRequest.uid, it) },
                    onConfirm = { state.onConfirm(sessionId, promptRequest.uid) },
                )
            }

            is SaveCreditCard -> CreditCardSaveDialogPrompt(
                promptRequest = promptRequest,
                onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) },
            )

            is SaveLoginPrompt -> SaveLoginDialogPrompt(
                promptRequest = promptRequest,
                icon = currentTab.content.icon,
                onShowSnackbarAfterLoginChange = state.onSaveLogin,
                loginValidationDelegate = state.loginValidationDelegate,
                onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) },
            )

            is SelectAddress -> SelectableListPrompt(
                promptRequest = promptRequest,
                header = stringResource(R.string.mozac_feature_prompts_select_address_2),
                manageText = stringResource(R.string.mozac_feature_prompts_manage_address),
                onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) },
            )


            is SelectCreditCard -> SelectableListPrompt(
                promptRequest = promptRequest,
                header = stringResource(R.string.mozac_feature_prompts_select_credit_card_2),
                manageText = stringResource(R.string.mozac_feature_prompts_manage_credit_cards_2),
                onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) },
            )

            is SelectLoginPrompt -> {
                when (state.selectLoginPromptController) {
                    is SelectLoginPromptController.LoginPickerDialog -> LoginPickerPrompt(
                        promptRequest = promptRequest,
                        controller = state.selectLoginPromptController as SelectLoginPromptController.LoginPickerDialog,
                        onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                        onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) },
                    )

                    is SelectLoginPromptController.StrongPasswordBarDialog -> {
                        PasswordGeneratorDialogPrompt(promptRequest = promptRequest,
                            currentUrl = state.currentUrl ?: "<empty>",
                            askFirst = true,
                            onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                            onConfirm = {
                                state.onConfirm(sessionId, promptRequest.uid, it)
                                state.onSaveLogin.invoke(false)
                            })
                        R.string.mozac_feature_prompts_suggest_strong_password_2
                    }

                    is SelectLoginPromptController.PasswordGeneratorDialog -> {
                        PasswordGeneratorDialogPrompt(promptRequest,
                            currentUrl = state.currentUrl ?: "<empty>",
                            askFirst = false,
                            onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                            onConfirm = {
                                state.onConfirm(sessionId, promptRequest.uid, it)
                                state.onSaveLogin.invoke(false)
                            })
                    }
                }
            }

            is Share -> {
//                    emitPromptDisplayedFact(promptName = "ShareSheet")
                // todo: customize this and make bottom prompt sheet
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

            is SingleChoice -> ChoiceDialogPrompt(
                promptRequest = promptRequest,
                dialogType = SINGLE_CHOICE_DIALOG_TYPE,
                onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) },
            )

            is TextPrompt -> TextPromptDialogPrompt(
                promptRequest = promptRequest,
                hasShownManyDialogs = promptAbuserDetector.areDialogsBeingAbused(),
                onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) },
            )

            is TimeSelection -> {
                val selectionType = when (promptRequest.type) {
                    TimeSelection.Type.DATE -> SELECTION_TYPE_DATE
                    TimeSelection.Type.DATE_AND_TIME -> SELECTION_TYPE_DATE_AND_TIME
                    TimeSelection.Type.TIME -> SELECTION_TYPE_TIME
                    TimeSelection.Type.MONTH -> SELECTION_TYPE_MONTH
                }
                TimeSelectionPrompt(
                    promptRequest = promptRequest,
                    type = selectionType,
                    onCancel = { state.onCancel(sessionId, promptRequest.uid) },
                    onConfirm = { state.onConfirm(sessionId, promptRequest.uid, it) },
                    onClear = {state.onClear(sessionId, promptRequest.uid)}
                )
            }

            else -> throw InvalidParameterException("Not valid prompt request type $promptRequest")
        }
    }
}