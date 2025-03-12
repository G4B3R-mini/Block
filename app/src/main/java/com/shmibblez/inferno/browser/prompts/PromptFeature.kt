/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.browser.prompts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.storage.CreditCardValidationDelegate
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
import mozilla.components.feature.prompts.share.DefaultShareDelegate
import mozilla.components.feature.prompts.share.ShareDelegate
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.session.SessionUseCases.ExitFullScreenUseCase
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.base.feature.ActivityResultHandler
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.base.feature.OnNeedToRequestPermissions
import mozilla.components.support.base.feature.PermissionsFeature
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.util.PromptAbuserDetector

@VisibleForTesting(otherwise = PRIVATE)
internal const val FRAGMENT_TAG = "mozac_feature_prompt_dialog"

/**
 * Feature for displaying native dialogs for html elements like: input type
 * date, file, time, color, option, menu, authentication, confirmation and alerts.
 *
 * There are some requests that are handled with intents instead of dialogs,
 * like file choosers and others. For this reason, you have to keep the feature
 * aware of the flow of requesting data from other apps, overriding
 * onActivityResult in your [Activity] or [Fragment] and forward its calls
 * to [onActivityResult].
 *
 * This feature will subscribe to the currently selected session and display
 * a suitable native dialog based on [Session.Observer.onPromptRequested] events.
 * Once the dialog is closed or the user selects an item from the dialog
 * the related [PromptRequest] will be consumed.
 *
 * @property container The [Activity] or [Fragment] which hosts this feature.
 * @property store The [BrowserStore] this feature should subscribe to.
 * @property customTabId Optional id of a custom tab. Instead of showing context
 * menus for the currently selected tab this feature will show only context menus
 * for this custom tab if an id is provided.
 * @property fragmentManager The [FragmentManager] to be used when displaying
 * a dialog (fragment).
 * @property shareDelegate Delegate used to display share sheet.
 * @property exitFullscreenUsecase Usecase allowing to exit browser tabs' fullscreen mode.
 * @property isLoginAutofillEnabled A callback invoked before an autofill prompt is triggered. If false,
 * 'autofill login' prompts will not be shown.
 * @property isSaveLoginEnabled A callback invoked when a login prompt is triggered. If false,
 * 'save login' prompts will not be shown.
 * @property isCreditCardAutofillEnabled A callback invoked when credit card fields are detected in the webpage.
 * If this resolves to `true` a prompt allowing the user to select the credit card details to be autocompleted
 * will be shown.
 * @property isAddressAutofillEnabled A callback invoked when address fields are detected in the webpage.
 * If this resolves to `true` a prompt allowing the user to select the address details to be autocompleted
 * will be shown.
 * @property loginExceptionStorage An implementation of [LoginExceptions] that saves and checks origins
 * the user does not want to see a save login dialog for.
 * @property loginDelegate Delegate for login picker.
 * @property suggestStrongPasswordDelegate Delegate for strong password generator.
 * @property isSuggestStrongPasswordEnabled Feature flag denoting whether the suggest strong password
 * feature is enabled or not. If this resolves to 'false', the feature will be hidden.
 * @property onSaveLoginWithStrongPassword A callback invoked to save a new login that uses the
 * generated strong password
 * @property shouldAutomaticallyShowSuggestedPassword A callback invoked to check whether the user
 * is engaging with signup for the first time.
 * @property onFirstTimeEngagedWithSignup A callback invoked when user is engaged with signup for
 * the first time.
 * @property creditCardDelegate Delegate for credit card picker.
 * @property addressDelegate Delegate for address picker.
 * @property fileUploadsDirCleaner a [FileUploadsDirCleaner] to clean up temporary file uploads.
 * @property onNeedToRequestPermissions A callback invoked when permissions
 * need to be requested before a prompt (e.g. a file picker) can be displayed.
 * Once the request is completed, [onPermissionsResult] needs to be invoked.
 */
@Suppress("LargeClass", "LongParameterList")
class PromptFeature private constructor(
    private val container: Activity,
    private val store: BrowserStore,
    private var customTabId: String?,
    private val fragmentManager: FragmentManager,
    private val identityCredentialColorsProvider: DialogColorsProvider = DialogColorsProvider {
        DialogColors.default()
    },
    private val tabsUseCases: TabsUseCases,
    private val shareDelegate: ShareDelegate,
    private val exitFullscreenUsecase: ExitFullScreenUseCase = SessionUseCases(store).exitFullscreen,
    /* override */ val creditCardValidationDelegate: CreditCardValidationDelegate? = null,
    /* override */ val loginValidationDelegate: LoginValidationDelegate? = null,
    private val isLoginAutofillEnabled: () -> Boolean = { false },
    private val isSaveLoginEnabled: () -> Boolean = { false },
    private val isCreditCardAutofillEnabled: () -> Boolean = { false },
    private val isAddressAutofillEnabled: () -> Boolean = { false },
    /* override */ val loginExceptionStorage: LoginExceptions? = null,
    private val loginDelegate: LoginDelegate = object : LoginDelegate {},
    private val suggestStrongPasswordDelegate: SuggestStrongPasswordDelegate = object :
        SuggestStrongPasswordDelegate {},
    private var shouldAutomaticallyShowSuggestedPassword: () -> Boolean = { false },
    private val onFirstTimeEngagedWithSignup: () -> Unit = {},
    private val onSaveLoginWithStrongPassword: (String, String) -> Unit = { _, _ -> },
    private val onSaveLogin: (Boolean) -> Unit = { _ -> },
    private val passwordGeneratorColorsProvider: PasswordGeneratorDialogColorsProvider = PasswordGeneratorDialogColorsProvider { PasswordGeneratorDialogColors.default() },
    private val hideUpdateFragmentAfterSavingGeneratedPassword: (String, String) -> Boolean = { _, _ -> true },
    private val removeLastSavedGeneratedPassword: () -> Unit = {},
    private val creditCardDelegate: CreditCardDelegate = object : CreditCardDelegate {},
    private val addressDelegate: AddressDelegate = DefaultAddressDelegate(),
    private val fileUploadsDirCleaner: FileUploadsDirCleaner,
    onNeedToRequestPermissions: OnNeedToRequestPermissions,
    androidPhotoPicker: AndroidPhotoPicker?,
) :
    LifecycleAwareFeature,
    PermissionsFeature,
//    Prompter,
//    UserInteractionHandler,
    ActivityResultHandler {

    @VisibleForTesting
    var activePromptRequest: PromptRequest? = null

    internal val promptAbuserDetector = PromptAbuserDetector()
    private val logger = Logger("PromptFeature")

//    @VisibleForTesting(otherwise = PRIVATE)
//    internal var activePrompt: WeakReference<PromptDialogFragment>? = null

//    // This set of weak references of fragments is only used for dismissing all prompts on navigation.
//    // For all other code only `activePrompt` is tracked for now.
//    @VisibleForTesting(otherwise = PRIVATE)
//    internal val activePromptsToDismiss =
//        Collections.newSetFromMap(WeakHashMap<PromptDialogFragment, Boolean>())

    constructor(
        activity: Activity,
        store: BrowserStore,
        customTabId: String? = null,
        fragmentManager: FragmentManager,
        tabsUseCases: TabsUseCases,
        identityCredentialColorsProvider: DialogColorsProvider = DialogColorsProvider { DialogColors.default() },
        shareDelegate: ShareDelegate = DefaultShareDelegate(),
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
        passwordGeneratorColorsProvider: PasswordGeneratorDialogColorsProvider = PasswordGeneratorDialogColorsProvider { PasswordGeneratorDialogColors.default() },
        hideUpdateFragmentAfterSavingGeneratedPassword: (String, String) -> Boolean = { _, _ -> true },
        removeLastSavedGeneratedPassword: () -> Unit = {},
        creditCardDelegate: CreditCardDelegate = object : CreditCardDelegate {},
        addressDelegate: AddressDelegate = DefaultAddressDelegate(),
        fileUploadsDirCleaner: FileUploadsDirCleaner,
        onNeedToRequestPermissions: OnNeedToRequestPermissions,
        androidPhotoPicker: com.shmibblez.inferno.browser.prompts.AndroidPhotoPicker? = null,
    ) : this(
        container = activity,
        store = store,
        customTabId = customTabId,
        fragmentManager = fragmentManager,
        tabsUseCases = tabsUseCases,
        identityCredentialColorsProvider = identityCredentialColorsProvider,
        shareDelegate = shareDelegate,
        exitFullscreenUsecase = exitFullscreenUsecase,
        creditCardValidationDelegate = creditCardValidationDelegate,
        loginValidationDelegate = loginValidationDelegate,
        isLoginAutofillEnabled = isLoginAutofillEnabled,
        isSaveLoginEnabled = isSaveLoginEnabled,
        isCreditCardAutofillEnabled = isCreditCardAutofillEnabled,
        isAddressAutofillEnabled = isAddressAutofillEnabled,
        loginExceptionStorage = loginExceptionStorage,
        fileUploadsDirCleaner = fileUploadsDirCleaner,
        onNeedToRequestPermissions = onNeedToRequestPermissions,
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
        androidPhotoPicker = androidPhotoPicker,
    )

    constructor(
        fragment: Fragment,
        store: BrowserStore,
        customTabId: String? = null,
        fragmentManager: FragmentManager,
        tabsUseCases: TabsUseCases,
        shareDelegate: ShareDelegate = DefaultShareDelegate(),
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
        hideUpdateFragmentAfterSavingGeneratedPassword: (String, String) -> Boolean = { _, _ -> true },
        removeLastSavedGeneratedPassword: () -> Unit = {},
        creditCardDelegate: CreditCardDelegate = object : CreditCardDelegate {},
        addressDelegate: AddressDelegate = DefaultAddressDelegate(),
        fileUploadsDirCleaner: FileUploadsDirCleaner,
        androidPhotoPicker: AndroidPhotoPicker? = null,
        onNeedToRequestPermissions: OnNeedToRequestPermissions,
    ) : this(
        container = fragment.requireActivity(),
        store = store,
        customTabId = customTabId,
        fragmentManager = fragmentManager,
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
        hideUpdateFragmentAfterSavingGeneratedPassword = hideUpdateFragmentAfterSavingGeneratedPassword,
        removeLastSavedGeneratedPassword = removeLastSavedGeneratedPassword,
        creditCardDelegate = creditCardDelegate,
        addressDelegate = addressDelegate,
        fileUploadsDirCleaner = fileUploadsDirCleaner,
        onNeedToRequestPermissions = onNeedToRequestPermissions,
        androidPhotoPicker = androidPhotoPicker,
    )

    @VisibleForTesting
    // var for testing purposes
    internal var filePicker = FilePicker(
        container,
        store,
        customTabId,
        fileUploadsDirCleaner,
        androidPhotoPicker,
        onNeedToRequestPermissions,
    )


    override val onNeedToRequestPermissions
        get() = filePicker.onNeedToRequestPermissions

    /* override */ fun onOpenLink(url: String) {
        tabsUseCases.addTab(
            url = url,
        )
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
//        if (requestCode == PIN_REQUEST) {
//            if (resultCode == Activity.RESULT_OK) {
//                creditCardPicker?.onAuthSuccess()
//            } else {
//                creditCardPicker?.onAuthFailure()
//            }
//
//            return true
//        }

        return filePicker.onActivityResult(requestCode, resultCode, data)
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
     * Handles the result received from the Android photo picker.
     *
     * @param listOf An array of [Uri] objects representing the selected photos.
     */

    fun onAndroidPhotoPickerResult(uriList: Array<Uri>) {
        filePicker.onAndroidPhotoPickerResult(uriList)
    }

    companion object {
        // The PIN request code
        const val PIN_REQUEST = 303
    }

    override fun start() {
        // nothing
    }

    override fun stop() {
        // nothing
    }
}

