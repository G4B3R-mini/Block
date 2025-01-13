/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.home.recenttabs.controller

import androidx.navigation.NavController
import mozilla.components.feature.tabs.TabsUseCases.SelectTabUseCase
//import mozilla.telemetry.glean.private.NoExtras
//import com.shmibblez.inferno.GleanMetrics.RecentTabs
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.components.appstate.AppAction
import com.shmibblez.inferno.home.HomeFragment
import com.shmibblez.inferno.home.HomeFragmentDirections
import com.shmibblez.inferno.home.recenttabs.RecentTab
import com.shmibblez.inferno.home.recenttabs.interactor.RecentTabInteractor

/**
 * An interface that handles the view manipulation of the recent tabs in the Home screen.
 */
interface RecentTabController {

    /**
     * @see [RecentTabInteractor.onRecentTabClicked]
     */
    fun handleRecentTabClicked(tabId: String)

    /**
     * @see [RecentTabInteractor.onRecentTabShowAllClicked]
     */
    fun handleRecentTabShowAllClicked()

    /**
     * @see [RecentTabInteractor.onRemoveRecentTab]
     */
    fun handleRecentTabRemoved(tab: RecentTab.Tab)
}

/**
 * The default implementation of [RecentTabController].
 *
 * @param selectTabUseCase [SelectTabUseCase] used selecting a tab.
 * @param navController [NavController] used for navigation.
 * @param appStore The [AppStore] that holds the state of the [HomeFragment].
 */
class DefaultRecentTabsController(
    private val selectTabUseCase: SelectTabUseCase,
    private val navController: NavController,
    private val appStore: AppStore,
) : RecentTabController {

    override fun handleRecentTabClicked(tabId: String) {
//        RecentTabs.recentTabOpened.record(NoExtras())

        selectTabUseCase.invoke(tabId)
        navController.navigate(R.id.browserFragment)
    }

    override fun handleRecentTabShowAllClicked() {
//        RecentTabs.showAllClicked.record(NoExtras())
        navController.navigate(HomeFragmentDirections.actionGlobalTabsTrayFragment())
    }

    override fun handleRecentTabRemoved(tab: RecentTab.Tab) {
        appStore.dispatch(AppAction.RemoveRecentTab(tab))
    }
}
