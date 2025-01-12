/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.search.toolbar

import androidx.navigation.NavController
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.NavGraphDirections
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.BrowserAnimator
import com.shmibblez.inferno.ext.nav

/**
 * An interface that handles the view manipulation of the search selector menu.
 */
interface SearchSelectorController {
    /**
     * @see [SearchSelectorInteractor.onMenuItemTapped]
     */
    fun handleMenuItemTapped(item: SearchSelectorMenu.Item)
}

/**
 * The default implementation of [SearchSelectorController].
 */
class DefaultSearchSelectorController(
    private val activity: HomeActivity,
    private val navController: NavController,
) : SearchSelectorController {

    override fun handleMenuItemTapped(item: SearchSelectorMenu.Item) {
        when (item) {
            SearchSelectorMenu.Item.SearchSettings -> {
                navController.nav(
                    R.id.homeFragment,
                    NavGraphDirections.actionGlobalSearchEngineFragment(),
                )
            }

            is SearchSelectorMenu.Item.SearchEngine -> {
                val directions = NavGraphDirections.actionGlobalSearchDialog(
                    sessionId = null,
                    searchEngine = item.searchEngine.id,
                )
                navController.nav(
                    R.id.homeFragment,
                    directions,
                    BrowserAnimator.getToolbarNavOptions(activity),
                )
            }
        }
    }
}
