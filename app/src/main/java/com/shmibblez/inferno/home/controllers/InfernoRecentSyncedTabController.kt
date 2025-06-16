package com.shmibblez.inferno.home.controllers

import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.components.appstate.AppAction
import com.shmibblez.inferno.ext.findExistingTabFromUrl
import com.shmibblez.inferno.home.recentsyncedtabs.RecentSyncedTab
import com.shmibblez.inferno.home.recentsyncedtabs.controller.DefaultRecentSyncedTabController
import com.shmibblez.inferno.home.recentsyncedtabs.controller.RecentSyncedTabController
import com.shmibblez.inferno.tabs.tabstray.InfernoTabsTraySelectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.TabsUseCases

/**
 * based off [DefaultRecentSyncedTabController]
 */
class InfernoRecentSyncedTabController(
    private val appStore: AppStore,
    private val browserStore: BrowserStore,
    private val onShowTabsTray: (InfernoTabsTraySelectedTab) -> Unit,
    private val loadUrlUseCase: SessionUseCases.LoadUrlUseCase,
    private val selectTabUseCase: TabsUseCases.SelectTabUseCase,
): RecentSyncedTabController {
    override fun handleRecentSyncedTabClick(tab: RecentSyncedTab) {
        // todo: synced tabs (handle selected correctly, load first (see what commented line does))
        //  could just test first and see what happens
//        RecentSyncedTabs.recentSyncedTabOpened[tab.deviceType.name.lowercase()].add()

        // old:
//        tabsUseCase.selectOrAddTab(tab.url)

        val existingTab = browserStore.findExistingTabFromUrl(tab.url)

        if (existingTab == null) {
            loadUrlUseCase.invoke(
                url = tab.url
            )
        } else {
            selectTabUseCase.invoke(existingTab.content.url)
        }
    }


    override fun handleSyncedTabShowAllClicked() {
        onShowTabsTray.invoke(InfernoTabsTraySelectedTab.SyncedTabs)
    }

    override fun handleRecentSyncedTabRemoved(tab: RecentSyncedTab) {
        appStore.dispatch(AppAction.RemoveRecentSyncedTab(tab))
    }
}