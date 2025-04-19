package com.shmibblez.inferno.browser.prompts.concept

/**
 * An interface for views that can display a generated strong password prompt.
 */
interface PasswordPromptState {

    var listener: Listener?

    /**
     * Shows a simple prompt for using a generated password.
     */
    fun showPrompt()

    /**
     * Hides the prompt.
     */
    fun hidePrompt()

    /**
     * Returns true if the prompt is visible and false otherwise.
     */
    fun isVisible(): Boolean

    /**
     * Interface to allow a class to listen to generated strong password event events.
     */
    interface Listener {
        /**
         * Called when a user clicks on the password generator prompt
         */
        fun onGeneratedPasswordPromptClick()
    }
}