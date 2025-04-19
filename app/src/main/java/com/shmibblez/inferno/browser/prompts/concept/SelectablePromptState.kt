package com.shmibblez.inferno.browser.prompts.concept

import android.view.View


/**
 * An interface for views that can display an option selection prompt.
 */
interface SelectablePromptState<T> {

    var listener: Listener<T>?

    /**
     * Shows an option selection prompt with the provided options.
     *
     * @param options A list of options to display in the prompt.
     */
    fun showPrompt(options: List<T>)

    /**
     * Hides the option selection prompt.
     */
    fun hidePrompt()

    /**
     * Casts this [SelectablePromptState] interface to an Android [View] object.
     */
    fun asView(): View = (this as View)

    /**
     * Interface to allow a class to listen to the option selection prompt events.
     */
    interface Listener<in T> {
        /**
         * Called when an user selects an options from the prompt.
         *
         * @param option The selected option.
         */
        fun onOptionSelect(option: T)

        /**
         * Called when the user invokes the option to manage the list of options.
         */
        fun onManageOptions()
    }
}