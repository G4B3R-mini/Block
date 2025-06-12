package com.shmibblez.inferno.home.controllers

import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.home.recentvisits.RecentlyVisitedItem
import com.shmibblez.inferno.home.recentvisits.controller.DefaultRecentVisitsController
import com.shmibblez.inferno.home.recentvisits.controller.RecentVisitsController
import kotlinx.coroutines.CoroutineScope
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.storage.HistoryMetadataStorage
import mozilla.components.feature.tabs.TabsUseCases.SelectOrAddUseCase

/**
 * todo: reference [DefaultRecentVisitsController]
 */
class InfernoRecentVisitsController(
    private val store: BrowserStore,
    private val appStore: AppStore,
    private val selectOrAddTabUseCase: SelectOrAddUseCase,
    private val storage: HistoryMetadataStorage,
    private val scope: CoroutineScope,
): RecentVisitsController {
    override fun handleHistoryShowAllClicked() {
        // TODO("Not yet implemented")
    }

    override fun handleRecentHistoryGroupClicked(recentHistoryGroup: RecentlyVisitedItem.RecentHistoryGroup) {
        // TODO("Not yet implemented")
    }

    override fun handleRemoveRecentHistoryGroup(groupTitle: String) {
        // TODO("Not yet implemented")
    }

    override fun handleRecentHistoryHighlightClicked(recentHistoryHighlight: RecentlyVisitedItem.RecentHistoryHighlight) {
        // TODO("Not yet implemented")
    }

    override fun handleRemoveRecentHistoryHighlight(highlightUrl: String) {
        // TODO("Not yet implemented")
    }

}