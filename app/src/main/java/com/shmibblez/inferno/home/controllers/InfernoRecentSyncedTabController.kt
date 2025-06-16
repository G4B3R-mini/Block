package com.shmibblez.inferno.home.controllers

import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.components.appstate.AppAction
import com.shmibblez.inferno.home.recentsyncedtabs.RecentSyncedTab
import com.shmibblez.inferno.home.recentsyncedtabs.controller.DefaultRecentSyncedTabController
import com.shmibblez.inferno.home.recentsyncedtabs.controller.RecentSyncedTabController
import com.shmibblez.inferno.tabs.tabstray.InfernoTabsTraySelectedTab
import mozilla.components.feature.tabs.TabsUseCases

/**
 * based off [DefaultRecentSyncedTabController]
 */
class InfernoRecentSyncedTabController(
    private val tabsUseCase: TabsUseCases,
    private val appStore: AppStore,
    private val onShowTabsTray: (InfernoTabsTraySelectedTab) -> Unit,
): RecentSyncedTabController {
    override fun handleRecentSyncedTabClick(tab: RecentSyncedTab) {
        // todo: synced tabs (handle selected correctly, load first (see what commented line does))
        //  could just test first and see what happens
//        RecentSyncedTabs.recentSyncedTabOpened[tab.deviceType.name.lowercase()].add()
        tabsUseCase.selectOrAddTab(tab.url)
    }


    override fun handleSyncedTabShowAllClicked() {
        onShowTabsTray.invoke(InfernoTabsTraySelectedTab.SyncedTabs)
    }

    override fun handleRecentSyncedTabRemoved(tab: RecentSyncedTab) {
        appStore.dispatch(AppAction.RemoveRecentSyncedTab(tab))
    }
}