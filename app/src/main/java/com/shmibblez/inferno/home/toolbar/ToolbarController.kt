/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.home.toolbar

import androidx.navigation.NavController
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.support.ktx.kotlin.isUrl
import com.shmibblez.inferno.BrowserDirection
//import com.shmibblez.inferno.GleanMetrics.Events
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.NavGraphDirections
import com.shmibblez.inferno.browser.BrowserAnimator
//import com.shmibblez.inferno.components.metrics.MetricsUtils
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.nav

/**
 * An interface that handles the view manipulation of the home screen toolbar.
 */
interface ToolbarController {
    /**
     * @see [ToolbarInteractor.onPasteAndGo]
     */
    fun handlePasteAndGo(clipboardText: String)

    /**
     * @see [ToolbarInteractor.onPaste]
     */
    fun handlePaste(clipboardText: String)

    /**
     * @see [ToolbarInteractor.onNavigateSearch]
     */
    fun handleNavigateSearch()
}

/**
 * The default implementation of [ToolbarController].
 */
class DefaultToolbarController(
    private val activity: HomeActivity,
    private val store: BrowserStore,
    private val navController: NavController,
) : ToolbarController {
    override fun handlePasteAndGo(clipboardText: String) {
        val searchEngine = store.state.search.selectedOrDefaultSearchEngine

        activity.openToBrowserAndLoad(
            searchTermOrURL = clipboardText,
            newTab = true,
            from = BrowserDirection.FromHome,
            engine = searchEngine,
        )

//        if (clipboardText.isUrl() || searchEngine == null) {
////            Events.enteredUrl.record(Events.EnteredUrlExtra(autocomplete = false))
//        } else {
//            val searchAccessPoint = MetricsUtils.Source.ACTION
//            MetricsUtils.recordSearchMetrics(
//                searchEngine,
//                searchEngine == store.state.search.selectedOrDefaultSearchEngine,
//                searchAccessPoint,
//                activity.components.nimbus.events,
//            )
//        }
    }
    override fun handlePaste(clipboardText: String) {
        val directions = NavGraphDirections.actionGlobalSearchDialog(
            sessionId = null,
            pastedText = clipboardText,
        )
        navController.nav(navController.currentDestination?.id, directions)
    }

    override fun handleNavigateSearch() {
        val directions =
            NavGraphDirections.actionGlobalSearchDialog(
                sessionId = null,
            )

        navController.nav(
            navController.currentDestination?.id,
            directions,
            BrowserAnimator.getToolbarNavOptions(activity),
        )

//        Events.searchBarTapped.record(Events.SearchBarTappedExtra("HOME"))
    }
}
