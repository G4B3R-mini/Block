package com.shmibblez.inferno.browser.prompts

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.fragment.app.FragmentManager
import com.shmibblez.inferno.browser.infernoFeatureState.InfernoFeatureState
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.selector.findTabOrCustomTabOrSelectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.storage.CreditCardValidationDelegate
import mozilla.components.concept.storage.LoginValidationDelegate
import mozilla.components.feature.prompts.PromptFeature
import mozilla.components.feature.prompts.address.AddressDelegate
import mozilla.components.feature.prompts.address.DefaultAddressDelegate
import mozilla.components.feature.prompts.creditcard.CreditCardDelegate
import mozilla.components.feature.prompts.file.AndroidPhotoPicker
import mozilla.components.feature.prompts.file.FileUploadsDirCleaner
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
import mozilla.components.support.base.feature.ActivityResultHandler
import mozilla.components.support.base.feature.OnNeedToRequestPermissions
import mozilla.components.support.base.feature.PermissionsFeature
import mozilla.components.support.base.feature.UserInteractionHandler

@Composable
fun rememberInfernoPromptFeatureState(
    context: Context,
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
    loginDelegate: LoginDelegate = object : LoginDelegate {},
    suggestStrongPasswordDelegate: SuggestStrongPasswordDelegate = object :
        SuggestStrongPasswordDelegate {},
     shouldAutomaticallyShowSuggestedPassword: () -> Boolean = { false },
    onFirstTimeEngagedWithSignup: () -> Unit = {},
    onSaveLoginWithStrongPassword: (String, String) -> Unit = { _, _ -> },
    onSaveLogin: (Boolean) -> Unit = { _ -> },
    passwordGeneratorColorsProvider: PasswordGeneratorDialogColorsProvider =
        PasswordGeneratorDialogColorsProvider { PasswordGeneratorDialogColors.default() },
    hideUpdateFragmentAfterSavingGeneratedPassword: (String, String) -> Boolean = { _, _ -> true },
    removeLastSavedGeneratedPassword: () -> Unit = {},
    creditCardDelegate: CreditCardDelegate = object : CreditCardDelegate {},
    addressDelegate: AddressDelegate = DefaultAddressDelegate(),
    fileUploadsDirCleaner: FileUploadsDirCleaner,
    onNeedToRequestPermissions: OnNeedToRequestPermissions,
    androidPhotoPicker: AndroidPhotoPicker?,
): InfernoPromptFeatureState {
    val state = InfernoPromptFeatureState(
        context = context,
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

    PromptFeature

    DisposableEffect(null) {
        state.start()
        // todo: how does composable react to state?
        //  take a look at lazylist to see how it implements state updates
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
    private val context: Context,
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
    private val loginDelegate: LoginDelegate = object : LoginDelegate {},
    private val suggestStrongPasswordDelegate: SuggestStrongPasswordDelegate = object :
        SuggestStrongPasswordDelegate {},
    private var shouldAutomaticallyShowSuggestedPassword: () -> Boolean = { false },
    private val onFirstTimeEngagedWithSignup: () -> Unit = {},
    private val onSaveLoginWithStrongPassword: (String, String) -> Unit = { _, _ -> },
    private val onSaveLogin: (Boolean) -> Unit = { _ -> },
    private val passwordGeneratorColorsProvider: PasswordGeneratorDialogColorsProvider =
        PasswordGeneratorDialogColorsProvider { PasswordGeneratorDialogColors.default() },
    private val hideUpdateFragmentAfterSavingGeneratedPassword: (String, String) -> Boolean = { _, _ -> true },
    private val removeLastSavedGeneratedPassword: () -> Unit = {},
    private val creditCardDelegate: CreditCardDelegate = object : CreditCardDelegate {},
    private val addressDelegate: AddressDelegate = DefaultAddressDelegate(),
    private val fileUploadsDirCleaner: FileUploadsDirCleaner,
    override val onNeedToRequestPermissions: OnNeedToRequestPermissions,
    androidPhotoPicker: AndroidPhotoPicker?,
): InfernoFeatureState,
    PermissionsFeature,
    InfernoPrompter,
    ActivityResultHandler,
    UserInteractionHandler {
    override fun start() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun onPermissionsResult(permissions: Array<String>, grantResults: IntArray) {
        TODO("Not yet implemented")
    }

    override fun onActivityResult(requestCode: Int, data: Intent?, resultCode: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun onBackPressed(): Boolean {
        TODO("Not yet implemented")
    }

    override fun onCancel(sessionId: String, promptRequestUID: String, value: Any?) {
        TODO("Not yet implemented")
    }

    override fun onConfirm(sessionId: String, promptRequestUID: String, value: Any?) {
        TODO("Not yet implemented")
    }

    override fun onClear(sessionId: String, promptRequestUID: String) {
        TODO("Not yet implemented")
    }

    override fun onOpenLink(url: String) {
        TODO("Not yet implemented")
    }

}

/**
 * Removes the [PromptRequest] indicated by [promptRequestUID] from the current Session if it it exists
 * and offers a [consume] callback for other optional side effects.
 *
 * @param sessionId Session id of the tab or custom tab in which to try consuming [PromptRequests].
 * If the id is not provided or a tab with that id is not found the method will act on the current tab.
 * @param promptRequestUID Id of the [PromptRequest] to be consumed.
 * @param consume callback with the [PromptRequest] if found, before being removed from the Session.
 */
internal fun BrowserStore.consumePromptFrom(
    sessionId: String?,
    promptRequestUID: String,
    consume: (PromptRequest) -> Unit,
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
 * @param sessionId Session id of the tab or custom tab in which to try consuming [PromptRequests].
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
 * @param sessionId Session id of the tab or custom tab in which to try consuming [PromptRequests].
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
        tab.content.promptRequests
            .filter { predicate(it) }
            .forEach {
                consume(it)
                dispatch(ContentAction.ConsumePromptRequestAction(tab.id, it))
            }
    }
}