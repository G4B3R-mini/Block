package com.shmibblez.inferno.browser.prompts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.shmibblez.inferno.browser.infernoFeatureState.InfernoFeatureState
import com.shmibblez.inferno.browser.prompts.address.InfernoAddressPicker
import com.shmibblez.inferno.browser.prompts.creditcard.InfernoCreditCardPicker
import com.shmibblez.inferno.browser.prompts.login.InfernoLoginDelegate
import com.shmibblez.inferno.browser.prompts.login.InfernoStrongPasswordPromptStateListener
import com.shmibblez.inferno.browser.prompts.login.SelectLoginPromptController
import com.shmibblez.inferno.browser.prompts.webPrompts.AndroidPhotoPicker
import com.shmibblez.inferno.browser.prompts.webPrompts.FilePicker
import com.shmibblez.inferno.browser.prompts.webPrompts.FileUploadsDirCleaner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.selector.findTabOrCustomTabOrSelectedTab
import mozilla.components.browser.state.state.SessionState
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
import mozilla.components.feature.prompts.login.LoginExceptions
import mozilla.components.feature.prompts.login.PasswordGeneratorDialogColors
import mozilla.components.feature.prompts.login.PasswordGeneratorDialogColorsProvider
import mozilla.components.feature.prompts.login.SuggestStrongPasswordDelegate
import mozilla.components.feature.prompts.share.ShareDelegate
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.session.SessionUseCases.ExitFullScreenUseCase
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.ActivityResultHandler
import mozilla.components.support.base.feature.OnNeedToRequestPermissions
import mozilla.components.support.base.feature.PermissionsFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.kotlin.ifNullOrEmpty
import mozilla.components.support.ktx.kotlin.tryGetHostFromUrl
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifAnyChanged
import mozilla.components.support.ktx.util.PromptAbuserDetector
import java.security.InvalidParameterException
import java.util.Collections
import java.util.Date

@Composable
fun rememberInfernoPromptFeatureState(
    activity: AppCompatActivity,
    store: BrowserStore,
    customTabId: String?,
    fragmentManager: FragmentManager,
    identityCredentialColorsProvider: DialogColorsProvider = DialogColorsProvider {
        DialogColors.default()
    },
    tabsUseCases: TabsUseCases,
    shareDelegate: ShareDelegate,
    exitFullscreenUsecase: ExitFullScreenUseCase = SessionUseCases(store).exitFullscreen,
    creditCardValidationDelegate: CreditCardValidationDelegate? = null,
    loginValidationDelegate: LoginValidationDelegate? = null,
    isLoginAutofillEnabled: () -> Boolean = { false },
    isSaveLoginEnabled: () -> Boolean = { false },
    isCreditCardAutofillEnabled: () -> Boolean = { false },
    isAddressAutofillEnabled: () -> Boolean = { false },
    loginExceptionStorage: LoginExceptions? = null,
    loginDelegate: InfernoLoginDelegate = object : InfernoLoginDelegate {},
    suggestStrongPasswordDelegate: SuggestStrongPasswordDelegate = object :
        SuggestStrongPasswordDelegate {},
    shouldAutomaticallyShowSuggestedPassword: () -> Boolean = { false },
    onFirstTimeEngagedWithSignup: () -> Unit = {},
    onSaveLoginWithStrongPassword: (String, String) -> Unit = { _, _ -> },
    onSaveLogin: (Boolean) -> Unit = { _ -> },
    passwordGeneratorColorsProvider: PasswordGeneratorDialogColorsProvider = PasswordGeneratorDialogColorsProvider { PasswordGeneratorDialogColors.default() },
    hideUpdateFragmentAfterSavingGeneratedPassword: (String, String) -> Boolean = { _, _ -> true },
    removeLastSavedGeneratedPassword: () -> Unit = {},
    creditCardDelegate: CreditCardDelegate = object : CreditCardDelegate {},
    addressDelegate: AddressDelegate = DefaultAddressDelegate(),
    fileUploadsDirCleaner: FileUploadsDirCleaner,
    onNeedToRequestPermissions: OnNeedToRequestPermissions,
    androidPhotoPicker: AndroidPhotoPicker?,
): InfernoPromptFeatureState {
    val state = InfernoPromptFeatureState(
        activity = activity,
        store = store,
        customTabId = customTabId,
        fragmentManager = fragmentManager,
        identityCredentialColorsProvider = identityCredentialColorsProvider,
        tabsUseCases = tabsUseCases,
        shareDelegate = shareDelegate,
        exitFullscreenUsecase = exitFullscreenUsecase,
        creditCardValidationDelegate = creditCardValidationDelegate,
        loginValidationDelegate = loginValidationDelegate,
        isLoginAutofillEnabled = isLoginAutofillEnabled,
        isSaveLoginEnabled = isSaveLoginEnabled,
        isCreditCardAutofillEnabled = isCreditCardAutofillEnabled,
        isAddressAutofillEnabled = isAddressAutofillEnabled,
        loginExceptionStorage = loginExceptionStorage,
        loginDelegate = loginDelegate,
        suggestStrongPasswordDelegate = suggestStrongPasswordDelegate,
        shouldAutomaticallyShowSuggestedPassword = shouldAutomaticallyShowSuggestedPassword,
        onFirstTimeEngagedWithSignup = onFirstTimeEngagedWithSignup,
        onSaveLoginWithStrongPassword = onSaveLoginWithStrongPassword,
        onSaveLogin = onSaveLogin,
        passwordGeneratorColorsProvider = passwordGeneratorColorsProvider,
        hideUpdateFragmentAfterSavingGeneratedPassword = hideUpdateFragmentAfterSavingGeneratedPassword,
        removeLastSavedGeneratedPassword = removeLastSavedGeneratedPassword,
        creditCardDelegate = creditCardDelegate,
        addressDelegate = addressDelegate,
        fileUploadsDirCleaner = fileUploadsDirCleaner,
        onNeedToRequestPermissions = onNeedToRequestPermissions,
        androidPhotoPicker = androidPhotoPicker,
    )

    DisposableEffect(null) {
        state.start()

        onDispose {
            state.stop()
        }
    }

    return remember {
        state
    }
}

// todo: move everything here so can implement biometric feature, add SecurityHandler interface
//   for biometric features, could also be called BiometricHandler
class InfernoPromptFeatureState internal constructor(
    private val activity: AppCompatActivity,
    private val store: BrowserStore,
    private var customTabId: String?,
    private val fragmentManager: FragmentManager,
    private val identityCredentialColorsProvider: DialogColorsProvider = DialogColorsProvider {
        DialogColors.default()
    },
    private val tabsUseCases: TabsUseCases,
    private val shareDelegate: ShareDelegate,
    private val exitFullscreenUsecase: ExitFullScreenUseCase = SessionUseCases(store).exitFullscreen,
    override val creditCardValidationDelegate: CreditCardValidationDelegate? = null,
    override val loginValidationDelegate: LoginValidationDelegate? = null,
    private val isLoginAutofillEnabled: () -> Boolean = { false },
    private val isSaveLoginEnabled: () -> Boolean = { false },
    private val isCreditCardAutofillEnabled: () -> Boolean = { false },
    private val isAddressAutofillEnabled: () -> Boolean = { false },
    override val loginExceptionStorage: LoginExceptions? = null,
    private val loginDelegate: InfernoLoginDelegate = object : InfernoLoginDelegate {},
    private val suggestStrongPasswordDelegate: SuggestStrongPasswordDelegate = object :
        SuggestStrongPasswordDelegate {},
    private var shouldAutomaticallyShowSuggestedPassword: () -> Boolean = { false },
    private val onFirstTimeEngagedWithSignup: () -> Unit = {},
    private val onSaveLoginWithStrongPassword: (String, String) -> Unit = { _, _ -> },
    internal val onSaveLogin: (Boolean) -> Unit = { _ -> },
    private val passwordGeneratorColorsProvider: PasswordGeneratorDialogColorsProvider = PasswordGeneratorDialogColorsProvider { PasswordGeneratorDialogColors.default() },
    private val hideUpdateFragmentAfterSavingGeneratedPassword: (String, String) -> Boolean = { _, _ -> true },
    private val removeLastSavedGeneratedPassword: () -> Unit = {},
    private val creditCardDelegate: CreditCardDelegate = object : CreditCardDelegate {},
    private val addressDelegate: AddressDelegate = DefaultAddressDelegate(),
    private val fileUploadsDirCleaner: FileUploadsDirCleaner,
    onNeedToRequestPermissions: OnNeedToRequestPermissions,
    androidPhotoPicker: AndroidPhotoPicker?,
) : InfernoFeatureState, PermissionsFeature, InfernoPrompter, ActivityResultHandler,
    UserInteractionHandler {

    // These three scopes have identical lifetimes. We do not yet have a way of combining scopes
    private var handlePromptScope: CoroutineScope? = null
    private var dismissPromptScope: CoroutineScope? = null

    @VisibleForTesting
    var activePromptRequest: PromptRequest? = null
        private set(value) {
            if (value == null) {
                // if null also set visiblePrompt to null
                // hides prompt in InfernoWebPrompter
                visiblePrompt = null
            }
            field = value
        }

    var currentUrl: String? = null
        private set

    private val promptAbuserDetector = PromptAbuserDetector()
    private val logger = Logger("PromptFeature")

    // prompt that will be shown by InfernoWebPrompter
    internal var visiblePrompt: PromptRequest? = null

    // This set of weak references of fragments is only used for dismissing all prompts on navigation.
    // For all other code only `activePrompt` is tracked for now.
    @VisibleForTesting(otherwise = PRIVATE)
    internal val activePromptsToDismiss =
        Collections.newSetFromMap(HashMap<PromptRequest, Boolean>())

    /** private constructor here **/

    @VisibleForTesting
    // var for testing purposes
    internal var filePicker = FilePicker(
        activity,
        store,
        customTabId,
        fileUploadsDirCleaner,
        androidPhotoPicker,
        onNeedToRequestPermissions,
    )

    var selectLoginPromptController = SelectLoginPromptController(

    )

    //    @VisibleForTesting(otherwise = PRIVATE)
//    internal var strongPasswordPromptViewListener =
//        with(suggestStrongPasswordDelegate) {
//            strongPasswordPromptViewListenerView?.let {
//                StrongPasswordPromptViewListener(store, it, customTabId)
//            }
//        }

    //    @VisibleForTesting(otherwise = PRIVATE)
//    internal var creditCardPicker =
//        with(creditCardDelegate) {
//            creditCardPickerView?.let {
//                CreditCardPicker(
//                    store = store,
//                    creditCardSelectBar = it,
//                    manageCreditCardsCallback = onManageCreditCards,
//                    selectCreditCardCallback = onSelectCreditCard,
//                    sessionId = customTabId,
//                )
//            }
//        }
    // todo: implement as state to integrate with credit card picker component,
    //  store value for biometric auth, reset when dismissed
    @VisibleForTesting(otherwise = PRIVATE)
    internal var creditCardPicker = with(creditCardDelegate) {
        InfernoCreditCardPicker(
            store = store,
            manageCreditCardsCallback = onManageCreditCards,
            selectCreditCardCallback = onSelectCreditCard,
            sessionId = customTabId,
        )
    }

    //    @VisibleForTesting(otherwise = PRIVATE)
//    internal var addressPicker =
//        with(addressDelegate) {
//            addressPickerView?.let {
//                AddressPicker(
//                    store = store,
//                    addressSelectBar = it,
//                    onManageAddresses = onManageAddresses,
//                    sessionId = customTabId,
//                )
//            }
//        }
    @VisibleForTesting(otherwise = PRIVATE)
    internal var addressPicker = with(addressDelegate) {
        InfernoAddressPicker(
            store = store,
            onManageAddresses = onManageAddresses,
            sessionId = customTabId,
        )
    }

    override val onNeedToRequestPermissions
        get() = filePicker.onNeedToRequestPermissions

    override fun onOpenLink(url: String) {
        tabsUseCases.addTab(
            url = url,
        )
    }

    /**
     * Starts observing the selected session to listen for prompt requests
     * and displays a dialog when needed.
     */
    @Suppress("ComplexMethod", "LongMethod")
    override fun start() {
        promptAbuserDetector.resetJSAlertAbuseState()

        handlePromptScope = store.flowScoped { flow ->
            flow.map { state -> state.findTabOrCustomTabOrSelectedTab(customTabId) }.ifAnyChanged {
                arrayOf(it?.content?.promptRequests, it?.content?.loading)
            }.collect { state ->
                state?.content?.let { content ->
                    if (content.promptRequests.lastOrNull() != activePromptRequest) {
                        // Dismiss any active select login or credit card prompt if it does
                        // not match the current prompt request for the session.
                        when (activePromptRequest) {
                            is SelectLoginPrompt -> {
                                if (selectLoginPromptController.isLoginPickerDialog()) {
                                    (selectLoginPromptController as SelectLoginPromptController.LoginPickerDialog).dismissCurrentLoginSelect(
                                        activePromptRequest as SelectLoginPrompt
                                    )
                                }
                                if (selectLoginPromptController.isStrongPasswordBarDialog()) {
                                    (selectLoginPromptController as SelectLoginPromptController.StrongPasswordBarDialog).dismissCurrentSuggestStrongPassword(
                                        activePromptRequest as SelectLoginPrompt,
                                    )
                                }
                                visiblePrompt = null
                            }

//                            removes fragment, nothing else
//                                is SaveLoginPrompt -> {
//                                    (activePrompt?.get() as? SaveLoginDialogFragment)?.dismissAllowingStateLoss()
//                                }

//                            removes fragment, nothing else
//                                is SaveCreditCard -> {
//                                    (activePrompt?.get() as? CreditCardSaveDialogFragment)?.dismissAllowingStateLoss()
//                                }

                            is SelectCreditCard -> {
                                creditCardPicker.dismissSelectCreditCardRequest(
                                    activePromptRequest as SelectCreditCard,
                                )
                            }

                            is SelectAddress -> {
                                addressPicker.dismissSelectAddressRequest(
                                    activePromptRequest as SelectAddress,
                                )
                            }

//                                is SingleChoice,
//                                is MultipleChoice,
//                                is MenuChoice,
//                                    -> {
//                                    (activePrompt?.get() as? ChoiceDialogFragment)?.let { dialog ->
//                                        if (dialog.isAdded) {
//                                            dialog.dismissAllowingStateLoss()
//                                        } else {
//                                            activePromptsToDismiss.remove(dialog)
//                                            activePrompt?.clear()
//                                        }
//                                    }
//                                }

                            else -> {
                                // no-op
                            }
                        }

                        // dismisses prompt in InfernoWebPrompter
                        activePromptRequest = null
                        onPromptRequested(state)
                    } else if (!content.loading) {
                        promptAbuserDetector.resetJSAlertAbuseState()
                    } else if (content.loading) {
                        dismissSelectPrompts()
                    }

                    activePromptRequest = content.promptRequests.lastOrNull()
                }
            }
        }

        // Dismiss all prompts when page host or session id changes. See Fenix#5326
        dismissPromptScope = store.flowScoped { flow ->
            flow.ifAnyChanged { state ->
                arrayOf(
                    state.selectedTabId,
                    state.findTabOrCustomTabOrSelectedTab(customTabId)?.content?.url?.tryGetHostFromUrl(),
                )
            }.collect {
                dismissSelectPrompts()

                store.consumeAllSessionPrompts(
                    sessionId = store.state.selectedTabId, // prompt?.sessionId,
                    predicate = { it.shouldDismissOnLoad && it !is File },
//                    consume = { prompt?.dismiss() },
                )

//                // Let's make sure we do not leave anything behind..
//                activePromptsToDismiss.forEach { fragment -> fragment.dismiss() }
            }
        }

//        fragmentManager.findFragmentByTag(mozilla.components.feature.prompts.FRAGMENT_TAG)?.let { fragment ->
//            // There's still a [PromptDialogFragment] visible from the last time. Re-attach this feature so that the
//            // fragment can invoke the callback on this feature once the user makes a selection. This can happen when
//            // the app was in the background and on resume the activity and fragments get recreated.
//            reattachFragment(fragment as PromptDialogFragment)
//        }
    }

    override fun stop() {
        // Stops observing the selected session for incoming prompt requests.
        handlePromptScope?.cancel()
        dismissPromptScope?.cancel()

        // Dismisses the logins prompt so that it can appear on another tab
        dismissSelectPrompts()
    }

    override fun onBackPressed(): Boolean {
//        if (visiblePrompt != null) {
//            visiblePrompt = null
//            return true
//        }
        return dismissSelectPrompts()
    }

    /**
     * Notifies the feature of intent results for prompt requests handled by
     * other apps like credit card and file chooser requests.
     *
     * @param requestCode The code of the app that requested the intent.
     * @param data The result of the request.
     * @param resultCode The code of the result.
     */
    override fun onActivityResult(requestCode: Int, data: Intent?, resultCode: Int): Boolean {
        if (requestCode == PIN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                creditCardPicker.onAuthSuccess()
            } else {
                creditCardPicker.onAuthFailure()
            }

            return true
        }

        return filePicker.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Notifies the feature that the biometric authentication was completed. It will then
     * either process or dismiss the prompt request.
     *
     * @param isAuthenticated True if the user is authenticated successfully from the biometric
     * authentication prompt or false otherwise.
     */
    fun onBiometricResult(isAuthenticated: Boolean) {
        if (isAuthenticated) {
            creditCardPicker.onAuthSuccess()
        } else {
            creditCardPicker.onAuthFailure()
        }
    }

    /**
     * Notifies the feature that the permissions request was completed. It will then
     * either process or dismiss the prompt request.
     *
     * @param permissions List of permission requested.
     * @param grantResults The grant results for the corresponding permissions
     * @see [onNeedToRequestPermissions].
     */
    override fun onPermissionsResult(permissions: Array<String>, grantResults: IntArray) {
        filePicker.onPermissionsResult(permissions, grantResults)
    }

    /**
     * Invoked when a native dialog needs to be shown.
     *
     * @param session The session which requested the dialog.
     */
    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onPromptRequested(session: SessionState) {
        // Some requests are handle with intents
        session.content.promptRequests.lastOrNull()?.let { promptRequest ->
            if (session.content.permissionRequestsList.isNotEmpty()) {
                val value: Any? = if (promptRequest is Popup) false else null
                onCancel(session.id, promptRequest.uid, value)
            } else {
                processPromptRequest(promptRequest, session)
            }
        }
    }

    @Suppress("NestedBlockDepth")
    @VisibleForTesting(otherwise = PRIVATE)
    internal fun processPromptRequest(
        promptRequest: PromptRequest,
        session: SessionState,
    ) {
        store.state.findTabOrCustomTabOrSelectedTab(customTabId)?.let {
            promptRequest.executeIfWindowedPrompt { exitFullscreenUsecase(it.id) }
        }

        when (promptRequest) {
            is File -> {
//                emitPromptDisplayedFact(promptName = "FilePrompt")
                filePicker.handleFileRequest(promptRequest)
            }

            is Share -> handleShareRequest(promptRequest, session)
            is SelectCreditCard -> {
//                emitSuccessfulCreditCardAutofillFormDetectedFact()
                if (isCreditCardAutofillEnabled() && promptRequest.creditCards.isNotEmpty()) {
                    creditCardPicker.handleSelectCreditCardRequest(promptRequest)
                }
            }

            is SelectLoginPrompt -> {
                if (!isLoginAutofillEnabled()) {
                    return
                }
                if (promptRequest.generatedPassword != null) {
                    if (shouldAutomaticallyShowSuggestedPassword.invoke()) {
                        onFirstTimeEngagedWithSignup.invoke()
                        selectLoginPromptController =
                            SelectLoginPromptController.PasswordGeneratorDialog
                    } else {
                        selectLoginPromptController =
                            SelectLoginPromptController.StrongPasswordBarDialog(
                                store, session.id,
                                onGeneratedPasswordPromptClick = {
//                                    strongPasswordPromptStateListener.hideStrongPasswordBar()
                                    visiblePrompt = null
                                    // todo: dismiss also? or done in onConfirm
                                }
                            )
//                        strongPasswordPromptStateListener.showStrongPasswordBar()
                    }
                } else {
//                    loginPicker.handleSelectLoginRequest(promptRequest)
                    selectLoginPromptController = SelectLoginPromptController.LoginPickerDialog(
                        store, loginDelegate.onManageLogins
                    )
                }
                emitPromptDisplayedFact(promptName = "SelectLoginPrompt")
                handleDialogsRequest(
                    promptRequest,
                    session,
                )
            }

            is SelectAddress -> {
//                emitSuccessfulAddressAutofillFormDetectedFact()
                if (isAddressAutofillEnabled() && promptRequest.addresses.isNotEmpty()) {
                    addressPicker.handleSelectAddressRequest(promptRequest)
                }
            }

            else -> handleDialogsRequest(promptRequest, session)
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
    override fun onCancel(sessionId: String, promptRequestUID: String, value: Any?) {
        store.consumePromptFrom(sessionId, promptRequestUID, visiblePrompt) {
//            emitPromptDismissedFact(promptName = it::class.simpleName.ifNullOrEmpty { "" })
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
    override fun onConfirm(sessionId: String, promptRequestUID: String, value: Any?) {
        store.consumePromptFrom(sessionId, promptRequestUID, visiblePrompt) {
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

                // todo: add onConfirm / onCancel callbacks to all prompt composables,
                //  in this case specifically, include button type pressed
                is Confirm -> {
                    val (isCheckBoxChecked, buttonType) = value as Pair<Boolean, MultiButtonDialogFragment.ButtonType>
                    promptAbuserDetector.userWantsMoreDialogs(!isCheckBoxChecked)
                    when (buttonType) {
                        MultiButtonDialogFragment.ButtonType.POSITIVE -> it.onConfirmPositiveButton(
                            !isCheckBoxChecked
                        )

                        MultiButtonDialogFragment.ButtonType.NEGATIVE -> it.onConfirmNegativeButton(
                            !isCheckBoxChecked
                        )

                        MultiButtonDialogFragment.ButtonType.NEUTRAL -> it.onConfirmNeutralButton(!isCheckBoxChecked)
                    }
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
    override fun onClear(sessionId: String, promptRequestUID: String) {
        store.consumePromptFrom(sessionId, promptRequestUID, visiblePrompt) {
            when (it) {
                is TimeSelection -> it.onClear()
                else -> {
                    // no-op
                }
            }
        }
    }

    private fun handleShareRequest(promptRequest: Share, session: SessionState) {
//        emitPromptDisplayedFact(promptName = "ShareSheet")
        shareDelegate.showShareSheet(
            context = activity.applicationContext,
            shareData = promptRequest.data,
            onDismiss = {
//                emitPromptDismissedFact(promptName = "ShareSheet")
                onCancel(session.id, promptRequest.uid)
            },
            onSuccess = { onConfirm(session.id, promptRequest.uid, null) },
        )
    }

    private fun showDialogRequest(prompt: PromptRequest) {
        visiblePrompt = prompt
    }

    /**
     * Called from on [onPromptRequested] to handle requests for showing native dialogs.
     */
    @Suppress("ComplexMethod", "LongMethod", "ReturnCount")
    @VisibleForTesting(otherwise = PRIVATE)
    internal fun handleDialogsRequest(
        promptRequest: PromptRequest,
        session: SessionState,
    ) {
        // Requests that are handled with dialogs
        when (promptRequest) {
            is SelectLoginPrompt -> {
                currentUrl = store.state.findTabOrCustomTabOrSelectedTab(customTabId)?.content?.url
                val generatedPassword = promptRequest.generatedPassword

                if (generatedPassword == null || currentUrl == null) {
                    logger.debug(
                        "Ignoring received SelectLogin.onGeneratedPasswordPromptClick" + " when either the generated password or the currentUrl is null.",
                    )
                    dismissDialogRequest(promptRequest, session)
                    return
                }

//                emitGeneratedPasswordShownFact()

                visiblePrompt = promptRequest
//                PasswordGeneratorDialogFragment.newInstance(
//                    sessionId = session.id,
//                    promptRequestUID = promptRequest.uid,
//                    generatedPassword = generatedPassword,
//                    currentUrl = currentUrl,
//                    onSavedGeneratedPassword = onSaveLogin,
//                    colorsProvider = passwordGeneratorColorsProvider,
//                )
            }

            is SaveCreditCard -> {
                if (!isCreditCardAutofillEnabled.invoke() || creditCardValidationDelegate == null || !promptRequest.creditCard.isValid) {
                    dismissDialogRequest(promptRequest, session)

                    if (creditCardValidationDelegate == null) {
                        logger.debug(
                            "Ignoring received SaveCreditCard because PromptFeature." + "creditCardValidationDelegate is null. If you are trying to autofill " + "credit cards, try attaching a CreditCardValidationDelegate to PromptFeature",
                        )
                    }

                    return
                }

//                emitCreditCardSaveShownFact()

                showDialogRequest(promptRequest)
            }

            is SaveLoginPrompt -> {
                if (!isSaveLoginEnabled.invoke() || loginValidationDelegate == null) {
                    dismissDialogRequest(promptRequest, session)

                    if (loginValidationDelegate == null) {
                        logger.debug(
                            "Ignoring received SaveLoginPrompt because PromptFeature." + "loginValidationDelegate is null. If you are trying to autofill logins, " + "try attaching a LoginValidationDelegate to PromptFeature",
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

                showDialogRequest(promptRequest)
            }

            is SingleChoice,
            is MultipleChoice,
            is MenuChoice,
            is Alert,
            is TimeSelection,
            is TextPrompt,
            is Authentication,
            is Color,
            is Popup,
            is BeforeUnload,
            is Confirm,
            is Repost,
            is PromptRequest.IdentityCredential.SelectProvider,
            is PromptRequest.IdentityCredential.SelectAccount,
            is PromptRequest.IdentityCredential.PrivacyPolicy,
                -> { /* no preops before showing */
            }

            else -> throw InvalidParameterException("Not valid prompt request type $promptRequest")
        }

        if (canShowThisPrompt(promptRequest)) {
//            // If the ChoiceDialogFragment's choices data were updated,
//            // we need to dismiss the previous dialog
//            visiblePrompt?.get()?.let { promptDialog ->
//                // ChoiceDialogFragment could update their choices data,
//                // and we need to dismiss the previous UI dialog,
//                // without consuming the engine callbacks, and allow to create a new dialog with the
//                // updated data.
//                if (promptDialog is ChoiceDialogFragment && !session.content.promptRequests.any { it.uid == promptDialog.promptRequestUID }) {
//                    // We want to avoid consuming the engine callbacks and allow a new dialog
//                    // to be created with the updated data.
//                    promptDialog.feature = null
//                    promptDialog.dismiss()
//                }
//            }

//            emitPromptDisplayedFact(promptName = dialog::class.simpleName.ifNullOrEmpty { "" })

            // sets visiblePrompt which is then shown by InfernoWebPrompter
            showDialogRequest(promptRequest)

//            if (promptRequest.shouldDismissOnLoad) {
//                activePromptsToDismiss.add(dialog)
//            }
        } else {
            dismissDialogRequest(promptRequest, session)
        }
        promptAbuserDetector.updateJSDialogAbusedState()
    }

    /**
     * Dismiss and consume the given prompt request for the session.
     */
    @VisibleForTesting
    internal fun dismissDialogRequest(promptRequest: PromptRequest, session: SessionState) {
        (promptRequest as Dismissible).onDismiss()
        store.dispatch(ContentAction.ConsumePromptRequestAction(session.id, promptRequest))
        visiblePrompt = null
//        emitPromptDismissedFact(promptName = promptRequest::class.simpleName.ifNullOrEmpty { "" })
    }

    private fun canShowThisPrompt(promptRequest: PromptRequest): Boolean {
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
     * Dismisses the select prompts if they are active and visible.
     *
     * @returns true if a select prompt was dismissed, otherwise false.
     */
    @VisibleForTesting
    fun dismissSelectPrompts(): Boolean {
        var result = false

        (activePromptRequest as? SelectLoginPrompt)?.let { selectLoginPrompt ->
            if (selectLoginPromptController.isLoginPickerDialog()) {
                (selectLoginPromptController as SelectLoginPromptController.LoginPickerDialog).dismissCurrentLoginSelect(
                    selectLoginPrompt
                )
                visiblePrompt = null
                result = true
            }

//                if (suggestStrongPasswordDelegate.strongPasswordPromptViewListenerView?.isVisible() == true) {
            if (selectLoginPromptController.isStrongPasswordBarDialog()) {
                (selectLoginPromptController as SelectLoginPromptController.StrongPasswordBarDialog).dismissCurrentSuggestStrongPassword(
                    selectLoginPrompt
                )
                visiblePrompt = null
                result = true
            }
        }

        // todo
        (activePromptRequest as? SelectCreditCard)?.let { selectCreditCardPrompt ->
            creditCardPicker.let { creditCardPicker ->
                if (creditCardDelegate.creditCardPickerView?.asView()?.isVisible == true) {
                    creditCardPicker.dismissSelectCreditCardRequest(selectCreditCardPrompt)
                    result = true
                }
            }
        }

        (activePromptRequest as? SelectAddress)?.let { selectAddressPrompt ->
            addressPicker.let { addressPicker ->
                if (addressDelegate.addressPickerView?.asView()?.isVisible == true) {
                    addressPicker.dismissSelectAddressRequest(selectAddressPrompt)
                    result = true
                }
            }
        }

        return result
    }

    /**
     * Handles the result received from the Android photo picker.
     *
     * @param listOf An array of [Uri] objects representing the selected photos.
     */
    // todo:
    //   - called from AndroidPhotoPicker, need to change to include prompt state, call from there
    //   - see where else called from
    fun onAndroidPhotoPickerResult(uriList: Array<Uri>) {
        filePicker.onAndroidPhotoPickerResult(uriList)
    }

    companion object {
        // The PIN request code
        const val PIN_REQUEST = 303
    }

}

/**
 * Removes the [PromptRequest] indicated by [promptRequestUID] from the current Session if it it exists
 * and offers a [consume] callback for other optional side effects.
 *
 * @param sessionId Session id of the tab or custom tab in which to try consuming [PromptRequest].
 * If the id is not provided or a tab with that id is not found the method will act on the current tab.
 * @param promptRequestUID Id of the [PromptRequest] to be consumed.
 * @param consume callback with the [PromptRequest] if found, before being removed from the Session.
 */
internal fun BrowserStore.consumePromptFrom(
    sessionId: String?,
    promptRequestUID: String,
    activePrompt: PromptRequest? = null,
    consume: (PromptRequest) -> Unit = {},
) {
    state.findTabOrCustomTabOrSelectedTab(sessionId)?.let { tab ->
        tab.content.promptRequests.firstOrNull { it.uid == promptRequestUID }?.let {
            consume(it)
            dispatch(ContentAction.ConsumePromptRequestAction(tab.id, it))
        }
    }
}

/**
 * Removes the most recent [PromptRequest] of type [P] from the current Session if it it exists
 * and offers a [consume] callback for other optional side effects.
 *
 * @param sessionId Session id of the tab or custom tab in which to try consuming [PromptRequest].
 * If the id is not provided or a tab with that id is not found the method will act on the current tab.
 * @param consume callback with the [PromptRequest] if found, before being removed from the Session.
 */
internal inline fun <reified P : PromptRequest> BrowserStore.consumePromptFrom(
    sessionId: String?,
    consume: (P) -> Unit,
) {
    state.findTabOrCustomTabOrSelectedTab(sessionId)?.let { tab ->
        tab.content.promptRequests.lastOrNull { it is P }?.let {
            consume(it as P)
            dispatch(ContentAction.ConsumePromptRequestAction(tab.id, it))
        }
    }
}

/**
 * Filters and removes all [PromptRequest]s from the current Session if it it exists
 * and offers a [consume] callback for other optional side effects on each filtered [PromptRequest].
 *
 * @param sessionId Session id of the tab or custom tab in which to try consuming [PromptRequest].
 * If the id is not provided or a tab with that id is not found the method will act on the current tab.
 * @param predicate function allowing matching only specific [PromptRequest]s from all contained in the Session.
 * @param consume callback with the [PromptRequest] if found, before being removed from the Session.
 */
internal fun BrowserStore.consumeAllSessionPrompts(
    sessionId: String?,
    predicate: (PromptRequest) -> Boolean,
    consume: (PromptRequest) -> Unit = { },
) {
    state.findTabOrCustomTabOrSelectedTab(sessionId)?.let { tab ->
        tab.content.promptRequests.filter { predicate(it) }.forEach {
            consume(it)
            dispatch(ContentAction.ConsumePromptRequestAction(tab.id, it))
        }
    }
}