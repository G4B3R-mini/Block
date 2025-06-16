package com.shmibblez.inferno.home.controllers

import com.shmibblez.inferno.search.toolbar.DefaultSearchSelectorController
import com.shmibblez.inferno.search.toolbar.SearchSelectorController
import com.shmibblez.inferno.search.toolbar.SearchSelectorMenu

/**
 * based off [DefaultSearchSelectorController]
 */
class InfernoSearchSelectorController(
    private val onNavToSearchSettings: () -> Unit,
) : SearchSelectorController {

    /**
     * Goes to search settings page
     *
     * todo: search settings page param to include search engine id below
     */
    override fun handleMenuItemTapped(item: SearchSelectorMenu.Item) {
        when (item) {
            SearchSelectorMenu.Item.SearchSettings -> {
                onNavToSearchSettings.invoke()
//                navController.nav(
//                    R.id.homeFragment,
//                    NavGraphDirections.actionGlobalSearchEngineFragment(),
//                )
            }

            is SearchSelectorMenu.Item.SearchEngine -> {
                onNavToSearchSettings.invoke()
//                val directions = NavGraphDirections.actionGlobalSearchDialog(
//                    sessionId = null,
//                    searchEngine = item.searchEngine.id,
//                )
//                navController.nav(
//                    R.id.homeFragment,
//                    directions,
//                    BrowserAnimator.getToolbarNavOptions(activity),
//                )
            }
        }
    }
}