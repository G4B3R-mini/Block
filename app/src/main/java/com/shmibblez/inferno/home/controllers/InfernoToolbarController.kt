package com.shmibblez.inferno.home.controllers

import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.home.toolbar.DefaultToolbarController
import com.shmibblez.inferno.home.toolbar.ToolbarController
import mozilla.components.browser.state.store.BrowserStore

/**
 * todo: reference [DefaultToolbarController]
 */
class InfernoToolbarController(
    private val activity: HomeActivity,
    private val store: BrowserStore,
) : ToolbarController {
    override fun handlePasteAndGo(clipboardText: String) {
        // TODO("Not yet implemented")
    }

    override fun handlePaste(clipboardText: String) {
        // TODO("Not yet implemented")
    }

    override fun handleNavigateSearch() {
        // TODO("Not yet implemented")
    }

}