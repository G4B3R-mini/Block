package com.shmibblez.inferno.home.controllers

import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.components.appstate.AppAction
import com.shmibblez.inferno.home.bookmarks.Bookmark
import com.shmibblez.inferno.home.bookmarks.controller.BookmarksController
import com.shmibblez.inferno.home.bookmarks.controller.DefaultBookmarksController
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineSession.LoadUrlFlags.Companion.ALLOW_JAVASCRIPT_URL
import mozilla.components.feature.tabs.TabsUseCases

/**
 * based of [DefaultBookmarksController]
 */
class InfernoBookmarksController(
    private val appStore: AppStore,
    private val browserStore: BrowserStore,
    private val selectOrAddTabUseCase: TabsUseCases.SelectOrAddUseCase,
    private val onNavToBookmarks: () -> Unit,
) : BookmarksController {
    override fun handleBookmarkClicked(bookmark: Bookmark) {
        val existingTabForBookmark = browserStore.state.tabs.firstOrNull {
            it.content.url == bookmark.url
        }

        if (existingTabForBookmark == null) {
            selectOrAddTabUseCase(
                url = bookmark.url!!,
                flags = EngineSession.LoadUrlFlags.select(ALLOW_JAVASCRIPT_URL),
            )
        } else {
            // select tab, will load since already on browser
            selectOrAddTabUseCase.invoke(existingTabForBookmark.id)
        }
    }

    override fun handleShowAllBookmarksClicked() {
        onNavToBookmarks.invoke()
    }

    override fun handleBookmarkRemoved(bookmark: Bookmark) {
        appStore.dispatch(AppAction.RemoveBookmark(bookmark))
    }
}