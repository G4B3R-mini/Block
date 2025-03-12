package com.shmibblez.inferno.browser.prompts

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.compose.AlertDialogPrompt
import com.shmibblez.inferno.browser.prompts.compose.AuthenticationPrompt
import com.shmibblez.inferno.browser.prompts.compose.ChoiceDialogPrompt
import com.shmibblez.inferno.browser.prompts.compose.ColorPickerDialogPrompt
import com.shmibblez.inferno.browser.prompts.compose.ConfirmDialogPrompt
import com.shmibblez.inferno.browser.prompts.compose.CreditCardSaveDialogPrompt
import com.shmibblez.inferno.browser.prompts.compose.GeneratePasswordPrompt
import com.shmibblez.inferno.browser.prompts.compose.LoginPickerPrompt
import com.shmibblez.inferno.browser.prompts.compose.MENU_CHOICE_DIALOG_TYPE
import com.shmibblez.inferno.browser.prompts.compose.MULTIPLE_CHOICE_DIALOG_TYPE
import com.shmibblez.inferno.browser.prompts.compose.MultiButtonDialogPrompt
import com.shmibblez.inferno.browser.prompts.compose.PasswordGeneratorDialogPrompt
import com.shmibblez.inferno.browser.prompts.compose.PrivacyPolicyDialog
import com.shmibblez.inferno.browser.prompts.compose.SELECTION_TYPE_DATE
import com.shmibblez.inferno.browser.prompts.compose.SELECTION_TYPE_DATE_AND_TIME
import com.shmibblez.inferno.browser.prompts.compose.SELECTION_TYPE_MONTH
import com.shmibblez.inferno.browser.prompts.compose.SELECTION_TYPE_TIME
import com.shmibblez.inferno.browser.prompts.compose.SINGLE_CHOICE_DIALOG_TYPE
import com.shmibblez.inferno.browser.prompts.compose.SaveLoginDialogPrompt
import com.shmibblez.inferno.browser.prompts.compose.SelectAccountDialogPrompt
import com.shmibblez.inferno.browser.prompts.compose.SelectProviderDialogPrompt
import com.shmibblez.inferno.browser.prompts.compose.SelectableListPrompt
import com.shmibblez.inferno.browser.prompts.compose.TextPromptDialogPrompt
import com.shmibblez.inferno.browser.prompts.compose.TimeSelectionPrompt
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.dialog.emitGeneratedPasswordShownFact
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.facts.emitLoginAutofillShownFact
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.facts.emitSuccessfulAddressAutofillFormDetectedFact
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.facts.emitSuccessfulCreditCardAutofillFormDetectedFact
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
import mozilla.components.concept.storage.CreditCardValidationDelegate
import mozilla.components.concept.storage.Login
import mozilla.components.concept.storage.LoginEntry
import mozilla.components.concept.storage.LoginValidationDelegate
import mozilla.components.feature.prompts.address.AddressDelegate
import mozilla.components.feature.prompts.address.DefaultAddressDelegate
import mozilla.components.feature.prompts.creditcard.CreditCardDelegate
import mozilla.components.feature.prompts.identitycredential.DialogColors
import mozilla.components.feature.prompts.identitycredential.DialogColorsProvider
import mozilla.components.feature.prompts.login.LoginDelegate
import mozilla.components.feature.prompts.login.LoginExceptions
import mozilla.components.feature.prompts.login.PasswordGeneratorDialogColors
import mozilla.components.feature.prompts.login.PasswordGeneratorDialogColorsProvider
import mozilla.components.feature.prompts.login.SuggestStrongPasswordDelegate
import mozilla.components.feature.prompts.share.ShareDelegate
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.session.SessionUseCases.ExitFullScreenUseCase
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.base.feature.OnNeedToRequestPermissions
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.kotlin.ifNullOrEmpty
import mozilla.components.support.ktx.util.PromptAbuserDetector
import java.util.Date

// todo:
//   - prompt requests pop up on bottom of screen, show active ones (could be multiple)
//     on the bottom stacked, first one on bottom and last on top
//   - depending on the type, show different prompt, for each lifecycle is the following:
//     1. active
//     2. pending dismissal,
//     3. process and dismiss (cancel) or just dismiss,
//        for process look at fun processPromptRequest() in [PromptFeature]
//     4. confirm check out onConfirm() in [PromptFeature]
//        if can be confirmed check moz prompt onConfirm() fun
//   - to consume prompt in browser store, check BrowserStore.consumePromptFrom() in [PromptFeature]
//     (there are multiple functions, also BrowserStore.consumeAllSessionPrompts)
//  - for specific prompts, things to keep in mind:
//    - if cannot be shown (permissions, settings, etc), then dismiss
//    - TimeSelection prompt: can be cleared
//    - select prompts with listeners:
//      - LoginPicker prompt
//      - SuggestStrongPassword prompt
//      - CreditCardPicker prompt
//      - AddressPicker prompt
//    - AndroidPhotoPicker result needs to be transferred over to filePicker:
//      filePicker.onAndroidPhotoPickerResult(uriList)
//
//

enum class PromptBottomSheetTemplateButtonPosition {
    BOTTOM, TOP
}

data class PromptBottomSheetTemplateAction(
    val text: String, val action: () -> Unit, val enabled: Boolean = true,
)


/**
 * @param onDismissRequest what to do when dialog dismissed
 * @param dismissOnSwipeDown whether should dismiss on swipe down
 * @param negativeAction negative action text and function
 * @param positiveAction positive action text and function
 * @param content dialog content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptBottomSheetTemplate(
    onDismissRequest: () -> Unit,
    dismissOnSwipeDown: Boolean = false,
    negativeAction: PromptBottomSheetTemplateAction? = null,
    neutralAction: PromptBottomSheetTemplateAction? = null,
    positiveAction: PromptBottomSheetTemplateAction? = null,
    buttonPosition: PromptBottomSheetTemplateButtonPosition = PromptBottomSheetTemplateButtonPosition.TOP,
    content: @Composable (ColumnScope.() -> Unit),
) {
    // don't dismiss on swipe down
    val sheetState = rememberModalBottomSheetState(confirmValueChange = { sheetValue ->
        sheetValue != SheetValue.Hidden || dismissOnSwipeDown
    })
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        // todo: use acorn colors
        containerColor = Color.Black,
        scrimColor = Color.Black.copy(alpha = 0.1F),
        shape = RectangleShape,
        dragHandle = {
            /* no drag handle */
//            BottomSheetDefaults.DragHandle(
//                color = Color.White,
//                height = ToolbarMenuItemConstants.SHEET_HANDLE_HEIGHT,
////            shape = RectangleShape,
//            )
        },
        content = {
            // content actions (cancel or confirm)
            // content below
            if (buttonPosition == PromptBottomSheetTemplateButtonPosition.TOP && (positiveAction != null || negativeAction != null)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // todo: button text color and colors if not enabled
                        if (negativeAction != null) {
                            TextButton(
                                onClick = negativeAction.action,
                                enabled = negativeAction.enabled,
                            ) {
                                InfernoText(
                                    text = negativeAction.text,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(all = 4.dp)
                                )
                            }
                        }
                        if (neutralAction != null) {
                            TextButton(
                                onClick = neutralAction.action,
                                enabled = neutralAction.enabled,
                            ) {
                                InfernoText(
                                    text = neutralAction.text,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(all = 4.dp)
                                )
                            }
                        }
                        if (positiveAction != null) {
                            TextButton(
                                onClick = positiveAction.action,
                                enabled = positiveAction.enabled,
                            ) {
                                InfernoText(
                                    text = positiveAction.text,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(all = 4.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        color = Color.LightGray, modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            // content
            content.invoke(this)
            // bottom buttons
            if (buttonPosition == PromptBottomSheetTemplateButtonPosition.BOTTOM && (positiveAction != null || negativeAction != null)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // todo: button text color and colors if not enabled
                    if (negativeAction != null) {
                        OutlinedButton(
                            onClick = negativeAction.action,
                            modifier = Modifier.weight(1F),
                            enabled = negativeAction.enabled,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(width = 1.dp, color = Color.White),
                        ) {
                            InfernoText(
                                text = negativeAction.text,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1F)
                                    .padding(all = 4.dp)
                            )
                        }
                    }

                    if (neutralAction != null) {
                        OutlinedButton(
                            onClick = neutralAction.action,
                            modifier = Modifier.weight(1F),
                            enabled = neutralAction.enabled,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(width = 1.dp, color = Color.White),
                        ) {
                            InfernoText(
                                text = neutralAction.text,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1F)
                                    .padding(all = 4.dp)
                            )
                        }
                    }

                    if (positiveAction != null) {
                        Button(
                            onClick = positiveAction.action,
                            modifier = Modifier.weight(1F),
                            enabled = positiveAction.enabled,
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(143, 0, 255)
                            ),
                        ) {
                            InfernoText(
                                text = positiveAction.text,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1F)
                                    .padding(all = 4.dp)
                            )
                        }
                    }
                }
            }
        },
    )
}

/**
 * replaces prompt integration
 * shows modal bottom sheet with requested prompt inside
 */
@Composable
fun PromptComponent(
    promptRequests: List<PromptRequest>,
    currentTab: TabSessionState?,

    /* private val */
//    container: PromptContainer,
    /* private val */
    store: BrowserStore,
    /* private var */
    customTabId: String?,
    /* private val */
    fragmentManager: FragmentManager,
    filePicker: FilePicker,
    /* private val */
    identityCredentialColorsProvider: DialogColorsProvider = DialogColorsProvider {
        DialogColors.default()
    },
    /* private val */
    tabsUseCases: TabsUseCases,
    /* private val */
    shareDelegate: ShareDelegate,
    /* private val */
    exitFullscreenUsecase: ExitFullScreenUseCase = SessionUseCases(store).exitFullscreen,
    /* override val */
    creditCardValidationDelegate: CreditCardValidationDelegate? = null,
    /* override val */
    loginValidationDelegate: LoginValidationDelegate? = null,
    /* private val */
    isLoginAutofillEnabled: () -> Boolean = { false },
    /* private val */
    isSaveLoginEnabled: () -> Boolean = { false },
    /* private val */
    isCreditCardAutofillEnabled: () -> Boolean = { false },
    /* private val */
    isAddressAutofillEnabled: () -> Boolean = { false },
    /* override val */
    loginExceptionStorage: LoginExceptions? = null,
    /* private val */
    loginDelegate: LoginDelegate = object : LoginDelegate {},
    /* private val */
    suggestStrongPasswordDelegate: SuggestStrongPasswordDelegate = object :
        SuggestStrongPasswordDelegate {},
    /* private var */
    shouldAutomaticallyShowSuggestedPassword: () -> Boolean = { false },
    /* private val */
    onFirstTimeEngagedWithSignup: () -> Unit = {},
    /* private val */
    onSaveLoginWithStrongPassword: (String, String) -> Unit = { _, _ -> },
    /* private val */
    onSaveLogin: (Boolean) -> Unit = { _ -> },
    /* private val */
    passwordGeneratorColorsProvider: PasswordGeneratorDialogColorsProvider = PasswordGeneratorDialogColorsProvider { PasswordGeneratorDialogColors.default() },
    /* private val */
    hideUpdateFragmentAfterSavingGeneratedPassword: (String, String) -> Boolean = { _, _ -> true },
    /* private val */
    removeLastSavedGeneratedPassword: () -> Unit = {},
    /* private val */
    creditCardDelegate: CreditCardDelegate = object : CreditCardDelegate {},
    /* private val */
    addressDelegate: AddressDelegate = DefaultAddressDelegate(),
    /* private val */
    fileUploadsDirCleaner: FileUploadsDirCleaner,
    onNeedToRequestPermissions: OnNeedToRequestPermissions,
    androidPhotoPicker: AndroidPhotoPicker?,
) {
    if (currentTab == null) return
    val context = LocalContext.current
    val logger = remember { Logger("Prompt Component") }
//    var promptRequest by remember { mutableStateOf<PromptRequest?>(null) }
    val sessionId = currentTab.id
    val promptAbuserDetector = remember { PromptAbuserDetector() }

    // show prompts one above the other, first one on top
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Log.d("PromptComponent", "prompts size: ${promptRequests.size}")
        for (prompt in promptRequests.reversed()) {
            // if cant show prompt, go to next one
            if (!canShowThisPrompt(prompt, promptAbuserDetector)) {
                continue
            }
            // display prompt depending on type
            when (prompt) {
                is Alert -> {
                    AlertDialogPrompt(prompt, sessionId, promptAbuserDetector)
                }

                is PromptRequest.Authentication -> {
                    AuthenticationPrompt(prompt, sessionId)
                }

                is PromptRequest.BeforeUnload -> {
                    val title =
                        stringResource(R.string.mozac_feature_prompt_before_unload_dialog_title)
                    val body =
                        stringResource(R.string.mozac_feature_prompt_before_unload_dialog_body)
                    val leaveLabel =
                        stringResource(R.string.mozac_feature_prompts_before_unload_leave)
                    val stayLabel =
                        stringResource(R.string.mozac_feature_prompts_before_unload_stay)
                    ConfirmDialogPrompt(
                        prompt, sessionId, title, body, stayLabel, leaveLabel, promptAbuserDetector
                    )
                }

                is PromptRequest.Color -> {
                    ColorPickerDialogPrompt(prompt, sessionId)
                }

                is PromptRequest.Confirm -> {
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

                is PromptRequest.File -> {
                    emitPromptDisplayedFact(promptName = "FilePrompt")
                    filePicker.handleFileRequest(prompt)
                }

                is PromptRequest.IdentityCredential.PrivacyPolicy -> {
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

                is PromptRequest.IdentityCredential.SelectAccount -> {
                    SelectAccountDialogPrompt(prompt, sessionId)
                }

                is PromptRequest.IdentityCredential.SelectProvider -> {
                    SelectProviderDialogPrompt(prompt, sessionId)
                }

                is PromptRequest.MenuChoice -> {
                    ChoiceDialogPrompt(prompt, sessionId, MENU_CHOICE_DIALOG_TYPE)
                }

                is PromptRequest.MultipleChoice -> {
                    ChoiceDialogPrompt(prompt, sessionId, MULTIPLE_CHOICE_DIALOG_TYPE)
                }

                is PromptRequest.Popup -> {
                    val title = context.getString(R.string.mozac_feature_prompts_popup_dialog_title)
                    val positiveLabel = context.getString(R.string.mozac_feature_prompts_allow)
                    val negativeLabel = context.getString(R.string.mozac_feature_prompts_deny)

                    ConfirmDialogPrompt(
                        prompt,
                        sessionId,
                        title,
                        "",
                        negativeLabel,
                        positiveLabel,
                        promptAbuserDetector
                    )
                }

                is PromptRequest.Repost -> {
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

                is PromptRequest.SaveCreditCard -> {
                    if (!isCreditCardAutofillEnabled.invoke() || creditCardValidationDelegate == null || !prompt.creditCard.isValid) {
                        dismissDialogRequest(prompt, sessionId, store)

                        if (creditCardValidationDelegate == null) {
                            logger.debug(
                                "Ignoring received SaveCreditCard because PromptFeature." + "creditCardValidationDelegate is null. If you are trying to autofill " + "credit cards, try attaching a CreditCardValidationDelegate to PromptFeature",
                            )
                        }

                        return
                    }

                    com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.facts.emitCreditCardSaveShownFact()

                    CreditCardSaveDialogPrompt(prompt, sessionId)
                }

                is PromptRequest.SaveLoginPrompt -> {
                    if (!isSaveLoginEnabled.invoke() || loginValidationDelegate == null) {
                        dismissDialogRequest(prompt, sessionId, store)

                        if (loginValidationDelegate == null) {
                            logger.debug(
                                "Ignoring received SaveLoginPrompt because PromptFeature." + "loginValidationDelegate is null. If you are trying to autofill logins, " + "try attaching a LoginValidationDelegate to PromptFeature",
                            )
                        }

                        return
                    } else if (hideUpdateFragmentAfterSavingGeneratedPassword(
                            prompt.logins[0].username,
                            prompt.logins[0].password,
                        )
                    ) {
                        removeLastSavedGeneratedPassword()
                        dismissDialogRequest(prompt, sessionId, store)

                        return
                    }

                    SaveLoginDialogPrompt(
                        loginData = prompt,
                        sessionId = sessionId,
                        icon = currentTab.content.icon,
                        onShowSnackbarAfterLoginChange = onSaveLogin,
                        loginValidationDelegate = loginValidationDelegate,
                    )
                }

                is PromptRequest.SelectAddress -> {
//                var addressPicker =
//                    with(addressDelegate) {
//                        addressPickerView?.let {
//                            AddressPicker(
//                                store = store,
//                                addressSelectBar = it,
//                                onManageAddresses = onManageAddresses,
//                                sessionId = customTabId,
//                            )
//                        }
//                    }
                    emitSuccessfulAddressAutofillFormDetectedFact()
                    if (isAddressAutofillEnabled() && prompt.addresses.isNotEmpty()) {
//                    addressPicker?.handleSelectAddressRequest(prompt)
                        SelectableListPrompt(
                            prompt,
                            sessionId,
                            header = stringResource(R.string.mozac_feature_prompts_select_address_2),
                            manageText = stringResource(R.string.mozac_feature_prompts_manage_address),
                        )
                    }
                }

                is PromptRequest.SelectCreditCard -> {
                    emitSuccessfulCreditCardAutofillFormDetectedFact()
                    if (isCreditCardAutofillEnabled() && prompt.creditCards.isNotEmpty()) {
//                    creditCardPicker?.handleSelectCreditCardRequest(prompt)
                        SelectableListPrompt(
                            prompt,
                            sessionId,
                            header = stringResource(R.string.mozac_feature_prompts_select_credit_card_2),
                            manageText = stringResource(R.string.mozac_feature_prompts_manage_credit_cards_2),
                        )

                    }
                }

                is PromptRequest.SelectLoginPrompt -> {
                    if (!isLoginAutofillEnabled()) {
                        return
                    }
                    if (prompt.generatedPassword != null) {
                        if (shouldAutomaticallyShowSuggestedPassword.invoke()) {
//                        todo: onFirstTimeEngagedWithSignup.invoke()
//                        handleDialogsRequest(
//                            promptRequest,
//                            session,
//                        )
                            val currentUrl =
                                store.state.findTabOrCustomTabOrSelectedTab(customTabId)?.content?.url
                            val generatedPassword = prompt.generatedPassword

                            if (generatedPassword == null || currentUrl == null) {
                                logger.debug(
                                    "Ignoring received SelectLogin.onGeneratedPasswordPromptClick" + " when either the generated password or the currentUrl is null.",
                                )
                                dismissDialogRequest(prompt, sessionId, store)
                                return
                            }
                            emitGeneratedPasswordShownFact()
                            // todo: password generator dialog
                            PasswordGeneratorDialogPrompt(
                                prompt,
                                sessionId,
                                currentUrl = currentUrl,
                                onSavedGeneratedPassword = onSaveLogin,
                            )

                        } else {
//                        strongPasswordPromptViewListener?.onGeneratedPasswordPromptClick = {
//                            handleDialogsRequest(
//                                promptRequest,
//                                session,
//                            )
//                        }
//                        strongPasswordPromptViewListener?.handleSuggestStrongPasswordRequest()
                            // todo: suggest strong password bar
                            val currentUrl =
                                store.state.findTabOrCustomTabOrSelectedTab(customTabId)?.content?.url
                            val generatedPassword = prompt.generatedPassword

                            if (generatedPassword == null || currentUrl == null) {
                                logger.debug(
                                    "Ignoring received SelectLogin.onGeneratedPasswordPromptClick" + " when either the generated password or the currentUrl is null.",
                                )
                                dismissDialogRequest(prompt, sessionId, store)
                                return
                            }
                            emitGeneratedPasswordShownFact()
                            GeneratePasswordPrompt(prompt, sessionId, onGeneratePassword = {
                                PasswordGeneratorDialogPrompt(
                                    prompt,
                                    sessionId,
                                    currentUrl = currentUrl,
                                    onSavedGeneratedPassword = onSaveLogin
                                )
                            })
                        }
                    } else {
                        emitLoginAutofillShownFact()
                        LoginPickerPrompt(prompt, sessionId)
//                    loginPicker?.handleSelectLoginRequest(promptRequest)
                    }
                    emitPromptDisplayedFact(promptName = "SelectLoginPrompt")
                }

                is PromptRequest.Share -> {
                    emitPromptDisplayedFact(promptName = "ShareSheet")
                    shareDelegate.showShareSheet(
                        context = context,
                        shareData = prompt.data,
                        onDismiss = {
                            emitPromptDismissedFact(promptName = "ShareSheet")
                            onDismiss(prompt)
                            onNegativeAction(prompt)
                            store.dispatch(
                                ContentAction.ConsumePromptRequestAction(
                                    sessionId, prompt
                                )
                            )
                        },
                        onSuccess = {
                            onPositiveAction(prompt)
                            store.dispatch(
                                ContentAction.ConsumePromptRequestAction(
                                    sessionId, prompt
                                )
                            )
                        },
                    )
                }

                is PromptRequest.SingleChoice -> {
                    ChoiceDialogPrompt(prompt, sessionId, SINGLE_CHOICE_DIALOG_TYPE)
                }

                is PromptRequest.TextPrompt -> {
                    TextPromptDialogPrompt(
                        prompt,
                        sessionId,
                        promptAbuserDetector.areDialogsBeingAbused(),
                        promptAbuserDetector,
                    )

                }

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
            }
        }
    }

}

fun onDismiss(promptRequest: PromptRequest) {
    when (promptRequest) {
        is Alert -> promptRequest.onDismiss.invoke()
        is PromptRequest.Authentication -> promptRequest.onDismiss.invoke()
        is PromptRequest.BeforeUnload -> promptRequest.onDismiss.invoke()
        is PromptRequest.Color -> promptRequest.onDismiss.invoke()
        is PromptRequest.Confirm -> promptRequest.onDismiss.invoke()
        is PromptRequest.File -> promptRequest.onDismiss.invoke()
        is PromptRequest.IdentityCredential.PrivacyPolicy -> promptRequest.onDismiss.invoke()
        is PromptRequest.IdentityCredential.SelectAccount -> promptRequest.onDismiss.invoke()
        is PromptRequest.IdentityCredential.SelectProvider -> promptRequest.onDismiss.invoke()
        is PromptRequest.MenuChoice -> promptRequest.onDismiss.invoke()
        is PromptRequest.MultipleChoice -> promptRequest.onDismiss.invoke()
        is PromptRequest.Popup -> promptRequest.onDismiss.invoke()
        is PromptRequest.Repost -> promptRequest.onDismiss.invoke()
        is PromptRequest.SaveCreditCard -> promptRequest.onDismiss.invoke()
        is PromptRequest.SaveLoginPrompt -> promptRequest.onDismiss.invoke()
        is PromptRequest.SelectAddress -> promptRequest.onDismiss.invoke()
        is PromptRequest.SelectCreditCard -> promptRequest.onDismiss.invoke()
        is PromptRequest.SelectLoginPrompt -> promptRequest.onDismiss.invoke()
        is PromptRequest.Share -> promptRequest.onDismiss.invoke()
        is PromptRequest.SingleChoice -> promptRequest.onDismiss.invoke()
        is PromptRequest.TextPrompt -> promptRequest.onDismiss.invoke()
        is TimeSelection -> promptRequest.onDismiss.invoke()
    }
}

fun onPositiveAction(promptRequest: PromptRequest, value: Any? = null, value2: Any? = null) {
    when (promptRequest) {
        is PromptRequest.MultipleChoice -> promptRequest.onConfirm.invoke(((value as HashMap<*, *>).mapKeys { it as Choice }).keys.toTypedArray())
        is PromptRequest.MenuChoice -> promptRequest.onConfirm.invoke(value as Choice)
        is PromptRequest.BeforeUnload -> promptRequest.onLeave.invoke()
        is Alert -> promptRequest.onConfirm.invoke(value as Boolean)
        is PromptRequest.Authentication -> promptRequest.onConfirm.invoke(
            value as String, value2 as String
        )

        is PromptRequest.Color -> promptRequest.onConfirm.invoke(value as String)
        is PromptRequest.Confirm -> promptRequest.onConfirmPositiveButton.invoke(value as Boolean)
        is PromptRequest.File -> {}
        is PromptRequest.IdentityCredential.PrivacyPolicy -> promptRequest.onConfirm.invoke(value as Boolean)
        is PromptRequest.IdentityCredential.SelectAccount -> promptRequest.onConfirm.invoke(value as Account)
        is PromptRequest.IdentityCredential.SelectProvider -> promptRequest.onConfirm.invoke(value as Provider)
        is PromptRequest.Popup -> promptRequest.onAllow.invoke()
        is PromptRequest.Repost -> promptRequest.onConfirm.invoke()
        is PromptRequest.SaveCreditCard -> promptRequest.onConfirm.invoke(value as CreditCardEntry)
        is PromptRequest.SaveLoginPrompt -> promptRequest.onConfirm.invoke(value as LoginEntry)
        is PromptRequest.SelectAddress -> promptRequest.onConfirm.invoke(value as Address)
        is PromptRequest.SelectCreditCard -> promptRequest.onConfirm.invoke(value as CreditCardEntry)
        is PromptRequest.SelectLoginPrompt -> promptRequest.onConfirm.invoke(value as Login)
        is PromptRequest.Share -> promptRequest.onSuccess.invoke()
        is PromptRequest.SingleChoice -> promptRequest.onConfirm.invoke(value as Choice)
        is PromptRequest.TextPrompt -> promptRequest.onConfirm.invoke(
            value as Boolean, value2 as String
        )

        is TimeSelection -> promptRequest.onConfirm.invoke(value as Date)
    }
}

fun onNeutralAction(promptRequest: PromptRequest, value: Any? = null) {
    when (promptRequest) {
        is Alert -> TODO()
        is PromptRequest.Authentication -> TODO()
        is PromptRequest.BeforeUnload -> promptRequest.onDismiss.invoke()
        is PromptRequest.Color -> TODO()
        is PromptRequest.Confirm -> promptRequest.onConfirmNeutralButton.invoke(value as Boolean)
        is PromptRequest.File -> TODO()
        is PromptRequest.IdentityCredential.PrivacyPolicy -> TODO()
        is PromptRequest.IdentityCredential.SelectAccount -> TODO()
        is PromptRequest.IdentityCredential.SelectProvider -> TODO()
        is PromptRequest.MenuChoice -> TODO()
        is PromptRequest.MultipleChoice -> TODO()
        is PromptRequest.Popup -> promptRequest.onDismiss.invoke()
        is PromptRequest.Repost -> promptRequest.onDismiss.invoke()
        is PromptRequest.SaveCreditCard -> TODO()
        is PromptRequest.SaveLoginPrompt -> TODO()
        is PromptRequest.SelectAddress -> TODO()
        is PromptRequest.SelectCreditCard -> TODO()
        is PromptRequest.SelectLoginPrompt -> TODO()
        is PromptRequest.Share -> TODO()
        is PromptRequest.SingleChoice -> TODO()
        is PromptRequest.TextPrompt -> TODO()
        is TimeSelection -> promptRequest.onClear.invoke()
    }
}

fun onNegativeAction(promptRequest: PromptRequest, value: Any? = null) {
    when (promptRequest) {
        is Alert -> promptRequest.onDismiss.invoke()
        is PromptRequest.Authentication -> promptRequest.onDismiss.invoke()
        is PromptRequest.BeforeUnload -> promptRequest.onStay.invoke()
        is PromptRequest.Color -> promptRequest.onDismiss.invoke()
        is PromptRequest.Confirm -> promptRequest.onConfirmNegativeButton.invoke(value as Boolean)
        is PromptRequest.File -> TODO()
        is PromptRequest.IdentityCredential.PrivacyPolicy -> promptRequest.onDismiss.invoke()
        is PromptRequest.IdentityCredential.SelectAccount -> promptRequest.onDismiss.invoke()
        is PromptRequest.IdentityCredential.SelectProvider -> promptRequest.onDismiss.invoke()
        is PromptRequest.MenuChoice -> promptRequest.onDismiss.invoke()
        is PromptRequest.MultipleChoice -> promptRequest.onDismiss.invoke()
        is PromptRequest.Popup -> promptRequest.onDismiss.invoke()
        is PromptRequest.Repost -> promptRequest.onDismiss.invoke()
        is PromptRequest.SaveCreditCard -> promptRequest.onDismiss.invoke()
        is PromptRequest.SaveLoginPrompt -> promptRequest.onDismiss.invoke()
        is PromptRequest.SelectAddress -> promptRequest.onDismiss.invoke()
        is PromptRequest.SelectCreditCard -> promptRequest.onDismiss.invoke()
        is PromptRequest.SelectLoginPrompt -> promptRequest.onDismiss.invoke()
        is PromptRequest.Share -> promptRequest.onFailure.invoke()
        is PromptRequest.SingleChoice -> promptRequest.onDismiss.invoke()
        is PromptRequest.TextPrompt -> promptRequest.onDismiss.invoke()
        is TimeSelection -> promptRequest.onDismiss.invoke()
    }
}

/**
 * Dismiss and consume the given prompt request for the session.
 */
//@VisibleForTesting
internal fun dismissDialogRequest(
    promptRequest: PromptRequest, sessionId: String, store: BrowserStore
) {
    onDismiss(promptRequest)
    store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, promptRequest))
    emitPromptDismissedFact(promptName = promptRequest::class.simpleName.ifNullOrEmpty { "" })
}

private fun canShowThisPrompt(
    promptRequest: PromptRequest, promptAbuserDetector: PromptAbuserDetector
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
        is PromptRequest.IdentityCredential.SelectProvider,
        is PromptRequest.IdentityCredential.SelectAccount,
        is PromptRequest.IdentityCredential.PrivacyPolicy,
            -> true

        is Alert, is TextPrompt, is Confirm, is Repost, is Popup -> promptAbuserDetector.shouldShowMoreDialogs
    }
}

// todo: call in onStop and in onBackPressed
///**
// * Dismisses the select prompts if they are active and visible.
// *
// * @returns true if a select prompt was dismissed, otherwise false.
// */
//@VisibleForTesting
//fun dismissSelectPrompts(): Boolean {
//    var result = false
//
//    (activePromptRequest as? SelectLoginPrompt)?.let { selectLoginPrompt ->
//        loginPicker?.let { loginPicker ->
//            if (loginDelegate.loginPickerView?.asView()?.isVisible == true) {
//                loginPicker.dismissCurrentLoginSelect(selectLoginPrompt)
//                result = true
//            }
//        }
//
//        strongPasswordPromptViewListener?.let { strongPasswordPromptViewListener ->
//            if (suggestStrongPasswordDelegate.strongPasswordPromptViewListenerView?.isVisible() == true) {
//                strongPasswordPromptViewListener.dismissCurrentSuggestStrongPassword(
//                    selectLoginPrompt,
//                )
//                result = true
//            }
//        }
//    }
//
//    (activePromptRequest as? SelectCreditCard)?.let { selectCreditCardPrompt ->
//        creditCardPicker?.let { creditCardPicker ->
//            if (creditCardDelegate.creditCardPickerView?.asView()?.isVisible == true) {
//                creditCardPicker.dismissSelectCreditCardRequest(selectCreditCardPrompt)
//                result = true
//            }
//        }
//    }
//
//    (activePromptRequest as? SelectAddress)?.let { selectAddressPrompt ->
//        addressPicker?.let { addressPicker ->
//            if (addressDelegate.addressPickerView?.asView()?.isVisible == true) {
//                addressPicker.dismissSelectAddressRequest(selectAddressPrompt)
//                result = true
//            }
//        }
//    }
//
//    return result
//}

// todo:
///**
// * Filters and removes all [PromptRequest]s from the current Session if it it exists
// * and offers a [consume] callback for other optional side effects on each filtered [PromptRequest].
// *
// * @param sessionId Session id of the tab or custom tab in which to try consuming [PromptRequests].
// * If the id is not provided or a tab with that id is not found the method will act on the current tab.
// * @param activePrompt The current active Prompt if known. If provided it will always be cleared,
// * irrespective of if [PromptRequest] indicated by [promptRequestUID] is found and removed or not.
// * @param predicate function allowing matching only specific [PromptRequest]s from all contained in the Session.
// * @param consume callback with the [PromptRequest] if found, before being removed from the Session.
// */
//internal fun BrowserStore.consumeAllSessionPrompts(
//    sessionId: String?,
//    activePrompt: WeakReference<com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.dialog.PromptDialogFragment>? = null,
//    predicate: (PromptRequest) -> Boolean,
//    consume: (PromptRequest) -> Unit = { },
//) {
//    state.findTabOrCustomTabOrSelectedTab(sessionId)?.let { tab ->
//        activePrompt?.clear()
//        tab.content.promptRequests
//            .filter { predicate(it) }
//            .forEach {
//                consume(it)
//                dispatch(ContentAction.ConsumePromptRequestAction(tab.id, it))
//            }
//    }
//}