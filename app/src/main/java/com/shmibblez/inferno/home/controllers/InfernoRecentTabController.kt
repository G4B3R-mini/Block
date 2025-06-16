package com.shmibblez.inferno.home.controllers

import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.components.appstate.AppAction
import com.shmibblez.inferno.home.recenttabs.RecentTab
import com.shmibblez.inferno.home.recenttabs.controller.DefaultRecentTabsController
import com.shmibblez.inferno.home.recenttabs.controller.RecentTabController
import com.shmibblez.inferno.tabs.tabstray.InfernoTabsTraySelectedTab
import mozilla.components.feature.tabs.TabsUseCases.SelectTabUseCase

/**
 * based off [DefaultRecentTabsController]
 */
class InfernoRecentTabController(
    private val selectTabUseCase: SelectTabUseCase,
    private val appStore: AppStore,
    private val onShowTabsTray: (InfernoTabsTraySelectedTab) -> Unit,
): RecentTabController {
    override fun handleRecentTabClicked(tabId: String) {
        selectTabUseCase.invoke(tabId)
    }

    override fun handleRecentTabShowAllClicked() {
        onShowTabsTray.invoke(InfernoTabsTraySelectedTab.NormalTabs)
    }

    override fun handleRecentTabRemoved(tab: RecentTab.Tab) {
        appStore.dispatch(AppAction.RemoveRecentTab(tab))
    }
}