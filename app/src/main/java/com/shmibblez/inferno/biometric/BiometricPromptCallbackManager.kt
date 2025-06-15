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
}