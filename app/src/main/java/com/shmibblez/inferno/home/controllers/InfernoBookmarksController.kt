package com.shmibblez.inferno.home.controllers

import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.home.bookmarks.Bookmark
import com.shmibblez.inferno.home.bookmarks.controller.BookmarksController
import com.shmibblez.inferno.home.bookmarks.controller.DefaultBookmarksController
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.tabs.TabsUseCases

/**
 * todo: reference [DefaultBookmarksController]
 */
class InfernoBookmarksController(
    private val activity: HomeActivity,
    private val appStore: AppStore,
    private val browserStore: BrowserStore,
    private val selectTabUseCase: TabsUseCases.SelectTabUseCase,
): BookmarksController {
    override fun handleBookmarkClicked(bookmark: Bookmark) {
//        TODO("Not yet implemented")
    }

    override fun handleShowAllBookmarksClicked() {
//        TODO("Not yet implemented")
    }

    override fun handleBookmarkRemoved(bookmark: Bookmark) {
//        TODO("Not yet implemented")
    }

}