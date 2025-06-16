package com.shmibblez.inferno.home.controllers

import com.shmibblez.inferno.home.toolbar.DefaultToolbarController
import com.shmibblez.inferno.home.toolbar.ToolbarController
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.search.SearchUseCases

/**
 * based off [DefaultToolbarController]
 */
class InfernoToolbarController(
    private val store: BrowserStore,
    private val defaultSearchUseCase: SearchUseCases.DefaultSearchUseCase,
) : ToolbarController {
    override fun handlePasteAndGo(clipboardText: String) {
        val searchEngine = store.state.search.selectedOrDefaultSearchEngine
        defaultSearchUseCase.invoke(
            searchTerms = clipboardText,
            searchEngine = searchEngine,
        )
    }

    override fun handlePaste(clipboardText: String) {
        // todo: paste requires massiv rework of some components, toolbar primarily, store in browser state?
    }

    override fun handleNavigateSearch() {
        // no-op, not needed since already in homepage, could pop up search bar when ready
        // todo: search bar, pop up when ready
    }
}