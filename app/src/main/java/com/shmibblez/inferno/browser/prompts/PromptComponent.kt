package com.shmibblez.inferno.browser.prompts

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentManager
import com.shmibblez.inferno.R
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.consumePromptFrom
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.dialog.PromptDialogFragment
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.facts.emitSuccessfulCreditCardAutofillFormDetectedFact
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.file.AndroidPhotoPicker
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.selector.findTabOrCustomTabOrSelectedTab
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.SessionState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.prompt.Choice
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.engine.prompt.PromptRequest.Alert
import mozilla.components.concept.engine.prompt.PromptRequest.Authentication
import mozilla.components.concept.engine.prompt.PromptRequest.BeforeUnload
import mozilla.components.concept.engine.prompt.PromptRequest.Color
import mozilla.components.concept.engine.prompt.PromptRequest.Confirm
import mozilla.components.concept.engine.prompt.PromptRequest.Dismissible
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
import mozilla.components.concept.storage.CreditCardEntry
import mozilla.components.concept.storage.CreditCardValidationDelegate
import mozilla.components.concept.storage.Login
import mozilla.components.concept.storage.LoginEntry
import mozilla.components.concept.storage.LoginValidationDelegate
import mozilla.components.feature.prompts.PromptFeature
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
import java.lang.ref.WeakReference
import java.security.InvalidParameterException
import java.util.Date

/**
 * replaces prompt integration
 * shows modal bottom sheet with requested prompt inside
 */
@Composable
private fun PromptComponent(promptRequests: List<PromptRequest>, currentTab: TabSessionState?,

                            /* private val */ container: PromptContainer,
                            /* private val */ store: BrowserStore,
                            /* private var */ customTabId: String?,
                            /* private val */ fragmentManager: FragmentManager,
                            /* private val */ identityCredentialColorsProvider: DialogColorsProvider = DialogColorsProvider {
                        DialogColors.default()
                    },
                            /* private val */ tabsUseCases: TabsUseCases,
                            /* private val */ shareDelegate: ShareDelegate,
                            /* private val */ exitFullscreenUsecase: ExitFullScreenUseCase = SessionUseCases(store).exitFullscreen,
                            /* override val */ creditCardValidationDelegate: CreditCardValidationDelegate? = null,
                            /* override val */ loginValidationDelegate: LoginValidationDelegate? = null,
                            /* private val */ isLoginAutofillEnabled: () -> Boolean = { false },
                            /* private val */ isSaveLoginEnabled: () -> Boolean = { false },
                            /* private val */ isCreditCardAutofillEnabled: () -> Boolean = { false },
                            /* private val */ isAddressAutofillEnabled: () -> Boolean = { false },
                            /* override val */ loginExceptionStorage: LoginExceptions? = null,
                            /* private val */ loginDelegate: LoginDelegate = object : LoginDelegate {},
                            /* private val */ suggestStrongPasswordDelegate: SuggestStrongPasswordDelegate = object :
                        SuggestStrongPasswordDelegate {},
                            /* private var */ shouldAutomaticallyShowSuggestedPassword: () -> Boolean = { false },
                            /* private val */ onFirstTimeEngagedWithSignup: () -> Unit = {},
                            /* private val */ onSaveLoginWithStrongPassword: (String, String) -> Unit = { _, _ -> },
                            /* private val */ onSaveLogin: (Boolean) -> Unit = { _ -> },
                            /* private val */ passwordGeneratorColorsProvider: PasswordGeneratorDialogColorsProvider =
                        PasswordGeneratorDialogColorsProvider { PasswordGeneratorDialogColors.default() },
                            /* private val */ hideUpdateFragmentAfterSavingGeneratedPassword: (String, String) -> Boolean = { _, _ -> true },
                            /* private val */ removeLastSavedGeneratedPassword: () -> Unit = {},
                            /* private val */ creditCardDelegate: CreditCardDelegate = object :
                        CreditCardDelegate {},
                            /* private val */ addressDelegate: AddressDelegate = DefaultAddressDelegate(),
                            /* private val */ fileUploadsDirCleaner: FileUploadsDirCleaner,
                            onNeedToRequestPermissions: OnNeedToRequestPermissions,
                            androidPhotoPicker: AndroidPhotoPicker?,) {
    if (currentTab == null) return
    val context = LocalContext.current
    val content = currentTab.content
    // todo: get SessionState
    val session = null // context.components.core.store.state.selectedTab.content
    val logger = remember{Logger("Prompt Component")}
//    var promptRequest by remember { mutableStateOf<PromptRequest?>(null) }
    val activePromptRequest = promptRequests.lastOrNull()

    var filePicker = FilePicker(
        container,
        store,
        customTabId,
        fileUploadsDirCleaner,
        androidPhotoPicker,
        onNeedToRequestPermissions,
    )

    when (activePromptRequest) {
        is File -> {
            emitPromptDisplayedFact(promptName = "FilePrompt")
            filePicker.handleFileRequest(activePromptRequest)
        }

        is Share -> handleShareRequest(activePromptRequest, session, activePrompt, store, promptAbuserDetector,)
        is SelectCreditCard -> {
            emitSuccessfulCreditCardAutofillFormDetectedFact()
            if (isCreditCardAutofillEnabled() && activePromptRequest.creditCards.isNotEmpty()) {
                // todo: credit card picker
//                creditCardPicker?.handleSelectCreditCardRequest(promptRequest)
            }
        }

        is SelectLoginPrompt -> {
            if (!isLoginAutofillEnabled()) {
                return
            }
            if (activePromptRequest.generatedPassword != null) {
                if (shouldAutomaticallyShowSuggestedPassword.invoke()) {
                    onFirstTimeEngagedWithSignup.invoke()
                    handleDialogsRequest(
                        activePromptRequest,
                        session,
                        customTabId,
                        container,
                        store,
                        logger
                    )
                } else {
                    // todo: strong password prompt listener
//                    strongPasswordPromptViewListener?.onGeneratedPasswordPromptClick = {
//                        handleDialogsRequest(
//                            promptRequest,
//                            session,
//                        )
//                    }
//                    strongPasswordPromptViewListener?.handleSuggestStrongPasswordRequest()
                }
            } else {
                // todo: login picker
//                loginPicker?.handleSelectLoginRequest(promptRequest)
            }
            emitPromptDisplayedFact(promptName = "SelectLoginPrompt")
        }

        is SelectAddress -> {
            emitSuccessfulAddressAutofillFormDetectedFact()
            if (isAddressAutofillEnabled() && activePromptRequest.addresses.isNotEmpty()) {
                addressPicker?.handleSelectAddressRequest(activePromptRequest)
            }
        }

        else -> {
            // todo: handle dialogs request
//            handleDialogsRequest(promptRequest, session)
        }
    }
}


/**
 * Removes the [PromptRequest] indicated by [promptRequestUID] from the current Session if it it exists
 * and offers a [consume] callback for other optional side effects.
 *
 * @param sessionId Session id of the tab or custom tab in which to try consuming [PromptRequests].
 * If the id is not provided or a tab with that id is not found the method will act on the current tab.
 * @param promptRequestUID Id of the [PromptRequest] to be consumed.
 * @param activePrompt The current active Prompt if known. If provided it will always be cleared,
 * irrespective of if [PromptRequest] indicated by [promptRequestUID] is found and removed or not.
 * @param consume callback with the [PromptRequest] if found, before being removed from the Session.
 */
internal fun BrowserStore.consumePromptFrom(
    sessionId: String?,
    promptRequestUID: String,
//    activePrompt: WeakReference<com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.dialog.PromptDialogFragment>? = null,
    consume: (PromptRequest) -> Unit,
) {
    state.findTabOrCustomTabOrSelectedTab(sessionId)?.let { tab ->
        // todo: clear active prompt
//        activePrompt?.clear()
        tab.content.promptRequests.firstOrNull { it.uid == promptRequestUID }?.let {
            consume(it)
            dispatch(ContentAction.ConsumePromptRequestAction(tab.id, it))
        }
    }
}

/**
 * Called from on [onPromptRequested] to handle requests for showing native dialogs.
 */
@Suppress("ComplexMethod", "LongMethod", "ReturnCount")
@VisibleForTesting(otherwise = PRIVATE)
internal fun handleDialogsRequest(
    promptRequest: PromptRequest,
    session: SessionState,
    customTabId: String?,
    container: PromptContainer,
    store: BrowserStore,
    logger: Logger,
) {
    // Requests that are handled with dialogs
    val dialog = when (promptRequest) {
        is SelectLoginPrompt -> {
            val currentUrl =
                store.state.findTabOrCustomTabOrSelectedTab(customTabId)?.content?.url
            val generatedPassword = promptRequest.generatedPassword

            if (generatedPassword == null || currentUrl == null) {
                logger.debug(
                    "Ignoring received SelectLogin.onGeneratedPasswordPromptClick" +
                            " when either the generated password or the currentUrl is null.",
                )
                dismissDialogRequest(promptRequest, session)
                return
            }

            emitGeneratedPasswordShownFact()

            PasswordGeneratorDialogFragment.newInstance(
                sessionId = session.id,
                promptRequestUID = promptRequest.uid,
                generatedPassword = generatedPassword,
                currentUrl = currentUrl,
                onSavedGeneratedPassword = onSaveLogin,
                colorsProvider = passwordGeneratorColorsProvider,
            )
        }

        is SaveCreditCard -> {
            if (!isCreditCardAutofillEnabled.invoke() || creditCardValidationDelegate == null ||
                !promptRequest.creditCard.isValid
            ) {
                dismissDialogRequest(promptRequest, session)

                if (creditCardValidationDelegate == null) {
                    logger.debug(
                        "Ignoring received SaveCreditCard because PromptFeature." +
                                "creditCardValidationDelegate is null. If you are trying to autofill " +
                                "credit cards, try attaching a CreditCardValidationDelegate to PromptFeature",
                    )
                }

                return
            }

            emitCreditCardSaveShownFact()

            CreditCardSaveDialogFragment.newInstance(
                sessionId = session.id,
                promptRequestUID = promptRequest.uid,
                shouldDismissOnLoad = false,
                creditCard = promptRequest.creditCard,
            )
        }

        is SaveLoginPrompt -> {
            if (!isSaveLoginEnabled.invoke() || loginValidationDelegate == null) {
                dismissDialogRequest(promptRequest, session)

                if (loginValidationDelegate == null) {
                    logger.debug(
                        "Ignoring received SaveLoginPrompt because PromptFeature." +
                                "loginValidationDelegate is null. If you are trying to autofill logins, " +
                                "try attaching a LoginValidationDelegate to PromptFeature",
                    )
                }

                return
            } else if (hideUpdateFragmentAfterSavingGeneratedPassword(
                    promptRequest.logins[0].username,
                    promptRequest.logins[0].password,
                )
            ) {
                removeLastSavedGeneratedPassword()
                dismissDialogRequest(promptRequest, session)

                return
            }

            SaveLoginDialogFragment.newInstance(
                sessionId = session.id,
                promptRequestUID = promptRequest.uid,
                shouldDismissOnLoad = false,
                hint = promptRequest.hint,
                // For v1, we only handle a single login and drop all others on the floor
                entry = promptRequest.logins[0],
                icon = session.content.icon,
                onShowSnackbarAfterLoginChange = onSaveLogin,
            )
        }

        is SingleChoice -> ChoiceDialogFragment.newInstance(
            promptRequest.choices,
            session.id,
            promptRequest.uid,
            true,
            SINGLE_CHOICE_DIALOG_TYPE,
        )

        is MultipleChoice -> ChoiceDialogFragment.newInstance(
            promptRequest.choices,
            session.id,
            promptRequest.uid,
            true,
            MULTIPLE_CHOICE_DIALOG_TYPE,
        )

        is MenuChoice -> ChoiceDialogFragment.newInstance(
            promptRequest.choices,
            session.id,
            promptRequest.uid,
            true,
            MENU_CHOICE_DIALOG_TYPE,
        )

        is Alert -> {
            with(promptRequest) {
                AlertDialogFragment.newInstance(
                    session.id,
                    promptRequest.uid,
                    true,
                    title,
                    message,
                    promptAbuserDetector.areDialogsBeingAbused(),
                )
            }
        }

        is TimeSelection -> {
            val selectionType = when (promptRequest.type) {
                TimeSelection.Type.DATE -> TimePickerDialogFragment.SELECTION_TYPE_DATE
                TimeSelection.Type.DATE_AND_TIME -> TimePickerDialogFragment.SELECTION_TYPE_DATE_AND_TIME
                TimeSelection.Type.TIME -> TimePickerDialogFragment.SELECTION_TYPE_TIME
                TimeSelection.Type.MONTH -> TimePickerDialogFragment.SELECTION_TYPE_MONTH
            }

            with(promptRequest) {
                TimePickerDialogFragment.newInstance(
                    session.id,
                    promptRequest.uid,
                    true,
                    initialDate,
                    minimumDate,
                    maximumDate,
                    selectionType,
                    stepValue,
                )
            }
        }

        is TextPrompt -> {
            with(promptRequest) {
                TextPromptDialogFragment.newInstance(
                    session.id,
                    promptRequest.uid,
                    true,
                    title,
                    inputLabel,
                    inputValue,
                    promptAbuserDetector.areDialogsBeingAbused(),
                    store.state.selectedTab?.content?.private == true,
                )
            }
        }

        is Authentication -> {
            with(promptRequest) {
                AuthenticationDialogFragment.newInstance(
                    session.id,
                    promptRequest.uid,
                    true,
                    title,
                    message,
                    userName,
                    password,
                    onlyShowPassword,
                    uri,
                )
            }
        }

        is Color -> ColorPickerDialogFragment.newInstance(
            session.id,
            promptRequest.uid,
            true,
            promptRequest.defaultColor,
        )

        is Popup -> {
            val title = container.getString(R.string.mozac_feature_prompts_popup_dialog_title)
            val positiveLabel = container.getString(R.string.mozac_feature_prompts_allow)
            val negativeLabel = container.getString(R.string.mozac_feature_prompts_deny)

            ConfirmDialogFragment.newInstance(
                sessionId = session.id,
                promptRequest.uid,
                title = title,
                message = promptRequest.targetUri,
                positiveButtonText = positiveLabel,
                negativeButtonText = negativeLabel,
                hasShownManyDialogs = promptAbuserDetector.areDialogsBeingAbused(),
                shouldDismissOnLoad = true,
            )
        }

        is BeforeUnload -> {
            val title =
                container.getString(R.string.mozac_feature_prompt_before_unload_dialog_title)
            val body =
                container.getString(R.string.mozac_feature_prompt_before_unload_dialog_body)
            val leaveLabel =
                container.getString(R.string.mozac_feature_prompts_before_unload_leave)
            val stayLabel =
                container.getString(R.string.mozac_feature_prompts_before_unload_stay)

            ConfirmDialogFragment.newInstance(
                sessionId = session.id,
                promptRequest.uid,
                title = title,
                message = body,
                positiveButtonText = leaveLabel,
                negativeButtonText = stayLabel,
                shouldDismissOnLoad = true,
            )
        }

        is Confirm -> {
            with(promptRequest) {
                val positiveButton = positiveButtonTitle.ifEmpty {
                    container.getString(R.string.mozac_feature_prompts_ok)
                }
                val negativeButton = negativeButtonTitle.ifEmpty {
                    container.getString(R.string.mozac_feature_prompts_cancel)
                }

                MultiButtonDialogFragment.newInstance(
                    session.id,
                    promptRequest.uid,
                    title,
                    message,
                    promptAbuserDetector.areDialogsBeingAbused(),
                    false,
                    positiveButton,
                    negativeButton,
                    neutralButtonTitle,
                )
            }
        }

        is Repost -> {
            val title = container.context.getString(R.string.mozac_feature_prompt_repost_title)
            val message =
                container.context.getString(R.string.mozac_feature_prompt_repost_message)
            val positiveAction =
                container.context.getString(R.string.mozac_feature_prompt_repost_positive_button_text)
            val negativeAction =
                container.context.getString(R.string.mozac_feature_prompt_repost_negative_button_text)

            ConfirmDialogFragment.newInstance(
                sessionId = session.id,
                promptRequestUID = promptRequest.uid,
                shouldDismissOnLoad = true,
                title = title,
                message = message,
                positiveButtonText = positiveAction,
                negativeButtonText = negativeAction,
            )
        }

        is PromptRequest.IdentityCredential.SelectProvider -> {
            // todo: implement SelectProviderDialogFragment
//            SelectProviderDialogFragment.newInstance(
//                sessionId = session.id,
//                promptRequestUID = promptRequest.uid,
//                shouldDismissOnLoad = true,
//                providers = promptRequest.providers,
//                colorsProvider = identityCredentialColorsProvider,
//            )
        }

        is PromptRequest.IdentityCredential.SelectAccount -> {
            // todo: implement SelectAccountDialogFragment
//            SelectAccountDialogFragment.newInstance(
//                sessionId = session.id,
//                promptRequestUID = promptRequest.uid,
//                shouldDismissOnLoad = true,
//                accounts = promptRequest.accounts,
//                provider = promptRequest.provider,
//                colorsProvider = identityCredentialColorsProvider,
//            )
        }

        is PromptRequest.IdentityCredential.PrivacyPolicy -> {
            val title =
                container.getString(
                    R.string.mozac_feature_prompts_identity_credentials_privacy_policy_title,
                    promptRequest.providerDomain,
                )
            val message =
                container.getString(
                    R.string.mozac_feature_prompts_identity_credentials_privacy_policy_description,
                    promptRequest.host,
                    promptRequest.providerDomain,
                    promptRequest.privacyPolicyUrl,
                    promptRequest.termsOfServiceUrl,
                )
            // todo: implement PrivacyPolicyDialogFragment
//            PrivacyPolicyDialogFragment.newInstance(
//                sessionId = session.id,
//                promptRequestUID = promptRequest.uid,
//                shouldDismissOnLoad = true,
//                title = title,
//                message = message,
//                icon = promptRequest.icon,
//            )
        }

        else -> throw InvalidParameterException("Not valid prompt request type $promptRequest")
    }

//    dialog.feature = this

    if (canShowThisPrompt(promptRequest)) {
        // If the ChoiceDialogFragment's choices data were updated,
        // we need to dismiss the previous dialog
        activePrompt?.get()?.let { promptDialog ->
            // ChoiceDialogFragment could update their choices data,
            // and we need to dismiss the previous UI dialog,
            // without consuming the engine callbacks, and allow to create a new dialog with the
            // updated data.
            if (promptDialog is ChoiceDialogFragment &&
                !session.content.promptRequests.any { it.uid == promptDialog.promptRequestUID }
            ) {
                // We want to avoid consuming the engine callbacks and allow a new dialog
                // to be created with the updated data.
                promptDialog.feature = null
                promptDialog.dismiss()
            }
        }

        emitPromptDisplayedFact(promptName = dialog::class.simpleName.ifNullOrEmpty { "" })
        // todo: show dialog
//        dialog.show(fragmentManager, FRAGMENT_TAG)
        activePrompt = WeakReference(dialog)

        if (promptRequest.shouldDismissOnLoad) {
            activePromptsToDismiss.add(dialog)
        }
    } else {
        dismissDialogRequest(promptRequest, session)
    }
    promptAbuserDetector.updateJSDialogAbusedState()
}

private fun handleShareRequest(promptContainer: PromptContainer, promptRequest: Share, session: SessionState,  activePrompt: WeakReference<PromptDialogFragment>?, store: BrowserStore, promptAbuserDetector: PromptAbuserDetector) {
    emitPromptDisplayedFact(promptName = "ShareSheet")
    // todo: show share sheet
//    shareDelegate.showShareSheet(
//        context = promptContainer.context,
//        shareData = promptRequest.data,
//        onDismiss = {
//            emitPromptDismissedFact(promptName = "ShareSheet")
//            onCancel(session.id, promptRequest.uid, activePrompt, store, promptAbuserDetector)
//        },
//        onSuccess = { onConfirm(session.id, promptRequest.uid, null) },
//    )
}

private fun handleShareRequest(promptRequest: Share, session: SessionState,  activePrompt: WeakReference<PromptDialogFragment>?, store: BrowserStore, promptAbuserDetector: PromptAbuserDetector,) {
    emitPromptDisplayedFact(promptName = "ShareSheet")
    shareDelegate.showShareSheet(
        context = container.context,
        shareData = promptRequest.data,
        onDismiss = {
            emitPromptDismissedFact(promptName = "ShareSheet")
            onCancel(session.id, promptRequest.uid, activePrompt, store, promptAbuserDetector)
        },
        onSuccess = { onConfirm(session.id, promptRequest.uid, activePrompt, store, promptAbuserDetector,null) },
    )
}

private fun canShowThisPrompt(promptRequest: PromptRequest, promptAbuserDetector: PromptAbuserDetector): Boolean {
    return when (promptRequest) {
        is SingleChoice,
        is MultipleChoice,
        is MenuChoice,
        is TimeSelection,
        is File,
        is Color,
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

/**
 * Invoked when a dialog is dismissed. This consumes the [PromptFeature]
 * value from the session indicated by [sessionId].
 *
 * @param sessionId this is the id of the session which requested the prompt.
 * @param promptRequestUID identifier of the [PromptRequest] for which this dialog was shown.
 * @param value an optional value provided by the dialog as a result of canceling the action.
 */
private fun onCancel(sessionId: String, promptRequestUID: String, activePrompt: WeakReference<PromptDialogFragment>?, store: BrowserStore, promptAbuserDetector: PromptAbuserDetector, value: Any?) {
    store.consumePromptFrom(sessionId, promptRequestUID, activePrompt) {
        emitPromptDismissedFact(promptName = it::class.simpleName.ifNullOrEmpty { "" })
        when (it) {
            is BeforeUnload -> it.onStay()
            is Popup -> {
                val shouldNotShowMoreDialogs = value as Boolean
                promptAbuserDetector.userWantsMoreDialogs(!shouldNotShowMoreDialogs)
                it.onDeny()
            }

            is Dismissible -> it.onDismiss()
            else -> {
                // no-op
            }
        }
    }
}


/**
 * Invoked when the user confirms the action on the dialog. This consumes
 * the [PromptFeature] value from the [SessionState] indicated by [sessionId].
 *
 * @param sessionId that requested to show the dialog.
 * @param promptRequestUID identifier of the [PromptRequest] for which this dialog was shown.
 * @param value an optional value provided by the dialog as a result of confirming the action.
 */
@Suppress("UNCHECKED_CAST", "ComplexMethod")
private fun onConfirm(sessionId: String, promptRequestUID: String,  activePrompt: WeakReference<PromptDialogFragment>?, store: BrowserStore, promptAbuserDetector: PromptAbuserDetector, value: Any?) {
    store.consumePromptFrom(sessionId, promptRequestUID, activePrompt) {
        when (it) {
            is TimeSelection -> it.onConfirm(value as Date)
            is Color -> it.onConfirm(value as String)
            is Alert -> {
                val shouldNotShowMoreDialogs = value as Boolean
                promptAbuserDetector.userWantsMoreDialogs(!shouldNotShowMoreDialogs)
                it.onConfirm(!shouldNotShowMoreDialogs)
            }

            is SingleChoice -> it.onConfirm(value as Choice)
            is MenuChoice -> it.onConfirm(value as Choice)
            is BeforeUnload -> it.onLeave()
            is Popup -> {
                val shouldNotShowMoreDialogs = value as Boolean
                promptAbuserDetector.userWantsMoreDialogs(!shouldNotShowMoreDialogs)
                it.onAllow()
            }

            is MultipleChoice -> it.onConfirm(value as Array<Choice>)

            is Authentication -> {
                val (user, password) = value as Pair<String, String>
                it.onConfirm(user, password)
            }

            is TextPrompt -> {
                val (shouldNotShowMoreDialogs, text) = value as Pair<Boolean, String>

                promptAbuserDetector.userWantsMoreDialogs(!shouldNotShowMoreDialogs)
                it.onConfirm(!shouldNotShowMoreDialogs, text)
            }

            is Share -> it.onSuccess()

            is SaveCreditCard -> it.onConfirm(value as CreditCardEntry)
            is SaveLoginPrompt -> it.onConfirm(value as LoginEntry)

            is Confirm -> {
                // todo: confirm dialog
//                val (isCheckBoxChecked, buttonType) =
//                    value as Pair<Boolean, MultiButtonDialogFragment.ButtonType>
//                promptAbuserDetector.userWantsMoreDialogs(!isCheckBoxChecked)
//                when (buttonType) {
//                    MultiButtonDialogFragment.ButtonType.POSITIVE ->
//                        it.onConfirmPositiveButton(!isCheckBoxChecked)
//
//                    MultiButtonDialogFragment.ButtonType.NEGATIVE ->
//                        it.onConfirmNegativeButton(!isCheckBoxChecked)
//
//                    MultiButtonDialogFragment.ButtonType.NEUTRAL ->
//                        it.onConfirmNeutralButton(!isCheckBoxChecked)
//                }
            }

            is Repost -> it.onConfirm()
            is PromptRequest.IdentityCredential.SelectProvider -> it.onConfirm(value as Provider)
            is PromptRequest.IdentityCredential.SelectAccount -> it.onConfirm(value as Account)
            is PromptRequest.IdentityCredential.PrivacyPolicy -> it.onConfirm(value as Boolean)
            is SelectLoginPrompt -> it.onConfirm(value as Login)
            else -> {
                // no-op
            }
        }
        emitPromptConfirmedFact(it::class.simpleName.ifNullOrEmpty { "" })
    }
}

/**
 * Invoked when the user is requesting to clear the selected value from the dialog.
 * This consumes the [PromptFeature] value from the [SessionState] indicated by [sessionId].
 *
 * @param sessionId that requested to show the dialog.
 * @param promptRequestUID identifier of the [PromptRequest] for which this dialog was shown.
 */
private fun onClear(sessionId: String, promptRequestUID: String,  activePrompt: WeakReference<PromptDialogFragment>?, store: BrowserStore) {
    store.consumePromptFrom(sessionId, promptRequestUID, activePrompt) {
        when (it) {
            is TimeSelection -> it.onClear()
            else -> {
                // no-op
            }
        }
    }
}
