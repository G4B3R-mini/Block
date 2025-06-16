package com.shmibblez.inferno.home.controllers

import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.components.appstate.AppAction
import com.shmibblez.inferno.home.recentvisits.RecentlyVisitedItem.RecentHistoryHighlight
import com.shmibblez.inferno.home.recentvisits.RecentlyVisitedItem.RecentHistoryGroup
import com.shmibblez.inferno.home.recentvisits.controller.DefaultRecentVisitsController
import com.shmibblez.inferno.home.recentvisits.controller.RecentVisitsController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mozilla.components.browser.state.action.HistoryMetadataAction
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.storage.HistoryMetadataStorage
import mozilla.components.feature.tabs.TabsUseCases.SelectOrAddUseCase

/**
 * based off [DefaultRecentVisitsController]
 */
class InfernoRecentVisitsController(
    private val store: BrowserStore,
    private val appStore: AppStore,
    private val selectOrAddTabUseCase: SelectOrAddUseCase,
    private val storage: HistoryMetadataStorage,
    private val scope: CoroutineScope,
    private val onNavToHistory: () -> Unit
): RecentVisitsController {
    override fun handleHistoryShowAllClicked() {
         onNavToHistory.invoke()
    }

    /**
     * Navigates to the history metadata group page to display the group.
     *
     * currently just goes to history
     * todo: history, needs to nav to group specifically, need to implement through search term param to history page
     *  could also show group in dialog
     *
     * @param recentHistoryGroup The [RecentHistoryGroup] to which to navigate to.
     */
    override fun handleRecentHistoryGroupClicked(recentHistoryGroup: RecentHistoryGroup) {
        onNavToHistory.invoke()
    }

    /**
     * Removes a [RecentHistoryGroup] with the given title from the homescreen.
     *
     * @param groupTitle The title of the [RecentHistoryGroup] to be removed.
     */
    override fun handleRemoveRecentHistoryGroup(groupTitle: String) {
        // We want to update the UI right away in response to user action without waiting for the IO.
        // First, dispatch actions that will clean up search groups in the two stores that have
        // metadata-related state.
        store.dispatch(HistoryMetadataAction.DisbandSearchGroupAction(searchTerm = groupTitle))
        appStore.dispatch(AppAction.DisbandSearchGroupAction(searchTerm = groupTitle))
        // Then, perform the expensive IO work of removing search groups from storage.
        scope.launch {
            storage.deleteHistoryMetadata(groupTitle)
        }    }

    /**
     * Switch to an already open tab for [recentHistoryHighlight] if one exists or
     * create a new tab in which to load this item's URL.
     *
     * @param recentHistoryHighlight the just clicked [RecentHistoryHighlight] to open in browser.
     */
    override fun handleRecentHistoryHighlightClicked(recentHistoryHighlight: RecentHistoryHighlight) {
        selectOrAddTabUseCase.invoke(recentHistoryHighlight.url)
    }

    /**
     * Removes a [RecentHistoryHighlight] with the given title from the homescreen.
     *
     * @param highlightUrl The title of the [RecentHistoryHighlight] to be removed.
     */
    override fun handleRemoveRecentHistoryHighlight(highlightUrl: String) {
        appStore.dispatch(AppAction.RemoveRecentHistoryHighlight(highlightUrl))
        scope.launch {
            storage.deleteHistoryMetadataForUrl(highlightUrl)
        }    }

}