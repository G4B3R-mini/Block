/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.settings.logins.controller

import androidx.navigation.NavController
import mozilla.telemetry.glean.private.NoExtras
import com.shmibblez.inferno.BrowserDirection
import com.shmibblez.inferno.GleanMetrics.Logins
import com.shmibblez.inferno.settings.SupportUtils
import com.shmibblez.inferno.settings.logins.LoginsAction
import com.shmibblez.inferno.settings.logins.LoginsFragmentStore
import com.shmibblez.inferno.settings.logins.SavedLogin
import com.shmibblez.inferno.settings.logins.SortingStrategy
import com.shmibblez.inferno.settings.logins.fragment.SavedLoginsFragmentDirections
import com.shmibblez.inferno.utils.Settings

/**
 * Controller for the saved logins list
 *
 * @param loginsFragmentStore Store used to hold in-memory collection state.
 * @param navController NavController manages app navigation within a NavHost.
 * @param browserNavigator Controller allowing browser navigation to any Uri.
 * @param addLoginCallback Callback used for add login
 * @param settings SharedPreferences wrapper for easier usage.
 */
class LoginsListController(
    private val loginsFragmentStore: LoginsFragmentStore,
    private val navController: NavController,
    private val browserNavigator: (
        searchTermOrURL: String,
        newTab: Boolean,
        from: BrowserDirection,
    ) -> Unit,
    private val addLoginCallback: () -> Unit,
    private val settings: Settings,
) {

    fun handleItemClicked(item: SavedLogin) {
        Logins.managementLoginsTapped.record(NoExtras())
        loginsFragmentStore.dispatch(LoginsAction.LoginSelected(item))
        Logins.openIndividualLogin.record(NoExtras())
        navController.navigate(
            SavedLoginsFragmentDirections.actionSavedLoginsFragmentToLoginDetailFragment(item.guid),
        )
    }

    fun handleAddLoginClicked() {
        Logins.managementAddTapped.record(NoExtras())
        addLoginCallback.invoke()
        navController.navigate(
            SavedLoginsFragmentDirections.actionSavedLoginsFragmentToAddLoginFragment(),
        )
    }

    fun handleLearnMoreClicked() {
        browserNavigator.invoke(
            SupportUtils.getGenericSumoURLForTopic(SupportUtils.SumoTopic.SYNC_SETUP),
            true,
            BrowserDirection.FromSavedLoginsFragment,
        )
    }

    fun handleSort(sortingStrategy: SortingStrategy) {
        loginsFragmentStore.dispatch(
            LoginsAction.SortLogins(
                sortingStrategy,
            ),
        )
        settings.savedLoginsSortingStrategy = sortingStrategy
    }
}
