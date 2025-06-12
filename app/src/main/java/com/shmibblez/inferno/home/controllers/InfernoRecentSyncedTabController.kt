package com.shmibblez.inferno.home.controllers

import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.home.recentsyncedtabs.RecentSyncedTab
import com.shmibblez.inferno.home.recentsyncedtabs.controller.DefaultRecentSyncedTabController
import com.shmibblez.inferno.home.recentsyncedtabs.controller.RecentSyncedTabController
import com.shmibblez.inferno.tabstray.TabsTrayAccessPoint
import mozilla.components.feature.tabs.TabsUseCases

/**
 * todo: reference [DefaultRecentSyncedTabController]
 */
class InfernoRecentSyncedTabController(
    private val tabsUseCase: TabsUseCases,
    private val accessPoint: TabsTrayAccessPoint,
    private val appStore: AppStore,
): RecentSyncedTabController {
    override fun handleRecentSyncedTabClick(tab: RecentSyncedTab) {
//        TODO("Not yet implemented")
    }

    override fun handleSyncedTabShowAllClicked() {
//        TODO("Not yet implemented")
    }

    override fun handleRecentSyncedTabRemoved(tab: RecentSyncedTab) {
//        TODO("Not yet implemented")
    }
}