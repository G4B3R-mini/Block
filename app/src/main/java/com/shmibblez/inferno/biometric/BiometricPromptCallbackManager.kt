package com.shmibblez.inferno.biometric

import androidx.biometric.BiometricPrompt
import com.shmibblez.inferno.HomeActivity
import java.util.concurrent.Executor

class BiometricPromptCallbackManager(
    private val activity: HomeActivity,
    executor: Executor,
    private val callbackListeners: MutableList<AuthenticationCallback> = mutableListOf(),
) : BiometricPrompt(activity, executor, object : AuthenticationCallback() {
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        callbackListeners.forEach { it.onAuthenticationError(errorCode, errString) }
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        callbackListeners.forEach { it.onAuthenticationFailed() }
    }

    override fun onAuthenticationSucceeded(result: AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        callbackListeners.forEach { it.onAuthenticationSucceeded(result) }
    }
}) {
    fun addCallbackListener(listener: AuthenticationCallback) {
        callbackListeners.add(listener)
    }

    fun removeCallbackListener(listener: AuthenticationCallback) {
        callbackListeners.remove(listener)
    }

    /**
     * show biometric prompt
     * @param title prompt title, ex: "Biometric login for my app"
     * @param subtitle prompt subtitle, ex: "Log in using your biometric credential"
     * @param negativeButtonText ex: "Use account password"
     */
    fun showPrompt(
        title: String,
        subtitle: String? = null,
        negativeButtonText: String = activity.getString(android.R.string.cancel),
    ) {
        val promptInfo = PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .build()

        this.authenticate(promptInfo)
    }

    // todo: incorporate this to authenticate instead of just showPrompt
    //  currently users that dont have biometric are cooked
    //  need to support non-biometric devices with pin
//    /**
//     * Shows a biometric prompt and fallback to prompting for the password.
//     */
//    private fun showBiometricPrompt(context: Context) {
//        if (BiometricPromptFeature.canUseFeature(BiometricManager.from(context))) {
//            biometricPromptFeature.get()
//                ?.requestAuthentication(getString(R.string.credit_cards_biometric_prompt_unlock_message_2))
//            return
//        }
//
//        // Fallback to prompting for password with the KeyguardManager
//        val manager = context.getSystemService<KeyguardManager>()
//        if (manager?.isKeyguardSecure == true) {
//            showPinVerification(manager)
//        } else {
//            // Warn that the device has not been secured
//            if (context.settings().shouldShowSecurityPinWarning) {
//                showPinDialogWarning(context)
//            } else {
//                promptsFeature.get()?.onBiometricResult(isAuthenticated = true)
//            }
//        }
//    }
}