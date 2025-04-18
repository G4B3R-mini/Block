package com.shmibblez.inferno.browser.prompts

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.webPrompts.FilePicker
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.AlertDialogPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.AuthenticationPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.ChoiceDialogPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.ColorPickerDialogPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.ConfirmDialogPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.CreditCardSaveDialogPrompt
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.GeneratePasswordPrompt
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
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
import mozilla.components.concept.storage.CreditCardValidationDelegate
import mozilla.components.concept.storage.Login
import mozilla.components.concept.storage.LoginEntry
import mozilla.components.concept.storage.LoginValidationDelegate
import mozilla.components.feature.prompts.share.ShareDelegate
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.kotlin.ifNullOrEmpty
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifAnyChanged
import mozilla.components.support.ktx.util.PromptAbuserDetector
import java.util.Date

// todo:
//   - colors
//   - some prompts not working nicely, maybe not dismissing properly
//   - move file picker implementation here, make clean with callbacks and such
//   - implement share dialog in compose
//   - some minor problems in some components, go to ones with todos in their files
//   - check usage of parameters that are not in use for [PromptComponent] (commented out)
//

/**
 * replaces prompt integration
 * shows modal bottom sheet with requested prompt inside
 */
@Composable
fun PromptComponent(
    setPromptRequests: (List<PromptRequest>) -> Unit,
    currentTab: TabSessionState?,

//    /* private val */
//    container: PromptContainer,
//    /* private val */
//    store: BrowserStore,
    /* private var */
    customTabId: String?,
//    /* private val */
//    fragmentManager: FragmentManager,
    filePicker: FilePicker?,
//    /* private val */
//    identityCredentialColorsProvider: DialogColorsProvider = DialogColorsProvider {
//        DialogColors.default()
//    },
//    /* private val */
//    tabsUseCases: TabsUseCases,
    /* private val */
    shareDelegate: ShareDelegate,
//    /* private val */
//    exitFullscreenUsecase: ExitFullScreenUseCase = SessionUseCases(store).exitFullscreen,
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
//    /* override val */
//    loginExceptionStorage: LoginExceptions? = null,
//    /* private val */
//    loginDelegate: LoginDelegate = object : LoginDelegate {},
//    /* private val */
//    suggestStrongPasswordDelegate: SuggestStrongPasswordDelegate = object :
//        SuggestStrongPasswordDelegate {},
    /* private var */
    shouldAutomaticallyShowSuggestedPassword: () -> Boolean = { false },
//    /* private val */
    onFirstTimeEngagedWithSignup: () -> Unit = {},
//    /* private val */
//    onSaveLoginWithStrongPassword: (String, String) -> Unit = { _, _ -> },
    /* private val */
    onSaveLogin: (Boolean) -> Unit = { _ -> },
//    /* private val */
//    passwordGeneratorColorsProvider: PasswordGeneratorDialogColorsProvider = PasswordGeneratorDialogColorsProvider { PasswordGeneratorDialogColors.default() },
    /* private val */
    hideUpdateFragmentAfterSavingGeneratedPassword: (String, String) -> Boolean = { _, _ -> true },
    /* private val */
    removeLastSavedGeneratedPassword: () -> Unit = {},
//    /* private val */
//    creditCardDelegate: CreditCardDelegate = object : CreditCardDelegate {},
//    /* private val */
//    addressDelegate: AddressDelegate = DefaultAddressDelegate(),
//    /* private val */
//    fileUploadsDirCleaner: FileUploadsDirCleaner,
//    onNeedToRequestPermissions: OnNeedToRequestPermissions,
//    androidPhotoPicker: AndroidPhotoPicker?,
) {
    if (currentTab == null) return
    val context = LocalContext.current
    val logger = remember { Logger("Prompt Component") }
//    var promptRequest by remember { mutableStateOf<PromptRequest?>(null) }
    val sessionId = currentTab.id
    val promptAbuserDetector = remember { PromptAbuserDetector() }

    val store = LocalContext.current.components.core.store
    var listener by remember { mutableStateOf<CoroutineScope?>(null) }
    var promptRequests by remember { mutableStateOf<List<PromptRequest>>(emptyList()) }

    DisposableEffect(null) {
        listener = store.flowScoped { flow ->
            flow.map { state -> state.findTabOrCustomTabOrSelectedTab(customTabId) }.ifAnyChanged {
                arrayOf(it?.content?.promptRequests, it?.content?.loading)
            }.collect { state ->
                state?.content?.let { content ->
                    // for testing:
                    // listOf(PromptComponentTestObjs.file, PromptComponentTestObjs.share, PromptComponentTestObjs.selectLoginPrompt)
                    promptRequests = content.promptRequests
                    setPromptRequests(promptRequests)
                }

            }
        }
        onDispose {
            listener?.cancel()
        }
    }

    // show prompts one above the other, first one on top
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
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

                is Authentication -> {
                    AuthenticationPrompt(prompt, sessionId)
                }

                is BeforeUnload -> {
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

                is File -> {
//                    emitPromptDisplayedFact(promptName = "FilePrompt")
                    filePicker?.handleFileRequest(prompt)
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

                is IdentityCredential.SelectAccount -> {
                    SelectAccountDialogPrompt(prompt, sessionId)
                }

                is IdentityCredential.SelectProvider -> {
                    SelectProviderDialogPrompt(prompt, sessionId)
                }

                is MenuChoice -> {
                    ChoiceDialogPrompt(prompt, sessionId, MENU_CHOICE_DIALOG_TYPE)
                }

                is MultipleChoice -> {
                    ChoiceDialogPrompt(prompt, sessionId, MULTIPLE_CHOICE_DIALOG_TYPE)
                }

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

                is SaveCreditCard -> {
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

                is SaveLoginPrompt -> {
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

                is SelectAddress -> {
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
//                    emitSuccessfulAddressAutofillFormDetectedFact()
                    Log.d(
                        "PromptComponent",
                        "isAddressAutofillEnabled: ${isAddressAutofillEnabled()}, addresses size: ${prompt.addresses.size}"
                    )
                    if (prompt.addresses.isNotEmpty()) {
//                    addressPicker?.handleSelectAddressRequest(prompt)
                        SelectableListPrompt(
                            prompt,
                            sessionId,
                            header = stringResource(R.string.mozac_feature_prompts_select_address_2),
                            manageText = stringResource(R.string.mozac_feature_prompts_manage_address),
                        )
                    }
                }

                is SelectCreditCard -> {
//                    emitSuccessfulCreditCardAutofillFormDetectedFact()
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

                is SelectLoginPrompt -> {
                    if (!isLoginAutofillEnabled()) {
                        return
                    }
                    if (prompt.generatedPassword != null) {
                        if (shouldAutomaticallyShowSuggestedPassword.invoke()) {
                            onFirstTimeEngagedWithSignup.invoke()
//                        handleDialogsRequest(
//                            promptRequest,
//                            session,
//                        )
                            val currentUrl =
                                store.state.findTabOrCustomTabOrSelectedTab(customTabId)?.content?.url
//                            val generatedPassword = prompt.generatedPassword

//                            if (generatedPassword == null || currentUrl == null) {
                            if (currentUrl == null) {
                                logger.debug(
                                    "Ignoring received SelectLogin.onGeneratedPasswordPromptClick" + " when either the generated password or the currentUrl is null.",
                                )
                                dismissDialogRequest(prompt, sessionId, store)
                                return
                            }
//                            emitGeneratedPasswordShownFact()
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
                            val currentUrl =
                                store.state.findTabOrCustomTabOrSelectedTab(customTabId)?.content?.url
//                            val generatedPassword = prompt.generatedPassword

//                            if (generatedPassword == null || currentUrl == null) {
                            if (currentUrl == null) {
                                logger.debug(
                                    "Ignoring received SelectLogin.onGeneratedPasswordPromptClick" + " when either the generated password or the currentUrl is null.",
                                )
                                dismissDialogRequest(prompt, sessionId, store)
                                return
                            }
//                            emitGeneratedPasswordShownFact()
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
//                        emitLoginAutofillShownFact()
                        LoginPickerPrompt(prompt, sessionId)
//                    loginPicker?.handleSelectLoginRequest(promptRequest)
                    }
//                    emitPromptDisplayedFact(promptName = "SelectLoginPrompt")
                }

                is Share -> {
//                    emitPromptDisplayedFact(promptName = "ShareSheet")
                    // todo: customize this and make bottom prompt sheet
                    shareDelegate.showShareSheet(
                        context = context,
                        shareData = prompt.data,
                        onDismiss = {
//                            emitPromptDismissedFact(promptName = "ShareSheet")
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

                is SingleChoice -> {
                    ChoiceDialogPrompt(prompt, sessionId, SINGLE_CHOICE_DIALOG_TYPE)
                }

                is TextPrompt -> {
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

@SuppressWarnings("UNCHECKED_CAST")
fun onPositiveAction(promptRequest: PromptRequest, value: Any? = null, value2: Any? = null) {
    when (promptRequest) {
        is MultipleChoice -> promptRequest.onConfirm.invoke((value as HashMap<Choice, Choice>).keys.toTypedArray())
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