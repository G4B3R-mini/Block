package com.shmibblez.inferno.home.controllers

import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.home.recenttabs.RecentTab
import com.shmibblez.inferno.home.recenttabs.controller.DefaultRecentTabsController
import com.shmibblez.inferno.home.recenttabs.controller.RecentTabController
import mozilla.components.feature.tabs.TabsUseCases.SelectTabUseCase

/**
 * todo: reference [DefaultRecentTabsController]
 */
class InfernoRecentTabController(
    private val selectTabUseCase: SelectTabUseCase,
    private val appStore: AppStore,
): RecentTabController {
    override fun handleRecentTabClicked(tabId: String) {
//        TODO("Not yet implemented")
    }

    override fun handleRecentTabShowAllClicked() {
//        TODO("Not yet implemented")
    }

    override fun handleRecentTabRemoved(tab: RecentTab.Tab) {
//        TODO("Not yet implemented")
    }
}